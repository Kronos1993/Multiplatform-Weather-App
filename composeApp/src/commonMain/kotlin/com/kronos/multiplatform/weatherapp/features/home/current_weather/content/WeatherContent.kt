package com.kronos.multiplatform.weatherapp.features.home.current_weather.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kronos.multiplatform.weatherapp.components.CurrentWeatherItem
import com.kronos.multiplatform.weatherapp.components.DailyWeatherList
import com.kronos.multiplatform.weatherapp.components.HourlyItemIndicator
import com.kronos.multiplatform.weatherapp.components.WeatherIndicatorList
import com.kronos.multiplatform.weatherapp.core.util.format
import com.kronos.multiplatform.weatherapp.core.util.isToday
import com.kronos.multiplatform.weatherapp.core.util.of
import com.kronos.multiplatform.weatherapp.data.remote.ktor.UrlProvider
import com.kronos.multiplatform.weatherapp.device.screen_config.DeviceScreenConfiguration
import com.kronos.multiplatform.weatherapp.domain.model.Indicator
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import weather_app.composeapp.generated.resources.Res
import weather_app.composeapp.generated.resources.humidity
import weather_app.composeapp.generated.resources.rain
import weather_app.composeapp.generated.resources.snow
import weather_app.composeapp.generated.resources.speed_km
import weather_app.composeapp.generated.resources.sun
import weather_app.composeapp.generated.resources.uv_index
import weather_app.composeapp.generated.resources.visibility
import weather_app.composeapp.generated.resources.visibility_km
import weather_app.composeapp.generated.resources.wind
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
fun WeatherHeaderSection(
    currentWeather: Forecast,
    isDarkTheme: Boolean,
    urlProvider: UrlProvider,
    imageQuality: String,
    alignment: Alignment.Horizontal = Alignment.Start,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = alignment
    ) {
        CurrentWeatherItem(
            currentWeather,
            isDarkTheme,
            urlProvider,
            imageQuality
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun WeatherContentSection(
    currentWeather: Forecast,
    isDarkTheme: Boolean,
    urlProvider: UrlProvider,
    imageQuality: String,
    weatherIndicatorsListState: LazyGridState,
    modifier: Modifier = Modifier
) {
    val currentDayForecast = remember(currentWeather) {
        currentWeather.forecast.forecastDay.find { forecastDay ->
            val date = Instant.of(forecastDay.date, false, TimeZone.of(currentWeather.location.tzId))
            date != null && date.isToday(TimeZone.of(currentWeather.location.tzId))
        } ?: currentWeather.forecast.forecastDay.firstOrNull()
    }

    val hours = remember(currentDayForecast) {
        val currentTime = Clock.System.now()
        currentDayForecast?.hours?.filter { hour ->
            val date = Instant.of(hour.time, true, TimeZone.of(currentWeather.location.tzId))
            date != null && date > currentTime
        }?.take(12) ?: emptyList() // Limitar a 12 horas para mejor visualización
    }

    val futureDays = remember(currentWeather) {
        currentWeather.forecast.forecastDay.filter {
            val date = Instant.of(it.date, false)
            date != null && !date.isToday()
        }.take(5) // Limitar a 5 días
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sección de pronóstico por horas
        if (hours.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(
                        items = hours,
                        key = { it.time }
                    ) { hour ->
                        HourlyItemIndicator(
                            item = hour,
                            urlProvider = urlProvider,
                            imageQuality = imageQuality,
                            darkTheme = isDarkTheme,
                            modifier = Modifier.width(80.dp)
                        )
                    }
                }
            }
        }

        val windText = stringResource(Res.string.wind)
        val humidityText = stringResource(Res.string.humidity)
        val uvIndexText = stringResource(Res.string.uv_index)
        val snowText = stringResource(Res.string.snow)
        val rainText = stringResource(Res.string.rain)
        val sunText = stringResource(Res.string.sun)
        val visibilityText = stringResource(Res.string.visibility)
        val speedKmText = stringResource(Res.string.speed_km)
        val visibilityKmText = stringResource(Res.string.visibility_km)

        // Indicadores del clima
        val indicators = remember(currentWeather, currentDayForecast) {
            listOf(
                Indicator(
                    1,
                    windText,
                    speedKmText.format(currentWeather.current.windSpeedKph),
                    Icons.Filled.Air
                ),
                Indicator(
                    2,
                    humidityText,
                    "%.0f%%".format(currentWeather.current.humidity),
                    Icons.Filled.WaterDrop
                ),
                Indicator(
                    3,
                    uvIndexText,
                    currentWeather.current.uv.toString(),
                    Icons.Filled.WbSunny
                ),
                if (currentDayForecast?.day?.dailyWillItSnow == true) {
                    Indicator(
                        4,
                        snowText,
                        "%.1f cm".format(currentDayForecast.day.totalsnowCm),
                        Icons.Filled.AcUnit
                    )
                } else {
                    Indicator(
                        5,
                        rainText,
                        "%.1f mm".format(currentWeather.current.precipitationMm),
                        Icons.Filled.WaterDrop
                    )
                },
                Indicator(
                    6,
                    sunText,
                    "${currentDayForecast?.astro?.sunrise ?: ""} - ${currentDayForecast?.astro?.sunset ?: ""}",
                    Icons.Filled.WbTwilight
                ),
                Indicator(
                    7,
                    visibilityText,
                    visibilityKmText.format(currentDayForecast?.day?.avgvisKm ?: 0),
                    Icons.Filled.Visibility
                )
            ).filter { it.description.isNotBlank() }
        }

        if (indicators.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WeatherIndicatorList(
                    indicators = indicators,
                    listState = weatherIndicatorsListState,
                    gridColumns = 2, // En landscape, 2 columnas es mejor
                    darkTheme = isDarkTheme,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Pronóstico de días
        if (futureDays.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DailyWeatherList(
                    days = futureDays,
                    listState = rememberLazyGridState(),
                    gridColumns = 1,
                    darkTheme = isDarkTheme,
                    urlProvider = urlProvider,
                    imageQuality = imageQuality,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun WeatherContent(
    weather: Forecast,
    deviceScreenConfiguration: DeviceScreenConfiguration,
    isDarkTheme: Boolean,
    urlProvider: UrlProvider,
    imageQuality: String,
    listState: LazyGridState
) {
    val rootModifier = Modifier
        .fillMaxSize()
        .clip(RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp))
        .background(MaterialTheme.colorScheme.surface)
        .padding(horizontal = 16.dp, vertical = 24.dp)
        .consumeWindowInsets(WindowInsets.navigationBars)

    when (deviceScreenConfiguration) {
        DeviceScreenConfiguration.MOBILE_PORTRAIT -> {
            Column(
                modifier = rootModifier,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                WeatherHeaderSection(
                    currentWeather = weather,
                    isDarkTheme = isDarkTheme,
                    urlProvider = urlProvider,
                    imageQuality = imageQuality,
                    modifier = Modifier.fillMaxWidth()
                )

                WeatherContentSection(
                    currentWeather = weather,
                    isDarkTheme = isDarkTheme,
                    urlProvider = urlProvider,
                    imageQuality = imageQuality,
                    weatherIndicatorsListState = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }

        DeviceScreenConfiguration.MOBILE_LANDSCAPE -> {
            Row(
                modifier = rootModifier,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header en landscape - más compacto
                WeatherHeaderSection(
                    currentWeather = weather,
                    isDarkTheme = isDarkTheme,
                    urlProvider = urlProvider,
                    imageQuality = imageQuality,
                    alignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(0.4f)
                        .verticalScroll(rememberScrollState())
                )

                // Content en landscape - más ancho
                WeatherContentSection(
                    currentWeather = weather,
                    isDarkTheme = isDarkTheme,
                    urlProvider = urlProvider,
                    imageQuality = imageQuality,
                    weatherIndicatorsListState = listState,
                    modifier = Modifier
                        .weight(0.6f)
                        .verticalScroll(rememberScrollState())
                )
            }
        }

        DeviceScreenConfiguration.TABLET_PORTRAIT -> {
            Column(
                modifier = rootModifier
                    .verticalScroll(rememberScrollState())
                    .padding(top = 48.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                WeatherHeaderSection(
                    currentWeather = weather,
                    isDarkTheme = isDarkTheme,
                    urlProvider = urlProvider,
                    imageQuality = imageQuality,
                    modifier = Modifier.widthIn(max = 600.dp),
                    alignment = Alignment.CenterHorizontally
                )

                WeatherContentSection(
                    currentWeather = weather,
                    isDarkTheme = isDarkTheme,
                    urlProvider = urlProvider,
                    imageQuality = imageQuality,
                    weatherIndicatorsListState = listState,
                    modifier = Modifier.widthIn(max = 600.dp)
                )
            }
        }

        DeviceScreenConfiguration.TABLET_LANDSCAPE,
        DeviceScreenConfiguration.DESKTOP -> {
            Row(
                modifier = rootModifier,
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                WeatherHeaderSection(
                    currentWeather = weather,
                    isDarkTheme = isDarkTheme,
                    urlProvider = urlProvider,
                    imageQuality = imageQuality,
                    alignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(0.35f)
                        .verticalScroll(rememberScrollState())
                )

                WeatherContentSection(
                    currentWeather = weather,
                    isDarkTheme = isDarkTheme,
                    urlProvider = urlProvider,
                    imageQuality = imageQuality,
                    weatherIndicatorsListState = listState,
                    modifier = Modifier
                        .weight(0.65f)
                        .verticalScroll(rememberScrollState())
                )
            }
        }
    }
}