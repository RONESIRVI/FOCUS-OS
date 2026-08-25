package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class LockMode(val title: String, val description: String) {
    NORMAL("Normal", "Standard Pomodoro countdown. Switch apps freely."),
    SOFT_LOCK("Soft Lock (Level 1)", "Warning overlay alerts you when attempting to switch apps."),
    STRICT_LOCK("Strict Lock (Level 2)", "Locks phone to Focus App & Whitelisted Apps only. Anti-Exit protection."),
    MAXIMUM_LOCK("Maximum Lock (Level 3)", "Kiosk lockdown mode. Emergency exit requires 10s delay penalty.")
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
    val allowedAppsCount: Int = 0
)

@Entity(tableName = "allowed_apps")
data class AllowedApp(
    @PrimaryKey val packageName: String,
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
