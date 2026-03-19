package com.kronos.multiplatform.weatherapp.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.provideContent
import com.kronos.multiplatform.weatherapp.widget.components.WeatherWidgetBackground
import com.kronos.multiplatform.weatherapp.widget.components.WeatherWithDigitalClockContent

class SmallWeatherWithDigitalClockGlanceWidget : BaseWeatherGlanceWidget() {
    override val TAG = this::class.simpleName.orEmpty()

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val weatherData = try {
            loadWeatherDataFromCache(context)
        } catch (e: Exception) {
            log("Error fatal en provideGlance: ${e.message}", isError = true)
            null
        }

        provideContent {
            WeatherWidgetBackground {
                WeatherWithDigitalClockContent(weatherData)
            }
        }
    }

    override fun getClassName() = SmallWeatherWithDigitalClockGlanceWidget::class.java
}
