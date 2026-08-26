import re

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

# Extract the block
shield_pattern = r'(\s*// Lock & Security Status Mode Selector\s*item \{\s*val setup by viewModel\.setupState\.collectAsState\(\)\s*SettingsSectionTitle\("LOCK MODE SHIELD"\).*?\}\s*\}\s*\})'
shield_match = re.search(shield_pattern, content, flags=re.DOTALL)

if not shield_match:
    print("Could not find LOCK MODE SHIELD block")
    exit(1)

shield_text = shield_match.group(1)

# Remove the block from original position
content = content.replace(shield_text, '')

# Find the injection point (after App Blocking System)
app_block_pattern = r'(\s*// App Blocking System.*?\}\s*\}\s*\})'
app_block_match = re.search(app_block_pattern, content, flags=re.DOTALL)

if not app_block_match:
    print("Could not find App Blocking System block")
    exit(1)

app_block_text = app_block_match.group(1)

# Replace the app_block_text with app_block_text + shield_text
content = content.replace(app_block_text, app_block_text + shield_text + "\n")

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)

print("Moved successfully")
