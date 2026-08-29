import sys

with open("app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt", "r") as f:
    lines = f.readlines()

start_idx = -1
end_idx = -1

for i, line in enumerate(lines):
    if "// Mode Selector Cards" in line:
        start_idx = i
    if "Spacer(modifier = Modifier.height(12.dp))" in line and "Allowed Apps Whitelist Button" in lines[i+2]:
        end_idx = i
        break

if start_idx != -1 and end_idx != -1:
    replacement = """                        // Mode Selector Cards
                        data class ModeOption(val mode: LockMode, val profile: String, val title: String, val desc: String)
                        listOf(
                            ModeOption(LockMode.MAXIMUM_LOCK, "STRICT", "Deep Work Mode", "Kiosk lockdown using Strict Schedule apps."),
                            ModeOption(LockMode.MAXIMUM_LOCK, "SPECIAL", "Special Whitelist Mode", "Kiosk lockdown using Special Whitelist apps."),
                            ModeOption(LockMode.SOFT_LOCK, "STRICT", "Mindful Mode", "Gentle alert banner when opening distracted apps.")
                        ).forEach { option ->
                            val isSelected = selectedLockMode == option.mode && selectedWhitelistProfile == option.profile
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) FocusWarning.copy(alpha = 0.12f) else FocusBackground,
                                border = BorderStroke(1.dp, if (isSelected) FocusWarning else FocusOutline),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { 
                                        selectedLockMode = option.mode
                                        selectedWhitelistProfile = option.profile
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { 
                                            selectedLockMode = option.mode
                                            selectedWhitelistProfile = option.profile
                                        },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = FocusWarning,
                                            unselectedColor = FocusTextSecondary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = option.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) Color.White else FocusTextSecondary
                                        )
                                        Text(
                                            text = option.desc,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = FocusTextSecondary
                                        )
                                    }
                                }
                            }
                        }
"""
    new_lines = lines[:start_idx] + [replacement] + lines[end_idx:]
    with open("app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt", "w") as f:
        f.writelines(new_lines)
    print("Replaced successfully")
else:
    print(f"Indices not found: start={start_idx}, end={end_idx}")
