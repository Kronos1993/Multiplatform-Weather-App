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
        modifier = GlanceModifier.fillMaxSize().padding(8.dp),
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
                    modifier = GlanceModifier.size(10.dp)
                )

                Spacer(modifier = GlanceModifier.width(8.dp))

                Text(
                    text = weatherData.location,
                    style = TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp, // Aumentado
                        color = ColorProvider(Color.White)
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )

                Text(
                    text = weatherData.time,
                    style = TextStyle(
                        fontSize = 12.sp, // Aumentado de 12sp a 14sp
                        color = ColorProvider(Color.White)
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(4.dp))

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
                    modifier = GlanceModifier.size(44.dp) // Aumentado
                )

                Spacer(modifier = GlanceModifier.width(8.dp))

                Text(
                    text = weatherData.currentTemp,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp, // Aumentado de 20sp a 24sp
                        color = ColorProvider(Color.White)
                    )
                )

                Spacer(modifier = GlanceModifier.width(4.dp))

                // Forecast Days
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

@Composable
fun SmallWeatherWidgetContent(weatherData: WeatherWidgetData?) {
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
                    modifier = GlanceModifier.size(36.dp) // Aumentado
                )

                Spacer(modifier = GlanceModifier.width(8.dp))

                Text(
                    text = weatherData.currentTemp,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp, // Aumentado de 16sp a 18sp
                        color = ColorProvider(Color.White)
                    )
                )
            }

            Text(
                text = weatherData.currentCondition,
                style = TextStyle(
                    fontSize = 16.sp, // Aumentado de 9sp a 11sp
                    color = ColorProvider(Color.White),
                ),
                maxLines = 1
            )

            Text(
                text = weatherData.location,
                style = TextStyle(
                    fontSize = 12.sp, // Aumentado de 10sp a 12sp
                    color = ColorProvider(Color.White)
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
                    modifier = GlanceModifier.size(18.dp) // Aumentado
                )

                Spacer(modifier = GlanceModifier.width(8.dp))

                Text(
                    text = weatherData.location,
                    style = TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp, // Aumentado de 16sp a 18sp
                        color = ColorProvider(Color.White)
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )

                Text(
                    text = weatherData.time,
                    style = TextStyle(
                        fontSize = 16.sp, // Aumentado de 14sp a 16sp
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
                    modifier = GlanceModifier.size(48.dp) // Aumentado
                )

                Spacer(modifier = GlanceModifier.width(16.dp))

                Column {
                    Text(
                        text = weatherData.currentTemp,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp, // Aumentado de 24sp a 28sp
                            color = ColorProvider(Color.White)
                        )
                    )

                    Text(
                        text = weatherData.currentCondition,
                        style = TextStyle(
                            fontSize = 16.sp, // Aumentado de 14sp a 16sp
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
                    modifier = GlanceModifier,
                    horizontal = Alignment.Start
                )

                Spacer(modifier = GlanceModifier.width(20.dp))

                ForecastDayItem(
                    dayName = weatherData.day2Name,
                    iconUrl = weatherData.day2IconUrl,
                    modifier = GlanceModifier,
                    horizontal = Alignment.End
                )
            }
        }
    }
}

@Composable
private fun ForecastDayItem(
    dayName: String,
    iconUrl: String,
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
                fontSize = 14.sp, // Aumentado de 12sp a 14sp
                color = ColorProvider(Color.White)
            )
        )

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
            modifier = GlanceModifier.size(32.dp) // Aumentado de 36dp a 40dp
        )
    }
}

@Composable
private fun WeatherDetailItem(label: String, value: String) {
    Row {
        Text(
            text = "$label: ",
            style = TextStyle(
                fontSize = 14.sp, // Aumentado de 12sp a 14sp
                fontWeight = FontWeight.Medium,
                color = ColorProvider(Color.White)
            ),
        )
        Text(
            text = value,
            style = TextStyle(
                fontSize = 14.sp, // Aumentado de 12sp a 14sp
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
                fontSize = 14.sp, // Aumentado de 12sp a 14sp
                color = ColorProvider(Color.White)
            )
        )
    }
}