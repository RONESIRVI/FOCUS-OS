import re

with open("app/src/main/java/com/example/ui/screens/ScheduleMainScreen.kt", "r") as f:
    content = f.read()

# Replace actions block
old_actions = """                actions = {
                    IconButton(onClick = onNavigateToCreate) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Schedule",
                            tint = Color.White
                        )
                    }
                },"""

new_actions = """                actions = {
                    Button(
                        onClick = onNavigateToCreate,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)), // Bright Blue
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("NEW SCHEDULE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },"""
content = content.replace(old_actions, new_actions)

# Remove floatingActionButton block
old_fab = """        floatingActionButton = {
            if (selectedTabIndex == 0) {
                FloatingActionButton(
                    onClick = onNavigateToCreate,
                    containerColor = FocusPrimary,
                    contentColor = Color.Black,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Schedule")
                }
            }
        },"""

content = content.replace(old_fab, "")

with open("app/src/main/java/com/example/ui/screens/ScheduleMainScreen.kt", "w") as f:
    f.write(content)
