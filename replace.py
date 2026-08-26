import sys

with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "r") as f:
    lines = f.readlines()

new_lines = []
with open("patch.txt", "r") as f:
    new_lines = f.readlines()

lines[433:516] = new_lines

with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "w") as f:
    f.writelines(lines)
