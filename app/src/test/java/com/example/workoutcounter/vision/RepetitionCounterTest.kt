package com.example.workoutcounter.vision

import com.example.workoutcounter.domain.model.ExerciseType
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class RepetitionCounterTest {
    
    private lateinit var repCounter: RepetitionCounter

    @Before
    fun setUp() {
        repCounter = RepetitionCounter(ExerciseType.PUSH_UPS)
    }

    @Test
    fun testInitialRepCountIsZero() {
        assertEquals(0, repCounter.getRepCount())
    }

    @Test
    fun testDetectsSingleRepForPushUps() {
        val restLandmarks = createPushUpRestLandmarks()
        val activeLandmarks = createPushUpActiveLandmarks()
        
        // Process frames to simulate a full rep cycle
        // Rest -> Active -> Rest (represents 1 complete rep)
        repeat(3) { repCounter.processFrame(restLandmarks) }
        repeat(3) { repCounter.processFrame(activeLandmarks) }
        repeat(3) { repCounter.processFrame(restLandmarks) }
        
        assertEquals(1, repCounter.getRepCount())
    }

    @Test
    fun testDetectsMultipleReps() {
        val restLandmarks = createPushUpRestLandmarks()
        val activeLandmarks = createPushUpActiveLandmarks()
        
        // Simulate 3 complete reps
        repeat(3) {
            repeat(3) { repCounter.processFrame(restLandmarks) }
            repeat(3) { repCounter.processFrame(activeLandmarks) }
            repeat(3) { repCounter.processFrame(restLandmarks) }
        }
        
        assertEquals(3, repCounter.getRepCount())
    }

    @Test
    fun testResetClearsRepCount() {
        repCounter.reset()
        
        val activeLandmarks = createPushUpActiveLandmarks()
        repeat(10) { repCounter.processFrame(activeLandmarks) }
        
        assertEquals(0, repCounter.getRepCount())
    }

    @Test
    fun testSquatRepCounting() {
        val squatCounter = RepetitionCounter(ExerciseType.SQUATS)
        val restLandmarks = createSquatRestLandmarks()
        val activeLandmarks = createSquatActiveLandmarks()
        
        // Process frames to simulate squat reps
        repeat(3) { squatCounter.processFrame(restLandmarks) }
        repeat(3) { squatCounter.processFrame(activeLandmarks) }
        repeat(3) { squatCounter.processFrame(restLandmarks) }
        
        assertEquals(1, squatCounter.getRepCount())
    }

    @Test
    fun testPullUpRepCounting() {
        val pullUpCounter = RepetitionCounter(ExerciseType.PULL_UPS)
        val restLandmarks = createPullUpRestLandmarks()
        val activeLandmarks = createPullUpActiveLandmarks()
        
        repeat(3) { pullUpCounter.processFrame(restLandmarks) }
        repeat(3) { pullUpCounter.processFrame(activeLandmarks) }
        repeat(3) { pullUpCounter.processFrame(restLandmarks) }
        
        assertEquals(1, pullUpCounter.getRepCount())
    }

    @Test
    fun testAbExerciseRepCounting() {
        val abCounter = RepetitionCounter(ExerciseType.AB_EXERCISES)
        val restLandmarks = createAbRestLandmarks()
        val activeLandmarks = createAbActiveLandmarks()
        
        repeat(3) { abCounter.processFrame(restLandmarks) }
        repeat(3) { abCounter.processFrame(activeLandmarks) }
        repeat(3) { abCounter.processFrame(restLandmarks) }
        
        assertEquals(1, abCounter.getRepCount())
    }

    // Helper methods
    private fun createPushUpRestLandmarks(): List<PoseDetector.PoseLandmark> {
        return List(33) { idx ->
            when (idx) {
                7, 8 -> PoseDetector.PoseLandmark(0.5f, 0.3f, 0f, 0.9f) // Elbows extended
                else -> PoseDetector.PoseLandmark(0.5f, 0.5f, 0f, 0.5f)
            }
        }
    }

    private fun createPushUpActiveLandmarks(): List<PoseDetector.PoseLandmark> {
        return List(33) { idx ->
            when (idx) {
                7, 8 -> PoseDetector.PoseLandmark(0.5f, 0.25f, 0f, 0.9f) // Elbows bent
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

    private fun createPullUpRestLandmarks(): List<PoseDetector.PoseLandmark> {
        return List(33) { idx ->
            when (idx) {
                9, 10 -> PoseDetector.PoseLandmark(0.5f, 0.7f, 0f, 0.9f)
                11, 12 -> PoseDetector.PoseLandmark(0.5f, 0.5f, 0f, 0.9f)
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

    private fun createAbRestLandmarks(): List<PoseDetector.PoseLandmark> {
        return List(33) { idx ->
            when (idx) {
                0 -> PoseDetector.PoseLandmark(0.5f, 0.3f, 0f, 0.9f)
                23, 24 -> PoseDetector.PoseLandmark(0.5f, 0.6f, 0f, 0.9f)
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
