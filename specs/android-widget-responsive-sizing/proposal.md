---
spec_id: android-widget-responsive-sizing
title: Complete size-responsive typography, icon, spacing, and style system for Android home-screen widgets
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
branch_suggested: feature/android-widget-responsive-sizing
---

# Proposal: Complete size-responsive typography, icon, spacing, and style system for Android home-screen widgets

## 1. Source

Manual intake (see `story.md`): the user wants the Android home-screen
widgets — which already work correctly — to better adapt font size, icon
size, spacing, and style to the available widget/screen size. During
intake the user confirmed the scope is **Android only** (no iOS widget
exists in this repo yet — confirmed during `/spec-new` investigation) and
named four priority acceptance bars: proportional icons, consistent
spacing/density, visual parity with the main app's style, and no
text clipping/overlap at any widget size.

## 2. Problem / Why

Planning-time investigation of `composeApp/src/androidMain/kotlin/com/kronos/multiplatform/weatherapp/widget/` found the size-adaptive system is **partially built, not absent**:

- `components/WidgetTheme.kt` already defines a `WidgetSizeClass` enum
  (SMALL/MEDIUM/LARGE) and a `WidgetTypography` data class with 8 tokens —
  `tempSize`, `conditionSize`, `locationSize`, `labelSize`, `detailSize`
  (sp) and `locationIconSize`, `weatherIconSize`, `forecastIconSize` (dp) —
  with three concrete instances (`SmallTypography`/`MediumTypography`/
  `LargeTypography`) branched by `LocalSize.current` in
  `rememberWidgetTypography()`.
- `components/WidgetComponents.kt`'s five content composables
  (`SmallWeatherWidgetContent`, `MediumWeatherWidgetContent`,
  `LargeWeatherWidgetContent`, `WeatherWithAnalogClockContent`,
  `WeatherWithDigitalClockContent`) all correctly consume
  `rememberWidgetTypography()` for font and icon sizes.

**The gap is spacing/density and two orphaned states, not fonts/icons**:
every `Spacer`/`padding` value in `WidgetComponents.kt` is a hardcoded
literal independent of size class (e.g. `width(6.dp)`, `height(4.dp)`,
`width(20.dp)`, `width(40.dp)`), so density does not adapt the way font
and icon size already do. `LoadingWidget` (icon 44.dp, text 13.sp) and
`WeatherWidgetErrorContent` (icon 36.dp, padding 16.dp, text 13.sp) never
call `rememberWidgetTypography()` at all — those two states are entirely
outside the size-adaptive system today. `rememberWidgetTypography()` and
a second function `rememberWidgetSizeClass()` also duplicate the same
`LocalSize`-branching logic independently, risking future drift.

Additionally, Glance's `GlanceAppWidget.sizeMode` is not overridden by
`BaseWeatherWidget`, so it defaults to `SizeMode.Single`: `LocalSize.current`
reflects only the size declared in each widget's `res/xml/widget_provider_info*.xml`
(128×48 / 256×60 / 256×120 / 196×48 dp), not a live drag-resize of an
already-placed widget instance. This matters for scoping this spec — see
**OQ-1**.

## 3. Scope

**In scope**
- Consolidate the duplicated `LocalSize`-branching logic in
  `rememberWidgetTypography()` / `rememberWidgetSizeClass()`
  (`WidgetTheme.kt`) into one shared resolver.
- Add size-class-aware spacing/density tokens (content padding, item gaps,
  section gaps) to the existing typography system in `WidgetTheme.kt`, and
  replace every hardcoded `Spacer`/`padding` literal in
  `WidgetComponents.kt`'s content composables with those tokens.
- Extend size-aware typography/spacing to `LoadingWidget` and
  `WeatherWidgetErrorContent`, which are currently hardcoded and outside
  the system entirely.
- Investigate and, if divergent, align widget color/style tokens with the
  main app's design system (visual-parity AC).
- Manual verification across all 5 existing widget variants (Small,
  Medium, Large, SmallWithDigitalClock, SmallWithAnalogClock) in both
  supported locales (en/es), confirming no text clipping/overlap.

**Out of scope**
- Migrating `BaseWeatherWidget` off Glance's default `SizeMode.Single`
  to `SizeMode.Responsive`/`SizeMode.Exact` to support **live**
  continuous resize of an already-placed widget instance. The user's
  intake describes adapting to "pequeño, mediano, grande" — which maps to
  the 3 existing separately-installable widget variants, not drag-resize
  of one placed instance. This is flagged as a non-blocking follow-up
  in **OQ-1**, not built here.
- iOS widgets — none exist in this repo (confirmed during `/spec-new`).

**Split-heuristic override**: the risk count below reaches 4, which is
this repo's split-heuristic trigger threshold
(`plan.split_thresholds.risks`). Split is deliberately **not**
recommended: 3 of the 4 risks are manual-verification-surface risks
inherent to touching one shared component pair
(`WidgetTheme.kt`/`WidgetComponents.kt`) consumed by all 5 widget
classes — splitting this into siblings would not reduce that surface,
only add coordination overhead in a solo-maintained repo with no
cross-cutting domain/data/DI changes. The one risk that *is* a distinct
subsystem (`SizeMode.Single` → live-resize) is excluded from scope
entirely (see above) rather than split off as a sibling spec, since it
was not part of the user's stated ask.

## 4. Affected areas

| Area | Source set(s) | Class(es) / file(s) touched | Change type | Resolved by | Notes |
|--------|--------------|-------------------------------|-------------|-------------|-------|
| components (Android widget) | androidMain | `widget/components/WidgetTheme.kt` | modify | | consolidate size-class resolver; add spacing tokens; possible color-token alignment |
| components (Android widget) | androidMain | `widget/components/WidgetComponents.kt` | modify | | replace hardcoded spacing literals; extend typography to `LoadingWidget`/`WeatherWidgetErrorContent` |
| components (Android widget) | androidMain | `widget/components/WidgetTheme.kt` (color tokens) | TBD | Step 1 | only if Step 1 finds the widget palette diverges from the app's theme |
| — (reference only, no edits expected) | androidMain | `res/xml/widget_provider_info*.xml` | none | | confirms the 4 declared size buckets (128×48, 256×60, 256×120, 196×48 dp) manual verification must cover |

No `features/`, `domain/`, `data/`, or `di/` package is touched — this is
confined to the androidMain Glance presentation layer.

## 5. Architectural gauntlet (this repo's hard rules)

### 5a. Always explicit (no shortcut)

- [x] **Expect/actual parity** — N/A. `expect_actual_touched: no`. No
      commonMain `expect` declaration is touched; this spec is entirely
      androidMain Glance code with no shared/`commonMain` counterpart.
      Approach: confirmed — no expect/actual involved.
- [x] **Dual localization** — N/A. `localization_touched: no`. This spec
      changes visual tokens (spacing, size, color), not user-facing
      string content — no new/changed strings in `composeResources` or
      `iosApp/*.strings`.
      Approach: confirmed — no string changes.
- [x] **Secrets & logging** — confirmed. This spec touches only Glance
      layout/theme composables; no code path here reads, logs, or prints
      the WeatherAPI key or any credential.
      Approach: confirmed — no secret-adjacent code touched.
- [x] **No automated tests exist** — acknowledged. Verification is
      build-green (`./gradlew :composeApp:assembleDebug`) plus manual
      placement of all 5 widget variants on an Android
      emulator/device home screen in both locales — see plan.md Step 7.
      Acknowledged: yes

### 5b. Confinement-conditional

- [x] **Confinement claim** — change is confined to
      `composeApp/src/androidMain/kotlin/.../widget/components/`
      (`WidgetTheme.kt`, `WidgetComponents.kt`). No new `domain/repository`
      interface, no new Koin binding, no cross-feature boundary crossed
      (widget composables only read already-cached weather data via the
      existing `loadWeatherDataFromCache` path in the widget classes,
      which this spec does not touch). Neither rule below is reachable.

## 6. Skills

**Skills**: direct edits

## 7. Risks

- **Shared-component blast radius** — `WidgetTheme.kt`/`WidgetComponents.kt`
  are consumed by all 5 `GlanceAppWidget` classes (Small, Medium, Large,
  SmallWithDigitalClock, SmallWithAnalogClock); any token change ripples
  across every variant and must be re-verified on all of them, not just
  the one being visually reviewed.
- **No automated tests; wide manual QA surface** — verification requires
  manually placing all 5 widget variants on an emulator/device home
  screen in both supported locales (en/es, since string length differs
  and affects clipping) — 10 manual checks for one UI change.
- **Duplicated size-class logic being consolidated** — `rememberWidgetTypography()`
  and `rememberWidgetSizeClass()` currently branch on `LocalSize` independently;
  consolidating them (plan.md Step 2) must preserve existing breakpoint
  behavior for every current caller, or every widget's current (already
  font/icon-correct) layout regresses.
- **`SizeMode.Single` changes what "verify at every size" means** —
  `LocalSize.current` only reflects each widget's *declared* min size
  from its provider-info XML, not a live drag-resize of a placed
  instance. Manual verification (plan.md Step 7) must place each of the
  4 declared size buckets separately; dragging one placed widget's resize
  handles will not exercise the new spacing tokens and could produce a
  false "looks fine" result.

## Out-of-band actions

- Manual verification on an Android emulator or physical device is
  required for plan.md Step 7 (widget rendering cannot be verified by
  `./gradlew` alone).

## 8. Acceptance criteria

- [ ] **AC-1** — Weather and location icons render at a size proportional
      to the widget's size class (Small/Medium/Large) across all 5 widget
      variants — already largely true via `WidgetTypography`; verified
      not regressed after the spacing/consolidation changes.
- [ ] **AC-2** — Spacing and padding between elements (icon-to-text gaps,
      section gaps, content padding) scale by size class instead of using
      a single hardcoded value shared across Small/Medium/Large — verified
      by inspecting `WidgetComponents.kt` for the absence of bare `.dp`
      Spacer/padding literals in the touched composables.
- [ ] **AC-3** — `LoadingWidget` and `WeatherWidgetErrorContent` render
      using the same size-aware typography/spacing tokens as the normal
      content state, instead of their current fixed 44dp/13sp and
      36dp/16dp/13sp values.
- [ ] **AC-4** — Widget visual style (color, typography) is consistent
      with the main app's design system, or an intentional divergence is
      documented if Step 1 investigation finds Glance's theming API
      cannot match it exactly.
- [ ] **AC-5** — No text (temperature, location, condition, forecast
      labels) is clipped or overlaps in any of the 5 widget variants, in
      either locale (en/es), at each variant's declared size.
- [ ] **AC-6** — `./gradlew :composeApp:assembleDebug` succeeds.

## 9. Open questions

- **OQ-1** — Should true live continuous resize (`SizeMode.Responsive`/
  `SizeMode.Exact`, so a single placed widget rescales as the user drags
  it, instead of only the 3 separately-installable size variants) be
  scoped as a follow-up spec? Not a blocker for this spec — the intake
  wording matches the existing Small/Medium/Large variant model, and this
  spec explicitly excludes the `SizeMode` migration (see §3 Out of
  scope).
- **OQ-2** — Does `WidgetTheme.kt` currently define its own hardcoded
  color palette, or already reference the app's shared design tokens
  (via `GlanceTheme.colors` or similar)? Resolved by plan.md Step 1
  ([investigate]); its answer decides whether plan.md Step 6 (color
  alignment) runs at all.
  - **Resolved (2026-08-18, Step 1)**: `WidgetTheme.kt` defines no
    color tokens at all (`widget/components/WidgetTheme.kt:1-80` —
    confirmed via grep, zero `Color`/`GlanceTheme` references). All
    widget colors are hardcoded inline in
    `widget/components/WidgetComponents.kt` as
    `ColorProvider(Color.White, Color.White)` /
    `ColorProvider(Color(0xCCFFFFFF), Color(0xCCFFFFFF))` (white /
    ~80%-alpha white text), over a fixed background image
    (`WeatherWidgetBackground`, `WidgetComponents.kt:42-53`, uses
    `ImageProvider(R.drawable.bg_widget_glass)` — a "glass" style
    backdrop, not a theme color). The app's own Material3 scheme
    (`core/ui/components/theme/Color.kt`,
    `core/ui/components/theme/AppTheme.kt`) is a purple-toned
    light/dark scheme (`primaryLight = 0xFF68548E`,
    `backgroundLight = 0xFFFEF7FF`, plus a full dark scheme) — neither
    of which the widget references.
    **theme_divergence = no**: this is judged an *intentional*
    divergence, not drift — a home-screen widget must stay legible
    over arbitrary user wallpapers, so a fixed glass-style image
    background with white/translucent-white text (a common Android
    widget pattern) is a defensible, deliberate design choice
    independent of the in-app Material scheme, not a bug to reconcile.
    AC-4 is satisfied by documenting this divergence here rather than
    by code changes. Step 6 (conditional on `theme_divergence = yes`)
    is skipped.
- **OQ-3** — Should `LoadingWidget`/`WeatherWidgetErrorContent` reuse the
  exact same `WidgetTypography`/spacing instance as the surrounding
  content composable for that widget variant (simplest, and the default
  this plan assumes), or get their own smaller-footprint tokens since
  they show less content per state? Non-blocking — resolved during
  plan.md Step 5 implementation; default is "reuse the same tokens" unless
  investigation surfaces a reason not to.
  - **Resolved (2026-08-18, Step 5)**: default applied as-is — both
    states now call `rememberWidgetTypography()`/`rememberWidgetSpacing()`
    directly (icon → `t.weatherIconSize`, text → `t.conditionSize`,
    padding/gap → `s.contentPadding`/`s.sectionGap`), reusing the same
    per-size-class tokens as their surrounding content composable. No
    reason surfaced to diverge.

---

## Serena memories consulted

- `mem:architecture` — Clean Architecture layering, confirmed widget
  work has no `domain`/`data`/`di` surface.
- `mem:conventions` — confirmed no user-facing strings are touched, so
  the dual-localization rule is N/A; confirmed no lint/test tooling to
  invent.
- `mem:core` — confirmed `androidMain/widget/` (Glance) as the canonical
  location for Android widget code; no dedicated widget memory exists
  yet (candidate for a new memory once this spec lands — see
  `memory_maintenance.md` hygiene rules).
