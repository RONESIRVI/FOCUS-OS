package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FocusAccentOrange
import com.example.ui.theme.FocusCyan
import com.example.ui.theme.FocusGold
import com.example.ui.theme.FocusGreen
import com.example.ui.theme.FocusPurple
import com.example.ui.theme.FocusSlateBg
import com.example.ui.theme.FocusSurface
import com.example.ui.theme.FocusSurfaceVariant
import com.example.ui.theme.FocusTextSecondary
import com.example.ui.viewmodel.FocusViewModel

@Composable
fun StatisticsScreen(
    viewModel: FocusViewModel,
    onBack: () -> Unit
) {
    val subjects by viewModel.allSubjects.collectAsState()
    val stats by viewModel.summaryStats.collectAsState()

    var selectedPeriodTab by remember { mutableStateOf("Day") }
    val periodTabs = listOf("Period", "Day", "Week", "Month", "Trend")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusSlateBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header Navigation Bar
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "STUDY STATISTICS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.White
                )
            }
        }

        // Top Segmented Period Tabs (YPT Style)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    periodTabs.forEach { tab ->
                        val isSel = selectedPeriodTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .background(
                                    if (isSel) FocusCyan else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedPeriodTab = tab },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSel) Color.Black else Color.White
                            )
                        }
                    }
                }
            }
        }

        // Total Study Time Display Box
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusSurface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TOTAL FOCUS TIME ($selectedPeriodTab)",
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                        color = FocusTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val hrs = stats.todayFocusSeconds / 3600
                    val mins = (stats.todayFocusSeconds % 3600) / 60
                    val secs = stats.todayFocusSeconds % 60
                    val timeString = String.format("%02d:%02d:%02d", hrs, mins, secs)

                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                        color = FocusCyan
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Target: 10:00:00 • 65% Completed",
                        style = MaterialTheme.typography.bodySmall,
                        color = FocusGreen
                    )
                }
            }
        }

        // Subject Ratio Donut Chart Section (YPT Style)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusSurface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = FocusCyan,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Subject Time Ratio",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Donut Chart Canvas Drawing
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(180.dp)) {
                            val totalSecs = subjects.sumOf { it.completedSeconds }.coerceAtLeast(1)
                            var startAngle = -90f
                            val strokeWidth = 32.dp.toPx()
                            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                            subjects.forEach { subject ->
                                val sweep = (subject.completedSeconds.toFloat() / totalSecs.toFloat()) * 360f
                                val color = try {
                                    Color(android.graphics.Color.parseColor(subject.categoryColorHex))
                                } catch (e: Exception) {
                                    FocusCyan
                                }

                                drawArc(
                                    color = color,
                                    startAngle = startAngle,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth)
                                )
                                startAngle += sweep
                            }
                        }

                        // Donut Center Text
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "SUBJECTS",
                                style = MaterialTheme.typography.labelSmall,
                                color = FocusTextSecondary
                            )
                            Text(
                                text = "${subjects.size} Active",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Legend Table Breakdown
                    subjects.forEach { subject ->
                        val hrs = subject.completedSeconds / 3600
                        val mins = (subject.completedSeconds % 3600) / 60
                        val totalSecs = subjects.sumOf { it.completedSeconds }.coerceAtLeast(1)
                        val pct = ((subject.completedSeconds.toFloat() / totalSecs.toFloat()) * 100).toInt()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(
                                            try {
                                                Color(android.graphics.Color.parseColor(subject.categoryColorHex))
                                            } catch (e: Exception) {
                                                FocusCyan
                                            },
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = subject.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${hrs}h ${mins}m",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "$pct%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FocusCyan
                                )
                            }
                        }
                    }
                }
            }
        }

        // AI Focus Coach Smart Insights Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusSurface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(FocusPurple.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Coach",
                            tint = FocusPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "AI FOCUS COACH INSIGHT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = FocusPurple
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your peak study velocity occurs in 50-minute blocks with Strict Lock Level 2. Maintaining your 7-day streak will unlock maximum retention mode!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
