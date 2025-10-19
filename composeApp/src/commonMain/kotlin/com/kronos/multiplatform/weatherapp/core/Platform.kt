package com.kronos.multiplatform.weatherapp.core

interface Platform {
    val platformType: PlatformType
    val deviceName: String
    val deviceId: String
}

enum class PlatformType {
    ANDROID,IOS
}

expect class DevicePlatform: Platform