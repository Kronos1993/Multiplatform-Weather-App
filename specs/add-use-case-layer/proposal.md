---
spec_id: add-use-case-layer
title: Add a use-case layer between ViewModels/consumers and repositories
type: refactor
priority: normal
source: manual
source_ref: dictated in chat, 2026-09-09
created: 2026-09-09
status: INTAKE_PARSED
recommend_split: no
blockers: []
depends_on: []
expect_actual_touched: no
localization_touched: no
branch_suggested: refactor/add-use-case-layer
---

# Proposal: Add a use-case layer between ViewModels/consumers and repositories

## Inherited assumptions

_(none — story.md has no explicit Assumptions section; the scope-cut question it raised about
`PreferenceRepository` was resolved directly with the user during planning, see below, rather than
carried forward as an assumption.)_

## 1. Source

Manual intake (`specs/add-use-case-layer/story.md`, dictated 2026-09-09). The user wants a use-case
layer inserted between every consumer (ViewModel, Worker, Glance widget, iOS background task) and
the repository interfaces under `domain/repository/*`, replicating a pattern from an unrelated
project of the user's — but with **no reference to that other project** in any generated code
comment or document; every design decision here is written as if it originated in this repo.

During planning, the user confirmed two decisions that expand and fix the scope described in
`story.md`:
- Keep this as a single spec (not split into siblings), despite the size — the work is mechanical
  and well-bounded, not ambiguous.
- Include `core/preferences/repository/PreferenceRepository` in the migration too (the story had
  left this as an open question, leaning towards excluding it as "infrastructure"). The user chose
  to apply the categorical rule ("no repository is used directly anywhere") without a carve-out.

Planning-time investigation (plain `Read`/`Grep` — the project's Kotlin LSP is currently down, see
`mem:kotlin_lsp_expired_build_upstream_blocker`) also surfaced a consumer `story.md` didn't
enumerate: `features/home/setting/SettingScreen.kt` reaches through
`PreferenceViewModel.preferenceRepository` (a public `val`) to call
`preferenceRepository.setPreference(...)` directly from a **Composable**, not just from a
ViewModel/Worker/widget/background task. This is now in scope too (see §4).

## 2. Problem / Why

Today, `domain/repository/*` interfaces are injected and called directly from ViewModels, Android
Workers, the Glance widget, and the iOS background task — and, via a leaked public property, from
one Composable Screen. There is no intermediate layer that owns "one unit of business logic per
repository operation." This makes it impossible to enforce a single narrow contract per operation,
harder to add cross-cutting logic to one operation without touching the ViewModel, and inconsistent
with the Clean Architecture boundary this repo already documents (`mem:architecture`) — the
boundary currently stops at the repository interface instead of at a use case.

This is a pure internal-architecture refactor: no user-facing behavior changes on any platform.

## 3. Scope

**In scope**

- New `core/usecase/UseCase.kt`: shared abstract base
  `abstract class UseCase<in P, out R> { abstract suspend fun run(params: P): R; suspend operator fun invoke(params: P): R = run(params) }`.
- One abstract + one `Impl` class pair per operation of **6** repository interfaces (see §4 for the
  full list), under `domain/usecase/<feature>/`, mirroring the `<feature>` subpackage naming already
  used under `data/repository/<feature>/` (and, for `PreferenceRepository`, a new `preferences`
  subpackage under `domain/usecase/`, chosen for consistency even though the interface itself lives
  under `core/preferences/repository/` rather than `domain/repository/`).
- A new flat `useCaseModule` in `di/Modules.kt`, registered in `initKoin()` (`di/Koin.kt`) before
  `viewModelModule`.
- Migrating every current direct repository consumer (3 ViewModels, `PreferenceViewModel`, 1
  Composable Screen, 3 Android Workers, 1 Glance widget, 1 iOS background task — 10 files, see §4)
  to inject/call the matching use case(s), typed as the **abstract** class, instead of the
  repository interface or `Impl`.
- `CLAUDE.md`'s Architecture section, updated to document the use-case layer as the new consumer →
  repository path.

**Out of scope**

- Any change to the 6 repository interfaces' or their `Impl` classes' shape/signatures — this is a
  pure additive layer on top; behavior is unchanged.
- `data/local/datasources/PreferenceDataSource` (used directly by
  `data/remote/datasources/WeatherRemoteDataSourceImpl.kt` for the "last forecast" cache) — that's a
  data-source-to-data-source call inside the data layer, not a `domain/repository` or
  `core/preferences/repository` consumer, and is unaffected by this refactor.
- Any change to `/new-feature`'s scaffolding templates. Investigated during planning
  (`.claude/skills/new-feature/SKILL.md` + its `assets/`/templates) — it does not currently generate
  a ViewModel that injects a repository directly in a way this spec needs to correct; if a future run
  reveals otherwise, that's a follow-up, not blocking this spec (see OQ-1).

**Explicit override of the split heuristic**: this proposal's §4 spans 4 of this repo's
`architecture.modules` categories (`domain`, `core`, `di`, `features`) and §7 lists 5 risks, both
past `.specs/config.json`'s `plan.split_thresholds` (3 modules / 4 risks) — the split heuristic would
normally fire. The user was shown a concrete 3-way split proposal (foundational use-case layer →
ViewModel migration → Worker/widget/iOS migration) during planning and explicitly chose to keep this
as one spec: the work is mechanical repetition of one pattern across a closed, fully-enumerated list
of operations and consumers (no scope ambiguity), and plan.md sequences it as many small,
independently-buildable steps so a single large spec doesn't mean a single large unreviewable diff.

## 4. Affected areas

| Area | Source set(s) | Class(es) / file(s) touched | Change type | Resolved by | Notes |
|--------|--------------|-------------------------------|-------------|-------------|-------|
| core | commonMain | `core/usecase/UseCase.kt` | New | | Shared base class, no existing consumers to enumerate (brand new). |
| domain | commonMain | `domain/usecase/location/{GetCurrentLocationUseCase,GetCurrentLocationUseCaseImpl,IsLocationEnabledUseCase,IsLocationEnabledUseCaseImpl}.kt` | New | | Wraps `LocationRepository` (2 ops). |
| domain | commonMain | `domain/usecase/radar/rain/{GetMapLayerTilesUseCase,GetMapLayerTilesUseCaseImpl}.kt` | New | | Wraps `MapLayerRepository` (1 op); subpackage mirrors `data/repository/radar/rain`. |
| domain | commonMain | `domain/usecase/user_custom_location/{GetSelectedUserLocationUseCase,GetCurrentUserLocationUseCase,SaveUserLocationUseCase,ListUserLocationsUseCase,DeleteUserLocationUseCase}(Impl).kt` | New | | Wraps `UserCustomLocationLocalRepository` (5 ops, 10 files). |
| domain | commonMain | `domain/usecase/alerts/{GetWeatherAlertsUseCase,GetWeatherAlertsUseCaseImpl}.kt` | New | | Wraps `WeatherAlertsRemoteRepository` (1 op); no active caller today (same as the repository itself), kept for architectural consistency per `story.md`. |
| domain | commonMain | `domain/usecase/weather/{GetWeatherUseCase,GetWeatherForecastByCityUseCase,GetWeatherForecastByCoordinatesUseCase,GetLastWeatherForecastUseCase,SetLastWeatherForecastUseCase}(Impl).kt` | New | | Wraps `WeatherRemoteRepository` (5 ops, 10 files); the two `getWeatherDataForecast` overloads (by city / by lat-lon) get distinctly-named use cases since overload resolution doesn't carry through the use-case boundary. |
| domain | commonMain | `domain/usecase/preferences/{GetStringPreferenceUseCase,GetIntPreferenceUseCase,GetBooleanPreferenceUseCase,GetDoublePreferenceUseCase,SetStringPreferenceUseCase,SetIntPreferenceUseCase,SetBooleanPreferenceUseCase,SetDoublePreferenceUseCase}(Impl).kt` | New | | Wraps `core/preferences/repository/PreferenceRepository` (8 ops — its 4 typed `get`/`set` overloads become 8 distinct use cases, 16 files); added per the user's planning decision to include `PreferenceRepository`. |
| di | commonMain | `di/Modules.kt` (new `useCaseModule`), `di/Koin.kt` (`initKoin()`) | Modified | | `useCaseModule` binds all 22 `XxxUseCaseImpl` to their abstract `XxxUseCase`; inserted before `viewModelModule`. |
| features | commonMain | `features/add_city/AddCityViewModel.kt` | Modified | | Replace `WeatherRemoteRepository`/`UserCustomLocationLocalRepository`/`MapLayerRepository`/`LocationRepository` constructor params with the specific use cases the class actually calls. |
| features | commonMain | `features/home/current_weather/WeatherViewModel.kt` | Modified | | Same 4 repositories → use cases. |
| features | commonMain | `features/home/user_location/UserCustomLocationViewModel.kt` | Modified | | `WeatherRemoteRepository`/`UserCustomLocationLocalRepository` → use cases. |
| core | commonMain | `core/preferences/PreferenceViewModel.kt` | Modified | | Constructor takes the 8 preference use cases instead of `PreferenceRepository`; `preferenceRepository` becomes a private implementation detail (or is removed) rather than a public `val`. |
| features | commonMain | `features/home/setting/SettingScreen.kt` | Modified | | 4 call sites currently do `viewModel.preferenceRepository.setPreference(key, it)` — replace with the existing `viewModel.savePreference(key, it)` wrapper (already present, currently unused by these call sites) now that the repository property is no longer public. |
| androidMain | androidMain | `job/WeatherAlertNotificationWorker.kt` | Modified | | `WeatherAlertsRemoteRepository`/`UserCustomLocationLocalRepository` `by inject()` → use cases. |
| androidMain | androidMain | `job/WeatherNotificationWorker.kt` | Modified | | `WeatherRemoteRepository`/`UserCustomLocationLocalRepository`/`PreferenceRepository` `by inject()` → use cases. |
| androidMain | androidMain | `job/WeatherSuggestionNotificationWorker.kt` | Modified | | Same 3 repositories → use cases. |
| androidMain | androidMain | `widget/BaseWeatherGlanceWidget.kt` | Modified | | Same 3 repositories → use cases. |
| iosMain | iosMain | `core/job/WeatherNotificationBackgroundTask.kt` | Modified | | Same 3 repositories → use cases. |
| docs | n/a | `CLAUDE.md` (Architecture section) | Modified | | Document consumer → use case → repository interface → `Impl` flow, replacing the current "ViewModels depend on domain/repository interfaces" framing. |

## 5. Architectural gauntlet (this repo's hard rules)

### 5a. Always explicit (no shortcut)

- [x] **Expect/actual parity** — N/A. No `expect` declaration is added or changed: `UseCase` and every
      `XxxUseCase`/`XxxUseCaseImpl` are plain `commonMain` classes with no platform-specific members;
      the per-platform consumer files (`androidMain`/`iosMain`) change which type they inject via
      Koin, not any `expect`/`actual` contract.
      Approach: confirmed — verified by reading all 6 repository interfaces and all 10 consumer files;
      none declares or implements an `expect`.
- [x] **Dual localization** — N/A. No user-facing string is added, removed, or reworded by this
      refactor; it changes only which class a constructor/`by inject()` references.
      Approach: confirmed.
- [x] **Secrets & logging** — confirmed. Every `XxxUseCaseImpl.run()` body is a one-line delegation
      to the wrapped repository method (`repository.method(params...)`); no new logging is introduced
      anywhere in this refactor, so the WeatherAPI key (passed as a plain `String` parameter on
      several use cases, exactly as it already is on the repository methods) is never logged,
      printed, or included in an error message that didn't already do so before this change.
      Approach: confirmed.
- [x] **No automated tests exist** — acknowledged. Verification is build-green
      (`./gradlew :composeApp:assembleDebug` after the additive use-case layer lands, `./gradlew
      build` after every consumer is migrated) plus a manual run on Android/desktop and an Xcode
      build for iOS; no test-suite step is invented anywhere in plan.md.
      Acknowledged: yes.

### 5b. Confinement-conditional

- [ ] **Confinement claim** — not applicable; this change is explicitly cross-cutting (new
      `domain`/`core` types, new `di` wiring, and edits to 10 consumer files across `features`,
      `androidMain`, and `iosMain`). Both rules below are answered explicitly instead.

- [x] **Domain/data boundary (DIP)** — every new `XxxUseCaseImpl` depends on the existing
      `domain/repository/*` interface (or `PreferenceRepository`), never on the `data/*Impl` class
      directly — the same DIP boundary the repositories already establish, just moved one layer
      out. No consumer will reference a `data/*Impl` class at any point; consumers will reference
      only the abstract `XxxUseCase`, which itself only knows about the repository interface.
      Approach: confirmed by design — see the `UseCase`/`XxxUseCaseImpl` shape in §3 and the file
      list in §4.
- [x] **Result-type error handling** — unchanged from today: use cases wrapping a repository method
      that already returns `core/result/Result` (`WeatherRemoteRepository`,
      `WeatherAlertsRemoteRepository`, `MapLayerRepository`) return that same `Result` type from
      `run()`; use cases wrapping a method that returns a plain nullable/value type
      (`LocationRepository`, `UserCustomLocationLocalRepository`, `PreferenceRepository`) return
      that same plain type — this refactor does not change any method's return shape, only where
      it's called from.
      Approach: confirmed — see the per-operation return types listed in §4.

## 6. Skills

**Skills**: direct edits

## 7. Risks

- A Koin binding mistake in the new `useCaseModule` (wrong `bind<>()`, or a missing entry among the
  22) breaks app startup entirely rather than one screen — Koin resolves DI lazily per-class, so a
  missing binding only surfaces the first time that specific `XxxUseCase` is injected, which may be
  on a code path not exercised by a quick manual smoke test (e.g. `WeatherAlertsRemoteRepository`'s
  use case, which has no active caller today).
- 22 near-identical `XxxUseCase`/`XxxUseCaseImpl` pairs (44 new files) is repetitive, boilerplate-
  heavy work with no automated test suite to catch a copy-paste mistake (e.g. an `Impl` silently
  delegating to the wrong repository method, or a `Params` field mismatched against its call sites)
  — each pair needs to be checked by hand against the interface it wraps.
- The two `WeatherRemoteRepository.getWeatherDataForecast` overloads (by city vs. by lat/lon) must
  map to two distinctly-named use cases (`GetWeatherForecastByCityUseCase` /
  `GetWeatherForecastByCoordinatesUseCase`) since a use case can't carry Kotlin overload resolution
  through its class boundary — a caller now picks the right one by class name instead of by
  argument types, so migrating a call site to the wrong one compiles cleanly but silently changes
  which weather-fetch path runs.
- `PreferenceRepository`'s `by inject()` sites live in 4 files reachable only at runtime on specific
  triggers (WorkManager retry windows, a Glance widget resize/update, an iOS background refresh) —
  none of which is exercised by starting the app once, so a missed or mistyped injection there can
  ship without being noticed until the corresponding background trigger fires days later.
- `CLAUDE.md`'s Architecture section is the one place this repo documents "ViewModels depend on
  domain/repository interfaces" as canonical; if it isn't updated in the same change, the next
  spec/feature to scaffold a ViewModel will follow stale guidance and reintroduce a direct
  repository dependency this refactor just removed.

## Out-of-band actions

- Manually run the app on Android (a screen backed by each migrated ViewModel: Home, Add City,
  Settings, User Location) and on Desktop (JVM) after the full migration, since no automated test
  suite exists to catch a runtime-only DI wiring mistake (see §7's first and fourth risks).
- Manually trigger or wait for each Android background path at least once post-merge — a
  `WeatherNotificationWorker`/`WeatherSuggestionNotificationWorker`/`WeatherAlertNotificationWorker`
  run, a Glance widget resize, and (via Xcode/TestFlight or a real device) the iOS background
  refresh task — to catch a `PreferenceRepository`/`WeatherAlertsRemoteRepository` use-case wiring
  mistake that a cold app start won't exercise.
- iOS build via Xcode (`iosApp/iosApp.xcodeproj`) — no headless build path exists for this target.

## 8. Acceptance criteria

- [ ] **AC-1** — `core/usecase/UseCase.kt` exists with the exact shape described in §3, and every one
      of the 22 operations across `LocationRepository`, `MapLayerRepository`,
      `UserCustomLocationLocalRepository`, `WeatherAlertsRemoteRepository`, `WeatherRemoteRepository`,
      and `PreferenceRepository` has a matching abstract `XxxUseCase` + concrete `XxxUseCaseImpl`
      pair under `domain/usecase/<feature>/`, each delegating to the repository with no added logic.
- [ ] **AC-2** — `grep -rn "domain\.repository\." --include=*.kt composeApp/src` and
      `grep -rn "core\.preferences\.repository\.PreferenceRepository" --include=*.kt composeApp/src`
      each return matches **only** inside `data/repository/*Impl` (or, for `PreferenceRepository`,
      `core/preferences/repository/PreferenceRepositoryImpl.kt`), the new `domain/usecase/*Impl`
      classes, and the two DI files (`data/*/di/Modules*.kt`, `core/di/Module.kt`,
      `di/Modules.kt`) that bind the interface to its implementation — no ViewModel, Worker, widget,
      background task, or Composable Screen file appears in either grep's output.
- [ ] **AC-3** — All 22 use cases are registered in the new `useCaseModule`
      (`singleOf(::XxxUseCaseImpl).bind<XxxUseCase>()` each) and `initKoin()` includes
      `useCaseModule` before `viewModelModule`.
- [ ] **AC-4** — `./gradlew build` succeeds (Android + JVM/desktop targets), and the iOS Xcode
      project builds; a manual run confirms no behavior change on Android and Desktop (per the
      Out-of-band actions above for full confidence, but at minimum: app launches, Home/Add
      City/Settings/User Location screens load data as before).
- [ ] **AC-5** — No new code comment anywhere in the diff references the other project this pattern
      was adapted from; every comment (if any) reads as if the design originated in this repo.
- [ ] **AC-6** — `CLAUDE.md`'s Architecture section documents the consumer → use case → repository
      interface → `Impl` flow.

## 9. Open questions

- **OQ-1** — `/new-feature`'s scaffolding was checked at a glance during planning and does not
  appear to generate a ViewModel that injects a repository directly in a way this spec must correct
  in lockstep; a full read of its templates is deferred to `plan.md` Step 1 (`[investigate]`) to
  confirm before this spec closes, and to decide whether it needs a small follow-up edit so newly
  scaffolded features start from the use-case pattern rather than needing a later migration like
  this one.
  **Resolved (plan.md Step 1)**: yes, it does. `.claude/skills/new-feature/SKILL.md`:
  - §5 "ViewModel" (lines 167-171) scaffolds `class {Feature}ViewModel(private val {feature}Repository: {Feature}Repository, ...)` — direct repository injection.
  - §4 "DI wiring" (lines 140-156) binds `{Feature}RepositoryImpl` to `{Feature}Repository` in `commonDataLocalModules`/`commonRemoteModules` and registers the ViewModel in `viewModelModule`, with no use-case scaffolding or binding step.
  `Step 1 result: new-feature-injects-repo-directly = yes` → plan.md Step 40 runs.

---

## Serena memories consulted

- `mem:core`
- `mem:architecture`
- `mem:conventions`
- `mem:kotlin_lsp_expired_build_upstream_blocker` — explains why this planning pass used plain
  `Read`/`Grep` instead of Serena's symbolic Kotlin tools.
