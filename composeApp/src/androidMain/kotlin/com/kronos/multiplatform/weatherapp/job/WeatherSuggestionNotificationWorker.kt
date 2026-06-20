package com.kronos.multiplatform.weatherapp.job

import android.content.Context
import android.content.res.Resources
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
import com.kronos.multiplatform.weatherapp.core.result.onError
import com.kronos.multiplatform.weatherapp.core.result.onSuccess
import com.kronos.multiplatform.weatherapp.core.util.IChangeLang
import com.kronos.multiplatform.weatherapp.data.mapper.mapCurrentSuggestions
import com.kronos.multiplatform.weatherapp.data.mapper.mapMorningSuggestions
import com.kronos.multiplatform.weatherapp.data.mapper.mapTomorrowNotification
import com.kronos.multiplatform.weatherapp.domain.model.DayMoment
import com.kronos.multiplatform.weatherapp.domain.model.MeasureUnit
import com.kronos.multiplatform.weatherapp.domain.model.SuggestionArg
import com.kronos.multiplatform.weatherapp.domain.model.SuggestionPriority
import com.kronos.multiplatform.weatherapp.domain.model.SuggestionType
import com.kronos.multiplatform.weatherapp.domain.model.UvIndexLevel
import com.kronos.multiplatform.weatherapp.domain.model.WeatherSuggestionModel
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast
import com.kronos.multiplatform.weatherapp.domain.repository.UserCustomLocationLocalRepository
import com.kronos.multiplatform.weatherapp.domain.repository.WeatherRemoteRepository
import com.kronos.multiplatform.weatherapp.job.model.NotificationWeatherParams
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class WeatherSuggestionNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val TAG = this::class.simpleName.orEmpty()
    private val weatherRemoteRepository: WeatherRemoteRepository by inject()
    private val userCustomLocationLocalRepository: UserCustomLocationLocalRepository by inject()
    private val preferenceRepository: PreferenceRepository by inject()
    private val notifications: INotifications by inject()
    private val loggerManager: ILogManager by inject()
    private val changeLang: IChangeLang by inject()

    // Tipo de notificación que dispara este worker
    // Se pasa como inputData desde el scheduler
    companion object {
        const val KEY_NOTIFICATION_TYPE = "notification_type"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val currentLang = preferenceRepository.getPreference(
                applicationContext.getString(R.string.default_lang_key),
                applicationContext.getString(R.string.default_language_value)
            )
            changeLang.onLangChange(currentLang)
            if (!hasValidatedNetworkConnection()) {
                log("No hay conexión validada, reintentando...", true)
                return@withContext Result.retry()
            }

            refreshWeather()
            Result.success()
        } catch (e: Exception) {
            handleWorkerError(e)
        }
    }

    private suspend fun refreshWeather() {
        val currentCity = userCustomLocationLocalRepository.getSelectedLocation()
            ?: userCustomLocationLocalRepository.getCurrentLocation()

        val weatherParams = getWeatherParams()

        val success = withRetry(maxRetries = 3) {
            when {
                currentCity != null && currentCity.lat != null && currentCity.lon != null -> {
                    fetchAndNotifyWeather(
                        queryLat = currentCity.lat,
                        queryLon = currentCity.lon,
                        weatherParams = weatherParams,
                        locationType = "coordinates"
                    )
                }

                currentCity != null -> {
                    fetchAndNotifyWeather(
                        queryCity = currentCity.cityName,
                        weatherParams = weatherParams,
                        locationType = "city"
                    )
                }

                else -> {
                    fetchAndNotifyWeather(
                        queryCity = applicationContext.getString(R.string.default_city_value),
                        weatherParams = weatherParams,
                        locationType = "default city"
                    )
                }
            }
            true
        }

        if (!success) {
            throw Exception("Falló después de todos los reintentos")
        }
    }

    private suspend fun fetchAndNotifyWeather(
        queryLat: Double? = null,
        queryLon: Double? = null,
        queryCity: String? = null,
        locationType: String,
        weatherParams: NotificationWeatherParams
    ) {
        val result = if (queryLat != null && queryLon != null) {
            weatherRemoteRepository.getWeatherDataForecast(
                queryLat,
                queryLon,
                weatherParams.lang,
                weatherParams.apiKey,
                weatherParams.days
            )
        } else {
            weatherRemoteRepository.getWeatherDataForecast(
                queryCity ?: "",
                weatherParams.lang,
                weatherParams.apiKey,
                weatherParams.days
            )
        }

        result
            .onSuccess { forecast ->
                weatherRemoteRepository.setLastWeatherForecast(
                    applicationContext.getString(R.string.current_weather_key),
                    forecast
                )
                val notificationType = NotificationType.from(inputData.getString(KEY_NOTIFICATION_TYPE).orEmpty()) ?: NotificationType.WEATHER_SUGGESTION_MORNING
                val timeZone = forecast.location.tzId
                val measureUnit = MeasureUnit.from(
                    preferenceRepository.getPreference(
                        applicationContext.getString(R.string.measure_unit_key),
                        applicationContext.getString(R.string.measure_unit_preference_default_value)
                    )
                )
                when (notificationType) {
                    NotificationType.WEATHER_SUGGESTION_EVENING -> handleEveningSuggestion(forecast, measureUnit)
                    NotificationType.WEATHER_SUGGESTION_MIDDAY  -> handleMiddaySuggestions(forecast, timeZone, measureUnit)
                    else -> handleMorningSuggestions(forecast, timeZone, measureUnit)
                }
                log("Weather from $locationType acquired: ${forecast.location.name}", false)
            }
            .onError { error ->
                throw Exception("Weather error from $locationType: ${error.errorMessage}")
            }
    }

    private suspend fun <T> withRetry(
        maxRetries: Int = 3,
        initialDelay: Long = 2000,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        repeat(maxRetries) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                if (attempt == maxRetries - 1) {
                    throw e
                }

                log(
                    "Intento ${attempt + 1} falló: ${e.message}. Reintentando en ${currentDelay}ms...",
                    true
                )

                if (isNetworkRelatedError(e)) {
                    delay(currentDelay)
                    currentDelay *= 2
                } else {
                    throw e
                }
            }
        }
        throw IllegalStateException("Unreachable")
    }

    private suspend fun getWeatherParams(): NotificationWeatherParams {
        return NotificationWeatherParams(
            lang = preferenceRepository.getPreference(
                applicationContext.getString(R.string.default_lang_key),
                applicationContext.getString(R.string.default_language_value)
            ),
            apiKey = applicationContext.getString(R.string.api_key),
            days = preferenceRepository.getPreference(
                applicationContext.getString(R.string.default_days_key),
                applicationContext.getString(R.string.default_days_values)
            ).toInt(),
            measureUnit = MeasureUnit.from(
                preferenceRepository.getPreference(
                    applicationContext.getString(R.string.measure_unit_key),
                    applicationContext.getString(R.string.measure_unit_preference_default_value)
                )
            )
        )
    }

    private fun hasValidatedNetworkConnection(): Boolean {
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private suspend fun handleWorkerError(e: Exception): Result {
        return when {
            isNetworkRelatedError(e) -> {
                log("Error de red/DNS en background: ${e.message}", true)
                Result.retry()
            }

            e is CancellationException -> {
                log("Worker cancelado", true)
                Result.success()
            }

            else -> {
                log("Error no manejado: ${e.message}", true)
                Result.failure()
            }
        }
    }

    private fun isNetworkRelatedError(e: Exception): Boolean {
        return e is UnknownHostException ||
                e is SocketTimeoutException ||
                e is ConnectException ||
                e.message?.contains("Unable to resolve host") == true ||
                e.message?.contains("No address associated with hostname") == true
    }

    private fun handleMorningSuggestions(
        forecast: Forecast,
        timeZone: String,
        measureUnit: MeasureUnit
    ) {
        val suggestions = forecast.mapMorningSuggestions(timeZone, measureUnit)
        if (suggestions.isEmpty()) return

        val top = suggestions.first()

        notifications.createNotificationSuggestion(
            title = "${top.icon} ${resolveTitle(top, forecast.location.name)}",

            shortDescription = resolveMessage(top),

            description = suggestions
                .joinToString(" • ") { resolveMessage(it) },

            group = NotificationGroup.SUGGESTION,
            notificationsId = NotificationType.WEATHER_SUGGESTION_MORNING
        )
    }

    private fun handleMiddaySuggestions(
        forecast: Forecast,
        timeZone: String,
        measureUnit: MeasureUnit
    ) {
        val suggestions = forecast.mapCurrentSuggestions(timeZone, measureUnit)
        if (suggestions.isEmpty()) return

        val top = suggestions.first()

        notifications.createNotificationSuggestion(
            title = "${top.icon} ${resolveTitle(top, forecast.location.name)}",

            shortDescription = resolveMessage(top),

            description = suggestions
                .joinToString(" • ") { resolveMessage(it) },

            group = NotificationGroup.SUGGESTION,
            notificationsId = NotificationType.WEATHER_SUGGESTION_MIDDAY
        )
    }

    private fun handleEveningSuggestion(
        forecast: Forecast,
        measureUnit: MeasureUnit
    ) {
        val suggestion = forecast.mapTomorrowNotification(
            forecast.location.tzId,
            measureUnit
        ) ?: return

        notifications.createNotificationSuggestion(
            title = resolveTitle(suggestion, forecast.location.name),

            shortDescription = resolveMessage(suggestion),

            description = "",

            group = NotificationGroup.SUGGESTION,
            notificationsId = NotificationType.WEATHER_SUGGESTION_EVENING
        )
    }



    private fun resolveTitle(
        suggestion: WeatherSuggestionModel,
        locationName: String
    ): String {

        val res = applicationContext.resources

        return when (suggestion.type) {

            // 🌧️ RAIN
            SuggestionType.RAIN -> when (suggestion.moment) {
                DayMoment.MORNING   -> res.getString(R.string.suggestion_rain_title_morning)
                DayMoment.AFTERNOON -> res.getString(R.string.suggestion_rain_title_afternoon)
                DayMoment.EVENING   -> res.getString(R.string.suggestion_rain_title_evening)
                DayMoment.NIGHT     -> res.getString(R.string.suggestion_rain_title_night)
            }

            // ☀️ UV
            SuggestionType.UV -> when (suggestion.priority) {
                SuggestionPriority.HIGH -> res.getString(R.string.suggestion_uv_high_title)
                else -> res.getString(R.string.suggestion_uv_medium_title)
            }

            // 🌡️ HEAT
            SuggestionType.HEAT -> when (suggestion.priority) {
                SuggestionPriority.HIGH ->
                    res.getString(R.string.suggestion_heat_high_title)

                SuggestionPriority.MEDIUM ->
                    res.getString(R.string.suggestion_heat_medium_title)

                else ->
                    res.getString(R.string.suggestion_heat_low_title)
            }

            // ❄️ COLD
            SuggestionType.COLD ->
                res.getString(R.string.suggestion_cold_title)

            // 💨 WIND
            SuggestionType.WIND ->
                res.getString(R.string.suggestion_wind_title)

            // 💧 HUMIDITY
            SuggestionType.HUMIDITY ->
                res.getString(R.string.suggestion_humidity_title)

            // 🌫️ VISIBILITY
            SuggestionType.VISIBILITY ->
                res.getString(R.string.suggestion_visibility_title)

            // 🌤️ TOMORROW
            SuggestionType.TOMORROW_FORECAST -> when (suggestion.priority) {

                SuggestionPriority.HIGH ->
                    res.getString(R.string.suggestion_tomorrow_rain_title)

                SuggestionPriority.MEDIUM ->
                    res.getString(R.string.suggestion_tomorrow_alert_title)
                        .format(locationName)

                else ->
                    res.getString(R.string.suggestion_tomorrow_clear_title)
            }

            // 🌅 MORNING SUMMARY
            SuggestionType.MORNING_SUMMARY -> when (suggestion.priority) {

                SuggestionPriority.HIGH ->
                    res.getString(R.string.suggestion_morning_alert_title)

                SuggestionPriority.MEDIUM ->
                    res.getString(R.string.suggestion_morning_warning_title)

                else ->
                    res.getString(R.string.suggestion_morning_normal_title)
            }
        }
    }

    fun resolveMessage(suggestion: WeatherSuggestionModel): String {
        val res = applicationContext.resources
        val template = when (suggestion.type) {

            SuggestionType.RAIN -> when (suggestion.moment) {
                DayMoment.MORNING   -> res.getString(R.string.suggestion_rain_morning_message)
                DayMoment.AFTERNOON -> res.getString(R.string.suggestion_rain_afternoon_message)
                DayMoment.EVENING   -> res.getString(R.string.suggestion_rain_evening_message)
                DayMoment.NIGHT     -> res.getString(R.string.suggestion_rain_night_message)
            }

            SuggestionType.UV -> when (suggestion.priority) {
                SuggestionPriority.HIGH -> when (suggestion.moment) {
                    DayMoment.MORNING   -> res.getString(R.string.suggestion_uv_high_morning_message)
                    DayMoment.AFTERNOON -> res.getString(R.string.suggestion_uv_high_afternoon_message)
                    DayMoment.EVENING   -> res.getString(R.string.suggestion_uv_high_evening_message)
                    DayMoment.NIGHT     -> res.getString(R.string.suggestion_uv_high_night_message)
                }
                else -> when (suggestion.moment) {
                    DayMoment.MORNING   -> res.getString(R.string.suggestion_uv_medium_morning_message)
                    DayMoment.AFTERNOON -> res.getString(R.string.suggestion_uv_medium_afternoon_message)
                    DayMoment.EVENING   -> res.getString(R.string.suggestion_uv_medium_evening_message)
                    DayMoment.NIGHT     -> res.getString(R.string.suggestion_uv_medium_night_message)
                }
            }

            SuggestionType.HEAT -> when (suggestion.priority) {
                SuggestionPriority.HIGH -> when (suggestion.moment) {
                    DayMoment.MORNING   -> res.getString(R.string.suggestion_heat_high_morning_message)
                    DayMoment.AFTERNOON -> res.getString(R.string.suggestion_heat_high_afternoon_message)
                    DayMoment.EVENING   -> res.getString(R.string.suggestion_heat_high_evening_message)
                    DayMoment.NIGHT     -> res.getString(R.string.suggestion_heat_high_night_message)
                }
                SuggestionPriority.MEDIUM -> when (suggestion.moment) {
                    DayMoment.MORNING   -> res.getString(R.string.suggestion_heat_medium_morning_message)
                    DayMoment.AFTERNOON -> res.getString(R.string.suggestion_heat_medium_afternoon_message)
                    DayMoment.EVENING   -> res.getString(R.string.suggestion_heat_medium_evening_message)
                    DayMoment.NIGHT     -> res.getString(R.string.suggestion_heat_medium_night_message)
                }
                else -> when (suggestion.moment) {
                    DayMoment.MORNING   -> res.getString(R.string.suggestion_heat_low_morning_message)
                    DayMoment.AFTERNOON -> res.getString(R.string.suggestion_heat_low_afternoon_message)
                    DayMoment.EVENING   -> res.getString(R.string.suggestion_heat_low_evening_message)
                    DayMoment.NIGHT     -> res.getString(R.string.suggestion_heat_low_night_message)
                }
            }

            SuggestionType.COLD -> when (suggestion.moment) {
                DayMoment.MORNING   -> res.getString(R.string.suggestion_cold_morning_message)
                DayMoment.AFTERNOON -> res.getString(R.string.suggestion_cold_afternoon_message)
                DayMoment.EVENING   -> res.getString(R.string.suggestion_cold_evening_message)
                DayMoment.NIGHT     -> res.getString(R.string.suggestion_cold_night_message)
            }

            SuggestionType.WIND -> when (suggestion.moment) {
                DayMoment.MORNING   -> res.getString(R.string.suggestion_wind_morning_message)
                DayMoment.AFTERNOON -> res.getString(R.string.suggestion_wind_afternoon_message)
                DayMoment.EVENING   -> res.getString(R.string.suggestion_wind_evening_message)
                DayMoment.NIGHT     -> res.getString(R.string.suggestion_wind_night_message)
            }

            SuggestionType.HUMIDITY -> when (suggestion.moment) {
                DayMoment.MORNING   -> res.getString(R.string.suggestion_humidity_morning_message)
                DayMoment.AFTERNOON -> res.getString(R.string.suggestion_humidity_afternoon_message)
                DayMoment.EVENING   -> res.getString(R.string.suggestion_humidity_evening_message)
                DayMoment.NIGHT     -> res.getString(R.string.suggestion_humidity_night_message)
            }

            SuggestionType.VISIBILITY -> when (suggestion.moment) {
                DayMoment.MORNING   -> res.getString(R.string.suggestion_visibility_morning_message)
                DayMoment.AFTERNOON -> res.getString(R.string.suggestion_visibility_afternoon_message)
                DayMoment.EVENING   -> res.getString(R.string.suggestion_visibility_evening_message)
                DayMoment.NIGHT     -> res.getString(R.string.suggestion_visibility_night_message)
            }

            SuggestionType.TOMORROW_FORECAST -> when (suggestion.priority) {
                SuggestionPriority.HIGH   -> res.getString(R.string.suggestion_tomorrow_rain_message)
                SuggestionPriority.MEDIUM -> when (suggestion.icon) {
                    "🧴" -> res.getString(R.string.suggestion_tomorrow_uv_message)
                    else -> res.getString(R.string.suggestion_tomorrow_heat_message)
                }
                else -> res.getString(R.string.suggestion_tomorrow_clear_message)
            }

            SuggestionType.MORNING_SUMMARY -> when (suggestion.icon) {
                "🌂" -> res.getString(R.string.suggestion_morning_rain_message)
                "🧴" -> res.getString(R.string.suggestion_morning_uv_high_message)
                "😎" -> res.getString(R.string.suggestion_morning_uv_medium_message)
                "💧" -> res.getString(R.string.suggestion_morning_heat_high_message)
                else -> res.getString(R.string.suggestion_morning_heat_medium_message)
            }
        }

        val resolvedArgs = suggestion.args.map { arg ->
            when (arg) {
                is SuggestionArg.Temperature -> "${arg.value}"
                is SuggestionArg.Percentage  -> "${arg.value}"
                is SuggestionArg.WindSpeed   -> "${arg.value}"
                is SuggestionArg.Distance    -> "${arg.value}"
                is SuggestionArg.Text        -> arg.value
                is SuggestionArg.Uv         -> uvIndexDescription(arg.level,res)
            }
        }

        return resolvedArgs.foldIndexed(template) { index, acc, arg ->
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

    fun uvIndexDescription(index: UvIndexLevel,res: Resources): String {
        return when (index) {
            UvIndexLevel.LOW -> res.getString(R.string.uv_index_low)
            UvIndexLevel.MEDIUM -> res.getString(R.string.uv_index_medium)
            UvIndexLevel.HIGH -> res.getString(R.string.uv_index_high)
            UvIndexLevel.VERY_HIGH -> res.getString(R.string.uv_index_very_high)
            else -> res.getString(R.string.uv_index_extreme)
        }
    }
}