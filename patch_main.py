import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Add necessary imports
imports = """
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.getValue
import com.example.ui.navigation.FocusBottomNavigation
"""

if "currentBackStackEntryAsState" not in content:
    content = content.replace("import com.example.ui.viewmodel.FocusViewModel", "import com.example.ui.viewmodel.FocusViewModel\n" + imports)

# Update Scaffold
old_scaffold = """                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        FocusNavGraph(
                            navController = navController,
                            viewModel = viewModel,
                            startDestination = startDestination
                        )
                    }
                }"""

new_scaffold = """                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val showBottomBar = currentRoute in listOf(FocusRoutes.HOME, FocusRoutes.SCHEDULE_MAIN, FocusRoutes.STATS, FocusRoutes.SETTINGS)

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            FocusBottomNavigation(
                                currentRoute = currentRoute ?: FocusRoutes.HOME,
                                onNavigate = { route -> 
                                    navController.navigate(route) {
                                        popUpTo(FocusRoutes.HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onQuickFocus = { navController.navigate(FocusRoutes.SETUP) }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding).background(com.example.ui.theme.FocusBackground)) {
                        FocusNavGraph(
                            navController = navController,
                            viewModel = viewModel,
                            startDestination = startDestination
                        )
                    }
                }"""

content = content.replace(old_scaffold, new_scaffold)

if "import androidx.compose.foundation.background" not in content:
    content = content.replace("import androidx.compose.foundation.layout.padding", "import androidx.compose.foundation.layout.padding\nimport androidx.compose.foundation.background")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)

print("Patched MainActivity.kt")
