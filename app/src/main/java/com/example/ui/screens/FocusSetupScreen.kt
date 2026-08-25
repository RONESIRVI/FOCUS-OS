package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LockMode
import com.example.services.SoundType
import com.example.ui.theme.FocusAccentOrange
import com.example.ui.theme.FocusCyan
import com.example.ui.theme.FocusGold
import com.example.ui.theme.FocusGreen
import com.example.ui.theme.FocusSlateBg
import com.example.ui.theme.FocusSurface
import com.example.ui.theme.FocusSurfaceVariant
import com.example.ui.theme.FocusTextSecondary
import com.example.ui.viewmodel.FocusViewModel
import com.example.util.LockPermissionHelper

@Composable
fun FocusSetupScreen(
    viewModel: FocusViewModel,
    onBack: () -> Unit,
    onNavigateToAppSelector: () -> Unit,
    onStartSession: () -> Unit
) {
    val setup by viewModel.setupState.collectAsState()
    val subjects by viewModel.allSubjects.collectAsState()
    val whitelistedApps by viewModel.whitelistedApps.collectAsState()
    val context = LocalContext.current

    var customDuration by remember { mutableFloatStateOf(setup.durationMinutes.toFloat()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusSlateBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Glassmorphic Gradient Top Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                FocusSurface,
                                FocusSlateBg
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(FocusSurfaceVariant, CircleShape)
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CREATE FOCUS SESSION",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "Configure parameters & lock settings",
                            style = MaterialTheme.typography.labelSmall,
                            color = FocusTextSecondary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(FocusCyan.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                            .border(1.dp, FocusCyan.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = FocusCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PRO MODE", style = MaterialTheme.typography.labelSmall, color = FocusCyan)
                        }
                    }
                }
            }

            // Scrollable Configuration Form
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // Section 1: Session Name & Subject Picker
                item {
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
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = FocusCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Session Name & Subject",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = setup.sessionName,
                                onValueChange = { viewModel.updateSetup(sessionName = it, subjectName = it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("session_name_input"),
                                placeholder = { Text("e.g. UPSC GS Study / Coding Sprint", color = FocusTextSecondary) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FocusCyan,
                                    unfocusedBorderColor = FocusSurfaceVariant,
                                    focusedContainerColor = FocusSlateBg,
                                    unfocusedContainerColor = FocusSlateBg,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            Text("Quick Subjects:", style = MaterialTheme.typography.labelSmall, color = FocusTextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(subjects) { subject ->
                                    val isSelected = setup.subjectName == subject.name
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isSelected) FocusCyan else FocusSurfaceVariant,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                viewModel.updateSetup(sessionName = subject.name, subjectName = subject.name)
                                            }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = subject.name,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            color = if (isSelected) Color.Black else Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 2: Duration Selector
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = FocusSurface),
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, FocusSurfaceVariant, RoundedCornerShape(22.dp))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = FocusCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Target Duration",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(FocusCyan.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${setup.durationMinutes} Mins",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = FocusCyan
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Preset Chips
                            val presets = listOf(15, 25, 45, 60, 90)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                presets.forEach { mins ->
                                    val isSel = setup.durationMinutes == mins
                                    val label = when (mins) {
                                        25 -> "25m Pomo"
                                        45 -> "45m Study"
                                        60 -> "60m Deep"
                                        90 -> "90m Sprint"
                                        else -> "${mins}m Quick"
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                            .background(
                                                if (isSel) FocusCyan else FocusSurfaceVariant,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                customDuration = mins.toFloat()
                                                viewModel.updateSetup(durationMinutes = mins)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            color = if (isSel) Color.Black else Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Slider(
                                value = customDuration,
                                onValueChange = {
                                    customDuration = it
                                    viewModel.updateSetup(durationMinutes = it.toInt())
                                },
                                valueRange = 5f..180f,
                                steps = 34,
                                colors = SliderDefaults.colors(
                                    thumbColor = FocusCyan,
                                    activeTrackColor = FocusCyan,
                                    inactiveTrackColor = FocusSurfaceVariant
                                )
                            )
                        }
                    }
                }

                // Section 3: Allowed Apps Quick Access Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = FocusSurface),
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, FocusSurfaceVariant, RoundedCornerShape(22.dp))
                            .clickable { onNavigateToAppSelector() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Apps,
                                    contentDescription = null,
                                    tint = FocusCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Allowed Apps During Session",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${whitelistedApps.size} apps permitted (Calculator, Notes...)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = FocusTextSecondary
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = FocusCyan
                            )
                        }
                    }
                }

                // Section 4: Lock Mode & Anti-Exit Shield Selector
                item {
                    val hasOverlay = remember { LockPermissionHelper.hasOverlayPermission(context) }
                    val hasUsage = remember { LockPermissionHelper.hasUsageStatsPermission(context) }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = FocusSurface),
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, FocusSurfaceVariant, RoundedCornerShape(22.dp))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = FocusAccentOrange,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Lock & Security Mode",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                                if (hasOverlay && hasUsage) {
                                    Text("SHIELD ACTIVE ✓", style = MaterialTheme.typography.labelSmall, color = FocusGreen)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            LockMode.entries.forEach { mode ->
                                val isSelected = setup.lockMode == mode
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) FocusCyan.copy(alpha = 0.12f) else FocusSlateBg
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { viewModel.updateSetup(lockMode = mode) }
                                        .border(
                                            width = if (isSelected) 1.5.dp else 0.dp,
                                            color = if (isSelected) FocusCyan else Color.Transparent,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { viewModel.updateSetup(lockMode = mode) },
                                            colors = RadioButtonDefaults.colors(selectedColor = FocusCyan)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = mode.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                            Text(
                                                text = mode.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = FocusTextSecondary
                                            )
                                        }
                                    }
                                }
                            }

                            // OS Permissions quick grant if missing
                            if (!hasOverlay || !hasUsage) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { LockPermissionHelper.openOverlaySettings(context) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (hasOverlay) FocusSurfaceVariant else FocusAccentOrange
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = if (hasOverlay) "Overlay: ON ✓" else "Enable Overlay",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                    Button(
                                        onClick = { LockPermissionHelper.openUsageStatsSettings(context) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (hasUsage) FocusSurfaceVariant else FocusAccentOrange
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = if (hasUsage) "Usage: ON ✓" else "Enable Usage Access",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 5: Ambient Sound Generator
                item {
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
                                    tint = FocusCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Background Focus Sound",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(SoundType.entries) { st ->
                                    val isSel = setup.selectedSound == st
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isSel) FocusCyan else FocusSurfaceVariant,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { viewModel.updateSetup(soundType = st) }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = st.label,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            color = if (isSel) Color.Black else Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            // Bottom Fixed Action Bar
            Surface(
                color = FocusSurface,
                shadowElevation = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.startFocusSession()
                            onStartSession()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("start_session_confirm_btn"),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FocusAccentOrange,
                            contentColor = Color.White
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "LAUNCH FOCUS SHIELD (${setup.durationMinutes} MIN)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

