import re

with open("app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt", "r") as f:
    content = f.read()

old_ui = """                                                Spacer(modifier = Modifier.width(8.dp))
                                                if (sound.name != "NONE") {
                                                    Text(
                                                        text = sound.badge,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                        color = if (isSelected) FocusPrimary else FocusTextSecondary,
                                                        modifier = Modifier
                                                            .background(
                                                                if (isSelected) FocusPrimary.copy(alpha = 0.15f) else FocusSurfaceVariant,
                                                                RoundedCornerShape(4.dp)
                                                            )
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = sound.hindiTitle,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                                color = FocusWarning
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = sound.description,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 14.sp),
                                                color = FocusTextSecondary,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )"""
new_ui = """                                            }"""

content = content.replace(old_ui, new_ui)

with open("app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt", "w") as f:
    f.write(content)
