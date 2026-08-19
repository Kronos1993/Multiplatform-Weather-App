package com.kronos.multiplatform.weatherapp.widget.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.LocalSize

// ── Breakpoints de tamaño ────────────────────────────────────────
private val SMALL_WIDGET_WIDTH  = 120.dp
private val MEDIUM_WIDGET_WIDTH = 200.dp
private val SMALL_WIDGET_HEIGHT = 100.dp

enum class WidgetSizeClass { SMALL, MEDIUM, LARGE }

data class WidgetTypography(
    val tempSize: TextUnit,
    val conditionSize: TextUnit,
    val locationSize: TextUnit,
    val labelSize: TextUnit,
    val detailSize: TextUnit,
    val locationIconSize: Dp,
    val weatherIconSize: Dp,
    val forecastIconSize: Dp,
    val clockDigitalDateSize: TextUnit,
    val clockDigitalTimeSize: TextUnit,
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
    clockDigitalDateSize = 14.sp,
    clockDigitalTimeSize = 24.sp,
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
    clockDigitalDateSize = 16.sp,
    clockDigitalTimeSize = 30.sp,
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
    clockDigitalDateSize = 18.sp,
    clockDigitalTimeSize = 36.sp,
)

// ── Tokens de espaciado por tamaño ───────────────────────────────
data class WidgetSpacing(
    val contentPaddingHorizontal: Dp,
    val contentPaddingVertical: Dp,
    val iconTextGap: Dp,
    val microGap: Dp,
    val sectionGap: Dp,
    val itemSpacing: Dp,
)

private val SmallSpacing = WidgetSpacing(
    contentPaddingHorizontal = 12.dp,
    contentPaddingVertical   = 8.dp,
    iconTextGap    = 4.dp,
    microGap       = 2.dp,
    sectionGap     = 4.dp,
    itemSpacing    = 10.dp,
)

private val MediumSpacing = WidgetSpacing(
    contentPaddingHorizontal = 14.dp,
    contentPaddingVertical   = 12.dp,
    iconTextGap    = 6.dp,
    microGap       = 3.dp,
    sectionGap     = 8.dp,
    itemSpacing    = 18.dp,
)

private val LargeSpacing = WidgetSpacing(
    contentPaddingHorizontal = 16.dp,
    contentPaddingVertical   = 14.dp,
    iconTextGap    = 8.dp,
    microGap       = 4.dp,
    sectionGap     = 12.dp,
    itemSpacing    = 32.dp,
)

// ── Resolución de tamaño (fuente única de verdad) ─────────────────
private fun resolveWidgetSizeClass(size: DpSize): WidgetSizeClass = when {
    size.width < SMALL_WIDGET_WIDTH || size.height < SMALL_WIDGET_HEIGHT -> WidgetSizeClass.SMALL
    size.width < MEDIUM_WIDGET_WIDTH -> WidgetSizeClass.MEDIUM
    else -> WidgetSizeClass.LARGE
}

@Composable
fun rememberWidgetTypography(): WidgetTypography = when (resolveWidgetSizeClass(LocalSize.current)) {
    WidgetSizeClass.SMALL -> SmallTypography
    WidgetSizeClass.MEDIUM -> MediumTypography
    WidgetSizeClass.LARGE -> LargeTypography
}

@Composable
fun rememberWidgetSpacing(): WidgetSpacing = when (resolveWidgetSizeClass(LocalSize.current)) {
    WidgetSizeClass.SMALL -> SmallSpacing
    WidgetSizeClass.MEDIUM -> MediumSpacing
    WidgetSizeClass.LARGE -> LargeSpacing
}

@Composable
fun rememberWidgetSizeClass(): WidgetSizeClass = resolveWidgetSizeClass(LocalSize.current)