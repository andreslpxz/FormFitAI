package com.formfit.ai.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.formfit.ai.core.data.AuthRepository
import com.formfit.ai.core.data.PreferencesManager
import com.formfit.ai.core.data.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class OnboardingStep {
    WELCOME,
    GENDER,
    GOAL,
    FREQUENCY,
    EQUIPMENT,
    REFERRAL
}

data class OnboardingUiState(
    val currentStep: Int = 0,
    val name: String = "",
    val gender: String = "",
    val goal: String = "",
    val frequency: String = "",
    val equipment: String = "",
    val referralSource: String = "",
    val isLoading: Boolean = false,
    val isComplete: Boolean = false
) {
    val canProceed: Boolean
        get() = when (OnboardingStep.values()[currentStep]) {
            OnboardingStep.WELCOME -> name.isNotBlank()
            OnboardingStep.GENDER -> gender.isNotBlank()
            OnboardingStep.GOAL -> goal.isNotBlank()
            OnboardingStep.FREQUENCY -> frequency.isNotBlank()
            OnboardingStep.EQUIPMENT -> equipment.isNotBlank()
            OnboardingStep.REFERRAL -> true
        }
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun setName(name: String) = _uiState.update { it.copy(name = name) }
    fun setGender(gender: String) = _uiState.update { it.copy(gender = gender) }
    fun setGoal(goal: String) = _uiState.update { it.copy(goal = goal) }
    fun setFrequency(frequency: String) = _uiState.update { it.copy(frequency = frequency) }
    fun setEquipment(equipment: String) = _uiState.update { it.copy(equipment = equipment) }
    fun setReferralSource(source: String) = _uiState.update { it.copy(referralSource = source) }

    fun nextStep() {
        val state = _uiState.value
        if (state.currentStep < OnboardingStep.values().size - 1) {
            _uiState.update { it.copy(currentStep = it.currentStep + 1) }
        } else {
            completeOnboarding()
        }
    }

    fun previousStep() {
        if (_uiState.value.currentStep > 0) {
            _uiState.update { it.copy(currentStep = it.currentStep - 1) }
        }
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val state = _uiState.value

            preferencesManager.saveOnboardingData(
                name = state.name,
                gender = state.gender,
                goal = state.goal,
                frequency = state.frequency,
                equipment = state.equipment,
                referral = state.referralSource
            )
            preferencesManager.setOnboardingComplete(true)

            if (authRepository.isLoggedIn()) {
                profileRepository.updateOnboardingData(
                    displayName = state.name,
                    gender = state.gender,
                    goal = state.goal,
                    frequency = state.frequency,
                    equipment = state.equipment,
                    referral = state.referralSource
                )
            }

            _uiState.update { it.copy(isLoading = false, isComplete = true) }
        }
    }
}
