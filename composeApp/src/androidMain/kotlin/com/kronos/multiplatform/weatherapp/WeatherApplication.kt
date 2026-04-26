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
import com.kronos.multiplatform.weatherapp.job.WeatherAlertNotificationWorker
import com.kronos.multiplatform.weatherapp.job.WeatherNotificationWorker
import com.kronos.multiplatform.weatherapp.job.WeatherSuggestionScheduler
import com.kronos.multiplatform.weatherapp.job.WeatherWidgetUpdateWorker
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import java.util.Date
import java.util.concurrent.TimeUnit


const val NOTIFICATION_CHANNEL = "KMP_WEATHER_NOTIFICATION_CHANNEL"
const val WEATHER_ALERT_NOTIFICATION_CHANNEL = "KMP_WEATHER_ALERT_NOTIFICATION_CHANNEL"
const val SUGGESTION_NOTIFICATION_CHANNEL = "KMP_WEATHER_SUGGESTION_CHANNEL"
const val TAG = "WeatherApp"


class WeatherApplication : Application() {

    private val exceptionHandler: ExceptionHandler by inject()

    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@WeatherApplication)
        }
        createNotificationChanel()
        createWeatherAlertNotificationChanel()
        createSuggestionNotificationChannel()

        scheduleWeatherWorker(60)
        scheduleWeatherAlertWorker(60 * 4)
        scheduleWeatherWidgetUpdateWorker()
        WeatherSuggestionScheduler.scheduleAll(this)

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


    private fun createWeatherAlertNotificationChanel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                WEATHER_ALERT_NOTIFICATION_CHANNEL,
                WEATHER_ALERT_NOTIFICATION_CHANNEL,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.description = WEATHER_ALERT_NOTIFICATION_CHANNEL
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun createSuggestionNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SUGGESTION_NOTIFICATION_CHANNEL,
                SUGGESTION_NOTIFICATION_CHANNEL,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = SUGGESTION_NOTIFICATION_CHANNEL
            }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
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
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    private fun scheduleWeatherAlertWorker(minutes: Long) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(false)
            .build()

        val validMinutes = maxOf(minutes, 15L)

        val workRequest = PeriodicWorkRequestBuilder<WeatherAlertNotificationWorker>(
            validMinutes, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WeatherAlertNotificationWorker::class.java.simpleName,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    private fun scheduleWeatherWidgetUpdateWorker() {
        WeatherWidgetUpdateWorker.schedule(this)
    }
}