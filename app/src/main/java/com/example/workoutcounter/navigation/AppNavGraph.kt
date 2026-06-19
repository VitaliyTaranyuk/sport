package com.example.workoutcounter.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.workoutcounter.ui.home.HomeScreen
import com.example.workoutcounter.ui.training.SelectExerciseScreen
import com.example.workoutcounter.ui.training.CameraScreen
import com.example.workoutcounter.ui.plan.PlanScreen
import com.example.workoutcounter.ui.history.HistoryScreen
import com.example.workoutcounter.ui.statistics.StatisticsScreen
import com.example.workoutcounter.ui.settings.SettingsScreen

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
