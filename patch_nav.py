import sys

with open("app/src/main/java/com/example/ui/navigation/NavGraph.kt", "r") as f:
    content = f.read()

# Add to FocusRoutes
content = content.replace('const val SETUP = "setup"', 'const val SETUP = "setup"\n    const val SCHEDULE_CREATE = "schedule_create"')

# Add new import
content = content.replace('import com.example.ui.screens.FocusSetupScreen', 'import com.example.ui.screens.FocusSetupScreen\nimport com.example.ui.screens.ScheduleCreateScreen')

# Add the new composable
new_composable = """        composable(FocusRoutes.SCHEDULE_CREATE) {
            ScheduleCreateScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToAppSelector = { navController.navigate(FocusRoutes.APP_SELECTOR) },
                onScheduleCreated = { navController.popBackStack() }
            )
        }
        composable(FocusRoutes.SETUP) {"""
        
content = content.replace('composable(FocusRoutes.SETUP) {', new_composable)

# Update HomeScreen call to expect onNavigateToScheduleCreate
# Wait, let's look at NavGraph.kt again.
# HomeScreen(...)
home_target = """            HomeScreen(
                viewModel = viewModel,
                onNavigateToSetup = { navController.navigate(FocusRoutes.SETUP) },
                onNavigateToAppSelector = { navController.navigate(FocusRoutes.APP_SELECTOR) },
                onNavigateToStats = { navController.navigate(FocusRoutes.STATS) },
                onNavigateToTimer = { navController.navigate(FocusRoutes.TIMER) }
            )"""
            
home_replacement = """            HomeScreen(
                viewModel = viewModel,
                onNavigateToSetup = { navController.navigate(FocusRoutes.SETUP) },
                onNavigateToScheduleCreate = { navController.navigate(FocusRoutes.SCHEDULE_CREATE) },
                onNavigateToAppSelector = { navController.navigate(FocusRoutes.APP_SELECTOR) },
                onNavigateToStats = { navController.navigate(FocusRoutes.STATS) },
                onNavigateToTimer = { navController.navigate(FocusRoutes.TIMER) }
            )"""

if home_target in content:
    content = content.replace(home_target, home_replacement)
else:
    print("Failed to replace home_target")

with open("app/src/main/java/com/example/ui/navigation/NavGraph.kt", "w") as f:
    f.write(content)
