import re

with open("app/src/main/AndroidManifest.xml", "r") as f:
    content = f.read()

# Add schedule exact alarm permission if not present
if "SCHEDULE_EXACT_ALARM" not in content:
    content = content.replace("<uses-permission android:name=\"android.permission.FOREGROUND_SERVICE\" />", "<uses-permission android:name=\"android.permission.FOREGROUND_SERVICE\" />\n    <uses-permission android:name=\"android.permission.SCHEDULE_EXACT_ALARM\" />\n    <uses-permission android:name=\"android.permission.USE_EXACT_ALARM\" />")

with open("app/src/main/AndroidManifest.xml", "w") as f:
    f.write(content)
