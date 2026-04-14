package com.formfit.ai.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.formfit.ai.core.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun setEmail(email: String) = _uiState.update { it.copy(email = email) }
    fun setPassword(password: String) = _uiState.update { it.copy(password = password) }
    fun setDisplayName(name: String) = _uiState.update { it.copy(displayName = name) }

    fun register() {
        val state = _uiState.value
        when {
            state.displayName.isBlank() ->
                _uiState.update { it.copy(errorMessage = "Please enter your name") }
            state.email.isBlank() ->
                _uiState.update { it.copy(errorMessage = "Please enter your email") }
            state.password.length < 8 ->
                _uiState.update { it.copy(errorMessage = "Password must be at least 8 characters") }
            else -> viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                authRepository.signUpWithEmail(state.email, state.password, state.displayName)
                    .onSuccess { _uiState.update { it.copy(isLoading = false, isAuthenticated = true) } }
                    .onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = "Registration failed: ${e.message}") } }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
