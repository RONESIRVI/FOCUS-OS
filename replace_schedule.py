import sys

with open("app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt", "r") as f:
    lines = f.readlines()

new_content = """                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = FocusAccentOrange,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Schedule Time",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(14.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        androidx.compose.material3.Switch(
                                            checked = isScheduled,
                                            onCheckedChange = { isScheduled = it },
                                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = FocusAccentOrange,
                                                uncheckedThumbColor = FocusTextSecondary,
                                                uncheckedTrackColor = FocusSurfaceVariant
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(if (isScheduled) "Scheduled" else "Start Now", color = Color.White)
                                    }
                                }
                                
                                if (isScheduled) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(FocusSurfaceVariant, RoundedCornerShape(12.dp))
                                                .clickable {
                                                    android.app.TimePickerDialog(
                                                        context,
                                                        { _, hour, min -> 
                                                            scheduleHour = hour
                                                            scheduleMinute = min
                                                        },
                                                        scheduleHour,
                                                        scheduleMinute,
                                                        false
                                                    ).show()
                                                }
                                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                        ) {
                                            Column {
                                                Text("Start Time", style = MaterialTheme.typography.labelSmall, color = FocusTextSecondary)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                val amPm = if (scheduleHour >= 12) "PM" else "AM"
                                                val h = if (scheduleHour % 12 == 0) 12 else scheduleHour % 12
                                                val m = String.format("%02d", scheduleMinute)
                                                Text(
                                                    text = "$h:$m $amPm",
                                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                    color = FocusCyan
                                                )
                                            }
                                        }
                                        
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(FocusSurfaceVariant, RoundedCornerShape(12.dp))
                                                .clickable {
                                                    // Optional: we can allow editing end time which would update duration
                                                    // For now, let's keep it derived to avoid complex state jumping, 
                                                    // but we'll show it clearly!
                                                    android.app.TimePickerDialog(
                                                        context,
                                                        { _, hour, min -> 
                                                            // calculate diff in minutes to set duration
                                                            var newTotalMins = (hour * 60 + min) - (scheduleHour * 60 + scheduleMinute)
                                                            if (newTotalMins < 0) {
                                                                newTotalMins += 24 * 60 // crossed midnight
                                                            }
                                                            if (newTotalMins < 5) newTotalMins = 5 // minimum
                                                            customDuration = newTotalMins.toFloat()
                                                            viewModel.updateSetup(durationMinutes = newTotalMins)
                                                        },
                                                        (scheduleHour + ((scheduleMinute + setup.durationMinutes)/60)) % 24,
                                                        (scheduleMinute + setup.durationMinutes) % 60,
                                                        false
                                                    ).show()
                                                }
                                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                        ) {
                                            Column {
                                                Text("End Time", style = MaterialTheme.typography.labelSmall, color = FocusTextSecondary)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                
                                                val endTotalMins = scheduleHour * 60 + scheduleMinute + setup.durationMinutes
                                                val endH24 = (endTotalMins / 60) % 24
                                                val endM = endTotalMins % 60
                                                val endAmPm = if (endH24 >= 12) "PM" else "AM"
                                                val endH12 = if (endH24 % 12 == 0) 12 else endH24 % 12
                                                val endMStr = String.format("%02d", endM)
                                                
                                                Text(
                                                    text = "$endH12:$endMStr $endAmPm",
                                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                    color = FocusAccentOrange
                                                )
                                            }
                                        }
                                    }
                                }
"""

# Replace lines 680 to 746 (inclusive 680 to 745, index 679 to 745)
# Let's find exactly where it is in the file.
start_idx = -1
end_idx = -1
for i, line in enumerate(lines):
    if "Row(verticalAlignment = Alignment.CenterVertically) {" in line and "Icons.Default.Schedule" in lines[i+2]:
        start_idx = i
        break

if start_idx != -1:
    for i in range(start_idx, len(lines)):
        if "// Section 2: Allowed Apps Whitelist" in lines[i]:
            end_idx = i - 1
            break

if start_idx != -1 and end_idx != -1:
    lines[start_idx:end_idx] = [new_content + "\n"]
    with open("app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt", "w") as f:
        f.writelines(lines)
    print("Success")
else:
    print(f"Failed to find indices. Start {start_idx}, End {end_idx}")

