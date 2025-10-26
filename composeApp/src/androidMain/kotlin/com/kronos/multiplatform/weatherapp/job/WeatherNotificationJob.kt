package com.kronos.multiplatform.weatherapp.job

import android.app.job.JobParameters
import android.app.job.JobService
import com.kronos.multiplatform.weatherapp.R
import com.kronos.multiplatform.weatherapp.core.notification.INotifications
import com.kronos.multiplatform.weatherapp.core.notification.NotificationGroup
import com.kronos.multiplatform.weatherapp.core.notification.NotificationType
import com.kronos.multiplatform.weatherapp.core.preferences.repository.PreferenceRepository
import com.kronos.multiplatform.weatherapp.core.result.onError
import com.kronos.multiplatform.weatherapp.core.result.onSuccess
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast
import com.kronos.multiplatform.weatherapp.domain.repository.UserCustomLocationLocalRepository
import com.kronos.multiplatform.weatherapp.domain.repository.WeatherRemoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

const val notificationJobId = 1

class WeatherNotificationJob : JobService() {

    private var jobCancelled = false
    private val weatherRemoteRepository: WeatherRemoteRepository by inject()
    private val userCustomLocationLocalRepository: UserCustomLocationLocalRepository by inject()
    private val preferenceRepository: PreferenceRepository by inject()
    private val notifications: INotifications by inject()
    private val jobScope = CoroutineScope(Dispatchers.IO + Job())

    override fun onStartJob(params: JobParameters?): Boolean {
        if (jobCancelled) {
            return false
        }

        jobScope.launch {
            doWork(params)
        }
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    private suspend fun doWork(params: JobParameters?) {
        if (params?.jobId == notificationJobId) {
            refreshWeather(params)
        }
    }

    private fun refreshWeather(params: JobParameters) {
        if (jobCancelled) return

        jobScope.launch(Dispatchers.IO) {
            val currentCity = userCustomLocationLocalRepository.getSelectedLocation()
                ?: userCustomLocationLocalRepository.getCurrentLocation()

            val weatherParams = getWeatherParams()

            when {
                currentCity != null && currentCity.lat != null && currentCity.lon != null -> {
                    // Usar coordenadas
                    fetchAndNotifyWeather(
                        queryLat = currentCity.lat ?: 0.0,
                        queryLon = currentCity.lon ?: 0.0,
                        weatherParams = weatherParams,
                        locationType = "coordinates"
                    )
                }

                currentCity != null -> {
                    // Usar nombre de ciudad
                    fetchAndNotifyWeather(
                        queryCity = currentCity.cityName,
                        weatherParams = weatherParams,
                        locationType = "city"
                    )
                }

                else -> {
                    // Usar ciudad por defecto
                    fetchAndNotifyWeather(
                        queryCity = preferenceRepository.getPreference(
                            application.getString(R.string.default_city_key),
                            applicationContext.getString(R.string.default_city_value)
                        ),
                        weatherParams = weatherParams,
                        locationType = "default city"
                    )
                }
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
                createWeatherNotification(forecast)
                log("Weather from $locationType acquired: ${forecast.location.name}", false)
            }
            .onError { error ->
                log("Weather error from $locationType: ${error.errorMessage}", isError = true)
            }
    }

    private fun createWeatherNotification(forecast: Forecast) {
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
                    forecast.forecast.forecastDay[0].day.mintempC.toString(),
                    forecast.forecast.forecastDay[0].day.maxtempC.toString(),
                    forecast.forecast.forecastDay[0].day.dailyChanceOfRain.toString()
                ),
            "https:${forecast.current.condition.icon}",
            NotificationGroup.GENERAL,
            NotificationType.FROM_APP
        )
        //updateWidget(applicationContext, WeatherWidgetProvider::class.java)
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

    private fun log(item: String, isError: Boolean = false) {
        if (isError) {
            println("ERROR: $item")
        } else {
            println("INFO: $item")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        jobScope.cancel()
    }
}