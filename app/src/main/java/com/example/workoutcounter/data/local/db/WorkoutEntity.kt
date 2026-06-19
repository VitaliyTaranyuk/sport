package com.example.workoutcounter.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "workout")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val totalSets: Int = 0,
    val totalReps: Int = 0
)

@Entity(tableName = "set_data")
data class SetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workoutId: Int,
    val exerciseId: Int,
    val setNumber: Int,
    val repCount: Int = 0,
    val duration: Long = 0, // in milliseconds
    val restDuration: Long = 0, // rest time after set
    val isAutomatic: Boolean = true
)

@Entity(tableName = "rep")
data class RepEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val setId: Int,
    val repNumber: Int,
    val timestamp: LocalDateTime
)
