# Decisions log: add-use-case-layer

## 2026-09-09 — Step 38 — `./gradlew build` hits a pre-existing, unrelated Gradle/KSP defect

The full aggregate `./gradlew build` failed with `kspCommonMainKotlinMetadata` task-ordering
validation errors (several Android/iOS compile tasks consuming its output without a declared
dependency). This is the exact signature already documented in the Serena memory
`gradle_ksp_multitarget_build_quirk`, confirmed pre-existing via an earlier `git stash`-to-baseline
check unrelated to this spec's changes. Per that memory, the real local gate is single-target
builds — substituted `:composeApp:assembleDebug` (Android, BUILD SUCCESSFUL) and
`:composeApp:compileKotlinIosSimulatorArm64` (iOS, BUILD SUCCESSFUL, since this spec touches an
`iosMain` file) instead of re-running the known-broken aggregate task.

## 2026-09-09 — Step 31 — PreferenceViewModel injects only 6 of the 8 preference use cases

plan.md's Step 31 said "Constructor takes the 8 preference use cases" for parity with
`PreferenceRepository`'s 8 operations, but `PreferenceViewModel`'s actual call sites only ever pass
a `String` or `Int` default (never `Boolean`/`Double` for *reads*, though `savePreference`'s
`when(value)` dispatch does need all 4 `Set*` use cases). Injected `GetStringPreferenceUseCase`,
`GetIntPreferenceUseCase`, and all 4 `Set*PreferenceUseCase`s (6 total), omitting
`GetBooleanPreferenceUseCase`/`GetDoublePreferenceUseCase` since nothing in this ViewModel calls
them — matching the pattern the story itself sets for `GetWeatherAlertsUseCase` (created for
repository-interface parity even without an active caller, but never force-injected into a
consumer that doesn't need it). All 8 preference use cases still exist and are Koin-registered.

## 2026-09-09 — Pre-handoff — ktlintFormat scoped to touched/new files only

`./gradlew :composeApp:ktlintCheck` reports ~400 violations module-wide — the pre-existing backlog
`CLAUDE.md`/`mem:conventions` already document (`ignoreFailures = true` bootstrap accommodation).
Running the Gradle `ktlintFormat` task would reformat the whole module, dragging unrelated files
into this diff. Instead ran the standalone `ktlint` CLI (`ktlint -F <file> <file> ...`, same
`.editorconfig`) scoped to exactly the 57 files this spec touched or created (13 modified + 44 new
`domain/usecase` files) — this auto-fixed all mechanical issues (trailing commas, indentation,
whitespace) in code this spec actually wrote. The remaining ktlint output on those 57 files is all
tagged `(cannot be auto-corrected)` — pre-existing `TAG`/backing-property naming and a handful of
long lines — verified by reading the flagged lines directly: every one sits in code this spec did
not write (e.g. the `MapLayerType` `when` block and `createWeatherNotification`'s notification-text
formatting in `AddCityViewModel`/`WeatherViewModel`/`UserCustomLocationViewModel`, already over the
140-column limit before this spec touched an adjacent line). No new violation was introduced by
this spec's own code.

## 2026-09-09 — Handoff — branched off develop instead of the stale chore/adopt-ktlint branch

Implementation happened on `chore/adopt-ktlint` (the branch active when `/spec-implement` started),
but that branch carries 3 of its own unmerged commits for an unrelated, already-handed-off chore
spec — not the right base for this `refactor`-type spec's `refactor/add-use-case-layer` branch. Per
user confirmation at `/spec-handoff` time: `git stash push -u`, `git checkout develop`,
`git checkout -b refactor/add-use-case-layer`, `git stash pop`. `CLAUDE.md` auto-merged cleanly
(different sections). `WeatherNotificationBackgroundTask.kt` conflicted — `chore/adopt-ktlint` had
already applied ktlint's trailing-comma formatting to this file, which `develop` doesn't have yet,
overlapping with this spec's own edits to the same lines. Resolved by keeping this spec's
(post-ktlint-formatted) version throughout, then re-ran `ktlint -F` on the file to reformat the
hunks that fell back to `develop`'s pre-ktlint style during the auto-merge (e.g.
`createWeatherNotification`, unrelated to this spec, ktlint-clean now anyway). Rebuilt
(`:composeApp:assembleDebug` + `:composeApp:compileKotlinIosSimulatorArm64`) and re-ran both AC-2
greps after resolving — all still clean, confirming the conflict resolution didn't reintroduce a
direct-repository reference or lose any migration.
