package com.kronos.multiplatform.weatherapp.core.job

import com.kronos.multiplatform.weatherapp.core.logguer.ILogManager
import com.kronos.multiplatform.weatherapp.core.logguer.LogLevel
import com.kronos.multiplatform.weatherapp.core.notification.INotifications
import com.kronos.multiplatform.weatherapp.core.notification.NotificationGroup
import com.kronos.multiplatform.weatherapp.core.notification.NotificationType
import com.kronos.multiplatform.weatherapp.core.preferences.repository.PreferenceRepository
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
import com.kronos.multiplatform.weatherapp.core.result.onError
import com.kronos.multiplatform.weatherapp.core.result.onSuccess
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WeatherSuggestionBackgroundTask : KoinComponent {

    private val weatherRemoteRepository: WeatherRemoteRepository by inject()
    private val userCustomLocationLocalRepository: UserCustomLocationLocalRepository by inject()
    private val preferenceRepository: PreferenceRepository by inject()
    private val notifications: INotifications by inject()
    private val loggerManager: ILogManager by inject()

    companion object {
        const val TASK_MORNING = "com.kronos.weatherapp.suggestion_morning"
        const val TASK_MIDDAY  = "com.kronos.weatherapp.suggestion_midday"
        const val TASK_EVENING = "com.kronos.weatherapp.suggestion_evening"
    }

    // ── Strings localizados — se setean desde Swift antes de ejecutar ──────
    private var strings: SuggestionStrings = SuggestionStrings()

    fun initStrings(strings: SuggestionStrings) {
        this.strings = strings
    }

    // ── Callback para iOS — se invoca cuando el forecast está listo ────────
    // Swift lo usa para programar UNCalendarNotificationTrigger
    var onForecastReady: ((Forecast, MeasureUnit) -> Unit)? = null

    // ── Entry points por tipo de tarea ─────────────────────────────────────
    suspend fun runMorning() = runSuggestion(NotificationType.WEATHER_SUGGESTION_MORNING)
    suspend fun runMidday()  = runSuggestion(NotificationType.WEATHER_SUGGESTION_MIDDAY)
    suspend fun runEvening() = runSuggestion(NotificationType.WEATHER_SUGGESTION_EVENING)

    // ── Entry point para iOS — fetch + callback, sin enviar notificación ───
    // Swift recibe el forecast y programa las notificaciones directamente
    suspend fun fetchAndNotify() {
        try {
            val currentCity = userCustomLocationLocalRepository.getSelectedLocation()
                ?: userCustomLocationLocalRepository.getCurrentLocation()

            val lang        = preferenceRepository.getPreference("default_lang_key", "en")
            val apiKey      = preferenceRepository.getPreference("api_key", "")
            val days        = preferenceRepository.getPreference("default_days_key", "3").toInt()
            val measureUnit = MeasureUnit.from(
                preferenceRepository.getPreference("measure_unit_key", "INTERNATIONAL")
            )

            val result = if (currentCity?.lat != null && currentCity.lon != null) {
                weatherRemoteRepository.getWeatherDataForecast(
                    currentCity.lat ?: 0.0,
                    currentCity.lon ?: 0.0,
                    lang, apiKey, days
                )
            } else {
                weatherRemoteRepository.getWeatherDataForecast(
                    currentCity?.cityName ?: "Panama",
                    lang, apiKey, days
                )
            }

            result
                .onSuccess { forecast ->
                    weatherRemoteRepository.setLastWeatherForecast("current_weather", forecast)
                    // Delegar a Swift para que programe con UNCalendarNotificationTrigger
                    onForecastReady?.invoke(forecast, measureUnit)
                    log("fetchAndNotify success for ${forecast.location.name}", false)
                }
                .onError {
                    log("fetchAndNotify error: ${it.errorMessage}", true)
                }
        } catch (e: Exception) {
            log("fetchAndNotify exception: ${e.message}", true)
        }
    }

    private suspend fun runSuggestion(type: NotificationType) {
        try {
            val currentCity = userCustomLocationLocalRepository.getSelectedLocation()
                ?: userCustomLocationLocalRepository.getCurrentLocation()

            val lang        = preferenceRepository.getPreference("default_lang_key", "en")
            val apiKey      = preferenceRepository.getPreference("api_key", "")
            val days        = preferenceRepository.getPreference("default_days_key", "3").toInt()
            val measureUnit = MeasureUnit.from(
                preferenceRepository.getPreference("measure_unit_key", "INTERNATIONAL")
            )

            val result = if (currentCity?.lat != null && currentCity.lon != null) {
                weatherRemoteRepository.getWeatherDataForecast(
                    currentCity.lat ?: 0.0,
                    currentCity.lon ?: 0.0,
                    lang, apiKey, days
                )
            } else {
                weatherRemoteRepository.getWeatherDataForecast(
                    currentCity?.cityName ?: "Panama",
                    lang, apiKey, days
                )
            }

            result
                .onSuccess { forecast ->
                    weatherRemoteRepository.setLastWeatherForecast("current_weather", forecast)
                    val timeZone = forecast.location.tzId
                    when (type) {
                        NotificationType.WEATHER_SUGGESTION_MORNING ->
                            handleMorning(forecast, timeZone, measureUnit)
                        NotificationType.WEATHER_SUGGESTION_MIDDAY ->
                            handleMidday(forecast, timeZone, measureUnit)
                        NotificationType.WEATHER_SUGGESTION_EVENING ->
                            handleEvening(forecast, measureUnit)
                        else -> Unit
                    }
                    log("Suggestion $type sent for ${forecast.location.name}", false)
                }
                .onError {
                    log("Error fetching forecast for suggestion: ${it.errorMessage}", true)
                }
        } catch (e: Exception) {
            log("Exception in suggestion task: ${e.message}", true)
        }
    }

    // ── Handlers ───────────────────────────────────────────────────────────

    private fun handleMorning(forecast: Forecast, timeZone: String, measureUnit: MeasureUnit) {
        val suggestions = forecast.mapMorningSuggestions(timeZone, measureUnit)
        if (suggestions.isEmpty()) return
        val top = suggestions.first()
        notifications.createNotificationSuggestion(
            title = resolveTitle(top, forecast.location.name),
            shortDescription = resolveMessage(top),
            description = suggestions.joinToString(" • ") { resolveMessage(it) },
            group = NotificationGroup.SUGGESTION,
            notificationsId = NotificationType.WEATHER_SUGGESTION_MORNING
        )
    }

    private fun handleMidday(forecast: Forecast, timeZone: String, measureUnit: MeasureUnit) {
        val suggestions = forecast.mapCurrentSuggestions(timeZone, measureUnit)
        if (suggestions.isEmpty()) return
        val top = suggestions.take(2)
        notifications.createNotificationSuggestion(
            title = resolveTitle(top.first(), forecast.location.name),
            shortDescription = resolveMessage(top.first()),
            description = top.joinToString(" • ") { resolveMessage(it) },
            group = NotificationGroup.SUGGESTION,
            notificationsId = NotificationType.WEATHER_SUGGESTION_MIDDAY
        )
    }

    private fun handleEvening(forecast: Forecast, measureUnit: MeasureUnit) {
        val suggestion = forecast.mapTomorrowNotification(
            forecast.location.tzId,
            measureUnit
        ) ?: return
        notifications.createNotificationSuggestion(
            title = resolveTitle(suggestion, forecast.location.name),
            shortDescription = resolveMessage(suggestion),
            description = resolveMessage(suggestion),
            group = NotificationGroup.SUGGESTION,
            notificationsId = NotificationType.WEATHER_SUGGESTION_EVENING
        )
    }

    // ── String resolvers ───────────────────────────────────────────────────

    private fun resolveTitle(suggestion: WeatherSuggestionModel, locationName: String): String =
        when (suggestion.type) {
            SuggestionType.RAIN -> when (suggestion.moment) {
                DayMoment.MORNING   -> strings.rainTitleMorning
                DayMoment.AFTERNOON -> strings.rainTitleAfternoon
                DayMoment.EVENING   -> strings.rainTitleEvening
                DayMoment.NIGHT     -> strings.rainTitleNight
            }
            SuggestionType.UV -> when (suggestion.priority) {
                SuggestionPriority.HIGH -> strings.uvHighTitle
                else                    -> strings.uvMediumTitle
            }
            SuggestionType.HEAT -> when (suggestion.priority) {
                SuggestionPriority.HIGH   -> strings.heatHighTitle
                SuggestionPriority.MEDIUM -> strings.heatMediumTitle
                else                      -> strings.heatLowTitle
            }
            SuggestionType.COLD       -> strings.coldTitle
            SuggestionType.WIND       -> strings.windTitle
            SuggestionType.HUMIDITY   -> strings.humidityTitle
            SuggestionType.VISIBILITY -> strings.visibilityTitle
            SuggestionType.TOMORROW_FORECAST -> when (suggestion.priority) {
                SuggestionPriority.HIGH   -> strings.tomorrowRainTitle
                SuggestionPriority.MEDIUM -> when (suggestion.icon) {
                    "🧴" -> strings.tomorrowUvTitle
                    else -> strings.tomorrowHeatTitle
                }
                else -> strings.tomorrowClearTitle
            }
            SuggestionType.MORNING_SUMMARY -> when (suggestion.icon) {
                "🌂" -> strings.morningRainTitle
                "🧴" -> strings.morningUvHighTitle
                "😎" -> strings.morningUvMediumTitle
                "💧" -> strings.morningHeatHighTitle
                else -> strings.morningHeatMediumTitle
            }
        }

    private fun resolveMessage(suggestion: WeatherSuggestionModel): String {
        val template = when (suggestion.type) {
            SuggestionType.RAIN -> when (suggestion.moment) {
                DayMoment.MORNING   -> strings.rainMorningMessage
                DayMoment.AFTERNOON -> strings.rainAfternoonMessage
                DayMoment.EVENING   -> strings.rainEveningMessage
                DayMoment.NIGHT     -> strings.rainNightMessage
            }
            SuggestionType.UV -> when (suggestion.priority) {
                SuggestionPriority.HIGH -> when (suggestion.moment) {
                    DayMoment.MORNING   -> strings.uvHighMorningMessage
                    DayMoment.AFTERNOON -> strings.uvHighAfternoonMessage
                    DayMoment.EVENING   -> strings.uvHighEveningMessage
                    DayMoment.NIGHT     -> strings.uvHighNightMessage
                }
                else -> when (suggestion.moment) {
                    DayMoment.MORNING   -> strings.uvMediumMorningMessage
                    DayMoment.AFTERNOON -> strings.uvMediumAfternoonMessage
                    DayMoment.EVENING   -> strings.uvMediumEveningMessage
                    DayMoment.NIGHT     -> strings.uvMediumNightMessage
                }
            }
            SuggestionType.HEAT -> when (suggestion.priority) {
                SuggestionPriority.HIGH -> when (suggestion.moment) {
                    DayMoment.MORNING   -> strings.heatHighMorningMessage
                    DayMoment.AFTERNOON -> strings.heatHighAfternoonMessage
                    DayMoment.EVENING   -> strings.heatHighEveningMessage
                    DayMoment.NIGHT     -> strings.heatHighNightMessage
                }
                SuggestionPriority.MEDIUM -> when (suggestion.moment) {
                    DayMoment.MORNING   -> strings.heatMediumMorningMessage
                    DayMoment.AFTERNOON -> strings.heatMediumAfternoonMessage
                    DayMoment.EVENING   -> strings.heatMediumEveningMessage
                    DayMoment.NIGHT     -> strings.heatMediumNightMessage
                }
                else -> when (suggestion.moment) {
                    DayMoment.MORNING   -> strings.heatLowMorningMessage
                    DayMoment.AFTERNOON -> strings.heatLowAfternoonMessage
                    DayMoment.EVENING   -> strings.heatLowEveningMessage
                    DayMoment.NIGHT     -> strings.heatLowNightMessage
                }
            }
            SuggestionType.COLD -> when (suggestion.moment) {
                DayMoment.MORNING   -> strings.coldMorningMessage
                DayMoment.AFTERNOON -> strings.coldAfternoonMessage
                DayMoment.EVENING   -> strings.coldEveningMessage
                DayMoment.NIGHT     -> strings.coldNightMessage
            }
            SuggestionType.WIND -> when (suggestion.moment) {
                DayMoment.MORNING   -> strings.windMorningMessage
                DayMoment.AFTERNOON -> strings.windAfternoonMessage
                DayMoment.EVENING   -> strings.windEveningMessage
                DayMoment.NIGHT     -> strings.windNightMessage
            }
            SuggestionType.HUMIDITY -> when (suggestion.moment) {
                DayMoment.MORNING   -> strings.humidityMorningMessage
                DayMoment.AFTERNOON -> strings.humidityAfternoonMessage
                DayMoment.EVENING   -> strings.humidityEveningMessage
                DayMoment.NIGHT     -> strings.humidityNightMessage
            }
            SuggestionType.VISIBILITY -> when (suggestion.moment) {
                DayMoment.MORNING   -> strings.visibilityMorningMessage
                DayMoment.AFTERNOON -> strings.visibilityAfternoonMessage
                DayMoment.EVENING   -> strings.visibilityEveningMessage
                DayMoment.NIGHT     -> strings.visibilityNightMessage
            }
            SuggestionType.TOMORROW_FORECAST -> when (suggestion.priority) {
                SuggestionPriority.HIGH   -> strings.tomorrowRainMessage
                SuggestionPriority.MEDIUM -> when (suggestion.icon) {
                    "🧴" -> strings.tomorrowUvMessage
                    else -> strings.tomorrowHeatMessage
                }
                else -> strings.tomorrowClearMessage
            }
            SuggestionType.MORNING_SUMMARY -> when (suggestion.icon) {
                "🌂" -> strings.morningRainMessage
                "🧴" -> strings.morningUvHighMessage
                "😎" -> strings.morningUvMediumMessage
                "💧" -> strings.morningHeatHighMessage
                else -> strings.morningHeatMediumMessage
            }
        }

        val resolvedArgs = suggestion.args.map { arg ->
            when (arg) {
                is SuggestionArg.Temperature -> "${arg.value}"
                is SuggestionArg.Percentage  -> "${arg.value}"
                is SuggestionArg.WindSpeed   -> "${arg.value}"
                is SuggestionArg.Distance    -> "${arg.value}"
                is SuggestionArg.Text        -> arg.value
                is SuggestionArg.Uv         -> uvLevelString(arg.level)
            }
        }

        return resolvedArgs.foldIndexed(template) { index, acc, arg ->
            acc.replace("%${index + 1}\$s", arg)
        }
    }

    private fun uvLevelString(level: UvIndexLevel): String = when (level) {
        UvIndexLevel.LOW       -> strings.uvLow
        UvIndexLevel.MEDIUM    -> strings.uvMedium
        UvIndexLevel.HIGH      -> strings.uvHigh
        UvIndexLevel.VERY_HIGH -> strings.uvVeryHigh
        UvIndexLevel.EXTREME   -> strings.uvExtreme
    }

    private fun log(msg: String, isError: Boolean) {
        runBlocking {
            loggerManager.log(
                if (isError) LogLevel.ERROR else LogLevel.INFO,
                "WeatherSuggestionBackgroundTask",
                msg
            )
        }
    }
}

// ── Data class — iOS no la usa, Android la llena desde sus strings.xml ────────
data class SuggestionStrings(
    // Rain titles por momento
    val rainTitleMorning: String = "",
    val rainTitleAfternoon: String = "",
    val rainTitleEvening: String = "",
    val rainTitleNight: String = "",

    // UV titles
    val uvHighTitle: String = "",
    val uvMediumTitle: String = "",

    // Heat titles
    val heatHighTitle: String = "",
    val heatMediumTitle: String = "",
    val heatLowTitle: String = "",

    // Otros titles
    val coldTitle: String = "",
    val windTitle: String = "",
    val humidityTitle: String = "",
    val visibilityTitle: String = "",

    // Tomorrow titles
    val tomorrowRainTitle: String = "",
    val tomorrowHeatTitle: String = "",
    val tomorrowUvTitle: String = "",
    val tomorrowClearTitle: String = "",

    // Morning summary titles
    val morningRainTitle: String = "",
    val morningUvHighTitle: String = "",
    val morningUvMediumTitle: String = "",
    val morningHeatHighTitle: String = "",
    val morningHeatMediumTitle: String = "",

    // UV levels
    val uvLow: String = "",
    val uvMedium: String = "",
    val uvHigh: String = "",
    val uvVeryHigh: String = "",
    val uvExtreme: String = "",

    // Rain messages
    val rainMorningMessage: String = "",
    val rainAfternoonMessage: String = "",
    val rainEveningMessage: String = "",
    val rainNightMessage: String = "",

    // UV High messages
    val uvHighMorningMessage: String = "",
    val uvHighAfternoonMessage: String = "",
    val uvHighEveningMessage: String = "",
    val uvHighNightMessage: String = "",

    // UV Medium messages
    val uvMediumMorningMessage: String = "",
    val uvMediumAfternoonMessage: String = "",
    val uvMediumEveningMessage: String = "",
    val uvMediumNightMessage: String = "",

    // Heat High messages
    val heatHighMorningMessage: String = "",
    val heatHighAfternoonMessage: String = "",
    val heatHighEveningMessage: String = "",
    val heatHighNightMessage: String = "",

    // Heat Medium messages
    val heatMediumMorningMessage: String = "",
    val heatMediumAfternoonMessage: String = "",
    val heatMediumEveningMessage: String = "",
    val heatMediumNightMessage: String = "",

    // Heat Low messages
    val heatLowMorningMessage: String = "",
    val heatLowAfternoonMessage: String = "",
    val heatLowEveningMessage: String = "",
    val heatLowNightMessage: String = "",

    // Cold messages
    val coldMorningMessage: String = "",
    val coldAfternoonMessage: String = "",
    val coldEveningMessage: String = "",
    val coldNightMessage: String = "",

    // Wind messages
    val windMorningMessage: String = "",
    val windAfternoonMessage: String = "",
    val windEveningMessage: String = "",
    val windNightMessage: String = "",

    // Humidity messages
    val humidityMorningMessage: String = "",
    val humidityAfternoonMessage: String = "",
    val humidityEveningMessage: String = "",
    val humidityNightMessage: String = "",

    // Visibility messages
    val visibilityMorningMessage: String = "",
    val visibilityAfternoonMessage: String = "",
    val visibilityEveningMessage: String = "",
    val visibilityNightMessage: String = "",

    // Tomorrow messages
    val tomorrowRainMessage: String = "",
    val tomorrowHeatMessage: String = "",
    val tomorrowUvMessage: String = "",
    val tomorrowClearMessage: String = "",

    // Morning summary messages
    val morningRainMessage: String = "",
    val morningUvHighMessage: String = "",
    val morningUvMediumMessage: String = "",
    val morningHeatHighMessage: String = "",
    val morningHeatMediumMessage: String = ""
)