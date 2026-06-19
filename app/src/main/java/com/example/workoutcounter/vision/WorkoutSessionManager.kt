package com.example.workoutcounter.vision

import com.example.workoutcounter.domain.model.ExerciseType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime

/**
 * Manages automatic workout tracking using vision
 * Handles exercise detection, set detection, rep counting, and rest periods
 */
class WorkoutSessionManager {
    
    private val exerciseClassifier = ExerciseClassifier()
    private var currentRepCounter: RepetitionCounter? = null
    private var currentExerciseType: ExerciseType? = null
    
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Idle)
    val sessionState: StateFlow<SessionState> = _sessionState

    private val _currentExercise = MutableStateFlow<ExerciseType?>(null)
    val currentExercise: StateFlow<ExerciseType?> = _currentExercise

    private val _repCount = MutableStateFlow(0)
    val repCount: StateFlow<Int> = _repCount

    private val _setCount = MutableStateFlow(0)
    val setCount: StateFlow<Int> = _setCount

    private val _restTimeRemaining = MutableStateFlow(0L)
    val restTimeRemaining: StateFlow<Long> = _restTimeRemaining

    // State tracking
    private var consecutiveRestFrames = 0
    private var consecutiveActiveFrames = 0
    private val framesNeededForStateChange = 5
    private var setStartTime: LocalDateTime? = null
    private var lastExerciseTime: LocalDateTime? = null
    private var isResting = false

    sealed class SessionState {
        object Idle : SessionState()
        object Detecting : SessionState()
        object SetInProgress : SessionState()
        object Resting : SessionState()
        object SetCompleted : SessionState()
    }

    /**
     * Process frame from camera
     * Returns true if any significant event occurred (rep, set complete, etc.)
     */
    fun processFrame(landmarks: List<PoseDetector.PoseLandmark>): Boolean {
        if (landmarks.size < 33) return false

        // Detect current exercise
        val detectedExercise = exerciseClassifier.classifyExercise(landmarks)
        
        // Check if person is standing (resting)
        val isStanding = exerciseClassifier.isStanding(landmarks)

        return handleStateTransition(detectedExercise, isStanding, landmarks)
    }

    private fun handleStateTransition(
        detectedExercise: ExerciseType?,
        isStanding: Boolean,
        landmarks: List<PoseDetector.PoseLandmark>
    ): Boolean {
        var eventOccurred = false

        when (_sessionState.value) {
            SessionState.Idle -> {
                if (detectedExercise != null && !isStanding) {
                    startSet(detectedExercise)
                    eventOccurred = true
                }
            }

            SessionState.Detecting -> {
                if (detectedExercise != null && !isStanding) {
                    startSet(detectedExercise)
                    eventOccurred = true
                }
            }

            SessionState.SetInProgress -> {
                if (detectedExercise == currentExerciseType && !isStanding) {
                    // Continue counting reps
                    val repCompleted = currentRepCounter?.processFrame(landmarks) ?: false
                    if (repCompleted) {
                        _repCount.value = currentRepCounter?.getRepCount() ?: 0
                        eventOccurred = true
                    }
                } else if (isStanding && detectedExercise == null) {
                    // Person stood up - set ended
                    endSet()
                    eventOccurred = true
                } else if (detectedExercise != null && detectedExercise != currentExerciseType) {
                    // Different exercise detected
                    endSet()
                    startSet(detectedExercise)
                    eventOccurred = true
                }
            }

            SessionState.Resting -> {
                if (detectedExercise != null && !isStanding) {
                    // Start new set
                    startSet(detectedExercise)
                    eventOccurred = true
                } else if (isStanding) {
                    // Still resting
                    updateRestTime()
                }
            }

            SessionState.SetCompleted -> {
                if (detectedExercise != null && !isStanding) {
                    startSet(detectedExercise)
                    eventOccurred = true
                } else if (isStanding) {
                    _sessionState.value = SessionState.Resting
                }
            }
        }

        lastExerciseTime = LocalDateTime.now()
        return eventOccurred
    }

    private fun startSet(exerciseType: ExerciseType) {
        _sessionState.value = SessionState.SetInProgress
        currentExerciseType = exerciseType
        _currentExercise.value = exerciseType
        _repCount.value = 0
        
        currentRepCounter = RepetitionCounter(exerciseType)
        setStartTime = LocalDateTime.now()
        isResting = false
        
        _setCount.value = (_setCount.value ?: 0) + 1
    }

    private fun endSet() {
        _sessionState.value = SessionState.SetCompleted
        isResting = true
        
        // Initialize rest timer (typical 60 seconds rest)
        _restTimeRemaining.value = 60000 // 60 seconds in ms
    }

    private fun updateRestTime() {
        val remaining = _restTimeRemaining.value
        if (remaining > 0) {
            _restTimeRemaining.value = remaining - 100 // Decrement by frame interval
        } else {
            _sessionState.value = SessionState.Idle
            isResting = false
        }
    }

    fun startSession() {
        _sessionState.value = SessionState.Detecting
        _setCount.value = 0
        _repCount.value = 0
        currentExerciseType = null
        _currentExercise.value = null
    }

    fun endSession() {
        _sessionState.value = SessionState.Idle
        currentRepCounter = null
        currentExerciseType = null
        _currentExercise.value = null
    }

    fun getCurrentSetInfo(): SetInfo? {
        return if (_sessionState.value == SessionState.SetInProgress) {
            SetInfo(
                exerciseType = currentExerciseType,
                repCount = _repCount.value,
                setNumber = _setCount.value,
                durationMs = if (setStartTime != null) {
                    java.time.Duration.between(setStartTime, LocalDateTime.now()).toMillis()
                } else 0
            )
        } else null
    }

    data class SetInfo(
        val exerciseType: ExerciseType?,
        val repCount: Int,
        val setNumber: Int,
        val durationMs: Long
    )
}
