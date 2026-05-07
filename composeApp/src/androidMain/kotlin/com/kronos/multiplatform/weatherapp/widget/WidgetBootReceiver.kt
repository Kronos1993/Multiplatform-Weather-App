package com.kronos.multiplatform.weatherapp.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kronos.multiplatform.weatherapp.job.WeatherWidgetUpdateWorker

class WidgetBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            WeatherWidgetUpdateWorker.schedule(context)
        }
    }
}