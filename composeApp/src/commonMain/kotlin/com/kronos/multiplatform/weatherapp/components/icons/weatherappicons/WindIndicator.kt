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
import org.jetbrains.compose.ui.tooling.preview.Preview

public val WeatherAppIcons.WindIndicator: ImageVector
    get() {
        if (_root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons._windIndicator != null) {
            return _root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons._windIndicator!!
        }
        _root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons._windIndicator = Builder(name = "WindIndicator", defaultWidth = 800.0.dp, defaultHeight =
                800.0.dp, viewportWidth = 32.0f, viewportHeight = 32.0f).apply {
            path(fill = SolidColor(Color(0xFF16BCD4)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(27.0f, 14.0f)
                curveToRelative(0.0f, 0.5f, -0.5f, 1.0f, -1.0f, 1.0f)
                horizontalLineTo(8.0f)
                curveToRelative(-3.9f, 0.0f, -7.0f, -3.1f, -7.0f, -7.0f)
                reflectiveCurveToRelative(3.1f, -7.0f, 7.0f, -7.0f)
                curveToRelative(0.5f, 0.0f, 1.0f, 0.5f, 1.0f, 1.0f)
                reflectiveCurveTo(8.5f, 3.0f, 8.0f, 3.0f)
                curveTo(5.2f, 3.0f, 3.0f, 5.2f, 3.0f, 8.0f)
                reflectiveCurveToRelative(2.2f, 5.0f, 5.0f, 5.0f)
                horizontalLineToRelative(18.0f)
                curveTo(26.5f, 13.0f, 27.0f, 13.5f, 27.0f, 14.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF2197F3)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(31.0f, 17.0f)
                curveToRelative(0.0f, 0.5f, -0.5f, 1.0f, -1.0f, 1.0f)
                horizontalLineTo(11.0f)
                curveToRelative(-0.5f, 0.0f, -1.0f, -0.5f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.5f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(19.0f)
                curveTo(30.5f, 16.0f, 31.0f, 16.5f, 31.0f, 17.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF16BCD4)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(27.0f, 20.0f)
                curveToRelative(0.0f, 0.5f, -0.5f, 1.0f, -1.0f, 1.0f)
                horizontalLineTo(11.0f)
                curveToRelative(-2.2f, 0.0f, -4.0f, 1.8f, -4.0f, 4.0f)
                curveToRelative(0.0f, 2.2f, 1.8f, 4.0f, 4.0f, 4.0f)
                curveToRelative(0.5f, 0.0f, 1.0f, 0.5f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.5f, 1.0f, -1.0f, 1.0f)
                curveToRelative(-3.3f, 0.0f, -6.0f, -2.7f, -6.0f, -6.0f)
                reflectiveCurveToRelative(2.7f, -6.0f, 6.0f, -6.0f)
                horizontalLineToRelative(15.0f)
                curveTo(26.5f, 19.0f, 27.0f, 19.5f, 27.0f, 20.0f)
                close()
            }
        }
        .build()
        return _root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons._windIndicator!!
    }

private var _windIndicator: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = WeatherAppIcons.WindIndicator, contentDescription = "")
    }
}
