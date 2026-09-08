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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.PhotoCamera
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
import androidx.compose.ui.platform.LocalContext
import com.example.util.ScheduleExporter

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
    val context = LocalContext.current

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
                    IconButton(
                        onClick = {
                            if (scheduledSessions.isNotEmpty()) {
                                val calendar = java.util.Calendar.getInstance()
                                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                                calendar.set(java.util.Calendar.MINUTE, 0)
                                calendar.set(java.util.Calendar.SECOND, 0)
                                calendar.set(java.util.Calendar.MILLISECOND, 0)
                                val startOfDay = calendar.timeInMillis
                                calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
                                val endOfDay = calendar.timeInMillis
                                
                                val todaysSessions = scheduledSessions.filter { 
                                    it.scheduledStartTime != null && 
                                    it.scheduledStartTime >= startOfDay && 
                                    it.scheduledStartTime < endOfDay 
                                }
                                
                                if (todaysSessions.isNotEmpty()) {
                                    ScheduleExporter.exportScheduleAsImage(context, todaysSessions)
                                } else {
                                    android.widget.Toast.makeText(context, "No schedule found for today.", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Download Schedule Image", tint = Color.White)
                    }
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
                    text = { Text("UPCOMING (${scheduledSessions.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("HISTORY (${historySessions.size})", fontWeight = FontWeight.Bold) }
                )
            }

            if (selectedTabIndex == 0) {
                if (scheduledSessions.isEmpty()) {
                    EmptyScheduleState(onNavigateToCreate)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
    val dateFormatter = remember { SimpleDateFormat("EEEE, d MMM", Locale.getDefault()) }
    
    val timeString = session.scheduledStartTime?.let { formatter.format(Date(it)) } ?: "N/A"
    val dateString = session.scheduledStartTime?.let { dateFormatter.format(Date(it)) } ?: ""
    val durationString = "${session.targetDurationMinutes} min"
    
    val isPending = session.scheduledStartTime?.let { it < System.currentTimeMillis() } == true

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2633)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(40.dp).padding(end = 12.dp)
                    )
                    Column {
                        Text(
                            text = if (session.sessionName.isNotBlank()) session.sessionName else "Study Session",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$dateString • $timeString",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = durationString,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isHistory) FocusTextSecondary else if (isPending) MaterialTheme.colorScheme.error else FocusPrimary)
                ) {
                    Text(
                        text = if (isHistory) "Completed" else if (isPending) "Pending" else "Upcoming",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isHistory) FocusTextSecondary else if (isPending) MaterialTheme.colorScheme.error else FocusPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            androidx.compose.material3.Divider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))
            
            // Middle section (Features)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Feature 1: Mode
                val isDeepWork = session.lockMode == "MAXIMUM_LOCK"
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (isDeepWork) Icons.Default.Psychology else Icons.Default.Warning,
                        contentDescription = null,
                        tint = FocusWarning,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isDeepWork) "Deep Work Mode" else "Strict Mode",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = FocusWarning
                    )
                }
                
                // Feature 2: Selfie
                if (session.requiresSelfie) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = FocusWarning,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Selfie Required",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = FocusWarning
                        )
                    }
                }
                
                // Feature 3: Cancel Locked
                if (!isHistory) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Cancel Locked",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bottom Button
            if (!isHistory) {
                Button(
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FocusPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = Color.Black, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("START SESSION", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                }
            } else {
                androidx.compose.material3.TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("DELETE HISTORY", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
