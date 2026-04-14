package com.kronos.multiplatform.weatherapp.data.mapper

import com.kronos.multiplatform.weatherapp.data.remote.dto.AirQualityDto
import com.kronos.multiplatform.weatherapp.data.remote.dto.AstroDto
import com.kronos.multiplatform.weatherapp.data.remote.dto.ConditionDto
import com.kronos.multiplatform.weatherapp.data.remote.dto.CurrentWeatherDto
import com.kronos.multiplatform.weatherapp.data.remote.dto.DailyForecastDto
import com.kronos.multiplatform.weatherapp.data.remote.dto.DayDto
import com.kronos.multiplatform.weatherapp.data.remote.dto.ForecastDayDto
import com.kronos.multiplatform.weatherapp.data.remote.dto.HourDto
import com.kronos.multiplatform.weatherapp.data.remote.dto.LocationDto
import com.kronos.multiplatform.weatherapp.data.remote.dto.WeatherAlertDto
import com.kronos.multiplatform.weatherapp.data.remote.dto.current.CurrentAlertsForecastDto
import com.kronos.multiplatform.weatherapp.data.remote.dto.current.CurrentForecastResponseDto
import com.kronos.multiplatform.weatherapp.data.remote.dto.forecast.ForecastResponseDto
import com.kronos.multiplatform.weatherapp.domain.model.AirQuality
import com.kronos.multiplatform.weatherapp.domain.model.Astro
import com.kronos.multiplatform.weatherapp.domain.model.Condition
import com.kronos.multiplatform.weatherapp.domain.model.CurrentWeather
import com.kronos.multiplatform.weatherapp.domain.model.DailyForecast
import com.kronos.multiplatform.weatherapp.domain.model.Day
import com.kronos.multiplatform.weatherapp.domain.model.ForecastDay
import com.kronos.multiplatform.weatherapp.domain.model.Hour
import com.kronos.multiplatform.weatherapp.domain.model.Location
import com.kronos.multiplatform.weatherapp.domain.model.MeasureUnit
import com.kronos.multiplatform.weatherapp.domain.model.MoonPhase
import com.kronos.multiplatform.weatherapp.domain.model.SuggestionPriority
import com.kronos.multiplatform.weatherapp.domain.model.SuggestionType
import com.kronos.multiplatform.weatherapp.domain.model.WeatherSuggestionModel
import com.kronos.multiplatform.weatherapp.domain.model.alerts.WeatherAlert
import com.kronos.multiplatform.weatherapp.domain.model.current.CurrentAlertsForecast
import com.kronos.multiplatform.weatherapp.domain.model.current.CurrentForecast
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast

fun AstroDto.toAstro() = Astro(
    sunrise = sunrise,
    sunset = sunset,
    moonrise = moonrise,
    moonset = moonset,
    moonPhase = MoonPhase.fromText(moon_phase),
    moonIllumination = moon_illumination,
    isMoonUp = is_moon_up == 1,
    isSunUp = is_sun_up == 1
)

fun ConditionDto.toCondition() = Condition(
    description = text,
    icon = icon,
    code = code
)

fun CurrentWeatherDto.toCurrentWeather() = CurrentWeather(
    tempC = temp_c,
    tempF = temp_f,
    isDay = is_day == 1,
    condition = condition.toCondition(),
    windSpeedKph = wind_kph,
    windSpeedMph = wind_mph,
    windDegree = wind_degree,
    windDir = wind_dir,
    pressureMb = pressure_mb,
    pressureIn = pressure_in,
    precipitationMm = precip_mm,
    humidity = humidity,
    cloud = cloud,
    feelslikeC = feelslike_c,
    feelslikeF = feelslike_f,
    visionKM = vis_km,
    visionMiles = vis_miles,
    uv = uv,
    gustMph = gust_mph,
    gustKph = gust_kph,
    airQuality = air_quality.toAirQuality()
)

fun DailyForecastDto.toDailyForecast() = DailyForecast(
    date = date,
    dateEpoch = date_epoch,
    day = day.toDay(),
    astro = astro.toAstro(),
    hours = hour.map { it.toHour() }
)

fun DayDto.toDay() = Day(
    maxtempC = maxtemp_c,
    maxtempF = maxtemp_f,
    mintempC = mintemp_c,
    mintempF = mintemp_f,
    avgtempC = avgtemp_c,
    avgtempF = avgtemp_f,
    maxwindMph = maxwind_mph,
    maxwindKph = maxwind_kph,
    totalprecipMm = totalprecip_mm,
    totalprecipIn = totalprecip_in,
    totalsnowCm = totalsnow_cm,
    avgvisKm = avgvis_km,
    avgvisMiles = avgvis_miles,
    avghumidity = avghumidity,
    dailyWillItRain = daily_will_it_rain == 1,
    dailyChanceOfRain = daily_chance_of_rain,
    dailyWillItSnow = daily_will_it_snow == 1,
    dailyChanceOfSnow = daily_chance_of_snow,
    condition = condition.toCondition(),
    uv = uv
)

fun HourDto.toHour() = Hour(
    timeEpoch = time_epoch,
    time = time,
    tempC = temp_c,
    tempF = temp_f,
    isDay = is_day == 1,
    condition = condition.toCondition(),
    windMph = wind_mph,
    windKph = wind_kph,
    windDegree = wind_degree,
    windDir = wind_dir,
    pressureMb = pressure_mb,
    pressureIn = pressure_in,
    precipMm = precip_mm,
    precipIn = precip_in,
    humidity = humidity,
    cloud = cloud,
    feelslikeC = feelslike_c,
    feelslikeF = feelslike_f,
    windchillC = windchill_c,
    windchillF = windchill_f,
    heatindexC = heatindex_c,
    heatindexF = heatindex_f,
    dewpoC = dewpoint_c,
    dewpoF = dewpoint_f,
    willItRain = will_it_rain == 1,
    chanceOfRain = chance_of_rain,
    willItSnow = will_it_snow == 1,
    chanceOfSnow = chance_of_snow,
    visKm = vis_km,
    visMiles = vis_miles,
    gustMph = gust_mph,
    gustKph = gust_kph,
    uv = uv
)

fun LocationDto.toLocation() = Location(
    name = name,
    region = region,
    country = country,
    lat = lat,
    lon = lon,
    tzId = tz_id,
    localtimeEpoch = localtime_epoch,
    localtime = localtime
)

fun ForecastDayDto.toForecastDay()= ForecastDay(
    forecastDay = forecastday.map { it.toDailyForecast() }
)

fun ForecastResponseDto.toForecast() = Forecast(
    location = location.toLocation(),
    current = current.toCurrentWeather(),
    forecast = forecast.toForecastDay(),
    alerts = alerts.alertList.map { it.toWeatherAlert() }
)

fun CurrentForecastResponseDto.toCurrentForecast() = CurrentForecast(
    location = location.toLocation(),
    current = current.toCurrentWeather()
)

fun AirQualityDto.toAirQuality() = AirQuality(
    co = co,
    no2 = no2,
    o3 = o3,
    so2 = so2,
    pm2_5 = pm2_5,
    pm10 = pm10,
    usEpaIndex = usEpaIndex,
    gbDefraIndex = gbDefraIndex
)

fun WeatherAlertDto.toWeatherAlert() = WeatherAlert(
    identifier = identifier,
    headline = headline,
    msgtype = msgtype,
    severity = severity,
    urgency = urgency,
    areas = areas,
    category = category,
    certainty = certainty,
    event = event,
    note = note,
    effective = effective,
    expires = expires,
    description = description,
    instruction = instruction
)

fun CurrentAlertsForecastDto.toCurrentAlertsForecast() = CurrentAlertsForecast(
    location = location.toLocation(),
    alerts = alerts.alertList.map { it.toWeatherAlert() }
)


private const val MAX_SUGGESTIONS = 3

fun Forecast.mapCurrentSuggestions(timeZone: String,measureUnit:MeasureUnit): List<WeatherSuggestionModel> {
    val suggestions = mutableListOf<WeatherSuggestionModel>()
    val current = this.current
    val todayForecast = this.getCurrentDayForecast(timeZone)
    val upcomingHours = todayForecast?.getUpcomingHours(timeZone) ?: emptyList()

    val rainSoon = upcomingHours.take(3).any { it.precipMm > 0.5 || it.chanceOfRain > 50 }
    if (rainSoon) {
        val maxChance = upcomingHours.take(3).maxOfOrNull { it.chanceOfRain } ?: 0.0
        suggestions.add(
            WeatherSuggestionModel(
                type = SuggestionType.RAIN,
                priority = SuggestionPriority.HIGH,
                icon = "🌂",
                args = listOf(maxChance.toInt().toString())
            )
        )
    }

    // ☀️ UV alto
    when {
        current.uv >= 8 -> suggestions.add(
            WeatherSuggestionModel(
                type = SuggestionType.UV,
                priority = SuggestionPriority.HIGH,
                icon = "🧴",
                args = listOf(current.uv.toInt().toString())
            )
        )
        current.uv >= 5 -> suggestions.add(
            WeatherSuggestionModel(
                type = SuggestionType.UV,
                priority = SuggestionPriority.MEDIUM,
                icon = "😎"
            )
        )
    }

    // 🌡️ Calor extremo
    if (current.feelslikeC >= 38) suggestions.add(
        WeatherSuggestionModel(
            type = SuggestionType.HEAT,
            priority = SuggestionPriority.HIGH,
            icon = "💧",
            args = listOf(
                if (measureUnit == MeasureUnit.INTERNATIONAL)
                    current.feelslikeC.toInt().toString()
                else
                    current.feelslikeF.toInt().toString()
            )
        )
    )

    // 💨 Viento fuerte
    if (current.windSpeedKph >= 40) suggestions.add(
        WeatherSuggestionModel(
            type = SuggestionType.WIND,
            priority = SuggestionPriority.MEDIUM,
            icon = "⚠️",
            args = listOf(
                if (measureUnit == MeasureUnit.INTERNATIONAL)
                    current.windSpeedKph.toInt().toString()
                else
                    current.windSpeedMph.toInt().toString()
            )
        )
    )

    // 💦 Humedad alta
    if (current.humidity >= 85) suggestions.add(
        WeatherSuggestionModel(
            type = SuggestionType.HUMIDITY,
            priority = SuggestionPriority.LOW,
            icon = "👕"
        )
    )

    return suggestions
        .sortedBy { it.priority.ordinal }
        .take(MAX_SUGGESTIONS)
}

fun Forecast.mapTomorrowNotification(measureUnit: MeasureUnit): WeatherSuggestionModel? {
    val tomorrow = this.getFutureDays(1).firstOrNull() ?: return null
    val day = tomorrow.day

    return when {
        day.dailyChanceOfRain > 70 -> WeatherSuggestionModel(
            type = SuggestionType.TOMORROW_FORECAST,
            priority = SuggestionPriority.HIGH,
            icon = "🌧️",
            args = if (measureUnit == MeasureUnit.INTERNATIONAL)
                listOf(
                    day.dailyChanceOfRain.toInt().toString(),
                    day.maxtempC.toInt().toString(),
                    day.mintempC.toInt().toString()
                )
            else
                listOf(
                    day.dailyChanceOfRain.toInt().toString(),
                    day.maxtempF.toInt().toString(),
                    day.mintempF.toInt().toString()
                )
        )
        day.uv >= 8 -> WeatherSuggestionModel(
            type = SuggestionType.TOMORROW_FORECAST,
            priority = SuggestionPriority.MEDIUM,
            icon = "☀️",
            args = if (measureUnit == MeasureUnit.INTERNATIONAL)
                listOf(day.maxtempC.toInt().toString(), day.uv.toInt().toString())
            else
                listOf(day.maxtempF.toInt().toString(), day.uv.toInt().toString())
        )
        else -> WeatherSuggestionModel(
            type = SuggestionType.TOMORROW_FORECAST,
            priority = SuggestionPriority.LOW,
            icon = "🌤️",
            args = if (measureUnit == MeasureUnit.INTERNATIONAL)
                listOf(day.maxtempC.toInt().toString(), day.mintempC.toInt().toString())
            else
                listOf(day.maxtempF.toInt().toString(), day.mintempF.toInt().toString())
        )
    }
}

fun Forecast.mapMorningSuggestions(timeZone: String,measureUnit: MeasureUnit): List<WeatherSuggestionModel> {
    val suggestions = mutableListOf<WeatherSuggestionModel>()
    val today = this.getCurrentDayForecast(timeZone) ?: return emptyList()
    val day = today.day

    if (day.dailyChanceOfRain > 50) suggestions.add(
        WeatherSuggestionModel(
            type = SuggestionType.MORNING_SUMMARY,
            priority = SuggestionPriority.HIGH,
            icon = "🌂",
            args = listOf(day.dailyChanceOfRain.toInt().toString())
        )
    )

    if (day.uv >= 8) suggestions.add(
        WeatherSuggestionModel(
            type = SuggestionType.MORNING_SUMMARY,
            priority = SuggestionPriority.HIGH,
            icon = "🧴",
            args = listOf(day.uv.toInt().toString())
        )
    )

    if (day.maxtempC >= 35) suggestions.add(
        WeatherSuggestionModel(
            type = SuggestionType.MORNING_SUMMARY,
            priority = SuggestionPriority.MEDIUM,
            icon = "🌡️",
            args = listOf(
                if (measureUnit == MeasureUnit.INTERNATIONAL) {
                    day.maxtempC.toInt().toString()
                }
                else {
                    day.maxtempF.toInt().toString()
                }
            )
        )
    )

    return suggestions
        .sortedBy { it.priority.ordinal }
        .take(MAX_SUGGESTIONS)
}