package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.FocusBackground
import com.example.ui.theme.FocusTextPrimary
import com.example.ui.theme.FocusTextSecondary
import com.example.ui.viewmodel.FocusViewModel

@Composable
fun SettingsScreen(
    viewModel: FocusViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "SETTINGS",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = FocusTextPrimary
            )
            Text(
                text = "(Coming Soon in Phase 3)",
                style = MaterialTheme.typography.bodyMedium,
                color = FocusTextSecondary
            )
        }
    }
}
