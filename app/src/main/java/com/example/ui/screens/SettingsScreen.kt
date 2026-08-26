package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.data.model.LockMode
import com.example.ui.viewmodel.FocusViewModel

@Composable
fun SettingsScreen(
    viewModel: FocusViewModel,
    onNavigateToAppSelector: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(24.dp)) }
        
        item {
            Text(
                text = "SETTINGS",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                color = FocusPrimary
            )
        }

        // Profile Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clickable { /* TODO: Image Picker */ }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(FocusSurfaceVariant, CircleShape)
                                .align(Alignment.Center),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = FocusTextSecondary, modifier = Modifier.size(32.dp))
                        }
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(FocusPrimary, CircleShape)
                                .align(Alignment.BottomEnd)
                                .border(2.dp, FocusSurface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Upload Photo", tint = Color.Black, modifier = Modifier.size(12.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Focus Student", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = FocusTextPrimary)
                        Text("Free Plan", style = MaterialTheme.typography.bodyMedium, color = FocusPrimary)
                    }
                    IconButton(onClick = { /* TODO: Edit Profile */ }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile", tint = FocusTextSecondary)
                    }
                }
            }
        }
        
        // App Blocking System
        item {
            SettingsSectionTitle("APP BLOCKING SYSTEM")
            SettingsCard {
                SettingsClickableItem(
                    icon = Icons.Default.Apps, 
                    title = "Manual Focus Apps", 
                    subtitle = "Allowed apps during Quick Focus",
                    onClick = { onNavigateToAppSelector("MANUAL") }
                )
                Divider(color = FocusSurfaceVariant)
                SettingsClickableItem(
                    icon = Icons.Default.AppRegistration, 
                    title = "Strict Schedule Apps", 
                    subtitle = "Allowed apps during Strict Focus",
                    onClick = { onNavigateToAppSelector("STRICT") }
                )
            }
        }

        // Focus Preferences
        item {
            SettingsSectionTitle("FOCUS PREFERENCES")
            SettingsCard {
                SettingsItem(icon = Icons.Default.Timer, title = "Default Duration", valueText = "45 Min")
                Divider(color = FocusSurfaceVariant)
                SettingsItem(icon = Icons.Default.Tune, title = "Default Mode", valueText = "Strict")
                Divider(color = FocusSurfaceVariant)
                SettingsItem(icon = Icons.Default.Headphones, title = "Focus Audio", valueText = "Deep Space")
            }
        }

        // Strict Focus Rules
        item {
            SettingsSectionTitle("STRICT FOCUS RULES")
            SettingsCard {
                SettingsToggleItem(icon = Icons.Default.CameraAlt, title = "Start Verification", subtitle = "Require photo to start", defaultChecked = true)
                Divider(color = FocusSurfaceVariant)
                SettingsToggleItem(icon = Icons.Default.Face, title = "End Verification", subtitle = "Require selfie to end", defaultChecked = true)
                Divider(color = FocusSurfaceVariant)
                SettingsToggleItem(icon = Icons.Default.Warning, title = "Security Alerts", subtitle = "Loud alarm on blocked apps", defaultChecked = true)
            }
        }

        // Notifications
        item {
            SettingsSectionTitle("NOTIFICATIONS")
            SettingsCard {
                SettingsToggleItem(icon = Icons.Default.NotificationsActive, title = "15 Min Warning", defaultChecked = true)
                Divider(color = FocusSurfaceVariant)
                SettingsToggleItem(icon = Icons.Default.NotificationsActive, title = "5 Min Warning", defaultChecked = true)
                Divider(color = FocusSurfaceVariant)
                SettingsToggleItem(icon = Icons.Default.Event, title = "Schedule Reminder", defaultChecked = true)
            }
        }

        // Lock & Security Status
        item {
            val setup by viewModel.setupState.collectAsState()
            SettingsSectionTitle("LOCK SHIELD STATUS")
            SettingsCard {
                LockMode.entries.filter { it != LockMode.NORMAL }.forEachIndexed { index, mode ->
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
                    if (index < LockMode.entries.size - 2) {
                        Divider(color = FocusSurfaceVariant)
                    }
                }
            }
        }

        // Permissions & Security
        item {
            SettingsSectionTitle("PERMISSIONS & SECURITY")
            SettingsCard {
                SettingsItem(icon = Icons.Default.Camera, title = "Camera", valueText = "Granted", valueColor = FocusPrimary)
                Divider(color = FocusSurfaceVariant)
                SettingsItem(icon = Icons.Default.Notifications, title = "Notifications", valueText = "Granted", valueColor = FocusPrimary)
                Divider(color = FocusSurfaceVariant)
                SettingsItem(icon = Icons.Default.Security, title = "App Blocking (Accessibility)", valueText = "Granted", valueColor = FocusPrimary)
                Divider(color = FocusSurfaceVariant)
                SettingsItem(icon = Icons.Default.BatteryChargingFull, title = "Battery Optimization", valueText = "Ignored", valueColor = FocusPrimary)
            }
        }

        // Appearance
        item {
            SettingsSectionTitle("APPEARANCE")
            SettingsCard {
                SettingsToggleItem(icon = Icons.Default.DarkMode, title = "Dark Mode", defaultChecked = true)
                Divider(color = FocusSurfaceVariant)
                SettingsItem(icon = Icons.Default.Palette, title = "Theme", valueText = "Electric Green")
            }
        }
        
        item { Spacer(modifier = Modifier.height(100.dp)) }
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
