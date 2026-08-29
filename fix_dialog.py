import re

with open("app/src/main/java/com/example/ui/screens/ScheduleValidationDialog.kt", "r") as f:
    content = f.read()

# Fix duration format
old_duration = """Text(
                        text = "Duration: ${durationMins / 60}h ${durationMins % 60}m",
                        color = Color.DarkGray,
                        fontSize = 12.sp
                    )"""
new_duration = """Text(
                        text = "Duration: ${if(durationMins >= 60) "${durationMins / 60}h " else ""}${if(durationMins % 60 > 0 || durationMins < 60) "${durationMins % 60}m" else ""}".trim(),
                        color = Color.DarkGray,
                        fontSize = 12.sp
                    )"""

content = content.replace(old_duration, new_duration)

with open("app/src/main/java/com/example/ui/screens/ScheduleValidationDialog.kt", "w") as f:
    f.write(content)
