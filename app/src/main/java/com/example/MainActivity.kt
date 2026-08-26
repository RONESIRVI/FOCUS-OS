package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.data.model.LockMode
import com.example.ui.navigation.FocusNavGraph
import com.example.ui.navigation.FocusRoutes
import com.example.ui.theme.FocusOSTheme
import com.example.ui.viewmodel.FocusViewModel

import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.getValue
import com.example.ui.navigation.FocusBottomNavigation
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log


class MainActivity : ComponentActivity() {

    private val viewModel: FocusViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        var startDestination = FocusRoutes.HOME
        val startSessionId = intent.getLongExtra("START_SESSION_ID", -1L)
        if (startSessionId != -1L) {
            viewModel.loadScheduledSession(startSessionId)
            startDestination = FocusRoutes.CAMERA_START
        }
        
        enableEdgeToEdge()
        setContent {
            FocusOSTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
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
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Note: Dynamic deep link or intent handling for an already running activity would go here, 
        // but for now relying on recreated activity with FLAG_ACTIVITY_CLEAR_TASK is sufficient.
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        enforceFocusLock()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            enforceFocusLock()
        }
    }

    private fun enforceFocusLock() {
        val timerState = viewModel.timerState.value
        if (timerState.isRunning && timerState.lockMode != LockMode.NORMAL) {
            
            // Check if current foreground app is in the blocklist
            var shouldBlock = true // Default to true if we can't check
            try {
                val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val time = System.currentTimeMillis()
                val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time)
                
                if (stats != null && stats.isNotEmpty()) {
                    var recentApp = ""
                    var lastTime = 0L
                    for (usageStats in stats) {
                        if (usageStats.lastTimeUsed > lastTime) {
                            lastTime = usageStats.lastTimeUsed
                            recentApp = usageStats.packageName
                        }
                    }
                    
                    if (recentApp == packageName || recentApp.contains("launcher") || recentApp.contains("systemui")) {
                        shouldBlock = false
                    } else {
                        // Check against block list
                        val blockedApps = if (timerState.lockMode == LockMode.STRICT_LOCK) {
                            viewModel.whitelistedAppsStrict.value
                        } else {
                            viewModel.whitelistedAppsManual.value
                        }
                        val isBlocked = blockedApps.any { it.packageName == recentApp && !it.isAllowed }
                        shouldBlock = isBlocked
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error checking usage stats", e)
            }
            
            if (shouldBlock) {
                viewModel.triggerDistractionWarning()

                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(intent)
            }
        }
    }
}

