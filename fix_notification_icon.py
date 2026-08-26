import re

with open("app/src/main/java/com/example/receivers/FocusScheduleReceiver.kt", "r") as f:
    content = f.read()

content = content.replace("android.R.drawable.ic_dialog_info", "R.mipmap.ic_launcher")
content = content.replace("android.R.drawable.ic_dialog_alert", "R.mipmap.ic_launcher")

with open("app/src/main/java/com/example/receivers/FocusScheduleReceiver.kt", "w") as f:
    f.write(content)
