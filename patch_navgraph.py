import re

with open("app/src/main/java/com/example/ui/navigation/NavGraph.kt", "r") as f:
    content = f.read()

old_settings = """        composable(FocusRoutes.SETTINGS) {
            // Placeholder for now
            com.example.ui.screens.SettingsScreen(viewModel = viewModel)
        }"""

new_settings = """        composable(FocusRoutes.SETTINGS) {
            com.example.ui.screens.SettingsScreen(
                viewModel = viewModel,
                onNavigateToAppSelector = { profile ->
                    viewModel.setAppSelectorProfile(profile)
                    navController.navigate(FocusRoutes.APP_SELECTOR)
                }
            )
        }"""

content = content.replace(old_settings, new_settings)

with open("app/src/main/java/com/example/ui/navigation/NavGraph.kt", "w") as f:
    f.write(content)
