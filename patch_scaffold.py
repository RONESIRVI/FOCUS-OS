import re

with open("app/src/main/java/com/example/ui/screens/StatisticsScreen.kt", "r") as f:
    content = f.read()

# add the ConditionalScaffold function right before StatisticsScreen
old_fun_start = """@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen("""

new_fun_start = """@Composable
fun ConditionalScaffold(
    isExporting: Boolean,
    topBar: @Composable () -> Unit,
    containerColor: androidx.compose.ui.graphics.Color,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit
) {
    if (isExporting) {
        androidx.compose.material3.Surface(color = containerColor) {
            content(androidx.compose.foundation.layout.PaddingValues(0.dp))
        }
    } else {
        androidx.compose.material3.Scaffold(
            topBar = topBar,
            containerColor = containerColor,
            content = content
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen("""

content = content.replace(old_fun_start, new_fun_start)

# Now replace Scaffold( with ConditionalScaffold(isExporting = isExporting,
old_scaffold = """    Scaffold(
        topBar = {"""
new_scaffold = """    ConditionalScaffold(
        isExporting = isExporting,
        topBar = {"""

content = content.replace(old_scaffold, new_scaffold)

with open("app/src/main/java/com/example/ui/screens/StatisticsScreen.kt", "w") as f:
    f.write(content)
