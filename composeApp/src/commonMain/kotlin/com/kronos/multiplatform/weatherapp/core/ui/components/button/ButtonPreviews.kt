package com.kronos.multiplatform.weatherapp.core.ui.components.button

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kronos.multiplatform.weatherapp.core.ui.components.IconPosition

@Preview(showBackground = true)
@Composable
fun PreviewButtonVariants() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        Button(text = "Primary Filled", onClick = {})
        Button(text = "Primary Outlined", type = ButtonType.OUTLINED, onClick = {})
        Button(text = "Primary Text", type = ButtonType.TEXT, onClick = {})

        Button(text = "Disabled", enabled = false, onClick = {}, type = ButtonType.OUTLINED)

        Button(
            text = "With Icon",
            icon = Icons.Default.Check,
            onClick = {}
        )

        Button(
            text = "Icon End",
            icon = Icons.Default.Check,
            iconPosition = IconPosition.END,
            onClick = {}
        )
    }
}