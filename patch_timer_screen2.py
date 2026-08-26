import re

with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "r") as f:
    content = f.read()

content = content.replace(
    'val currentProfile = if (timerState.lockMode == LockMode.STRICT_LOCK || timerState.lockMode == LockMode.MAXIMUM_LOCK) "STRICT" else "MANUAL"',
    'val currentProfile = if (timerState.lockMode == LockMode.MAXIMUM_LOCK) "STRICT" else "MANUAL"'
)

with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "w") as f:
    f.write(content)
