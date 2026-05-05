package com.formfit.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.formfit.ai.core.data.AuthRepository
import com.formfit.ai.core.data.PreferencesManager
import com.formfit.ai.core.data.ProfileRepository
import com.formfit.ai.core.data.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.gotrue.SessionStatus
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AppAuthState {
    object Initializing : AppAuthState()
    data class Authenticated(val userId: String) : AppAuthState()
    object Unauthenticated : AppAuthState()
}

@HiltViewModel
class AppViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val preferencesManager: PreferencesManager,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    val authState: StateFlow<AppAuthState> = authRepository.sessionStatusFlow
        .map { status ->
            @Suppress("REDUNDANT_ELSE_IN_WHEN")
            when (status) {
                is SessionStatus.Authenticated ->
                    AppAuthState.Authenticated(status.session.user?.id ?: "")
                is SessionStatus.NotAuthenticated -> AppAuthState.Unauthenticated
                else -> AppAuthState.Initializing
            }
        }
        .catch { e ->
            Log.e("AppViewModel", "Error collecting session status", e)
            emit(AppAuthState.Unauthenticated)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AppAuthState.Initializing
        )

    private val _hasOnboarded = MutableStateFlow(false)
    val hasOnboarded: StateFlow<Boolean> = _hasOnboarded.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                _hasOnboarded.value = preferencesManager.onboardingComplete.first()
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error loading onboarding state", e)
            }
        }

        viewModelScope.launch {
            try {
                authRepository.sessionStatusFlow.collect { status ->
                    if (status is SessionStatus.Authenticated) {
                        try {
                            ensureProfileExists()
                            subscriptionRepository.refreshSubscription()
                        } catch (e: Exception) {
                            Log.e("AppViewModel", "Error during post-auth setup", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error collecting session status", e)
            }
        }
    }

    private suspend fun ensureProfileExists() {
        val user = authRepository.currentUser ?: return
        val existingProfile = profileRepository.getProfile().getOrNull()
        if (existingProfile == null) {
            val onboardingData = preferencesManager.getOnboardingData().first()
            profileRepository.createProfile(
                displayName = onboardingData.name.ifBlank {
                    user.userMetadata?.get("full_name")
                        ?.toString()?.trim('"')
                        ?: user.email
                        ?: "User"
                },
                gender = onboardingData.gender,
                goal = onboardingData.goal,
                frequency = onboardingData.frequency,
                equipment = onboardingData.equipment,
                referral = onboardingData.referral,
                onboardingComplete = onboardingData.name.isNotBlank()
            )
        }
    }
}
