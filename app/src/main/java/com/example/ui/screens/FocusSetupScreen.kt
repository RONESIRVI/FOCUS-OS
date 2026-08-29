package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Headphones
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
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Clear
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextOverflow

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
    var customSubject by remember { mutableStateOf(setup.subjectName) }
    var customGoal by remember { mutableStateOf(setup.sessionName) }

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
                            val displayHrs = setup.durationMinutes / 60
                            val displayMins = setup.durationMinutes % 60
                            val displayTime = if (displayHrs > 0) String.format("%02d:%02d:00", displayHrs, displayMins) else String.format("%02d:00", displayMins)
                            Text(
                                text = displayTime,
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
                                            val finalSubject = if (customSubject.isNotBlank()) customSubject.trim() else "Study Session"
                                            val finalGoal = if (customGoal.isNotBlank()) customGoal.trim() else finalSubject
                                            if (customSubject.isNotBlank() && subjects.none { it.name.equals(customSubject.trim(), ignoreCase = true) }) {
                                                viewModel.addCustomSubject(customSubject.trim(), "#0284C7")
                                            }
                                            viewModel.updateSetup(
                                                sessionName = finalGoal,
                                                subjectName = finalSubject,
                                                durationMinutes = customDuration.toInt()
                                            )
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
                        // Section 1: Subject & Study Goal Customization
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
                                            imageVector = Icons.Default.MenuBook,
                                            contentDescription = null,
                                            tint = FocusPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "SUBJECT & STUDY GOAL",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = FocusSurfaceVariant
                                    ) {
                                        Text(
                                            text = "Custom",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = FocusPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Subject input
                                Text(
                                    text = "SUBJECT / AREA OF STUDY",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = FocusTextSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = customSubject,
                                    onValueChange = {
                                        customSubject = it
                                        viewModel.updateSetup(subjectName = it)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("setup_subject_input"),
                                    placeholder = {
                                        Text(
                                            "e.g. Mathematics, Physics, History, UPSC, Coding...",
                                            color = FocusTextSecondary.copy(alpha = 0.45f),
                                            fontSize = 14.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.School,
                                            contentDescription = null,
                                            tint = FocusPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        if (customSubject.isNotBlank()) {
                                            IconButton(onClick = {
                                                customSubject = ""
                                                viewModel.updateSetup(subjectName = "")
                                            }) {
                                                Icon(
                                                    Icons.Default.Clear,
                                                    contentDescription = "Clear",
                                                    tint = FocusTextSecondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = FocusPrimary,
                                        unfocusedBorderColor = FocusSurfaceVariant,
                                        focusedContainerColor = FocusBackground,
                                        unfocusedContainerColor = FocusBackground,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = false,
                                    minLines = 1,
                                    maxLines = 5
                                )

                                // Quick pick from saved subjects
                                if (subjects.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Quick pick:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = FocusTextSecondary.copy(alpha = 0.7f),
                                            fontSize = 11.sp
                                        )
                                        subjects.forEach { sub ->
                                            val isSel = customSubject.equals(sub.name, ignoreCase = true)
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isSel) FocusPrimary.copy(alpha = 0.2f) else FocusBackground,
                                                border = BorderStroke(1.dp, if (isSel) FocusPrimary else FocusSurfaceVariant),
                                                modifier = Modifier.clickable {
                                                    customSubject = sub.name
                                                    viewModel.updateSetup(subjectName = sub.name)
                                                }
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = sub.name,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                                        ),
                                                        color = if (isSel) FocusPrimary else FocusTextSecondary
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Delete",
                                                        tint = if (isSel) FocusPrimary else FocusTextSecondary.copy(alpha = 0.4f),
                                                        modifier = Modifier
                                                            .size(12.dp)
                                                            .clickable {
                                                                viewModel.deleteCustomSubject(sub)
                                                            }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Goal input
                                Text(
                                    text = "TARGET GOAL / CHAPTER (OPTIONAL)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = FocusTextSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = customGoal,
                                    onValueChange = {
                                        customGoal = it
                                        viewModel.updateSetup(sessionName = it)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("setup_goal_input"),
                                    placeholder = {
                                        Text(
                                            "e.g. Chapter 4 Numericals, Solve 30 MCQs, Revise notes...",
                                            color = FocusTextSecondary.copy(alpha = 0.45f),
                                            fontSize = 14.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.TrackChanges,
                                            contentDescription = null,
                                            tint = FocusWarning,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        if (customGoal.isNotBlank()) {
                                            IconButton(onClick = {
                                                customGoal = ""
                                                viewModel.updateSetup(sessionName = "")
                                            }) {
                                                Icon(
                                                    Icons.Default.Clear,
                                                    contentDescription = "Clear",
                                                    tint = FocusTextSecondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = FocusWarning,
                                        unfocusedBorderColor = FocusSurfaceVariant,
                                        focusedContainerColor = FocusBackground,
                                        unfocusedContainerColor = FocusBackground,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = false,
                                    minLines = 1,
                                    maxLines = 5
                                )
                            }
                        }

                        // Section 2: Ambient Focus Sound Generator
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
                                Spacer(modifier = Modifier.height(14.dp))
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    SoundType.entries.forEach { st ->
                                        val isSel = setup.selectedSound == st
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (isSel) FocusPrimary.copy(alpha = 0.1f) else FocusBackground,
                                            border = BorderStroke(
                                                width = if (isSel) 1.5.dp else 1.dp,
                                                color = if (isSel) FocusPrimary else FocusSurfaceVariant
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { viewModel.updateSetup(soundType = st) }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(46.dp)
                                                        .background(
                                                            if (isSel) FocusPrimary.copy(alpha = 0.2f) else FocusSurfaceVariant.copy(alpha = 0.4f),
                                                            androidx.compose.foundation.shape.CircleShape
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = if (st.isBinaural) Icons.Default.Headphones else Icons.Default.MusicNote,
                                                        contentDescription = null,
                                                        tint = if (isSel) FocusPrimary else FocusTextSecondary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(14.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = st.label,
                                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                            color = if (isSel) FocusPrimary else Color.White
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        if (st.name != "NONE") {
                                                            Text(
                                                                text = st.badge,
                                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                                color = if (isSel) FocusPrimary else FocusTextSecondary,
                                                                modifier = Modifier
                                                                    .background(
                                                                        if (isSel) FocusPrimary.copy(alpha = 0.15f) else FocusSurfaceVariant,
                                                                        RoundedCornerShape(4.dp)
                                                                    )
                                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = st.hindiTitle,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                                        color = FocusWarning
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = st.description,
                                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 14.sp),
                                                        color = FocusTextSecondary,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(10.dp))

                                                RadioButton(
                                                    selected = isSel,
                                                    onClick = { viewModel.updateSetup(soundType = st) },
                                                    colors = RadioButtonDefaults.colors(
                                                        selectedColor = FocusPrimary,
                                                        unselectedColor = FocusTextSecondary
                                                    ),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
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
}
