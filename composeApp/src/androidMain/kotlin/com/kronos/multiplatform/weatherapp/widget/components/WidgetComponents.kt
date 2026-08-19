package com.kronos.multiplatform.weatherapp.widget.components

import android.content.Context
import android.graphics.Bitmap
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
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
    val s = rememberWidgetSpacing()
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.bg_widget_glass))
            .padding(horizontal = s.contentPaddingHorizontal, vertical = s.contentPaddingVertical),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

// Dispatches to the plain content composable matching the widget's live size, letting an
// already-placed Small/Medium/Large instance transform layout as the user drag-resizes it —
// not just rescale tokens within one fixed layout. Thresholds mirror the real declared
// minWidth/minHeight from widget_provider_info_medium.xml (256x60) and
// widget_provider_info_large.xml (256x120): BaseWeatherGlanceWidget's SizeMode.Responsive only
// ever reports one of 4 known DpSize points (see WIDGET_RESPONSIVE_SIZES), so a plain
// dimension comparison is sufficient — this intentionally does NOT reuse
// WidgetTheme.kt's resolveWidgetSizeClass (tuned for font/icon/spacing scaling, not layout
// choice: e.g. Medium's own default of 256x60 resolves SMALL there via its short-height rule,
// which would wrongly dispatch to SmallWeatherWidgetContent here).
@Composable
fun AdaptiveWeatherWidgetContent(weatherData: WeatherWidgetData) {
    val size = LocalSize.current
    when {
        size.width >= 256.dp && size.height >= 120.dp -> LargeWeatherWidgetContent(weatherData, LocalContext.current)
        size.width >= 256.dp && size.height >= 60.dp -> MediumWeatherWidgetContent(weatherData)
        else -> SmallWeatherWidgetContent(weatherData)
    }
}

@Composable
fun SmallWeatherWidgetContent(weatherData: WeatherWidgetData) {
    val t = rememberWidgetTypography()
    val s = rememberWidgetSpacing()

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
            Spacer(modifier = GlanceModifier.width(s.iconTextGap))
            Text(
                text = weatherData.currentTemp,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = t.tempSize,
                    color = ColorProvider(Color.White, Color.White)
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(s.sectionGap))

        Text(
            text = weatherData.currentCondition,
            style = TextStyle(
                fontSize = t.conditionSize,
                color = ColorProvider(Color(0xCCFFFFFF), Color(0xCCFFFFFF))
            ),
            maxLines = 1
        )

        Spacer(modifier = GlanceModifier.height(s.sectionGap))

        LocationRow(location = weatherData.location, typography = t)
    }
}

@Composable
fun MediumWeatherWidgetContent(weatherData: WeatherWidgetData) {
    val t = rememberWidgetTypography()
    val s = rememberWidgetSpacing()

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(actionRunCallback<OpenAppCallback>()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LocationRow(location = weatherData.location, typography = t)

        Spacer(modifier = GlanceModifier.height(s.sectionGap))

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
                Spacer(modifier = GlanceModifier.width(s.iconTextGap))
                Text(
                    text = weatherData.currentTemp,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = t.tempSize,
                        color = ColorProvider(Color.White, Color.White)
                    )
                )
            }

            Spacer(modifier = GlanceModifier.width(s.itemSpacing))

            ForecastDayCompact(
                dayName = weatherData.day1Name,
                icon = weatherData.day1IconBitmap,
                typography = t
            )

            Spacer(modifier = GlanceModifier.width(s.itemSpacing))

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
    val s = rememberWidgetSpacing()

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

        Spacer(modifier = GlanceModifier.height(s.sectionGap))

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
                Spacer(modifier = GlanceModifier.height(s.microGap))
                Text(
                    text = weatherData.currentTemp,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = t.tempSize,
                        color = ColorProvider(Color.White, Color.White)
                    )
                )
                Spacer(modifier = GlanceModifier.height(s.microGap))
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

            Spacer(modifier = GlanceModifier.width(s.itemSpacing))

            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.Start
            ) {
                WeatherDetailRow(
                    label = context.getString(R.string.humidity),
                    value = "${weatherData.humidity}%",
                    typography = t
                )
                Spacer(modifier = GlanceModifier.height(s.sectionGap))
                WeatherDetailRow(
                    label = context.getString(R.string.wind),
                    value = "${weatherData.windSpeed} ${weatherData.windDirection}",
                    typography = t
                )
                Spacer(modifier = GlanceModifier.height(s.sectionGap))
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

        Spacer(modifier = GlanceModifier.height(s.sectionGap))

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ForecastDayFull(
                dayName = weatherData.day1Name,
                icon = weatherData.day1IconBitmap,
                typography = t
            )
            Spacer(modifier = GlanceModifier.width(s.itemSpacing))
            ForecastDayFull(
                dayName = weatherData.day2Name,
                icon = weatherData.day2IconBitmap,
                typography = t
            )
        }
    }
}

// Maps the live size class to a dedicated analog-clock RemoteViews layout resource — the
// AnalogClock view's dial/hands are vector drawables driven by fixed layout_width/layout_height
// dp values (not a Compose-tokenized size), so scaling it live means picking among discrete
// pre-built layout variants rather than applying a single runtime value. Small keeps the
// original widget_rtc_analog_clock.xml unchanged (zero risk to the default appearance).
private fun analogClockLayoutRes(sizeClass: WidgetSizeClass): Int = when (sizeClass) {
    WidgetSizeClass.SMALL -> R.layout.widget_rtc_analog_clock
    WidgetSizeClass.MEDIUM -> R.layout.widget_rtc_analog_clock_medium
    WidgetSizeClass.LARGE -> R.layout.widget_rtc_analog_clock_large
}

@Composable
fun WeatherWithAnalogClockContent(weatherData: WeatherWidgetData?) {
    val t = rememberWidgetTypography()
    val s = rememberWidgetSpacing()
    val sizeClass = rememberWidgetSizeClass()
    val size = LocalSize.current
    val conditionMaxLines = if (size.height >= 100.dp) 2 else 1

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
                    Spacer(GlanceModifier.width(s.iconTextGap))
                    Text(
                        text = weatherData.currentTemp,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = t.tempSize,
                            color = ColorProvider(Color.White, Color.White)
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(s.microGap))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = GlanceModifier.width(t.locationIconSize + s.iconTextGap))
                    Text(
                        text = weatherData.currentCondition,
                        style = TextStyle(
                            fontSize = t.conditionSize,
                            color = ColorProvider(Color(0xCCFFFFFF), Color(0xCCFFFFFF))
                        ),
                        maxLines = conditionMaxLines
                    )
                }

                Spacer(modifier = GlanceModifier.height(s.microGap))

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
                    analogClockLayoutRes(sizeClass)
                )
            )
        }
    }
}

@Composable
fun WeatherWithDigitalClockContent(weatherData: WeatherWidgetData?) {
    val t = rememberWidgetTypography()
    val s = rememberWidgetSpacing()
    val size = LocalSize.current
    val conditionMaxLines = if (size.height >= 100.dp) 2 else 1

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
                    Spacer(GlanceModifier.width(s.iconTextGap))
                    Text(
                        text = weatherData.currentTemp,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = t.tempSize,
                            color = ColorProvider(Color.White, Color.White)
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(s.microGap))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = GlanceModifier.width(t.locationIconSize + s.iconTextGap))
                    Text(
                        text = weatherData.currentCondition,
                        style = TextStyle(
                            fontSize = t.conditionSize,
                            color = ColorProvider(Color(0xCCFFFFFF), Color(0xCCFFFFFF))
                        ),
                        maxLines = conditionMaxLines
                    )
                }

                Spacer(modifier = GlanceModifier.height(s.microGap))

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
                ).apply {
                    setTextViewTextSize(R.id.dateClock, TypedValue.COMPLEX_UNIT_SP, t.clockDigitalDateSize.value)
                    setTextViewTextSize(R.id.textClock, TypedValue.COMPLEX_UNIT_SP, t.clockDigitalTimeSize.value)
                }
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
    val t = rememberWidgetTypography()
    val s = rememberWidgetSpacing()
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_no_weather_data),
            contentDescription = "Loading",
            modifier = GlanceModifier.size(t.weatherIconSize)
        )
        Spacer(modifier = GlanceModifier.height(s.sectionGap))
        Text(
            text = stringResource(R.string.loading_dialog_text),
            style = TextStyle(
                fontSize = t.conditionSize,
                color = ColorProvider(Color(0xCCFFFFFF), Color(0xCCFFFFFF))
            )
        )
    }
}

@Composable
fun WeatherWidgetErrorContent(message: String) {
    val t = rememberWidgetTypography()
    val s = rememberWidgetSpacing()
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.bg_widget_glass))
            .padding(horizontal = s.contentPaddingHorizontal, vertical = s.contentPaddingVertical),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                provider = ImageProvider(R.drawable.ic_no_weather_data),
                contentDescription = "Error",
                modifier = GlanceModifier.size(t.weatherIconSize)
            )
            Spacer(modifier = GlanceModifier.height(s.sectionGap))
            Text(
                text = message,
                style = TextStyle(
                    color = ColorProvider(Color(0xCCFFFFFF), Color(0xCCFFFFFF)),
                    fontSize = t.conditionSize,
                    textAlign = TextAlign.Center
                ),
                maxLines = 2
            )
        }
    }
}

@Composable
internal fun LocationRow(
    location: String,
    typography: WidgetTypography
) {
    val s = rememberWidgetSpacing()
    Row(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_locations_widget),
            contentDescription = "Location",
            modifier = GlanceModifier.size(typography.locationIconSize)
        )
        Spacer(modifier = GlanceModifier.width(s.iconTextGap))
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

@Composable
internal fun WeatherDetailRow(
    label: String,
    value: String,
    typography: WidgetTypography
) {
    val s = rememberWidgetSpacing()
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
        Spacer(modifier = GlanceModifier.width(s.iconTextGap))
        Text(
            text = value,
            style = TextStyle(
                fontSize = typography.detailSize,
                color = ColorProvider(Color.White, Color.White)
            )
        )
    }
}

@Composable
internal fun ForecastDayCompact(
    dayName: String,
    icon: Bitmap?,
    typography: WidgetTypography
) {
    val s = rememberWidgetSpacing()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = dayName,
            style = TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = typography.labelSize,
                color = ColorProvider(Color(0xCCFFFFFF), Color(0xCCFFFFFF))
            )
        )
        Spacer(modifier = GlanceModifier.height(s.microGap))
        WeatherIcon(bitmap = icon, size = typography.forecastIconSize)
    }
}

@Composable
internal fun ForecastDayFull(
    dayName: String,
    icon: Bitmap?,
    typography: WidgetTypography
) {
    val s = rememberWidgetSpacing()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = dayName,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = typography.labelSize,
                color = ColorProvider(Color(0xCCFFFFFF), Color(0xCCFFFFFF))
            )
        )
        Spacer(modifier = GlanceModifier.height(s.microGap))
        WeatherIcon(bitmap = icon, size = typography.forecastIconSize)
    }
}