import re

with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "r") as f:
    content = f.read()

old_btn = """                            OutlinedButton(
                                onClick = {
                                    showExitAttemptDialog = false
                                    showEmergencyConfirm = true
                                },"""

new_btn = """                            OutlinedButton(
                                onClick = {
                                    if (timerState.lockMode == LockMode.MAXIMUM_LOCK && timerState.remainingSeconds > 0) {
                                        viewModel.addPenaltyTime(420)
                                    }
                                    showExitAttemptDialog = false
                                    showEmergencyConfirm = true
                                },"""

content = content.replace(old_btn, new_btn)

with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "w") as f:
    f.write(content)
