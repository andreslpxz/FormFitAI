package com.formfit.ai.ui.screens.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.formfit.ai.core.data.PreferencesManager
import com.formfit.ai.core.data.SubscriptionRepository
import com.formfit.ai.core.model.SubscriptionPlan
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ProfileViewModel"

data class ProfileUiState(
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val subscriptionPlan: SubscriptionPlan = SubscriptionPlan.FREE,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val isEditingName: Boolean = false,
    val editNameBuffer: String = ""
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val supabaseClient: SupabaseClient,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        refreshSubscription()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                val user = supabaseClient.auth.currentSessionOrNull()?.user
                _uiState.update { state ->
                    state.copy(
                        email = user?.email ?: "",
                        displayName = user?.userMetadata?.get("display_name")
                            ?.toString()?.trim('"') ?: ""
                    )
                }
            } catch (e: IllegalStateException) {
                Log.w(TAG, "Auth session unavailable when loading profile: ${e.message}")
            }
        }

        viewModelScope.launch {
            preferencesManager.soundEnabled.collect { enabled ->
                _uiState.update { it.copy(soundEnabled = enabled) }
            }
        }

        viewModelScope.launch {
            preferencesManager.hapticsEnabled.collect { enabled ->
                _uiState.update { it.copy(hapticsEnabled = enabled) }
            }
        }
    }

    private fun refreshSubscription() {
        viewModelScope.launch {
            subscriptionRepository.refreshSubscription()
            subscriptionRepository.subscriptionPlan.collect { plan ->
                _uiState.update { it.copy(subscriptionPlan = plan) }
            }
        }
    }

    fun startEditingName() {
        _uiState.update { it.copy(isEditingName = true, editNameBuffer = it.displayName) }
    }

    fun updateNameBuffer(name: String) {
        _uiState.update { it.copy(editNameBuffer = name) }
    }

    fun saveDisplayName() {
        val newName = _uiState.value.editNameBuffer.trim()
        if (newName.isBlank()) {
            _uiState.update { it.copy(isEditingName = false) }
            return
        }
        viewModelScope.launch {
            try {
                supabaseClient.auth.updateUser {
                    data = buildMap { put("display_name", newName) }
                }
                _uiState.update { it.copy(displayName = newName, isEditingName = false) }
                Log.d(TAG, "Display name updated to: $newName")
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Auth state error updating display name: ${e.message}", e)
                _uiState.update { it.copy(isEditingName = false) }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update display name: ${e.javaClass.simpleName}", e)
                _uiState.update { it.copy(isEditingName = false) }
            }
        }
    }

    fun cancelEditName() {
        _uiState.update { it.copy(isEditingName = false) }
    }

    fun toggleSound() {
        viewModelScope.launch {
            preferencesManager.setSoundEnabled(!_uiState.value.soundEnabled)
        }
    }

    fun toggleHaptics() {
        viewModelScope.launch {
            preferencesManager.setHapticsEnabled(!_uiState.value.hapticsEnabled)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                supabaseClient.auth.signOut()
            } catch (e: IllegalStateException) {
                Log.w(TAG, "Auth state error during sign out: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Sign out failed: ${e.javaClass.simpleName}", e)
            }
        }
    }
}
