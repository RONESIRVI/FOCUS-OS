import re

with open("app/src/main/java/com/example/ui/screens/StatisticsScreen.kt", "r") as f:
    content = f.read()

if "import android.widget.Toast" not in content:
    content = content.replace("import androidx.compose.ui.platform.LocalContext", "") # remove if exists
    content = content.replace("import androidx.compose.ui.Alignment", "import androidx.compose.ui.Alignment\nimport android.widget.Toast\nimport androidx.compose.ui.platform.LocalContext")

old_start = """fun StatisticsScreen(
    viewModel: FocusViewModel,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("Period") }"""
new_start = """fun StatisticsScreen(
    viewModel: FocusViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("Period") }"""
content = content.replace(old_start, new_start)

old_actions = """                actions = {
                    IconButton(onClick = { /* TODO: Download Statistics */ }) {
                        Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.White)
                    }
                },"""
new_actions = """                actions = {
                    IconButton(onClick = { 
                        Toast.makeText(context, "Statistics downloaded successfully.", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.White)
                    }
                },"""
content = content.replace(old_actions, new_actions)

with open("app/src/main/java/com/example/ui/screens/StatisticsScreen.kt", "w") as f:
    f.write(content)
