package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FocusSession
import com.example.ui.theme.*
import com.example.ui.viewmodel.FocusViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleMainScreen(
    viewModel: FocusViewModel,
    onNavigateToCreate: () -> Unit,
    onStartScheduled: (FocusSession) -> Unit,
    onNavigateToSessionRouter: (Long) -> Unit
) {
    val scheduledSessions by viewModel.scheduledSessions.collectAsState(initial = emptyList())
    val allSessions by viewModel.allSessions.collectAsState(initial = emptyList())
    val historySessions = allSessions.filter { it.scheduledStartTime != null && it.status == "COMPLETED" }.sortedByDescending { it.timestamp }
    
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Timetable",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White
                    )
                },
                actions = {
                    Button(
                        onClick = onNavigateToCreate,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)), // Bright Blue
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("NEW SCHEDULE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FocusBackground,
                    titleContentColor = Color.White
                )
            )
        },

        containerColor = FocusBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = FocusBackground,
                contentColor = FocusPrimary,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = FocusPrimary
                        )
                    }
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("UPCOMING", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("HISTORY", fontWeight = FontWeight.Bold) }
                )
            }

            if (selectedTabIndex == 0) {
                if (scheduledSessions.isEmpty()) {
                    EmptyScheduleState(onNavigateToCreate)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(scheduledSessions, key = { it.id }) { session ->
                            ScheduleCard(
                                session = session,
                                isHistory = false,
                                onStart = {
                                    onNavigateToSessionRouter(session.id)
                                },
                                onDelete = { viewModel.deleteScheduledSession(session) }
                            )
                        }
                    }
                }
            } else {
                if (historySessions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No schedule history found", color = FocusTextSecondary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(historySessions, key = { it.id }) { session ->
                            ScheduleCard(
                                session = session,
                                isHistory = true,
                                onStart = { },
                                onDelete = { viewModel.deleteScheduledSession(session) } // Reuse delete logic
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyScheduleState(onCreateClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            tint = FocusOutline,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No Upcoming Sessions",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Create a disciplined schedule to enforce focus at specific times.",
            style = MaterialTheme.typography.bodyMedium,
            color = FocusTextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onCreateClick,
            colors = ButtonDefaults.buttonColors(containerColor = FocusSurfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("CREATE SCHEDULE", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ScheduleCard(
    session: FocusSession,
    isHistory: Boolean,
    onStart: () -> Unit,
    onDelete: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("EEE, d MMM", Locale.getDefault()) }
    
    val timeString = session.scheduledStartTime?.let { formatter.format(Date(it)) } ?: "N/A"
    val dateString = session.scheduledStartTime?.let { dateFormatter.format(Date(it)) } ?: ""
    val durationString = "${session.targetDurationMinutes} min"

    val isPending = session.scheduledStartTime?.let { it < System.currentTimeMillis() } == true

    Card(
        colors = CardDefaults.cardColors(containerColor = FocusSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FocusOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (isHistory) FocusTextSecondary else if (isPending) MaterialTheme.colorScheme.error else FocusPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$dateString • $timeString",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isHistory) FocusTextSecondary else if (isPending) MaterialTheme.colorScheme.error else FocusPrimary
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isHistory) FocusSurfaceVariant else if (isPending) MaterialTheme.colorScheme.error.copy(alpha = 0.2f) else FocusPrimary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (isHistory) "COMPLETED" else if (isPending) "PENDING" else "UPCOMING",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Black),
                        color = if (isHistory) FocusTextSecondary else if (isPending) MaterialTheme.colorScheme.error else FocusPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body
            Text(
                text = session.subjectName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${session.sessionName} • $durationString",
                style = MaterialTheme.typography.bodyMedium,
                color = FocusTextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lock Mode Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = FocusWarning,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${session.lockMode} Mode Enforced",
                    style = MaterialTheme.typography.labelSmall,
                    color = FocusWarning
                )
                if (session.requiresSelfie) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "• Selfie Required",
                        style = MaterialTheme.typography.labelSmall,
                        color = FocusWarning
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions
            if (!isHistory) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("CANCEL", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onStart,
                        colors = ButtonDefaults.buttonColors(containerColor = FocusPrimary),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("START", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("DELETE HISTORY", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
