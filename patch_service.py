import re

with open("app/src/main/java/com/example/services/FocusTimerService.kt", "r") as f:
    content = f.read()

content = content.replace('LockMode.STRICT_LOCK', 'LockMode.MAXIMUM_LOCK')

with open("app/src/main/java/com/example/services/FocusTimerService.kt", "w") as f:
    f.write(content)
