import re

with open("app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt", "r") as f:
    content = f.read()

toast_code = """            launch(Dispatchers.Main) {
                val formatter = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
                val timeStr = formatter.format(java.util.Date(scheduledStartTime))
                val diffMins = (scheduledStartTime - System.currentTimeMillis()) / 60000
                android.widget.Toast.makeText(context, "✅ Strict Focus scheduled for $timeStr (in $diffMins mins)", android.widget.Toast.LENGTH_LONG).show()
            }"""

content = re.sub(r"            launch\(Dispatchers\.Main\) \{.*?Toast.*?\}", toast_code, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt", "w") as f:
    f.write(content)
