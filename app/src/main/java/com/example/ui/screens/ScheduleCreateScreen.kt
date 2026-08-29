package com.example.ui.screens



import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LockMode
import com.example.services.SoundType
import com.example.ui.theme.*
import com.example.ui.viewmodel.FocusViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DatePresetType(val label: String, val offsetDays: Int) {
    TODAY("Today", 0),
    TOMORROW("Tomorrow", 1),
    DAY_AFTER("In 2 Days", 2),
    CUSTOM("Pick Date 📅", -1)
}

data class SubjectPreset(val name: String, val emoji: String, val color: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleCreateScreen(
    viewModel: FocusViewModel,
    onBack: () -> Unit,
    onNavigateToAppSelector: () -> Unit,
    onScheduleCreated: () -> Unit
) {
    val setup by viewModel.setupState.collectAsState()
    val whitelistedAppsStrict by viewModel.whitelistedAppsStrict.collectAsState()
    val whitelistedAppsSpecial by viewModel.whitelistedAppsSpecial.collectAsState()
    val whitelistedAppsManual by viewModel.whitelistedAppsManual.collectAsState()
    val scheduledSessions by viewModel.scheduledSessions.collectAsState(initial = emptyList())
    var showValidationDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var validationConflicts by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<List<com.example.data.model.FocusSession>>(emptyList()) }
    var nextValidationSession by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.example.data.model.FocusSession?>(null) }
    val userSubjects by viewModel.allSubjects.collectAsState()
    val context = LocalContext.current

    // Current time calendar base
    val nowCalendar = remember { Calendar.getInstance() }

    // Date State
    var selectedPreset by remember { mutableStateOf(DatePresetType.TODAY) }
    var selectedCalendar by remember {
        mutableStateOf(
            Calendar.getInstance().apply {
                add(Calendar.MINUTE, 5)
            }
        )
    }

    // Time State
    var startHour by remember { mutableStateOf(selectedCalendar.get(Calendar.HOUR_OF_DAY)) }
    var startMinute by remember { mutableStateOf(selectedCalendar.get(Calendar.MINUTE)) }

    var endHour by remember {
        val nextHourCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, startMinute)
            add(Calendar.MINUTE, 60)
        }
        mutableStateOf(nextHourCal.get(Calendar.HOUR_OF_DAY))
    }
    var endMinute by remember { mutableStateOf(startMinute) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showTwoStepTimeDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }

    @OptIn(ExperimentalMaterial3Api::class)
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedCalendar.timeInMillis)
    
    @OptIn(ExperimentalMaterial3Api::class)
    val startTimePickerState = rememberTimePickerState(initialHour = startHour, initialMinute = startMinute, is24Hour = false)
    
    @OptIn(ExperimentalMaterial3Api::class)
    val endTimePickerState = rememberTimePickerState(initialHour = endHour, initialMinute = endMinute, is24Hour = false)

    // Session & Subject details - clean, user writes according to their preference
    var subjectName by remember {
        mutableStateOf(setup.subjectName)
    }
    var sessionName by remember {
        mutableStateOf(setup.sessionName)
    }
    var selectedLockMode by remember { mutableStateOf(setup.lockMode) }
    var selectedWhitelistProfile by remember { mutableStateOf(setup.whitelistProfile) }
    var selectedModeId by remember { 
        mutableStateOf(
            when {
                setup.lockMode == LockMode.SOFT_LOCK -> "MINDFUL"
                setup.lockMode == LockMode.MAXIMUM_LOCK && setup.whitelistProfile == "STRICT" -> "DEEP_WORK"
                else -> "SPECIAL"
            }
        )
    }
    var selectedSound by remember { mutableStateOf(setup.selectedSound) }
    var requiresPhoto by remember { mutableStateOf(setup.requiresPhoto) }
    var requiresSelfie by remember { mutableStateOf(setup.requiresSelfie) }

    // Duration calculation
    val calculatedDurationMinutes = remember(startHour, startMinute, endHour, endMinute) {
        var startMins = startHour * 60 + startMinute
        var endMins = endHour * 60 + endMinute
        if (endMins <= startMins) {
            endMins += 24 * 60
        }
        (endMins - startMins).coerceAtLeast(5)
    }

    // Date formatting helper
    val dateFormatLong = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
    val dateFormatShort = SimpleDateFormat("EEE, d MMM", Locale.getDefault())

    // Date Picker Dialog function
    val openDatePicker = {
        showDatePicker = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "CREATE SCHEDULE",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "Set Custom Date & Strict Time Slots",
                            style = MaterialTheme.typography.labelSmall,
                            color = FocusTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("schedule_back_btn")) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = FocusSurfaceVariant,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = FocusWarning,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (selectedModeId) {
                                    "SPECIAL" -> "CUSTOM"
                                    "MINDFUL" -> "MINDFUL"
                                    else -> "DEEP WORK"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = FocusWarning
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FocusBackground
                )
            )
        },
        bottomBar = {
            Surface(
                color = FocusSurface,
                border = BorderStroke(1.dp, FocusOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .navigationBarsPadding()
                ) {
                    val formattedDate = dateFormatShort.format(selectedCalendar.time)
                    val startAmPm = if (startHour >= 12) "PM" else "AM"
                    val startH = if (startHour % 12 == 0) 12 else startHour % 12
                    val startM = String.format("%02d", startMinute)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⏰ $formattedDate • $startH:$startM $startAmPm",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = FocusTextSecondary
                        )
                        Text(
                            text = "⚡ $calculatedDurationMinutes mins block",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = FocusPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (selectedModeId == "SPECIAL" && selectedWhitelistProfile.isBlank()) {
                                android.widget.Toast.makeText(context, "Please select an App Blocking System", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                val userStart = selectedCalendar.timeInMillis
                                val userEnd = userStart + (calculatedDurationMinutes * 60 * 1000L)
                                val conflicts = scheduledSessions.filter { it.status == "SCHEDULED" }.filter { s ->
                                    val sStart = s.scheduledStartTime ?: return@filter false
                                    val sEnd = s.scheduledEndTime ?: return@filter false
                                    userStart < sEnd && userEnd > sStart
                                }
                                validationConflicts = conflicts
                                nextValidationSession = scheduledSessions.filter { it.status == "SCHEDULED" && (it.scheduledStartTime ?: 0) >= userEnd }.minByOrNull { it.scheduledStartTime ?: 0 }
                                showValidationDialog = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("confirm_schedule_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FocusWarning,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AlarmOn,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CONFIRM & LOCK SCHEDULE",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        },
        containerColor = FocusBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // ==========================================
            // SECTION 1: 📅 SCHEDULE DATE SELECTOR (CORE FEATURE)
            // ==========================================
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = FocusSurface),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, FocusPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = FocusPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SELECT SCHEDULE DATE",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = Color.White
                                )
                            }

                            Text(
                                text = "EXTRA SCHEDULE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = FocusWarning
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Date Selector Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DatePresetType.values().forEach { preset ->
                                val isSelected = selectedPreset == preset
                                val chipBorder = if (isSelected) FocusPrimary else FocusOutline
                                val chipBg = if (isSelected) FocusPrimary.copy(alpha = 0.15f) else FocusSurfaceVariant

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = chipBg,
                                    border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, chipBorder),
                                    modifier = Modifier
                                        .clickable {
                                            if (preset == DatePresetType.CUSTOM) {
                                                openDatePicker()
                                            } else {
                                                selectedPreset = preset
                                                selectedCalendar = Calendar.getInstance().apply {
                                                    add(Calendar.DAY_OF_YEAR, preset.offsetDays)
                                                    set(Calendar.HOUR_OF_DAY, startHour)
                                                    set(Calendar.MINUTE, startMinute)
                                                }
                                            }
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = FocusPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Text(
                                            text = preset.label,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            color = if (isSelected) FocusPrimary else FocusTextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Selected Date Interactive Banner
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = FocusBackground,
                            border = BorderStroke(1.dp, FocusOutline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { openDatePicker() }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Active Target Date",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = FocusTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = dateFormatLong.format(selectedCalendar.time),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White
                                    )
                                }

                                OutlinedButton(
                                    onClick = { openDatePicker() },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    border = BorderStroke(1.dp, FocusPrimary.copy(alpha = 0.6f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EditCalendar,
                                        contentDescription = "Pick Date",
                                        modifier = Modifier.size(14.dp),
                                        tint = FocusPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "CHANGE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FocusPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // SECTION 2: ⏰ TIME WINDOW & DURATION
            // ==========================================
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = FocusSurface),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, FocusOutline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = FocusWarning,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "STUDY TIME WINDOW",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = Color.White
                                )
                            }

                            // Dynamic duration chip
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = FocusWarning.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${calculatedDurationMinutes / 60}h ${calculatedDurationMinutes % 60}m (${calculatedDurationMinutes}m)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = FocusWarning,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 🎬 VIDEO FLOW: Interactive 2-Step Time & Duration Picker Banner
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = FocusPrimary.copy(alpha = 0.15f),
                            border = BorderStroke(1.5.dp, FocusPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showTwoStepTimeDialog = true }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = FocusPrimary,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.AccessTime,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "SELECT TIME & DURATION",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Time Picker ➔ कितने वक़्त के लिए? ⏱️",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                            color = FocusPrimary
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = FocusPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // START TIME CARD (Read-Only Display)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = FocusBackground,
                                border = BorderStroke(1.dp, FocusPrimary.copy(alpha = 0.3f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "START TIME",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = FocusPrimary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val amPm = if (startHour >= 12) "PM" else "AM"
                                    val h = if (startHour % 12 == 0) 12 else startHour % 12
                                    val m = String.format("%02d", startMinute)
                                    Text(
                                        text = "$h:$m $amPm",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "SCHEDULED START ⏱️",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = FocusTextSecondary
                                    )
                                }
                            }

                            // END TIME CARD (Read-Only Display)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = FocusBackground,
                                border = BorderStroke(1.dp, FocusWarning.copy(alpha = 0.3f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "END TIME",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = FocusWarning
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val amPm = if (endHour >= 12) "PM" else "AM"
                                    val h = if (endHour % 12 == 0) 12 else endHour % 12
                                    val m = String.format("%02d", endMinute)
                                    Text(
                                        text = "$h:$m $amPm",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "SCHEDULED END 🔔",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = FocusTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // SECTION 3: 📚 SUBJECT & STUDY GOAL
            // ==========================================
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = FocusSurface),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, FocusOutline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = FocusPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SUBJECT & STUDY GOAL",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = Color.White
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = FocusSurfaceVariant
                            ) {
                                Text(
                                    text = "Custom",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = FocusPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 1. Subject Name Input
                        Text(
                            text = "SUBJECT / AREA OF STUDY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = FocusTextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = subjectName,
                            onValueChange = { subjectName = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("schedule_subject_input"),
                            placeholder = {
                                Text(
                                    "e.g. Mathematics, Physics, History, UPSC, Coding...",
                                    color = FocusTextSecondary.copy(alpha = 0.45f),
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.School,
                                    contentDescription = null,
                                    tint = FocusPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (subjectName.isNotBlank()) {
                                    IconButton(onClick = { subjectName = "" }) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Clear Subject",
                                            tint = FocusTextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FocusPrimary,
                                unfocusedBorderColor = FocusOutline,
                                focusedContainerColor = FocusBackground,
                                unfocusedContainerColor = FocusBackground,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = false,
                            minLines = 1,
                            maxLines = 5
                        )

                        // Quick Pick from user's previously saved subjects (if any)
                        if (userSubjects.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Quick pick:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FocusTextSecondary.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                                userSubjects.forEach { sub ->
                                    val isSel = subjectName.equals(sub.name, ignoreCase = true)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSel) FocusPrimary.copy(alpha = 0.2f) else FocusBackground,
                                        border = BorderStroke(1.dp, if (isSel) FocusPrimary else FocusOutline.copy(alpha = 0.6f)),
                                        modifier = Modifier.clickable {
                                            subjectName = sub.name
                                        }
                                    ) {
                                        Text(
                                            text = sub.name,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (isSel) FocusPrimary else FocusTextSecondary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 2. Study Goal / Specific Topic Input
                        Text(
                            text = "TARGET GOAL / CHAPTER (OPTIONAL)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = FocusTextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = sessionName,
                            onValueChange = { sessionName = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("schedule_session_input"),
                            placeholder = {
                                Text(
                                    "e.g. Chapter 4 Numericals, Solve 30 MCQs, Revise notes...",
                                    color = FocusTextSecondary.copy(alpha = 0.45f),
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.TrackChanges,
                                    contentDescription = null,
                                    tint = FocusWarning,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (sessionName.isNotBlank()) {
                                    IconButton(onClick = { sessionName = "" }) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Clear Goal",
                                            tint = FocusTextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FocusWarning,
                                unfocusedBorderColor = FocusOutline,
                                focusedContainerColor = FocusBackground,
                                unfocusedContainerColor = FocusBackground,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = false,
                            minLines = 1,
                            maxLines = 5
                        )
                    }
                }
            }

            // ==========================================
            // SECTION 4: 🔒 SECURITY & LOCK MODE
            // ==========================================
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = FocusSurface),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, FocusOutline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = FocusWarning,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "LOCKDOWN LEVEL",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Mode Selector Cards
                        data class ModeOption(val id: String, val mode: LockMode, val title: String, val desc: String)
                        listOf(
                            ModeOption("DEEP_WORK", LockMode.MAXIMUM_LOCK, "Deep Work Mode", "Kiosk lockdown using Strict Schedule apps."),
                            ModeOption("SPECIAL", LockMode.MAXIMUM_LOCK, "Special Whitelist Mode", "Kiosk lockdown using custom selected apps."),
                            ModeOption("MINDFUL", LockMode.SOFT_LOCK, "Mindful Mode", "Gentle alert banner when opening distracted apps.")
                        ).forEach { option ->
                            val isSelected = selectedModeId == option.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) FocusWarning.copy(alpha = 0.12f) else FocusBackground,
                                border = BorderStroke(1.dp, if (isSelected) FocusWarning else FocusOutline),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { 
                                        selectedModeId = option.id
                                        selectedLockMode = option.mode
                                        if (option.id == "DEEP_WORK" || option.id == "MINDFUL") {
                                            selectedWhitelistProfile = "STRICT"
                                        } else if (option.id == "SPECIAL") {
                                            selectedWhitelistProfile = ""
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { 
                                            selectedModeId = option.id
                                            selectedLockMode = option.mode
                                            if (option.id == "DEEP_WORK" || option.id == "MINDFUL") {
                                                selectedWhitelistProfile = "STRICT"
                                            } else if (option.id == "SPECIAL") {
                                                selectedWhitelistProfile = ""
                                            }
                                        },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = FocusWarning,
                                            unselectedColor = FocusTextSecondary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = option.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) Color.White else FocusTextSecondary
                                        )
                                        Text(
                                            text = option.desc,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = FocusTextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        if (selectedModeId == "SPECIAL") {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "APP BLOCKING SYSTEM",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                                color = FocusTextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val listOptions = listOf(
                                    Pair("MANUAL", "Manual\nFocus"),
                                    Pair("STRICT", "Strict\nSchedule"),
                                    Pair("SPECIAL", "Special\nWhitelist")
                                )
                                listOptions.forEach { (profileId, label) ->
                                    val isSelected = selectedWhitelistProfile == profileId
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = FocusBackground,
                                        border = BorderStroke(1.dp, if (isSelected) FocusPrimary.copy(alpha = 0.5f) else FocusOutline.copy(alpha = 0.3f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedWhitelistProfile = profileId }
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 16.dp, horizontal = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.sp,
                                                    lineHeight = 16.sp,
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                ),
                                                color = if (isSelected) FocusPrimary else Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Allowed Apps Whitelist Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = FocusSurfaceVariant,
                            border = BorderStroke(1.dp, FocusOutline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setAppSelectorProfile(selectedWhitelistProfile)
                                    onNavigateToAppSelector()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Apps,
                                        contentDescription = null,
                                        tint = FocusPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        val currentAppsTitle = when (selectedWhitelistProfile) {
                                            "SPECIAL" -> "Special Whitelist Apps"
                                            "MANUAL" -> "Quick Focus Apps"
                                            "STRICT" -> "Strict Schedule Apps"
                                            else -> "Select App Blocking System"
                                        }
                                        Text(
                                            text = currentAppsTitle,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        if (selectedWhitelistProfile.isNotBlank()) {
                                            val currentAppsCount = when (selectedWhitelistProfile) {
                                                "SPECIAL" -> whitelistedAppsSpecial.size
                                                "MANUAL" -> whitelistedAppsManual.size
                                                else -> whitelistedAppsStrict.size
                                            }
                                            Text(
                                                text = "${currentAppsCount} apps allowed during schedule",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = FocusTextSecondary
                                            )
                                        } else {
                                            Text(
                                                text = "Required for Special Whitelist Mode",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = FocusWarning
                                            )
                                        }
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Select",
                                    tint = FocusTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // SECTION 5: 📸 CAMERA & ACCOUNTABILITY
            // ==========================================
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = FocusSurface),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, FocusOutline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = FocusAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "STRICT VERIFICATION (PROOF OF STUDY)",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Desk Photo Verification Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "📸 Study Desk Snapshot",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Requires photo of books/desk before timer unlocks",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FocusTextSecondary
                                )
                            }
                            Switch(
                                checked = requiresPhoto,
                                onCheckedChange = { requiresPhoto = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = FocusAccent,
                                    uncheckedTrackColor = FocusSurfaceVariant
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Completion Selfie Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🤳 Completion Proof Selfie",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Take proof selfie after session finishes to log streak",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FocusTextSecondary
                                )
                            }
                            Switch(
                                checked = requiresSelfie,
                                onCheckedChange = { requiresSelfie = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = FocusAccent,
                                    uncheckedTrackColor = FocusSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }

            // ==========================================
            // SECTION 6: 🎵 BINAURAL AUDIO
            // ==========================================
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = FocusSurface),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, FocusOutline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = null,
                                tint = FocusPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AMBIENT FOCUS SOUND",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SoundType.entries.forEach { sound ->
                                val isSelected = selectedSound == sound
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) FocusPrimary.copy(alpha = 0.1f) else FocusBackground,
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) FocusPrimary else FocusSurfaceVariant
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedSound = sound }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .background(
                                                    if (isSelected) FocusPrimary.copy(alpha = 0.2f) else FocusSurfaceVariant.copy(alpha = 0.4f),
                                                    androidx.compose.foundation.shape.CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (sound.isBinaural) Icons.Default.Headphones else Icons.Default.MusicNote,
                                                contentDescription = null,
                                                tint = if (isSelected) FocusPrimary else FocusTextSecondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = sound.label,
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = if (isSelected) FocusPrimary else Color.White
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                if (sound.name != "NONE") {
                                                    Text(
                                                        text = sound.badge,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                        color = if (isSelected) FocusPrimary else FocusTextSecondary,
                                                        modifier = Modifier
                                                            .background(
                                                                if (isSelected) FocusPrimary.copy(alpha = 0.15f) else FocusSurfaceVariant,
                                                                RoundedCornerShape(4.dp)
                                                            )
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = sound.hindiTitle,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                                color = FocusWarning
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = sound.description,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 14.sp),
                                                color = FocusTextSecondary,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { selectedSound = sound },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = FocusPrimary,
                                                unselectedColor = FocusTextSecondary
                                            ),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }

        @OptIn(ExperimentalMaterial3Api::class)
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedCalendar.timeInMillis = millis
                            selectedCalendar.set(java.util.Calendar.HOUR_OF_DAY, startHour)
                            selectedCalendar.set(java.util.Calendar.MINUTE, startMinute)
                            selectedPreset = DatePresetType.CUSTOM
                        }
                        showDatePicker = false
                    }) {
                        Text("OK", color = FocusPrimary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel", color = FocusTextSecondary)
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        @OptIn(ExperimentalMaterial3Api::class)
        if (showStartTimePicker) {
            AlertDialog(
                onDismissRequest = { showStartTimePicker = false },
                title = { Text("Select Start Time") },
                text = { TimePicker(state = startTimePickerState) },
                confirmButton = {
                    TextButton(onClick = {
                        startHour = startTimePickerState.hour
                        startMinute = startTimePickerState.minute
                        selectedCalendar.set(java.util.Calendar.HOUR_OF_DAY, startHour)
                        selectedCalendar.set(java.util.Calendar.MINUTE, startMinute)
                        showStartTimePicker = false
                    }) {
                        Text("OK", color = FocusPrimary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showStartTimePicker = false }) {
                        Text("Cancel", color = FocusTextSecondary)
                    }
                }
            )
        }

        @OptIn(ExperimentalMaterial3Api::class)
        if (showEndTimePicker) {
            AlertDialog(
                onDismissRequest = { showEndTimePicker = false },
                title = { Text("Select End Time") },
                text = { TimePicker(state = endTimePickerState) },
                confirmButton = {
                    TextButton(onClick = {
                        endHour = endTimePickerState.hour
                        endMinute = endTimePickerState.minute
                        showEndTimePicker = false
                    }) {
                        Text("OK", color = FocusPrimary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEndTimePicker = false }) {
                        Text("Cancel", color = FocusTextSecondary)
                    }
                }
            )
        }

        @OptIn(ExperimentalMaterial3Api::class)
        if (showTwoStepTimeDialog) {
            TwoStepTimeAndDurationDialog(
                initialHour = startHour,
                initialMinute = startMinute,
                onDismiss = { showTwoStepTimeDialog = false },
                onTimeAndDurationSelected = { h, m, durationMins ->
                    startHour = h
                    startMinute = m
                    selectedCalendar.set(java.util.Calendar.HOUR_OF_DAY, startHour)
                    selectedCalendar.set(java.util.Calendar.MINUTE, startMinute)

                    val endCal = java.util.Calendar.getInstance().apply {
                        set(java.util.Calendar.HOUR_OF_DAY, startHour)
                        set(java.util.Calendar.MINUTE, startMinute)
                        add(java.util.Calendar.MINUTE, durationMins)
                    }
                    endHour = endCal.get(java.util.Calendar.HOUR_OF_DAY)
                    endMinute = endCal.get(java.util.Calendar.MINUTE)

                    showTwoStepTimeDialog = false
                }
            )
        }

        if (showValidationDialog) {
            val userStart = selectedCalendar.timeInMillis
            val userEnd = userStart + (calculatedDurationMinutes * 60 * 1000L)
            ScheduleValidationDialog(
                conflicts = validationConflicts,
                userStart = userStart,
                userEnd = userEnd,
                nextSession = nextValidationSession,
                onChangeTime = { showValidationDialog = false },
                onSave = { 
                    showValidationDialog = false
                    showReminderDialog = true
                },
                onCancel = { showValidationDialog = false }
            )
        }

        if (showReminderDialog) {
            SetReminderDialog(
                onDismiss = { showReminderDialog = false },
                onSaveReminders = { selectedReminderMinutes ->
                    val finalSubject = if (subjectName.isNotBlank()) subjectName.trim() else "Study Session"
                    val finalSession = if (sessionName.isNotBlank()) sessionName.trim() else finalSubject

                    if (subjectName.isNotBlank() && userSubjects.none { it.name.equals(subjectName.trim(), ignoreCase = true) }) {
                        viewModel.addCustomSubject(subjectName.trim(), "#0284C7")
                    }

                    viewModel.updateSetup(
                        sessionName = finalSession,
                        subjectName = finalSubject,
                        durationMinutes = calculatedDurationMinutes,
                        lockMode = selectedLockMode,
                        soundType = selectedSound,
                        requiresPhoto = requiresPhoto,
                        requiresSelfie = requiresSelfie,
                        whitelistProfile = selectedWhitelistProfile
                    )

                    viewModel.scheduleFocusSession(
                        hour = startHour,
                        minute = startMinute,
                        targetYear = selectedCalendar.get(Calendar.YEAR),
                        targetMonth = selectedCalendar.get(Calendar.MONTH),
                        targetDayOfMonth = selectedCalendar.get(Calendar.DAY_OF_MONTH),
                        reminderMinutesList = selectedReminderMinutes
                    )

                    showReminderDialog = false
                    onScheduleCreated()
                }
            )
        }
    }
}

@Composable
fun SetReminderDialog(
    onDismiss: () -> Unit,
    onSaveReminders: (List<Int>) -> Unit
) {
    val options = remember {
        listOf(
            1 to "1 minutes before",
            15 to "15 minutes before",
            30 to "30 minutes before",
            90 to "1.5 hour before",
            120 to "2 hour before"
        )
    }

    var selectedOffsets by remember { mutableStateOf(setOf(15, 30)) }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Set Reminder",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "How long in advance do you want to be notified? (Select all that apply)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = Color(0xFF475569)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    options.forEach { (minutes, label) ->
                        val isSelected = selectedOffsets.contains(minutes)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) Color(0xFFEFF6FF) else Color.White,
                            border = BorderStroke(
                                1.5.dp,
                                if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedOffsets = if (isSelected) {
                                        selectedOffsets - minutes
                                    } else {
                                        selectedOffsets + minutes
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, Color(0xFF94A3B8), CircleShape)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        fontSize = 15.sp
                                    ),
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE2E8F0),
                            contentColor = Color(0xFF475569)
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        )
                    }

                    // Save button
                    Button(
                        onClick = {
                            val selectedList = selectedOffsets.toList().sorted()
                            onSaveReminders(if (selectedList.isEmpty()) listOf(15) else selectedList)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Save",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VerticalListPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(170.dp)
            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.lazy.LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items.size) { index ->
                val item = items[index]
                val isSelected = index == selectedIndex
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Color(0xFFE0E7FF) else Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(vertical = 4.dp)
                        .clickable { onSelect(index) }
                ) {
                    Text(
                        text = item,
                        style = if (isSelected) {
                            MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF3730A3)
                            )
                        } else {
                            MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoStepTimeAndDurationDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onTimeAndDurationSelected: (hour: Int, minute: Int, durationMins: Int) -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1 = Select Time, 2 = Select Duration, 3 = Success Animation
    
    var hour12 by remember { mutableStateOf(if (initialHour % 12 == 0) 12 else initialHour % 12) }
    var minuteVal by remember { mutableStateOf(initialMinute) }
    var isAm by remember { mutableStateOf(initialHour < 12) }

    var selectedDuration by remember { mutableStateOf(45) } // default 45 mins as in video

    val durationOptions = remember {
        listOf(
            10 to "10 मिनट",
            25 to "25 मिनट",
            45 to "45 मिनट",
            90 to "1.5 Hours",
            150 to "2.5 Hours",
            210 to "3.5 Hours",
            240 to "4 Hours",
            300 to "5 Hours"
        )
    }

    val hoursList = remember { (1..12).map { String.format("%02d", it) } }
    val minutesList = remember { (0..59).map { String.format("%02d", it) } }
    val amPmList = remember { listOf("AM", "PM") }

    LaunchedEffect(step) {
        if (step == 3) {
            kotlinx.coroutines.delay(700)
            val h24 = if (isAm) {
                if (hour12 == 12) 0 else hour12
            } else {
                if (hour12 == 12) 12 else hour12 + 12
            }
            onTimeAndDurationSelected(h24, minuteVal, selectedDuration)
        }
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (step) {
                    1 -> {
                        // STEP 1: SELECT TIME (Video Style Header & Time Picker)
                        Text(
                            text = "Select Time",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp
                            ),
                            color = Color(0xFF0F172A)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Highlighted selected time display
                        val formattedHour = String.format("%02d", hour12)
                        val formattedMin = String.format("%02d", minuteVal)
                        val amPmStr = if (isAm) "AM" else "PM"
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = "$formattedHour : $formattedMin $amPmStr",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0F172A),
                                    letterSpacing = 2.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            VerticalListPicker(
                                items = hoursList,
                                selectedIndex = hour12 - 1,
                                onSelect = { hour12 = it + 1 },
                                modifier = Modifier.weight(1f)
                            )
                            VerticalListPicker(
                                items = minutesList,
                                selectedIndex = minuteVal,
                                onSelect = { minuteVal = it },
                                modifier = Modifier.weight(1f)
                            )
                            VerticalListPicker(
                                items = amPmList,
                                selectedIndex = if (isAm) 0 else 1,
                                onSelect = { isAm = (it == 0) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = onDismiss) {
                                Text(
                                    text = "CANCEL",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF64748B)
                                )
                            }

                            Button(
                                onClick = { step = 2 },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2563EB),
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = "DONE",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }
                        }
                    }

                    2 -> {
                        // STEP 2: कितने वक़्त के लिए? (Select Duration - Grid Selection)
                        Text(
                            text = "Select Duration",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "कितने वक़्त के लिए?",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp
                            ),
                            color = Color(0xFF0F172A)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // 3x2 Grid layout for duration options as seen in video
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            durationOptions.chunked(3).forEach { rowItems ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    rowItems.forEach { (mins, label) ->
                                        val isSelected = selectedDuration == mins
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (isSelected) Color(0xFF2563EB) else Color(0xFFF1F5F9),
                                            border = BorderStroke(
                                                1.dp,
                                                if (isSelected) Color(0xFF1D4ED8) else Color(0xFFCBD5E1)
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .clickable { selectedDuration = mins }
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                        fontSize = 14.sp
                                                    ),
                                                    color = if (isSelected) Color.White else Color(0xFF1E293B)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        Button(
                            onClick = { step = 3 },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2563EB),
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Submit",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            )
                        }
                    }

                    3 -> {
                        // STEP 3: SUCCESS CHECKMARK ANIMATION (as in video frame 00:09)
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCFCE7))
                                .border(2.dp, Color(0xFF22C55E), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Success",
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Time & Duration Locked!",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
