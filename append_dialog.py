import re

with open("app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt", "r") as f:
    content = f.read()

dialog_code = """
    if (showValidationDialog) {
        val userStart = System.currentTimeMillis()
        val userEnd = userStart + (customDuration.toInt() * 60 * 1000L)
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
                val finalSubject = if (customSubject.isNotBlank()) customSubject else "Focus Session"
                val finalGoal = if (customGoal.isNotBlank()) customGoal else "General Study"
                if (customSubject.isNotBlank() && subjects.none { it.name.equals(customSubject.trim(), ignoreCase = true) }) {
                    viewModel.addCustomSubject(customSubject.trim(), "#0284C7")
                }
                viewModel.updateSetup(
                    sessionName = finalGoal,
                    subjectName = finalSubject,
                    durationMinutes = customDuration.toInt()
                )
                onStartSession()
            },
            onCancel = { showValidationDialog = false }
        )
    }
}"""

# find the last closing brace
last_brace_idx = content.rfind("}")
if last_brace_idx != -1:
    content = content[:last_brace_idx] + dialog_code + content[last_brace_idx+1:]

with open("app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt", "w") as f:
    f.write(content)
print("FocusSetupScreen patched.")
