package com.kronos.multiplatform.weatherapp.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.kronos.multiplatform.weatherapp.components.icons.WeatherAppIcons
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.TempIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.WaterDropsIndicator
import com.kronos.multiplatform.weatherapp.components.theme.extendedDark
import com.kronos.multiplatform.weatherapp.components.theme.extendedLight
import com.kronos.multiplatform.weatherapp.core.util.format
import com.kronos.multiplatform.weatherapp.core.util.formatDateTime
import com.kronos.multiplatform.weatherapp.core.util.getHour
import com.kronos.multiplatform.weatherapp.core.util.isToday
import com.kronos.multiplatform.weatherapp.core.util.isTomorrow
import com.kronos.multiplatform.weatherapp.core.util.of
import com.kronos.multiplatform.weatherapp.core.util.toDayOfWeekText
import com.kronos.multiplatform.weatherapp.data.remote.ktor.UrlProvider
import com.kronos.multiplatform.weatherapp.domain.model.DailyForecast
import com.kronos.multiplatform.weatherapp.domain.model.Hour
import com.kronos.multiplatform.weatherapp.domain.model.UserCustomLocation
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast
import com.kronos.multiplatform.weatherapp.features.home.current_weather.content.Indicator
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.stringResource
import weather_app.composeapp.generated.resources.Res
import weather_app.composeapp.generated.resources.feels_like_temp_celsius
import weather_app.composeapp.generated.resources.friday
import weather_app.composeapp.generated.resources.gps
import weather_app.composeapp.generated.resources.location_name
import weather_app.composeapp.generated.resources.monday
import weather_app.composeapp.generated.resources.saturday
import weather_app.composeapp.generated.resources.sunday
import weather_app.composeapp.generated.resources.temp_celsius
import weather_app.composeapp.generated.resources.thursday
import weather_app.composeapp.generated.resources.today
import weather_app.composeapp.generated.resources.tomorrow
import weather_app.composeapp.generated.resources.tuesday
import weather_app.composeapp.generated.resources.wednesday
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Composable
fun HourlyItemIndicator(
    item: Hour,
    urlProvider: UrlProvider,
    imageQuality: String,
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
    onItemClick: (Hour) -> Unit,
) {
    val cardBackgroundColor = if (darkTheme) {
        extendedDark.backgroundCardColor.color
    } else {
        extendedLight.backgroundCardColor.color
    }

    Card(
        modifier = modifier
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = { onItemClick(item) }
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {

            val date = Instant.of(item.time, true)

            val hour = date?.getHour() ?: ""

            LabelText(
                text = hour,
                modifier = Modifier.wrapContentSize(),
                textColor = Color.White,
                size = ComponentSize.MEDIUM
            )

            val imageRequest = ImageRequest.Builder(LocalPlatformContext.current)
                .data(urlProvider.getImageUrl(item.condition.icon, imageQuality))
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()

            AsyncImage(
                model = imageRequest,
                contentDescription = "weather",
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.Transparent),
                contentScale = ContentScale.Fit
            )

            LabelText(
                text = item.tempC.toString(),
                modifier = Modifier.wrapContentSize(),
                textColor = Color.White,
                size = ComponentSize.MEDIUM,
                fontWeight = Bold
            )
        }
    }
}

@Composable
fun WeatherIndicatorItem(
    item: Indicator,
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .wrapContentWidth()
            .padding(horizontal = 5.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = item.image,
            contentDescription = "weather",
            tint = Color.Unspecified,
            modifier = Modifier.size(48.dp),
        )

        LabelText(
            item.header,
            textColor = Color.White,
            textAlign = TextAlign.Center,
            size = ComponentSize.MEDIUM,
            modifier = Modifier.wrapContentWidth()
        )

        LabelText(
            item.description,
            textColor = Color.White,
            textAlign = TextAlign.Center,
            size = ComponentSize.SMALL,
            fontWeight = Bold,
            modifier = Modifier.wrapContentWidth()
        )
    }
}

@Composable
fun WindCompassIndicator(
    item: Indicator.Wind,
    modifier: Modifier = Modifier,
    darkTheme: Boolean
) {
    val animatedRotation by animateFloatAsState(
        targetValue = item.windDegree,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "wind-rotation"
    )
    Column(
        modifier = modifier
            .wrapContentWidth()
            .padding(horizontal = 5.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CompassView(
            modifier = Modifier.size(48.dp),
            rotation = animatedRotation,
            darkTheme = darkTheme
        )
        LabelText(
            item.header,
            textColor = Color.White,
            textAlign = TextAlign.Center,
            size = ComponentSize.MEDIUM,
            modifier = Modifier.wrapContentWidth()
        )
        LabelText(
            item.description,
            textColor = Color.White,
            textAlign = TextAlign.Center,
            fontWeight = Bold,
            size = ComponentSize.EXTRA_SMALL,
            modifier = Modifier.wrapContentWidth()
        )
    }
}

@Composable
fun UvIndexProgressIndicator(
    item: Indicator.UVIndex,
    modifier: Modifier = Modifier,
    darkTheme: Boolean
) {
    Column(
        modifier = modifier
            .wrapContentWidth()
            .padding(horizontal = 5.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedSemiCircularProgress(
            item.level.toFloat(),
            modifier = Modifier.size(48.dp),
        )
        LabelText(
            item.header,
            textColor = Color.White,
            textAlign = TextAlign.Center,
            size = ComponentSize.MEDIUM,
            modifier = Modifier.wrapContentWidth()
        )
        LabelText(
            item.description,
            textColor = Color.White,
            textAlign = TextAlign.Center,
            fontWeight = Bold,
            size = ComponentSize.EXTRA_SMALL,
            modifier = Modifier.wrapContentWidth()
        )
    }
}

@Composable
fun WeatherIndicatorList(
    indicators: List<Indicator>,
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
) {
    val cardBackgroundColor = if (darkTheme) {
        extendedDark.backgroundCardColor.color
    } else {
        extendedLight.backgroundCardColor.color
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        FlowRow(
            modifier = Modifier.padding(4.dp),
            maxItemsInEachRow = 3,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            indicators.forEach {
                when (it) {
                    is Indicator.UVIndex -> {
                        UvIndexProgressIndicator(
                            it,
                            darkTheme = darkTheme,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    is Indicator.Wind -> {
                        WindCompassIndicator(
                            it,
                            darkTheme = darkTheme,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    is Indicator.Default -> {
                        WeatherIndicatorItem(
                            item = it,
                            darkTheme = darkTheme,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun CurrentWeatherItem(
    currentWeather: Forecast,
    darkTheme: Boolean,
    urlProvider: UrlProvider,
    imageQuality: String,
    currentLang: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        HeaderText(
            currentWeather.current.condition.description,
            textColor = Color.White,
            textAlign = TextAlign.Center,
            size = ComponentSize.SMALL
        )

        val imageRequest = ImageRequest.Builder(LocalPlatformContext.current)
            .data(urlProvider.getImageUrl(currentWeather.current.condition.icon, imageQuality))
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()

        AsyncImage(
            model = imageRequest,
            contentDescription = "weather",
            modifier = Modifier
                .background(Color.Transparent)
                .size(128.dp),
            contentScale = ContentScale.Crop
        )

        val date = Instant.of(currentWeather.location.localtime, true)

        val stringDate = formatDateTime(date!!, "EEE MMM d | h:mm aa", currentLang)

        BodyText(
            stringDate,
            textColor = Color.White,
            size = ComponentSize.MEDIUM
        )

        DisplayText(
            stringResource(Res.string.temp_celsius).format(
                currentWeather.current.tempC,
            ),
            textColor = Color.White,
            size = ComponentSize.SMALL
        )

        BodyText(
            stringResource(Res.string.feels_like_temp_celsius).format(
                currentWeather.current.feelslikeC,
            ),
            textColor = Color.White,
            size = ComponentSize.MEDIUM
        )

        BodyText(
            stringResource(Res.string.location_name).format(
                currentWeather.location.country,
                currentWeather.location.name
            ),
            textColor = Color.White,
            size = ComponentSize.MEDIUM
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun CurrentWeatherCompactItem(
    currentWeather: Forecast,
    darkTheme: Boolean,
    urlProvider: UrlProvider,
    imageQuality: String,
    currentLang: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            val imageRequest = ImageRequest.Builder(LocalPlatformContext.current)
                .data(urlProvider.getImageUrl(currentWeather.current.condition.icon, imageQuality))
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()

            AsyncImage(
                model = imageRequest,
                contentDescription = "weather",
                modifier = Modifier
                    .background(Color.Transparent)
                    .size(96.dp),
                contentScale = ContentScale.Crop
            )

            TitleText(
                currentWeather.current.condition.description,
                textColor = Color.White,
                textAlign = TextAlign.Start,
                size = ComponentSize.LARGE,
                fontWeight = Bold
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Top
        ) {
            val date = Instant.of(currentWeather.location.localtime, true)

            val stringDate = formatDateTime(date!!, "EEE MMM d | h:mm aa", currentLang)

            BodyText(
                stringDate,
                textColor = Color.White,
                size = ComponentSize.SMALL,
                textAlign = TextAlign.End
            )

            HeaderText(
                stringResource(Res.string.temp_celsius).format(
                    currentWeather.current.tempC,
                ),
                textColor = Color.White,
                size = ComponentSize.MEDIUM,
                textAlign = TextAlign.End
            )

            BodyText(
                stringResource(Res.string.feels_like_temp_celsius).format(
                    currentWeather.current.feelslikeC,
                ),
                textColor = Color.White,
                size = ComponentSize.SMALL,
                textAlign = TextAlign.End
            )

            BodyText(
                stringResource(Res.string.location_name).format(
                    currentWeather.location.country,
                    currentWeather.location.name
                ),
                textColor = Color.White,
                size = ComponentSize.SMALL,
                textAlign = TextAlign.End,
                maxLines = 1,
                textOverflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun CurrentWeatherLandscapeCompactItem(
    currentWeather: Forecast,
    darkTheme: Boolean,
    urlProvider: UrlProvider,
    imageQuality: String,
    currentLang: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val imageRequest = ImageRequest.Builder(LocalPlatformContext.current)
            .data(urlProvider.getImageUrl(currentWeather.current.condition.icon, imageQuality))
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()

        AsyncImage(
            model = imageRequest,
            contentDescription = "weather",
            modifier = Modifier
                .background(Color.Transparent)
                .size(96.dp),
            contentScale = ContentScale.Crop
        )

        HeaderText(
            currentWeather.current.condition.description,
            textColor = Color.White,
            textAlign = TextAlign.Center,
            size = ComponentSize.SMALL,
            fontWeight = Bold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {

                val date = Instant.of(currentWeather.location.localtime, true)

                val stringDate = formatDateTime(date!!, "EEE MMM d | h:mm aa", currentLang)

                BodyText(
                    stringDate,
                    textColor = Color.White,
                    size = ComponentSize.SMALL,
                    textAlign = TextAlign.Start
                )

                BodyText(
                    stringResource(Res.string.location_name).format(
                        currentWeather.location.country,
                        currentWeather.location.name
                    ),
                    textColor = Color.White,
                    size = ComponentSize.SMALL,
                    textAlign = TextAlign.Start,
                    maxLines = 1,
                    textOverflow = TextOverflow.Ellipsis
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {

                HeaderText(
                    stringResource(Res.string.temp_celsius).format(
                        currentWeather.current.tempC,
                    ),
                    textColor = Color.White,
                    size = ComponentSize.SMALL,
                    textAlign = TextAlign.End
                )

                BodyText(
                    stringResource(Res.string.feels_like_temp_celsius).format(
                        currentWeather.current.feelslikeC,
                    ),
                    textColor = Color.White,
                    size = ComponentSize.SMALL,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun CurrentWeatherBigScreenCompactItem(
    currentWeather: Forecast,
    darkTheme: Boolean,
    urlProvider: UrlProvider,
    imageQuality: String,
    currentLang: String,
    modifier: Modifier = Modifier,
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val imageRequest = ImageRequest.Builder(LocalPlatformContext.current)
            .data(urlProvider.getImageUrl(currentWeather.current.condition.icon, imageQuality))
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()

        AsyncImage(
            model = imageRequest,
            contentDescription = "weather",
            modifier = Modifier
                .background(Color.Transparent)
                .size(256.dp),
            contentScale = ContentScale.Crop
        )

        HeaderText(
            currentWeather.current.condition.description,
            textColor = Color.White,
            textAlign = TextAlign.Center,
            size = ComponentSize.MEDIUM,
            fontWeight = Bold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {

                val date = Instant.of(currentWeather.location.localtime, true)

                val stringDate = formatDateTime(date!!, "EEE MMM d | h:mm aa", currentLang)

                TitleText(
                    stringDate,
                    textColor = Color.White,
                    size = ComponentSize.MEDIUM,
                    textAlign = TextAlign.Start
                )

                TitleText(
                    stringResource(Res.string.location_name).format(
                        currentWeather.location.country,
                        currentWeather.location.name
                    ),
                    textColor = Color.White,
                    size = ComponentSize.MEDIUM,
                    textAlign = TextAlign.Start,
                    maxLines = 1,
                    textOverflow = TextOverflow.Ellipsis
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {

                DisplayText(
                    stringResource(Res.string.temp_celsius).format(
                        currentWeather.current.tempC,
                    ),
                    textColor = Color.White,
                    size = ComponentSize.SMALL,
                    textAlign = TextAlign.End
                )

                TitleText(
                    stringResource(Res.string.feels_like_temp_celsius).format(
                        currentWeather.current.feelslikeC,
                    ),
                    textColor = Color.White,
                    size = ComponentSize.MEDIUM,
                    textAlign = TextAlign.End
                )

            }
        }
    }
}


@OptIn(ExperimentalTime::class)
@Composable
fun DailyWeatherItemIndicator(
    item: DailyForecast,
    urlProvider: UrlProvider,
    imageQuality: String,
    currentLang: String,
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
    onItemClick: (DailyForecast) -> Unit,
) {
    val cardBackgroundColor = if (darkTheme) {
        extendedDark.backgroundCardColor.color
    } else {
        extendedLight.backgroundCardColor.color
    }

    Card(
        modifier = modifier
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = {
            onItemClick(item)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                val date = Instant.of(item.date, false)
                val dayOfWeek = if (date != null) {
                    if (date.isToday()) {
                        stringResource(Res.string.today)
                    } else if (date.isTomorrow()) {
                        stringResource(Res.string.tomorrow)
                    } else {
                        when (date.toDayOfWeekText()) {
                            DayOfWeek.MONDAY -> stringResource(Res.string.monday)
                            DayOfWeek.TUESDAY -> stringResource(Res.string.tuesday)
                            DayOfWeek.WEDNESDAY -> stringResource(Res.string.wednesday)
                            DayOfWeek.THURSDAY -> stringResource(Res.string.thursday)
                            DayOfWeek.FRIDAY -> stringResource(Res.string.friday)
                            DayOfWeek.SATURDAY -> stringResource(Res.string.saturday)
                            DayOfWeek.SUNDAY -> stringResource(Res.string.sunday)
                        }
                    }
                } else {
                    ""
                }

                BodyText(
                    text = dayOfWeek,
                    textColor = Color.White,
                    size = ComponentSize.MEDIUM,
                    textAlign = TextAlign.Start,
                    maxLines = 1,
                    textOverflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                LabelText(
                    text = "${item.day.avghumidity}%",
                    vector = WeatherAppIcons.WaterDropsIndicator,
                    iconPosition = IconPosition.START,
                    textColor = Color.White,
                    size = ComponentSize.MEDIUM,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                val imageRequest = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(urlProvider.getImageUrl(item.day.condition.icon, imageQuality))
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()

                AsyncImage(
                    model = imageRequest,
                    contentDescription = "weather",
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.Transparent),
                    contentScale = ContentScale.Crop
                )
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {
                LabelText(
                    text = stringResource(Res.string.temp_celsius).format(item.day.mintempC.toString()) +
                            "\n" +
                            stringResource(Res.string.temp_celsius).format(item.day.maxtempC.toString()),
                    modifier = Modifier,
                    textColor = Color.White,
                    size = ComponentSize.MEDIUM,
                    vector = WeatherAppIcons.TempIndicator,
                    iconPosition = IconPosition.START,
                    textAlign = TextAlign.End,
                    maxLines = 2
                )
            }
        }
    }
}


@Composable
fun DailyWeatherList(
    days: List<DailyForecast>,
    darkTheme: Boolean,
    urlProvider: UrlProvider,
    imageQuality: String,
    currentLang: String,
    modifier: Modifier = Modifier,
    onItemClick: (DailyForecast) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.Transparent),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        days.forEach { day ->
            DailyWeatherItemIndicator(
                item = day,
                urlProvider = urlProvider,
                imageQuality = imageQuality,
                currentLang = currentLang,
                darkTheme = darkTheme,
                modifier = Modifier.fillMaxWidth(),
                onItemClick
            )
        }
    }
}

@Composable
fun WeatherIdleState(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        }
    }
}

@Composable
fun WeatherLoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
    }
}

@Composable
fun UserCustomLocationIdleState(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        }
    }
}

@Composable
fun UserCustomLocationLoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
    }
}

@Composable
fun UserCustomLocationItem(
    item: UserCustomLocation,
    urlProvider: UrlProvider,
    imageQuality: String,
    darkTheme: Boolean,
    enableStartToEnd: Boolean = true,
    startToEndIcon: ImageVector,
    onSwipeStartToEnd: (UserCustomLocation) -> Unit,
    enableEndToStart: Boolean = true,
    endToStartIcon: ImageVector,
    onSwipeEndToStart: (UserCustomLocation) -> Unit,
    onItemClick: (UserCustomLocation) -> Unit,
    onItemLongClick: (UserCustomLocation) -> Unit,
    resetSwipe: Boolean = false,
    modifier: Modifier = Modifier,
) {

    val cardBackgroundColor = if (darkTheme) {
        extendedDark.backgroundCardColor.color
    } else {
        extendedLight.backgroundCardColor.color
    }

    SwipeActionContainer(
        item = item,
        modifier = Modifier.padding(5.dp),
        enableStartToEnd = enableStartToEnd,
        startToEndIcon = startToEndIcon,
        onSwipeStartToEnd = {
            onSwipeStartToEnd(item)
        },
        enableEndToStart = enableEndToStart,
        endToStartIcon = endToStartIcon,
        onSwipeEndToStart = {
            onSwipeEndToStart(item)
        },
        resetSwipe = resetSwipe
    ) {
        Card(
            modifier = modifier
                .padding(4.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
            elevation = CardDefaults.cardElevation(4.dp),
            onClick = {
                onItemClick(item)
            },
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val imageRequest = ImageRequest.Builder(LocalPlatformContext.current)
                        .data(urlProvider.getImageUrl(item.icon, imageQuality))
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .build()

                    AsyncImage(
                        model = imageRequest,
                        contentDescription = "weather",
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.Transparent),
                        contentScale = ContentScale.Crop
                    )

                    HeaderText(
                        text = stringResource(Res.string.temp_celsius).format(item.tempC.toString()),
                        modifier = Modifier.weight(1f),
                        textColor = Color.White,
                        size = ComponentSize.MEDIUM,
                        textAlign = TextAlign.End
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TitleText(
                        text = item.cityName,
                        modifier = Modifier.weight(1f),
                        textColor = Color.White,
                        size = ComponentSize.MEDIUM,
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        fontWeight = Bold,
                        textOverflow = TextOverflow.Ellipsis
                    )

                    if (item.isCurrent) {
                        LabelText(
                            text = stringResource(Res.string.gps),
                            iconPosition = IconPosition.START,
                            textColor = Color.White,
                            size = ComponentSize.LARGE,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    if (item.isSelected) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = "selected",
                            modifier = Modifier.size(24.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}