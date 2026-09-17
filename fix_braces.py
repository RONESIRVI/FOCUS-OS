import os

file_path = "app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt"
with open(file_path, "r") as f:
    lines = f.readlines()

new_lines = []
expected_indent = 0

for i, line in enumerate(lines):
    if line.strip() == "":
        new_lines.append(line)
        continue
        
    actual_indent = len(line) - len(line.lstrip())
    
    # If the current line's indentation is less than the expected indent,
    # it means one or more blocks were closed.
    while expected_indent > actual_indent:
        expected_indent -= 4
        new_lines.append(" " * expected_indent + "}\n")
        
    new_lines.append(line)
    
    # Calculate the expected indent for the next line
    if line.rstrip().endswith("{") or line.rstrip().endswith("("):
        expected_indent += 4
    elif line.strip().startswith(")") and expected_indent >= actual_indent:
        pass # this is just a closing parenthesis

while expected_indent > 0:
    expected_indent -= 4
    new_lines.append(" " * expected_indent + "}\n")

with open(file_path + ".fixed", "w") as f:
    f.writelines(new_lines)

print("Done")
