package com.example.workoutcounter.vision

import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerOptions
import android.content.Context
import android.graphics.Bitmap

/**
 * Handles pose detection using MediaPipe Pose Landmarker
 */
class PoseDetector(context: Context) {
    private var poseLandmarker: PoseLandmarker? = null
    
    init {
        // Initialize pose landmarker
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("pose_landmarker_full.bundle")
            .build()

        val options = PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.IMAGE)
            .build()

        poseLandmarker = PoseLandmarker.createFromOptions(context, options)
    }

    fun detectPose(bitmap: Bitmap): PoseDetectionResult? {
        return try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            val result = poseLandmarker?.detect(mpImage)
            
            if (result?.landmarks()?.isNotEmpty() == true) {
                val landmarks = result.landmarks()[0]
                PoseDetectionResult(
                    landmarks = landmarks.map { landmark ->
                        PoseLandmark(
                            x = landmark.x(),
                            y = landmark.y(),
                            z = landmark.z(),
                            confidence = landmark.presence()
                        )
                    }
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun close() {
        poseLandmarker?.close()
    }

    data class PoseDetectionResult(
        val landmarks: List<PoseLandmark>
    )

    data class PoseLandmark(
        val x: Float,
        val y: Float,
        val z: Float,
        val confidence: Float
    )
}
