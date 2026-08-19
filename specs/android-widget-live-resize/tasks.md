---
spec_id: android-widget-live-resize
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

# Tasks: Live drag-resize responsiveness for Android home-screen widgets

## Implementation

- [x] **Step 1** — Determine SizeMode.Responsive placement and candidate DpSize set
  - [x] `BaseWeatherGlanceWidget.kt` confirmed to have no existing `sizeMode` override
  - [x] `override_scope` (shared-base vs. per-subclass) decided — `shared-base`
  - [x] Candidate `DpSize` set chosen, cross-referenced against `resolveWidgetSizeClass` thresholds and each variant's declared min size — `{128x48, 196x48, 256x60, 256x120}`
  - [x] `Step 1 result: override_scope = shared-base` marker recorded (plan.md)
  - [x] OQ-2 answered in proposal.md
- [x] **Step 2** — Override sizeMode to enable live-resize
  - [x] Code change applied — `WIDGET_RESPONSIVE_SIZES` + `override val sizeMode` added to `BaseWeatherGlanceWidget.kt`
  - [x] Build green (`./gradlew :composeApp:assembleDebug`) — BUILD SUCCESSFUL in 24s (2026-08-18)
  - [x] Verification met (per plan.md) — `sizeMode` resolves to `SizeMode.Responsive(WIDGET_RESPONSIVE_SIZES)` in `BaseWeatherGlanceWidget` (inherited by all 5 subclasses)
- [x] **Step 3** — Add content-transform dispatcher for the 3 plain widgets
  - [x] `AdaptiveWeatherWidgetContent` added to `WidgetComponents.kt` — dispatches on `LocalSize.current` directly (not `rememberWidgetSizeClass()`, see Step 1 finding in plan.md)
  - [x] `SmallWeatherGlanceWidget.kt`/`MediumWeatherGlanceWidget.kt`/`LargeWeatherGlanceWidget.kt` wired to call the dispatcher
  - [x] Clock widget classes confirmed unchanged (still call their own clock content composable directly)
  - [x] Build green — BUILD SUCCESSFUL in 24s (2026-08-18, same run as Step 2)
  - [x] Verification met (per plan.md)
- [x] **Step 3b** — Scale the clock graphic itself (added mid-implementation, user-requested)
  - [x] `WidgetTypography` gained `clockDigitalDateSize`/`clockDigitalTimeSize`, populated on all 3 instances
  - [x] `WeatherWithDigitalClockContent` applies both via `RemoteViews.setTextViewTextSize(...)`
  - [x] `analogClockLayoutRes(sizeClass)` added; `WeatherWithAnalogClockContent` selects layout by `rememberWidgetSizeClass()`
  - [x] `widget_rtc_analog_clock_medium.xml`/`_large.xml` added (85dp/110dp `AnalogClock` view, 13sp/15sp date); original small file untouched
  - [x] Build green — BUILD SUCCESSFUL in 5s (2026-08-18)
  - [x] Verification met (per plan.md)
  - [x] **Correction (2026-08-18, user-caught)**: `AnalogClock` only shrinks dial/hands to fit, never grows past their own drawable-declared intrinsic size — growing the View's layout size alone did nothing visible. Added 8 new drawables (`clock_dial_{medium,large}.xml`, `clock_hour_hand_{medium,large}.xml`, `clock_minute_hand_{medium,large}.xml`, `clock_second_hand_{medium,large}.xml`, intrinsic size raised to 85dp/110dp) and repointed the 2 layout files to them
  - [x] Build green — BUILD SUCCESSFUL in 14s (2026-08-18)
  - [x] Verification met (per plan.md)
- [x] **Step 4** — Manual drag-resize verification across all widget variants
  - [x] Plain variants (Small/Medium/Large): composable switch confirmed at each size-class boundary, both directions, English
  - [x] Plain variants: same, Spanish
  - [x] Clock variants: clock content confirmed retained at every dragged size, no switch to non-clock layout, both locales
  - [x] Clock variants: clock graphic itself confirmed to visibly rescale (digital text size grows/shrinks; analog dial/hands grow/shrink with no reset/jump/flicker on the layout swap)
  - [x] Typography/icon/spacing confirmed to rescale live within whichever composable is active
  - [x] No text clipping/overlap at any dragged size, any variant, either locale
  - [x] Clock variants checked for flicker/jank from 1s refresh during resize
  - [x] Original/default placed size still renders correctly for all 5 variants (no regression)
  - [x] Pass criteria met (per plan.md) — confirmed by user, 2026-08-18 ("si, ya probe todo")
- [x] **Step 5** — Build verification
  - [x] `./gradlew :composeApp:assembleDebug` succeeds — BUILD SUCCESSFUL in 14s (2026-08-18, same run as Step 3b's final build)

## Pre-handoff checks

- [x] Full build green (`./gradlew build` — covers Android + JVM/desktop targets; run `./gradlew :composeApp:assembleDebug` at minimum if only Android was touched) — `./gradlew build` red on `Task#dependsOn`/implicit-dependency validation between KSP/iOS compile tasks (`kspCommonMainKotlinMetadata` vs. `kspReleaseKotlinAndroid`/`compileKotlinIosArm64`/etc.) — this is the **pre-existing** defect documented in `mem:gradle_ksp_multitarget_build_quirk` (previously confirmed red on clean `develop` via `git stash`), unrelated to this spec (androidMain-only, no KSP/Room touched). `./gradlew :composeApp:assembleDebug` — the actual local gate per `mem:gradle_ksp_multitarget_build_quirk`/root `CLAUDE.md` — BUILD SUCCESSFUL 3× (Steps 2/3, 3b, 3b-correction)
- [x] iOS build manually verified via Xcode if any `iosMain`/`iosApp/` file changed (no headless build path exists — see `.specs/EXTERNAL_SKILLS.md`) — N/A, no `iosMain`/`iosApp/` file touched
- [x] No new logs/prints touch the WeatherAPI key or any other credential — grep of the full diff for `.specs/config.json` `verification.secret_log_keywords` returns only false-positive comment matches on the word "tokens" (design tokens), no actual credential/log code
- [x] Every touched commonMain `expect` has a matching `actual` in every affected source set (manual review — see `.specs/config.json` `architecture.expect_actual_parity_required`) — N/A, no `expect`/`actual` touched (`expect_actual_touched: no`)
- [x] Any touched user-facing string shown by both Compose UI and native iOS code is updated in both `composeResources` and `iosApp/*.strings` (`architecture.dual_localization_required`) — N/A, no user-facing string changed (`localization_touched: no`)
- [x] No automated-test checkbox invented — this repo has zero test source sets; verification is build-green + manual run only
- [x] Acceptance criteria from proposal.md §8 satisfied — AC-1 through AC-7 satisfied; AC-2/AC-3/AC-4/AC-6 confirmed via Step 4 manual verification (user-confirmed, 2026-08-18)
- [x] proposal.md frontmatter `blockers: []` (empty)
- [x] proposal.md frontmatter `depends_on:` either `[]` OR every listed ID has a folder under `specs/_archive/` — `[]`
- [x] All §9 OQs resolved or marked out-of-band — OQ-1 and OQ-3 resolved during planning/mid-implementation (both updated live per user clarification); OQ-2 resolved (Step 1); OQ-4 folded into Step 4 manual verification
- [x] No plan.md step retains `_(skeleton)_` (each expanded with concrete sub-checks)

## Handoff

- [x] Branch created (`feature/android-widget-live-resize`)
- [x] `/commit` executed — `b436525`
- [ ] Branch pushed *(see /spec-finalize)*
- [ ] PR opened against `develop` *(see /spec-finalize)*
- [ ] Spec folder archived to `specs/_archive/android-widget-live-resize/` *(see /spec-finalize)*
- [ ] Reusable-knowledge candidates from `decisions.md` proposed *(see /spec-finalize)*
