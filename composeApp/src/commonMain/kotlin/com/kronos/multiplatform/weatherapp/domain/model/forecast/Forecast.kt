package com.kronos.multiplatform.weatherapp.domain.model.forecast

import com.kronos.multiplatform.weatherapp.core.util.isToday
import com.kronos.multiplatform.weatherapp.core.util.of
import com.kronos.multiplatform.weatherapp.domain.model.CurrentWeather
import com.kronos.multiplatform.weatherapp.domain.model.DailyForecast
import com.kronos.multiplatform.weatherapp.domain.model.ForecastDay
import com.kronos.multiplatform.weatherapp.domain.model.Location
import kotlinx.datetime.TimeZone
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class Forecast(
    val location: Location = Location(),
    val current: CurrentWeather = CurrentWeather(),
    val forecast: ForecastDay = ForecastDay(),
){
    @OptIn(ExperimentalTime::class)
    fun getCurrentDayForecast(
        timeZone: String
    ): DailyForecast? {
        return forecast.forecastDay.find { forecastDay ->
            Instant.of(forecastDay.date, false, TimeZone.of(timeZone))?.isToday(
                TimeZone.of(timeZone)
            ) ?: false
        } ?: forecast.forecastDay.firstOrNull()
    }

    @OptIn(ExperimentalTime::class)
    fun getFutureDays(amountOfDays: Int = 3): List<DailyForecast> {
        return forecast.forecastDay.filter {
            val date = Instant.of(it.date, false)
            date != null && !date.isToday()
        }.take(amountOfDays)
    }
}
