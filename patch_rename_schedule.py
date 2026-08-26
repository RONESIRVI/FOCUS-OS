import re

with open("app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt", "r") as f:
    content = f.read()

content = content.replace('"Soft Lock"', '"Mindful Mode"')
content = content.replace('"Maximum Lock"', '"Deep Work Mode"')

with open("app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt", "w") as f:
    f.write(content)
