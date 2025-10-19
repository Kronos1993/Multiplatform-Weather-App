package com.kronos.multiplatform.weatherapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
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
        Column(modifier = Modifier.fillMaxWidth()) {
            LabelText(
                item.time,
                modifier = Modifier.weight(1f).padding(start = 5.dp),
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
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )

            TitleText(
                item.tempC.toString(),
                modifier = Modifier.weight(1f).padding(start = 5.dp),
                textColor = Color.White,
                size = ComponentSize.MEDIUM
            )
        }
    }
}

@Composable
fun WeatherIndicatorsItem(
    item: Indicator,
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {

        Icon(
            imageVector = item.image,
            contentDescription = "weather",
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(60.dp)),
        )

        HeaderText(
            item.header,
            modifier = Modifier.weight(1f).padding(start = 5.dp),
            textColor = Color.White,
            size = ComponentSize.SMALL
        )

        LabelText(
            item.description,
            modifier = Modifier.weight(1f).padding(start = 5.dp),
            textColor = Color.White,
            size = ComponentSize.SMALL
        )
    }
}


@Composable
fun WeatherIndicatorList(
    indicators: List<Indicator>,
    listState: LazyGridState,
    gridColumns: Int = 1,
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
) {
    val cardColor = if (darkTheme) {
        backgroundCardColorDark
    } else {
        backgroundCardColorLight
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        LazyVerticalGrid(
            state = listState,
            columns = GridCells.Fixed(gridColumns),
            modifier = modifier
                .fillMaxSize()
                .background(color = Color.Transparent),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(indicators) {
                WeatherIndicatorsItem(item = it, darkTheme = darkTheme)
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
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        HeaderText(
            currentWeather.current.condition.description,
            modifier = Modifier.weight(1f),
            textColor = Color.White,
            size = ComponentSize.MEDIUM
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
                .background(Color.Gray)
                .size(128.dp),
            contentScale = ContentScale.Crop
        )

        LabelText(
            currentWeather.location.localtime,
            modifier = Modifier.weight(1f).padding(start = 5.dp),
            textColor = Color.White,
            size = ComponentSize.MEDIUM
        )

        DisplayText(
            stringResource(Res.string.temp_celsius).format(
                currentWeather.current.tempC,
            ),
            modifier = Modifier.weight(1f).padding(start = 5.dp),
            textColor = Color.White,
            size = ComponentSize.SMALL
        )

        LabelText(
            stringResource(Res.string.feels_like_temp_celsius).format(
                currentWeather.current.feelslikeC,
            ),
            modifier = Modifier.weight(1f).padding(start = 5.dp),
            textColor = Color.White,
            size = ComponentSize.MEDIUM
        )

        LabelText(
            stringResource(Res.string.location_name).format(
                currentWeather.location.country,
                currentWeather.location.name
            ),
            modifier = Modifier.weight(1f).padding(start = 5.dp),
            textColor = Color.White,
            size = ComponentSize.MEDIUM
        )
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
        Row(modifier = Modifier.fillMaxWidth()) {
            LabelText(
                "Show day",
                modifier = Modifier.weight(1f).padding(start = 5.dp),
                textColor = Color.White,
                size = ComponentSize.MEDIUM
            )

            LabelText(
                "Show day",
                vector = Icons.Filled.BatterySaver,
                iconPosition = IconPosition.START,
                modifier = Modifier.weight(1f).padding(start = 5.dp),
                textColor = Color.White,
                size = ComponentSize.MEDIUM
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
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )

            LabelText(
                item.day.avgtempC.toString(),
                modifier = Modifier.weight(1f).padding(start = 5.dp),
                textColor = Color.White,
                size = ComponentSize.MEDIUM
            )
        }
    }
}


@Composable
fun DailyWeatherList(
    days: List<DailyForecast>,
    listState: LazyGridState,
    gridColumns: Int = 1,
    darkTheme: Boolean,
    urlProvider: UrlProvider,
    imageQuality: String,
    modifier: Modifier = Modifier,
) {
    val cardColor = if (darkTheme) {
        backgroundCardColorDark
    } else {
        backgroundCardColorLight
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        LazyVerticalGrid(
            state = listState,
            columns = GridCells.Fixed(gridColumns),
            modifier = modifier
                .fillMaxSize()
                .background(color = Color.Transparent),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(days) {
                DailyWeatherItemIndicator(
                    item = it,
                    urlProvider = urlProvider,
                    imageQuality = imageQuality,
                    darkTheme = darkTheme
                )
            }
        }
    }
}