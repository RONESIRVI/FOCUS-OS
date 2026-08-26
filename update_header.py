import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

old_header = """        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hamburger Menu Icon (3 horizontal lines) -> Opens Full App Guide
                IconButton(
                    onClick = { showAppGuide = true },
                    modifier = Modifier.testTag("hamburger_menu_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "App Complete Guide",
                        tint = FocusTextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "FOCUS OS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    color = FocusPrimary
                )

                // Notification Bell with dynamic badge
                Box(
                    modifier = Modifier.clickable { showNotifications = true }.testTag("header_notifications_btn"),
                    contentAlignment = Alignment.TopEnd
                ) {
                    IconButton(onClick = { showNotifications = true }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications & Alerts",
                            tint = FocusTextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    // Notification Badge
                    val alertCount = (if (scheduledSessions.isNotEmpty()) 1 else 0) + 2 // dynamic alerts
                    Surface(
                        shape = CircleShape,
                        color = FocusWarning,
                        modifier = Modifier
                            .padding(top = 6.dp, end = 6.dp)
                            .size(16.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$alertCount",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                ),
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }"""

new_header = """        // Premium Top Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Premium Styled App Brand
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
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "FOCUS OS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "PRODUCTIVITY ENGINE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                fontSize = 8.sp
                            ),
                            color = FocusPrimary
                        )
                    }
                }

                // Action Icons (Guide & Notifications)
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
                }
            }
        }"""

if old_header in content:
    content = content.replace(old_header, new_header)
    with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
        f.write(content)
    print("Replaced perfectly.")
else:
    print("Could not find the exact old_header block to replace.")

