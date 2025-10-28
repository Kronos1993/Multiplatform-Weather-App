package com.kronos.multiplatform.weatherapp.widget


import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
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

    /**
     * Carga los datos del clima desde cache o API.
     * Luego los guarda en el estado del widget (GlanceState).
     */
    protected suspend fun loadWeatherData(context: Context): WeatherWidgetData? = withContext(Dispatchers.IO) {
        try {
            // Primero intentar cargar el clima guardado en caché local (repositorio)
            val cachedWeatherResult = loadCachedWeather()
            if (cachedWeatherResult != null) {
                val weatherParams = getWeatherParams(context)
                val data = createWeatherWidgetData(context, cachedWeatherResult, weatherParams.imageQuality)
                saveWeatherToGlance(context, data)
                return@withContext data
            }

            // Si no hay caché, obtener de la API
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
                is Result.Success -> {
                    val data = createWeatherWidgetData(context, result.data, weatherParams.imageQuality)
                    saveWeatherToGlance(context, data)
                    data
                }
                is Result.Error -> loadLastGlanceData(context) // Recupera último estado persistido
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadLastGlanceData(context) // Si hay error, muestra último estado persistido
        }
    }

    /**
     * Intenta cargar el clima desde la caché (preferencias locales del repositorio)
     */
    private suspend fun loadCachedWeather(): Forecast? {
        return try {
            val cachedResult = weatherRemoteRepository.getLastWeatherForecast("last_weather_data")
            when (cachedResult) {
                is Result.Success -> cachedResult.data
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

    // ============================================================
    // 🔹 PERSISTENCIA GLANCE
    // ============================================================

    private suspend fun saveWeatherToGlance(context: Context, data: WeatherWidgetData) {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(this::class.java)
        glanceIds.forEach { glanceId ->
            updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[stringPreferencesKey("location")] = data.location
                    this[stringPreferencesKey("time")] = data.time
                    this[stringPreferencesKey("currentTemp")] = data.currentTemp
                    this[stringPreferencesKey("currentCondition")] = data.currentCondition
                    this[stringPreferencesKey("humidity")] = data.humidity
                    this[stringPreferencesKey("windSpeed")] = data.windSpeed
                    this[stringPreferencesKey("windDirection")] = data.windDirection
                    this[stringPreferencesKey("uvIndex")] = data.uvIndex
                    this[stringPreferencesKey("currentIconUrl")] = data.currentIconUrl
                    this[stringPreferencesKey("day1Name")] = data.day1Name
                    this[stringPreferencesKey("day1IconUrl")] = data.day1IconUrl
                    this[stringPreferencesKey("day2Name")] = data.day2Name
                    this[stringPreferencesKey("day2IconUrl")] = data.day2IconUrl
                }
            }
        }
    }

    private suspend fun loadLastGlanceData(context: Context): WeatherWidgetData? {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(this::class.java)
        if (glanceIds.isEmpty()) return null
        val glanceId = glanceIds.first()

        val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId)
        return WeatherWidgetData(
            location = prefs[stringPreferencesKey("location")] ?: "",
            time = prefs[stringPreferencesKey("time")] ?: "",
            currentTemp = prefs[stringPreferencesKey("currentTemp")] ?: "",
            currentCondition = prefs[stringPreferencesKey("currentCondition")] ?: "",
            humidity = prefs[stringPreferencesKey("humidity")] ?: "",
            windSpeed = prefs[stringPreferencesKey("windSpeed")] ?: "",
            windDirection = prefs[stringPreferencesKey("windDirection")] ?: "",
            uvIndex = prefs[stringPreferencesKey("uvIndex")] ?: "",
            currentIconUrl = prefs[stringPreferencesKey("currentIconUrl")] ?: "",
            day1Name = prefs[stringPreferencesKey("day1Name")] ?: "",
            day1IconUrl = prefs[stringPreferencesKey("day1IconUrl")] ?: "",
            day2Name = prefs[stringPreferencesKey("day2Name")] ?: "",
            day2IconUrl = prefs[stringPreferencesKey("day2IconUrl")] ?: ""
        )
    }

    // ============================================================
    // 🔹 UTILIDADES DE FECHAS
    // ============================================================

    @OptIn(ExperimentalTime::class)
    private fun isToday(dateString: String): Boolean = try {
        val instant = Instant.of(dateString, includeHours = false)
        instant?.isToday() ?: false
    } catch (e: Exception) {
        false
    }

    @OptIn(ExperimentalTime::class)
    private fun formatLocalTime(localTime: String, language: String): String = try {
        val instant = Instant.of(localTime, includeHours = true, timezone = TimeZone.currentSystemDefault())
        if (instant != null) {
            formatDateTime(instant, "dd-MMM hh:mm aa", language = language)
        } else ""
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
        } else ""
    } catch (e: Exception) {
        ""
    }

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
