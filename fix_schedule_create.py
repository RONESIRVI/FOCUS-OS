import sys

with open("app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt", "r") as f:
    content = f.read()

content = content.replace("FocusBackground", "FocusSlateBg")

with open("app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt", "w") as f:
    f.write(content)
