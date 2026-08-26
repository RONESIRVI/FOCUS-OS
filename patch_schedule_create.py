import re

with open("app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt", "r") as f:
    content = f.read()

header = """            Text(
                text = "CREATE SCHEDULE",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                ),
                color = Color.White
            )"""

new_header = """            Text(
                text = "CREATE STRICT SCHEDULE",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                ),
                color = Color.White
            )"""
            
content = content.replace(header, new_header)

lock_section = """            // App Selection Preview
            Text("WHITELISTED APPS", style = MaterialTheme.typography.labelMedium, color = FocusTextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusSurfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.setAppSelectorProfile("STRICT")
                        onNavigateToAppSelector()
                    }
            )"""
            
new_lock_section = """            // Strict Mode Indicator
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(40.dp).background(FocusWarning.copy(alpha=0.2f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = FocusWarning, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("STRICT MODE", style = MaterialTheme.typography.labelSmall, color = FocusTextSecondary)
                        Text("ENABLED", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = FocusWarning)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // App Selection Preview
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusSurfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.setAppSelectorProfile("STRICT")
                        onNavigateToAppSelector()
                    }
            )"""

content = content.replace(lock_section, new_lock_section)

if "import androidx.compose.material.icons.filled.Lock" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Schedule", "import androidx.compose.material.icons.filled.Schedule\nimport androidx.compose.material.icons.filled.Lock")

with open("app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt", "w") as f:
    f.write(content)
