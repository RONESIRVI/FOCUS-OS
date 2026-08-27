package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ui.screens.AppSelectorScreen
import com.example.ui.screens.FocusSetupScreen
import com.example.ui.screens.ScheduleCreateScreen
import com.example.ui.screens.FocusTimerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SessionCompleteScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.screens.CameraVerificationScreen
import com.example.ui.screens.SessionStartRouterScreen
import com.example.ui.viewmodel.FocusViewModel

object FocusRoutes {
    const val HOME = "home"
    const val SETUP = "setup"
    const val SCHEDULE_CREATE = "schedule_create"
    const val SCHEDULE_MAIN = "schedule_main"
    const val SETTINGS = "settings"
    const val APP_SELECTOR = "app_selector"
    const val CAMERA_START = "camera_start"
    const val TIMER = "timer"
    const val CAMERA_END = "camera_end"
    const val SESSION_COMPLETE = "session_complete"
    const val STATS = "stats"
    const val SESSION_START_ROUTER = "session_start_router"
}

@Composable
fun FocusNavGraph(
    navController: NavHostController,
    viewModel: FocusViewModel,
    startDestination: String = FocusRoutes.HOME
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(FocusRoutes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToSetup = { navController.navigate(FocusRoutes.SETUP) },
                onNavigateToScheduleCreate = { navController.navigate(FocusRoutes.SCHEDULE_CREATE) },
                onNavigateToScheduleMain = { navController.navigate(FocusRoutes.SCHEDULE_MAIN) },
                onNavigateToAppSelector = { navController.navigate(FocusRoutes.APP_SELECTOR) },
                onNavigateToStats = { navController.navigate(FocusRoutes.STATS) },
                onNavigateToTimer = { navController.navigate(FocusRoutes.TIMER) },
                onNavigateToCameraStart = { navController.navigate(FocusRoutes.CAMERA_START) },
                onNavigateToSessionRouter = { sessionId -> navController.navigate("${FocusRoutes.SESSION_START_ROUTER}/$sessionId") }
            )
        }
                
        composable(FocusRoutes.SCHEDULE_MAIN) {
            com.example.ui.screens.ScheduleMainScreen(
                viewModel = viewModel,
                onNavigateToCreate = { navController.navigate(FocusRoutes.SCHEDULE_CREATE) },
                onStartScheduled = { _ -> },
                onNavigateToSessionRouter = { sessionId -> navController.navigate("${FocusRoutes.SESSION_START_ROUTER}/$sessionId") }
            )
        }

        composable(FocusRoutes.SETTINGS) {
            com.example.ui.screens.SettingsScreen(
                viewModel = viewModel,
                onNavigateToAppSelector = { profile ->
                    viewModel.setAppSelectorProfile(profile)
                    navController.navigate(FocusRoutes.APP_SELECTOR)
                }
            )
        }

        composable(FocusRoutes.SCHEDULE_CREATE) {
            ScheduleCreateScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToAppSelector = { navController.navigate(FocusRoutes.APP_SELECTOR) },
                onScheduleCreated = { navController.popBackStack() }
            )
        }

        composable(FocusRoutes.SETUP) {
            FocusSetupScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToAppSelector = { navController.navigate(FocusRoutes.APP_SELECTOR) },
                onStartSession = {
                    if (viewModel.setupState.value.requiresPhoto) {
                        navController.navigate(FocusRoutes.CAMERA_START)
                    } else {
                        viewModel.startFocusSession()
                        navController.navigate(FocusRoutes.TIMER)
                    }
                }
            )
        }

        composable(FocusRoutes.APP_SELECTOR) {
            AppSelectorScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(FocusRoutes.CAMERA_START) {
            CameraVerificationScreen(
                viewModel = viewModel,
                isStart = true,
                onVerificationComplete = {
                    viewModel.startFocusSession() // Actually start the service here
                    navController.navigate(FocusRoutes.TIMER) {
                        popUpTo(FocusRoutes.CAMERA_START) { inclusive = true }
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }

                composable(
            route = "${FocusRoutes.SESSION_START_ROUTER}/{sessionId}",
            arguments = listOf(androidx.navigation.navArgument("sessionId") { type = androidx.navigation.NavType.LongType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: -1L
            SessionStartRouterScreen(
                sessionId = sessionId,
                viewModel = viewModel,
                onNavigateToCamera = { 
                    navController.navigate(FocusRoutes.CAMERA_START) {
                        popUpTo(FocusRoutes.HOME)
                    }
                },
                onNavigateToTimer = { 
                    navController.navigate(FocusRoutes.TIMER) {
                        popUpTo(FocusRoutes.HOME)
                    }
                }
            )
        }

        composable(FocusRoutes.TIMER) {
            FocusTimerScreen(
                viewModel = viewModel,
                onSessionComplete = { 
                    if (viewModel.setupState.value.requiresSelfie) {
                        navController.navigate(FocusRoutes.CAMERA_END) {
                            popUpTo(FocusRoutes.TIMER) { inclusive = true }
                        }
                    } else {
                        val wasScheduled = viewModel.activeScheduledSessionId.value != null
                        viewModel.completeFocusSession()
                        if (wasScheduled) {
                            android.widget.Toast.makeText(navController.context, "SCHEDULE History Save Ho चुकी है", android.widget.Toast.LENGTH_LONG).show()
                            navController.navigate(FocusRoutes.SCHEDULE_MAIN) {
                                popUpTo(FocusRoutes.TIMER) { inclusive = true }
                            }
                        } else {
                            navController.navigate(FocusRoutes.HOME) {
                                popUpTo(FocusRoutes.TIMER) { inclusive = true }
                            }
                        }
                    }
                }
            )
        }

        composable(FocusRoutes.CAMERA_END) {
            CameraVerificationScreen(
                viewModel = viewModel,
                isStart = false,
                onVerificationComplete = {
                    val wasScheduled = viewModel.activeScheduledSessionId.value != null
                    viewModel.completeFocusSession() // Save end photo
                    if (wasScheduled) {
                        android.widget.Toast.makeText(navController.context, "SCHEDULE History Save Ho चुकी है", android.widget.Toast.LENGTH_LONG).show()
                        navController.navigate(FocusRoutes.SCHEDULE_MAIN) {
                            popUpTo(FocusRoutes.TIMER) { inclusive = true }
                            popUpTo(FocusRoutes.CAMERA_END) { inclusive = true }
                        }
                    } else {
                        navController.navigate(FocusRoutes.HOME) {
                            popUpTo(FocusRoutes.TIMER) { inclusive = true }
                            popUpTo(FocusRoutes.CAMERA_END) { inclusive = true }
                        }
                    }
                },
                onCancel = { /* No cancel allowed at end */ }
            )
        }

        composable(FocusRoutes.SESSION_COMPLETE) {
            SessionCompleteScreen(
                viewModel = viewModel,
                onNavigateHome = {
                    navController.navigate(FocusRoutes.HOME) {
                        popUpTo(FocusRoutes.HOME) { inclusive = true }
                    }
                }
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
