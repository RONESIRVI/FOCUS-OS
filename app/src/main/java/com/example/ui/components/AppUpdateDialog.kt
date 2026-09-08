package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import com.example.util.update.AppUpdateManager
import com.example.util.update.UpdateInfo
import com.example.util.update.UpdateStatus

@Composable
fun AppUpdateDialog(
    status: UpdateStatus,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    when (status) {
        is UpdateStatus.UpdateAvailable -> {
            UpdateAvailableContent(
                info = status.info,
                onUpdateClick = { AppUpdateManager.startDownload(context, status.info) },
                onDismiss = onDismiss
            )
        }
        is UpdateStatus.Downloading -> {
            DownloadingContent(
                percent = status.progressPercent,
                downloadedMB = status.downloadedMB,
                totalMB = status.totalMB
            )
        }
        is UpdateStatus.ReadyToInstall -> {
            ReadyToInstallContent(
                info = status.info,
                onInstallClick = { AppUpdateManager.installApk(context, status.apkFile) },
                onDismiss = onDismiss
            )
        }
        is UpdateStatus.NoUpdate -> {
            NoUpdateContent(
                currentVersion = status.currentVersion,
                onDismiss = onDismiss
            )
        }
        is UpdateStatus.Error -> {
            ErrorUpdateContent(
                message = status.message,
                onDismiss = onDismiss
            )
        }
        is UpdateStatus.Checking -> {
            CheckingUpdateContent()
        }
        is UpdateStatus.Idle -> {
            // Nothing
        }
    }
}

@Composable
private fun UpdateAvailableContent(
    info: UpdateInfo,
    onUpdateClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = { if (!info.isMandatory) onDismiss() },
        properties = DialogProperties(dismissOnBackPress = !info.isMandatory, dismissOnClickOutside = !info.isMandatory)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusSurface),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, FocusPrimary.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Surface(
                    shape = CircleShape,
                    color = FocusPrimary.copy(alpha = 0.15f),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = "New Update Available",
                            tint = FocusPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = FocusPrimary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "NEW VERSION AVAILABLE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                        color = FocusPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Focus OS ${info.versionName}",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Text(
                    text = "Size: ~${info.fileSizeMB} • ${if (info.releaseDate.isNotEmpty()) info.releaseDate else "Direct APK Update"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = FocusTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Release notes box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FocusSurfaceVariant)
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = FocusPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "WHAT'S NEW & IMPROVEMENTS:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = FocusPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = info.releaseNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = FocusTextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Reassuring Data Safety notice
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF10281C))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = FocusPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Your data, active records, timetable & stats will remain 100% safe and intact.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = FocusPrimary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onUpdateClick,
                    colors = ButtonDefaults.buttonColors(containerColor = FocusPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("UPDATE NOW (DIRECT DOWNLOAD)", fontWeight = FontWeight.Bold)
                }

                if (!info.isMandatory) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Remind Me Later", color = FocusTextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadingContent(
    percent: Int,
    downloadedMB: Float,
    totalMB: Float
) {
    Dialog(
        onDismissRequest = { /* Cannot dismiss during active download */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusSurface),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, FocusPrimary.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = FocusPrimary.copy(alpha = 0.15f),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { if (percent > 0) percent / 100f else 0.05f },
                            color = FocusPrimary,
                            trackColor = FocusSurfaceVariant,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = FocusPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Downloading Update...",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (percent > 0) "$percent% Completed • ${String.format("%.1f", downloadedMB)} MB / ${String.format("%.1f", totalMB)} MB" else "Connecting to download stream...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FocusTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                LinearProgressIndicator(
                    progress = { if (percent > 0) percent / 100f else 0f },
                    color = FocusPrimary,
                    trackColor = FocusSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Once downloaded, the installation prompt will appear directly on screen.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = FocusTextSecondary
                )
            }
        }
    }
}

@Composable
private fun ReadyToInstallContent(
    info: UpdateInfo,
    onInstallClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusSurface),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, FocusPrimary.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = FocusPrimary.copy(alpha = 0.15f),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = FocusPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Download Complete!",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Update is ready to install. Tap Install below to upgrade to ${info.versionName}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FocusTextSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onInstallClick,
                    colors = ButtonDefaults.buttonColors(containerColor = FocusPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.InstallMobile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("INSTALL UPDATE NOW", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = FocusTextSecondary)
                }
            }
        }
    }
}

@Composable
private fun CheckingUpdateContent() {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusSurface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(color = FocusPrimary, modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Checking for Updates...", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    Text("Connecting to release server", style = MaterialTheme.typography.bodySmall, color = FocusTextSecondary)
                }
            }
        }
    }
}

@Composable
private fun NoUpdateContent(
    currentVersion: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = FocusPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("You're Up to Date!", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    text = "You are currently running the latest version of Focus OS ($currentVersion).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FocusTextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "✅ All system security shields, timetables and lockdown features are fully active.",
                    style = MaterialTheme.typography.bodySmall,
                    color = FocusPrimary
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Great", color = FocusPrimary, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = FocusSurface
    )
}

@Composable
private fun ErrorUpdateContent(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFFF5252))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Update Check Status", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Text(text = message, style = MaterialTheme.typography.bodyMedium, color = FocusTextSecondary)
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = FocusPrimary, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = FocusSurface
    )
}
