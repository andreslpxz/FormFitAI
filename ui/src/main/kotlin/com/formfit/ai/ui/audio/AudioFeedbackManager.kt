package com.formfit.ai.ui.audio

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AudioFeedbackManager(private val context: Context) {

    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    fun playRepComplete() {
        scope.launch {
            try {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
            } catch (_: Exception) {}
        }
        vibrateShort()
    }

    fun playFormWarning() {
        scope.launch {
            try {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 150)
            } catch (_: Exception) {}
        }
    }

    fun playWorkoutComplete() {
        scope.launch {
            try {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
                delay(200)
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
                delay(200)
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 300)
            } catch (_: Exception) {}
        }
        vibrateLong()
    }

    private fun vibrateShort() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateLong() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 100, 100, 200), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 100, 100, 100, 100, 200), -1)
            }
        } catch (_: Exception) {}
    }

    fun release() {
        try {
            toneGenerator.release()
        } catch (_: Exception) {}
    }
}
