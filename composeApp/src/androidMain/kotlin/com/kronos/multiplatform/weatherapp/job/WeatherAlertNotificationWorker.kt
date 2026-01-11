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
import com.kronos.multiplatform.weatherapp.core.result.onError
import com.kronos.multiplatform.weatherapp.core.result.onSuccess
import com.kronos.multiplatform.weatherapp.domain.model.alerts.WeatherAlert
import com.kronos.multiplatform.weatherapp.domain.repository.UserCustomLocationLocalRepository
import com.kronos.multiplatform.weatherapp.domain.repository.WeatherAlertsRemoteRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class WeatherAlertNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val TAG = this::class.simpleName.orEmpty()

    private val weatherAlertsRemoteRepository: WeatherAlertsRemoteRepository by inject()
    private val userCustomLocationLocalRepository: UserCustomLocationLocalRepository by inject()
    private val notifications: INotifications by inject()
    private val loggerManager: ILogManager by inject()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            if (!hasValidatedNetworkConnection()) {
                log("No hay conexión validada, reintentando...", true)
                return@withContext Result.retry()
            }
            refreshWeatherAlerts()
            Result.success()
        } catch (e: Exception) {
            handleWorkerError(e)
        }
    }

    private suspend fun refreshWeatherAlerts() {
        val currentCity = userCustomLocationLocalRepository.getSelectedLocation()
            ?: userCustomLocationLocalRepository.getCurrentLocation()

        val weatherParams = getWeatherAlertsParams()

        val success = withRetry(maxRetries = 3) {
            when {
                currentCity != null -> {
                    fetchAndNotifyWeatherAlert(
                        queryCity = currentCity.cityName,
                        weatherParams = weatherParams,
                        locationType = "city"
                    )
                }
            }
            true
        }

        if (!success) {
            throw Exception("Falló después de todos los reintentos")
        }
    }

    private suspend fun fetchAndNotifyWeatherAlert(
        queryCity: String? = null,
        weatherParams: WeatherAlertParams,
        locationType: String
    ) {
        weatherAlertsRemoteRepository.getWeatherAlertsData(
            queryCity ?: "",
            weatherParams.apiKey,
        )
            .onSuccess { alerts ->
                createWeatherAlertNotification(alerts.alerts)
                log("Weather alert from $locationType acquired: ${alerts.location.name}", false)
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

    private fun createWeatherAlertNotification(alerts: List<WeatherAlert>) {
        val notificationTitle = if (alerts.size > 1)
            applicationContext.getString(R.string.notification_alerts_multiple_title)
                .format(alerts.size)
        else
            alerts[0].headline.orEmpty()
        val notificationShortDetails = if (alerts.size > 1)
            applicationContext.getString(R.string.notification_alerts_multiple_details)
        else
            alerts[0].instruction.orEmpty()

        val notificationLongDetails = if (alerts.size > 1)
            applicationContext.getString(R.string.notification_alerts_multiple_details)
        else
            alerts[0].description.orEmpty()

        notifications.createNotificationAlerts(
            title = notificationTitle,
            shortDescription = notificationShortDetails,
            description = notificationLongDetails,
            NotificationGroup.GENERAL,
            NotificationType.FROM_APP
        )
    }

    private data class WeatherAlertParams(
        val apiKey: String,
    )

    private fun getWeatherAlertsParams(): WeatherAlertParams {
        return WeatherAlertParams(
            apiKey = applicationContext.getString(R.string.api_key),
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
