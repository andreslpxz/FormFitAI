package com.formfit.ai.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.formfit.ai.core.data.BodyWeightDao
import com.formfit.ai.core.data.WorkoutSessionDao
import com.formfit.ai.core.model.BodyWeightEntry
import com.formfit.ai.core.model.WorkoutSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class DailyActivity(
    val dayOffsetFromToday: Int,
    val sessionCount: Int
)

data class ChartPoint(
    val label: String,
    val value: Float
)

data class ProgressUiState(
    val totalSessions: Int = 0,
    val totalReps: Int = 0,
    val avgFormScore: Float = 0f,
    val totalCalories: Float = 0f,
    val recentSessions: List<WorkoutSession> = emptyList(),
    val personalBests: List<WorkoutSession> = emptyList(),
    val last30DaysActivity: List<DailyActivity> = emptyList(),
    val repsOverTime: List<ChartPoint> = emptyList(),
    val formScoreByExercise: List<ChartPoint> = emptyList(),
    val weeklyFrequency: List<ChartPoint> = emptyList(),
    val currentWeightKg: Float? = null,
    val weightHistory: List<BodyWeightEntry> = emptyList(),
    val showWeightDialog: Boolean = false,
    val currentStreakDays: Int = 0,
    val longestStreakDays: Int = 0
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val workoutSessionDao: WorkoutSessionDao,
    private val bodyWeightDao: BodyWeightDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        loadSessionData()
        loadWeightData()
    }

    private fun loadSessionData() {
        viewModelScope.launch {
            workoutSessionDao.getAllSessions().collect { sessions ->
                val now = System.currentTimeMillis()
                val thirtyDaysAgo = now - 30L * 24 * 3600 * 1000

                val activity = buildDailyActivity(sessions, thirtyDaysAgo, now)
                val streak = calculateCurrentStreak(activity)
                val longestStreak = calculateLongestStreak(activity)

                _uiState.update { state ->
                    state.copy(
                        recentSessions = sessions.take(10),
                        totalSessions = sessions.size,
                        totalReps = sessions.sumOf { it.repCount },
                        avgFormScore = if (sessions.isEmpty()) 0f
                            else sessions.map { it.avgFormScore }.average().toFloat(),
                        totalCalories = sessions.sumOf { it.caloriesBurned.toDouble() }.toFloat(),
                        last30DaysActivity = activity,
                        repsOverTime = buildRepsOverTime(sessions),
                        weeklyFrequency = buildWeeklyFrequency(sessions),
                        currentStreakDays = streak,
                        longestStreakDays = longestStreak
                    )
                }
            }
        }

        viewModelScope.launch {
            workoutSessionDao.getPersonalBests().collect { pbs ->
                _uiState.update { it.copy(personalBests = pbs) }
            }
        }

        viewModelScope.launch {
            workoutSessionDao.getFormScoreByExercise().collect { rows ->
                val points = rows.map { session ->
                    ChartPoint(
                        label = session.exerciseName.take(8),
                        value = session.avgFormScore
                    )
                }
                _uiState.update { it.copy(formScoreByExercise = points) }
            }
        }
    }

    private fun loadWeightData() {
        viewModelScope.launch {
            bodyWeightDao.getLatestEntry().collect { entry ->
                _uiState.update { it.copy(currentWeightKg = entry?.weightKg) }
            }
        }
        viewModelScope.launch {
            val since = System.currentTimeMillis() - 90L * 24 * 3600 * 1000
            bodyWeightDao.getEntriesSince(since).collect { entries ->
                _uiState.update { it.copy(weightHistory = entries) }
            }
        }
    }

    private fun buildDailyActivity(
        sessions: List<WorkoutSession>,
        startMs: Long,
        nowMs: Long
    ): List<DailyActivity> {
        val dayMs = 24L * 3600 * 1000
        val cal = Calendar.getInstance()

        fun dateKey(timestamp: Long): Long {
            cal.timeInMillis = timestamp
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

        val countByDay = sessions
            .filter { it.createdAt >= startMs }
            .groupBy { dateKey(it.createdAt) }
            .mapValues { (_, v) -> v.size }

        val todayKey = dateKey(nowMs)
        return (0 until 30).map { offset ->
            val dayKey = todayKey - (offset.toLong() * dayMs)
            DailyActivity(
                dayOffsetFromToday = offset,
                sessionCount = countByDay[dayKey] ?: 0
            )
        }.reversed()
    }

    private fun calculateCurrentStreak(activity: List<DailyActivity>): Int {
        var streak = 0
        for (day in activity.reversed()) {
            if (day.sessionCount > 0) streak++ else break
        }
        return streak
    }

    private fun calculateLongestStreak(activity: List<DailyActivity>): Int {
        var longest = 0
        var current = 0
        for (day in activity) {
            if (day.sessionCount > 0) {
                current++
                if (current > longest) longest = current
            } else {
                current = 0
            }
        }
        return longest
    }

    private fun buildRepsOverTime(sessions: List<WorkoutSession>): List<ChartPoint> {
        return sessions.takeLast(8).reversed().mapIndexed { index, session ->
            ChartPoint(label = "${index + 1}", value = session.repCount.toFloat())
        }
    }

    private fun buildWeeklyFrequency(sessions: List<WorkoutSession>): List<ChartPoint> {
        val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val cal = Calendar.getInstance()
        val countByDow = IntArray(7)
        val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 3600 * 1000
        sessions.filter { it.createdAt >= thirtyDaysAgo }.forEach { session ->
            cal.timeInMillis = session.createdAt
            val dow = (cal.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7
            countByDow[dow]++
        }
        return dayLabels.mapIndexed { i, label ->
            ChartPoint(label = label, value = countByDow[i].toFloat())
        }
    }

    fun showWeightDialog() = _uiState.update { it.copy(showWeightDialog = true) }
    fun dismissWeightDialog() = _uiState.update { it.copy(showWeightDialog = false) }

    fun logWeight(weightKg: Float) {
        viewModelScope.launch {
            bodyWeightDao.insert(BodyWeightEntry(weightKg = weightKg))
            _uiState.update { it.copy(showWeightDialog = false) }
        }
    }
}
