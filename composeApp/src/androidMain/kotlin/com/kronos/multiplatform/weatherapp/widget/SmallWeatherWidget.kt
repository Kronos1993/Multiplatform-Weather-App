package com.kronos.multiplatform.weatherapp.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.provideContent
import com.kronos.multiplatform.weatherapp.widget.components.SmallWeatherWidgetContent

class SmallWeatherGlanceWidget : BaseWeatherGlanceWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val weatherData = loadWeatherData(context)
        provideContent {
            SmallWeatherWidgetContent(weatherData)
        }
    }
}