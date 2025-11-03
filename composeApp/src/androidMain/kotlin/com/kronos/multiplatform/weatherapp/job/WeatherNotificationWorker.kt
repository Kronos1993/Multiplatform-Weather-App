package com.kronos.multiplatform.weatherapp.job

import android.content.Context
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
import com.kronos.multiplatform.weatherapp.core.widget.IWidgetUpdater
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast
import com.kronos.multiplatform.weatherapp.domain.repository.UserCustomLocationLocalRepository
import com.kronos.multiplatform.weatherapp.domain.repository.WeatherRemoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WeatherNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val TAG = this::class.simpleName.orEmpty()

    private val weatherRemoteRepository: WeatherRemoteRepository by inject()
    private val userCustomLocationLocalRepository: UserCustomLocationLocalRepository by inject()
    private val preferenceRepository: PreferenceRepository by inject()
    private val notifications: INotifications by inject()
    private val loggerManager: ILogManager by inject()
    private val widgetUpdater: IWidgetUpdater by inject()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            refreshWeather()
            Result.success()
        } catch (e: Exception) {
            log("Error ejecutando WeatherNotificationWorker: ${e.message}", true)
            Result.retry()
        }
    }

    private suspend fun refreshWeather() {
        val currentCity = userCustomLocationLocalRepository.getSelectedLocation()
            ?: userCustomLocationLocalRepository.getCurrentLocation()

        val weatherParams = getWeatherParams()

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
    }

    private suspend fun fetchAndNotifyWeather(
        queryLat: Double? = null,
        queryLon: Double? = null,
        queryCity: String? = null,
        weatherParams: WeatherParams,
        locationType: String
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
                createWeatherNotification(forecast)
                log("Weather from $locationType acquired: ${forecast.location.name}", false)
            }
            .onError { error ->
                log("Weather error from $locationType: ${error.errorMessage}", true)
            }
    }

    private suspend fun createWeatherNotification(forecast: Forecast) {
        notifications.createNotification(
            applicationContext.getString(R.string.notification_title)
                .format(forecast.current.tempC, forecast.location.region),
            applicationContext.getString(R.string.notification_short_details)
                .format(
                    forecast.current.condition.description,
                    forecast.current.feelslikeC
                ),
            applicationContext.getString(R.string.notification_long_details)
                .format(
                    forecast.current.condition.description,
                    forecast.current.feelslikeC,
                    forecast.forecast.forecastDay[0].day.mintempC,
                    forecast.forecast.forecastDay[0].day.maxtempC,
                    forecast.forecast.forecastDay[0].day.dailyChanceOfRain
                ),
            "https:${forecast.current.condition.icon}",
            NotificationGroup.GENERAL,
            NotificationType.FROM_APP
        )

        // Actualiza widgets después de la notificación
        widgetUpdater.updateAllWeatherWidgets()
    }

    private data class WeatherParams(
        val lang: String,
        val apiKey: String,
        val days: Int
    )

    private suspend fun getWeatherParams(): WeatherParams {
        return WeatherParams(
            lang = preferenceRepository.getPreference(
                applicationContext.getString(R.string.default_lang_key),
                applicationContext.getString(R.string.default_language_value)
            ),
            apiKey = applicationContext.getString(R.string.api_key),
            days = preferenceRepository.getPreference(
                applicationContext.getString(R.string.default_days_key),
                applicationContext.getString(R.string.default_days_values)
            ).toInt()
        )
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
