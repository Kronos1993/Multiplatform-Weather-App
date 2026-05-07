package com.kronos.multiplatform.weatherapp.job

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kronos.multiplatform.weatherapp.core.notification.NotificationType
import java.util.Calendar
import java.util.concurrent.TimeUnit

object WeatherSuggestionScheduler {

    // ── Calcula el delay en ms hasta la próxima ocurrencia de una hora ─────
    private fun delayUntil(hour: Int, minute: Int = 0): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // Si ya pasó la hora de hoy, programar para mañana
        if (target.before(now)) target.add(Calendar.DAY_OF_YEAR, 1)
        return target.timeInMillis - now.timeInMillis
    }

    // ── Amanecer: se dispara a las 6:00 AM ────────────────────────────────
    fun scheduleMorning(context: Context) {
        val delay = delayUntil(hour = 6, minute = 0)
        val data = Data.Builder()
            .putString(
                WeatherSuggestionNotificationWorker.KEY_NOTIFICATION_TYPE,
                NotificationType.WEATHER_SUGGESTION_MORNING.toString()
            )
            .build()

        val request = OneTimeWorkRequestBuilder<WeatherSuggestionNotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("suggestion_morning")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "suggestion_morning",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    // ── Mediodía: se dispara a las 12:00 PM ───────────────────────────────
    fun scheduleMidDay(context: Context) {
        val delay = delayUntil(hour = 12, minute = 0)
        val data = Data.Builder()
            .putString(
                WeatherSuggestionNotificationWorker.KEY_NOTIFICATION_TYPE,
                NotificationType.WEATHER_SUGGESTION_MIDDAY.toString()
            )
            .build()

        val request = OneTimeWorkRequestBuilder<WeatherSuggestionNotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("suggestion_midday")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "suggestion_midday",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    // ── Noche: se dispara a las 9:00 PM ───────────────────────────────────
    fun scheduleEvening(context: Context) {
        val delay = delayUntil(hour = 21, minute = 0)
        val data = Data.Builder()
            .putString(
                WeatherSuggestionNotificationWorker.KEY_NOTIFICATION_TYPE,
                NotificationType.WEATHER_SUGGESTION_EVENING.toString()
            )
            .build()

        val request = OneTimeWorkRequestBuilder<WeatherSuggestionNotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("suggestion_evening")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "suggestion_evening",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    // ── Programa los 3 de una vez ──────────────────────────────────────────
    fun scheduleAll(context: Context) {
        scheduleMorning(context)
        scheduleMidDay(context)
        scheduleEvening(context)
    }

    // ── Re-programa diariamente (llamado desde WeatherNotificationWorker) ──
    fun scheduleDailyPeriodic(context: Context) {
        val data = Data.Builder()
            .putString(
                WeatherSuggestionNotificationWorker.KEY_NOTIFICATION_TYPE,
                NotificationType.WEATHER_SUGGESTION_MORNING.toString()
            )
            .build()

        val request = PeriodicWorkRequestBuilder<WeatherSuggestionNotificationWorker>(
            24, TimeUnit.HOURS
        )
            .setInputData(data)
            .addTag("suggestion_daily")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "suggestion_daily",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelAll(context: Context) {
        WorkManager.getInstance(context).apply {
            cancelUniqueWork("suggestion_morning")
            cancelUniqueWork("suggestion_midday")
            cancelUniqueWork("suggestion_evening")
        }
    }
}