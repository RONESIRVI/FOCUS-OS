package com.example.ui.screens

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.data.model.LockMode
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import com.example.ui.theme.*
import com.example.ui.viewmodel.FocusViewModel
import com.example.util.LockPermissionHelper
import com.example.util.PermissionItemState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: FocusViewModel,
    onNavigateToAppSelector: (String) -> Unit
) {
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showAccessibilityDisclosure by remember { mutableStateOf(false) }
    var showUsageAccessDisclosure by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("Focus Student") }
    var profilePhotoUri by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("FocusPrefs", Context.MODE_PRIVATE)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            uri?.let {
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val file = File(context.filesDir, "profile_photo.jpg")
                    val outputStream = FileOutputStream(file)
                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()
                    
                    val newUri = Uri.fromFile(file).toString()
                    sharedPrefs.edit().putString("PROFILE_PHOTO_URI", newUri).apply()
                    profilePhotoUri = newUri
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    )


    var permissionsList by remember {
        mutableStateOf(LockPermissionHelper.getAllPermissionsStatus(context))
    }

    // Refresh permissions automatically when returning from Settings screen
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionsList = LockPermissionHelper.getAllPermissionsStatus(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        userName = sharedPrefs.getString("USER_NAME", "Focus Student") ?: "Focus Student"
        profilePhotoUri = sharedPrefs.getString("PROFILE_PHOTO_URI", null)
    }

    val grantedCount = permissionsList.count { it.isGranted }
    val totalCount = permissionsList.size
    val shieldPercentage = (grantedCount.toFloat() / totalCount.toFloat() * 100).toInt()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SETTINGS",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                    color = FocusPrimary
                )
                IconButton(onClick = { showInfoDialog = true }) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Permission Architecture Info", tint = FocusPrimary)
                }
            }
        }

        // Profile Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clickable {
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    ) {
                        if (profilePhotoUri != null) {
                            AsyncImage(
                                model = profilePhotoUri,
                                contentDescription = "Profile Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(60.dp)
                                    .align(Alignment.Center)
                                    .clip(CircleShape)
                                    .border(2.dp, FocusPrimary, CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(FocusSurfaceVariant, CircleShape)
                                    .align(Alignment.Center),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = FocusTextSecondary, modifier = Modifier.size(32.dp))
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(FocusPrimary, CircleShape)
                                .align(Alignment.BottomEnd)
                                .border(2.dp, FocusSurface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Shield, contentDescription = "Pro Protection", tint = Color.Black, modifier = Modifier.size(12.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(userName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = FocusTextPrimary)
                        Text("Strict Lockdown Mode Active", style = MaterialTheme.typography.bodySmall, color = FocusPrimary)
                    }
                    IconButton(onClick = { showEditProfileDialog = true }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile", tint = FocusTextSecondary)
                    }
                }
            }
        }

        // Shield Health Overview Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (grantedCount >= 8) Color(0xFF10281C) else Color(0xFF281E10)
                ),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (grantedCount >= 8) FocusPrimary.copy(alpha = 0.5f) else Color(0xFFFFB74D)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (grantedCount >= 8) Icons.Default.VerifiedUser else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (grantedCount >= 8) FocusPrimary else Color(0xFFFFB74D),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "SHIELD PROTECTION LEVEL",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                    color = FocusTextSecondary
                                )
                                Text(
                                    text = if (grantedCount == totalCount) "Maximum Lockdown Ready (10/10)" else "$grantedCount of $totalCount Permissions Active",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (grantedCount >= 8) FocusPrimary else Color(0xFFFFB74D)
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (grantedCount >= 8) FocusPrimary.copy(alpha = 0.2f) else Color(0xFFFFB74D).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "$shieldPercentage%",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                                color = if (grantedCount >= 8) FocusPrimary else Color(0xFFFFB74D),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { grantedCount.toFloat() / totalCount.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (grantedCount >= 8) FocusPrimary else Color(0xFFFFB74D),
                        trackColor = FocusSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (grantedCount == totalCount) "All 10 required services and permissions are active. Your study sessions are 100% distraction-proof." else "Setup all listed permissions below for bulletproof app blocking and background stability.",
                        style = MaterialTheme.typography.bodySmall,
                        color = FocusTextSecondary
                    )
                }
            }
        }

        // App Blocking System
        item {
            SettingsSectionTitle("APP BLOCKING SYSTEM")
            SettingsCard {
                SettingsClickableItem(
                    icon = Icons.Default.Apps,
                    title = "Manual Focus Whitelist",
                    subtitle = "Allowed apps during Quick Focus sessions",
                    onClick = { onNavigateToAppSelector("MANUAL") }
                )
                Divider(color = FocusSurfaceVariant)
                SettingsClickableItem(
                    icon = Icons.Default.AppRegistration,
                    title = "Strict Schedule Whitelist",
                    subtitle = "Allowed apps during Strict Scheduled Focus",
                    onClick = { onNavigateToAppSelector("STRICT") }
                )
            }
        }

        // Lock & Security Status Mode Selector
        item {
            val setup by viewModel.setupState.collectAsState()
            SettingsSectionTitle("LOCK MODE SHIELD")
            SettingsCard {
                val selectableModes = LockMode.entries.filter { it != LockMode.NORMAL }
                selectableModes.forEachIndexed { index, mode ->
                    val isSelected = setup.lockMode == mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.updateSetup(lockMode = mode) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.updateSetup(lockMode = mode) },
                                colors = RadioButtonDefaults.colors(selectedColor = FocusPrimary, unselectedColor = FocusTextSecondary)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(text = mode.title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = if (isSelected) FocusPrimary else FocusTextPrimary)
                                Text(text = mode.description, style = MaterialTheme.typography.bodySmall, color = FocusTextSecondary)
                            }
                        }
                    }
                    if (index < selectableModes.size - 1) {
                        Divider(color = FocusSurfaceVariant)
                    }
                }
            }
        }

        // Section 1: 🔴 Most Important (Core App-Blocking)
        item {
            SettingsSectionTitle("🔴 CORE APP-BLOCKING ENGINE (MOST IMPORTANT)")
            Text(
                text = "These core permissions detect distracting apps and display the focus shield overlay during study.",
                style = MaterialTheme.typography.bodySmall,
                color = FocusTextSecondary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }

        item {
            SettingsCard {
                // 1. Usage Stats
                PermissionRowItem(
                    number = "1",
                    title = "Usage Access (PACKAGE_USAGE_STATS)",
                    badgeText = "Main Blocker",
                    description = "Monitors foreground app switching in real-time to detect and block unauthorized apps during study.",
                    isGranted = LockPermissionHelper.hasUsageStatsPermission(context),
                    actionLabel = "Grant Access",
                    onAction = { showUsageAccessDisclosure = true }
                )
                Divider(color = FocusSurfaceVariant)

                // 2. Draw Over Apps
                PermissionRowItem(
                    number = "2",
                    title = "Draw Over Other Apps (Overlay)",
                    badgeText = "Shield Lock",
                    description = "Instantly brings the Focus lock shield over distracting apps when tapped during active sessions.",
                    isGranted = LockPermissionHelper.hasOverlayPermission(context),
                    actionLabel = "Enable Overlay",
                    onAction = { LockPermissionHelper.openOverlaySettings(context) }
                )
                Divider(color = FocusSurfaceVariant)

                // 3. Query Packages
                PermissionRowItem(
                    number = "3",
                    title = "Installed Apps Discovery",
                    badgeText = "Launcher Queries",
                    description = "Discovers installed launcher apps using secure queries API for custom study whitelisting.",
                    isGranted = LockPermissionHelper.hasQueryAllPackagesPermission(context),
                    actionLabel = "Configured",
                    onAction = { }
                )
            }
        }

        // Section 2: 🟠 Background Reliability & Persistence
        item {
            SettingsSectionTitle("🟠 BACKGROUND PERSISTENCE & RELIABILITY")
            Text(
                text = "Ensures background timer stability and protects against Android OEM battery cleaners.",
                style = MaterialTheme.typography.bodySmall,
                color = FocusTextSecondary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }

        item {
            SettingsCard {
                // 4. Foreground Service
                PermissionRowItem(
                    number = "4",
                    title = "Foreground Service",
                    badgeText = "Active Service",
                    description = "Maintains the focus protection engine and binaural audio synthesizer in background.",
                    isGranted = true,
                    actionLabel = "Active",
                    onAction = { }
                )
                Divider(color = FocusSurfaceVariant)

                // 5. Special Use FGS
                PermissionRowItem(
                    number = "5",
                    title = "Foreground Service Type: specialUse",
                    badgeText = "High Priority",
                    description = "Designated high-priority Android runtime classification for continuous study lockdown.",
                    isGranted = true,
                    actionLabel = "Active",
                    onAction = { }
                )
                Divider(color = FocusSurfaceVariant)

                // 6. Battery Optimization
                PermissionRowItem(
                    number = "6",
                    title = "Ignore Battery Optimisation",
                    badgeText = "Anti-Kill",
                    description = "Prevents aggressive Android Doze mode and OEM task killers from terminating focus sessions.",
                    isGranted = LockPermissionHelper.isIgnoringBatteryOptimizations(context),
                    actionLabel = "Exempt Battery",
                    onAction = { LockPermissionHelper.requestIgnoreBatteryOptimizations(context) }
                )
                Divider(color = FocusSurfaceVariant)

                // 7. Boot Startup
                PermissionRowItem(
                    number = "7",
                    title = "Run at Startup (RECEIVE_BOOT_COMPLETED)",
                    badgeText = "Auto Restore",
                    description = "Restores all scheduled study timers and alarms automatically after phone reboot.",
                    isGranted = LockPermissionHelper.hasBootPermission(context),
                    actionLabel = "Configured",
                    onAction = { }
                )
            }
        }

        // Section 3: 🟡 Supporting Lockdown Tools
        item {
            SettingsSectionTitle("🟡 SUPPORTING LOCKDOWN SERVICES")
            Text(
                text = "Provides instant lock screen overlay, alert alarms, and precise timer triggers.",
                style = MaterialTheme.typography.bodySmall,
                color = FocusTextSecondary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }

        item {
            SettingsCard {
                // 8. Draw Over Other Apps
                PermissionRowItem(
                    number = "8",
                    title = "Draw Over Other Apps (Overlay)",
                    badgeText = "Lock Shield",
                    description = "Draws full-screen study lock screen directly over blocked apps when opened.",
                    isGranted = LockPermissionHelper.hasOverlayPermission(context),
                    actionLabel = "Allow Overlay",
                    onAction = { LockPermissionHelper.openOverlaySettings(context) }
                )
                Divider(color = FocusSurfaceVariant)

                // 9. Notifications
                PermissionRowItem(
                    number = "9",
                    title = "Notifications (POST_NOTIFICATIONS)",
                    badgeText = "Timer Bar",
                    description = "Displays ongoing focus countdown in status bar and 2-minute pre-schedule reminders.",
                    isGranted = LockPermissionHelper.hasNotificationPermission(context),
                    actionLabel = "Enable Alerts",
                    onAction = { LockPermissionHelper.openNotificationSettings(context) }
                )
                Divider(color = FocusSurfaceVariant)

                // 10. Schedule Exact Alarms
                PermissionRowItem(
                    number = "10",
                    title = "Schedule Exact Alarms",
                    badgeText = "Precise Alarms",
                    description = "Triggers strict scheduled focus sessions at the exact planned second.",
                    isGranted = LockPermissionHelper.canScheduleExactAlarms(context),
                    actionLabel = "Allow Alarms",
                    onAction = { LockPermissionHelper.openExactAlarmSettings(context) }
                )
            }
        }

        // Focus Preferences
        item {
            SettingsSectionTitle("FOCUS PREFERENCES")
            SettingsCard {
                SettingsItem(icon = Icons.Default.Timer, title = "Default Duration", valueText = "45 Min")
                Divider(color = FocusSurfaceVariant)
                SettingsItem(icon = Icons.Default.Tune, title = "Default Mode", valueText = "Strict Lock")
                Divider(color = FocusSurfaceVariant)
                SettingsItem(icon = Icons.Default.Headphones, title = "Focus Audio", valueText = "Deep Space")
            }
        }

        // Strict Focus Rules
        item {
            SettingsSectionTitle("STRICT FOCUS RULES")
            SettingsCard {
                SettingsToggleItem(icon = Icons.Default.CameraAlt, title = "Start Verification", subtitle = "Require study desk photo to start", defaultChecked = true)
                Divider(color = FocusSurfaceVariant)
                SettingsToggleItem(icon = Icons.Default.Face, title = "End Verification", subtitle = "Require selfie proof to complete", defaultChecked = true)
                Divider(color = FocusSurfaceVariant)
                SettingsToggleItem(icon = Icons.Default.Warning, title = "Security Audio Siren", subtitle = "Plays warning sound on distraction", defaultChecked = true)
            }
        }


        // Appearance
        item {
            SettingsSectionTitle("APPEARANCE")
            SettingsCard {
                SettingsToggleItem(icon = Icons.Default.DarkMode, title = "OLED Dark Mode", defaultChecked = true)
                Divider(color = FocusSurfaceVariant)
                SettingsItem(icon = Icons.Default.Palette, title = "Accent Theme", valueText = "Electric Emerald")
            }
        }

        // Privacy & Google Play Policy Compliance
        item {
            SettingsSectionTitle("🔒 PRIVACY & PLAY STORE COMPLIANCE")
            SettingsCard {
                SettingsClickableItem(
                    icon = Icons.Default.Policy,
                    title = "Privacy & Data Policy",
                    subtitle = "100% Offline, Zero Tracking, No Keystroke Logging",
                    onClick = { showPrivacyPolicyDialog = true }
                )
                Divider(color = FocusSurfaceVariant)
                SettingsClickableItem(
                    icon = Icons.Default.Security,
                    title = "Usage Access Disclosure",
                    subtitle = "Google Play Prominent Disclosure & Strict Scope",
                    onClick = { showUsageAccessDisclosure = true }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(100.dp)) }
    }

    // Permission Architecture Info Dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = FocusPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("App-Blocking Architecture", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "How FOCUS OS reliably blocks distracting apps during study:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = FocusPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Official Usage Access (PACKAGE_USAGE_STATS) monitors foreground app changes with zero privacy invasion.\n\n" +
                                "• When a blocked distracting app is opened during active study, the system instantly raises the Focus Lock screen.\n\n" +
                                "• Foreground Service + Battery Optimization Exemption work in synergy to ensure continuous, unkillable background protection.",
                        style = MaterialTheme.typography.bodySmall,
                        color = FocusTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Got It", color = FocusPrimary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = FocusSurface
        )
    }

    if (showEditProfileDialog) {
        var tempName by remember { mutableStateOf(userName) }
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile Name", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = FocusPrimary
                    ),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    userName = tempName
                    sharedPrefs.edit().putString("USER_NAME", tempName).apply()
                    showEditProfileDialog = false
                }) {
                    Text("Save", color = FocusPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = FocusTextSecondary)
                }
            },
            containerColor = FocusSurface
        )
    }

    // Google Play Prominent Disclosure Dialog for Usage Access
    if (showUsageAccessDisclosure) {
        AlertDialog(
            onDismissRequest = { showUsageAccessDisclosure = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = FocusPrimary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Usage Access Disclosure",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Why Usage Access is needed:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = FocusPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "FOCUS OS requires Usage Access solely to detect which application is running in the foreground during an active study lockdown session.\n\n" +
                                "• Used strictly to trigger study lock screens if a blocked application is launched.\n\n" +
                                "• Does NOT track browsing history, accounts, or personal activity.\n\n" +
                                "• Processed 100% locally on your device with complete data privacy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = FocusTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUsageAccessDisclosure = false
                        LockPermissionHelper.openUsageStatsSettings(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FocusPrimary, contentColor = Color.Black)
                ) {
                    Text("Agree & Open Settings", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUsageAccessDisclosure = false }) {
                    Text("Decline", color = FocusTextSecondary)
                }
            },
            containerColor = FocusSurface
        )
    }

    // Privacy & Transparency Dialog
    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Policy,
                    contentDescription = null,
                    tint = FocusPrimary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Privacy & Policy Compliance",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Google Play Policy Compliance Assurances:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = FocusPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. Zero Dangerous Interception: No sensitive accessibility logging or keylogging.\n\n" +
                                "2. Official Usage Access API: Monitors foreground apps without reading screen text, messages, or credentials.\n\n" +
                                "3. Foreground Service: Declared with 'specialUse' for student focus session lockdown.\n\n" +
                                "4. Zero External Transmission: All study stats, custom notes, and app whitelists remain strictly on your local device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = FocusTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyPolicyDialog = false }) {
                    Text("Close", color = FocusPrimary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = FocusSurface
        )
    }
}

@Composable
fun PermissionRowItem(
    number: String,
    title: String,
    badgeText: String,
    description: String,
    isGranted: Boolean,
    actionLabel: String,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    shape = CircleShape,
                    color = if (isGranted) FocusPrimary.copy(alpha = 0.15f) else Color(0xFFFF5252).copy(alpha = 0.15f),
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = number,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                            color = if (isGranted) FocusPrimary else Color(0xFFFF5252)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = FocusTextPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = FocusSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = FocusTextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (isGranted) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = FocusPrimary.copy(alpha = 0.15f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Granted",
                            tint = FocusPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ACTIVE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = FocusPrimary
                        )
                    }
                }
            } else {
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF5252),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = FocusTextSecondary
        )
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
        color = FocusTextSecondary,
        modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FocusSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            content()
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    valueText: String? = null,
    valueColor: Color = FocusTextSecondary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = FocusPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, color = FocusTextPrimary)
                if (subtitle != null) {
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = FocusTextSecondary)
                }
            }
        }
        if (valueText != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = valueText, style = MaterialTheme.typography.bodyMedium, color = valueColor)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = FocusTextSecondary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(imageVector = icon, contentDescription = null, tint = FocusPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, color = FocusTextPrimary)
                if (subtitle != null) {
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = FocusTextSecondary)
                }
            }
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = FocusTextSecondary, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    defaultChecked: Boolean = false
) {
    var checked by remember { mutableStateOf(defaultChecked) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(imageVector = icon, contentDescription = null, tint = FocusPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, color = FocusTextPrimary)
                if (subtitle != null) {
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = FocusTextSecondary)
                }
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = { checked = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = FocusBackground,
                checkedTrackColor = FocusPrimary,
                uncheckedThumbColor = FocusTextSecondary,
                uncheckedTrackColor = FocusSurfaceVariant
            )
        )
    }
}
