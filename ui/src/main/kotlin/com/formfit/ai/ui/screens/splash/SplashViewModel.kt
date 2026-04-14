package com.formfit.ai.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.formfit.ai.core.data.AuthRepository
import com.formfit.ai.core.data.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashUiState {
    object Loading : SplashUiState()
    data class Ready(val isLoggedIn: Boolean, val hasOnboarded: Boolean) : SplashUiState()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            delay(1800)

            val hasOnboarded = preferencesManager.onboardingComplete.first()
            val isLoggedIn = authRepository.isLoggedIn()

            _uiState.value = SplashUiState.Ready(
                isLoggedIn = isLoggedIn,
                hasOnboarded = hasOnboarded
            )
        }
    }
}
