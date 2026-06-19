package com.example.workoutcounter.domain.model

import java.time.LocalDateTime

enum class ExerciseType {
    PULL_UPS,
    PUSH_UPS,
    SQUATS,
    AB_EXERCISES
}

data class Exercise(
    val id: Int,
    val name: String,
    val type: ExerciseType,
    val iconRes: Int
)

data class Workout(
    val id: Int,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val sets: List<Set> = emptyList(),
    val totalSets: Int = 0,
    val totalReps: Int = 0
)

data class Set(
    val id: Int,
    val workoutId: Int,
    val exercise: Exercise,
    val setNumber: Int,
    val repCount: Int = 0,
    val duration: Long = 0,
    val restDuration: Long = 0,
    val isAutomatic: Boolean = true,
    val reps: List<Rep> = emptyList()
)

data class Rep(
    val id: Int,
    val setId: Int,
    val repNumber: Int,
    val timestamp: LocalDateTime
)

// Statistics model
data class WorkoutStatistics(
    val totalWorkouts: Int,
    val totalSets: Int,
    val totalReps: Int,
    val averageRepsPerSet: Double,
    val favoriteExercise: ExerciseType? = null
)
