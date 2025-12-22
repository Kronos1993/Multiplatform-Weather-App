package com.kronos.multiplatform.weatherapp.features.home.current_weather.content

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.kronos.multiplatform.weatherapp.components.CurrentWeatherBigScreenCompactItem
import com.kronos.multiplatform.weatherapp.components.CurrentWeatherCompactItem
import com.kronos.multiplatform.weatherapp.components.CurrentWeatherItem
import com.kronos.multiplatform.weatherapp.components.CurrentWeatherLandscapeCompactItem
import com.kronos.multiplatform.weatherapp.components.DailyWeatherList
import com.kronos.multiplatform.weatherapp.components.HourlyItemIndicator
import com.kronos.multiplatform.weatherapp.components.WeatherIndicatorList
import com.kronos.multiplatform.weatherapp.components.icons.WeatherAppIcons
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.MoonFallIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.RainyIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.SnowflakeIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.SunIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.SunSunriseIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.VisibilityIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.WaterDropsIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.WindIndicator
import com.kronos.multiplatform.weatherapp.components.maps.FixMapView
import com.kronos.multiplatform.weatherapp.components.maps.markers.MapMarker
import com.kronos.multiplatform.weatherapp.core.util.format
import com.kronos.multiplatform.weatherapp.data.remote.ktor.UrlProvider
import com.kronos.multiplatform.weatherapp.device.screen_config.DeviceScreenConfiguration
import com.kronos.multiplatform.weatherapp.domain.model.DailyForecast
import com.kronos.multiplatform.weatherapp.domain.model.Hour
import com.kronos.multiplatform.weatherapp.domain.model.Indicator
import com.kronos.multiplatform.weatherapp.domain.model.MoonPhase
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import weather_app.composeapp.generated.resources.Res
import weather_app.composeapp.generated.resources.first_quarter_moon_indicator
import weather_app.composeapp.generated.resources.full_moon_indicator
import weather_app.composeapp.generated.resources.humidity
import weather_app.composeapp.generated.resources.moon
import weather_app.composeapp.generated.resources.moon_phase_first_quarter
import weather_app.composeapp.generated.resources.moon_phase_full_moon
import weather_app.composeapp.generated.resources.moon_phase_last_quarter
import weather_app.composeapp.generated.resources.moon_phase_new_moon
import weather_app.composeapp.generated.resources.moon_phase_waning_crescent
import weather_app.composeapp.generated.resources.moon_phase_waning_gibbous
import weather_app.composeapp.generated.resources.moon_phase_waxing_crescent
import weather_app.composeapp.generated.resources.moon_phase_waxing_gibbous
import weather_app.composeapp.generated.resources.new_moon_indicator
import weather_app.composeapp.generated.resources.rain
import weather_app.composeapp.generated.resources.snow
import weather_app.composeapp.generated.resources.speed_km
import weather_app.composeapp.generated.resources.sun
import weather_app.composeapp.generated.resources.third_quarter_moon_indicator
import weather_app.composeapp.generated.resources.uv_index
import weather_app.composeapp.generated.resources.uv_index_extreme
import weather_app.composeapp.generated.resources.uv_index_high
import weather_app.composeapp.generated.resources.uv_index_low
import weather_app.composeapp.generated.resources.uv_index_medium
import weather_app.composeapp.generated.resources.uv_index_very_high
import weather_app.composeapp.generated.resources.visibility
import weather_app.composeapp.generated.resources.visibility_km
import weather_app.composeapp.generated.resources.waning_crescent_moon_indicator
import weather_app.composeapp.generated.resources.waning_gibbous_moon_indicator
import weather_app.composeapp.generated.resources.waxing_cescent_moon_indicator
import weather_app.composeapp.generated.resources.waxing_gibbous_moon_indicator
import weather_app.composeapp.generated.resources.wind
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
private fun getWeatherIndicators(
    currentWeather: Forecast,
    currentDayForecast: DailyForecast?
): List<Indicator> {
    val windText = stringResource(Res.string.wind)
    val humidityText = stringResource(Res.string.humidity)
    val uvIndexText = stringResource(Res.string.uv_index)
    val uvDescription = uvIndexDescription(currentWeather.current.uv)
    val snowText = stringResource(Res.string.snow)
    val rainText = stringResource(Res.string.rain)
    val sunText = stringResource(Res.string.sun)
    val moonPhaseIcon =
        currentDayForecast?.astro?.moonPhase?.icon() ?: WeatherAppIcons.MoonFallIndicator
    val moonPhaseName =
        currentDayForecast?.astro?.moonPhase?.name() ?: stringResource(Res.string.moon)
    val visibilityText = stringResource(Res.string.visibility)
    val speedKmText = stringResource(Res.string.speed_km)
    val visibilityKmText = stringResource(Res.string.visibility_km)

    return listOf(
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
            uvDescription,
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
        if (currentWeather.current.isDay) {
            Indicator(
                6,
                sunText,
                "${currentDayForecast?.astro?.sunrise ?: ""} - ${currentDayForecast?.astro?.sunset ?: ""}",
                WeatherAppIcons.SunSunriseIndicator
            )
        } else {
            Indicator(
                6,
                moonPhaseName,
                "${currentDayForecast?.astro?.moonset ?: ""} - ${currentDayForecast?.astro?.moonrise ?: ""}",
                moonPhaseIcon
            )
        },
        Indicator(
            7,
            visibilityText,
            visibilityKmText.format(currentDayForecast?.day?.avgvisKm ?: 0),
            WeatherAppIcons.VisibilityIndicator
        )
    ).filter { it.description.isNotBlank() }
}

@Composable
fun WeatherContentPortrait(
    weather: Forecast,
    deviceScreenConfiguration: DeviceScreenConfiguration,
    isDarkTheme: Boolean,
    urlProvider: UrlProvider,
    imageQuality: String,
    currentLang: String,
    onHourItemClicked: (Hour) -> Unit,
    onDailyItemClicked: (DailyForecast) -> Unit,
    amountOfDays: Int = 3
) {
    var isCompact by rememberSaveable { mutableStateOf(false) }
    val scrollState = rememberLazyListState()

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (deviceScreenConfiguration != DeviceScreenConfiguration.MOBILE_PORTRAIT) {
                    return Offset.Zero
                }

                if (available.y > 30 && isCompact) {
                    isCompact = false
                    return Offset(0f, available.y)
                } else if (available.y < -30 && !isCompact) {
                    isCompact = true
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }
        }
    }

    val shouldUseCompactBehavior =
        deviceScreenConfiguration == DeviceScreenConfiguration.MOBILE_PORTRAIT
    val actualCompactMode = isCompact && shouldUseCompactBehavior

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
            .nestedScroll(nestedScrollConnection)
            .consumeWindowInsets(WindowInsets.navigationBars),
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
                .weight(1f),
            onHourItemClicked = onHourItemClicked,
            onDailyItemClicked = onDailyItemClicked,
            amountOfDays = amountOfDays
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
    currentLang: String,
    modifier: Modifier = Modifier,
    isCompactMode: Boolean = false
) {
    AnimatedContent(
        targetState = isCompactMode,
        modifier = modifier,
        transitionSpec = {
            fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)).togetherWith(
                fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing))
            )
        },
        label = "headerTransition"
    ) { compact ->
        when {
            compact -> CurrentWeatherCompactItem(
                currentWeather = currentWeather,
                darkTheme = isDarkTheme,
                urlProvider = urlProvider,
                imageQuality = imageQuality,
                currentLang = currentLang,
                modifier = Modifier.wrapContentHeight()
            )

            else -> CurrentWeatherItem(
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
    modifier: Modifier = Modifier,
    onHourItemClicked: (Hour) -> Unit,
    onDailyItemClicked: (DailyForecast) -> Unit,
    amountOfDays: Int = 3
) {
    val timeZone = currentWeather.location.tzId
    val currentDayForecast = currentWeather.getCurrentDayForecast(timeZone)

    val hours = currentDayForecast?.getUpcomingHours(timeZone).orEmpty()

    val futureDays = currentWeather.getFutureDays(amountOfDays)

    val indicators = getWeatherIndicators(currentWeather, currentDayForecast)


    LazyColumn(
        state = scrollState,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
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
                            onItemClick = onHourItemClicked
                        )
                    }
                }
            }
        }

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
                        modifier = Modifier.fillMaxWidth(),
                        onItemClick = onDailyItemClicked,
                    )
                }
            }
        }

        item {
            FixMapView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                markers = listOf(
                    MapMarker(
                        id = "1",
                        latitude = currentWeather.location.lat,
                        longitude = currentWeather.location.lon,
                        title = currentWeather.location.name,
                        description = currentWeather.location.region,
                        customProperties = mapOf(
                            "temp" to currentWeather.current.tempC.toString(),
                            "condition" to currentWeather.current.condition.description,
                            "icon" to currentWeather.current.condition.icon
                        )
                    )
                ),
                onMapClick = {},
                onMapLongClick = {},
                darkTheme = isDarkTheme,
            )
        }

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
    onHourItemClicked: (Hour) -> Unit,
    onDailyItemClicked: (DailyForecast) -> Unit,
    amountOfDays: Int = 3
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier.weight(0.4f),
            verticalArrangement = Arrangement.Center
        ) {
            when (deviceScreenConfiguration) {
                DeviceScreenConfiguration.MOBILE_LANDSCAPE ->
                    CurrentWeatherLandscapeCompactItem(
                        currentWeather = weather,
                        darkTheme = isDarkTheme,
                        urlProvider = urlProvider,
                        imageQuality = imageQuality,
                        currentLang = currentLang,
                        modifier = Modifier.fillMaxWidth()
                    )

                DeviceScreenConfiguration.TABLET_LANDSCAPE,
                DeviceScreenConfiguration.DESKTOP ->
                    CurrentWeatherBigScreenCompactItem(
                        currentWeather = weather,
                        darkTheme = isDarkTheme,
                        urlProvider = urlProvider,
                        imageQuality = imageQuality,
                        currentLang = currentLang,
                        modifier = Modifier.fillMaxWidth()
                    )

                else -> WeatherContentPortrait(
                    weather = weather,
                    deviceScreenConfiguration = deviceScreenConfiguration,
                    isDarkTheme = isDarkTheme,
                    urlProvider = urlProvider,
                    imageQuality = imageQuality,
                    currentLang = currentLang,
                    onHourItemClicked = onHourItemClicked,
                    onDailyItemClicked = onDailyItemClicked,
                    amountOfDays = amountOfDays
                )
            }
        }

        Column(
            modifier = Modifier.weight(0.6f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WeatherContentSection(
                currentWeather = weather,
                isDarkTheme = isDarkTheme,
                urlProvider = urlProvider,
                imageQuality = imageQuality,
                currentLang = currentLang,
                scrollState = rememberLazyListState(),
                modifier = Modifier.fillMaxSize(),
                onHourItemClicked = onHourItemClicked,
                onDailyItemClicked = onDailyItemClicked,
                amountOfDays = amountOfDays
            )
        }
    }
}

@Composable
fun uvIndexDescription(index: Double): String {
    return when (index) {
        in 0.0..2.9 -> stringResource(Res.string.uv_index_low)
        in 3.0..5.9 -> stringResource(Res.string.uv_index_medium)
        in 6.0..7.9 -> stringResource(Res.string.uv_index_high)
        in 8.0..10.9 -> stringResource(Res.string.uv_index_very_high)
        else -> stringResource(Res.string.uv_index_extreme)
    }
}

@Composable
fun MoonPhase.icon(): ImageVector = when (this) {
    MoonPhase.NEW_MOON -> vectorResource(Res.drawable.new_moon_indicator)
    MoonPhase.WAXING_CRESCENT -> vectorResource(Res.drawable.waxing_cescent_moon_indicator)
    MoonPhase.FIRST_QUARTER -> vectorResource(Res.drawable.first_quarter_moon_indicator)
    MoonPhase.WAXING_GIBBOUS -> vectorResource(Res.drawable.waxing_gibbous_moon_indicator)
    MoonPhase.FULL_MOON -> vectorResource(Res.drawable.full_moon_indicator)
    MoonPhase.WANING_GIBBOUS -> vectorResource(Res.drawable.waning_gibbous_moon_indicator)
    MoonPhase.LAST_QUARTER -> vectorResource(Res.drawable.third_quarter_moon_indicator)
    MoonPhase.WANING_CRESCENT -> vectorResource(Res.drawable.waning_crescent_moon_indicator)
}

@Composable
fun MoonPhase.name(): String = when (this) {
    MoonPhase.NEW_MOON -> stringResource(Res.string.moon_phase_new_moon)
    MoonPhase.WAXING_CRESCENT -> stringResource(Res.string.moon_phase_waxing_crescent)
    MoonPhase.FIRST_QUARTER -> stringResource(Res.string.moon_phase_first_quarter)
    MoonPhase.WAXING_GIBBOUS -> stringResource(Res.string.moon_phase_waxing_gibbous)
    MoonPhase.FULL_MOON -> stringResource(Res.string.moon_phase_full_moon)
    MoonPhase.WANING_GIBBOUS -> stringResource(Res.string.moon_phase_waning_gibbous)
    MoonPhase.LAST_QUARTER -> stringResource(Res.string.moon_phase_last_quarter)
    MoonPhase.WANING_CRESCENT -> stringResource(Res.string.moon_phase_waning_crescent)
}