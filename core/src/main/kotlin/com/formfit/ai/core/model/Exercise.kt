package com.formfit.ai.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Exercise(
    val id: String,
    val name: String,
    val description: String,
    val category: ExerciseCategory,
    val difficulty: Difficulty,
    @SerialName("muscles_targeted") val musclesTargeted: List<String>,
    @SerialName("calories_per_minute") val caloriesPerMinute: Float,
    @SerialName("default_reps") val defaultReps: Int = 10,
    @SerialName("default_sets") val defaultSets: Int = 3,
    @SerialName("form_tips") val formTips: List<String>,
    @SerialName("is_pro_only") val isProOnly: Boolean = false,
    @SerialName("lottie_animation") val lottieAnimation: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null
)

@Serializable
enum class ExerciseCategory {
    @SerialName("strength") STRENGTH,
    @SerialName("cardio") CARDIO,
    @SerialName("mobility") MOBILITY,
    @SerialName("hiit") HIIT;

    fun label(): String = when (this) {
        STRENGTH -> "Strength"
        CARDIO -> "Cardio"
        MOBILITY -> "Mobility"
        HIIT -> "HIIT"
    }

    fun emoji(): String = when (this) {
        STRENGTH -> "💪"
        CARDIO -> "🏃"
        MOBILITY -> "🧘"
        HIIT -> "⚡"
    }
}

@Serializable
enum class Difficulty {
    @SerialName("beginner") BEGINNER,
    @SerialName("intermediate") INTERMEDIATE,
    @SerialName("advanced") ADVANCED;

    fun label(): String = when (this) {
        BEGINNER -> "Beginner"
        INTERMEDIATE -> "Intermediate"
        ADVANCED -> "Advanced"
    }
}

val ExerciseLibrary = listOf(
    Exercise(
        id = "squats",
        name = "Squats",
        description = "The king of lower body exercises. Works quads, hamstrings, glutes, and core.",
        category = ExerciseCategory.STRENGTH,
        difficulty = Difficulty.BEGINNER,
        musclesTargeted = listOf("Quadriceps", "Hamstrings", "Glutes", "Core"),
        caloriesPerMinute = 8f,
        defaultReps = 15,
        defaultSets = 3,
        formTips = listOf(
            "Keep knees aligned with toes",
            "Lower until thighs are parallel to floor",
            "Keep chest up and back straight",
            "Drive through heels when rising"
        )
    ),
    Exercise(
        id = "pushups",
        name = "Push-ups",
        description = "Classic upper body exercise targeting chest, triceps, and shoulders.",
        category = ExerciseCategory.STRENGTH,
        difficulty = Difficulty.BEGINNER,
        musclesTargeted = listOf("Chest", "Triceps", "Shoulders", "Core"),
        caloriesPerMinute = 7f,
        defaultReps = 12,
        defaultSets = 3,
        formTips = listOf(
            "Keep body in a straight line",
            "Elbows at 45° from torso",
            "Lower until chest nearly touches floor",
            "Fully extend arms at the top"
        )
    ),
    Exercise(
        id = "lunges",
        name = "Lunges",
        description = "Unilateral leg exercise for balance, strength and coordination.",
        category = ExerciseCategory.STRENGTH,
        difficulty = Difficulty.BEGINNER,
        musclesTargeted = listOf("Quadriceps", "Glutes", "Hamstrings", "Calves"),
        caloriesPerMinute = 7.5f,
        defaultReps = 12,
        defaultSets = 3,
        formTips = listOf(
            "Step far enough forward for 90° knee angles",
            "Keep front knee above ankle",
            "Back knee nearly touches floor",
            "Keep torso upright"
        )
    ),
    Exercise(
        id = "plank",
        name = "Plank",
        description = "Full core stability exercise that engages the entire body.",
        category = ExerciseCategory.STRENGTH,
        difficulty = Difficulty.BEGINNER,
        musclesTargeted = listOf("Core", "Shoulders", "Glutes", "Back"),
        caloriesPerMinute = 5f,
        defaultReps = 1,
        defaultSets = 3,
        formTips = listOf(
            "Keep hips level — not sagging or raised",
            "Gaze at the floor between hands",
            "Breathe steadily throughout",
            "Engage core and glutes"
        )
    ),
    Exercise(
        id = "bicep_curls",
        name = "Bicep Curls",
        description = "Isolation exercise for the biceps brachii.",
        category = ExerciseCategory.STRENGTH,
        difficulty = Difficulty.BEGINNER,
        musclesTargeted = listOf("Biceps", "Forearms"),
        caloriesPerMinute = 5f,
        defaultReps = 12,
        defaultSets = 3,
        formTips = listOf(
            "Keep elbows close to torso",
            "Don't swing the body",
            "Fully extend at the bottom",
            "Squeeze at the top"
        ),
        isProOnly = false
    ),
    Exercise(
        id = "shoulder_press",
        name = "Shoulder Press",
        description = "Overhead press targeting the deltoids and triceps.",
        category = ExerciseCategory.STRENGTH,
        difficulty = Difficulty.INTERMEDIATE,
        musclesTargeted = listOf("Shoulders", "Triceps", "Core"),
        caloriesPerMinute = 6f,
        defaultReps = 10,
        defaultSets = 3,
        formTips = listOf(
            "Press directly overhead, not forward",
            "Keep core tight",
            "Avoid arching the lower back",
            "Full extension at the top"
        ),
        isProOnly = false
    )
)
