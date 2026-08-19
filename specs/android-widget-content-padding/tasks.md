---
spec_id: android-widget-content-padding
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

# Tasks: Increase Android widget content padding so text doesn't sit flush against the widget's edges

## Implementation

- [x] **Step 1** — Enumerate all consumers of `WidgetSpacing.contentPadding`
  - [x] Code change applied (N/A — investigate step, no code change)
  - [x] Build green (N/A — no code touched this step)
  - [x] Verification met (per plan.md) — exactly 2 consumers found via grep sweep of `androidMain/kotlin`: `WidgetComponents.kt:51` (`WeatherWidgetBackground`) and `WidgetComponents.kt:488` (`WeatherWidgetErrorContent`, single-arg `.padding(s.contentPadding)`). No other file references the field.
- [x] **Step 2** — Split and increase the content-padding tokens
  - [x] Code change applied
  - [x] Build green — covered by Step 3's `./gradlew :composeApp:assembleDebug` (exit 0)
  - [x] Verification met (per plan.md) — `WidgetSpacing` now declares `contentPaddingHorizontal`/`contentPaddingVertical`; `SmallSpacing`/`MediumSpacing`/`LargeSpacing` updated to 12/8, 14/12, 16/14 dp respectively
- [x] **Step 3** — Update token consumers to the new field names
  - [x] Code change applied
  - [x] Build green — `./gradlew :composeApp:assembleDebug` exit 0, `compileDebugKotlinAndroid` ran (not UP-TO-DATE)
  - [x] Verification met (per plan.md) — no unresolved-reference errors; grep confirms zero remaining `contentPadding` (old field) references
- [x] **Step 3b** — Fix vertical centering that cancels the padding token — **REVERTED** (user reported it looked worse; see `decisions.md`)
  - [x] Code change applied, then reverted — all 5 composables restored to original `CenterVertically`/`CenterHorizontally`/`Start` alignment
  - [x] Build green (post-revert) — `./gradlew :composeApp:assembleDebug` exit 0 (BUILD SUCCESSFUL)
  - [x] Verification met (per plan.md, N/A — step reverted, superseded by Step 3c)
- [x] **Step 3c** — Reduce the widget background's corner radius
  - [x] Code change applied — `bg_widget_glass.xml`: `android:radius` `20dp → 12dp` (option confirmed with user via AskUserQuestion)
  - [x] Build green — `./gradlew :composeApp:assembleDebug` exit 0 (BUILD SUCCESSFUL)
  - [x] Verification met (per plan.md) — root cause (20dp radius ≈ half the 48dp Small widget's height, eating the padding's visual effect at the corner) recorded in `decisions.md`
- [x] **Step 5** — Clock widgets: allow 2-line condition text when resized taller (out-of-scope add-on, done at user's request)
  - [x] Code change applied — `WeatherWithAnalogClockContent`/`WeatherWithDigitalClockContent`: `val size = LocalSize.current`; condition `Text`'s `maxLines` now `if (size.height >= 100.dp) 2 else 1`
  - [x] Build green — `./gradlew :composeApp:assembleDebug` BUILD SUCCESSFUL
  - [x] Verification met (per decisions.md) — raw `LocalSize` comparison, consistent with `AdaptiveWeatherWidgetContent`'s existing pattern
- [x] **Step 6** — Fix Medium/Large widgets not shrinking back down (bug surfaced by user while testing)
  - [x] Code change applied — added `android:minResizeWidth="128dp"` / `android:minResizeHeight="48dp"` to `widget_provider_info_large.xml` and `widget_provider_info_medium.xml`
  - [x] Build green — `./gradlew :composeApp:assembleDebug` BUILD SUCCESSFUL
  - [x] Verification met (per decisions.md) — root cause (unset minResize defaults to minWidth/minHeight, blocking shrink) confirmed against the `AppWidgetProviderInfo` contract
- [x] **Step 4** — Manually verify all 5 widget variants on-device
  - [x] Code change applied (N/A — verify step, no code change)
  - [x] Build green — debug APK rebuilt after Steps 3c/5/6 (`composeApp/build/outputs/apk/debug/composeApp-debug.apk`)
  - [x] Verification met (per plan.md) — user confirmed on-device (round 4): padding/corner-radius reads correctly, clock widgets show 2 lines of condition text when resized tall enough, Medium/Large widgets now shrink and transform content correctly

## Pre-handoff checks

- [x] Full build green (`./gradlew build` — covers Android + JVM/desktop targets; run `./gradlew :composeApp:assembleDebug` at minimum if only Android was touched) — only `androidMain` touched, so `./gradlew :composeApp:assembleDebug` is sufficient per `verification.build_matrix_at_pre_handoff` doc; last run (Step 6 rebuild) was BUILD SUCCESSFUL, inherited rather than re-run
- [x] iOS build manually verified via Xcode if any `iosMain`/`iosApp/` file changed (N/A — no `iosMain`/`iosApp/` file touched)
- [x] No new logs/prints touch the WeatherAPI key or any other credential — grepped the full diff for secret keywords, zero real hits (one harmless comment containing the word "Tokens" in Spanish, referring to spacing tokens)
- [x] Every touched commonMain `expect` has a matching `actual` in every affected source set (N/A — no `expect`/`actual` touched; all changes are `androidMain`-only)
- [x] Any touched user-facing string shown by both Compose UI and native iOS code is updated in both `composeResources` and `iosApp/*.strings` (N/A — no user-facing string touched)
- [x] No automated-test checkbox invented — this repo has zero test source sets (confirmed in `CLAUDE.md`); verification is build-green + manual run only
- [x] Acceptance criteria from proposal.md §8 satisfied — AC-1 through AC-6 all ticked, user-confirmed on-device
- [x] proposal.md frontmatter `blockers: []` (empty)
- [x] proposal.md frontmatter `depends_on:` either `[]` OR every listed ID has a folder under `specs/_archive/` — `[]`
- [x] All §9 OQs resolved or marked out-of-band — OQ-1 resolved
- [x] No plan.md step retains `_(skeleton)_` (each expanded with concrete sub-checks) — confirmed via grep, none found

## Handoff

- [x] Branch created (`chore/android-widget-content-padding`)
- [x] `/commit` executed — commit `3ff4dde`
- [ ] Branch pushed *(see /spec-finalize)*
- [ ] PR opened against `develop` *(see /spec-finalize)*
- [ ] Spec folder archived to `specs/_archive/android-widget-content-padding/` *(see /spec-finalize)*
- [ ] Reusable-knowledge candidates from `decisions.md` proposed *(see /spec-finalize)*
