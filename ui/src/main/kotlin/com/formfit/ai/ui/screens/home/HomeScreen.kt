package com.formfit.ai.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.formfit.ai.core.model.ExerciseCategory
import com.formfit.ai.core.model.ExerciseLibrary
import com.formfit.ai.ui.components.ExerciseCard
import com.formfit.ai.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateToExercise: (String) -> Unit,
    onNavigateToRoutines: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FormFitNavy),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(GradientEnd, FormFitNavy))
                    )
                    .padding(horizontal = 24.dp)
                    .padding(top = 48.dp, bottom = 24.dp)
            ) {
                Column {
                    Text(
                        text = "Good ${getGreeting()}, ${uiState.userName.ifBlank { "Athlete" }} 👋",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Ready to train with perfect form?",
                        fontSize = 14.sp,
                        color = TextMuted
                    )

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickStatCard(
                            label = "Workouts",
                            value = "${uiState.weeklyWorkouts}",
                            unit = "this week",
                            modifier = Modifier.weight(1f)
                        )
                        QuickStatCard(
                            label = "Streak",
                            value = "${uiState.currentStreak}",
                            unit = "days",
                            modifier = Modifier.weight(1f)
                        )
                        QuickStatCard(
                            label = "Form Score",
                            value = "${(uiState.avgFormScore * 100).toInt()}%",
                            unit = "avg",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Your Routines", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                TextButton(onClick = onNavigateToRoutines) {
                    Text("See All", color = FormFitGreen, fontSize = 13.sp)
                }
            }
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.featuredRoutines) { routine ->
                    RoutineCard(
                        routine = routine,
                        onClick = { onNavigateToRoutines() }
                    )
                }
                item {
                    AddRoutineCard(onClick = onNavigateToRoutines)
                }
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Exercises", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(Modifier.height(8.dp))
            CategoryFilter(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = viewModel::setCategory
            )
        }

        val filteredExercises = if (uiState.selectedCategory == null) {
            ExerciseLibrary
        } else {
            ExerciseLibrary.filter { it.category == uiState.selectedCategory }
        }

        items(filteredExercises) { exercise ->
            ExerciseCard(
                exercise = exercise,
                onClick = { onNavigateToExercise(exercise.id) },
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 5.dp)
            )
        }
    }
}

@Composable
fun QuickStatCard(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = FormFitSurface,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = FormFitGreen)
            Text(unit, fontSize = 10.sp, color = TextMuted)
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
        }
    }
}

@Composable
fun RoutineCard(routine: com.formfit.ai.core.model.Routine, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .width(160.dp)
            .height(110.dp),
        color = FormFitSurface2,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "${routine.estimatedMinutes} min",
                fontSize = 11.sp,
                color = FormFitGreen,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(routine.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 2)
            Spacer(Modifier.weight(1f))
            Text("${routine.exercises.size} exercises", fontSize = 11.sp, color = TextMuted)
        }
    }
}

@Composable
fun AddRoutineCard(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .width(100.dp)
            .height(110.dp),
        color = FormFitSurface,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("+", fontSize = 28.sp, fontWeight = FontWeight.Black, color = FormFitGreen)
            Text("New", fontSize = 11.sp, color = TextMuted)
        }
    }
}

@Composable
fun CategoryFilter(
    selectedCategory: ExerciseCategory?,
    onCategorySelected: (ExerciseCategory?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("All") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = FormFitGreen,
                    selectedLabelColor = FormFitNavy
                )
            )
        }
        items(ExerciseCategory.values().toList()) { cat ->
            FilterChip(
                selected = selectedCategory == cat,
                onClick = { onCategorySelected(cat) },
                label = { Text("${cat.emoji()} ${cat.label()}") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = FormFitGreen,
                    selectedLabelColor = FormFitNavy
                )
            )
        }
    }
}

private fun getGreeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Morning"
        hour < 17 -> "Afternoon"
        else -> "Evening"
    }
}
