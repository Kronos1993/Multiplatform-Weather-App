package com.kronos.multiplatform.weatherapp.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kronos.multiplatform.weatherapp.components.button.Button
import com.kronos.multiplatform.weatherapp.components.button.ButtonStyle
import com.kronos.multiplatform.weatherapp.components.button.ButtonType
import com.kronos.multiplatform.weatherapp.components.icons.WeatherAppIcons
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.NoWeatherIndicator
import org.jetbrains.compose.resources.stringResource
import weather_app.composeapp.generated.resources.Res
import weather_app.composeapp.generated.resources.no_weather_data

@Composable
fun NoItemsToShow(
    text: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TitleText(
            text = text,
            size = ComponentSize.MEDIUM
        )
    }
}

@Composable
fun NoWeatherItem(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = WeatherAppIcons.NoWeatherIndicator,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Unspecified
            )

            Spacer(modifier = Modifier.height(16.dp))
            HeaderText(
                text = stringResource(Res.string.no_weather_data),
                modifier = Modifier.padding(top = 16.dp),
                size = ComponentSize.SMALL,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                text = "Retry",
                type = ButtonType.FILLED,
                style = ButtonStyle.INFO,
                onClick = onRetry)
        }
    }
}
