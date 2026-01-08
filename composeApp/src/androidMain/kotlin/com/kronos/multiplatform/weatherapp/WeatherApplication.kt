package com.kronos.multiplatform.weatherapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kronos.multiplatform.weatherapp.core.exception.ExceptionHandler
import com.kronos.multiplatform.weatherapp.di.initKoin
import com.kronos.multiplatform.weatherapp.job.WeatherNotificationWorker
import com.mmk.kmpnotifier.notification.NotifierManager
import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import java.util.Date
import java.util.concurrent.TimeUnit


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

        scheduleWeatherWorker(15)

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

    private fun scheduleWeatherWorker(minutes: Long) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(false)
            .build()

        val validMinutes = maxOf(minutes, 15L)

        val workRequest = PeriodicWorkRequestBuilder<WeatherNotificationWorker>(
            validMinutes, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WeatherNotificationWorker::class.java.simpleName,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}