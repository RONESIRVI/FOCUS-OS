import re

with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "r") as f:
    content = f.read()

old_dialog = """        if (showEmergencyConfirm) {
            Dialog(
                onDismissRequest = { showEmergencyConfirm = false }
            ) {"""

new_dialog = """        if (showEmergencyConfirm) {
            Dialog(
                onDismissRequest = {
                    // Cannot dismiss in Maximum Lock until penalty is over
                    if (timerState.lockMode != LockMode.MAXIMUM_LOCK) {
                        showEmergencyConfirm = false
                    }
                },
                properties = androidx.compose.ui.window.DialogProperties(
                    dismissOnBackPress = timerState.lockMode != LockMode.MAXIMUM_LOCK,
                    dismissOnClickOutside = timerState.lockMode != LockMode.MAXIMUM_LOCK
                )
            ) {"""

content = content.replace(old_dialog, new_dialog)

with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "w") as f:
    f.write(content)
