package com.kronos.multiplatform.weatherapp.components

import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kronos.multiplatform.weatherapp.components.theme.extendedDark
import com.kronos.multiplatform.weatherapp.components.theme.extendedLight

data class Step(
    var currentStep: Boolean = false,
    var text: String = "",
    var index: Int = 0
)

@Composable
fun Stepper(
    steps: List<Step>, // List of step titles or numbers
    modifier: Modifier = Modifier,
    circleSize: Dp = 24.dp, // Size of the circles
    isDarkTheme: Boolean = false,
    horizontal:Boolean = true
) {

    val lineColor: Color = if (isDarkTheme) {
        extendedLight.divider.color
    }else{
        extendedDark.divider.color
    }

    val completedColor: Color =if (isDarkTheme) {
        extendedLight.completedStep.color
    }else{
        extendedDark.completedStep.color
    }

    val currentColor: Color = if (isDarkTheme) {
        extendedLight.currentStep.color
    }else{
        extendedDark.currentStep.color
    }

    val pendingColor: Color = if (isDarkTheme) {
        extendedLight.incompletedStep.color
    }else{
        extendedDark.incompletedStep.color
    }

    if (horizontal){
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            steps.forEachIndexed { index, step ->
                val isCompleted = index < steps.indexOfFirst { it.currentStep }
                val isCurrent = step.currentStep
                val backgroundColor = when {
                    isCompleted -> completedColor
                    isCurrent -> currentColor
                    else -> pendingColor
                }

                // Render the circle
                StepCircle(
                    number = (if (isCompleted) "✔" else step.text),
                    backgroundColor = backgroundColor,
                    circleSize = circleSize,
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted || isCurrent) Color.White else Color.Black
                    )
                )

                // Render the connecting line if it's not the last step
                if (index < steps.size - 1) {
                    StepLine(
                        color = lineColor,
                        length = 32.dp
                    )
                }
            }
        }
    }else{
        Column(
            modifier = modifier
                .fillMaxHeight()
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            steps.forEachIndexed { index, step ->
                val isCompleted = index < steps.indexOfFirst { it.currentStep }
                val isCurrent = step.currentStep
                val backgroundColor = when {
                    isCompleted -> completedColor
                    isCurrent -> currentColor
                    else -> pendingColor
                }

                // Render the circle
                StepCircle(
                    number = (if (isCompleted) "✔" else step.text),
                    backgroundColor = backgroundColor,
                    circleSize = circleSize,
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted || isCurrent) Color.White else Color.Black
                    )
                )

                // Render the connecting line if it's not the last step
                if (index < steps.size - 1) {
                    StepLine(
                        color = lineColor,
                        length = 8.dp,
                        horizontal
                    )
                }
            }
        }
    }

}

@Composable
fun StepCircle(
    number: String,
    backgroundColor: Color,
    circleSize: Dp,
    textStyle: TextStyle
) {
    Box(
        modifier = Modifier
            .size(circleSize)
            .background(backgroundColor, shape = CircleShape)
            .border(1.dp, Color.Black, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = number, style = textStyle)
    }
}

@Composable
fun StepLine(
    color: Color,
    length: Dp,
    horizontal:Boolean = true
) {
    if (horizontal){
        Box(
            modifier = Modifier
                .height(2.dp)
                .width(length)
                .background(color)
        )
    }else{
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(length)
                .background(color)
        )
    }
}