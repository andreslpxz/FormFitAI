package com.formfit.ai.ui.screens.workout

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.formfit.ai.vision.model.PoseLandmarkResult

@Composable
fun PoseCameraScreen(
    exerciseId: String = "squats",
    onBack: () -> Unit = {},
    viewModel: PoseDetectionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
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

    AnimatedContent(
        targetState = hasCameraPermission,
        transitionSpec = { fadeIn() togetherWith fadeOut() }
    ) { isGranted ->
        if (isGranted) {
            CameraContent(viewModel = viewModel, uiState = uiState, poseResult = poseResult)
        } else {
            CameraPermissionScreen(
                onPermissionGranted = { hasCameraPermission = true },
                onPermissionDenied = onBack
            )
        }
    }
}

@Composable
private fun CameraContent(
    viewModel: PoseDetectionViewModel,
    uiState: PoseDetectionUiState,
    poseResult: PoseLandmarkResult?
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
        onDispose {
            viewModel.cameraManager.stopCamera()
        }
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

        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(12.dp)
                .align(Alignment.TopEnd)
        ) {
            FormScoreBadge(
                score = uiState.formQuality.score,
                visible = uiState.isCalibrated
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
