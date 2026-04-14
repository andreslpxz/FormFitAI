package com.formfit.ai.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.formfit.ai.vision.CameraManager
import com.formfit.ai.vision.PoseAnalysisManager
import com.formfit.ai.vision.PoseLandmarkerHelper
import com.formfit.ai.vision.model.CalibrationState
import com.formfit.ai.vision.model.OverallFormQuality
import com.formfit.ai.vision.model.PoseLandmarkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
    val fps: Float = 0f
)

@HiltViewModel
class PoseDetectionViewModel @Inject constructor(
    val poseLandmarkerHelper: PoseLandmarkerHelper,
    val cameraManager: CameraManager,
    private val poseAnalysisManager: PoseAnalysisManager,
    private val poseSmoother: com.formfit.ai.vision.PoseLandmarkSmoother
) : ViewModel() {

    private val _uiState = MutableStateFlow(PoseDetectionUiState())
    val uiState: StateFlow<PoseDetectionUiState> = _uiState.asStateFlow()

    private val _smoothedPoseResult = MutableStateFlow<PoseLandmarkResult?>(null)
    val poseResult: StateFlow<PoseLandmarkResult?> = _smoothedPoseResult.asStateFlow()

    private var lastFrameTimestamp = 0L
    private var frameCount = 0

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

                val formQuality = if (calibration == CalibrationState.CALIBRATED) {
                    when (_uiState.value.currentExercise) {
                        "squats" -> poseAnalysisManager.analyzeSquatForm(result.landmarks)
                        "pushups" -> poseAnalysisManager.analyzePushupForm(result.landmarks)
                        "lunges" -> poseAnalysisManager.analyzeLungeForm(result.landmarks)
                        else -> OverallFormQuality.perfect()
                    }
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

    fun setExercise(exerciseId: String) {
        _uiState.update { it.copy(currentExercise = exerciseId) }
    }

    fun setCameraPermissionGranted(granted: Boolean) {
        _uiState.update { it.copy(hasCameraPermission = granted) }
    }

    override fun onCleared() {
        super.onCleared()
        cameraManager.shutdown()
    }
}
