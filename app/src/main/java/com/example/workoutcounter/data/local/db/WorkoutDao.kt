package com.example.workoutcounter.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout ORDER BY startTime DESC")
    fun getAll(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workout WHERE id = :id")
    suspend fun getById(id: Int): WorkoutEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workout: WorkoutEntity): Long

    @Update
    suspend fun update(workout: WorkoutEntity)

    @Query("SELECT * FROM workout WHERE endTime IS NULL LIMIT 1")
    suspend fun getActiveWorkout(): WorkoutEntity?
}

@Dao
interface SetDao {
    @Query("SELECT * FROM set_data WHERE workoutId = :workoutId ORDER BY setNumber")
    fun getByWorkoutId(workoutId: Int): Flow<List<SetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(set: SetEntity): Long

    @Update
    suspend fun update(set: SetEntity)

    @Query("DELETE FROM set_data WHERE id = :id")
    suspend fun delete(id: Int)
}

@Dao
interface RepDao {
    @Query("SELECT * FROM rep WHERE setId = :setId ORDER BY repNumber")
    fun getBySetId(setId: Int): Flow<List<RepEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rep: RepEntity): Long

    @Query("SELECT COUNT(*) FROM rep WHERE setId = :setId")
    suspend fun getRepCount(setId: Int): Int
}
