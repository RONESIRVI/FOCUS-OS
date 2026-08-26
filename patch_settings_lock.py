import re

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

# Add LockMode enum import if needed
if "com.example.data.model.LockMode" not in content:
    content = content.replace("import com.example.ui.theme.*", "import com.example.ui.theme.*\nimport com.example.data.model.LockMode")

new_lock_section = """        // Lock & Security Status
        item {
            val setup by viewModel.setupState.collectAsState()
            SettingsSectionTitle("LOCK SHIELD STATUS")
            SettingsCard {
                LockMode.entries.filter { it != LockMode.NORMAL }.forEachIndexed { index, mode ->
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
                    if (index < LockMode.entries.size - 2) {
                        Divider(color = FocusSurfaceVariant)
                    }
                }
            }
        }

        // Permissions & Security"""

content = content.replace("        // Permissions & Security", new_lock_section)

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
