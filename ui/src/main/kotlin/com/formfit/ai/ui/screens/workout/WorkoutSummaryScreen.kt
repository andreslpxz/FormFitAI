package com.formfit.ai.ui.screens.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Home
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
import com.formfit.ai.core.data.WorkoutRepository
import com.formfit.ai.core.model.WorkoutSession
import com.formfit.ai.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutSummaryViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
    private val _session = MutableStateFlow<WorkoutSession?>(null)
    val session: StateFlow<WorkoutSession?> = _session.asStateFlow()

    fun loadSession(id: Long) {
        viewModelScope.launch {
            _session.value = workoutRepository.getSessionById(id)
        }
    }
}

@Composable
fun WorkoutSummaryScreen(
    sessionId: Long,
    onGoHome: () -> Unit,
    viewModel: WorkoutSummaryViewModel = hiltViewModel()
) {
    val session by viewModel.session.collectAsState()

    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FormFitNavy),
        contentAlignment = Alignment.Center
    ) {
        if (session == null) {
            CircularProgressIndicator(color = FormFitTeal)
        } else {
            WorkoutSummaryContent(session = session!!, onGoHome = onGoHome)
        }
    }
}

@Composable
private fun WorkoutSummaryContent(
    session: WorkoutSession,
    onGoHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .background(FormFitTeal.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.EmojiEvents,
                contentDescription = null,
                tint = FormFitTeal,
                modifier = Modifier.size(52.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Workout Complete!",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = session.exerciseName,
            color = TextMuted,
            fontSize = 15.sp
        )

        Spacer(Modifier.height(36.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryStatCard(
                label = "Reps",
                value = "${session.repCount}",
                modifier = Modifier.weight(1f)
            )
            SummaryStatCard(
                label = "Duration",
                value = formatDuration(session.durationSeconds),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryStatCard(
                label = "Calories",
                value = "${session.caloriesBurned.toInt()} kcal",
                modifier = Modifier.weight(1f)
            )
            SummaryStatCard(
                label = "Form Score",
                value = "${(session.avgFormScore * 100).toInt()}%",
                modifier = Modifier.weight(1f),
                valueColor = formScoreColor(session.avgFormScore)
            )
        }

        if (session.avgFormScore > 0f) {
            Spacer(Modifier.height(24.dp))
            LinearProgressIndicator(
                progress = session.avgFormScore,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = formScoreColor(session.avgFormScore),
                trackColor = SurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = formScoreLabel(session.avgFormScore),
                color = formScoreColor(session.avgFormScore),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onGoHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FormFitTeal),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Rounded.Home, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Back to Home", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SummaryStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.White
) {
    Column(
        modifier = modifier
            .background(SurfaceVariant, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, color = valueColor, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(4.dp))
        Text(text = label, color = TextMuted, fontSize = 12.sp)
    }
}

private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}

private fun formScoreColor(score: Float) = when {
    score >= 0.85f -> Color(0xFF4CAF50)
    score >= 0.6f -> Color(0xFFFF9800)
    else -> Color(0xFFF44336)
}

private fun formScoreLabel(score: Float) = when {
    score >= 0.85f -> "EXCELLENT FORM"
    score >= 0.6f -> "GOOD FORM"
    else -> "NEEDS IMPROVEMENT"
}
