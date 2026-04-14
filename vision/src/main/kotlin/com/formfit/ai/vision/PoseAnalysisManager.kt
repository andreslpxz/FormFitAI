package com.formfit.ai.vision

import com.formfit.ai.vision.model.BodySide
import com.formfit.ai.vision.model.CalibrationState
import com.formfit.ai.vision.model.FormQualityLevel
import com.formfit.ai.vision.model.JointAngle
import com.formfit.ai.vision.model.OverallFormQuality
import com.formfit.ai.vision.model.PoseLandmarkIndex
import com.formfit.ai.vision.model.PoseLandmarkResult
import com.formfit.ai.vision.model.SegmentQuality
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

@Singleton
class PoseAnalysisManager @Inject constructor() {

    fun calculateAngle(
        a: NormalizedLandmark,
        b: NormalizedLandmark,
        c: NormalizedLandmark
    ): Float {
        val ax = a.x() - b.x()
        val ay = a.y() - b.y()
        val cx = c.x() - b.x()
        val cy = c.y() - b.y()

        val dot = ax * cx + ay * cy
        val magA = sqrt(ax.pow(2) + ay.pow(2))
        val magC = sqrt(cx.pow(2) + cy.pow(2))

        if (magA == 0f || magC == 0f) return 0f

        val cosAngle = (dot / (magA * magC)).coerceIn(-1f, 1f)
        return Math.toDegrees(acos(cosAngle).toDouble()).toFloat()
    }

    fun calculateKneeAngle(
        landmarks: List<NormalizedLandmark>,
        side: BodySide
    ): Float {
        val hipIdx = if (side == BodySide.LEFT) PoseLandmarkIndex.LEFT_HIP else PoseLandmarkIndex.RIGHT_HIP
        val kneeIdx = if (side == BodySide.LEFT) PoseLandmarkIndex.LEFT_KNEE else PoseLandmarkIndex.RIGHT_KNEE
        val ankleIdx = if (side == BodySide.LEFT) PoseLandmarkIndex.LEFT_ANKLE else PoseLandmarkIndex.RIGHT_ANKLE

        if (landmarks.size <= max(hipIdx, max(kneeIdx, ankleIdx))) return 180f

        return calculateAngle(
            landmarks[hipIdx],
            landmarks[kneeIdx],
            landmarks[ankleIdx]
        )
    }

    fun calculateHipAngle(
        landmarks: List<NormalizedLandmark>,
        side: BodySide
    ): Float {
        val shoulderIdx = if (side == BodySide.LEFT) PoseLandmarkIndex.LEFT_SHOULDER else PoseLandmarkIndex.RIGHT_SHOULDER
        val hipIdx = if (side == BodySide.LEFT) PoseLandmarkIndex.LEFT_HIP else PoseLandmarkIndex.RIGHT_HIP
        val kneeIdx = if (side == BodySide.LEFT) PoseLandmarkIndex.LEFT_KNEE else PoseLandmarkIndex.RIGHT_KNEE

        if (landmarks.size <= max(shoulderIdx, max(hipIdx, kneeIdx))) return 180f

        return calculateAngle(
            landmarks[shoulderIdx],
            landmarks[hipIdx],
            landmarks[kneeIdx]
        )
    }

    fun calculateElbowAngle(
        landmarks: List<NormalizedLandmark>,
        side: BodySide
    ): Float {
        val shoulderIdx = if (side == BodySide.LEFT) PoseLandmarkIndex.LEFT_SHOULDER else PoseLandmarkIndex.RIGHT_SHOULDER
        val elbowIdx = if (side == BodySide.LEFT) PoseLandmarkIndex.LEFT_ELBOW else PoseLandmarkIndex.RIGHT_ELBOW
        val wristIdx = if (side == BodySide.LEFT) PoseLandmarkIndex.LEFT_WRIST else PoseLandmarkIndex.RIGHT_WRIST

        if (landmarks.size <= max(shoulderIdx, max(elbowIdx, wristIdx))) return 180f

        return calculateAngle(
            landmarks[shoulderIdx],
            landmarks[elbowIdx],
            landmarks[wristIdx]
        )
    }

    fun calculateShoulderAngle(
        landmarks: List<NormalizedLandmark>,
        side: BodySide
    ): Float {
        val elbowIdx = if (side == BodySide.LEFT) PoseLandmarkIndex.LEFT_ELBOW else PoseLandmarkIndex.RIGHT_ELBOW
        val shoulderIdx = if (side == BodySide.LEFT) PoseLandmarkIndex.LEFT_SHOULDER else PoseLandmarkIndex.RIGHT_SHOULDER
        val hipIdx = if (side == BodySide.LEFT) PoseLandmarkIndex.LEFT_HIP else PoseLandmarkIndex.RIGHT_HIP

        if (landmarks.size <= max(shoulderIdx, max(elbowIdx, hipIdx))) return 180f

        return calculateAngle(
            landmarks[elbowIdx],
            landmarks[shoulderIdx],
            landmarks[hipIdx]
        )
    }

    fun analyzeSquatForm(landmarks: List<NormalizedLandmark>): OverallFormQuality {
        if (landmarks.isEmpty()) return OverallFormQuality.empty()

        val segments = mutableListOf<SegmentQuality>()
        val issues = mutableListOf<String>()
        val suggestions = mutableListOf<String>()
        var totalScore = 1.0f

        val leftKneeAngle = calculateKneeAngle(landmarks, BodySide.LEFT)
        val rightKneeAngle = calculateKneeAngle(landmarks, BodySide.RIGHT)
        val leftHipAngle = calculateHipAngle(landmarks, BodySide.LEFT)
        val rightHipAngle = calculateHipAngle(landmarks, BodySide.RIGHT)

        val leftKneeQuality = when {
            leftKneeAngle < 60f -> {
                issues.add("Left knee angle too deep")
                suggestions.add("Don't go below parallel on left side")
                totalScore -= 0.2f
                FormQualityLevel.WARNING
            }
            leftKneeAngle > 170f -> FormQualityLevel.GOOD
            else -> FormQualityLevel.GOOD
        }

        val rightKneeQuality = when {
            rightKneeAngle < 60f -> {
                issues.add("Right knee angle too deep")
                suggestions.add("Don't go below parallel on right side")
                totalScore -= 0.2f
                FormQualityLevel.WARNING
            }
            rightKneeAngle > 170f -> FormQualityLevel.GOOD
            else -> FormQualityLevel.GOOD
        }

        if (landmarks.size > PoseLandmarkIndex.RIGHT_KNEE) {
            val leftKnee = landmarks[PoseLandmarkIndex.LEFT_KNEE]
            val rightKnee = landmarks[PoseLandmarkIndex.RIGHT_KNEE]
            val leftAnkle = landmarks[PoseLandmarkIndex.LEFT_ANKLE]
            val rightAnkle = landmarks[PoseLandmarkIndex.RIGHT_ANKLE]

            if (leftKnee.x() < leftAnkle.x() - 0.05f) {
                issues.add("Left knee caving inward (valgus)")
                suggestions.add("Push left knee outward over toes")
                segments.add(SegmentQuality(
                    PoseLandmarkIndex.LEFT_HIP, PoseLandmarkIndex.LEFT_KNEE,
                    FormQualityLevel.ERROR, "Knee valgus detected"
                ))
                totalScore -= 0.25f
            }

            if (rightKnee.x() > rightAnkle.x() + 0.05f) {
                issues.add("Right knee caving inward (valgus)")
                suggestions.add("Push right knee outward over toes")
                segments.add(SegmentQuality(
                    PoseLandmarkIndex.RIGHT_HIP, PoseLandmarkIndex.RIGHT_KNEE,
                    FormQualityLevel.ERROR, "Knee valgus detected"
                ))
                totalScore -= 0.25f
            }
        }

        segments.add(SegmentQuality(
            PoseLandmarkIndex.LEFT_HIP, PoseLandmarkIndex.LEFT_KNEE, leftKneeQuality
        ))
        segments.add(SegmentQuality(
            PoseLandmarkIndex.RIGHT_HIP, PoseLandmarkIndex.RIGHT_KNEE, rightKneeQuality
        ))

        if (issues.isEmpty()) {
            suggestions.add("Great form! Keep it up!")
        }

        return OverallFormQuality(
            score = totalScore.coerceIn(0f, 1f),
            segments = segments,
            issues = issues,
            suggestions = suggestions
        )
    }

    fun analyzePushupForm(landmarks: List<NormalizedLandmark>): OverallFormQuality {
        if (landmarks.isEmpty()) return OverallFormQuality.empty()

        val segments = mutableListOf<SegmentQuality>()
        val issues = mutableListOf<String>()
        val suggestions = mutableListOf<String>()
        var totalScore = 1.0f

        val leftElbowAngle = calculateElbowAngle(landmarks, BodySide.LEFT)
        val rightElbowAngle = calculateElbowAngle(landmarks, BodySide.RIGHT)

        if (landmarks.size > PoseLandmarkIndex.RIGHT_ANKLE) {
            val leftShoulder = landmarks[PoseLandmarkIndex.LEFT_SHOULDER]
            val leftHip = landmarks[PoseLandmarkIndex.LEFT_HIP]
            val leftAnkle = landmarks[PoseLandmarkIndex.LEFT_ANKLE]
            val bodyAngle = calculateAngle(leftShoulder, leftHip, leftAnkle)

            if (abs(bodyAngle - 180f) > 20f) {
                issues.add("Keep your body in a straight line")
                suggestions.add("Engage core and don't let hips sag or raise")
                segments.add(SegmentQuality(
                    PoseLandmarkIndex.LEFT_SHOULDER, PoseLandmarkIndex.LEFT_HIP,
                    FormQualityLevel.ERROR, "Body not straight"
                ))
                totalScore -= 0.3f
            }
        }

        if (leftElbowAngle < 160f && rightElbowAngle < 160f) {
            val elbowQuality = if (leftElbowAngle < 60f) FormQualityLevel.WARNING else FormQualityLevel.GOOD
            segments.add(SegmentQuality(PoseLandmarkIndex.LEFT_SHOULDER, PoseLandmarkIndex.LEFT_ELBOW, elbowQuality))
            segments.add(SegmentQuality(PoseLandmarkIndex.RIGHT_SHOULDER, PoseLandmarkIndex.RIGHT_ELBOW, elbowQuality))
        }

        if (issues.isEmpty()) {
            suggestions.add("Excellent push-up form!")
        }

        return OverallFormQuality(
            score = totalScore.coerceIn(0f, 1f),
            segments = segments,
            issues = issues,
            suggestions = suggestions
        )
    }

    fun analyzeLungeForm(landmarks: List<NormalizedLandmark>): OverallFormQuality {
        if (landmarks.isEmpty()) return OverallFormQuality.empty()

        val segments = mutableListOf<SegmentQuality>()
        val issues = mutableListOf<String>()
        val suggestions = mutableListOf<String>()
        var totalScore = 1.0f

        val leftKneeAngle = calculateKneeAngle(landmarks, BodySide.LEFT)
        val rightKneeAngle = calculateKneeAngle(landmarks, BodySide.RIGHT)

        val frontKneeAngle = min(leftKneeAngle, rightKneeAngle)
        if (frontKneeAngle > 110f) {
            issues.add("Step further forward for deeper lunge")
            suggestions.add("Aim for 90° angle at front knee")
            totalScore -= 0.2f
        }

        if (issues.isEmpty()) {
            suggestions.add("Good lunge depth!")
        }

        return OverallFormQuality(
            score = totalScore.coerceIn(0f, 1f),
            segments = segments,
            issues = issues,
            suggestions = suggestions
        )
    }

    fun checkCalibration(result: PoseLandmarkResult): CalibrationState {
        if (result.landmarks.isEmpty()) return CalibrationState.NOT_STARTED

        val landmarks = result.landmarks

        if (landmarks.size <= PoseLandmarkIndex.LEFT_ANKLE) return CalibrationState.NOT_STARTED

        val topY = landmarks[PoseLandmarkIndex.NOSE].y()
        val bottomY = landmarks[PoseLandmarkIndex.LEFT_ANKLE].y()
        val bodyHeight = bottomY - topY

        if (bodyHeight < 0.4f) return CalibrationState.TOO_CLOSE
        if (bodyHeight < 0.55f) return CalibrationState.TOO_FAR

        val centerX = (landmarks[PoseLandmarkIndex.LEFT_SHOULDER].x() +
                landmarks[PoseLandmarkIndex.RIGHT_SHOULDER].x()) / 2f
        if (centerX < 0.35f) return CalibrationState.MOVE_RIGHT
        if (centerX > 0.65f) return CalibrationState.MOVE_LEFT

        return CalibrationState.CALIBRATED
    }

    fun getCalibrationMessage(state: CalibrationState): String = when (state) {
        CalibrationState.NOT_STARTED -> "Stand in front of the camera"
        CalibrationState.TOO_CLOSE -> "Step back a bit"
        CalibrationState.TOO_FAR -> "Step closer to the camera"
        CalibrationState.MOVE_LEFT -> "Move slightly to the left"
        CalibrationState.MOVE_RIGHT -> "Move slightly to the right"
        CalibrationState.CALIBRATED -> "Perfect! You're ready to start"
    }
}
