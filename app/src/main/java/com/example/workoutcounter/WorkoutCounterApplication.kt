package com.example.workoutcounter

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WorkoutCounterApplication : Application() {
    // No custom logic for now – Hilt initialization only
}
