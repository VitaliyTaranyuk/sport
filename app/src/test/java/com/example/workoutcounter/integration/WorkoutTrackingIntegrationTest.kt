package com.example.workoutcounter.integration

import com.example.workoutcounter.data.local.db.AppDatabase
import com.example.workoutcounter.data.local.db.ExerciseEntity
import com.example.workoutcounter.data.repository.ExerciseRepositoryImpl
import com.example.workoutcounter.data.repository.WorkoutRepositoryImpl
import com.example.workoutcounter.data.repository.SetRepositoryImpl
import com.example.workoutcounter.data.repository.RepRepositoryImpl
import com.example.workoutcounter.domain.model.ExerciseType
import com.example.workoutcounter.vision.ExerciseClassifier
import com.example.workoutcounter.vision.PoseDetector
import com.example.workoutcounter.vision.RepetitionCounter
import com.example.workoutcounter.vision.WorkoutSessionManager
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Integration tests for workout tracking workflow
 */
class WorkoutTrackingIntegrationTest {
    
    private lateinit var sessionManager: WorkoutSessionManager
    private lateinit var classifier: ExerciseClassifier
    
    @Before
    fun setUp() {
        sessionManager = WorkoutSessionManager()
        classifier = ExerciseClassifier()
    }

    @Test
    fun testCompleteWorkoutSession() {
        // Start session
        sessionManager.startSession()
        assertEquals(WorkoutSessionManager.SessionState.Detecting, sessionManager.sessionState.value)
        
        // Simulate exercise detection and rep counting
        val pushUpLandmarks = createPushUpSequence()
        pushUpLandmarks.forEach { landmarks ->
            sessionManager.processFrame(landmarks)
        }
        
        // Verify sets were created
        assertTrue(sessionManager.setCount.value > 0)
        
        // End session
        sessionManager.endSession()
        assertEquals(WorkoutSessionManager.SessionState.Idle, sessionManager.sessionState.value)
    }

    @Test
    fun testMultipleExercisesInSession() {
        sessionManager.startSession()
        
        // Push-ups sequence
        val pushUpLandmarks = createPushUpSequence()
        pushUpLandmarks.forEach { landmarks ->
            sessionManager.processFrame(landmarks)
        }
        val pushUpSets = sessionManager.setCount.value
        
        // Standing sequence
        val standingLandmarks = List(5) { createStandingLandmarks() }
        standingLandmarks.forEach { landmarks ->
            sessionManager.processFrame(landmarks)
        }
        
        // Squats sequence
        val squatLandmarks = createSquatSequence()
        squatLandmarks.forEach { landmarks ->
            sessionManager.processFrame(landmarks)
        }
        
        // Verify multiple exercises detected
        assertTrue(sessionManager.setCount.value > pushUpSets)
    }

    @Test
    fun testRestPeriodDetection() {
        sessionManager.startSession()
        
        val activeLandmarks = createPushUpSequence()
        activeLandmarks.forEach { sessionManager.processFrame(it) }
        
        val standingLandmarks = List(5) { createStandingLandmarks() }
        standingLandmarks.forEach { sessionManager.processFrame(it) }
        
        // Verify session transitioned to Resting state
        assertTrue(
            sessionManager.sessionState.value == WorkoutSessionManager.SessionState.SetCompleted ||
            sessionManager.sessionState.value == WorkoutSessionManager.SessionState.Resting
        )
    }

    @Test
    fun testExerciseClassificationAccuracy() {
        val pushUpLandmarks = createPushUpActiveLandmarks()
        val squatLandmarks = createSquatActiveLandmarks()
        val pullUpLandmarks = createPullUpActiveLandmarks()
        val abLandmarks = createAbActiveLandmarks()
        
        assertEquals(ExerciseType.PUSH_UPS, classifier.classifyExercise(pushUpLandmarks))
        assertEquals(ExerciseType.SQUATS, classifier.classifyExercise(squatLandmarks))
        assertEquals(ExerciseType.PULL_UPS, classifier.classifyExercise(pullUpLandmarks))
        assertEquals(ExerciseType.AB_EXERCISES, classifier.classifyExercise(abLandmarks))
    }

    @Test
    fun testRepCountingAccuracy() {
        val repCounter = RepetitionCounter(ExerciseType.PUSH_UPS)
        
        // Simulate 5 complete push-ups
        repeat(5) {
            val restLandmarks = createPushUpRestLandmarks()
            val activeLandmarks = createPushUpActiveLandmarks()
            
            repeat(3) { repCounter.processFrame(restLandmarks) }
            repeat(3) { repCounter.processFrame(activeLandmarks) }
            repeat(3) { repCounter.processFrame(restLandmarks) }
        }
        
        assertEquals(5, repCounter.getRepCount())
    }

    // Helper methods
    private fun createPushUpSequence(): List<List<PoseDetector.PoseLandmark>> {
        return List(15) {
            if (it % 3 == 0) createPushUpRestLandmarks()
            else createPushUpActiveLandmarks()
        }
    }

    private fun createSquatSequence(): List<List<PoseDetector.PoseLandmark>> {
        return List(15) {
            if (it % 3 == 0) createSquatRestLandmarks()
            else createSquatActiveLandmarks()
        }
    }

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

    private fun createSquatRestLandmarks(): List<PoseDetector.PoseLandmark> {
        return List(33) { idx ->
            when (idx) {
                23, 24 -> PoseDetector.PoseLandmark(0.5f, 0.4f, 0f, 0.9f)
                25, 26 -> PoseDetector.PoseLandmark(0.5f, 0.35f, 0f, 0.9f)
                27, 28 -> PoseDetector.PoseLandmark(0.5f, 0.3f, 0f, 0.9f)
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

    private fun createPullUpActiveLandmarks(): List<PoseDetector.PoseLandmark> {
        return List(33) { idx ->
            when (idx) {
                9, 10 -> PoseDetector.PoseLandmark(0.5f, 0.2f, 0f, 0.9f)
                11, 12 -> PoseDetector.PoseLandmark(0.5f, 0.5f, 0f, 0.9f)
                else -> PoseDetector.PoseLandmark(0.5f, 0.5f, 0f, 0.5f)
            }
        }
    }

    private fun createAbActiveLandmarks(): List<PoseDetector.PoseLandmark> {
        return List(33) { idx ->
            when (idx) {
                0 -> PoseDetector.PoseLandmark(0.5f, 0.5f, 0f, 0.9f)
                23, 24 -> PoseDetector.PoseLandmark(0.5f, 0.6f, 0f, 0.9f)
                else -> PoseDetector.PoseLandmark(0.5f, 0.5f, 0f, 0.5f)
            }
        }
    }
}
