package com.kronos.multiplatform.weatherapp.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
fun LoadingDialog(title: StringResource, message: StringResource, showDialog: Boolean) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(stringResource(title))
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(message))
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    body: String,
    showDialog: Boolean,
    confirmText: String,
    onConfirm: () -> Unit,
    cancelText: String,
    onCancel: () -> Unit,
    onClose: (() -> Unit)? = null
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                if (onClose != null)
                    onClose()
                else
                    onCancel()
            },
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = { onConfirm() }) {
                    Text(
                        text = confirmText,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { onCancel() }) {
                    Text(
                        text = cancelText,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            shape = MaterialTheme.shapes.medium
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun SelectDateDialog(
    datePickerState: DatePickerState,
    confirmText: StringResource,
    onConfirm: (selectedDateMillis: Long?) -> Unit,
    cancelText: StringResource,
    onClose: () -> Unit,
    showDialog: Boolean,
) {
    datePickerState.selectedDateMillis = Clock.System.now().toEpochMilliseconds()
    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { onClose() },
            confirmButton = {
                TextButton(onClick = { onConfirm(datePickerState.selectedDateMillis) }) {
                    Text(
                        text = stringResource(confirmText),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            modifier = Modifier,
            dismissButton = {
                TextButton(onClick = { onClose() }) {
                    Text(
                        text = stringResource(cancelText),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            content = {
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false
                )
            }
        )
    }
}

@Composable
fun ShowCityInfoDialog(
    cityName: String,
    temp: String,
    showDialog: Boolean,
    confirmText: String,
    onConfirm: () -> Unit,
    cancelText: String,
    onCancel: () -> Unit,
    onClose: (() -> Unit)? = null
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                if (onClose != null)
                    onClose()
                else
                    onCancel()
            },
            title = {},
            text = {
                Column {
                    HeaderText(
                        temp,
                        size = ComponentSize.SMALL
                    )

                    TitleText(
                        cityName,
                        size = ComponentSize.MEDIUM
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { onConfirm() }) {
                    Text(
                        text = confirmText,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { onCancel() }) {
                    Text(
                        text = cancelText,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            shape = MaterialTheme.shapes.medium
        )
    }
}

@Composable
fun ShowSelectedCityInfoDialog(
    cityName: String,
    temp: String,
    iconUrl: String,
    showDialog: Boolean,
    confirmText: String,
    onConfirm: () -> Unit,
    onClose: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onClose,
            title = {
                TitleText(
                    text = cityName,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (iconUrl.isNotEmpty()) {
                        val imageRequest = ImageRequest.Builder(LocalPlatformContext.current)
                            .data(iconUrl)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .build()

                        AsyncImage(
                            model = imageRequest,
                            contentDescription = "Weather",
                            modifier = Modifier.size(64.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    TitleText(
                        text = temp,
                        fontWeight = FontWeight.Bold,
                        size = ComponentSize.MEDIUM
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { onConfirm() }) {
                    Text(
                        text = confirmText,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            dismissButton = null
        )
    }
}