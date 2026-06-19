package com.example.workoutcounter.vision

import com.example.workoutcounter.domain.model.ExerciseType
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class ExerciseClassifierTest {
    
    private lateinit var classifier: ExerciseClassifier

    @Before
    fun setUp() {
        classifier = ExerciseClassifier()
    }

    @Test
    fun testClassifyPullUpsPose() {
        // Create mock landmarks for pull-ups pose
        val landmarks = createPullUpLandmarks()
        val result = classifier.classifyExercise(landmarks)
        
        assertEquals(ExerciseType.PULL_UPS, result)
    }

    @Test
    fun testClassifyPushUpsPose() {
        val landmarks = createPushUpLandmarks()
        val result = classifier.classifyExercise(landmarks)
        
        assertEquals(ExerciseType.PUSH_UPS, result)
    }

    @Test
    fun testClassifySquatsPose() {
        val landmarks = createSquatLandmarks()
        val result = classifier.classifyExercise(landmarks)
        
        assertEquals(ExerciseType.SQUATS, result)
    }

    @Test
    fun testClassifyAbExercisesPose() {
        val landmarks = createAbExerciseLandmarks()
        val result = classifier.classifyExercise(landmarks)
        
        assertEquals(ExerciseType.AB_EXERCISES, result)
    }

    @Test
    fun testIsStandingPosition() {
        val landmarks = createStandingLandmarks()
        val result = classifier.isStanding(landmarks)
        
        assertTrue(result)
    }

    @Test
    fun testIsNotStandingWhenExercising() {
        val landmarks = createSquatLandmarks()
        val result = classifier.isStanding(landmarks)
        
        assertFalse(result)
    }

    @Test
    fun testInsufficientLandmarksReturnsNull() {
        val landmarks = List(10) { 
            PoseDetector.PoseLandmark(0f, 0f, 0f, 0.5f) 
        }
        val result = classifier.classifyExercise(landmarks)
        
        assertNull(result)
    }

    // Helper methods to create mock landmarks
    private fun createPullUpLandmarks(): List<PoseDetector.PoseLandmark> {
        return List(33) { idx ->
            when (idx) {
                9, 10 -> PoseDetector.PoseLandmark(0.5f, 0.1f, 0f, 0.9f) // Wrists above shoulders
                11, 12 -> PoseDetector.PoseLandmark(0.5f, 0.3f, 0f, 0.9f) // Shoulders
                23, 24 -> PoseDetector.PoseLandmark(0.5f, 0.6f, 0f, 0.9f) // Hips
                else -> PoseDetector.PoseLandmark(0.5f, 0.5f, 0f, 0.5f)
            }
        }
    }

    private fun createPushUpLandmarks(): List<PoseDetector.PoseLandmark> {
        return List(33) { idx ->
            when (idx) {
                0 -> PoseDetector.PoseLandmark(0.5f, 0.3f, 0f, 0.9f) // Nose
                9, 10 -> PoseDetector.PoseLandmark(0.4f, 0.4f, 0f, 0.9f) // Wrists below chest
                23, 24 -> PoseDetector.PoseLandmark(0.5f, 0.35f, 0f, 0.9f) // Hips at similar height
                else -> PoseDetector.PoseLandmark(0.5f, 0.35f, 0f, 0.5f)
            }
        }
    }

    private fun createSquatLandmarks(): List<PoseDetector.PoseLandmark> {
        return List(33) { idx ->
            when (idx) {
                23, 24 -> PoseDetector.PoseLandmark(0.5f, 0.4f, 0f, 0.9f) // Hips
                25, 26 -> PoseDetector.PoseLandmark(0.5f, 0.65f, 0f, 0.9f) // Knees (bent)
                27, 28 -> PoseDetector.PoseLandmark(0.5f, 0.8f, 0f, 0.9f) // Ankles
                else -> PoseDetector.PoseLandmark(0.5f, 0.5f, 0f, 0.5f)
            }
        }
    }

    private fun createAbExerciseLandmarks(): List<PoseDetector.PoseLandmark> {
        return List(33) { idx ->
            when (idx) {
                0 -> PoseDetector.PoseLandmark(0.5f, 0.7f, 0f, 0.9f) // Nose low
                11, 12 -> PoseDetector.PoseLandmark(0.5f, 0.6f, 0f, 0.9f) // Shoulders
                23, 24 -> PoseDetector.PoseLandmark(0.5f, 0.3f, 0f, 0.9f) // Hips above shoulders
                else -> PoseDetector.PoseLandmark(0.5f, 0.5f, 0f, 0.5f)
            }
        }
    }

    private fun createStandingLandmarks(): List<PoseDetector.PoseLandmark> {
        return List(33) { idx ->
            when (idx) {
                23, 24 -> PoseDetector.PoseLandmark(0.5f, 0.6f, 0f, 0.9f) // Hips
                27, 28 -> PoseDetector.PoseLandmark(0.5f, 0.9f, 0f, 0.9f) // Ankles below hips
                else -> PoseDetector.PoseLandmark(0.5f, 0.5f, 0f, 0.5f)
            }
        }
    }
}
