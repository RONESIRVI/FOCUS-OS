import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

tz_code = """        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("America/Los_Angeles"))
        super.onCreate(savedInstanceState)"""

content = content.replace("super.onCreate(savedInstanceState)", tz_code)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
