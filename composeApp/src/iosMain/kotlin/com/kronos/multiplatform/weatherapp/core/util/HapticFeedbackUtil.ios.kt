package com.kronos.multiplatform.weatherapp.core.util

import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle

actual class HapticFeedback : IHapticFeedback {
    override fun vibrate(intensity: Float) {
        val style = when {
            intensity < 0.3 -> UIImpactFeedbackStyle.UIImpactFeedbackStyleLight
            intensity < 0.7 -> UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium
            else -> UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy
        }
        val generator = UIImpactFeedbackGenerator(style)
        generator.prepare()
        generator.impactOccurred()
    }
}