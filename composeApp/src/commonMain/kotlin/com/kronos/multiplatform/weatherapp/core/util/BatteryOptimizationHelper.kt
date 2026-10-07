package com.kronos.multiplatform.weatherapp.core.util

interface IBatteryOptimizationHelper {
    fun isSupported(): Boolean

    fun isIgnoringBatteryOptimizations(): Boolean

    fun requestIgnoreBatteryOptimizations()

    fun hasOemAutoStartSettings(): Boolean

    fun openOemAutoStartSettings()
}

expect class BatteryOptimizationHelper : IBatteryOptimizationHelper
