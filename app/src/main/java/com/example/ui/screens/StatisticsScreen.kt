package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart

import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FocusSession
import com.example.ui.theme.*
import com.example.ui.viewmodel.FocusViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

val StatBlue = Color(0xFF0EA5E9)
val StatGreen = Color(0xFF22C55E)
val SubjectColorPalette = listOf(
    Color(0xFF38BDF8),
    Color(0xFFF59E0B),
    Color(0xFFEC4899),
    Color(0xFF10B981),
    Color(0xFF8B5CF6),
    Color(0xFF06B6D4),
    Color(0xFFF97316),
    Color(0xFFA855F7),
    Color(0xFF64748B)
)

data class PeriodInfo(
    val title: String,
    val startMillis: Long,
    val endMillis: Long,
    val isCustom: Boolean = false
) {
    val displayRange: String get() {
        val sdf = SimpleDateFormat("M/d", Locale.getDefault())
        val days = maxOf(1, ((endMillis - startMillis) / (24 * 3600 * 1000L)).toInt() + 1)
        return "${sdf.format(Date(startMillis))} ~ ${sdf.format(Date(endMillis))} (${days}d)"
    }
    val shortRange: String get() {
        val sdf = SimpleDateFormat("M/d", Locale.getDefault())
        return "${sdf.format(Date(startMillis))} ~ ${sdf.format(Date(endMillis))}"
    }
}

fun formatSecondsToHms(totalSec: Int): String {
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, s)
}

fun formatSecondsToReadable(totalSec: Int): String {
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

fun calculatePeriodPreset(type: String, customStart: Long? = null, customEnd: Long? = null): PeriodInfo {
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    val now = cal.timeInMillis

    return when (type) {
        "Last 7 days" -> {
            val startCal = Calendar.getInstance().apply {
                timeInMillis = now
                add(Calendar.DAY_OF_YEAR, -6)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            PeriodInfo("Last 7 days", startCal.timeInMillis, now)
        }
        "Last 14 days" -> {
            val startCal = Calendar.getInstance().apply {
                timeInMillis = now
                add(Calendar.DAY_OF_YEAR, -13)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            PeriodInfo("Last 14 days", startCal.timeInMillis, now)
        }
        "Last 28 days" -> {
            val startCal = Calendar.getInstance().apply {
                timeInMillis = now
                add(Calendar.DAY_OF_YEAR, -27)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            PeriodInfo("Last 28 days", startCal.timeInMillis, now)
        }
        "Last 30 days" -> {
            val startCal = Calendar.getInstance().apply {
                timeInMillis = now
                add(Calendar.DAY_OF_YEAR, -29)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            PeriodInfo("Last 30 days", startCal.timeInMillis, now)
        }
        "Last 90 days" -> {
            val startCal = Calendar.getInstance().apply {
                timeInMillis = now
                add(Calendar.DAY_OF_YEAR, -89)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            PeriodInfo("Last 90 days", startCal.timeInMillis, now)
        }
        "This Month" -> {
            val startCal = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            PeriodInfo("This Month", startCal.timeInMillis, now)
        }
        "Last Month" -> {
            val startCal = Calendar.getInstance().apply {
                timeInMillis = now
                add(Calendar.MONTH, -1)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val endCal = Calendar.getInstance().apply {
                timeInMillis = startCal.timeInMillis
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            PeriodInfo("Last Month", startCal.timeInMillis, endCal.timeInMillis)
        }
        "Last 12 months" -> {
            val startCal = Calendar.getInstance().apply {
                timeInMillis = now
                add(Calendar.YEAR, -1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            PeriodInfo("Last 12 months", startCal.timeInMillis, now)
        }
        else -> {
            if (customStart != null && customEnd != null) {
                PeriodInfo("Select period", customStart, customEnd, isCustom = true)
            } else {
                val startCal = Calendar.getInstance().apply {
                    timeInMillis = now
                    add(Calendar.DAY_OF_YEAR, -29)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                PeriodInfo("Select period", startCal.timeInMillis, now, isCustom = true)
            }
        }
    }
}

@Composable
fun ConditionalScaffold(
    isExporting: Boolean,
    topBar: @Composable () -> Unit,
    containerColor: androidx.compose.ui.graphics.Color,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit
) {
    if (isExporting) {
        androidx.compose.material3.Surface(color = containerColor) {
            content(androidx.compose.foundation.layout.PaddingValues(0.dp))
        }
    } else {
        androidx.compose.material3.Scaffold(
            topBar = topBar,
            containerColor = containerColor,
            content = content
        )
    }
}

fun savePeriodToPrefs(sharedPrefs: android.content.SharedPreferences, prefix: String, period: PeriodInfo) {
    sharedPrefs.edit()
        .putString("${prefix}_TITLE", period.title)
        .putLong("${prefix}_START", period.startMillis)
        .putLong("${prefix}_END", period.endMillis)
        .putBoolean("${prefix}_IS_CUSTOM", period.isCustom)
        .apply()
}

fun loadPeriodFromPrefs(sharedPrefs: android.content.SharedPreferences, prefix: String, defaultPresetTitle: String): PeriodInfo {
    val title = sharedPrefs.getString("${prefix}_TITLE", null) ?: return calculatePeriodPreset(defaultPresetTitle)
    val isCustom = sharedPrefs.getBoolean("${prefix}_IS_CUSTOM", false)
    return if (isCustom) {
        val start = sharedPrefs.getLong("${prefix}_START", 0L)
        val end = sharedPrefs.getLong("${prefix}_END", 0L)
        PeriodInfo(title = title, startMillis = start, endMillis = end, isCustom = true)
    } else {
        calculatePeriodPreset(title)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: FocusViewModel,
    onBack: () -> Unit,
    isExporting: Boolean = false,
    initialTab: String = "Period",
    initialPrimaryPeriod: PeriodInfo? = null,
    initialComparisonPeriod: PeriodInfo? = null,
    initialCompareModeEnabled: Boolean? = null
) {
    val context = LocalContext.current
    val view = androidx.compose.ui.platform.LocalView.current
    val sharedPrefs = remember(context) { context.getSharedPreferences("FocusPrefs", android.content.Context.MODE_PRIVATE) }
    val allSessions by viewModel.allSessions.collectAsState()

    var selectedTab by remember { mutableStateOf(initialTab) }
    var showCompareDialog by remember { mutableStateOf(false) }
    var compareModeEnabled by remember { mutableStateOf(initialCompareModeEnabled ?: sharedPrefs.getBoolean("STAT_COMPARE_ENABLED", true)) }

    // Period state (Persistent from SharedPreferences)
    var primaryPeriod by remember {
        mutableStateOf(initialPrimaryPeriod ?: loadPeriodFromPrefs(sharedPrefs, "STAT_PRIMARY", "Last 30 days"))
    }
    var comparisonPeriod by remember {
        mutableStateOf(initialComparisonPeriod ?: loadPeriodFromPrefs(sharedPrefs, "STAT_COMP", "Last 90 days"))
    }

    // Calculations for Period 1
    val period1Sessions = remember(allSessions, primaryPeriod) {
        allSessions.filter {
            it.timestamp in primaryPeriod.startMillis..primaryPeriod.endMillis && it.completedDurationSeconds > 0
        }
    }
    val period1TotalSeconds = remember(period1Sessions) { period1Sessions.sumOf { it.completedDurationSeconds } }
    val period1Days = remember(primaryPeriod) {
        maxOf(1, ((primaryPeriod.endMillis - primaryPeriod.startMillis) / (24 * 3600 * 1000L)).toInt() + 1)
    }
    val period1DailyAvg = remember(period1TotalSeconds, period1Days) { period1TotalSeconds / period1Days }

    // Calculations for Period 2 (Comparison)
    val period2Sessions = remember(allSessions, comparisonPeriod) {
        allSessions.filter {
            it.timestamp in comparisonPeriod.startMillis..comparisonPeriod.endMillis && it.completedDurationSeconds > 0
        }
    }
    val period2TotalSeconds = remember(period2Sessions) { period2Sessions.sumOf { it.completedDurationSeconds } }
    val period2Days = remember(comparisonPeriod) {
        maxOf(1, ((comparisonPeriod.endMillis - comparisonPeriod.startMillis) / (24 * 3600 * 1000L)).toInt() + 1)
    }
    val period2DailyAvg = remember(period2TotalSeconds, period2Days) { period2TotalSeconds / period2Days }

    ConditionalScaffold(
        isExporting = isExporting,
        topBar = {
            if (!isExporting) {
                TopAppBar(
                title = { Text("Statistics", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showCompareDialog = true }) {
                        Icon(Icons.Default.Tune, contentDescription = "Period Filter & Compare", tint = Color.White)
                    }
                    IconButton(onClick = { 
                        com.example.util.ComposeViewExporter.captureAndSaveComposeView(context = context, width = view.width) {
                            androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.wrapContentHeight()) {
                                StatisticsScreen(
                                    viewModel = viewModel,
                                    onBack = {},
                                    isExporting = true,
                                    initialTab = selectedTab,
                                    initialPrimaryPeriod = primaryPeriod,
                                    initialComparisonPeriod = comparisonPeriod,
                                    initialCompareModeEnabled = compareModeEnabled
                                )
                            }
                        }
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FocusBackground)
            )
            }
        },
        containerColor = FocusBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .let { if (isExporting) it.fillMaxWidth() else it.fillMaxSize() }
                .padding(padding)
                .padding(horizontal = 16.dp)
                .let { if (isExporting) it else it.verticalScroll(rememberScrollState()) },
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Navigation Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Period", "Day", "Week", "Month").forEach { tab ->
                    TabButton(
                        text = tab,
                        isSelected = selectedTab == tab,
                        onClick = { selectedTab = tab }
                    )
                }
            }

            when (selectedTab) {
                "Period" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PeriodCard(
                            color = StatBlue,
                            title = primaryPeriod.title,
                            subtitle = primaryPeriod.displayRange,
                            onClick = { showCompareDialog = true }
                        )
                        if (compareModeEnabled) {
                            PeriodCard(
                                color = StatGreen,
                                title = comparisonPeriod.title,
                                subtitle = comparisonPeriod.displayRange,
                                onClick = { showCompareDialog = true }
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = FocusSurface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Total time", color = StatBlue, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        formatSecondsToHms(period1TotalSeconds),
                                        color = Color.White,
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Daily average", color = StatBlue, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        formatSecondsToHms(period1DailyAvg),
                                        color = Color.White,
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light)
                                    )
                                }
                            }
                            if (compareModeEnabled) {
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = FocusSurfaceVariant)
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Total time", color = StatGreen, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            formatSecondsToHms(period2TotalSeconds),
                                            color = Color.White,
                                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Daily average", color = StatGreen, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            formatSecondsToHms(period2DailyAvg),
                                            color = Color.White,
                                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = FocusSurface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cumulative focus time", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                                CumulativeLineChart(
                                    period1 = primaryPeriod,
                                    sessions1 = period1Sessions,
                                    period2 = comparisonPeriod,
                                    sessions2 = period2Sessions,
                                    showCompare = compareModeEnabled
                                )
                            }
                        }
                    }

                    SubjectRatioCard(
                        color = StatBlue,
                        periodTitle = primaryPeriod.title,
                        sessions = period1Sessions
                    )

                    if (compareModeEnabled) {
                        SubjectRatioCard(
                            color = StatGreen,
                            periodTitle = comparisonPeriod.title,
                            sessions = period2Sessions
                        )
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = FocusSurface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.BarChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Subject time per day", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            DailyStackedBarChart(
                                period = primaryPeriod,
                                sessions = period1Sessions
                            )
                        }
                    }
                }
                "Day" -> { DayTabContent(allSessions = allSessions) }
                "Week" -> { WeekTabContent(allSessions = allSessions) }
                "Month" -> { MonthTabContent(allSessions = allSessions) }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }


    if (showCompareDialog) {
        CompareDialog(
            compareModeEnabled = compareModeEnabled,
            onCompareModeChanged = { 
                compareModeEnabled = it
                sharedPrefs.edit().putBoolean("STAT_COMPARE_ENABLED", it).apply()
            },
            currentPrimary = primaryPeriod,
            currentComparison = comparisonPeriod,
            onApply = { newPrimary, newComparison ->
                primaryPeriod = newPrimary
                comparisonPeriod = newComparison
                savePeriodToPrefs(sharedPrefs, "STAT_PRIMARY", newPrimary)
                savePeriodToPrefs(sharedPrefs, "STAT_COMP", newComparison)
                sharedPrefs.edit().putBoolean("STAT_COMPARE_ENABLED", compareModeEnabled).apply()
                showCompareDialog = false
            },
            onDismiss = { showCompareDialog = false }
        )
    }
}
}
@Composable
fun PeriodCard(
    color: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FocusSurface),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(color, RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(title, color = Color.White, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(subtitle, color = FocusTextSecondary, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Pick date",
                    tint = FocusTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
            }
        }
    }
}

@Composable
fun CumulativeLineChart(
    period1: PeriodInfo,
    sessions1: List<FocusSession>,
    period2: PeriodInfo,
    sessions2: List<FocusSession>,
    showCompare: Boolean
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val padLeft = 60f
        val padBottom = 50f
        val padTop = 20f
        val w = size.width - padLeft - 20f
        val h = size.height - padBottom - padTop

        // Determine step points (10 samples across the period)
        val steps = 10
        val p1Duration = maxOf(1L, period1.endMillis - period1.startMillis)
        val p2Duration = maxOf(1L, period2.endMillis - period2.startMillis)

        val p1CumHours = FloatArray(steps + 1)
        val p2CumHours = FloatArray(steps + 1)

        for (i in 0..steps) {
            val p1TimeLimit = period1.startMillis + (p1Duration * i / steps)
            val p1Secs = sessions1.filter { it.timestamp <= p1TimeLimit }.sumOf { it.completedDurationSeconds }
            p1CumHours[i] = p1Secs / 3600f

            if (showCompare) {
                val p2TimeLimit = period2.startMillis + (p2Duration * i / steps)
                val p2Secs = sessions2.filter { it.timestamp <= p2TimeLimit }.sumOf { it.completedDurationSeconds }
                p2CumHours[i] = p2Secs / 3600f
            }
        }

        val maxHourVal = maxOf(1f, maxOf(p1CumHours.maxOrNull() ?: 0f, if (showCompare) p2CumHours.maxOrNull() ?: 0f else 0f))
        val yCeil = kotlin.math.ceil(maxHourVal).toInt().coerceAtLeast(3)

        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#9CA3AF")
            textSize = 26f
            isAntiAlias = true
        }

        // Draw horizontal grid lines and Y-axis labels
        val yDivisions = 4
        for (i in 0..yDivisions) {
            val yVal = yCeil * (yDivisions - i) / yDivisions
            val yPos = padTop + (h * i / yDivisions)
            drawLine(
                color = FocusSurfaceVariant.copy(alpha = 0.6f),
                start = Offset(padLeft, yPos),
                end = Offset(padLeft + w, yPos),
                strokeWidth = 1f
            )
            drawContext.canvas.nativeCanvas.drawText("${yVal}h", 8f, yPos + 8f, paint)
        }

        // Draw X-axis labels
        val xLabels = listOf("Start", "25%", "50%", "75%", "End")
        xLabels.forEachIndexed { i, label ->
            val xPos = padLeft + (w * i / (xLabels.size - 1))
            drawContext.canvas.nativeCanvas.drawText(label, xPos - 20f, size.height - 10f, paint)
        }

        // Helper to construct path
        fun createPath(data: FloatArray): Path {
            val path = Path()
            for (i in 0..steps) {
                val x = padLeft + (w * i / steps)
                val normalizedY = (data[i] / yCeil).coerceIn(0f, 1f)
                val y = padTop + h - (normalizedY * h)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            return path
        }

        // Draw Comparison Line (Green)
        if (showCompare) {
            val greenPath = createPath(p2CumHours)
            drawPath(
                path = greenPath,
                color = StatGreen,
                style = Stroke(width = 3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // Draw Primary Line (Blue)
        val bluePath = createPath(p1CumHours)
        drawPath(
            path = bluePath,
            color = StatBlue,
            style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun SubjectRatioCard(
    color: Color,
    periodTitle: String,
    sessions: List<FocusSession>,
    isSecond: Boolean = false
) {
    val totalSeconds = sessions.sumOf { it.completedDurationSeconds }
    val subjectMap = remember(sessions) {
        sessions.groupBy { it.subjectName }
            .mapValues { (_, list) -> list.sumOf { it.completedDurationSeconds } }
            .toList()
            .sortedByDescending { it.second }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = FocusSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(color, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Subject ratio – $periodTitle",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (totalSeconds == 0 || subjectMap.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(110.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = FocusSurfaceVariant,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 24f)
                            )
                        }
                        Text("0h", color = FocusTextSecondary, style = MaterialTheme.typography.titleSmall)
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Text("No study logs available for this period.", color = FocusTextSecondary, style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Donut Chart
                    Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            var currentAngle = -90f
                            subjectMap.forEachIndexed { index, (_, secs) ->
                                val sweep = (secs.toFloat() / totalSeconds.toFloat()) * 360f
                                val sliceColor = SubjectColorPalette[index % SubjectColorPalette.size]
                                drawArc(
                                    color = sliceColor,
                                    startAngle = currentAngle,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    style = Stroke(width = 28f)
                                )
                                currentAngle += sweep
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(formatSecondsToReadable(totalSeconds), color = Color.White, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Legends List
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        subjectMap.take(5).forEachIndexed { index, (name, secs) ->
                            val sliceColor = SubjectColorPalette[index % SubjectColorPalette.size]
                            val percent = ((secs.toFloat() / totalSeconds) * 100).toInt()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(modifier = Modifier.size(10.dp).background(sliceColor, CircleShape))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        name,
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1
                                    )
                                }
                                Text(
                                    "${formatSecondsToReadable(secs)} ($percent%)",
                                    color = FocusTextSecondary,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyStackedBarChart(
    period: PeriodInfo,
    sessions: List<FocusSession>
) {
    // Generate up to last 12 days in the period
    val sdf = SimpleDateFormat("M/d", Locale.getDefault())
    val cal = Calendar.getInstance()
    
    val daysCount = minOf(12, maxOf(5, ((period.endMillis - period.startMillis) / (24 * 3600 * 1000L)).toInt() + 1))
    val dayLabels = mutableListOf<String>()
    val dayStartTimes = mutableListOf<Long>()

    for (i in (daysCount - 1) downTo 0) {
        val dayCal = Calendar.getInstance().apply {
            timeInMillis = period.endMillis - (i * 24 * 3600 * 1000L)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        dayLabels.add(sdf.format(Date(dayCal.timeInMillis)))
        dayStartTimes.add(dayCal.timeInMillis)
    }

    // Compute subject times per day
    val subjects = sessions.map { it.subjectName }.distinct()
    val subjectColorMap = subjects.mapIndexed { idx, sub -> sub to SubjectColorPalette[idx % SubjectColorPalette.size] }.toMap()

    val daySubjectData = dayStartTimes.map { startTime ->
        val endTime = startTime + (24 * 3600 * 1000L) - 1
        val daySessions = sessions.filter { it.timestamp in startTime..endTime }
        daySessions.groupBy { it.subjectName }
            .mapValues { (_, list) -> list.sumOf { it.completedDurationSeconds } }
    }

    val maxDailySeconds = daySubjectData.maxOfOrNull { map -> map.values.sum() } ?: 0
    val maxDailyHours = maxOf(1f, maxDailySeconds / 3600f)

    Column {
        Text(
            "Daily max: ${formatSecondsToReadable(maxDailySeconds)}",
            color = FocusTextSecondary,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val padBottom = 40f
                val h = size.height - padBottom
                val stepX = size.width / dayLabels.size
                val barWidth = stepX * 0.6f

                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#9CA3AF")
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }

                dayLabels.forEachIndexed { i, label ->
                    val centerX = (i * stepX) + (stepX / 2)
                    drawContext.canvas.nativeCanvas.drawText(label, centerX, size.height - 8f, paint)

                    val subData = daySubjectData[i]
                    val totalSec = subData.values.sum()

                    if (totalSec > 0) {
                        var currentY = h
                        subData.forEach { (sub, secs) ->
                            val segHeight = (secs / (maxDailyHours * 3600f)) * h
                            val color = subjectColorMap[sub] ?: StatBlue
                            drawRoundRect(
                                color = color,
                                topLeft = Offset(centerX - (barWidth / 2), currentY - segHeight),
                                size = Size(barWidth, segHeight),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                            currentY -= segHeight
                        }
                    } else {
                        // Empty bar placeholder
                        drawRoundRect(
                            color = FocusSurfaceVariant.copy(alpha = 0.3f),
                            topLeft = Offset(centerX - (barWidth / 2), h - 6f),
                            size = Size(barWidth, 6f),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        if (subjects.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(subjects) { sub ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(subjectColorMap[sub] ?: StatBlue, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(sub, color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        } else {
            Text("No subject focus logged in this period.", color = FocusTextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun CompareDialog(
    compareModeEnabled: Boolean,
    onCompareModeChanged: (Boolean) -> Unit,
    currentPrimary: PeriodInfo,
    currentComparison: PeriodInfo,
    onApply: (PeriodInfo, PeriodInfo) -> Unit,
    onDismiss: () -> Unit
) {
    val presetOptions = listOf(
        "Last 7 days",
        "Last 14 days",
        "Last 28 days",
        "Last 30 days",
        "Last 90 days",
        "This Month",
        "Last Month",
        "Last 12 months",
        "Select period"
    )

    var selectedPrimaryType by remember { mutableStateOf(if (currentPrimary.isCustom || currentPrimary.title !in presetOptions) "Select period" else currentPrimary.title) }
    var selectedComparisonType by remember { mutableStateOf(if (currentComparison.isCustom || currentComparison.title !in presetOptions) "Select period" else currentComparison.title) }

    var tempPrimary by remember { mutableStateOf(currentPrimary) }
    var tempComparison by remember { mutableStateOf(currentComparison) }

    var datePickerTargetPeriod by remember { mutableStateOf<Int?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusSurface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header & Compare Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Period & Compare",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Compare", color = if (compareModeEnabled) StatGreen else FocusTextSecondary, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = compareModeEnabled,
                            onCheckedChange = { onCompareModeChanged(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = StatGreen,
                                uncheckedTrackColor = FocusSurfaceVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Columns for Primary and Comparison
                Box(modifier = Modifier.weight(1f, fill = false).heightIn(max = 420.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                        // Primary Period Column (StatBlue)
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).background(StatBlue, RoundedCornerShape(2.dp)))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Primary", color = StatBlue, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            presetOptions.forEach { opt ->
                                val isSelected = selectedPrimaryType == opt
                                val subtitle = if (opt == "Select period") {
                                    tempPrimary.shortRange
                                } else {
                                    calculatePeriodPreset(opt).shortRange
                                }

                                CompareOptionCard(
                                    title = opt,
                                    subtitle = subtitle,
                                    isSelected = isSelected,
                                    activeColor = StatBlue,
                                    isCustomOption = opt == "Select period",
                                    onCardClick = {
                                        selectedPrimaryType = opt
                                        if (opt == "Select period") {
                                            datePickerTargetPeriod = 1
                                        } else {
                                            tempPrimary = calculatePeriodPreset(opt)
                                        }
                                    },
                                    onPickDatesClick = {
                                        selectedPrimaryType = "Select period"
                                        datePickerTargetPeriod = 1
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Comparison Period Column (StatGreen)
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).background(if (compareModeEnabled) StatGreen else FocusTextSecondary.copy(alpha = 0.5f), RoundedCornerShape(2.dp)))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Compare",
                                    color = if (compareModeEnabled) StatGreen else FocusTextSecondary.copy(alpha = 0.5f),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            presetOptions.forEach { opt ->
                                val isSelected = compareModeEnabled && selectedComparisonType == opt
                                val subtitle = if (opt == "Select period") {
                                    tempComparison.shortRange
                                } else {
                                    calculatePeriodPreset(opt).shortRange
                                }

                                CompareOptionCard(
                                    title = opt,
                                    subtitle = subtitle,
                                    isSelected = isSelected,
                                    activeColor = StatGreen,
                                    enabled = compareModeEnabled,
                                    isCustomOption = opt == "Select period",
                                    onCardClick = {
                                        if (compareModeEnabled) {
                                            selectedComparisonType = opt
                                            if (opt == "Select period") {
                                                datePickerTargetPeriod = 2
                                            } else {
                                                tempComparison = calculatePeriodPreset(opt)
                                            }
                                        }
                                    },
                                    onPickDatesClick = {
                                        if (compareModeEnabled) {
                                            selectedComparisonType = "Select period"
                                            datePickerTargetPeriod = 2
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = FocusTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val finalPrimary = if (selectedPrimaryType == "Select period") tempPrimary else calculatePeriodPreset(selectedPrimaryType)
                            val finalComparison = if (selectedComparisonType == "Select period") tempComparison else calculatePeriodPreset(selectedComparisonType)
                            onApply(finalPrimary, finalComparison)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Apply", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (datePickerTargetPeriod != null) {
        val target = datePickerTargetPeriod!!
        val initialStart = if (target == 1) tempPrimary.startMillis else tempComparison.startMillis
        val initialEnd = if (target == 1) tempPrimary.endMillis else tempComparison.endMillis

        CustomDateRangePickerDialog(
            initialStartMillis = initialStart,
            initialEndMillis = initialEnd,
            targetColor = if (target == 1) StatBlue else StatGreen,
            onDateRangeSelected = { start, end ->
                val newPeriod = PeriodInfo(
                    title = "Select period",
                    startMillis = start,
                    endMillis = end,
                    isCustom = true
                )
                if (target == 1) {
                    tempPrimary = newPeriod
                    selectedPrimaryType = "Select period"
                } else {
                    tempComparison = newPeriod
                    selectedComparisonType = "Select period"
                }
                datePickerTargetPeriod = null
            },
            onDismiss = { datePickerTargetPeriod = null }
        )
    }
}

@Composable
fun CompareOptionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    activeColor: Color,
    enabled: Boolean = true,
    isCustomOption: Boolean = false,
    onCardClick: () -> Unit,
    onPickDatesClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) activeColor.copy(alpha = 0.15f) else FocusSurfaceVariant.copy(alpha = 0.4f))
            .border(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                color = if (isSelected) activeColor else FocusSurfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = enabled) { onCardClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (enabled) (if (isSelected) Color.White else FocusTextSecondary) else FocusTextSecondary.copy(alpha = 0.4f),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = if (enabled) (if (isSelected) activeColor else FocusTextSecondary.copy(alpha = 0.7f)) else FocusTextSecondary.copy(alpha = 0.3f),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                )
            }
            if (isCustomOption && enabled) {
                IconButton(
                    onClick = onPickDatesClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditCalendar,
                        contentDescription = "Pick custom dates",
                        tint = if (isSelected) activeColor else FocusTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = activeColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDateRangePickerDialog(
    initialStartMillis: Long,
    initialEndMillis: Long,
    targetColor: Color = StatBlue,
    onDateRangeSelected: (Long, Long) -> Unit,
    onDismiss: () -> Unit
) {
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStartMillis,
        initialSelectedEndDateMillis = initialEndMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val start = dateRangePickerState.selectedStartDateMillis ?: initialStartMillis
                    val end = dateRangePickerState.selectedEndDateMillis ?: start
                    // Convert UTC millis from date picker to end of day
                    val startCal = Calendar.getInstance().apply {
                        timeInMillis = start
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val endCal = Calendar.getInstance().apply {
                        timeInMillis = end
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }
                    onDateRangeSelected(startCal.timeInMillis, endCal.timeInMillis)
                },
                colors = ButtonDefaults.buttonColors(containerColor = targetColor)
            ) {
                Text("Select Range", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = FocusTextSecondary)
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = FocusSurface
        )
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            title = {
                Text(
                    text = "Select Date Range",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            },
            headline = {
                val start = dateRangePickerState.selectedStartDateMillis
                val end = dateRangePickerState.selectedEndDateMillis
                val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val text = if (start != null && end != null) {
                    "${sdf.format(Date(start))} – ${sdf.format(Date(end))}"
                } else if (start != null) {
                    "Start: ${sdf.format(Date(start))} (Tap end date)"
                } else {
                    "Tap start and end dates on calendar"
                }
                Text(
                    text = text,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = targetColor
                )
            },
            colors = DatePickerDefaults.colors(
                containerColor = FocusSurface,
                titleContentColor = Color.White,
                headlineContentColor = Color.White,
                weekdayContentColor = FocusTextSecondary,
                subheadContentColor = Color.White,
                yearContentColor = Color.White,
                currentYearContentColor = targetColor,
                selectedYearContentColor = Color.White,
                selectedYearContainerColor = targetColor,
                dayContentColor = Color.White,
                selectedDayContainerColor = targetColor,
                selectedDayContentColor = Color.White,
                dayInSelectionRangeContainerColor = targetColor.copy(alpha = 0.25f),
                dayInSelectionRangeContentColor = Color.White
            )
        )
    }
}

@Composable
fun DayTabContent(allSessions: List<FocusSession>) {
    var viewedCal by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDayCal by remember { mutableStateOf(Calendar.getInstance()) }

    val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(viewedCal.time)

    // Calculate daily sessions
    val sdfDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val selectedDayStr = sdfDay.format(selectedDayCal.time)

    val selectedDaySessions = remember(allSessions, selectedDayStr) {
        allSessions.filter {
            sdfDay.format(Date(it.timestamp)) == selectedDayStr && it.completedDurationSeconds > 0
        }
    }
    val selectedDayTotalSec = remember(selectedDaySessions) {
        selectedDaySessions.sumOf { it.completedDurationSeconds }
    }

    // Days in current month
    val monthCal = Calendar.getInstance().apply {
        time = viewedCal.time
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val firstDayOfWeek = monthCal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
    val maxDaysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val leadDays = (firstDayOfWeek + 5) % 7 // adjust to Mon=0

    Card(
        colors = CardDefaults.cardColors(containerColor = FocusSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Month Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    viewedCal = (viewedCal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                }) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous Month", tint = Color.White)
                }
                Text(monthName, color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                IconButton(onClick = {
                    viewedCal = (viewedCal.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                }) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next Month", tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Weekday Headers
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach {
                    Text(it, color = FocusTextSecondary, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            // Days Grid (6 rows x 7 columns)
            val totalCells = ((leadDays + maxDaysInMonth + 6) / 7) * 7
            for (row in 0 until (totalCells / 7)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    for (col in 0 until 7) {
                        val dayNumber = (row * 7 + col) - leadDays + 1
                        if (dayNumber in 1..maxDaysInMonth) {
                            val thisDayCal = (monthCal.clone() as Calendar).apply {
                                set(Calendar.DAY_OF_MONTH, dayNumber)
                            }
                            val dateStr = sdfDay.format(thisDayCal.time)
                            val daySec = allSessions.filter { sdfDay.format(Date(it.timestamp)) == dateStr }.sumOf { it.completedDurationSeconds }
                            val isSelected = selectedDayStr == dateStr

                            val bgIntensity = when {
                                daySec > 3 * 3600 -> StatBlue.copy(alpha = 0.8f)
                                daySec > 3600 -> StatBlue.copy(alpha = 0.5f)
                                daySec > 0 -> StatBlue.copy(alpha = 0.25f)
                                else -> Color.Transparent
                            }

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bgIntensity)
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        selectedDayCal = thisDayCal
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$dayNumber",
                                    color = if (isSelected || daySec > 0) Color.White else FocusTextSecondary,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(38.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Selected Day Study Logs Card
    Card(
        colors = CardDefaults.cardColors(containerColor = FocusSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault()).format(selectedDayCal.time),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    "Total: ${formatSecondsToReadable(selectedDayTotalSec)}",
                    color = StatBlue,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (selectedDaySessions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No focus study logs recorded for this day.", color = FocusTextSecondary, style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    selectedDaySessions.forEach { session ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(FocusSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(session.subjectName, color = Color.White, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text(
                                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(session.timestamp)),
                                    color = FocusTextSecondary,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Text(
                                formatSecondsToReadable(session.completedDurationSeconds),
                                color = StatBlue,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeekTabContent(allSessions: List<FocusSession>) {
    var viewedCal by remember { mutableStateOf(Calendar.getInstance()) }
    val year = viewedCal.get(Calendar.YEAR)
    val quarter = (viewedCal.get(Calendar.MONTH) / 3) + 1

    Card(
        colors = CardDefaults.cardColors(containerColor = FocusSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    viewedCal = (viewedCal.clone() as Calendar).apply { add(Calendar.MONTH, -3) }
                }) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = Color.White)
                }
                Text("$year Q$quarter", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                IconButton(onClick = {
                    viewedCal = (viewedCal.clone() as Calendar).apply { add(Calendar.MONTH, 3) }
                }) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Generate 12 weeks for the quarter
            val sdfWeek = SimpleDateFormat("M/d", Locale.getDefault())
            val qStartCal = Calendar.getInstance().apply {
                time = viewedCal.time
                set(Calendar.MONTH, (quarter - 1) * 3)
                set(Calendar.DAY_OF_MONTH, 1)
            }

            val weekData = (0 until 12).map { wIdx ->
                val start = qStartCal.timeInMillis + (wIdx * 7 * 24 * 3600 * 1000L)
                val end = start + (7 * 24 * 3600 * 1000L) - 1
                val totalSec = allSessions.filter { it.timestamp in start..end }.sumOf { it.completedDurationSeconds }
                Triple("${sdfWeek.format(Date(start))} ~", totalSec, totalSec > 0)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (row in 0 until 3) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (col in 0 until 4) {
                            val idx = row * 4 + col
                            if (idx < weekData.size) {
                                val (label, sec, hasData) = weekData[idx]
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (hasData) StatBlue.copy(alpha = 0.2f) else FocusSurfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .padding(vertical = 12.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            if (sec > 0) formatSecondsToReadable(sec) else "0h",
                                            color = if (sec > 0) StatBlue else FocusTextSecondary,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
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

@Composable
fun MonthTabContent(allSessions: List<FocusSession>) {
    var viewedCal by remember { mutableStateOf(Calendar.getInstance()) }
    val year = viewedCal.get(Calendar.YEAR)

    Card(
        colors = CardDefaults.cardColors(containerColor = FocusSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    viewedCal = (viewedCal.clone() as Calendar).apply { add(Calendar.YEAR, -1) }
                }) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = Color.White)
                }
                Text("$year", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                IconButton(onClick = {
                    viewedCal = (viewedCal.clone() as Calendar).apply { add(Calendar.YEAR, 1) }
                }) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val monthData = (0 until 12).map { mIdx ->
                val mCalStart = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, mIdx)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val mCalEnd = Calendar.getInstance().apply {
                    timeInMillis = mCalStart.timeInMillis
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                val sec = allSessions.filter { it.timestamp in mCalStart.timeInMillis..mCalEnd.timeInMillis }.sumOf { it.completedDurationSeconds }
                Pair(monthNames[mIdx], sec)
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (row in 0 until 3) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (col in 0 until 4) {
                            val idx = row * 4 + col
                            val (mName, sec) = monthData[idx]
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (sec > 0) StatBlue.copy(alpha = 0.2f) else FocusSurfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(vertical = 14.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(mName, color = Color.White, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        if (sec > 0) formatSecondsToReadable(sec) else "0h",
                                        color = if (sec > 0) StatBlue else FocusTextSecondary,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    androidx.compose.material3.TextButton(
        onClick = onClick,
        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
            contentColor = if (isSelected) androidx.compose.ui.graphics.Color.White else FocusTextSecondary
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        modifier = androidx.compose.ui.Modifier.background(
            if (isSelected) FocusSurfaceVariant else androidx.compose.ui.graphics.Color.Transparent,
            androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
        )
    ) {
        androidx.compose.material3.Text(text, style = androidx.compose.material3.MaterialTheme.typography.labelMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))
    }
}
