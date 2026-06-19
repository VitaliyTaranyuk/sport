package com.example.workoutcounter.vision

import com.example.workoutcounter.domain.model.ExerciseType
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Classifies exercise type based on pose landmarks
 * MediaPipe Pose provides 33 landmarks
 */
class ExerciseClassifier {
    
    /**
     * Classify exercise type from pose landmarks
     * Returns null if exercise cannot be determined
     */
    fun classifyExercise(landmarks: List<PoseDetector.PoseLandmark>): ExerciseType? {
        if (landmarks.size < 33) return null

        return when {
            isPullUpsPose(landmarks) -> ExerciseType.PULL_UPS
            isPushUpsPose(landmarks) -> ExerciseType.PUSH_UPS
            isSquatsPose(landmarks) -> ExerciseType.SQUATS
            isAbExercisesPose(landmarks) -> ExerciseType.AB_EXERCISES
            else -> null
        }
    }

    private fun isPullUpsPose(landmarks: List<PoseDetector.PoseLandmark>): Boolean {
        // Pull-ups: Arms above head, body vertical
        val leftShoulder = landmarks[11] // Left shoulder
        val rightShoulder = landmarks[12] // Right shoulder
        val leftWrist = landmarks[9] // Left wrist
        val rightWrist = landmarks[10] // Right wrist
        val leftHip = landmarks[23] // Left hip
        val rightHip = landmarks[24] // Right hip

        // Check if wrists are above shoulders (arms raised)
        val wristsAboveShoulder = leftWrist.y < leftShoulder.y && rightWrist.y < rightShoulder.y
        
        // Check if body is relatively vertical
        val bodyVertical = abs(leftHip.x - rightHip.x) < 0.15f
        
        return wristsAboveShoulder && bodyVertical
    }

    private fun isPushUpsPose(landmarks: List<PoseDetector.PoseLandmark>): Boolean {
        // Push-ups: Body horizontal or tilted, arms bent
        val nose = landmarks[0]
        val leftHip = landmarks[23]
        val rightHip = landmarks[24]
        val leftWrist = landmarks[9]
        val rightWrist = landmarks[10]
        val leftElbow = landmarks[7]
        val rightElbow = landmarks[8]

        // Check if body is horizontal (nose and hips at similar height level)
        val bodyHorizontal = abs(nose.y - (leftHip.y + rightHip.y) / 2) < 0.3f
        
        // Check if wrists are roughly under nose/chest
        val wristsUnderBody = leftWrist.y > nose.y && rightWrist.y > nose.y
        
        return bodyHorizontal && wristsUnderBody
    }

    private fun isSquatsPose(landmarks: List<PoseDetector.PoseLandmark>): Boolean {
        // Squats: Knees bent, back relatively straight
        val leftHip = landmarks[23]
        val rightHip = landmarks[24]
        val leftKnee = landmarks[25]
        val rightKnee = landmarks[26]
        val leftAnkle = landmarks[27]
        val rightAnkle = landmarks[28]

        // Calculate knee bend angle
        val leftKneeBend = calculateAngle(leftHip, leftKnee, leftAnkle)
        val rightKneeBend = calculateAngle(rightHip, rightKnee, rightAnkle)

        // In squat position, knees should be significantly bent (< 120 degrees)
        return leftKneeBend < 120f && rightKneeBend < 120f
    }

    private fun isAbExercisesPose(landmarks: List<PoseDetector.PoseLandmark>): Boolean {
        // Ab exercises: Lying on back, or doing crunches
        val nose = landmarks[0]
        val leftShoulder = landmarks[11]
        val rightShoulder = landmarks[12]
        val leftHip = landmarks[23]
        val rightHip = landmarks[24]

        // Check if body is mostly horizontal (lying down)
        val bodyHorizontal = abs(nose.y - (leftHip.y + rightHip.y) / 2) > 0.3f
        
        // Check if shoulders are visible and low relative to hips
        val shouldersBelowHips = (leftShoulder.y + rightShoulder.y) / 2 > (leftHip.y + rightHip.y) / 2
        
        return bodyHorizontal || shouldersBelowHips
    }

    /**
     * Calculate angle between three points
     * Used for determining body position angles
     */
    private fun calculateAngle(p1: PoseDetector.PoseLandmark, p2: PoseDetector.PoseLandmark, p3: PoseDetector.PoseLandmark): Float {
        val vec1 = Triple(p1.x - p2.x, p1.y - p2.y, p1.z - p2.z)
        val vec2 = Triple(p3.x - p2.x, p3.y - p2.y, p3.z - p2.z)

        val dotProduct = vec1.first * vec2.first + vec1.second * vec2.second + vec1.third * vec2.third
        val mag1 = sqrt(vec1.first * vec1.first + vec1.second * vec1.second + vec1.third * vec1.third)
        val mag2 = sqrt(vec2.first * vec2.first + vec2.second * vec2.second + vec2.third * vec2.third)

        if (mag1 == 0f || mag2 == 0f) return 0f

        val cosAngle = dotProduct / (mag1 * mag2)
        val clampedCosAngle = cosAngle.coerceIn(-1f, 1f)
        val angle = Math.toDegrees(Math.acos(clampedCosAngle.toDouble())).toFloat()

        return angle
    }

    /**
     * Check if person is standing upright (not actively exercising)
     */
    fun isStanding(landmarks: List<PoseDetector.PoseLandmark>): Boolean {
        if (landmarks.size < 33) return false

        val nose = landmarks[0]
        val leftHip = landmarks[23]
        val rightHip = landmarks[24]
        val leftAnkle = landmarks[27]
        val rightAnkle = landmarks[28]

        // Check if body is vertical
        val bodyVertical = abs((leftHip.x + rightHip.x) / 2 - (leftAnkle.x + rightAnkle.x) / 2) < 0.1f
        
        // Check if ankles are visible and below hips
        val legsExtended = leftAnkle.y > leftHip.y && rightAnkle.y > rightHip.y
        
        return bodyVertical && legsExtended
    }
}
