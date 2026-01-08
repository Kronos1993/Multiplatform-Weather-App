package com.kronos.multiplatform.weatherapp.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import com.kronos.multiplatform.weatherapp.R
import com.kronos.multiplatform.weatherapp.widget.components.WeatherWidgetErrorContent
import com.kronos.multiplatform.weatherapp.widget.components.WeatherWithAnalogClockContent

class SmallWeatherWithAnalogClockGlanceWidget : BaseWeatherGlanceWidget() {

    override val TAG = this::class.simpleName.orEmpty()

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        try {
            val weatherData = runCatching {
                loadWeatherDataFromCache(context)
            }.getOrNull()

            provideContent{
                WeatherWithAnalogClockContent(weatherData)
            }

        } catch (e: Exception) {
            log("Error providing glance content: ${e.message}", true)
            provideContent {
                WeatherWidgetErrorContent(context.getString(R.string.widget_error_text))
            }
        }
    }

    override fun getClassName(): Class<out GlanceAppWidget> {
        return SmallWeatherWithAnalogClockGlanceWidget::class.java
    }
}
