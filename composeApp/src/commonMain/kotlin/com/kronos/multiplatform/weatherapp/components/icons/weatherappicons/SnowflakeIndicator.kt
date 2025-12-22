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

public val WeatherAppIcons.SnowflakeIndicator: ImageVector
    get() {
        if (_snowflakeIndicator != null) {
            return _snowflakeIndicator!!
        }
        _snowflakeIndicator = Builder(name = "SnowflakeIndicator", defaultWidth = 800.0.dp,
                defaultHeight = 800.0.dp, viewportWidth = 32.0f, viewportHeight = 32.0f).apply {
            path(fill = SolidColor(Color(0xFF2197F3)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(30.0f, 15.0f)
                horizontalLineTo(17.0f)
                verticalLineTo(2.0f)
                curveToRelative(0.0f, -0.6f, -0.4f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.4f, -1.0f, 1.0f)
                verticalLineToRelative(13.0f)
                horizontalLineTo(2.0f)
                curveToRelative(-0.6f, 0.0f, -1.0f, 0.4f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.4f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(13.0f)
                verticalLineToRelative(13.0f)
                curveToRelative(0.0f, 0.6f, 0.4f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.4f, 1.0f, -1.0f)
                verticalLineTo(17.0f)
                horizontalLineToRelative(13.0f)
                curveToRelative(0.6f, 0.0f, 1.0f, -0.4f, 1.0f, -1.0f)
                reflectiveCurveTo(30.6f, 15.0f, 30.0f, 15.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF16BCD4)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(16.0f, 11.0f)
                curveToRelative(-0.2f, 0.0f, -0.4f, -0.1f, -0.6f, -0.2f)
                lineToRelative(-5.0f, -4.0f)
                curveToRelative(-0.4f, -0.3f, -0.5f, -1.0f, -0.2f, -1.4f)
                curveToRelative(0.3f, -0.4f, 1.0f, -0.5f, 1.4f, -0.2f)
                lineTo(16.0f, 8.7f)
                lineToRelative(4.4f, -3.5f)
                curveToRelative(0.4f, -0.3f, 1.1f, -0.3f, 1.4f, 0.2f)
                curveToRelative(0.3f, 0.4f, 0.3f, 1.1f, -0.2f, 1.4f)
                lineToRelative(-5.0f, 4.0f)
                curveTo(16.4f, 10.9f, 16.2f, 11.0f, 16.0f, 11.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF16BCD4)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(21.0f, 27.0f)
                curveToRelative(-0.2f, 0.0f, -0.4f, -0.1f, -0.6f, -0.2f)
                lineTo(16.0f, 23.3f)
                lineToRelative(-4.4f, 3.5f)
                curveToRelative(-0.4f, 0.3f, -1.1f, 0.3f, -1.4f, -0.2f)
                curveToRelative(-0.3f, -0.4f, -0.3f, -1.1f, 0.2f, -1.4f)
                lineToRelative(5.0f, -4.0f)
                curveToRelative(0.4f, -0.3f, 0.9f, -0.3f, 1.2f, 0.0f)
                lineToRelative(5.0f, 4.0f)
                curveToRelative(0.4f, 0.3f, 0.5f, 1.0f, 0.2f, 1.4f)
                curveTo(21.6f, 26.9f, 21.3f, 27.0f, 21.0f, 27.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF16BCD4)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(6.0f, 22.0f)
                curveToRelative(-0.2f, 0.0f, -0.4f, -0.1f, -0.6f, -0.2f)
                curveToRelative(-0.4f, -0.3f, -0.5f, -1.0f, -0.2f, -1.4f)
                lineTo(8.7f, 16.0f)
                lineToRelative(-3.5f, -4.4f)
                curveToRelative(-0.3f, -0.4f, -0.3f, -1.1f, 0.2f, -1.4f)
                curveToRelative(0.4f, -0.3f, 1.1f, -0.3f, 1.4f, 0.2f)
                lineToRelative(4.0f, 5.0f)
                curveToRelative(0.3f, 0.4f, 0.3f, 0.9f, 0.0f, 1.2f)
                lineToRelative(-4.0f, 5.0f)
                curveTo(6.6f, 21.9f, 6.3f, 22.0f, 6.0f, 22.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF16BCD4)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(26.0f, 22.0f)
                curveToRelative(-0.3f, 0.0f, -0.6f, -0.1f, -0.8f, -0.4f)
                lineToRelative(-4.0f, -5.0f)
                curveToRelative(-0.3f, -0.4f, -0.3f, -0.9f, 0.0f, -1.2f)
                lineToRelative(4.0f, -5.0f)
                curveToRelative(0.3f, -0.4f, 1.0f, -0.5f, 1.4f, -0.2f)
                curveToRelative(0.4f, 0.3f, 0.5f, 1.0f, 0.2f, 1.4f)
                lineTo(23.3f, 16.0f)
                lineToRelative(3.5f, 4.4f)
                curveToRelative(0.3f, 0.4f, 0.3f, 1.1f, -0.2f, 1.4f)
                curveTo(26.4f, 21.9f, 26.2f, 22.0f, 26.0f, 22.0f)
                close()
            }
        }
        .build()
        return _snowflakeIndicator!!
    }

private var _snowflakeIndicator: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = WeatherAppIcons.SnowflakeIndicator, contentDescription = "")
    }
}
