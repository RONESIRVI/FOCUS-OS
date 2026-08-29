package com.example

import android.content.Intent
import com.example.services.FocusTimerService
import com.example.data.model.LockMode
import com.example.services.SoundType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@RunWith(RobolectricTestRunner::class)
class PenaltySystemTest {

    @org.junit.Ignore("Robolectric context issues with startTimer")
    @Test
    fun testPenaltyTimeAddition() = runBlocking {
        // Start the service
        val service = Robolectric.buildService(FocusTimerService::class.java).create().get()

        // Start a 10-minute session (600 seconds)
        service.startTimer(
            durationMinutes = 10,
            sessionName = "Test",
            subjectName = "Test Sub",
            lockMode = LockMode.MAXIMUM_LOCK,
            soundType = SoundType.NONE,
            requiresSelfie = false,
            isScheduled = false,
            isSpecialSession = true
        )

        val initialState = service.timerState.value
        assertEquals("Initial total should be 600 seconds", 600, initialState.totalSeconds)
        assertEquals("Initial remaining should be 600 seconds", 600, initialState.remainingSeconds)

        // Add penalty of 420 seconds (7 minutes)
        service.addExtraTime(420)

        val updatedState = service.timerState.value
        assertEquals("Total should be increased by 420 seconds", 600 + 420, updatedState.totalSeconds)
        assertEquals("Remaining should be increased by 420 seconds", 600 + 420, updatedState.remainingSeconds)
    }
}
