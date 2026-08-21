package com.kronos.multiplatform.weatherapp.core.job

import com.kronos.multiplatform.weatherapp.core.logguer.ILogManager
import com.kronos.multiplatform.weatherapp.core.logguer.LogLevel
import com.kronos.multiplatform.weatherapp.core.notification.INotifications
import com.kronos.multiplatform.weatherapp.core.notification.NotificationGroup
import com.kronos.multiplatform.weatherapp.core.notification.NotificationType
import com.kronos.multiplatform.weatherapp.core.preferences.repository.PreferenceRepository
import com.kronos.multiplatform.weatherapp.core.result.onError
import com.kronos.multiplatform.weatherapp.core.result.onSuccess
import com.kronos.multiplatform.weatherapp.core.util.IChangeLang
import com.kronos.multiplatform.weatherapp.core.util.format
import com.kronos.multiplatform.weatherapp.core.widget.IWidgetUpdater
import com.kronos.multiplatform.weatherapp.domain.model.MeasureUnit
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast
import com.kronos.multiplatform.weatherapp.domain.repository.UserCustomLocationLocalRepository
import com.kronos.multiplatform.weatherapp.domain.repository.WeatherRemoteRepository
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import weather_app.composeapp.generated.resources.Res
import weather_app.composeapp.generated.resources.api_key
import weather_app.composeapp.generated.resources.day_preference_default_value
import weather_app.composeapp.generated.resources.default_days_key
import weather_app.composeapp.generated.resources.default_lang_key
import weather_app.composeapp.generated.resources.default_language_value
import weather_app.composeapp.generated.resources.measure_unit_key
import weather_app.composeapp.generated.resources.measure_unit_preference_default_value

class WeatherNotificationBackgroundTask : KoinComponent {

    private val weatherRemoteRepository: WeatherRemoteRepository by inject()
    private val userCustomLocationLocalRepository: UserCustomLocationLocalRepository by inject()
    private val preferenceRepository: PreferenceRepository by inject()
    private val notifications: INotifications by inject()
    private val loggerManager: ILogManager by inject()
    private val widgetUpdater: IWidgetUpdater by inject()
    private val changeLang: IChangeLang by inject()

    private var notificationTitle: String = ""
    private var notificationShortDetails: String = ""
    private var notificationLongDetails: String = ""
    private var notificationTitleFahrenheit: String = ""
    private var notificationShortDetailsFahrenheit: String = ""
    private var notificationLongDetailsFahrenheit: String = ""

    fun initNotificationStrings(
        title: String,
        shortDetails: String,
        longDetails: String,
        titleFahrenheit: String,
        shortDetailsFahrenheit: String,
        longDetailsFahrenheit: String
    ) {
        this.notificationTitle = title
        this.notificationShortDetails = shortDetails
        this.notificationLongDetails = longDetails
        this.notificationTitleFahrenheit = titleFahrenheit
        this.notificationShortDetailsFahrenheit = shortDetailsFahrenheit
        this.notificationLongDetailsFahrenheit = longDetailsFahrenheit
    }

    // Set by Swift (WeatherNotificationAppDelegate) to reschedule suggestion
    // notifications with fresh content whenever the hourly refresh succeeds,
    // reusing this task's forecast fetch instead of registering separate
    // BGAppRefreshTasks for each suggestion slot.
    var onForecastReady: ((Forecast, MeasureUnit) -> Unit)? = null

    suspend fun refreshWeather() {
        try {
            val currentCity = userCustomLocationLocalRepository.getSelectedLocation()
                ?: userCustomLocationLocalRepository.getCurrentLocation()

            val weatherParams = getWeatherParams()

            val forecast = if (currentCity?.lat != null && currentCity.lon != null) {
                weatherRemoteRepository.getWeatherDataForecast(
                    currentCity.lat ?: 0.0,
                    currentCity.lon ?: 0.0,
                    weatherParams.lang,
                    weatherParams.apiKey,
                    weatherParams.days
                )
            } else if (!currentCity?.cityName.isNullOrEmpty()) {
                weatherRemoteRepository.getWeatherDataForecast(
                    currentCity.cityName,
                    weatherParams.lang,
                    weatherParams.apiKey,
                    weatherParams.days
                )
            } else {
                weatherRemoteRepository.getWeatherDataForecast(
                    "Panama",
                    weatherParams.lang,
                    weatherParams.apiKey,
                    weatherParams.days
                )
            }

            forecast
                .onSuccess {
                    createWeatherNotification(it, weatherParams.measureUnit)
                    weatherRemoteRepository.setLastWeatherForecast("current_weather",it)
                    widgetUpdater.updateAllWeatherWidgets()
                    onForecastReady?.invoke(it, weatherParams.measureUnit)
                    loggerManager.log(
                        LogLevel.INFO,
                        "WeatherNotificationBackgroundTask",
                        "Clima actualizado en background"
                    )
                }
                .onError {
                    loggerManager.log(
                        LogLevel.ERROR,
                        "WeatherNotificationBackgroundTask",
                        "Error getting forecast: ${it.errorMessage}"
                    )
                }

        } catch (e: Exception) {
            loggerManager.log(
                LogLevel.ERROR,
                "WeatherNotificationBackgroundTask",
                "Error: ${e.message}"
            )
            println("❌ Error actualizando clima: ${e.message}")
        }
    }

    private suspend fun getWeatherParams(): WeatherParams {
        // Bug found while chasing "notification text shows unresolved keys":
        // these three lookups used the literal Kotlin resource NAMES
        // ("default_lang_key", "default_days_key", "measure_unit_key") as the
        // DataStore lookup key, but the app actually stores preferences under
        // the resource's VALUE ("default_lang", "default_days", "measure_unit"
        // — see composeResources/values/preference_key.xml). The lookup key
        // never matched what SettingScreen.kt writes, so every background
        // refresh silently used the hardcoded fallback regardless of the
        // user's saved settings — worse for measureUnit, whose old fallback
        // string "INTERNATIONAL" doesn't match MeasureUnit.from() at all
        // (only "1" parses as INTERNATIONAL; anything else, including that
        // literal fallback, silently resolved to IMPERIAL). Android's
        // WeatherNotificationWorker.doWork() already does this correctly via
        // context.getString(R.string.default_lang_key) — this mirrors that
        // using the non-composable Compose Resources accessor, and also
        // reapplies the resolved language via changeLang.onLangChange(lang)
        // on every refresh (same as the Android worker), instead of relying
        // solely on the app's AppleLanguages default set once at launch.
        val lang = preferenceRepository.getPreference(
            getString(Res.string.default_lang_key),
            getString(Res.string.default_language_value)
        )
        changeLang.onLangChange(lang)

        return WeatherParams(
            lang = lang,
            apiKey = getString(Res.string.api_key),
            days = preferenceRepository.getPreference(
                getString(Res.string.default_days_key),
                getString(Res.string.day_preference_default_value)
            ).toInt(),
            measureUnit = MeasureUnit.from(
                preferenceRepository.getPreference(
                    getString(Res.string.measure_unit_key),
                    getString(Res.string.measure_unit_preference_default_value)
                )
            )
        )
    }

    private fun createWeatherNotification(forecast: Forecast, measureUnit: MeasureUnit) {
        val title = if (measureUnit == MeasureUnit.INTERNATIONAL)
            notificationTitle.format(forecast.current.tempC, forecast.location.region.orEmpty())
        else
            notificationTitleFahrenheit.format(forecast.current.tempF, forecast.location.region.orEmpty())

        val shortDetails = if (measureUnit == MeasureUnit.INTERNATIONAL)
            notificationShortDetails.format(
                forecast.current.condition.description,
                forecast.current.feelslikeC
            )
        else
            notificationShortDetailsFahrenheit.format(
                forecast.current.condition.description,
                forecast.current.feelslikeF
            )

        val longDetails = if (measureUnit == MeasureUnit.INTERNATIONAL)
            notificationLongDetails.format(
                forecast.current.condition.description,
                forecast.current.feelslikeC,
                forecast.forecast.forecastDay[0].day.mintempC,
                forecast.forecast.forecastDay[0].day.maxtempC,
                forecast.forecast.forecastDay[0].day.dailyChanceOfRain
            )
        else
            notificationLongDetailsFahrenheit.format(
                forecast.current.condition.description,
                forecast.current.feelslikeF,
                forecast.forecast.forecastDay[0].day.mintempF,
                forecast.forecast.forecastDay[0].day.maxtempF,
                forecast.forecast.forecastDay[0].day.dailyChanceOfRain
            )

        notifications.createNotification(
            title = title,
            shortDescription = shortDetails,
            description = longDetails,
            notificationImageUrl = "https:${forecast.current.condition.icon}",
            group = NotificationGroup.GENERAL,
            notificationsId = NotificationType.WEATHER_UPDATED
        )
    }

    private data class WeatherParams(
        val lang: String,
        val apiKey: String,
        val days: Int,
        val measureUnit: MeasureUnit
    )
}
