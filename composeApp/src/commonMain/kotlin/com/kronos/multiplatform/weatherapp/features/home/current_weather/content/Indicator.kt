package com.kronos.multiplatform.weatherapp.features.home.current_weather.content

import androidx.compose.ui.graphics.vector.ImageVector

sealed class Indicator(
    open val id: Int,
    open val header: String,
    open val description: String,
    open val image: ImageVector
) {

    data class Default(
        override val id: Int,
        override val header: String,
        override val description: String,
        override val image: ImageVector
    ) : Indicator(id, header, description, image)

    data class Wind(
        override val id: Int,
        override val header: String,
        override val description: String,
        override val image: ImageVector,
        val windDegree: Float,
    ) : Indicator(id, header, description, image)

    data class UVIndex(
        override val id: Int,
        override val header: String,
        override val description: String,
        override val image: ImageVector,
        val level: Double,
    ) : Indicator(id, header, description, image)
}