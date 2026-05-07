package com.kronos.multiplatform.weatherapp.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.LocalContext
import androidx.glance.appwidget.provideContent
import com.kronos.multiplatform.weatherapp.R
import com.kronos.multiplatform.weatherapp.widget.components.LargeWeatherWidgetContent
import com.kronos.multiplatform.weatherapp.widget.components.WeatherWidgetBackground
import com.kronos.multiplatform.weatherapp.widget.components.WeatherWidgetErrorContent

class LargeWeatherGlanceWidget : BaseWeatherGlanceWidget() {
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
                if (weatherData != null) {
                    LargeWeatherWidgetContent(weatherData, LocalContext.current)
                } else {
                    WeatherWidgetErrorContent(context.getString(R.string.widget_error_text))
                }
            }
        }
    }

    override fun getClassName() = LargeWeatherGlanceWidget::class.java
}