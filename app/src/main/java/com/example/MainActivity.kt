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
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.data.model.LockMode
import com.example.ui.navigation.FocusNavGraph
import com.example.ui.navigation.FocusRoutes
import com.example.ui.theme.FocusOSTheme
import com.example.ui.viewmodel.FocusViewModel

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
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
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

