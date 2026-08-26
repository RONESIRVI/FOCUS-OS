import re

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

# I will recreate the items from scratch and place them in the correct order.

lock_mode_code = """
        // Lock & Security Status Mode Selector
        item {
            val setup by viewModel.setupState.collectAsState()
            SettingsSectionTitle("LOCK MODE SHIELD")
            SettingsCard {
                val selectableModes = LockMode.entries.filter { it != LockMode.NORMAL }
                selectableModes.forEachIndexed { index, mode ->
                    val isSelected = setup.lockMode == mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.updateSetup(lockMode = mode) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.updateSetup(lockMode = mode) },
                                colors = RadioButtonDefaults.colors(selectedColor = FocusPrimary, unselectedColor = FocusTextSecondary)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(text = mode.title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = if (isSelected) FocusPrimary else FocusTextPrimary)
                                Text(text = mode.description, style = MaterialTheme.typography.bodySmall, color = FocusTextSecondary)
                            }
                        }
                    }
                    if (index < selectableModes.size - 1) {
                        Divider(color = FocusSurfaceVariant)
                    }
                }
            }
        }
"""

# Let's remove the broken parts in the file
# 1. Remove whatever is left between "Strict Focus Rules" and "Appearance"

pattern_to_remove = r'(// Strict Focus Rules.*?\}\s*\}\s*\})[\s\S]*?(// Appearance)'
match = re.search(pattern_to_remove, content)
if match:
    # replace the middle part with nothing
    content = content[:match.end(1)] + "\n\n        " + match.group(2) + content[match.end(2):]

# 2. Insert `lock_mode_code` between `App Blocking System` and `Section 1: Core App-Blocking`

app_blocking_system_end = r'(// App Blocking System.*?\}\s*\}\s*\})'
match2 = re.search(app_blocking_system_end, content, flags=re.DOTALL)
if match2:
    start_part = content[:match2.end(1)]
    end_part = content[match2.end(1):]
    content = start_part + "\n" + lock_mode_code + end_part

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)

