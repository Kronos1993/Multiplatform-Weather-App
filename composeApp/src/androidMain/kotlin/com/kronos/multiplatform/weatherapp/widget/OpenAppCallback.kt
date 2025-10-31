package com.kronos.multiplatform.weatherapp.widget

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.kronos.multiplatform.weatherapp.MainActivity

class OpenAppCallback : ActionCallback{

    //private val widgetUpdater: WidgetUpdater by inject()

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        Log.i("OpenAppCallback", "onAction: clicked")
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
            //widgetUpdater.updateAllWeatherWidgets()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}