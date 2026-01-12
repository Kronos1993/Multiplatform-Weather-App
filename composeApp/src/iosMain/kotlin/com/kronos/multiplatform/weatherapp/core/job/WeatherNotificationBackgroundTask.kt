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

    private val taskId = "com.kronos.weatherapp.refresh_weather_notification"

    private var currentWeatherKey: String = "Current Weather"
    private var notificationTitle: String = "Weather: %.1f°C in %s"
    private var notificationShortDetails: String = "%s, feels like %.1f°C"
    private var notificationLongDetails: String =
        "%s, feels like %.1f°C. Min %.1f°C / Max %.1f°C, Rain %d%"

    fun initNotificationStrings() {
        this.currentWeatherKey = "7e068f83abce49b58b2142037230910"
        this.notificationTitle = "%s° | %s"
        this.notificationShortDetails = "%s. Feels like %s°C"
        this.notificationLongDetails = "%s\nFeels like %s°C\nTemp min %s°C - max %s°C\nRain possibility %s%"
    }

/*    @OptIn(ExperimentalForeignApi::class)
    fun schedule() {
        val request = BGAppRefreshTaskRequest(identifier = taskId).apply {
            earliestBeginDate = NSDate().dateByAddingTimeInterval(60.0 * 60) // 1 hora
        }

        val success = BGTaskScheduler.sharedScheduler.submitTaskRequest(request, null)
        if (!success) {
            println("⚠️ No se pudo agendar la tarea en background.")
        } else {
            println("✅ Tarea programada exitosamente para dentro de 1h.")
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun handleAppRefresh(task: BGAppRefreshTask) {
        val operationQueue = NSOperationQueue()
        val operation = NSBlockOperation.run{
            kotlinx.coroutines.GlobalScope.launch {
                refreshWeather()
                task.setTaskCompletedWithSuccess(true)
                schedule()
            }
        }

        task.expirationHandler = {
            println("⏰ Task expiró antes de completarse.")
            operation.cancel()
        }

        operationQueue.addOperation(operation as NSOperation)
    }*/

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
                    createWeatherNotification(it)
                    weatherRemoteRepository.setLastWeatherForecast("current_weather",it)
                    widgetUpdater.updateAllWeatherWidgets()
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
            days = preferenceRepository.getPreference("default_days_key", "1").toInt()
        )
    }

    private fun createWeatherNotification(forecast: Forecast) {
        val title =
            notificationTitle.format(forecast.current.tempC, forecast.location.region.toString())
        val shortDetails = notificationShortDetails.format(
            forecast.current.condition.description,
            forecast.current.feelslikeC
        )
        val longDetails = notificationLongDetails.format(
            forecast.current.condition.description,
            forecast.current.feelslikeC,
            forecast.forecast.forecastDay[0].day.mintempC,
            forecast.forecast.forecastDay[0].day.maxtempC,
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
        val days: Int
    )
}
