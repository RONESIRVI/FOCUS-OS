package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.ui.theme.FocusWarning
import com.example.ui.theme.FocusPrimary
import com.example.ui.theme.FocusPrimary
import com.example.ui.theme.FocusPrimary
import com.example.ui.theme.FocusBackground
import com.example.ui.theme.FocusSurface
import com.example.ui.theme.FocusSurfaceVariant
import com.example.ui.theme.FocusTextSecondary
import com.example.ui.viewmodel.FocusViewModel

@Composable
fun HomeScreen(
    viewModel: FocusViewModel,
    onNavigateToSetup: () -> Unit,
    onNavigateToScheduleCreate: () -> Unit,
    onNavigateToAppSelector: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToTimer: () -> Unit
) {
    val stats by viewModel.summaryStats.collectAsState()
    val scheduledSessions by viewModel.scheduledSessions.collectAsState()
    val timerState by viewModel.timerState.collectAsState()
    val sessions by viewModel.allSessions.collectAsState()
    val subjects by viewModel.allSubjects.collectAsState()
    val whitelistedApps by viewModel.whitelistedAppsManual.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = FocusSurface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF0284C7).copy(alpha = 0.3f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(FocusPrimary.copy(alpha = 0.2f), CircleShape)
                                        .border(1.dp, FocusPrimary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "Focus Shield",
                                        tint = FocusPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "FOCUS OS",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Control Your Attention.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = FocusTextSecondary
                                    )
                                }
                            }

                            // Active Session pill if running
                            if (timerState.isRunning) {
                                Button(
                                    onClick = onNavigateToTimer,
                                    colors = ButtonDefaults.buttonColors(containerColor = FocusWarning),
                                    shape = CircleShape,
                                    modifier = Modifier.testTag("resume_active_session_btn")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ACTIVE", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Big Start Focus Button
                        Button(
                            onClick = onNavigateToSetup,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("start_focus_btn"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FocusPrimary,
                                contentColor = Color.Black
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Start Focus",
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "START FOCUS SESSION",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dashboard Stats Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Streak Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = FocusSurface),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = FocusWarning,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Streak", style = MaterialTheme.typography.labelMedium, color = FocusTextSecondary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${stats.currentStreakDays} Days",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                // Total Focus Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = FocusSurface),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Total Focus",
                                tint = FocusPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Total Focused", style = MaterialTheme.typography.labelMedium, color = FocusTextSecondary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${stats.totalFocusHours}h",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                // Focus Score Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = FocusSurface),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Score",
                                tint = FocusPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Focus Score", style = MaterialTheme.typography.labelMedium, color = FocusTextSecondary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${stats.focusScore}/100",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Allowed Apps Quick Access Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToAppSelector() },
                colors = CardDefaults.cardColors(containerColor = FocusSurface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Allowed Apps During Focus",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                        }
                        Text(
                            text = "${whitelistedApps.size} Allowed",
                            style = MaterialTheme.typography.labelMedium,
                            color = FocusPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(whitelistedApps) { app ->
                            Box(
                                modifier = Modifier
                                    .background(FocusSurfaceVariant, CircleShape)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = FocusPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = app.appName.take(16),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Study Tasks / Subjects Quick Chips
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Study Subjects & Tasks",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "View Analytics",
                        style = MaterialTheme.typography.labelMedium,
                        color = FocusPrimary,
                        modifier = Modifier.clickable { onNavigateToStats() }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(subjects) { subject ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = FocusSurface),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.clickable {
                                viewModel.updateSetup(
                                    sessionName = subject.name,
                                    subjectName = subject.name
                                )
                                onNavigateToSetup()
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(
                                            try {
                                                Color(android.graphics.Color.parseColor(subject.categoryColorHex))
                                            } catch (e: Exception) {
                                                FocusPrimary
                                            },
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = subject.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Continuous Streak Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = FocusSurface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = FocusWarning,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "FOCUS STREAK",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = FocusWarning
                            )
                        }
                        Text("${stats.currentStreakDays} DAYS ACTIVE", style = MaterialTheme.typography.labelSmall, color = FocusPrimary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val currentDayInWeek = stats.currentStreakDays % 7
                        for (idx in 0 until 7) {
                            val isCompleted = idx < currentDayInWeek
                            val isActive = idx == currentDayInWeek
                            
                            val label = when {
                                isCompleted -> "✓"
                                isActive -> "🔥"
                                else -> "🔒"
                            }
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 2.dp)
                                    .height(36.dp)
                                    .background(
                                        if (isCompleted) FocusPrimary.copy(alpha = 0.2f)
                                        else if (isActive) FocusWarning.copy(alpha = 0.3f)
                                        else FocusSurfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = if (isActive) 1.dp else 0.dp,
                                        color = if (isActive) FocusWarning else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "D${idx + 1} $label",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 10.sp
                                    ),
                                    color = if (isCompleted) FocusPrimary
                                    else if (isActive) FocusWarning
                                    else FocusTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Today's Schedule Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TODAY'S SCHEDULE",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = FocusTextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (scheduledSessions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No sessions scheduled.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = FocusTextSecondary
                            )
                        }
                    } else {
                        scheduledSessions.take(5).forEach { session ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(8.dp).background(FocusWarning, CircleShape))
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                val formatter = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
                                val timeStr = session.scheduledStartTime?.let { formatter.format(java.util.Date(it)) } ?: "Upcoming"
                                
                                Text(
                                    text = timeStr,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.width(80.dp)
                                )
                                
                                Text(
                                    text = session.subjectName.uppercase(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = { onNavigateToScheduleCreate() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ADD SESSION", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

    }
}
