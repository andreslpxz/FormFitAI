package com.formfit.ai.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.formfit.ai.core.data.WorkoutRepository
import com.formfit.ai.core.model.ExerciseLibrary
import com.formfit.ai.core.model.WorkoutSession
import com.formfit.ai.vision.CameraManager
import com.formfit.ai.vision.PoseAnalysisManager
import com.formfit.ai.vision.PoseLandmarkerHelper
import com.formfit.ai.vision.PoseLandmarkSmoother
import com.formfit.ai.vision.RepCounterEngine
import com.formfit.ai.vision.RepCounterState
import com.formfit.ai.vision.model.CalibrationState
import com.formfit.ai.vision.model.OverallFormQuality
import com.formfit.ai.vision.model.PoseLandmarkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PoseDetectionUiState(
    val currentExercise: String = "squats",
    val calibrationState: CalibrationState = CalibrationState.NOT_STARTED,
    val isCalibrated: Boolean = false,
    val hasCameraPermission: Boolean = false,
    val isLandmarkerReady: Boolean = false,
    val formQuality: OverallFormQuality = OverallFormQuality.empty(),
    val fps: Float = 0f,
    val isWorkoutActive: Boolean = false,
    val elapsedSeconds: Int = 0,
    val repCounterState: RepCounterState = RepCounterState(),
    val formScoreAccumulator: Float = 0f,
    val formScoredFrames: Int = 0,
    val savedSessionId: Long? = null
)

@HiltViewModel
class PoseDetectionViewModel @Inject constructor(
    val poseLandmarkerHelper: PoseLandmarkerHelper,
    val cameraManager: CameraManager,
    private val poseAnalysisManager: PoseAnalysisManager,
    private val poseSmoother: PoseLandmarkSmoother,
    private val repCounterEngine: RepCounterEngine,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PoseDetectionUiState())
    val uiState: StateFlow<PoseDetectionUiState> = _uiState.asStateFlow()

    private val _smoothedPoseResult = MutableStateFlow<PoseLandmarkResult?>(null)
    val poseResult: StateFlow<PoseLandmarkResult?> = _smoothedPoseResult.asStateFlow()

    private var lastFrameTimestamp = 0L
    private var frameCount = 0
    private var timerJob: Job? = null

    init {
        setupLandmarker()
        observePoseResults()
    }

    private fun setupLandmarker() {
        viewModelScope.launch {
            try {
                poseLandmarkerHelper.setup()
                _uiState.update { it.copy(isLandmarkerReady = true) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun observePoseResults() {
        viewModelScope.launch {
            poseLandmarkerHelper.poseResults.collect { rawResult ->
                if (rawResult == null) {
                    _smoothedPoseResult.value = null
                    _uiState.update { state ->
                        state.copy(
                            calibrationState = CalibrationState.NOT_STARTED,
                            formQuality = OverallFormQuality.empty()
                        )
                    }
                    return@collect
                }

                val result = poseSmoother.smooth(rawResult)
                _smoothedPoseResult.value = result

                val calibration = poseAnalysisManager.checkCalibration(result)
                val exerciseId = _uiState.value.currentExercise

                val formQuality = if (calibration == CalibrationState.CALIBRATED) {
                    poseAnalysisManager.analyzeFormForExercise(exerciseId, result.landmarks)
                } else {
                    OverallFormQuality.empty()
                }

                val now = System.currentTimeMillis()
                frameCount++
                val elapsed = now - lastFrameTimestamp
                val fps = if (elapsed > 0) (frameCount * 1000f / elapsed) else 0f
                if (elapsed >= 1000L) {
                    lastFrameTimestamp = now
                    frameCount = 0
                }

                if (_uiState.value.isWorkoutActive && calibration == CalibrationState.CALIBRATED) {
                    val repState = repCounterEngine.processForExercise(exerciseId, result).value
                    val score = formQuality.score
                    _uiState.update { state ->
                        state.copy(
                            calibrationState = calibration,
                            isCalibrated = true,
                            formQuality = formQuality,
                            fps = fps,
                            repCounterState = repState,
                            formScoreAccumulator = state.formScoreAccumulator + score,
                            formScoredFrames = state.formScoredFrames + 1
                        )
                    }
                } else {
                    _uiState.update { state ->
                        state.copy(
                            calibrationState = calibration,
                            isCalibrated = calibration == CalibrationState.CALIBRATED,
                            formQuality = formQuality,
                            fps = fps
                        )
                    }
                }
            }
        }
    }

    fun startWorkout() {
        val exerciseId = _uiState.value.currentExercise
        repCounterEngine.reset(exerciseId)
        _uiState.update { it.copy(
            isWorkoutActive = true,
            elapsedSeconds = 0,
            formScoreAccumulator = 0f,
            formScoredFrames = 0,
            repCounterState = RepCounterState(),
            savedSessionId = null
        ) }
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    fun finishWorkout(userId: String = "") {
        timerJob?.cancel()
        val state = _uiState.value
        val exerciseId = state.currentExercise
        val exercise = ExerciseLibrary.find { it.id == exerciseId }
        val repCount = state.repCounterState.count
        val durationSeconds = state.elapsedSeconds
        val avgFormScore = if (state.formScoredFrames > 0)
            (state.formScoreAccumulator / state.formScoredFrames).coerceIn(0f, 1f)
        else 0f
        val calories = (exercise?.caloriesPerMinute ?: 6f) * (durationSeconds / 60f)

        _uiState.update { it.copy(isWorkoutActive = false) }

        viewModelScope.launch {
            val session = WorkoutSession(
                userId = userId,
                exerciseId = exerciseId,
                exerciseName = exercise?.name ?: exerciseId,
                repCount = repCount,
                durationSeconds = durationSeconds,
                avgFormScore = avgFormScore,
                caloriesBurned = calories,
                setsCompleted = 1,
                formIssues = state.formQuality.issues
            )
            val savedId = workoutRepository.saveSession(session)
            _uiState.update { it.copy(savedSessionId = savedId) }
        }
    }

    fun setExercise(exerciseId: String) {
        _uiState.update { it.copy(currentExercise = exerciseId) }
    }

    fun setCameraPermissionGranted(granted: Boolean) {
        _uiState.update { it.copy(hasCameraPermission = granted) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        cameraManager.shutdown()
    }
}
