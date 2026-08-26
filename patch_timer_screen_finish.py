import re

with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "r") as f:
    content = f.read()

old_finish = """                // Finish Session Button
                Button(
                    onClick = {
                        viewModel.completeFocusSession()
                        onSessionComplete()
                    },"""

new_finish = """                // Finish Session Button
                Button(
                    onClick = {
                        if (timerState.lockMode == LockMode.MAXIMUM_LOCK && timerState.remainingSeconds > 0) {
                            // Penalty for trying to cheat and finish early in Deep Work Mode
                            viewModel.addPenaltyTime(420) // 7 minutes
                            showExitAttemptDialog = false
                            showEmergencyConfirm = true
                        } else {
                            viewModel.completeFocusSession()
                            onSessionComplete()
                        }
                    },"""

content = content.replace(old_finish, new_finish)

with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "w") as f:
    f.write(content)
