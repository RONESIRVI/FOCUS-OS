package com.example.ui.screens

import androidx.compose.foundation.background
import com.example.ui.theme.FocusTextPrimary
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import com.example.ui.theme.FocusDanger
import com.example.ui.theme.FocusDangerDark
import com.example.ui.theme.FocusPrimary
import com.example.ui.theme.FocusPrimaryDark
import com.example.ui.theme.FocusSurfaceVariant
import com.example.ui.theme.FocusTextSecondary

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

@Composable
fun ScheduleValidationDialog(
    saveText: String = "SAVE SCHEDULE",
    changeText: String = "CHANGE SELECTED TIME",
    conflicts: List<FocusSession>,
    userStart: Long,
    userEnd: Long,
    previousSessions: List<FocusSession> = emptyList(),
    nextSessions: List<FocusSession> = emptyList(),
    nextSession: FocusSession? = null,
    onChangeTime: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val userStartStr = timeFormat.format(Date(userStart))
    val userEndStr = timeFormat.format(Date(userEnd))
    val durationMins = ((userEnd - userStart) / 60000).toInt()
    
    val isConflict = conflicts.isNotEmpty()

    // Resolve after sessions: use nextSessions list, or fall back to single nextSession if provided
    val resolvedAfterSessions = when {
        nextSessions.isNotEmpty() -> nextSessions
        nextSession != null -> listOf(nextSession)
        else -> emptyList()
    }

    Dialog(onDismissRequest = onCancel) {
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusTextPrimary),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Surface(
                    shape = CircleShape,
                    color = if (isConflict) FocusDanger.copy(alpha = 0.1f) else Color(0xFF22C55E).copy(alpha = 0.1f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = if (isConflict) Icons.Default.Warning else Icons.Default.Check,
                        contentDescription = null,
                        tint = if (isConflict) FocusDanger else Color(0xFF22C55E),
                        modifier = Modifier.padding(14.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Title
                Text(
                    text = if (isConflict) "${conflicts.size} TIME CONFLICTS FOUND" else "No Conflicts — Slot Free",
                    color = if (isConflict) FocusDanger else Color(0xFF22C55E),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Subtitle
                Text(
                    text = if (isConflict) "Your selected time conflicts with other scheduled sessions. Check the timeline below:" 
                           else "Your proposed time fits nicely into your schedule timetable.",
                    color = FocusTextPrimary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Conflicts List (if any)
                if (isConflict) {
                    Text(
                        text = "OVERLAPPING CONFLICTING SESSIONS",
                        color = FocusDanger,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    conflicts.forEach { session ->
                        val sStart = session.scheduledStartTime?.let { timeFormat.format(Date(it)) } ?: ""
                        val sEnd = session.scheduledEndTime?.let { timeFormat.format(Date(it)) } ?: ""
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .background(Color(0xFFFFF5F5), RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFFFFCDD2), RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = session.subjectName.ifBlank { session.sessionName }.uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "$sStart – $sEnd",
                                    color = FocusDanger,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, FocusDanger),
                                color = Color.Transparent
                            ) {
                                Text(
                                    text = "${session.targetDurationMinutes}m",
                                    color = FocusDanger,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // ==========================================
                // TIMELINE VIEW: SESSIONS BEFORE
                // ==========================================
                if (previousSessions.isNotEmpty()) {
                    Text(
                        text = "SESSIONS SCHEDULED BEFORE THIS",
                        color = FocusTextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    previousSessions.forEach { prev ->
                        val pStart = prev.scheduledStartTime?.let { timeFormat.format(Date(it)) } ?: ""
                        val pEnd = prev.scheduledEndTime?.let { timeFormat.format(Date(it)) } ?: ""

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .background(FocusSurfaceVariant, RoundedCornerShape(10.dp))
                                .border(1.dp, FocusPrimary.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = prev.subjectName.ifBlank { prev.sessionName }.uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = FocusTextPrimary,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "$pStart – $pEnd",
                                    color = FocusTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = "${prev.targetDurationMinutes}m",
                                color = FocusTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = FocusTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
                
                // ==========================================
                // YOUR SELECTED TIME (PROPOSED NEW SESSION)
                // ==========================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isConflict) FocusPrimary.copy(alpha = 0.1f) else Color(0xFF22C55E).copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.5.dp,
                            if (isConflict) Color(0xFF2196F3) else Color(0xFF22C55E),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "NEW SCHEDULED TIME",
                        color = if (isConflict) FocusPrimaryDark else Color(0xFF166534),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$userStartStr – $userEndStr",
                        color = if (isConflict) FocusPrimaryDark else Color(0xFF1B5E20),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Duration: ${if(durationMins >= 60) "${durationMins / 60}h " else ""}${if(durationMins % 60 > 0 || durationMins < 60) "${durationMins % 60}m" else ""}".trim(),
                        color = FocusTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // ==========================================
                // TIMELINE VIEW: SESSIONS AFTER
                // ==========================================
                if (resolvedAfterSessions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = FocusTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "SESSIONS SCHEDULED AFTER THIS",
                        color = FocusTextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    resolvedAfterSessions.forEach { nxt ->
                        val nStart = nxt.scheduledStartTime?.let { timeFormat.format(Date(it)) } ?: ""
                        val nEnd = nxt.scheduledEndTime?.let { timeFormat.format(Date(it)) } ?: ""

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .background(FocusSurfaceVariant, RoundedCornerShape(10.dp))
                                .border(1.dp, FocusPrimary.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = nxt.subjectName.ifBlank { nxt.sessionName }.uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = FocusTextPrimary,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "$nStart – $nEnd",
                                    color = FocusTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = "${nxt.targetDurationMinutes}m",
                                color = FocusPrimaryDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                
                if (isConflict) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(FocusDanger.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = FocusDanger, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Please adjust the time to eliminate overlap.",
                            color = FocusDangerDark,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF22C55E).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = CircleShape, color = Color(0xFF22C55E), modifier = Modifier.size(18.dp)) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = FocusTextPrimary, modifier = Modifier.padding(3.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "This time slot is completely clear.",
                            color = Color(0xFF166534),
                            fontSize = 12.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Action Buttons
                if (isConflict) {
                    Button(
                        onClick = onChangeTime,
                        colors = ButtonDefaults.buttonColors(containerColor = FocusDanger),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(changeText, color = FocusTextPrimary, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onSave,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(saveText, color = FocusTextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CANCEL", color = FocusTextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
