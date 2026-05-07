package com.kronos.multiplatform.weatherapp.components.icons.weatherappicons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeCap.Companion.Round
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.kronos.multiplatform.weatherapp.components.icons.WeatherAppIcons
import androidx.compose.ui.tooling.preview.Preview

public val WeatherAppIcons.WaterDropsIndicator: ImageVector
    get() {
        if (_root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons._waterDropsIndicator != null) {
            return _root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons._waterDropsIndicator!!
        }
        _root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons._waterDropsIndicator = Builder(name = "WaterDropsIndicator", defaultWidth = 800.0.dp,
                defaultHeight = 800.0.dp, viewportWidth = 64.0f, viewportHeight = 64.0f).apply {
            path(fill = SolidColor(Color(0xFFB4DFFB)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(26.7f, 63.0f)
                curveTo(36.475f, 63.0f, 44.399f, 55.076f, 44.399f, 45.3f)
                curveTo(44.399f, 35.525f, 26.7f, 9.0f, 26.7f, 9.0f)
                curveTo(26.7f, 9.0f, 9.0f, 35.525f, 9.0f, 45.3f)
                curveTo(9.0f, 55.076f, 16.924f, 63.0f, 26.7f, 63.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF4796E7)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(41.111f, 41.0f)
                curveTo(48.352f, 41.0f, 54.222f, 35.13f, 54.222f, 27.889f)
                curveTo(54.222f, 20.648f, 41.111f, 1.0f, 41.111f, 1.0f)
                curveTo(41.111f, 1.0f, 28.0f, 20.648f, 28.0f, 27.889f)
                curveTo(28.0f, 35.13f, 33.87f, 41.0f, 41.111f, 41.0f)
                close()
            }
            path(fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFFB4DFFB)),
                    strokeLineWidth = 2.0f, strokeLineCap = Round, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = EvenOdd) {
                moveTo(32.0f, 28.0f)
                curveTo(32.0f, 32.971f, 36.029f, 37.0f, 41.0f, 37.0f)
                lineTo(41.0f, 37.0f)
            }
            path(fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2.0f, strokeLineCap = Round, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = EvenOdd) {
                moveTo(13.0f, 46.0f)
                curveTo(13.0f, 52.627f, 18.373f, 58.0f, 25.0f, 58.0f)
                lineTo(25.0f, 58.0f)
            }
        }
        .build()
        return _root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons._waterDropsIndicator!!
    }

private var _waterDropsIndicator: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = WeatherAppIcons.WaterDropsIndicator, contentDescription = "")
    }
}
