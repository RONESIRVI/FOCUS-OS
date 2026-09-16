sed -i '126a \    val showPendingLockOverlay by viewModel.showPendingLockOverlay.collectAsState()\n    val pendingSessionNameOverlay by viewModel.pendingSessionNameOverlay.collectAsState()\n    val pendingSessionIdOverlay by viewModel.pendingSessionIdOverlay.collectAsState()' app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt

cat << 'INNER_EOF' > notif_block.txt

        // In-Session Notification for overlapping scheduled sessions
        androidx.compose.animation.AnimatedVisibility(
            visible = showPendingLockOverlay,
            enter = androidx.compose.animation.slideInVertically(initialOffsetY = { -it }) + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { -it }) + androidx.compose.animation.fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp, start = 16.dp, end = 16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FocusSurface.copy(alpha = 0.95f)),
                border = BorderStroke(1.dp, FocusPrimary.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = FocusWarning, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Scheduled Session Starting", color = FocusPrimary, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            Text(pendingSessionNameOverlay, color = FocusTextPrimary, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
                        }
                        IconButton(onClick = { viewModel.dismissLockOverlay() }) {
                            Icon(Icons.Default.Clear, contentDescription = "Dismiss", tint = FocusTextSecondary)
                        }
                    }
                }
            }
        }
INNER_EOF

sed -i '1742r notif_block.txt' app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt
