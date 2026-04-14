package com.formfit.ai.ui.audio

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "AudioFeedbackManager"

class AudioFeedbackManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 70)
    } catch (e: RuntimeException) {
        Log.e(TAG, "Failed to create ToneGenerator: ${e.message}", e)
        null
    }

    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                manager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire Vibrator service: ${e.message}", e)
            null
        }
    }

    fun playRepComplete() {
        scope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
                    ?: Log.w(TAG, "ToneGenerator unavailable; skipping rep-complete beep")
            } catch (e: IllegalStateException) {
                Log.w(TAG, "ToneGenerator in invalid state during rep-complete beep: ${e.message}")
            }
        }
        vibrateShort()
    }

    fun playFormWarning() {
        scope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 150)
                    ?: Log.w(TAG, "ToneGenerator unavailable; skipping form-warning tone")
            } catch (e: IllegalStateException) {
                Log.w(TAG, "ToneGenerator in invalid state during form-warning tone: ${e.message}")
            }
        }
    }

    fun playWorkoutComplete() {
        scope.launch {
            try {
                toneGenerator?.let { gen ->
                    gen.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
                    delay(220)
                    gen.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
                    delay(220)
                    gen.startTone(ToneGenerator.TONE_PROP_BEEP, 350)
                } ?: Log.w(TAG, "ToneGenerator unavailable; skipping completion fanfare")
            } catch (e: IllegalStateException) {
                Log.w(TAG, "ToneGenerator in invalid state during completion fanfare: ${e.message}")
            }
        }
        vibrateLong()
    }

    private fun vibrateShort() {
        try {
            val vib = vibrator ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(50)
            }
        } catch (e: UnsupportedOperationException) {
            Log.w(TAG, "Short vibration not supported on this device: ${e.message}")
        }
    }

    private fun vibrateLong() {
        try {
            val vib = vibrator ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 100, 100, 200), -1))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(longArrayOf(0, 100, 100, 100, 100, 200), -1)
            }
        } catch (e: UnsupportedOperationException) {
            Log.w(TAG, "Wave vibration not supported on this device: ${e.message}")
        }
    }

    fun release() {
        try {
            toneGenerator?.release()
        } catch (e: RuntimeException) {
            Log.w(TAG, "Error releasing ToneGenerator: ${e.message}")
        }
    }
}
