package com.kronos.multiplatform.weatherapp.widget.components

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
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
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.kronos.multiplatform.weatherapp.R
import com.kronos.multiplatform.weatherapp.widget.model.WeatherWidgetData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.net.URL

@Composable
fun MediumWeatherWidgetContent(weatherData: WeatherWidgetData?) {
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (weatherData == null) {
            LoadingWidget()
        } else {
            // Header
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_locations_widget),
                    contentDescription = "Location",
                    modifier = GlanceModifier.size(20.dp)
                )

                Spacer(modifier = GlanceModifier.width(8.dp))

                Text(
                    text = weatherData.location,
                    style = TextStyle(
                        fontWeight = FontWeight.Medium,
                        color = ColorProvider(Color.White)
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )

                Text(
                    text = weatherData.time,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = ColorProvider(Color.White)
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(12.dp))

            // Main Content
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Current Weather
                val image = runBlocking(Dispatchers.IO) {
                    BitmapFactory.decodeStream(
                        URL(weatherData.currentIconUrl).openConnection()
                            .getInputStream()
                    )
                }

                Image(
                    ImageProvider(
                        image
                    ),
                    contentDescription = "Current weather",
                    modifier = GlanceModifier.size(40.dp)
                )

                Spacer(modifier = GlanceModifier.width(12.dp))

                Text(
                    text = weatherData.currentTemp,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = ColorProvider(Color.White)
                    )
                )

                Spacer(modifier = GlanceModifier.width(16.dp))

                // Forecast Days
                Row(
                    modifier = GlanceModifier.defaultWeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ForecastDayItem(
                        dayName = weatherData.day1Name,
                        iconUrl = weatherData.day1IconUrl,
                        modifier = GlanceModifier.defaultWeight()
                    )

                    ForecastDayItem(
                        dayName = weatherData.day2Name,
                        iconUrl = weatherData.day2IconUrl,
                        modifier = GlanceModifier.defaultWeight()
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherWidgetContent(weatherData: WeatherWidgetData?) {
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(8.dp),
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
                val image = runBlocking(Dispatchers.IO) {
                    BitmapFactory.decodeStream(
                        URL(weatherData.currentIconUrl).openConnection()
                            .getInputStream()
                    )
                }

                Image(
                    ImageProvider(image),
                    contentDescription = "Current weather",
                    modifier = GlanceModifier.size(32.dp)
                )

                Spacer(modifier = GlanceModifier.width(8.dp))

                Text(
                    text = weatherData.currentTemp,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ColorProvider(Color.White)
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(4.dp))

            Text(
                text = weatherData.location,
                style = TextStyle(
                    fontSize = 10.sp,
                    color = ColorProvider(Color.White)
                ),
                maxLines = 1
            )

            Spacer(modifier = GlanceModifier.height(2.dp))

            Text(
                text = weatherData.time,
                style = TextStyle(
                    fontSize = 9.sp,
                    color = ColorProvider(Color.White.copy(alpha = 0.8f))
                )
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            Text(
                text = weatherData.currentCondition,
                style = TextStyle(
                    fontSize = 9.sp,
                    color = ColorProvider(Color.White),
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
fun LargeWeatherWidgetContent(weatherData: WeatherWidgetData?) {
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(20.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (weatherData == null) {
            LoadingWidget()
        } else {
            // Header
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_locations_widget),
                    contentDescription = "Location",
                    modifier = GlanceModifier.size(24.dp)
                )

                Spacer(modifier = GlanceModifier.width(8.dp))

                Text(
                    text = weatherData.location,
                    style = TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = ColorProvider(Color.White)
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )

                Text(
                    text = weatherData.time,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = ColorProvider(Color.White)
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(16.dp))

            // Current Weather Section
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val image = runBlocking(Dispatchers.IO) {
                    BitmapFactory.decodeStream(
                        URL(weatherData.currentIconUrl).openConnection()
                            .getInputStream()
                    )
                }

                Image(
                    ImageProvider(
                        image
                    ),
                    contentDescription = "Current weather",
                    modifier = GlanceModifier.size(40.dp)
                )

                Spacer(modifier = GlanceModifier.width(16.dp))

                Column {
                    Text(
                        text = weatherData.currentTemp,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = ColorProvider(Color.White)
                        )
                    )

                    Text(
                        text = weatherData.currentCondition,
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = ColorProvider(Color.White)
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.width(32.dp))

                // Weather Details
                Column {
                    WeatherDetailItem("Humidity", "${weatherData.humidity}%")
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    WeatherDetailItem(
                        "Wind",
                        "${weatherData.windSpeed} ${weatherData.windDirection}"
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    WeatherDetailItem("UV", weatherData.uvIndex)
                }
            }

            Spacer(modifier = GlanceModifier.height(20.dp))

            // Forecast Section
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ForecastDayItem(
                    dayName = weatherData.day1Name,
                    iconUrl = weatherData.day1IconUrl,
                    modifier = GlanceModifier
                )

                ForecastDayItem(
                    dayName = weatherData.day2Name,
                    iconUrl = weatherData.day2IconUrl,
                    modifier = GlanceModifier
                )
            }
        }
    }
}

@Composable
private fun ForecastDayItem(
    dayName: String,
    iconUrl: String,
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
                color = ColorProvider(Color.White)
            )
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        val image = runBlocking(Dispatchers.IO) {
            BitmapFactory.decodeStream(
                URL(iconUrl).openConnection()
                    .getInputStream()
            )
        }

        Image(
            ImageProvider(
                image
            ),
            contentDescription = "Current weather",
            modifier = GlanceModifier.size(36.dp)
        )
    }
}

@Composable
private fun WeatherDetailItem(label: String, value: String) {
    Row {
        Text(
            text = "$label: ",
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = ColorProvider(Color.White)
            ),
        )
        Text(
            text = value,
            style = TextStyle(
                fontSize = 12.sp,
                color = ColorProvider(Color.White)
            )
        )
    }
}

@Composable
private fun LoadingWidget() {
    Box(
        modifier = GlanceModifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Loading...", style = TextStyle(
                fontSize = 12.sp,
                color = ColorProvider(Color.White)
            )
        )
    }
}