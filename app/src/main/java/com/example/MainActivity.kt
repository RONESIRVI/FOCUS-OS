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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
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
        FocusLockManager.onDistractionListener = { blockedPkg, showRedModal ->
            runOnUiThread {
                if (showRedModal) {
                    viewModel.triggerDistractionWarning(blockedPkg, showRedModal = true)
                } else {
                    viewModel.triggerDistractionWarning(blockedPkg, showSoftModal = true)
                }
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
            viewModel.triggerStartSession(startSessionId)
            intent.removeExtra("START_SESSION_ID")
        }
        val blockedPkgOnCreate = intent.getStringExtra("BLOCKED_PACKAGE_EVENT")
        if (blockedPkgOnCreate != null) {
            viewModel.triggerDistractionWarning(blockedPkgOnCreate, showRedModal = true)
            intent.removeExtra("BLOCKED_PACKAGE_EVENT")
        }
        val blockedPkgSoftOnCreate = intent.getStringExtra("BLOCKED_PACKAGE_EVENT_SOFT")
        if (blockedPkgSoftOnCreate != null) {
            viewModel.triggerDistractionWarning(blockedPkgSoftOnCreate, showSoftModal = true)
            intent.removeExtra("BLOCKED_PACKAGE_EVENT_SOFT")
        }
        
        enableEdgeToEdge()
        setContent {
            FocusOSTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val showBottomBar = currentRoute in listOf(FocusRoutes.HOME, FocusRoutes.SCHEDULE_MAIN, FocusRoutes.STATS, FocusRoutes.SETTINGS)

                val timerState by viewModel.timerState.collectAsState()
                val showLockOverlay by viewModel.showLockOverlay.collectAsState()
                val showSoftLockOverlay by viewModel.showSoftLockOverlay.collectAsState()

                LaunchedEffect(showLockOverlay, showSoftLockOverlay) {
                    if ((showLockOverlay || showSoftLockOverlay) && timerState.isRunning && currentRoute != FocusRoutes.TIMER) {
                        navController.navigate(FocusRoutes.TIMER) {
                            popUpTo(FocusRoutes.HOME)
                            launchSingleTop = true
                        }
                    }
                }

                val startSessionEvent by viewModel.startSessionEvent.collectAsState()
                LaunchedEffect(startSessionEvent) {
                    startSessionEvent?.let { sessionId ->
                        navController.navigate("${FocusRoutes.SESSION_START_ROUTER}/$sessionId") {
                            popUpTo(FocusRoutes.HOME)
                        }
                        viewModel.clearStartSessionEvent()
                    }
                }
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
            viewModel.triggerStartSession(startSessionId)
            intent.removeExtra("START_SESSION_ID")
        }
        val blockedPkg = intent.getStringExtra("BLOCKED_PACKAGE_EVENT")
        if (blockedPkg != null) {
            viewModel.triggerDistractionWarning(blockedPkg, showRedModal = true)
            intent.removeExtra("BLOCKED_PACKAGE_EVENT")
        }
        val blockedPkgSoft = intent.getStringExtra("BLOCKED_PACKAGE_EVENT_SOFT")
        if (blockedPkgSoft != null) {
            viewModel.triggerDistractionWarning(blockedPkgSoft, showSoftModal = true)
            intent.removeExtra("BLOCKED_PACKAGE_EVENT_SOFT")
        }
        val blockedPkgPending = intent.getStringExtra("BLOCKED_PACKAGE_EVENT_PENDING")
        if (blockedPkgPending != null) {
            val pName = intent.getStringExtra("PENDING_SESSION_NAME") ?: "Scheduled Focus"
            val pId = intent.getLongExtra("PENDING_SESSION_ID", -1L)
            viewModel.triggerPendingDistractionWarning(blockedPkgPending, pName, pId)
            intent.removeExtra("BLOCKED_PACKAGE_EVENT_PENDING")
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
        if (FocusLockManager.isCameraVerificationActive) {
            Log.d("MainActivity", "Camera verification active, skipping enforceFocusLock")
            return
        }
        val timerState = viewModel.timerState.value
        if ((timerState.isRunning && timerState.lockMode != LockMode.NORMAL) || FocusLockManager.hasPendingSchedule()) {
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
                    
                    if (recentApp.isNotEmpty() && !FocusLockManager.isPackageAllowed(this, recentApp, packageName)) {
                        FocusLockManager.handleBlockedAppOpened(
                            context = this,
                            blockedPackageName = recentApp,
                            remainingSeconds = timerState.remainingSeconds,
                            subjectName = timerState.subjectName
                        )
                    } else {
                        FocusLockOverlayManager.dismissOverlay()
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error checking usage stats", e)
            }
        }
    }
}
