import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# Add states
state_vars = """    var showQuickDurationDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var showValidationDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var validationConflicts by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<List<com.example.data.model.FocusSession>>(emptyList()) }
    var nextValidationSession by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.example.data.model.FocusSession?>(null) }
    var pendingDuration by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }"""
content = content.replace("    var showQuickDurationDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }", state_vars, 1)

# Modify onSubmit in QuickDurationDialog
old_onSubmit = """        QuickDurationDialog(
            onDismissRequest = { showQuickDurationDialog = false },
            onSubmit = { duration ->
                showQuickDurationDialog = false
                viewModel.startSpecialSession(duration, selectedSpecialWhitelist)
                onNavigateToTimer()
            }
        )"""

new_onSubmit = """        QuickDurationDialog(
            onDismissRequest = { showQuickDurationDialog = false },
            onSubmit = { duration ->
                showQuickDurationDialog = false
                val userStart = System.currentTimeMillis()
                val userEnd = userStart + (duration * 60 * 1000L)
                val conflicts = scheduledSessions.filter { it.status == "SCHEDULED" }.filter { s ->
                    val sStart = s.scheduledStartTime ?: return@filter false
                    val sEnd = s.scheduledEndTime ?: return@filter false
                    userStart < sEnd && userEnd > sStart
                }
                
                if (conflicts.isNotEmpty()) {
                    validationConflicts = conflicts
                    nextValidationSession = scheduledSessions.filter { it.status == "SCHEDULED" && (it.scheduledStartTime ?: 0) >= userEnd }.minByOrNull { it.scheduledStartTime ?: 0 }
                    pendingDuration = duration
                    showValidationDialog = true
                } else {
                    viewModel.startSpecialSession(duration, selectedSpecialWhitelist)
                    onNavigateToTimer()
                }
            }
        )
        
    if (showValidationDialog) {
        val userStart = System.currentTimeMillis()
        val userEnd = userStart + (pendingDuration * 60 * 1000L)
        ScheduleValidationDialog(
            saveText = "START SESSION",
            changeText = "CHANGE DURATION",
            conflicts = validationConflicts,
            userStart = userStart,
            userEnd = userEnd,
            nextSession = nextValidationSession,
            onChangeTime = { showValidationDialog = false },
            onSave = { 
                showValidationDialog = false
                viewModel.startSpecialSession(pendingDuration, selectedSpecialWhitelist)
                onNavigateToTimer()
            },
            onCancel = { showValidationDialog = false }
        )
    }"""
content = content.replace(old_onSubmit, new_onSubmit)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
print("HomeScreen Patched.")
