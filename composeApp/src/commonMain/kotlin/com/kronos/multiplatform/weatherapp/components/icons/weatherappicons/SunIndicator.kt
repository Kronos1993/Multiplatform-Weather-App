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

public val WeatherAppIcons.SunIndicator: ImageVector
    get() {
        if (_root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons._sunIndicator != null) {
            return _root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons._sunIndicator!!
        }
        _root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons._sunIndicator = Builder(name = "SunIndicator", defaultWidth = 800.0.dp, defaultHeight =
                800.0.dp, viewportWidth = 32.0f, viewportHeight = 32.0f).apply {
            path(fill = SolidColor(Color(0xFFFFC10A)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(26.0f, 16.0f)
                curveToRelative(0.0f, 5.5f, -4.5f, 10.0f, -10.0f, 10.0f)
                reflectiveCurveTo(6.0f, 21.5f, 6.0f, 16.0f)
                reflectiveCurveTo(10.5f, 6.0f, 16.0f, 6.0f)
                reflectiveCurveTo(26.0f, 10.5f, 26.0f, 16.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFF44236)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(16.0f, 1.0f)
                curveToRelative(-0.6f, 0.0f, -1.0f, 0.4f, -1.0f, 1.0f)
                verticalLineToRelative(2.0f)
                curveToRelative(0.0f, 0.6f, 0.4f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.4f, 1.0f, -1.0f)
                verticalLineTo(2.0f)
                curveTo(17.0f, 1.4f, 16.6f, 1.0f, 16.0f, 1.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFF44236)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(16.0f, 27.0f)
                curveToRelative(-0.6f, 0.0f, -1.0f, 0.4f, -1.0f, 1.0f)
                verticalLineToRelative(2.0f)
                curveToRelative(0.0f, 0.6f, 0.4f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.4f, 1.0f, -1.0f)
                verticalLineToRelative(-2.0f)
                curveTo(17.0f, 27.4f, 16.6f, 27.0f, 16.0f, 27.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFF44236)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(30.0f, 15.0f)
                horizontalLineToRelative(-2.0f)
                curveToRelative(-0.6f, 0.0f, -1.0f, 0.4f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.4f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(0.6f, 0.0f, 1.0f, -0.4f, 1.0f, -1.0f)
                reflectiveCurveTo(30.6f, 15.0f, 30.0f, 15.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFF44236)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(4.0f, 15.0f)
                horizontalLineTo(2.0f)
                curveToRelative(-0.6f, 0.0f, -1.0f, 0.4f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.4f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(0.6f, 0.0f, 1.0f, -0.4f, 1.0f, -1.0f)
                reflectiveCurveTo(4.6f, 15.0f, 4.0f, 15.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFF44236)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(25.2f, 5.4f)
                lineToRelative(-1.4f, 1.4f)
                curveToRelative(-0.4f, 0.4f, -0.4f, 1.0f, 0.0f, 1.4f)
                curveToRelative(0.2f, 0.2f, 0.5f, 0.3f, 0.7f, 0.3f)
                reflectiveCurveToRelative(0.5f, -0.1f, 0.7f, -0.3f)
                lineToRelative(1.4f, -1.4f)
                curveToRelative(0.4f, -0.4f, 0.4f, -1.0f, 0.0f, -1.4f)
                reflectiveCurveTo(25.6f, 5.0f, 25.2f, 5.4f)
                close()
            }
            path(fill = SolidColor(Color(0xFFF44236)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(6.8f, 23.8f)
                lineToRelative(-1.4f, 1.4f)
                curveToRelative(-0.4f, 0.4f, -0.4f, 1.0f, 0.0f, 1.4f)
                curveToRelative(0.2f, 0.2f, 0.5f, 0.3f, 0.7f, 0.3f)
                reflectiveCurveToRelative(0.5f, -0.1f, 0.7f, -0.3f)
                lineToRelative(1.4f, -1.4f)
                curveToRelative(0.4f, -0.4f, 0.4f, -1.0f, 0.0f, -1.4f)
                reflectiveCurveTo(7.2f, 23.4f, 6.8f, 23.8f)
                close()
            }
            path(fill = SolidColor(Color(0xFFF44236)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(6.8f, 5.4f)
                curveTo(6.4f, 5.0f, 5.8f, 5.0f, 5.4f, 5.4f)
                reflectiveCurveToRelative(-0.4f, 1.0f, 0.0f, 1.4f)
                lineToRelative(1.4f, 1.4f)
                curveTo(7.0f, 8.4f, 7.3f, 8.5f, 7.5f, 8.5f)
                reflectiveCurveTo(8.0f, 8.4f, 8.2f, 8.2f)
                curveToRelative(0.4f, -0.4f, 0.4f, -1.0f, 0.0f, -1.4f)
                lineTo(6.8f, 5.4f)
                close()
            }
            path(fill = SolidColor(Color(0xFFF44236)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(25.2f, 23.8f)
                curveToRelative(-0.4f, -0.4f, -1.0f, -0.4f, -1.4f, 0.0f)
                reflectiveCurveToRelative(-0.4f, 1.0f, 0.0f, 1.4f)
                lineToRelative(1.4f, 1.4f)
                curveToRelative(0.2f, 0.2f, 0.5f, 0.3f, 0.7f, 0.3f)
                reflectiveCurveToRelative(0.5f, -0.1f, 0.7f, -0.3f)
                curveToRelative(0.4f, -0.4f, 0.4f, -1.0f, 0.0f, -1.4f)
                lineTo(25.2f, 23.8f)
                close()
            }
        }
        .build()
        return _root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons._sunIndicator!!
    }

private var _sunIndicator: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = WeatherAppIcons.SunIndicator, contentDescription = "")
    }
}
