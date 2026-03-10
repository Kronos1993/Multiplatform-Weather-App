package com.kronos.multiplatform.weatherapp.core.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.kronos.multiplatform.weatherapp.core.ui.components.theme.errorLight
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun CompassBackground(
    darkTheme: Boolean
) {
    val borderColor = Color.Black.copy(alpha = 0.5f)
    val northColor = errorLight
    val cardinalPointColor = Color.White
    val tickColor = Color.White

    Canvas(modifier = Modifier.fillMaxSize()) {

        val radius = size.minDimension / 2
        val center = center

        // Anillo exterior
        drawCircle(
            color = borderColor,
            radius = radius,
            style = Stroke(2.dp.toPx())
        )

        // Anillo interior
        drawCircle(
            color = borderColor.copy(alpha = 0.5f),
            radius = radius * 0.7f,
            style = Stroke(1.dp.toPx())
        )

        // Marcas principales
        listOf(
            0f to northColor,
            90f to cardinalPointColor,
            180f to cardinalPointColor,
            270f to cardinalPointColor
        ).forEach { (angle, color) ->
            rotate(angle) {
                drawLine(
                    color = color,
                    start = Offset(center.x, center.y - radius),
                    end = Offset(center.x, center.y - radius + 8.dp.toPx()),
                    strokeWidth = if (angle == 0f) 3.dp.toPx() else 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // Marcas secundarias (cada 30°)
        repeat(12) { i ->
            val angle = i * 30f
            rotate(angle) {
                if (angle!= 0f && angle != 90f && angle != 180f && angle != 270f){
                    drawLine(
                        color = tickColor,
                        start = Offset(center.x, center.y - radius),
                        end = Offset(center.x, center.y - radius + 4.dp.toPx()),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
        }

        // Centro
        drawCircle(
            color = borderColor,
            radius = 2.dp.toPx(),
            center = center
        )
    }
}

@Composable
fun CompassArrow(
    rotation: Float
) {
    val forwardColor = errorLight
    val backwardColor = Color.White

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .rotate(rotation)
    ) {
        val center = center

        val length = size.minDimension * 0.26f
        val inset = length * 0.35f
        val width = 5.dp.toPx()

        val forwardPath = Path().apply {
            moveTo(center.x, center.y - length)
            lineTo(center.x - width, center.y - inset)
            lineTo(center.x + width, center.y - inset)
            close()
        }

        drawPath(
            path = forwardPath,
            color = forwardColor
        )

        val backwardPath = Path().apply {
            moveTo(center.x, center.y + length)
            lineTo(center.x - width, center.y + inset)
            lineTo(center.x + width, center.y + inset)
            close()
        }

        drawPath(
            path = backwardPath,
            color = backwardColor
        )
    }
}


@Composable
fun CompassView(
    rotation: Float,
    darkTheme: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "wind-rotation"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CompassBackground(darkTheme = darkTheme)

        CompassArrow(
            rotation = animatedRotation
        )
    }
}


@Composable
@Preview
fun CompassPreview() {
    CompassView(
        rotation = 80f,
        modifier = Modifier.size(100.dp),

    )
}



