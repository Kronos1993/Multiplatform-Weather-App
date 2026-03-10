package com.kronos.multiplatform.weatherapp.core.ui.components


import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class ProgressRange(
    val start: Float,
    val end: Float,
    val color: Color
)

val rangesColor = listOf(
    ProgressRange(0.0f, 2.9f, Color(0xFF4CAF50)),
    ProgressRange(3.0f, 5.9f, Color(0xffffe667)),
    ProgressRange(6.0f, 7.9f, Color(0xFFFF9800)),
    ProgressRange(8.0f, 10.9f, Color(0xFFF44336)),
    ProgressRange(11.0f, 14.0f, Color(0xffb56df8))
)

fun lerpColor(start: Color, end: Color, t: Float): Color {
    return Color(
        red = start.red + (end.red - start.red) * t,
        green = start.green + (end.green - start.green) * t,
        blue = start.blue + (end.blue - start.blue) * t,
        alpha = start.alpha + (end.alpha - start.alpha) * t
    )
}

fun startColor(color: Color) = color.copy(alpha = 0.5f)
fun endColor(color: Color) = color

fun progressToAngle(value: Float, max: Float): Float {
    val t = (value / max).coerceIn(0f, 1f)
    return 180f + (180f * t) // izquierda → derecha
}

@Composable
fun AnimatedSemiCircularProgress(
    uvIndex: Float,
    max: Float = 14f,
    modifier: Modifier = Modifier
) {
    val animatedValue by animateFloatAsState(
        targetValue = if (uvIndex > 13f) 13f else uvIndex,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "anim"
    )

    Canvas(modifier = modifier) {

        val stroke = 6.dp.toPx()
        val radius = size.minDimension / 2.2f
        val center = Offset(
            x = size.width / 2f,
            y = size.height-8.dp.toPx() //dejar 8 dp de espacio en la parte inferior
        )

        val startAngle = 180f
        val totalSweep = 180f

        // ===== Arco por rangos UV =====
        rangesColor.forEach { range ->

            val rangeStart = range.start / max
            val rangeEnd = range.end / max

            val angleStart = startAngle + totalSweep * rangeStart
            val angleSweep = totalSweep * (rangeEnd - rangeStart)

            val steps = 10
            val stepSweep = angleSweep / steps

            repeat(steps) { step ->
                val t = step / (steps - 1f)

                val color = lerpColor(
                    startColor(range.color),
                    endColor(range.color),
                    t
                )

                drawArc(
                    color = color,
                    startAngle = angleStart + step * stepSweep,
                    sweepAngle = stepSweep,
                    useCenter = false,
                    style = Stroke(width = stroke, cap = StrokeCap.Butt),
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )
            }
        }

        // ===== Indicador =====
        val angleRad =
            progressToAngle(animatedValue, max) * (PI / 180f)

        val indicatorRadius = radius - stroke / 20

        val indicatorCenter = Offset(
            x = center.x + cos(angleRad).toFloat() * indicatorRadius,
            y = center.y + sin(angleRad).toFloat() * indicatorRadius
        )

        drawCircle(
            color = Color.White,
            radius = 4.dp.toPx(),
            center = indicatorCenter
        )
    }
}


@Preview(showBackground = true)
@Composable
fun SemiCircularProgressPreview() {

    Column(
        modifier = Modifier
            .wrapContentWidth()
            .padding(horizontal = 5.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedSemiCircularProgress(
            2f,
            modifier = Modifier.size(48.dp).height(24.dp),
        )
        LabelText(
            "header",
            textAlign = TextAlign.Center,
            size = ComponentSize.MEDIUM,
            modifier = Modifier.wrapContentWidth()
        )
        LabelText(
            "description",
            textAlign = TextAlign.Center,
            fontWeight = Bold,
            size = ComponentSize.EXTRA_SMALL,
            modifier = Modifier.wrapContentWidth()
        )
    }
}