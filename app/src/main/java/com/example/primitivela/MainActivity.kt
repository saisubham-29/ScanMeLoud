package com.example.primitivela

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.primitivela.ui.theme.PrimitiveLATheme
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "attendance-db"
        ).build()
        val dao = db.attendanceDao()

        setContent {
            PrimitiveLATheme {
                val context = LocalContext.current
                var currentScreen by remember { mutableIntStateOf(0) }
                var activeEventId by remember { mutableIntStateOf(-1) }

                // 1. Permission State
                var hasCameraPermission by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                }

                // 2. Permission Launcher
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    hasCameraPermission = isGranted
                    if (!isGranted) {
                        Toast.makeText(context, "Camera permission is required to scan", Toast.LENGTH_LONG).show()
                    }
                }

                val events by dao.getAllEvents().collectAsState(initial = emptyList())

                if (currentScreen == 0) {
                    MainDashboard(
                        events = events,
                        onCreateEvent = { name ->
                            // Check permission before proceeding to Scanner
                            if (hasCameraPermission) {
                                lifecycleScope.launch {
                                    val id = dao.insertEvent(Event(name = name))
                                    activeEventId = id.toInt()
                                    currentScreen = 1
                                }
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        onEventClick = { event ->
                            // Check permission before proceeding to Scanner
                            if (hasCameraPermission) {
                                activeEventId = event.id
                                currentScreen = 1
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        onExportClick = { event, format ->
                            lifecycleScope.launch {
                                val records = dao.getRecordsForEvent(event.id)
                                if (records.isNotEmpty()) {
                                    shareEventData(this@MainActivity, event.name, records, format)
                                } else {
                                    Toast.makeText(this@MainActivity, "No scans to export!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onDeleteClick = { event ->
                            lifecycleScope.launch {
                                dao.deleteEvent(event)
                                Toast.makeText(this@MainActivity, "Event deleted", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                } else {
                    ScannerScreen(
                        onIdScanned = { barcode ->
                            lifecycleScope.launch {
                                dao.insertRecord(AttendanceRecord(eventId = activeEventId, barcodeValue = barcode))
                            }
                        },
                        onCancel = { currentScreen = 0 }
                    )

                    BackHandler { currentScreen = 0 }
                }
            }
        }
    }
}

fun shareEventData(context: Context, eventName: String, records: List<AttendanceRecord>, format: String) {
    val fileName = "${eventName.replace(" ", "_")}.$format"
    val file = File(context.cacheDir, fileName)

    val content = if (format == "csv") {
        "Scanned_ID\n" + records.joinToString("\n") { it.barcodeValue }
    } else {
        records.joinToString("\n") { it.barcodeValue }
    }

    try {
        file.writeText(content)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export Attendance"))
    } catch (e: Exception) {
        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}