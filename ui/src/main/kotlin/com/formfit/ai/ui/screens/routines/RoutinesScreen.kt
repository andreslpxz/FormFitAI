package com.formfit.ai.ui.screens.routines

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.formfit.ai.core.data.RoutineDao
import com.formfit.ai.core.model.Difficulty
import com.formfit.ai.core.model.PresetRoutines
import com.formfit.ai.core.model.Routine
import com.formfit.ai.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RoutinesViewModel @Inject constructor(
    private val routineDao: RoutineDao
) : ViewModel() {

    val userRoutines: StateFlow<List<Routine>> = routineDao.getUserRoutines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@Composable
fun RoutinesScreen(
    onNavigateToRoutineBuilder: (String) -> Unit,
    onStartRoutine: (String) -> Unit,
    viewModel: RoutinesViewModel = hiltViewModel()
) {
    val userRoutines by viewModel.userRoutines.collectAsState()

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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 48.dp, bottom = 16.dp)
                ) {
                    Text(
                        text = "Routines",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Preset and custom workout plans",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            }

            item {
                SectionHeader(title = "Preset Routines")
            }

            items(PresetRoutines) { routine ->
                RoutineCard(
                    routine = routine,
                    onClick = { onStartRoutine(routine.exercises.firstOrNull()?.exerciseId ?: "squats") }
                )
            }

            if (userRoutines.isNotEmpty()) {
                item {
                    SectionHeader(title = "My Routines")
                }
                items(userRoutines) { routine ->
                    RoutineCard(
                        routine = routine,
                        onClick = { onStartRoutine(routine.exercises.firstOrNull()?.exerciseId ?: "squats") },
                        onEdit = { onNavigateToRoutineBuilder(routine.id) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { onNavigateToRoutineBuilder("new") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = FormFitTeal,
            contentColor = Color.White
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Create routine")
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = TextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
private fun RoutineCard(
    routine: Routine,
    onClick: () -> Unit,
    onEdit: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(SurfaceVariant, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(FormFitTeal.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.FitnessCenter,
                contentDescription = null,
                tint = FormFitTeal,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = routine.name,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccessTime,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${routine.estimatedMinutes} min",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = "·",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                Text(
                    text = "${routine.exercises.size} exercises",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                Text(
                    text = "·",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                DifficultyBadge(difficulty = routine.difficulty)
            }
        }

        if (onEdit != null) {
            TextButton(onClick = onEdit) {
                Text("Edit", color = FormFitTeal, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun DifficultyBadge(difficulty: Difficulty) {
    val color = when (difficulty) {
        Difficulty.BEGINNER -> Color(0xFF4CAF50)
        Difficulty.INTERMEDIATE -> Color(0xFFFF9800)
        Difficulty.ADVANCED -> Color(0xFFF44336)
    }
    Text(
        text = difficulty.label(),
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold
    )
}
