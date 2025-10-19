package com.kronos.multiplatform.weatherapp.job

import android.app.job.JobParameters
import android.app.job.JobService
import com.kronos.multiplatform.weatherapp.R
import com.kronos.multiplatform.weatherapp.core.notification.INotifications
import com.kronos.multiplatform.weatherapp.core.preferences.repository.PreferenceRepository
import com.kronos.multiplatform.weatherapp.core.result.Error
import com.kronos.multiplatform.weatherapp.core.result.Result
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
            /*setLanguageForApp(
                baseContext,
                PreferencesUtil.getPreference(
                    applicationContext,
                    applicationContext.getString(R.string.default_lang_key),
                    applicationContext.getString(R.string.default_language_value)
                )!!
            )*/

            var currentCity = userCustomLocationLocalRepository.getSelectedLocation()
            if (currentCity == null)
                currentCity = userCustomLocationLocalRepository.getCurrentLocation()

            val response: Result<Forecast, Error>
            if (currentCity != null) {
                if (currentCity.isCurrent) {
                    response = weatherRemoteRepository.getWeatherDataForecast(
                        currentCity.lat!!,
                        currentCity.lon!!,
                        preferenceRepository.getPreference(
                            applicationContext.getString(R.string.default_lang_key),
                            applicationContext.getString(R.string.default_language_value)
                        )!!,
                        applicationContext.resources.getString(R.string.api_key),
                        preferenceRepository.getPreference(
                            application.getString(R.string.default_days_key),
                            applicationContext.resources.getString(R.string.default_days_values)
                        )!!.toInt()
                    )
                } else {
                    response = weatherRemoteRepository.getWeatherDataForecast(
                        currentCity.cityName,
                        preferenceRepository.getPreference(
                            applicationContext.getString(R.string.default_lang_key),
                            applicationContext.getString(R.string.default_language_value)
                        )!!,
                        applicationContext.resources.getString(R.string.api_key),
                        preferenceRepository.getPreference(
                            application.getString(R.string.default_days_key),
                            applicationContext.resources.getString(R.string.default_days_values)
                        )!!.toInt()
                    )
                }
            } else {
                response = weatherRemoteRepository.getWeatherDataForecast(
                    preferenceRepository.getPreference(
                        application.getString(R.string.default_city_key),
                        applicationContext.getString(R.string.default_city_value)
                    )!!,
                    preferenceRepository.getPreference(
                        applicationContext.getString(R.string.default_lang_key),
                        applicationContext.getString(R.string.default_language_value)
                    )!!,
                    applicationContext.resources.getString(R.string.api_key),
                    preferenceRepository.getPreference(
                        application.getString(R.string.default_days_key),
                        applicationContext.resources.getString(R.string.default_days_values)
                    )!!.toInt()
                )
            }
            /*if (response.data != null) {
                notifications.createNotification(
                    applicationContext.getString(R.string.notification_title)
                        .format(response.data!!.current.tempC, response.data!!.location.region),
                    applicationContext.getString(R.string.notification_short_details)
                        .format(
                            response.data!!.current.condition.description,
                            response.data!!.current.feelslikeC
                        ),
                    applicationContext.getString(R.string.notification_long_details)
                        .format(
                            response.data!!.current.condition.description,
                            response.data!!.current.feelslikeC,
                            response.data!!.forecast.forecastDay[0].day.mintempC.toString(),
                            response.data!!.forecast.forecastDay[0].day.maxtempC.toString(),
                            response.data!!.forecast.forecastDay[0].day.dailyChanceOfRain.toString()
                        ),
                    NotificationGroup.GENERAL.name,
                    NotificationType.WEATHER_STATUS,
                    R.drawable.ic_weather_app_icon,
                    applicationContext,
                    BitmapFactory.decodeStream(
                        URL("https:${response.data!!.current.condition.icon}").openConnection()
                            .getInputStream()
                    )
                )
            }
            updateWidget(applicationContext, WeatherWidgetProvider::class.java)*/
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        jobScope.cancel()
    }
}






