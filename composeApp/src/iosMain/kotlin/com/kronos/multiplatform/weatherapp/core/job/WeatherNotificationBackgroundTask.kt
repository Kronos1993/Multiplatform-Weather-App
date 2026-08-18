package com.kronos.multiplatform.weatherapp.core.job

import com.kronos.multiplatform.weatherapp.core.logguer.ILogManager
import com.kronos.multiplatform.weatherapp.core.logguer.LogLevel
import com.kronos.multiplatform.weatherapp.core.notification.INotifications
import com.kronos.multiplatform.weatherapp.core.notification.NotificationGroup
import com.kronos.multiplatform.weatherapp.core.notification.NotificationType
import com.kronos.multiplatform.weatherapp.core.preferences.repository.PreferenceRepository
import com.kronos.multiplatform.weatherapp.core.result.onError
import com.kronos.multiplatform.weatherapp.core.result.onSuccess
import com.kronos.multiplatform.weatherapp.core.util.format
import com.kronos.multiplatform.weatherapp.core.widget.IWidgetUpdater
import com.kronos.multiplatform.weatherapp.domain.model.MeasureUnit
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast
import com.kronos.multiplatform.weatherapp.domain.repository.UserCustomLocationLocalRepository
import com.kronos.multiplatform.weatherapp.domain.repository.WeatherRemoteRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WeatherNotificationBackgroundTask : KoinComponent {

    private val weatherRemoteRepository: WeatherRemoteRepository by inject()
    private val userCustomLocationLocalRepository: UserCustomLocationLocalRepository by inject()
    private val preferenceRepository: PreferenceRepository by inject()
    private val notifications: INotifications by inject()
    private val loggerManager: ILogManager by inject()
    private val widgetUpdater: IWidgetUpdater by inject()

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
        return WeatherParams(
            lang = preferenceRepository.getPreference("default_lang_key", "en"),
            apiKey = preferenceRepository.getPreference("api_key", ""),
            days = preferenceRepository.getPreference("default_days_key", "1").toInt(),
            measureUnit = MeasureUnit.from(
                preferenceRepository.getPreference("measure_unit_key", "INTERNATIONAL")
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
