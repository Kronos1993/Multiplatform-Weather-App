---
spec_id: ios-weather-notifications-parity
generated_by: /spec-plan
generated_at: 2026-08-17T00:00:00Z
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

# Plan: iOS weather notification parity with Android (UI dedup, background refresh, suggestions)

## Strategy

Fix the well-understood, self-contained root cause first (the notification
replace/dedup bug in `AppNotification.ios.kt`, Step 2 — affects both the
UI-triggered and background-triggered `WEATHER_UPDATED` notification, plus
`WEATHER_ALERT` and every `WEATHER_SUGGESTION_*` type). Verify the
already-implemented background-refresh wiring rather than rewriting it
(Step 3). Resolve the two competing suggestion-notification implementations
via a dedicated investigate step (Step 1) before touching any suggestion
code, then execute exactly one of two conditional completion steps (Step 4
or Step 5) based on that decision. Once the touched files reach their final
shape, do a bounded refactor pass strictly confined to those same files
(Step 6 — dead-code removal and de-duplication only, no wider audit; scope
explicitly confirmed with the user). Close with a full manual parity pass
against the story's own validation checklist (Step 7).

**Localization guardrail (applies to every step below that can add a
user-facing string, especially Step 4/Step 5)**: any new iOS string
introduced during implementation — not already present in
`iosApp/en.strings`/`iosApp/es.strings` — must be added to **both** files
before that step is considered done, and mirrored into
`composeApp/src/commonMain/composeResources/values/strings_suggestions.xml` +
`values-es/strings_suggestions.xml` if the same copy is also shown in the
Compose UI. No step may leave an English-only or Spanish-only string.

## Steps

### Step 1 — Decide the suggestion-notification implementation path [investigate]

- **Files / symbols**:
  - `composeApp/src/iosMain/kotlin/com/kronos/multiplatform/weatherapp/core/job/WeatherSuggestionBackgroundTask.kt` — compare `runMorning`/`runMidday`/`runEvening` (+ `initStrings`/`SuggestionStrings`) against `fetchAndNotify()`/`onForecastReady`
  - `iosApp/iosApp/WeatherNotificationAppDelegate.swift` — confirm `onForecastReady` is not currently wired, and whether any `BGTaskScheduler.shared.register` calls exist for `suggestion_morning`/`suggestion_midday`/`suggestion_evening` (expected: none yet)
  - `iosApp/iosApp/WeatherSuggestionScheduler.swift`, `iosApp/iosApp/SuggestionStringResolver.swift` — confirm completeness of the Swift-side scheduling/string-resolution code
  - `iosApp/en.strings`, `iosApp/es.strings` — confirm all keys `SuggestionStringResolver.swift` references exist (spot-checked during planning: 76/76 present)
  - `composeApp/src/commonMain/composeResources/values/strings_suggestions.xml`, `values-es/strings_suggestions.xml` — check for any content parity gap against the notification copy
- **Question(s) to answer**:
  - proposal.md OQ-1 — which path (Swift `UNCalendarNotificationTrigger` vs Kotlin immediate-fire) is completed, which is deleted
  - proposal.md OQ-2 — do the `suggestion_morning`/`suggestion_midday`/`suggestion_evening` `BGTaskSchedulerPermittedIdentifiers` entries in `Info.plist` get real `BGAppRefreshTask` registrations, or get removed in favor of reusing the existing hourly refresh task's cached forecast
- **Outputs to record**:
  - `Step 1 result: suggestion_path = swift` or `Step 1 result: suggestion_path = kotlin` — gates Step 4 / Step 5
  - `Step 1 result: suggestion_bg_identifiers = keep` or `= remove` — informs whichever of Step 4/5 executes
  - Update proposal.md §4 "Resolved by" cells for the `core/job (suggestions)`, `iosApp (suggestions)`, and `localization` rows with the concrete outcome
- **Why**: two incompatible, half-built implementations exist for the same requirement today; picking one and explicitly removing the other must happen before any suggestion-notification code change, per the story's "use iOS's own mechanisms, don't just port Android's" requirement.

### Step 2 — Fix notification replace/dedup in AppNotification.ios.kt [implement]

- **Skill**: direct edits
- **Area(s)**: `core/notification` (iosMain)
- **Files / symbols**:
  - `composeApp/src/iosMain/kotlin/com/kronos/multiplatform/weatherapp/core/notification/AppNotification.ios.kt` — `createNotification`, `createNotificationAlerts`, `createNotificationSuggestion`
- **Skill args / inputs**: none
- **Why**: root cause of "cada actualización genera una nueva notificación visible" — `UNTimeIntervalNotificationTrigger(1.0)` fires and moves the request from pending to delivered before a same-identifier replacement is possible on a second call, so delivered notifications stack instead of being replaced. Fix: before calling `addNotificationRequest`, call `UNUserNotificationCenter.currentNotificationCenter().removeDeliveredNotificationsWithIdentifiers(listOf(notificationsId.name))` and `removePendingNotificationRequestsWithIdentifiers(listOf(notificationsId.name))`, so at most one visible/pending notification exists per `NotificationType` at a time — the native iOS equivalent of Android's `NotificationManagerCompat.notify(id, ...)` replace behavior, not a port of Android's implementation.
- **Verification**: `./gradlew build` green; manually run in Xcode/iOS Simulator, trigger a manual weather refresh from the UI 3+ times in a row, and confirm only one `WEATHER_UPDATED` notification remains visible in Notification Center at any time, always showing the latest content (AC-1, AC-2).

### Step 3 — Verify background weather refresh wiring [verify]

- **What to check**: `iosApp/iosApp/WeatherNotificationAppDelegate.swift`'s `BGTaskScheduler.shared.register(forTaskWithIdentifier: "com.kronos.weatherapp.refresh_weather_notification", ...)` registration, `scheduleWeatherRefresh()` calls in `didFinishLaunchingWithOptions` and `applicationDidEnterBackground`, and `WeatherNotificationBackgroundTask.refreshWeather()` / `doInitNotificationStrings()` wiring; confirm `Info.plist`'s `BGTaskSchedulerPermittedIdentifiers` includes this identifier (already present). Force a run without waiting for the OS's real schedule via Xcode's LLDB command: `e -l objc -- (void)[[BGTaskScheduler sharedScheduler] _simulateLaunchForTaskWithIdentifier:@"com.kronos.weatherapp.refresh_weather_notification"]` (after backgrounding the app once).
- **Pass criteria**: the simulated background refresh produces exactly one `WEATHER_UPDATED` notification (benefiting from Step 2's fix) with fresh forecast data, and `scheduleWeatherRefresh()` re-arms a new request afterward (visible via the "⏰ BGTask clima programada en 60 min" log line). If any gap is found, add the minimal fix here and note it as a deviation from "verify only" in tasks.md.

### Step 4 — Complete the Swift/UNCalendarNotificationTrigger suggestion path [implement, conditional]

- **Condition**: Step 1 result: suggestion_path = swift (resolved — see proposal.md OQ-1)
- **Skill**: direct edits
- **Area(s)**: `core/job` (iosMain), `iosApp` (Swift)
- **Design (resolved by Step 1, OQ-1/OQ-2)**: reuse the existing hourly
  `refresh_weather_notification` `BGAppRefreshTask` instead of registering
  3 new competing ones — `UNCalendarNotificationTrigger` delivers at its
  scheduled wall-clock time on its own; it only needs *fresh content*
  periodically, which the hourly task already provides.
- **Files / symbols**:
  - `composeApp/src/iosMain/kotlin/com/kronos/multiplatform/weatherapp/core/job/WeatherNotificationBackgroundTask.kt` — add `var onForecastReady: ((Forecast, MeasureUnit) -> Unit)? = null`; read `measure_unit_key` from `preferenceRepository` (same pattern `WeatherSuggestionBackgroundTask.fetchAndNotify()` already uses); invoke `onForecastReady?.invoke(forecast, measureUnit)` in `refreshWeather()`'s `onSuccess` branch, alongside the existing `createWeatherNotification(it)` call
  - `iosApp/iosApp/WeatherNotificationAppDelegate.swift` — in `handleWeatherRefresh(task:)`, before `worker.refreshWeather()`, set `worker.onForecastReady = { [weak self] forecast, unit in self?.suggestionScheduler.scheduleAll(forecast: forecast, measureUnit: unit) }`; no new `BGTaskScheduler.register`/`submit` calls
  - `iosApp/iosApp/Info.plist` — remove the `suggestion_morning`/`suggestion_midday`/`suggestion_evening` entries from `BGTaskSchedulerPermittedIdentifiers` (keep `refresh_weather_notification`)
  - `composeApp/src/iosMain/kotlin/com/kronos/multiplatform/weatherapp/core/job/WeatherSuggestionBackgroundTask.kt` — **deleted entirely** (see decisions.md): its one remaining live responsibility (fetch forecast, expose via callback) was folded into `WeatherNotificationBackgroundTask.onForecastReady` above so the hourly task's forecast fetch is reused instead of duplicated; once `fetchAndNotify()` had no callers left (confirmed via repo-wide grep — `WeatherSuggestionBackgroundTask` was never actually constructed anywhere, including by `WeatherNotificationAppDelegate.swift`), the whole file became dead code
- **Skill args / inputs**: none
- **Why**: finishes the already-started native-iOS scheduling mechanism (fixed local-time delivery independent of whether a background task actually runs) instead of leaving it half-wired; removes now-confirmed-dead code; avoids fragmenting iOS's single shared background-execution budget across 4 competing periodic tasks.
- **Verification**: `./gradlew build` green; manually trigger the hourly `refresh_weather_notification` task via the LLDB simulate-launch command (`e -l objc -- (void)[[BGTaskScheduler sharedScheduler] _simulateLaunchForTaskWithIdentifier:@"com.kronos.weatherapp.refresh_weather_notification"]`), and confirm the 3 suggestion notifications get (re)scheduled with fresh content — inspect via `UNUserNotificationCenter.current().getPendingNotificationRequests` in a debug breakpoint/log, or wait for one to fire at its scheduled time — with correct localized (en/es) copy (AC-4).

### Step 5 — Complete the Kotlin-driven immediate-fire suggestion path [implement, conditional]

- **Condition**: Step 1 result: suggestion_path = kotlin
- **Skill**: direct edits
- **Area(s)**: `core/job` (iosMain), `iosApp` (Swift)
- **Files / symbols**:
  - `composeApp/src/iosMain/kotlin/com/kronos/multiplatform/weatherapp/core/job/WeatherSuggestionBackgroundTask.kt` — wire `initStrings(...)` with iOS localized string values so `runMorning`/`runMidday`/`runEvening` no longer emit blank text on iOS; add scheduling analogous to Android's `WeatherSuggestionScheduler` delay-based pattern
  - `iosApp/iosApp/WeatherNotificationAppDelegate.swift`, `iosApp/iosApp/WeatherSuggestionScheduler.swift`, `iosApp/iosApp/SuggestionStringResolver.swift` — remove the now-unused Swift scheduler/resolver and its `onForecastReady`-related wiring (explicitly stated as removed code, not silent — called out here and in tasks.md)
- **Skill args / inputs**: none
- **Why**: keeps suggestion-notification content generation on the Kotlin side, mirroring Android's structure, if Step 1 determines that's the preferred direction.
- **Verification**: `./gradlew build` green; manually background the app, force each suggestion task, and confirm exactly one localized notification appears per slot (AC-4).

### Step 6 — Refactor cleanup within touched files [implement]

- **Skill**: direct edits
- **Area(s)**: `core/job` (iosMain)
- **Files / symbols**:
  - `composeApp/src/iosMain/kotlin/com/kronos/multiplatform/weatherapp/core/job/WeatherNotificationBackgroundTask.kt` — remove the ~30 lines of commented-out `BGAppRefreshTaskRequest`/`handleAppRefresh` code (now superseded by the Swift-side `BGTaskScheduler` wiring in `WeatherNotificationAppDelegate.swift`, confirmed live in Step 3)
- **Note**: the originally-planned second half of this step — extracting a shared "resolve city → read prefs → fetch forecast" helper between `WeatherNotificationBackgroundTask.kt` and `WeatherSuggestionBackgroundTask.kt` — is superseded by Step 4's outcome: `WeatherSuggestionBackgroundTask.kt` was deleted entirely (its logic folded into `WeatherNotificationBackgroundTask.onForecastReady`), so the duplication this was meant to resolve no longer exists. See decisions.md.
- **Skill args / inputs**: none
- **Why**: bounded refactor scope explicitly confirmed with the user — strictly limited to files this spec already touches (no wider audit of `job/`, widgets, or Android code). Removes dead code this spec's own investigation surfaced.
- **Verification**: `./gradlew build` green; behavior unchanged — pure removal of already-dead commented-out code, no runtime path affected.

### Step 7 — Manual iOS/Android parity pass [verify]

- **What to check**: repeat the story's own "Validación manual" checklist on
  both an iOS simulator/device and an Android emulator/device — single
  manual refresh, multiple consecutive manual refreshes, closing the app and
  checking background behavior, suggestion notification timing/content, and
  confirming no notification type duplicates or interferes with another
  (`WEATHER_UPDATED`, `WEATHER_ALERT`, `WEATHER_SUGGESTION_*`).
- **Pass criteria**: every item in proposal.md §8 Acceptance criteria is
  observed true on both platforms; any remaining iOS/Android behavioral
  difference is attributable only to a stated platform constraint (recorded
  inline in this step's notes when found). If OQ-3 resolved to "no real
  device available," this step must say so explicitly for the
  background/suggestion-dependent criteria rather than claim a check that
  wasn't actually performed.

### Step 8 — Follow-up: string audit + banner/re-arm fixes (user-requested, post-Step-7) [implement]

- **Skill**: direct edits
- **Area(s)**: `core/job` (iosMain), `iosApp` (Swift), localization
- **Files / symbols**:
  - `composeApp/src/iosMain/kotlin/com/kronos/multiplatform/weatherapp/core/job/WeatherNotificationBackgroundTask.kt` — `initNotificationStrings`/`createWeatherNotification` sourced from real localized strings + `MeasureUnit` branching (was hardcoded English)
  - `composeApp/src/iosMain/kotlin/com/kronos/multiplatform/weatherapp/MainViewController.kt` — removed vestigial call broken by the signature change above
  - `iosApp/en.strings`, `iosApp/es.strings` — added missing `notification_*` keys; resynced all `suggestion_*` values to Android's exact text/positional syntax (`%1$@`/`%2$@` + proper `%%` escaping)
  - `iosApp/iosApp.xcodeproj/project.pbxproj` — registers `en.strings`/`es.strings` as bundled resources (pre-existing WIP from before this spec started; required for the strings above to actually ship in the app, carried forward as part of this spec's file set)
  - `iosApp/iosApp/SuggestionStringResolver.swift` — `applyArgs` rewritten to use native `String(format:arguments:)` instead of a hand-rolled `%1$s` replace
  - `iosApp/iosApp/WeatherNotificationAppDelegate.swift` — `willPresent` suppresses `.banner` for `WEATHER_UPDATED` while foregrounded; `performWeatherRefresh` extracted and also called from `didFinishLaunchingWithOptions` so suggestions re-arm on every app launch, not just the opportunistic hourly background task
- **Skill args / inputs**: none
- **Why**: the user asked to verify no hardcoded/non-Android-matching iOS strings (found two real bugs — see decisions.md), then flagged the foreground banner still being intrusive and asked whether suggestions fire on schedule (they didn't, reliably) — all three are direct continuations of this spec's own story (UI-notification intrusiveness, suggestion reliability), not new scope.
- **Verification**: `./gradlew clean` + `:composeApp:assembleDebug` green; full clean `xcodebuild -scheme iosApp -destination 'platform=iOS Simulator,...' build` green; a standalone `swift` script executing `String(format:arguments:)` against the real templates; user-confirmed on a physical iOS device.

## Dependencies

- Step 4 and Step 5 both depend on Step 1's `suggestion_path` result —
  exactly one of them executes, the other is skipped.
- Step 2 and Step 3 are independent of Step 1 and of each other; either
  order is fine.
- Step 6 depends on Step 3 and on whichever of Step 4/Step 5 executed —
  it refactors the final shape of `WeatherNotificationBackgroundTask.kt`
  and `WeatherSuggestionBackgroundTask.kt`, so it must run after both are
  settled.
- Step 7 depends on all prior (non-skipped) steps being complete.

## Out-of-band actions

- Real-device verification is required for background refresh and
  suggestion-notification timing/reliability — see proposal.md
  "Out-of-band actions" for the LLDB simulate-launch command and its limits.
- Confirm WeatherAPI key quota comfortably covers the final refresh
  frequency once Step 1's design is settled.

## Rollback

`git restore` the specific files listed in each step's "Files / symbols" if
that step needs to be undone. Step 2's change is small and self-contained —
reverting it alone is safe. If Step 4 or Step 5 is abandoned partway,
restoring `iosApp/iosApp/` and
`composeApp/src/iosMain/kotlin/com/kronos/multiplatform/weatherapp/core/job/WeatherSuggestionBackgroundTask.kt`
returns to the **pre-spec uncommitted WIP state**, not to a clean baseline —
that WIP predates this spec and was never itself committed, so a partial
rollback should be checked against `git status`/`git diff` before assuming
a clean slate.
