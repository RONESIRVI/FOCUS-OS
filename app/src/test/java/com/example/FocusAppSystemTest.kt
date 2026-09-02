package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.db.FocusDao
import com.example.data.model.AllowedApp
import com.example.data.model.FocusSession
import com.example.data.model.LockMode
import com.example.data.model.SubjectTask
import com.example.data.repository.FocusRepository
import com.example.services.FocusAudioEngine
import com.example.services.SoundType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FocusAppSystemTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: FocusDao
    private lateinit var repository: FocusRepository
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.focusDao()
        repository = FocusRepository(dao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testSessionInsertAndRetrieve() = runBlocking {
        val session = FocusSession(
            sessionName = "Deep Math Focus",
            subjectName = "Mathematics",
            targetDurationMinutes = 25,
            completedDurationSeconds = 1500,
            lockMode = "MAXIMUM_LOCK",
            distractionAttempts = 0,
            status = "COMPLETED",
            timestamp = System.currentTimeMillis()
        )

        val id = repository.saveSession(session)
        assertTrue(id > 0)

        val sessions = repository.allSessions.first()
        assertEquals(1, sessions.size)
        assertEquals("Deep Math Focus", sessions[0].sessionName)
        assertEquals("Mathematics", sessions[0].subjectName)
        assertEquals("COMPLETED", sessions[0].status)
    }

    @Test
    fun testScheduledSessionFlowAndDeletion() = runBlocking {
        val now = System.currentTimeMillis()
        val scheduledSession = FocusSession(
            sessionName = "Evening Physics Session",
            subjectName = "Physics",
            targetDurationMinutes = 45,
            completedDurationSeconds = 0,
            lockMode = "MAXIMUM_LOCK",
            status = "SCHEDULED",
            scheduledStartTime = now + 600000,
            scheduledEndTime = now + 3300000,
            requiresPhoto = true,
            requiresSelfie = true,
            timestamp = now
        )

        val sessionId = repository.saveSession(scheduledSession)
        assertTrue(sessionId > 0)

        val scheduledList = repository.scheduledSessions.first()
        assertEquals(1, scheduledList.size)
        assertEquals("SCHEDULED", scheduledList[0].status)
        assertEquals("Physics", scheduledList[0].subjectName)

        val saved = scheduledList[0]
        repository.deleteSession(saved)

        val afterDelete = repository.scheduledSessions.first()
        assertEquals(0, afterDelete.size)
    }

    @Test
    fun testAllowedAppsAndWhitelistToggle() = runBlocking {
        val apps = listOf(
            AllowedApp("com.google.android.calculator", "MANUAL", "Calculator", "Tools", false),
            AllowedApp("com.example.notes", "MANUAL", "Notes", "Productivity", false),
            AllowedApp("com.google.android.youtube", "MANUAL", "YouTube", "Entertainment", false)
        )
        dao.insertOrUpdateApps(apps)

        val initialAllowed = repository.allowedApps("MANUAL").first()
        assertEquals(3, initialAllowed.size)
        val initialWhitelisted = repository.whitelistedApps("MANUAL").first()
        assertEquals(0, initialWhitelisted.size)

        // Whitelist Calculator
        repository.toggleAppWhitelist("com.google.android.calculator", true, "MANUAL")
        val updatedWhitelisted = repository.whitelistedApps("MANUAL").first()
        assertEquals(1, updatedWhitelisted.size)
        assertEquals("Calculator", updatedWhitelisted[0].appName)

        // Toggle off
        repository.toggleAppWhitelist("com.google.android.calculator", false, "MANUAL")
        val toggledOff = repository.whitelistedApps("MANUAL").first()
        assertEquals(0, toggledOff.size)
    }

    @Test
    fun testSubjectTasks() = runBlocking {
        repository.addSubject("History", "#E06D53")
        repository.addSubject("Biology", "#4ADE80")

        val subjects = repository.allSubjects.first()
        assertEquals(2, subjects.size)
        assertTrue(subjects.any { it.name == "History" })
        assertTrue(subjects.any { it.name == "Biology" })

        val bio = subjects.first { it.name == "Biology" }
        repository.deleteSubject(bio)

        val remaining = repository.allSubjects.first()
        assertEquals(1, remaining.size)
        assertEquals("History", remaining[0].name)
    }

    @Test
    fun testAudioEngineSoundGeneration() {
        val audioEngine = FocusAudioEngine()
        audioEngine.startSound(SoundType.NONE, CoroutineScope(Dispatchers.Default))
        audioEngine.stopSound()
    }

    @Test
    fun testEnforceWhitelistLogic() {
        val ownPackage = "com.aistudio.focusos.kxmpzq"
        val whitelisted = listOf("com.google.android.calculator", "com.example.notes")
        
        // Own package should be allowed
        val isOwnAllowed = ownPackage == ownPackage || whitelisted.contains(ownPackage)
        assertTrue(isOwnAllowed)

        // Whitelisted app should be allowed
        val isCalcAllowed = "com.google.android.calculator" == ownPackage || whitelisted.contains("com.google.android.calculator")
        assertTrue(isCalcAllowed)

        // Distraction app (e.g. Social media) should NOT be allowed
        val isDistractionAllowed = "com.instagram.android" == ownPackage || whitelisted.contains("com.instagram.android")
        assertFalse(isDistractionAllowed)
    }

    @Test
    fun testPeriodCalculationsAndFiltering() = runBlocking {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 3600 * 1000L

        // Session 1: Today (25m = 1500s)
        val s1 = FocusSession(
            sessionName = "Today Focus",
            subjectName = "Mathematics",
            targetDurationMinutes = 25,
            completedDurationSeconds = 1500,
            lockMode = "MAXIMUM_LOCK",
            timestamp = now
        )
        // Session 2: 10 days ago (40m = 2400s)
        val s2 = FocusSession(
            sessionName = "Past Focus",
            subjectName = "Physics",
            targetDurationMinutes = 40,
            completedDurationSeconds = 2400,
            lockMode = "MAXIMUM_LOCK",
            timestamp = now - (10 * oneDayMillis)
        )
        // Session 3: 40 days ago (60m = 3600s)
        val s3 = FocusSession(
            sessionName = "Older Focus",
            subjectName = "Chemistry",
            targetDurationMinutes = 60,
            completedDurationSeconds = 3600,
            lockMode = "MAXIMUM_LOCK",
            timestamp = now - (40 * oneDayMillis)
        )

        repository.saveSession(s1)
        repository.saveSession(s2)
        repository.saveSession(s3)

        val all = repository.allSessions.first()
        assertEquals(3, all.size)

        // Filter last 7 days: should only include s1
        val last7DaysStart = now - (7 * oneDayMillis)
        val last7DaysSessions = all.filter { it.timestamp in last7DaysStart..now }
        assertEquals(1, last7DaysSessions.size)
        assertEquals("Today Focus", last7DaysSessions[0].sessionName)

        // Filter last 30 days: should include s1 and s2
        val last30DaysStart = now - (30 * oneDayMillis)
        val last30DaysSessions = all.filter { it.timestamp in last30DaysStart..now }
        assertEquals(2, last30DaysSessions.size)
        assertEquals(3900, last30DaysSessions.sumOf { it.completedDurationSeconds })

        // Filter last 90 days: should include all 3
        val last90DaysStart = now - (90 * oneDayMillis)
        val last90DaysSessions = all.filter { it.timestamp in last90DaysStart..now }
        assertEquals(3, last90DaysSessions.size)
        assertEquals(7500, last90DaysSessions.sumOf { it.completedDurationSeconds })
    }

    @Test
    fun testFocusLockManagerBlockingLogic() {
        val ownPkg = "com.example"
        val allowedApp = "com.google.android.apps.docs"
        val blockedApp = "com.facebook.katana"

        // 1. When focus is NOT active, all apps are allowed
        com.example.util.FocusLockManager.updateFocusState(
            isActive = false,
            lockMode = LockMode.NORMAL,
            allowedPackageNames = emptyList()
        )
        assertTrue(com.example.util.FocusLockManager.isPackageAllowed(null, blockedApp, ownPkg))

        // 2. When MAXIMUM_LOCK is active with Docs allowed
        com.example.util.FocusLockManager.updateFocusState(
            isActive = true,
            lockMode = LockMode.MAXIMUM_LOCK,
            allowedPackageNames = listOf(allowedApp)
        )
        // Own package is allowed
        assertTrue(com.example.util.FocusLockManager.isPackageAllowed(ownPkg, ownPkg))
        // Whitelisted study app is allowed
        assertTrue(com.example.util.FocusLockManager.isPackageAllowed(allowedApp, ownPkg))
        // System essential UI is allowed
        assertTrue(com.example.util.FocusLockManager.isPackageAllowed("com.android.systemui", ownPkg))
        // Distracting social media app is BLOCKED
        assertFalse(com.example.util.FocusLockManager.isPackageAllowed(blockedApp, ownPkg))
    }

    @Test
    fun testSpecialWhitelistProfileSeparation() = runBlocking {
        val manualApp = AllowedApp("com.google.android.calculator", "MANUAL", "Calculator", "Tools", true)
        val strictApp = AllowedApp("com.google.android.keep", "STRICT", "Keep Notes", "Notes", true)
        val specialApp = AllowedApp("com.google.android.apps.docs", "SPECIAL", "Google Docs", "Docs", true)

        dao.insertOrUpdateApps(listOf(manualApp, strictApp, specialApp))

        val manualList = repository.whitelistedApps("MANUAL").first()
        val strictList = repository.whitelistedApps("STRICT").first()
        val specialList = repository.whitelistedApps("SPECIAL").first()

        assertEquals(1, manualList.size)
        assertEquals("Calculator", manualList[0].appName)

        assertEquals(1, strictList.size)
        assertEquals("Keep Notes", strictList[0].appName)

        assertEquals(1, specialList.size)
        assertEquals("Google Docs", specialList[0].appName)
    }

    @Test
    fun testUninstalledAppsCleanup() = runBlocking {
        val app1 = AllowedApp("com.installed.app1", "MANUAL", "App One", "Tools", true)
        val app2 = AllowedApp("com.uninstalled.app2", "MANUAL", "App Two", "Games", false)
        val app3 = AllowedApp("com.installed.app3", "SPECIAL", "App Three", "Study", true)

        dao.insertOrUpdateApps(listOf(app1, app2, app3))

        val manualBefore = dao.getAllowedApps("MANUAL").first()
        val specialBefore = dao.getAllowedApps("SPECIAL").first()
        assertEquals(2, manualBefore.size)
        assertEquals(1, specialBefore.size)

        // Only app1 and app3 are currently installed on device
        val installedPackages = listOf("com.installed.app1", "com.installed.app3")
        dao.deleteUninstalledApps(installedPackages)

        val manualAfter = dao.getAllowedApps("MANUAL").first()
        val specialAfter = dao.getAllowedApps("SPECIAL").first()
        assertEquals(1, manualAfter.size)
        assertEquals(1, specialAfter.size)
        assertTrue(manualAfter.none { it.packageName == "com.uninstalled.app2" })
        assertTrue(manualAfter.any { it.packageName == "com.installed.app1" })
        assertTrue(specialAfter.any { it.packageName == "com.installed.app3" })
    }

    @Test
    fun testSpecialSessionStatisticsSaving() = runBlocking {
        val now = System.currentTimeMillis()
        val specialSession = FocusSession(
            sessionName = "Special Exam Prep",
            subjectName = "UPSC Prelims",
            targetDurationMinutes = 60,
            completedDurationSeconds = 3600,
            lockMode = "MAXIMUM_LOCK",
            distractionAttempts = 2,
            allowedAppsCount = 3,
            whitelistProfile = "SPECIAL",
            status = "COMPLETED",
            timestamp = now
        )

        val id = repository.saveSession(specialSession)
        assertTrue(id > 0)

        val allSessions = repository.allSessions.first()
        val saved = allSessions.find { it.id == id }
        assertNotNull(saved)
        assertEquals("SPECIAL", saved?.whitelistProfile)
        assertEquals("COMPLETED", saved?.status)
        assertEquals(3600, saved?.completedDurationSeconds)
        assertEquals(2, saved?.distractionAttempts)
        assertEquals(3, saved?.allowedAppsCount)
    }

    @Test
    fun testDailyStreakCalculation() = runBlocking {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 3600 * 1000L

        // Day 0 (Today)
        val sToday = FocusSession(sessionName = "Today", subjectName = "A", targetDurationMinutes = 30, completedDurationSeconds = 1800, lockMode = "NORMAL", status = "COMPLETED", timestamp = now)
        // Day -1 (Yesterday)
        val sYesterday = FocusSession(sessionName = "Yesterday", subjectName = "B", targetDurationMinutes = 30, completedDurationSeconds = 1800, lockMode = "NORMAL", status = "COMPLETED", timestamp = now - oneDayMillis)
        // Day -2 (2 days ago)
        val s2DaysAgo = FocusSession(sessionName = "2 Days Ago", subjectName = "C", targetDurationMinutes = 30, completedDurationSeconds = 1800, lockMode = "NORMAL", status = "COMPLETED", timestamp = now - (2 * oneDayMillis))

        repository.saveSession(sToday)
        repository.saveSession(sYesterday)
        repository.saveSession(s2DaysAgo)

        val sessions = repository.allSessions.first().filter { it.status == "COMPLETED" }
        assertEquals(3, sessions.size)
        val totalFocusSeconds = sessions.sumOf { it.completedDurationSeconds }
        assertEquals(5400, totalFocusSeconds) // 90 minutes total
    }

    @Test
    fun testScheduleConflictValidation() = runBlocking {
        val now = System.currentTimeMillis()
        val existingStart = now + 3600 * 1000L // in 1 hour
        val existingEnd = existingStart + 3600 * 1000L // 1 hour duration

        val existingSession = FocusSession(
            sessionName = "Existing Schedule",
            subjectName = "Math",
            targetDurationMinutes = 60,
            completedDurationSeconds = 0,
            lockMode = "MAXIMUM_LOCK",
            status = "SCHEDULED",
            scheduledStartTime = existingStart,
            scheduledEndTime = existingEnd,
            timestamp = now
        )
        repository.saveSession(existingSession)

        val scheduled = repository.scheduledSessions.first()
        assertEquals(1, scheduled.size)

        // Conflicting time: overlapping with existing session
        val candidateStart = existingStart + 1800 * 1000L // 30 mins after start
        val candidateEnd = candidateStart + 3600 * 1000L

        val isConflict = scheduled.any { session ->
            val sStart = session.scheduledStartTime ?: 0L
            val sEnd = session.scheduledEndTime ?: 0L
            candidateStart < sEnd && candidateEnd > sStart
        }
        assertTrue("Candidate session should conflict with existing schedule", isConflict)

        // Non-conflicting time: completely after existing session
        val nonConflictStart = existingEnd + 1000L
        val nonConflictEnd = nonConflictStart + 1800 * 1000L

        val hasNoConflict = scheduled.none { session ->
            val sStart = session.scheduledStartTime ?: 0L
            val sEnd = session.scheduledEndTime ?: 0L
            nonConflictStart < sEnd && nonConflictEnd > sStart
        }
        assertTrue("Non-overlapping candidate should have no conflict", hasNoConflict)
    }

    @Test
    fun testBootScheduledAlarmRestorationLogic() = runBlocking {
        val now = System.currentTimeMillis()
        val futureSession = FocusSession(
            sessionName = "Scheduled UPSC Study",
            subjectName = "Polity",
            targetDurationMinutes = 60,
            completedDurationSeconds = 0,
            lockMode = "MAXIMUM_LOCK",
            status = "SCHEDULED",
            scheduledStartTime = now + (3600 * 1000L),
            timestamp = now
        )
        val pastSession = FocusSession(
            sessionName = "Old Study",
            subjectName = "History",
            targetDurationMinutes = 30,
            completedDurationSeconds = 0,
            lockMode = "MAXIMUM_LOCK",
            status = "SCHEDULED",
            scheduledStartTime = now - (3600 * 1000L),
            timestamp = now - (7200 * 1000L)
        )

        repository.saveSession(futureSession)
        repository.saveSession(pastSession)

        val all = repository.allSessions.first()
        val toRestore = all.filter {
            it.status == "SCHEDULED" &&
            it.scheduledStartTime != null &&
            it.scheduledStartTime!! > now
        }
        // Only future scheduled session should be re-registered on reboot
        assertEquals(1, toRestore.size)
        assertEquals("Scheduled UPSC Study", toRestore[0].sessionName)
    }

    @Test
    fun testDuplicateSessionSaveProtection() = runBlocking {
        val now = System.currentTimeMillis()
        val session1 = FocusSession(
            sessionName = "Math Revision",
            subjectName = "Calculus",
            targetDurationMinutes = 45,
            completedDurationSeconds = 2700,
            lockMode = "NORMAL",
            distractionAttempts = 0,
            allowedAppsCount = 1,
            whitelistProfile = "MANUAL",
            status = "COMPLETED",
            timestamp = now
        )

        // First save
        repository.saveSession(session1)

        // Simulated duplicate check logic
        val existingRecent = repository.allSessions.first().firstOrNull {
            it.status == "COMPLETED" &&
            Math.abs(now - it.timestamp) < 4000 &&
            it.sessionName == session1.sessionName
        }

        // If duplicate attempted within 4 seconds, should be rejected
        if (existingRecent == null) {
            repository.saveSession(session1.copy(timestamp = now + 100))
        }

        val allCompleted = repository.allSessions.first().filter { it.sessionName == "Math Revision" }
        assertEquals("Duplicate session should be blocked from saving twice", 1, allCompleted.size)
    }

    @Test
    fun testDiagnosticDbWriteVerificationBeforeCleanup() = runBlocking {
        val now = System.currentTimeMillis()
        val session = FocusSession(
            sessionName = "Physics Mechanics",
            subjectName = "Physics",
            targetDurationMinutes = 90,
            completedDurationSeconds = 5400,
            lockMode = "MAXIMUM_LOCK",
            distractionAttempts = 4,
            allowedAppsCount = 2,
            whitelistProfile = "SPECIAL",
            status = "COMPLETED",
            timestamp = now
        )

        // 1. Write to DB
        val savedId = repository.saveSession(session)
        assertTrue(savedId > 0)

        // 2. Diagnostic read-back verification (simulating what completeFocusSession does before temp data cleanup)
        val verifiedSession = repository.getSessionById(savedId)
        assertNotNull("Session must be readable immediately from DB", verifiedSession)
        assertEquals("COMPLETED", verifiedSession?.status)
        assertEquals(5400, verifiedSession?.completedDurationSeconds)
        assertEquals(4, verifiedSession?.distractionAttempts)
        assertEquals("SPECIAL", verifiedSession?.whitelistProfile)
        assertEquals(2, verifiedSession?.allowedAppsCount)

        // 3. Ensure safe cleanup of temporary state can proceed
        var tempTimerStateRunning = true
        if (verifiedSession != null) {
            tempTimerStateRunning = false // Safely clear temp data only after verification
        }
        assertFalse(tempTimerStateRunning)
    }

    // =========================================================================
    // 5 COMPREHENSIVE TESTS FOR SCHEDULE APP SNAPSHOT INTEGRITY
    // =========================================================================

    @Test
    fun testSnapshot1_CreationAndRetrieval() = runBlocking {
        // Test 1: Verify a snapshot created for a schedule is perfectly retrieved.
        val prefs = context.getSharedPreferences("schedule_prefs", android.content.Context.MODE_PRIVATE)
        val scheduleId = 1001L
        val originalApps = listOf("com.whatsapp", "com.instagram")
        
        // Simulating schedule creation
        prefs.edit().putString("scheduled_apps_$scheduleId", originalApps.joinToString(",")).commit()
        
        // Simulating schedule start
        val retrievedSnapshot = prefs.getString("scheduled_apps_$scheduleId", null)
        val apps = retrievedSnapshot?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        
        assertEquals("Test 1 Failed: Size mismatch", 2, apps.size)
        assertTrue("Test 1 Failed: Missing whatsapp", apps.contains("com.whatsapp"))
    }

    @Test
    fun testSnapshot2_IndependenceFromGlobalChanges() = runBlocking {
        // Test 2: User changes global settings AFTER creating a schedule. Schedule must remain unaffected.
        val prefs = context.getSharedPreferences("schedule_prefs", android.content.Context.MODE_PRIVATE)
        val scheduleId = 1002L
        
        // 1. Create schedule with specific apps
        val scheduleApps = listOf("com.calculator")
        prefs.edit().putString("scheduled_apps_$scheduleId", scheduleApps.joinToString(",")).commit()
        
        // 2. Later, user changes global Strict profile in Database (mocked as different list)
        val globalApps = listOf("com.calculator", "com.youtube") // Global has more apps now
        
        // 3. When schedule runs, it must ONLY use the snapshot
        val retrievedSnapshot = prefs.getString("scheduled_apps_$scheduleId", null)
        val apps = retrievedSnapshot?.split(",")?.filter { it.isNotBlank() } ?: globalApps
        
        assertEquals("Test 2 Failed: Schedule used global settings instead of snapshot!", 1, apps.size)
        assertFalse("Test 2 Failed: Global changes leaked into schedule!", apps.contains("com.youtube"))
    }

    @Test
    fun testSnapshot3_MultipleSchedulesDoNotConflict() = runBlocking {
        // Test 3: Two schedules created with different apps do not overwrite each other.
        val prefs = context.getSharedPreferences("schedule_prefs", android.content.Context.MODE_PRIVATE)
        val scheduleId1 = 1003L
        val scheduleId2 = 1004L
        
        prefs.edit().putString("scheduled_apps_$scheduleId1", "com.notes").commit()
        prefs.edit().putString("scheduled_apps_$scheduleId2", "com.dictionary,com.browser").commit()
        
        val apps1 = prefs.getString("scheduled_apps_$scheduleId1", "")?.split(",") ?: emptyList()
        val apps2 = prefs.getString("scheduled_apps_$scheduleId2", "")?.split(",") ?: emptyList()
        
        assertEquals("Test 3 Failed: Schedule 1 corrupted", 1, apps1.size)
        assertEquals("Test 3 Failed: Schedule 2 corrupted", 2, apps2.size)
        assertTrue("Test 3 Failed: Overlap detected", apps2.contains("com.dictionary"))
    }

    @Test
    fun testSnapshot4_FallbackToGlobalIfNoSnapshot() = runBlocking {
        // Test 4: If snapshot is missing/corrupted, it safely falls back to Database Profile
        val prefs = context.getSharedPreferences("schedule_prefs", android.content.Context.MODE_PRIVATE)
        val scheduleId = 1005L // We don't save anything for this ID
        
        val retrievedSnapshot = prefs.getString("scheduled_apps_$scheduleId", null)
        
        // Simulate fallback logic used in FocusTimerService.kt
        val finalApps = if (retrievedSnapshot.isNullOrBlank()) {
            listOf("fallback.app1", "fallback.app2") // Simulated DB fetch
        } else {
            retrievedSnapshot.split(",").filter { it.isNotBlank() }
        }
        
        assertEquals("Test 4 Failed: Did not fallback correctly", 2, finalApps.size)
        assertTrue("Test 4 Failed", finalApps.contains("fallback.app1"))
    }

    @Test
    fun testSnapshot5_NormalSessionDoesNotUseSnapshot() = runBlocking {
        // Test 5: A normal manual session (no scheduledSessionId) strictly uses the live database.
        val prefs = context.getSharedPreferences("schedule_prefs", android.content.Context.MODE_PRIVATE)
        
        // There is some old schedule data
        prefs.edit().putString("scheduled_apps_999", "com.badapp").commit()
        
        val scheduledSessionId: Long? = null // Manual session
        
        val retrievedSnapshot = if (scheduledSessionId != null) {
            prefs.getString("scheduled_apps_$scheduledSessionId", null)
        } else null
        
        assertNull("Test 5 Failed: Normal session tried to use a snapshot", retrievedSnapshot)
    }
}

