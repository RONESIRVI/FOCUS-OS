import re

with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "r") as f:
    content = f.read()

content = content.replace(
    'emergencyPenaltyCountdown = if (timerState.lockMode == LockMode.MAXIMUM_LOCK) 200 else 0',
    'emergencyPenaltyCountdown = if (timerState.lockMode == LockMode.MAXIMUM_LOCK) 300 else 0'
)

with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "w") as f:
    f.write(content)
