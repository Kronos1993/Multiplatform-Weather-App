package com.kronos.multiplatform.weatherapp.core.util

interface IHapticFeedback {
    fun vibrate(intensity: Float)
}

expect class HapticFeedback : IHapticFeedback