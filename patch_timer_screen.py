import re

with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "r") as f:
    content = f.read()

old_effect = """    // Emergency exit penalty timer
    LaunchedEffect(showEmergencyConfirm) {
        if (showEmergencyConfirm) {
            emergencyPenaltyCountdown = 10
            while (emergencyPenaltyCountdown > 0) {
                delay(1000)
                emergencyPenaltyCountdown--
            }
        }
    }"""

new_effect = """    // Emergency exit penalty timer
    LaunchedEffect(showEmergencyConfirm) {
        if (showEmergencyConfirm) {
            emergencyPenaltyCountdown = if (timerState.lockMode == LockMode.MAXIMUM_LOCK) 200 else 0
            while (emergencyPenaltyCountdown > 0) {
                delay(1000)
                emergencyPenaltyCountdown--
            }
        }
    }"""

content = content.replace(old_effect, new_effect)

with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "w") as f:
    f.write(content)
