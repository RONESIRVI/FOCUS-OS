package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import android.app.TimePickerDialog
import java.util.Calendar
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LockMode
import com.example.services.SoundType
import com.example.ui.theme.FocusWarning
import com.example.ui.theme.FocusPrimary
import com.example.ui.theme.FocusPrimary
import com.example.ui.theme.FocusPrimary
import com.example.ui.theme.FocusBackground
import com.example.ui.theme.FocusSurface
import com.example.ui.theme.FocusSurfaceVariant
import com.example.ui.theme.FocusTextSecondary
import com.example.ui.viewmodel.FocusViewModel
import com.example.util.LockPermissionHelper

data class PresetOption(
    val minutes: Int,
    val label: String,
    val sublabel: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun FocusSetupScreen(
    viewModel: FocusViewModel,
    onBack: () -> Unit,
    onNavigateToAppSelector: () -> Unit,
    onStartSession: () -> Unit
) {
    val setup by viewModel.setupState.collectAsState()
    val subjects by viewModel.allSubjects.collectAsState()
    val whitelistedApps by viewModel.whitelistedAppsManual.collectAsState()
    val context = LocalContext.current

    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var newSubjectName by remember { mutableStateOf("") }

    var customDuration by remember { mutableFloatStateOf(setup.durationMinutes.toFloat()) }

    
    
    

    val hasOverlay = remember { LockPermissionHelper.hasOverlayPermission(context) }
    val hasUsage = remember { LockPermissionHelper.hasUsageStatsPermission(context) }

    val presetList = listOf(
        PresetOption(15, "15m", "QUICK", Icons.Default.Timer, FocusPrimary),
        PresetOption(25, "25m", "POMODORO", Icons.Default.LocalFireDepartment, FocusWarning),
        PresetOption(45, "45m", "STUDY", Icons.Default.MenuBook, FocusPrimary),
        PresetOption(60, "60m", "DEEP WORK", Icons.Default.Psychology, Color(0xFF38BDF8)),
        PresetOption(90, "90m", "SPRINT", Icons.Default.DirectionsRun, FocusPrimary)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Glassmorphic Navigation Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(FocusSurface, FocusBackground)
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
                            .background(FocusPrimary.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                            .border(1.dp, FocusPrimary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = FocusPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PRO MODE", style = MaterialTheme.typography.labelSmall, color = FocusPrimary)
                        }
                    }
                }
            }

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // HERO SECTION: Holographic Shield Mesh, Digital Time, Slider, Presets & Glowing Launch Button
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF0F172A),
                                        Color(0xFF1E293B)
                                    )
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            // Holographic Mesh / Shield Barrier Canvas Illustration
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val strokeWidth = 1.dp.toPx()
                                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                                    // Grid Lines
                                    for (i in 1..4) {
                                        val y = size.height * (i / 5f)
                                        drawLine(
                                            color = Color(0xFF0284C7).copy(alpha = 0.25f),
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = strokeWidth,
                                            pathEffect = pathEffect
                                        )
                                    }
                                    for (i in 1..8) {
                                        val x = size.width * (i / 9f)
                                        drawLine(
                                            color = Color(0xFF0284C7).copy(alpha = 0.2f),
                                            start = Offset(x, 0f),
                                            end = Offset(x, size.height),
                                            strokeWidth = strokeWidth,
                                            pathEffect = pathEffect
                                        )
                                    }
                                }

                                // Floating app icons bouncing on shield
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Left Distraction Apps (Blocked)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(Color(0xFF1877F2).copy(alpha = 0.2f), CircleShape)
                                                .border(1.dp, Color(0xFF1877F2).copy(alpha = 0.6f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("f", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(Color(0xFFE4405F).copy(alpha = 0.2f), CircleShape)
                                                .border(1.dp, Color(0xFFE4405F).copy(alpha = 0.6f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("ig", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                    }

                                    // Center Shield Barrier
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .background(
                                                Brush.radialGradient(
                                                    colors = listOf(
                                                        FocusPrimary.copy(alpha = 0.35f),
                                                        Color.Transparent
                                                    )
                                                ),
                                                CircleShape
                                            )
                                            .border(1.5.dp, FocusPrimary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = FocusPrimary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }

                                    // Right Distraction Apps
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(Color(0xFF25D366).copy(alpha = 0.2f), CircleShape)
                                                .border(1.dp, Color(0xFF25D366).copy(alpha = 0.6f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("wa", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(Color(0xFFFF0000).copy(alpha = 0.2f), CircleShape)
                                                .border(1.dp, Color(0xFFFF0000).copy(alpha = 0.6f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("yt", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Large Digital Timer Display e.g. "45:00"
                            Text(
                                text = "${setup.durationMinutes}:00",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 2.sp
                                ),
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Duration Custom Slider with 5 min & 180 min bounds
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Slider(
                                    value = customDuration,
                                    onValueChange = {
                                        customDuration = it
                                        viewModel.updateSetup(durationMinutes = it.toInt())
                                    },
                                    valueRange = 5f..180f,
                                    steps = 34,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = FocusPrimary,
                                        inactiveTrackColor = FocusSurfaceVariant
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("5 minutes", style = MaterialTheme.typography.labelSmall, color = FocusTextSecondary)
                                    Text("180 minutes", style = MaterialTheme.typography.labelSmall, color = FocusTextSecondary)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Preset Options Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                presetList.forEach { preset ->
                                    val isSel = setup.durationMinutes == preset.minutes
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(72.dp)
                                            .background(
                                                if (isSel) FocusPrimary.copy(alpha = 0.25f) else FocusSurface,
                                                RoundedCornerShape(16.dp)
                                            )
                                            .border(
                                                width = if (isSel) 1.5.dp else 1.dp,
                                                color = if (isSel) FocusPrimary else FocusSurfaceVariant,
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .clickable {
                                                customDuration = preset.minutes.toFloat()
                                                viewModel.updateSetup(durationMinutes = preset.minutes)
                                            }
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = preset.icon,
                                                contentDescription = null,
                                                tint = if (isSel) FocusPrimary else preset.color,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = preset.label,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                            Text(
                                                text = preset.sublabel,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                                color = if (isSel) FocusPrimary else FocusTextSecondary
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Futuristic Glowing Launch Button & Permission Floating Card
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // 3D Glowing Button Box
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(64.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    FocusWarning,
                                                    Color(0xFF0284C7)
                                                )
                                            ),
                                            RoundedCornerShape(20.dp)
                                        )
                                        .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                        .clickable {
                                            viewModel.startFocusSession()
                                            onStartSession()
                                        }
                                        .testTag("start_session_confirm_btn"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Shield,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "LAUNCH FOCUS SHIELD",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 1.sp
                                            ),
                                            color = Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Floating Permissions & Lock Shield Status Badge
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = FocusSurface),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, FocusSurfaceVariant, RoundedCornerShape(16.dp))
                                        .clickable {
                                            if (!hasOverlay) LockPermissionHelper.openOverlaySettings(context)
                                            else if (!hasUsage) LockPermissionHelper.openUsageStatsSettings(context)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(if (hasOverlay && hasUsage) FocusPrimary else FocusWarning, CircleShape)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "PERMISSIONS & LOCK SHIELD STATUS",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        letterSpacing = 0.5.sp
                                                    ),
                                                    color = Color.White
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                Text(
                                                    text = "OS OVERLAYS: ${if (hasOverlay) "ENABLED ✓" else "TAP TO GRANT ⚠"}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (hasOverlay) FocusPrimary else FocusWarning
                                                )
                                                Text(
                                                    text = "USAGE ACCESS: ${if (hasUsage) "GRANTED ✓" else "TAP TO GRANT ⚠"}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (hasUsage) FocusPrimary else FocusWarning
                                                )
                                            }
                                        }

                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = FocusPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // DETAILED SETUP OPTIONS BELOW HERO SECTION
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Section 1: Session Name & Subject Picker
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
                                        tint = FocusPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Session Subject",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(14.dp))
                                Text("Select a Subject:", style = MaterialTheme.typography.labelSmall, color = FocusTextSecondary)
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    FocusSurfaceVariant,
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .border(1.dp, FocusPrimary, RoundedCornerShape(12.dp))
                                                .clickable {
                                                    showAddSubjectDialog = true
                                                }
                                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Add Subject",
                                                    tint = FocusPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Add",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                    color = FocusPrimary
                                                )
                                            }
                                        }
                                    }
                                    items(subjects) { subject ->
                                        val isSelected = setup.subjectName == subject.name
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (isSelected) FocusPrimary else FocusSurfaceVariant,
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .clickable {
                                                    viewModel.updateSetup(sessionName = subject.name, subjectName = subject.name)
                                                }
                                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = subject.name,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                    ),
                                                    color = if (isSelected) Color.Black else Color.White
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Delete Subject",
                                                    tint = if (isSelected) Color.Black.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.3f),
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clickable {
                                                            viewModel.deleteCustomSubject(subject)
                                                        }
                                                )
                                            }
                                        }
                                    }
                                }
                                
                        // Section 2: Allowed Apps Whitelist
                        Card(
                            colors = CardDefaults.cardColors(containerColor = FocusSurface),
                            shape = RoundedCornerShape(22.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, FocusSurfaceVariant, RoundedCornerShape(22.dp))
                                .clickable {
                                    viewModel.setAppSelectorProfile("MANUAL")
                                    onNavigateToAppSelector()
                                }
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
                                        tint = FocusPrimary,
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
                                    tint = FocusPrimary
                                )
                            }
                        }

                        // Section 3: Lock & Security Mode
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
                                            tint = FocusWarning,
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
                                        Text("SHIELD ACTIVE ✓", style = MaterialTheme.typography.labelSmall, color = FocusPrimary)
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                LockMode.entries.filter { it != LockMode.NORMAL }.forEach { mode ->
                                    val isSelected = setup.lockMode == mode
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) FocusPrimary.copy(alpha = 0.12f) else FocusBackground
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable { viewModel.updateSetup(lockMode = mode) }
                                            .border(
                                                width = if (isSelected) 1.5.dp else 0.dp,
                                                color = if (isSelected) FocusPrimary else Color.Transparent,
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
                                                colors = RadioButtonDefaults.colors(selectedColor = FocusPrimary)
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
                            }
                        }

                        // Section 4: Ambient Focus Sound Generator
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
                                                    if (isSel) FocusPrimary else FocusSurfaceVariant,
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

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
    
    if (showAddSubjectDialog) {
        AlertDialog(
            onDismissRequest = { showAddSubjectDialog = false },
            title = { Text("Add Quick Subject", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = newSubjectName,
                    onValueChange = { newSubjectName = it },
                    placeholder = { Text("Subject Name", color = FocusTextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newSubjectName.isNotBlank()) {
                        viewModel.addCustomSubject(newSubjectName.trim(), "#0284C7")
                        newSubjectName = ""
                        showAddSubjectDialog = false
                    }
                }) {
                    Text("Add", color = FocusPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubjectDialog = false }) {
                    Text("Cancel", color = FocusTextSecondary)
                }
            },
            containerColor = FocusSurface
        )
    }
}
}
}
