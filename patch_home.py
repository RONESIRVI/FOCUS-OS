import sys

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

target1 = """fun HomeScreen(
    viewModel: FocusViewModel,
    onNavigateToSetup: () -> Unit,
    onNavigateToAppSelector: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToTimer: () -> Unit
)"""

repl1 = """fun HomeScreen(
    viewModel: FocusViewModel,
    onNavigateToSetup: () -> Unit,
    onNavigateToScheduleCreate: () -> Unit,
    onNavigateToAppSelector: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToTimer: () -> Unit
)"""

content = content.replace(target1, repl1)

# Find the OutlinedButton for ADD SESSION
target2 = """OutlinedButton(
                        onClick = { onNavigateToSetup() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),"""
                        
repl2 = """OutlinedButton(
                        onClick = { onNavigateToScheduleCreate() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),"""

content = content.replace(target2, repl2)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
