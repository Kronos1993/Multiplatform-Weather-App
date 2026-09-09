---
spec_id: add-use-case-layer
generated_by: /spec-plan
generated_at: 2026-09-09T00:00:00Z
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

# Plan: Add a use-case layer between ViewModels/consumers and repositories

## Strategy

Land the use-case layer as pure addition first (base class → 22 operation pairs → Koin wiring →
build-green checkpoint), so nothing existing is at risk while that boilerplate goes in. Only then
migrate every consumer, one file at a time, from repository injection to use-case injection —
grouped by consumer kind (ViewModels/Screen → Android Workers/widget → iOS background task) so a
mistake in one group is easy to isolate. Close with a full-build + grep verification of the "no
direct repository use" acceptance criterion, then the `CLAUDE.md` doc update and the
`/new-feature`-scaffolding follow-up decided by Step 1.

Each use-case-creation step is scoped to exactly one repository operation (abstract + `Impl`, 2
files) to keep every step within this template's ≤3-files-per-step guidance despite the sheer
operation count (22) — see proposal.md §4/§7 for why this stayed one spec instead of splitting.

## Steps

### Step 1 — Read /new-feature's scaffolding templates for direct repository injection [investigate]

- **Files / symbols**:
  - `.claude/skills/new-feature/SKILL.md` — read the ViewModel-scaffolding step/template it documents
  - Any template/example file it references for the generated ViewModel constructor shape
- **Question(s) to answer**: Does the skill's generated ViewModel constructor inject a
  `domain/repository/*` interface directly? (resolves proposal.md OQ-1)
- **Outputs to record**: `Step 1 result: new-feature-injects-repo-directly = yes|no`. If `yes`,
  record which template file/line needs updating — Step 40 below is conditional on this.
- **Why**: without this, `/new-feature` could keep generating code that reintroduces the exact
  pattern this spec removes.

### Step 2 — Add the shared UseCase base class [implement]

- **Skill**: direct edits
- **Area(s)**: `core`
- **Files / symbols**:
  - `composeApp/src/commonMain/kotlin/com/kronos/multiplatform/weatherapp/core/usecase/UseCase.kt` — new:
    `abstract class UseCase<in P, out R> { abstract suspend fun run(params: P): R; suspend operator fun invoke(params: P): R = run(params) }`
- **Skill args / inputs**: none
- **Verification**: file exists; `./gradlew :composeApp:compileKotlinDesktop` (or equivalent quick
  compile) resolves with no errors introduced by this file alone.

<!-- ---- domain/usecase/location (LocationRepository, 2 ops) ---- -->

### Step 3 — Add GetCurrentLocationUseCase [implement]

- **Skill**: direct edits
- **Area(s)**: `domain`
- **Files / symbols**:
  - `domain/usecase/location/GetCurrentLocationUseCase.kt` — new: `abstract class GetCurrentLocationUseCase : UseCase<Unit, LocationModel?>()`
  - `domain/usecase/location/GetCurrentLocationUseCaseImpl.kt` — new: `class GetCurrentLocationUseCaseImpl(private val repository: LocationRepository) : GetCurrentLocationUseCase() { override suspend fun run(params: Unit) = repository.getCurrentLocation() }`
- **Skill args / inputs**: none
- **Verification**: both files exist; symbol `GetCurrentLocationUseCaseImpl` compiles against `LocationRepository`.

### Step 4 — Add IsLocationEnabledUseCase [implement]

- **Skill**: direct edits
- **Area(s)**: `domain`
- **Files / symbols**:
  - `domain/usecase/location/IsLocationEnabledUseCase.kt` — new: `abstract class IsLocationEnabledUseCase : UseCase<Unit, Boolean>()`
  - `domain/usecase/location/IsLocationEnabledUseCaseImpl.kt` — new, delegates to `LocationRepository.isLocationEnabled()`
- **Skill args / inputs**: none
- **Verification**: both files exist and compile.

<!-- ---- domain/usecase/radar/rain (MapLayerRepository, 1 op) ---- -->

### Step 5 — Add GetMapLayerTilesUseCase [implement]

- **Skill**: direct edits
- **Area(s)**: `domain`
- **Files / symbols**:
  - `domain/usecase/radar/rain/GetMapLayerTilesUseCase.kt` — new: `abstract class GetMapLayerTilesUseCase : UseCase<Unit, Result<MapLayerTiles, Error>>()`
  - `domain/usecase/radar/rain/GetMapLayerTilesUseCaseImpl.kt` — new, delegates to `MapLayerRepository.getLayerTiles()`
- **Skill args / inputs**: none
- **Why**: subpackage mirrors `data/repository/radar/rain` (two-level), not a flat `map_layer`.
- **Verification**: both files exist and compile.

<!-- ---- domain/usecase/user_custom_location (UserCustomLocationLocalRepository, 5 ops) ---- -->

### Step 6 — Add GetSelectedUserLocationUseCase [implement]

- **Skill**: direct edits
- **Area(s)**: `domain`
- **Files / symbols**:
  - `domain/usecase/user_custom_location/GetSelectedUserLocationUseCase.kt` — new: `UseCase<Unit, UserCustomLocation?>`
  - `domain/usecase/user_custom_location/GetSelectedUserLocationUseCaseImpl.kt` — new, delegates to `UserCustomLocationLocalRepository.getSelectedLocation()`
- **Skill args / inputs**: none
- **Verification**: both files exist and compile.

### Step 7 — Add GetCurrentUserLocationUseCase [implement]

- **Skill**: direct edits
- **Area(s)**: `domain`
- **Files / symbols**:
  - `domain/usecase/user_custom_location/GetCurrentUserLocationUseCase.kt` — new: `UseCase<Unit, UserCustomLocation?>`
  - `domain/usecase/user_custom_location/GetCurrentUserLocationUseCaseImpl.kt` — new, delegates to `UserCustomLocationLocalRepository.getCurrentLocation()`
- **Skill args / inputs**: none
- **Why**: named `...UserLocation...` (not `...Location...`) to stay unambiguous next to Step 3's `GetCurrentLocationUseCase` (GPS location, a different repository).
- **Verification**: both files exist and compile.

### Step 8 — Add SaveUserLocationUseCase [implement]

- **Skill**: direct edits
- **Area(s)**: `domain`
- **Files / symbols**:
  - `domain/usecase/user_custom_location/SaveUserLocationUseCase.kt` — new: `abstract class SaveUserLocationUseCase : UseCase<SaveUserLocationUseCase.Params, UserCustomLocation>()` with nested `data class Params(val userCustomLocation: UserCustomLocation, val isCurrent: Boolean = false)`
  - `domain/usecase/user_custom_location/SaveUserLocationUseCaseImpl.kt` — new, delegates to `UserCustomLocationLocalRepository.saveLocation(params.userCustomLocation, params.isCurrent)`
- **Skill args / inputs**: none
- **Verification**: both files exist and compile.

### Step 9 — Add ListUserLocationsUseCase [implement]

- **Skill**: direct edits
- **Area(s)**: `domain`
- **Files / symbols**:
  - `domain/usecase/user_custom_location/ListUserLocationsUseCase.kt` — new: `UseCase<Unit, List<UserCustomLocation>>`
  - `domain/usecase/user_custom_location/ListUserLocationsUseCaseImpl.kt` — new, delegates to `UserCustomLocationLocalRepository.listAll()`
- **Skill args / inputs**: none
- **Verification**: both files exist and compile.

### Step 10 — Add DeleteUserLocationUseCase [implement]

- **Skill**: direct edits
- **Area(s)**: `domain`
- **Files / symbols**:
  - `domain/usecase/user_custom_location/DeleteUserLocationUseCase.kt` — new: `abstract class DeleteUserLocationUseCase : UseCase<DeleteUserLocationUseCase.Params, Boolean>()` with nested `data class Params(val userCustomLocation: UserCustomLocation)`
  - `domain/usecase/user_custom_location/DeleteUserLocationUseCaseImpl.kt` — new, delegates to `UserCustomLocationLocalRepository.delete(params.userCustomLocation)`
- **Skill args / inputs**: none
- **Verification**: both files exist and compile.

<!-- ---- domain/usecase/alerts (WeatherAlertsRemoteRepository, 1 op) ---- -->

### Step 11 — Add GetWeatherAlertsUseCase [implement]

- **Skill**: direct edits
- **Area(s)**: `domain`
- **Files / symbols**:
  - `domain/usecase/alerts/GetWeatherAlertsUseCase.kt` — new: `abstract class GetWeatherAlertsUseCase : UseCase<GetWeatherAlertsUseCase.Params, Result<CurrentAlertsForecast, Error>>()` with nested `data class Params(val lat: Double, val lon: Double, val apiKey: String)`
  - `domain/usecase/alerts/GetWeatherAlertsUseCaseImpl.kt` — new, delegates to `WeatherAlertsRemoteRepository.getWeatherAlertsData(params.lat, params.lon, params.apiKey)`
- **Skill args / inputs**: none
- **Why**: created for architectural consistency even though no consumer calls it today (matches the repository's own current state — see proposal.md §4).
- **Verification**: both files exist and compile.

<!-- ---- domain/usecase/weather (WeatherRemoteRepository, 5 ops) ---- -->

### Step 12 — Add GetWeatherUseCase [implement]

- **Skill**: direct edits
- **Area(s)**: `domain`
- **Files / symbols**:
  - `domain/usecase/weather/GetWeatherUseCase.kt` — new: `abstract class GetWeatherUseCase : UseCase<GetWeatherUseCase.Params, Result<CurrentForecast, Error>>()` with nested `data class Params(val city: String, val lang: String, val apiKey: String)`
  - `domain/usecase/weather/GetWeatherUseCaseImpl.kt` — new, delegates to `WeatherRemoteRepository.getWeatherData(params.city, params.lang, params.apiKey)`
- **Skill args / inputs**: none
- **Verification**: both files exist and compile.

### Step 13 — Add GetWeatherForecastByCityUseCase [implement]

- **Skill**: direct edits
- **Area(s)**: `domain`
- **Files / symbols**:
  - `domain/usecase/weather/GetWeatherForecastByCityUseCase.kt` — new: `abstract class GetWeatherForecastByCityUseCase : UseCase<GetWeatherForecastByCityUseCase.Params, Result<Forecast, Error>>()` with nested `data class Params(val city: String, val lang: String, val apiKey: String, val days: Int = 1)`
  - `domain/usecase/weather/GetWeatherForecastByCityUseCaseImpl.kt` — new, delegates to the city-overload `WeatherRemoteRepository.getWeatherDataForecast(params.city, params.lang, params.apiKey, params.days)`
- **Skill args / inputs**: none
- **Why**: distinct name from Step 14 — see proposal.md §7 (overload resolution doesn't cross the use-case boundary).
- **Verification**: both files exist and compile.

### Step 14 — Add GetWeatherForecastByCoordinatesUseCase [implement]

- **Skill**: direct edits
- **Area(s)**: `domain`
- **Files / symbols**:
  - `domain/usecase/weather/GetWeatherForecastByCoordinatesUseCase.kt` — new: `abstract class GetWeatherForecastByCoordinatesUseCase : UseCase<GetWeatherForecastByCoordinatesUseCase.Params, Result<Forecast, Error>>()` with nested `data class Params(val lat: Double, val lon: Double, val lang: String, val apiKey: String, val days: Int = 1)`
  - `domain/usecase/weather/GetWeatherForecastByCoordinatesUseCaseImpl.kt` — new, delegates to the lat/lon-overload `WeatherRemoteRepository.getWeatherDataForecast(params.lat, params.lon, params.lang, params.apiKey, params.days)`
- **Skill args / inputs**: none
- **Verification**: both files exist and compile.

### Step 15 — Add GetLastWeatherForecastUseCase [implement]

- **Skill**: direct edits
- **Area(s)**: `domain`
- **Files / symbols**:
  - `domain/usecase/weather/GetLastWeatherForecastUseCase.kt` — new: `abstract class GetLastWeatherForecastUseCase : UseCase<GetLastWeatherForecastUseCase.Params, Result<Forecast, Error>>()` with nested `data class Params(val prefKey: String)`
  - `domain/usecase/weather/GetLastWeatherForecastUseCaseImpl.kt` — new, delegates to `WeatherRemoteRepository.getLastWeatherForecast(params.prefKey)`
- **Skill args / inputs**: none
- **Verification**: both files exist and compile.

### Step 16 — Add SetLastWeatherForecastUseCase [implement]

- **Skill**: direct edits
- **Area(s)**: `domain`
- **Files / symbols**:
  - `domain/usecase/weather/SetLastWeatherForecastUseCase.kt` — new: `abstract class SetLastWeatherForecastUseCase : UseCase<SetLastWeatherForecastUseCase.Params, Result<Boolean, Error>>()` with nested `data class Params(val prefKey: String, val forecast: Forecast)`
  - `domain/usecase/weather/SetLastWeatherForecastUseCaseImpl.kt` — new, delegates to `WeatherRemoteRepository.setLastWeatherForecast(params.prefKey, params.forecast)`
- **Skill args / inputs**: none
- **Verification**: both files exist and compile.

<!-- ---- domain/usecase/preferences (PreferenceRepository, 8 ops) ---- -->

### Step 17 — Add GetStringPreferenceUseCase [implement]

- **Skill**: direct edits
- **Area(s)**: `domain`
- **Files / symbols**:
  - `domain/usecase/preferences/GetStringPreferenceUseCase.kt` — new: `abstract class GetStringPreferenceUseCase : UseCase<GetStringPreferenceUseCase.Params, String>()` with nested `data class Params(val key: String, val defaultValue: String)`
  - `domain/usecase/preferences/GetStringPreferenceUseCaseImpl.kt` — new, delegates to `PreferenceRepository.getPreference(params.key, params.defaultValue)` (String overload)
- **Skill args / inputs**: none
- **Verification**: both files exist and compile.

### Step 18 — Add GetIntPreferenceUseCase [implement]

- **Skill**: direct edits
- **Area(s)**: `domain`
- **Files / symbols**:
  - `domain/usecase/preferences/GetIntPreferenceUseCase.kt` — new, `Params(key: String, defaultValue: Int)` → `Int`
  - `domain/usecase/preferences/GetIntPreferenceUseCaseImpl.kt` — new, delegates to the Int overload of `PreferenceRepository.getPreference`
- **Skill args / inputs**: none
- **Verification**: both files exist and compile.

### Step 19 — Add GetBooleanPreferenceUseCase [implement]

- **Skill**: direct edits
- **Area(s)**: `domain`
- **Files / symbols**:
  - `domain/usecase/preferences/GetBooleanPreferenceUseCase.kt` — new, `Params(key: String, defaultValue: Boolean)` → `Boolean`
  - `domain/usecase/preferences/GetBooleanPreferenceUseCaseImpl.kt` — new, delegates to the Boolean overload of `PreferenceRepository.getPreference`
- **Skill args / inputs**: none
- **Verification**: both files exist and compile.

### Step 20 — Add GetDoublePreferenceUseCase [implement]

- **Skill**: direct edits
- **Area(s)**: `domain`
- **Files / symbols**:
  - `domain/usecase/preferences/GetDoublePreferenceUseCase.kt` — new, `Params(key: String, defaultValue: Double)` → `Double`
  - `domain/usecase/preferences/GetDoublePreferenceUseCaseImpl.kt` — new, delegates to the Double overload of `PreferenceRepository.getPreference`
- **Skill args / inputs**: none
- **Verification**: both files exist and compile.

### Step 21 — Add SetStringPreferenceUseCase [implement]

- **Skill**: direct edits
- **Area(s)**: `domain`
- **Files / symbols**:
  - `domain/usecase/preferences/SetStringPreferenceUseCase.kt` — new, `Params(key: String, value: String)` → `Unit`
  - `domain/usecase/preferences/SetStringPreferenceUseCaseImpl.kt` — new, delegates to the String overload of `PreferenceRepository.setPreference`
- **Skill args / inputs**: none
- **Verification**: both files exist and compile.

### Step 22 — Add SetIntPreferenceUseCase [implement]

- **Skill**: direct edits
- **Area(s)**: `domain`
- **Files / symbols**:
  - `domain/usecase/preferences/SetIntPreferenceUseCase.kt` — new, `Params(key: String, value: Int)` → `Unit`
  - `domain/usecase/preferences/SetIntPreferenceUseCaseImpl.kt` — new, delegates to the Int overload of `PreferenceRepository.setPreference`
- **Skill args / inputs**: none
- **Verification**: both files exist and compile.

### Step 23 — Add SetBooleanPreferenceUseCase [implement]

- **Skill**: direct edits
- **Area(s)**: `domain`
- **Files / symbols**:
  - `domain/usecase/preferences/SetBooleanPreferenceUseCase.kt` — new, `Params(key: String, value: Boolean)` → `Unit`
  - `domain/usecase/preferences/SetBooleanPreferenceUseCaseImpl.kt` — new, delegates to the Boolean overload of `PreferenceRepository.setPreference`
- **Skill args / inputs**: none
- **Verification**: both files exist and compile.

### Step 24 — Add SetDoublePreferenceUseCase [implement]

- **Skill**: direct edits
- **Area(s)**: `domain`
- **Files / symbols**:
  - `domain/usecase/preferences/SetDoublePreferenceUseCase.kt` — new, `Params(key: String, value: Double)` → `Unit`
  - `domain/usecase/preferences/SetDoublePreferenceUseCaseImpl.kt` — new, delegates to the Double overload of `PreferenceRepository.setPreference`
- **Skill args / inputs**: none
- **Verification**: both files exist and compile.

<!-- ---- DI wiring ---- -->

### Step 25 — Register all 22 use cases in a new useCaseModule [implement]

- **Skill**: direct edits
- **Area(s)**: `di`
- **Files / symbols**:
  - `di/Modules.kt` — add `val useCaseModule = module { singleOf(::XxxUseCaseImpl).bind<XxxUseCase>() }` — one line per use case from Steps 3–24 (22 entries)
- **Skill args / inputs**: none
- **Verification**: file compiles; every `XxxUseCaseImpl` from Steps 3–24 appears exactly once.

### Step 26 — Wire useCaseModule into initKoin() [implement]

- **Skill**: direct edits
- **Area(s)**: `di`
- **Files / symbols**:
  - `di/Koin.kt` — add `useCaseModule` to the `modules(...)` list in `initKoin()`, before `viewModelModule`
- **Skill args / inputs**: none
- **Verification**: file compiles; `useCaseModule` appears in the list before `viewModelModule`.

### Step 27 — Verify the additive use-case layer builds green [verify]

- **What to check**: `./gradlew :composeApp:assembleDebug` succeeds with the new
  `core/usecase/`, `domain/usecase/`, and `di` changes in place, before any consumer is touched.
- **Pass criteria**: build succeeds with no compile errors; no existing consumer file has been
  modified yet (this step exists purely to catch a mistake in Steps 2–26 before consumer migration
  begins).

<!-- ---- Consumer migration: ViewModels + Screen ---- -->

### Step 28 — Migrate AddCityViewModel to use cases [implement]

- **Skill**: direct edits
- **Area(s)**: `features/add_city`
- **Files / symbols**:
  - `features/add_city/AddCityViewModel.kt` — replace the `weatherRemoteRepository`,
    `userCustomLocationLocalRepository`, `mapLayerRepository`, `locationRepository` constructor
    params with the specific use cases (from Steps 3–16) this class's call sites actually invoke;
    update every call site from `repository.method(...)` to `useCase(Params(...))` or
    `useCase(Unit)`
- **Skill args / inputs**: none
- **Verification**: `./gradlew :composeApp:assembleDebug` succeeds; no import of
  `domain.repository.*` remains in this file.

### Step 29 — Migrate WeatherViewModel to use cases [implement]

- **Skill**: direct edits
- **Area(s)**: `features/home/current_weather`
- **Files / symbols**:
  - `features/home/current_weather/WeatherViewModel.kt` — same 4-repository → use-case replacement as Step 28
- **Skill args / inputs**: none
- **Verification**: build succeeds; no `domain.repository.*` import remains.

### Step 30 — Migrate UserCustomLocationViewModel to use cases [implement]

- **Skill**: direct edits
- **Area(s)**: `features/home/user_location`
- **Files / symbols**:
  - `features/home/user_location/UserCustomLocationViewModel.kt` — replace
    `weatherRemoteRepository`/`userCustomLocationLocalRepository` with the use cases it calls
- **Skill args / inputs**: none
- **Verification**: build succeeds; no `domain.repository.*` import remains.

### Step 31 — Migrate PreferenceViewModel to use cases [implement]

- **Skill**: direct edits
- **Area(s)**: `core/preferences`
- **Files / symbols**:
  - `core/preferences/PreferenceViewModel.kt` — replace the `PreferenceRepository` constructor param
    with the 8 preference use cases from Steps 17–24; rewrite `loadPreferences`, `savePreference`,
    and every `getPreferenceXxx`/`setPreferenceXxx` method to call the matching use case instead of
    `preferenceRepository.getPreference(...)`/`.setPreference(...)`; the `preferenceRepository`
    property is no longer public (private or removed) — `savePreference(key, value: Any)` remains
    the public entry point for callers
- **Skill args / inputs**: none
- **Why**: this ViewModel's `preferenceRepository` was a public `val`, which Step 32's Screen was
  reaching through directly — making it private/removed is what makes Step 32 necessary and
  correct.
- **Verification**: build succeeds; no `core.preferences.repository.PreferenceRepository` import
  remains in this file; `preferenceRepository` is not a public member.

### Step 32 — Fix SettingScreen's direct repository access [implement]

- **Skill**: direct edits
- **Area(s)**: `features/home/setting`
- **Files / symbols**:
  - `features/home/setting/SettingScreen.kt` — 4 call sites currently do
    `viewModel.preferenceRepository.setPreference(key, it)` (lines ~220, ~238, ~270, ~290 as read
    during planning); replace each with `viewModel.savePreference(key, it)`, which already performs
    the same type-dispatched write and is already public on `PreferenceViewModel`
- **Skill args / inputs**: none
- **Why**: this is the one non-ViewModel/Worker/widget consumer found during planning (see
  proposal.md §1) — without this fix, `PreferenceViewModel.preferenceRepository` could not be made
  private in Step 31 without breaking this Screen.
- **Verification**: build succeeds; `grep -n "preferenceRepository"` in this file returns nothing.

<!-- ---- Consumer migration: Android Workers + widget ---- -->

### Step 33 — Migrate WeatherAlertNotificationWorker to use cases [implement]

- **Skill**: direct edits
- **Area(s)**: `androidMain` (`job`)
- **Files / symbols**:
  - `job/WeatherAlertNotificationWorker.kt` — replace the `weatherAlertsRemoteRepository`/
    `userCustomLocationLocalRepository` `by inject()` properties with the matching use cases
    (`GetWeatherAlertsUseCase` + the relevant `user_custom_location` use case(s)); update call sites
- **Skill args / inputs**: none
- **Verification**: `./gradlew :composeApp:assembleDebug` succeeds; no `domain.repository.*` import remains.

### Step 34 — Migrate WeatherNotificationWorker to use cases [implement]

- **Skill**: direct edits
- **Area(s)**: `androidMain` (`job`)
- **Files / symbols**:
  - `job/WeatherNotificationWorker.kt` — replace `weatherRemoteRepository`/
    `userCustomLocationLocalRepository`/`preferenceRepository` `by inject()` properties with the
    matching use cases (from `weather`, `user_custom_location`, and `preferences`); update call sites
- **Skill args / inputs**: none
- **Verification**: build succeeds; no `domain.repository.*` or
  `core.preferences.repository.PreferenceRepository` import remains.

### Step 35 — Migrate WeatherSuggestionNotificationWorker to use cases [implement]

- **Skill**: direct edits
- **Area(s)**: `androidMain` (`job`)
- **Files / symbols**:
  - `job/WeatherSuggestionNotificationWorker.kt` — same 3-repository → use-case replacement as Step 34
- **Skill args / inputs**: none
- **Verification**: build succeeds; no direct repository import remains.

### Step 36 — Migrate BaseWeatherGlanceWidget to use cases [implement]

- **Skill**: direct edits
- **Area(s)**: `androidMain` (`widget`)
- **Files / symbols**:
  - `widget/BaseWeatherGlanceWidget.kt` — same 3-repository → use-case replacement as Step 34
- **Skill args / inputs**: none
- **Verification**: build succeeds; no direct repository import remains.

<!-- ---- Consumer migration: iOS background task ---- -->

### Step 37 — Migrate WeatherNotificationBackgroundTask to use cases [implement]

- **Skill**: direct edits
- **Area(s)**: `iosMain` (`core/job`)
- **Files / symbols**:
  - `core/job/WeatherNotificationBackgroundTask.kt` — same 3-repository → use-case replacement as Step 34
- **Skill args / inputs**: none
- **Verification**: `./gradlew build` succeeds (this file is iosMain-only, so `assembleDebug` alone
  won't compile it); no direct repository import remains.

<!-- ---- Repo-wide verification + docs ---- -->

### Step 38 — Full build + no-direct-repository-access verification [verify]

- **What to check**: `./gradlew build` (Android + JVM/desktop); then run both greps from
  proposal.md AC-2 (`domain\.repository\.` and
  `core\.preferences\.repository\.PreferenceRepository`) over `composeApp/src` and confirm every
  match is inside `data/repository/*Impl`, `core/preferences/repository/PreferenceRepositoryImpl.kt`,
  the new `domain/usecase/*Impl` classes, or the DI files that bind interface → impl.
- **Pass criteria**: build green; both greps' output contains no ViewModel, Worker, widget,
  background task, or Screen file.

### Step 39 — Update CLAUDE.md's Architecture section [implement]

- **Skill**: direct edits
- **Area(s)**: docs
- **Files / symbols**:
  - `CLAUDE.md` — Architecture section: replace "ViewModels depend on `domain/repository` interfaces
    ... (Dependency Inversion)" framing with the consumer → `domain/usecase/<feature>/XxxUseCase` →
    `domain/repository/*Interface` → `data/repository/<feature>/*Impl` flow; add a short bullet
    under the existing `domain/repository/*` bullet describing `domain/usecase/*` and
    `core/usecase/UseCase.kt`; add "consumers inject the abstract `XxxUseCase`, never the repository
    interface or `Impl`, directly" to the SOLID/DIP bullet
- **Skill args / inputs**: none
- **Verification**: section reads consistently with the new architecture; no remaining reference to
  ViewModels injecting repository interfaces directly.

### Step 40 — Update /new-feature's scaffolding to generate a use case, if needed [implement, conditional]

- **Condition**: Step 1 result: new-feature-injects-repo-directly = yes
- **Skill**: direct edits
- **Area(s)**: `.claude/skills/new-feature/`
- **Files / symbols**:
  - The specific template file Step 1 identified — add a use-case scaffolding step (base + Impl
    pair, following this spec's pattern) ahead of the ViewModel-generation step, and change the
    generated ViewModel constructor to inject the scaffolded use case instead of the repository
    interface
- **Why**: keeps newly scaffolded features starting from the pattern this spec establishes, instead
  of needing a follow-up migration like this one.
- **Verification**: the skill's own self-consistency (its SKILL.md example/template) reflects use-case
  injection, not direct repository injection.

## Dependencies

- Steps 3–24 (use-case creation) can run in any relative order among themselves, but all must
  complete before Step 25 (Koin registration references every `XxxUseCaseImpl`).
- Step 25 depends on Steps 3–24; Step 26 depends on Step 25; Step 27 (verify) depends on Step 26.
- Steps 28–37 (consumer migration) each depend on Step 27 passing (the use-case layer must exist and
  compile before any consumer can be migrated to it) and on the specific use cases they inject
  existing (e.g. Step 28 depends on Steps 3, 5, 6–10, 12–16 — the location/map-layer/
  user-custom-location/weather use cases `AddCityViewModel` calls).
- Step 32 depends on Step 31 (the Screen's fix only makes sense once `PreferenceViewModel` exposes
  `savePreference` as the replacement for the removed public `preferenceRepository`).
- Step 38 depends on Steps 28–37 all being complete.
- Step 40 depends on Step 1's recorded result.

## Out-of-band actions

- See proposal.md "Out-of-band actions" — manual runs on Android/Desktop, manual/triggered runs of
  each Android background path, and an Xcode build for iOS, all post-migration.

## Rollback

Every step is a local, uncommitted-until-handoff file change. If a step fails partway (a build
break Step 27/38 catches, or a mismatched `Params`/call-site edit), `git restore` the specific files
listed in that step's "Files / symbols" and retry — there's no migration tool, generated schema, or
test suite involved, so a partial application never needs anything beyond reverting the files it
touched. Because Steps 2–26 are purely additive (no existing file is modified until Step 28), a
failure anywhere before Step 28 can be rolled back by simply deleting the new files it created,
without risk to any existing consumer.
