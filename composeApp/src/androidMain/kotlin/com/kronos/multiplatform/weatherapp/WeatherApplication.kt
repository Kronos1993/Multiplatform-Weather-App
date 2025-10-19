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
import com.mmk.kmpnotifier.notification.NotifierManager
import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import java.util.Date
import kotlin.jvm.java


const val NOTIFICATION_CHANNEL = "WEATHER_NOTIFICATION_CHANNEL"
const val TAG = "WeatherApp"


class WeatherApplication: Application(){

    private val exceptionHandler: ExceptionHandler by inject()

    override fun onCreate() {
        super.onCreate()
        initKoin{
            androidContext(this@WeatherApplication)
        }
        createNotificationChanel()
        runBlocking {
            scheduleJob(applicationContext, 15 * 60000L)
        }
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
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun createNotificationChanel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL, NOTIFICATION_CHANNEL, NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.description = NOTIFICATION_CHANNEL
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun scheduleJob(context: Context, periodic: Long) {
        // Asegúrate de que el intervalo sea al menos de 15 minutos
        val validInterval = if (periodic < 15 * 60000L) {
            15 * 60000L // Establecer al menos 15 minutos
        } else {
            periodic
        }

        val componentName = ComponentName(context, WeatherNotificationJob::class.java)

        val jobInfo = JobInfo.Builder(notificationJobId, componentName)
            .setPersisted(true)
            .setPeriodic(validInterval) // Establecer intervalo válido
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) // O cualquier condición necesaria (ej. red, carga de batería)
            .setBackoffCriteria(60000L, JobInfo.BACKOFF_POLICY_EXPONENTIAL) // Establecer políticas de retroceso
            .build()

        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val resultCode = scheduler.schedule(jobInfo)

        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "Job service scheduled successfully with job ID $notificationJobId")
        } else {
            Log.d(TAG, "Job service schedule failed with job ID $notificationJobId")
        }
    }


}