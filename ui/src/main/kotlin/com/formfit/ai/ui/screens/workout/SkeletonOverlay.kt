package com.formfit.ai.ui.screens.workout

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.formfit.ai.vision.model.FormQualityLevel
import com.formfit.ai.vision.model.OverallFormQuality
import com.formfit.ai.vision.model.PoseLandmarkResult
import com.formfit.ai.vision.model.SKELETON_CONNECTIONS
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark

private val COLOR_GOOD = Color(0xFF00E676)
private val COLOR_WARNING = Color(0xFFFFB74D)
private val COLOR_ERROR = Color(0xFFEF5350)
private val COLOR_LANDMARK = Color(0xFFFFFFFF)
private val COLOR_DEFAULT_BONE = Color(0xB3FFFFFF)

@Composable
fun SkeletonOverlay(
    poseResult: PoseLandmarkResult?,
    formQuality: OverallFormQuality?,
    modifier: Modifier = Modifier,
    isMirrored: Boolean = true
) {
    val segmentQualityMap = remember(formQuality) {
        formQuality?.segments?.associate {
            Pair(it.startLandmark, it.endLandmark) to it.quality
        } ?: emptyMap()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val result = poseResult ?: return@Canvas
        if (result.landmarks.isEmpty()) return@Canvas

        val landmarks = result.landmarks
        val canvasWidth = size.width
        val canvasHeight = size.height

        fun landmarkToOffset(lm: NormalizedLandmark): Offset {
            val x = if (isMirrored) (1f - lm.x()) * canvasWidth else lm.x() * canvasWidth
            val y = lm.y() * canvasHeight
            return Offset(x, y)
        }

        for ((startIdx, endIdx) in SKELETON_CONNECTIONS) {
            if (startIdx >= landmarks.size || endIdx >= landmarks.size) continue

            val startLm = landmarks[startIdx]
            val endLm = landmarks[endIdx]

            if ((startLm.visibility().isPresent && startLm.visibility().get() < 0.5f) ||
                (endLm.visibility().isPresent && endLm.visibility().get() < 0.5f)) continue

            val quality = segmentQualityMap[Pair(startIdx, endIdx)]
                ?: segmentQualityMap[Pair(endIdx, startIdx)]

            val boneColor = when (quality) {
                FormQualityLevel.GOOD -> COLOR_GOOD
                FormQualityLevel.WARNING -> COLOR_WARNING
                FormQualityLevel.ERROR -> COLOR_ERROR
                null -> COLOR_DEFAULT_BONE
            }

            drawBone(
                start = landmarkToOffset(startLm),
                end = landmarkToOffset(endLm),
                color = boneColor
            )
        }

        for (i in landmarks.indices) {
            val lm = landmarks[i]
            if (lm.visibility().isPresent && lm.visibility().get() < 0.5f) continue

            val offset = landmarkToOffset(lm)
            drawLandmark(offset, COLOR_LANDMARK)
        }
    }
}

private fun DrawScope.drawBone(start: Offset, end: Offset, color: Color) {
    drawLine(
        color = color.copy(alpha = 0.85f),
        start = start,
        end = end,
        strokeWidth = 6f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawLandmark(offset: Offset, color: Color) {
    drawCircle(
        color = color,
        radius = 7f,
        center = offset
    )
    drawCircle(
        color = Color.Black.copy(alpha = 0.5f),
        radius = 4f,
        center = offset
    )
}
