cat << 'INNER_EOF' > replacement.txt
                        // Mode Selector Cards
                        data class ModeOption(val mode: LockMode, val profile: String, val title: String, val desc: String)
                        listOf(
                            ModeOption(LockMode.MAXIMUM_LOCK, "STRICT", "Deep Work Mode", "Kiosk lockdown using Strict Schedule whitelist."),
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
INNER_EOF
