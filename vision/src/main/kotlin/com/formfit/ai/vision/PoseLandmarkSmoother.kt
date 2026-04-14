package com.formfit.ai.vision

import com.formfit.ai.vision.model.PoseLandmarkResult
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark

class PoseLandmarkSmoother {

    companion object {
        private const val ALPHA = 0.35f
        private const val TOTAL_LANDMARKS = 33
    }

    private val smoothX = FloatArray(TOTAL_LANDMARKS) { 0f }
    private val smoothY = FloatArray(TOTAL_LANDMARKS) { 0f }
    private val smoothZ = FloatArray(TOTAL_LANDMARKS) { 0f }
    private var initialized = false

    fun smooth(result: PoseLandmarkResult): PoseLandmarkResult {
        val landmarks = result.landmarks
        if (landmarks.isEmpty()) {
            initialized = false
            return result
        }

        if (!initialized || landmarks.size != TOTAL_LANDMARKS) {
            landmarks.forEachIndexed { i, lm ->
                if (i < TOTAL_LANDMARKS) {
                    smoothX[i] = lm.x()
                    smoothY[i] = lm.y()
                    smoothZ[i] = lm.z()
                }
            }
            initialized = true
            return result
        }

        val smoothedLandmarks = landmarks.mapIndexed { i, lm ->
            if (i >= TOTAL_LANDMARKS) return@mapIndexed lm

            smoothX[i] = lerp(smoothX[i], lm.x(), ALPHA)
            smoothY[i] = lerp(smoothY[i], lm.y(), ALPHA)
            smoothZ[i] = lerp(smoothZ[i], lm.z(), ALPHA)

            NormalizedLandmark.create(
                smoothX[i],
                smoothY[i],
                smoothZ[i],
                lm.visibility(),
                lm.presence()
            )
        }

        return result.copy(landmarks = smoothedLandmarks)
    }

    fun reset() {
        initialized = false
        smoothX.fill(0f)
        smoothY.fill(0f)
        smoothZ.fill(0f)
    }

    private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t
}
