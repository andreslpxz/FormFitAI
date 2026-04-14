package com.formfit.ai.vision.model

enum class FormQualityLevel {
    GOOD,
    WARNING,
    ERROR
}

data class SegmentQuality(
    val startLandmark: Int,
    val endLandmark: Int,
    val quality: FormQualityLevel,
    val message: String? = null
)

data class OverallFormQuality(
    val score: Float,
    val segments: List<SegmentQuality>,
    val issues: List<String>,
    val suggestions: List<String>
) {
    val isGood: Boolean get() = score >= 0.75f
    val hasErrors: Boolean get() = segments.any { it.quality == FormQualityLevel.ERROR }

    companion object {
        fun perfect() = OverallFormQuality(
            score = 1.0f,
            segments = emptyList(),
            issues = emptyList(),
            suggestions = emptyList()
        )

        fun empty() = OverallFormQuality(
            score = 0f,
            segments = emptyList(),
            issues = emptyList(),
            suggestions = listOf("Get in position and face the camera")
        )
    }
}

data class JointAngle(
    val joint: String,
    val angle: Float,
    val side: BodySide,
    val isWithinRange: Boolean,
    val idealMin: Float,
    val idealMax: Float
)

enum class BodySide {
    LEFT, RIGHT, CENTER
}

enum class CalibrationState {
    NOT_STARTED,
    TOO_CLOSE,
    TOO_FAR,
    MOVE_LEFT,
    MOVE_RIGHT,
    CALIBRATED
}
