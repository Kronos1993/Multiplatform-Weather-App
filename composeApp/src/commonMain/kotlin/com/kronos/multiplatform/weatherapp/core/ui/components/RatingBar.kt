package com.kronos.multiplatform.weatherapp.core.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kronos.multiplatform.weatherapp.core.ui.components.theme.ratingColorContainerDark
import com.kronos.multiplatform.weatherapp.core.ui.components.theme.ratingColorContainerLight
import com.kronos.multiplatform.weatherapp.core.ui.components.theme.surfaceDimDark
import com.kronos.multiplatform.weatherapp.core.ui.components.theme.surfaceDimLight

@Composable
fun StarRatingBar(
    maxStars: Int = 5,
    modifier: Modifier = Modifier,
    rating: Float,
    isDarkTheme: Boolean,
    onRatingChanged: (Float) -> Unit,
    starSize: Dp = 24.dp
) {
    val density = LocalDensity.current.density
    val starSize = starSize
    val starSpacing = (0.5f * density).dp

    Row(
        modifier = modifier.selectableGroup(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxStars) {
            val isSelected = i <= rating
            val icon = if (isSelected) Icons.Filled.Star else Icons.Outlined.Star
            val iconTintColor = if (isSelected) {
                if (isDarkTheme)
                    ratingColorContainerDark
                else
                    ratingColorContainerLight
            } else {
                if (isDarkTheme)
                    surfaceDimDark
                else
                    surfaceDimLight
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTintColor,
                modifier = Modifier
                    .selectable(
                        selected = isSelected,
                        onClick = {
                            onRatingChanged(i.toFloat())
                        }
                    )
                    .width(starSize).height(starSize)
            )

            if (i < maxStars) {
                Spacer(modifier = Modifier.width(starSpacing))
            }
        }
    }
}