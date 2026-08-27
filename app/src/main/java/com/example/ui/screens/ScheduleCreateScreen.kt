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
    val whitelistedApps by viewModel.whitelistedAppsStrict.collectAsState()
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
                                text = selectedLockMode.title.split(" ").first().uppercase(),
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
                            showReminderDialog = true
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // START TIME CARD
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = FocusBackground,
                                border = BorderStroke(1.dp, FocusPrimary.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        showStartTimePicker = true
                                    }
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
                                        text = "Tap to edit ⏱️",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = FocusTextSecondary
                                    )
                                }
                            }

                            // END TIME CARD
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = FocusBackground,
                                border = BorderStroke(1.dp, FocusWarning.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        showEndTimePicker = true
                                    }
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
                                        text = "Tap to edit 🔔",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = FocusTextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Duration Presets (+30m, +45m, +1h, +2h, +3h)
                        Text(
                            text = "Quick Duration Adders",
                            style = MaterialTheme.typography.labelSmall,
                            color = FocusTextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "30 Min" to 30,
                                "45 Min" to 45,
                                "1 Hour" to 60,
                                "1.5 Hours" to 90,
                                "2 Hours" to 120,
                                "3 Hours" to 180
                            ).forEach { (label, durationMins) ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = FocusSurfaceVariant,
                                    border = BorderStroke(1.dp, FocusOutline),
                                    modifier = Modifier.clickable {
                                        val cal = Calendar.getInstance().apply {
                                            set(Calendar.HOUR_OF_DAY, startHour)
                                            set(Calendar.MINUTE, startMinute)
                                            add(Calendar.MINUTE, durationMins)
                                        }
                                        endHour = cal.get(Calendar.HOUR_OF_DAY)
                                        endMinute = cal.get(Calendar.MINUTE)
                                    }
                                ) {
                                    Text(
                                        text = "+$label",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = FocusPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
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
                            singleLine = true
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
                            singleLine = true
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
                        listOf(
                                                        LockMode.MAXIMUM_LOCK to ("Deep Work Mode" to "Kiosk lockdown mode with penalty timer on emergency quit."),
                            LockMode.SOFT_LOCK to ("Mindful Mode" to "Gentle alert banner when opening distracted apps.")
                        ).forEach { (mode, details) ->
                            val isSelected = selectedLockMode == mode
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) FocusWarning.copy(alpha = 0.12f) else FocusBackground,
                                border = BorderStroke(1.dp, if (isSelected) FocusWarning else FocusOutline),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { selectedLockMode = mode }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedLockMode = mode },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = FocusWarning,
                                            unselectedColor = FocusTextSecondary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = details.first,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) Color.White else FocusTextSecondary
                                        )
                                        Text(
                                            text = details.second,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = FocusTextSecondary
                                        )
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
                                    viewModel.setAppSelectorProfile("STRICT")
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
                                        Text(
                                            text = "Allowed Whitelist Apps",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Text(
                                            text = "${whitelistedApps.size} apps allowed during schedule",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = FocusTextSecondary
                                        )
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

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SoundType.entries.forEach { sound ->
                                val isSelected = selectedSound == sound
                                Card(
                                    modifier = Modifier
                                        .width(220.dp)
                                        .clickable { selectedSound = sound },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) FocusPrimary.copy(alpha = 0.15f) else FocusBackground,
                                    ),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) FocusPrimary else FocusSurfaceVariant
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = sound.badge,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = if (isSelected) FocusPrimary else FocusTextSecondary,
                                                modifier = Modifier
                                                    .background(
                                                        if (isSelected) FocusPrimary.copy(alpha = 0.2f) else FocusSurfaceVariant,
                                                        RoundedCornerShape(6.dp)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                            if (sound.isBinaural) {
                                                Icon(
                                                    imageVector = Icons.Default.Headphones,
                                                    contentDescription = "Headphones Recommended",
                                                    tint = if (isSelected) FocusPrimary else FocusTextSecondary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = sound.label,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) FocusPrimary else Color.White
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = sound.hindiTitle,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                            color = FocusWarning
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = sound.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = FocusTextSecondary,
                                            maxLines = 3
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
                        requiresSelfie = requiresSelfie
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
