package com.kronos.multiplatform.weatherapp.widget


import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.RemoteViews
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.kronos.multiplatform.weatherapp.R
import com.kronos.multiplatform.weatherapp.core.logguer.ILogManager
import com.kronos.multiplatform.weatherapp.core.logguer.LogLevel
import com.kronos.multiplatform.weatherapp.core.preferences.repository.PreferenceRepository
import com.kronos.multiplatform.weatherapp.core.result.Result
import com.kronos.multiplatform.weatherapp.core.util.IChangeLang
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
import java.net.URL
import java.util.Locale
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

abstract class BaseWeatherGlanceWidget : GlanceAppWidget(), KoinComponent {

    private val weatherRemoteRepository: WeatherRemoteRepository by inject()
    private val userCustomLocationLocalRepository: UserCustomLocationLocalRepository by inject()
    private val preferenceRepository: PreferenceRepository by inject()
    private val urlProvider: UrlProvider by inject()
    private val loggerManager: ILogManager by inject()

    private val changeLang: IChangeLang by inject()

    protected abstract fun getClassName(): Class<out GlanceAppWidget>

    protected open val TAG = this::class.simpleName.orEmpty()

    /**
     * Carga los datos del clima desde caché.
     * Luego los guarda en el estado del widget (GlanceState).
     */
    protected suspend fun loadWeatherDataFromCache(context: Context): WeatherWidgetData? {
        log("Iniciando carga de datos del clima (GlanceWidget) solo de la cache")
        val currentLang = preferenceRepository.getPreference(
            context.getString(R.string.default_lang_key),
            context.getString(R.string.default_language_value)
        )
        changeLang.onLangChange(currentLang)
        var weatherData: WeatherWidgetData? = null
        try {
            val cachedWeatherResult = loadCachedWeather(context)
            if (cachedWeatherResult != null) {
                log("Clima cargado desde preferencias correctamente.")
                val weatherParams = getWeatherParams(context)
                log("Actualizando widget.")
                val data = createWeatherWidgetData(
                    context,
                    cachedWeatherResult,
                    weatherParams.imageQuality
                )
                log("Widget actualizado.")
                saveWeatherToGlance(context, data)
                log("Actualizando cache.")
                weatherData = data
            } else {
                log("Cargando datos de la cache de glance")
                weatherData = loadLastGlanceData(context)
            }
        } catch (e: Exception) {
            log("Ha ocurrido un error cargando los datos del clima: ${e.message}", isError = true)
        }
        return weatherData
    }

    /**
     * Carga los datos del clima desde caché o API.
     * Luego los guarda en el estado del widget (GlanceState).
     */
    protected suspend fun loadWeatherData(context: Context): WeatherWidgetData? {
        log("Iniciando carga de datos del clima (GlanceWidget)")
        var weatherData: WeatherWidgetData? = null
        try {
            // Primero intentar cargar el clima guardado en caché local (repositorio)
            log("Intentando cargar datos desde caché local...")
            val cachedWeatherResult = loadCachedWeather(context)
            if (cachedWeatherResult != null) {
                log("Clima cargado desde caché correctamente.")
                val weatherParams = getWeatherParams(context)
                val data = createWeatherWidgetData(
                    context,
                    cachedWeatherResult,
                    weatherParams.imageQuality
                )
                saveWeatherToGlance(context, data)
                weatherData = data
            } else {
                // Si no hay caché, obtener de la API
                log("No se encontró caché. Solicitando datos desde API...")
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
                        log("No hay ubicación seleccionada ni actual. Usando ciudad por defecto.")
                        weatherRemoteRepository.getWeatherDataForecast(
                            weatherParams.defaultCity,
                            weatherParams.lang,
                            weatherParams.apiKey,
                            weatherParams.days
                        )
                    }
                }

                when (result) {
                    is Result.Success -> {
                        log("Datos del clima obtenidos exitosamente de la API para ${result.data.location.name}.")
                        val data = createWeatherWidgetData(
                            context,
                            result.data,
                            weatherParams.imageQuality
                        )
                        weatherRemoteRepository.setLastWeatherForecast(
                            context.getString(R.string.current_weather_key),
                            result.data
                        )
                        saveWeatherToGlance(context, data)
                        log("Datos guardados en Glance correctamente.")
                        weatherData = data
                    }

                    is Result.Error -> {
                        log(
                            "Error obteniendo datos del clima desde API: ${result.error.errorMessage}",
                            isError = true
                        )
                        weatherData = loadLastGlanceData(context)
                    }
                }
            }

        } catch (e: Exception) {
            log("Excepción al cargar datos del clima: ${e.message}", isError = true)
            e.printStackTrace()
            weatherData = loadLastGlanceData(context)
        }

        return weatherData
    }

    /**
     * Intenta cargar el clima desde la caché (preferencias locales del repositorio)
     */
    private suspend fun loadCachedWeather(context: Context): Forecast? {
        return try {
            val cachedResult =
                weatherRemoteRepository.getLastWeatherForecast(context.getString(R.string.current_weather_key))
            when (cachedResult) {
                is Result.Success -> cachedResult.data
                is Result.Error -> {
                    log(
                        "No se pudo cargar caché: ${cachedResult.error.errorMessage}",
                        isError = true
                    )
                    null
                }
            }
        } catch (e: Exception) {
            log("Error al intentar cargar caché: ${e.message}", isError = true)
            null
        }
    }

    private suspend fun getWeatherParams(context: Context): WeatherParams {
        return WeatherParams(
            defaultCity = preferenceRepository.getPreference(
                context.getString(R.string.default_city_key),
                context.getString(R.string.default_city_value)
            ),
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

        val day1 =
            if (futureDays.isNotEmpty()) getDayName(context, futureDays[0], currentLanguage) else ""
        val day2 =
            if (futureDays.size > 1) getDayName(context, futureDays[1], currentLanguage) else ""

        val currentIconUrl =
            urlProvider.getImageUrl(forecast.current.condition.icon, imageQuality)
        val currentBitmap = loadBitmapSafely(currentIconUrl)

        val day1IconUrl =
            if (futureDays.isNotEmpty())
                urlProvider.getImageUrl(futureDays[0].day.condition.icon, imageQuality)
            else
                ""
        val day1Bitmap = loadBitmapSafely(day1IconUrl)

        val day2IconUrl =
            if (futureDays.size > 1)
                urlProvider.getImageUrl(futureDays[1].day.condition.icon, imageQuality)
            else ""
        val day2Bitmap = loadBitmapSafely(day2IconUrl)

        val weatherWidgetData = WeatherWidgetData(
            location = forecast.location.name,
            time = formatLocalTime(forecast.location.localtime, currentLanguage),
            currentTemp = context.getString(R.string.temp_celsius_widget)
                .format(forecast.current.tempC),
            currentCondition = forecast.current.condition.description,
            humidity = forecast.current.humidity.toString(),
            windSpeed = context.getString(R.string.speed_km).format(forecast.current.windSpeedKph),
            windDirection = forecast.current.windDir,
            uvIndex = forecast.current.uv.toString(),
            currentIconUrl = currentIconUrl,
            day1Name = day1,
            day1IconUrl = day1IconUrl,
            day2Name = day2,
            day2IconUrl = day2IconUrl,
            currentIconBitmap = currentBitmap,
            day1IconBitmap = day1Bitmap,
            day2IconBitmap = day2Bitmap
        )

        log(
            """
                Widget Data:
                - Location: ${weatherWidgetData.location}
                - Time: ${weatherWidgetData.time}
                - Current Temp: ${weatherWidgetData.currentTemp}
                - Condition: ${weatherWidgetData.currentCondition}
                - Humidity: ${weatherWidgetData.humidity}%
                - Wind: ${weatherWidgetData.windSpeed} ${weatherWidgetData.windDirection}
                - UV Index: ${weatherWidgetData.uvIndex}
                - Current Icon: ${weatherWidgetData.currentIconUrl}
                - Day1: ${weatherWidgetData.day1Name} 
                - Day1 Icon: ${weatherWidgetData.day1IconUrl}
                - Day2: ${weatherWidgetData.day2Name} 
                - Day2 Icon: ${weatherWidgetData.day2IconUrl}
            """.trimIndent()
        )

        return weatherWidgetData
    }

    private suspend fun loadBitmapSafely(url: String?): Bitmap? = withContext(Dispatchers.IO) {
        if (url.isNullOrBlank()) return@withContext null
        try {
            val connection = URL(url).openConnection()
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            BitmapFactory.decodeStream(connection.getInputStream())
        } catch (e: Exception) {
            Log.e(TAG, "Error al descargar imagen $url: ${e.message}")
            log("Error al descargar imagen $url: ${e.message}   ", isError = true)
            null
        }
    }


    override fun onCompositionError(
        context: Context,
        glanceId: GlanceId,
        appWidgetId: Int,
        throwable: Throwable
    ) {
        val rv = RemoteViews(context.packageName, R.layout.glance_widget_error)
        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, rv)
    }
    // ============================================================
    //  PERSISTENCIA GLANCE
    // ============================================================

    private suspend fun saveWeatherToGlance(context: Context, data: WeatherWidgetData) {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(getClassName())
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
        updateAll(context)
    }

    private suspend fun loadLastGlanceData(context: Context): WeatherWidgetData {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(getClassName())
        if (glanceIds.isEmpty()) {
            return WeatherWidgetData(
                location = "",
                time = "",
                currentTemp = "--°",
                currentCondition = context.getString(R.string.widget_error_text),
                humidity = "--",
                windSpeed = "--",
                windDirection = "--",
                uvIndex = "--",
                currentIconUrl = "",
                day1Name = "",
                day1IconUrl = "",
                day2Name = "",
                day2IconUrl = ""
            )
        }
        val glanceId = glanceIds.first()

        val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId)

        val currentIconUrl = prefs[stringPreferencesKey("currentIconUrl")] ?: ""
        val day1IconUrl = prefs[stringPreferencesKey("day1IconUrl")] ?: ""
        val day2IconUrl = prefs[stringPreferencesKey("day2IconUrl")] ?: ""

        val currentBitmap = loadBitmapSafely(currentIconUrl)
        val day1Bitmap = loadBitmapSafely(day1IconUrl)
        val day2Bitmap = loadBitmapSafely(day2IconUrl)

        return WeatherWidgetData(
            location = prefs[stringPreferencesKey("location")] ?: "",
            time = prefs[stringPreferencesKey("time")] ?: "",
            currentTemp = prefs[stringPreferencesKey("currentTemp")] ?: "",
            currentCondition = prefs[stringPreferencesKey("currentCondition")] ?: "",
            humidity = prefs[stringPreferencesKey("humidity")] ?: "",
            windSpeed = prefs[stringPreferencesKey("windSpeed")] ?: "",
            windDirection = prefs[stringPreferencesKey("windDirection")] ?: "",
            uvIndex = prefs[stringPreferencesKey("uvIndex")] ?: "",
            currentIconUrl = currentIconUrl,
            day1Name = prefs[stringPreferencesKey("day1Name")] ?: "",
            day1IconUrl = day1IconUrl,
            day2Name = prefs[stringPreferencesKey("day2Name")] ?: "",
            day2IconUrl = day2IconUrl,
            currentIconBitmap = currentBitmap,
            day1IconBitmap = day1Bitmap,
            day2IconBitmap = day2Bitmap
        )
    }

    // ============================================================
    //  UTILIDADES DE FECHAS
    // ============================================================

    @OptIn(ExperimentalTime::class)
    private fun isToday(dateString: String): Boolean = try {
        val instant = Instant.of(dateString, includeHours = false)
        instant?.isToday() ?: false
    } catch (_: Exception) {
        false
    }

    @OptIn(ExperimentalTime::class)
    private fun formatLocalTime(localTime: String, language: String): String = try {
        val instant =
            Instant.of(localTime, includeHours = true, timezone = TimeZone.currentSystemDefault())
        if (instant != null) {
            formatDateTime(instant, "dd-MMM hh:mm aa", language = language)
        } else ""
    } catch (_: Exception) {
        ""
    }

    @OptIn(ExperimentalTime::class)
    private fun getDayName(
        context: Context,
        dailyForecast: DailyForecast,
        language: String
    ): String = try {
        val dateString = dailyForecast.date
        val instant = Instant.of(dateString, includeHours = false)

        if (instant != null) {
            when {
                instant.isToday() -> context.getString(R.string.today)
                instant.isTomorrow() -> context.getString(R.string.tomorrow)
                else -> {
                    when (instant.toDayOfWeekText()) {
                        DayOfWeek.MONDAY -> context.getString(R.string.monday).capitalize(language)
                        DayOfWeek.TUESDAY -> context.getString(R.string.tuesday)
                            .capitalize(language)

                        DayOfWeek.WEDNESDAY -> context.getString(R.string.wednesday)
                            .capitalize(language)

                        DayOfWeek.THURSDAY -> context.getString(R.string.thursday)
                            .capitalize(language)

                        DayOfWeek.FRIDAY -> context.getString(R.string.friday).capitalize(language)
                        DayOfWeek.SATURDAY -> context.getString(R.string.saturday)
                            .capitalize(language)

                        DayOfWeek.SUNDAY -> context.getString(R.string.sunday).capitalize(language)
                    }
                }
            }
        } else ""
    } catch (_: Exception) {
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

    // ============================================================
    //  LOGGING
    // ============================================================

    protected suspend fun log(message: String, isError: Boolean = false) {
        if (isError) {
            Log.e(TAG, message)
            loggerManager.log(LogLevel.ERROR, TAG, message)
        } else {
            Log.i(TAG, message)
            loggerManager.log(LogLevel.INFO, TAG, message)
        }
    }
}