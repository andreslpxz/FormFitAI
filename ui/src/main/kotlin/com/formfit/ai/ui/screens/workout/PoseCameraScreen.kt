package com.formfit.ai.ui.screens.workout

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.formfit.ai.ui.audio.AudioFeedbackManager
import com.formfit.ai.ui.theme.*
import com.formfit.ai.vision.model.OverallFormQuality
import com.formfit.ai.vision.model.PoseLandmarkResult

@Composable
fun PoseCameraScreen(
    exerciseId: String = "squats",
    onBack: () -> Unit = {},
    onWorkoutFinished: (Long) -> Unit = {},
    viewModel: PoseDetectionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val audioManager = remember { AudioFeedbackManager(context) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        viewModel.setCameraPermissionGranted(granted)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val poseResult by viewModel.poseResult.collectAsStateWithLifecycle()

    LaunchedEffect(exerciseId) {
        viewModel.setExercise(exerciseId)
    }

    LaunchedEffect(hasCameraPermission) {
        viewModel.setCameraPermissionGranted(hasCameraPermission)
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(uiState.repCounterState.isRepComplete) {
        if (uiState.repCounterState.isRepComplete && uiState.isWorkoutActive) {
            audioManager.playRepComplete()
        }
    }

    var lastFormWarnMs by remember { mutableStateOf(0L) }
    LaunchedEffect(uiState.formQuality.issues) {
        if (uiState.isWorkoutActive && uiState.formQuality.issues.isNotEmpty()) {
            val now = System.currentTimeMillis()
            if (now - lastFormWarnMs > 5_000L) {
                lastFormWarnMs = now
                audioManager.playFormWarning()
            }
        }
    }

    LaunchedEffect(uiState.savedSessionId) {
        uiState.savedSessionId?.let { id ->
            audioManager.playWorkoutComplete()
            onWorkoutFinished(id)
        }
    }

    DisposableEffect(Unit) {
        onDispose { audioManager.release() }
    }

    AnimatedContent(
        targetState = hasCameraPermission,
        transitionSpec = { fadeIn() togetherWith fadeOut() }
    ) { isGranted ->
        if (isGranted) {
            ActiveWorkoutContent(
                viewModel = viewModel,
                uiState = uiState,
                poseResult = poseResult,
                onBack = onBack
            )
        } else {
            CameraPermissionScreen(
                onPermissionGranted = { hasCameraPermission = true },
                onPermissionDenied = onBack
            )
        }
    }
}

@Composable
private fun ActiveWorkoutContent(
    viewModel: PoseDetectionViewModel,
    uiState: PoseDetectionUiState,
    poseResult: PoseLandmarkResult?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    LaunchedEffect(previewView) {
        viewModel.cameraManager.startCamera(lifecycleOwner, previewView)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.cameraManager.stopCamera() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        SkeletonOverlay(
            poseResult = poseResult,
            formQuality = if (uiState.isCalibrated) uiState.formQuality else null,
            modifier = Modifier.fillMaxSize()
        )

        CalibrationOverlay(
            calibrationState = uiState.calibrationState,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.weight(1f))

                if (uiState.isWorkoutActive) {
                    ChronoBadge(seconds = uiState.elapsedSeconds)
                }
            }
        }

        if (uiState.isCalibrated && uiState.isWorkoutActive) {
            AnimatedRepCounter(
                repCount = uiState.repCounterState.count,
                isRepComplete = uiState.repCounterState.isRepComplete,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            AnimatedVisibility(
                visible = uiState.isCalibrated && uiState.isWorkoutActive &&
                    uiState.formQuality.issues.isNotEmpty(),
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                FormFeedbackPanel(formQuality = uiState.formQuality)
            }

            if (!uiState.isWorkoutActive && uiState.isCalibrated) {
                FormScoreBadge(score = uiState.formQuality.score, visible = true)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                if (!uiState.isWorkoutActive) {
                    Button(
                        onClick = { viewModel.startWorkout() },
                        enabled = uiState.isCalibrated,
                        modifier = Modifier.height(52.dp).defaultMinSize(minWidth = 200.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FormFitTeal),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Text(
                            text = if (uiState.isCalibrated) "Start Workout" else "Calibrating…",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = { viewModel.finishWorkout() },
                        modifier = Modifier.height(52.dp).defaultMinSize(minWidth = 200.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Icon(Icons.Rounded.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Finish  ·  ${uiState.repCounterState.count} reps",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedRepCounter(
    repCount: Int,
    isRepComplete: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isRepComplete) 1.4f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
        label = "repScale"
    )
    val color by animateColorAsState(
        targetValue = if (isRepComplete) FormFitTeal else Color.White,
        animationSpec = tween(200),
        label = "repColor"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$repCount",
            color = color.copy(alpha = 0.9f),
            fontSize = 96.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .scale(scale)
                .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
                .padding(horizontal = 24.dp, vertical = 8.dp)
        )
        Text(
            text = "REPS",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp
        )
    }
}

@Composable
private fun ChronoBadge(seconds: Int) {
    val m = seconds / 60
    val s = seconds % 60
    Box(
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = "%d:%02d".format(m, s),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FormFeedbackPanel(formQuality: OverallFormQuality) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.Black.copy(alpha = 0.75f),
                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(16.dp)
    ) {
        formQuality.issues.take(2).forEach { issue ->
            Text(
                text = "⚠ $issue",
                color = Color(0xFFFF9800),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        formQuality.suggestions.take(1).forEach { suggestion ->
            Text(
                text = "→ $suggestion",
                color = TextMuted,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun FormScoreBadge(
    score: Float,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    val color = when {
        score >= 0.85f -> Color(0xFF4CAF50)
        score >= 0.6f -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
    val label = when {
        score >= 0.85f -> "GOOD FORM"
        score >= 0.6f -> "CHECK FORM"
        else -> "FIX FORM"
    }

    Box(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .background(color.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )
    }
}
