package com.example.workoutcounter.di

import android.content.Context
import androidx.room.Room
import com.example.workoutcounter.data.local.db.AppDatabase
import com.example.workoutcounter.data.repository.ExerciseRepositoryImpl
import com.example.workoutcounter.domain.repository.ExerciseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "workout.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides @Singleton
    fun provideExerciseRepository(db: AppDatabase): ExerciseRepository =
        ExerciseRepositoryImpl(db.exerciseDao())
}
