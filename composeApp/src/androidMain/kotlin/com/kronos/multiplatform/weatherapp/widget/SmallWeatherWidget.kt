package com.kronos.multiplatform.weatherapp.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import com.kronos.multiplatform.weatherapp.widget.components.SmallWeatherWidgetContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class SmallWeatherGlanceWidget : BaseWeatherGlanceWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        withContext(Dispatchers.IO) {
            val weatherData = loadWeatherData(context)
            provideContent {
                SmallWeatherWidgetContent(weatherData)
            }
        }
    }

    override fun getClassName(): Class<out GlanceAppWidget> {
        return SmallWeatherGlanceWidget::class.java
    }
}