import sys

with open("app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt", "r") as f:
    lines = f.readlines()

# Let's completely read and modify the file because it's messy.
import re

content = "".join(lines)

# Remove isScheduled variables
content = re.sub(r'var isScheduled by remember.*?\}', '', content, count=1)
content = re.sub(r'var scheduleHour by remember.*?\}', '', content, count=1)
content = re.sub(r'var scheduleMinute by remember.*?\}', '', content, count=1)

# Modify the start button logic
old_start_btn = """                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(64.dp)
                                                .background(
                                                    if (isScheduled) FocusAccentOrange else FocusGreen,
                                                    RoundedCornerShape(18.dp)
                                                )
                                                .clickable {
                                                    if (isScheduled) {
                                                        viewModel.scheduleFocusSession(scheduleHour, scheduleMinute)
                                                        // After scheduling, just go back
                                                        onBack()
                                                    } else {
                                                        viewModel.startFocusSession()
                                                        onStartSession()
                                                    }
                                                }
                                                .testTag("start_session_confirm_btn"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (isScheduled) {
                                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.White)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                }
                                                Text(
                                                    text = if (isScheduled) "SCHEDULE SESSION" else "LAUNCH FOCUS SHIELD",
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.ExtraBold,
                                                        letterSpacing = 1.sp
                                                    ),
                                                    color = Color.White
                                                )
                                            }
                                        }"""
                                        
new_start_btn = """                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(64.dp)
                                                .background(FocusGreen, RoundedCornerShape(18.dp))
                                                .clickable {
                                                    viewModel.startFocusSession()
                                                    onStartSession()
                                                }
                                                .testTag("start_session_confirm_btn"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "LAUNCH FOCUS SHIELD",
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.ExtraBold,
                                                        letterSpacing = 1.sp
                                                    ),
                                                    color = Color.White
                                                )
                                            }
                                        }"""
content = content.replace(old_start_btn, new_start_btn)

# Remove the schedule toggle section
# Wait, I'll just use regex or find indices.
schedule_toggle_start = content.find("Row(\n                                    verticalAlignment = Alignment.CenterVertically")
schedule_toggle_end = content.find("                        // Section 2: Allowed Apps Whitelist")
if schedule_toggle_start != -1 and schedule_toggle_end != -1:
    content = content[:schedule_toggle_start] + "                        Spacer(modifier = Modifier.height(16.dp))\n" + content[schedule_toggle_end:]

with open("app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt", "w") as f:
    f.write(content)
