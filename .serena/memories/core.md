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
- `mem:android_widget_spacing_tokens` — `widget/components/WidgetTheme.kt`'s size-class resolver
  (`resolveWidgetSizeClass`) and the `WidgetTypography`/`WidgetSpacing` token pattern; read before
  adding any new size-dependent value to the Android home-screen widgets.
- `mem:android_widget_error_content_double_padding` — a pre-existing double background/padding
  trap in `WeatherWidgetErrorContent`; read before touching that composable.
- `mem:android_widget_sizemode_responsive_candidates` — `BaseWeatherGlanceWidget`'s
  `SizeMode.Responsive` candidate set is exactly the 4 real declared widget minimums, never
  synthetic in-between values; read before adding any new size-mode candidate or breakpoint.
- `mem:android_widget_content_dispatch_size_classification` — why the live-resize
  content-transform dispatcher (`AdaptiveWeatherWidgetContent`) deliberately does NOT reuse
  `rememberWidgetSizeClass()`; read before changing which composable a widget renders.
- `mem:android_widget_clock_graphic_scaling` — how the clock widgets' native
  `AnalogClock`/`TextClock` graphic scales outside the Glance token system, and the
  `AnalogClock` intrinsic-size gotcha (it never grows past its drawable's declared size); read
  before touching `widget_rtc_analog_clock*.xml`/`widget_rtc_digital_clock.xml` or their
  drawables.
- `mem:android_widget_padding_vs_centering` — centering cancels an ancestor's padding
  algebraically (visible margin becomes `containerSize/2 - contentSize/2`, independent of the
  padding value); read before assuming a padding/spacing token change will be visible on a
  centered widget composable, and check the background drawable's corner radius first on compact
  widgets.
- `mem:android_widget_clock_condition_maxlines_resize` — the two clock-variant widgets share the
  same `SizeMode.Responsive` candidate set as Small/Medium/Large, so `LocalSize.current` does
  change on resize even though they have no `Adaptive*` dispatch; read before assuming their
  content is resize-inert, and before adding any new size-dependent behavior to them.
- `mem:android_widget_minresize_missing_blocks_adaptive_content` — a widget provider XML missing
  `android:minResizeWidth`/`android:minResizeHeight` silently floors shrink-resize at its
  `minWidth`/`minHeight`, which can look like a broken `Adaptive*` dispatch when it's really a
  manifest omission; read before adding a new resizable widget provider or debugging why one
  won't transform content on shrink.
