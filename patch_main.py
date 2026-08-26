import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_code = """        val blockedPkg = intent.getStringExtra("BLOCKED_PACKAGE_EVENT")
        if (blockedPkg != null) {
            viewModel.triggerDistractionWarning(blockedPkg)
        }"""

new_code = """        val blockedPkg = intent.getStringExtra("BLOCKED_PACKAGE_EVENT")
        if (blockedPkg != null) {
            viewModel.triggerDistractionWarning(blockedPkg, showRedModal = true)
        }"""

content = content.replace(old_code, new_code)

# There is also one in onCreate? Let's check.
content = content.replace('viewModel.triggerDistractionWarning(blockedPkg)', 'viewModel.triggerDistractionWarning(blockedPkg, showRedModal = true)')

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)

