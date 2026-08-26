import re

with open("app/src/main/java/com/example/receivers/FocusScheduleReceiver.kt", "r") as f:
    content = f.read()

content = content.replace(".setFullScreenIntent(pendingIntent, true)", "")

with open("app/src/main/java/com/example/receivers/FocusScheduleReceiver.kt", "w") as f:
    f.write(content)
