package com.kronos.multiplatform.weatherapp.components.icons.weatherappicons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.kronos.multiplatform.weatherapp.components.icons.WeatherAppIcons

public val WeatherAppIcons.SnowflakeWeatherIndicator: ImageVector
    get() {
        if (_snowflakeWeatherIndicator != null) {
            return _snowflakeWeatherIndicator!!
        }
        _snowflakeWeatherIndicator = Builder(name = "SnowflakeWeatherIndicator", defaultWidth =
                800.0.dp, defaultHeight = 800.0.dp, viewportWidth = 512.0f, viewportHeight =
                512.0f).apply {
            path(fill = SolidColor(Color(0xFFCCF7F5)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(489.87f, 233.87f)
                horizontalLineToRelative(-74.58f)
                lineToRelative(21.72f, -21.72f)
                curveToRelative(8.64f, -8.64f, 8.64f, -22.65f, 0.0f, -31.29f)
                curveToRelative(-8.64f, -8.64f, -22.65f, -8.64f, -31.29f, 0.0f)
                lineToRelative(-53.02f, 53.02f)
                horizontalLineToRelative(-43.28f)
                lineTo(437.02f, 106.27f)
                curveToRelative(8.64f, -8.64f, 8.64f, -22.65f, 0.0f, -31.29f)
                curveToRelative(-8.64f, -8.64f, -22.65f, -8.64f, -31.29f, 0.0f)
                lineTo(278.13f, 202.58f)
                verticalLineToRelative(-43.28f)
                lineToRelative(53.02f, -53.02f)
                curveToRelative(8.64f, -8.64f, 8.64f, -22.65f, 0.0f, -31.29f)
                curveToRelative(-8.64f, -8.64f, -22.65f, -8.64f, -31.29f, 0.0f)
                lineTo(278.13f, 96.71f)
                verticalLineTo(22.13f)
                curveTo(278.13f, 9.91f, 268.22f, 0.0f, 256.0f, 0.0f)
                curveToRelative(-12.22f, 0.0f, -22.13f, 9.91f, -22.13f, 22.13f)
                verticalLineToRelative(74.58f)
                lineToRelative(-21.73f, -21.73f)
                curveToRelative(-8.64f, -8.64f, -22.65f, -8.64f, -31.29f, 0.0f)
                curveToRelative(-8.64f, 8.64f, -8.64f, 22.65f, 0.0f, 31.29f)
                lineToRelative(53.02f, 53.02f)
                verticalLineToRelative(43.28f)
                lineTo(106.28f, 74.98f)
                curveToRelative(-8.64f, -8.64f, -22.65f, -8.64f, -31.29f, 0.0f)
                reflectiveCurveToRelative(-8.64f, 22.65f, 0.0f, 31.29f)
                lineTo(202.58f, 233.87f)
                horizontalLineToRelative(-43.28f)
                lineToRelative(-53.02f, -53.02f)
                curveToRelative(-8.64f, -8.64f, -22.65f, -8.64f, -31.29f, 0.0f)
                reflectiveCurveToRelative(-8.64f, 22.65f, 0.0f, 31.29f)
                lineToRelative(21.73f, 21.73f)
                horizontalLineTo(22.13f)
                curveToRelative(-12.22f, 0.0f, -22.13f, 9.91f, -22.13f, 22.13f)
                curveToRelative(0.0f, 12.22f, 9.91f, 22.13f, 22.13f, 22.13f)
                horizontalLineToRelative(74.58f)
                lineToRelative(-21.73f, 21.73f)
                curveToRelative(-8.64f, 8.64f, -8.64f, 22.65f, 0.0f, 31.29f)
                curveToRelative(4.32f, 4.32f, 9.98f, 6.48f, 15.65f, 6.48f)
                curveToRelative(5.66f, 0.0f, 11.33f, -2.16f, 15.65f, -6.48f)
                lineToRelative(53.02f, -53.02f)
                horizontalLineToRelative(43.28f)
                lineTo(74.98f, 405.73f)
                curveToRelative(-8.64f, 8.64f, -8.64f, 22.65f, 0.0f, 31.29f)
                curveToRelative(4.32f, 4.32f, 9.98f, 6.48f, 15.65f, 6.48f)
                curveToRelative(5.66f, 0.0f, 11.33f, -2.16f, 15.65f, -6.48f)
                lineToRelative(127.6f, -127.6f)
                verticalLineToRelative(43.28f)
                lineToRelative(-53.02f, 53.02f)
                curveToRelative(-8.64f, 8.64f, -8.64f, 22.65f, 0.0f, 31.29f)
                curveToRelative(8.64f, 8.64f, 22.65f, 8.64f, 31.29f, 0.0f)
                lineToRelative(21.73f, -21.73f)
                verticalLineToRelative(74.58f)
                curveToRelative(0.0f, 12.22f, 9.91f, 22.13f, 22.13f, 22.13f)
                curveToRelative(12.22f, 0.0f, 22.13f, -9.91f, 22.13f, -22.13f)
                verticalLineToRelative(-74.58f)
                lineToRelative(21.73f, 21.73f)
                curveToRelative(4.32f, 4.32f, 9.98f, 6.48f, 15.65f, 6.48f)
                reflectiveCurveToRelative(11.33f, -2.16f, 15.65f, -6.48f)
                curveToRelative(8.64f, -8.64f, 8.64f, -22.65f, 0.0f, -31.29f)
                lineToRelative(-53.02f, -53.02f)
                verticalLineToRelative(-43.28f)
                lineTo(405.73f, 437.02f)
                curveToRelative(4.32f, 4.32f, 9.98f, 6.48f, 15.65f, 6.48f)
                reflectiveCurveToRelative(11.33f, -2.16f, 15.65f, -6.48f)
                curveToRelative(8.64f, -8.64f, 8.64f, -22.65f, 0.0f, -31.29f)
                lineTo(309.42f, 278.13f)
                horizontalLineToRelative(43.28f)
                lineToRelative(53.02f, 53.02f)
                curveToRelative(4.32f, 4.32f, 9.98f, 6.48f, 15.65f, 6.48f)
                curveToRelative(5.66f, 0.0f, 11.33f, -2.16f, 15.65f, -6.48f)
                curveToRelative(8.64f, -8.64f, 8.64f, -22.65f, 0.0f, -31.29f)
                lineToRelative(-21.73f, -21.73f)
                horizontalLineToRelative(74.59f)
                curveToRelative(12.22f, 0.0f, 22.13f, -9.91f, 22.13f, -22.13f)
                curveTo(512.0f, 243.78f, 502.09f, 233.87f, 489.87f, 233.87f)
                close()
            }
            path(fill = SolidColor(Color(0xFF74D6D0)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(489.87f, 233.87f)
                horizontalLineToRelative(-74.58f)
                lineToRelative(21.72f, -21.72f)
                curveToRelative(8.64f, -8.64f, 8.64f, -22.65f, 0.0f, -31.29f)
                curveToRelative(-8.64f, -8.64f, -22.65f, -8.64f, -31.29f, 0.0f)
                lineToRelative(-53.02f, 53.02f)
                horizontalLineToRelative(-43.28f)
                lineTo(437.02f, 106.27f)
                curveToRelative(8.64f, -8.64f, 8.64f, -22.65f, 0.0f, -31.29f)
                curveToRelative(-8.64f, -8.64f, -22.65f, -8.64f, -31.29f, 0.0f)
                lineTo(278.13f, 202.58f)
                verticalLineToRelative(-43.28f)
                lineToRelative(53.02f, -53.02f)
                curveToRelative(8.64f, -8.64f, 8.64f, -22.65f, 0.0f, -31.29f)
                curveToRelative(-8.64f, -8.64f, -22.65f, -8.64f, -31.29f, 0.0f)
                lineTo(278.13f, 96.71f)
                verticalLineTo(22.13f)
                curveTo(278.13f, 9.91f, 268.22f, 0.0f, 256.0f, 0.0f)
                verticalLineToRelative(512.0f)
                curveToRelative(12.22f, 0.0f, 22.13f, -9.91f, 22.13f, -22.13f)
                verticalLineToRelative(-74.58f)
                lineToRelative(21.73f, 21.73f)
                curveToRelative(4.32f, 4.32f, 9.98f, 6.48f, 15.65f, 6.48f)
                reflectiveCurveToRelative(11.33f, -2.16f, 15.65f, -6.48f)
                curveToRelative(8.64f, -8.64f, 8.64f, -22.65f, 0.0f, -31.29f)
                lineToRelative(-53.02f, -53.02f)
                verticalLineToRelative(-43.28f)
                lineToRelative(127.6f, 127.6f)
                curveToRelative(4.32f, 4.32f, 9.98f, 6.48f, 15.65f, 6.48f)
                reflectiveCurveToRelative(11.33f, -2.16f, 15.65f, -6.48f)
                curveToRelative(8.64f, -8.64f, 8.64f, -22.65f, 0.0f, -31.29f)
                lineTo(309.42f, 278.13f)
                horizontalLineToRelative(43.28f)
                lineToRelative(53.02f, 53.02f)
                curveToRelative(4.32f, 4.32f, 9.98f, 6.48f, 15.65f, 6.48f)
                curveToRelative(5.66f, 0.0f, 11.33f, -2.16f, 15.65f, -6.48f)
                curveToRelative(8.64f, -8.64f, 8.64f, -22.65f, 0.0f, -31.29f)
                lineToRelative(-21.73f, -21.73f)
                horizontalLineToRelative(74.59f)
                curveToRelative(12.22f, 0.0f, 22.13f, -9.91f, 22.13f, -22.13f)
                curveTo(512.0f, 243.78f, 502.09f, 233.87f, 489.87f, 233.87f)
                close()
            }
        }
        .build()
        return _snowflakeWeatherIndicator!!
    }

private var _snowflakeWeatherIndicator: ImageVector? = null
