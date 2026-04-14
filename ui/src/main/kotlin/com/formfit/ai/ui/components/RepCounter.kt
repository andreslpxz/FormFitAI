package com.formfit.ai.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.formfit.ai.ui.theme.FormFitGreen
import com.formfit.ai.ui.theme.TextMuted
import kotlinx.coroutines.delay

@Composable
fun RepCounter(
    count: Int,
    targetReps: Int,
    progressPercentage: Float,
    isRepJustCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    var flash by remember { mutableStateOf(false) }

    LaunchedEffect(isRepJustCompleted) {
        if (isRepJustCompleted) {
            flash = true
            delay(300)
            flash = false
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (flash) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "rep_scale"
    )

    val ringColor by animateColorAsState(
        targetValue = if (flash) Color.White else FormFitGreen,
        animationSpec = tween(200),
        label = "ring_color"
    )

    Box(
        modifier = modifier.scale(scale),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { if (targetReps > 0) (count.toFloat() / targetReps).coerceIn(0f, 1f) else 0f },
            modifier = Modifier.size(120.dp),
            color = ringColor,
            trackColor = Color.White.copy(alpha = 0.15f),
            strokeWidth = 6.dp,
            strokeCap = StrokeCap.Round
        )

        CircularProgressIndicator(
            progress = { progressPercentage.coerceIn(0f, 1f) },
            modifier = Modifier.size(104.dp),
            color = ringColor.copy(alpha = 0.4f),
            trackColor = Color.Transparent,
            strokeWidth = 3.dp,
            strokeCap = StrokeCap.Round
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = count,
                transitionSpec = {
                    slideInVertically { height -> -height } togetherWith
                    slideOutVertically { height -> height }
                },
                label = "rep_count_animation"
            ) { targetCount ->
                Text(
                    text = targetCount.toString(),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            if (targetReps > 0) {
                Text(
                    text = "/ $targetReps",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
fun WorkoutTimer(
    elapsedSeconds: Int,
    modifier: Modifier = Modifier
) {
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60

    Text(
        text = "%02d:%02d".format(minutes, seconds),
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = modifier
    )
}
