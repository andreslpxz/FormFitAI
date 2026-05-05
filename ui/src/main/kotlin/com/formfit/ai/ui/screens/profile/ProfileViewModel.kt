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
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "ProfileViewModel"
private const val AVATARS_BUCKET = "avatars"

data class ProfileUiState(
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val subscriptionPlan: SubscriptionPlan = SubscriptionPlan.FREE,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val isEditingName: Boolean = false,
    val editNameBuffer: String = "",
    val isUploadingAvatar: Boolean = false
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
                val metaAvatarUrl = user?.userMetadata?.get("avatar_url")
                    ?.toString()?.trim('"')?.takeIf { it.isNotBlank() }

                _uiState.update { state ->
                    state.copy(
                        email = user?.email ?: "",
                        displayName = user?.userMetadata?.get("display_name")
                            ?.toString()?.trim('"') ?: "",
                        avatarUrl = metaAvatarUrl
                    )
                }

                if (metaAvatarUrl == null) {
                    user?.id?.let { uid -> loadStorageAvatarUrl(uid) }
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

    private suspend fun loadStorageAvatarUrl(userId: String) {
        try {
            val url = withContext(Dispatchers.IO) {
                supabaseClient.storage.from(AVATARS_BUCKET).publicUrl("$userId/avatar.jpg")
            }
            _uiState.update { it.copy(avatarUrl = url) }
        } catch (e: Exception) {
            Log.d(TAG, "No avatar in storage for user $userId: ${e.message}")
        }
    }

    fun uploadAvatar(imageBytes: ByteArray) {
        val userId = try {
            supabaseClient.auth.currentUserOrNull()?.id ?: return
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Auth unavailable for avatar upload: ${e.message}")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingAvatar = true) }
            try {
                val path = "$userId/avatar.jpg"
                withContext(Dispatchers.IO) {
                    supabaseClient.storage.from(AVATARS_BUCKET).upload(path, imageBytes, upsert = true)
                }
                val url = supabaseClient.storage.from(AVATARS_BUCKET).publicUrl(path)
                supabaseClient.auth.modifyUser {
                    data {
                        put("avatar_url", kotlinx.serialization.json.JsonPrimitive(url))
                    }
                }
                _uiState.update { it.copy(avatarUrl = url, isUploadingAvatar = false) }
                Log.d(TAG, "Avatar uploaded successfully")
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Auth error uploading avatar: ${e.message}", e)
                _uiState.update { it.copy(isUploadingAvatar = false) }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload avatar: ${e.javaClass.simpleName} — ${e.message}", e)
                _uiState.update { it.copy(isUploadingAvatar = false) }
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
                supabaseClient.auth.modifyUser {
                    data {
                        put("display_name", kotlinx.serialization.json.JsonPrimitive(newName))
                    }
                }
                _uiState.update { it.copy(displayName = newName, isEditingName = false) }
                Log.d(TAG, "Display name updated to: $newName")
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Auth state error updating display name: ${e.message}", e)
                _uiState.update { it.copy(isEditingName = false) }
            } catch (e: RuntimeException) {
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
