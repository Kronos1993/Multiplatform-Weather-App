package com.kronos.multiplatform.weatherapp.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import com.kronos.multiplatform.weatherapp.widget.components.LargeWeatherWidgetContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LargeWeatherGlanceWidget : BaseWeatherGlanceWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        withContext(Dispatchers.IO) {
            val weatherData =
                loadWeatherData(context)
            provideContent {
                LargeWeatherWidgetContent(weatherData,context)
            }
        }
    }

    override fun getClassName(): Class<out GlanceAppWidget> {
        return LargeWeatherGlanceWidget::class.java
    }
}