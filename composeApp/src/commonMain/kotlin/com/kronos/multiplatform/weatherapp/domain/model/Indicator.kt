package com.kronos.multiplatform.weatherapp.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

data class Indicator(
    var id: Int,
    var header: String,
    var description: String,
    var image: ImageVector
)
