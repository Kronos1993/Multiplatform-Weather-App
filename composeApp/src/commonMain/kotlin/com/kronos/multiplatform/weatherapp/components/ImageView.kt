package com.kronos.multiplatform.weatherapp.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CircleImageView(image: ImageBitmap, size: Dp = 64.dp, cornerRadius: Dp = 32.dp,onClick: () -> Unit) {
    Image(
        image,
        contentDescription = "Imagen desde cámara o almacenamiento",
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color.Gray)
            .clickable {
                onClick()
            },
        contentScale = ContentScale.Crop
    )
}

@Composable
fun CircleImageView(image: ImageVector, size: Dp = 64.dp, cornerRadius: Dp = 32.dp,onClick: () -> Unit) {
    Image(
        image,
        contentDescription = "Imagen desde cámara o almacenamiento",
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .clickable {
                onClick()
            },
        contentScale = ContentScale.Crop
    )
}