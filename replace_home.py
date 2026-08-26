import sys

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    lines = f.readlines()

new_content = """        // Today's Schedule Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TODAY'S SCHEDULE",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = FocusTextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (scheduledSessions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No sessions scheduled.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = FocusTextSecondary
                            )
                        }
                    } else {
                        scheduledSessions.take(5).forEach { session ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(8.dp).background(FocusAccentOrange, CircleShape))
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                val formatter = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
                                val timeStr = session.scheduledStartTime?.let { formatter.format(java.util.Date(it)) } ?: "Upcoming"
                                
                                Text(
                                    text = timeStr,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.width(80.dp)
                                )
                                
                                Text(
                                    text = session.subjectName.uppercase(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = { onNavigateToSetup() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ADD SESSION", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
"""

start_idx = -1
end_idx = -1

for i, line in enumerate(lines):
    if "// Today's Schedule" in line:
        start_idx = i
        break

if start_idx != -1:
    # Find the end of the LazyColumn content which is just before the last 2 closing brackets
    # Wait, the best way is to find the line `    }` that closes the LazyColumn.
    for i in range(start_idx, len(lines)):
        if "    }" in lines[i] and "}" in lines[i+1]:
            end_idx = i
            break

if start_idx != -1 and end_idx != -1:
    lines[start_idx:end_idx] = [new_content + "\n"]
    with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
        f.writelines(lines)
    print("Success")
else:
    print(f"Failed to find indices. Start {start_idx}, End {end_idx}")
