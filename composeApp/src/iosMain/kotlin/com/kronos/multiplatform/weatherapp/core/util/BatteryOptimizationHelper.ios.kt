package com.kronos.multiplatform.weatherapp.core.util

// iOS has no battery-optimization whitelist; background refresh is governed by BGTaskScheduler.
actual class BatteryOptimizationHelper : IBatteryOptimizationHelper {
    override fun isSupported(): Boolean = false

    override fun isIgnoringBatteryOptimizations(): Boolean = true

    override fun requestIgnoreBatteryOptimizations() = Unit

    override fun hasOemAutoStartSettings(): Boolean = false

    override fun openOemAutoStartSettings() = Unit
}
