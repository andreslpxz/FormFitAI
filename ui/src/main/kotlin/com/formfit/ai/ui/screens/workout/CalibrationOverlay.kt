package com.formfit.ai.ui.screens.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.formfit.ai.vision.model.CalibrationState

@Composable
fun CalibrationOverlay(
    calibrationState: CalibrationState,
    modifier: Modifier = Modifier
) {
    val isCalibrated = calibrationState == CalibrationState.CALIBRATED
    val isActive = calibrationState != CalibrationState.CALIBRATED

    AnimatedVisibility(
        visible = isActive,
        enter = fadeIn(tween(300)),
        exit = fadeOut(tween(500)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            BodyFrameGuide()

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 48.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CalibrationStatusChip(calibrationState)

                Spacer(Modifier.height(4.dp))

                Text(
                    text = getCalibrationMessage(calibrationState),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Position yourself so your full body is visible",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BodyFrameGuide() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 80.dp, vertical = 40.dp)
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 4f
            val cornerLength = 40f
            val color = androidx.compose.ui.graphics.Color(0x99FFFFFF)

            val w = size.width
            val h = size.height

            val corners = listOf(
                Triple(0f, 0f, Pair(cornerLength, 0f) to Pair(0f, cornerLength)),
                Triple(w, 0f, Pair(-cornerLength, 0f) to Pair(0f, cornerLength)),
                Triple(0f, h, Pair(cornerLength, 0f) to Pair(0f, -cornerLength)),
                Triple(w, h, Pair(-cornerLength, 0f) to Pair(0f, -cornerLength))
            )

            corners.forEach { (x, y, lines) ->
                val start = androidx.compose.ui.geometry.Offset(x, y)
                drawLine(color, start,
                    androidx.compose.ui.geometry.Offset(x + lines.first.first, y + lines.first.second),
                    strokeWidth, androidx.compose.ui.graphics.StrokeCap.Round)
                drawLine(color, start,
                    androidx.compose.ui.geometry.Offset(x + lines.second.first, y + lines.second.second),
                    strokeWidth, androidx.compose.ui.graphics.StrokeCap.Round)
            }
        }
    }
}

@Composable
private fun CalibrationStatusChip(state: CalibrationState) {
    val (label, color) = when (state) {
        CalibrationState.NOT_STARTED -> "Detecting..." to Color(0xFF9E9E9E)
        CalibrationState.CALIBRATED -> "Ready!" to Color(0xFF4CAF50)
        CalibrationState.TOO_CLOSE -> "Too Close" to Color(0xFFF44336)
        CalibrationState.TOO_FAR -> "Too Far" to Color(0xFFFF9800)
        CalibrationState.MOVE_LEFT, CalibrationState.MOVE_RIGHT -> "Adjust" to Color(0xFF2196F3)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.9f))
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun getCalibrationMessage(state: CalibrationState): String = when (state) {
    CalibrationState.NOT_STARTED -> "Stand in front of the camera"
    CalibrationState.TOO_CLOSE -> "Step back a little"
    CalibrationState.TOO_FAR -> "Step closer to the camera"
    CalibrationState.MOVE_LEFT -> "Move slightly to the left"
    CalibrationState.MOVE_RIGHT -> "Move slightly to the right"
    CalibrationState.CALIBRATED -> "You're ready to start!"
}
