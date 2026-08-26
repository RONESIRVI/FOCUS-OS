import re

with open("app/src/main/java/com/example/data/model/Models.kt", "r") as f:
    content = f.read()

content = content.replace('"Soft Lock (Level 1)"', '"Mindful Mode"')
content = content.replace('"Maximum Lock (Level 2)"', '"Deep Work Mode"')

with open("app/src/main/java/com/example/data/model/Models.kt", "w") as f:
    f.write(content)
