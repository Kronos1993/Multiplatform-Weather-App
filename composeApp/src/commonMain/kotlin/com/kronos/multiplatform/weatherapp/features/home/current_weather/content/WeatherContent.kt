package com.kronos.multiplatform.weatherapp.features.home.current_weather.content

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.kronos.multiplatform.weatherapp.components.CurrentWeatherCompactItem
import com.kronos.multiplatform.weatherapp.components.CurrentWeatherItem
import com.kronos.multiplatform.weatherapp.components.DailyWeatherList
import com.kronos.multiplatform.weatherapp.components.HourlyItemIndicator
import com.kronos.multiplatform.weatherapp.components.WeatherIndicatorList
import com.kronos.multiplatform.weatherapp.components.icons.WeatherAppIcons
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.RainyIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.SnowflakeIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.SunIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.SunSunriseIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.VisibilityIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.WaterDropsIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.WindIndicator
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
fun WeatherContent(
    weather: Forecast,
    deviceScreenConfiguration: DeviceScreenConfiguration,
    isDarkTheme: Boolean,
    urlProvider: UrlProvider,
    imageQuality: String,
    paddingValues: PaddingValues
) {
    var isCompact by rememberSaveable { mutableStateOf(false) }
    val scrollState = rememberLazyListState()

    // Nested scroll connection corregido
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Scroll hacia ABAJO (valor positivo) -> expandir
                if (available.y > 30 && isCompact) {
                    isCompact = false
                    return Offset(0f, available.y) // Consumir parte del scroll
                }
                // Scroll hacia ARRIBA (valor negativo) -> contraer
                else if (available.y < -30 && !isCompact) {
                    isCompact = true
                    return Offset(0f, available.y) // Consumir parte del scroll
                }
                return Offset.Zero
            }
        }
    }

    val rootModifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.surface)
        .padding(horizontal = 8.dp)
        .nestedScroll(nestedScrollConnection)
        .consumeWindowInsets(WindowInsets.navigationBars)

    val shouldUseCompactBehavior = when (deviceScreenConfiguration) {
        DeviceScreenConfiguration.MOBILE_PORTRAIT,
        DeviceScreenConfiguration.TABLET_PORTRAIT -> true
        else -> false
    }

    val actualCompactMode = isCompact && shouldUseCompactBehavior

    Column(
        modifier = rootModifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        WeatherHeaderSection(
            currentWeather = weather,
            isDarkTheme = isDarkTheme,
            urlProvider = urlProvider,
            imageQuality = imageQuality,
            modifier = Modifier.fillMaxWidth(),
            isCompactMode = actualCompactMode,
        )

        WeatherContentSection(
            currentWeather = weather,
            isDarkTheme = isDarkTheme,
            urlProvider = urlProvider,
            imageQuality = imageQuality,
            scrollState = scrollState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}

@Composable
fun WeatherHeaderSection(
    currentWeather: Forecast,
    isDarkTheme: Boolean,
    urlProvider: UrlProvider,
    imageQuality: String,
    modifier: Modifier = Modifier,
    isCompactMode: Boolean = false
) {
    val transition = updateTransition(targetState = isCompactMode, label = "headerTransition")

    val alpha by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 300, easing = FastOutSlowInEasing)
        }, label = "alpha"
    ) { compact -> if (compact) 1f else 1f }

    val verticalPadding by transition.animateDp(
        transitionSpec = {
            tween(durationMillis = 300, easing = FastOutSlowInEasing)
        }, label = "verticalPadding"
    ) { compact -> if (compact) 8.dp else 16.dp }

    Box(
        modifier = modifier
            .alpha(alpha)
            .padding(vertical = verticalPadding),
        contentAlignment = Alignment.TopCenter
    ) {
        Crossfade(
            targetState = isCompactMode,
            modifier = Modifier.fillMaxWidth(),
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            label = "headerCrossfade"
        ) { compact ->
            if (compact) {
                CurrentWeatherCompactItem(
                    currentWeather = currentWeather,
                    darkTheme = isDarkTheme,
                    urlProvider = urlProvider,
                    imageQuality = imageQuality,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                CurrentWeatherItem(
                    currentWeather = currentWeather,
                    darkTheme = isDarkTheme,
                    urlProvider = urlProvider,
                    imageQuality = imageQuality
                )
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun WeatherContentSection(
    currentWeather: Forecast,
    isDarkTheme: Boolean,
    urlProvider: UrlProvider,
    imageQuality: String,
    scrollState: LazyListState,
    modifier: Modifier = Modifier
) {
    val currentDayForecast = remember(currentWeather) {
        currentWeather.forecast.forecastDay.find { forecastDay ->
            val date =
                Instant.of(forecastDay.date, false, TimeZone.of(currentWeather.location.tzId))
            date != null && date.isToday(TimeZone.of(currentWeather.location.tzId))
        } ?: currentWeather.forecast.forecastDay.firstOrNull()
    }

    val hours = remember(currentDayForecast) {
        val currentTime = Clock.System.now()
        currentDayForecast?.hours?.filter { hour ->
            val date = Instant.of(hour.time, true, TimeZone.of(currentWeather.location.tzId))
            date != null && date > currentTime
        }?.take(12) ?: emptyList()
    }

    val futureDays = remember(currentWeather) {
        currentWeather.forecast.forecastDay.filter {
            val date = Instant.of(it.date, false)
            date != null && !date.isToday()
        }.take(5)
    }

    // Obtener strings
    val windText = stringResource(Res.string.wind)
    val humidityText = stringResource(Res.string.humidity)
    val uvIndexText = stringResource(Res.string.uv_index)
    val snowText = stringResource(Res.string.snow)
    val rainText = stringResource(Res.string.rain)
    val sunText = stringResource(Res.string.sun)
    val visibilityText = stringResource(Res.string.visibility)
    val speedKmText = stringResource(Res.string.speed_km)
    val visibilityKmText = stringResource(Res.string.visibility_km)

    val indicators = remember(currentWeather, currentDayForecast) {
        listOf(
            Indicator(
                1,
                windText,
                speedKmText.format(currentWeather.current.windSpeedKph),
                WeatherAppIcons.WindIndicator
            ),
            Indicator(
                2,
                humidityText,
                "${currentWeather.current.humidity}%",
                WeatherAppIcons.WaterDropsIndicator
            ),
            Indicator(
                3,
                uvIndexText,
                currentWeather.current.uv.toString(),
                WeatherAppIcons.SunIndicator
            ),
            if (currentDayForecast?.day?.dailyWillItSnow == true) {
                Indicator(
                    4,
                    snowText,
                    "${currentDayForecast.day.totalsnowCm} cm",
                    WeatherAppIcons.SnowflakeIndicator
                )
            } else {
                Indicator(
                    5,
                    rainText,
                    "${currentWeather.current.precipitationMm} mm",
                    WeatherAppIcons.RainyIndicator
                )
            },
            Indicator(
                6,
                sunText,
                "${currentDayForecast?.astro?.sunrise ?: ""} - ${currentDayForecast?.astro?.sunset ?: ""}",
                WeatherAppIcons.SunSunriseIndicator
            ),
            Indicator(
                7,
                visibilityText,
                visibilityKmText.format(currentDayForecast?.day?.avgvisKm ?: 0),
                WeatherAppIcons.VisibilityIndicator
            )
        ).filter { it.description.isNotBlank() }
    }

    LazyColumn(
        state = scrollState,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Item vacío para que el scroll empiece desde el header
        item {
            Spacer(modifier = Modifier.height(1.dp))
        }

        // Sección de horas
        if (hours.isNotEmpty()) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        items(hours, key = { it.time }) { hour ->
                            HourlyItemIndicator(
                                item = hour,
                                urlProvider = urlProvider,
                                imageQuality = imageQuality,
                                darkTheme = isDarkTheme,
                            )
                        }
                    }
                }
            }
        }

        // Sección de indicadores
        if (indicators.isNotEmpty()) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WeatherIndicatorList(
                        indicators = indicators,
                        darkTheme = isDarkTheme,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Sección de días futuros
        if (futureDays.isNotEmpty()) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DailyWeatherList(
                        days = futureDays,
                        darkTheme = isDarkTheme,
                        urlProvider = urlProvider,
                        imageQuality = imageQuality,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Espacio al final
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}