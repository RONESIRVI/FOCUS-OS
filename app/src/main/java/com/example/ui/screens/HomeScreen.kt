package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FocusSession
import com.example.ui.dialogs.AppGuideDialog
import com.example.ui.dialogs.NotificationCenterDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.FocusViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: FocusViewModel,
    onNavigateToSetup: () -> Unit,
    onNavigateToScheduleCreate: () -> Unit,
    onNavigateToScheduleMain: () -> Unit,
    onNavigateToAppSelector: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToTimer: () -> Unit
) {
    val stats by viewModel.summaryStats.collectAsState()
    val scheduledSessions by viewModel.scheduledSessions.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()

    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("FocusPrefs", Context.MODE_PRIVATE)
    var userName by remember { mutableStateOf("Focus Student") }
    
    var showAppGuide by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    var dismissedNotificationIds by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(Unit) {
        userName = sharedPrefs.getString("USER_NAME", "Focus Student") ?: "Focus Student"
    }

    // App Guide Dialog (opened via Hamburger Menu 3 lines)
    if (showAppGuide) {
        AppGuideDialog(
            onDismiss = { showAppGuide = false },
            onNavigateToShield = onNavigateToSetup,
            onNavigateToSchedule = onNavigateToScheduleMain,
            onNavigateToAppSelector = onNavigateToAppSelector
        )
    }

    // Notifications Center Dialog (opened via Header Bell Icon)
    if (showNotifications) {
        NotificationCenterDialog(
            scheduledSessions = scheduledSessions,
            summaryStats = stats,
            dismissedIds = dismissedNotificationIds,
            onDismissNotification = { id -> dismissedNotificationIds = dismissedNotificationIds + id },
            onDismiss = { showNotifications = false },
            onStartScheduledSession = { session ->
                viewModel.loadScheduledSession(session.id)
                if (session.requiresPhoto) {
                    onNavigateToTimer() // Will route through flow
                } else {
                    viewModel.startFocusSession()
                    onNavigateToTimer()
                }
            },
            onOpenShield = onNavigateToSetup,
            onOpenSchedule = onNavigateToScheduleMain
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hamburger Menu Icon (3 horizontal lines) -> Opens Full App Guide
                IconButton(
                    onClick = { showAppGuide = true },
                    modifier = Modifier.testTag("hamburger_menu_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "App Complete Guide",
                        tint = FocusTextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "FOCUS OS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    color = FocusPrimary
                )

                // Notification Bell with dynamic badge
                Box(
                    modifier = Modifier.clickable { showNotifications = true }.testTag("header_notifications_btn"),
                    contentAlignment = Alignment.TopEnd
                ) {
                    IconButton(onClick = { showNotifications = true }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications & Alerts",
                            tint = FocusTextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    // Notification Badge
                    val alertCount = (if (scheduledSessions.isNotEmpty()) 1 else 0) + 2 // dynamic alerts
                    Surface(
                        shape = CircleShape,
                        color = FocusWarning,
                        modifier = Modifier
                            .padding(top = 6.dp, end = 6.dp)
                            .size(16.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$alertCount",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                ),
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        // Greeting & Motivation
        item {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    text = "Hi, $userName 👋",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = FocusTextPrimary
                )
                Text(
                    text = "100% Distraction-free Study Environment",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FocusTextSecondary
                )
            }
        }

        // Today's Progress Card
        item {
            val hours = stats.todayFocusSeconds / 3600
            val minutes = (stats.todayFocusSeconds % 3600) / 60
            val goalHours = 5
            val goalProgress = (stats.todayFocusSeconds.toFloat() / (goalHours * 3600f)).coerceIn(0f, 1f)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = FocusSurface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, FocusOutline)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoGraph,
                                contentDescription = null,
                                tint = FocusPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "TODAY'S PROGRESS",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = FocusTextSecondary
                            )
                        }
                        
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FocusSurfaceVariant
                        ) {
                            Text(
                                text = "${(goalProgress * 100).toInt()}% Goal",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (goalProgress >= 1f) FocusPrimary else FocusWarning,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Text(
                        text = "${hours}h ${minutes}m",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = FocusTextPrimary
                    )
                    Text(
                        text = "Real Study Focus Time Today",
                        style = MaterialTheme.typography.bodySmall,
                        color = FocusTextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Completed Sessions", style = MaterialTheme.typography.labelSmall, color = FocusTextSecondary)
                            Text("${stats.totalSessions}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = FocusTextPrimary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Focus Score", style = MaterialTheme.typography.labelSmall, color = FocusTextSecondary)
                            Text("${stats.focusScore}%", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = FocusPrimary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Daily Goal", style = MaterialTheme.typography.labelSmall, color = FocusTextSecondary)
                            Text("${goalHours}h", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = FocusTextPrimary)
                        }
                    }
                }
            }
        }

        // Focus Streak Card (Real Data & Dynamic Progress)
        item {
            val goalProgress = (stats.todayFocusSeconds.toFloat() / (5 * 3600f)).coerceIn(0f, 1f)
            val streakDays = stats.currentStreakDays
            val hours = stats.todayFocusSeconds / 3600
            val minutes = (stats.todayFocusSeconds % 3600) / 60

            val motivationalMessage = remember(streakDays, goalProgress, stats.todayFocusSeconds) {
                when {
                    goalProgress >= 1.0f -> "🏆 5-Hour Daily Study Goal Achieved! Phenomenal discipline!"
                    streakDays > 1 -> "🔥 $streakDays-Day streak active! Keep going strong."
                    streakDays == 1 -> "Day 1 accomplished! Keep the momentum burning tomorrow."
                    stats.todayFocusSeconds > 0 -> "Great start today (${hours}h ${minutes}m)! Finish sessions to build your daily streak."
                    else -> "No active streak yet. Complete a study session today to start your streak!"
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().testTag("streak_card"),
                colors = CardDefaults.cardColors(containerColor = FocusSurface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, FocusOutline)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔥 $streakDays ${if (streakDays == 1) "DAY" else "DAYS"}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            ),
                            color = FocusWarning
                        )

                        Text(
                            text = "${hours}h ${minutes}m / 5h Goal",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = FocusTextSecondary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Dynamic Real Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(FocusSurfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(goalProgress.coerceAtLeast(0.04f))
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(FocusWarning, FocusPrimary)
                                    )
                                )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = motivationalMessage,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = if (streakDays > 0 || stats.todayFocusSeconds > 0) Color.White else FocusTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Today's Schedule Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = FocusPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TODAY'S SCHEDULE",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = FocusTextPrimary
                    )
                }
                
                TextButton(
                    onClick = onNavigateToScheduleCreate,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "+ ADD NEW",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = FocusPrimary
                    )
                }
            }
        }

        // Schedule Items (Dynamic based on real scheduled sessions)
        item {
            if (scheduledSessions.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = FocusSurfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = FocusTextSecondary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No strict sessions scheduled for today.", color = FocusTextSecondary, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onNavigateToScheduleCreate,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = FocusPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FocusPrimary.copy(alpha = 0.6f))
                        ) {
                            Text("Set Study Timetable Alarm")
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    scheduledSessions.take(3).forEach { session ->
                        val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
                        val startTimeStr = session.scheduledStartTime?.let { timeFormatter.format(Date(it)) } ?: "Upcoming"
                        val endTimeStr = session.scheduledEndTime?.let { timeFormatter.format(Date(it)) } ?: ""
                        
                        val sessionDate = session.scheduledStartTime?.let { Date(it) } ?: Date(session.timestamp)
                        val todayCal = java.util.Calendar.getInstance()
                        val sessionCal = java.util.Calendar.getInstance().apply { time = sessionDate }
                        val isToday = todayCal.get(java.util.Calendar.YEAR) == sessionCal.get(java.util.Calendar.YEAR) &&
                                todayCal.get(java.util.Calendar.DAY_OF_YEAR) == sessionCal.get(java.util.Calendar.DAY_OF_YEAR)
                        val tomorrowCal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, 1) }
                        val isTomorrow = tomorrowCal.get(java.util.Calendar.YEAR) == sessionCal.get(java.util.Calendar.YEAR) &&
                                tomorrowCal.get(java.util.Calendar.DAY_OF_YEAR) == sessionCal.get(java.util.Calendar.DAY_OF_YEAR)

                        val dateTag = when {
                            isToday -> "TODAY"
                            isTomorrow -> "TOMORROW"
                            else -> SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(sessionDate).uppercase()
                        }
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = FocusSurface),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FocusOutline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = FocusWarning, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(dateTag, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = FocusWarning)
                                    }
                                    
                                    if (session.requiresPhoto) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = FocusSurfaceVariant
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = FocusPrimary, modifier = Modifier.size(10.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("PHOTO REQUIRED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = FocusPrimary)
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("📚 ${session.subjectName.uppercase()}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = FocusTextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("$startTimeStr — $endTimeStr (${session.targetDurationMinutes} mins)", style = MaterialTheme.typography.bodyMedium, color = FocusTextSecondary)
                                
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = FocusPrimary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Strict Focus", style = MaterialTheme.typography.labelMedium, color = FocusPrimary)
                                    }

                                    FilledTonalButton(
                                        onClick = {
                                            viewModel.loadScheduledSession(session.id)
                                            onNavigateToTimer()
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = FocusWarning.copy(alpha = 0.2f),
                                            contentColor = FocusWarning
                                        )
                                    ) {
                                        Text("START NOW", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        item {
            Text(
                text = "VIEW FULL SCHEDULE TIMETABLE →",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = FocusPrimary,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onNavigateToScheduleMain() }
            )
        }

        // Quick Focus Session Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusSurface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, FocusOutline),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 32.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = FocusPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("QUICK FOCUS MODE", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = FocusTextPrimary)
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Instant Pomodoro or custom timers with binaural beats and 10-permission distraction lockdown.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FocusTextSecondary,
                        lineHeight = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height(18.dp))
                    
                    Button(
                        onClick = onNavigateToSetup,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FocusPrimary, contentColor = FocusOnPrimary)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("START FOCUS SESSION", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(60.dp)) }
    }
}
