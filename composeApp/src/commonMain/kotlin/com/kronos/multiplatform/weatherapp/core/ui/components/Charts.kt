package com.kronos.multiplatform.weatherapp.core.ui.components

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.IndicatorPosition
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.Pie
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun BarCharView(
    title: String,
    data: List<Triple<String, Float, Color>>,
    legendLabels:Map<String,String> = emptyMap(),
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    xAxisTextColor: Color? = null,
    yAxisTextColor: Color? = null
) {
    val graphData = data.map {
        Bars(
            label = it.first,
            values = listOf(
                Bars.Data(
                    label = if (legendLabels.isNotEmpty()){
                        legendLabels[it.first].orEmpty()
                    }else{
                        it.first
                    },
                    value = it.second.toDouble(),
                    color = Brush.verticalGradient(
                        listOf(
                            it.third.copy(alpha = 0.9f),
                            it.third.copy(alpha = 0.8f),
                            it.third.copy(alpha = 0.8f),
                            it.third.copy(alpha = 0.9f),
                            it.third,
                        )
                    ),
                ),
            )
        )
    }

    val xAxisProperties = GridProperties.AxisProperties(
        enabled = true,
        color = if (xAxisTextColor != null)
            SolidColor(xAxisTextColor)
        else {
            SolidColor(
                if (isDarkTheme)
                    Color.White
                else
                    Color.Black
            )
        },
    )

    val yAxisProperties = GridProperties.AxisProperties(
        enabled = true,
        color = if (yAxisTextColor != null)
            SolidColor(yAxisTextColor)
        else {
            SolidColor(
                if (isDarkTheme)
                    Color.White
                else
                    Color.Black
            )
        },
    )

    val hIndicators = HorizontalIndicatorProperties(
        enabled = true,
        textStyle = TextStyle(
            color =
                yAxisTextColor
                    ?: if (isDarkTheme)
                        Color.White
                    else
                        Color.Black,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        ),
        position = IndicatorPosition.Horizontal.Start,
    )

    Column(modifier) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        ColumnChart(
            modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp),
            data = remember {
                graphData
            },
            barProperties = BarProperties(
                cornerRadius = Bars.Data.Radius.Rectangle(topRight = 6.dp, topLeft = 6.dp),
                spacing = 3.dp,
                thickness = 20.dp,
            ),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            gridProperties = GridProperties(
                enabled = true,
                xAxisProperties = xAxisProperties,
                yAxisProperties = yAxisProperties
            ),
            labelProperties = LabelProperties(
                enabled = true,
                textStyle = TextStyle(
                    color = if (isDarkTheme)
                        Color.White
                    else
                        Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                labels = data.map { it.first },
            ),
            labelHelperProperties = LabelHelperProperties(
                enabled = true,
                textStyle = TextStyle(
                    color = if (isDarkTheme)
                        Color.White
                    else
                        Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
            ),
            indicatorProperties = hIndicators
        )
    }
}


@Composable
fun LineChartView(
    title: String,
    data: List<Triple<String, Float, Color>> = listOf(),
    legendLabels:Map<String,String> = emptyMap(),
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    xAxisTextColor: Color? = null,
    yAxisTextColor: Color? = null,
    curveLines: Boolean = true
) {

    val graphData = data.map {
        Line(
            label = if (legendLabels.isNotEmpty()){
                legendLabels[it.first].orEmpty()
            }else{
                it.first
            },
            values = listOf(it.second.toDouble()),
            color = SolidColor(it.third),
            curvedEdges = curveLines,
            firstGradientFillColor = it.third.copy(alpha = .8f),
            secondGradientFillColor = it.third.copy(alpha = .5f),
            strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
            gradientAnimationDelay = 1000,
            drawStyle = DrawStyle.Stroke(width = 2.dp),
        )
    }

    val xAxisProperties = GridProperties.AxisProperties(
        enabled = true,
        color = if (xAxisTextColor != null)
            SolidColor(xAxisTextColor)
        else {
            SolidColor(
                if (isDarkTheme)
                    Color.White
                else
                    Color.Black
            )
        },
    )

    val yAxisProperties = GridProperties.AxisProperties(
        enabled = true,
        color = if (yAxisTextColor != null)
            SolidColor(yAxisTextColor)
        else {
            SolidColor(
                if (isDarkTheme)
                    Color.White
                else
                    Color.Black
            )
        },
    )

    val hIndicators = HorizontalIndicatorProperties(
        enabled = true,
        textStyle = TextStyle(
            color =
                yAxisTextColor
                    ?: if (isDarkTheme)
                        Color.White
                    else
                        Color.Black,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        ),
        position = IndicatorPosition.Horizontal.Start,
    )

    Column(modifier) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        LineChart(
            modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp),
            data = remember {
                graphData
            },
            animationMode = AnimationMode.Together(delayBuilder = {
                it * 500L
            }),
            gridProperties = GridProperties(
                enabled = true,
                xAxisProperties = xAxisProperties,
                yAxisProperties = yAxisProperties
            ),
            labelProperties = LabelProperties(
                enabled = true,
                textStyle = TextStyle(
                    color = if (isDarkTheme)
                        Color.White
                    else
                        Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                labels = data.map { it.first },
            ),
            labelHelperProperties = LabelHelperProperties(
                enabled = true,
                textStyle = TextStyle(
                    color = if (isDarkTheme)
                        Color.White
                    else
                        Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
            ),
            indicatorProperties = hIndicators
        )
    }
}

@Composable
fun MultipleLineChartView(
    title: String,
    data: List<Triple<String, List<Pair<String, Float>>, Color>>,
    legendLabels:Map<String,String> = emptyMap(),
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    xAxisTextColor: Color? = null,
    yAxisTextColor: Color? = null,
    curveLines: Boolean = true
) {
    if (data.isEmpty()) return

    val lines = remember(data) {
        data.map { (first, second, color) ->
            Line(
                label = if (legendLabels.isNotEmpty()){
                    legendLabels[first].orEmpty()
                }else{
                    first
                },
                values = second.map { it.second.toDouble() },
                color = SolidColor(color),
                curvedEdges = curveLines,
                firstGradientFillColor = color.copy(alpha = .8f),
                secondGradientFillColor = color.copy(alpha = .5f),
                strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                gradientAnimationDelay = 1000,
                drawStyle = DrawStyle.Stroke(width = 2.dp),
            )
        }
    }

    val xAxisProperties = GridProperties.AxisProperties(
        enabled = true,
        color = if (xAxisTextColor != null)
            SolidColor(xAxisTextColor)
        else {
            SolidColor(
                if (isDarkTheme)
                    Color.White
                else
                    Color.Black
            )
        },
    )

    val yAxisProperties = GridProperties.AxisProperties(
        enabled = true,
        color = if (yAxisTextColor != null)
            SolidColor(yAxisTextColor)
        else {
            SolidColor(
                if (isDarkTheme)
                    Color.White
                else
                    Color.Black
            )
        },
    )

    val hIndicators = HorizontalIndicatorProperties(
        enabled = true,
        textStyle = TextStyle(
            color =
                yAxisTextColor
                    ?: if (isDarkTheme)
                        Color.White
                    else
                        Color.Black,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        ),
        position = IndicatorPosition.Horizontal.Start,
    )

    Column(modifier) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        LineChart(
            modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp),
            data = remember {
                lines
            },
            animationMode = AnimationMode.Together(delayBuilder = {
                it * 500L
            }),
            gridProperties = GridProperties(
                enabled = true,
                xAxisProperties = xAxisProperties,
                yAxisProperties = yAxisProperties
            ),
            labelProperties = LabelProperties(
                enabled = true,
                textStyle = TextStyle(
                    color = if (isDarkTheme)
                        Color.White
                    else
                        Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                labels = data.firstOrNull()?.second?.map { it.first } ?: emptyList()
            ),
            labelHelperProperties = LabelHelperProperties(
                enabled = true,
                textStyle = TextStyle(
                    color = if (isDarkTheme)
                        Color.White
                    else
                        Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
            ),
            indicatorProperties = hIndicators,
        )
    }
}


@Composable
fun PieChartView(
    data: List<Triple<String, Float, Color>> = listOf(),
    title: String,
    holeSize: Float = 0f,
    modifier: Modifier = Modifier
) {
    val graphData = data.map {
        Pie(
            label = it.first,
            data = it.second.toDouble(),
            color = it.third,
            selectedColor = it.third
        )
    }

    var data by remember {
        mutableStateOf(graphData)
    }

    Column(modifier) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        PieChart(
            modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp),
            data = data,
            onPieClick = {
                println("${it.label} Clicked")
                val pieIndex = data.indexOf(it)
                data =
                    data.mapIndexed { mapIndex, pie -> pie.copy(selected = pieIndex == mapIndex) }
            },
            selectedScale = 1.2f,
            scaleAnimEnterSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            colorAnimEnterSpec = tween(300),
            colorAnimExitSpec = tween(300),
            scaleAnimExitSpec = tween(300),
            spaceDegreeAnimExitSpec = tween(300),
            style =
                if (holeSize > 0F)
                    Pie.Style.Stroke()
                else
                    Pie.Style.Fill

        )

    }
}

@Preview(
    name = "Bar Chart Preview",
    showBackground = true,
    widthDp = 900,
    heightDp = 500
)
@Composable
fun BarChartViewPreview() {

    val barChartPreviewData = listOf(
        Triple("Jan", 12F, Color(0xFF1E88E5)),
        Triple("Feb", 18F, Color(0xFFD81B60)),
        Triple("Mar", 8F, Color(0xFF43A047)),
        Triple("Apr", 22F, Color(0xFFF4511E)),
        Triple("May", 15F, Color(0xFF6D4C41))
    )

    MaterialTheme {
        BarCharView(
            data = barChartPreviewData,
            title = "Sales per Month",
            modifier = Modifier
                .fillMaxSize(),
            isDarkTheme = true,
        )
    }
}

@Preview(
    name = "Line Chart Preview",
    showBackground = true,
    widthDp = 900,
    heightDp = 500
)
@Composable
fun LineChartViewPreview() {

    val barChartPreviewData = listOf(
        Triple("Jan", 12F, Color(0xFF1E88E5)),
        Triple("Feb", 18F, Color(0xFFD81B60)),
        Triple("Mar", 8F, Color(0xFF43A047)),
        Triple("Apr", 22F, Color(0xFFF4511E)),
        Triple("May", 15F, Color(0xFF6D4C41))
    )

    MaterialTheme {
        LineChartView(
            title = "Sales per Month",
            data = barChartPreviewData,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            curveLines = false,
        )
    }
}

@Preview(
    name = "Pie Chart Preview",
    showBackground = true,
    widthDp = 600,
    heightDp = 500
)
@Composable
fun PieChartViewPreview() {

    val pieChartPreviewData = listOf(
        Triple("Android", 45f, Color(0xFF3DDC84)),
        Triple("iOS", 30f, Color(0xFF000000)),
        Triple("Web", 15f, Color(0xFF4285F4)),
        Triple("Other", 10f, Color(0xFF9E9E9E))
    )

    MaterialTheme {
        PieChartView(
            data = pieChartPreviewData,
            title = "Platform Distribution",
            holeSize = 0F,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        )
    }
}
