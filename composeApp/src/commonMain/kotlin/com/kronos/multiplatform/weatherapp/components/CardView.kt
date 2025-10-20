package com.kronos.multiplatform.weatherapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.room.util.TableInfo
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.kronos.multiplatform.weatherapp.components.icons.WeatherAppIcons
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.WaterDropsIndicator
import com.kronos.multiplatform.weatherapp.components.theme.BackgroundCardColorDashboardAcceptedDark
import com.kronos.multiplatform.weatherapp.components.theme.BackgroundCardColorDashboardAcceptedLight
import com.kronos.multiplatform.weatherapp.components.theme.backgroundCardColorDark
import com.kronos.multiplatform.weatherapp.components.theme.backgroundCardColorLight
import com.kronos.multiplatform.weatherapp.core.util.format
import com.kronos.multiplatform.weatherapp.data.remote.ktor.UrlProvider
import com.kronos.multiplatform.weatherapp.domain.model.DailyForecast
import com.kronos.multiplatform.weatherapp.domain.model.Hour
import com.kronos.multiplatform.weatherapp.domain.model.Indicator
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast
import org.jetbrains.compose.resources.stringResource
import weather_app.composeapp.generated.resources.Res
import weather_app.composeapp.generated.resources.feels_like_temp_celsius
import weather_app.composeapp.generated.resources.location_name
import weather_app.composeapp.generated.resources.temp_celsius

@Composable
fun HourlyItemIndicator(
    item: Hour,
    urlProvider: UrlProvider,
    imageQuality: String,
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
) {
    val cardBackgroundColor = if (darkTheme) {
        BackgroundCardColorDashboardAcceptedDark
    } else {
        BackgroundCardColorDashboardAcceptedLight
    }

    Card(
        modifier = modifier
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            LabelText(
                text = item.time,
                modifier = Modifier.wrapContentSize(),
                textColor = Color.White,
                size = ComponentSize.MEDIUM
            )

            val imageRequest = ImageRequest.Builder(LocalPlatformContext.current)
                .data(urlProvider.getImageUrl(item.condition.icon, imageQuality))
                .memoryCachePolicy(CachePolicy.DISABLED)
                .diskCachePolicy(CachePolicy.DISABLED)
                .build()

            AsyncImage(
                model = imageRequest,
                contentDescription = "weather",
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.Transparent),
                contentScale = ContentScale.Fit
            )

            // Temperatura - abajo
            TitleText(
                text = item.tempC.toString(),
                modifier = Modifier.wrapContentSize(),
                textColor = Color.White,
                size = ComponentSize.MEDIUM
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
    val cardColor = if (darkTheme) {
        backgroundCardColorDark
    } else {
        backgroundCardColorLight
    }

    val displayIndicators = indicators.take(6)

    val firstRow = displayIndicators.take(3)
    val secondRow = displayIndicators.drop(3).take(3)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.Transparent)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Primera fila
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                firstRow.forEach { indicator ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        WeatherIndicatorItem(
                            item = indicator,
                            darkTheme = darkTheme,
                            modifier = Modifier.wrapContentWidth()
                        )
                    }
                }

                repeat(3 - firstRow.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            // Segunda fila
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                secondRow.forEach { indicator ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        WeatherIndicatorItem(
                            item = indicator,
                            darkTheme = darkTheme,
                            modifier = Modifier.wrapContentWidth()
                        )
                    }
                }

                repeat(3 - secondRow.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun CurrentWeatherItem(
    currentWeather: Forecast,
    darkTheme: Boolean,
    urlProvider: UrlProvider,
    imageQuality: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth()
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp,Alignment.CenterVertically),
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
            .memoryCachePolicy(CachePolicy.DISABLED)
            .diskCachePolicy(CachePolicy.DISABLED)
            .build()

        AsyncImage(
            model = imageRequest,
            contentDescription = "weather",
            modifier = Modifier
                .background(Color.Transparent)
                .size(128.dp),
            contentScale = ContentScale.Crop
        )

        BodyText(
            currentWeather.location.localtime,
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

@Composable
fun CurrentWeatherCompactItem(
    currentWeather: Forecast,
    darkTheme: Boolean,
    urlProvider: UrlProvider,
    imageQuality: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            val imageRequest = ImageRequest.Builder(LocalPlatformContext.current)
                .data(urlProvider.getImageUrl(currentWeather.current.condition.icon, imageQuality))
                .memoryCachePolicy(CachePolicy.DISABLED)
                .diskCachePolicy(CachePolicy.DISABLED)
                .build()

            AsyncImage(
                model = imageRequest,
                contentDescription = "weather",
                modifier = Modifier
                    .background(Color.Transparent)
                    .size(48.dp),
                contentScale = ContentScale.Crop
            )

            BodyText(
                currentWeather.current.condition.description,
                textColor = Color.White,
                textAlign = TextAlign.Start,
                size = ComponentSize.MEDIUM,
                fontWeight = FontWeight.Bold
            )

            BodyText(
                currentWeather.location.name,
                textColor = Color.White.copy(alpha = 0.8f),
                size = ComponentSize.SMALL,
                textAlign = TextAlign.Start
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            BodyText(
                currentWeather.location.localtime,
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
                textColor = Color.White.copy(alpha = 0.8f),
                size = ComponentSize.SMALL,
                textAlign = TextAlign.End
            )

            BodyText(
                stringResource(Res.string.location_name).format(
                    currentWeather.location.country,
                    currentWeather.location.name
                ),
                textColor = Color.White.copy(alpha = 0.8f),
                size = ComponentSize.SMALL,
                textAlign = TextAlign.End,
                maxLines = 1,
                textOverflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Composable
fun DailyWeatherItemIndicator(
    item: DailyForecast,
    urlProvider: UrlProvider,
    imageQuality: String,
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
) {
    val cardBackgroundColor = if (darkTheme) {
        BackgroundCardColorDashboardAcceptedDark
    } else {
        BackgroundCardColorDashboardAcceptedLight
    }

    Card(
        modifier = modifier
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically, // Alineación vertical centrada
            horizontalArrangement = Arrangement.SpaceBetween // Distribuye el espacio entre elementos
        ) {
            BodyText(
                "Show day",
                modifier = Modifier.weight(1f),
                textColor = Color.White,
                size = ComponentSize.MEDIUM,
                textAlign = TextAlign.Start
            )

            LabelText(
                "${item.day.avghumidity}%",
                vector = WeatherAppIcons.WaterDropsIndicator,
                iconPosition = IconPosition.START,
                modifier = Modifier.weight(1f),
                textColor = Color.White,
                size = ComponentSize.MEDIUM,
                textAlign = TextAlign.Center
            )

            val imageRequest = ImageRequest.Builder(LocalPlatformContext.current)
                .data(urlProvider.getImageUrl(item.day.condition.icon, imageQuality))
                .memoryCachePolicy(CachePolicy.DISABLED)
                .diskCachePolicy(CachePolicy.DISABLED)
                .build()

            AsyncImage(
                model = imageRequest,
                contentDescription = "weather",
                modifier = Modifier
                    .size(40.dp) // Tamaño fijo para la imagen
                    .background(Color.Transparent),
                contentScale = ContentScale.Crop
            )

            LabelText(
                item.day.avgtempC.toString(),
                modifier = Modifier.weight(1f),
                textColor = Color.White,
                size = ComponentSize.MEDIUM,
                textAlign = TextAlign.End
            )
        }
    }
}


@Composable
fun DailyWeatherList(
    days: List<DailyForecast>,
    darkTheme: Boolean,
    urlProvider: UrlProvider,
    imageQuality: String,
    modifier: Modifier = Modifier,
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
                darkTheme = darkTheme,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}