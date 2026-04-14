package com.formfit.ai.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.formfit.ai.ui.screens.splash.SplashScreen

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
                        !hasOnboarded -> navController.navigate(Routes.Onboarding.route) {
                            popUpTo(Routes.Splash.route) { inclusive = true }
                        }
                        !isLoggedIn -> navController.navigate(Routes.Auth.route) {
                            popUpTo(Routes.Splash.route) { inclusive = true }
                        }
                        else -> navController.navigate(Routes.Main.route) {
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
            PlaceholderScreen("Exercise Detail: $exerciseId")
        }

        composable(
            route = Routes.ActiveWorkout.route,
            arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: ""
            PlaceholderScreen("Active Workout: $exerciseId")
        }

        composable(
            route = Routes.WorkoutSummary.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) {
            PlaceholderScreen("Workout Summary")
        }

        composable(Routes.Routines.route) {
            PlaceholderScreen("Routines")
        }

        composable(
            route = Routes.RoutineBuilder.route,
            arguments = listOf(navArgument("routineId") { type = NavType.StringType })
        ) {
            PlaceholderScreen("Routine Builder")
        }

        composable(Routes.Plans.route) {
            PlaceholderScreen("Subscription Plans")
        }
    }
}

@Composable
private fun PlaceholderScreen(label: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = label)
    }
}
