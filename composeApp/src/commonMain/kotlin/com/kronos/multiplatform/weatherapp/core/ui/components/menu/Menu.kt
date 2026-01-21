package com.kronos.multiplatform.weatherapp.core.ui.components.menu

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AppBarAction {
    data class Menu(val items: List<MenuItem>) : AppBarAction()
    data class Icon(
        val icon: ImageVector,
        val tint:Color = Color.White,
        val contentDescription: String?,
        val onClick: () -> Unit,
    ) : AppBarAction()
}

data class MenuItem(
    val label: String,
    val icon: ImageVector? = null,
    val tint:Color = Color.White,
    val onClick: () -> Unit
)