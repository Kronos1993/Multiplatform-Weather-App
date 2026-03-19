package com.kronos.multiplatform.weatherapp.core.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import com.kronos.multiplatform.weatherapp.widget.AnalogClockWeatherWidgetReceiver
import com.kronos.multiplatform.weatherapp.widget.DigitalClockWeatherWidgetReceiver
import com.kronos.multiplatform.weatherapp.widget.LargeWeatherGlanceWidget
import com.kronos.multiplatform.weatherapp.widget.LargeWeatherWidgetReceiver
import com.kronos.multiplatform.weatherapp.widget.MediumWeatherGlanceWidget
import com.kronos.multiplatform.weatherapp.widget.MediumWeatherWidgetReceiver
import com.kronos.multiplatform.weatherapp.widget.SmallWeatherGlanceWidget
import com.kronos.multiplatform.weatherapp.widget.SmallWeatherWithAnalogClockGlanceWidget
import com.kronos.multiplatform.weatherapp.widget.SmallWeatherWithDigitalClockGlanceWidget
import com.kronos.multiplatform.weatherapp.widget.WeatherWidgetReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// WidgetUpdater.kt
actual class WidgetUpdater(private val context: Context) : IWidgetUpdater {

    override suspend fun updateAllWeatherWidgets() {
        try {
            updateGlanceWidgets()
            log("All weather widgets updated successfully")
        } catch (e: Exception) {
            log("Error updating widgets: ${e.message}", true)
        }
    }

    private suspend fun updateGlanceWidgets() = withContext(Dispatchers.IO) {
        try {
            val glanceAppWidgetManager = GlanceAppWidgetManager(context)

            try {
                updateGlanceWidget(glanceAppWidgetManager, SmallWeatherGlanceWidget(), "Small")
            } catch (e: Exception) {
                log("Error updating small glance widget: ${e.message}", true)
            }

            try {
                updateGlanceWidget(glanceAppWidgetManager, MediumWeatherGlanceWidget(), "Medium")
            } catch (e: Exception) {
                log("Error updating medium glance widget: ${e.message}", true)
            }

            try {
                updateGlanceWidget(glanceAppWidgetManager, LargeWeatherGlanceWidget(), "Large")
            } catch (e: Exception) {
                log("Error updating large glance widget: ${e.message}", true)
            }

            try {
                updateGlanceWidget(glanceAppWidgetManager, SmallWeatherWithDigitalClockGlanceWidget(), "Digital clock")
            } catch (e: Exception) {
                log("Error updating digital clock glance widget: ${e.message}", true)
            }

            try {
                updateGlanceWidget(glanceAppWidgetManager, SmallWeatherWithAnalogClockGlanceWidget(), "Analog clock")
            } catch (e: Exception) {
                log("Error updating analog clock glance widget: ${e.message}", true)
            }

        } catch (e: Exception) {
            log("Error in updateGlanceWidgets: ${e.message}", true)
        }
    }

    private suspend fun updateGlanceWidget(
        glanceAppWidgetManager: GlanceAppWidgetManager,
        glanceWidget: GlanceAppWidget,
        widgetName: String
    ) {
        try {
            val allGlanceIds = glanceAppWidgetManager.getGlanceIds(glanceWidget::class.java)
            if (allGlanceIds.isNotEmpty()) {
                glanceWidget.updateAll(context)
                log("$widgetName glance widget updated for ${allGlanceIds.size} instances")
            } else {
                log("No active $widgetName glance widgets found")
            }
        } catch (e: Exception) {
            log("Error updating $widgetName glance widget: ${e.message}", true)
            throw e
        }
    }

    suspend fun updateSpecificWidget(widgetClass: Class<*>) = withContext(Dispatchers.IO) {
        try {
            val glanceAppWidgetManager = GlanceAppWidgetManager(context)
            when (widgetClass) {
                WeatherWidgetReceiver::class.java ->
                    updateGlanceWidget(glanceAppWidgetManager, SmallWeatherGlanceWidget(), "Small")
                MediumWeatherWidgetReceiver::class.java ->
                    updateGlanceWidget(glanceAppWidgetManager, MediumWeatherGlanceWidget(), "Medium")
                LargeWeatherWidgetReceiver::class.java ->
                    updateGlanceWidget(glanceAppWidgetManager, LargeWeatherGlanceWidget(), "Large")
                SmallWeatherWithAnalogClockGlanceWidget::class.java ->
                    updateGlanceWidget(glanceAppWidgetManager, SmallWeatherWithAnalogClockGlanceWidget(), "Analog Clock")
                SmallWeatherWithDigitalClockGlanceWidget::class.java ->
                    updateGlanceWidget(glanceAppWidgetManager, SmallWeatherWithDigitalClockGlanceWidget(), "Digital Clock")
            }
            log("Specific widget ${widgetClass.simpleName} updated")
        } catch (e: Exception) {
            log("Error updating specific widget ${widgetClass.simpleName}: ${e.message}", true)
        }
    }

    fun getInstalledWidgetsInfo(): Map<String, Int> {
        return try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            mapOf(
                "Small" to appWidgetManager.getAppWidgetIds(ComponentName(context, WeatherWidgetReceiver::class.java)).size,
                "Medium" to appWidgetManager.getAppWidgetIds(ComponentName(context, MediumWeatherWidgetReceiver::class.java)).size,
                "Large" to appWidgetManager.getAppWidgetIds(ComponentName(context, LargeWeatherWidgetReceiver::class.java)).size,
                "AnalogClock" to appWidgetManager.getAppWidgetIds(ComponentName(context, AnalogClockWeatherWidgetReceiver::class.java)).size,
                "DigitalClock" to appWidgetManager.getAppWidgetIds(ComponentName(context, DigitalClockWeatherWidgetReceiver::class.java)).size
            )
        } catch (e: Exception) {
            log("Error getting widget info: ${e.message}", true)
            emptyMap()
        }
    }

    fun hasInstalledWidgets(): Boolean = getInstalledWidgetsInfo().values.sum() > 0

    private fun log(item: String, isError: Boolean = false) {
        val tag = if (isError) "ERROR" else "INFO"
        println("$tag: $item")
    }
}