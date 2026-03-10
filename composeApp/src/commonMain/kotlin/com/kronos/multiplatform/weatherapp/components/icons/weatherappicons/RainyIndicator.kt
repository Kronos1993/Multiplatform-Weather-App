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

public val WeatherAppIcons.RainyIndicator: ImageVector
    get() {
        if (_root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons._rainyIndicator != null) {
            return _root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons._rainyIndicator!!
        }
        _root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons._rainyIndicator = Builder(name = "RainyIndicator", defaultWidth = 800.0.dp, defaultHeight =
                800.0.dp, viewportWidth = 48.0f, viewportHeight = 48.0f).apply {
            path(fill = SolidColor(Color(0xFFe1e6e8)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(44.0f, 17.91f)
                arcToRelative(8.8f, 8.8f, 0.0f, false, false, -5.7f, -3.63f)
                quadToRelative(0.0f, -0.4f, 0.0f, -0.81f)
                arcTo(11.71f, 11.71f, 0.0f, false, false, 15.0f, 11.55f)
                arcToRelative(12.16f, 12.16f, 0.0f, false, false, -1.89f, -0.15f)
                curveTo(7.0f, 11.41f, 2.0f, 16.0f, 2.0f, 21.65f)
                curveTo(2.0f, 27.11f, 5.06f, 31.0f, 9.87f, 31.76f)
                curveTo(10.29f, 32.0f, 37.6f, 32.0f, 37.93f, 31.82f)
                curveToRelative(4.65f, -0.54f, 8.07f, -4.0f, 8.07f, -8.25f)
                arcTo(9.14f, 9.14f, 0.0f, false, false, 44.0f, 17.91f)
                close()
            }
            path(fill = SolidColor(Color(0xFF38b1e7)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(29.92f, 46.5f)
                arcToRelative(2.0f, 2.0f, 0.0f, false, true, -1.18f, -0.38f)
                arcToRelative(2.0f, 2.0f, 0.0f, false, true, -0.43f, -2.76f)
                lineToRelative(6.15f, -8.28f)
                arcToRelative(2.0f, 2.0f, 0.0f, false, true, 2.79f, -0.43f)
                arcToRelative(2.0f, 2.0f, 0.0f, false, true, 0.43f, 2.76f)
                lineToRelative(-6.15f, 8.28f)
                arcTo(2.0f, 2.0f, 0.0f, false, true, 29.92f, 46.5f)
                close()
            }
            path(fill = SolidColor(Color(0xFF38b1e7)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(19.92f, 46.5f)
                arcToRelative(2.0f, 2.0f, 0.0f, false, true, -1.18f, -0.38f)
                arcToRelative(2.0f, 2.0f, 0.0f, false, true, -0.43f, -2.76f)
                lineToRelative(6.15f, -8.28f)
                arcToRelative(2.0f, 2.0f, 0.0f, false, true, 2.79f, -0.43f)
                arcToRelative(2.0f, 2.0f, 0.0f, false, true, 0.43f, 2.76f)
                lineToRelative(-6.15f, 8.28f)
                arcTo(2.0f, 2.0f, 0.0f, false, true, 19.92f, 46.5f)
                close()
            }
            path(fill = SolidColor(Color(0xFF38b1e7)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(8.92f, 46.5f)
                arcToRelative(2.0f, 2.0f, 0.0f, false, true, -1.18f, -0.38f)
                arcToRelative(2.0f, 2.0f, 0.0f, false, true, -0.43f, -2.76f)
                lineToRelative(6.15f, -8.28f)
                arcToRelative(2.0f, 2.0f, 0.0f, false, true, 2.79f, -0.43f)
                arcToRelative(2.0f, 2.0f, 0.0f, false, true, 0.43f, 2.76f)
                lineToRelative(-6.15f, 8.28f)
                arcTo(2.0f, 2.0f, 0.0f, false, true, 8.92f, 46.5f)
                close()
            }
        }
        .build()
        return _root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons._rainyIndicator!!
    }

private var _rainyIndicator: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = WeatherAppIcons.RainyIndicator, contentDescription = "")
    }
}
