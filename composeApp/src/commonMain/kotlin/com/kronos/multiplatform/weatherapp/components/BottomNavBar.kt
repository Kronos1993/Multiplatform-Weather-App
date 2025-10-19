package com.kronos.multiplatform.weatherapp.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun BottomNavBar(
    selectedIndex: Int,
    options: List<Triple<StringResource, DrawableResource, Unit>>,
    modifier: Modifier = Modifier,
    labelled: Boolean = true,
) {
    NavigationBar(
        modifier = modifier.fillMaxWidth(),
    ) {
        for ((index, option) in options.withIndex()) {
            NavigationBarItem(
                icon = {
                    Icon(
                        painterResource(option.second),
                        contentDescription = stringResource(option.first),
                        modifier = Modifier.height(24.dp)
                    )
                },
                label = {
                    Text(
                        stringResource(option.first),
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                },
                selected = selectedIndex == index, // Use index for selected state
                alwaysShowLabel = labelled,
                onClick = { option.third }
            )
        }
    }
}