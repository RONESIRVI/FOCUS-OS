import re

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

target = 'testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"'

replacement = '''testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    
    val formatter = java.text.SimpleDateFormat("dd-MMM-yyyy_HH-mm-ss", java.util.Locale.getDefault())
    val dateStr = formatter.format(java.util.Date())
    setProperty("archivesBaseName", "FocusOS_v${versionName}_${dateStr}")'''

content = content.replace(target, replacement)

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
