package com.kronos.multiplatform.weatherapp.features.home.current_weather.content

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.kronos.multiplatform.weatherapp.components.AlertIndicator
import com.kronos.multiplatform.weatherapp.components.CurrentWeatherBigScreenCompactItem
import com.kronos.multiplatform.weatherapp.components.CurrentWeatherCompactItem
import com.kronos.multiplatform.weatherapp.components.CurrentWeatherItem
import com.kronos.multiplatform.weatherapp.components.CurrentWeatherLandscapeCompactItem
import com.kronos.multiplatform.weatherapp.components.DailyWeatherList
import com.kronos.multiplatform.weatherapp.components.HourlyItemIndicator
import com.kronos.multiplatform.weatherapp.components.WeatherIndicatorList
import com.kronos.multiplatform.weatherapp.components.icons.WeatherAppIcons
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.CloudsIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.CompassIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.MoonFallIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.MoonPhases
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.PressionIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.RainyIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.SnowflakeIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.SunIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.SunSunriseIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.VisibilityIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.WaterDropsIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.WindIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.moonphases.FirstQuarterMoonIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.moonphases.FullMoonIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.moonphases.NewMoonIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.moonphases.ThirdQuarterMoonIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.moonphases.WaningCrescentMoonIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.moonphases.WaningGibbousMoonIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.moonphases.WaxingCescentMoonIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.moonphases.WaxingGibbousMoonIndicator
import com.kronos.multiplatform.weatherapp.core.ui.components.BodyText
import com.kronos.multiplatform.weatherapp.core.ui.components.ComponentSize
import com.kronos.multiplatform.weatherapp.core.ui.components.ExpressiveBaseCardView
import com.kronos.multiplatform.weatherapp.core.ui.components.HeaderText
import com.kronos.multiplatform.weatherapp.core.ui.components.LabelText
import com.kronos.multiplatform.weatherapp.core.ui.components.TitleText
import com.kronos.multiplatform.weatherapp.components.maps.FixMapView
import com.kronos.multiplatform.weatherapp.components.maps.layers.MapLayerState
import com.kronos.multiplatform.weatherapp.components.maps.layers.MapLayerType
import com.kronos.multiplatform.weatherapp.components.maps.markers.MapMarker
import com.kronos.multiplatform.weatherapp.core.util.format
import com.kronos.multiplatform.weatherapp.core.util.formatDateTime
import com.kronos.multiplatform.weatherapp.data.mapper.mapCurrentSuggestions
import com.kronos.multiplatform.weatherapp.data.remote.ktor.UrlProvider
import com.kronos.multiplatform.weatherapp.device.screen_config.DeviceScreenConfiguration
import com.kronos.multiplatform.weatherapp.domain.model.DailyForecast
import com.kronos.multiplatform.weatherapp.domain.model.DayMoment
import com.kronos.multiplatform.weatherapp.domain.model.Hour
import com.kronos.multiplatform.weatherapp.domain.model.MeasureUnit
import com.kronos.multiplatform.weatherapp.domain.model.MoonPhase
import com.kronos.multiplatform.weatherapp.domain.model.SuggestionArg
import com.kronos.multiplatform.weatherapp.domain.model.SuggestionPriority
import com.kronos.multiplatform.weatherapp.domain.model.SuggestionType
import com.kronos.multiplatform.weatherapp.domain.model.UvIndexLevel
import com.kronos.multiplatform.weatherapp.domain.model.WeatherSuggestionModel
import com.kronos.multiplatform.weatherapp.domain.model.alerts.WeatherAlert
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast
import org.jetbrains.compose.resources.stringResource
import weather_app.composeapp.generated.resources.Res
import weather_app.composeapp.generated.resources.air_quality
import weather_app.composeapp.generated.resources.air_quality_danger_sensitive_group
import weather_app.composeapp.generated.resources.air_quality_good
import weather_app.composeapp.generated.resources.air_quality_hazardous
import weather_app.composeapp.generated.resources.air_quality_moderate
import weather_app.composeapp.generated.resources.air_quality_unhealthy
import weather_app.composeapp.generated.resources.air_quality_very_unhealthy
import weather_app.composeapp.generated.resources.alert_certainty
import weather_app.composeapp.generated.resources.alert_instructions
import weather_app.composeapp.generated.resources.alert_severity
import weather_app.composeapp.generated.resources.alert_urgency
import weather_app.composeapp.generated.resources.alert_validity
import weather_app.composeapp.generated.resources.atmospheric_pression
import weather_app.composeapp.generated.resources.close
import weather_app.composeapp.generated.resources.clouds
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
import weather_app.composeapp.generated.resources.rain
import weather_app.composeapp.generated.resources.snow
import weather_app.composeapp.generated.resources.speed_km
import weather_app.composeapp.generated.resources.speed_miles
import weather_app.composeapp.generated.resources.suggestion_cold_afternoon_message
import weather_app.composeapp.generated.resources.suggestion_cold_evening_message
import weather_app.composeapp.generated.resources.suggestion_cold_morning_message
import weather_app.composeapp.generated.resources.suggestion_cold_night_message
import weather_app.composeapp.generated.resources.suggestion_cold_title
import weather_app.composeapp.generated.resources.suggestion_heat_high_afternoon_message
import weather_app.composeapp.generated.resources.suggestion_heat_high_evening_message
import weather_app.composeapp.generated.resources.suggestion_heat_high_morning_message
import weather_app.composeapp.generated.resources.suggestion_heat_high_night_message
import weather_app.composeapp.generated.resources.suggestion_heat_high_title
import weather_app.composeapp.generated.resources.suggestion_heat_low_afternoon_message
import weather_app.composeapp.generated.resources.suggestion_heat_low_evening_message
import weather_app.composeapp.generated.resources.suggestion_heat_low_morning_message
import weather_app.composeapp.generated.resources.suggestion_heat_low_night_message
import weather_app.composeapp.generated.resources.suggestion_heat_low_title
import weather_app.composeapp.generated.resources.suggestion_heat_medium_afternoon_message
import weather_app.composeapp.generated.resources.suggestion_heat_medium_evening_message
import weather_app.composeapp.generated.resources.suggestion_heat_medium_morning_message
import weather_app.composeapp.generated.resources.suggestion_heat_medium_night_message
import weather_app.composeapp.generated.resources.suggestion_heat_medium_title
import weather_app.composeapp.generated.resources.suggestion_humidity_afternoon_message
import weather_app.composeapp.generated.resources.suggestion_humidity_evening_message
import weather_app.composeapp.generated.resources.suggestion_humidity_morning_message
import weather_app.composeapp.generated.resources.suggestion_humidity_night_message
import weather_app.composeapp.generated.resources.suggestion_humidity_title
import weather_app.composeapp.generated.resources.suggestion_morning_alert_title
import weather_app.composeapp.generated.resources.suggestion_morning_heat_high_message
import weather_app.composeapp.generated.resources.suggestion_morning_heat_medium_message
import weather_app.composeapp.generated.resources.suggestion_morning_normal_title
import weather_app.composeapp.generated.resources.suggestion_morning_rain_message
import weather_app.composeapp.generated.resources.suggestion_morning_uv_high_message
import weather_app.composeapp.generated.resources.suggestion_morning_uv_medium_message
import weather_app.composeapp.generated.resources.suggestion_morning_warning_title
import weather_app.composeapp.generated.resources.suggestion_rain_afternoon_message
import weather_app.composeapp.generated.resources.suggestion_rain_evening_message
import weather_app.composeapp.generated.resources.suggestion_rain_morning_message
import weather_app.composeapp.generated.resources.suggestion_rain_night_message
import weather_app.composeapp.generated.resources.suggestion_rain_title_afternoon
import weather_app.composeapp.generated.resources.suggestion_rain_title_evening
import weather_app.composeapp.generated.resources.suggestion_rain_title_morning
import weather_app.composeapp.generated.resources.suggestion_rain_title_night
import weather_app.composeapp.generated.resources.suggestion_tomorrow_alert_title
import weather_app.composeapp.generated.resources.suggestion_tomorrow_clear_message
import weather_app.composeapp.generated.resources.suggestion_tomorrow_clear_title
import weather_app.composeapp.generated.resources.suggestion_tomorrow_heat_message
import weather_app.composeapp.generated.resources.suggestion_tomorrow_rain_message
import weather_app.composeapp.generated.resources.suggestion_tomorrow_rain_title
import weather_app.composeapp.generated.resources.suggestion_tomorrow_uv_message
import weather_app.composeapp.generated.resources.suggestion_uv_high_afternoon_message
import weather_app.composeapp.generated.resources.suggestion_uv_high_evening_message
import weather_app.composeapp.generated.resources.suggestion_uv_high_morning_message
import weather_app.composeapp.generated.resources.suggestion_uv_high_night_message
import weather_app.composeapp.generated.resources.suggestion_uv_high_title
import weather_app.composeapp.generated.resources.suggestion_uv_medium_afternoon_message
import weather_app.composeapp.generated.resources.suggestion_uv_medium_evening_message
import weather_app.composeapp.generated.resources.suggestion_uv_medium_morning_message
import weather_app.composeapp.generated.resources.suggestion_uv_medium_night_message
import weather_app.composeapp.generated.resources.suggestion_uv_medium_title
import weather_app.composeapp.generated.resources.suggestion_visibility_afternoon_message
import weather_app.composeapp.generated.resources.suggestion_visibility_evening_message
import weather_app.composeapp.generated.resources.suggestion_visibility_morning_message
import weather_app.composeapp.generated.resources.suggestion_visibility_night_message
import weather_app.composeapp.generated.resources.suggestion_visibility_title
import weather_app.composeapp.generated.resources.suggestion_wind_afternoon_message
import weather_app.composeapp.generated.resources.suggestion_wind_evening_message
import weather_app.composeapp.generated.resources.suggestion_wind_morning_message
import weather_app.composeapp.generated.resources.suggestion_wind_night_message
import weather_app.composeapp.generated.resources.suggestion_wind_title
import weather_app.composeapp.generated.resources.sun
import weather_app.composeapp.generated.resources.unknow
import weather_app.composeapp.generated.resources.uv_index
import weather_app.composeapp.generated.resources.uv_index_extreme
import weather_app.composeapp.generated.resources.uv_index_high
import weather_app.composeapp.generated.resources.uv_index_low
import weather_app.composeapp.generated.resources.uv_index_medium
import weather_app.composeapp.generated.resources.uv_index_very_high
import weather_app.composeapp.generated.resources.visibility
import weather_app.composeapp.generated.resources.visibility_km
import weather_app.composeapp.generated.resources.visibility_miles
import weather_app.composeapp.generated.resources.wind
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Composable
private fun getWeatherIndicators(
    currentWeather: Forecast,
    currentDayForecast: DailyForecast?,
    measureUnit: MeasureUnit
): List<Indicator> {
    val windText = stringResource(Res.string.wind)
    val speedKmText = stringResource(Res.string.speed_km)
    val speedMpHText = stringResource(Res.string.speed_miles)
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
    val visibilityKmText = stringResource(Res.string.visibility_km)
    val visibilityMpHText = stringResource(Res.string.visibility_miles)
    val cloudText = stringResource(Res.string.clouds)
    val atmosphericPressionText = stringResource(Res.string.atmospheric_pression)
    val airQuality = stringResource(Res.string.air_quality)
    val airQualityText = airQualityIndexDescription(currentWeather.current.airQuality.usEpaIndex)

    return listOf(
        Indicator.Wind(
            1,
            windText,
            if (measureUnit == MeasureUnit.INTERNATIONAL)
                speedKmText.format(currentWeather.current.windSpeedKph)
            else
                speedMpHText.format(currentWeather.current.windSpeedMph),
            WeatherAppIcons.CompassIndicator,
            windDegree = currentWeather.current.windDegree.toFloat(),
        ),
        Indicator.Default(
            2,
            humidityText,
            "${currentWeather.current.humidity}%",
            WeatherAppIcons.WaterDropsIndicator
        ),
        Indicator.UVIndex(
            3,
            uvIndexText,
            uvDescription,
            WeatherAppIcons.SunIndicator,
            currentWeather.current.uv
        ),
        if (currentDayForecast?.day?.dailyWillItSnow == true) {
            Indicator.Default(
                4,
                snowText,
                "${currentDayForecast.day.totalsnowCm} cm",
                WeatherAppIcons.SnowflakeIndicator
            )
        } else {
            Indicator.Default(
                5,
                rainText,
                "${currentWeather.current.precipitationMm} mm",
                WeatherAppIcons.RainyIndicator
            )
        },
        if (currentWeather.current.isDay) {
            Indicator.Default(
                6,
                sunText,
                "${currentDayForecast?.astro?.sunrise ?: ""} - ${currentDayForecast?.astro?.sunset ?: ""}",
                WeatherAppIcons.SunSunriseIndicator
            )
        } else {
            Indicator.Default(
                6,
                moonPhaseName,
                "${currentDayForecast?.astro?.moonrise ?: ""} - ${currentDayForecast?.astro?.moonset ?: ""}",
                moonPhaseIcon
            )
        },
        Indicator.Default(
            7,
            visibilityText,

            if (measureUnit == MeasureUnit.INTERNATIONAL)
                visibilityKmText.format(currentWeather.current.visionKM)
            else
                visibilityMpHText.format(currentWeather.current.visionMiles),
            WeatherAppIcons.VisibilityIndicator
        ),
        Indicator.Default(
            8,
            cloudText,
            "${currentWeather.current.cloud}%",
            WeatherAppIcons.CloudsIndicator
        ),
        Indicator.Default(
            9,
            atmosphericPressionText,
            if (measureUnit == MeasureUnit.INTERNATIONAL)
                "${currentWeather.current.pressureMb} mbar"
            else
                "${currentWeather.current.pressureIn} inHg",
            WeatherAppIcons.PressionIndicator
        ),
        Indicator.Default(
            10,
            airQuality,
            airQualityText,
            WeatherAppIcons.WindIndicator
        )
    ).filter { it.description.isNotBlank() }
}

@Composable
fun WeatherContentPortrait(
    weather: Forecast,
    mapLayers: List<MapLayerState>,
    onLayerToggled: (MapLayerType) -> Unit,
    deviceScreenConfiguration: DeviceScreenConfiguration,
    isDarkTheme: Boolean,
    urlProvider: UrlProvider,
    imageQuality: String,
    currentLang: String,
    measureUnit: MeasureUnit,
    onHourItemClicked: (Hour) -> Unit,
    onAlertItemClicked: (WeatherAlert) -> Unit,
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
            measureUnit = measureUnit,
            modifier = Modifier.fillMaxWidth(),
            isCompactMode = actualCompactMode,
        )

        WeatherContentSection(
            currentWeather = weather,
            mapLayers = mapLayers,
            onLayerToggled = onLayerToggled,
            isDarkTheme = isDarkTheme,
            urlProvider = urlProvider,
            imageQuality = imageQuality,
            currentLang = currentLang,
            measureUnit = measureUnit,
            scrollState = scrollState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            onHourItemClicked = onHourItemClicked,
            onAlertItemClicked = onAlertItemClicked,
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
    measureUnit: MeasureUnit,
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
                measureUnit = measureUnit,
                modifier = Modifier.wrapContentHeight()
            )

            else -> CurrentWeatherItem(
                currentWeather = currentWeather,
                darkTheme = isDarkTheme,
                urlProvider = urlProvider,
                imageQuality = imageQuality,
                currentLang = currentLang,
                measureUnit = measureUnit,
                modifier = Modifier.wrapContentHeight()
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun WeatherContentSection(
    currentWeather: Forecast,
    mapLayers: List<MapLayerState>,
    onLayerToggled: (MapLayerType) -> Unit,
    isDarkTheme: Boolean,
    urlProvider: UrlProvider,
    imageQuality: String,
    currentLang: String,
    measureUnit: MeasureUnit,
    scrollState: LazyListState,
    modifier: Modifier = Modifier,
    onHourItemClicked: (Hour) -> Unit,
    onAlertItemClicked: (WeatherAlert) -> Unit,
    onDailyItemClicked: (DailyForecast) -> Unit,
    amountOfDays: Int = 3
) {
    val timeZone = currentWeather.location.tzId
    val currentDayForecast = currentWeather.getCurrentDayForecast(timeZone)

    val hours = currentDayForecast?.getUpcomingHours(timeZone).orEmpty()

    val alerts = currentWeather.alerts

    val futureDays = currentWeather.getFutureDays(amountOfDays)

    val indicators = getWeatherIndicators(currentWeather, currentDayForecast, measureUnit)

    val suggestions = currentWeather.mapCurrentSuggestions(timeZone, measureUnit)

    LazyColumn(
        state = scrollState,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
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
                            measureUnit = measureUnit,
                            darkTheme = isDarkTheme,
                            onItemClick = onHourItemClicked
                        )
                    }
                }
            }
        }

        if (alerts.isNotEmpty()) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .height(IntrinsicSize.Max)
                ) {
                    alerts.forEach { alert ->
                        AlertIndicator(
                            alert = alert,
                            darkTheme = isDarkTheme,
                            onItemClick = onAlertItemClicked,
                            modifier = Modifier.fillMaxHeight()
                        )
                    }
                }
            }
        }

        if (suggestions.isNotEmpty()) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .height(IntrinsicSize.Max)
                ) {
                    suggestions.forEach { suggestion ->
                        SuggestionCard(
                            locationName = currentWeather.location.name,
                            suggestion = suggestion,
                            isDarkTheme = isDarkTheme,
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
                        measureUnit = measureUnit,
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
                mapLayers = mapLayers,
                onLayerToggled = onLayerToggled,
                onMapClick = {},
                onMapLongClick = {},
                darkTheme = isDarkTheme,
            )
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun WeatherContentLandscape(
    weather: Forecast,
    mapLayers: List<MapLayerState>,
    onLayerToggled: (MapLayerType) -> Unit,
    isDarkTheme: Boolean,
    urlProvider: UrlProvider,
    imageQuality: String,
    currentLang: String,
    measureUnit: MeasureUnit,
    modifier: Modifier = Modifier,
    deviceScreenConfiguration: DeviceScreenConfiguration,
    onHourItemClicked: (Hour) -> Unit,
    onAlertItemClicked: (WeatherAlert) -> Unit,
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
                        measureUnit = measureUnit,
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
                        measureUnit = measureUnit,
                        modifier = Modifier.fillMaxWidth()
                    )

                else -> WeatherContentPortrait(
                    weather = weather,
                    mapLayers = mapLayers,
                    onLayerToggled = onLayerToggled,
                    deviceScreenConfiguration = deviceScreenConfiguration,
                    isDarkTheme = isDarkTheme,
                    urlProvider = urlProvider,
                    imageQuality = imageQuality,
                    currentLang = currentLang,
                    measureUnit = measureUnit,
                    onHourItemClicked = onHourItemClicked,
                    onAlertItemClicked = onAlertItemClicked,
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
                mapLayers = mapLayers,
                onLayerToggled = onLayerToggled,
                isDarkTheme = isDarkTheme,
                urlProvider = urlProvider,
                imageQuality = imageQuality,
                currentLang = currentLang,
                measureUnit = measureUnit,
                scrollState = rememberLazyListState(),
                modifier = Modifier.fillMaxSize(),
                onHourItemClicked = onHourItemClicked,
                onAlertItemClicked = onAlertItemClicked,
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
fun UvIndexLevel.toLocalizedString(): String = when (this) {
    UvIndexLevel.LOW -> stringResource(Res.string.uv_index_low)
    UvIndexLevel.MEDIUM -> stringResource(Res.string.uv_index_medium)
    UvIndexLevel.HIGH -> stringResource(Res.string.uv_index_high)
    UvIndexLevel.VERY_HIGH -> stringResource(Res.string.uv_index_very_high)
    UvIndexLevel.EXTREME -> stringResource(Res.string.uv_index_extreme)
}

@Composable
fun airQualityIndexDescription(index: Int): String {
    return when (index) {
        1 -> stringResource(Res.string.air_quality_good)
        2 -> stringResource(Res.string.air_quality_moderate)
        3 -> stringResource(Res.string.air_quality_danger_sensitive_group)
        4 -> stringResource(Res.string.air_quality_unhealthy)
        5 -> stringResource(Res.string.air_quality_very_unhealthy)
        6 -> stringResource(Res.string.air_quality_hazardous)
        else -> stringResource(Res.string.unknow)
    }
}

@Composable
fun MoonPhase.icon(): ImageVector = when (this) {
    MoonPhase.NEW_MOON -> WeatherAppIcons.MoonPhases.NewMoonIndicator
    MoonPhase.WAXING_CRESCENT -> WeatherAppIcons.MoonPhases.WaxingCescentMoonIndicator
    MoonPhase.FIRST_QUARTER -> WeatherAppIcons.MoonPhases.FirstQuarterMoonIndicator
    MoonPhase.WAXING_GIBBOUS -> WeatherAppIcons.MoonPhases.WaxingGibbousMoonIndicator
    MoonPhase.FULL_MOON -> WeatherAppIcons.MoonPhases.FullMoonIndicator
    MoonPhase.WANING_GIBBOUS -> WeatherAppIcons.MoonPhases.WaningGibbousMoonIndicator
    MoonPhase.LAST_QUARTER -> WeatherAppIcons.MoonPhases.ThirdQuarterMoonIndicator
    MoonPhase.WANING_CRESCENT -> WeatherAppIcons.MoonPhases.WaningCrescentMoonIndicator
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

@Composable
fun ShowCityInfoDialog(
    cityName: String,
    temp: String,
    showDialog: Boolean,
    confirmText: String,
    onConfirm: () -> Unit,
    cancelText: String,
    onCancel: () -> Unit,
    onClose: (() -> Unit)? = null
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                if (onClose != null)
                    onClose()
                else
                    onCancel()
            },
            title = {},
            text = {
                Column {
                    HeaderText(
                        temp,
                        size = ComponentSize.SMALL
                    )

                    TitleText(
                        cityName,
                        size = ComponentSize.MEDIUM
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { onConfirm() }) {
                    Text(
                        text = confirmText,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { onCancel() }) {
                    Text(
                        text = cancelText,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            shape = MaterialTheme.shapes.medium
        )
    }
}

@Composable
fun ShowSelectedCityInfoDialog(
    cityName: String,
    temp: String,
    iconUrl: String,
    showDialog: Boolean,
    confirmText: String,
    onConfirm: () -> Unit,
    onClose: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onClose,
            title = {
                TitleText(
                    text = cityName,
                    fontWeight = Bold
                )
            },
            text = {
                ExpressiveBaseCardView(
                    modifier = Modifier.fillMaxWidth(),
                    cardBackgroundColor = MaterialTheme.colorScheme.surfaceContainer,
                    elevation = 0.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        if (iconUrl.isNotEmpty()) {
                            val imageRequest = ImageRequest.Builder(LocalPlatformContext.current)
                                .data(iconUrl)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .build()
                            AsyncImage(
                                model = imageRequest,
                                contentDescription = "Weather",
                                modifier = Modifier.size(64.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        TitleText(text = temp, fontWeight = Bold, size = ComponentSize.MEDIUM)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onConfirm() }) {
                    Text(
                        text = confirmText,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            dismissButton = null
        )
    }
}


@OptIn(ExperimentalTime::class)
@Composable
fun ShowAlertInfoDialog(
    alert: WeatherAlert?,
    showDialog: Boolean,
    onClose: () -> Unit,
    isDarkTheme: Boolean
) {
    if (!showDialog) return

    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BodyText(
                    text = alert?.headline.orEmpty(),
                    size = ComponentSize.MEDIUM,
                    fontWeight = Bold,
                    isDarkTheme = isDarkTheme
                )

                alert?.event?.takeIf { it.isNotBlank() }?.let {
                    TitleText(
                        text = it,
                        size = ComponentSize.SMALL,
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(
                        rememberScrollState()
                    ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    alert?.severity?.takeIf { it.isNotBlank() }?.let {
                        LabelText(
                            text = "${stringResource(Res.string.alert_severity)}\n$it",
                            isDarkTheme = isDarkTheme
                        )
                    }
                    alert?.urgency?.takeIf { it.isNotBlank() }?.let {
                        LabelText(
                            text = "${stringResource(Res.string.alert_urgency)}\n$it",
                            isDarkTheme = isDarkTheme
                        )
                    }
                    alert?.certainty?.takeIf { it.isNotBlank() }?.let {
                        LabelText(
                            text = "${stringResource(Res.string.alert_certainty)}\n$it",
                            isDarkTheme = isDarkTheme
                        )
                    }
                }

                alert?.areas?.takeIf { it.isNotBlank() }?.let {
                    val areas = alert.areas
                        .split(";")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        maxItemsInEachRow = 3,
                        horizontalArrangement = Arrangement.spacedBy(1.dp),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        areas.forEach { area ->
                            AssistChip(
                                onClick = { },
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = Color.White
                                ),
                                label = {
                                    LabelText(
                                        text = area,
                                        textColor = Color.White,
                                        size = ComponentSize.EXTRA_SMALL,
                                        fontWeight = Bold
                                    )
                                }
                            )
                        }
                    }
                }

                alert?.description?.takeIf { it.isNotBlank() }?.let {
                    BodyText(
                        text = it,
                        fontWeight = FontWeight.Medium,
                        isDarkTheme = isDarkTheme
                    )
                }

                alert?.instruction?.takeIf { it.isNotBlank() }?.let {
                    BodyText(
                        text = "${stringResource(Res.string.alert_instructions)}$it",
                        fontWeight = FontWeight.Medium,
                        isDarkTheme = isDarkTheme
                    )
                }

                if (!alert?.effective.isNullOrBlank() && !alert.expires.isNullOrBlank()) {
                    val from =
                        formatDateTime(Instant.parse(alert.effective), "dd-MMM hh:mm aa")
                    val until =
                        formatDateTime(Instant.parse(alert.expires), "dd-MMM hh:mm aa")
                    LabelText(
                        text = stringResource(Res.string.alert_validity).format(from, until),
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onClose() }) {
                Text(
                    text = stringResource(Res.string.close),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
    )
}

@Composable
fun SuggestionCard(
    locationName: String,
    suggestion: WeatherSuggestionModel,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val backgroundColor = when (suggestion.priority) {
        SuggestionPriority.HIGH -> Color(0xFFB71C1C)
        SuggestionPriority.MEDIUM -> Color(0xFFE65100)
        SuggestionPriority.LOW -> Color(0xFF1565C0)
    }

    ExpressiveBaseCardView(
        modifier = modifier.widthIn(max = 250.dp),
        cardBackgroundColor = backgroundColor,
        elevation = 2.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                BodyText(
                    text = suggestion.resolveTitle(locationName),
                    size = ComponentSize.MEDIUM,
                    fontWeight = Bold
                )
                LabelText(
                    text = suggestion.resolveMessage(),
                    size = ComponentSize.LARGE,
                )
            }
        }
    }
}

@Composable
fun WeatherSuggestionModel.resolveTitle(locationName: String): String = when (type) {

        // 🌧️ RAIN
        SuggestionType.RAIN -> when (moment) {
            DayMoment.MORNING   -> stringResource(Res.string.suggestion_rain_title_morning)
            DayMoment.AFTERNOON -> stringResource(Res.string.suggestion_rain_title_afternoon)
            DayMoment.EVENING   -> stringResource(Res.string.suggestion_rain_title_evening)
            DayMoment.NIGHT     -> stringResource(Res.string.suggestion_rain_title_night)
        }

        // ☀️ UV
        SuggestionType.UV -> when (priority) {
            SuggestionPriority.HIGH -> stringResource(Res.string.suggestion_uv_high_title)
            else -> stringResource(Res.string.suggestion_uv_medium_title)
        }

        // 🌡️ HEAT
        SuggestionType.HEAT -> when (priority) {
            SuggestionPriority.HIGH ->
                stringResource(Res.string.suggestion_heat_high_title)

            SuggestionPriority.MEDIUM ->
                stringResource(Res.string.suggestion_heat_medium_title)

            else ->
                stringResource(Res.string.suggestion_heat_low_title)
        }

        // ❄️ COLD
        SuggestionType.COLD ->
            stringResource(Res.string.suggestion_cold_title)

        // 💨 WIND
        SuggestionType.WIND ->
            stringResource(Res.string.suggestion_wind_title)

        // 💧 HUMIDITY
        SuggestionType.HUMIDITY ->
            stringResource(Res.string.suggestion_humidity_title)

        // 🌫️ VISIBILITY
        SuggestionType.VISIBILITY ->
            stringResource(Res.string.suggestion_visibility_title)

        // 🌤️ TOMORROW
        SuggestionType.TOMORROW_FORECAST -> when (priority) {

            SuggestionPriority.HIGH ->
                stringResource(Res.string.suggestion_tomorrow_rain_title)

            SuggestionPriority.MEDIUM ->
                stringResource(Res.string.suggestion_tomorrow_alert_title)
                    .format(locationName)

            else ->
                stringResource(Res.string.suggestion_tomorrow_clear_title)
        }

        // 🌅 MORNING SUMMARY
        SuggestionType.MORNING_SUMMARY -> when (priority) {

            SuggestionPriority.HIGH ->
                stringResource(Res.string.suggestion_morning_alert_title)

            SuggestionPriority.MEDIUM ->
                stringResource(Res.string.suggestion_morning_warning_title)

            else ->
                stringResource(Res.string.suggestion_morning_normal_title)
        }
    }


@Composable
fun WeatherSuggestionModel.resolveMessage(): String {
    val template = when (type) {

        SuggestionType.RAIN -> when (moment) {
            DayMoment.MORNING   -> stringResource(Res.string.suggestion_rain_morning_message)
            DayMoment.AFTERNOON -> stringResource(Res.string.suggestion_rain_afternoon_message)
            DayMoment.EVENING   -> stringResource(Res.string.suggestion_rain_evening_message)
            DayMoment.NIGHT     -> stringResource(Res.string.suggestion_rain_night_message)
        }

        SuggestionType.UV -> when (priority) {
            SuggestionPriority.HIGH -> when (moment) {
                DayMoment.MORNING   -> stringResource(Res.string.suggestion_uv_high_morning_message)
                DayMoment.AFTERNOON -> stringResource(Res.string.suggestion_uv_high_afternoon_message)
                DayMoment.EVENING   -> stringResource(Res.string.suggestion_uv_high_evening_message)
                DayMoment.NIGHT     -> stringResource(Res.string.suggestion_uv_high_night_message)
            }
            else -> when (moment) {
                DayMoment.MORNING   -> stringResource(Res.string.suggestion_uv_medium_morning_message)
                DayMoment.AFTERNOON -> stringResource(Res.string.suggestion_uv_medium_afternoon_message)
                DayMoment.EVENING   -> stringResource(Res.string.suggestion_uv_medium_evening_message)
                DayMoment.NIGHT     -> stringResource(Res.string.suggestion_uv_medium_night_message)
            }
        }

        SuggestionType.HEAT -> when (priority) {
            SuggestionPriority.HIGH -> when (moment) {
                DayMoment.MORNING   -> stringResource(Res.string.suggestion_heat_high_morning_message)
                DayMoment.AFTERNOON -> stringResource(Res.string.suggestion_heat_high_afternoon_message)
                DayMoment.EVENING   -> stringResource(Res.string.suggestion_heat_high_evening_message)
                DayMoment.NIGHT     -> stringResource(Res.string.suggestion_heat_high_night_message)
            }
            SuggestionPriority.MEDIUM -> when (moment) {
                DayMoment.MORNING   -> stringResource(Res.string.suggestion_heat_medium_morning_message)
                DayMoment.AFTERNOON -> stringResource(Res.string.suggestion_heat_medium_afternoon_message)
                DayMoment.EVENING   -> stringResource(Res.string.suggestion_heat_medium_evening_message)
                DayMoment.NIGHT     -> stringResource(Res.string.suggestion_heat_medium_night_message)
            }
            else -> when (moment) {
                DayMoment.MORNING   -> stringResource(Res.string.suggestion_heat_low_morning_message)
                DayMoment.AFTERNOON -> stringResource(Res.string.suggestion_heat_low_afternoon_message)
                DayMoment.EVENING   -> stringResource(Res.string.suggestion_heat_low_evening_message)
                DayMoment.NIGHT     -> stringResource(Res.string.suggestion_heat_low_night_message)
            }
        }

        SuggestionType.COLD -> when (moment) {
            DayMoment.MORNING   -> stringResource(Res.string.suggestion_cold_morning_message)
            DayMoment.AFTERNOON -> stringResource(Res.string.suggestion_cold_afternoon_message)
            DayMoment.EVENING   -> stringResource(Res.string.suggestion_cold_evening_message)
            DayMoment.NIGHT     -> stringResource(Res.string.suggestion_cold_night_message)
        }

        SuggestionType.WIND -> when (moment) {
            DayMoment.MORNING   -> stringResource(Res.string.suggestion_wind_morning_message)
            DayMoment.AFTERNOON -> stringResource(Res.string.suggestion_wind_afternoon_message)
            DayMoment.EVENING   -> stringResource(Res.string.suggestion_wind_evening_message)
            DayMoment.NIGHT     -> stringResource(Res.string.suggestion_wind_night_message)
        }

        SuggestionType.HUMIDITY -> when (moment) {
            DayMoment.MORNING   -> stringResource(Res.string.suggestion_humidity_morning_message)
            DayMoment.AFTERNOON -> stringResource(Res.string.suggestion_humidity_afternoon_message)
            DayMoment.EVENING   -> stringResource(Res.string.suggestion_humidity_evening_message)
            DayMoment.NIGHT     -> stringResource(Res.string.suggestion_humidity_night_message)
        }

        SuggestionType.VISIBILITY -> when (moment) {
            DayMoment.MORNING   -> stringResource(Res.string.suggestion_visibility_morning_message)
            DayMoment.AFTERNOON -> stringResource(Res.string.suggestion_visibility_afternoon_message)
            DayMoment.EVENING   -> stringResource(Res.string.suggestion_visibility_evening_message)
            DayMoment.NIGHT     -> stringResource(Res.string.suggestion_visibility_night_message)
        }

        SuggestionType.TOMORROW_FORECAST -> when (priority) {
            SuggestionPriority.HIGH   -> stringResource(Res.string.suggestion_tomorrow_rain_message)
            SuggestionPriority.MEDIUM -> when (icon) {
                "🧴" -> stringResource(Res.string.suggestion_tomorrow_uv_message)
                else -> stringResource(Res.string.suggestion_tomorrow_heat_message)
            }
            else -> stringResource(Res.string.suggestion_tomorrow_clear_message)
        }

        SuggestionType.MORNING_SUMMARY -> when (icon) {
            "🌂" -> stringResource(Res.string.suggestion_morning_rain_message)
            "🧴" -> stringResource(Res.string.suggestion_morning_uv_high_message)
            "😎" -> stringResource(Res.string.suggestion_morning_uv_medium_message)
            "💧" -> stringResource(Res.string.suggestion_morning_heat_high_message)
            else -> stringResource(Res.string.suggestion_morning_heat_medium_message)
        }
    }

    val resolvedArgs = args.map { arg ->
        when (arg) {
            is SuggestionArg.Temperature -> "${arg.value}"
            is SuggestionArg.Percentage  -> "${arg.value}"
            is SuggestionArg.WindSpeed   -> "${arg.value}"
            is SuggestionArg.Distance    -> "${arg.value}"
            is SuggestionArg.Text        -> arg.value
            is SuggestionArg.Uv         -> arg.level.toLocalizedString()
        }
    }

    return resolvedArgs.foldIndexed(template) { index, acc, arg ->
        acc.replace("%${index + 1}\$s", arg)
    }
}

@Preview(showBackground = true, heightDp = 600, widthDp = 800)
@Composable
fun SuggestionsAllCombinationsPreview() {

    val allSuggestions = buildList {

        val moments = DayMoment.values()
        val priorities = SuggestionPriority.values()

        // 🌧️ RAIN (usa porcentaje)
        moments.forEach { moment ->
            add(
                WeatherSuggestionModel(
                    type = SuggestionType.RAIN,
                    priority = SuggestionPriority.HIGH,
                    moment = moment,
                    args = listOf(SuggestionArg.Percentage(75)),
                    icon = "🌧️"
                )
            )
        }

        // ☀️ UV (usa prioridad + momento)
        moments.forEach { moment ->
            priorities.forEach { priority ->
                add(
                    WeatherSuggestionModel(
                        type = SuggestionType.UV,
                        priority = priority,
                        moment = moment,
                        args = listOf(SuggestionArg.Uv(UvIndexLevel.HIGH)),
                        icon = "☀️"
                    )
                )
            }
        }

        // 🌡️ HEAT
        moments.forEach { moment ->
            priorities.forEach { priority ->
                add(
                    WeatherSuggestionModel(
                        type = SuggestionType.HEAT,
                        priority = priority,
                        moment = moment,
                        args = listOf(SuggestionArg.Temperature(34)),
                        icon = "🥵"
                    )
                )
            }
        }

        // ❄️ COLD
        moments.forEach { moment ->
            add(
                WeatherSuggestionModel(
                    type = SuggestionType.COLD,
                    priority = SuggestionPriority.LOW,
                    moment = moment,
                    args = listOf(SuggestionArg.Temperature(10)),
                    icon = "🧥"
                )
            )
        }

        // 💨 WIND
        moments.forEach { moment ->
            add(
                WeatherSuggestionModel(
                    type = SuggestionType.WIND,
                    priority = SuggestionPriority.MEDIUM,
                    moment = moment,
                    args = listOf(SuggestionArg.WindSpeed(35)),
                    icon = "💨"
                )
            )
        }

        // 💧 HUMIDITY (sin args)
        moments.forEach { moment ->
            add(
                WeatherSuggestionModel(
                    type = SuggestionType.HUMIDITY,
                    priority = SuggestionPriority.MEDIUM,
                    moment = moment,
                    args = emptyList(),
                    icon = "💧"
                )
            )
        }

        // 🌫️ VISIBILITY
        moments.forEach { moment ->
            add(
                WeatherSuggestionModel(
                    type = SuggestionType.VISIBILITY,
                    priority = SuggestionPriority.HIGH,
                    moment = moment,
                    args = listOf(SuggestionArg.Distance(3)),
                    icon = "🌫️"
                )
            )
        }

        // 🌤️ TOMORROW
        priorities.forEach { priority ->
            add(
                WeatherSuggestionModel(
                    type = SuggestionType.TOMORROW_FORECAST,
                    priority = priority,
                    moment = DayMoment.MORNING,
                    args = listOf(
                        SuggestionArg.Percentage(60),
                        SuggestionArg.Temperature(30),
                        SuggestionArg.Temperature(24)
                    ),
                    icon = "🌤️"
                )
            )
        }

        // 🌅 MORNING SUMMARY
        priorities.forEach { priority ->
            add(
                WeatherSuggestionModel(
                    type = SuggestionType.MORNING_SUMMARY,
                    priority = priority,
                    moment = DayMoment.MORNING,
                    args = listOf(SuggestionArg.Temperature(32)),
                    icon = "🌡️"
                )
            )
        }
    }

    val locationName = "Panama City"

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {

        allSuggestions.chunked(3).forEach { rowItems ->

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .height(IntrinsicSize.Max)
            ) {
                rowItems.forEach { suggestion ->
                    SuggestionCard(
                        locationName = locationName,
                        suggestion = suggestion,
                        isDarkTheme = false
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ShowCityDialogPreview() {
    ShowCityInfoDialog(
        cityName = "Panama",
        temp = "30°C",
        showDialog = true,
        confirmText = "OK",
        cancelText = "Cancel",
        onConfirm = {},
        onCancel = {}
    )
}

@Preview
@Composable
fun ShowSelectedCityDialogPreview() {
    ShowSelectedCityInfoDialog(
        cityName = "Panama",
        temp = "30°C",
        iconUrl = "",
        showDialog = true,
        confirmText = "Select",
        onConfirm = {},
        onClose = {}
    )
}
