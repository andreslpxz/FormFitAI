package com.formfit.ai.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.formfit.ai.ui.theme.ErrorRed
import com.formfit.ai.ui.theme.FormFitGreen
import com.formfit.ai.ui.theme.SkeletonAmber
import com.formfit.ai.ui.theme.SkeletonWhite
import com.formfit.ai.vision.model.FormQualityLevel
import com.formfit.ai.vision.model.OverallFormQuality
import com.formfit.ai.vision.model.PoseLandmarkIndex
import com.formfit.ai.vision.model.PoseLandmarkResult
import com.formfit.ai.vision.model.SKELETON_CONNECTIONS
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark

@Composable
fun SkeletonOverlay(
    poseResult: PoseLandmarkResult?,
    formQuality: OverallFormQuality,
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(
        targetValue = if (poseResult != null) 1f else 0f,
        animationSpec = tween(300),
        label = "skeleton_alpha"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        if (poseResult == null || poseResult.landmarks.isEmpty()) return@Canvas

        val landmarks = poseResult.landmarks
        val imageWidth = poseResult.imageWidth.toFloat()
        val imageHeight = poseResult.imageHeight.toFloat()

        val scaleX = size.width / imageWidth
        val scaleY = size.height / imageHeight

        drawConnections(
            drawScope = this,
            landmarks = landmarks,
            formQuality = formQuality,
            scaleX = scaleX,
            scaleY = scaleY,
            alpha = alpha
        )

        drawLandmarkPoints(
            drawScope = this,
            landmarks = landmarks,
            formQuality = formQuality,
            scaleX = scaleX,
            scaleY = scaleY,
            alpha = alpha
        )
    }
}

private fun drawConnections(
    drawScope: DrawScope,
    landmarks: List<NormalizedLandmark>,
    formQuality: OverallFormQuality,
    scaleX: Float,
    scaleY: Float,
    alpha: Float
) {
    for ((startIdx, endIdx) in SKELETON_CONNECTIONS) {
        if (startIdx >= landmarks.size || endIdx >= landmarks.size) continue

        val startLandmark = landmarks[startIdx]
        val endLandmark = landmarks[endIdx]

        if (startLandmark.visibility().orElse(0f) < 0.5f ||
            endLandmark.visibility().orElse(0f) < 0.5f) continue

        val segmentQuality = formQuality.segments.find {
            (it.startLandmark == startIdx && it.endLandmark == endIdx) ||
            (it.startLandmark == endIdx && it.endLandmark == startIdx)
        }

        val lineColor = when (segmentQuality?.quality) {
            FormQualityLevel.ERROR -> ErrorRed
            FormQualityLevel.WARNING -> SkeletonAmber
            FormQualityLevel.GOOD -> FormFitGreen
            null -> SkeletonWhite
        }.copy(alpha = alpha * 0.9f)

        val start = Offset(
            x = startLandmark.x() * drawScope.size.width,
            y = startLandmark.y() * drawScope.size.height
        )
        val end = Offset(
            x = endLandmark.x() * drawScope.size.width,
            y = endLandmark.y() * drawScope.size.height
        )

        drawScope.drawLine(
            color = lineColor,
            start = start,
            end = end,
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )
    }
}

private fun drawLandmarkPoints(
    drawScope: DrawScope,
    landmarks: List<NormalizedLandmark>,
    formQuality: OverallFormQuality,
    scaleX: Float,
    scaleY: Float,
    alpha: Float
) {
    val importantLandmarks = setOf(
        PoseLandmarkIndex.LEFT_SHOULDER, PoseLandmarkIndex.RIGHT_SHOULDER,
        PoseLandmarkIndex.LEFT_ELBOW, PoseLandmarkIndex.RIGHT_ELBOW,
        PoseLandmarkIndex.LEFT_WRIST, PoseLandmarkIndex.RIGHT_WRIST,
        PoseLandmarkIndex.LEFT_HIP, PoseLandmarkIndex.RIGHT_HIP,
        PoseLandmarkIndex.LEFT_KNEE, PoseLandmarkIndex.RIGHT_KNEE,
        PoseLandmarkIndex.LEFT_ANKLE, PoseLandmarkIndex.RIGHT_ANKLE
    )

    for ((index, landmark) in landmarks.withIndex()) {
        if (!importantLandmarks.contains(index)) continue
        if (landmark.visibility().orElse(0f) < 0.5f) continue

        val hasError = formQuality.segments.any { seg ->
            (seg.startLandmark == index || seg.endLandmark == index) &&
            seg.quality == FormQualityLevel.ERROR
        }

        val pointColor = if (hasError) ErrorRed else FormFitGreen
        val position = Offset(
            x = landmark.x() * drawScope.size.width,
            y = landmark.y() * drawScope.size.height
        )

        drawScope.drawCircle(
            color = Color.White.copy(alpha = alpha * 0.9f),
            radius = 8f,
            center = position
        )
        drawScope.drawCircle(
            color = pointColor.copy(alpha = alpha),
            radius = 6f,
            center = position
        )
    }
}
