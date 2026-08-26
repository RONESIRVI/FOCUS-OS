import sys

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    lines = f.readlines()

new_lines = []
for idx, line in enumerate(lines):
    if idx >= 596 and idx <= 622:
        continue
    new_lines.append(line)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.writelines(new_lines)

