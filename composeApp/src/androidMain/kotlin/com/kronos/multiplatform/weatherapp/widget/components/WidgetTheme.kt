package com.kronos.multiplatform.weatherapp.widget.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.LocalSize

// ── Breakpoints de tamaño ────────────────────────────────────────
private val SMALL_WIDGET_WIDTH  = 120.dp
private val MEDIUM_WIDGET_WIDTH = 200.dp
private val SMALL_WIDGET_HEIGHT = 100.dp

enum class WidgetSizeClass { SMALL, MEDIUM, LARGE }

// ── Tokens de tipografía por tamaño ─────────────────────────────
data class WidgetTypography(
    val tempSize: TextUnit,
    val conditionSize: TextUnit,
    val locationSize: TextUnit,
    val labelSize: TextUnit,
    val detailSize: TextUnit,
    val locationIconSize: Dp,
    val weatherIconSize: Dp,
    val forecastIconSize: Dp,
)

private val SmallTypography = WidgetTypography(
    tempSize         = 18.sp,
    conditionSize    = 15.sp,
    locationSize     = 14.sp,
    labelSize        = 14.sp,
    detailSize       = 14.sp,
    locationIconSize = 8.dp,
    weatherIconSize  = 28.dp,
    forecastIconSize = 22.dp,
)

private val MediumTypography = WidgetTypography(
    tempSize         = 22.sp,
    conditionSize    = 17.sp,
    locationSize     = 16.sp,
    labelSize        = 15.sp,
    detailSize       = 17.sp,
    locationIconSize = 10.dp,
    weatherIconSize  = 36.dp,
    forecastIconSize = 28.dp,
)

private val LargeTypography = WidgetTypography(
    tempSize         = 28.sp,
    conditionSize    = 18.sp,
    locationSize     = 18.sp,
    labelSize        = 17.sp,
    detailSize       = 18.sp,
    locationIconSize = 13.dp,
    weatherIconSize  = 52.dp,
    forecastIconSize = 38.dp,
)

// ── Resolución automática según tamaño del widget ────────────────
@Composable
fun rememberWidgetTypography(): WidgetTypography {
    val size = LocalSize.current
    return when {
        size.width < SMALL_WIDGET_WIDTH || size.height < SMALL_WIDGET_HEIGHT -> SmallTypography
        size.width < MEDIUM_WIDGET_WIDTH -> MediumTypography
        else -> LargeTypography
    }
}

@Composable
fun rememberWidgetSizeClass(): WidgetSizeClass {
    val size = LocalSize.current
    return when {
        size.width < SMALL_WIDGET_WIDTH || size.height < SMALL_WIDGET_HEIGHT -> WidgetSizeClass.SMALL
        size.width < MEDIUM_WIDGET_WIDTH -> WidgetSizeClass.MEDIUM
        else -> WidgetSizeClass.LARGE
    }
}