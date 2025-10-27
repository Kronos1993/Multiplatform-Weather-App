package com.kronos.multiplatform.weatherapp.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import com.kronos.multiplatform.weatherapp.R
import com.kronos.multiplatform.weatherapp.core.preferences.repository.PreferenceRepository
import com.kronos.multiplatform.weatherapp.core.result.Result
import com.kronos.multiplatform.weatherapp.core.util.formatDateTime
import com.kronos.multiplatform.weatherapp.core.util.isToday
import com.kronos.multiplatform.weatherapp.core.util.isTomorrow
import com.kronos.multiplatform.weatherapp.core.util.of
import com.kronos.multiplatform.weatherapp.core.util.toDayOfWeekText
import com.kronos.multiplatform.weatherapp.data.remote.ktor.UrlProvider
import com.kronos.multiplatform.weatherapp.domain.model.DailyForecast
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast
import com.kronos.multiplatform.weatherapp.domain.repository.UserCustomLocationLocalRepository
import com.kronos.multiplatform.weatherapp.domain.repository.WeatherRemoteRepository
import com.kronos.multiplatform.weatherapp.widget.model.WeatherParams
import com.kronos.multiplatform.weatherapp.widget.model.WeatherWidgetData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Locale
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

abstract class BaseWeatherGlanceWidget : GlanceAppWidget(), KoinComponent {

    private val weatherRemoteRepository: WeatherRemoteRepository by inject()
    private val userCustomLocationLocalRepository: UserCustomLocationLocalRepository by inject()
    private val preferenceRepository: PreferenceRepository by inject()
    private val urlProvider: UrlProvider by inject()

    protected suspend fun loadWeatherData(context: Context): WeatherWidgetData? = withContext(Dispatchers.IO) {
        try {
            // Primero intentar cargar el clima guardado
            val cachedWeatherResult = loadCachedWeather()
            if (cachedWeatherResult != null) {
                val weatherParams = getWeatherParams(context)
                return@withContext createWeatherWidgetData(context, cachedWeatherResult, weatherParams.imageQuality)
            }

            // Si no hay caché, cargar de la API como antes
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

    /**
     * Intenta cargar el clima desde la caché (preferencias)
     */
    private suspend fun loadCachedWeather(): Forecast? {
        return try {
            val cachedResult = weatherRemoteRepository.getLastWeatherForecast("last_weather_data")
            when (cachedResult) {
                is Result.Success -> {
                    cachedResult.data
                }
                is Result.Error -> null
            }
        } catch (e: Exception) {
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
        val currentLanguage = preferenceRepository.getPreference(
            context.getString(R.string.default_lang_key),
            context.getString(R.string.default_language_value)
        )

        val futureDays = forecast.forecast.forecastDay.filterNot { isToday(it.date) }.take(2)

        val day1 = if (futureDays.isNotEmpty()) getDayName(context, futureDays[0], currentLanguage) else ""
        val day2 = if (futureDays.size > 1) getDayName(context, futureDays[1], currentLanguage) else ""

        return WeatherWidgetData(
            location = forecast.location.name,
            time = formatLocalTime(forecast.location.localtime, currentLanguage),
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

    @OptIn(ExperimentalTime::class)
    private fun isToday(dateString: String): Boolean = try {
        val instant = Instant.of(dateString, includeHours = false)
        instant?.isToday() ?: false
    } catch (e: Exception) {
        false
    }

    @OptIn(ExperimentalTime::class)
    private fun formatLocalTime(localTime: String, language: String): String = try {
        // Parsear el string "yyyy-MM-dd HH:mm" a Instant
        val instant = Instant.of(localTime, includeHours = true, timezone = TimeZone.currentSystemDefault())
        if (instant != null) {
            // Usar el formato "dd-MMM hh:mm aa" con el idioma correspondiente
            formatDateTime(instant, "dd-MMM hh:mm aa", language = language)
        } else {
            ""
        }
    } catch (e: Exception) {
        ""
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun getDayName(context: Context, dailyForecast: DailyForecast, language: String): String = try {
        val dateString = dailyForecast.date
        val instant = Instant.of(dateString, includeHours = false)

        if (instant != null) {
            when {
                instant.isToday() -> context.getString(R.string.today)
                instant.isTomorrow() -> context.getString(R.string.tomorrow)
                else -> {
                    when (instant.toDayOfWeekText()) {
                        DayOfWeek.MONDAY -> context.getString(R.string.monday).capitalize(language)
                        DayOfWeek.TUESDAY -> context.getString(R.string.tuesday).capitalize(language)
                        DayOfWeek.WEDNESDAY -> context.getString(R.string.wednesday).capitalize(language)
                        DayOfWeek.THURSDAY -> context.getString(R.string.thursday).capitalize(language)
                        DayOfWeek.FRIDAY -> context.getString(R.string.friday).capitalize(language)
                        DayOfWeek.SATURDAY -> context.getString(R.string.saturday).capitalize(language)
                        DayOfWeek.SUNDAY -> context.getString(R.string.sunday).capitalize(language)
                        else -> ""
                    }
                }
            }
        } else {
            ""
        }
    } catch (e: Exception) {
        ""
    }

    // Función auxiliar para capitalizar según el idioma
    private fun String.capitalize(language: String): String {
        return when (language) {
            "es" -> this.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString()
            }
            else -> this.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString()
            }
        }
    }
}