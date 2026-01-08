package com.kronos.multiplatform.weatherapp.core.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
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

actual class WidgetUpdater(private val context: Context) : IWidgetUpdater {

    override suspend fun updateAllWeatherWidgets() {
        try {
            updateGlanceWidgets()

            sendWidgetUpdateBroadcast()

            log("All weather widgets updated successfully")
        } catch (e: Exception) {
            log("Error updating widgets: ${e.message}", true)
        }
    }

    /**
     * Actualiza widgets específicos usando GlanceAppWidgetManager
     */
    private suspend fun updateGlanceWidgets() = withContext(Dispatchers.IO) {
        try {
            val glanceAppWidgetManager = GlanceAppWidgetManager(context)

            try {
                val smallGlanceWidget = SmallWeatherGlanceWidget()
                updateGlanceWidget(glanceAppWidgetManager, smallGlanceWidget, "Small")
            } catch (e: Exception) {
                log("Error updating small glance widget: ${e.message}", true)
            }

            try {
                val mediumGlanceWidget = MediumWeatherGlanceWidget()
                updateGlanceWidget(glanceAppWidgetManager, mediumGlanceWidget, "Medium")
            } catch (e: Exception) {
                log("Error updating medium glance widget: ${e.message}", true)
            }

            try {
                val largeGlanceWidget = LargeWeatherGlanceWidget()
                updateGlanceWidget(glanceAppWidgetManager, largeGlanceWidget, "Large")
            } catch (e: Exception) {
                log("Error updating large glance widget: ${e.message}", true)
            }

            try {
                val digitalClockGlanceWidget = SmallWeatherWithDigitalClockGlanceWidget()
                updateGlanceWidget(
                    glanceAppWidgetManager,
                    digitalClockGlanceWidget,
                    "Digital clock"
                )
            } catch (e: Exception) {
                log("Error updating large glance widget: ${e.message}", true)
            }

            try {
                val analogClockGlanceWidget = SmallWeatherWithAnalogClockGlanceWidget()
                updateGlanceWidget(glanceAppWidgetManager, analogClockGlanceWidget, "Analog clock")
            } catch (e: Exception) {
                log("Error updating large glance widget: ${e.message}", true)
            }

        } catch (e: Exception) {
            log("Error in updateGlanceWidgets: ${e.message}", true)
        }
    }

    /**
     * Método helper para actualizar un widget Glance específico
     */
    private suspend fun updateGlanceWidget(
        glanceAppWidgetManager: GlanceAppWidgetManager,
        glanceWidget: GlanceAppWidget,
        widgetName: String
    ) {
        try {
            val allGlanceIds: List<GlanceId> =
                glanceAppWidgetManager.getGlanceIds(glanceWidget::class.java)

            if (allGlanceIds.isNotEmpty()) {
                allGlanceIds.forEach { glanceId ->
                    glanceWidget.update(context, glanceId)
                }
                log("$widgetName glance widget updated for ${allGlanceIds.size} instances")
            } else {
                log("No active $widgetName glance widgets found")
            }
        } catch (e: Exception) {
            log("Error updating $widgetName glance widget: ${e.message}", true)
            throw e
        }
    }

    /**
     * Determina si un GlanceId pertenece a un tipo específico de widget
     */
    private fun belongsToWidget(
        glanceId: GlanceId,
        widgetClass: Class<*>,
        widgetName: String
    ): Boolean {
        return try {
            when (widgetName) {
                "Small" -> glanceId.toString().contains("WeatherGlanceWidget", ignoreCase = true)
                "Medium" -> glanceId.toString()
                    .contains("MediumWeatherGlanceWidget", ignoreCase = true)

                "Large" -> glanceId.toString()
                    .contains("LargeWeatherGlanceWidget", ignoreCase = true)

                else -> false
            }
        } catch (e: Exception) {
            log("Error checking widget type for $glanceId: ${e.message}", true)
            false
        }
    }

    /**
     * Envía broadcast para actualizar widgets
     */
    private fun sendWidgetUpdateBroadcast() {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)

            updateWidgetsByClass(appWidgetManager, WeatherWidgetReceiver::class.java, "Small")

            updateWidgetsByClass(
                appWidgetManager,
                MediumWeatherWidgetReceiver::class.java,
                "Medium"
            )

            updateWidgetsByClass(appWidgetManager, LargeWeatherWidgetReceiver::class.java, "Large")

            updateWidgetsByClass(
                appWidgetManager,
                SmallWeatherWithAnalogClockGlanceWidget::class.java,
                "Analog Clock"
            )

            updateWidgetsByClass(
                appWidgetManager,
                SmallWeatherWithDigitalClockGlanceWidget::class.java,
                "Digital Clock"
            )

        } catch (e: Exception) {
            log("Error sending widget broadcast: ${e.message}", true)
        }
    }

    /**
     * Actualiza widgets específicos por clase usando broadcast
     */
    private fun updateWidgetsByClass(
        appWidgetManager: AppWidgetManager,
        widgetClass: Class<*>,
        widgetName: String
    ) {
        try {
            val componentName = ComponentName(context, widgetClass)
            val widgetIds = appWidgetManager.getAppWidgetIds(componentName)

            if (widgetIds.isNotEmpty()) {
                val updateIntent = Intent(context, widgetClass)
                updateIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
                context.sendBroadcast(updateIntent)

                log("$widgetName widget broadcast sent for ${widgetIds.size} widgets")
            } else {
                log("No $widgetName widgets installed")
            }
        } catch (e: Exception) {
            log("Error updating $widgetName widgets: ${e.message}", true)
        }
    }

    /**
     * Actualiza solo un tipo específico de widget
     */
    suspend fun updateSpecificWidget(widgetClass: Class<*>) = withContext(Dispatchers.IO) {
        try {
            val glanceAppWidgetManager = GlanceAppWidgetManager(context)

            when (widgetClass) {
                WeatherWidgetReceiver::class.java -> {
                    val glanceWidget = SmallWeatherGlanceWidget()
                    updateGlanceWidget(glanceAppWidgetManager, glanceWidget, "Small")
                }

                MediumWeatherWidgetReceiver::class.java -> {
                    val glanceWidget = MediumWeatherGlanceWidget()
                    updateGlanceWidget(glanceAppWidgetManager, glanceWidget, "Medium")
                }

                LargeWeatherWidgetReceiver::class.java -> {
                    val glanceWidget = LargeWeatherGlanceWidget()
                    updateGlanceWidget(glanceAppWidgetManager, glanceWidget, "Large")
                }

                SmallWeatherWithAnalogClockGlanceWidget::class.java -> {
                    val glanceWidget = SmallWeatherWithAnalogClockGlanceWidget()
                    updateGlanceWidget(glanceAppWidgetManager, glanceWidget, "Analog Clock")
                }

                SmallWeatherWithDigitalClockGlanceWidget::class.java -> {
                    val glanceWidget = SmallWeatherWithDigitalClockGlanceWidget()
                    updateGlanceWidget(glanceAppWidgetManager, glanceWidget, "Digital Clock")
                }
            }
            log("Specific widget ${widgetClass.simpleName} updated")
        } catch (e: Exception) {
            log("Error updating specific widget ${widgetClass.simpleName}: ${e.message}", true)
        }
    }

    /**
     * Recrea widgets específicos notificando cambio de datos
     */
    private fun recreateWidgetsByClass(
        appWidgetManager: AppWidgetManager,
        widgetClass: Class<*>,
        widgetName: String
    ) {
        try {
            val componentName = ComponentName(context, widgetClass)
            val widgetIds = appWidgetManager.getAppWidgetIds(componentName)

            if (widgetIds.isNotEmpty()) {
                appWidgetManager.notifyAppWidgetViewDataChanged(widgetIds, android.R.id.content)
                log("$widgetName widgets recreated for ${widgetIds.size} instances")
            }
        } catch (e: Exception) {
            log("Error recreating $widgetName widgets: ${e.message}", true)
        }
    }

    /**
     * Obtiene información sobre los widgets instalados
     */
    fun getInstalledWidgetsInfo(): Map<String, Int> {
        return try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val info = mutableMapOf<String, Int>()

            info["Small"] = appWidgetManager.getAppWidgetIds(
                ComponentName(context, WeatherWidgetReceiver::class.java)
            ).size

            info["Medium"] = appWidgetManager.getAppWidgetIds(
                ComponentName(context, MediumWeatherWidgetReceiver::class.java)
            ).size

            info["Large"] = appWidgetManager.getAppWidgetIds(
                ComponentName(context, LargeWeatherWidgetReceiver::class.java)
            ).size

            info["AnalogClock"] = appWidgetManager.getAppWidgetIds(
                ComponentName(context, AnalogClockWeatherWidgetReceiver::class.java)
            ).size

            info["DigitalClock"] = appWidgetManager.getAppWidgetIds(
                ComponentName(context, DigitalClockWeatherWidgetReceiver::class.java)
            ).size

            info
        } catch (e: Exception) {
            log("Error getting widget info: ${e.message}", true)
            emptyMap()
        }
    }

    /**
     * Verifica si hay algún widget instalado
     */
    fun hasInstalledWidgets(): Boolean {
        val info = getInstalledWidgetsInfo()
        return info.values.sum() > 0
    }

    private fun log(item: String, isError: Boolean = false) {
        val tag = if (isError) "ERROR" else "INFO"
        println("$tag: $item")
    }
}