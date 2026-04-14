package com.formfit.ai.ui.screens.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Monitor
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.formfit.ai.core.model.WorkoutSession
import com.formfit.ai.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProgressScreen(
    onNavigateToPlans: () -> Unit = {},
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showWeightDialog) {
        WeightLogDialog(
            onDismiss = viewModel::dismissWeightDialog,
            onLog = viewModel::logWeight
        )
    }

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
                    .background(Brush.verticalGradient(listOf(GradientEnd, FormFitNavy)))
                    .padding(horizontal = 24.dp)
                    .padding(top = 48.dp, bottom = 20.dp)
            ) {
                Column {
                    Text(
                        "Progress",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text("Your fitness journey", fontSize = 14.sp, color = TextMuted)
                }
            }
        }

        item {
            StreakHeader(
                currentStreak = uiState.currentStreakDays,
                longestStreak = uiState.longestStreakDays
            )
            Spacer(Modifier.height(16.dp))
        }

        item {
            SectionLabel("Activity — Last 30 Days")
            Spacer(Modifier.height(8.dp))
            StreakCalendar(
                activity = uiState.last30DaysActivity,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(20.dp))
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatBigCard("Total Workouts", "${uiState.totalSessions}", "🏋️", Modifier.weight(1f))
                StatBigCard("Total Reps", "${uiState.totalReps}", "🔢", Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatBigCard(
                    "Avg Form Score",
                    "${(uiState.avgFormScore * 100).toInt()}%",
                    "⭐",
                    Modifier.weight(1f)
                )
                StatBigCard(
                    "Calories Burned",
                    "${uiState.totalCalories.toInt()} kcal",
                    "🔥",
                    Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(24.dp))
        }

        if (uiState.repsOverTime.isNotEmpty()) {
            item {
                SectionLabel("Reps Over Time")
                Spacer(Modifier.height(8.dp))
                LineChartCard(
                    points = uiState.repsOverTime,
                    label = "reps",
                    lineColor = FormFitGreen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(160.dp)
                )
                Spacer(Modifier.height(20.dp))
            }
        }

        if (uiState.isPro && uiState.formScoreByExercise.isNotEmpty()) {
            item {
                SectionLabel("Avg Form Score by Exercise")
                Spacer(Modifier.height(8.dp))
                BarChartCard(
                    points = uiState.formScoreByExercise,
                    maxValue = 1f,
                    barColor = FormFitTeal,
                    labelSuffix = "%",
                    labelScale = 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(180.dp)
                )
                Spacer(Modifier.height(20.dp))
            }
        } else if (!uiState.isPro) {
            item {
                ProFeatureBanner(
                    title = "Form Score Analysis",
                    description = "See AI-scored form breakdown by exercise. Upgrade to unlock.",
                    onUpgrade = onNavigateToPlans,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )
                Spacer(Modifier.height(20.dp))
            }
        }

        item {
            SectionLabel("Workout Frequency (Last 30 Days)")
            Spacer(Modifier.height(8.dp))
            BarChartCard(
                points = uiState.weeklyFrequency,
                maxValue = (uiState.weeklyFrequency.maxOfOrNull { it.value } ?: 1f).coerceAtLeast(1f),
                barColor = FormFitBlue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(160.dp)
            )
            Spacer(Modifier.height(24.dp))
        }

        if (uiState.personalBests.isNotEmpty()) {
            item {
                SectionLabel("Personal Bests")
                Spacer(Modifier.height(8.dp))
            }
            items(uiState.personalBests) { session ->
                PersonalBestRow(
                    session = session,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )
            }
            item { Spacer(Modifier.height(20.dp)) }
        }

        if (uiState.isPro) {
            item {
                SectionLabel("Body Stats")
                Spacer(Modifier.height(8.dp))
                BodyStatsCard(
                    currentWeightKg = uiState.currentWeightKg,
                    weightHistory = uiState.weightHistory,
                    onLogWeight = viewModel::showWeightDialog,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )
                Spacer(Modifier.height(24.dp))
            }
        } else {
            item {
                ProFeatureBanner(
                    title = "Body Stats Tracking",
                    description = "Log weight and track body composition over time. Pro only.",
                    onUpgrade = onNavigateToPlans,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )
                Spacer(Modifier.height(24.dp))
            }
        }

        if (uiState.recentSessions.isNotEmpty()) {
            item {
                SectionLabel("Recent Sessions")
                Spacer(Modifier.height(8.dp))
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
                Spacer(Modifier.height(40.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🏃", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No workouts yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "Complete your first workout to see progress!",
                        fontSize = 14.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun StreakHeader(currentStreak: Int, longestStreak: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            color = if (currentStreak > 0) FormFitGreen.copy(alpha = 0.15f) else FormFitSurface,
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "🔥",
                    fontSize = 28.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "$currentStreak",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (currentStreak > 0) FormFitGreen else TextMuted
                )
                Text("Day Streak", fontSize = 12.sp, color = TextMuted)
            }
        }
        Surface(
            modifier = Modifier.weight(1f),
            color = FormFitSurface,
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🏆", fontSize = 28.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    "$longestStreak",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarningAmber
                )
                Text("Best Streak", fontSize = 12.sp, color = TextMuted)
            }
        }
    }
}

@Composable
private fun StreakCalendar(
    activity: List<DailyActivity>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = FormFitSurface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val rows = 5
            val cols = 6
            val days = activity.takeLast(rows * cols)

            repeat(rows) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(cols) { col ->
                        val idx = row * cols + col
                        val day = days.getOrNull(idx)
                        val hasActivity = (day?.sessionCount ?: 0) > 0
                        val isToday = day?.dayOffsetFromToday == 0

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when {
                                        hasActivity && (day?.sessionCount ?: 0) >= 2 -> FormFitGreen
                                        hasActivity -> FormFitGreen.copy(alpha = 0.55f)
                                        else -> SurfaceVariant
                                    }
                                )
                                .then(
                                    if (isToday) Modifier.border(1.dp, FormFitTeal, RoundedCornerShape(4.dp))
                                    else Modifier
                                )
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(10.dp)
                        .background(SurfaceVariant, RoundedCornerShape(2.dp))
                )
                Text("None", fontSize = 10.sp, color = TextMuted)
                Spacer(Modifier.width(4.dp))
                Box(
                    Modifier
                        .size(10.dp)
                        .background(FormFitGreen.copy(alpha = 0.55f), RoundedCornerShape(2.dp))
                )
                Text("1 session", fontSize = 10.sp, color = TextMuted)
                Spacer(Modifier.width(4.dp))
                Box(
                    Modifier
                        .size(10.dp)
                        .background(FormFitGreen, RoundedCornerShape(2.dp))
                )
                Text("2+", fontSize = 10.sp, color = TextMuted)
            }
        }
    }
}

@Composable
private fun LineChartCard(
    points: List<ChartPoint>,
    label: String,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = FormFitSurface,
        shape = RoundedCornerShape(16.dp)
    ) {
        if (points.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No data yet", color = TextMuted, fontSize = 13.sp)
            }
            return@Surface
        }

        val maxVal = points.maxOf { it.value }.coerceAtLeast(1f)
        val fillColor = lineColor.copy(alpha = 0.18f)

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 28.dp)
        ) {
            val w = size.width
            val h = size.height
            val stepX = if (points.size > 1) w / (points.size - 1) else w
            val gridColor = Color.White.copy(alpha = 0.05f)

            repeat(4) { i ->
                val y = h * i / 3f
                drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1.dp.toPx())
            }

            val path = Path()
            val fillPath = Path()

            points.forEachIndexed { i, pt ->
                val x = i * stepX
                val y = h - (pt.value / maxVal * h)
                if (i == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, h)
                    fillPath.lineTo(x, y)
                } else {
                    val prevX = (i - 1) * stepX
                    val prevY = h - (points[i - 1].value / maxVal * h)
                    val cpX = (prevX + x) / 2f
                    path.cubicTo(cpX, prevY, cpX, y, x, y)
                    fillPath.cubicTo(cpX, prevY, cpX, y, x, y)
                }
            }
            val lastX = (points.size - 1) * stepX
            fillPath.lineTo(lastX, h)
            fillPath.close()

            drawPath(fillPath, Brush.verticalGradient(listOf(fillColor, Color.Transparent)))
            drawPath(path, lineColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))

            points.forEachIndexed { i, pt ->
                val x = i * stepX
                val y = h - (pt.value / maxVal * h)
                drawCircle(lineColor, radius = 4.dp.toPx(), center = Offset(x, y))
                drawCircle(FormFitNavy, radius = 2.dp.toPx(), center = Offset(x, y))
            }
        }
    }
}

@Composable
private fun BarChartCard(
    points: List<ChartPoint>,
    maxValue: Float,
    barColor: Color,
    modifier: Modifier = Modifier,
    labelSuffix: String = "",
    labelScale: Float = 1f
) {
    Surface(
        modifier = modifier,
        color = FormFitSurface,
        shape = RoundedCornerShape(16.dp)
    ) {
        if (points.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("No data yet", color = TextMuted, fontSize = 13.sp)
            }
            return@Surface
        }

        val safeMax = maxValue.coerceAtLeast(0.01f)

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp)
        ) {
            val w = size.width
            val h = size.height
            val barCount = points.size
            val barGap = 6.dp.toPx()
            val barWidth = (w - barGap * (barCount - 1)) / barCount
            val gridColor = Color.White.copy(alpha = 0.05f)

            repeat(4) { i ->
                val y = h * i / 3f
                drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1.dp.toPx())
            }

            points.forEachIndexed { i, pt ->
                val left = i * (barWidth + barGap)
                val barH = (pt.value / safeMax * h).coerceAtLeast(2.dp.toPx())
                val top = h - barH

                drawRoundRect(
                    color = barColor.copy(alpha = 0.25f),
                    topLeft = Offset(left, 0f),
                    size = Size(barWidth, h),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barH),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }
        }
    }
}

@Composable
private fun PersonalBestRow(session: WorkoutSession, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = FormFitSurface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.FitnessCenter,
                contentDescription = null,
                tint = WarningAmber,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = session.exerciseName,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${session.repCount} reps",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = FormFitGreen
                )
                Text(
                    "${(session.avgFormScore * 100).toInt()}% form",
                    fontSize = 11.sp,
                    color = if (session.avgFormScore >= 0.75f) FormFitGreen else WarningAmber
                )
            }
        }
    }
}

@Composable
private fun BodyStatsCard(
    currentWeightKg: Float?,
    weightHistory: List<com.formfit.ai.core.model.BodyWeightEntry>,
    onLogWeight: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier, color = FormFitSurface, shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Monitor,
                    contentDescription = null,
                    tint = FormFitBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Body Weight", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onLogWeight) {
                    Text("Log Weight", color = FormFitTeal, fontSize = 13.sp)
                }
            }

            if (currentWeightKg != null) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "%.1f".format(currentWeightKg),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("kg", fontSize = 16.sp, color = TextMuted, modifier = Modifier.padding(bottom = 4.dp))
                }

                if (weightHistory.size >= 2) {
                    val diff = weightHistory.last().weightKg - weightHistory[weightHistory.size - 2].weightKg
                    val sign = if (diff >= 0) "+" else ""
                    Text(
                        "$sign${"%.1f".format(diff)} kg from previous",
                        fontSize = 12.sp,
                        color = if (diff <= 0) FormFitGreen else WarningAmber
                    )
                }
            } else {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Tap 'Log Weight' to track your body stats",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun WeightLogDialog(
    onDismiss: () -> Unit,
    onLog: (Float) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val valid = text.toFloatOrNull()?.let { it > 0f && it < 500f } == true

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FormFitSurface,
        title = { Text("Log Body Weight", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FormFitTeal,
                    unfocusedBorderColor = SurfaceVariant,
                    focusedLabelColor = FormFitTeal,
                    unfocusedLabelColor = TextMuted,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = { text.toFloatOrNull()?.let { onLog(it) } },
                enabled = valid
            ) { Text("Save", color = FormFitTeal) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) }
        }
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(horizontal = 24.dp)
    )
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
private fun ProFeatureBanner(
    title: String,
    description: String,
    onUpgrade: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = SurfaceVariant,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FormFitTeal.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = FormFitTeal,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = description,
                fontSize = 13.sp,
                color = TextMuted
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onUpgrade,
                colors = ButtonDefaults.buttonColors(containerColor = FormFitTeal),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Upgrade to Pro", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
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
