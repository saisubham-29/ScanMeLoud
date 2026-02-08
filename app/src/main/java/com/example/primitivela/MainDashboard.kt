package com.example.primitivela

import android.R.color.white
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(
    events: List<Event>,
    onCreateEvent: (String, String) -> Unit,
    onEventClick: (Event, String) -> Unit,
    onExportClick: (Event, String) -> Unit,
    onDeleteClick: (Event) -> Unit,
    onViewStudentsClick: (Event) -> Unit,
    onCreateCsvEvent: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var showCsvDialog by remember { mutableStateOf(false) }
    var eventName by remember { mutableStateOf("") }
    var selectedSession by remember { mutableStateOf("morning") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Primitive-LA",
                        style = MaterialTheme.typography.titleLarge.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF121212), // Deep Matte Black
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            Column {
                FloatingActionButton(
                    onClick = { showCsvDialog = true },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Default.MailOutline, contentDescription = "CSV Event")
                }
                FloatingActionButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "New Event")
                }
            }
        }
    ) { padding ->
        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No events yet. Tap + to start.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                // contentPadding adds space at the top/bottom of the whole list
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                // verticalArrangement adds a gap between each individual card
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(events) { event ->
                    EventItem(
                        event = event,
                        onClick = { session -> onEventClick(event, session) },
                        onExport = { format -> onExportClick(event, format) },
                        onDelete = { onDeleteClick(event) },
                        onViewStudents = { onViewStudentsClick(event) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    DeveloperCreditCard()
                    Spacer(modifier = Modifier.height(24.dp))
                }

            }
        }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Create New Event") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = eventName,
                            onValueChange = { eventName = it },
                            label = { Text("Event Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Select Session:", style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("morning", "afternoon", "evening").forEach { session ->
                                FilterChip(
                                    onClick = { selectedSession = session },
                                    label = { Text(session.capitalize()) },
                                    selected = selectedSession == session
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (eventName.isNotBlank()) {
                            onCreateEvent(eventName, selectedSession)
                            eventName = ""
                            selectedSession = "morning"
                            showDialog = false
                        }
                    }) { Text("Start Scanning") }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                }
            )
        }

        if (showCsvDialog) {
            AlertDialog(
                onDismissRequest = { showCsvDialog = false },
                title = { Text("Create CSV Event") },
                text = {
                    OutlinedTextField(
                        value = eventName,
                        onValueChange = { eventName = it },
                        label = { Text("Event Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        if (eventName.isNotBlank()) {
                            onCreateCsvEvent(eventName)
                            eventName = ""
                            showCsvDialog = false
                        }
                    }) { Text("Upload CSV") }
                },
                dismissButton = {
                    TextButton(onClick = { showCsvDialog = false }) { Text("Cancel") }
                }
            )
        }
    }

@Composable
fun EventItem(
    event: Event,
    onClick: (String) -> Unit,
    onExport: (String) -> Unit,
    onDelete: () -> Unit,
    onViewStudents: () -> Unit
) {
    val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(event.createdAt))
    var showMenu by remember { mutableStateOf(false) }
    var showSessionDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { showSessionDialog = true },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = event.name, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "Created: $date",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    // 1. VIEW STUDENTS OPTION
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        text = { Text("View Students") },
                        onClick = {
                            onViewStudents()
                            showMenu = false
                        }
                    )

                    // 2. EXPORT OPTIONS
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                        text = { Text("Export as .CSV") },
                        onClick = {
                            onExport("csv")
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                        text = { Text("Export as .TXT") },
                        onClick = {
                            onExport("txt")
                            showMenu = false
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // 3. DELETE OPTION
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color.Red
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Delete", color = Color.Red)
                            }
                        },
                        onClick = {
                            onDelete()
                            showMenu = false
                        }
                    )
                }
            }
        }
    }

    // Session Selection Dialog
    if (showSessionDialog) {
        AlertDialog(
            onDismissRequest = { showSessionDialog = false },
            title = { Text("Select Session") },
            text = {
                Column {
                    listOf("morning", "afternoon", "evening").forEach { session ->
                        TextButton(
                            onClick = {
                                onClick(session)
                                showSessionDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(session.capitalize(), modifier = Modifier.padding(8.dp))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSessionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}



@Composable
fun DeveloperCreditCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFFFFFFF)
                        )
                    )
                )
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Developed by",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Image(
                painter = painterResource(id = R.drawable.lightlogo),
                contentDescription = "Developer Logo",
                modifier = Modifier.size(180.dp), // increased size
                contentScale = ContentScale.Fit
            )
        }
    }
}
