package com.kronos.multiplatform.weatherapp.core.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

actual class HapticFeedback(private val context: Context) : IHapticFeedback {
    override fun vibrate(intensity: Float) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (vibrator?.hasVibrator() == true) {
            val amplitude = (intensity * 255).toInt().coerceIn(1, 255) // Map intensity [0.0, 1.0] to amplitude
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(50, amplitude)
                )
            } else {
                vibrator.vibrate(50) // Legacy vibration
            }
        }
    }
}