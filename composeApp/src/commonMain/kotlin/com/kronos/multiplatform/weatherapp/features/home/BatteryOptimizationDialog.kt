package com.kronos.multiplatform.weatherapp.features.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import weather_app.composeapp.generated.resources.Res
import weather_app.composeapp.generated.resources.battery_optimization_dialog_allow
import weather_app.composeapp.generated.resources.battery_optimization_dialog_body
import weather_app.composeapp.generated.resources.battery_optimization_dialog_dont_ask
import weather_app.composeapp.generated.resources.battery_optimization_dialog_not_now
import weather_app.composeapp.generated.resources.battery_optimization_dialog_oem_settings
import weather_app.composeapp.generated.resources.battery_optimization_dialog_title

@Composable
fun BatteryOptimizationDialog(
    showDialog: Boolean,
    showOemSettings: Boolean,
    showDontAskAgain: Boolean,
    onAllow: () -> Unit,
    onOemSettings: () -> Unit,
    onNotNow: () -> Unit,
    onDontAskAgain: () -> Unit,
) {
    if (!showDialog) return

    AlertDialog(
        onDismissRequest = onNotNow,
        title = {
            Text(
                text = stringResource(Res.string.battery_optimization_dialog_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Text(
                text = stringResource(Res.string.battery_optimization_dialog_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            Column {
                TextButton(onClick = onAllow) {
                    Text(
                        text = stringResource(Res.string.battery_optimization_dialog_allow),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                if (showOemSettings) {
                    TextButton(onClick = onOemSettings) {
                        Text(
                            text = stringResource(Res.string.battery_optimization_dialog_oem_settings),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        },
        dismissButton = {
            Column {
                TextButton(onClick = onNotNow) {
                    Text(text = stringResource(Res.string.battery_optimization_dialog_not_now))
                }
                if (showDontAskAgain) {
                    TextButton(onClick = onDontAskAgain) {
                        Text(
                            text = stringResource(Res.string.battery_optimization_dialog_dont_ask),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        },
        shape = MaterialTheme.shapes.medium,
    )
}
