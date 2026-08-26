import re

with open("app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt", "r") as f:
    content = f.read()

# Removing the App Selector Card from FocusSetupScreen.kt
card_to_remove = """                        Text("WHITELISTED APPS", style = MaterialTheme.typography.labelMedium, color = FocusTextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = FocusBackground),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, FocusSurfaceVariant, RoundedCornerShape(22.dp))
                                .clickable {
                                    viewModel.setAppSelectorProfile("MANUAL")
                                    onNavigateToAppSelector()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(FocusSurfaceVariant, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Apps, contentDescription = null, tint = FocusPrimary)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${whitelistedApps.size} Apps Allowed",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = FocusTextPrimary
                                    )
                                    Text(
                                        text = "Tap to modify list",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = FocusTextSecondary
                                    )
                                }
                                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = FocusTextSecondary)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))"""

if card_to_remove in content:
    content = content.replace(card_to_remove, "")
else:
    print("Could not find the card to remove in FocusSetupScreen.kt")

with open("app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt", "w") as f:
    f.write(content)
