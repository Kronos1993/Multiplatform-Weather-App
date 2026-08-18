---
spec_id: android-widget-responsive-sizing
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

# Tasks: Complete size-responsive typography, icon, spacing, and style system for Android home-screen widgets

## Implementation

- [x] **Step 1** — Check widget color/style tokens against the app's design system
  - [x] `WidgetTheme.kt` read; color source (hardcoded vs. shared token) identified
  - [x] `Step 1 result: theme_divergence = no` marker recorded
  - [x] OQ-2 answered in proposal.md
- [x] **Step 2** — Consolidate the duplicated size-class resolver
  - [x] Code change applied
  - [x] Build green (`./gradlew :composeApp:assembleDebug`) — BUILD SUCCESSFUL (confirmed via Step 8)
  - [x] Verification met (per plan.md) — `resolveWidgetSizeClass` exists; both `rememberWidgetTypography`/`rememberWidgetSizeClass` delegate to it; no independent breakpoint literals remain in either
- [x] **Step 3** — Add size-class-aware spacing/density tokens
  - [x] Code change applied — `WidgetSpacing` data class (`contentPadding`, `iconTextGap`, `microGap`, `sectionGap`, `itemSpacing`) + `SmallSpacing`/`MediumSpacing`/`LargeSpacing` + `rememberWidgetSpacing()` added to `WidgetTheme.kt`
  - [x] Build green — BUILD SUCCESSFUL (confirmed via Step 8)
  - [x] Verification met — new spacing fields/tokens exist on all three size instances
- [x] **Step 4** — Replace hardcoded spacing literals in the content composables
  - [x] Code change applied — `WeatherWidgetBackground`, `SmallWeatherWidgetContent`, `MediumWeatherWidgetContent`, `LargeWeatherWidgetContent`, `WeatherWithAnalogClockContent`, `WeatherWithDigitalClockContent`, `LocationRow`, `WeatherDetailRow`, `ForecastDayCompact`, `ForecastDayFull` now use `rememberWidgetSpacing()` tokens
  - [x] Build green — BUILD SUCCESSFUL (confirmed via Step 8)
  - [x] Verification met — grep for bare `.dp` literals in `Spacer(...)`/`.padding(...)` in these composables returns none (only `LoadingWidget`/`WeatherWidgetErrorContent` remain, Step 5's scope)
- [x] **Step 5** — Extend size-aware tokens to Loading and Error states
  - [x] Code change applied — `LoadingWidget` and `WeatherWidgetErrorContent` now call `rememberWidgetTypography()`/`rememberWidgetSpacing()` (icon → `t.weatherIconSize`, text → `t.conditionSize`, padding/gap → `s.contentPadding`/`s.sectionGap`), reusing the surrounding content's tokens per OQ-3's default
  - [x] Build green — BUILD SUCCESSFUL (confirmed via Step 8)
  - [x] Verification met — `LoadingWidget`/`WeatherWidgetErrorContent` no longer contain the fixed `44.dp`/`13.sp`/`36.dp`/`16.dp` literals (see decisions.md for a pre-existing double-background/padding finding noted, not fixed — out of scope)
- [x] **Step 6** — Align widget color/style tokens with the app theme _(conditional — only if Step 1 result: theme_divergence = yes)_
  - [x] Condition evaluated — `theme_divergence = no` (Step 1)
  - [x] Code change applied (or skipped: reason recorded) — skipped: no divergence to fix, colors are an intentional widget-specific design (see proposal.md OQ-2 resolution)
  - [x] Build green — N/A, no code touched
  - [x] Verification met — N/A
- [x] **Step 7** — Manual visual verification across all widget variants
  - [x] All 5 variants placed on emulator/device in English — Samsung S20FE, phone
  - [x] All 5 variants placed on emulator/device in Spanish — Samsung S20FE, phone
  - [x] Loading state verified — confirmed clean
  - [x] Error state verified — confirmed clean
  - [x] Pass criteria met (per plan.md) — no clipping/overlap on phone (S20FE, 5x6 grid) in either locale, all 5 variants, Loading + Error states. **New finding, out of this spec's scope**: on a Samsung Tab A9 (8x6 grid, landscape), the analog clock widget's date row is clipped at the bottom — confirmed via screenshots. Root cause is the tablet's launcher grid giving the widget's host container less height than the widget's own declared minimum in that layout, not a token-calibration issue in this spec's code (same composable/tokens render correctly on phone). Tracked for a follow-up spec (adjacent to OQ-1) — see decisions.md.
- [x] **Step 8** — Build verification
  - [x] `./gradlew :composeApp:assembleDebug` succeeds — BUILD SUCCESSFUL in 31s (2026-08-18)

## Pre-handoff checks

- [x] Full build green (`./gradlew build` — covers Android + JVM/desktop targets; run `./gradlew :composeApp:assembleDebug` at minimum if only Android was touched) — only androidMain touched; `./gradlew :composeApp:assembleDebug` BUILD SUCCESSFUL (Step 8)
- [x] iOS build manually verified via Xcode if any `iosMain`/`iosApp/` file changed (no headless build path exists — see `.specs/EXTERNAL_SKILLS.md`) — N/A, no `iosMain`/`iosApp/` file changed
- [x] No new logs/prints touch the WeatherAPI key or any other credential — grep of both changed files for `.specs/config.json` `verification.secret_log_keywords` returns only false-positive comment matches on the word "Tokens" (design tokens), no actual credential/log code
- [x] Every touched commonMain `expect` has a matching `actual` in every affected source set (manual review — see `.specs/config.json` `architecture.expect_actual_parity_required`) — N/A, no `expect`/`actual` touched (`expect_actual_touched: no`)
- [x] Any touched user-facing string shown by both Compose UI and native iOS code is updated in both `composeResources` and `iosApp/*.strings` (`architecture.dual_localization_required`) — N/A, no user-facing string changed (`localization_touched: no`)
- [x] No automated-test checkbox invented — this repo has zero test source sets (confirmed in `CLAUDE.md`); verification is build-green + manual run only — confirmed N/A, no test step invented
- [x] Acceptance criteria from proposal.md §8 satisfied — AC-1 through AC-6 satisfied; AC-5 confirmed via Step 7 manual verification on Samsung S20FE (phone), all 5 variants, en/es, Loading + Error states, no clipping/overlap. A tablet-only clipping issue on the analog clock widget was found (Samsung Tab A9, 8x6 landscape grid) but judged out of this spec's scope — see Step 7 note and decisions.md; tracked as a follow-up spec
- [x] proposal.md frontmatter `blockers: []` (empty)
- [x] proposal.md frontmatter `depends_on:` either `[]` OR every listed ID has a folder under `specs/_archive/` — `[]`
- [x] All §9 OQs resolved or marked out-of-band — OQ-1 out-of-band (follow-up, not blocking), OQ-2 resolved (Step 1), OQ-3 resolved (Step 5)
- [x] No plan.md step retains `_(skeleton)_` (each expanded with concrete sub-checks)

## Handoff

- [ ] Branch created (`feature/android-widget-responsive-sizing`)
- [ ] `/commit` executed
- [ ] Branch pushed
- [ ] PR opened against `develop`
- [ ] Spec folder archived to `specs/_archive/android-widget-responsive-sizing/`
- [ ] Reusable-knowledge candidates from `decisions.md` proposed
