package com.kronos.multiplatform.weatherapp.core

import platform.UIKit.UIDevice
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDefaults
import kotlin.run
import kotlin.text.isNullOrEmpty

actual class DevicePlatform : Platform {
    override val platformType: PlatformType = PlatformType.IOS

    override val deviceName: String = UIDevice.currentDevice.name

    override val deviceId: String = getPersistentDeviceIdentifier()

    private fun getPersistentDeviceIdentifier(): String {
        return try {
            val vendorId = UIDevice.currentDevice.identifierForVendor?.UUIDString
            if (!vendorId.isNullOrEmpty()) return vendorId
            getOrCreatePersistentUUID()
        } catch (e: Exception) {
            // Fallback a UUID temporal
            NSUUID().UUIDString
        }
    }

    private fun getOrCreatePersistentUUID(): String {
        val userDefaults = NSUserDefaults.standardUserDefaults
        val key = "persistent_device_uuid"

        return userDefaults.stringForKey(key) ?: run {
            val newUUID = NSUUID().UUIDString
            userDefaults.setObject(newUUID, key)
            userDefaults.synchronize()
            newUUID
        }
    }
}