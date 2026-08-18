# Decisions log: ios-weather-notifications-parity

<!--
APPEND-ONLY LOG of non-obvious choices made during /spec-implement.
See .specs/templates/decisions.md for the format contract.
-->

## 2026-08-17 — Step 2 — iOS notification replace requires clearing both delivered and pending state, not just pending

`AppNotification.ios.kt` originally relied on `addNotificationRequest` with a
stable per-`NotificationType` identifier to "replace" the previous
notification, mirroring the mental model of Android's
`NotificationManagerCompat.notify(id, ...)`. On iOS this only works while the
prior request is still *pending* — `UNUserNotificationCenter` does not retroactively
replace an already-*delivered* notification just because a new request shares
its identifier. Since the trigger used a 1-second delay, the prior request
had almost always already delivered by the time a second call arrived,
so notifications stacked instead of replacing. Fix: call
`removeDeliveredNotificationsWithIdentifiers` and
`removePendingNotificationRequestsWithIdentifiers` for the same identifier
immediately before `addNotificationRequest`, extracted into a shared
`postNotification` helper used by all three `create*` methods. This is the
correct native-iOS idiom for "at most one visible notification per logical
type" — not a port of Android's implementation.

## 2026-08-17 — Step 4 — WeatherSuggestionBackgroundTask.kt deleted; forecast fetch consolidated into WeatherNotificationBackgroundTask

The pre-existing (uncommitted) design had two classes that each fetched the
forecast independently: `WeatherNotificationBackgroundTask` (hourly
`BGAppRefreshTask`, posts the `WEATHER_UPDATED` notification) and
`WeatherSuggestionBackgroundTask.fetchAndNotify()` (meant to be invoked by
per-suggestion-slot `BGAppRefreshTask`s that were declared in `Info.plist`
but never actually registered anywhere). Rather than wiring up 3 additional
background-task registrations — which would fragment iOS's single,
OS-throttled background-execution budget across 4 competing tasks and
double WeatherAPI call volume — `onForecastReady` was added directly to
`WeatherNotificationBackgroundTask` and invoked from its existing
`refreshWeather()` success path, reusing the one fetch it already performs.
A repo-wide grep confirmed `WeatherSuggestionBackgroundTask` was never
actually constructed anywhere (not even by `WeatherNotificationAppDelegate.swift`,
which only ever instantiated `WeatherSuggestionScheduler`), so once its
callback responsibility moved, the entire file was dead code and was
deleted rather than partially trimmed. The 3 corresponding
`BGTaskSchedulerPermittedIdentifiers` entries were removed from
`Info.plist` for the same reason — `UNCalendarNotificationTrigger` delivers
at its scheduled wall-clock time on its own; it does not need a dedicated
background task to fire, only periodic fresh content, which the hourly
task now provides for free.

## 2026-08-17 — Steps 2/4/6 — `./gradlew build` (aggregate, all targets) has a real pre-existing task-graph defect; per-platform builds are green

Two distinct build symptoms showed up while verifying this spec, worth
separating clearly:

1. **`./gradlew build` (the full aggregate task, all targets — Android
   debug+release, JVM, iOS arm64+simulatorArm64 — scheduled together,
   parallel where possible) fails with 3 "Task failed with an exception"
   errors**: Gradle's task-validation flags `:composeApp:compileKotlinIosSimulatorArm64`/
   `:composeApp:compileDebugKotlinAndroid`/`:composeApp:kspDebugKotlinAndroid`/
   `:composeApp:kspKotlinIosSimulatorArm64` (exact set varies slightly
   run-to-run) for consuming `:composeApp:kspCommonMainKotlinMetadata`'s
   generated-code output without an explicit `dependsOn`/`mustRunAfter`
   declared between them. A `git stash`-based baseline check (`git stash
   push -u` to clean `develop` HEAD, re-run, `git stash pop`) reproduces
   this **exact same failure** — this one is validated as a genuine
   pre-existing Gradle build-script defect (missing task dependency
   declarations for multi-target KSP output), not something this spec's
   Kotlin/Swift edits caused. Root-caused to the project's own multi-
   target Gradle configuration, not to source content — out of this
   spec's scope to fix.
2. A **separate, secondary symptom** — Room "Redeclaration"/"has no
   corresponding expected declaration" compile errors around
   `ApplicationDatabaseConstructor`/`*_Impl` — appeared transiently in
   this session's early single-target runs (`:composeApp:assembleDebug`,
   the iOS klib via `xcodebuild`) run against an already-populated
   `build/` directory. This turned out to be **stale incremental KSP
   output**, not the same defect as (1): running `./gradlew clean` then
   re-running each single-target build resolved it completely —
   `./gradlew :composeApp:assembleDebug` → `BUILD SUCCESSFUL`, and a full
   `xcodebuild -scheme iosApp -destination 'platform=iOS Simulator,...'
   build` (compiles the KMP `iosMain` framework via Gradle as a build
   phase, then the Swift `iosApp` target, then code-signs) → `** BUILD
   SUCCEEDED **` — both green, with every one of this spec's changes
   applied.

**Net conclusion**: this repo's per-platform builds — `:composeApp:assembleDebug`
for Android and the Xcode build for iOS, which is exactly what root
`CLAUDE.md`'s Definition of Done asks for — are genuinely green for this
spec after a clean rebuild. The aggregate `./gradlew build` command is red
on a pre-existing, validated baseline defect unrelated to this spec and
is not something `/spec-implement` should attempt to fix as a side
effect; flag separately if a fully green `./gradlew build` is wanted.
**Lesson for next time**: don't assume a `./gradlew clean` is unnecessary
just because a target built successfully earlier in the same session —
this repo's multi-target KSP metadata generation can leave stale
generated Room code behind when a source file is added/removed mid-
session, and re-running `./gradlew build` (all targets, parallel) is a
different — and separately red — signal from any single-target build.

## 2026-08-17 — Follow-up (user-requested string audit) — WEATHER_UPDATED notification was 100% hardcoded/non-localized; ALL suggestion-notification placeholders were silently broken

The user asked to specifically check for hardcoded ("quemado") iOS strings
and Android-message parity. Two separate, real, previously-undiscovered
bugs turned up:

**1. `WeatherNotificationBackgroundTask.initNotificationStrings()` hardcoded
English literals that didn't match Android at all.** The `WEATHER_UPDATED`
notification (both the UI-triggered and hourly-background-triggered paths
share this one method) never respected the Spanish locale or the
metric/Fahrenheit preference — `initNotificationStrings()` set fixed
English-only templates (`"%s° | %s"`, missing even the `°C` Android has)
regardless of `measure_unit_key`. Fixed: added `notification_title`/
`notification_short_details`/`notification_long_details` (+ `_fahrenheit`
variants) to `iosApp/en.strings`/`es.strings`, copied byte-for-byte from
`composeResources/values[-es]/strings.xml`; changed `initNotificationStrings`
to accept these as parameters (mirroring `WeatherViewModel.initNotificationsString`'s
existing pattern) supplied via `NSLocalizedString` from
`WeatherNotificationAppDelegate.handleWeatherRefresh`; and made
`createWeatherNotification` branch on `MeasureUnit` for both the template
and the underlying temperature fields (`tempC`/`tempF` etc.), matching
Android's `WeatherNotificationWorker.createWeatherNotification` exactly.
Also fixed `forecast.location.region.toString()` → `.orEmpty()` (the
`.toString()` on a nullable String would have rendered the literal word
"null" if region were ever null; `WeatherViewModel`'s UI-path equivalent
already used `.orEmpty()` correctly) and removed the dead `currentWeatherKey`
property (set but never read anywhere in the class).

**2. Every suggestion-notification message was rendering broken/unresolved
placeholders.** `SuggestionStringResolver.applyArgs` (Swift) replaces
`"%1$s"`, `"%2$s"`, etc. — an exact mirror of Android's
`WeatherSuggestionNotificationWorker.resolveMessage`'s
`acc.replace("%${index+1}$s", arg)`. But `iosApp/en.strings`/`es.strings`
actually stored these templates with `%@`/`%@%%` (Objective-C format-
specifier convention) instead of `%1$s` — so the search pattern never
matched anything, and every suggestion notification would have shown the
raw `%@` characters to the user instead of the interpolated value. Fixed
by resyncing all suggestion-message *values* in both `.strings` files to
Android's exact `%1$s`/`%2$s`/`%3$s` text (generated programmatically from
`strings_suggestions.xml`/`values-es/strings_suggestions.xml` to avoid
transcription errors) — **no change needed to `SuggestionStringResolver.swift`
at all**, its substitution logic was already correct, just fed the wrong
template syntax. This resync also caught `suggestion_tomorrow_uv_message`
using reversed argument order vs Android (`%2$s UV · High %1$s°` — UV
value at index 2, temp at index 1) and ~40 messages per language that had
been independently paraphrased rather than transcribed verbatim from
Android — both are now byte-identical to Android's source text.

**Also removed while fixing (1)**: `MainViewController.kt` had a leftover
`WeatherNotificationBackgroundTask().initNotificationStrings()` call on an
instance that was immediately discarded (the line below it,
`//backgroundTask.schedule()`, was already commented out) — this call
broke once the method gained parameters and had no behavior to preserve
(nothing else used that instance), so the whole 3-line block plus its
now-unused import were deleted rather than updated.

**Not fixed — flagged as a separate, larger gap**: Android's
`WeatherSuggestionNotificationWorker` uses 4 additional string keys
(`suggestion_morning_alert_title`, `suggestion_morning_warning_title`,
`suggestion_morning_normal_title`, `suggestion_tomorrow_alert_title`) as
part of more elaborate title-selection logic that
`SuggestionStringResolver.swift`'s `resolveTitle` does not implement at
all — this is a behavioral gap, not a missing-string gap, and needs
its own investigation of `WeatherSuggestionNotificationWorker.kt`'s title
logic before porting.

**Not fixed — pre-existing Android bug, replicated faithfully for parity**:
`values-es/strings_suggestions.xml`'s `suggestion_morning_rain_message` has
`%1$s%%` (double percent) while every other Spanish rain message uses a
single `%1$s%` — since Android's own `resolveMessage` does a literal
substring replace (not printf-style formatting), this shows a literal
double `%` sign to Spanish users on Android today. Copied verbatim to iOS
per the "same as Android" requirement rather than silently diverging;
worth fixing on both platforms together in a follow-up.

**Also confirmed during this pass**: this repo has no actual Kotlin
Multiplatform `jvm()`/desktop target today (`composeApp/build.gradle.kts`'s
only JVM-related line is the Android Kotlin compiler's `jvmTarget.set(...)`
bytecode-level setting, not a KMP desktop target) — `CLAUDE.md`'s "targeting
Android, iOS, and Desktop (JVM)" line is stale; flagged to the user rather
than edited unilaterally.

## 2026-08-17 — Follow-up (user pushback) — replaced the hand-rolled `%1$s` replace with native `String(format:arguments:)`

The user correctly pushed back on the fix above: matching Android's *final
displayed text* is the goal, not reimplementing Android's own
`%1$s`-substring-replace algorithm by hand in Swift when Foundation already
has a native mechanism for exactly this (positional argument formatting
paired with `NSLocalizedString`-sourced templates — the same pairing this
file already uses for `resolveTitle`/plain lookups). Changed
`SuggestionStringResolver.applyArgs` from manual
`replacingOccurrences(of: "%N$s", with: arg)` to
`String(format: template, arguments: resolved)` (`resolved` typed
`[CVarArg]`, required since `[String]` doesn't implicitly bridge to
`[CVarArg]` — Swift arrays are invariant). Consequently the `.strings`
template *syntax* also had to change from Android's raw `%1$s`/`%2$s` to
Foundation's `%1$@`/`%2$@`, and every literal `%` that follows a
percentage placeholder now needs proper `%%` escaping (real printf-style
parsing, unlike the old manual replace, actually interprets `%%` — a
single un-escaped `%` after a valid specifier is undefined/stripped
behavior in `String(format:)`). This is a legitimate, intentional
divergence in *stored template text* from Android's XML — the outcome
(rendered message) is unchanged, verified by extracting the exact
templates into a standalone Swift script and running
`String(format:arguments:)` for real (not just compiling) against: a
single-arg + escaped-percent case, a 3-arg natural-order case, and the
reversed-order `tomorrow_uv_message` case — all produced the expected
text. `notification_title`/`notification_short_details`/`notification_long_details`
(+ Fahrenheit variants) were deliberately left as bare `%s` — those cross
the KMP boundary and are substituted by the shared Kotlin
`core/util/StringUtil.kt` `.format()` extension (the same one
`WeatherViewModel`'s already-correct UI-triggered path uses), not by
Swift/Foundation, so the "use iOS's native mechanism" principle applies
to *where the localized text comes from* (`NSLocalizedString`, unchanged)
rather than to the substitution engine for that particular string family.

## 2026-08-17 — Follow-up (user report) — foreground banner was still intrusive; Step 2's fix only covered Notification Center stacking, not on-screen interruption

The user reported that refreshing weather while the app is open still pops
a visible banner on iOS, which Android doesn't do — a second, distinct
half of the story's original "el usuario no debe recibir una sucesión de
alertas visuales cada vez que actualiza el clima" requirement that Step
2's dedup fix didn't cover. Step 2 ensures at most one `WEATHER_UPDATED`
notification exists in Notification Center at a time; it does nothing
about whether iOS *visually interrupts the current screen* with a banner
while the app is foregrounded — that's controlled separately, by
`UNUserNotificationCenterDelegate.userNotificationCenter(_:willPresent:withCompletionHandler:)`,
which `WeatherNotificationAppDelegate.swift` was unconditionally answering
with `[.banner, .list, .badge]` for every notification, regardless of
type. `willPresent` is only invoked while the app is active/foreground
(a backgrounded app's notification presentation is handled entirely by
the OS, unaffected by this change) — exactly matching where the user says
the intrusiveness shows up.

Fixed by branching on `notification.request.identifier`: for
`"WEATHER_UPDATED"` specifically, return `[.list, .badge]` (still updates
Notification Center and the badge, so the latest data is available when
the user checks — just no banner popup interrupting the screen they're
already looking at); every other identifier (`WEATHER_ALERT`, the
`suggestion_*` scheduled notifications) keeps the full
`[.banner, .list, .badge]` treatment, since a severe-weather alert should
still interrupt, and this wasn't the behavior the user was complaining
about. Deliberately scoped to `WEATHER_UPDATED` only — not asked to
extend to alerts/suggestions, and alerts in particular should probably
keep interrupting on purpose.

## 2026-08-17 — Follow-up (user question) — suggestions were not reliably re-armed daily; nothing ran on app launch

The user asked directly whether suggestions fire on schedule like on
Android. They don't, reliably: two compounding gaps.

1. `WeatherSuggestionScheduler.scheduleAt` uses
   `UNCalendarNotificationTrigger(dateMatching:, repeats: false)` — each of
   the 3 daily notifications is **one-shot**. Once it fires (or is
   replaced by `cancelPrevious()`), nothing re-arms it for the next day
   without another `scheduleAll` call.
2. The *only* thing that ever called `scheduleAll` was the hourly
   `refresh_weather_notification` `BGAppRefreshTask` succeeding — which is
   opportunistic (iOS decides if/when it runs; no guarantee it fires
   every hour, especially for infrequently-opened apps). Opening the app
   and refreshing manually from the UI (`WeatherViewModel`'s path) never
   touched `WeatherSuggestionScheduler` at all.

Android avoids this via a stronger re-arm chain: `WeatherApplication.onCreate()`
calls `WeatherSuggestionScheduler.scheduleAll(this)` on **every app
process start** (guaranteed, user-driven), and
`WeatherNotificationWorker.fetchAndNotifyWeather`'s success additionally
calls `scheduleDailyPeriodic` (a genuinely recurring 24h WorkManager
request) — two layers of reliability iOS's port only had one (weaker) copy
of.

Fixed using iOS's own launch lifecycle (not a port of `Application.onCreate`,
but the natural iOS equivalent moment): extracted the worker-setup/fetch
logic already in `handleWeatherRefresh` into a shared
`performWeatherRefresh(onComplete:)`, and call it once from
`application(_:didFinishLaunchingWithOptions:)` in addition to from the
`BGAppRefreshTask` handler. This guarantees suggestions get freshly
(re)scheduled every time the user actually opens the app, with the hourly
background task remaining as a best-effort supplement for days the app
isn't opened at all.

**Residual, disclosed limitation**: if the app is never opened for
multiple days *and* iOS never opportunistically runs the background
refresh task in that window (a real possibility — Apple deliberately
does not guarantee background execution time to preserve battery, and
WorkManager on Android is a materially stronger reliability guarantee
than BGTaskScheduler), suggestions can still silently stop until the next
app open. This is an inherent iOS platform constraint, not a bug in this
fix; both re-arm paths (launch + hourly background) now exist to
minimize how often that residual gap is actually hit.
