package com.kronos.multiplatform.weatherapp.core

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.core.content.edit
import java.net.NetworkInterface
import java.util.UUID
import kotlin.getValue

actual class DevicePlatform(private val context: Context) : Platform {
    override val platformType = PlatformType.ANDROID

    override val deviceName: String by lazy {
        val customName = try {
            Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
                ?: Settings.System.getString(context.contentResolver, "device_name")
                ?: ""
        } catch (e: Exception) {
            ""
        }
        val manufacturerModel = "${Build.MANUFACTURER} ${Build.MODEL}".trim()

        if (customName.isNotBlank()) {
            "$customName ($manufacturerModel)"
        } else {
            manufacturerModel
        }
    }

    override val deviceId: String by lazy {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)?.takeIf { it.isNotEmpty() }
            ?: getMacAddress().takeIf { it.isNotEmpty() }
            ?: getPersistentUUID(context).toString()
    }

    private fun getMacAddress(): String {
        return try {
            NetworkInterface.getNetworkInterfaces()
                .toList()
                .firstOrNull { it.name.equals("wlan0", ignoreCase = true) }
                ?.hardwareAddress
                ?.joinToString("") { String.format("%02X", it) }
                ?: ""
        } catch (ex: Exception) {
            ""
        }
    }

    private fun getPersistentUUID(context: Context): UUID {
        val prefs = context.getSharedPreferences("device_prefs", Context.MODE_PRIVATE)
        return prefs.getString("uuid", null)?.let { UUID.fromString(it) } ?: run {
            val newUUID = UUID.randomUUID()
            prefs.edit { putString("uuid", newUUID.toString()) }
            newUUID
        }
    }
}