package com.kronos.multiplatform.weatherapp.core.util

import androidx.activity.compose.BackHandler
import kotlin.system.exitProcess
import androidx.compose.runtime.Composable

actual class CloseAppImpl() : ICloseApp {

    override fun closeApp() {
        exitProcess(0)
    }
}

@Composable
actual fun BackPressHandlerEffect(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled, onBack)
}