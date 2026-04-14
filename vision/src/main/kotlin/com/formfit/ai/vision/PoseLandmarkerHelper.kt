package com.formfit.ai.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import com.formfit.ai.vision.model.PoseLandmarkResult
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PoseLandmarkerHelper(
    private val context: Context
) {
    companion object {
        const val MODEL_POSE_LANDMARKER_FULL = "pose_landmarker_full.task"
        const val DEFAULT_POSE_DETECTION_CONFIDENCE = 0.5f
        const val DEFAULT_POSE_TRACKING_CONFIDENCE = 0.5f
        const val DEFAULT_POSE_PRESENCE_CONFIDENCE = 0.5f
        const val DEFAULT_NUM_POSES = 1
    }

    private var poseLandmarker: PoseLandmarker? = null

    private val _poseResults = MutableStateFlow<PoseLandmarkResult?>(null)
    val poseResults: StateFlow<PoseLandmarkResult?> = _poseResults.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var imageWidth = 0
    private var imageHeight = 0

    fun setup(
        minPoseDetectionConfidence: Float = DEFAULT_POSE_DETECTION_CONFIDENCE,
        minPoseTrackingConfidence: Float = DEFAULT_POSE_TRACKING_CONFIDENCE,
        minPosePresenceConfidence: Float = DEFAULT_POSE_PRESENCE_CONFIDENCE
    ) {
        poseLandmarker = buildDelegateChain(
            minPoseDetectionConfidence,
            minPoseTrackingConfidence,
            minPosePresenceConfidence
        )
        _isRunning.value = poseLandmarker != null
    }

    private fun buildDelegateChain(
        detectionConf: Float,
        trackingConf: Float,
        presenceConf: Float
    ): PoseLandmarker? {
        val gpuResult = tryCreateWithDelegate(
            Delegate.GPU, detectionConf, trackingConf, presenceConf
        )
        if (gpuResult != null) return gpuResult

        return tryCreateWithDelegate(
            Delegate.CPU, detectionConf, trackingConf, presenceConf
        )
    }

    private fun tryCreateWithDelegate(
        delegate: Delegate,
        minPoseDetectionConfidence: Float,
        minPoseTrackingConfidence: Float,
        minPosePresenceConfidence: Float
    ): PoseLandmarker? {
        return try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(MODEL_POSE_LANDMARKER_FULL)
                .setDelegate(delegate)
                .build()

            val options = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setMinPoseDetectionConfidence(minPoseDetectionConfidence)
                .setMinTrackingConfidence(minPoseTrackingConfidence)
                .setMinPosePresenceConfidence(minPosePresenceConfidence)
                .setNumPoses(DEFAULT_NUM_POSES)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener { result, image -> onResult(result, image) }
                .setErrorListener { error -> error.printStackTrace() }
                .build()

            PoseLandmarker.createFromOptions(context, options)
        } catch (e: Exception) {
            null
        }
    }

    fun detectAsync(imageProxy: ImageProxy) {
        val bitmap = imageProxyToBitmap(imageProxy)
        imageWidth = imageProxy.width
        imageHeight = imageProxy.height

        val mpImage = BitmapImageBuilder(bitmap).build()
        poseLandmarker?.detectAsync(mpImage, System.currentTimeMillis())
        imageProxy.close()
    }

    private fun onResult(result: PoseLandmarkerResult, image: MPImage) {
        if (result.landmarks().isEmpty()) {
            _poseResults.value = null
            return
        }

        val landmarks = result.landmarks()[0]
        val worldLandmarks = if (result.worldLandmarks().isNotEmpty()) {
            result.worldLandmarks()[0]
        } else {
            emptyList()
        }

        _poseResults.value = PoseLandmarkResult(
            landmarks = landmarks,
            worldLandmarks = worldLandmarks,
            imageWidth = image.width,
            imageHeight = image.height
        )
    }

    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val plane = imageProxy.planes[0]
        val buffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * imageProxy.width

        val bitmapBuffer = Bitmap.createBitmap(
            imageProxy.width + rowPadding / pixelStride,
            imageProxy.height,
            Bitmap.Config.ARGB_8888
        )
        bitmapBuffer.copyPixelsFromBuffer(buffer)

        val matrix = Matrix().apply {
            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            if (imageProxy.imageInfo.rotationDegrees == 0 ||
                imageProxy.imageInfo.rotationDegrees == 180) {
                postScale(-1f, 1f, imageProxy.width / 2f, imageProxy.height / 2f)
            } else {
                postScale(-1f, 1f, imageProxy.height / 2f, imageProxy.width / 2f)
            }
        }

        val rotatedBitmap = Bitmap.createBitmap(
            bitmapBuffer, 0, 0,
            imageProxy.width, imageProxy.height,
            matrix, true
        )
        bitmapBuffer.recycle()
        return rotatedBitmap
    }

    fun close() {
        poseLandmarker?.close()
        poseLandmarker = null
        _isRunning.value = false
        _poseResults.value = null
    }
}
