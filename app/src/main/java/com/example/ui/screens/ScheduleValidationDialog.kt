package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.FocusSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.ui.theme.FocusBackground
import com.example.ui.theme.FocusPrimary
import com.example.ui.theme.FocusSurface
import com.example.ui.theme.FocusTextSecondary

@Composable
fun ScheduleValidationDialog(
    saveText: String = "SAVE SCHEDULE",
    changeText: String = "CHANGE SELECTED TIME",
    conflicts: List<FocusSession>,
    userStart: Long,
    userEnd: Long,
    nextSession: FocusSession?,
    onChangeTime: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val userStartStr = timeFormat.format(Date(userStart))
    val userEndStr = timeFormat.format(Date(userEnd))
    val durationMins = ((userEnd - userStart) / 60000).toInt()
    
    val isConflict = conflicts.isNotEmpty()

    Dialog(onDismissRequest = onCancel) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Surface(
                    shape = CircleShape,
                    color = if (isConflict) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = if (isConflict) Icons.Default.Warning else Icons.Default.Check,
                        contentDescription = null,
                        tint = if (isConflict) Color(0xFFE53935) else Color(0xFF43A047),
                        modifier = Modifier.padding(16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Title
                Text(
                    text = if (isConflict) "${conflicts.size} TIME CONFLICTS FOUND" else "No Conflicts",
                    color = if (isConflict) Color(0xFFE53935) else Color(0xFF43A047),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Subtitle
                Text(
                    text = if (isConflict) "Your selected time conflicts with multiple scheduled sessions." 
                           else "Great! Your selected time is available.",
                    color = Color.DarkGray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Conflicts List
                if (isConflict) {
                    Text(
                        text = "CONFLICTING SESSIONS",
                        color = Color(0xFFE53935),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    conflicts.forEach { session ->
                        val sStart = session.scheduledStartTime?.let { timeFormat.format(Date(it)) } ?: ""
                        val sEnd = session.scheduledEndTime?.let { timeFormat.format(Date(it)) } ?: ""
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(1.dp, Color(0xFFFFCDD2), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = session.subjectName.uppercase(), fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
                                Text(text = "$sStart - $sEnd", color = Color(0xFFE53935), fontSize = 12.sp)
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE53935)),
                                color = Color.Transparent
                            ) {
                                Text(
                                    text = "${session.targetDurationMinutes}m",
                                    color = Color(0xFFE53935),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Selected Time
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isConflict) Color(0xFFE3F2FD) else Color(0xFFE8F5E9),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "YOUR SELECTED TIME",
                        color = if (isConflict) Color(0xFF1976D2) else Color(0xFF43A047),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$userStartStr - $userEndStr",
                        color = if (isConflict) Color(0xFF1976D2) else Color(0xFF43A047),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Duration: ${if(durationMins >= 60) "${durationMins / 60}h " else ""}${if(durationMins % 60 > 0 || durationMins < 60) "${durationMins % 60}m" else ""}".trim(),
                        color = Color.DarkGray,
                        fontSize = 12.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (isConflict) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE53935), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Please change your time to avoid overlap with existing sessions.",
                            color = Color(0xFFC62828),
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = CircleShape, color = Color(0xFF43A047), modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.padding(4.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "This time slot is free from any scheduled sessions.",
                            color = Color(0xFF2E7D32),
                            fontSize = 12.sp
                        )
                    }
                    
                    if (nextSession != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "NEXT SESSION AFTER THIS",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val nStart = nextSession.scheduledStartTime?.let { timeFormat.format(Date(it)) } ?: ""
                        val nEnd = nextSession.scheduledEndTime?.let { timeFormat.format(Date(it)) } ?: ""
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = nextSession.subjectName.uppercase(), fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp)
                                Text(text = "$nStart - $nEnd", color = Color.DarkGray, fontSize = 12.sp)
                            }
                            Text(
                                text = "${nextSession.targetDurationMinutes}m",
                                color = Color(0xFF1976D2),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Buttons
                if (isConflict) {
                    Button(
                        onClick = onChangeTime,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(changeText, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onSave,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(saveText, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CANCEL", color = Color.DarkGray, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
