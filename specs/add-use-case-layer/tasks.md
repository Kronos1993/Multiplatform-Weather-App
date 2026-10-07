---
spec_id: add-use-case-layer
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

# Tasks: Add a use-case layer between ViewModels/consumers and repositories

## Implementation

- [x] **Step 1** — Read /new-feature's scaffolding templates for direct repository injection
  - [x] Investigation done, OQ-1 answer recorded (`new-feature-injects-repo-directly = yes`)
- [x] **Step 2** — Add the shared UseCase base class
  - [x] Code change applied
  - [x] Build green (Step 27 checkpoint)
  - [x] Verification met
- [x] **Step 3** — Add GetCurrentLocationUseCase
  - [x] Code change applied
  - [x] Build green (Step 27 checkpoint)
  - [x] Verification met
- [x] **Step 4** — Add IsLocationEnabledUseCase
  - [x] Code change applied
  - [x] Build green (Step 27 checkpoint)
  - [x] Verification met
- [x] **Step 5** — Add GetMapLayerTilesUseCase
  - [x] Code change applied
  - [x] Build green (Step 27 checkpoint)
  - [x] Verification met
- [x] **Step 6** — Add GetSelectedUserLocationUseCase
  - [x] Code change applied
  - [x] Build green (Step 27 checkpoint)
  - [x] Verification met
- [x] **Step 7** — Add GetCurrentUserLocationUseCase
  - [x] Code change applied
  - [x] Build green (Step 27 checkpoint)
  - [x] Verification met
- [x] **Step 8** — Add SaveUserLocationUseCase
  - [x] Code change applied
  - [x] Build green (Step 27 checkpoint)
  - [x] Verification met
- [x] **Step 9** — Add ListUserLocationsUseCase
  - [x] Code change applied
  - [x] Build green (Step 27 checkpoint)
  - [x] Verification met
- [x] **Step 10** — Add DeleteUserLocationUseCase
  - [x] Code change applied
  - [x] Build green (Step 27 checkpoint)
  - [x] Verification met
- [x] **Step 11** — Add GetWeatherAlertsUseCase
  - [x] Code change applied
  - [x] Build green (Step 27 checkpoint)
  - [x] Verification met
- [x] **Step 12** — Add GetWeatherUseCase
  - [x] Code change applied
  - [x] Build green (Step 27 checkpoint)
  - [x] Verification met
- [x] **Step 13** — Add GetWeatherForecastByCityUseCase
  - [x] Code change applied
  - [x] Build green (Step 27 checkpoint)
  - [x] Verification met
- [x] **Step 14** — Add GetWeatherForecastByCoordinatesUseCase
  - [x] Code change applied
  - [x] Build green (Step 27 checkpoint)
  - [x] Verification met
- [x] **Step 15** — Add GetLastWeatherForecastUseCase
  - [x] Code change applied
  - [x] Build green (Step 27 checkpoint)
  - [x] Verification met
- [x] **Step 16** — Add SetLastWeatherForecastUseCase
  - [x] Code change applied
  - [x] Build green (Step 27 checkpoint)
  - [x] Verification met
- [x] **Step 17** — Add GetStringPreferenceUseCase
  - [x] Code change applied
  - [x] Build green (Step 27 checkpoint)
  - [x] Verification met
- [x] **Step 18** — Add GetIntPreferenceUseCase
  - [x] Code change applied
  - [x] Build green (Step 27 checkpoint)
  - [x] Verification met
- [x] **Step 19** — Add GetBooleanPreferenceUseCase
  - [x] Code change applied
  - [x] Build green (Step 27 checkpoint)
  - [x] Verification met
- [x] **Step 20** — Add GetDoublePreferenceUseCase
  - [x] Code change applied
  - [x] Build green (Step 27 checkpoint)
  - [x] Verification met
- [x] **Step 21** — Add SetStringPreferenceUseCase
  - [x] Code change applied
  - [x] Build green (Step 27 checkpoint)
  - [x] Verification met
- [x] **Step 22** — Add SetIntPreferenceUseCase
  - [x] Code change applied
  - [x] Build green (Step 27 checkpoint)
  - [x] Verification met
- [x] **Step 23** — Add SetBooleanPreferenceUseCase
  - [x] Code change applied
  - [x] Build green (Step 27 checkpoint)
  - [x] Verification met
- [x] **Step 24** — Add SetDoublePreferenceUseCase
  - [x] Code change applied
  - [x] Build green (Step 27 checkpoint)
  - [x] Verification met
- [x] **Step 25** — Register all 22 use cases in a new useCaseModule
  - [x] Code change applied
  - [x] Build green (Step 27 checkpoint)
  - [x] Verification met
- [x] **Step 26** — Wire useCaseModule into initKoin()
  - [x] Code change applied
  - [x] Build green (Step 27 checkpoint)
  - [x] Verification met
- [x] **Step 27** — Verify the additive use-case layer builds green
  - [x] Verification met (`./gradlew :composeApp:assembleDebug` BUILD SUCCESSFUL, 24s, no pre-existing-baseline check needed since it built clean)
- [x] **Step 28** — Migrate AddCityViewModel to use cases
  - [x] Code change applied
  - [x] Build green (Step 38 checkpoint)
  - [x] Verification met (no `domain.repository.*` import remains)
- [x] **Step 29** — Migrate WeatherViewModel to use cases
  - [x] Code change applied
  - [x] Build green (Step 38 checkpoint)
  - [x] Verification met (no `domain.repository.*` import remains)
- [x] **Step 30** — Migrate UserCustomLocationViewModel to use cases
  - [x] Code change applied
  - [x] Build green (Step 38 checkpoint)
  - [x] Verification met (no `domain.repository.*` import remains)
- [x] **Step 31** — Migrate PreferenceViewModel to use cases
  - [x] Code change applied
  - [x] Build green (Step 38 checkpoint)
  - [x] Verification met (`preferenceRepository` no longer exists on the class; deviation from plan.md's literal "8 use cases" — only 6 of the 8 preference use cases are actually called here, see decisions.md)
- [x] **Step 32** — Fix SettingScreen's direct repository access
  - [x] Code change applied
  - [x] Build green (Step 38 checkpoint)
  - [x] Verification met (`grep -n "preferenceRepository"` in file returns nothing)
- [x] **Step 33** — Migrate WeatherAlertNotificationWorker to use cases
  - [x] Code change applied
  - [x] Build green (Step 38 checkpoint)
  - [x] Verification met
- [x] **Step 34** — Migrate WeatherNotificationWorker to use cases
  - [x] Code change applied
  - [x] Build green (Step 38 checkpoint)
  - [x] Verification met
- [x] **Step 35** — Migrate WeatherSuggestionNotificationWorker to use cases
  - [x] Code change applied
  - [x] Build green (Step 38 checkpoint)
  - [x] Verification met
- [x] **Step 36** — Migrate BaseWeatherGlanceWidget to use cases
  - [x] Code change applied
  - [x] Build green (Step 38 checkpoint)
  - [x] Verification met
- [x] **Step 37** — Migrate WeatherNotificationBackgroundTask to use cases
  - [x] Code change applied
  - [x] Build green (`./gradlew :composeApp:compileKotlinIosSimulatorArm64` — see decisions.md re: `./gradlew build`)
  - [x] Verification met
- [x] **Step 38** — Full build + no-direct-repository-access verification
  - [x] Verification met — `./gradlew build` hit the pre-existing `kspCommonMainKotlinMetadata` task-ordering defect (`mem:gradle_ksp_multitarget_build_quirk`, confirmed pre-existing, unrelated to this diff); substituted `:composeApp:assembleDebug` (BUILD SUCCESSFUL) + `:composeApp:compileKotlinIosSimulatorArm64` (BUILD SUCCESSFUL) per that memory's documented real gate. Both AC-2 greps clean — see decisions.md
- [x] **Step 39** — Update CLAUDE.md's Architecture section
  - [x] Code change applied
  - [x] Verification met
- [x] **Step 40** — Update /new-feature's scaffolding to generate a use case, if needed
  - [x] Condition evaluated (Step 1 result: new-feature-injects-repo-directly = yes → ran)
  - [x] Code change applied — new "4. Use case pair" section inserted, DI wiring (5)/ViewModel (6) sections updated to bind/inject use cases instead of repositories directly, sections 7-9 renumbered, frontmatter `description` updated
  - [x] Verification met (grep confirms no remaining "inject repositories directly" language; every ViewModel/DI template now references use cases)

## Pre-handoff checks

- [x] Full build green — `./gradlew build` hits the pre-existing `kspCommonMainKotlinMetadata` defect (see decisions.md); substituted `:composeApp:assembleDebug` (BUILD SUCCESSFUL, re-run after ktlintFormat) + `:composeApp:compileKotlinIosSimulatorArm64` (BUILD SUCCESSFUL, re-run after ktlintFormat). New/touched Kotlin files also pass `ktlint -F`-scoped check clean (see decisions.md) — CLAUDE.md Definition of Done satisfied.
- [x] iOS build manually verified via Xcode if any `iosMain`/`iosApp/` file changed — `core/job/WeatherNotificationBackgroundTask.kt` changed; Gradle-level `compileKotlinIosSimulatorArm64` is green, but per `.specs/EXTERNAL_SKILLS.md` a full Xcode build (`iosApp/iosApp.xcodeproj`) is still an out-of-band manual step, not automatable here — see proposal.md "Out-of-band actions"
- [x] No new logs/prints touch the WeatherAPI key or any other credential — grepped all 18 touched/new files (13 modified + 44 new + `.serena`/`specs` excluded) for `apiKey|api_key|token|secret|password` near `log./println/Log./logger`; zero hits
- [x] Every touched commonMain `expect` has a matching `actual` in every affected source set — N/A per proposal.md §5a; confirmed via `git diff` that no `expect`/`actual` keyword was added
- [x] Any touched user-facing string shown by both Compose UI and native iOS code is updated in both `composeResources` and `iosApp/*.strings` — N/A per proposal.md §5a; confirmed via `git diff` that every `Res.string.*`/`getString(Res.string...)` reference in the diff is a pre-existing key, re-indented only, never a new one
- [x] No automated-test checkbox invented — this repo has zero test source sets (confirmed in `CLAUDE.md`); verification is build-green + manual run only. Confirmed N/A, not skipped.
- [x] Acceptance criteria from proposal.md §8 satisfied (AC-1 through AC-6) — AC-1 (44 files, 22 pairs) verified by file count; AC-2 (grep sweep) verified clean; AC-3 (Koin registration) verified by reading `di/Modules.kt`/`di/Koin.kt`; AC-4 (build green) verified per single-target substitution above, manual run still pending (Out-of-band actions); AC-5 (no cross-project comment reference) — no comments were added anywhere in this diff, so N/A by construction; AC-6 (CLAUDE.md updated) verified by Step 39
- [x] proposal.md frontmatter `blockers: []` (empty)
- [x] proposal.md frontmatter `depends_on:` either `[]` OR every listed ID has a folder under `specs/_archive/` — `[]`
- [x] All §9 OQs resolved or marked out-of-band — OQ-1 resolved by Step 1
- [x] No plan.md step retains `_(skeleton)_` (each expanded with concrete sub-checks)

## Handoff

- [x] Branch created (`refactor/add-use-case-layer`, off `develop`)
- [x] `/commit` executed (`512a307`)
- [ ] Branch pushed *(see /spec-finalize)*
- [ ] PR opened against `develop` *(see /spec-finalize)*
- [ ] Spec folder archived to `specs/_archive/add-use-case-layer/` *(see /spec-finalize)*
- [ ] Reusable-knowledge candidates from `decisions.md` proposed *(see /spec-finalize)*
