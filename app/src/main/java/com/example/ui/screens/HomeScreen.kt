package com.example.ui.screens

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    LaunchedEffect(Unit) {
        userName = sharedPrefs.getString("USER_NAME", "Focus Student") ?: "Focus Student"
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
                Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = FocusTextPrimary)
                Text(
                    text = "FOCUS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    color = FocusPrimary
                )
                Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notifications", tint = FocusTextPrimary)
            }
        }

        // Greeting
        item {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = "Hi, $userName 👋",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = FocusTextPrimary
                )
                Text(
                    text = "Stay focused. Stay consistent.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FocusTextSecondary
                )
            }
        }

        // Today's Progress Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = FocusSurface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AutoGraph, contentDescription = null, tint = FocusPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("TODAY'S PROGRESS", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = FocusTextSecondary)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val hours = stats.todayFocusSeconds / 3600
                    val minutes = (stats.todayFocusSeconds % 3600) / 60
                    
                    Text(
                        text = "${hours}h ${minutes}m",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = FocusTextPrimary
                    )
                    Text(
                        text = "Focus Time",
                        style = MaterialTheme.typography.bodySmall,
                        color = FocusTextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Sessions", style = MaterialTheme.typography.labelSmall, color = FocusTextSecondary)
                            Text("${stats.totalSessions}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = FocusTextPrimary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Goal", style = MaterialTheme.typography.labelSmall, color = FocusTextSecondary)
                            Text("5h", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = FocusTextPrimary)
                        }
                    }
                }
            }
        }

        // Focus Streak
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = FocusSurface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "🔥 ${stats.currentStreakDays} DAYS",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = FocusWarning
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Simple Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(FocusSurfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .fillMaxHeight()
                                .background(FocusWarning)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Keep going. You're strong.",
                        style = MaterialTheme.typography.bodySmall,
                        color = FocusTextSecondary
                    )
                }
            }
        }

        // Today's Schedule Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S SCHEDULE",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = FocusTextPrimary
                )
                Text(
                    text = "+ ADD",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = FocusPrimary,
                    modifier = Modifier.clickable { onNavigateToScheduleCreate() }
                )
            }
        }

        // Schedule Items (Dynamic based on data)
        item {
            if (scheduledSessions.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = FocusSurfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No strict sessions scheduled today.", color = FocusTextSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    scheduledSessions.take(3).forEach { session ->
                        val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
                        val startTimeStr = session.scheduledStartTime?.let { formatter.format(Date(it)) } ?: "Upcoming"
                        val endTimeStr = session.scheduledEndTime?.let { formatter.format(Date(it)) } ?: ""
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = FocusSurface),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FocusOutline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = FocusWarning, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("UPCOMING", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = FocusWarning)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("📚 ${session.subjectName.uppercase()}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = FocusTextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("$startTimeStr — $endTimeStr", style = MaterialTheme.typography.bodyMedium, color = FocusTextSecondary)
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = FocusPrimary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Strict Focus", style = MaterialTheme.typography.labelMedium, color = FocusPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        item {
            Text(
                text = "VIEW FULL SCHEDULE →",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = FocusTextSecondary,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { /* TODO: Navigate to schedule */ }
            )
        }

        // Quick Focus Session (Existing System)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusSurface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, FocusOutline),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 32.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = FocusPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("QUICK FOCUS", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = FocusTextPrimary)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Start a focus session anytime.\\nNo fixed schedule required.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FocusTextSecondary,
                        lineHeight = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Button(
                        onClick = onNavigateToSetup,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FocusPrimary, contentColor = FocusOnPrimary)
                    ) {
                        Text("START FOCUS SESSION", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(60.dp)) }
    }
}
