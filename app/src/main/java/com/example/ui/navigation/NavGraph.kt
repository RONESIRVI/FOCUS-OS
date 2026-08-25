package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ui.screens.AppSelectorScreen
import com.example.ui.screens.FocusSetupScreen
import com.example.ui.screens.FocusTimerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SessionCompleteScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.viewmodel.FocusViewModel

object FocusRoutes {
    const val HOME = "home"
    const val SETUP = "setup"
    const val APP_SELECTOR = "app_selector"
    const val TIMER = "timer"
    const val SESSION_COMPLETE = "session_complete"
    const val STATS = "stats"
}

@Composable
fun FocusNavGraph(
    navController: NavHostController,
    viewModel: FocusViewModel
) {
    NavHost(
        navController = navController,
        startDestination = FocusRoutes.HOME
    ) {
        composable(FocusRoutes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToSetup = { navController.navigate(FocusRoutes.SETUP) },
                onNavigateToAppSelector = { navController.navigate(FocusRoutes.APP_SELECTOR) },
                onNavigateToStats = { navController.navigate(FocusRoutes.STATS) },
                onNavigateToTimer = { navController.navigate(FocusRoutes.TIMER) }
            )
        }

        composable(FocusRoutes.SETUP) {
            FocusSetupScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToAppSelector = { navController.navigate(FocusRoutes.APP_SELECTOR) },
                onStartSession = { navController.navigate(FocusRoutes.TIMER) }
            )
        }

        composable(FocusRoutes.APP_SELECTOR) {
            AppSelectorScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(FocusRoutes.TIMER) {
            FocusTimerScreen(
                viewModel = viewModel,
                onSessionComplete = { navController.navigate(FocusRoutes.SESSION_COMPLETE) }
            )
        }

        composable(FocusRoutes.SESSION_COMPLETE) {
            SessionCompleteScreen(
                viewModel = viewModel,
                onNavigateHome = {
                    navController.navigate(FocusRoutes.HOME) {
                        popUpTo(FocusRoutes.HOME) { inclusive = true }
                    }
                },
                onNavigateStats = { navController.navigate(FocusRoutes.STATS) }
            )
        }

        composable(FocusRoutes.STATS) {
            StatisticsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
