package com.formfit.ai.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity(tableName = "workout_sessions")
@Serializable
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @SerialName("user_id") val userId: String = "",
    @SerialName("exercise_id") val exerciseId: String,
    @SerialName("exercise_name") val exerciseName: String,
    @SerialName("rep_count") val repCount: Int,
    @SerialName("duration_seconds") val durationSeconds: Int,
    @SerialName("avg_form_score") val avgFormScore: Float,
    @SerialName("calories_burned") val caloriesBurned: Float,
    @SerialName("sets_completed") val setsCompleted: Int = 1,
    @SerialName("form_issues") val formIssues: List<String> = emptyList(),
    @SerialName("synced_to_cloud") val syncedToCloud: Boolean = false,
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class WorkoutSummary(
    val session: WorkoutSession,
    val repsBySet: List<Int> = emptyList(),
    val formScoreByRep: List<Float> = emptyList(),
    val peakAngle: Float = 0f,
    val totalCalories: Float = 0f
)
