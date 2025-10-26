package com.kronos.multiplatform.weatherapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.util.Log
import com.kronos.multiplatform.weatherapp.core.exception.ExceptionHandler
import com.kronos.multiplatform.weatherapp.di.initKoin
import com.kronos.multiplatform.weatherapp.job.WeatherNotificationJob
import com.kronos.multiplatform.weatherapp.job.notificationJobId
import com.kronos.multiplatform.weatherapp.widget.WidgetUpdater
import com.mmk.kmpnotifier.notification.NotifierManager
import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import java.util.Date


const val NOTIFICATION_CHANNEL = "KMP_WEATHER_NOTIFICATION_CHANNEL"
const val TAG = "WeatherApp"


class WeatherApplication : Application() {

    private val exceptionHandler: ExceptionHandler by inject()

    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@WeatherApplication)
        }
        createNotificationChanel()

        scheduleJobIfNeeded(applicationContext, 60 * 60000L)

        NotifierManager.initialize(
            configuration = NotificationPlatformConfiguration.Android(
                notificationIconResId = R.drawable.ic_weather_app_icon,
                showPushNotification = true,
                notificationChannelData = NotificationPlatformConfiguration.Android.NotificationChannelData(
                    id = NOTIFICATION_CHANNEL,
                    name = NOTIFICATION_CHANNEL,
                    description = NOTIFICATION_CHANNEL
                )
            )
        )

        try {
            exceptionHandler.init()
            Log.d(TAG, "App open on ${Date().toLocaleString()}")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        runBlocking(Dispatchers.IO) {
            val widgetUpdater = WidgetUpdater(applicationContext)
            widgetUpdater.updateAllWeatherWidgets()
        }
    }

    private fun createNotificationChanel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL, NOTIFICATION_CHANNEL, NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.description = NOTIFICATION_CHANNEL
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun scheduleJobIfNeeded(context: Context, periodic: Long) {
        val scheduler = context.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler

        val pendingJob = scheduler.getPendingJob(notificationJobId)
        if (pendingJob != null) {
            Log.d(TAG, "Job already scheduled with ID $notificationJobId")
            return
        }

        val validInterval = if (periodic < 15 * 60000L) {
            15 * 60000L // Establecer al menos 15 minutos
        } else {
            periodic
        }

        val componentName = ComponentName(context, WeatherNotificationJob::class.java)

        val jobInfo = JobInfo.Builder(notificationJobId, componentName)
            .setPersisted(true)
            .setPeriodic(validInterval) // Establecer intervalo válido
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .build()

        val resultCode = scheduler.schedule(jobInfo)

        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(
                TAG,
                "Job service scheduled successfully with job ID $notificationJobId, interval: ${validInterval / 60000} minutes"
            )
        } else {
            Log.d(TAG, "Job service schedule failed with job ID $notificationJobId")
        }
    }

}