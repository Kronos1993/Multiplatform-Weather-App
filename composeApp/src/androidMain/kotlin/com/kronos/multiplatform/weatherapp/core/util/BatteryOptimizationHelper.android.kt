package com.kronos.multiplatform.weatherapp.core.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.core.net.toUri

actual class BatteryOptimizationHelper(private val context: Context) : IBatteryOptimizationHelper {
    override fun isSupported(): Boolean = true

    override fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    // The generic optimization list hides the app behind a filter on some OEMs (e.g. One UI defaults to
    // "Not optimized"), so open the app's own info screen, where Battery -> Unrestricted is always reachable.
    override fun requestIgnoreBatteryOptimizations() {
        val opened = openAppDetails()
        if (!opened) tryStart(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
    }

    // Package visibility (targetSdk 30+) hides other apps' activities from resolveActivity without a
    // <queries> entry, so the button is gated on the manufacturer and the components are tried in order.
    override fun hasOemAutoStartSettings(): Boolean = oemComponents().isNotEmpty()

    override fun openOemAutoStartSettings() {
        val opened = oemComponents().any { tryStart(Intent().setComponent(it)) }
        if (!opened) openAppDetails()
    }

    private fun openAppDetails(): Boolean {
        return tryStart(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData("package:${context.packageName}".toUri()),
        )
    }

    private fun tryStart(intent: Intent): Boolean {
        return try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            true
        } catch (e: Exception) {
            Log.w(TAG, "Unable to open ${intent.component ?: intent.action}: ${e.javaClass.simpleName}")
            false
        }
    }

    private fun oemComponents(): List<ComponentName> {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return OEM_AUTO_START_COMPONENTS
            .filterKeys { manufacturer.contains(it) }
            .values
            .flatten()
    }

    private companion object {
        const val TAG = "BatteryOptimization"

        val OEM_AUTO_START_COMPONENTS: Map<String, List<ComponentName>> = mapOf(
            "honor" to listOf(
                ComponentName(
                    "com.hihonor.systemmanager",
                    "com.hihonor.systemmanager.startupmgr.ui.StartupNormalAppListActivity",
                ),
                ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity",
                ),
            ),
            "huawei" to listOf(
                ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity",
                ),
                ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.optimize.process.ProtectActivity",
                ),
            ),
            "xiaomi" to listOf(
                ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity",
                ),
            ),
            "oppo" to listOf(
                ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.permission.startup.StartupAppListActivity",
                ),
                ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.startupapp.StartupAppListActivity",
                ),
            ),
            "vivo" to listOf(
                ComponentName(
                    "com.vivo.permissionmanager",
                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity",
                ),
                ComponentName(
                    "com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity",
                ),
            ),
            "samsung" to listOf(
                ComponentName(
                    "com.samsung.android.lool",
                    "com.samsung.android.sm.battery.ui.BatteryActivity",
                ),
            ),
        )
    }
}
