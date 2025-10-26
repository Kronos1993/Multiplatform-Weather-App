package com.kronos.multiplatform.weatherapp.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.provideContent
import com.kronos.multiplatform.weatherapp.widget.components.SmallWeatherWidgetContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class WeatherGlanceWidget : BaseWeatherGlanceWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val weatherData = runBlocking(Dispatchers.IO) {
                loadWeatherData(context)
            }
            SmallWeatherWidgetContent(weatherData)
        }
    }
}