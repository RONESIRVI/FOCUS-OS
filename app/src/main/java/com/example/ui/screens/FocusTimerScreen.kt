package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.LockMode
import com.example.services.SoundType
import com.example.ui.theme.FocusAccentOrange
import com.example.ui.theme.FocusCoralRed
import com.example.ui.theme.FocusCyan
import com.example.ui.theme.FocusCyanDark
import com.example.ui.theme.FocusGold
import com.example.ui.theme.FocusGreen
import com.example.ui.theme.FocusSlateBg
import com.example.ui.theme.FocusSurface
import com.example.ui.theme.FocusTextSecondary
import com.example.ui.viewmodel.FocusViewModel
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
    val whitelistedApps by viewModel.whitelistedApps.collectAsState()
    val context = LocalContext.current

    var showEmergencyConfirm by remember { mutableStateOf(false) }
    var emergencyPenaltyCountdown by remember { mutableIntStateOf(10) }

    // Emergency exit penalty timer
    LaunchedEffect(showEmergencyConfirm) {
        if (showEmergencyConfirm) {
            emergencyPenaltyCountdown = 10
            while (emergencyPenaltyCountdown > 0) {
                delay(1000)
                emergencyPenaltyCountdown--
            }
        }
    }

    // Auto navigate when timer reaches zero
    LaunchedEffect(timerState.isRunning, timerState.remainingSeconds) {
        if (!timerState.isRunning && timerState.remainingSeconds <= 0 && timerState.totalSeconds > 0) {
            onSessionComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusSlateBg)
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Subject Badge
                Box(
                    modifier = Modifier
                        .background(FocusSurface.copy(alpha = 0.85f), CircleShape)
                        .border(1.dp, FocusCyan.copy(alpha = 0.5f), CircleShape)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(FocusCyan, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = timerState.subjectName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                // Lock Mode Badge
                Box(
                    modifier = Modifier
                        .background(FocusAccentOrange.copy(alpha = 0.2f), CircleShape)
                        .border(1.dp, FocusAccentOrange, CircleShape)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = FocusAccentOrange,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = timerState.lockMode.title.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = FocusAccentOrange
                        )
                    }
                }
            }

            // Central Ring Clock Timer
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .testTag("timer_clock_ring"),
                contentAlignment = Alignment.Center
            ) {
                val progress = if (timerState.totalSeconds > 0) {
                    timerState.remainingSeconds.toFloat() / timerState.totalSeconds.toFloat()
                } else 1.0f

                // Animated Circular Ring Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
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
                            colors = listOf(FocusCyan, FocusCyanDark, FocusAccentOrange, FocusCyan)
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
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (timerState.isPaused) "PAUSED" else "FOCUS SESSION ACTIVE",
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                        color = if (timerState.isPaused) FocusGold else FocusGreen
                    )
                }
            }

            // Anti-Exit Shield Simulator Button (Simulate home/switch app)
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusSurface.copy(alpha = 0.9f)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.triggerDistractionWarning()
                    }
                    .testTag("test_anti_exit_btn")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = FocusCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Anti-Exit Shield Active",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Tap to simulate home/app switch block",
                                style = MaterialTheme.typography.bodySmall,
                                color = FocusTextSecondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(FocusCyan.copy(alpha = 0.2f), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("TEST LOCK", style = MaterialTheme.typography.labelSmall, color = FocusCyan)
                    }
                }
            }

            // Allowed Apps Launcher Bar
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "ALLOWED STUDY APPS",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                    color = FocusTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(whitelistedApps) { app ->
                        Box(
                            modifier = Modifier
                                .background(FocusSurface.copy(alpha = 0.9f), RoundedCornerShape(14.dp))
                                .border(1.dp, FocusCyan.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                .clickable {
                                    Toast
                                        .makeText(
                                            context,
                                            "Launching ${app.appName} within Whitelist",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = FocusGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = app.appName.take(12),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Action Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pause / Resume
                Button(
                    onClick = {
                        if (timerState.isPaused) viewModel.resumeSession() else viewModel.pauseSession()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("pause_resume_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (timerState.isPaused) FocusGreen else FocusSurface,
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
                        viewModel.completeFocusSession()
                        onSessionComplete()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("finish_session_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FocusAccentOrange,
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

        // STRICT LOCK ANTI-EXIT OVERLAY DIALOG
        if (showLockOverlay) {
            Dialog(
                onDismissRequest = { /* Modal lock - require explicit action */ },
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                )
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = FocusSurface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(FocusCoralRed.copy(alpha = 0.2f), CircleShape)
                                .border(2.dp, FocusCoralRed, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = FocusCoralRed,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "⚠️ FOCUS SESSION IS ACTIVE",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            ),
                            color = FocusCoralRed
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "You are trying to leave your study session. Stay disciplined!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .background(FocusSlateBg, RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "Time Remaining: ${timerState.remainingSeconds / 60}m ${timerState.remainingSeconds % 60}s",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = FocusCyan
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Return to Focus Button
                        Button(
                            onClick = { viewModel.dismissLockOverlay() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("return_to_focus_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = FocusCyan, contentColor = Color.Black),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("RETURN TO FOCUS SESSION", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Emergency Exit Button
                        OutlinedButton(
                            onClick = { showEmergencyConfirm = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = FocusCoralRed),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("EMERGENCY EXIT", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        // Emergency Penalty Confirmation Dialog
        if (showEmergencyConfirm) {
            Dialog(
                onDismissRequest = { showEmergencyConfirm = false }
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = FocusSlateBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Emergency Exit Penalty",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Exiting early will reset your daily focus streak! Please wait for the cooling timer.",
                            style = MaterialTheme.typography.bodySmall,
                            color = FocusTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (emergencyPenaltyCountdown > 0) "Wait $emergencyPenaltyCountdown Seconds..." else "Exit Unlocked",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (emergencyPenaltyCountdown > 0) FocusGold else FocusGreen
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
                            colors = ButtonDefaults.buttonColors(containerColor = FocusCoralRed)
                        ) {
                            Text("CONFIRM EARLY EXIT")
                        }
                    }
                }
            }
        }
    }
}
