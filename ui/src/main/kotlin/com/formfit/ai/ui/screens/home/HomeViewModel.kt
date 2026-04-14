package com.formfit.ai.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.formfit.ai.core.data.PreferencesManager
import com.formfit.ai.core.data.WorkoutSessionDao
import com.formfit.ai.core.model.ExerciseCategory
import com.formfit.ai.core.model.PresetRoutines
import com.formfit.ai.core.model.Routine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val userName: String = "",
    val weeklyWorkouts: Int = 0,
    val currentStreak: Int = 0,
    val avgFormScore: Float = 0f,
    val featuredRoutines: List<Routine> = PresetRoutines,
    val selectedCategory: ExerciseCategory? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val workoutSessionDao: WorkoutSessionDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            preferencesManager.getOnboardingData().collect { data ->
                _uiState.update { it.copy(userName = data.name) }
            }
        }

        viewModelScope.launch {
            val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            workoutSessionDao.getSessionCountSince(oneWeekAgo).collect { count ->
                _uiState.update { it.copy(weeklyWorkouts = count) }
            }
        }
    }

    fun setCategory(category: ExerciseCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }
}
