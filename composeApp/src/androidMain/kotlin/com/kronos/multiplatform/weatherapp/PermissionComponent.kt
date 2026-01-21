package com.kronos.multiplatform.weatherapp

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kronos.multiplatform.weatherapp.core.ui.components.ConfirmDialog
import org.jetbrains.compose.resources.stringResource
import weather_app.composeapp.generated.resources.Res
import weather_app.composeapp.generated.resources.cancel
import weather_app.composeapp.generated.resources.ok
import weather_app.composeapp.generated.resources.storage_permission_dialog_message
import weather_app.composeapp.generated.resources.storage_permission_dialog_title

@Composable
fun StoragePermissionHandler(
    onPermissionReady: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var showDialog by remember { mutableStateOf(false) }
    var permissionGranted by remember { mutableStateOf(hasStoragePermission()) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (hasStoragePermission()) {
                    permissionGranted = true
                    onPermissionReady()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        if (!permissionGranted) {
            showDialog = true
        } else {
            onPermissionReady()
        }
    }

    ConfirmDialog(
        showDialog = showDialog,
        title = stringResource(Res.string.storage_permission_dialog_title),
        body = stringResource(Res.string.storage_permission_dialog_message),
        onConfirm = {
            activity?.let { openManageStorageSettings(it) }
            showDialog = false
        },
        confirmText = stringResource(Res.string.ok),
        onCancel = {
            showDialog = false
            onPermissionReady()
        },
        cancelText = stringResource(Res.string.cancel),
        onClose = {
            showDialog = false
            onPermissionReady()
        }
    )
}

fun hasStoragePermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        true
    }
}

fun openManageStorageSettings(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = "package:${activity.packageName}".toUri()
            activity.startActivity(intent)
        } catch (_: Exception) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            activity.startActivity(intent)
        }
    }
}