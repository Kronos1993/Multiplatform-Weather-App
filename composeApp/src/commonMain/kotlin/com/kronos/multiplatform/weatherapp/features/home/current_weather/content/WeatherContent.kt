package com.kronos.multiplatform.weatherapp.features.home.current_weather.content

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.kronos.multiplatform.weatherapp.components.CurrentWeatherBigScreenCompactItem
import com.kronos.multiplatform.weatherapp.components.CurrentWeatherCompactItem
import com.kronos.multiplatform.weatherapp.components.CurrentWeatherItem
import com.kronos.multiplatform.weatherapp.components.CurrentWeatherLandscapeCompactItem
import com.kronos.multiplatform.weatherapp.components.DailyWeatherList
import com.kronos.multiplatform.weatherapp.components.FixMapView
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
fun WeatherContentPortrait(
    weather: Forecast,
    deviceScreenConfiguration: DeviceScreenConfiguration,
    isDarkTheme: Boolean,
    urlProvider: UrlProvider,
    imageQuality: String,
    currentLang: String,
) {
    var isCompact by rememberSaveable { mutableStateOf(false) }
    val scrollState = rememberLazyListState()

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Solo aplicar comportamiento compact en portrait móvil
                if (deviceScreenConfiguration != DeviceScreenConfiguration.MOBILE_PORTRAIT) {
                    return Offset.Zero
                }

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

    val shouldUseCompactBehavior = deviceScreenConfiguration == DeviceScreenConfiguration.MOBILE_PORTRAIT
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
            currentLang = currentLang,
            modifier = Modifier.fillMaxWidth(),
            isCompactMode = actualCompactMode,
        )

        WeatherContentSection(
            currentWeather = weather,
            isDarkTheme = isDarkTheme,
            urlProvider = urlProvider,
            imageQuality = imageQuality,
            currentLang = currentLang,
            scrollState = scrollState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WeatherHeaderSection(
    currentWeather: Forecast,
    isDarkTheme: Boolean,
    urlProvider: UrlProvider,
    imageQuality: String,
    currentLang:String,
    modifier: Modifier = Modifier,
    isCompactMode: Boolean = false
) {
    AnimatedContent(
        targetState = isCompactMode,
        modifier = modifier,
        transitionSpec = {
            fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) with
                    fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing))
        },
        label = "headerTransition"
    ) { compact ->
        if (compact) {
            CurrentWeatherCompactItem(
                currentWeather = currentWeather,
                darkTheme = isDarkTheme,
                urlProvider = urlProvider,
                imageQuality = imageQuality,
                currentLang = currentLang,
                modifier = Modifier.wrapContentHeight()
            )
        } else {
            CurrentWeatherItem(
                currentWeather = currentWeather,
                darkTheme = isDarkTheme,
                urlProvider = urlProvider,
                imageQuality = imageQuality,
                currentLang = currentLang,
                modifier = Modifier.wrapContentHeight()
            )
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
    currentLang: String,
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
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
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
                        currentLang = currentLang,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            FixMapView(
                modifier = Modifier
                    .fillMaxSize()
                    .height(300.dp),
                lat = currentWeather.location.lat,
                lon = currentWeather.location.lon,
                onMapClick = {},
                onMapLongClick = {},
                darkTheme = isDarkTheme
            )
        }

        // Espacio al final
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun WeatherContentLandscape(
    weather: Forecast,
    isDarkTheme: Boolean,
    urlProvider: UrlProvider,
    imageQuality: String,
    currentLang: String,
    modifier: Modifier = Modifier,
    deviceScreenConfiguration: DeviceScreenConfiguration,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(0.4f),
            verticalArrangement = Arrangement.Center
        ) {

            when (deviceScreenConfiguration) {
                DeviceScreenConfiguration.MOBILE_PORTRAIT,
                DeviceScreenConfiguration.TABLET_PORTRAIT-> {
                    WeatherContentPortrait(
                        weather = weather!!,
                        deviceScreenConfiguration = deviceScreenConfiguration,
                        isDarkTheme = isDarkTheme,
                        urlProvider = urlProvider,
                        imageQuality = imageQuality,
                        currentLang = currentLang,
                    )
                }

                DeviceScreenConfiguration.MOBILE_LANDSCAPE->{
                    CurrentWeatherLandscapeCompactItem(
                        currentWeather = weather,
                        darkTheme = isDarkTheme,
                        urlProvider = urlProvider,
                        imageQuality = imageQuality,
                        currentLang = currentLang,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                DeviceScreenConfiguration.TABLET_LANDSCAPE,
                DeviceScreenConfiguration.DESKTOP -> {
                    CurrentWeatherBigScreenCompactItem(
                        currentWeather = weather,
                        darkTheme = isDarkTheme,
                        urlProvider = urlProvider,
                        imageQuality = imageQuality,
                        currentLang = currentLang,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(0.6f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WeatherContentSectionLandscape(
                currentWeather = weather,
                isDarkTheme = isDarkTheme,
                urlProvider = urlProvider,
                imageQuality = imageQuality,
                currentLang = currentLang,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun WeatherContentSectionLandscape(
    currentWeather: Forecast,
    isDarkTheme: Boolean,
    urlProvider: UrlProvider,
    imageQuality: String,
    currentLang: String,
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
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Sección de horas
        if (hours.isNotEmpty()) {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
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

        // Sección de indicadores
        if (indicators.isNotEmpty()) {
            item {
                WeatherIndicatorList(
                    indicators = indicators,
                    darkTheme = isDarkTheme,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Sección de días futuros
        if (futureDays.isNotEmpty()) {
            item {
                DailyWeatherList(
                    days = futureDays,
                    darkTheme = isDarkTheme,
                    urlProvider = urlProvider,
                    imageQuality = imageQuality,
                    currentLang = currentLang,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            FixMapView(
                modifier = Modifier
                    .fillMaxSize()
                    .height(300.dp),
                lat = currentWeather.location.lat,
                lon = currentWeather.location.lon,
                onMapClick = {},
                onMapLongClick = {},
                darkTheme = isDarkTheme
            )
        }

        // Espacio al final
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}