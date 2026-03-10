package com.kronos.multiplatform.weatherapp.components.icons.weatherappicons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.tooling.preview.Preview

public val WeatherAppIcons.NoWeatherIndicator: ImageVector
    get() {
        if (_root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons._noWeatherIndicator != null) {
            return _root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons._noWeatherIndicator!!
        }
        _root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons._noWeatherIndicator = Builder(name = "NoWeatherIndicator", defaultWidth = 800.0.dp,
                defaultHeight = 800.0.dp, viewportWidth = 16.001f, viewportHeight = 16.001f).apply {
            path(fill = SolidColor(Color(0xFF808080)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(8.64f, 0.668f)
                curveTo(6.482f, 0.67f, 4.648f, 1.999f, 3.772f, 3.9f)
                lineToRelative(-0.061f, 0.13f)
                lineToRelative(-0.139f, 0.01f)
                curveTo(1.578f, 4.184f, 0.0f, 5.91f, 0.0f, 8.008f)
                curveTo(0.0f, 10.201f, 1.716f, 12.0f, 3.838f, 12.0f)
                horizontalLineTo(6.0f)
                verticalLineToRelative(-1.0f)
                horizontalLineTo(3.838f)
                curveTo(2.281f, 11.0f, 1.0f, 9.679f, 1.0f, 8.008f)
                curveToRelative(0.0f, -1.597f, 1.184f, -2.866f, 2.644f, -2.973f)
                lineToRelative(0.723f, -0.05f)
                lineToRelative(0.31f, -0.663f)
                verticalLineToRelative(-0.004f)
                curveToRelative(0.729f, -1.58f, 2.213f, -2.649f, 3.961f, -2.65f)
                curveToRelative(2.368f, 0.002f, 4.292f, 1.952f, 4.405f, 4.447f)
                verticalLineToRelative(0.01f)
                lineToRelative(0.05f, 0.776f)
                lineToRelative(0.704f, 0.296f)
                arcToRelative(1.944f, 1.944f, 0.0f, false, true, 1.195f, 1.815f)
                curveToRelative(0.01f, 1.114f, -0.846f, 1.988f, -1.871f, 1.988f)
                horizontalLineTo(9.998f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(3.123f)
                curveToRelative(1.591f, 0.0f, 2.877f, -1.35f, 2.877f, -2.988f)
                arcToRelative(2.958f, 2.958f, 0.0f, false, false, -1.805f, -2.735f)
                lineToRelative(-0.136f, -0.058f)
                lineToRelative(-0.01f, -0.149f)
                curveTo(13.911f, 3.077f, 11.56f, 0.67f, 8.642f, 0.668f)
                close()
            }
            path(fill = SolidColor(Color(0xFFda1636)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(7.291f, 6.0f)
                verticalLineToRelative(2.662f)
                curveToRelative(0.0f, 0.793f, 0.03f, 1.517f, 0.086f, 2.168f)
                curveToRelative(0.057f, 0.642f, 0.128f, 1.284f, 0.213f, 1.926f)
                horizontalLineToRelative(0.82f)
                curveToRelative(0.085f, -0.642f, 0.157f, -1.284f, 0.213f, -1.926f)
                curveToRelative(0.057f, -0.651f, 0.086f, -1.375f, 0.086f, -2.168f)
                lineTo(8.709f, 6.0f)
                close()
                moveTo(8.001f, 14.102f)
                curveToRelative(-0.294f, 0.0f, -0.524f, 0.094f, -0.694f, 0.283f)
                arcToRelative(0.965f, 0.965f, 0.0f, false, false, -0.256f, 0.666f)
                curveToRelative(0.0f, 0.255f, 0.086f, 0.477f, 0.256f, 0.666f)
                curveToRelative(0.17f, 0.189f, 0.4f, 0.283f, 0.693f, 0.283f)
                curveToRelative(0.293f, 0.0f, 0.524f, -0.094f, 0.694f, -0.283f)
                arcToRelative(0.965f, 0.965f, 0.0f, false, false, 0.255f, -0.666f)
                arcToRelative(0.965f, 0.965f, 0.0f, false, false, -0.255f, -0.666f)
                curveToRelative(-0.17f, -0.19f, -0.401f, -0.283f, -0.694f, -0.283f)
                close()
            }
        }
        .build()
        return _root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons._noWeatherIndicator!!
    }

private var _noWeatherIndicator: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = WeatherAppIcons.NoWeatherIndicator, contentDescription = "")
    }
}
