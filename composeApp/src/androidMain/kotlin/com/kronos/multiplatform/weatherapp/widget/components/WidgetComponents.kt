package com.kronos.multiplatform.weatherapp.widget.components

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.kronos.multiplatform.weatherapp.R
import com.kronos.multiplatform.weatherapp.widget.OpenAppCallback
import com.kronos.multiplatform.weatherapp.widget.model.WeatherWidgetData

@Composable
fun MediumWeatherWidgetContent(weatherData: WeatherWidgetData?) {
    Box(
        modifier = GlanceModifier.fillMaxSize().clickable(
            actionRunCallback<OpenAppCallback>()
        ),
        contentAlignment = Alignment.Center
    ) {
        if (weatherData == null) {
            LoadingWidget()
        } else {
            Column(
                modifier = GlanceModifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = GlanceModifier,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_locations_widget),
                        contentDescription = "Location",
                        modifier = GlanceModifier.size(10.dp)
                    )

                    Spacer(modifier = GlanceModifier.width(8.dp))

                    Text(
                        text = weatherData.location,
                        style = TextStyle(
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = ColorProvider(Color.White,Color.White)
                        )
                    )

                    Spacer(modifier = GlanceModifier.width(12.dp))

                    Text(
                        text = weatherData.time,
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = ColorProvider(Color.White,Color.White)
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                Row(
                    modifier = GlanceModifier,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    if (weatherData.currentIconBitmap!=null)
                        Image(
                            ImageProvider(weatherData.currentIconBitmap),
                            contentDescription = "Current weather",
                            modifier = GlanceModifier.size(44.dp)
                        )
                    else
                        Image(
                            ImageProvider(R.drawable.ic_weather_app_icon),
                            contentDescription = "Current weather",
                            modifier = GlanceModifier.size(44.dp)
                        )

                    Spacer(modifier = GlanceModifier.width(12.dp))

                    Text(
                        text = weatherData.currentTemp,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = ColorProvider(Color.White,Color.White)
                        )
                    )

                    Spacer(modifier = GlanceModifier.width(16.dp))

                    MediumForecastDayItem(
                        dayName = weatherData.day1Name,
                        icon = weatherData.day1IconBitmap,
                        modifier = GlanceModifier
                    )

                    Spacer(modifier = GlanceModifier.width(12.dp))

                    MediumForecastDayItem(
                        dayName = weatherData.day2Name,
                        icon = weatherData.day2IconBitmap,
                        modifier = GlanceModifier
                    )
                }
            }
        }
    }
}

@Composable
private fun MediumForecastDayItem(
    dayName: String,
    icon: Bitmap?,
    modifier: GlanceModifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dayName,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = ColorProvider(Color.White,Color.White)
            )
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        if (icon!=null)
            Image(
                ImageProvider(icon),
                contentDescription = "Forecast weather",
                modifier = GlanceModifier.size(32.dp)
            )
        else
            Image(
                ImageProvider(R.drawable.ic_weather_app_icon),
                contentDescription = "Forecast weather",
                modifier = GlanceModifier.size(32.dp)
            )
    }
}


@Composable
fun SmallWeatherWidgetContent(weatherData: WeatherWidgetData?) {
    Box(
        modifier = GlanceModifier.fillMaxSize().clickable(
            actionRunCallback<OpenAppCallback>()
        ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = GlanceModifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (weatherData == null) {
                LoadingWidget()
            } else {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    if (weatherData.currentIconBitmap!=null)
                        Image(
                            ImageProvider(weatherData.currentIconBitmap),
                            contentDescription = "Current weather",
                            modifier = GlanceModifier.size(36.dp)
                        )
                    else
                        Image(
                            ImageProvider(R.drawable.ic_weather_app_icon),
                            contentDescription = "Current weather",
                            modifier = GlanceModifier.size(36.dp)
                        )

                    Spacer(modifier = GlanceModifier.width(8.dp))

                    Text(
                        text = weatherData.currentTemp,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = ColorProvider(Color.White,Color.White)
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(4.dp))

                Text(
                    text = weatherData.currentCondition,
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = ColorProvider(Color.White,Color.White),
                    ),
                    maxLines = 1
                )

                Spacer(modifier = GlanceModifier.height(2.dp))

                Text(
                    text = weatherData.location,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = ColorProvider(Color.White,Color.White)
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun LargeWeatherWidgetContent(weatherData: WeatherWidgetData?, context: Context) {
    Box(
        modifier = GlanceModifier.fillMaxSize().clickable(
            actionRunCallback<OpenAppCallback>()
        ),
        contentAlignment = Alignment.Center
    ) {
        if (weatherData == null) {
            LoadingWidget()
        } else {
            Column(
                modifier = GlanceModifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header - Centrado
                Row(
                    modifier = GlanceModifier,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_locations_widget),
                        contentDescription = "Location",
                        modifier = GlanceModifier.size(18.dp)
                    )

                    Spacer(modifier = GlanceModifier.width(8.dp))

                    Text(
                        text = weatherData.location,
                        style = TextStyle(
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            color = ColorProvider(Color.White,Color.White)
                        )
                    )

                    Spacer(modifier = GlanceModifier.width(16.dp))

                    Text(
                        text = weatherData.time,
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = ColorProvider(Color.White,Color.White)
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = GlanceModifier.defaultWeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        if (weatherData.currentIconBitmap!=null)
                            Image(
                                ImageProvider(weatherData.currentIconBitmap),
                                contentDescription = "Current weather",
                                modifier = GlanceModifier.size(64.dp)
                            )
                        else
                            Image(
                                ImageProvider(R.drawable.ic_weather_app_icon),
                                contentDescription = "Current weather",
                                modifier = GlanceModifier.size(64.dp)
                            )

                        Text(
                            text = weatherData.currentTemp,
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp,
                                color = ColorProvider(Color.White,Color.White)
                            )
                        )

                        Spacer(modifier = GlanceModifier.height(4.dp))

                        Text(
                            text = weatherData.currentCondition,
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = ColorProvider(Color.White,Color.White),
                                textAlign = TextAlign.Center,
                            ),
                            maxLines = 2
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(32.dp))

                    Column(
                        modifier = GlanceModifier.defaultWeight(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        WeatherDetailItem(
                            context.getString(R.string.humidity),
                            "${weatherData.humidity}%"
                        )
                        Spacer(modifier = GlanceModifier.height(8.dp))
                        WeatherDetailItem(
                            context.getString(R.string.wind),
                            "${weatherData.windSpeed} ${weatherData.windDirection}"
                        )
                        Spacer(modifier = GlanceModifier.height(8.dp))
                        WeatherDetailItem(context.getString(R.string.uv_index), weatherData.uvIndex)
                    }
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                Row(
                    modifier = GlanceModifier,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ForecastDayItem(
                        dayName = weatherData.day1Name,
                        icon = weatherData.day1IconBitmap,
                        modifier = GlanceModifier,
                        horizontal = Alignment.CenterHorizontally
                    )

                    Spacer(modifier = GlanceModifier.width(40.dp))

                    ForecastDayItem(
                        dayName = weatherData.day2Name,
                        icon = weatherData.day2IconBitmap,
                        modifier = GlanceModifier,
                        horizontal = Alignment.CenterHorizontally
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherDetailItem(label: String, value: String) {
    Row(
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "$label:",
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = ColorProvider(Color.White,Color.White)
            )
        )

        Spacer(modifier = GlanceModifier.width(8.dp))

        Text(
            text = value,
            style = TextStyle(
                fontSize = 14.sp,
                color = ColorProvider(Color.White,Color.White)
            )
        )
    }
}

@Composable
private fun ForecastDayItem(
    dayName: String,
    icon: Bitmap?,
    modifier: GlanceModifier,
    horizontal: Alignment.Horizontal = Alignment.CenterHorizontally
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontal
    ) {
        Text(
            text = dayName,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = ColorProvider(Color.White,Color.White)
            )
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        if (icon != null)
            Image(
                ImageProvider(icon),
                contentDescription = "Forecast weather",
                modifier = GlanceModifier.size(48.dp)
            )
        else
            Image(
                ImageProvider(R.drawable.ic_weather_app_icon),
                contentDescription = "Forecast weather",
                modifier = GlanceModifier.size(48.dp)
            )
    }
}

@Composable
private fun LoadingWidget() {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_no_weather_data),
            contentDescription = "Weather loading",
            modifier = GlanceModifier.size(48.dp)
        )

        Spacer(modifier = GlanceModifier.height(16.dp))

        Text(
            text = stringResource(R.string.loading_dialog_text),
            style = TextStyle(
                fontSize = 14.sp,
                color = ColorProvider(Color.White,Color.White)
            )
        )
    }
}

@Composable
fun WeatherWidgetErrorContent(message: String) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color.DarkGray,Color.DarkGray))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = TextStyle(
                color = ColorProvider(Color.White,Color.White),
                fontSize = 14.sp
            ),
            maxLines = 2
        )
    }
}
