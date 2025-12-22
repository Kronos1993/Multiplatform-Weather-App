package com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.moonphases

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
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.MoonPhasesGroup
import org.jetbrains.compose.ui.tooling.preview.Preview

public val MoonPhasesGroup.WaningCrescentMoonIndicator: ImageVector
    get() {
        if (_waningCrescentMoonIndicator != null) {
            return _waningCrescentMoonIndicator!!
        }
        _waningCrescentMoonIndicator = Builder(
            name = "WaningCrescentMoonIndicator", defaultWidth = 800.0.dp,
            defaultHeight = 800.0.dp, viewportWidth = 32.0f, viewportHeight =
                32.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFD9D9D9)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(16.0f, 14.0f)
                moveToRelative(-10.0f, 0.0f)
                arcToRelative(10.0f, 10.0f, 0.0f, true, true, 20.0f, 0.0f)
                arcToRelative(10.0f, 10.0f, 0.0f, true, true, -20.0f, 0.0f)
            }
            path(
                fill = SolidColor(Color(0xFFFFC10A)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(16.0f, 4.0f)
                arcTo(10.0f, 10.0f, 0.0f, true, false, 16.0f, 24.0f)
                arcTo(7.0f, 10.0f, 0.0f, true, true, 16.0f, 4.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF2197F3)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(31.0f, 24.0f)
                curveToRelative(0.0f, 0.5f, -0.5f, 1.0f, -1.0f, 1.0f)
                curveToRelative(-1.5f, 0.0f, -2.2f, 0.4f, -3.0f, 0.9f)
                curveToRelative(-0.4f, 0.2f, -0.8f, 0.5f, -1.3f, 0.6f)
                curveTo(24.9f, 26.8f, 24.0f, 27.0f, 23.0f, 27.0f)
                curveToRelative(-0.1f, 0.0f, -0.1f, 0.0f, -0.2f, 0.0f)
                curveToRelative(-0.1f, 0.0f, -0.1f, 0.0f, -0.2f, 0.0f)
                curveToRelative(-1.8f, -0.1f, -2.8f, -0.6f, -3.6f, -1.1f)
                curveToRelative(-0.8f, -0.5f, -1.5f, -0.9f, -3.0f, -0.9f)
                reflectiveCurveToRelative(-2.2f, 0.4f, -3.0f, 0.9f)
                curveToRelative(-0.8f, 0.5f, -1.7f, 1.0f, -3.2f, 1.1f)
                curveToRelative(-0.3f, 0.0f, -0.5f, 0.0f, -0.8f, 0.0f)
                curveToRelative(-1.0f, 0.0f, -1.9f, -0.2f, -2.7f, -0.5f)
                curveTo(4.2f, 25.4f, 3.5f, 25.0f, 2.0f, 25.0f)
                curveToRelative(-0.5f, 0.0f, -1.0f, -0.5f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.5f, -1.0f, 1.0f, -1.0f)
                curveToRelative(2.0f, 0.0f, 3.1f, 0.6f, 4.0f, 1.1f)
                curveToRelative(0.3f, 0.1f, 0.5f, 0.3f, 0.8f, 0.4f)
                curveTo(7.6f, 24.9f, 8.2f, 25.0f, 9.0f, 25.0f)
                curveToRelative(1.5f, 0.0f, 2.2f, -0.4f, 3.0f, -0.9f)
                curveToRelative(0.9f, -0.5f, 2.0f, -1.1f, 4.0f, -1.1f)
                curveToRelative(2.0f, 0.0f, 3.1f, 0.6f, 4.0f, 1.1f)
                curveToRelative(0.8f, 0.5f, 1.5f, 0.9f, 3.0f, 0.9f)
                curveToRelative(0.8f, 0.0f, 1.4f, -0.1f, 2.0f, -0.3f)
                curveToRelative(0.4f, -0.1f, 0.7f, -0.3f, 1.0f, -0.5f)
                curveToRelative(0.9f, -0.5f, 1.9f, -1.0f, 3.7f, -1.0f)
                curveTo(30.5f, 23.0f, 31.0f, 23.5f, 31.0f, 24.0f)
                close()
            }
        }
            .build()
        return _waningCrescentMoonIndicator!!
    }

private var _waningCrescentMoonIndicator: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = MoonPhasesGroup.WaningCrescentMoonIndicator, contentDescription = "")
    }
}
