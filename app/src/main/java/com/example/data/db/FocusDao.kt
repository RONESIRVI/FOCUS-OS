package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AllowedApp
import com.example.data.model.FocusSession
import com.example.data.model.SubjectTask
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusDao {
    // Focus Sessions
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<FocusSession>>

    @Query("SELECT * FROM focus_sessions WHERE timestamp >= :startTimeMs ORDER BY timestamp DESC")
    fun getSessionsSince(startTimeMs: Long): Flow<List<FocusSession>>

    @Query("SELECT * FROM focus_sessions WHERE status = 'SCHEDULED' ORDER BY scheduledStartTime ASC")
    fun getScheduledSessions(): Flow<List<FocusSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSession): Long

    @Update
    suspend fun updateSession(session: FocusSession)

    // Allowed Apps
    @Query("SELECT * FROM allowed_apps WHERE profile = :profile ORDER BY appName ASC")
    fun getAllowedApps(profile: String = "MANUAL"): Flow<List<AllowedApp>>

    @Query("SELECT * FROM allowed_apps WHERE profile = :profile AND isAllowed = 1")
    fun getWhitelistedApps(profile: String = "MANUAL"): Flow<List<AllowedApp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateApps(apps: List<AllowedApp>)

    @Query("UPDATE allowed_apps SET isAllowed = :isAllowed WHERE packageName = :packageName AND profile = :profile")
    suspend fun setAppAllowed(packageName: String, isAllowed: Boolean, profile: String = "MANUAL")

    // Subject Tasks
    @Query("SELECT * FROM subject_tasks ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<SubjectTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectTask)

    @androidx.room.Delete
    suspend fun deleteSubject(subject: SubjectTask)

    @Query("UPDATE subject_tasks SET completedSeconds = completedSeconds + :addSeconds WHERE name = :subjectName")
    suspend fun addSubjectTime(subjectName: String, addSeconds: Int)
}
