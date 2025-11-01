package com.kronos.multiplatform.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //FileKit.init(this)

        setContent {
            WeatherAppRoot()
        }
    }
}

@Composable
fun WeatherAppRoot() {
    var permissionChecked by remember { mutableStateOf(false) }

    if (!permissionChecked) {
        StoragePermissionHandler(
            onPermissionReady = { permissionChecked = true }
        )
    } else {
        App()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}