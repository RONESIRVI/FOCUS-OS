package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FocusSession
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudySummaryStats
import java.text.SimpleDateFormat
import java.util.*

data class AppNotification(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val icon: ImageVector,
    val iconTint: Color,
    val idString: String = java.util.UUID.randomUUID().toString(),
    val tag: String,
    val actionText: String? = null,
    val onAction: (() -> Unit)? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterDialog(
    scheduledSessions: List<FocusSession>,
    summaryStats: StudySummaryStats,
    dismissedIds: Set<String>,
    onDismissNotification: (String) -> Unit,
    onClearAll: (List<String>) -> Unit,
    onDismiss: () -> Unit,
    onStartScheduledSession: (FocusSession) -> Unit,
    onOpenShield: () -> Unit,
    onOpenSchedule: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    
    // Build real dynamic notifications based on live data
    val notifications = remember(scheduledSessions, summaryStats, dismissedIds) {
        val list = mutableListOf<AppNotification>()
        

        // 1. Scheduled session alerts
        scheduledSessions.forEach { session ->
            val timeStr = session.scheduledStartTime?.let { formatter.format(Date(it)) } ?: "Today"
            list.add(
                AppNotification(
                    id = "schedule_${session.id}",
                    title = "Scheduled: ${session.subjectName}",
                    description = "Session '${session.sessionName}' is set for $timeStr. Strict lock & camera verification enabled.",
                    timestamp = "TODAY • $timeStr",
                    icon = Icons.Default.Schedule,
                    iconTint = FocusWarning,
                    tag = "TIMETABLE",
                    actionText = "START SESSION NOW",
                    onAction = { onStartScheduledSession(session) }
                )
            )
        }

        // 2. Daily Goal Alert
        val goalHours = 5
        val completedHours = summaryStats.todayFocusSeconds / 3600
        val completedMinutes = (summaryStats.todayFocusSeconds % 3600) / 60
        val progressPercent = ((summaryStats.todayFocusSeconds.toFloat() / (goalHours * 3600f)) * 100).toInt().coerceIn(0, 100)
        
        list.add(
            AppNotification(
                id = "daily_goal",
                title = "Daily Study Goal ($progressPercent% Complete)",
                description = if (progressPercent >= 100)
                    "🎉 Congratulations! You achieved your 5-hour daily study target today ($completedHours h $completedMinutes m)!"
                else
                    "You've focused for ${completedHours}h ${completedMinutes}m today out of your 5h goal. Keep going strong!",
                timestamp = "DAILY TRACKER",
                icon = Icons.Default.Flag,
                iconTint = if (progressPercent >= 100) FocusPrimary else FocusWarning,
                tag = "GOAL"
            )
        )

        // 3. Shield Status Notification
        list.add(
            AppNotification(
                id = "shield_status",
                title = "10-Permission Anti-Distraction Shield",
                description = "Accessibility Service & Usage Blocker are ready to enforce strict anti-cheat app lockdown.",
                timestamp = "SECURITY METER",
                icon = Icons.Default.Shield,
                iconTint = FocusPrimary,
                tag = "SHIELD",
                actionText = "CHECK SHIELD STATUS",
                onAction = onOpenShield
            )
        )

        // 4. Streak Alert
        list.add(
            AppNotification(
                id = "streak_alert",
                title = "Study Streak: ${summaryStats.currentStreakDays} ${if (summaryStats.currentStreakDays == 1) "Day" else "Days"}",
                description = if (summaryStats.currentStreakDays == 0)
                    "Start and complete a focus session today to ignite your consecutive day streak!"
                else
                    "🔥 Keep your streak burning! Complete today's focus goals to maintain your momentum.",
                timestamp = "STREAK",
                icon = Icons.Default.LocalFireDepartment,
                iconTint = FocusWarning,
                tag = "STREAK"
            )
        )

        list.filter { it.id !in dismissedIds }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "NOTIFICATIONS & ALERTS",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = FocusWarning,
                                modifier = Modifier.size(22.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${notifications.size}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    },
                    actions = {
                        TextButton(onClick = { onClearAll(notifications.map { it.id }) }) {
                            Text("Clear All", color = FocusTextSecondary, fontSize = 12.sp)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = FocusSurface,
                        titleContentColor = Color.White
                    )
                )
            },
            containerColor = FocusBackground
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                items(notifications, key = { it.id }) { notif ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = FocusSurface),
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FocusOutline),
                        modifier = Modifier.fillMaxWidth().testTag("notification_card_${notif.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(notif.iconTint.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = notif.icon,
                                            contentDescription = null,
                                            tint = notif.iconTint,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = notif.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = FocusSurfaceVariant
                                    ) {
                                        Text(
                                            text = notif.tag,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                            color = notif.iconTint,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { onDismissNotification(notif.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = FocusTextSecondary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = notif.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = FocusTextSecondary,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = notif.timestamp,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = FocusTextSecondary.copy(alpha = 0.7f)
                                )

                                if (notif.actionText != null && notif.onAction != null) {
                                    FilledTonalButton(
                                        onClick = {
                                            onDismiss()
                                            notif.onAction.invoke()
                                        },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = notif.iconTint.copy(alpha = 0.2f),
                                            contentColor = notif.iconTint
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = notif.actionText,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FocusSurfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CLOSE NOTIFICATIONS", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
