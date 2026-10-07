---
spec_id: battery-optimization-exemption-prompt
generated_by: /spec-plan
generated_at: 2026-10-07T00:00:00Z
---

> **Blockers**: none
> **Depends on**: none
>
> /spec-implement refuses to start while any blocker remains OR any
> dependency is not yet archived. To clear:
> - blocker → resolve the OQ in proposal.md §9, remove its ID from `blockers:`
> - dependency → wait for the depended spec to land in `specs/_archive/`,
>   then remove its ID from `depends_on:`
>
> Re-run /spec-plan after either to regenerate this banner.

# Plan: Solicitar exención de optimización de batería para todos los dispositivos Android

## Strategy

Add a platform helper behind an interface, following the existing `IExpectedIntents`/`ExpectedIntents`
expect/actual + Koin pattern. Then add a small state holder and a dialog that `HomeScreen` shows
once the existing `PermissionFlow` reaches `Completed`. The platform layer goes first, so the UI
steps compile against a real binding. All steps are direct edits; no new screen is added, so
`/new-feature` does not apply.

## Steps

### Step 1 — Map the Home permission flow and preference keys [investigate]

- **Files / symbols**:
  - `composeApp/src/commonMain/kotlin/com/kronos/multiplatform/weatherapp/features/home/HomeScreen.kt`: `HomeScreen`, `PermissionFlow`, the two `LaunchedEffect`s that drive it
  - `composeApp/src/commonMain/kotlin/com/kronos/multiplatform/weatherapp/core/viewmodel/PermissionViewModel.kt`: how it is constructed and scoped
  - `composeApp/src/commonMain/kotlin/com/kronos/multiplatform/weatherapp/domain/usecase/preferences/GetBooleanPreferenceUseCase.kt` / `SetBooleanPreferenceUseCase.kt`: `Params` shape
  - `composeApp/src/commonMain/composeResources/values/preference_key.xml`: key naming convention
  - `composeApp/src/commonMain/kotlin/com/kronos/multiplatform/weatherapp/features/home/setting/SettingScreen.kt`: `SettingsScreen` row list, `SettingRadioOptions` signature
- **Question(s) to answer**:
  - Exact spot to show the dialog once `currentPermissionFlow == PermissionFlow.Completed` (and on later launches where the flow completes immediately)
  - How `R.string.*_key` preference keys are resolved from commonMain (Compose `Res.string` vs Android `R`)
  - Whether a storage type for OQ-3's snooze timestamp already exists (Double/String preference use case)
  - Which row component to use for a clickable Settings entry with a dynamic subtitle (`SettingRadioOptions` is radio-only; look for a plain clickable setting row under `components/`, else build one inside `SettingScreen.kt`)
- **Outputs to record**: proposal.md §4 `features/home` and `features/home/setting` rows (concrete insertion point, row component); OQ-3 note on storage type.
- **Why**: `HomeScreen`'s state machine is the riskiest touch point (§7).

### Step 2 — Add the commonMain helper contract [implement]

- **Skill**: direct edits
- **Area(s)**: `core`
- **Files / symbols**:
  - `composeApp/src/commonMain/kotlin/com/kronos/multiplatform/weatherapp/core/util/BatteryOptimizationHelper.kt` (new): `interface IBatteryOptimizationHelper { fun isSupported(): Boolean; fun isIgnoringBatteryOptimizations(): Boolean; fun requestIgnoreBatteryOptimizations(); fun hasOemAutoStartSettings(): Boolean; fun openOemAutoStartSettings() }` + `expect class BatteryOptimizationHelper : IBatteryOptimizationHelper`
- **Skill args / inputs**: none
- **Verification**: file exists. The build fails until Step 3 adds the actuals, so verify after Step 3.

### Step 3 — Add the Android and iOS actuals [implement]

- **Skill**: direct edits
- **Area(s)**: `core`
- **Files / symbols**:
  - `composeApp/src/androidMain/kotlin/com/kronos/multiplatform/weatherapp/core/util/BatteryOptimizationHelper.android.kt` (new): `actual class BatteryOptimizationHelper(private val context: Context)`
    - `PowerManager.isIgnoringBatteryOptimizations`
    - `ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS` (`FLAG_ACTIVITY_NEW_TASK`), falling back to `ACTION_APPLICATION_DETAILS_SETTINGS` (OQ-1: no direct request)
    - an OEM `ComponentName` list checked with `resolveActivity`
    - every `startActivity` wrapped against `ActivityNotFoundException`/`SecurityException`
  - `composeApp/src/iosMain/kotlin/com/kronos/multiplatform/weatherapp/core/util/BatteryOptimizationHelper.ios.kt` (new): no-op `actual class BatteryOptimizationHelper`; `isSupported() = false`, `isIgnoringBatteryOptimizations() = true`, `hasOemAutoStartSettings() = false`
- **Skill args / inputs**: none (OQ-1 resolved: settings list only)
- **Verification**: `./gradlew :composeApp:assembleDebug` green.

### Step 4 — Register the helper in Koin [implement]

- **Skill**: direct edits
- **Area(s)**: `di`
- **Files / symbols**:
  - `composeApp/src/androidMain/kotlin/com/kronos/multiplatform/weatherapp/core/di/Module.android.kt`: `singleOf(::BatteryOptimizationHelper).bind<IBatteryOptimizationHelper>()` next to the `ExpectedIntents` binding
  - `composeApp/src/iosMain/kotlin/com/kronos/multiplatform/weatherapp/core/di/Module.ios.kt`: same binding
- **Skill args / inputs**: none
- **Verification**: build green; both modules contain the binding.

### Step 5 — Add strings and the preference keys [implement]

- **Skill**: direct edits
- **Area(s)**: resources
- **Files / symbols**:
  - `composeApp/src/commonMain/composeResources/values/strings.xml` + `values-es/strings.xml`: title, body, and the four button labels (OQ-5 draft unless changed)
  - `composeApp/src/commonMain/composeResources/values/preference_key.xml`: `battery_optimization_dont_ask_key` (+ snooze key per OQ-3)
  - `composeApp/src/commonMain/composeResources/values/preference_strings.xml` + `values-es/preference_strings.xml`: Settings row title + the two status subtitles (OQ-5)
- **Skill args / inputs**: OQ-3, OQ-5
- **Verification**: build green; each new string exists in both `values` and `values-es`.

### Step 6 — Add the state holder, the dialog, and the HomeScreen wiring [implement]

- **Skill**: direct edits
- **Area(s)**: `core` (viewmodel), `features/home`
- **Files / symbols** (locations confirmed, OQ-2):
  - `core/viewmodel/BatteryOptimizationViewModel.kt` (new), extends `ParentViewModel`:
    - injects `IBatteryOptimizationHelper`, `GetBooleanPreferenceUseCase`, `SetBooleanPreferenceUseCase` (+ snooze storage per OQ-3)
    - exposes `shouldShowPrompt: StateFlow<Boolean>` (Home: respects dont-ask/snooze) and `isRestricted: StateFlow<Boolean>` (Settings status)
    - `openFromSettings()` shows the dialog unconditionally
    - actions `onAllow()`, `onOemSettings()`, `onNotNow()`, `onDontAskAgain()`
    - re-checks `isIgnoringBatteryOptimizations()` on resume
  - `core/di/Module.kt`: `viewModelOf(::BatteryOptimizationViewModel)`
  - `features/home/BatteryOptimizationDialog.kt` (new): `AlertDialog` composable; OEM button shown only when `hasOemAutoStartSettings()`
  - `features/home/HomeScreen.kt`: show the dialog only when `currentPermissionFlow == PermissionFlow.Completed && shouldShowPrompt`
- **Skill args / inputs**: Step 1 output, OQ-3
- **Verification**: build green; `PermissionFlow` transitions are unchanged (diff shows no edits inside the existing `when` branches).

### Step 7 — Add the Settings entry [implement]

- **Skill**: direct edits
- **Area(s)**: `features/home/setting`
- **Files / symbols**:
  - `composeApp/src/commonMain/kotlin/com/kronos/multiplatform/weatherapp/features/home/setting/SettingScreen.kt`: `SettingsScreen` gets `koinViewModel<BatteryOptimizationViewModel>()`. When `isSupported()` it adds a "Background updates" row (row component per Step 1) after the measure-unit row. Its subtitle comes from `isRestricted`, it calls `openFromSettings()` on click, and it renders `BatteryOptimizationDialog` when shown. Status is re-checked on resume.
- **Skill args / inputs**: Step 1 output
- **Why**: users who chose "No volver a preguntar" (or had the exemption revoked) need a way back
- **Verification**: build green; the row is guarded by `isSupported()`.

### Step 8 — Build, lint, parity check, and manual run [verify]

- **What to check**:
  - `./gradlew :composeApp:assembleDebug`
  - `./gradlew :composeApp:ktlintCheck`, filtered to the new/touched files
  - `expect class BatteryOptimizationHelper` has actuals in androidMain + iosMain
  - manual emulator run covering AC-1..AC-6: fresh install, grant/deny the permission prompts, then the dialog appears; Allow; "No volver a preguntar"; "Ahora no"; switch language to ES; Settings row status + tap after "No volver a preguntar" (AC-9)
- **Pass criteria**: build green, no ktlint violations in touched files, parity confirmed, AC-1..AC-6 and AC-9 observed on the emulator.

## Dependencies

- Step 3 depends on Step 2 (the actuals need the `expect`).
- Step 4 depends on Step 3.
- Step 6 depends on Steps 1, 4, and 5.
- Step 7 depends on Step 6 (reuses `BatteryOptimizationViewModel` + `BatteryOptimizationDialog`).

## Out-of-band actions

- Honor/MagicOS real-device check (proposal §Out-of-band).
- iOS build via Xcode, because `iosMain` gets a new `actual` file.

## Rollback

All changes are additive: new files under `core/util`, `core/viewmodel`, `features/home`, new
strings/keys, two Koin bindings, one `HomeScreen` condition, one `SettingsScreen` row.
Revert by deleting the new files and running `git restore` on the touched ones. No data migration
is involved; the unused preference keys are harmless.
