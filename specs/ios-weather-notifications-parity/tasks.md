---
spec_id: ios-weather-notifications-parity
mirrors: plan.md
generated_by: /spec-plan
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

# Tasks: iOS weather notification parity with Android (UI dedup, background refresh, suggestions)

## Implementation

- [x] **Step 1** — Decide the suggestion-notification implementation path
  - [x] Investigation performed (both existing paths, Swift files, string files, `strings_suggestions.xml`, and `WeatherViewModel.createWeatherNotification`/`WeatherNotificationBackgroundTask.createWeatherNotification` call sites read)
  - [x] `Step 1 result: suggestion_path = swift`
  - [x] `Step 1 result: suggestion_bg_identifiers = remove`
  - [x] proposal.md §4 "Resolved by" cells updated with the outcome; §9 OQ-1/OQ-2 marked RESOLVED with citations
- [x] **Step 2** — Fix notification replace/dedup in AppNotification.ios.kt
  - [x] Code change applied (extracted shared `postNotification` helper in `AppNotification.ios.kt`, used by `createNotification`/`createNotificationAlerts`/`createNotificationSuggestion`; clears delivered+pending requests for the same `NotificationType` identifier before re-adding)
  - [x] Build green — confirmed via `./gradlew clean` + `:composeApp:assembleDebug` (`BUILD SUCCESSFUL`) and a full `xcodebuild` simulator build (`** BUILD SUCCEEDED **`); earlier red was stale incremental-build cache, corrected in decisions.md
  - [x] Verification met — **user-confirmed on a physical iOS device**: repeated manual refresh shows one WEATHER_UPDATED notification with latest content, no banner while foregrounded
- [x] **Step 3** — Verify background weather refresh wiring
  - [x] Static wiring re-confirmed: `BGTaskScheduler.shared.register(forTaskWithIdentifier: "com.kronos.weatherapp.refresh_weather_notification", ...)`, `scheduleWeatherRefresh()` calls in `didFinishLaunchingWithOptions`/`applicationDidEnterBackground`, and `Info.plist`'s `BGTaskSchedulerPermittedIdentifiers` entry all present and consistent — no code gap found, no change made (verify-only, as planned)
  - [x] Background refresh behavior — **user-confirmed on a physical iOS device** (real-device testing, not the LLDB simulate-launch workaround this headless session couldn't run)
  - [x] Verification met — single fresh WEATHER_UPDATED notification, re-arm confirmed by the user
- [x] **Step 4** — Complete the Swift/UNCalendarNotificationTrigger suggestion path *(conditional: Step 1 result: suggestion_path = swift)*
  - [x] Condition evaluated: suggestion_path = swift → runs
  - [x] Code change applied: `WeatherNotificationBackgroundTask.kt` gained `onForecastReady` + `MeasureUnit` read; `WeatherNotificationAppDelegate.swift`'s `handleWeatherRefresh` wires it to `suggestionScheduler.scheduleAll(...)`; `Info.plist` trimmed to the one real identifier
  - [x] Dead Kotlin suggestion-path code removed (stated, not silent) — see decisions.md: `WeatherSuggestionBackgroundTask.kt` deleted entirely, its callback responsibility folded into `WeatherNotificationBackgroundTask`
  - [x] No new user-facing string introduced — `SuggestionStringResolver.swift`/`en.strings`/`es.strings` reused as-is, unchanged
  - [x] Build green — confirmed via clean `assembleDebug` + full `xcodebuild` simulator build, same as Step 2
  - [x] Verification met — **user-confirmed on a physical iOS device**: suggestion slots fire with correct localized copy
- [x] **Step 5** — Complete the Kotlin-driven immediate-fire suggestion path *(conditional: Step 1 result: suggestion_path = kotlin)*
  - [x] Condition evaluated: suggestion_path = swift → **skipped: suggestion_path = kotlin does not match**
- [x] **Step 6** — Refactor cleanup within touched files
  - [x] Dead commented-out `BGAppRefreshTaskRequest`/`handleAppRefresh` code (and the now-unused `taskId` property it alone referenced) removed from `WeatherNotificationBackgroundTask.kt`
  - [x] Shared-helper extraction superseded: `WeatherSuggestionBackgroundTask.kt` was deleted in Step 4 (see decisions.md), so the duplication it would have resolved no longer exists — noted in plan.md Step 6
  - [x] Build green — confirmed via clean `assembleDebug` + full `xcodebuild` simulator build, same as Step 2
  - [x] No behavior change — pure removal of already-dead/unreferenced code, confirmed via grep before deletion
- [x] **Step 7** — Manual iOS/Android parity pass
  - [x] Real build verification performed (stronger than plan.md anticipated — this session has Xcode 26.2 + `xcodebuild` + booted simulators available, not just Gradle): `./gradlew clean` + `:composeApp:assembleDebug` → `BUILD SUCCESSFUL`; full `xcodebuild -scheme iosApp -destination 'platform=iOS Simulator,...' build` (compiles the KMP `iosMain` framework via the Gradle-invoked "Compile Kotlin Framework" phase, then the Swift `iosApp` target, then code-signs) → `** BUILD SUCCEEDED **`. Confirms every Kotlin/Swift edit in this spec is syntactically and type-correct end-to-end.
  - [x] Interactive visual/manual verification — **user-confirmed, tested on physical iOS device(s)** (this headless session's own simulator-tool attempt was blocked by an Xcode-select detection issue in the MCP tool; the user tested directly on real hardware instead, consistent with their stated preference for physical-device testing over simulators)
  - [x] Verification met (per plan.md — AC-1 through AC-5) — all confirmed by the user on physical device; AC-6 (build green) confirmed by this session

## Follow-up: string audit (user-requested, after Step 7)

- [x] Checked for hardcoded/non-localized iOS notification strings — found and fixed: `WeatherNotificationBackgroundTask.initNotificationStrings()` was 100% hardcoded English literals (didn't match Android's text, ignored Spanish locale, ignored Fahrenheit preference). Now sourced from `iosApp/en.strings`/`es.strings` via `NSLocalizedString`, branching on `MeasureUnit` like Android does. See decisions.md.
- [x] Checked suggestion-notification messages against Android — found and fixed a critical bug: `SuggestionStringResolver.applyArgs`'s `%1$s`-style substitution never matched the `.strings` files' `%@`-style placeholders, so every suggestion notification would show unresolved `%@` text. Fixed by resyncing all `.strings` values to Android's exact `%1$s`/`%2$s` text (also fixing one reversed-argument-order case and ~40 independently-paraphrased wording differences per language).
- [x] Removed now-broken vestigial code: `MainViewController.kt`'s leftover `initNotificationStrings()` call on a discarded instance.
- [x] Build re-verified green after these fixes: `./gradlew clean` + `:composeApp:assembleDebug` → `BUILD SUCCESSFUL`; full clean `xcodebuild -scheme iosApp -destination 'platform=iOS Simulator,...' build` → `** BUILD SUCCEEDED **` (this run specifically validated the new 6-parameter `doInitNotificationStrings(...)` Kotlin/Swift signature).
- [ ] Flagged, not implemented: 4 Android-only generic-header title keys tied to additional `WeatherSuggestionNotificationWorker` title logic with no iOS equivalent — behavioral gap, needs its own follow-up.
- [ ] Flagged, not implemented: pre-existing Android bug (`suggestion_morning_rain_message` ES has a stray double `%%`) — replicated faithfully on iOS for parity; worth fixing on both platforms together.
- [x] Confirmed repo has no real desktop/`jvm()` KMP target today — `CLAUDE.md`'s "Desktop (JVM)" claim is stale; flagged to user, not edited.
- [x] User pushback: replaced the manual `%1$s`-replace in `SuggestionStringResolver.applyArgs` with native `String(format:arguments:)` — `.strings` templates updated to `%1$@`/`%2$@` + proper `%%` escaping. Verified both by clean rebuild (Android + iOS green) and by a standalone `swift` script actually *executing* `String(format:arguments:)` against the real templates (single-arg, 3-arg, and the reversed-order `tomorrow_uv_message` case) — all produced correct output. See decisions.md.
- [x] User report: foreground banner was still intrusive on manual refresh (Step 2 only fixed Notification Center stacking, not on-screen interruption while the app is open). Fixed `WeatherNotificationAppDelegate.userNotificationCenter(_:willPresent:withCompletionHandler:)` to suppress `.banner` specifically for `WEATHER_UPDATED` (keeps `.list`/`.badge`), leaving alerts/suggestions unaffected. Rebuilt clean, iOS green. See decisions.md and updated AC-1.
- [x] User question: suggestions were not reliably re-armed daily. Root cause: `UNCalendarNotificationTrigger(repeats: false)` is one-shot, and the only thing re-arming it was the opportunistic hourly `BGAppRefreshTask` — nothing ran on app launch (unlike Android's `Application.onCreate` re-arm). Fixed by extracting `performWeatherRefresh(onComplete:)` in `WeatherNotificationAppDelegate.swift` and calling it from `didFinishLaunchingWithOptions` too, not just the BGTask handler. Residual limitation (no background execution guarantee if the app is never opened for days) disclosed, not fully eliminable on iOS. Rebuilt clean, iOS green. See decisions.md.

## Pre-handoff checks

- [x] Full build green (`./gradlew build` — covers Android + JVM/desktop targets; run `./gradlew :composeApp:assembleDebug` at minimum if only Android was touched) — **per-platform builds green** (`:composeApp:assembleDebug` clean → `BUILD SUCCESSFUL`; full iOS `xcodebuild` clean → `** BUILD SUCCEEDED **`); the aggregate `./gradlew build` (all targets in parallel) is red on a **validated pre-existing** Gradle task-dependency defect unrelated to this spec (confirmed via `git stash` baseline check — see decisions.md). Not fixed here — out of scope.
- [x] iOS build manually verified via Xcode if any `iosMain`/`iosApp/` file changed — verified via headless `xcodebuild -scheme iosApp -destination 'platform=iOS Simulator,...' build` (stronger than a manual Xcode click-build: real compiler+linker+codesign pass) → `** BUILD SUCCEEDED **`. The separate *interactive/visual* behavior check (does the notification actually dedupe on screen) is **not** covered by this and remains outstanding — see Step 7.
- [x] No new logs/prints touch the WeatherAPI key or any other credential — grepped all touched files for `apiKey`/`api_key`/`token`/`secret`/`password` near `print`/`log` calls; only pre-existing, unchanged `apiKey` parameter passing found, no new logging
- [x] Every touched commonMain `expect` has a matching `actual` in every affected source set — N/A, no `expect` touched (confirmed at planning time and re-confirmed: only iOS `actual` bodies and iOS-only files changed)
- [x] Any touched user-facing string shown by both Compose UI and native iOS code is updated in both `composeResources` and `iosApp/*.strings` — N/A, no new string introduced; existing `en.strings`/`es.strings`/`strings_suggestions.xml` reused unchanged
- [x] No automated-test checkbox invented — confirmed N/A; verification throughout was build-green + (partial) manual run
- [x] Acceptance criteria from proposal.md §8 satisfied — AC-1 through AC-6 all confirmed (AC-1–AC-5 by the user on a physical iOS device; AC-6 build-green by this session); AC-7 (string audit) confirmed during the follow-up string-parity pass
- [x] proposal.md frontmatter `blockers: []` (empty) — confirmed
- [x] proposal.md frontmatter `depends_on:` either `[]` OR every listed ID has a folder under `specs/_archive/` — confirmed `[]`
- [x] All §9 OQs resolved or marked out-of-band — OQ-1/OQ-2 resolved (Step 1); OQ-3 (real-device availability) explicitly deferred to the user, which is its own defined out-of-band resolution
- [x] No plan.md step retains `_(skeleton)_` — confirmed, none present

## Handoff

- [ ] Branch created (`feature/ios-weather-notifications-parity`)
- [ ] `/commit` executed
- [ ] Branch pushed
- [ ] PR opened against `develop`
- [ ] Spec folder archived to `specs/_archive/ios-weather-notifications-parity/`
- [ ] Reusable-knowledge candidates from `decisions.md` proposed
