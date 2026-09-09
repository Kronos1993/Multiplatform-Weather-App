# Decisions log — adopt-ktlint

Append-only. Non-obvious choices made during /spec-implement.

## 2026-09-09 — Plugin and version chosen (Step 1)

- **Decision**: use `org.jlleitschuh.gradle.ktlint` version `14.2.0` (latest
  stable as of 2026-03-12), and leave the ktlint *engine* version at the
  plugin's own default rather than pinning it explicitly via
  `ktlint { version.set(...) }`.
- **Why**: the plugin has declared Kotlin Multiplatform support since
  before this project's Kotlin version, requires Gradle ≥7.4 / Kotlin
  ≥1.4 (well under this project's versions), and has defaulted to the
  ktlint 1.x engine (the `.editorconfig`-driven `ktlint_standard_*` rule
  scheme) since its own v13.0.0 — which is exactly the scheme the
  project's `.editorconfig` already assumes. Pinning the ktlint engine
  version separately would add another version to track in a repo with
  no CI gate and no test suite exercising it; the plugin's own default
  is enough here and keeps `libs.versions.toml` to one new entry.
- **Sources**: https://plugins.gradle.org/plugin/org.jlleitschuh.gradle.ktlint , https://github.com/JLLeitschuh/ktlint-gradle , https://github.com/JLLeitschuh/ktlint-gradle/blob/main/CHANGELOG.md

## 2026-09-09 — Compliance measurement (Step 3)

- **Decision**: `Step 3 result: violation_scope = small`.
- **Evidence**: `./gradlew :composeApp:ktlintCheck` (full run, not `--dry-run`)
  failed exactly 2 of ~20 generated check tasks:
  `ktlintKotlinScriptCheck` (20 violations, all in
  `composeApp/build.gradle.kts`) and `ktlintIosMainSourceSetCheck` (68
  violations across 19 files under `composeApp/src/iosMain/kotlin/...`).
  `ktlintAndroidMainSourceSetCheck` and `ktlintCommonMainSourceSetCheck`
  passed with zero violations (no report file generated for either).
  Every violation is one of: `final-newline`,
  `trailing-comma-on-call-site`, `trailing-comma-on-declaration-site`,
  `no-consecutive-blank-lines`, `no-empty-first-line-in-class-body`,
  `no-blank-line-before-rbrace`, `curly-spacing`, `comma-spacing`,
  `comment-spacing`, `no-consecutive-comments`, `paren-spacing`,
  `import-ordering`, `no-multi-spaces` — all purely mechanical formatting
  rules with zero semantic/naming/structural judgment involved. No
  overlap with the rules the `.editorconfig` deliberately disables
  (`function-naming`, `package-name`, `filename`, and the wrapping-rule
  block) — confirming the existing `.editorconfig` did not need any
  edits for this pass (AC-2 satisfied as "confirmed complete", not
  "updated").
  Full file list and per-rule counts: see the ktlint HTML/txt reports
  under `composeApp/build/reports/ktlint/` from this run (not committed —
  build output).
- **Why `small`**: 88 violations across 20 files, zero requiring
  judgment, is squarely within "mechanical and bounded" — proceeds
  straight to Step 4 (`ktlintFormat`) in this same spec rather than
  opening a follow-up spec.
- **Partial fix already applied before the correction below**: ran
  `ktlintKotlinScriptFormat` + `ktlintIosMainSourceSetFormat`
  (auto-fixed everything except one `no-consecutive-comments` violation
  in `composeApp/build.gradle.kts:119`, fixed by hand with a blank
  line). Both `ktlintKotlinScriptCheck` and `ktlintIosMainSourceSetCheck`
  are now green. This work is kept — it's correct and verified — even
  though the `small` classification below turned out to be wrong for
  the *rest* of the codebase.

## 2026-09-09 — CORRECTION: violation_scope is actually `large`

- **What went wrong**: the Step 3 measurement above was taken from a
  single `./gradlew :composeApp:ktlintCheck` run *without* `--continue`.
  Gradle's default fail-fast behavior meant that once
  `ktlintKotlinScriptCheck` and `ktlintIosMainSourceSetCheck` failed,
  Gradle stopped scheduling further independent tasks — including
  `ktlintAndroidMainSourceSetCheck` and `ktlintCommonMainSourceSetCheck`,
  which had *started* (their worker action lines appeared in the log)
  but never finished or reported a result. Their absence from the
  failure list was misread as "passed clean" — it actually meant "never
  finished checking."
- **Corrected measurement** (`./gradlew :composeApp:ktlintCheck
  --continue`, run after the Step 4 partial fix above): 7 source-set
  check tasks fail, not 2:
  - `ktlintCommonMainSourceSetCheck` — **185 distinct files** with
    violations (thousands of violation lines — dominated by
    `trailing-comma-on-call-site`/`final-newline`/`paren-spacing`
    per the `App.kt`/`CardView.kt` sample, i.e. still mechanical rules,
    just at a much larger scale than "small").
  - `ktlintAndroidMainSourceSetCheck` — 41 distinct files.
  - `ktlintAndroidReleaseSourceSetCheck` / `ktlintAndroidDebugSourceSetCheck`
    — 4 files each (AGP build-variant duplicates of the androidMain set).
  - `ktlintIosArm64MainSourceSetCheck` / `ktlintIosX64MainSourceSetCheck` /
    `ktlintIosSimulatorArm64MainSourceSetCheck` — 5 files each (per-target
    duplicates; `ktlintIosMainSourceSetCheck` itself is green after the
    Step 4 fix above).
- **Revised decision**: `Step 3 result: violation_scope = large`
  (supersedes the `small` marker above — do not act on that one).
  Step 4 does **not** run against this bulk (`commonMain`/`androidMain`
  and their variant/target duplicates) in this spec — reformatting
  ~220 files is exactly the "unreviewable mass diff" this plan's Step 4
  condition was designed to prevent. Recommending a follow-up spec
  (see OQ-3 resolution in proposal.md) sized specifically for that
  cleanup, likely split further by area given the file count.
- **Consequence for AC-5 (`./gradlew build` stays green)**: since the
  `org.jlleitschuh.gradle.ktlint` plugin wires `ktlintCheck` into the
  standard `check` lifecycle task (which `build` depends on), leaving
  this backlog unresolved would turn `./gradlew build` red — a real
  regression, not a pre-existing one, since `build` was green before
  this spec. Fix: `ktlint { ignoreFailures.set(true) }` added in
  `composeApp/build.gradle.kts`, with a comment pointing at this
  decision and the follow-up cleanup spec. This keeps ktlint installed,
  runnable, and its violations visible in console output/reports
  (satisfies "instalar" + "verificar compliance" + "dejar constancia"),
  without gating the build on a backlog this spec deliberately doesn't
  fix. The follow-up cleanup spec's own Definition of Done should flip
  `ignoreFailures` back to `false` once the backlog is cleared, so
  ktlint actually starts enforcing rather than just reporting.

## 2026-09-09 — Exclude KSP/Room-generated sources from ktlint's scope

- **Finding**: of the 185 files flagged in `ktlintCommonMainSourceSetCheck`,
  8 were under `composeApp/build/generated/ksp/...` (Room-generated DAO
  impls / KSP metadata) — reached because `composeApp/build.gradle.kts`
  explicitly adds `build/generated/ksp/metadata` to `commonMain`'s
  `kotlin.srcDir(...)`. Similarly 1 of 41 in `androidMain`. Some
  iOS-target-specific check tasks (`ktlintIosArm64/IosX64/IosSimulatorArm64
  MainSourceSetCheck`) failed almost entirely on generated Room DAO impls
  under `build/generated/ksp/<target>/...`.
- **Decision**: added a `filter { exclude { ... } }` block to the
  `ktlint {}` extension in `composeApp/build.gradle.kts` excluding any
  path containing a `generated` path segment. This was already
  anticipated in plan.md Step 4's own file note ("confirmar que ningún
  archivo generado... excluir vía ktlint filter si el plugin no los
  excluye por defecto") — applied here rather than deferred, since it's
  needed for accurate numbers regardless of which half of Step 4 runs.
- **Verified**: re-ran `./gradlew :composeApp:ktlintCheck --continue
  --rerun-tasks` (forced, no cache) after adding the filter —
  `ktlintCommonMainSourceSetCheck` now reports 177 files (was 185, all 8
  generated ones gone) and `ktlintAndroidMainSourceSetCheck` reports 40
  (was 41). `ktlintCheck` overall still `BUILD SUCCESSFUL` (thanks to
  `ignoreFailures`).
- **Final corrected numbers for the follow-up cleanup spec's sizing**:
  **177 files in commonMain + 40 in androidMain** (217 total) carry real,
  hand-written-code ktlint violations, still dominated by mechanical
  rules per the earlier sample (`trailing-comma-on-call-site`,
  `final-newline`, `paren-spacing`, etc.) — genuinely `large` by file
  count, but likely still mostly `ktlintFormat`-fixable; the follow-up
  spec should confirm that with its own investigate step rather than
  assume it here.

## 2026-09-09 — `./gradlew build` red: pre-existing baseline, not this spec

- **What happened**: plan.md Step 6 ran `./gradlew build` after all
  other steps and got `BUILD FAILED` — Gradle's parallel-execution task
  validation flags several KSP/compile tasks
  (`:composeApp:compileReleaseKotlinAndroid`,
  `:composeApp:compileKotlinIosX64/IosArm64/IosSimulatorArm64`,
  `:composeApp:compileDebugKotlinAndroid`, etc.) for consuming
  `:composeApp:kspCommonMainKotlinMetadata`'s output without a declared
  task dependency.
- **Confirmed pre-existing, not introduced by this spec**: `git stash -u`
  (stashing every change from this spec) then `./gradlew build` on the
  clean tree reproduced the identical failure signature
  (`:composeApp:kspDebugKotlinAndroid` uses `kspCommonMainKotlinMetadata`'s
  output without declaring a dependency). `git stash pop` restored this
  spec's changes afterward. This also matches the pre-existing Serena
  memory `gradle_ksp_multitarget_build_quirk`, which documents this exact
  defect as unrelated to any particular source change and out of scope
  for feature/chore work — `CLAUDE.md`'s actual Definition of Done never
  required the aggregate `./gradlew build` to be green locally, only
  `:composeApp:assembleDebug` (+ Xcode for iOS).
- **AC-5 reinterpreted accordingly**: verified via
  `./gradlew :composeApp:assembleDebug` (Android, this spec's primary
  touched platform) instead of the aggregate `./gradlew build`, per the
  actual Definition of Done and the pre-existing-baseline exception in
  `/spec-implement`'s verification policy. Not a regression introduced
  here.

## 2026-09-09 — Unplanned files changed: Android Studio's ktlint IDE plugin

- **What happened**: `git status` after Step 6 showed 6 files modified
  that no command in this spec touched: `WeatherApplication.kt`,
  `job/WeatherWidgetUpdateWorker.kt`, `widget/OpenAppCallback.kt`,
  `widget/WeatherWidgetReceiver.kt`, `widget/WidgetBootReceiver.kt`
  (all `androidMain`) and `features/home/current_weather/WeatherScreen.kt`
  (`commonMain`) — all outside plan.md's Step 4 file scope
  (iosMain + `composeApp/build.gradle.kts` only).
- **Per `/spec-implement`'s "does not silently expand scope" rule**,
  stopped and surfaced this to the user rather than deciding unilaterally.
  User confirmed: they already had ktlint's IntelliJ/Android Studio
  plugin installed, which auto-formatted these files (format-on-save)
  while they were open in the IDE during this session's Gradle runs —
  not something this spec's commands did.
- **Verified style-only before including**: read every diff
  (`git diff` per file). All changes are exactly the same mechanical
  categories already covered by this spec elsewhere: trailing commas,
  removed double-blank-lines, `{` joined to its declaration line, added
  final newline, one `when`-branch-label reflow
  (`DeviceScreenConfiguration.DESKTOP -> {` → trailing-comma-style
  multi-line), and one missing space after a comma
  (`defaultCity,measureUnit` → `defaultCity, measureUnit`) in
  `WeatherScreen.kt`. Zero logic/behavior changes in any of the 6 files.
- **Decision**: kept all 6 (user's explicit call, confirmed style-only).
  Not re-run through `ktlintCheck` as a discrete step since they were
  already IDE-formatted to the same `.editorconfig` rules this spec
  installed; `ktlintCheck`'s reports (Step 3/verification above) already
  cover whether they're clean going forward.
