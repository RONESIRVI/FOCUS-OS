import sys

with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "r") as f:
    lines = f.readlines()

new_lines = []
for idx, line in enumerate(lines):
    if idx >= 574 and idx <= 588:
        continue
    new_lines.append(line)

with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "w") as f:
    f.writelines(new_lines)
