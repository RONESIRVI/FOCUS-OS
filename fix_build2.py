import re

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

content = content.replace('setProperty("archivesBaseName", "FocusOS_v${versionName}_${dateStr}")', 
                          'base.archivesName.set("FocusOS_v${versionName}_${dateStr}")')

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
