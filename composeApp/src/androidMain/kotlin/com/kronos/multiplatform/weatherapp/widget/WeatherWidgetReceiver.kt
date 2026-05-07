package com.kronos.multiplatform.weatherapp.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.kronos.multiplatform.weatherapp.core.widget.WidgetUpdater
import com.kronos.multiplatform.weatherapp.job.WeatherWidgetUpdateWorker

abstract class BaseWeatherWidgetReceiver : GlanceAppWidgetReceiver() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        WeatherWidgetUpdateWorker.schedule(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WeatherWidgetUpdateWorker.schedule(context)
        WeatherWidgetUpdateWorker.forceUpdate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        val updater = WidgetUpdater(context)
        if (!updater.hasInstalledWidgets()) {
            WeatherWidgetUpdateWorker.cancel(context)
        }
    }
}

class WeatherWidgetReceiver : BaseWeatherWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SmallWeatherGlanceWidget()
}

class MediumWeatherWidgetReceiver : BaseWeatherWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MediumWeatherGlanceWidget()
}

class LargeWeatherWidgetReceiver : BaseWeatherWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LargeWeatherGlanceWidget()
}

class AnalogClockWeatherWidgetReceiver : BaseWeatherWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SmallWeatherWithAnalogClockGlanceWidget()
}

class DigitalClockWeatherWidgetReceiver : BaseWeatherWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SmallWeatherWithDigitalClockGlanceWidget()
}