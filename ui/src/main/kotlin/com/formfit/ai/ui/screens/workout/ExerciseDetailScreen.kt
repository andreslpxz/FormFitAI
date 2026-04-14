package com.formfit.ai.ui.screens.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.formfit.ai.core.model.Difficulty
import com.formfit.ai.core.model.Exercise
import com.formfit.ai.core.model.ExerciseLibrary
import com.formfit.ai.ui.theme.*

@Composable
fun ExerciseDetailScreen(
    exerciseId: String,
    onBack: () -> Unit,
    onStartWorkout: (String) -> Unit
) {
    val exercise = ExerciseLibrary.find { it.id == exerciseId }
        ?: return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FormFitNavy)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                ExerciseHeroHeader(exercise = exercise, onBack = onBack)
            }

            item {
                Spacer(Modifier.height(24.dp))
                Column(Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        text = exercise.description,
                        color = TextMuted,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )

                    Spacer(Modifier.height(20.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ExerciseChip(label = exercise.difficulty.label())
                        ExerciseChip(label = "${exercise.defaultSets} sets × ${exercise.defaultReps} reps")
                        ExerciseChip(label = "${exercise.caloriesPerMinute.toInt()} cal/min")
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "Muscles Worked",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        exercise.musclesTargeted.take(4).forEach { muscle ->
                            MuscleChip(muscle = muscle)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "Form Tips",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }

            items(exercise.formTips) { tip ->
                FormTipRow(tip = tip)
            }
        }

        Button(
            onClick = { onStartWorkout(exerciseId) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FormFitTeal),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Start Workout",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ExerciseHeroHeader(exercise: Exercise, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(GradientEnd, FormFitNavy)
                )
            )
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            Text(
                text = exercise.category.emoji() + " " + exercise.category.label(),
                color = FormFitTeal,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = exercise.name,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun FormTipRow(tip: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = FormFitTeal,
            modifier = Modifier
                .size(18.dp)
                .padding(top = 2.dp)
        )
        Text(
            text = tip,
            color = TextMuted,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun ExerciseChip(label: String) {
    Box(
        modifier = Modifier
            .background(SurfaceVariant, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text = label, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun MuscleChip(muscle: String) {
    Box(
        modifier = Modifier
            .background(FormFitTeal.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text = muscle, color = FormFitTeal, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
