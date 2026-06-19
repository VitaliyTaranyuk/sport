package com.example.workoutcounter.vision.error

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Handles errors and edge cases in workout tracking
 */
class ErrorHandler {
    
    private val _errorState = MutableStateFlow<WorkoutError?>(null)
    val errorState: StateFlow<WorkoutError?> = _errorState

    sealed class WorkoutError {
        object CameraNotAvailable : WorkoutError()
        object CameraAccessDenied : WorkoutError()
        object InvalidPoseDetection : WorkoutError()
        object LowConfidencePose : WorkoutError()
        data class ExerciseDetectionFailed(val reason: String) : WorkoutError()
        data class ProcessingError(val message: String) : WorkoutError()
    }

    /**
     * Handle camera initialization error
     */
    fun handleCameraError(exception: Exception) {
        val error = when {
            exception.message?.contains("permission", ignoreCase = true) == true -> 
                WorkoutError.CameraAccessDenied
            exception.message?.contains("camera", ignoreCase = true) == true -> 
                WorkoutError.CameraNotAvailable
            else -> WorkoutError.ProcessingError(exception.message ?: "Unknown error")
        }
        _errorState.value = error
    }

    /**
     * Handle pose detection error
     */
    fun handlePoseDetectionError(confidence: Float) {
        if (confidence < 0.5f) {
            _errorState.value = WorkoutError.InvalidPoseDetection
        }
    }

    /**
     * Handle low confidence in pose
     */
    fun handleLowConfidencePose(confidence: Float) {
        if (confidence < 0.7f) {
            _errorState.value = WorkoutError.LowConfidencePose
        }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _errorState.value = null
    }

    /**
     * Get user-friendly error message
     */
    fun getErrorMessage(error: WorkoutError): String {
        return when (error) {
            is WorkoutError.CameraNotAvailable -> 
                "Камера недоступна. Пожалуйста, проверьте, что устройство имеет камеру."
            is WorkoutError.CameraAccessDenied -> 
                "Доступ к камере запрещен. Пожалуйста, разрешите доступ в настройках."
            is WorkoutError.InvalidPoseDetection -> 
                "Не удается определить позу. Пожалуйста, убедитесь, что вы хорошо видны в камере."
            is WorkoutError.LowConfidencePose -> 
                "Низкая точность определения позы. Пожалуйста, посмотрите в камеру."
            is WorkoutError.ExerciseDetectionFailed -> 
                "Не удается определить упражнение: ${error.reason}"
            is WorkoutError.ProcessingError -> 
                "Ошибка обработки: ${error.message}"
        }
    }
}
