import re

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

old_profile = """                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(FocusSurfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = FocusTextSecondary, modifier = Modifier.size(32.dp))
                    }"""

new_profile = """                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clickable { /* TODO: Image Picker */ }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(FocusSurfaceVariant, CircleShape)
                                .align(Alignment.Center),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = FocusTextSecondary, modifier = Modifier.size(32.dp))
                        }
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(FocusPrimary, CircleShape)
                                .align(Alignment.BottomEnd)
                                .border(2.dp, FocusSurface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Upload Photo", tint = Color.Black, modifier = Modifier.size(12.dp))
                        }
                    }"""

content = content.replace(old_profile, new_profile)

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
