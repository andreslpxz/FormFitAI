package com.formfit.ai.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String = "",
    val email: String = "",
    @SerialName("display_name") val displayName: String = "",
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val gender: String = "",
    val goal: FitnessGoal = FitnessGoal.STAY_ACTIVE,
    @SerialName("workout_frequency") val workoutFrequency: WorkoutFrequency = WorkoutFrequency.THREE_FOUR,
    val equipment: EquipmentLevel = EquipmentLevel.NONE,
    @SerialName("referral_source") val referralSource: String = "",
    @SerialName("subscription_plan") val subscriptionPlan: SubscriptionPlan = SubscriptionPlan.FREE,
    @SerialName("subscription_expiry") val subscriptionExpiry: String? = null,
    @SerialName("onboarding_complete") val onboardingComplete: Boolean = false,
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
enum class FitnessGoal {
    @SerialName("lose_weight") LOSE_WEIGHT,
    @SerialName("build_muscle") BUILD_MUSCLE,
    @SerialName("stay_active") STAY_ACTIVE,
    @SerialName("rehab") REHAB;

    fun label(): String = when (this) {
        LOSE_WEIGHT -> "Lose Weight"
        BUILD_MUSCLE -> "Build Muscle"
        STAY_ACTIVE -> "Stay Active"
        REHAB -> "Injury Rehab"
    }

    fun emoji(): String = when (this) {
        LOSE_WEIGHT -> "🔥"
        BUILD_MUSCLE -> "💪"
        STAY_ACTIVE -> "⚡"
        REHAB -> "🏥"
    }
}

@Serializable
enum class WorkoutFrequency {
    @SerialName("1_2") ONE_TWO,
    @SerialName("3_4") THREE_FOUR,
    @SerialName("5_plus") FIVE_PLUS;

    fun label(): String = when (this) {
        ONE_TWO -> "1–2x per week"
        THREE_FOUR -> "3–4x per week"
        FIVE_PLUS -> "5+ per week"
    }
}

@Serializable
enum class EquipmentLevel {
    @SerialName("none") NONE,
    @SerialName("dumbbells") DUMBBELLS,
    @SerialName("full_gym") FULL_GYM;

    fun label(): String = when (this) {
        NONE -> "No Equipment"
        DUMBBELLS -> "Dumbbells"
        FULL_GYM -> "Full Gym"
    }
}

@Serializable
enum class SubscriptionPlan {
    @SerialName("free") FREE,
    @SerialName("pro_monthly") PRO_MONTHLY,
    @SerialName("pro_yearly") PRO_YEARLY;

    fun isPro(): Boolean = this != FREE

    fun label(): String = when (this) {
        FREE -> "Free"
        PRO_MONTHLY -> "Pro Monthly"
        PRO_YEARLY -> "Pro Yearly"
    }
}
