package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FocusAccentOrange
import com.example.ui.theme.FocusCyan
import com.example.ui.theme.FocusGold
import com.example.ui.theme.FocusGreen
import com.example.ui.theme.FocusSlateBg
import com.example.ui.theme.FocusSurface
import com.example.ui.theme.FocusTextSecondary
import com.example.ui.viewmodel.FocusViewModel

@Composable
fun SessionCompleteScreen(
    viewModel: FocusViewModel,
    onNavigateHome: () -> Unit,
    onNavigateStats: () -> Unit
) {
    val stats by viewModel.summaryStats.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusSlateBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Hero Trophy Box
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(FocusGreen.copy(alpha = 0.2f), CircleShape)
                    .border(3.dp, FocusGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = FocusGreen,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "EXCELLENT WORK!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Focus Lock Session Completed Successfully",
                style = MaterialTheme.typography.bodyMedium,
                color = FocusTextSecondary
            )
        }

        // Summary Card
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusSurface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = FocusCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Today's Total Focus", style = MaterialTheme.typography.bodyMedium, color = FocusTextSecondary)
                    }
                    val hrs = stats.todayFocusSeconds / 3600
                    val mins = (stats.todayFocusSeconds % 3600) / 60
                    Text(
                        text = "${hrs}h ${mins}m",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = null, tint = FocusAccentOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Focus Streak", style = MaterialTheme.typography.bodyMedium, color = FocusTextSecondary)
                    }
                    Text(
                        text = "${stats.currentStreakDays} Days 🔥",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = FocusAccentOrange
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = FocusGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Session Score", style = MaterialTheme.typography.bodyMedium, color = FocusTextSecondary)
                    }
                    Text(
                        text = "${stats.focusScore} / 100",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = FocusGold
                    )
                }
            }
        }

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onNavigateHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("start_another_session_btn"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FocusCyan, contentColor = Color.Black)
            ) {
                Text("START ANOTHER SESSION", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }

            OutlinedButton(
                onClick = onNavigateStats,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("VIEW DETAILED STUDY ANALYTICS")
            }
        }
    }
}
