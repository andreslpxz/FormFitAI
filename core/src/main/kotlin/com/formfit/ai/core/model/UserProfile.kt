package com.formfit.ai.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val gender: String? = null,
    val goal: String? = null,
    @SerialName("workout_frequency") val workoutFrequency: String? = null,
    val equipment: String? = null,
    @SerialName("referral_source") val referralSource: String? = null,
    @SerialName("subscription_plan") val subscriptionPlan: String = "free",
    @SerialName("subscription_expiry") val subscriptionExpiry: String? = null,
    @SerialName("onboarding_complete") val onboardingComplete: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
) {
    val isPro: Boolean get() = subscriptionPlan == "pro"
    val displayNameOrEmail: String get() = displayName ?: "User"
}
