package com.kronos.multiplatform.weatherapp.widget


import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.RemoteViews
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
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
import com.kronos.multiplatform.weatherapp.domain.model.MeasureUnit
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast
import com.kronos.multiplatform.weatherapp.domain.repository.UserCustomLocationLocalRepository
import com.kronos.multiplatform.weatherapp.domain.repository.WeatherRemoteRepository
import com.kronos.multiplatform.weatherapp.widget.model.WeatherParams
import com.kronos.multiplatform.weatherapp.widget.model.WeatherWidgetData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
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
    protected var measureUnit: MeasureUnit = MeasureUnit.INTERNATIONAL

    protected abstract fun getClassName(): Class<out GlanceAppWidget>
    protected open val TAG = this::class.simpleName.orEmpty()

    // ============================================================
    //  CARGA DE DATOS
    // ============================================================

    protected suspend fun loadWeatherDataFromCache(context: Context): WeatherWidgetData? {
        log("Iniciando carga de datos del clima (GlanceWidget) solo de la cache")
        val currentLang = preferenceRepository.getPreference(
            context.getString(R.string.default_lang_key),
            context.getString(R.string.default_language_value)
        )
        changeLang.onLangChange(currentLang)

        measureUnit = MeasureUnit.from(
            preferenceRepository.getPreference(
                context.getString(R.string.measure_unit_key),
                context.getString(R.string.measure_unit_preference_default_value)
            )
        )

        return try {
            val cachedWeatherResult = loadCachedWeather(context)
            if (cachedWeatherResult != null) {
                log("Clima cargado desde preferencias correctamente.")
                val weatherParams = getWeatherParams(context)
                val data = createWeatherWidgetData(
                    context,
                    cachedWeatherResult,
                    weatherParams.imageQuality
                )
                saveWeatherToGlance(context, data)
                data
            } else {
                // MEJORA: Siempre intenta cargar del estado Glance antes de devolver null
                log("Sin cache de repositorio, cargando estado Glance previo")
                loadLastGlanceData(context)
            }
        } catch (e: Exception) {
            log("Ha ocurrido un error cargando los datos del clima: ${e.message}", isError = true)
            // MEJORA: En caso de excepción, intentar recuperar datos previos de Glance
            runCatching { loadLastGlanceData(context) }.getOrNull()
        }
    }

    protected suspend fun loadWeatherData(context: Context): WeatherWidgetData? {
        log("Iniciando carga de datos del clima (GlanceWidget)")
        return try {
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
                data
            } else {
                log("No se encontró caché. Solicitando datos desde API...")
                fetchFromApiAndSave(context)
            }
        } catch (e: Exception) {
            log("Excepción al cargar datos del clima: ${e.message}", isError = true)
            e.printStackTrace()
            runCatching { loadLastGlanceData(context) }.getOrNull()
        }
    }

    private suspend fun fetchFromApiAndSave(context: Context): WeatherWidgetData? {
        val currentCity = userCustomLocationLocalRepository.getSelectedLocation()
            ?: userCustomLocationLocalRepository.getCurrentLocation()
        val weatherParams = getWeatherParams(context)

        val result = when {
            currentCity != null && currentCity.lat != null && currentCity.lon != null ->
                weatherRemoteRepository.getWeatherDataForecast(
                    currentCity.lat!!,
                    currentCity.lon!!,
                    weatherParams.lang,
                    weatherParams.apiKey,
                    weatherParams.days
                )

            currentCity != null ->
                weatherRemoteRepository.getWeatherDataForecast(
                    currentCity.cityName,
                    weatherParams.lang,
                    weatherParams.apiKey,
                    weatherParams.days
                )

            else -> {
                log("No hay ubicación. Usando ciudad por defecto.")
                weatherRemoteRepository.getWeatherDataForecast(
                    weatherParams.defaultCity,
                    weatherParams.lang,
                    weatherParams.apiKey,
                    weatherParams.days
                )
            }
        }

        return when (result) {
            is Result.Success -> {
                log("Datos obtenidos de API para ${result.data.location.name}.")
                val data = createWeatherWidgetData(context, result.data, weatherParams.imageQuality)
                weatherRemoteRepository.setLastWeatherForecast(
                    context.getString(R.string.current_weather_key),
                    result.data
                )
                saveWeatherToGlance(context, data)
                data
            }

            is Result.Error -> {
                log("Error API: ${result.error.errorMessage}", isError = true)
                runCatching { loadLastGlanceData(context) }.getOrNull()
            }
        }
    }

    private suspend fun loadCachedWeather(context: Context): Forecast? {
        return try {
            when (val r = weatherRemoteRepository.getLastWeatherForecast(
                context.getString(R.string.current_weather_key)
            )) {
                is Result.Success -> r.data
                is Result.Error -> {
                    log("No se pudo cargar caché: ${r.error.errorMessage}", isError = true)
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

    // ============================================================
    //  CREACIÓN DE DATOS DEL WIDGET
    // ============================================================

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

        val currentIconUrl = urlProvider.getImageUrl(forecast.current.condition.icon, imageQuality)
        val day1IconUrl = if (futureDays.isNotEmpty())
            urlProvider.getImageUrl(futureDays[0].day.condition.icon, imageQuality) else ""
        val day2IconUrl = if (futureDays.size > 1)
            urlProvider.getImageUrl(futureDays[1].day.condition.icon, imageQuality) else ""

        // MEJORA: Carga de bitmaps en paralelo con async para reducir tiempo total
        val bitmaps = coroutineScope {
            val current = async { loadBitmapWithRetry(currentIconUrl) }
            val d1 = async { loadBitmapWithRetry(day1IconUrl) }
            val d2 = async { loadBitmapWithRetry(day2IconUrl) }
            Triple(current.await(), d1.await(), d2.await())
        }

        return WeatherWidgetData(
            location = forecast.location.name,
            time = formatLocalTime(forecast.location.localtime, currentLanguage),
            currentTemp = if (measureUnit == MeasureUnit.INTERNATIONAL)
                context.getString(R.string.temp_celsius_widget).format(forecast.current.tempC)
            else
                context.getString(R.string.temp_fahrenheit_widget).format(forecast.current.tempF),
            currentCondition = forecast.current.condition.description,
            tomorrowCondition = if (forecast.forecast.forecastDay.size > 2)
                forecast.forecast.forecastDay[1].day.condition.description else "",
            humidity = forecast.current.humidity.toString(),
            windSpeed = if (measureUnit == MeasureUnit.INTERNATIONAL)
                context.getString(R.string.speed_km).format(forecast.current.windSpeedKph)
            else
                context.getString(R.string.speed_miles).format(forecast.current.windSpeedMph),
            windDirection = forecast.current.windDir,
            uvIndex = forecast.current.uv,
            currentIconUrl = currentIconUrl,
            day1Name = day1,
            day1IconUrl = day1IconUrl,
            day2Name = day2,
            day2IconUrl = day2IconUrl,
            currentIconBitmap = bitmaps.first,
            day1IconBitmap = bitmaps.second,
            day2IconBitmap = bitmaps.third
        )
    }

    // ============================================================
    //  CARGA DE IMÁGENES — MEJORA PRINCIPAL
    // ============================================================

    /**
     * MEJORA: Añade reintentos (hasta 3) con backoff y dispatcher IO explícito.
     * El problema original era que:
     * 1. No había reintentos si la conexión fallaba puntualmente.
     * 2. No se garantizaba siempre el dispatcher IO.
     * 3. Un timeout de 5s por imagen en serie podía sumar 15s+ de bloqueo.
     * Ahora se llama en paralelo (ver createWeatherWidgetData) y reintenta.
     */
    private suspend fun loadBitmapWithRetry(
        url: String?,
        maxRetries: Int = 3,
        delayMs: Long = 500L
    ): Bitmap? = withContext(Dispatchers.IO) {
        if (url.isNullOrBlank()) return@withContext null

        // Asegura que la URL tenga esquema https
        val safeUrl = when {
            url.startsWith("//") -> "https:$url"
            url.startsWith("http://") -> url.replace("http://", "https://")
            else -> url
        }

        repeat(maxRetries) { attempt ->
            try {
                val connection = URL(safeUrl).openConnection() as java.net.HttpURLConnection
                connection.apply {
                    connectTimeout = 6000
                    readTimeout = 6000
                    // MEJORA: Cabeceras para evitar bloqueos por user-agent vacío
                    setRequestProperty("User-Agent", "WeatherWidget/1.0")
                    setRequestProperty("Accept", "image/*")
                    instanceFollowRedirects = true
                }
                val bitmap = BitmapFactory.decodeStream(connection.inputStream)
                if (bitmap != null) return@withContext bitmap
                // Si bitmap es null pero no hubo excepción, reintentamos
            } catch (e: Exception) {
                log(
                    "Intento ${attempt + 1}/$maxRetries fallido para $safeUrl: ${e.message}",
                    isError = true
                )
                if (attempt < maxRetries - 1) {
                    delay(delayMs * (attempt + 1)) // backoff lineal: 500ms, 1000ms, 1500ms
                }
            }
        }
        log("No se pudo cargar imagen tras $maxRetries intentos: $safeUrl", isError = true)
        null
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
                    this[stringPreferencesKey("tomorrowCondition")] = data.tomorrowCondition
                    this[stringPreferencesKey("humidity")] = data.humidity
                    this[stringPreferencesKey("windSpeed")] = data.windSpeed
                    this[stringPreferencesKey("windDirection")] = data.windDirection
                    this[doublePreferencesKey("uvIndex")] = data.uvIndex
                    this[stringPreferencesKey("currentIconUrl")] = data.currentIconUrl
                    this[stringPreferencesKey("day1Name")] = data.day1Name
                    this[stringPreferencesKey("day1IconUrl")] = data.day1IconUrl
                    this[stringPreferencesKey("day2Name")] = data.day2Name
                    this[stringPreferencesKey("day2IconUrl")] = data.day2IconUrl
                }
            }
        }
    }

    private suspend fun loadLastGlanceData(context: Context): WeatherWidgetData {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(getClassName())
        if (glanceIds.isEmpty()) return emptyWidgetData(context)

        val prefs = getAppWidgetState(
            context,
            PreferencesGlanceStateDefinition,
            glanceIds.first()
        )

        val currentIconUrl = prefs[stringPreferencesKey("currentIconUrl")] ?: ""
        val day1IconUrl = prefs[stringPreferencesKey("day1IconUrl")] ?: ""
        val day2IconUrl = prefs[stringPreferencesKey("day2IconUrl")] ?: ""

        val bitmaps = coroutineScope {
            val c = async { loadBitmapWithRetry(currentIconUrl) }
            val d1 = async { loadBitmapWithRetry(day1IconUrl) }
            val d2 = async { loadBitmapWithRetry(day2IconUrl) }
            Triple(c.await(), d1.await(), d2.await())
        }

        return WeatherWidgetData(
            location = prefs[stringPreferencesKey("location")] ?: "",
            time = prefs[stringPreferencesKey("time")] ?: "",
            currentTemp = prefs[stringPreferencesKey("currentTemp")] ?: "--°",
            currentCondition = prefs[stringPreferencesKey("currentCondition")] ?: "",
            tomorrowCondition = prefs[stringPreferencesKey("tomorrowCondition")] ?: "",
            humidity = prefs[stringPreferencesKey("humidity")] ?: "--",
            windSpeed = prefs[stringPreferencesKey("windSpeed")] ?: "--",
            windDirection = prefs[stringPreferencesKey("windDirection")] ?: "--",
            uvIndex = prefs[doublePreferencesKey("uvIndex")] ?: 0.0,
            currentIconUrl = currentIconUrl,
            day1Name = prefs[stringPreferencesKey("day1Name")] ?: "",
            day1IconUrl = day1IconUrl,
            day2Name = prefs[stringPreferencesKey("day2Name")] ?: "",
            day2IconUrl = day2IconUrl,
            currentIconBitmap = bitmaps.first,
            day1IconBitmap = bitmaps.second,
            day2IconBitmap = bitmaps.third
        )
    }

    private fun emptyWidgetData(context: Context) = WeatherWidgetData(
        location = "", time = "", currentTemp = "--°",
        currentCondition = context.getString(R.string.widget_error_text),
        tomorrowCondition = "--", humidity = "--", windSpeed = "--",
        windDirection = "--", uvIndex = 0.0, currentIconUrl = "",
        day1Name = "", day1IconUrl = "", day2Name = "", day2IconUrl = ""
    )

    // ============================================================
    //  ERROR DE COMPOSICIÓN
    // ============================================================

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
    //  UTILIDADES DE FECHAS
    // ============================================================

    @OptIn(ExperimentalTime::class)
    private fun isToday(dateString: String): Boolean = try {
        Instant.of(dateString, includeHours = false)?.isToday() ?: false
    } catch (_: Exception) {
        false
    }

    @OptIn(ExperimentalTime::class)
    private fun formatLocalTime(localTime: String, language: String): String = try {
        val instant =
            Instant.of(localTime, includeHours = true, timezone = TimeZone.currentSystemDefault())
        if (instant != null) formatDateTime(instant, "dd-MMM hh:mm aa", language = language) else ""
    } catch (_: Exception) {
        ""
    }

    @OptIn(ExperimentalTime::class)
    private fun getDayName(
        context: Context,
        dailyForecast: DailyForecast,
        language: String
    ): String = try {
        val instant = Instant.of(dailyForecast.date, includeHours = false)
        if (instant != null) {
            when {
                instant.isToday() -> context.getString(R.string.today)
                instant.isTomorrow() -> context.getString(R.string.tomorrow)
                else -> when (instant.toDayOfWeekText()) {
                    DayOfWeek.MONDAY -> context.getString(R.string.monday).capitalize(language)
                    DayOfWeek.TUESDAY -> context.getString(R.string.tuesday).capitalize(language)
                    DayOfWeek.WEDNESDAY -> context.getString(R.string.wednesday)
                        .capitalize(language)

                    DayOfWeek.THURSDAY -> context.getString(R.string.thursday).capitalize(language)
                    DayOfWeek.FRIDAY -> context.getString(R.string.friday).capitalize(language)
                    DayOfWeek.SATURDAY -> context.getString(R.string.saturday).capitalize(language)
                    DayOfWeek.SUNDAY -> context.getString(R.string.sunday).capitalize(language)
                }
            }
        } else ""
    } catch (_: Exception) {
        ""
    }

    private fun String.capitalize(language: String): String {
        val locale = if (language == "es") Locale("es", "ES") else Locale.ENGLISH
        return replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
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