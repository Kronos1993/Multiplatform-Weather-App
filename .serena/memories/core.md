# WeatherApp — Core Map

KMP (Kotlin Multiplatform) weather app targeting Android, iOS, Desktop (JVM). Single Gradle module
`:composeApp` (Compose Multiplatform, shared UI+logic) plus a native `iosApp` Xcode project as the iOS
entry point. Root package: `com.kronos.multiplatform.weatherapp` (== applicationId/namespace).

## Source map
- `composeApp/src/commonMain/kotlin/.../weatherapp/` — shared: `core/`, `data/`, `domain/`, `features/`,
  `device/`, `di/`, `components/`, `validator/`. See `mem:architecture` for layering.
- `composeApp/src/androidMain/` — Android actuals: `widget/` (Glance), `job/` (WorkManager), plus
  per-layer `data/local`, `data/remote`, `core/*` actuals.
- `composeApp/src/iosMain/` — iOS actuals (`.ios.kt`/`.native.kt` suffix convention), `core/job/` (bg tasks).
- `composeApp/src/jvmMain/` — desktop actuals.
- `composeApp/src/swift/` — Swift cinterop sources (MapLibre).
- `iosApp/iosApp/` — native SwiftUI host app: entry point, notification scheduling
  (`WeatherSuggestionScheduler.swift`, `WeatherNotificationAppDelegate.swift`), string resolution
  (`SuggestionStringResolver.swift`). Localized strings at `iosApp/en.strings`, `iosApp/es.strings`.
  See `mem:ios_notification_mechanics` before editing any of this.

## Invariants
- No automated test suite exists in the repo (no test source sets) — see `mem:task_completion`.
- No lint/formatter config (no detekt/ktlint/`.editorconfig`) — style is convention-only, see `mem:conventions`.

## References
- `mem:tech_stack` — languages, frameworks, key deps, versions, build config.
- `mem:architecture` — Clean Architecture layering, DI wiring, Result pattern, feature/ViewModel shape.
- `mem:conventions` — code style, localization duplication, error-handling convention.
- `mem:suggested_commands` — build/run commands per target, Darwin shell notes.
- `mem:task_completion` — what "done" means for a change (no test/lint commands to invent).
- `mem:ios_notification_mechanics` — UNUserNotificationCenter replace semantics, `willPresent`
  foreground banner control, `UNCalendarNotificationTrigger` one-shot re-arm gotcha, and where/how
  notification string templates get their args substituted (Kotlin shared vs. Swift-native) — read
  before touching anything under `core/notification/*.ios.kt`, `core/job/WeatherNotificationBackgroundTask.kt`,
  or `iosApp/iosApp/Weather*.swift`.
- `mem:gradle_ksp_multitarget_build_quirk` — why the aggregate `./gradlew build` can be red on a
  clean checkout while `:composeApp:assembleDebug` and the iOS `xcodebuild` are green; read before
  concluding a source change broke the build if only the aggregate task fails.
