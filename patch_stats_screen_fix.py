import re

with open("app/src/main/java/com/example/ui/screens/StatisticsScreen.kt", "r") as f:
    content = f.read()

old_modifier = """                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .let { if (isExporting) it else it.verticalScroll(rememberScrollState()) },"""

new_modifier = """                .let { if (isExporting) it.fillMaxWidth() else it.fillMaxSize() }
                .padding(padding)
                .padding(horizontal = 16.dp)
                .let { if (isExporting) it else it.verticalScroll(rememberScrollState()) },"""

content = content.replace(old_modifier, new_modifier)

with open("app/src/main/java/com/example/ui/screens/StatisticsScreen.kt", "w") as f:
    f.write(content)
