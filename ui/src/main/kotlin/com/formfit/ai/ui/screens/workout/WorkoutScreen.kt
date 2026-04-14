package com.formfit.ai.ui.screens.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.formfit.ai.core.model.ExerciseLibrary
import com.formfit.ai.ui.components.ExerciseCard
import com.formfit.ai.ui.theme.*

@Composable
fun WorkoutScreen(
    onNavigateToExercise: (String) -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FormFitNavy)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(GradientEnd)
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 20.dp)
        ) {
            Column {
                Text("💪 Train", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text("Choose your exercise", fontSize = 14.sp, color = TextMuted)
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    "All Exercises",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            items(ExerciseLibrary) { exercise ->
                ExerciseCard(
                    exercise = exercise,
                    onClick = { onNavigateToExercise(exercise.id) }
                )
            }
        }
    }
}
