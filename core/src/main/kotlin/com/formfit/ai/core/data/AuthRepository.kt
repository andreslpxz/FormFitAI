package com.formfit.ai.core.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.OTP
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

private const val DEEP_LINK_CALLBACK = "formfitai://auth/callback"

@Singleton
class AuthRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    val currentUser get() = supabaseClient.auth.currentUserOrNull()
    val currentSession get() = supabaseClient.auth.currentSessionOrNull()
    val sessionStatusFlow: Flow<SessionStatus> get() = supabaseClient.auth.sessionStatus

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
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun signInWithGoogle(idToken: String, rawNonce: String): Result<Unit> = try {
        supabaseClient.auth.signInWith(IDToken) {
            this.idToken = idToken
            this.provider = Google
            this.nonce = rawNonce
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
        supabaseClient.auth.resetPasswordForEmail(
            email = email,
            redirectUrl = DEEP_LINK_CALLBACK
        )
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
