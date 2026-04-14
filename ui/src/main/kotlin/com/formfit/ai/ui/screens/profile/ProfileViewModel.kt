package com.formfit.ai.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.formfit.ai.core.data.PreferencesManager
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

data class ProfileUiState(
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val subscriptionPlan: SubscriptionPlan = SubscriptionPlan.FREE,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val user = supabaseClient.auth.currentSessionOrNull()?.user
            _uiState.update { state ->
                state.copy(
                    email = user?.email ?: "",
                    displayName = user?.userMetadata?.get("display_name")?.toString()?.trim('"') ?: ""
                )
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
