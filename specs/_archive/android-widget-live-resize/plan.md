---
spec_id: android-widget-live-resize
generated_by: /spec-plan
generated_at: 2026-08-18T00:00:00Z
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

# Plan: Live drag-resize responsiveness for Android home-screen widgets

## Strategy

Two gaps, closed in sequence. Gap 1: `sizeMode` defaults to
`SizeMode.Single`, so `LocalSize.current` never reflects a live
drag-resize — Step 1 picks the candidate `DpSize` set and override
placement, Step 2 applies it to all 5 widget classes (needed even by the
2 clock variants, for their own internal token rescale). Gap 2: the 3
plain widget classes (Small/Medium/Large) each render only their own
fixed content composable — Step 3 adds one shared dispatcher composable
(reusing `rememberWidgetSizeClass()`, already public in `WidgetTheme.kt`)
so any of the 3, once placed, transforms between the three existing
layouts as its live size crosses a size-class boundary. The 2 clock
variants are explicitly excluded from Step 3 (user-confirmed, see
proposal.md OQ-3) — they only get Step 2's rescale. Steps 4–5 verify
manually (drag-resize both directions, transform + rescale, both
locales, all 5 variants) and via build.

## Steps

### Step 1 — Determine SizeMode.Responsive placement and candidate DpSize set [investigate]

- **Files / symbols**:
  - `composeApp/src/androidMain/kotlin/.../widget/BaseWeatherGlanceWidget.kt` — confirm no existing `sizeMode` override; determine whether a single shared override here is viable for all 5 subclasses
  - `composeApp/src/androidMain/kotlin/.../widget/components/WidgetTheme.kt` — re-confirm `resolveWidgetSizeClass` thresholds (`SMALL_WIDGET_WIDTH`=120dp, `SMALL_WIDGET_HEIGHT`=100dp, `MEDIUM_WIDGET_WIDTH`=200dp)
  - `composeApp/src/androidMain/res/xml/widget_provider_info.xml`, `widget_provider_info_medium.xml`, `widget_provider_info_large.xml`, `widget_provider_info_with_analog_clock.xml`, `widget_provider_info_with_digital_clock.xml` — declared `minWidth`/`minHeight` per variant (128×48, 256×60, 256×120, 196×48, 196×48)
- **Question(s) to answer**:
  - Can one shared `sizeMode = SizeMode.Responsive(setOf(...))` override on `BaseWeatherGlanceWidget` correctly serve all 5 subclasses, or does each variant's differing declared minimum (esp. Large's 256×120 already exceeding the Small/Medium thresholds) require per-subclass overrides with different candidate sets? (feeds AC-1)
  - What concrete `DpSize` candidate set best aligns Glance's nearest-match snapping with `resolveWidgetSizeClass`'s existing breakpoints and each variant's declared minimum? (resolves proposal.md OQ-2)
- **Outputs to record**: proposal.md §4 Affected areas TBD row resolved (confirm/deny per-clock-subclass sizeMode placement); the concrete `Set<DpSize>` to use in Step 2; `Step 1 result: override_scope = shared-base | per-subclass` marker.
- **Why**: picking the `SizeMode.Responsive` candidate set determines both how smoothly the rescale feels AND where the Step 3 content-transform boundaries land — get it misaligned with `resolveWidgetSizeClass` and the widget could visually switch composable at a size that still looks cramped, or never reach Large at all.

**Result (2026-08-18)**: `Step 1 result: override_scope = shared-base`. One
`sizeMode` override on `BaseWeatherGlanceWidget` serves all 5 subclasses —
`WIDGET_RESPONSIVE_SIZES = {DpSize(128.dp,48.dp), DpSize(196.dp,48.dp),
DpSize(256.dp,60.dp), DpSize(256.dp,120.dp)}` (the 4 distinct real
declared minimums across all 5 `widget_provider_info*.xml` files).
**Finding that refines Step 3's approach**: `resolveWidgetSizeClass`
(typography) is width/height-OR-based and classifies Medium's own
declared default (256×60) as SMALL (height 60 < `SMALL_WIDGET_HEIGHT`
100dp) — this is pre-existing, unchanged, and harmless for typography
(it's the exact font sizing Medium already ships with today). But
reusing it for CONTENT dispatch would misfire: a freshly-placed Medium
widget would render `SmallWeatherWidgetContent` instead of
`MediumWeatherWidgetContent` — a regression. Step 3's dispatcher
therefore compares `LocalSize.current` directly against the real
declared minimums (256×60 / 256×120) instead of calling
`rememberWidgetSizeClass()`. This is safe only because
`SizeMode.Responsive` limits `LocalSize.current` to exactly one of the 4
`WIDGET_RESPONSIVE_SIZES` points — see proposal.md §4 for the full
reasoning. `WidgetTheme.kt` needed no code change (confirmed).

### Step 2 — Override sizeMode to enable live-resize [implement]

- **Skill**: direct edits
- **Area(s)**: `widget` (androidMain)
- **Files / symbols**:
  - `composeApp/src/androidMain/kotlin/.../widget/BaseWeatherGlanceWidget.kt` — add `override val sizeMode: SizeMode = SizeMode.Responsive(<Step 1's candidate set>)` if Step 1 result is `shared-base`
  - `composeApp/src/androidMain/kotlin/.../widget/SmallWeatherGlanceWidget.kt`, `MediumWeatherGlanceWidget.kt`, `LargeWeatherGlanceWidget.kt`, `SmallWeatherWithAnalogClockGlanceWidget.kt`, `SmallWeatherWithDigitalClockGlanceWidget.kt` — add per-class `sizeMode` overrides instead, only if Step 1 result is `per-subclass`
- **Skill args / inputs**: none
- **Why**: closes Gap 1 (proposal.md §2) for all 5 widget classes — the 2 clock variants need this too, for their own internal token rescale (proposal.md AC-4), even though they don't get Step 3's content-transform.
- **Verification**: build green (`./gradlew :composeApp:assembleDebug`); `sizeMode` symbol present and resolves to `SizeMode.Responsive(...)` (not the default `SizeMode.Single`) in every class Step 1 named.

### Step 3 — Add content-transform dispatcher for the 3 plain widgets [implement]

- **Skill**: direct edits
- **Area(s)**: `widget` (androidMain)
- **Files / symbols**:
  - `composeApp/src/androidMain/kotlin/.../widget/components/WidgetComponents.kt` — add `AdaptiveWeatherWidgetContent(weatherData: WeatherWidgetData)`, which reads `LocalSize.current` directly (NOT `rememberWidgetSizeClass()` — see Step 1's finding) and renders `SmallWeatherWidgetContent(weatherData)` / `MediumWeatherWidgetContent(weatherData)` / `LargeWeatherWidgetContent(weatherData, LocalContext.current)` by comparing against the real declared minimums (256×60 / 256×120)
  - `composeApp/src/androidMain/kotlin/.../widget/SmallWeatherGlanceWidget.kt`, `MediumWeatherGlanceWidget.kt`, `LargeWeatherGlanceWidget.kt` — change `provideGlance`'s `provideContent` block to call the new dispatcher composable instead of each class's own fixed content composable
- **Skill args / inputs**: none
- **Why**: closes Gap 2 (proposal.md §2) — without this, Step 2's live `LocalSize.current` only feeds the already-existing token rescale; the layout itself never transforms, which is the behavior the user explicitly asked for.
- **Verification**: build green; each of `SmallWeatherGlanceWidget`/`MediumWeatherGlanceWidget`/`LargeWeatherGlanceWidget`'s `provideGlance` references the new dispatcher composable (not its own former fixed call); `SmallWeatherWithAnalogClockGlanceWidget`/`SmallWeatherWithDigitalClockGlanceWidget` are unchanged in this step (still call their own clock content composable directly, per proposal.md §3 Out of scope).

### Step 3b — Scale the clock graphic itself (digital text size, analog dial/hands) [implement]

_Added 2026-08-18, mid-implementation, at the user's explicit request
("y el escalado de los relojes?" → confirmed: scale both, add new
analog resources if needed)._

- **Skill**: direct edits
- **Area(s)**: `widget` (androidMain)
- **Files / symbols**:
  - `composeApp/src/androidMain/kotlin/.../widget/components/WidgetTheme.kt` — add `clockDigitalDateSize`/`clockDigitalTimeSize: TextUnit` fields to `WidgetTypography`; populate Small (14sp/24sp, matching the pre-existing hardcoded XML values exactly — no regression), Medium (16sp/30sp), Large (18sp/36sp)
  - `composeApp/src/androidMain/kotlin/.../widget/components/WidgetComponents.kt` — `WeatherWithDigitalClockContent` calls `RemoteViews.setTextViewTextSize(R.id.dateClock/textClock, TypedValue.COMPLEX_UNIT_SP, t.clockDigitalDateSize/TimeSize.value)`; add `analogClockLayoutRes(sizeClass: WidgetSizeClass): Int` and have `WeatherWithAnalogClockContent` pass its result (keyed on `rememberWidgetSizeClass()`) as the layout resource to `RemoteViews(...)`
  - `composeApp/src/androidMain/res/layout/widget_rtc_analog_clock_medium.xml`, `widget_rtc_analog_clock_large.xml` (new files) — copies of the existing (untouched) `widget_rtc_analog_clock.xml` with `AnalogClock` `layout_width`/`layout_height` raised to 85dp/110dp (from 65dp), `android:dial`/`hand_hour`/`hand_minute`/`hand_second` repointed to the `_medium`/`_large` drawables below, and `TextClock` `textSize` raised to 13sp/15sp (from 11sp)
  - `composeApp/src/androidMain/res/drawable/clock_dial_medium.xml`, `clock_dial_large.xml`, `clock_hour_hand_medium.xml`, `clock_hour_hand_large.xml`, `clock_minute_hand_medium.xml`, `clock_minute_hand_large.xml`, `clock_second_hand_medium.xml`, `clock_second_hand_large.xml` (8 new files) — copies of the 4 originals with only `android:width`/`android:height` raised to 85dp/110dp (from 65dp); `viewportWidth`/`viewportHeight`/pathData unchanged
- **Skill args / inputs**: none
- **Why**: the weather-side content of both clock variants already rescaled (existing `rememberWidgetTypography()`/`rememberWidgetSpacing()` calls), but the clock graphic itself — a native Android View rendered via `AndroidRemoteViews`, outside the Glance composable tree — had hardcoded XML text sizes and a fixed-size `AnalogClock` disconnected from the token system. RemoteViews has no universally-available (pre-API-31) call to resize a View's layout params at runtime, so the analog case needs discrete layout-resource variants rather than a single runtime value (digital's `setTextViewTextSize` has been available since well below `minSdk` 24, so no such constraint there). **Correction discovered by the user testing**: `AnalogClock` only shrinks its dial/hands to fit available space, never grows them past the drawable's own declared intrinsic size — so growing only the View's `layout_width`/`layout_height` left the dial/hands rendered at their original 65dp regardless. The 8 new drawable variants (with a larger *intrinsic* `android:width`/`android:height`, not just a larger container) are the actual fix — see `decisions.md`.
- **Verification**: build green; `WidgetTypography` has the 2 new fields on all 3 instances; `WeatherWithDigitalClockContent` calls `setTextViewTextSize` for both `TextClock` views; `WeatherWithAnalogClockContent` selects among 3 distinct layout resource IDs by size class; the 2 layout XML files reference the size-matched drawable variants (not the shared originals); the 8 new drawable files exist and differ from their originals only in `android:width`/`android:height`.

### Step 4 — Manual drag-resize verification across all widget variants [verify]

- **What to check**: place each of the 5 variants (Small, Medium, Large,
  SmallWithDigitalClock, SmallWithAnalogClock) on an Android
  emulator/device home screen. For the 3 plain variants: drag through
  small→large→small and confirm (a) the rendered content composable
  switches at each size-class boundary (Small↔Medium↔Large layouts, not
  just rescaled values) in both directions; (b) within each rendered
  composable, typography/icon/spacing match its size class; (c) the
  composable switch settles cleanly with no partial/mixed render or
  flicker loop once the drag is released. For the 2 clock variants: drag
  through the same range and confirm (d) clock content is retained at
  every size — no switch to a non-clock layout; (e) internal
  typography/icon/spacing still rescale; (f) no visible flicker/jank from
  the 1-second `updatePeriodMillis` tick interacting with resize-triggered
  recomposition (proposal.md §7 risk); (g) the clock graphic itself also
  visibly grows/shrinks — digital clock's date/time text size changes,
  analog clock's dial and hands change size at each size-class boundary
  — with no visible reset/jump/flicker on the analog clock's
  layout-resource swap (proposal.md §7 risk). For all 5: confirm (g) no text
  clipping/overlap at any dragged size; (h) each variant still renders
  correctly at its original placed/default size (no regression). Repeat
  in both English and Spanish locales.
- **Pass criteria**: no crash, no stale/frozen layout after a resize
  gesture, correct composable rendered per size class for the 3 plain
  variants (both directions), clock variants never switch away from
  clock content, no text clipping/overlap, no visible clock-widget
  flicker/jank, for all 5 variants, in both locales. Satisfies AC-2
  through AC-6; also addresses OQ-4 as a manual-verification item.

### Step 5 — Build verification [verify]

- **What to check**: `./gradlew :composeApp:assembleDebug`
- **Pass criteria**: BUILD SUCCESSFUL. Satisfies AC-7.

## Dependencies

- Step 2 depends on Step 1's `override_scope` marker and candidate
  `DpSize` set.
- Step 3 depends on Step 2's `sizeMode` change being in place (the
  dispatcher composable relies on `rememberWidgetSizeClass()` reading a
  live `LocalSize.current`).
- Step 3b depends on Step 2's `sizeMode` change (the clock content
  composables' `rememberWidgetSizeClass()`/`rememberWidgetTypography()`
  calls need a live `LocalSize.current` too); independent of Step 3
  (disjoint files).
- Step 4 depends on Steps 2, 3, and 3b's code changes being present in a
  debug build installed on the verification device/emulator.

## Out-of-band actions

- Manual verification on an Android emulator or physical device is
  required for Step 4 (drag-resize and content-transform behavior cannot
  be verified by `./gradlew` alone).

## Rollback

If Step 2 or Step 3 causes a regression discovered in Step 4 (crash,
stale layout, incorrect composable rendered, or a clock variant
unexpectedly losing its clock content), `git restore` the specific files
listed in that step — Step 2 and Step 3 touch disjoint file sets
(`BaseWeatherGlanceWidget.kt`/clock classes vs. `WidgetComponents.kt`/the
3 plain classes), so either can be reverted independently without
affecting the other.
