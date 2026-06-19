package com.example.workoutcounter.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.workoutcounter.ui.screens.HomeScreen
import com.example.workoutcounter.ui.screens.SelectExerciseScreen
import com.example.workoutcounter.ui.screens.CameraScreen
import com.example.workoutcounter.ui.screens.PlanScreen
import com.example.workoutcounter.ui.screens.HistoryScreen
import com.example.workoutcounter.ui.screens.StatisticsScreen
import com.example.workoutcounter.ui.screens.SettingsScreen

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("training") { SelectExerciseScreen(navController) }
        composable("camera") { CameraScreen(navController) }
        composable("plans") { PlanScreen(navController) }
        composable("history") { HistoryScreen(navController) }
        composable("statistics") { StatisticsScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
    }
}
