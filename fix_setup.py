import sys

with open("app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt", "r") as f:
    lines = f.readlines()

new_lines = []
for idx, line in enumerate(lines):
    if "// Section 2: Allowed Apps Whitelist" in line:
        new_lines.append("                            }\n")
        new_lines.append("                        }\n")
    new_lines.append(line)

with open("app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt", "w") as f:
    f.writelines(new_lines)
