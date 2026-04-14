package com.formfit.ai.core.data

import com.formfit.ai.core.model.UserProfile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    companion object {
        private const val TABLE = "profiles"
    }

    suspend fun createProfile(
        displayName: String,
        gender: String = "",
        goal: String = "",
        frequency: String = "",
        equipment: String = "",
        referral: String = "",
        onboardingComplete: Boolean = false
    ): Result<Unit> {
        return try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("No authenticated user"))

            val profile = mapOf(
                "id" to userId,
                "display_name" to displayName,
                "avatar_url" to "",
                "gender" to gender,
                "goal" to goal,
                "workout_frequency" to frequency,
                "equipment" to equipment,
                "referral_source" to referral,
                "subscription_plan" to "free",
                "onboarding_complete" to onboardingComplete
            )

            supabaseClient.postgrest[TABLE].upsert(profile)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOnboardingData(
        displayName: String,
        gender: String,
        goal: String,
        frequency: String,
        equipment: String,
        referral: String
    ): Result<Unit> {
        return try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("No authenticated user"))

            val update = mapOf(
                "display_name" to displayName,
                "gender" to gender,
                "goal" to goal,
                "workout_frequency" to frequency,
                "equipment" to equipment,
                "referral_source" to referral,
                "onboarding_complete" to true
            )

            supabaseClient.postgrest[TABLE].update(update) {
                filter { eq("id", userId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfile(): Result<UserProfile?> {
        return try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("No authenticated user"))

            val result = supabaseClient.postgrest[TABLE]
                .select(columns = Columns.ALL) {
                    filter { eq("id", userId) }
                }
                .decodeSingleOrNull<UserProfile>()

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSubscription(plan: String): Result<Unit> {
        return try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("No authenticated user"))

            supabaseClient.postgrest[TABLE].update(
                mapOf("subscription_plan" to plan)
            ) {
                filter { eq("id", userId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
