package com.formfit.ai.core.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.OTP
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val profileRepository: ProfileRepository,
    private val preferencesManager: PreferencesManager
) {
    val currentUser get() = supabaseClient.auth.currentUserOrNull()
    val currentSession get() = supabaseClient.auth.currentSessionOrNull()

    suspend fun isLoggedIn(): Boolean = try {
        supabaseClient.auth.currentSessionOrNull() != null
    } catch (e: Exception) {
        false
    }

    suspend fun signInWithEmail(email: String, password: String): Result<Unit> = try {
        supabaseClient.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Result<Unit> = try {
        supabaseClient.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = kotlinx.serialization.json.buildJsonObject {
                put("display_name", kotlinx.serialization.json.JsonPrimitive(displayName))
            }
        }

        val onboardingData = preferencesManager.getOnboardingData().first()
        profileRepository.createProfile(
            displayName = displayName,
            gender = onboardingData.gender,
            goal = onboardingData.goal,
            frequency = onboardingData.frequency,
            equipment = onboardingData.equipment,
            referral = onboardingData.referral,
            onboardingComplete = true
        )

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun signInWithGoogle(idToken: String, rawNonce: String): Result<Unit> = try {
        supabaseClient.auth.signInWith(Google) {
            this.idToken = idToken
            this.nonce = rawNonce
        }

        val user = supabaseClient.auth.currentUserOrNull()
        if (user != null) {
            val onboardingData = preferencesManager.getOnboardingData().first()
            profileRepository.createProfile(
                displayName = user.userMetadata?.get("full_name")
                    ?.toString()?.trim('"') ?: user.email ?: "User",
                gender = onboardingData.gender,
                goal = onboardingData.goal,
                frequency = onboardingData.frequency,
                equipment = onboardingData.equipment,
                referral = onboardingData.referral,
                onboardingComplete = onboardingData.name.isNotBlank()
            )
        }

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun sendMagicLink(email: String): Result<Unit> = try {
        supabaseClient.auth.signInWith(OTP) {
            this.email = email
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = try {
        supabaseClient.auth.resetPasswordForEmail(email)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun signOut(): Result<Unit> = try {
        supabaseClient.auth.signOut()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun refreshSession(): Result<Unit> = try {
        supabaseClient.auth.refreshCurrentSession()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
