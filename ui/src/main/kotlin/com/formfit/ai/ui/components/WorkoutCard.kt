package com.formfit.ai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.formfit.ai.core.model.Difficulty
import com.formfit.ai.core.model.Exercise
import com.formfit.ai.ui.theme.CardBorder
import com.formfit.ai.ui.theme.ErrorRed
import com.formfit.ai.ui.theme.FormFitGreen
import com.formfit.ai.ui.theme.FormFitSurface
import com.formfit.ai.ui.theme.FormFitSurface2
import com.formfit.ai.ui.theme.TextMuted
import com.formfit.ai.ui.theme.TextSecondary
import com.formfit.ai.ui.theme.WarningAmber

@Composable
fun ExerciseCard(
    exercise: Exercise,
    onClick: () -> Unit,
    isProUser: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isLocked = exercise.isProOnly && !isProUser

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = !isLocked) { onClick() },
        color = FormFitSurface,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                FormFitGreen.copy(alpha = 0.15f),
                                FormFitGreen.copy(alpha = 0.05f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = exercise.category.emoji(), fontSize = 26.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (isLocked) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Pro",
                            tint = WarningAmber,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = exercise.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DifficultyChip(difficulty = exercise.difficulty)
                    MuscleChip(muscle = exercise.musclesTargeted.firstOrNull() ?: "")
                }
            }
        }
    }
}

@Composable
fun DifficultyChip(difficulty: Difficulty) {
    val color = when (difficulty) {
        Difficulty.BEGINNER -> FormFitGreen
        Difficulty.INTERMEDIATE -> WarningAmber
        Difficulty.ADVANCED -> ErrorRed
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = difficulty.label(),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
fun MuscleChip(muscle: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(FormFitSurface2)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = muscle,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
    }
}
