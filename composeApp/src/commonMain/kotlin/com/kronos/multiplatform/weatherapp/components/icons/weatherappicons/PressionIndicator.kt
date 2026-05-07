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

public val WeatherAppIcons.PressionIndicator: ImageVector
    get() {
        if (_root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons._pressionIndicator != null) {
            return _root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons._pressionIndicator!!
        }
        _root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons._pressionIndicator = Builder(name = "PressionIndicator", defaultWidth = 800.0.dp,
                defaultHeight = 800.0.dp, viewportWidth = 512.0f, viewportHeight = 512.0f).apply {
            path(fill = SolidColor(Color(0xFFF2F2F2)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(256.0f, 512.0f)
                curveTo(114.51f, 512.0f, 0.0f, 397.5f, 0.0f, 256.0f)
                curveTo(0.0f, 114.51f, 114.5f, 0.0f, 256.0f, 0.0f)
                curveToRelative(141.49f, 0.0f, 256.0f, 114.5f, 256.0f, 256.0f)
                curveTo(512.0f, 397.49f, 397.5f, 512.0f, 256.0f, 512.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAEB6BD)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(256.0f, 0.0f)
                verticalLineToRelative(512.0f)
                curveToRelative(141.5f, 0.0f, 256.0f, -114.51f, 256.0f, -256.0f)
                curveTo(512.0f, 114.5f, 397.49f, 0.0f, 256.0f, 0.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF40596B)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(256.0f, 455.11f)
                curveTo(146.21f, 455.11f, 56.89f, 365.79f, 56.89f, 256.0f)
                reflectiveCurveTo(146.21f, 56.89f, 256.0f, 56.89f)
                reflectiveCurveTo(455.11f, 146.21f, 455.11f, 256.0f)
                reflectiveCurveTo(365.79f, 455.11f, 256.0f, 455.11f)
                close()
            }
            path(fill = SolidColor(Color(0xFF364C5C)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(256.0f, 56.89f)
                verticalLineToRelative(398.22f)
                curveToRelative(109.79f, 0.0f, 199.11f, -89.32f, 199.11f, -199.11f)
                reflectiveCurveTo(365.79f, 56.89f, 256.0f, 56.89f)
                close()
            }
            path(fill = SolidColor(Color(0xFF2C3E4E)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(280.59f, 256.0f)
                curveToRelative(0.0f, 9.43f, 7.64f, 17.07f, 17.07f, 17.07f)
                horizontalLineToRelative(156.72f)
                curveToRelative(0.99f, -11.54f, 0.99f, -22.58f, 0.0f, -34.13f)
                horizontalLineTo(297.65f)
                curveTo(288.23f, 238.93f, 280.59f, 246.57f, 280.59f, 256.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF364C5C)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(231.29f, 238.93f)
                horizontalLineTo(57.63f)
                curveToRelative(-0.99f, 11.54f, -0.99f, 22.58f, 0.0f, 34.13f)
                horizontalLineToRelative(173.66f)
                curveToRelative(9.43f, 0.0f, 17.07f, -7.64f, 17.07f, -17.07f)
                curveTo(248.35f, 246.57f, 240.71f, 238.93f, 231.29f, 238.93f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFD15C)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(228.2f, 283.8f)
                curveToRelative(-26.11f, -26.11f, -16.57f, -70.15f, 17.19f, -83.61f)
                lineToRelative(73.62f, -29.39f)
                curveToRelative(13.9f, -5.55f, 27.72f, 8.29f, 22.18f, 22.18f)
                lineToRelative(-27.68f, 69.33f)
                curveTo(296.21f, 305.8f, 251.65f, 307.25f, 228.2f, 283.8f)
                close()
            }
            path(fill = SolidColor(Color(0xFFF8B64C)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(319.01f, 170.81f)
                lineTo(256.0f, 195.96f)
                verticalLineToRelative(102.19f)
                curveToRelative(21.67f, 3.73f, 45.81f, -6.44f, 57.5f, -35.82f)
                lineToRelative(27.68f, -69.33f)
                curveTo(346.73f, 179.1f, 332.91f, 165.27f, 319.01f, 170.81f)
                close()
            }
        }
        .build()
        return _root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons._pressionIndicator!!
    }

private var _pressionIndicator: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = WeatherAppIcons.PressionIndicator, contentDescription = "")
    }
}
