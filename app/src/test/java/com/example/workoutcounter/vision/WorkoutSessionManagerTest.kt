package com.example.workoutcounter.vision

import com.example.workoutcounter.domain.model.ExerciseType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class WorkoutSessionManagerTest {
    
    private lateinit var manager: WorkoutSessionManager

    @Before
    fun setUp() {
        manager = WorkoutSessionManager()
    }

    @Test
    fun testSessionStartsInIdleState() {
        assertEquals(WorkoutSessionManager.SessionState.Idle, manager.sessionState.value)
    }

    @Test
    fun testStartSessionChangesState() = runTest {
        manager.startSession()
        assertEquals(WorkoutSessionManager.SessionState.Detecting, manager.sessionState.value)
    }

    @Test
    fun testEndSessionReturnsToIdle() = runTest {
        manager.startSession()
        manager.endSession()
        assertEquals(WorkoutSessionManager.SessionState.Idle, manager.sessionState.value)
    }

    @Test
    fun testSetCountIncrementsOnSetStart() = runTest {
        manager.startSession()
        assertEquals(0, manager.setCount.first())
        
        // Process landmarks representing exercise
        val activeLandmarks = createPushUpActiveLandmarks()
        manager.processFrame(activeLandmarks)
        
        assertEquals(1, manager.setCount.value)
    }

    @Test
    fun testExerciseTypeDetectedCorrectly() = runTest {
        manager.startSession()
        
        val pushUpLandmarks = createPushUpActiveLandmarks()
        manager.processFrame(pushUpLandmarks)
        
        assertEquals(ExerciseType.PUSH_UPS, manager.currentExercise.first())
    }

    @Test
    fun testRepCountIncrementsOnRepCompletion() = runTest {
        manager.startSession()
        
        val restLandmarks = createPushUpRestLandmarks()
        val activeLandmarks = createPushUpActiveLandmarks()
        
        // Start set
        repeat(3) { manager.processFrame(activeLandmarks) }
        assertEquals(1, manager.setCount.value)
        
        // Simulate rep cycle
        repeat(3) { manager.processFrame(restLandmarks) }
        repeat(3) { manager.processFrame(activeLandmarks) }
        
        assertTrue(manager.repCount.value > 0)
    }

    @Test
    fun testSetEndsWhenPersonStandsUp() = runTest {
        manager.startSession()
        
        val activeLandmarks = createPushUpActiveLandmarks()
        val standingLandmarks = createStandingLandmarks()
        
        // Start set
        repeat(3) { manager.processFrame(activeLandmarks) }
        assertEquals(WorkoutSessionManager.SessionState.SetInProgress, manager.sessionState.value)
        
        // Person stands up
        repeat(5) { manager.processFrame(standingLandmarks) }
        
        assertEquals(WorkoutSessionManager.SessionState.SetCompleted, manager.sessionState.value)
    }

    @Test
    fun testNewSetStartsWithDifferentExercise() = runTest {
        manager.startSession()
        
        val pushUpLandmarks = createPushUpActiveLandmarks()
        val squatLandmarks = createSquatActiveLandmarks()
        
        // Start push-up set
        repeat(3) { manager.processFrame(pushUpLandmarks) }
        assertEquals(ExerciseType.PUSH_UPS, manager.currentExercise.value)
        assertEquals(1, manager.setCount.value)
        
        // Switch to squats
        repeat(5) { manager.processFrame(squatLandmarks) }
        
        assertEquals(ExerciseType.SQUATS, manager.currentExercise.value)
        assertEquals(2, manager.setCount.value)
    }

    @Test
    fun testGetCurrentSetInfo() = runTest {
        manager.startSession()
        
        val activeLandmarks = createPushUpActiveLandmarks()
        repeat(3) { manager.processFrame(activeLandmarks) }
        
        val setInfo = manager.getCurrentSetInfo()
        assertNotNull(setInfo)
        assertEquals(ExerciseType.PUSH_UPS, setInfo?.exerciseType)
        assertEquals(1, setInfo?.setNumber)
    }

    @Test
    fun testRestTimerStartsAfterSetEnd() = runTest {
        manager.startSession()
        
        val activeLandmarks = createPushUpActiveLandmarks()
        val standingLandmarks = createStandingLandmarks()
        
        repeat(3) { manager.processFrame(activeLandmarks) }
        repeat(5) { manager.processFrame(standingLandmarks) }
        
        assertEquals(WorkoutSessionManager.SessionState.SetCompleted, manager.sessionState.value)
        assertTrue(manager.restTimeRemaining.value > 0)
    }

    // Helper methods
    private fun createPushUpActiveLandmarks(): List<PoseDetector.PoseLandmark> {
        return List(33) { idx ->
            when (idx) {
                0 -> PoseDetector.PoseLandmark(0.5f, 0.3f, 0f, 0.9f)
                7, 8 -> PoseDetector.PoseLandmark(0.4f, 0.25f, 0f, 0.9f)
                9, 10 -> PoseDetector.PoseLandmark(0.4f, 0.4f, 0f, 0.9f)
                23, 24 -> PoseDetector.PoseLandmark(0.5f, 0.35f, 0f, 0.9f)
                else -> PoseDetector.PoseLandmark(0.5f, 0.35f, 0f, 0.5f)
            }
        }
    }

    private fun createPushUpRestLandmarks(): List<PoseDetector.PoseLandmark> {
        return List(33) { idx ->
            when (idx) {
                0 -> PoseDetector.PoseLandmark(0.5f, 0.3f, 0f, 0.9f)
                7, 8 -> PoseDetector.PoseLandmark(0.5f, 0.3f, 0f, 0.9f)
                9, 10 -> PoseDetector.PoseLandmark(0.4f, 0.4f, 0f, 0.9f)
                23, 24 -> PoseDetector.PoseLandmark(0.5f, 0.35f, 0f, 0.9f)
                else -> PoseDetector.PoseLandmark(0.5f, 0.35f, 0f, 0.5f)
            }
        }
    }

    private fun createSquatActiveLandmarks(): List<PoseDetector.PoseLandmark> {
        return List(33) { idx ->
            when (idx) {
                23, 24 -> PoseDetector.PoseLandmark(0.5f, 0.6f, 0f, 0.9f)
                25, 26 -> PoseDetector.PoseLandmark(0.5f, 0.7f, 0f, 0.9f)
                27, 28 -> PoseDetector.PoseLandmark(0.5f, 0.8f, 0f, 0.9f)
                else -> PoseDetector.PoseLandmark(0.5f, 0.5f, 0f, 0.5f)
            }
        }
    }

    private fun createStandingLandmarks(): List<PoseDetector.PoseLandmark> {
        return List(33) { idx ->
            when (idx) {
                23, 24 -> PoseDetector.PoseLandmark(0.5f, 0.6f, 0f, 0.9f)
                27, 28 -> PoseDetector.PoseLandmark(0.5f, 0.9f, 0f, 0.9f)
                else -> PoseDetector.PoseLandmark(0.5f, 0.5f, 0f, 0.5f)
            }
        }
    }
}
