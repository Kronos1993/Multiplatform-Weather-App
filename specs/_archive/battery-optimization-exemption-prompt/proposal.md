---
spec_id: battery-optimization-exemption-prompt
title: Solicitar exención de optimización de batería para todos los dispositivos Android
type: story
priority: high
source: manual
source_ref: Reporte de usuario (Honor 200 / MagicOS) — notificaciones y widgets no se actualizan; dictado por el mantenedor en chat
created: 2026-10-07
status: INTAKE_PARSED
recommend_split: no
blockers: []
depends_on: []
expect_actual_touched: yes
localization_touched: no
branch_suggested: feature/battery-optimization-exemption-prompt
---

# Proposal: Solicitar exención de optimización de batería para todos los dispositivos Android

## 1. Source

Manual intake (`story.md`): a user reported that on a Honor 200 (MagicOS) neither weather
notifications nor Glance widgets update. The periodic WorkManager workers don't run because the
OS kills the app in the background. The maintainer asked that the battery prompt appear for
**all** Android users, not only Honor/Huawei. The code-side worker fixes shipped separately in
PR #49 (`bugfix/background-worker-reliability`).

## 2. Problem / Why

The app never asks to be excluded from battery optimization and never points the user to the
OEM's background/auto-launch settings. On aggressive OEMs (MagicOS, EMUI, MIUI, ColorOS,
FuntouchOS, One UI's "sleeping apps") and under Doze on stock Android, the workers
(`WeatherNotificationWorker`, `WeatherAlertNotificationWorker`, `WeatherWidgetUpdateWorker`,
`WeatherSuggestionNotificationWorker`) are deferred indefinitely or never run. Users see stale
widgets and no notifications, with no hint how to fix it.

## 3. Scope

**In scope**
- A platform helper `IBatteryOptimizationHelper` (commonMain interface + `expect class`), following
  the existing `IExpectedIntents`/`ExpectedIntents` pattern:
  - `isSupported()`: `true` on Android, `false` on iOS. It hides the Settings entry on iOS.
  - `isIgnoringBatteryOptimizations()`: Android uses `PowerManager.isIgnoringBatteryOptimizations(packageName)`; iOS always returns `true`, so no prompt.
  - `requestIgnoreBatteryOptimizations()`: opens `Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS` (OQ-1 resolved: settings list only, no manifest permission), with fallback to `ACTION_APPLICATION_DETAILS_SETTINGS`.
  - `hasOemAutoStartSettings()` / `openOemAutoStartSettings()`: known OEM component list (Honor, Huawei, Xiaomi, Oppo, Vivo, Samsung, …), used only when the intent resolves via `PackageManager`.
- Android `actual` (real) and iOS `actual` (no-op), registered in `Module.android.kt` / `Module.ios.kt`.
- A dialog shown from `HomeScreen` **after** the existing notification/location `PermissionFlow`
  reaches `Completed`, on every Android device where the app is still subject to optimization.
  It has these actions:
  - "Permitir" (opens the system screen),
  - "Ajustes del fabricante" (shown only if `hasOemAutoStartSettings()`),
  - "Ahora no" (snooze, see OQ-3),
  - "No volver a preguntar" (persisted).
- The "don't ask again" / snooze state is persisted through the existing `GetBooleanPreferenceUseCase`/`SetBooleanPreferenceUseCase` (plus whatever OQ-3 needs), with a new key in `preference_key.xml`.
- **Settings entry** (`features/home/setting/SettingScreen.kt`), Android only (`isSupported()`).
  - A "Actualizaciones en segundo plano" / "Background updates" row. Its subtitle shows the current
    status: unrestricted vs. restricted by battery optimization, re-checked on resume.
  - Tapping it opens the same `BatteryOptimizationDialog`, ignoring the "no volver a preguntar" and
    snooze state, so users who dismissed the Home prompt can still fix it.
  - When already unrestricted, the dialog still offers "Ajustes del fabricante" if available, since
    MagicOS auto-launch is separate from Doze.
- New strings in `composeResources/values/strings.xml` and `values-es/strings.xml` (Settings row in `preference_strings.xml`).

**Split override**: `/spec-plan`'s heuristic recommended a split (3 capability areas: core + di + features/home; 4 risks). The maintainer chose not to split (2026-10-07): the `di` change is two mechanical bindings, and the helper has no consumer besides this prompt, so a helper-only PR would ship dead code.

**Out of scope**
- Worker fixes (shipped in PR #49).
- iOS/desktop behavior changes. iOS has no equivalent concept, and the build declares no `jvm()` target (see §5a).
- Any Honor-only path. Per the maintainer, the prompt is for every Android device; OEM settings are only an *extra* button.

## 4. Affected areas

| Area | Source set(s) | Class(es) / file(s) touched | Change type | Resolved by | Notes |
|--------|--------------|-------------------------------|-------------|-------------|-------|
| core | commonMain | `core/util/BatteryOptimizationHelper.kt` (new): `IBatteryOptimizationHelper` + `expect class BatteryOptimizationHelper` | new | | mirrors `core/util/ExpectedIntents.kt` |
| core | androidMain | `core/util/BatteryOptimizationHelper.android.kt` (new): `actual class BatteryOptimizationHelper(context)` | new | | PowerManager + Settings intents + OEM component list |
| core | iosMain | `core/util/BatteryOptimizationHelper.ios.kt` (new): no-op `actual` | new | | `isIgnoringBatteryOptimizations() = true` |
| di | androidMain, iosMain | `core/di/Module.android.kt`, `core/di/Module.ios.kt`: `singleOf(::BatteryOptimizationHelper).bind<IBatteryOptimizationHelper>()` | edit | | same block as `ExpectedIntents` |
| features/home | commonMain | `features/home/HomeScreen.kt` (show dialog after `PermissionFlow.Completed`); dialog composable + state holder | edit + new | Step 1 ✓ | Step 1: render the dialog inside `HomeScreen`'s `Surface` next to `ConfirmDialog`, gated on `currentPermissionFlow == PermissionFlow.Completed` (`HomeScreen.kt`, the `Completed` value is set in the `LaunchedEffect(permissionNotification, permissionLocation)` branches). The `when` branches are untouched. On later launches with both permissions granted, the `Idle → else` branch sets `Completed` immediately. |
| features/home/setting | commonMain | `features/home/setting/SettingScreen.kt`: new row + `koinViewModel<BatteryOptimizationViewModel>()` | edit | Step 1 ✓ | Step 1: no existing row fits. `SettingClickableOption` (`core/ui/components/Setting.kt`) has no subtitle and takes a `DrawableResource`; `SettingRadioOptions` is radio-only. Build a private `BackgroundUpdatesSettingRow` inside `SettingScreen.kt` that mirrors `SettingRadioOptions`' ImageVector header (`TitleText` + `BodyText`, `Color.White`), clickable. |
| resources | commonMain | `composeResources/values/strings.xml`, `values-es/strings.xml`, `values/preference_key.xml`, `values[-es]/preference_strings.xml` | edit | | Compose-only strings |

## 5. Architectural gauntlet (this repo's hard rules)

### 5a. Always explicit (no shortcut)

- [x] **Expect/actual parity**: adds one new `expect class BatteryOptimizationHelper`. Actuals are
      needed in `androidMain` (real) and `iosMain` (no-op). `composeApp/build.gradle.kts` declares
      only `androidTarget` + iOS targets (no `jvm()`); the existing `expect class ExpectedIntents`
      also has no jvmMain actual. So no jvmMain actual is needed, and adding one would be dead code.
      Note: root `CLAUDE.md` still lists Desktop as a target; flagged as out-of-band, not changed here.
      Approach: Steps 2–3 add both actuals; Step 8 checks parity.
- [x] **Dual localization**: N/A (`localization_touched: no`). The new strings appear only in the
      Compose dialog. No iOS notification or widget text changes. Both Compose locales (`values` and
      `values-es`) are still updated.
      Approach: Step 5.
- [x] **Secrets & logging**: intent only, since there is no diff yet. The helper logs nothing beyond
      intent-resolution failures (exception class/message). It never touches the WeatherAPI key.
      `/spec-implement`'s pre-handoff secrets grep re-checks this against the real diff.
      Approach: confirmed.
- [x] **No automated tests exist**: verification is `./gradlew :composeApp:assembleDebug` +
      `ktlintCheck` + a manual run on an Android emulator. A real Honor/MagicOS device check is
      out-of-band.
      Acknowledged: yes

### 5b. Confinement-conditional

- [ ] **Confinement claim**: not claimed. The change adds a new `core` platform helper and Koin bindings.

- [x] **Domain/data boundary (DIP)**: no repository or data access is added. The helper is a
      platform utility, injected through the `IBatteryOptimizationHelper` interface (never the
      `actual` class), exactly like `IExpectedIntents` is injected into `AboutViewModel`. Persisted
      "don't ask again" state goes through the existing preference use cases
      (`GetBooleanPreferenceUseCase` / `SetBooleanPreferenceUseCase`), never the preference repository.
      Approach: as described.
- [x] **Result-type error handling**: no repository call can fail here. Intent launches catch
      `ActivityNotFoundException`/`SecurityException` inside the Android `actual` and fall back to the
      generic settings screen. Nothing is thrown to the UI.
      Approach: as described.

## 6. Skills

**Skills**: direct edits

## 7. Risks

- **Usability of the settings list**: with OQ-1 = settings list only, the user lands on the generic "battery optimization" list and has to find WeatherApp (on some OEMs, switch the filter to "All apps"). The dialog body should say so. No Play policy risk, since no restricted permission is declared.
- **OEM auto-start intents are undocumented**: component names change between OS versions and can
  throw `SecurityException`/`ActivityNotFoundException`. Every launch must be `resolveActivity`-checked
  and wrapped, with a fallback to the app-details screen.
- **`HomeScreen`'s hand-rolled `PermissionFlow` state machine**: putting the dialog in the wrong
  state could re-trigger or skip the notification/location prompts. The dialog must only key off
  `PermissionFlow.Completed`.
- **Exemption ≠ fix on MagicOS**: Honor's "Gestión de inicio de apps" is separate from Doze
  whitelisting. The exemption alone may not be enough, which is why the OEM button exists, and
  this can only be confirmed on a real Honor device (out-of-band).

## Out-of-band actions

- Manual check on a real Honor 200 (or other MagicOS device): after granting the exemption + OEM
  auto-launch, the widget and hourly notification refresh with the app closed for a few hours.
- Root `CLAUDE.md` lists Desktop (JVM) as a target, but `build.gradle.kts` declares no `jvm()`
  target. Update the doc or restore the target (separate decision).

## 8. Acceptance criteria

- [ ] **AC-1**: On an Android emulator/device where the app is subject to battery optimization, after the notification/location permission prompts finish, the battery-optimization dialog appears, regardless of manufacturer.
- [ ] **AC-2** (revised 2026-10-07, see OQ-1): "Permitir" opens WeatherApp's app-info screen (`ACTION_APPLICATION_DETAILS_SETTINGS`), where Battery → Unrestricted is reachable. If that fails, it falls back to `ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS`. The manifest declares no `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`. No crash in either path.
- [ ] **AC-3**: The "Ajustes del fabricante" button appears only when a known OEM auto-start activity resolves on the device. On a stock emulator it is hidden.
- [ ] **AC-4**: After granting the exemption, the dialog no longer appears on later launches (`isIgnoringBatteryOptimizations()` is true).
- [ ] **AC-5**: "No volver a preguntar" stops the dialog permanently across app restarts. "Ahora no" hides it according to OQ-3's rule and never shows it again in the same session.
- [ ] **AC-9**: On Android, Settings shows a "Background updates" row with the current status. Tapping it opens the battery dialog even after "No volver a preguntar". After returning from system settings with the exemption granted, the status updates. On iOS the row is not shown.
- [ ] **AC-6**: The dialog text shows in English and Spanish according to the app language (`values` / `values-es`).
- [ ] **AC-7**: The iOS build is unaffected; the iOS `actual` returns `true`, so no dialog. Manual Xcode build per pre-handoff checks.
- [ ] **AC-8**: `./gradlew :composeApp:assembleDebug` is green and `./gradlew :composeApp:ktlintCheck` reports no violations in new/touched files.

## 9. Open questions

- **OQ-1** — ~~Which mechanism should "Permitir" use?~~ **Resolved (2026-10-07)**: settings list only (`ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS`). No manifest permission, no Play policy risk.
  **Revised 2026-10-07 (during /spec-implement, maintainer decision)**: tested on a Samsung phone, the dialog opened the list, but One UI defaults its filter to "Apps not optimized", so WeatherApp wasn't visible. "Permitir" now opens the app's own info screen (`ACTION_APPLICATION_DETAILS_SETTINGS`), with the list as fallback. Still no manifest permission. The dialog body tells the user to open Battery → Unrestricted.
- **OQ-2** — **Resolved (2026-10-07)**: locations confirmed as proposed. ~~Confirm the new file locations (root `CLAUDE.md`: confirm before creating any file)~~:
  - `core/util/BatteryOptimizationHelper.kt` (+ `.android.kt`, `.ios.kt`), next to `ExpectedIntents`.
  - A state holder `core/viewmodel/BatteryOptimizationViewModel.kt` (next to `PermissionViewModel`), registered with `viewModelOf` in `core/di/Module.kt`.
  - The dialog composable in `features/home/BatteryOptimizationDialog.kt`.
- **OQ-3** — **Resolved (2026-10-07)**: default applied, so "Ahora no" snoozes for 3 days (Home prompt only). Step 1 note: `GetDoublePreferenceUseCase`/`SetDoublePreferenceUseCase` already exist (`domain/usecase/preferences/`), so the snooze-until epoch millis is stored as a Double (`kotlin.time.Clock.System.now().toEpochMilliseconds()`, the same `Clock` the codebase uses). Default rule (3 days) applied. Preference keys are Compose resources (`preference_key.xml`); the ViewModel resolves them with `org.jetbrains.compose.resources.getString(Res.string.…)`. Resume re-checks use `LifecycleResumeEffect` (`lifecycle-runtime-compose` is already a dependency). Original question: The snooze rule for "Ahora no". Default proposal: hide for 3 days, stored as an epoch-millis
  value via the existing preference use cases. Alternative: show again on the next cold start.
- **OQ-4** — **Resolved (2026-10-07)**: the Settings entry is **in scope** for this spec (maintainer decision). See §3 and AC-9.
- **OQ-5** — **Resolved (2026-10-07)**: draft copy shipped, with the body revised for the app-info flow (see OQ-1 revision); the maintainer accepted it on device. Draft:
  - Title: "Mantén el clima actualizado" / "Keep your weather up to date".
  - Body: "Para que las notificaciones y los widgets se actualicen con la app cerrada, busca WeatherApp en la lista y desactiva la optimización de batería." / "To keep notifications and widgets updating while the app is closed, find WeatherApp in the list and turn off battery optimization."
  - Settings row: "Actualizaciones en segundo plano" / "Background updates"; subtitle "Sin restricciones" / "Unrestricted" or "Limitadas por ahorro de batería — toca para corregir" / "Limited by battery saving — tap to fix".
  - Buttons: Permitir/Allow · Ajustes del fabricante/Manufacturer settings · Ahora no/Not now · No volver a preguntar/Don't ask again.
  Non-blocking: the draft is used unless the maintainer changes it.

---

## Serena memories consulted

- `core` (entry point, via root `CLAUDE.md`)
- `architecture`
- `conventions`
