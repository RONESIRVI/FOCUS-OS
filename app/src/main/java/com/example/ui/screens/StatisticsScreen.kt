package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.FocusViewModel

val StatBlue = Color(0xFF0EA5E9)
val StatGreen = Color(0xFF22C55E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: FocusViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("Period") }
    var showCompareDialog by remember { mutableStateOf(false) }
    var compareModeEnabled by remember { mutableStateOf(true) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        Toast.makeText(context, "Statistics downloaded successfully.", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FocusBackground)
            )
        },
        containerColor = FocusBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tabs
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val tabs = listOf("Period", "Day", "Week", "Month", "Trend")
                    items(tabs) { tab ->
                        val isSelected = tab == selectedTab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) StatBlue else Color.Transparent)
                                .border(1.dp, if (isSelected) Color.Transparent else FocusSurfaceVariant, RoundedCornerShape(20.dp))
                                .clickable { selectedTab = tab }
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = tab,
                                color = if (isSelected) Color.White else FocusTextSecondary,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            if (selectedTab == "Period") {
            // Period Selectors
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PeriodCard(
                        color = StatBlue,
                        title = "Select period",
                        subtitle = "6/1 ~ 6/30 (30d)",
                        onClick = { showCompareDialog = true }
                    )
                    if (compareModeEnabled) {
                        PeriodCard(
                            color = StatGreen,
                            title = "Last 90 days",
                            subtitle = "5/29 ~ 8/26 (90d)",
                            onClick = { showCompareDialog = true }
                        )
                    }
                }
            }

            // Summary Cards
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = FocusSurface),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total time", color = StatBlue, style = MaterialTheme.typography.bodyMedium)
                                Text("0:00:00", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Daily average", color = StatBlue, style = MaterialTheme.typography.bodyMedium)
                                Text("0:00:00", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light))
                            }
                        }
                        if (compareModeEnabled) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Divider(color = FocusSurfaceVariant)
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Total time", color = StatGreen, style = MaterialTheme.typography.bodyMedium)
                                    Text("0:00:00", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Daily average", color = StatGreen, style = MaterialTheme.typography.bodyMedium)
                                    Text("0:00:00", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light))
                                }
                            }
                        }
                    }
                }
            }

            // Cumulative Time Comparison Line Graph
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = FocusSurface),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(260.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Cumulative time comparison", color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(modifier = Modifier.fillMaxSize()) {
                            CumulativeLineChart(compareModeEnabled)
                        }
                    }
                }
            }

            // Subject Ratio Pie Chart 1 (Blue)
            item {
                SubjectRatioCard(color = StatBlue)
            }

            // Subject Ratio Pie Chart 2 (Green)
            if (compareModeEnabled) {
                item {
                    SubjectRatioCard(color = StatGreen, isSecond = true)
                }
            }

            // Subject time per day - Stacked Bar Chart
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = FocusSurface),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(320.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(StatBlue, RoundedCornerShape(2.dp)))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Subject time per day", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Daily max: 0h 0m", color = FocusTextSecondary, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            StackedBarChart()
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        // Legend (Empty)
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
                            Text("No subject data available", color = FocusTextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            } else if (selectedTab == "Day") {
                item { DayTabContent() }
            } else if (selectedTab == "Week") {
                item { WeekTabContent() }
            } else if (selectedTab == "Month") {
                item { MonthTabContent() }
            } else {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        Text("Coming Soon", color = FocusTextSecondary)
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }

    if (showCompareDialog) {
        CompareDialog(
            compareModeEnabled = compareModeEnabled,
            onCompareModeChanged = { compareModeEnabled = it },
            onDismiss = { showCompareDialog = false }
        )
    }
}

@Composable
fun PeriodCard(color: Color, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FocusSurface),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).background(color, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                    Text(subtitle, color = FocusTextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
fun CumulativeLineChart(showCompare: Boolean) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val yLabels = listOf("18", "15", "12", "9", "6", "3", "0")
        val xLabels = listOf("Start", "Day 12", "Day 24", "Day 36", "Day 48", "Day 60", "Day 72", "Day 84")
        
        val padLeft = 60f
        val padBottom = 60f
        val w = size.width - padLeft
        val h = size.height - padBottom
        
        // Draw grid and y-labels
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#9CA3AF")
            textSize = 30f
        }
        
        val stepY = h / (yLabels.size - 1)
        yLabels.forEachIndexed { i, label ->
            val y = i * stepY
            drawContext.canvas.nativeCanvas.drawText(label, 0f, y + 10f, paint)
        }
        
        // Draw x-labels
        val stepX = w / (xLabels.size - 1)
        xLabels.forEachIndexed { i, label ->
            val x = padLeft + (i * stepX)
            drawContext.canvas.nativeCanvas.drawText(label, x - 20f, size.height - 10f, paint)
        }

        // Draw Line 1 (Blue) - Empty Data
        val bluePath = Path().apply {
            moveTo(padLeft, h) // start at 0
            lineTo(w + padLeft, h) // Flat line at 0
        }
        drawPath(path = bluePath, color = StatBlue, style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round))

        if (showCompare) {
            val greenPath = Path().apply {
                moveTo(padLeft, h)
                lineTo(w + padLeft, h) // Flat line at 0
            }
            drawPath(path = greenPath, color = StatGreen, style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}

@Composable
fun SubjectRatioCard(color: Color, isSecond: Boolean = false) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FocusSurface),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(color, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Subject ratio", color = Color.White, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Pie Chart
                Box(modifier = Modifier.size(140.dp)) {
                    DonutChart(isSecond)
                }
                Spacer(modifier = Modifier.width(24.dp))
                // Legend list (Empty)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("No data available", color = FocusTextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun DonutChart(isSecond: Boolean) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 40f
        drawArc(
            color = FocusSurfaceVariant,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )
    }
}

@Composable
fun StackedBarChart() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val xLabels = listOf("6/19", "6/20", "6/21", "6/22", "6/23", "6/24", "6/25", "6/26", "6/27", "6/28", "6/29", "6/30")
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#9CA3AF")
            textSize = 28f
        }
        
        val stepX = size.width / (xLabels.size)
        val barWidth = stepX * 0.6f
        val h = size.height - 40f
        
        xLabels.forEachIndexed { i, label ->
            val x = (i * stepX) + (stepX/2) - 30f
            drawContext.canvas.nativeCanvas.drawText(label, x, size.height, paint)
        }
        
        // Hardcode a few stacked bars to match screenshot
        // Colors: Blue(RAS), LBlue(Adv RAS), Red(PYQS), Green(Rev), Yellow(Value), LGreen(Mock)
        val cRas = Color(0xFF2563EB)
        val cRed = Color(0xFFDC2626)
        val cGreen = Color(0xFF65A30D)
        val cLGreen = Color(0xFF22C55E)
        val cYellow = Color(0xFFEAB308)
        
        fun drawStackedBar(index: Int, segments: List<Pair<Color, Float>>) {
            val x = (index * stepX) + (stepX - barWidth)/2
            var currentY = h
            segments.forEach { (color, height) ->
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, currentY - height),
                    size = Size(barWidth, height),
                    cornerRadius = CornerRadius(0f)
                )
                currentY -= height
            }
        }
        
        // No study logs yet
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, color = FocusTextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun CompareDialog(compareModeEnabled: Boolean, onCompareModeChanged: (Boolean) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusSurfaceVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    Text("Compare", color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = compareModeEnabled,
                        onCheckedChange = { onCompareModeChanged(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = FocusBackground, checkedTrackColor = StatGreen)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        CompareOptionCard("Select period", "7/29 ~ 8/26", true, StatBlue)
                        CompareOptionCard("Last 28 days", "7/30 ~ 8/26", false, Color.Transparent)
                        CompareOptionCard("Last 90 days", "5/29 ~ 8/26", false, Color.Transparent)
                        CompareOptionCard("Last 12 months", "8/26/2025 ~ 8/26", false, Color.Transparent)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        CompareOptionCard("Select period", "7/29 ~ 8/26", false, Color.Transparent)
                        CompareOptionCard("Last 28 days", "7/30 ~ 8/26", false, Color.Transparent)
                        CompareOptionCard("Last 90 days", "5/29 ~ 8/26", true, StatGreen)
                        CompareOptionCard("Last 12 months", "8/26/2025 ~ 8/26", false, Color.Transparent)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = Color.White) }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onDismiss) { Text("OK", color = Color.White) }
                }
            }
        }
    }
}

@Composable
fun CompareOptionCard(title: String, subtitle: String, isSelected: Boolean, strokeColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(if (isSelected) 1.dp else 0.dp, strokeColor, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(title, color = Color.White, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, color = FocusTextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}


@Composable
fun DayTabContent() {
    Card(
        colors = CardDefaults.cardColors(containerColor = FocusSurface),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = Color.White)
                Text("Aug", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach {
                    Text(it, color = FocusTextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            val days = (27..31).map { it.toString() to false } + (1..31).map { it.toString() to true } + (1..6).map { it.toString() to false }
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                for (row in 0 until 6) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        for (col in 0 until 7) {
                            val index = row * 7 + col
                            if (index < days.size) {
                                val (day, currentMonth) = days[index]
                                val isSelected = currentMonth && day == "26"
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .border(if (isSelected) 1.dp else 0.dp, if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day,
                                        color = if (currentMonth) Color.White else FocusTextSecondary,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(36.dp))
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val colors = listOf(Color(0xFF0F172A), Color(0xFF1E3A8A), Color(0xFF1D4ED8), Color(0xFF3B82F6), Color(0xFF60A5FA))
                    val labels = listOf("0+", "4+", "7+", "10+", "12+")
                    colors.zip(labels).forEach { (color, label) ->
                        Box(modifier = Modifier.background(color).padding(horizontal = 4.dp, vertical = 2.dp)) {
                            Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                        }
                    }
                }
                Text("Aug: 0H 00M", color = FocusTextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun WeekTabContent() {
    Column {
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusSurface),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("2026 Q3", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.height(24.dp))
                
                val weeks = listOf(
                    Triple("6/29 ~", "", false), Triple("7/6 ~", "", false), Triple("7/13 ~", "", false), Triple("7/20 ~", "", false), Triple("7/27 ~", "", false),
                    Triple("8/3 ~", "", false), Triple("8/10 ~", "", false), Triple("8/17 ~", "", false), Triple("8/24 ~", "", false), Triple("8/31 ~", "", false),
                    Triple("9/7 ~", "", false), Triple("9/14 ~", "", false), Triple("9/21 ~", "", false), Triple("9/28 ~", "", false), Triple("", "", false)
                )
                
                Column {
                    for (row in 0 until 3) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (col in 0 until 5) {
                                val index = row * 5 + col
                                if (index < weeks.size) {
                                    val (week, time, isHighlighted) = weeks[index]
                                    val isSelected = week == "8/24 ~"
                                    val hasData = time.isNotEmpty()
                                    if (week.isEmpty()) {
                                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                                        continue
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .background(if (hasData) StatBlue.copy(alpha = if (isHighlighted) 0.3f else 0.1f) else Color.Transparent)
                                            .border(if (isSelected) 1.dp else 0.dp, if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(2.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(week, color = if (hasData || isSelected) Color.White else FocusTextSecondary, style = MaterialTheme.typography.bodySmall)
                                            if (hasData) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(time, color = Color.White, style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusSurface),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(150.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Mon, Aug 24 ~ Sun, Aug 30", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(24.dp))
                Text("No study logs.", color = Color.White, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun MonthTabContent() {
    Column {
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusSurface),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("2026", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.height(24.dp))
                
                val months = listOf(
                    Triple("Jan", "", false), Triple("Feb", "", false), Triple("Mar", "", false), Triple("Apr", "", false),
                    Triple("May", "", false), Triple("Jun", "", false), Triple("Jul", "", false), Triple("Aug", "", false),
                    Triple("Sep", "", false), Triple("Oct", "", false), Triple("Nov", "", false), Triple("Dec", "", false)
                )
                
                Column {
                    for (row in 0 until 3) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (col in 0 until 4) {
                                val index = row * 4 + col
                                val (month, time, isHighlighted) = months[index]
                                val isSelected = month == "Aug"
                                val hasData = time.isNotEmpty()
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .background(if (hasData) StatBlue.copy(alpha = if (isHighlighted) 0.3f else 0.1f) else Color.Transparent)
                                        .border(if (isSelected) 1.dp else 0.dp, if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(2.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(month, color = if (hasData || isSelected) Color.White else FocusTextSecondary, style = MaterialTheme.typography.bodyMedium)
                                        if (hasData) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(time, color = Color.White, style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusSurface),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(150.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Aug 2026", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(24.dp))
                Text("No study logs.", color = Color.White, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
