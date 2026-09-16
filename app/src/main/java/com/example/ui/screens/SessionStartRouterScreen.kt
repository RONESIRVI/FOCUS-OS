package com.example.ui.screens

import androidx.compose.foundation.background
import com.example.ui.theme.FocusTextPrimary
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.FocusBackground
import com.example.ui.theme.FocusPrimary
import com.example.ui.viewmodel.FocusViewModel
import com.example.util.FocusLockManager

@Composable
fun SessionStartRouterScreen(
    sessionId: Long,
    viewModel: FocusViewModel,
    onNavigateToCamera: () -> Unit,
    onNavigateToTimer: () -> Unit,
    onCancel: () -> Unit = {}
) {
    val timerState by viewModel.timerState.collectAsState()
    var showConflictDialog by remember { mutableStateOf(false) }

    LaunchedEffect(sessionId) {
        // Prevent starting if another focus session is already running
        if (FocusLockManager.isFocusActive || timerState.isRunning) {
            showConflictDialog = true
            return@LaunchedEffect
        }

        viewModel.loadAndStartScheduledSession(sessionId) { requiresPhoto ->
            if (requiresPhoto) {
                onNavigateToCamera()
            } else {
                viewModel.startFocusSession()
                onNavigateToTimer()
            }
        }
    }

    if (showConflictDialog) {
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
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Icon
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFEBEE),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFE53935),
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Title
                    Text(
                        text = "Session Already Running",
                        color = Color(0xFFE53935),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Subtitle
                    Text(
                        text = "A Focus Session is already running. You cannot start another session until the current one ends.",
                        color = Color(0xFF424242),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = onCancel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "GO BACK",
                            color = FocusTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground),
        contentAlignment = Alignment.Center
    ) {
        if (!showConflictDialog) {
            CircularProgressIndicator(color = FocusPrimary)
        }
    }
}

