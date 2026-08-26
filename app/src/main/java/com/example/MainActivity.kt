package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.LockMode
import com.example.ui.navigation.FocusBottomNavigation
import com.example.ui.navigation.FocusNavGraph
import com.example.ui.navigation.FocusRoutes
import com.example.ui.theme.FocusBackground
import com.example.ui.theme.FocusOSTheme
import com.example.ui.viewmodel.FocusViewModel
import com.example.util.FocusLockManager
import com.example.util.FocusLockOverlayManager

class MainActivity : ComponentActivity() {

    private val viewModel: FocusViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FocusLockManager.onDistractionListener = { blockedPkg ->
            runOnUiThread {
                viewModel.triggerDistractionWarning(blockedPkg)
            }
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

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
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding).consumeWindowInsets(innerPadding).imePadding().background(FocusBackground)) {
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
        val startSessionId = intent.getLongExtra("START_SESSION_ID", -1L)
        if (startSessionId != -1L) {
            viewModel.loadScheduledSession(startSessionId)
        }
        val blockedPkg = intent.getStringExtra("BLOCKED_PACKAGE_EVENT")
        if (blockedPkg != null) {
            viewModel.triggerDistractionWarning(blockedPkg)
        }
    }

    override fun onResume() {
        super.onResume()
        FocusLockOverlayManager.dismissOverlay()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        enforceFocusLock()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            FocusLockOverlayManager.dismissOverlay()
        } else {
            enforceFocusLock()
        }
    }

    private fun enforceFocusLock() {
        val timerState = viewModel.timerState.value
        if (timerState.isRunning && timerState.lockMode != LockMode.NORMAL) {
            try {
                val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? android.app.usage.UsageStatsManager
                val time = System.currentTimeMillis()
                val stats = usageStatsManager?.queryUsageStats(android.app.usage.UsageStatsManager.INTERVAL_DAILY, time - 1000 * 6, time)
                
                if (!stats.isNullOrEmpty()) {
                    var recentApp = ""
                    var lastTime = 0L
                    for (usageStats in stats) {
                        if (usageStats.lastTimeUsed > lastTime) {
                            lastTime = usageStats.lastTimeUsed
                            recentApp = usageStats.packageName
                        }
                    }
                    
                    if (recentApp.isNotEmpty() && !FocusLockManager.isPackageAllowed(recentApp, packageName)) {
                        FocusLockManager.handleBlockedAppOpened(
                            context = this,
                            blockedPackageName = recentApp,
                            remainingSeconds = timerState.remainingSeconds,
                            subjectName = timerState.subjectName
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error checking usage stats", e)
            }
        }
    }
}
