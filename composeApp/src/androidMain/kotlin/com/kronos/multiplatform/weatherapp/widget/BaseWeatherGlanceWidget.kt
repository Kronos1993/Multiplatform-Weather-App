package com.kronos.multiplatform.weatherapp.widget

import android.content.Context
import android.icu.util.Calendar
import androidx.glance.appwidget.GlanceAppWidget
import com.kronos.multiplatform.weatherapp.R
import com.kronos.multiplatform.weatherapp.core.preferences.repository.PreferenceRepository
import com.kronos.multiplatform.weatherapp.core.result.Result
import com.kronos.multiplatform.weatherapp.data.remote.ktor.UrlProvider
import com.kronos.multiplatform.weatherapp.domain.model.DailyForecast
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast
import com.kronos.multiplatform.weatherapp.domain.repository.UserCustomLocationLocalRepository
import com.kronos.multiplatform.weatherapp.domain.repository.WeatherRemoteRepository
import com.kronos.multiplatform.weatherapp.widget.model.WeatherParams
import com.kronos.multiplatform.weatherapp.widget.model.WeatherWidgetData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.Locale

abstract class BaseWeatherGlanceWidget : GlanceAppWidget(), KoinComponent {

    private val weatherRemoteRepository: WeatherRemoteRepository by inject()
    private val userCustomLocationLocalRepository: UserCustomLocationLocalRepository by inject()
    private val preferenceRepository: PreferenceRepository by inject()
    private val urlProvider: UrlProvider by inject()

    protected suspend fun loadWeatherData(context: Context): WeatherWidgetData? = withContext(Dispatchers.IO) {
        try {
            val currentCity = userCustomLocationLocalRepository.getSelectedLocation()
                ?: userCustomLocationLocalRepository.getCurrentLocation()

            val weatherParams = getWeatherParams(context)

            val result = when {
                currentCity != null && currentCity.lat != null && currentCity.lon != null -> {
                    weatherRemoteRepository.getWeatherDataForecast(
                        currentCity.lat!!,
                        currentCity.lon!!,
                        weatherParams.lang,
                        weatherParams.apiKey,
                        weatherParams.days
                    )
                }
                currentCity != null -> {
                    weatherRemoteRepository.getWeatherDataForecast(
                        currentCity.cityName,
                        weatherParams.lang,
                        weatherParams.apiKey,
                        weatherParams.days
                    )
                }
                else -> {
                    weatherRemoteRepository.getWeatherDataForecast(
                        preferenceRepository.getPreference(
                            context.getString(R.string.default_city_key),
                            context.getString(R.string.default_city_value)
                        ),
                        weatherParams.lang,
                        weatherParams.apiKey,
                        weatherParams.days
                    )
                }
            }

            when (result) {
                is Result.Success -> createWeatherWidgetData(context, result.data, weatherParams.imageQuality)
                is Result.Error -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun getWeatherParams(context: Context): WeatherParams {
        return WeatherParams(
            lang = preferenceRepository.getPreference(
                context.getString(R.string.default_lang_key),
                context.getString(R.string.default_language_value)
            ),
            apiKey = context.getString(R.string.api_key),
            days = preferenceRepository.getPreference(
                context.getString(R.string.default_days_key),
                context.getString(R.string.default_days_values)
            ).toInt(),
            imageQuality = preferenceRepository.getPreference(
                context.getString(R.string.default_image_quality_key),
                context.getString(R.string.default_image_quality_value)
            )
        )
    }

    private suspend fun createWeatherWidgetData(
        context: Context,
        forecast: Forecast,
        imageQuality: String
    ): WeatherWidgetData {
        val futureDays = forecast.forecast.forecastDay.filterNot { isToday(it.date) }.take(2)

        val day1 = if (futureDays.isNotEmpty()) getDayName(context, futureDays[0]) else ""
        val day2 = if (futureDays.size > 1) getDayName(context, futureDays[1]) else ""

        return WeatherWidgetData(
            location = forecast.location.name,
            time = formatLocalTime(forecast.location.localtime),
            currentTemp = context.getString(R.string.temp_celsius_widget).format(forecast.current.tempC),
            currentCondition = forecast.current.condition.description,
            humidity = forecast.current.humidity.toString(),
            windSpeed = context.getString(R.string.speed_km).format(forecast.current.windSpeedKph),
            windDirection = forecast.current.windDir,
            uvIndex = forecast.current.uv.toString(),
            currentIconUrl = urlProvider.getImageUrl(forecast.current.condition.icon, imageQuality),
            day1Name = day1,
            day1IconUrl = if (futureDays.isNotEmpty()) urlProvider.getImageUrl(futureDays[0].day.condition.icon, imageQuality) else "",
            day2Name = day2,
            day2IconUrl = if (futureDays.size > 1) urlProvider.getImageUrl(futureDays[1].day.condition.icon, imageQuality) else ""
        )
    }

    private fun isToday(dateString: String): Boolean = try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        val today = Calendar.getInstance()
        val targetDate = Calendar.getInstance().apply { time = date!! }
        today.get(Calendar.YEAR) == targetDate.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == targetDate.get(Calendar.DAY_OF_YEAR)
    } catch (e: Exception) {
        false
    }

    private fun formatLocalTime(localTime: String): String = try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("hh:mm aa dd-MMM", Locale.getDefault())
        val date = inputFormat.parse(localTime)
        outputFormat.format(date!!)
    } catch (e: Exception) {
        ""
    }

    private suspend fun getDayName(context: Context, dailyForecast: DailyForecast): String = try {
        val dateString = dailyForecast.date
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(dateString) ?: return ""
        val calendar = Calendar.getInstance().apply { time = date }
        val today = Calendar.getInstance()

        when {
            isSameDay(calendar, today) -> context.getString(R.string.today)
            isTomorrow(calendar, today) -> context.getString(R.string.tomorrow)
            else -> {
                val locale = if (
                    preferenceRepository.getPreference(
                        context.getString(R.string.default_lang_key),
                        context.getString(R.string.default_language_value)
                    ) == "en"
                ) Locale.US else Locale.getDefault()
                SimpleDateFormat("EEEE", locale).format(date).capitalize()
            }
        }
    } catch (e: Exception) {
        ""
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean =
        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)

    private fun isTomorrow(target: Calendar, today: Calendar): Boolean {
        val tomorrow = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
        return isSameDay(target, tomorrow)
    }
}

private fun String.capitalize(): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }