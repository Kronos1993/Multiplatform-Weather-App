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
import com.kronos.multiplatform.weatherapp.core.result.onError
import com.kronos.multiplatform.weatherapp.core.result.onSuccess
import com.kronos.multiplatform.weatherapp.core.util.IChangeLang
import com.kronos.multiplatform.weatherapp.core.util.format
import com.kronos.multiplatform.weatherapp.core.widget.IWidgetUpdater
import com.kronos.multiplatform.weatherapp.domain.model.MeasureUnit
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast
import com.kronos.multiplatform.weatherapp.domain.repository.UserCustomLocationLocalRepository
import com.kronos.multiplatform.weatherapp.domain.repository.WeatherRemoteRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

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
    private val changeLang: IChangeLang by inject()

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
                widgetUpdater.updateAllWeatherWidgets()
                createWeatherNotification(forecast, weatherParams.measureUnit)
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

    private fun isNetworkRelatedError(e: Exception): Boolean {
        return e is UnknownHostException ||
                e is SocketTimeoutException ||
                e is ConnectException ||
                e.message?.contains("Unable to resolve host") == true ||
                e.message?.contains("No address associated with hostname") == true
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

    private fun createWeatherNotification(forecast: Forecast, measureUnit: MeasureUnit) {
        notifications.createNotification(
            title =
                if (measureUnit == MeasureUnit.INTERNATIONAL)
                    applicationContext.getString(R.string.notification_title).format(
                        forecast.current.tempC,
                        forecast.location.region.orEmpty()
                    )
                else
                    applicationContext.getString(R.string.notification_title_fahrenheit).format(
                        forecast.current.tempF,
                        forecast.location.region.orEmpty()
                    ),

            shortDescription = if (measureUnit == MeasureUnit.INTERNATIONAL)
                applicationContext.getString(R.string.notification_short_details)
                    .format(
                        forecast.current.condition.description,
                        forecast.current.feelslikeC
                    )
            else
                applicationContext.getString(R.string.notification_short_details_fahrenheit)
                    .format(
                        forecast.current.condition.description,
                        forecast.current.feelslikeF
                    ),

            description = if (measureUnit == MeasureUnit.INTERNATIONAL)
                applicationContext.getString(R.string.notification_long_details).format(
                    forecast.current.condition.description,
                    forecast.current.feelslikeC,
                    forecast.forecast.forecastDay[0].day.mintempC.toString(),
                    forecast.forecast.forecastDay[0].day.maxtempC.toString(),
                    forecast.forecast.forecastDay[0].day.dailyChanceOfRain.toString()
                )
            else
                applicationContext.getString(R.string.notification_long_details_fahrenheit)
                    .format(
                        forecast.current.condition.description,
                        forecast.current.feelslikeF,
                        forecast.forecast.forecastDay[0].day.mintempF.toString(),
                        forecast.forecast.forecastDay[0].day.maxtempF.toString(),
                        forecast.forecast.forecastDay[0].day.dailyChanceOfRain.toString()
                    ),
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
            ).toInt(),
            measureUnit = MeasureUnit.from(
                preferenceRepository.getPreference(
                    applicationContext.getString(R.string.measure_unit_key),
                    applicationContext.getString(R.string.measure_unit_preference_default_value)
                )
            )
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
