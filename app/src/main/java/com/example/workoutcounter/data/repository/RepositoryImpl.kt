package com.example.workoutcounter.data.repository

import com.example.workoutcounter.data.local.db.AppDatabase
import com.example.workoutcounter.data.local.db.ExerciseEntity
import com.example.workoutcounter.data.local.db.SetEntity
import com.example.workoutcounter.data.local.db.WorkoutEntity
import com.example.workoutcounter.domain.model.Exercise
import com.example.workoutcounter.domain.model.ExerciseType
import com.example.workoutcounter.domain.repository.ExerciseRepository
import com.example.workoutcounter.domain.repository.RepRepository
import com.example.workoutcounter.domain.repository.SetRepository
import com.example.workoutcounter.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

class ExerciseRepositoryImpl(
    private val db: AppDatabase
) : ExerciseRepository {
    override fun getAll(): Flow<List<Exercise>> = 
        db.exerciseDao().getAll().map { entities ->
            entities.map { it.toDomainModel() }
        }

    override fun getByType(type: ExerciseType): Flow<List<Exercise>> {
        return getAll().map { exercises ->
            exercises.filter { it.type == type }
        }
    }

    override suspend fun insert(exercise: Exercise) {
        db.exerciseDao().insertAll(exercise.toEntity())
    }

    private fun ExerciseEntity.toDomainModel() = Exercise(
        id = id,
        name = name,
        type = ExerciseType.PULL_UPS, // Determine from name or iconRes
        iconRes = iconRes
    )

    private fun Exercise.toEntity() = ExerciseEntity(
        id = id,
        name = name,
        iconRes = iconRes
    )
}

class WorkoutRepositoryImpl(
    private val db: AppDatabase
) : WorkoutRepository {
    override suspend fun startWorkout() {
        val workout = WorkoutEntity(
            startTime = LocalDateTime.now()
        )
        db.workoutDao().insert(workout)
    }

    override suspend fun endWorkout() {
        val activeWorkout = db.workoutDao().getActiveWorkout()
        activeWorkout?.let {
            db.workoutDao().update(
                it.copy(endTime = LocalDateTime.now())
            )
        }
    }

    override fun getActiveWorkout(): Flow<Int?> = 
        kotlinx.coroutines.flow.flow {
            val workout = db.workoutDao().getActiveWorkout()
            emit(workout?.id)
        }
}

class SetRepositoryImpl(
    private val db: AppDatabase
) : SetRepository {
    override suspend fun addSet(exerciseType: ExerciseType, repCount: Int): Int {
        val workoutId = db.workoutDao().getActiveWorkout()?.id ?: return -1
        
        // Get exercise by type
        val exercises = db.exerciseDao().getAll()
        
        val set = SetEntity(
            workoutId = workoutId,
            exerciseId = 1, // Should be obtained from exercise selection
            setNumber = 1,
            repCount = repCount,
            isAutomatic = true
        )
        return db.setDao().insert(set).toInt()
    }

    override suspend fun endSet(setId: Int, restDuration: Long) {
        // Implementation for ending a set
    }
}

class RepRepositoryImpl(
    private val db: AppDatabase
) : RepRepository {
    override suspend fun addRep(setId: Int, repNumber: Int) {
        val rep = com.example.workoutcounter.data.local.db.RepEntity(
            setId = setId,
            repNumber = repNumber,
            timestamp = LocalDateTime.now()
        )
        db.repDao().insert(rep)
    }

    override suspend fun getRepCount(setId: Int): Int {
        return db.repDao().getRepCount(setId)
    }
}
