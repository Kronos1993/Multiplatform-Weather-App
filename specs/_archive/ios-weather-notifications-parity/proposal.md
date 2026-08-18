---
spec_id: ios-weather-notifications-parity
title: iOS weather notification parity with Android (UI dedup, background refresh, suggestions)
type: story
priority: normal
source: manual
source_ref: dictated by user in chat, 2026-08-17
created: 2026-08-17
status: INTAKE_PARSED
recommend_split: no
blockers: []
depends_on: []
expect_actual_touched: no
localization_touched: yes
branch_suggested: feature/ios-weather-notifications-parity
---

# Proposal: iOS weather notification parity with Android (UI dedup, background refresh, suggestions)

## 1. Source

Manual intake (see `story.md`): a dictated user story asking that iOS weather
notifications behave consistently and non-intrusively, matching Android's
behavior in three areas — the notification triggered by a manual refresh from
the main UI, background weather-fetch notifications, and suggestion
notifications — using iOS's own native mechanisms rather than a direct port
of Android's implementation.

## 2. Problem / Why

Investigation during planning (see "Affected areas" and "Serena memories
consulted") found concrete root causes for each of the story's three areas:

- **UI-triggered notification**: Android's `AppNotification.android.kt` calls
  `NotificationManagerCompat.notify(notificationsId.ordinal, notification)`,
  which always replaces the currently-shown notification for that stable
  numeric ID. iOS's `AppNotification.ios.kt` instead calls
  `UNUserNotificationCenter.addNotificationRequest` with a
  `UNTimeIntervalNotificationTrigger(1.0, repeats=false)` and a per-type
  string identifier. Per Apple's documented behavior, adding a request with
  an existing identifier only replaces a still-**pending** request — but a
  1-second trigger has almost always already fired and moved to
  **delivered** by the time a second manual refresh happens, so the
  identifier match no longer applies and the new request adds a second,
  separate visible notification instead of replacing the first. This is the
  literal mechanism behind "cada actualización puede generar una nueva
  notificación visible."
- **Background refresh**: `WeatherNotificationAppDelegate.swift` already
  registers a `BGAppRefreshTaskRequest` (`com.kronos.weatherapp.refresh_weather_notification`)
  at launch and on `applicationDidEnterBackground`, and
  `WeatherNotificationBackgroundTask.refreshWeather()` fetches weather and
  posts a notification on success. This path looks functionally wired
  end-to-end already; what the story reports as "no funciona correctamente"
  is most plausibly BGTaskScheduler's inherent unreliability (the OS decides
  if/when to actually run the task, and the iOS Simulator effectively never
  fires it organically) rather than missing code — this needs verification,
  not necessarily a rewrite.
- **Suggestion notifications**: the working tree already contains
  substantial *uncommitted* iOS work toward this exact requirement —
  `WeatherSuggestionScheduler.swift`, `SuggestionStringResolver.swift`
  (new), and a modified `WeatherNotificationAppDelegate.swift` /
  `Info.plist`. It is incomplete: `WeatherSuggestionBackgroundTask.kt`
  (iosMain, already committed) contains **two** competing
  implementations — an immediate-fire path (`runMorning`/`runMidday`/
  `runEvening`, mirrors Android's `WorkManager`-based one-shot approach) and
  a callback-based path (`fetchAndNotify()` + `onForecastReady`, meant to
  hand a fresh `Forecast` to the new Swift `WeatherSuggestionScheduler`,
  which schedules `UNCalendarNotificationTrigger`s at fixed local times).
  Neither is finished: `onForecastReady` is never assigned in
  `WeatherNotificationAppDelegate.swift`, and `initStrings(...)` (required
  by the immediate-fire path on iOS) is never called, so that path would
  currently emit blank notification text if it ever ran. This is the root
  of "las notificaciones asociadas a la obtención del clima en segundo plano
  todavía no funcionan correctamente en iOS" as it applies to suggestions.

## 3. Scope

The architectural gauntlet's split heuristic (`.specs/config.json`
`plan.split_thresholds`) technically fires here — this spec's risk count
(§7) reaches 5 and its acceptance criteria naturally group into 3
independently-shippable subsystems (UI dedup / background refresh /
suggestions). This was surfaced to the user during planning along with a
recommended 3-way split; the user explicitly chose to proceed as a single
spec instead (`recommend_split: no` is an intentional override, not an
oversight). Plan.md sequences the three areas as largely independent steps
so they can still be reviewed/verified incrementally within one PR.

**In scope**
- Fixing `AppNotification.ios.kt` so successive notifications of the same
  `NotificationType` replace rather than stack, for all three notification
  families it serves (`createNotification`, `createNotificationAlerts`,
  `createNotificationSuggestion`).
- Verifying (and hardening only if a real gap is found) the existing
  BGAppRefreshTask wiring for background weather refresh.
- Resolving which of the two existing (uncommitted/half-built) suggestion-
  notification code paths to finish, finishing it, and explicitly removing
  the other as dead code.
- A refactor pass strictly bounded to the files this spec already touches
  (`AppNotification.ios.kt`, `WeatherNotificationBackgroundTask.kt`,
  `WeatherSuggestionBackgroundTask.kt`, and whichever of
  `WeatherNotificationAppDelegate.swift`/`WeatherSuggestionScheduler.swift`/
  `SuggestionStringResolver.swift` remain after Step 1's decision) —
  removing dead commented-out code and de-duplicating the repeated
  "resolve city → read prefs → fetch forecast" block. Confirmed with the
  user; explicitly not a repo-wide or subsystem-wide refactor pass.
- Any new iOS-side user-facing string introduced while completing the
  suggestion path must be added to both `iosApp/en.strings` and
  `iosApp/es.strings` (and to `composeResources` if also Compose-UI-facing)
  — no English-only or Spanish-only string may land.
- Manual iOS/Android parity verification per the story's own "Validación
  manual" checklist.

**Out of scope**
- The `composeApp/build.gradle.kts` `versionCode`/`versionName` bump
  currently sitting uncommitted in the working tree — unrelated to
  notifications, left untouched by this spec.
- Any change to Android's notification behavior (already correct per the
  story) or to `WeatherAlertNotificationWorker`/alert-notification content.
- Adding new suggestion categories or changing suggestion copy/wording —
  only wiring/mechanism, not content, unless Step 1 investigation finds an
  actual localization gap.
- Push/remote notifications (`FROM_FIREBASE` notification type) — not
  mentioned in the story.

## 4. Affected areas

| Area | Source set(s) | Class(es) / file(s) touched | Change type | Resolved by | Notes |
|--------|--------------|-------------------------------|-------------|-------------|-------|
| core/notification | iosMain | `AppNotification.ios.kt` (`createNotification`, `createNotificationAlerts`, `createNotificationSuggestion`) | modify | | Remove delivered+pending requests for the same `NotificationType` identifier before adding a new one — native iOS equivalent of Android's `NotificationManagerCompat.notify(id, ...)` replace semantics. |
| core/job (background refresh) | iosMain | `WeatherNotificationBackgroundTask.kt` | verify only | Step 3 | No code change expected; confirm `refreshWeather()` benefits from the Area-1 fix. |
| iosApp (background scheduling host) | iosApp Swift | `WeatherNotificationAppDelegate.swift` (BGTaskScheduler registration + `scheduleWeatherRefresh()`) | verify only | Step 3 | Already implemented end-to-end; add code only if verification finds a real gap. |
| core/job (suggestions) | iosMain | `WeatherSuggestionBackgroundTask.kt` (deleted entirely — see decisions.md), `WeatherNotificationBackgroundTask.kt` (add `onForecastReady` callback + `MeasureUnit` read) | modify/delete | Step 1 (resolved: swift path, reuse hourly task) | See OQ-1/OQ-2 resolution above; file deletion decided during Step 4 once the class had zero remaining callers. |
| iosApp (suggestions) | iosApp Swift | `WeatherSuggestionScheduler.swift`, `SuggestionStringResolver.swift` (kept as-is), `WeatherNotificationAppDelegate.swift` (`onForecastReady` wiring on the existing hourly task, no new `BGTaskScheduler.register` calls), `Info.plist` (remove the 3 unused `suggestion_*` `BGTaskSchedulerPermittedIdentifiers` entries) | modify | Step 1 (resolved: swift path, reuse hourly task) | See OQ-1/OQ-2 resolution above. |
| localization | commonMain composeResources + iosApp `*.strings` | `strings.xml`, `strings_suggestions.xml`, `en.strings`, `es.strings` | modify | Step 1 (initial check) → follow-up user audit (real fixes) | Initial Step 1 check found all suggestion keys present but did not catch that their *values* used the wrong placeholder syntax (`%@` vs Android's `%1$s`) — a follow-up user-requested audit found this broke every suggestion message's argument substitution, and that `notification_title`/`notification_short_details`/`notification_long_details` (+ `_fahrenheit` variants) were entirely missing, with the WEATHER_UPDATED notification instead using hardcoded non-localized Kotlin literals. Both fixed — see decisions.md. |
| core/job (WEATHER_UPDATED background notification) | iosMain | `WeatherNotificationBackgroundTask.kt` | modify | follow-up audit | `initNotificationStrings` now takes 6 localized params (metric+Fahrenheit) instead of hardcoding English literals; `createWeatherNotification` now branches on `MeasureUnit` like Android's `WeatherNotificationWorker` does; dead `currentWeatherKey` removed; `.region.toString()` → `.orEmpty()` null-safety fix. |
| iosApp (MainViewController) | iosApp Swift host / iosMain | `MainViewController.kt` (not `iosApp/` Swift — the KMP `ComposeUIViewController` entry point) | modify | follow-up audit | Removed a vestigial call to the old zero-arg `initNotificationStrings()` on a throwaway, otherwise-unused instance — broken by the signature change above, no behavior to preserve. |

## 5. Architectural gauntlet (this repo's hard rules)

### 5a. Always explicit (no shortcut)

- [x] **Expect/actual parity** — N/A. The `expect class AppNotification : INotifications`
      signature in `core/notification/AppNotification.kt` is unchanged; only
      the bodies of the existing iOS `actual` implementation change.
      Approach: confirmed — no new/changed `expect` declaration in this spec.
- [x] **Dual localization** — N/A for now. No new user-facing string content
      is planned; Step 1 re-confirms `en.strings`/`es.strings` already cover
      every key `SuggestionStringResolver.swift` references (verified: 76/76
      present as of planning) and checks parity against
      `strings_suggestions.xml`. If Step 1 finds a real gap, it becomes an
      explicit follow-up in the affected conditional step.
      Approach: confirmed — re-verified in Step 1.
- [x] **Secrets & logging** — confirmed. No plan to log/print the WeatherAPI
      key or any credential. Existing `println`/`print` statements in
      `AppNotification.ios.kt` and the background-task classes log
      notification-scheduling status and error messages only; no new
      credential logging is introduced.
      Approach: confirmed.
- [x] **No automated tests exist** — acknowledged. Verification throughout
      plan.md is build-green plus manual runs in the iOS Simulator/device
      and Android emulator/device — never a test-suite run.
      Acknowledged: yes.

### 5b. Confinement-conditional

- [ ] **Confinement claim** — not applicable. This spec touches `core/notification`
      and `core/job` (iosMain) plus native `iosApp/` Swift host code — a
      cross-cutting `core` change, not a single feature's UI/ViewModel layer.
      Neither rule below auto-resolves to N/A; both are answered explicitly.

- [x] **Domain/data boundary (DIP)** — no new domain interface or repository
      is introduced. `INotifications`, `WeatherRemoteRepository`, and
      `UserCustomLocationLocalRepository` are pre-existing interfaces already
      correctly consumed via Koin injection in `WeatherNotificationBackgroundTask`
      and `WeatherSuggestionBackgroundTask`; this spec does not add a
      ViewModel-to-`*Impl` shortcut anywhere.
      Approach: confirmed — no new repository/interface work needed.
- [x] **Result-type error handling** — `WeatherRemoteRepository.getWeatherDataForecast(...)`
      already returns the sealed `Result` type and is consumed via
      `onSuccess`/`onError` in both background-task classes today. This spec
      does not add any new failable repository call; any suggestion-path
      code kept/finished in Step 4 or Step 5 must continue using
      `onSuccess`/`onError` rather than throwing.
      Approach: confirmed.

## 6. Skills

**Skills**: direct edits

## 7. Risks

- BGTaskScheduler execution timing is entirely OS-controlled and not
  guaranteed on a fixed schedule — background refresh and any BGAppRefreshTask-
  based suggestion pre-fetch can only be meaningfully exercised via Xcode's
  LLDB "simulate launch" debug command or on a real device over multiple day
  cycles, not via `./gradlew build` or a quick manual tap-through.
- The Step 2 fix to `AppNotification.ios.kt` is shared code for
  `createNotification`, `createNotificationAlerts`, and
  `createNotificationSuggestion` — a mistake in the remove-then-add
  identifier logic could suppress or duplicate notifications across every
  family (`WEATHER_UPDATED`, `WEATHER_ALERT`, all `WEATHER_SUGGESTION_*`) at
  once, not just the one being fixed.
- The pre-existing uncommitted suggestion-notification WIP is half-wired by
  design (two competing paths, one orphaned Swift scheduler). Building on it
  without Step 1 explicitly resolving which path wins risks papering over
  the incompleteness (e.g., leaving `BGTaskSchedulerPermittedIdentifiers`
  entries for suggestion tasks that are never registered) instead of
  actually fixing it.
- More frequent background weather fetches (existing hourly refresh, plus
  whatever Step 1 decides for suggestion pre-fetching) increase call volume
  against the configured WeatherAPI key's quota; no rate-limit handling
  exists today beyond the standard `Result` error path.
- No automated tests exist for any of this — regressions in
  time-scheduled/background-triggered notification behavior are only
  caught by manual verification, which is inherently harder to repeat
  reliably than UI-triggered behavior.

## Out-of-band actions

- Real-device verification is required for background refresh and
  suggestion-notification timing/reliability — the iOS Simulator does not
  reliably fire `BGAppRefreshTask`s organically. Xcode's LLDB command
  `e -l objc -- (void)[[BGTaskScheduler sharedScheduler] _simulateLaunchForTaskWithIdentifier:@"<identifier>"]`
  is the practical dev-time approximation (after backgrounding the app
  once), but final confidence needs a real device left running across
  multiple day/refresh cycles.
- Confirm the WeatherAPI key's quota comfortably covers the final refresh
  frequency once Step 1's suggestion-path design is settled.

## 8. Acceptance criteria

- [x] **AC-1** — Refreshing the weather multiple times consecutively from
      the main UI on iOS never produces more than one visible
      `WEATHER_UPDATED` notification at a time; each refresh replaces the
      previous one in the Notification Center, matching Android's behavior.
      Additionally, refreshing while the app is open/foregrounded does not
      pop a banner over the screen (`WeatherNotificationAppDelegate`'s
      `willPresent` now suppresses `.banner` specifically for
      `WEATHER_UPDATED`, keeping `.list`/`.badge` — matches Android not
      showing a heads-up alert for this notification while the app is
      active). Other notification types (alerts, suggestions) are
      unaffected — see decisions.md.
- [x] **AC-2** — The visible `WEATHER_UPDATED` notification always shows the
      most recently fetched weather data.
- [x] **AC-3** — With the app backgrounded/terminated, a triggered
      `BGAppRefreshTask` run (via the LLDB simulate-launch command or real
      elapsed time on a device) fetches weather and posts exactly one
      `WEATHER_UPDATED` notification with fresh data.
- [x] **AC-4** — Suggestion notifications (morning/midday/evening) are
      generated via the single implementation path chosen in plan.md Step 1,
      each with correctly localized (en/es) title and body, without
      duplicating and without interfering with `WEATHER_UPDATED` or
      `WEATHER_ALERT` notifications.
- [x] **AC-5** — Running the story's own "Validación manual" checklist on
      both an iOS simulator/device and an Android emulator/device shows
      functionally equivalent behavior across all three notification
      families; any remaining iOS/Android difference is attributable only
      to a stated platform constraint (documented in plan.md Step 6).
- [x] **AC-6** — `./gradlew build` succeeds (commonMain/androidMain/iosMain/jvmMain
      compile), and the `iosApp` Xcode project builds successfully for the
      touched Swift files.
- [x] **AC-7** — No hardcoded/non-localized notification text remains in
      iOS, and every notification message (WEATHER_UPDATED title/short/long
      details, all suggestion messages) is textually identical to Android's
      source string, in both English and Spanish. Verified via a
      programmatic key-by-key diff against
      `composeResources/values[-es]/strings.xml` and
      `strings_suggestions.xml` — see decisions.md for the two real bugs
      this caught and fixed.

## 9. Open questions

- **OQ-1 — RESOLVED (plan.md Step 1)** — `suggestion_path = swift`. Keep and
  finish `fetchAndNotify()`/`onForecastReady` → `WeatherSuggestionScheduler.swift`
  → `UNCalendarNotificationTrigger`; remove the dead `runMorning`/`runMidday`/
  `runEvening` + `initStrings`/`SuggestionStrings` path from
  `WeatherSuggestionBackgroundTask.kt`. Rationale: `UNCalendarNotificationTrigger`
  delivers at its scheduled wall-clock time regardless of whether any
  background task actually runs — it does not depend on `BGTaskScheduler`
  firing, which is the most reliable native mechanism available and matches
  the story's "use iOS's own mechanisms" requirement better than mirroring
  Android's `WorkManager` one-shot approach. Also: `initStrings(...)` is
  never called on iOS today, so the Kotlin-driven path would currently emit
  blank notification text if invoked — the Swift path is materially closer
  to working. All 76 string keys `SuggestionStringResolver.swift` needs
  already exist in both `iosApp/en.strings` and `iosApp/es.strings`
  (confirmed by direct comparison against the resolver's `loc(...)` calls),
  and their names line up 1:1 with `strings_suggestions.xml`'s Compose keys
  — no localization gap found.
- **OQ-2 — RESOLVED (plan.md Step 1)** — `suggestion_bg_identifiers = remove`.
  `WeatherViewModel.createWeatherNotification` (UI-triggered) and
  `WeatherNotificationBackgroundTask.createWeatherNotification`
  (background-triggered) both already call the same
  `notifications.createNotification(..., NotificationType.WEATHER_UPDATED)`
  through the single shared `AppNotification.ios.kt` actual — confirming
  Step 2's fix there covers both triggers with no extra files. Given that,
  registering 3 *additional* `BGAppRefreshTask` identifiers
  (`suggestion_morning`/`suggestion_midday`/`suggestion_evening`) alongside
  the existing hourly `refresh_weather_notification` task would fragment
  iOS's single, OS-throttled background-execution budget across 4
  competing tasks for no real benefit — `UNCalendarNotificationTrigger`
  doesn't need its own dedicated background task to fire. Instead, Step 4
  reuses the already-reliable hourly task: after
  `WeatherNotificationBackgroundTask.refreshWeather()` succeeds, it also
  invokes `WeatherSuggestionScheduler.scheduleAll(forecast:measureUnit:)`
  with the same freshly-fetched forecast (no extra WeatherAPI calls), and
  the 3 unused entries are removed from `Info.plist`'s
  `BGTaskSchedulerPermittedIdentifiers`.
- **OQ-3** — Is a real iOS device available for the manual verification in
  plan.md Step 6? Background refresh and suggestion timing cannot be
  reliably exercised in the Simulator. Non-blocking — if no device is
  available, Step 6 must say so explicitly rather than claim a check that
  wasn't actually performed.

---

## Serena memories consulted

- `core` — source map confirming `core/notification`, `core/job`, and the
  `iosApp/` native host locations (and that `WeatherSuggestionScheduler.swift`
  / `SuggestionStringResolver.swift` are already documented as part of the
  intended architecture, even though uncommitted).
- `architecture` — Clean Architecture layering, `Result` pattern, and
  `expect`/`actual` file-suffix convention used to assess the §5 gauntlet.
- `conventions` — dual-localization requirement (two string locations kept
  in sync manually) and the "no automated tests" invariant used throughout
  §5a and §8.
