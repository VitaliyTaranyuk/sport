package com.example.workoutcounter.domain.repository

import com.example.workoutcounter.domain.model.Exercise
import com.example.workoutcounter.domain.model.ExerciseType
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun getAll(): Flow<List<Exercise>>
    fun getByType(type: ExerciseType): Flow<List<Exercise>>
    suspend fun insert(exercise: Exercise)
}

interface WorkoutRepository {
    suspend fun startWorkout()
    suspend fun endWorkout()
    fun getActiveWorkout(): Flow<Int?> // returns workoutId
}

interface SetRepository {
    suspend fun addSet(exerciseType: ExerciseType, repCount: Int): Int // returns setId
    suspend fun endSet(setId: Int, restDuration: Long)
}

interface RepRepository {
    suspend fun addRep(setId: Int, repNumber: Int)
    suspend fun getRepCount(setId: Int): Int
}
