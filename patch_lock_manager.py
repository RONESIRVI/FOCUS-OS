import re

with open("app/src/main/java/com/example/util/FocusLockManager.kt", "r") as f:
    content = f.read()

# Let's see the current handleBlockedAppOpened
print(content)
