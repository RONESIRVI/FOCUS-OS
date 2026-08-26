import re

with open("app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt", "r") as f:
    content = f.read()

content = re.sub(r'LockMode\.STRICT_LOCK.*?,\n', '', content)
content = content.replace('LockMode.STRICT_LOCK', 'LockMode.MAXIMUM_LOCK')

with open("app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt", "w") as f:
    f.write(content)
