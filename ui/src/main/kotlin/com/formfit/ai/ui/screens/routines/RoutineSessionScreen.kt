package com.formfit.ai.ui.screens.routines

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.PlayArrow
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
import com.formfit.ai.core.model.PresetRoutines
import com.formfit.ai.core.model.Routine
import com.formfit.ai.core.model.RoutineExercise
import com.formfit.ai.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoutineSessionUiState(
    val routine: Routine? = null,
    val currentExerciseIndex: Int = 0,
    val completedIndices: Set<Int> = emptySet(),
    val isLoading: Boolean = true
)

@HiltViewModel
class RoutineSessionViewModel @Inject constructor(
    private val routineDao: RoutineDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutineSessionUiState())
    val uiState: StateFlow<RoutineSessionUiState> = _uiState.asStateFlow()

    fun loadRoutine(routineId: String) {
        viewModelScope.launch {
            val routine = routineDao.getRoutineById(routineId)
                ?: PresetRoutines.firstOrNull { it.id == routineId }
            _uiState.update { it.copy(routine = routine, isLoading = false) }
        }
    }

    fun markExerciseDone(index: Int) {
        _uiState.update { state ->
            val newCompleted = state.completedIndices + index
            val nextIndex = (index + 1).coerceAtMost((state.routine?.exercises?.size ?: 1) - 1)
            state.copy(
                completedIndices = newCompleted,
                currentExerciseIndex = if (newCompleted.contains(nextIndex)) nextIndex else nextIndex
            )
        }
    }

    fun setCurrentExercise(index: Int) {
        _uiState.update { it.copy(currentExerciseIndex = index) }
    }

    val isRoutineComplete: Boolean
        get() {
            val state = _uiState.value
            val total = state.routine?.exercises?.size ?: 0
            return total > 0 && state.completedIndices.size >= total
        }
}

@Composable
fun RoutineSessionScreen(
    routineId: String,
    onBack: () -> Unit,
    onStartExercise: (String) -> Unit,
    onRoutineComplete: () -> Unit,
    viewModel: RoutineSessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(routineId) {
        viewModel.loadRoutine(routineId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FormFitNavy)
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                color = FormFitTeal,
                modifier = Modifier.align(Alignment.Center)
            )
            return@Box
        }

        val routine = uiState.routine
        if (routine == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Routine not found", color = Color.White)
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onBack) { Text("Go Back", color = FormFitTeal) }
            }
            return@Box
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Column {
                        Text(
                            text = routine.name,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.completedIndices.size} / ${routine.exercises.size} exercises done",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            item {
                LinearProgressIndicator(
                    progress = {
                        if (routine.exercises.isEmpty()) 0f
                        else uiState.completedIndices.size.toFloat() / routine.exercises.size
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(4.dp),
                    color = FormFitTeal,
                    trackColor = SurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
            }

            itemsIndexed(routine.exercises) { index, exercise ->
                ExerciseSequenceRow(
                    exercise = exercise,
                    index = index,
                    isActive = index == uiState.currentExerciseIndex,
                    isDone = uiState.completedIndices.contains(index),
                    onSelectExercise = { viewModel.setCurrentExercise(index) },
                    onStartExercise = {
                        onStartExercise(exercise.exerciseId)
                    },
                    onMarkDone = { viewModel.markExerciseDone(index) }
                )
            }
        }

        if (uiState.completedIndices.size == routine.exercises.size && routine.exercises.isNotEmpty()) {
            Button(
                onClick = onRoutineComplete,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FormFitTeal),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Finish Routine", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ExerciseSequenceRow(
    exercise: RoutineExercise,
    index: Int,
    isActive: Boolean,
    isDone: Boolean,
    onSelectExercise: () -> Unit,
    onStartExercise: () -> Unit,
    onMarkDone: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            isDone -> SurfaceVariant.copy(alpha = 0.5f)
            isActive -> SurfaceVariant
            else -> SurfaceVariant.copy(alpha = 0.7f)
        },
        animationSpec = tween(300),
        label = "rowBgColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isActive) FormFitTeal else Color.Transparent,
        animationSpec = tween(300),
        label = "rowBorderColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(16.dp),
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.5.dp, borderColor) else null,
        onClick = onSelectExercise
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = if (isDone) FormFitTeal else if (isActive) FormFitTeal.copy(alpha = 0.3f) else SurfaceVariant,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = "Done",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text(
                            text = "${index + 1}",
                            color = if (isActive) FormFitTeal else TextMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.exerciseName,
                        color = if (isDone) TextMuted else Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${exercise.sets} sets × ${exercise.reps} reps  ·  ${exercise.restSeconds}s rest",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            if (isActive && !isDone) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onMarkDone,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TextMuted)
                    ) {
                        Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Mark Done", fontSize = 13.sp)
                    }
                    Button(
                        onClick = onStartExercise,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = FormFitTeal),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Start", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
