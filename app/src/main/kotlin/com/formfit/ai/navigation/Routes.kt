package com.formfit.ai.navigation

sealed class Routes(val route: String) {

    object Splash : Routes("splash")
    object Onboarding : Routes("onboarding")
    object Auth : Routes("auth")
    object Login : Routes("login")
    object Register : Routes("register")
    object ForgotPassword : Routes("forgot_password")

    object Main : Routes("main")
    object Home : Routes("home")
    object Workout : Routes("workout")
    object Progress : Routes("progress")
    object Profile : Routes("profile")

    object ExerciseDetail : Routes("exercise/{exerciseId}") {
        fun createRoute(exerciseId: String) = "exercise/$exerciseId"
    }
    object ActiveWorkout : Routes("active_workout/{exerciseId}") {
        fun createRoute(exerciseId: String) = "active_workout/$exerciseId"
    }
    object WorkoutSummary : Routes("workout_summary/{sessionId}") {
        fun createRoute(sessionId: Long) = "workout_summary/$sessionId"
    }
    object Routines : Routes("routines")
    object RoutineSession : Routes("routine_session/{routineId}") {
        fun createRoute(routineId: String) = "routine_session/$routineId"
    }
    object RoutineBuilder : Routes("routine_builder/{routineId}") {
        fun createRoute(routineId: String = "new") = "routine_builder/$routineId"
    }
    object Plans : Routes("plans")
}
