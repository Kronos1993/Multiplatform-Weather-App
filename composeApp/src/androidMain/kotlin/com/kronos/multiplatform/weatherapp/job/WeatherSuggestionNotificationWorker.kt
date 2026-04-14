package com.kronos.multiplatform.weatherapp.job

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kronos.multiplatform.weatherapp.R
import com.kronos.multiplatform.weatherapp.core.logguer.ILogManager
import com.kronos.multiplatform.weatherapp.core.logguer.LogLevel
import com.kronos.multiplatform.weatherapp.core.notification.INotifications
import com.kronos.multiplatform.weatherapp.core.notification.NotificationGroup
import com.kronos.multiplatform.weatherapp.core.notification.NotificationType
import com.kronos.multiplatform.weatherapp.core.preferences.repository.PreferenceRepository
import com.kronos.multiplatform.weatherapp.data.mapper.mapCurrentSuggestions
import com.kronos.multiplatform.weatherapp.data.mapper.mapMorningSuggestions
import com.kronos.multiplatform.weatherapp.data.mapper.mapTomorrowNotification
import com.kronos.multiplatform.weatherapp.domain.model.MeasureUnit
import com.kronos.multiplatform.weatherapp.domain.model.SuggestionPriority
import com.kronos.multiplatform.weatherapp.domain.model.SuggestionType
import com.kronos.multiplatform.weatherapp.domain.model.WeatherSuggestionModel
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WeatherSuggestionNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val TAG = this::class.simpleName.orEmpty()

    private val preferenceRepository: PreferenceRepository by inject()
    private val notifications: INotifications by inject()
    private val loggerManager: ILogManager by inject()

    // Tipo de notificación que dispara este worker
    // Se pasa como inputData desde el scheduler
    companion object {
        const val KEY_NOTIFICATION_TYPE = "notification_type"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val notificationType = NotificationType.from(inputData.getString(KEY_NOTIFICATION_TYPE).orEmpty()) ?: NotificationType.WEATHER_SUGGESTION_MORNING

            val measureUnit = MeasureUnit.from(
                preferenceRepository.getPreference(
                    applicationContext.getString(R.string.measure_unit_key),
                    applicationContext.getString(R.string.measure_unit_preference_default_value)
                )
            )


            val json = preferenceRepository.getPreference(
                applicationContext.getString(R.string.current_weather_key),
                ""
            )

            if (json.isEmpty()) {
                log("No hay datos de clima en cache, saltando notificación", true)
                return@withContext Result.success()
            }

            val jsonConfig = Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
                explicitNulls = false
                useAlternativeNames = false
            }

            val forecast = jsonConfig.decodeFromString<Forecast>(json)
            val timeZone = forecast.location.tzId

            when (notificationType) {
                NotificationType.WEATHER_SUGGESTION_EVENING -> handleEveningSuggestion(forecast, measureUnit)
                NotificationType.WEATHER_SUGGESTION_MIDDAY  -> handleMiddaySuggestions(forecast, timeZone, measureUnit)
                else -> handleMorningSuggestions(forecast, timeZone, measureUnit)
            }

            Result.success()
        } catch (e: CancellationException) {
            log("Worker cancelado", true)
            Result.success()
        } catch (e: Exception) {
            log("Error en suggestion worker: ${e.message}", true)
            Result.failure()
        }
    }
    private fun handleMorningSuggestions(
        forecast: Forecast,
        timeZone: String,
        measureUnit: MeasureUnit
    ) {
        val suggestions = forecast.mapMorningSuggestions(timeZone, measureUnit)
        if (suggestions.isEmpty()) return

        val top = suggestions.first()
        notifications.createNotification(
            title = resolveTitle(top),
            shortDescription = resolveMessage(top),
            description = suggestions.joinToString(" • ") { resolveTitle(it) },
            notificationImageUrl = "",
            group = NotificationGroup.SUGGESTION,
            notificationsId = NotificationType.WEATHER_SUGGESTION_MORNING
        )
    }

    // ── Noche: pronóstico de mañana ────────────────────────────────────────
    private fun handleEveningSuggestion(
        forecast: Forecast,
        measureUnit: MeasureUnit
    ) {
        val suggestion = forecast.mapTomorrowNotification(measureUnit) ?: return

        notifications.createNotification(
            title = resolveTitle(suggestion),
            shortDescription = resolveMessage(suggestion),
            description = resolveMessage(suggestion),
            notificationImageUrl = "",
            group = NotificationGroup.SUGGESTION,
            notificationsId = NotificationType.WEATHER_SUGGESTION_EVENING
        )
    }

    private fun handleMiddaySuggestions(
        forecast: Forecast,
        timeZone: String,
        measureUnit: MeasureUnit
    ) {
        val suggestions = forecast.mapCurrentSuggestions(timeZone, measureUnit)
        val rainSuggestion = suggestions.firstOrNull { it.type == SuggestionType.RAIN }
            ?: return

        notifications.createNotification(
            title = resolveTitle(rainSuggestion),
            shortDescription = resolveMessage(rainSuggestion),
            description = resolveMessage(rainSuggestion),
            notificationImageUrl = "",
            group = NotificationGroup.SUGGESTION,
            notificationsId = NotificationType.WEATHER_SUGGESTION_MIDDAY
        )
    }

    private fun resolveTitle(suggestion: WeatherSuggestionModel): String {
        val res = applicationContext.resources
        return when (suggestion.type) {
            SuggestionType.RAIN ->
                res.getString(R.string.suggestion_rain_title)
            SuggestionType.UV -> when (suggestion.priority) {
                SuggestionPriority.HIGH -> res.getString(R.string.suggestion_uv_high_title)
                else -> res.getString(R.string.suggestion_uv_medium_title)
            }
            SuggestionType.HEAT ->
                res.getString(R.string.suggestion_heat_title)
            SuggestionType.WIND ->
                res.getString(R.string.suggestion_wind_title)
            SuggestionType.HUMIDITY ->
                res.getString(R.string.suggestion_humidity_title)
            SuggestionType.TOMORROW_FORECAST -> when (suggestion.priority) {
                SuggestionPriority.HIGH -> res.getString(R.string.suggestion_tomorrow_rain_title)
                SuggestionPriority.MEDIUM -> res.getString(R.string.suggestion_tomorrow_uv_title)
                else -> res.getString(R.string.suggestion_tomorrow_clear_title)
            }
            SuggestionType.MORNING_SUMMARY -> when (suggestion.priority) {
                SuggestionPriority.HIGH -> when (suggestion.icon) {
                    "🌂" -> res.getString(R.string.suggestion_morning_rain_title)
                    else -> res.getString(R.string.suggestion_morning_uv_title)
                }
                else -> res.getString(R.string.suggestion_morning_heat_title)
            }
        }
    }

    private fun resolveMessage(suggestion: WeatherSuggestionModel): String {
        val res = applicationContext.resources
        val template = when (suggestion.type) {
            SuggestionType.RAIN ->
                res.getString(R.string.suggestion_rain_message)
            SuggestionType.UV -> when (suggestion.priority) {
                SuggestionPriority.HIGH -> res.getString(R.string.suggestion_uv_high_message)
                else -> res.getString(R.string.suggestion_uv_medium_message)
            }
            SuggestionType.HEAT ->
                res.getString(R.string.suggestion_heat_message)
            SuggestionType.WIND ->
                res.getString(R.string.suggestion_wind_message)
            SuggestionType.HUMIDITY ->
                res.getString(R.string.suggestion_humidity_message)
            SuggestionType.TOMORROW_FORECAST -> when (suggestion.priority) {
                SuggestionPriority.HIGH -> res.getString(R.string.suggestion_tomorrow_rain_message)
                SuggestionPriority.MEDIUM -> res.getString(R.string.suggestion_tomorrow_uv_message)
                else -> res.getString(R.string.suggestion_tomorrow_clear_message)
            }
            SuggestionType.MORNING_SUMMARY -> when (suggestion.priority) {
                SuggestionPriority.HIGH -> when (suggestion.icon) {
                    "🌂" -> res.getString(R.string.suggestion_morning_rain_message)
                    else -> res.getString(R.string.suggestion_morning_uv_message)
                }
                else -> res.getString(R.string.suggestion_morning_heat_message)
            }
        }
        // Aplicar args en orden
        return suggestion.args.foldIndexed(template) { index, acc, arg ->
            acc.replace("%${index + 1}\$s", arg)
        }
    }

    private suspend fun log(item: String, isError: Boolean = false) {
        if (isError) {
            println("ERROR: $item")
            loggerManager.log(LogLevel.ERROR, TAG, item)
        } else {
            println("INFO: $item")
            loggerManager.log(LogLevel.INFO, TAG, item)
        }
    }
}