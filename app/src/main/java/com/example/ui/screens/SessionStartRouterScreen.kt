package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.ui.theme.FocusBackground
import com.example.ui.theme.FocusPrimary
import com.example.ui.viewmodel.FocusViewModel

@Composable
fun SessionStartRouterScreen(
    sessionId: Long,
    viewModel: FocusViewModel,
    onNavigateToCamera: () -> Unit,
    onNavigateToTimer: () -> Unit
) {
    LaunchedEffect(sessionId) {
        viewModel.loadAndStartScheduledSession(sessionId) { requiresPhoto ->
            if (requiresPhoto) {
                onNavigateToCamera()
            } else {
                viewModel.startFocusSession()
                onNavigateToTimer()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = FocusPrimary)
    }
}
