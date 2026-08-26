import re

with open("app/src/main/java/com/example/ui/navigation/NavGraph.kt", "r") as f:
    content = f.read()

if "SCHEDULE_MAIN" not in content:
    content = content.replace("const val SCHEDULE_CREATE = \"schedule_create\"", "const val SCHEDULE_CREATE = \"schedule_create\"\n    const val SCHEDULE_MAIN = \"schedule_main\"\n    const val SETTINGS = \"settings\"")

if "composable(FocusRoutes.SCHEDULE_MAIN)" not in content:
    nav_extensions = """
        composable(FocusRoutes.SCHEDULE_MAIN) {
            // Placeholder for now
            com.example.ui.screens.ScheduleMainScreen(viewModel = viewModel, onNavigateToCreate = { navController.navigate(FocusRoutes.SCHEDULE_CREATE) })
        }
        composable(FocusRoutes.SETTINGS) {
            // Placeholder for now
            com.example.ui.screens.SettingsScreen(viewModel = viewModel)
        }
"""
    content = content.replace("composable(FocusRoutes.SCHEDULE_CREATE) {", nav_extensions + "        composable(FocusRoutes.SCHEDULE_CREATE) {")

with open("app/src/main/java/com/example/ui/navigation/NavGraph.kt", "w") as f:
    f.write(content)

print("Patched NavGraph.kt")
