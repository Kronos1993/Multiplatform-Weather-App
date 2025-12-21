package com.kronos.multiplatform.weatherapp.widget

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import com.kronos.multiplatform.weatherapp.R
import com.kronos.multiplatform.weatherapp.widget.components.LargeWeatherWidgetContent
import com.kronos.multiplatform.weatherapp.widget.components.WeatherWidgetErrorContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LargeWeatherGlanceWidget : BaseWeatherGlanceWidget() {

    override val TAG = this::class.simpleName.orEmpty()

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        try {
            val weatherData = runCatching {
                loadWeatherDataFromCache(context)
            }.getOrNull()

            provideContent {
                if (weatherData != null) {
                    LargeWeatherWidgetContent(weatherData, context)
                } else {
                    WeatherWidgetErrorContent(context.getString(R.string.widget_error_text))
                }
            }

        } catch (e: Exception) {
            Log.e("MediumWeatherGlanceWidget", "Error providing glance content", e)
            log("Error providing glance content: ${e.message}",true)
            provideContent {
                WeatherWidgetErrorContent(context.getString(R.string.widget_error_text))
            }
        }
    }

    override fun getClassName(): Class<out GlanceAppWidget> {
        return LargeWeatherGlanceWidget::class.java
    }
}