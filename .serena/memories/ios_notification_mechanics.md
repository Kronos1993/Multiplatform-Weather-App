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
