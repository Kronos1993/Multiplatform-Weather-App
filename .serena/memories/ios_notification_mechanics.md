# iOS Notification Mechanics (UNUserNotificationCenter)

Gotchas discovered fixing `core/notification/AppNotification.ios.kt`,
`core/job/WeatherNotificationBackgroundTask.kt`, and
`iosApp/iosApp/WeatherSuggestionScheduler.swift` /
`SuggestionStringResolver.swift` for `spec-id: ios-weather-notifications-parity`.

## "Replace" semantics differ from Android

- `UNUserNotificationCenter.addNotificationRequest` with an existing
  identifier only replaces a still-**pending** request. Once a request has
  **delivered** (e.g. after a short `UNTimeIntervalNotificationTrigger`
  fires), a same-identifier add does NOT replace it — it stacks a second
  visible notification.
- To get Android's `NotificationManagerCompat.notify(id, ...)` replace
  behavior, call BOTH `removeDeliveredNotificationsWithIdentifiers` AND
  `removePendingNotificationRequestsWithIdentifiers` for that identifier
  immediately before `addNotificationRequest`. See
  `AppNotification.ios.kt`'s `postNotification` helper.

## `willPresent` controls foreground interruption, per notification type

- `UNUserNotificationCenterDelegate.userNotificationCenter(_:willPresent:withCompletionHandler:)`
  fires only while the app is foreground/active; a backgrounded app's
  presentation is handled by the OS regardless of this delegate.
- Return `[.list, .badge]` (no `.banner`) for routine/non-urgent
  notification types to avoid interrupting the screen while the app is
  open — matches Android not showing a heads-up alert for equivalent
  notifications. Branch on `notification.request.identifier` (it equals
  the Kotlin `NotificationType.name`, e.g. `"WEATHER_UPDATED"`, since
  identifiers are set from Kotlin's `.name` when posting).

## `UNCalendarNotificationTrigger(repeats: false)` is one-shot

- A trigger built from `dateMatching: DateComponents(hour:, minute:)` with
  `repeats: false` fires once at the next matching wall-clock time, then
  is consumed — it does NOT recur daily on its own.
- Recurring daily content requires re-arming (re-scheduling) before each
  occurrence. In this app, suggestion notifications are re-armed both at
  app launch (`didFinishLaunchingWithOptions`) and via the opportunistic
  hourly `BGAppRefreshTask` — launch is the only *guaranteed* re-arm
  point, since `BGTaskScheduler` execution timing is OS-controlled with
  no guarantee of running in any given window (mirrors why Android's
  `Application.onCreate` re-arms `WorkManager` requests on every process
  start).

## Localized notification string templates

- iOS notification/suggestion string templates live in native
  `iosApp/en.strings` / `iosApp/es.strings` (`NSLocalizedString`), NOT in
  Compose Multiplatform resources — background/notification code can run
  without any live Compose composition, so `stringResource()` isn't
  available there.
- These `.strings` values must match Android's `composeResources/values[-es]/*.xml`
  text byte-for-byte, including argument order — but the placeholder
  *token* differs: Android's XML uses `%1$s`/`%2$s` (consumed by a custom
  Kotlin `String.format` extension in `core/util/StringUtil.kt`, shared
  cross-platform), while Foundation's native `String(format:arguments:)`
  needs `%1$@`/`%2$@` and requires literal `%` to be escaped as `%%`.
  Which one applies depends on which side does the substitution:
  - `notification_title`/`notification_short_details`/`notification_long_details`
    (WEATHER_UPDATED) cross into shared Kotlin (`WeatherNotificationBackgroundTask.createWeatherNotification`
    calls the shared `.format()` extension) — keep bare `%s` there.
  - Suggestion messages are resolved entirely in Swift
    (`SuggestionStringResolver.applyArgs`) — use Foundation's native
    `String(format: template, arguments: resolved)` (`resolved` must be
    typed `[CVarArg]`, not `[String]` — Swift arrays are invariant and
    won't implicitly bridge) instead of hand-rolling Android's `%1$s`
    substring-replace algorithm in Swift. Don't reinvent Android's
    formatting when Foundation already has the native equivalent.

## Koin must start before any background-triggered notification path runs

- `initKoin()` used to run only inside `MainViewController()`'s
  `ComposeUIViewController` configure block — only executes once SwiftUI
  builds `ComposeView` (app already foregrounded). `AppDelegate
  .didFinishLaunchingWithOptions` calls `performWeatherRefresh()` →
  `WeatherNotificationBackgroundTask` (a `KoinComponent`) before that point,
  and `BGAppRefreshTask` wake-ups can run without SwiftUI ever building the
  view. Uninitialized Koin makes `by inject()` throw, silently swallowed by
  `refreshWeather()`'s catch — no crash, no notification, no suggestion
  re-arm.
- Fixed by starting Koin explicitly and early in `AppDelegate
  .didFinishLaunchingWithOptions` via a no-arg wrapper in
  `MainViewController.kt` (`startKoinIOS()`).

## Kotlin/Native renames "init*" top-level functions to "doInit*" in ObjC/Swift export

- Any Kotlin top-level function whose name starts with `init` gets renamed
  to `doInit...` when exported to Objective-C/Swift (avoids colliding with
  Cocoa's initializer naming convention — same reason `initKoin(config:)`
  shows up as `doInitKoin(config:)`).
- Check the generated `ComposeApp.h` header if a Swift call to a Kotlin
  function reports "no member" — it may just be under the `doInit`-prefixed
  name. Avoid the `init` prefix on any Kotlin function meant to be called
  from Swift.

## `api_key` must come from the bundled resource, never from a preference

- `WeatherNotificationBackgroundTask.getWeatherParams()` used to read
  `apiKey` via `preferenceRepository.getPreference("api_key", "")` — but
  nothing in the app ever calls `setPreference("api_key", ...)`; it's a
  bundled secret (`composeResources/values/api.xml`), not a user
  preference. Every background/launch refresh silently 401'd.
- Fixed by reading it with the non-composable Compose Resources accessor:
  `getString(Res.string.api_key)` (works outside composition, unlike
  `stringResource()`).

## `getWeatherParams()`'s other preference lookups used the wrong keys

- `default_lang_key`/`default_days_key`/`measure_unit_key` were used as
  literal DataStore lookup keys, but the app stores preferences under the
  resource's *value* (`"default_lang"`/`"default_days"`/`"measure_unit"` —
  see `composeResources/values/preference_key.xml`), not its name. Every
  background refresh silently ignored the user's saved language/days/units.
- The `measureUnit` fallback was doubly wrong: `"INTERNATIONAL"` was passed
  as the default, but `MeasureUnit.from()` only parses `"1"` as
  INTERNATIONAL — any other string (including that literal fallback)
  resolves to IMPERIAL.
- Android's `WeatherNotificationWorker.doWork()` already did this correctly
  via `context.getString(R.string.default_lang_key)` — mirror that pattern
  (resolve the *resource*, not a literal string) for any new iOS preference
  lookup, and call `changeLang.onLangChange(lang)` after resolving it,
  matching Android's per-worker-run re-application.

## `en.strings`/`es.strings` are flat files, not real Apple localization

- `project.pbxproj` has them as plain `PBXFileReference`s (not a
  `PBXVariantGroup`), and `knownRegions` only lists `en` — `es` isn't a
  declared project localization. Plain `NSLocalizedString(key, comment:)`
  looks up the default `"Localizable"` table inside the current locale's
  `.lproj`, which doesn't exist, so it always fell back to returning the
  raw key (notification text showed literal keys like `notification_title`
  instead of resolved strings).
- Fix: pass `tableName:` explicitly (`"en"`/`"es"`, matching the literal
  bundled filename) instead of relying on `.lproj` resolution. The app's
  in-app language picker (`ChangeLang.ios.kt`) already writes the chosen
  language to `NSUserDefaults["AppleLanguages"]` — read that same key to
  pick the table.
- `AppDelegate.ensureDefaultLanguagePreference()` seeds that key from the
  system language on first-ever launch (before the user has touched the
  language picker), so background code never depends on unset state.
