package com.example.ui.screens

import com.example.services.SoundType

import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.PaddingValues
import com.example.ui.components.AppIconView
import com.example.ui.viewmodel.PendingAttempt
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AllowedApp
import com.example.data.model.LockMode
import com.example.ui.theme.FocusBackground
import com.example.ui.theme.FocusDanger
import com.example.ui.theme.FocusPrimary
import com.example.ui.theme.FocusPrimaryDark
import com.example.ui.theme.FocusSurface
import com.example.ui.theme.FocusSurfaceVariant
import com.example.ui.theme.FocusTextSecondary
import com.example.ui.theme.FocusWarning
import com.example.ui.viewmodel.FocusViewModel
import com.example.util.FocusLockManager
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun FocusTimerScreen(
    viewModel: FocusViewModel,
    onSessionComplete: () -> Unit
) {
    val timerState by viewModel.timerState.collectAsState()
    val showLockOverlay by viewModel.showLockOverlay.collectAsState()
    val showSoftLockOverlay by viewModel.showSoftLockOverlay.collectAsState()
    val lastBlockedPackage by viewModel.lastBlockedPackage.collectAsState()
    val context = LocalContext.current

    val whitelistedAppsManual by viewModel.whitelistedAppsManual.collectAsState()
    val whitelistedAppsStrict by viewModel.whitelistedAppsStrict.collectAsState()
    val whitelistedAppsSpecial by viewModel.whitelistedAppsSpecial.collectAsState()
    val isScheduled = viewModel.activeScheduledSessionId.collectAsState().value != null || timerState.isScheduled
    val currentProfile = when {
        timerState.whitelistProfile.isNotBlank() -> timerState.whitelistProfile
        timerState.isSpecialSession -> "SPECIAL"
        isScheduled -> "STRICT"
        else -> "MANUAL"
    }
    val whitelistedApps = when (currentProfile) {
        "SPECIAL" -> whitelistedAppsSpecial
        "STRICT" -> whitelistedAppsStrict
        else -> whitelistedAppsManual
    }

    val allAppsManual by viewModel.allowedAppsManual.collectAsState()
    val allAppsStrict by viewModel.allowedAppsStrict.collectAsState()
    val allAppsSpecial by viewModel.allowedAppsSpecial.collectAsState()
    val allApps = when (currentProfile) {
        "SPECIAL" -> allAppsSpecial
        "STRICT" -> allAppsStrict
        else -> allAppsManual
    }

    var showExitAttemptDialog by remember { mutableStateOf(false) }
    var showManageWhitelistDialog by remember { mutableStateOf(false) }
    var showEmergencyConfirm by remember { mutableStateOf(false) }

    val audioPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument(),
        onResult = { uri: android.net.Uri? ->
            if (uri != null) {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val prefs = context.getSharedPreferences("FocusPrefs", android.content.Context.MODE_PRIVATE)
                prefs.edit().putString("AMBIENT_CUSTOM_AUDIO_URI", uri.toString()).apply()
                viewModel.setSound(com.example.services.SoundType.CUSTOM_AUDIO)
            }
        }
    )
    var emergencyPenaltyCountdown by remember { mutableIntStateOf(10) }

    // Intercept hardware Back Button: Instead of letting user exit to social media, prompt with allowed apps chooser
    BackHandler(enabled = timerState.isRunning) {
        showExitAttemptDialog = true
    }

    // Emergency exit penalty timer
    LaunchedEffect(showEmergencyConfirm) {
        if (showEmergencyConfirm) {
            emergencyPenaltyCountdown = if (timerState.lockMode == LockMode.MAXIMUM_LOCK) 300 else 0
            while (emergencyPenaltyCountdown > 0) {
                delay(1000)
                emergencyPenaltyCountdown--
            }
        }
    }

    // Auto navigate when timer reaches zero or is waiting for verification
    LaunchedEffect(timerState.isRunning, timerState.isWaitingVerification, timerState.remainingSeconds) {
        if ((!timerState.isRunning || timerState.isWaitingVerification) && timerState.remainingSeconds <= 0 && timerState.totalSeconds > 0) {
            onSessionComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground)
    ) {
        // Starry Night Ambient Canvas Drawing
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Night Sky Gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070B14),
                        Color(0xFF0F172A),
                        Color(0xFF1E293B)
                    )
                )
            )

            // Star field dots
            val random = Random(42)
            for (i in 0..70) {
                val x = random.nextFloat() * width
                val y = random.nextFloat() * (height * 0.65f)
                val starRadius = random.nextFloat() * 2.5f + 0.5f
                val starAlpha = random.nextFloat() * 0.7f + 0.3f
                drawCircle(
                    color = Color.White.copy(alpha = starAlpha),
                    radius = starRadius,
                    center = Offset(x, y)
                )
            }

            // Mountain Silhouette at bottom
            val mountainPath = Path().apply {
                moveTo(0f, height * 0.85f)
                lineTo(width * 0.25f, height * 0.76f)
                lineTo(width * 0.5f, height * 0.82f)
                lineTo(width * 0.75f, height * 0.72f)
                lineTo(width, height * 0.84f)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(
                path = mountainPath,
                color = Color(0xFF030712)
            )
        }

        // Timer Screen Main Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Subject Badge
                Box(
                    modifier = Modifier
                        .background(FocusSurface.copy(alpha = 0.85f), CircleShape)
                        .border(1.dp, FocusPrimary.copy(alpha = 0.5f), CircleShape)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(FocusPrimary, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = timerState.subjectName.ifBlank { "Active Session" },
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                // Lock Mode Badge & Distraction counter
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (timerState.distractionAttempts > 0) {
                        Box(
                            modifier = Modifier
                                .background(FocusDanger.copy(alpha = 0.2f), CircleShape)
                                .border(1.dp, FocusDanger, CircleShape)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "🛡️ ${timerState.distractionAttempts} Blocked",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = FocusDanger
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(FocusWarning.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, FocusWarning, CircleShape)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = FocusWarning,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = timerState.lockMode.title.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = FocusWarning
                            )
                        }
                    }

                    // Test Preview Block Overlay Button
                    Button(
                        onClick = {
                            viewModel.triggerDistractionWarning(
                                blockedPackage = "com.google.android.youtube",
                                showRedModal = true
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("TEST BLOCK CARD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Central Ring Clock Timer
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .testTag("timer_clock_ring"),
                contentAlignment = Alignment.Center
            ) {
                val progress = if (timerState.totalSeconds > 0) {
                    timerState.remainingSeconds.toFloat() / timerState.totalSeconds.toFloat()
                } else 1.0f

                // Animated Circular Ring Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 12.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    val arcSize = Size(diameter, diameter)

                    // Track Ring background
                    drawArc(
                        color = Color.White.copy(alpha = 0.1f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth)
                    )

                    // Tick Marks Ring
                    for (i in 0 until 60) {
                        val angle = Math.toRadians((i * 6).toDouble())
                        val r1 = (diameter / 2) - 8
                        val r2 = diameter / 2
                        val cx = size.width / 2
                        val cy = size.height / 2
                        val startX = (cx + r1 * cos(angle)).toFloat()
                        val startY = (cy + r1 * sin(angle)).toFloat()
                        val endX = (cx + r2 * cos(angle)).toFloat()
                        val endY = (cy + r2 * sin(angle)).toFloat()
                        drawLine(
                            color = Color.White.copy(alpha = if (i % 5 == 0) 0.4f else 0.15f),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = if (i % 5 == 0) 3f else 1.5f
                        )
                    }

                    // Progress Ring arc
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(FocusPrimary, FocusPrimaryDark, FocusWarning, FocusPrimary)
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth)
                    )
                }

                // Clock Digital Time Text Display
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val hrs = timerState.remainingSeconds / 3600
                    val mins = (timerState.remainingSeconds % 3600) / 60
                    val secs = timerState.remainingSeconds % 60
                    val timeFormatted = if (hrs > 0) {
                        String.format("%02d:%02d:%02d", hrs, mins, secs)
                    } else {
                        String.format("%02d:%02d", mins, secs)
                    }

                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (timerState.isPaused) "PAUSED" else "FOCUS SESSION ACTIVE",
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                        color = if (timerState.isPaused) FocusWarning else FocusPrimary
                    )
                }
            }

            // ALLOWED STUDY APPS SECTION DIRECTLY UNDER THE TIMER
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("allowed_apps_container"),
                colors = CardDefaults.cardColors(containerColor = FocusSurface.copy(alpha = 0.95f)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, FocusPrimary.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Apps,
                                contentDescription = null,
                                tint = FocusPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ALLOWED STUDY APPS",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${whitelistedApps.size})",
                                style = MaterialTheme.typography.labelSmall,
                                color = FocusPrimary
                            )
                        }

                        if (!isScheduled) {
                            // Quick Add / Manage Button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FocusPrimary.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, FocusPrimary.copy(alpha = 0.5f)),
                            modifier = Modifier.clickable(enabled = !isScheduled) { showManageWhitelistDialog = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add",
                                    tint = FocusPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Manage Apps",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = FocusPrimary
                                )
                            }
                        }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (whitelistedApps.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(whitelistedApps, key = { it.packageName }) { app ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = FocusBackground.copy(alpha = 0.8f),
                                    border = BorderStroke(1.dp, FocusPrimary.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .clickable {
                                            val launched = FocusLockManager.launchAllowedApp(context, app.packageName)
                                            if (launched) {
                                                Toast.makeText(context, "Opening ${app.appName} (Study Session)", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Could not open ${app.appName}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(FocusPrimary.copy(alpha = 0.25f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = app.appName.take(1).uppercase(),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = FocusPrimary
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = app.appName,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "TAP TO OPEN ↗",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 9.sp
                                                ),
                                                color = FocusPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Empty state: guide the student to select allowed study apps
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(FocusBackground.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .border(1.dp, FocusSurfaceVariant, RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "No external apps whitelisted.\nSocial media & other apps are locked.",
                                style = MaterialTheme.typography.bodySmall,
                                color = FocusTextSecondary
                            )
                            if (!isScheduled) {
                                Button(
                                onClick = { showManageWhitelistDialog = true }, enabled = !isScheduled,
                                colors = ButtonDefaults.buttonColors(containerColor = FocusPrimary, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("+ Select Apps", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                            }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 2: Ambient Focus Sound Generator (for Active Session)
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusSurface),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, FocusSurfaceVariant, RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = FocusPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ambient Focus Sound",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SoundType.entries.forEach { st ->
                            val isSel = timerState.selectedSound == st
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSel) FocusPrimary.copy(alpha = 0.1f) else FocusBackground,
                                border = BorderStroke(
                                    width = if (isSel) 1.5.dp else 1.dp,
                                    color = if (isSel) FocusPrimary else FocusSurfaceVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        if (st == SoundType.CUSTOM_AUDIO) {
                                            audioPickerLauncher.launch(arrayOf("audio/*"))
                                        } else {
                                            viewModel.setSound(st) 
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .background(
                                                if (isSel) FocusPrimary.copy(alpha = 0.2f) else FocusSurfaceVariant.copy(alpha = 0.4f),
                                                androidx.compose.foundation.shape.CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (st.isBinaural) Icons.Default.Headphones else Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = if (isSel) FocusPrimary else FocusTextSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = st.label,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSel) FocusPrimary else Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    RadioButton(
                                        selected = isSel,
                                        onClick = { 
                                            if (st == SoundType.CUSTOM_AUDIO) {
                                                audioPickerLauncher.launch(arrayOf("audio/*"))
                                            } else {
                                                viewModel.setSound(st) 
                                            }
                                        },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = FocusPrimary,
                                            unselectedColor = FocusTextSecondary
                                        ),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Action Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pause / Resume
                Button(
                    onClick = {
                        if (timerState.isPaused) viewModel.resumeSession() else viewModel.pauseSession()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("pause_resume_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (timerState.isPaused) FocusPrimary else FocusSurface,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (timerState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (timerState.isPaused) "RESUME" else "PAUSE", fontWeight = FontWeight.Bold)
                    }
                }

                // Finish Session Button
                Button(
                    onClick = {
                        if (timerState.lockMode == LockMode.MAXIMUM_LOCK && timerState.remainingSeconds > 0) {
                            // Penalty for trying to cheat and finish early in Deep Work Mode
                            viewModel.addPenaltyTime(420) // 7 minutes
                            showExitAttemptDialog = false
                            showEmergencyConfirm = true
                        } else {
                            viewModel.completeFocusSession()
                            onSessionComplete()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("finish_session_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FocusWarning,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("FINISH", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ==========================================
        // 1. EXIT ATTEMPT / ALLOWED APP SELECTOR DIALOG
        // ==========================================
        if (showExitAttemptDialog) {
            Dialog(
                onDismissRequest = { showExitAttemptDialog = false },
                properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = FocusSurface,
                    border = BorderStroke(1.dp, FocusPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(FocusPrimary.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = FocusPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "FOCUS SESSION IN PROGRESS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Social media & distracting apps are blocked. Which allowed study app would you like to open?",
                            style = MaterialTheme.typography.bodySmall,
                            color = FocusTextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (whitelistedApps.isNotEmpty()) {
                            Text(
                                text = "CHOOSE STUDY APP TO OPEN:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = FocusPrimary,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((whitelistedApps.size * 60).coerceAtMost(220).dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(whitelistedApps, key = { it.packageName }) { app ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = FocusBackground,
                                        border = BorderStroke(1.dp, FocusPrimary.copy(alpha = 0.3f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                showExitAttemptDialog = false
                                                val launched = FocusLockManager.launchAllowedApp(context, app.packageName)
                                                if (launched) {
                                                    Toast.makeText(context, "Opening ${app.appName} (Study Mode)", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Could not open ${app.appName}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .background(FocusPrimary.copy(alpha = 0.2f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = app.appName.take(1).uppercase(),
                                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = FocusPrimary
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = app.appName,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                    color = Color.White
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "OPEN",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = FocusPrimary
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                    contentDescription = null,
                                                    tint = FocusPrimary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = FocusBackground,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No study apps whitelisted yet.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = FocusTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    if (!isScheduled) {
                                        Button(
                                        onClick = {
                                            showExitAttemptDialog = false
                                            showManageWhitelistDialog = true
                                        }, enabled = !isScheduled,
                                        colors = ButtonDefaults.buttonColors(containerColor = FocusPrimary, contentColor = Color.Black),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("+ Select Study Apps", fontWeight = FontWeight.Bold)
                                    }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Actions: Stay on timer vs Emergency exit
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showExitAttemptDialog = false },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FocusPrimary, contentColor = Color.Black),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Stay on Timer", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    if (timerState.lockMode == LockMode.MAXIMUM_LOCK && timerState.remainingSeconds > 0) {
                                        viewModel.addPenaltyTime(420)
                                    }
                                    showExitAttemptDialog = false
                                    showEmergencyConfirm = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = FocusDanger),
                                border = BorderStroke(1.dp, FocusDanger.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Emergency Stop", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. IN-SESSION APP WHITELIST MANAGER DIALOG
        // ==========================================
        if (showManageWhitelistDialog) {
            var inSessionSearch by remember { mutableStateOf("") }
            val filteredInSessionApps = allApps.filter {
                it.appName.contains(inSessionSearch, ignoreCase = true) ||
                        it.category.contains(inSessionSearch, ignoreCase = true)
            }

            Dialog(
                onDismissRequest = { showManageWhitelistDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = FocusSurface,
                    border = BorderStroke(1.dp, FocusPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .fillMaxHeight(0.85f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "SELECT ALLOWED STUDY APPS",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Toggle apps you want to use during this session",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FocusTextSecondary
                                )
                            }
                            IconButton(onClick = { showManageWhitelistDialog = false }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = inSessionSearch,
                            onValueChange = { inSessionSearch = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search installed apps...", color = FocusTextSecondary) },
                            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = FocusPrimary) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FocusPrimary,
                                unfocusedBorderColor = FocusSurfaceVariant,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredInSessionApps, key = { it.packageName }) { app ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (app.isAllowed) FocusPrimary.copy(alpha = 0.15f) else FocusBackground
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, if (app.isAllowed) FocusPrimary.copy(alpha = 0.6f) else FocusSurfaceVariant),
                                    modifier = Modifier.fillMaxWidth()
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
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .background(
                                                        if (app.isAllowed) FocusPrimary.copy(alpha = 0.2f) else FocusSurfaceVariant,
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (app.isAllowed) Icons.Default.Check else Icons.Default.Lock,
                                                    contentDescription = null,
                                                    tint = if (app.isAllowed) FocusPrimary else Color.Gray,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = app.appName,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = if (app.isAllowed) "✓ ALLOWED FOR STUDY" else "🔒 BLOCKED",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = if (app.isAllowed) FocusPrimary else FocusDanger
                                                )
                                            }
                                        }

                                        Switch(
                                            checked = app.isAllowed,
                                            onCheckedChange = { allowed ->
                                                viewModel.toggleAppAllowed(app.packageName, allowed, currentProfile)
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.Black,
                                                checkedTrackColor = FocusPrimary,
                                                uncheckedThumbColor = Color.Gray,
                                                uncheckedTrackColor = FocusSurfaceVariant
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showManageWhitelistDialog = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FocusPrimary, contentColor = Color.Black)
                        ) {
                            Text("SAVE & CONTINUE STUDY", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. STRICT LOCK DISTRACTION OVERLAY DIALOG
        // ==========================================
        if (showLockOverlay || showSoftLockOverlay) {
            val pendingAttempts by viewModel.pendingAttemptsList.collectAsState()
            val primaryBlockedPkg = lastBlockedPackage ?: "com.google.android.youtube"
            val primaryBlockedAppName = remember(primaryBlockedPkg) { viewModel.getAppDisplayName(primaryBlockedPkg) }
            val sdfNow = remember { java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()) }
            val currentAttemptTime = remember { sdfNow.format(java.util.Date()) }

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

            val latestAppName = attemptsToShow.firstOrNull()?.appName ?: primaryBlockedAppName

            val mins = timerState.remainingSeconds / 60
            val secs = timerState.remainingSeconds % 60
            val timeStr = String.format("%02d:%02d", mins, secs)
            val sessionNameText = timerState.sessionName.ifBlank { "ACTIVE FOCUS SESSION" }
            val subjectNameText = timerState.subjectName.ifBlank { "Deep Study" }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.96f))
                    .padding(14.dp)
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                contentAlignment = Alignment.Center
            ) {
                    // Red Shield Frame Container
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
                            

                            Spacer(modifier = Modifier.height(8.dp))

                            // Title: BLOCKED APP DETECTED
                            Text(
                                text = "BLOCKED APP DETECTED",
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
                                text = "Distracting apps are completely restricted during your active focus session.",
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
                                    // Session Name & Subject Row
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
                                            text = "${subjectNameText.uppercase()} – ${sessionNameText.uppercase()}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            ),
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    HorizontalDivider(color = Color(0xFF2E364A), thickness = 1.dp)

                                    // Time Remaining Row
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
                                            Text(text = "⏱️", fontSize = 16.sp)
                                        }
                                        Text(
                                            text = "$timeStr REMAINING",
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.Bold,
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
                                        text = "SESSION IN PROGRESS",
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
                                        text = "You tried to open $latestAppName. This app is restricted because your focus session is currently active.",
                                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 17.sp),
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Primary Button: BACK TO FOCUS TIMER
                            Button(
                                onClick = {
                                    viewModel.dismissLockOverlay()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("return_to_focus_btn")
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
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "BACK TO FOCUS TIMER",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.8.sp
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Secondary Button: Open Allowed Study App
                            Button(
                                onClick = {
                                    viewModel.dismissLockOverlay()
                                    showExitAttemptDialog = true
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
                                        imageVector = Icons.Default.Apps,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "OPEN ALLOWED STUDY APP",
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

        // ==========================================
        // 4. EMERGENCY PENALTY CONFIRMATION DIALOG
        // ==========================================
        if (showEmergencyConfirm) {
            Dialog(
                onDismissRequest = {
                    // Cannot dismiss in Maximum Lock until penalty is over
                    if (timerState.lockMode != LockMode.MAXIMUM_LOCK) {
                        showEmergencyConfirm = false
                    }
                },
                properties = androidx.compose.ui.window.DialogProperties(
                    dismissOnBackPress = timerState.lockMode != LockMode.MAXIMUM_LOCK,
                    dismissOnClickOutside = timerState.lockMode != LockMode.MAXIMUM_LOCK
                )
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = FocusBackground,
                    border = BorderStroke(1.dp, FocusDanger.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Emergency Early Exit",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Exiting early will reset your daily study streak. Please wait for cooling timer.",
                            style = MaterialTheme.typography.bodySmall,
                            color = FocusTextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (emergencyPenaltyCountdown > 0) "Wait $emergencyPenaltyCountdown Seconds..." else "Exit Unlocked",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (emergencyPenaltyCountdown > 0) FocusWarning else FocusDanger
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (emergencyPenaltyCountdown <= 0) {
                                    showEmergencyConfirm = false
                                    viewModel.emergencyExitSession()
                                    onSessionComplete()
                                }
                            },
                            enabled = emergencyPenaltyCountdown <= 0,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("confirm_emergency_exit_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = FocusDanger)
                        ) {
                            Text("CONFIRM EARLY EXIT", fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedButton(
                            onClick = {
                                showEmergencyConfirm = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = FocusPrimary),
                            border = BorderStroke(1.dp, FocusPrimary.copy(alpha = 0.5f))
                        ) {
                            Text("RESUME STUDYING", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

