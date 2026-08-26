with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

bad_ending = """                    }

        }

        // Appearance"""

good_ending = """                    }
                    if (index < selectableModes.size - 1) {
                        Divider(color = FocusSurfaceVariant)
                    }
                }
            }
        }

        // Appearance"""

content = content.replace(bad_ending, good_ending)

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
