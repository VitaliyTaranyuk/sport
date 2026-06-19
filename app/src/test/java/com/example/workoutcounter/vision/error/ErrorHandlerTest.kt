package com.example.workoutcounter.vision.error

import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class ErrorHandlerTest {
    
    private lateinit var errorHandler: ErrorHandler

    @Before
    fun setUp() {
        errorHandler = ErrorHandler()
    }

    @Test
    fun testInitialStateIsNull() {
        assertNull(errorHandler.errorState.value)
    }

    @Test
    fun testHandleCameraAccessDeniedError() {
        val exception = Exception("permission denied for camera")
        errorHandler.handleCameraError(exception)
        
        assertEquals(ErrorHandler.WorkoutError.CameraAccessDenied, errorHandler.errorState.value)
    }

    @Test
    fun testHandleCameraNotAvailableError() {
        val exception = Exception("camera not found")
        errorHandler.handleCameraError(exception)
        
        assertEquals(ErrorHandler.WorkoutError.CameraNotAvailable, errorHandler.errorState.value)
    }

    @Test
    fun testHandleInvalidPoseDetection() {
        errorHandler.handlePoseDetectionError(0.3f)
        
        assertEquals(ErrorHandler.WorkoutError.InvalidPoseDetection, errorHandler.errorState.value)
    }

    @Test
    fun testHandleLowConfidencePose() {
        errorHandler.handleLowConfidencePose(0.6f)
        
        assertEquals(ErrorHandler.WorkoutError.LowConfidencePose, errorHandler.errorState.value)
    }

    @Test
    fun testAcceptHighConfidencePose() {
        errorHandler.handleLowConfidencePose(0.8f)
        
        assertNull(errorHandler.errorState.value)
    }

    @Test
    fun testClearError() {
        errorHandler.handleCameraError(Exception("camera error"))
        assertNotNull(errorHandler.errorState.value)
        
        errorHandler.clearError()
        assertNull(errorHandler.errorState.value)
    }

    @Test
    fun testGetErrorMessage() {
        val error = ErrorHandler.WorkoutError.CameraAccessDenied
        val message = errorHandler.getErrorMessage(error)
        
        assertTrue(message.contains("Доступ"))
    }
}
