package com.formfit.ai.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun setEmail(email: String) = _uiState.update { it.copy(email = email) }
    fun setPassword(password: String) = _uiState.update { it.copy(password = password) }
    fun setDisplayName(name: String) = _uiState.update { it.copy(displayName = name) }

    fun register() {
        val state = _uiState.value
        if (state.password.length < 8) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 8 characters") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                supabaseClient.auth.signUpWith(Email) {
                    email = state.email
                    password = state.password
                    data = kotlinx.serialization.json.buildJsonObject {
                        put("display_name", kotlinx.serialization.json.JsonPrimitive(state.displayName))
                    }
                }
                _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Registration failed: ${e.message}") }
            }
        }
    }
}
