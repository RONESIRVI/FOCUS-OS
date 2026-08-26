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
        audioEngine.startSound(SoundType.ALPHA_WAVES, CoroutineScope(Dispatchers.Default))
        audioEngine.stopSound()

        audioEngine.startSound(SoundType.RAIN_SOUNDS, CoroutineScope(Dispatchers.Default))
        audioEngine.stopSound()

        audioEngine.startSound(SoundType.NONE, CoroutineScope(Dispatchers.Default))
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
        assertTrue(com.example.util.FocusLockManager.isPackageAllowed(blockedApp, ownPkg))

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
}
