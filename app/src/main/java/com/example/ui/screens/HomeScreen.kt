package com.example.ui.screens
import androidx.compose.ui.draw.scale

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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FocusSession
import com.example.ui.dialogs.AppGuideDialog
import com.example.ui.dialogs.NotificationCenterDialog
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import com.example.ui.theme.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import com.example.ui.components.AppIconView
import com.example.ui.viewmodel.PendingAttempt
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
    onNavigateToTimer: () -> Unit,
    onNavigateToCameraStart: () -> Unit,
    onNavigateToSessionRouter: (Long) -> Unit
) {
    val stats by viewModel.summaryStats.collectAsState()
    val scheduledSessions by viewModel.scheduledSessions.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val showPendingLockOverlay by viewModel.showPendingLockOverlay.collectAsState()
    val pendingSessionNameOverlay by viewModel.pendingSessionNameOverlay.collectAsState()
    val pendingSessionIdOverlay by viewModel.pendingSessionIdOverlay.collectAsState()
    val lastBlockedPackage by viewModel.lastBlockedPackage.collectAsState()
    var showSpecialWhitelistPopup by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var showQuickDurationDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var showValidationDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var validationConflicts by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<List<com.example.data.model.FocusSession>>(emptyList()) }
    var nextValidationSession by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.example.data.model.FocusSession?>(null) }
    var pendingDuration by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }
    var selectedSpecialWhitelist by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("FocusPrefs", Context.MODE_PRIVATE)
    var userName by remember { mutableStateOf(sharedPrefs.getString("USER_NAME", "Focus Student") ?: "Focus Student") }
    var profilePhotoUri by remember { mutableStateOf<String?>(sharedPrefs.getString("PROFILE_PHOTO_URI", null)) }
    
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
                onNavigateToSessionRouter(session.id)
            },
            onOpenShield = onNavigateToSetup,
            onOpenSchedule = onNavigateToScheduleMain
        )
    }

    if (showSpecialWhitelistPopup) {
        AppBlockingSystemDialog(
            onDismiss = { showSpecialWhitelistPopup = false },
            onSelectWhitelist = { whitelist ->
                selectedSpecialWhitelist = whitelist
                showSpecialWhitelistPopup = false
                showQuickDurationDialog = true
            }
        )
    }
    if (showQuickDurationDialog) {
        QuickDurationDialog(
            onDismissRequest = { showQuickDurationDialog = false },
            onSubmit = { duration ->
                showQuickDurationDialog = false
                val userStart = System.currentTimeMillis()
                val userEnd = userStart + (duration * 60 * 1000L)
                val conflicts = scheduledSessions.filter { it.status == "SCHEDULED" }.filter { s ->
                    val sStart = s.scheduledStartTime ?: return@filter false
                    val sEnd = s.scheduledEndTime ?: return@filter false
                    userStart < sEnd && userEnd > sStart
                }
                
                if (conflicts.isNotEmpty()) {
                    validationConflicts = conflicts
                    nextValidationSession = scheduledSessions.filter { it.status == "SCHEDULED" && (it.scheduledStartTime ?: 0) >= userEnd }.minByOrNull { it.scheduledStartTime ?: 0 }
                    pendingDuration = duration
                    showValidationDialog = true
                } else {
                    viewModel.startSpecialSession(duration, selectedSpecialWhitelist)
                    onNavigateToTimer()
                }
            }
        )
        
    if (showValidationDialog) {
        val userStart = System.currentTimeMillis()
        val userEnd = userStart + (pendingDuration * 60 * 1000L)
        ScheduleValidationDialog(
            saveText = "START SESSION",
            changeText = "CHANGE DURATION",
            conflicts = validationConflicts,
            userStart = userStart,
            userEnd = userEnd,
            nextSession = nextValidationSession,
            onChangeTime = { showValidationDialog = false },
            onSave = { 
                showValidationDialog = false
                viewModel.startSpecialSession(pendingDuration, selectedSpecialWhitelist)
                onNavigateToTimer()
            },
            onCancel = { showValidationDialog = false }
        )
    }
    }

    if (showPendingLockOverlay) {

        val pendingAttempts by viewModel.pendingAttemptsList.collectAsState()
        val scheduledSessions by viewModel.scheduledSessions.collectAsState()
        
        val currentPendingSession = remember(pendingSessionIdOverlay, scheduledSessions) {
            scheduledSessions.find { it.id == pendingSessionIdOverlay }
                ?: scheduledSessions.find { it.status == "SCHEDULED" }
        }
        
        val sessionTitleText = remember(currentPendingSession, pendingSessionNameOverlay) {
            currentPendingSession?.let { session ->
                if (session.subjectName.isNotBlank() && session.sessionName.isNotBlank()) {
                    "${session.subjectName.uppercase()} – ${session.sessionName.uppercase()}"
                } else if (session.subjectName.isNotBlank()) {
                    session.subjectName.uppercase()
                } else {
                    session.sessionName.uppercase()
                }
            } ?: if (pendingSessionNameOverlay.isNotBlank()) pendingSessionNameOverlay.uppercase() else "POLITY – LAXMIKANTH"
        }
        
        val timeRangeText = remember(currentPendingSession) {
            val now = System.currentTimeMillis()
            val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
            if (currentPendingSession?.scheduledStartTime != null) {
                val startStr = sdf.format(java.util.Date(currentPendingSession.scheduledStartTime))
                val endMillis = currentPendingSession.scheduledEndTime 
                    ?: (currentPendingSession.scheduledStartTime + currentPendingSession.targetDurationMinutes * 60 * 1000L)
                val endStr = sdf.format(java.util.Date(endMillis))
                "$startStr – $endStr"
            } else {
                val startStr = sdf.format(java.util.Date(now))
                val endStr = sdf.format(java.util.Date(now + 45 * 60 * 1000L))
                "$startStr – $endStr"
            }
        }
        
        val primaryBlockedPkg = lastBlockedPackage ?: "com.google.android.youtube"
        val primaryBlockedAppName = remember(primaryBlockedPkg) { viewModel.getAppDisplayName(primaryBlockedPkg) }
        
        val sdfNow = remember { java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()) }
        val currentAttemptTime = remember { sdfNow.format(java.util.Date()) }

        Dialog(
            onDismissRequest = { /* Modal lock */ },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                // Red Shield Frame Container matching attached image
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.96f)
                        .border(
                            width = 2.5.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFEF4444),
                                    Color(0xFFB91C1C),
                                    Color(0xFF7F1D1D),
                                    Color(0xFFEF4444)
                                )
                            ),
                            shape = RoundedCornerShape(32.dp)
                        ),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF131722)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 22.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header Alert Icon: Glowing Gradient Warning Triangle ⚠️
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFF97316).copy(alpha = 0.4f),
                                            Color(0xFFEF4444).copy(alpha = 0.15f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⚠️",
                                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Title: SESSION PENDING
                        Text(
                            text = "SESSION PENDING",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Subtitle
                        Text(
                            text = "Your scheduled focus session has not been started yet.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = Color.White.copy(alpha = 0.75f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Session Info Inset Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF2E364A), RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF1B2130)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Session Name Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(Color(0xFF252D3F), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "📖", fontSize = 16.sp)
                                    }
                                    Text(
                                        text = sessionTitleText,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Divider(color = Color(0xFF2E364A), thickness = 1.dp)

                                // Time Range Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(Color(0xFF252D3F), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "🕒", fontSize = 16.sp)
                                    }
                                    Text(
                                        text = timeRangeText,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = Color(0xFFF97316)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // STATUS Badge Row
                        Text(
                            text = "STATUS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = Color.White.copy(alpha = 0.5f)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .border(1.5.dp, Color(0xFF991B1B), RoundedCornerShape(20.dp))
                                .background(Color(0xFF3B1212), RoundedCornerShape(20.dp))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFFEF4444), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "START REQUIRED",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // YOU TRIED TO OPEN Section Header
                        Text(
                            text = "YOU TRIED TO OPEN",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.5.sp
                            ),
                            color = Color(0xFFEF4444)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // List of Blocked Attempts
                        val attemptsToShow = if (pendingAttempts.isNotEmpty()) {
                            pendingAttempts
                        } else {
                            listOf(
                                PendingAttempt(
                                    packageName = primaryBlockedPkg,
                                    appName = primaryBlockedAppName,
                                    timeFormatted = currentAttemptTime
                                )
                            )
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            attemptsToShow.take(4).forEach { attempt ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFF2E364A), RoundedCornerShape(14.dp)),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF1B2130)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            // App Icon
                                            AppIconView(
                                                packageName = attempt.packageName,
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                            )

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column {
                                                Text(
                                                    text = attempt.appName,
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = attempt.packageName,
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                    color = Color.White.copy(alpha = 0.5f),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        // Attempt Time
                                        Text(
                                            text = attempt.timeFormatted,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = Color(0xFFEF4444)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Lock Notice Box
                        val latestAppName = attemptsToShow.firstOrNull()?.appName ?: primaryBlockedAppName
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF374151), RoundedCornerShape(14.dp)),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF1B2130).copy(alpha = 0.7f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "You tried to open $latestAppName. This app is restricted because your scheduled focus session is still pending.",
                                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 17.sp),
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Primary Button: START SESSION NOW (Gradient Orange/Red Pill Button)
                        Button(
                            onClick = {
                                viewModel.dismissLockOverlay()
                                if (pendingSessionIdOverlay != -1L) {
                                    onNavigateToSessionRouter(pendingSessionIdOverlay)
                                } else if (currentPendingSession != null) {
                                    onNavigateToSessionRouter(currentPendingSession.id)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFF97316),
                                            Color(0xFFEA580C),
                                            Color(0xFFDC2626)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White
                            ),
                            shape = CircleShape
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "START SESSION NOW",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.8.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Secondary Button: Return to Focus App (Dark Pill Button)
                        Button(
                            onClick = {
                                viewModel.dismissLockOverlay()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .border(1.dp, Color(0xFF2E364A), CircleShape),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1B2130),
                                contentColor = Color.White
                            ),
                            shape = CircleShape
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Return to Focus App",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }
        }
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

        // Nearest Scheduled Session Warning Banner (NO SECONDS, ONLY REMINDER)
        item {
            val now = System.currentTimeMillis()
            val nearestSession = scheduledSessions
                .filter { it.status == "SCHEDULED" && it.scheduledStartTime != null }
                .minByOrNull { 
                    val diff = (it.scheduledStartTime ?: 0L) - now
                    if (diff < -300000L) Long.MAX_VALUE else Math.abs(diff)
                }

            if (nearestSession != null && nearestSession.scheduledStartTime != null) {
                val startMs = nearestSession.scheduledStartTime
                val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
                val timeStr = timeFormatter.format(Date(startMs))

                val diffMs = startMs - now
                val diffMins = (diffMs / (60 * 1000L)).toInt()
                val diffHours = diffMins / 60
                val diffDays = diffHours / 24

                val reminderText = when {
                    diffMs <= 0 -> "⚠️ Session time reached ($timeStr) - Pending"
                    diffDays > 0 -> "Starts in $diffDays day(s) ($timeStr)"
                    diffHours > 0 -> "Starts in $diffHours hr ${diffMins % 60} mins ($timeStr)"
                    diffMins > 0 -> "Starts in $diffMins mins ($timeStr)"
                    else -> "Starting now ($timeStr)"
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = FocusWarning.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, FocusWarning),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(FocusWarning.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Schedule Warning",
                                    tint = FocusWarning,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "UPCOMING SCHEDULE REMINDER",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = FocusWarning
                                )
                                Text(
                                    text = nearestSession.sessionName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White
                                )
                                Text(
                                    text = reminderText,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = FocusTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                onNavigateToSessionRouter(nearestSession.id)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FocusWarning,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "START NOW",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Black
                                )
                            )
                        }
                    }
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
                                            onNavigateToSessionRouter(session.id)
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
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showSpecialWhitelistPopup = true }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Special Whitelist",
                        tint = FocusPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Special Whitelist Switch",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
                Switch(
                    checked = false,
                    onCheckedChange = { showSpecialWhitelistPopup = true },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = FocusPrimary,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = FocusSurfaceVariant
                    )
                )
            }
        }
        item { Spacer(modifier = Modifier.height(60.dp)) }
    }
}


@Composable
fun AppBlockingSystemDialog(
    onDismiss: () -> Unit,
    onSelectWhitelist: (String) -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
            color = androidx.compose.ui.graphics.Color(0xFF0F172A), // Dark Navy
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = androidx.compose.ui.Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                androidx.compose.foundation.layout.Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Shield,
                        contentDescription = "Logo",
                        tint = androidx.compose.ui.graphics.Color(0xFF3B82F6),
                        modifier = androidx.compose.ui.Modifier.size(24.dp)
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                    androidx.compose.material3.Text(
                        text = "Singal",
                        color = androidx.compose.ui.graphics.Color(0xFF3B82F6),
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(24.dp))
                
                androidx.compose.material3.Text(
                    text = "APP BLOCKING SYSTEM",
                    color = androidx.compose.ui.graphics.Color.White,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(24.dp))
                
                AppBlockingOption(
                    title = "Manual Focus Whitelist",
                    subtitle = "Allowed apps during Quick Focus sessions",
                    onClick = { onSelectWhitelist("MANUAL") }
                )
                androidx.compose.material3.Divider(color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.1f))
                
                AppBlockingOption(
                    title = "Strict Schedule Whitelist",
                    subtitle = "Allowed apps during Strict Scheduled Focus",
                    onClick = { onSelectWhitelist("STRICT") }
                )
                androidx.compose.material3.Divider(color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.1f))
                
                AppBlockingOption(
                    title = "Special Whitelist",
                    subtitle = "Special allowed apps configuration",
                    onClick = { onSelectWhitelist("SPECIAL") }
                )
            }
        }
    }
}

@Composable
fun AppBlockingOption(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Column(modifier = androidx.compose.ui.Modifier.weight(1f)) {
            androidx.compose.material3.Text(
                text = title,
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 14.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )
            androidx.compose.material3.Text(
                text = subtitle,
                color = androidx.compose.ui.graphics.Color.Gray,
                fontSize = 11.sp,
                maxLines = 2,
                lineHeight = 14.sp
            )
        }
        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(16.dp))
        androidx.compose.material3.Icon(
            imageVector = androidx.compose.material.icons.Icons.Default.KeyboardArrowRight,
            contentDescription = "Select",
            tint = androidx.compose.ui.graphics.Color.Gray,
            modifier = androidx.compose.ui.Modifier.size(24.dp)
        )
    }
}

@Composable
fun QuickDurationDialog(
    onDismissRequest: () -> Unit,
    onSubmit: (Int) -> Unit
) {
    var selectedDurationMins by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(45) }
    
    val durations = listOf(
        "10 मिनट" to 10,
        "25 मिनट" to 25,
        "45 मिनट" to 45,
        "1.5 Hours" to 90,
        "2.5 Hours" to 150,
        "3.5 Hours" to 210,
        "4 Hours" to 240,
        "5 Hours" to 300
    )
    
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismissRequest) {
        androidx.compose.material3.Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            color = androidx.compose.ui.graphics.Color(0xFFF8FAFC),
            shadowElevation = 12.dp,
            modifier = androidx.compose.ui.Modifier.width(340.dp)
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = androidx.compose.ui.Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                androidx.compose.material3.Text(
                    text = "कितने वक़्त के लिए?",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color(0xFF0F172A)
                    )
                )
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(20.dp))
                
                val chunkedDurations = durations.chunked(3)
                chunkedDurations.forEach { rowItems ->
                    androidx.compose.foundation.layout.Row(
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { (label, durationMins) ->
                            val isSelected = selectedDurationMins == durationMins
                            androidx.compose.material3.Surface(
                                modifier = androidx.compose.ui.Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedDurationMins = durationMins
                                    },
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                                color = if (isSelected) androidx.compose.ui.graphics.Color(0xFF2563EB) else androidx.compose.ui.graphics.Color(0xFFE2E8F0).copy(alpha = 0.6f)
                            ) {
                                androidx.compose.foundation.layout.Box(
                                    modifier = androidx.compose.ui.Modifier.padding(vertical = 12.dp),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
                                ) {
                                    androidx.compose.material3.Text(
                                        text = label,
                                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                                        color = if (isSelected) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color(0xFF334155),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                        if (rowItems.size < 3) {
                            repeat(3 - rowItems.size) {
                                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.weight(1f))
                            }
                        }
                    }
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(10.dp))
                }
                
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(20.dp))
                
                androidx.compose.material3.Button(
                    onClick = { onSubmit(selectedDurationMins) },
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF2563EB))
                ) {
                    androidx.compose.material3.Text("Submit", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
