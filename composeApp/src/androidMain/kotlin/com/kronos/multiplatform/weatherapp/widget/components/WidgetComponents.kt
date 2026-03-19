package com.kronos.multiplatform.weatherapp.widget.components

import android.content.Context
import android.graphics.Bitmap
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentWidth
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.kronos.multiplatform.weatherapp.R
import com.kronos.multiplatform.weatherapp.widget.OpenAppCallback
import com.kronos.multiplatform.weatherapp.widget.model.WeatherWidgetData


@Composable
fun WeatherWidgetBackground(content: @Composable () -> Unit) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.bg_widget_glass))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun SmallWeatherWidgetContent(weatherData: WeatherWidgetData) {
    val t = rememberWidgetTypography()

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(actionRunCallback<OpenAppCallback>()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WeatherIcon(bitmap = weatherData.currentIconBitmap, size = t.weatherIconSize)
            Spacer(modifier = GlanceModifier.width(6.dp))
            Text(
                text = weatherData.currentTemp,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = t.tempSize,
                    color = ColorProvider(Color.White, Color.White)
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(4.dp))

        Text(
            text = weatherData.currentCondition,
            style = TextStyle(
                fontSize = t.conditionSize,
                color = ColorProvider(Color(0xCCFFFFFF), Color(0xCCFFFFFF))
            ),
            maxLines = 1
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        LocationRow(location = weatherData.location, typography = t)
    }
}

@Composable
fun MediumWeatherWidgetContent(weatherData: WeatherWidgetData) {
    val t = rememberWidgetTypography()

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(actionRunCallback<OpenAppCallback>()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LocationRow(location = weatherData.location, typography = t)

        Spacer(modifier = GlanceModifier.height(8.dp))

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                WeatherIcon(bitmap = weatherData.currentIconBitmap, size = t.weatherIconSize)
                Spacer(modifier = GlanceModifier.width(6.dp))
                Text(
                    text = weatherData.currentTemp,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = t.tempSize,
                        color = ColorProvider(Color.White, Color.White)
                    )
                )
            }

            Spacer(modifier = GlanceModifier.width(20.dp))

            ForecastDayCompact(
                dayName = weatherData.day1Name,
                icon = weatherData.day1IconBitmap,
                typography = t
            )

            Spacer(modifier = GlanceModifier.width(14.dp))

            ForecastDayCompact(
                dayName = weatherData.day2Name,
                icon = weatherData.day2IconBitmap,
                typography = t
            )
        }
    }
}

@Composable
fun LargeWeatherWidgetContent(weatherData: WeatherWidgetData, context: Context) {
    val t = rememberWidgetTypography()

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(actionRunCallback<OpenAppCallback>()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LocationRow(location = weatherData.location, typography = t)
        }

        Spacer(modifier = GlanceModifier.height(10.dp))

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                WeatherIcon(bitmap = weatherData.currentIconBitmap, size = t.weatherIconSize)
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = weatherData.currentTemp,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = t.tempSize,
                        color = ColorProvider(Color.White, Color.White)
                    )
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = weatherData.currentCondition,
                    style = TextStyle(
                        fontSize = t.conditionSize,
                        color = ColorProvider(Color(0xCCFFFFFF), Color(0xCCFFFFFF)),
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 2
                )
            }

            Spacer(modifier = GlanceModifier.width(16.dp))

            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.Start
            ) {
                WeatherDetailRow(
                    label = context.getString(R.string.humidity),
                    value = "${weatherData.humidity}%",
                    typography = t
                )
                Spacer(modifier = GlanceModifier.height(6.dp))
                WeatherDetailRow(
                    label = context.getString(R.string.wind),
                    value = "${weatherData.windSpeed} ${weatherData.windDirection}",
                    typography = t
                )
                Spacer(modifier = GlanceModifier.height(6.dp))
                WeatherDetailRow(
                    label = context.getString(R.string.uv_index),
                    value = when (weatherData.uvIndex) {
                        in 0.0..2.9 -> context.getString(R.string.uv_index_low)
                        in 3.0..5.9 -> context.getString(R.string.uv_index_medium)
                        in 6.0..7.9 -> context.getString(R.string.uv_index_high)
                        in 8.0..10.9 -> context.getString(R.string.uv_index_very_high)
                        else -> context.getString(R.string.uv_index_extreme)
                    },
                    typography = t
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(16.dp))

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ForecastDayFull(
                dayName = weatherData.day1Name,
                icon = weatherData.day1IconBitmap,
                typography = t
            )
            Spacer(modifier = GlanceModifier.width(40.dp))
            ForecastDayFull(
                dayName = weatherData.day2Name,
                icon = weatherData.day2IconBitmap,
                typography = t
            )
        }
    }
}

@Composable
fun WeatherWithAnalogClockContent(weatherData: WeatherWidgetData?) {
    val t = rememberWidgetTypography()

    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(actionRunCallback<OpenAppCallback>()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (weatherData != null) {
            Column(
                modifier = GlanceModifier.fillMaxHeight().defaultWeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WeatherIcon(bitmap = weatherData.currentIconBitmap, size = t.weatherIconSize)
                    Spacer(GlanceModifier.width(4.dp))
                    Text(
                        text = weatherData.currentTemp,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = t.tempSize,
                            color = ColorProvider(Color.White, Color.White)
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = GlanceModifier.width(t.locationIconSize + 4.dp))
                    Text(
                        text = weatherData.currentCondition,
                        style = TextStyle(
                            fontSize = t.conditionSize,
                            color = ColorProvider(Color(0xCCFFFFFF), Color(0xCCFFFFFF))
                        ),
                        maxLines = 1
                    )
                }

                Spacer(modifier = GlanceModifier.height(2.dp))

                LocationRow(location = weatherData.location, typography = t)
            }
        } else {
            Box(modifier = GlanceModifier.defaultWeight(), contentAlignment = Alignment.Center) {
                LoadingWidget()
            }
        }

        Box(
            modifier = GlanceModifier.fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            AndroidRemoteViews(
                remoteViews = RemoteViews(
                    LocalContext.current.packageName,
                    R.layout.widget_rtc_analog_clock
                )
            )
        }
    }
}

@Composable
fun WeatherWithDigitalClockContent(weatherData: WeatherWidgetData?) {
    val t = rememberWidgetTypography()

    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(actionRunCallback<OpenAppCallback>()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (weatherData != null) {
            Column(
                modifier = GlanceModifier.fillMaxHeight().defaultWeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WeatherIcon(bitmap = weatherData.currentIconBitmap, size = t.weatherIconSize)
                    Spacer(GlanceModifier.width(4.dp))
                    Text(
                        text = weatherData.currentTemp,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = t.tempSize,
                            color = ColorProvider(Color.White, Color.White)
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = GlanceModifier.width(t.locationIconSize + 4.dp))
                    Text(
                        text = weatherData.currentCondition,
                        style = TextStyle(
                            fontSize = t.conditionSize,
                            color = ColorProvider(Color(0xCCFFFFFF), Color(0xCCFFFFFF))
                        ),
                        maxLines = 1
                    )
                }

                Spacer(modifier = GlanceModifier.height(2.dp))

                LocationRow(location = weatherData.location, typography = t)
            }
        } else {
            Box(modifier = GlanceModifier.defaultWeight(), contentAlignment = Alignment.Center) {
                LoadingWidget()
            }
        }

        Box(
            modifier = GlanceModifier.fillMaxHeight().wrapContentWidth(),
            contentAlignment = Alignment.Center
        ) {
            AndroidRemoteViews(
                remoteViews = RemoteViews(
                    LocalContext.current.packageName,
                    R.layout.widget_rtc_digital_clock
                )
            )
        }
    }
}

// ============================================================
//  COMPONENTES REUTILIZABLES
// ============================================================

/**
 * Icono del clima — muestra fallback si el bitmap es null.
 * Centralizado para no repetir la lógica if/else en cada widget.
 */
@Composable
internal fun WeatherIcon(bitmap: Bitmap?, size: Dp) {
    Image(
        provider = if (bitmap != null) ImageProvider(bitmap)
        else ImageProvider(R.drawable.ic_weather_app_icon),
        contentDescription = "Weather icon",
        modifier = GlanceModifier.size(size)
    )
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
            contentDescription = "Loading",
            modifier = GlanceModifier.size(44.dp)
        )
        Spacer(modifier = GlanceModifier.height(12.dp))
        Text(
            text = stringResource(R.string.loading_dialog_text),
            style = TextStyle(
                fontSize = 13.sp,
                color = ColorProvider(Color(0xCCFFFFFF), Color(0xCCFFFFFF))
            )
        )
    }
}

@Composable
fun WeatherWidgetErrorContent(message: String) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.bg_widget_glass))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                provider = ImageProvider(R.drawable.ic_no_weather_data),
                contentDescription = "Error",
                modifier = GlanceModifier.size(36.dp)
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = message,
                style = TextStyle(
                    color = ColorProvider(Color(0xCCFFFFFF), Color(0xCCFFFFFF)),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                ),
                maxLines = 2
            )
        }
    }
}

// ── LocationRow ──────────────────────────────────────────────────
@Composable
internal fun LocationRow(
    location: String,
    typography: WidgetTypography
) {
    Row(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_locations_widget),
            contentDescription = "Location",
            modifier = GlanceModifier.size(typography.locationIconSize)
        )
        Spacer(modifier = GlanceModifier.width(4.dp))
        Text(
            text = location,
            style = TextStyle(
                fontSize = typography.locationSize,
                color = ColorProvider(Color(0xCCFFFFFF), Color(0xCCFFFFFF))
            ),
            maxLines = 1
        )
    }
}

// ── WeatherDetailRow ─────────────────────────────────────────────
@Composable
internal fun WeatherDetailRow(
    label: String,
    value: String,
    typography: WidgetTypography
) {
    Row(
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = TextStyle(
                fontSize = typography.detailSize,
                fontWeight = FontWeight.Medium,
                color = ColorProvider(Color(0xCCFFFFFF), Color(0xCCFFFFFF))
            )
        )
        Spacer(modifier = GlanceModifier.width(6.dp))
        Text(
            text = value,
            style = TextStyle(
                fontSize = typography.detailSize,
                color = ColorProvider(Color.White, Color.White)
            )
        )
    }
}

// ── ForecastDayCompact ───────────────────────────────────────────
@Composable
internal fun ForecastDayCompact(
    dayName: String,
    icon: Bitmap?,
    typography: WidgetTypography
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = dayName,
            style = TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = typography.labelSize,
                color = ColorProvider(Color(0xCCFFFFFF), Color(0xCCFFFFFF))
            )
        )
        Spacer(modifier = GlanceModifier.height(3.dp))
        WeatherIcon(bitmap = icon, size = typography.forecastIconSize)
    }
}

// ── ForecastDayFull ──────────────────────────────────────────────
@Composable
internal fun ForecastDayFull(
    dayName: String,
    icon: Bitmap?,
    typography: WidgetTypography
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = dayName,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = typography.labelSize,
                color = ColorProvider(Color(0xCCFFFFFF), Color(0xCCFFFFFF))
            )
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        WeatherIcon(bitmap = icon, size = typography.forecastIconSize)
    }
}