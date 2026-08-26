import re

with open("app/src/main/java/com/example/ui/screens/StatisticsScreen.kt", "r") as f:
    content = f.read()

old_scaffold = """    Scaffold(
        topBar = {
            TopAppBar("""

new_scaffold = """    Scaffold(
        topBar = {
            if (!isExporting) {
                TopAppBar("""

content = content.replace(old_scaffold, new_scaffold)

# Need to close the if (!isExporting) block after the TopAppBar block.
old_topbar_end = """                colors = TopAppBarDefaults.topAppBarColors(containerColor = FocusBackground)
            )
        },
        containerColor = FocusBackground"""

new_topbar_end = """                colors = TopAppBarDefaults.topAppBarColors(containerColor = FocusBackground)
            )
            }
        },
        containerColor = FocusBackground"""

content = content.replace(old_topbar_end, new_topbar_end)

with open("app/src/main/java/com/example/ui/screens/StatisticsScreen.kt", "w") as f:
    f.write(content)
