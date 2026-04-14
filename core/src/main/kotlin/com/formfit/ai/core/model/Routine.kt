package com.formfit.ai.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity(tableName = "routines")
@Serializable
data class Routine(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    @SerialName("is_preset") val isPreset: Boolean = false,
    @SerialName("user_id") val userId: String = "",
    val exercises: List<RoutineExercise>,
    @SerialName("estimated_minutes") val estimatedMinutes: Int,
    val difficulty: Difficulty,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class RoutineExercise(
    @SerialName("exercise_id") val exerciseId: String,
    @SerialName("exercise_name") val exerciseName: String,
    val sets: Int = 3,
    val reps: Int = 10,
    @SerialName("rest_seconds") val restSeconds: Int = 60,
    val order: Int = 0
)

val PresetRoutines = listOf(
    Routine(
        id = "beginner_full_body",
        name = "Beginner Full Body",
        description = "Perfect starter routine covering all major muscle groups",
        isPreset = true,
        exercises = listOf(
            RoutineExercise("squats", "Squats", sets = 3, reps = 12, restSeconds = 60, order = 0),
            RoutineExercise("pushups", "Push-ups", sets = 3, reps = 8, restSeconds = 60, order = 1),
            RoutineExercise("lunges", "Lunges", sets = 2, reps = 10, restSeconds = 60, order = 2),
            RoutineExercise("plank", "Plank", sets = 3, reps = 1, restSeconds = 45, order = 3)
        ),
        estimatedMinutes = 25,
        difficulty = Difficulty.BEGINNER
    ),
    Routine(
        id = "upper_body_blast",
        name = "Upper Body Blast",
        description = "Chest, shoulders, and arms focused workout",
        isPreset = true,
        exercises = listOf(
            RoutineExercise("pushups", "Push-ups", sets = 4, reps = 12, restSeconds = 60, order = 0),
            RoutineExercise("shoulder_press", "Shoulder Press", sets = 3, reps = 10, restSeconds = 60, order = 1),
            RoutineExercise("bicep_curls", "Bicep Curls", sets = 3, reps = 12, restSeconds = 45, order = 2)
        ),
        estimatedMinutes = 20,
        difficulty = Difficulty.INTERMEDIATE
    ),
    Routine(
        id = "leg_day",
        name = "Leg Day",
        description = "Lower body strength and endurance",
        isPreset = true,
        exercises = listOf(
            RoutineExercise("squats", "Squats", sets = 4, reps = 15, restSeconds = 75, order = 0),
            RoutineExercise("lunges", "Lunges", sets = 3, reps = 12, restSeconds = 60, order = 1)
        ),
        estimatedMinutes = 20,
        difficulty = Difficulty.INTERMEDIATE
    )
)
