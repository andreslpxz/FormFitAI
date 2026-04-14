package com.formfit.ai.vision

import com.formfit.ai.vision.model.BodySide
import com.formfit.ai.vision.model.PoseLandmarkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class RepState {
    IDLE,
    STANDING,
    DESCENDING,
    THRESHOLD_REACHED,
    ASCENDING,
    REPETITION_CONFIRMED
}

data class RepCounterState(
    val count: Int = 0,
    val currentState: RepState = RepState.IDLE,
    val progressPercentage: Float = 0f,
    val isRepComplete: Boolean = false
)

@Singleton
class RepCounterEngine @Inject constructor(
    private val poseAnalysisManager: PoseAnalysisManager
) {
    private val _squatState = MutableStateFlow(RepCounterState())
    val squatState: StateFlow<RepCounterState> = _squatState.asStateFlow()

    private val _pushupState = MutableStateFlow(RepCounterState())
    val pushupState: StateFlow<RepCounterState> = _pushupState.asStateFlow()

    private val _lungeState = MutableStateFlow(RepCounterState())
    val lungeState: StateFlow<RepCounterState> = _lungeState.asStateFlow()

    private val _bicepCurlState = MutableStateFlow(RepCounterState())
    val bicepCurlState: StateFlow<RepCounterState> = _bicepCurlState.asStateFlow()

    private var squatRepCount = 0
    private var squatCurrentState = RepState.IDLE
    private var squatDebounceCount = 0

    private var pushupRepCount = 0
    private var pushupCurrentState = RepState.IDLE
    private var pushupDebounceCount = 0

    private var lungeRepCount = 0
    private var lungeCurrentState = RepState.IDLE

    private var bicepCurlRepCount = 0
    private var bicepCurlCurrentState = RepState.IDLE

    companion object {
        const val SQUAT_STANDING_THRESHOLD = 160f
        const val SQUAT_BOTTOM_THRESHOLD = 100f
        const val PUSHUP_UP_THRESHOLD = 160f
        const val PUSHUP_DOWN_THRESHOLD = 90f
        const val LUNGE_STANDING_THRESHOLD = 160f
        const val LUNGE_BOTTOM_THRESHOLD = 100f
        const val BICEP_CURL_DOWN_THRESHOLD = 160f
        const val BICEP_CURL_UP_THRESHOLD = 50f
        const val DEBOUNCE_FRAMES = 3
    }

    fun processSquat(result: PoseLandmarkResult) {
        val leftKneeAngle = poseAnalysisManager.calculateKneeAngle(result.landmarks, BodySide.LEFT)
        val rightKneeAngle = poseAnalysisManager.calculateKneeAngle(result.landmarks, BodySide.RIGHT)
        val avgKneeAngle = (leftKneeAngle + rightKneeAngle) / 2f

        val progress = when {
            avgKneeAngle >= SQUAT_STANDING_THRESHOLD -> 0f
            avgKneeAngle <= SQUAT_BOTTOM_THRESHOLD -> 1f
            else -> 1f - (avgKneeAngle - SQUAT_BOTTOM_THRESHOLD) / (SQUAT_STANDING_THRESHOLD - SQUAT_BOTTOM_THRESHOLD)
        }

        val newState = when (squatCurrentState) {
            RepState.IDLE, RepState.STANDING -> {
                if (avgKneeAngle >= SQUAT_STANDING_THRESHOLD) RepState.STANDING
                else if (avgKneeAngle < SQUAT_STANDING_THRESHOLD) RepState.DESCENDING
                else squatCurrentState
            }
            RepState.DESCENDING -> {
                when {
                    avgKneeAngle <= SQUAT_BOTTOM_THRESHOLD -> RepState.THRESHOLD_REACHED
                    avgKneeAngle >= SQUAT_STANDING_THRESHOLD -> RepState.STANDING
                    else -> RepState.DESCENDING
                }
            }
            RepState.THRESHOLD_REACHED -> {
                if (avgKneeAngle > SQUAT_BOTTOM_THRESHOLD + 10f) RepState.ASCENDING
                else RepState.THRESHOLD_REACHED
            }
            RepState.ASCENDING -> {
                if (avgKneeAngle >= SQUAT_STANDING_THRESHOLD) {
                    squatDebounceCount++
                    if (squatDebounceCount >= DEBOUNCE_FRAMES) {
                        squatDebounceCount = 0
                        squatRepCount++
                        RepState.REPETITION_CONFIRMED
                    } else {
                        RepState.ASCENDING
                    }
                } else {
                    RepState.ASCENDING
                }
            }
            RepState.REPETITION_CONFIRMED -> {
                RepState.STANDING
            }
        }

        squatCurrentState = newState

        _squatState.value = RepCounterState(
            count = squatRepCount,
            currentState = squatCurrentState,
            progressPercentage = progress,
            isRepComplete = newState == RepState.REPETITION_CONFIRMED
        )
    }

    fun processPushup(result: PoseLandmarkResult) {
        val leftElbowAngle = poseAnalysisManager.calculateElbowAngle(result.landmarks, BodySide.LEFT)
        val rightElbowAngle = poseAnalysisManager.calculateElbowAngle(result.landmarks, BodySide.RIGHT)
        val avgElbowAngle = (leftElbowAngle + rightElbowAngle) / 2f

        val progress = when {
            avgElbowAngle >= PUSHUP_UP_THRESHOLD -> 0f
            avgElbowAngle <= PUSHUP_DOWN_THRESHOLD -> 1f
            else -> 1f - (avgElbowAngle - PUSHUP_DOWN_THRESHOLD) / (PUSHUP_UP_THRESHOLD - PUSHUP_DOWN_THRESHOLD)
        }

        val newState = when (pushupCurrentState) {
            RepState.IDLE, RepState.STANDING -> {
                if (avgElbowAngle >= PUSHUP_UP_THRESHOLD) RepState.STANDING
                else RepState.DESCENDING
            }
            RepState.DESCENDING -> {
                when {
                    avgElbowAngle <= PUSHUP_DOWN_THRESHOLD -> RepState.THRESHOLD_REACHED
                    avgElbowAngle >= PUSHUP_UP_THRESHOLD -> RepState.STANDING
                    else -> RepState.DESCENDING
                }
            }
            RepState.THRESHOLD_REACHED -> {
                if (avgElbowAngle > PUSHUP_DOWN_THRESHOLD + 10f) RepState.ASCENDING
                else RepState.THRESHOLD_REACHED
            }
            RepState.ASCENDING -> {
                if (avgElbowAngle >= PUSHUP_UP_THRESHOLD) {
                    pushupDebounceCount++
                    if (pushupDebounceCount >= DEBOUNCE_FRAMES) {
                        pushupDebounceCount = 0
                        pushupRepCount++
                        RepState.REPETITION_CONFIRMED
                    } else {
                        RepState.ASCENDING
                    }
                } else {
                    RepState.ASCENDING
                }
            }
            RepState.REPETITION_CONFIRMED -> RepState.STANDING
        }

        pushupCurrentState = newState

        _pushupState.value = RepCounterState(
            count = pushupRepCount,
            currentState = pushupCurrentState,
            progressPercentage = progress,
            isRepComplete = newState == RepState.REPETITION_CONFIRMED
        )
    }

    fun processBicepCurl(result: PoseLandmarkResult) {
        val leftElbow = poseAnalysisManager.calculateElbowAngle(result.landmarks, BodySide.LEFT)
        val rightElbow = poseAnalysisManager.calculateElbowAngle(result.landmarks, BodySide.RIGHT)
        val avgElbow = (leftElbow + rightElbow) / 2f

        val newState = when (bicepCurlCurrentState) {
            RepState.IDLE, RepState.STANDING -> {
                if (avgElbow >= BICEP_CURL_DOWN_THRESHOLD) RepState.STANDING
                else RepState.DESCENDING
            }
            RepState.DESCENDING -> {
                when {
                    avgElbow <= BICEP_CURL_UP_THRESHOLD -> RepState.THRESHOLD_REACHED
                    avgElbow >= BICEP_CURL_DOWN_THRESHOLD -> RepState.STANDING
                    else -> RepState.DESCENDING
                }
            }
            RepState.THRESHOLD_REACHED -> {
                if (avgElbow > BICEP_CURL_UP_THRESHOLD + 10f) RepState.ASCENDING else RepState.THRESHOLD_REACHED
            }
            RepState.ASCENDING -> {
                if (avgElbow >= BICEP_CURL_DOWN_THRESHOLD) {
                    bicepCurlRepCount++
                    RepState.REPETITION_CONFIRMED
                } else RepState.ASCENDING
            }
            RepState.REPETITION_CONFIRMED -> RepState.STANDING
        }

        bicepCurlCurrentState = newState
        _bicepCurlState.value = RepCounterState(
            count = bicepCurlRepCount,
            currentState = bicepCurlCurrentState,
            progressPercentage = 0f,
            isRepComplete = newState == RepState.REPETITION_CONFIRMED
        )
    }

    fun reset(exerciseId: String) {
        when (exerciseId) {
            "squats" -> { squatRepCount = 0; squatCurrentState = RepState.IDLE; _squatState.value = RepCounterState() }
            "pushups" -> { pushupRepCount = 0; pushupCurrentState = RepState.IDLE; _pushupState.value = RepCounterState() }
            "lunges" -> { lungeRepCount = 0; lungeCurrentState = RepState.IDLE; _lungeState.value = RepCounterState() }
            "bicep_curls" -> { bicepCurlRepCount = 0; bicepCurlCurrentState = RepState.IDLE; _bicepCurlState.value = RepCounterState() }
        }
    }
}
