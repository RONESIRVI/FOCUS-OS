import re

with open("app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt", "r") as f:
    content = f.read()

content = content.replace("serviceConnection.addPenaltyTime(seconds)", "timerService?.addPenaltyTime(seconds)")

with open("app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt", "w") as f:
    f.write(content)
