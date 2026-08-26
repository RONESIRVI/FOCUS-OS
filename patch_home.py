import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# Add imports if missing
if "import androidx.compose.foundation.Image" not in content:
    content = content.replace("import androidx.compose.foundation.layout.*", "import androidx.compose.foundation.layout.*\nimport androidx.compose.foundation.Image")

if "import androidx.compose.ui.res.painterResource" not in content:
    content = content.replace("import androidx.compose.ui.Modifier", "import androidx.compose.ui.Modifier\nimport androidx.compose.ui.res.painterResource\nimport com.example.R")


old_block = """                // Premium Styled App Brand
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(FocusPrimary, Color(0xFF60A5FA))
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }"""

new_block = """                // Premium Styled App Brand
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = FocusSurfaceVariant, // neutral background since icon has its own colors
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "App Icon",
                            modifier = Modifier.size(36.dp)
                        )
                    }"""

content = content.replace(old_block, new_block)

old_actions = """                // Action Icons (Guide & Notifications)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // App Guide (MenuBook Icon)
                    Surface(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .clickable { showAppGuide = true }
                            .testTag("hamburger_menu_btn"),
                        shape = CircleShape,
                        color = FocusSurfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FocusSurfaceVariant)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "App Complete Guide",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(10.dp))
                    
                    // Notification Bell
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(FocusSurfaceVariant.copy(alpha = 0.4f))
                            .border(1.dp, FocusSurfaceVariant, CircleShape)
                            .clickable { showNotifications = true }
                            .testTag("header_notifications_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications & Alerts",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(20.dp)
                        )
                        
                        // Notification Badge (Minimalist Dot)
                        val alertCount = (if (scheduledSessions.isNotEmpty()) 1 else 0) + 2
                        if (alertCount > 0) {
                            Surface(
                                shape = CircleShape,
                                color = FocusWarning,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 10.dp, end = 10.dp)
                                    .size(8.dp)
                            ) {}
                        }
                    }
                }"""


new_actions = """                // Action Icons (Guide & Notifications)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // App Guide (MenuBook Icon)
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { showAppGuide = true }
                            .testTag("hamburger_menu_btn"),
                        shape = CircleShape,
                        color = FocusSurfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FocusSurfaceVariant)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "App Complete Guide",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Notification Bell
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(FocusSurfaceVariant.copy(alpha = 0.4f))
                            .border(1.dp, FocusSurfaceVariant, CircleShape)
                            .clickable { showNotifications = true }
                            .testTag("header_notifications_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications & Alerts",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(24.dp)
                        )
                        
                        // Notification Badge (Minimalist Dot)
                        val alertCount = (if (scheduledSessions.isNotEmpty()) 1 else 0) + 2
                        if (alertCount > 0) {
                            Surface(
                                shape = CircleShape,
                                color = FocusWarning,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 10.dp, end = 10.dp)
                                    .size(12.dp)
                            ) {}
                        }
                    }
                }"""

content = content.replace(old_actions, new_actions)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
print("done")
