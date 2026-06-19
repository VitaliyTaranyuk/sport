package com.example.workoutcounter.vision

import com.example.workoutcounter.domain.model.ExerciseType
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Counts repetitions based on pose landmarks
 * Detects the full motion cycle for each exercise type
 */
class RepetitionCounter(
    private val exerciseType: ExerciseType
) {
    
    private var repCount = 0
    private var inMotion = false
    private var motionPhase = MotionPhase.REST // Current phase of the motion
    private var lastLandmarks: List<PoseDetector.PoseLandmark>? = null
    private var consecutiveFramesInPhase = 0
    private val framesNeededForPhaseChange = 3 // Need 3 frames to confirm phase change

    enum class MotionPhase {
        REST,           // Neutral position
        ASCENDING,      // Going up (for push-ups, pull-ups)
        DESCENDING,     // Going down
        TOP_POSITION,   // At top of motion
        BOTTOM_POSITION // At bottom of motion
    }

    /**
     * Process landmarks and update rep count
     * Returns true if a new rep was completed
     */
    fun processFrame(landmarks: List<PoseDetector.PoseLandmark>): Boolean {
        if (lastLandmarks == null) {
            lastLandmarks = landmarks
            return false
        }

        val newPhase = detectMotionPhase(landmarks, lastLandmarks!!)
        
        if (newPhase != motionPhase) {
            consecutiveFramesInPhase++
            
            // Only change phase after seeing it consistently
            if (consecutiveFramesInPhase >= framesNeededForPhaseChange) {
                val repCompleted = handlePhaseChange(motionPhase, newPhase)
                motionPhase = newPhase
                consecutiveFramesInPhase = 0
                lastLandmarks = landmarks
                
                if (repCompleted) {
                    repCount++
                    return true
                }
            }
        } else {
            consecutiveFramesInPhase = 0
        }

        lastLandmarks = landmarks
        return false
    }

    private fun detectMotionPhase(
        current: List<PoseDetector.PoseLandmark>,
        previous: List<PoseDetector.PoseLandmark>
    ): MotionPhase {
        return when (exerciseType) {
            ExerciseType.PUSH_UPS -> detectPushUpPhase(current, previous)
            ExerciseType.PULL_UPS -> detectPullUpPhase(current, previous)
            ExerciseType.SQUATS -> detectSquatPhase(current, previous)
            ExerciseType.AB_EXERCISES -> detectAbPhase(current, previous)
        }
    }

    private fun detectPushUpPhase(
        current: List<PoseDetector.PoseLandmark>,
        previous: List<PoseDetector.PoseLandmark>
    ): MotionPhase {
        val currentElbowBend = calculateElbowBend(current, isLeft = true)
        val previousElbowBend = calculateElbowBend(previous, isLeft = true)
        
        return when {
            currentElbowBend < 60f -> MotionPhase.BOTTOM_POSITION // Arms fully bent
            currentElbowBend > 140f -> MotionPhase.TOP_POSITION // Arms extended
            currentElbowBend > previousElbowBend -> MotionPhase.ASCENDING
            else -> MotionPhase.DESCENDING
        }
    }

    private fun detectPullUpPhase(
        current: List<PoseDetector.PoseLandmark>,
        previous: List<PoseDetector.PoseLandmark>
    ): MotionPhase {
        val currentWristHeight = current[9].y // Left wrist
        val previousWristHeight = previous[9].y
        val shoulderHeight = current[11].y // Left shoulder
        
        return when {
            currentWristHeight < shoulderHeight - 0.2f -> MotionPhase.TOP_POSITION
            currentWristHeight > shoulderHeight + 0.1f -> MotionPhase.BOTTOM_POSITION
            currentWristHeight < previousWristHeight -> MotionPhase.ASCENDING
            else -> MotionPhase.DESCENDING
        }
    }

    private fun detectSquatPhase(
        current: List<PoseDetector.PoseLandmark>,
        previous: List<PoseDetector.PoseLandmark>
    ): MotionPhase {
        val currentKneeBend = calculateKneeBend(current, isLeft = true)
        val previousKneeBend = calculateKneeBend(previous, isLeft = true)
        
        return when {
            currentKneeBend < 90f -> MotionPhase.BOTTOM_POSITION // Fully squatted
            currentKneeBend > 160f -> MotionPhase.TOP_POSITION // Fully extended
            currentKneeBend < previousKneeBend -> MotionPhase.DESCENDING
            else -> MotionPhase.ASCENDING
        }
    }

    private fun detectAbPhase(
        current: List<PoseDetector.PoseLandmark>,
        previous: List<PoseDetector.PoseLandmark>
    ): MotionPhase {
        // For ab exercises, track distance between chest and hip
        val currentDistance = calculateChestHipDistance(current)
        val previousDistance = calculateChestHipDistance(previous)
        
        return when {
            currentDistance < 0.2f -> MotionPhase.TOP_POSITION // Crunched
            currentDistance > 0.4f -> MotionPhase.BOTTOM_POSITION // Extended
            currentDistance < previousDistance -> MotionPhase.ASCENDING
            else -> MotionPhase.DESCENDING
        }
    }

    private fun handlePhaseChange(from: MotionPhase, to: MotionPhase): Boolean {
        // A rep is completed when we go from top → bottom → top
        return (from == MotionPhase.TOP_POSITION && to == MotionPhase.BOTTOM_POSITION) ||
               (from == MotionPhase.BOTTOM_POSITION && to == MotionPhase.TOP_POSITION)
    }

    private fun calculateElbowBend(landmarks: List<PoseDetector.PoseLandmark>, isLeft: Boolean): Float {
        val shoulder = if (isLeft) landmarks[11] else landmarks[12]
        val elbow = if (isLeft) landmarks[7] else landmarks[8]
        val wrist = if (isLeft) landmarks[9] else landmarks[10]
        
        return calculateAngle(shoulder, elbow, wrist)
    }

    private fun calculateKneeBend(landmarks: List<PoseDetector.PoseLandmark>, isLeft: Boolean): Float {
        val hip = if (isLeft) landmarks[23] else landmarks[24]
        val knee = if (isLeft) landmarks[25] else landmarks[26]
        val ankle = if (isLeft) landmarks[27] else landmarks[28]
        
        return calculateAngle(hip, knee, ankle)
    }

    private fun calculateChestHipDistance(landmarks: List<PoseDetector.PoseLandmark>): Float {
        val chest = landmarks[0] // nose (approximate chest)
        val hip = landmarks[23]
        
        return sqrt((chest.x - hip.x) * (chest.x - hip.x) + (chest.y - hip.y) * (chest.y - hip.y))
    }

    private fun calculateAngle(
        p1: PoseDetector.PoseLandmark,
        p2: PoseDetector.PoseLandmark,
        p3: PoseDetector.PoseLandmark
    ): Float {
        val vec1 = Triple(p1.x - p2.x, p1.y - p2.y, p1.z - p2.z)
        val vec2 = Triple(p3.x - p2.x, p3.y - p2.y, p3.z - p2.z)

        val dotProduct = vec1.first * vec2.first + vec1.second * vec2.second + vec1.third * vec2.third
        val mag1 = sqrt(vec1.first * vec1.first + vec1.second * vec1.second + vec1.third * vec1.third)
        val mag2 = sqrt(vec2.first * vec2.first + vec2.second * vec2.second + vec2.third * vec2.third)

        if (mag1 == 0f || mag2 == 0f) return 0f

        val cosAngle = (dotProduct / (mag1 * mag2)).coerceIn(-1f, 1f)
        return Math.toDegrees(Math.acos(cosAngle.toDouble())).toFloat()
    }

    fun getRepCount(): Int = repCount
    
    fun reset() {
        repCount = 0
        motionPhase = MotionPhase.REST
        lastLandmarks = null
        inMotion = false
        consecutiveFramesInPhase = 0
    }
}
