# Android: sending users to lift battery optimization (per-OEM behavior)

- `ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS` (generic list) is unreliable UX: One UI (Samsung) opens it filtered to "Apps not optimized", so the app is invisible until the user switches the filter to "All".
- Preferred: `ACTION_APPLICATION_DETAILS_SETTINGS` for our package → Battery → Unrestricted (always shows the app, no permission). Generic list kept only as fallback.
- `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` + `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` permission was deliberately rejected: Google Play restricted-permission policy.
- Battery exemption ≠ OEM auto-launch. MagicOS/EMUI ("App launch"), MIUI ("Autostart"), ColorOS/FuntouchOS have separate managers; Samsung's equivalent is Device care → Battery → Background usage limits → "Never sleeping apps" (the `com.samsung.android.lool` battery activity lands on Battery, not deeper; no public deep link).
- Background: OEM process killing stops WorkManager workers (widgets + notifications), first reported on Honor 200 / MagicOS. See `mem:android_package_visibility_oem_intents` for how the OEM screens are launched.
