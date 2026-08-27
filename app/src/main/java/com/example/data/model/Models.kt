package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class LockMode(val title: String, val description: String) {
    NORMAL("Normal", "Standard Pomodoro countdown. Switch apps freely."),
    SOFT_LOCK("Mindful Mode", "Gentle warning alert appears only when attempting to open blocked apps."),
    MAXIMUM_LOCK("Deep Work Mode", "Kiosk lockdown mode. Emergency exit requires 300s delay penalty.")
}

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionName: String,
    val subjectName: String,
    val targetDurationMinutes: Int,
    val completedDurationSeconds: Int,
    val lockMode: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = true,
    val distractionAttempts: Int = 0,
    val allowedAppsCount: Int = 0,
    
    // Scheduled & Verification Fields
    val scheduledStartTime: Long? = null,
    val scheduledEndTime: Long? = null,
    val requiresPhoto: Boolean = false,
    val requiresSelfie: Boolean = false,
    val startPhotoUri: String? = null,
    val endSelfieUri: String? = null,
    val status: String = "COMPLETED" // "SCHEDULED", "ACTIVE", "COMPLETED", "MISSED"
)

@Entity(
    tableName = "allowed_apps",
    primaryKeys = ["packageName", "profile"]
)
data class AllowedApp(
    val packageName: String,
    val profile: String = "MANUAL", // "MANUAL" or "STRICT"
    val appName: String,
    val category: String, // e.g., "Notes", "Reader", "Video", "Browser", "Tools"
    val isAllowed: Boolean = true,
    val iconName: String = "ic_app"
)

@Entity(tableName = "subject_tasks")
data class SubjectTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val categoryColorHex: String,
    val targetHours: Float = 5.0f,
    val completedSeconds: Int = 0
)

data class DailyFocusStat(
    val dateString: String,
    val totalSeconds: Int,
    val sessionCount: Int,
    val streakDays: Int
)
