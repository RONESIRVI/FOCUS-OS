import re

with open("app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt", "r") as f:
    content = f.read()

# Fix 1: Make sure SPECIAL sets selectedWhitelistProfile to "SPECIAL" not ""
content = content.replace(
'''                                        } else if (option.id == "SPECIAL") {
                                            selectedWhitelistProfile = ""
                                        }''',
'''                                        } else if (option.id == "SPECIAL") {
                                            selectedWhitelistProfile = "SPECIAL"
                                        }''')

content = content.replace(
'''                                            } else if (option.id == "SPECIAL") {
                                                selectedWhitelistProfile = ""
                                            }''',
'''                                            } else if (option.id == "SPECIAL") {
                                                selectedWhitelistProfile = "SPECIAL"
                                            }''')

# Fix 2: Display the correct count of allowed apps
target_count = '''                                            val currentAppsCount = when (selectedWhitelistProfile) {
                                                "SPECIAL" -> whitelistedAppsSpecial.size
                                                "MANUAL" -> whitelistedAppsManual.size
                                                else -> whitelistedAppsStrict.size
                                            }'''

replacement_count = '''                                            val currentAppsCount = when (selectedWhitelistProfile) {
                                                "SPECIAL" -> whitelistedAppsSpecial.count { it.isAllowed }
                                                "MANUAL" -> whitelistedAppsManual.count { it.isAllowed }
                                                else -> whitelistedAppsStrict.count { it.isAllowed }
                                            }'''

content = content.replace(target_count, replacement_count)

with open("app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt", "w") as f:
    f.write(content)
