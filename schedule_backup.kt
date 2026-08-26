package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FocusSession
import com.example.ui.theme.*
import com.example.ui.viewmodel.FocusViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ScheduleMainScreen(
    viewModel: FocusViewModel,
    onNavigateToCreate: () -> Unit,
    onStartScheduled: (Long) -> Unit = {}
) {
    val scheduledSessions by viewModel.scheduledSessions.collectAsState()

    // Group sessions by Date (yyyy-MM-dd)
    val groupedSessions = remember(scheduledSessions) {
        val groupDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        scheduledSessions.groupBy { session ->
            val time = session.scheduledStartTime ?: session.timestamp
            groupDateFormat.format(Date(time))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground)
            .padding(horizontal = 18.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "FOCUS TIMETABLE",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White
                )
                Text(
                    text = "Strict Study Schedules & Alarms",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FocusTextSecondary
                )
            }

            Button(
                onClick = onNavigateToCreate,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FocusWarning,
                    contentColor = Color.Black
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("add_schedule_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Schedule",
                    modifier = Modifier.size(18.dp),
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "ADD",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (scheduledSessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = FocusSurface),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, FocusOutline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(28.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = FocusSurfaceVariant,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = FocusPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No Focus Sessions Scheduled",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Plan ahead! Add study schedules for today, tomorrow, or any custom date with strict lockdown.",
                            style = MaterialTheme.typography.bodySmall,
                            color = FocusTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onNavigateToCreate,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FocusWarning,
                                contentColor = Color.Black
                            ),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.AddAlarm, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "CREATE FIRST SCHEDULE",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black)
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Render each Date Group
                groupedSessions.forEach { (dateKey, sessionsInGroup) ->
                    val firstSessionTime = sessionsInGroup.firstOrNull()?.scheduledStartTime ?: System.currentTimeMillis()
                    val groupDate = Date(firstSessionTime)

                    val todayCal = Calendar.getInstance()
                    val sessionCal = Calendar.getInstance().apply { time = groupDate }

                    val isToday = todayCal.get(Calendar.YEAR) == sessionCal.get(Calendar.YEAR) &&
                            todayCal.get(Calendar.DAY_OF_YEAR) == sessionCal.get(Calendar.DAY_OF_YEAR)

                    val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                    val isTomorrow = tomorrowCal.get(Calendar.YEAR) == sessionCal.get(Calendar.YEAR) &&
                            tomorrowCal.get(Calendar.DAY_OF_YEAR) == sessionCal.get(Calendar.DAY_OF_YEAR)

                    val headerDateLabel = when {
                        isToday -> "🌟 TODAY • ${SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(groupDate).uppercase()}"
                        isTomorrow -> "🚀 TOMORROW • ${SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(groupDate).uppercase()}"
                        else -> "📅 ${SimpleDateFormat("EEEE • d MMMM yyyy", Locale.getDefault()).format(groupDate).uppercase()}"
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isToday) FocusPrimary.copy(alpha = 0.12f) else FocusSurfaceVariant,
                            border = BorderStroke(1.dp, if (isToday) FocusPrimary.copy(alpha = 0.4f) else FocusOutline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = headerDateLabel,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isToday) FocusPrimary else Color.White
                                )

                                Text(
                                    text = "${sessionsInGroup.size} ${if (sessionsInGroup.size == 1) "Session" else "Sessions"}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = FocusTextSecondary
                                )
                            }
                        }
                    }

                    items(sessionsInGroup) { session ->
                        val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
                        val startTimeStr = session.scheduledStartTime?.let { timeFormatter.format(Date(it)) } ?: ""
                        val endTimeStr = session.scheduledEndTime?.let { timeFormatter.format(Date(it)) } ?: ""

                        Card(
                            colors = CardDefaults.cardColors(containerColor = FocusSurface),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, FocusOutline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "📚 ${session.subjectName.uppercase()}",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteScheduledSession(session) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete",
                                            tint = FocusWarning.copy(alpha = 0.8f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                if (session.sessionName.isNotBlank() && session.sessionName != session.subjectName) {
                                    Text(
                                        text = session.sessionName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = FocusTextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Time Range & Duration
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = FocusPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$startTimeStr – $endTimeStr",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = FocusSurfaceVariant
                                    ) {
                                        Text(
                                            text = "${session.targetDurationMinutes} mins",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = FocusPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Badges and Start Button Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = FocusWarning.copy(alpha = 0.15f)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = null,
                                                    tint = FocusWarning,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = session.lockMode.split("_").first(),
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = FocusWarning
                                                )
                                            }
                                        }

                                        if (session.requiresPhoto) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = FocusAccent.copy(alpha = 0.15f)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CameraAlt,
                                                        contentDescription = null,
                                                        tint = FocusAccent,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "PHOTO",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = FocusAccent
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    FilledTonalButton(
                                        onClick = {
                                            viewModel.loadScheduledSession(session.id)
                                            onStartScheduled(session.id)
                                        },
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = FocusWarning.copy(alpha = 0.2f),
                                            contentColor = FocusWarning
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "START NOW",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}
