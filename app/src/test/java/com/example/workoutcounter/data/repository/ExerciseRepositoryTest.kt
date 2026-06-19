package com.example.workoutcounter.data.repository

import com.example.workoutcounter.data.local.db.AppDatabase
import com.example.workoutcounter.data.local.db.ExerciseDao
import com.example.workoutcounter.data.local.db.ExerciseEntity
import com.example.workoutcounter.domain.model.ExerciseType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class ExerciseRepositoryTest {
    
    private lateinit var repository: ExerciseRepositoryImpl
    private val mockDb: AppDatabase = mockk()
    private val mockDao: ExerciseDao = mockk()

    @Before
    fun setUp() {
        coEvery { mockDb.exerciseDao() } returns mockDao
        repository = ExerciseRepositoryImpl(mockDb)
    }

    @Test
    fun testGetAllExercises() = runTest {
        val exercises = listOf(
            ExerciseEntity(1, "Push-ups", 0),
            ExerciseEntity(2, "Pull-ups", 1)
        )
        coEvery { mockDao.getAll() } returns flowOf(exercises)
        
        val result = repository.getAll()
        
        assertNotNull(result)
    }

    @Test
    fun testInsertExercise() = runTest {
        val exercise = com.example.workoutcounter.domain.model.Exercise(
            id = 1,
            name = "Push-ups",
            type = ExerciseType.PUSH_UPS,
            iconRes = 0
        )
        coEvery { mockDao.insertAll(any()) } returns Unit
        
        repository.insert(exercise)
        
        coVerify { mockDao.insertAll(any()) }
    }
}
