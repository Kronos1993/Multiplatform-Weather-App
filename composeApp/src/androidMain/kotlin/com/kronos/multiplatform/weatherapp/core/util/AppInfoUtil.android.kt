package com.kronos.multiplatform.weatherapp.core.util

import android.content.Context
import android.content.pm.PackageManager

actual class AppInfo(private val context: Context) : IAppInfo {
    override fun getAppVersion(): String {
        try {
            val packageInfo =
                context.packageManager?.getPackageInfo("com.kronos.multiplatform.weatherapp", 0)
            return packageInfo?.versionName.toString()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return ""
    }
}