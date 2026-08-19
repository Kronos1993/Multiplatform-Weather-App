---
spec_id: android-widget-live-resize
title: Live drag-resize responsiveness for Android home-screen widgets
type: story
priority: normal
source: manual
source_ref: dictated by user in chat, 2026-08-18
created: 2026-08-18
status: INTAKE_PARSED
recommend_split: no
blockers: []
depends_on: []
expect_actual_touched: no
localization_touched: no
branch_suggested: feature/android-widget-live-resize
---

# Proposal: Live drag-resize responsiveness for Android home-screen widgets

## Inherited assumptions

_No Assumptions section in story.md — section omitted._

## 1. Source

Manual intake (see `story.md`): the user wants the existing Android
home-screen widgets to react live when the user drags an already-placed
widget's resize handles larger or smaller — not just render correctly at
the fixed size they were placed at. This is the direct, explicitly-named
follow-up to **OQ-1** in the archived spec
`specs/_archive/android-widget-responsive-sizing/proposal.md` (PR #17,
merged 2026-08-18), which built the size-class-aware typography/spacing
token system but deliberately excluded live drag-resize because Glance's
`sizeMode` defaults to `SizeMode.Single`.

During planning, the user clarified the scope of "responsive" explicitly
(chat, 2026-08-18): it is not only fonts/icons/spacing rescaling in
place — **the widget's layout itself must transform from one existing
composable to another when the live size crosses into a different size
class** (e.g. a Small instance dragged large enough starts rendering
`MediumWeatherWidgetContent`/`LargeWeatherWidgetContent`, and shrinks
back on the way down). A follow-up clarifying question (`AskUserQuestion`)
confirmed the two clock variants are explicitly excluded from this
content-transform behavior — see §9 OQ-1/OQ-3.

## 2. Problem / Why

Investigation of `composeApp/src/androidMain/kotlin/.../widget/` confirms
two gaps, one already closable with existing infrastructure and one
needing a small new dispatcher:

- All 5 `res/xml/widget_provider_info*.xml` files already declare
  `android:resizeMode="horizontal|vertical"` with no
  `maxResizeWidth`/`maxResizeHeight` cap — Android's launcher already
  lets the user drag any placed widget instance larger or smaller today.
- `widget/BaseWeatherGlanceWidget.kt` (the abstract base all 5
  `*GlanceWidget` classes extend) never overrides `GlanceAppWidget.sizeMode`,
  so it defaults to `SizeMode.Single`: `LocalSize.current` inside every
  widget's composable tree is pinned to the size declared in its
  `widget_provider_info*.xml` and never reflects a live drag-resize of an
  already-placed instance. **Gap 1**: no live size signal at all.
- Today, each of `SmallWeatherGlanceWidget.kt`, `MediumWeatherGlanceWidget.kt`,
  `LargeWeatherGlanceWidget.kt`'s `provideGlance` unconditionally renders
  its own fixed content composable (`SmallWeatherWidgetContent`/
  `MediumWeatherWidgetContent`/`LargeWeatherWidgetContent(weatherData, LocalContext.current)`
  — confirmed by direct inspection). Even once Gap 1 is closed and
  `LocalSize.current` reflects live size, these three classes would still
  each render only their own single composable — **Gap 2**: nothing
  dispatches to a *different* content composable as the live size crosses
  a size-class boundary.
- What's already fully reusable: `widget/components/WidgetTheme.kt` already
  exposes `rememberWidgetSizeClass(): WidgetSizeClass` (public,
  `WidgetTheme.kt:118`), and all three plain content composables take a
  compatible `weatherData` parameter (`LargeWeatherWidgetContent` needs
  one extra `LocalContext.current` argument, trivially suppliable from any
  Glance composable). All three widget classes already load the *same*
  full weather data via the shared `BaseWeatherGlanceWidget.loadWeatherDataFromCache(context)`
  — so no data-shape gap blocks dispatch; `LargeWeatherWidgetContent`'s
  forecast rows are already populated regardless of which widget class
  loaded the data.

So closing Gap 1 (sizeMode) makes size *known* live; closing Gap 2 (a
small size-class dispatcher composable, reusing the three composables
and `rememberWidgetSizeClass()` that already exist) makes the widget
*transform*, not just rescale.

## 3. Scope

**In scope**
- Override `sizeMode` (default `SizeMode.Single`) with `SizeMode.Responsive(<candidate DpSize set>)`
  across all 5 widget classes, so `LocalSize.current` reflects each
  instance's live, current on-screen size.
- Choose a `SizeMode.Responsive` candidate `DpSize` set (investigated in
  plan.md Step 1) that aligns with `resolveWidgetSizeClass`'s existing
  breakpoints (`SMALL_WIDGET_WIDTH`=120dp, `SMALL_WIDGET_HEIGHT`=100dp,
  `MEDIUM_WIDGET_WIDTH`=200dp) and each variant's own declared
  `minWidth`/`minHeight`.
- Add a shared content-dispatch composable (plan.md Step 3) to
  `widget/components/WidgetComponents.kt` that reads
  `rememberWidgetSizeClass()` and renders `SmallWeatherWidgetContent`/
  `MediumWeatherWidgetContent`/`LargeWeatherWidgetContent` accordingly;
  wire `SmallWeatherGlanceWidget.kt`, `MediumWeatherGlanceWidget.kt`, and
  `LargeWeatherGlanceWidget.kt`'s `provideGlance` to call it instead of
  their own fixed composable. **This is the core new behavior**: any of
  these three, once placed, transforms between the three existing
  layouts as the user drags it across size-class boundaries — it is no
  longer "the Small widget" or "the Large widget" once placed, it is one
  adaptive weather widget whose rendered layout tracks its live size.
- Manual drag-resize verification across all 5 existing widget variants
  in both supported locales (en/es) — confirming both the rescale (within
  one composable) and the transform (across composables) behave
  correctly, and that the transition back down (large → small) also
  switches back.

- **Scale the clock graphic itself, not just the surrounding weather info**
  (added 2026-08-18, mid-implementation, at the user's explicit request —
  "y el escalado de los relojes?"). The weather-side content of both
  clock variants already rescaled correctly via `rememberWidgetTypography()`/
  `rememberWidgetSpacing()` (unchanged by this addition), but the clock
  graphic itself — rendered via `AndroidRemoteViews` embedding a native
  Android View layout (`res/layout/widget_rtc_analog_clock.xml`/
  `widget_rtc_digital_clock.xml`), entirely outside the Glance composable
  tree — did not scale: text sizes and the `AnalogClock`'s dial/hand
  drawables were hardcoded in XML, disconnected from the token system.
  Digital clock: `WeatherWithDigitalClockContent` now calls
  `RemoteViews.setTextViewTextSize(...)` on the date/time `TextClock`
  views using 2 new `WidgetTypography` fields (`clockDigitalDateSize`,
  `clockDigitalTimeSize`), scaled the same way as the other typography
  tokens. Analog clock: since an `AnalogClock`'s vector-drawable dial
  scales cleanly with its View bounds but RemoteViews has no
  universally-available (pre-API-31) runtime "set layout size" call, two
  new layout resources were added — `widget_rtc_analog_clock_medium.xml`,
  `widget_rtc_analog_clock_large.xml` — identical to the existing
  (untouched) small variant except for a larger `AnalogClock`
  `layout_width`/`layout_height` and `TextClock` `textSize`;
  `WeatherWithAnalogClockContent` picks between the 3 layout resources by
  `rememberWidgetSizeClass()`.

**Out of scope**
- **Content-transform for the two clock variants**
  (`SmallWeatherWithAnalogClockGlanceWidget`/`SmallWeatherWithDigitalClockGlanceWidget`).
  Confirmed explicitly with the user via a clarifying question during
  planning: these keep rendering their own clock content composable at
  every size — only their internal typography/icon/spacing tokens AND
  (per the addition above) the clock graphic itself rescale live — the
  clock is never replaced by a non-clock layout. No "Medium/Large with
  clock" WEATHER layout exists and building one is new UI work, not part
  of this spec. See §9 OQ-3.
- Removing, renaming, or replacing any of the 5 existing widget
  variants — explicitly ruled out by the user from the start.
- iOS widgets — none exist in this repo.

**Split-heuristic note**: §7 Risks lists 5 entries (grew from 4 to 5
after the clock-graphic-scaling addition below), past this repo's
split-heuristic trigger threshold (`plan.split_thresholds.risks`). Split
is deliberately **not** recommended: every risk traces back to the same
single coupled change (live size signal + a Compose-side dispatcher +,
now, a RemoteViews-side rescale for the clock graphic) confined entirely
to `composeApp/src/androidMain/.../widget/`, consumed by the same 5
widget classes touched by the prior (already-shipped) size-token spec.
Splitting sizeMode from content-dispatch (or the clock-graphic scaling)
into sibling specs would not reduce the manual-verification surface (all
three must be exercised together in the same Step 4 drag-resize pass)
and would only add coordination overhead in a solo-maintained repo.

## 4. Affected areas

| Area | Source set(s) | Class(es) / file(s) touched | Change type | Resolved by | Notes |
|--------|--------------|-------------------------------|-------------|-------------|-------|
| components (Android widget) | androidMain | `widget/BaseWeatherGlanceWidget.kt` | modify | Step 1 | added `override val sizeMode: SizeMode = SizeMode.Responsive(WIDGET_RESPONSIVE_SIZES)` — one shared override for all 5 subclasses (`override_scope = shared-base`, see Step 1 resolution below). `WIDGET_RESPONSIVE_SIZES` = the 4 *distinct* declared minimums across all 5 `widget_provider_info*.xml` files: `DpSize(128.dp,48.dp)` Small, `DpSize(196.dp,48.dp)` both clock variants, `DpSize(256.dp,60.dp)` Medium, `DpSize(256.dp,120.dp)` Large. Using only real, already-designed breakpoints (not arbitrary in-between values) keeps every consumer's reasoning to 4 known points instead of a continuous range — resolves OQ-2. |
| components (Android widget) | androidMain | `widget/SmallWeatherWithAnalogClockGlanceWidget.kt`, `SmallWeatherWithDigitalClockGlanceWidget.kt` | none | Step 1 | not touched — `override_scope = shared-base` means the single `BaseWeatherGlanceWidget.sizeMode` override is inherited by both clock subclasses too (their internal token rescale, per AC-4). Content composable NOT changed (see §3 Out of scope). |
| components (Android widget) | androidMain | `widget/components/WidgetComponents.kt` | modify | Step 1 | added `AdaptiveWeatherWidgetContent(weatherData)`, a dispatcher composable — **implementation note (2026-08-18, Step 1 finding)**: it does NOT reuse `rememberWidgetSizeClass()`. That function is tuned for typography/spacing (SMALL if width<120dp OR height<100dp), and Medium's own declared default (256×60) resolves SMALL under it (height 60<100) — reusing it for content dispatch would make a freshly-placed, never-resized Medium widget render `SmallWeatherWidgetContent` instead of `MediumWeatherWidgetContent`, a regression against AC-5. Instead it compares `LocalSize.current` directly against the real declared minimums from `widget_provider_info_medium.xml`/`_large.xml` (256×60 / 256×120) — safe specifically because `BaseWeatherGlanceWidget`'s new `SizeMode.Responsive` set (see below) only ever reports one of 4 known exact points, never an arbitrary in-between value. `rememberWidgetTypography()`/`rememberWidgetSpacing()` are untouched, so already-verified font/icon/spacing behavior is unaffected. |
| components (Android widget) | androidMain | `widget/SmallWeatherGlanceWidget.kt`, `MediumWeatherGlanceWidget.kt`, `LargeWeatherGlanceWidget.kt` | modify | | `provideGlance` calls the new dispatcher composable instead of each class's own fixed content composable |
| components (Android widget) | androidMain | `widget/components/WidgetTheme.kt` | modify | | (added 2026-08-18) `WidgetTypography` gained 2 fields — `clockDigitalDateSize`, `clockDigitalTimeSize` — populated on all 3 size instances (Small values match the digital clock XML's current hardcoded 14sp/24sp exactly, zero regression); `rememberWidgetSizeClass()` unchanged, now also consumed by `WeatherWithAnalogClockContent` to pick a layout resource. |
| components (Android widget) | androidMain | `res/layout/widget_rtc_analog_clock_medium.xml`, `res/layout/widget_rtc_analog_clock_large.xml` (new files) | add | | (added 2026-08-18) copies of the existing `widget_rtc_analog_clock.xml` with a larger `AnalogClock` `layout_width`/`layout_height` (85dp/110dp vs. the original 65dp) and `TextClock` `textSize` (13sp/15sp vs. 11sp); the original file is untouched and still used for `SMALL`. |
| components (Android widget) | androidMain | `res/drawable/clock_dial_medium.xml`, `clock_dial_large.xml`, `clock_hour_hand_{medium,large}.xml`, `clock_minute_hand_{medium,large}.xml`, `clock_second_hand_{medium,large}.xml` (8 new files) | add | | (added 2026-08-18, correction) — `AnalogClock` only shrinks its dial/hands to fit available space, it never grows them past their own drawable-declared intrinsic size; growing the View's `layout_width`/`layout_height` alone (row above) left the dial/hands rendered at their original 65dp with blank space around them, caught by the user testing. These 8 files are exact copies of the 4 originals with only `android:width`/`android:height` raised to 85dp/110dp (`viewportWidth`/`Height`/pathData unchanged — vector scales losslessly); the 2 new layout files (row above) reference them by size tier. Originals untouched, still used for `SMALL`. See `decisions.md`. |
| — (reference only, no edits expected) | androidMain | `res/xml/widget_provider_info*.xml` (5 files) | none | | confirmed already declaring `resizeMode="horizontal|vertical"` with no max-resize cap |

No `features/`, `domain/`, `data/`, or `di/` package is touched — this is
confined to the androidMain Glance presentation layer, same as the prior
spec.

## 5. Architectural gauntlet (this repo's hard rules)

### 5a. Always explicit (no shortcut)

- [x] **Expect/actual parity** — N/A. `expect_actual_touched: no`. No
      commonMain `expect` declaration is touched; entirely androidMain
      Glance code with no shared/`commonMain` counterpart.
      Approach: confirmed — no expect/actual involved.
- [x] **Dual localization** — N/A. `localization_touched: no`. This
      spec changes only which live size Glance reports and which
      already-existing content composable renders for that size; no
      new/changed user-facing string.
      Approach: confirmed — no string changes.
- [x] **Secrets & logging** — confirmed. Touches only `sizeMode`
      configuration and a content-dispatch composable; no code path here
      reads, logs, or prints the WeatherAPI key or any credential.
      Approach: confirmed — no secret-adjacent code touched.
- [x] **No automated tests exist** — acknowledged. Verification is
      build-green (`./gradlew :composeApp:assembleDebug`) plus manual
      drag-resize of all 5 widget variants on an Android emulator/device
      home screen in both locales — see plan.md Step 4.
      Acknowledged: yes

### 5b. Confinement-conditional

- [x] **Confinement claim** — change is confined to
      `composeApp/src/androidMain/` (`widget/BaseWeatherGlanceWidget.kt`,
      the 3 plain `*GlanceWidget.kt` classes,
      `widget/components/WidgetComponents.kt`,
      `widget/components/WidgetTheme.kt`, and 2 new `res/layout/widget_rtc_analog_clock_*.xml`
      resource files). No new `domain/repository` interface, no new Koin
      binding, no cross-feature boundary crossed. Neither rule below is
      reachable.

## 6. Skills

**Skills**: direct edits

## 7. Risks

- **`SizeMode.Responsive` candidate-size calibration** — the discrete
  `DpSize` set Glance snaps `LocalSize.current` to must align with
  `resolveWidgetSizeClass`'s existing thresholds and each variant's own
  declared minimum, or a dragged widget could jump between size classes
  at an inconsistent point, or never reach a size class its layout was
  designed for.
- **Content-composable switching abruptness** — unlike the internal
  token rescale (smooth per-value change), switching between
  structurally different composables (e.g. Small's minimal content vs.
  Large's multi-day forecast rows) is a full `RemoteViews` re-render with
  no host-provided cross-fade; each AppWidget host renders this as a
  discrete "snap" rather than an animated transform. This is expected
  Android AppWidget-host behavior, not a bug, but must be visually
  confirmed to settle correctly (no partial/mixed render, no flicker
  loop) once the user releases a drag — plan.md Step 4.
- **Wide manual QA surface, no automated tests** — verification requires
  dragging all 5 widget variants through multiple sizes (both directions:
  growing and shrinking) on a real emulator/device home screen, in both
  supported locales (en/es) — for the 3 plain variants this also means
  confirming the correct composable renders at each size class boundary,
  not just that values rescaled.
- **Clock-widget refresh interaction** — `SmallWeatherWithAnalogClockGlanceWidget`
  and `SmallWeatherWithDigitalClockGlanceWidget` declare
  `android:updatePeriodMillis="1000"` (a 1-second tick). Switching their
  `sizeMode` to `Responsive` means each tick's recomposition also runs
  under the new size-reactive path; an untested interaction between the
  1-second update cadence and a live resize-triggered recomposition
  could show as flicker or jank and needs explicit manual verification
  (plan.md Step 4), not assumed safe by default.
- **Analog clock layout-swap on resize** (added 2026-08-18) — switching
  between `widget_rtc_analog_clock.xml`/`_medium.xml`/`_large.xml` is a
  full `RemoteViews` layout swap (new `AnalogClock`/`TextClock` view
  instances each time), not an in-place property update like the digital
  clock's `setTextViewTextSize`. Combined with the 1-second tick above,
  this needs explicit confirmation the analog hands don't visibly
  reset/jump or flicker when a resize crosses a size-class boundary.

## Out-of-band actions

- Manual verification on an Android emulator or physical device is
  required for plan.md Step 4 (drag-resize and content-transform
  behavior cannot be verified by `./gradlew` alone).

## 8. Acceptance criteria

- [x] **AC-1** — `BaseWeatherGlanceWidget` (or each `*GlanceWidget`
      subclass, per Step 1's finding) declares `sizeMode = SizeMode.Responsive(...)`
      instead of the default `SizeMode.Single`, so `LocalSize.current`
      reflects each instance's live, current on-screen size.
- [x] **AC-2** — For the 3 plain variants (Small, Medium, Large), when an
      already-placed instance is dragged across a size-class boundary
      (per `rememberWidgetSizeClass()`), the rendered content composable
      switches to match the new size class (`SmallWeatherWidgetContent`/
      `MediumWeatherWidgetContent`/`LargeWeatherWidgetContent`) —
      verified by manual drag-resize in both directions (growing and
      shrinking).
- [x] **AC-3** — Within whichever content composable is currently
      rendered, typography/icon/spacing continue to reflect
      `WidgetTypography`/`WidgetSpacing` for the live size (already-built
      token system, now fed by a live `LocalSize.current`).
- [x] **AC-4** — The 2 clock variants (SmallWithDigitalClock,
      SmallWithAnalogClock) do **not** switch content composable at any
      size — they keep rendering their own clock content at every
      dragged size (explicit, user-confirmed exclusion, §9 OQ-3); their
      internal typography/icon/spacing tokens rescale live; and (added
      2026-08-18) the clock graphic itself also rescales live — the
      digital clock's date/time text size via
      `RemoteViews.setTextViewTextSize(...)`, the analog clock's dial and
      date text via the size-class-selected `widget_rtc_analog_clock*.xml`
      layout variant.
- [x] **AC-5** — All 5 widgets continue to render correctly at their
      originally-declared/default placed size — no removal, replacement,
      or regression versus current behavior in either locale.
- [x] **AC-6** — No text (temperature, location, condition, forecast
      labels, clock date where applicable) is clipped or overlaps at any
      size reachable via drag-resize within each variant's declared
      `resizeMode` range, in either locale (en/es).
- [x] **AC-7** — `./gradlew :composeApp:assembleDebug` succeeds.

## 9. Open questions

- **OQ-1** — Should live resize also switch WHICH content composable
  renders, or only rescale fonts/icons/spacing within the widget's own
  originally-chosen layout? **Resolved (2026-08-18, updated after user
  clarification in chat)**: content DOES switch for the 3 plain variants
  — see §3 In scope. (Initial planning pass had defaulted to
  rescale-only; the user corrected this explicitly: "la idea es que si,
  escalen los textos e iconos, pero tambien que se transformen de 1 a
  otros si el tamaño es suficiente.")
- **OQ-2** — Exact `SizeMode.Responsive` candidate `DpSize` set.
  **Resolved (2026-08-18, Step 1)**: `{DpSize(128.dp,48.dp),
  DpSize(196.dp,48.dp), DpSize(256.dp,60.dp), DpSize(256.dp,120.dp)}` —
  the 4 distinct real declared minimums, not synthetic in-between
  values. See §4 for the full reasoning and the related content-dispatch
  finding (dispatcher doesn't reuse `rememberWidgetSizeClass()`).
- **OQ-3** — Do the two clock variants get the same content-transform
  treatment as the plain variants, or keep clock content at every size?
  **Resolved (2026-08-18, via clarifying question)**: clock content is
  kept at every size — no content-transform for the 2 clock variants,
  only internal token rescale (same as their original plan). Building
  "Medium/Large with clock" layouts was explicitly declined as
  out-of-scope new UI work.
- **OQ-4** — Should `SizeMode.Responsive`'s behavior on `minSdk` 24
  devices/launchers that may not support live drag-resize be explicitly
  checked (i.e. confirm graceful degradation to today's static
  behavior, no crash)? Not a blocker — folded into plan.md Step 4's
  manual verification, not a gate on starting implementation.

---

## Serena memories consulted

- `mem:core` — confirmed `androidMain/widget/` (Glance) as the canonical
  location for Android widget code, and pointed at the two widget
  memories below.
- `mem:android_widget_spacing_tokens` — confirmed `resolveWidgetSizeClass`/
  `WidgetTypography`/`WidgetSpacing` already branch on `LocalSize.current`
  and are the single source of truth for size-dependent widget values;
  confirmed `sizeMode` is not overridden anywhere today (`SizeMode.Single`
  default) — this is Gap 1 this spec closes. `rememberWidgetSizeClass()`
  (also documented there as existing) is Gap 2's dispatch key.
- `mem:android_widget_error_content_double_padding` — confirmed
  `WeatherWidgetErrorContent`'s pre-existing double-padding issue is
  unrelated to this spec's scope (not touched here).
- `mem:architecture` — confirmed no `domain`/`data`/`di` surface is
  reachable from widget code.
- `mem:conventions` — confirmed no user-facing strings touched, so the
  dual-localization rule is N/A; confirmed no lint/test tooling to
  invent.
