package com.kronos.multiplatform.weatherapp.domain.model

import com.kronos.multiplatform.weatherapp.core.util.of
import kotlinx.datetime.TimeZone
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class DailyForecast(
    val date: String,
    val dateEpoch: Long,
    val day: Day,
    val astro: Astro,
    val hours: List<Hour>,
){
    @OptIn(ExperimentalTime::class)
    fun getUpcomingHours(
        timeZone: String
    ): List<Hour> {
        val currentTime = Clock.System.now()
        return hours.filter { hour ->
            Instant.of(hour.time, true, TimeZone.of(timeZone))?.let { it > currentTime } ?: false
        }
    }
}
