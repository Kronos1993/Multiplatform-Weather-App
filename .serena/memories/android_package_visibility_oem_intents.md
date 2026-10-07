# Android: launching other apps' activities (OEM settings) under package visibility

- targetSdk is 30+ (currently 36) → `PackageManager.resolveActivity`/`queryIntentActivities` return null/empty for another app's explicit `ComponentName` unless the manifest declares `<queries>` for that package. Pre-checking OEM components that way silently hides the feature on every device.
- `startActivity` is NOT subject to package visibility: launch explicit components directly, wrapped in try/catch (`ActivityNotFoundException`, `SecurityException`), and fall back to a public intent (`ACTION_APPLICATION_DETAILS_SETTINGS` with `package:` URI).
- Pattern in use: `BatteryOptimizationHelper.android.kt` gates the "manufacturer settings" button on `Build.MANUFACTURER` matching a known-OEM map, then tries each component in order. The alternative is adding `<queries><package …/></queries>` per OEM package to `AndroidManifest.xml`.
- OEM component names are undocumented and change across OS versions; keep the list multi-entry per OEM and keep the fallback.
