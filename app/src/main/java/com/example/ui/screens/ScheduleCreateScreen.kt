package com.example.ui.screens

import android.app.Application
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Lock
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.FocusViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleCreateScreen(
    viewModel: FocusViewModel,
    onBack: () -> Unit,
    onNavigateToAppSelector: () -> Unit,
    onScheduleCreated: () -> Unit
) {
    val setup by viewModel.setupState.collectAsState()
    val whitelistedApps by viewModel.whitelistedAppsStrict.collectAsState()
    val context = LocalContext.current

    var sessionName by remember { mutableStateOf(setup.sessionName) }
    
    var startHour by remember { mutableStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    var startMinute by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MINUTE)) }
    
    var endHour by remember { mutableStateOf((startHour + 1) % 24) }
    var endMinute by remember { mutableStateOf(startMinute) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CREATE FOCUS SESSION", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FocusBackground,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = FocusBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Session Name
            Text("Session Name", style = MaterialTheme.typography.labelMedium, color = FocusTextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = sessionName,
                onValueChange = { sessionName = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. GS Polity", color = FocusTextSecondary.copy(alpha = 0.5f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FocusWarning,
                    unfocusedBorderColor = FocusSurfaceVariant,
                    focusedContainerColor = FocusSurface,
                    unfocusedContainerColor = FocusSurface,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Schedule Time
            Text("Schedule Time", style = MaterialTheme.typography.labelMedium, color = FocusTextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Start Time
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(FocusSurface, RoundedCornerShape(12.dp))
                        .border(1.dp, FocusSurfaceVariant, RoundedCornerShape(12.dp))
                        .clickable {
                            TimePickerDialog(
                                context,
                                { _, h, m ->
                                    startHour = h
                                    startMinute = m
                                },
                                startHour,
                                startMinute,
                                false
                            ).show()
                        }
                        .padding(16.dp)
                ) {
                    Column {
                        Text("Start Time", style = MaterialTheme.typography.labelSmall, color = FocusTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        val amPm = if (startHour >= 12) "PM" else "AM"
                        val h = if (startHour % 12 == 0) 12 else startHour % 12
                        val m = String.format("%02d", startMinute)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = FocusPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("$h:$m $amPm", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        }
                    }
                }
                
                // End Time
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(FocusSurface, RoundedCornerShape(12.dp))
                        .border(1.dp, FocusSurfaceVariant, RoundedCornerShape(12.dp))
                        .clickable {
                            TimePickerDialog(
                                context,
                                { _, h, m ->
                                    endHour = h
                                    endMinute = m
                                },
                                endHour,
                                endMinute,
                                false
                            ).show()
                        }
                        .padding(16.dp)
                ) {
                    Column {
                        Text("End Time", style = MaterialTheme.typography.labelSmall, color = FocusTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        val amPm = if (endHour >= 12) "PM" else "AM"
                        val h = if (endHour % 12 == 0) 12 else endHour % 12
                        val m = String.format("%02d", endMinute)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = FocusWarning, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("$h:$m $amPm", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Allowed Apps
            Text("Select Allowed Class Apps", style = MaterialTheme.typography.labelMedium, color = FocusTextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.setAppSelectorProfile("STRICT")
                        onNavigateToAppSelector()
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Allowed Apps", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        Text("${whitelistedApps.size} apps selected", style = MaterialTheme.typography.bodySmall, color = FocusTextSecondary)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = "Select Apps", tint = FocusTextSecondary)
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Create Button
            Button(
                onClick = {
                    // Update VM state first
                    viewModel.updateSetup(sessionName = sessionName)
                    
                    // Calculate duration in minutes
                    var startMins = startHour * 60 + startMinute
                    var endMins = endHour * 60 + endMinute
                    if (endMins <= startMins) {
                        endMins += 24 * 60
                    }
                    val durationMins = endMins - startMins
                    viewModel.updateSetup(durationMinutes = durationMins)
                    
                    // Schedule it
                    viewModel.scheduleFocusSession(startHour, startMinute)
                    
                    onScheduleCreated()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FocusWarning),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "CREATE SCHEDULE",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White
                )
            }
        }
    }
}
