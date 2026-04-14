package com.formfit.ai.vision

import com.formfit.ai.vision.model.BodySide
import com.formfit.ai.vision.model.PoseLandmarkIndex
import com.formfit.ai.vision.model.PoseLandmarkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

class RepCounterEngine(
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

    private val _shoulderPressState = MutableStateFlow(RepCounterState())
    val shoulderPressState: StateFlow<RepCounterState> = _shoulderPressState.asStateFlow()

    private val _plankState = MutableStateFlow(RepCounterState())
    val plankState: StateFlow<RepCounterState> = _plankState.asStateFlow()

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

    private var shoulderPressRepCount = 0
    private var shoulderPressCurrentState = RepState.IDLE

    private var plankFramesHeld = 0
    private var plankRepCount = 0
    private var plankCurrentState = RepState.IDLE
    private val PLANK_HOLD_FRAMES = 60

    companion object {
        const val SQUAT_STANDING_THRESHOLD = 160f
        const val SQUAT_BOTTOM_THRESHOLD = 100f
        const val PUSHUP_UP_THRESHOLD = 160f
        const val PUSHUP_DOWN_THRESHOLD = 90f
        const val LUNGE_STANDING_THRESHOLD = 160f
        const val LUNGE_BOTTOM_THRESHOLD = 100f
        const val BICEP_CURL_DOWN_THRESHOLD = 160f
        const val BICEP_CURL_UP_THRESHOLD = 50f
        const val SHOULDER_PRESS_DOWN_THRESHOLD = 90f
        const val SHOULDER_PRESS_UP_THRESHOLD = 160f
        const val DEBOUNCE_FRAMES = 3
    }

    fun processForExercise(exerciseId: String, result: PoseLandmarkResult): StateFlow<RepCounterState> {
        return when (exerciseId) {
            "squats" -> { processSquat(result); squatState }
            "pushups" -> { processPushup(result); pushupState }
            "lunges" -> { processLunge(result); lungeState }
            "bicep_curls" -> { processBicepCurl(result); bicepCurlState }
            "shoulder_press" -> { processShoulderPress(result); shoulderPressState }
            "plank" -> { processPlank(result); plankState }
            else -> { processSquat(result); squatState }
        }
    }

    fun getStateForExercise(exerciseId: String): StateFlow<RepCounterState> = when (exerciseId) {
        "squats" -> squatState
        "pushups" -> pushupState
        "lunges" -> lungeState
        "bicep_curls" -> bicepCurlState
        "shoulder_press" -> shoulderPressState
        "plank" -> plankState
        else -> squatState
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
                else RepState.DESCENDING
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
                    } else RepState.ASCENDING
                } else RepState.ASCENDING
            }
            RepState.REPETITION_CONFIRMED -> RepState.STANDING
        }
        squatCurrentState = newState
        _squatState.value = RepCounterState(squatRepCount, squatCurrentState, progress, newState == RepState.REPETITION_CONFIRMED)
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
                    } else RepState.ASCENDING
                } else RepState.ASCENDING
            }
            RepState.REPETITION_CONFIRMED -> RepState.STANDING
        }
        pushupCurrentState = newState
        _pushupState.value = RepCounterState(pushupRepCount, pushupCurrentState, progress, newState == RepState.REPETITION_CONFIRMED)
    }

    fun processLunge(result: PoseLandmarkResult) {
        val leftKneeAngle = poseAnalysisManager.calculateKneeAngle(result.landmarks, BodySide.LEFT)
        val rightKneeAngle = poseAnalysisManager.calculateKneeAngle(result.landmarks, BodySide.RIGHT)
        val minKneeAngle = minOf(leftKneeAngle, rightKneeAngle)

        val progress = when {
            minKneeAngle >= LUNGE_STANDING_THRESHOLD -> 0f
            minKneeAngle <= LUNGE_BOTTOM_THRESHOLD -> 1f
            else -> 1f - (minKneeAngle - LUNGE_BOTTOM_THRESHOLD) / (LUNGE_STANDING_THRESHOLD - LUNGE_BOTTOM_THRESHOLD)
        }

        val newState = when (lungeCurrentState) {
            RepState.IDLE, RepState.STANDING -> {
                if (minKneeAngle >= LUNGE_STANDING_THRESHOLD) RepState.STANDING
                else RepState.DESCENDING
            }
            RepState.DESCENDING -> {
                when {
                    minKneeAngle <= LUNGE_BOTTOM_THRESHOLD -> RepState.THRESHOLD_REACHED
                    minKneeAngle >= LUNGE_STANDING_THRESHOLD -> RepState.STANDING
                    else -> RepState.DESCENDING
                }
            }
            RepState.THRESHOLD_REACHED -> {
                if (minKneeAngle > LUNGE_BOTTOM_THRESHOLD + 10f) RepState.ASCENDING
                else RepState.THRESHOLD_REACHED
            }
            RepState.ASCENDING -> {
                if (minKneeAngle >= LUNGE_STANDING_THRESHOLD) {
                    lungeRepCount++
                    RepState.REPETITION_CONFIRMED
                } else RepState.ASCENDING
            }
            RepState.REPETITION_CONFIRMED -> RepState.STANDING
        }
        lungeCurrentState = newState
        _lungeState.value = RepCounterState(lungeRepCount, lungeCurrentState, progress, newState == RepState.REPETITION_CONFIRMED)
    }

    fun processBicepCurl(result: PoseLandmarkResult) {
        val leftElbow = poseAnalysisManager.calculateElbowAngle(result.landmarks, BodySide.LEFT)
        val rightElbow = poseAnalysisManager.calculateElbowAngle(result.landmarks, BodySide.RIGHT)
        val avgElbow = (leftElbow + rightElbow) / 2f

        val progress = when {
            avgElbow >= BICEP_CURL_DOWN_THRESHOLD -> 0f
            avgElbow <= BICEP_CURL_UP_THRESHOLD -> 1f
            else -> 1f - (avgElbow - BICEP_CURL_UP_THRESHOLD) / (BICEP_CURL_DOWN_THRESHOLD - BICEP_CURL_UP_THRESHOLD)
        }

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
                if (avgElbow > BICEP_CURL_UP_THRESHOLD + 10f) RepState.ASCENDING
                else RepState.THRESHOLD_REACHED
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
        _bicepCurlState.value = RepCounterState(bicepCurlRepCount, bicepCurlCurrentState, progress, newState == RepState.REPETITION_CONFIRMED)
    }

    fun processShoulderPress(result: PoseLandmarkResult) {
        val leftElbow = poseAnalysisManager.calculateElbowAngle(result.landmarks, BodySide.LEFT)
        val rightElbow = poseAnalysisManager.calculateElbowAngle(result.landmarks, BodySide.RIGHT)
        val avgElbow = (leftElbow + rightElbow) / 2f

        val progress = when {
            avgElbow <= SHOULDER_PRESS_DOWN_THRESHOLD -> 0f
            avgElbow >= SHOULDER_PRESS_UP_THRESHOLD -> 1f
            else -> (avgElbow - SHOULDER_PRESS_DOWN_THRESHOLD) / (SHOULDER_PRESS_UP_THRESHOLD - SHOULDER_PRESS_DOWN_THRESHOLD)
        }

        val newState = when (shoulderPressCurrentState) {
            RepState.IDLE, RepState.STANDING -> {
                if (avgElbow <= SHOULDER_PRESS_DOWN_THRESHOLD) RepState.STANDING
                else RepState.ASCENDING
            }
            RepState.ASCENDING -> {
                when {
                    avgElbow >= SHOULDER_PRESS_UP_THRESHOLD -> RepState.THRESHOLD_REACHED
                    avgElbow <= SHOULDER_PRESS_DOWN_THRESHOLD -> RepState.STANDING
                    else -> RepState.ASCENDING
                }
            }
            RepState.THRESHOLD_REACHED -> {
                if (avgElbow < SHOULDER_PRESS_UP_THRESHOLD - 10f) RepState.DESCENDING
                else RepState.THRESHOLD_REACHED
            }
            RepState.DESCENDING -> {
                if (avgElbow <= SHOULDER_PRESS_DOWN_THRESHOLD) {
                    shoulderPressRepCount++
                    RepState.REPETITION_CONFIRMED
                } else RepState.DESCENDING
            }
            RepState.REPETITION_CONFIRMED -> RepState.STANDING
        }
        shoulderPressCurrentState = newState
        _shoulderPressState.value = RepCounterState(shoulderPressRepCount, shoulderPressCurrentState, progress, newState == RepState.REPETITION_CONFIRMED)
    }

    fun processPlank(result: PoseLandmarkResult) {
        if (result.landmarks.size < 33) {
            _plankState.value = RepCounterState(plankRepCount, plankCurrentState, 0f, false)
            return
        }
        val landmarks = result.landmarks
        val leftShoulder = landmarks[PoseLandmarkIndex.LEFT_SHOULDER]
        val rightShoulder = landmarks[PoseLandmarkIndex.RIGHT_SHOULDER]
        val leftHip = landmarks[PoseLandmarkIndex.LEFT_HIP]
        val rightHip = landmarks[PoseLandmarkIndex.RIGHT_HIP]
        val leftAnkle = landmarks[PoseLandmarkIndex.LEFT_ANKLE]
        val rightAnkle = landmarks[PoseLandmarkIndex.RIGHT_ANKLE]

        val shoulderY = (leftShoulder.y() + rightShoulder.y()) / 2f
        val hipY = (leftHip.y() + rightHip.y()) / 2f
        val ankleY = (leftAnkle.y() + rightAnkle.y()) / 2f

        val isPlankPosition = kotlin.math.abs(hipY - shoulderY) < 0.15f &&
            kotlin.math.abs(hipY - ankleY) < 0.2f

        val newState = when (plankCurrentState) {
            RepState.IDLE, RepState.STANDING -> {
                if (isPlankPosition) {
                    plankFramesHeld = 1
                    RepState.DESCENDING
                } else RepState.STANDING
            }
            RepState.DESCENDING -> {
                if (!isPlankPosition) {
                    plankFramesHeld = 0
                    RepState.STANDING
                } else {
                    plankFramesHeld++
                    if (plankFramesHeld >= PLANK_HOLD_FRAMES) RepState.THRESHOLD_REACHED
                    else RepState.DESCENDING
                }
            }
            RepState.THRESHOLD_REACHED -> {
                if (!isPlankPosition) {
                    plankRepCount++
                    plankFramesHeld = 0
                    RepState.REPETITION_CONFIRMED
                } else RepState.THRESHOLD_REACHED
            }
            RepState.REPETITION_CONFIRMED -> {
                plankFramesHeld = 0
                RepState.STANDING
            }
            RepState.ASCENDING -> RepState.STANDING
        }
        plankCurrentState = newState
        val plankProgress = if (newState == RepState.DESCENDING)
            (plankFramesHeld.toFloat() / PLANK_HOLD_FRAMES).coerceIn(0f, 1f)
        else if (newState == RepState.THRESHOLD_REACHED) 1f
        else 0f

        _plankState.value = RepCounterState(plankRepCount, plankCurrentState, plankProgress, newState == RepState.REPETITION_CONFIRMED)
    }

    fun reset(exerciseId: String) {
        when (exerciseId) {
            "squats" -> { squatRepCount = 0; squatCurrentState = RepState.IDLE; squatDebounceCount = 0; _squatState.value = RepCounterState() }
            "pushups" -> { pushupRepCount = 0; pushupCurrentState = RepState.IDLE; pushupDebounceCount = 0; _pushupState.value = RepCounterState() }
            "lunges" -> { lungeRepCount = 0; lungeCurrentState = RepState.IDLE; _lungeState.value = RepCounterState() }
            "bicep_curls" -> { bicepCurlRepCount = 0; bicepCurlCurrentState = RepState.IDLE; _bicepCurlState.value = RepCounterState() }
            "shoulder_press" -> { shoulderPressRepCount = 0; shoulderPressCurrentState = RepState.IDLE; _shoulderPressState.value = RepCounterState() }
            "plank" -> { plankRepCount = 0; plankCurrentState = RepState.IDLE; plankFramesHeld = 0; _plankState.value = RepCounterState() }
        }
    }
}
