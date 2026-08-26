import re

with open("app/src/main/java/com/example/ui/screens/StatisticsScreen.kt", "r") as f:
    content = f.read()

# 1. Total time summary (Blue)
content = content.replace("""                                Text("Total time", color = StatBlue, style = MaterialTheme.typography.bodyMedium)
                                Text("13:48:36", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light))
                                Text("(Allowed Apps 0:11:33)", color = FocusTextSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Daily average", color = StatBlue, style = MaterialTheme.typography.bodyMedium)
                                Text("0:55:14", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light))""",
"""                                Text("Total time", color = StatBlue, style = MaterialTheme.typography.bodyMedium)
                                Text("0:00:00", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Daily average", color = StatBlue, style = MaterialTheme.typography.bodyMedium)
                                Text("0:00:00", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light))""")

# 2. Total time summary (Green)
content = content.replace("""                                    Text("Total time", color = StatGreen, style = MaterialTheme.typography.bodyMedium)
                                    Text("17:30:36", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light))
                                    Text("(Allowed Apps 0:11:33)", color = FocusTextSecondary, style = MaterialTheme.typography.bodySmall)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Daily average", color = StatGreen, style = MaterialTheme.typography.bodyMedium)
                                    Text("0:43:46", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light))""",
"""                                    Text("Total time", color = StatGreen, style = MaterialTheme.typography.bodyMedium)
                                    Text("0:00:00", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Daily average", color = StatGreen, style = MaterialTheme.typography.bodyMedium)
                                    Text("0:00:00", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light))""")

# 3. Stacked bar chart legend & subtitle
content = content.replace("""Text("Daily max: 2h 6m", color = FocusTextSecondary, style = MaterialTheme.typography.bodySmall)""",
                          """Text("Daily max: 0h 0m", color = FocusTextSecondary, style = MaterialTheme.typography.bodySmall)""")

content = content.replace("""                        // Legend
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            LegendItem(color = Color(0xFF2563EB), text = "RAS Self")
                            LegendItem(color = Color(0xFF0EA5E9), text = "Advance RAS")
                            LegendItem(color = Color(0xFFDC2626), text = "PYQS Test")
                        }
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                            LegendItem(color = Color(0xFF65A30D), text = "REVISION")
                            LegendItem(color = Color(0xFFEAB308), text = "Value Addit...")
                            LegendItem(color = Color(0xFF22C55E), text = "MOCK Test")
                        }""",
"""                        // Legend (Empty)
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
                            Text("No subject data available", color = FocusTextSecondary, style = MaterialTheme.typography.bodySmall)
                        }""")

with open("app/src/main/java/com/example/ui/screens/StatisticsScreen.kt", "w") as f:
    f.write(content)
