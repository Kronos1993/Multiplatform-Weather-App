package com.kronos.multiplatform.weatherapp.core.util

import androidx.compose.runtime.Composable

interface ICloseApp {
    fun closeApp()
}

expect class CloseAppImpl : ICloseApp

@Composable
expect fun BackPressHandlerEffect(enabled: Boolean, onBack: () -> Unit)