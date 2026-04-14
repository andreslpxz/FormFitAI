package com.formfit.ai.ui.screens.routines

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.formfit.ai.core.data.RoutineDao
import com.formfit.ai.core.model.Difficulty
import com.formfit.ai.core.model.ExerciseLibrary
import com.formfit.ai.core.model.Routine
import com.formfit.ai.core.model.RoutineExercise
import com.formfit.ai.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.math.roundToInt

private const val TAG = "RoutineBuilderScreen"

data class RoutineBuilderUiState(
    val routineName: String = "",
    val routineDescription: String = "",
    val exercises: List<RoutineExercise> = emptyList(),
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false
)

@HiltViewModel
class RoutineBuilderViewModel @Inject constructor(
    private val routineDao: RoutineDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutineBuilderUiState())
    val uiState: StateFlow<RoutineBuilderUiState> = _uiState.asStateFlow()

    fun loadRoutine(routineId: String) {
        if (routineId == "new") return
        viewModelScope.launch {
            val routine = routineDao.getRoutineById(routineId) ?: return@launch
            _uiState.update {
                it.copy(
                    routineName = routine.name,
                    routineDescription = routine.description,
                    exercises = routine.exercises
                )
            }
        }
    }

    fun setName(name: String) = _uiState.update { it.copy(routineName = name) }
    fun setDescription(desc: String) = _uiState.update { it.copy(routineDescription = desc) }

    fun addExercise(exerciseId: String) {
        val exercise = ExerciseLibrary.find { it.id == exerciseId } ?: return
        val order = _uiState.value.exercises.size
        val routineExercise = RoutineExercise(
            exerciseId = exerciseId,
            exerciseName = exercise.name,
            sets = exercise.defaultSets,
            reps = exercise.defaultReps,
            restSeconds = 60,
            order = order
        )
        _uiState.update { it.copy(exercises = it.exercises + routineExercise) }
    }

    fun removeExercise(index: Int) {
        _uiState.update { state ->
            val updated = state.exercises.toMutableList().also { it.removeAt(index) }
                .mapIndexed { i, ex -> ex.copy(order = i) }
            state.copy(exercises = updated)
        }
    }

    fun reorderExercise(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        _uiState.update { state ->
            val list = state.exercises.toMutableList()
            val item = list.removeAt(fromIndex)
            val clampedTo = toIndex.coerceIn(0, list.size)
            list.add(clampedTo, item)
            state.copy(exercises = list.mapIndexed { i, ex -> ex.copy(order = i) })
        }
    }

    fun updateExerciseSets(index: Int, sets: Int) {
        _uiState.update { state ->
            val updated = state.exercises.toMutableList().apply {
                this[index] = this[index].copy(sets = sets.coerceIn(1, 10))
            }
            state.copy(exercises = updated)
        }
    }

    fun updateExerciseReps(index: Int, reps: Int) {
        _uiState.update { state ->
            val updated = state.exercises.toMutableList().apply {
                this[index] = this[index].copy(reps = reps.coerceIn(1, 100))
            }
            state.copy(exercises = updated)
        }
    }

    fun updateExerciseRest(index: Int, restSeconds: Int) {
        _uiState.update { state ->
            val updated = state.exercises.toMutableList().apply {
                this[index] = this[index].copy(restSeconds = restSeconds.coerceIn(0, 300))
            }
            state.copy(exercises = updated)
        }
    }

    fun saveRoutine(routineId: String) {
        val state = _uiState.value
        if (state.routineName.isBlank() || state.exercises.isEmpty()) return

        val estimatedMinutes = state.exercises.sumOf { it.sets * 2 + it.restSeconds / 60 }

        val routine = Routine(
            id = if (routineId == "new") UUID.randomUUID().toString() else routineId,
            name = state.routineName,
            description = state.routineDescription,
            isPreset = false,
            exercises = state.exercises,
            estimatedMinutes = estimatedMinutes.coerceAtLeast(5),
            difficulty = Difficulty.INTERMEDIATE
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            routineDao.insertRoutine(routine)
            _uiState.update { it.copy(isSaving = false, savedSuccessfully = true) }
        }
    }
}

@Composable
fun RoutineBuilderScreen(
    routineId: String,
    onBack: () -> Unit,
    viewModel: RoutineBuilderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showExercisePicker by remember { mutableStateOf(false) }

    LaunchedEffect(routineId) {
        viewModel.loadRoutine(routineId)
    }

    LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) onBack()
    }

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
                    Text(
                        text = if (routineId == "new") "Create Routine" else "Edit Routine",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.routineName,
                        onValueChange = viewModel::setName,
                        label = { Text("Routine Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FormFitTeal,
                            unfocusedBorderColor = SurfaceVariant,
                            focusedLabelColor = FormFitTeal,
                            unfocusedLabelColor = TextMuted,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = uiState.routineDescription,
                        onValueChange = viewModel::setDescription,
                        label = { Text("Description (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FormFitTeal,
                            unfocusedBorderColor = SurfaceVariant,
                            focusedLabelColor = FormFitTeal,
                            unfocusedLabelColor = TextMuted,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(Modifier.height(20.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Exercises", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        if (uiState.exercises.isNotEmpty()) {
                            Text(
                                "(long-press to drag · reorder)",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            itemsIndexed(
                items = uiState.exercises,
                key = { _, exercise -> exercise.order }
            ) { index, exercise ->
                DraggableExerciseRow(
                    exercise = exercise,
                    index = index,
                    totalCount = uiState.exercises.size,
                    onReorder = { from, to -> viewModel.reorderExercise(from, to) },
                    onRemove = { viewModel.removeExercise(index) },
                    onSetsChange = { viewModel.updateExerciseSets(index, it) },
                    onRepsChange = { viewModel.updateExerciseReps(index, it) },
                    onRestChange = { viewModel.updateExerciseRest(index, it) }
                )
            }

            item {
                TextButton(
                    onClick = { showExercisePicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, tint = FormFitTeal)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Exercise", color = FormFitTeal, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Button(
            onClick = { viewModel.saveRoutine(routineId) },
            enabled = uiState.routineName.isNotBlank() && uiState.exercises.isNotEmpty() && !uiState.isSaving,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FormFitTeal),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Text("Save Routine", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showExercisePicker) {
        ExercisePickerDialog(
            onDismiss = { showExercisePicker = false },
            onExerciseSelected = { exerciseId ->
                viewModel.addExercise(exerciseId)
                showExercisePicker = false
            }
        )
    }
}

@Composable
private fun DraggableExerciseRow(
    exercise: RoutineExercise,
    index: Int,
    totalCount: Int,
    onReorder: (from: Int, to: Int) -> Unit,
    onRemove: () -> Unit,
    onSetsChange: (Int) -> Unit,
    onRepsChange: (Int) -> Unit,
    onRestChange: (Int) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    var itemHeightPx by remember { mutableIntStateOf(1) }

    val elevation = if (isDragging) 8.dp else 0.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .offset { IntOffset(0, if (isDragging) dragOffsetY.roundToInt() else 0) }
            .zIndex(if (isDragging) 1f else 0f)
            .shadow(elevation, RoundedCornerShape(16.dp))
            .background(
                if (isDragging) SurfaceVariant.copy(alpha = 0.95f) else SurfaceVariant,
                RoundedCornerShape(16.dp)
            )
            .onGloballyPositioned { coords -> itemHeightPx = coords.size.height.coerceAtLeast(1) }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.DragHandle,
                contentDescription = "Drag to reorder",
                tint = TextMuted,
                modifier = Modifier
                    .size(22.dp)
                    .pointerInput(index) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                isDragging = true
                                dragOffsetY = 0f
                                Log.d(TAG, "Started dragging exercise at index $index")
                            },
                            onDrag = { _, dragAmount ->
                                dragOffsetY += dragAmount.y
                            },
                            onDragEnd = {
                                val delta = (dragOffsetY / itemHeightPx).roundToInt()
                                val targetIndex = (index + delta).coerceIn(0, totalCount - 1)
                                if (targetIndex != index) {
                                    Log.d(TAG, "Reordering exercise from $index to $targetIndex")
                                    onReorder(index, targetIndex)
                                }
                                isDragging = false
                                dragOffsetY = 0f
                            },
                            onDragCancel = {
                                isDragging = false
                                dragOffsetY = 0f
                            }
                        )
                    }
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = exercise.exerciseName,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${index + 1}",
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(end = 4.dp)
            )
            IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "Remove",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NumberStepper(
                label = "Sets",
                value = exercise.sets,
                onDecrement = { onSetsChange(exercise.sets - 1) },
                onIncrement = { onSetsChange(exercise.sets + 1) },
                modifier = Modifier.weight(1f)
            )
            NumberStepper(
                label = "Reps",
                value = exercise.reps,
                onDecrement = { onRepsChange(exercise.reps - 1) },
                onIncrement = { onRepsChange(exercise.reps + 1) },
                modifier = Modifier.weight(1f)
            )
            NumberStepper(
                label = "Rest (s)",
                value = exercise.restSeconds,
                onDecrement = { onRestChange(exercise.restSeconds - 15) },
                onIncrement = { onRestChange(exercise.restSeconds + 15) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun NumberStepper(
    label: String,
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, color = TextMuted, fontSize = 10.sp)
        Spacer(Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(FormFitNavy, RoundedCornerShape(6.dp))
                    .clickable(onClick = onDecrement),
                contentAlignment = Alignment.Center
            ) {
                Text("−", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Text(text = "$value", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(FormFitNavy, RoundedCornerShape(6.dp))
                    .clickable(onClick = onIncrement),
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ExercisePickerDialog(
    onDismiss: () -> Unit,
    onExerciseSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceVariant,
        title = {
            Text("Add Exercise", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                ExerciseLibrary.forEach { exercise ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onExerciseSelected(exercise.id) }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = exercise.category.emoji(), fontSize = 20.sp)
                        Column {
                            Text(
                                text = exercise.name,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = exercise.difficulty.label(),
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                    HorizontalDivider(color = FormFitNavy.copy(alpha = 0.5f), thickness = 0.5.dp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}
