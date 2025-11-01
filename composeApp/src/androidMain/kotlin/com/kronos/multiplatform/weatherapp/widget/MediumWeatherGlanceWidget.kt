package com.kronos.multiplatform.weatherapp.widget

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import com.kronos.multiplatform.weatherapp.R
import com.kronos.multiplatform.weatherapp.widget.components.MediumWeatherWidgetContent
import com.kronos.multiplatform.weatherapp.widget.components.WeatherWidgetErrorContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediumWeatherGlanceWidget : BaseWeatherGlanceWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        try {
            val weatherData = withContext(Dispatchers.IO) {
                loadWeatherData(context)
            }

            provideContent {
                if (weatherData != null) {
                    MediumWeatherWidgetContent(weatherData)
                } else {
                    WeatherWidgetErrorContent(context.getString(R.string.widget_error_text))
                }
            }

        } catch (e: Exception) {
            Log.e("MediumWeatherGlanceWidget", "Error providing glance content", e)

            provideContent {
                WeatherWidgetErrorContent(context.getString(R.string.widget_error_text))
            }
        }
    }

    override fun getClassName(): Class<out GlanceAppWidget> {
        return MediumWeatherGlanceWidget::class.java
    }
}
