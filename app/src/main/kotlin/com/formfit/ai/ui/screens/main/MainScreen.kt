package com.formfit.ai.ui.screens.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.formfit.ai.navigation.FormFitBottomNav
import com.formfit.ai.navigation.Routes
import com.formfit.ai.ui.screens.home.HomeScreen
import com.formfit.ai.ui.screens.profile.ProfileScreen
import com.formfit.ai.ui.screens.progress.ProgressScreen
import com.formfit.ai.ui.screens.workout.WorkoutScreen

@Composable
fun MainScreen(rootNavController: NavHostController) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            FormFitBottomNav(navController = bottomNavController)
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Routes.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.Home.route) {
                HomeScreen(
                    onNavigateToExercise = { exerciseId ->
                        rootNavController.navigate(Routes.ExerciseDetail.createRoute(exerciseId))
                    },
                    onNavigateToRoutines = {
                        rootNavController.navigate(Routes.Routines.route)
                    }
                )
            }
            composable(Routes.Workout.route) {
                WorkoutScreen(
                    onNavigateToExercise = { exerciseId ->
                        rootNavController.navigate(Routes.ExerciseDetail.createRoute(exerciseId))
                    }
                )
            }
            composable(Routes.Progress.route) {
                ProgressScreen(
                    onNavigateToPlans = {
                        rootNavController.navigate(Routes.Plans.route)
                    }
                )
            }
            composable(Routes.Profile.route) {
                ProfileScreen(
                    onNavigateToPlans = {
                        rootNavController.navigate(Routes.Plans.route)
                    },
                    onSignOut = {
                        rootNavController.navigate(Routes.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
