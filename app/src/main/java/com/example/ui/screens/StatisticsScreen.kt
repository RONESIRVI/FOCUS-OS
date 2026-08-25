package com.example.ui.screens

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.FocusViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun StatisticsScreen(
    viewModel: FocusViewModel,
    onBack: () -> Unit
) {
    val subjects by viewModel.allSubjects.collectAsState()
    val stats by viewModel.summaryStats.collectAsState()

    var selectedPeriodTab by remember { mutableStateOf("Weekly") }
    val periodTabs = listOf("Weekly", "Monthly", "All-Time")

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()

    // Multiplier for demo purposes to show different stats based on tab
    val timeMultiplier = when(selectedPeriodTab) {
        "Weekly" -> 1
        "Monthly" -> 4
        "All-Time" -> 20
        else -> 1
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusSlateBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }
                    drawLayer(graphicsLayer)
                }
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Header Navigation Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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

                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                saveBitmapToGallery(context, bitmap, selectedPeriodTab)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error saving image", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download Stats Image",
                        tint = FocusCyan
                    )
                }
            }

            // Top Segmented Period Tabs
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

            // Total Study Time Display Box
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

                    val displaySeconds = (stats.todayFocusSeconds + (subjects.sumOf { it.completedSeconds })) * timeMultiplier
                    val hrs = displaySeconds / 3600
                    val mins = (displaySeconds % 3600) / 60
                    val secs = displaySeconds % 60
                    val timeString = String.format("%02d:%02d:%02d", hrs, mins, secs)

                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                        color = FocusCyan
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Consistent study routine maintained",
                        style = MaterialTheme.typography.bodySmall,
                        color = FocusGreen
                    )
                }
            }

            // Subject Ratio Donut Chart Section
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

                    val activeSubjects = subjects.filter { (it.completedSeconds * timeMultiplier) > 0 }.takeIf { it.isNotEmpty() } ?: subjects

                    // Donut Chart Canvas Drawing
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(180.dp)) {
                            val totalSecs = activeSubjects.sumOf { it.completedSeconds * timeMultiplier }.coerceAtLeast(1)
                            var startAngle = -90f
                            val strokeWidth = 32.dp.toPx()
                            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                            activeSubjects.forEach { subject ->
                                val subjectSecs = subject.completedSeconds * timeMultiplier
                                val sweep = (subjectSecs.toFloat() / totalSecs.toFloat()) * 360f
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
                                text = "${activeSubjects.size} Active",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Legend Table Breakdown
                    activeSubjects.forEach { subject ->
                        val subjectSecs = subject.completedSeconds * timeMultiplier
                        val hrs = subjectSecs / 3600
                        val mins = (subjectSecs % 3600) / 60
                        val totalSecs = activeSubjects.sumOf { it.completedSeconds * timeMultiplier }.coerceAtLeast(1)
                        val pct = if (totalSecs > 0) ((subjectSecs.toFloat() / totalSecs.toFloat()) * 100).toInt() else 0

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

            // AI Focus Coach Smart Insights Card
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
                            text = "Great job! Consistently studying during your $selectedPeriodTab period improves retention.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

suspend fun saveBitmapToGallery(context: Context, bitmap: Bitmap, period: String) {
    withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val filename = "Focus_Stats_${period}_${System.currentTimeMillis()}.png"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/FocusOS")
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "$period Statistics Image Saved to Gallery!", Toast.LENGTH_LONG).show()
            }
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to save image.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
