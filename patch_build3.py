import re

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

# add imports at the very beginning
if "import java.text.SimpleDateFormat" not in content:
    content = "import java.text.SimpleDateFormat\nimport java.util.Date\nimport java.util.Locale\n" + content

# replace the java.text.SimpleDateFormat with just SimpleDateFormat
content = content.replace("java.text.SimpleDateFormat", "SimpleDateFormat")
content = content.replace("java.util.Date", "Date")
content = content.replace("java.util.Locale", "Locale")

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
