with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

# 1. Extract the whole block
start_str = "        // Lock & Security Status Mode Selector\n"
end_str = """                }
            }
        }\n"""

start_idx = content.find(start_str)
if start_idx == -1:
    print("Could not find start")
    exit(1)

# Find the end of the item block.
# Since end_str might match multiple times, let's find the first one AFTER start_idx
end_idx = content.find(end_str, start_idx) + len(end_str)

block_to_move = content[start_idx:end_idx]

# 2. Remove it from its current position
content = content[:start_idx] + content[end_idx:]

# 3. Find insertion point
insert_target = """                )
            }
        }

        // Section 1: 🔴 Most Important (Core App-Blocking)"""

insert_idx = content.find(insert_target)
if insert_idx == -1:
    print("Could not find insertion point")
    exit(1)

# insert the block before the target
# wait, the target starts with `                )\n            }\n        }\n\n        // Section 1: ...`
# The block should be inserted BEFORE `        // Section 1: ...`
# Let's adjust target
target_split = """        // Section 1: 🔴 Most Important (Core App-Blocking)"""
split_idx = content.find(target_split)

if split_idx != -1:
    content = content[:split_idx] + block_to_move + "\n" + content[split_idx:]
else:
    print("split_idx not found")

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
