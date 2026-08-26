package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.R
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
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
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
    var profilePhotoUri by remember { mutableStateOf<String?>(null) }
    
    var showAppGuide by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    var dismissedNotificationIds by remember { mutableStateOf(sharedPrefs.getStringSet("DISMISSED_NOTIFS", setOf()) ?: setOf()) }

    LaunchedEffect(Unit) {
        userName = sharedPrefs.getString("USER_NAME", "Focus Student") ?: "Focus Student"
        profilePhotoUri = sharedPrefs.getString("PROFILE_PHOTO_URI", null)
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
            onClearAll = { idsToClear ->
                val newSet = dismissedNotificationIds + idsToClear
                dismissedNotificationIds = newSet
                sharedPrefs.edit().putStringSet("DISMISSED_NOTIFS", newSet).apply()
            },
            onDismissNotification = { id -> 
                val newSet = dismissedNotificationIds + id
                dismissedNotificationIds = newSet
                sharedPrefs.edit().putStringSet("DISMISSED_NOTIFS", newSet).apply()
            },
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

        // Premium Top Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Premium Styled App Brand
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = FocusSurfaceVariant, // neutral background since icon has its own colors
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "App Icon",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "FOCUS OS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "PRODUCTIVITY ENGINE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                fontSize = 8.sp
                            ),
                            color = FocusPrimary
                        )
                    }
                }

                // Action Icons (Guide & Notifications)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // App Guide (MenuBook Icon)
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { showAppGuide = true }
                            .testTag("hamburger_menu_btn"),
                        shape = CircleShape,
                        color = FocusSurfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FocusSurfaceVariant)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "App Complete Guide",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Notification Bell
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(FocusSurfaceVariant.copy(alpha = 0.4f))
                            .border(1.dp, FocusSurfaceVariant, CircleShape)
                            .clickable { showNotifications = true }
                            .testTag("header_notifications_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications & Alerts",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(24.dp)
                        )
                        
                        // Notification Badge (Minimalist Dot)
                        val alertCount = (if (scheduledSessions.isNotEmpty()) 1 else 0) + 2
                        if (alertCount > 0) {
                            Surface(
                                shape = CircleShape,
                                color = FocusWarning,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 10.dp, end = 10.dp)
                                    .size(12.dp)
                            ) {}
                        }
                    }
                }
            }
        }

        // Greeting & Motivation
        item {
            Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                if (profilePhotoUri != null) {
                    AsyncImage(
                        model = profilePhotoUri,
                        contentDescription = "Profile Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .border(2.dp, FocusPrimary, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Column {
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
        item { Spacer(modifier = Modifier.height(60.dp)) }
    }
}
