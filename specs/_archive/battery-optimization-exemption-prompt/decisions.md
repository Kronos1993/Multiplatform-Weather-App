# Decisions log: battery-optimization-exemption-prompt

<!--
APPEND-ONLY LOG of non-obvious choices made during /spec-implement.
Each entry is a reusable-knowledge candidate surfaced by /spec-finalize.
-->

## 2026-10-07 — Step 3 — OEM auto-start button gated on Build.MANUFACTURER, not resolveActivity

With targetSdk 36, Android 11+ package visibility makes `PackageManager.resolveActivity` return null
for another app's explicit component unless the manifest declares a `<queries>` entry for that
package. Pre-checking OEM components would therefore hide the button on every device, and adding
`<queries>` was outside the plan's file list. Instead `hasOemAutoStartSettings()` matches
`Build.MANUFACTURER` against the known-OEM map, and `openOemAutoStartSettings()` tries each
component with `startActivity` (visibility doesn't apply there), catching failures and falling back
to `ACTION_APPLICATION_DETAILS_SETTINGS`. AC-3's "only when it resolves" is met as "only on a known
OEM, with a guaranteed fallback".

## 2026-10-07 — Step 6 — Separate Home/Settings dialog flags in a shared ViewModel

Settings is a tab rendered inside `HomeScreen`, so `koinViewModel<BatteryOptimizationViewModel>()`
in both composables resolves the same instance (same `ViewModelStoreOwner`). A single `showDialog`
flag would render the dialog twice. The ViewModel exposes `showHomePrompt` (respects dont-ask/snooze,
evaluated once per instance) and `showSettingsDialog` (unconditional), and each screen renders only
its own. "Ahora no" snoozes for 3 days only when dismissed from the Home prompt.

## 2026-10-07 — Step 6 — Preference keys resolved in the ViewModel via compose-resources getString

Unlike `SettingsScreen`/`PreferenceViewModel`, which resolve keys with `stringResource` in the
composable and pass them down, `BatteryOptimizationViewModel` resolves its keys with the suspend
`org.jetbrains.compose.resources.getString(Res.string.…)`. This keeps both screens free of key
plumbing. The snooze timestamp is stored as a Double through the existing
`Get/SetDoublePreferenceUseCase` (no Long preference use case exists).

## 2026-10-07 — Step 7 — ktlint -F applied to whole touched files

To satisfy "touched files pass ktlint clean", the standalone `ktlint -F` (see
`mem:ktlint_scoped_formatting_workflow`) was run on the 10 touched/new `.kt` files. That also fixed
pre-existing mechanical violations in `HomeScreen.kt` (trailing commas, blank lines, import order)
and `core/di/Module.kt` (spacing, final newline). These are format-only, no behavior change, but
they inflate those two files' diffs.

## 2026-10-07 — Step 8 — "Permitir" opens app info instead of the battery-optimization list

On a Samsung (One UI) test device, `ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS` opened the list
filtered to "Apps not optimized", so the app was invisible until the user switched the filter to
"All". The maintainer chose to open `ACTION_APPLICATION_DETAILS_SETTINGS` for the package instead:
the app is always there, and Battery → Unrestricted is two taps away on Android 12+ / One UI. The
list stays as the fallback. Rejected: the direct `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`
request (Play restricted-permission policy) and keeping the list with "switch the filter" copy.

## 2026-10-07 — Pre-handoff — aggregate `./gradlew build` red for a pre-existing reason

`./gradlew build --continue` failed on `kspCommonMainKotlinMetadata` plus `Java heap space` OOMs in
the iOS release framework link tasks. This matches `mem:gradle_ksp_multitarget_build_quirk`
(pre-existing KSP task-graph defect; the aggregate build isn't the repo's local gate). The gate,
`./gradlew :composeApp:assembleDebug`, is green. The OOM in `linkReleaseFramework*` suggests the
Gradle/Kotlin daemon heap is also too small for an all-target build; worth noting if CI ever runs it.
