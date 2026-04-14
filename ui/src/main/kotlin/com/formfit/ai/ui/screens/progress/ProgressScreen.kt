package com.formfit.ai.ui.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.formfit.ai.core.model.WorkoutSession
import com.formfit.ai.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = hiltViewModel()
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
                    .background(GradientEnd)
                    .padding(horizontal = 24.dp)
                    .padding(top = 48.dp, bottom = 20.dp)
            ) {
                Column {
                    Text("📊 Progress", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text("Your fitness journey", fontSize = 14.sp, color = TextMuted)
                }
            }
        }

        item {
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatBigCard(
                    label = "Total Workouts",
                    value = "${uiState.totalSessions}",
                    emoji = "🏋️",
                    modifier = Modifier.weight(1f)
                )
                StatBigCard(
                    label = "Total Reps",
                    value = "${uiState.totalReps}",
                    emoji = "🔢",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatBigCard(
                    label = "Avg Form Score",
                    value = "${(uiState.avgFormScore * 100).toInt()}%",
                    emoji = "⭐",
                    modifier = Modifier.weight(1f)
                )
                StatBigCard(
                    label = "Calories Burned",
                    value = "${uiState.totalCalories.toInt()} kcal",
                    emoji = "🔥",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (uiState.recentSessions.isNotEmpty()) {
            item {
                Spacer(Modifier.height(24.dp))
                Text(
                    "Recent Sessions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(Modifier.height(12.dp))
            }

            items(uiState.recentSessions) { session ->
                SessionHistoryCard(
                    session = session,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 8.dp)
                )
            }
        } else {
            item {
                Spacer(Modifier.height(60.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🏃", fontSize = 56.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("No workouts yet", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    Text("Complete your first workout to see progress!", fontSize = 14.sp, color = TextMuted)
                }
            }
        }
    }
}

@Composable
fun StatBigCard(label: String, value: String, emoji: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = FormFitSurface, shape = RoundedCornerShape(14.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(emoji, fontSize = 24.sp)
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = FormFitGreen)
            Text(label, fontSize = 12.sp, color = TextMuted)
        }
    }
}

@Composable
fun SessionHistoryCard(session: WorkoutSession, modifier: Modifier = Modifier) {
    val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    Surface(modifier = modifier.fillMaxWidth(), color = FormFitSurface, shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(session.exerciseName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Text(dateFormat.format(Date(session.createdAt)), fontSize = 12.sp, color = TextMuted)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${session.repCount} reps", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FormFitGreen)
                Text(
                    "${(session.avgFormScore * 100).toInt()}% form",
                    fontSize = 11.sp,
                    color = if (session.avgFormScore >= 0.75f) FormFitGreen else WarningAmber
                )
            }
        }
    }
}
