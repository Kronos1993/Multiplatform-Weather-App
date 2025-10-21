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

public val WeatherAppIcons.TempIndicator: ImageVector
    get() {
        if (_tempIndicator != null) {
            return _tempIndicator!!
        }
        _tempIndicator = Builder(name = "TempIndicator", defaultWidth = 800.0.dp, defaultHeight =
                800.0.dp, viewportWidth = 32.0f, viewportHeight = 32.0f).apply {
            path(fill = SolidColor(Color(0xFFE92662)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(23.0f, 24.0f)
                curveToRelative(0.0f, 3.9f, -3.1f, 7.0f, -7.0f, 7.0f)
                reflectiveCurveToRelative(-7.0f, -3.1f, -7.0f, -7.0f)
                curveToRelative(0.0f, -2.3f, 1.1f, -4.4f, 3.0f, -5.7f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -2.2f, 1.8f, -4.0f, 4.0f, -4.0f)
                reflectiveCurveToRelative(4.0f, 1.8f, 4.0f, 4.0f)
                verticalLineToRelative(13.3f)
                curveTo(21.9f, 19.6f, 23.0f, 21.7f, 23.0f, 24.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFC10A)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(16.0f, 24.0f)
                moveToRelative(-4.0f, 0.0f)
                arcToRelative(4.0f, 4.0f, 0.0f, true, true, 8.0f, 0.0f)
                arcToRelative(4.0f, 4.0f, 0.0f, true, true, -8.0f, 0.0f)
            }
        }
        .build()
        return _tempIndicator!!
    }

private var _tempIndicator: ImageVector? = null
