package com.formfit.ai.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.formfit.ai.core.data.WorkoutSessionDao
import com.formfit.ai.core.model.WorkoutSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProgressUiState(
    val totalSessions: Int = 0,
    val totalReps: Int = 0,
    val avgFormScore: Float = 0f,
    val totalCalories: Float = 0f,
    val recentSessions: List<WorkoutSession> = emptyList(),
    val personalBests: List<WorkoutSession> = emptyList()
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val workoutSessionDao: WorkoutSessionDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            workoutSessionDao.getRecentSessions().collect { sessions ->
                _uiState.update { state ->
                    state.copy(
                        recentSessions = sessions,
                        totalSessions = sessions.size,
                        totalReps = sessions.sumOf { it.repCount },
                        avgFormScore = if (sessions.isEmpty()) 0f else sessions.map { it.avgFormScore }.average().toFloat(),
                        totalCalories = sessions.sumOf { it.caloriesBurned.toDouble() }.toFloat()
                    )
                }
            }
        }
    }
}
