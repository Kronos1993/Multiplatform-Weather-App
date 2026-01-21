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

public val WeatherAppIcons.CloudsIndicator: ImageVector
    get() {
        if (_root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons._cloudsIndicator != null) {
            return _root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons._cloudsIndicator!!
        }
        _root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons._cloudsIndicator = Builder(name = "CloudsIndicator", defaultWidth = 800.0.dp, defaultHeight
                = 800.0.dp, viewportWidth = 36.0f, viewportHeight = 36.0f).apply {
            path(fill = SolidColor(Color(0xFFFFAC33)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(16.0f, 2.0f)
                reflectiveCurveToRelative(0.0f, -2.0f, 2.0f, -2.0f)
                reflectiveCurveToRelative(2.0f, 2.0f, 2.0f, 2.0f)
                verticalLineToRelative(2.0f)
                reflectiveCurveToRelative(0.0f, 2.0f, -2.0f, 2.0f)
                reflectiveCurveToRelative(-2.0f, -2.0f, -2.0f, -2.0f)
                lineTo(16.0f, 2.0f)
                close()
                moveTo(34.0f, 16.0f)
                reflectiveCurveToRelative(2.0f, 0.0f, 2.0f, 2.0f)
                reflectiveCurveToRelative(-2.0f, 2.0f, -2.0f, 2.0f)
                horizontalLineToRelative(-2.0f)
                reflectiveCurveToRelative(-2.0f, 0.0f, -2.0f, -2.0f)
                reflectiveCurveToRelative(2.0f, -2.0f, 2.0f, -2.0f)
                horizontalLineToRelative(2.0f)
                close()
                moveTo(4.0f, 16.0f)
                reflectiveCurveToRelative(2.0f, 0.0f, 2.0f, 2.0f)
                reflectiveCurveToRelative(-2.0f, 2.0f, -2.0f, 2.0f)
                lineTo(2.0f, 20.0f)
                reflectiveCurveToRelative(-2.0f, 0.0f, -2.0f, -2.0f)
                reflectiveCurveToRelative(2.0f, -2.0f, 2.0f, -2.0f)
                horizontalLineToRelative(2.0f)
                close()
                moveTo(9.121f, 7.293f)
                reflectiveCurveToRelative(1.414f, 1.414f, 0.0f, 2.828f)
                reflectiveCurveToRelative(-2.828f, 0.0f, -2.828f, 0.0f)
                lineTo(4.878f, 8.708f)
                reflectiveCurveToRelative(-1.414f, -1.414f, 0.0f, -2.829f)
                curveToRelative(1.415f, -1.414f, 2.829f, 0.0f, 2.829f, 0.0f)
                lineToRelative(1.414f, 1.414f)
                close()
                moveTo(29.708f, 10.121f)
                reflectiveCurveToRelative(-1.414f, 1.414f, -2.828f, 0.0f)
                reflectiveCurveToRelative(0.0f, -2.828f, 0.0f, -2.828f)
                lineToRelative(1.414f, -1.414f)
                reflectiveCurveToRelative(1.414f, -1.414f, 2.828f, 0.0f)
                reflectiveCurveToRelative(0.0f, 2.828f, 0.0f, 2.828f)
                lineToRelative(-1.414f, 1.414f)
                close()
                moveTo(8.708f, 31.121f)
                reflectiveCurveToRelative(-1.414f, 1.414f, -2.828f, 0.0f)
                reflectiveCurveToRelative(0.0f, -2.828f, 0.0f, -2.828f)
                lineToRelative(1.414f, -1.414f)
                reflectiveCurveToRelative(1.414f, -1.414f, 2.828f, 0.0f)
                reflectiveCurveToRelative(0.0f, 2.828f, 0.0f, 2.828f)
                lineToRelative(-1.414f, 1.414f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFAC33)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(18.0f, 18.0f)
                moveToRelative(-10.0f, 0.0f)
                arcToRelative(10.0f, 10.0f, 0.0f, true, true, 20.0f, 0.0f)
                arcToRelative(10.0f, 10.0f, 0.0f, true, true, -20.0f, 0.0f)
            }
            path(fill = SolidColor(Color(0xFFE1E8ED)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(29.777f, 23.2f)
                curveToRelative(-0.642f, 0.0f, -1.26f, 0.1f, -1.843f, 0.285f)
                curveToRelative(-0.688f, -2.028f, -2.56f, -3.485f, -4.767f, -3.485f)
                curveToRelative(-2.368f, 0.0f, -4.35f, 1.678f, -4.899f, 3.937f)
                arcToRelative(3.407f, 3.407f, 0.0f, false, false, -2.101f, -0.736f)
                curveToRelative(-1.933f, 0.0f, -3.5f, 1.611f, -3.5f, 3.6f)
                curveToRelative(0.0f, 0.483f, 0.096f, 0.941f, 0.264f, 1.363f)
                arcTo(3.715f, 3.715f, 0.0f, false, false, 11.889f, 28.0f)
                curveTo(9.741f, 28.0f, 8.0f, 29.791f, 8.0f, 32.0f)
                reflectiveCurveToRelative(1.741f, 4.0f, 3.889f, 4.0f)
                horizontalLineToRelative(17.889f)
                curveTo(33.214f, 36.0f, 36.0f, 33.136f, 36.0f, 29.6f)
                curveToRelative(0.0f, -3.535f, -2.786f, -6.4f, -6.223f, -6.4f)
                close()
            }
        }
        .build()
        return _root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons._cloudsIndicator!!
    }

private var _cloudsIndicator: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = WeatherAppIcons.CloudsIndicator, contentDescription = "")
    }
}
