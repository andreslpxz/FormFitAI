package com.formfit.ai.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.formfit.ai.AppAuthState
import com.formfit.ai.AppViewModel
import com.formfit.ai.ui.screens.auth.AuthScreen
import com.formfit.ai.ui.screens.auth.ForgotPasswordScreen
import com.formfit.ai.ui.screens.auth.LoginScreen
import com.formfit.ai.ui.screens.auth.RegisterScreen
import com.formfit.ai.ui.screens.main.MainScreen
import com.formfit.ai.ui.screens.onboarding.OnboardingScreen
import com.formfit.ai.ui.screens.plans.PlansScreen
import com.formfit.ai.ui.screens.routines.RoutineBuilderScreen
import com.formfit.ai.ui.screens.routines.RoutineSessionScreen
import com.formfit.ai.ui.screens.routines.RoutinesScreen
import com.formfit.ai.ui.screens.splash.SplashScreen
import com.formfit.ai.ui.screens.workout.ExerciseDetailScreen
import com.formfit.ai.ui.screens.workout.PoseCameraScreen
import com.formfit.ai.ui.screens.workout.WorkoutSummaryScreen

private val AUTH_ROUTES = setOf(
    Routes.Auth.route,
    Routes.Login.route,
    Routes.Register.route,
    Routes.ForgotPassword.route
)

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    appViewModel: AppViewModel = hiltViewModel()
) {
    val authState by appViewModel.authState.collectAsStateWithLifecycle()
    val hasOnboarded by appViewModel.hasOnboarded.collectAsStateWithLifecycle()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    LaunchedEffect(authState) {
        when (authState) {
            is AppAuthState.Authenticated -> {
                if (currentRoute != null && currentRoute in AUTH_ROUTES) {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is AppAuthState.Unauthenticated -> {
                if (currentRoute == Routes.Main.route) {
                    navController.navigate(Routes.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            AppAuthState.Initializing -> Unit
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        }
    ) {
        composable(Routes.Splash.route) {
            SplashScreen(
                onSplashComplete = { isLoggedIn, _ ->
                    when {
                        isLoggedIn -> navController.navigate(Routes.Main.route) {
                            popUpTo(Routes.Splash.route) { inclusive = true }
                        }
                        !hasOnboarded -> navController.navigate(Routes.Onboarding.route) {
                            popUpTo(Routes.Splash.route) { inclusive = true }
                        }
                        else -> navController.navigate(Routes.Auth.route) {
                            popUpTo(Routes.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Routes.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Routes.Auth.route) {
                        popUpTo(Routes.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Auth.route) {
            AuthScreen(
                onNavigateToLogin = { navController.navigate(Routes.Login.route) },
                onNavigateToRegister = { navController.navigate(Routes.Register.route) },
                onAuthSuccess = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Login.route) {
            LoginScreen(
                onBack = { navController.popBackStack() },
                onLoginSuccess = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onForgotPassword = { navController.navigate(Routes.ForgotPassword.route) }
            )
        }

        composable(Routes.Register.route) {
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Main.route) {
            MainScreen(rootNavController = navController)
        }

        composable(
            route = Routes.ExerciseDetail.route,
            arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: ""
            ExerciseDetailScreen(
                exerciseId = exerciseId,
                onBack = { navController.popBackStack() },
                onStartWorkout = { id ->
                    navController.navigate(Routes.ActiveWorkout.createRoute(id))
                }
            )
        }

        composable(
            route = Routes.ActiveWorkout.route,
            arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: "squats"
            PoseCameraScreen(
                exerciseId = exerciseId,
                onBack = { navController.popBackStack() },
                onWorkoutFinished = { sessionId ->
                    navController.navigate(Routes.WorkoutSummary.createRoute(sessionId)) {
                        popUpTo(Routes.ActiveWorkout.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.WorkoutSummary.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            WorkoutSummaryScreen(
                sessionId = sessionId,
                onGoHome = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Routines.route) {
            RoutinesScreen(
                onNavigateToRoutineBuilder = { routineId ->
                    navController.navigate(Routes.RoutineBuilder.createRoute(routineId))
                },
                onStartRoutine = { routineId ->
                    navController.navigate(Routes.RoutineSession.createRoute(routineId))
                }
            )
        }

        composable(
            route = Routes.RoutineSession.route,
            arguments = listOf(navArgument("routineId") { type = NavType.StringType })
        ) { backStackEntry ->
            val routineId = backStackEntry.arguments?.getString("routineId") ?: return@composable
            RoutineSessionScreen(
                routineId = routineId,
                onBack = { navController.popBackStack() },
                onStartExercise = { exerciseId ->
                    navController.navigate(Routes.ActiveWorkout.createRoute(exerciseId))
                },
                onRoutineComplete = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.Routines.route) { inclusive = false }
                    }
                }
            )
        }

        composable(
            route = Routes.RoutineBuilder.route,
            arguments = listOf(navArgument("routineId") { type = NavType.StringType })
        ) { backStackEntry ->
            val routineId = backStackEntry.arguments?.getString("routineId") ?: "new"
            RoutineBuilderScreen(
                routineId = routineId,
                onBack = { navController.popBackStack() },
                onNavigateToPlans = { navController.navigate(Routes.Plans.route) }
            )
        }

        composable(Routes.Plans.route) {
            PlansScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
