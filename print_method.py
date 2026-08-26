import re
with open("app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt", "r") as f:
    content = f.read()

idx = content.find("fun deleteScheduledSession(session: FocusSession)")
if idx != -1:
    print(content[idx:idx+2500])
