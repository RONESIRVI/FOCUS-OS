import re

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

# Fix imports
content = content.replace("import SimpleDateFormat", "import java.text.SimpleDateFormat")
content = content.replace("import Date", "import java.util.Date")
content = content.replace("import Locale", "import java.util.Locale")

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
