import re

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

content = content.replace('versionCode = 3', 'versionCode = 4')
content = content.replace('versionName = "2.0.0"', 'versionName = "2.1.0"')

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
