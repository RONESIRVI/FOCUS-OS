import re

with open("app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt", "r") as f:
    content = f.read()

# Add state variables
state_vars = """    val whitelistedAppsManual by viewModel.whitelistedAppsManual.collectAsState()
    val scheduledSessions by viewModel.scheduledSessions.collectAsState(initial = emptyList())
    var showValidationDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var validationConflicts by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<List<com.example.data.model.FocusSession>>(emptyList()) }
    var nextValidationSession by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.example.data.model.FocusSession?>(null) }"""
content = content.replace("    val whitelistedAppsManual by viewModel.whitelistedAppsManual.collectAsState()", state_vars, 1)

# Modify button onClick
old_onClick = """                        onClick = {
                            if (selectedModeId == "SPECIAL" && selectedWhitelistProfile.isBlank()) {
                                android.widget.Toast.makeText(context, "Please select an App Blocking System", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                showReminderDialog = true
                            }
                        },"""

new_onClick = """                        onClick = {
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
                        },"""
content = content.replace(old_onClick, new_onClick)

# Add dialog block
old_dialog = """        if (showReminderDialog) {"""

new_dialog = """        if (showValidationDialog) {
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

        if (showReminderDialog) {"""
content = content.replace(old_dialog, new_dialog)

with open("app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt", "w") as f:
    f.write(content)
print("Patch applied.")
