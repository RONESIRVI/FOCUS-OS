import sys

with open("app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt", "r") as f:
    lines = f.readlines()

new_lines = []
skip = False
for line in lines:
    if "Row(" in line and "verticalAlignment = Alignment.CenterVertically" in line and skip == False:
        # Wait, the previous patch also had Row(verticalAlignment...)
        pass

# Actually, let's just delete the block manually by line index.
# Line 680 to 800 roughly. Let's just use Python's index based on strings.

with open("app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt", "r") as f:
    content = f.read()

start_str = """                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {"""

# We need to find this block and delete up to "                            }
#                        }"

idx1 = content.find(start_str)
idx2 = content.find("                        // Section 2: Allowed Apps Whitelist")

if idx1 != -1 and idx2 != -1:
    content = content[:idx1] + content[idx2:]
    with open("app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt", "w") as f:
        f.write(content)
    print("Success")
else:
    print("Failed")
