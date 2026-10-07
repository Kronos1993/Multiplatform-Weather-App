---
spec_id: battery-optimization-exemption-prompt
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

# Tasks: Solicitar exención de optimización de batería para todos los dispositivos Android

## Implementation

- [x] **Step 1** — Map the Home permission flow and preference keys
  - [x] Insertion point recorded in proposal.md §4
  - [x] OQ-3 storage-type note recorded
  - [x] Settings row component chosen
- [x] **Step 2** — Add the commonMain helper contract
  - [x] `IBatteryOptimizationHelper` + `expect class BatteryOptimizationHelper` created
- [x] **Step 3** — Add the Android and iOS actuals
  - [x] Android `actual` (PowerManager, fallback chain, OEM list, guarded launches)
  - [x] iOS no-op `actual`
  - [x] Build green (`./gradlew :composeApp:assembleDebug`)
- [x] **Step 4** — Register the helper in Koin
  - [x] Binding in `Module.android.kt` and `Module.ios.kt`
  - [x] Build green
- [x] **Step 5** — Add strings and the preference keys
  - [x] Strings in `values` + `values-es`
  - [x] Keys in `preference_key.xml`
  - [x] Settings row strings in `preference_strings.xml` (`values` + `values-es`)
  - [x] Build green
- [x] **Step 6** — Add the state holder, the dialog, and the HomeScreen wiring
  - [x] `BatteryOptimizationViewModel` + `viewModelOf` registration
  - [x] `BatteryOptimizationDialog` composable
  - [x] `HomeScreen` shows the dialog only after `PermissionFlow.Completed`
  - [x] Build green
- [x] **Step 7** — Add the Settings entry
  - [x] Row in `SettingsScreen`, guarded by `isSupported()`
  - [x] Tapping opens the dialog regardless of dont-ask/snooze
  - [x] Build green
- [x] **Step 8** — Build, lint, parity check, and manual run
  - [x] `assembleDebug` green
  - [x] `ktlintCheck` clean for touched files
  - [x] expect/actual parity confirmed (androidMain + iosMain)
  - [x] AC-1..AC-6 and AC-9 observed on the emulator (maintainer, on a physical Samsung/One UI device, 2026-10-07)

## Pre-handoff checks

- [x] Full build green (`./gradlew build` — covers Android + JVM/desktop targets; run `./gradlew :composeApp:assembleDebug` at minimum if only Android was touched)
  - `assembleDebug` green. Aggregate `./gradlew build` red (pre-existing baseline: KSP task-graph defect + iOS link OOM, see decisions.md / `mem:gradle_ksp_multitarget_build_quirk`)
- [x] iOS build manually verified via Xcode if any `iosMain`/`iosApp/` file changed (no headless build path exists — see `.specs/EXTERNAL_SKILLS.md`)
  - verified by the maintainer in Xcode, 2026-10-07
- [x] No new logs/prints touch the WeatherAPI key or any other credential
- [x] Every touched commonMain `expect` has a matching `actual` in every affected source set (manual review — see `.specs/config.json` `architecture.expect_actual_parity_required`)
- [x] Any touched user-facing string shown by both Compose UI and native iOS code is updated in both `composeResources` and `iosApp/*.strings` (`architecture.dual_localization_required`)
  - N/A: the new strings are Compose-only; `values` + `values-es` updated
- [x] No automated-test checkbox invented — this repo has zero test source sets (confirmed in `CLAUDE.md`); verification is build-green + manual run only
- [x] Acceptance criteria from proposal.md §8 satisfied
- [x] proposal.md frontmatter `blockers: []` (empty)
- [x] proposal.md frontmatter `depends_on:` either `[]` OR every listed ID has a folder under `specs/_archive/`
- [x] All §9 OQs resolved or marked out-of-band
- [x] No plan.md step retains `_(skeleton)_` (each expanded with concrete sub-checks)

## Handoff

- [x] Branch created (`feature/battery-optimization-exemption-prompt`)
- [x] `/commit` executed
- [ ] Branch pushed *(see /spec-finalize)*
- [ ] PR opened against `develop` *(see /spec-finalize)*
- [ ] Spec folder archived to `specs/_archive/battery-optimization-exemption-prompt/` *(see /spec-finalize)*
- [ ] Reusable-knowledge candidates from `decisions.md` proposed *(see /spec-finalize)*
