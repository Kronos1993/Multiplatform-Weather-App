package com.kronos.multiplatform.weatherapp.core.widget

interface IWidgetUpdater {
    suspend fun updateAllWeatherWidgets()
}

expect class WidgetUpdater : IWidgetUpdater