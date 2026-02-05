package com.example.primitivela

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun ScannerScreen(
    eventId: Int,
    session: String,
    dao: AttendanceDao,
    onIdScanned: (String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val scope = rememberCoroutineScope()

    var lastScannedValue by remember { mutableStateOf<String?>(null) }
    var isPaused by remember { mutableStateOf(false) }
    var scanMessage by remember { mutableStateOf<String?>(null) }
    var isValidScan by remember { mutableStateOf(false) }
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var studentToAdd by remember { mutableStateOf("") }

    // FORCE ML KIT TO LOOK FOR ALL BARCODE TYPES (1D IDs & QR)
    val options = remember {
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
    }
    val scanner = remember { BarcodeScanning.getClient(options) }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Camera Viewfinder
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = androidx.camera.lifecycle.ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        // Using '1' directly fixes the "Unresolved reference" red line
                        .setBackpressureStrategy(1)
                        .build()

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        if (!isPaused) {
                            processImageProxy(scanner, imageProxy) { result ->
                                scope.launch {
                                    lastScannedValue = result
                                    
                                    // Check if student exists in CSV data
                                    val student = dao.getStudent(eventId, result)
                                    if (student != null) {
                                        val isAlreadyPresent = when (session) {
                                            "morning" -> student.morningPresent
                                            "afternoon" -> student.afternoonPresent
                                            "evening" -> student.eveningPresent
                                            else -> false
                                        }
                                        if (isAlreadyPresent) {
                                            scanMessage = "${student.name} already marked present for $session"
                                            isValidScan = false
                                        } else {
                                            scanMessage = "Thank you ${student.name}\n$session attendance recorded"
                                            isValidScan = true
                                        }
                                    } else {
                                        // Check if event has CSV data
                                        val students = dao.getStudentsForEvent(eventId)
                                        if (students.isNotEmpty()) {
                                            studentToAdd = result
                                            showAddStudentDialog = true
                                            isValidScan = false
                                        } else {
                                            // Regular scanning mode
                                            scanMessage = null
                                            isValidScan = true
                                        }
                                    }
                                    isPaused = true
                                }
                            }
                        } else {
                            imageProxy.close()
                        }
                    }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis
                        )
                    } catch (e: Exception) {
                        Log.e("Scanner", "Binding failed", e)
                    }
                }, androidx.core.content.ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Result Overlay (Confirmation)
        if (isPaused && lastScannedValue != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(24.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (scanMessage != null) {
                            Text(
                                text = scanMessage!!,
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                color = if (isValidScan) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        } else {
                            Text("Scanned ID:", style = MaterialTheme.typography.labelLarge)
                            Text(
                                text = lastScannedValue!!,
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedButton(onClick = {
                                lastScannedValue = null
                                scanMessage = null
                                isPaused = false
                            }) {
                                Text("Retry")
                            }
                            if (isValidScan) {
                                Button(onClick = {
                                    onIdScanned(lastScannedValue!!)
                                    lastScannedValue = null
                                    scanMessage = null
                                    isPaused = false
                                }) {
                                    Text("Next")
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Back Button, Session Indicator, and Refresh Button
        Column(
            modifier = Modifier.padding(top = 40.dp, start = 16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onCancel) {
                    Surface(color = Color.Black.copy(alpha = 0.5f), shape = MaterialTheme.shapes.small) {
                        Text(
                            " Back ",
                            color = Color.White,
                            modifier = Modifier.padding(4.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                IconButton(onClick = { 
                    lastScannedValue = null
                    scanMessage = null
                    isPaused = false
                }) {
                    Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), shape = MaterialTheme.shapes.small) {
                        Text(
                            " Refresh ",
                            color = Color.White,
                            modifier = Modifier.padding(4.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    " ${session.capitalize()} Session ",
                    color = Color.White,
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    onResult: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let {
                        if (it.isNotBlank()) onResult(it)
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}