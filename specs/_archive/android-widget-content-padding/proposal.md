---
spec_id: android-widget-content-padding
title: Increase Android widget content padding so text doesn't sit flush against the widget's edges
type: chore
priority: low
source: manual
source_ref: user request in chat, 2026-08-19
created: 2026-08-19
status: INTAKE_PARSED
recommend_split: no
blockers: []
depends_on: []
expect_actual_touched: no
localization_touched: no
branch_suggested: chore/android-widget-content-padding
---

# Proposal: Increase Android widget content padding so text doesn't sit flush against the widget's edges

## 1. Source

Manual intake (`specs/android-widget-content-padding/story.md`). The user described the Android
home-screen widget's internal spacing as cramped and attached a reference screenshot of the
Small-with-analog-clock widget (dark theme): the weather icon, "29.1°C", condition text, and
location pin sit close to the widget's top/left edges, and the analog clock + date sit close to
the right edge.

## 2. Problem / Why

This affects **all 5 declared Android home-screen widgets** (Small, Medium, Large,
Small+AnalogClock, Small+DigitalClock), not just one variant. All widget content is inset from the
rounded-corner background by a single `contentPadding` token per size class
(`WidgetSpacing.contentPadding`, defined in `widget/components/WidgetTheme.kt`: `8.dp` Small /
`10.dp` Medium / `12.dp` Large), applied uniformly on all four sides by `WeatherWidgetBackground`
in `widget/components/WidgetComponents.kt`
(`.padding(horizontal = s.contentPadding, vertical = s.contentPadding)`). Every one of the 5
widgets resolves to one of these three token sets, so every one of them reads as tighter than it
should — the fix touches the shared token source, not a single widget.

The problem is most visually evident on the Small-with-analog-clock widget (the user's reference
screenshot) because it's pinned to the tightest token set (`8.dp`) permanently: per
`mem:android_widget_content_dispatch_size_classification`, both
`SmallWeatherWithAnalogClockGlanceWidget` and `SmallWeatherWithDigitalClockGlanceWidget` declare a
`48dp` minimum height (`widget_provider_info_with_analog_clock.xml` / `_with_digital_clock.xml`),
which is below `SMALL_WIDGET_HEIGHT` and therefore always resolves to `WidgetSizeClass.SMALL` —
unlike the plain Small widget, these two can never "graduate" to a roomier token set by resizing.
Medium and Large read as less cramped today only because their tokens are already a bit larger
(`10.dp`/`12.dp`), not because they're exempt from the same problem.

## 3. Scope

**In scope**
- Increase the widget content-inset tokens in `WidgetTheme.kt` so content reads with more
  breathing room from the widget's edge, across all 5 declared widget variants (Small, Medium,
  Large, Small+AnalogClock, Small+DigitalClock).
- Split the single `contentPadding` token into independent horizontal/vertical values so
  horizontal room can be increased generously while vertical stays conservative on the
  permanently-`SMALL`-classified clock widgets (48dp declared min height leaves little vertical
  slack for 3 stacked text lines — see Risks).
- Update both consumers of the token (`WeatherWidgetBackground` and `WeatherWidgetErrorContent`)
  to the new field names.
- **(Added after Step 2/3 shipped with no visible on-device improvement — attempted, then reverted;
  see `decisions.md`)** An alignment change (`Alignment.CenterVertically` → `Alignment.Top`) was
  tried and reverted after the user reported it looked worse (top-anchoring left an unbalanced
  empty strip at the bottom on widgets rendered taller than their natural content height). All 5
  composables are back to their original alignment.
- **(Added after the above — the actual fix)** Reduce `widget/components/bg_widget_glass.xml`'s
  corner radius from `20dp` to `12dp`. On the Small widget (`48dp` declared height), a `20dp`
  radius consumes a curve nearly half the widget's height, so corner-adjacent content (the weather
  icon) visually reads as clipped by the curve regardless of the padding token's value — this, not
  centering, was the dominant reason the padding increase read as invisible. Confirmed with the
  user before applying.

**Out of scope**
- Fixing `WeatherWidgetErrorContent`'s pre-existing double background/padding application
  (`mem:android_widget_error_content_double_padding`) — a real cleanup candidate, but a separate
  concern from token *values*. Only the field rename propagates into it here; its double-apply
  behavior is preserved.
- Any change to `WidgetTypography` (font sizes), `iconTextGap`/`microGap`/`sectionGap`/
  `itemSpacing` (inter-element spacing) — only the outer content-to-edge inset is in scope.
- Any change to `SizeMode.Responsive` candidates, size-class breakpoints, or which composable a
  widget dispatches to.

## 4. Affected areas

| Area | Source set(s) | Class(es) / file(s) touched | Change type | Resolved by | Notes |
|--------|--------------|-------------------------------|-------------|-------------|-------|
| Android widget theming | androidMain | `widget/components/WidgetTheme.kt` — `WidgetSpacing` struct, `SmallSpacing`/`MediumSpacing`/`LargeSpacing` | modify | | Replace single `contentPadding: Dp` field with `contentPaddingHorizontal: Dp` / `contentPaddingVertical: Dp`; bump values |
| Android widget components | androidMain | `widget/components/WidgetComponents.kt` — `WeatherWidgetBackground`, `WeatherWidgetErrorContent` | modify | | Update `.padding(...)` calls to the two new fields |
| Android widget components (reference check) | androidMain | any other call site reading `WidgetSpacing.contentPadding` | TBD | Step 1 | Confirmed exhaustively via `find_referencing_symbols` before renaming the field |
| Android widget content composables | androidMain | `widget/components/WidgetComponents.kt` — `SmallWeatherWidgetContent`, `MediumWeatherWidgetContent`, `LargeWeatherWidgetContent`, `WeatherWithAnalogClockContent`, `WeatherWithDigitalClockContent` | reverted (no net change) | Step 3b | Alignment change tried and reverted — see Scope addendum above and `decisions.md` |
| Android widget background drawable | androidMain resources | `widget/components/bg_widget_glass.xml` — `<corners android:radius>` | modify | Step 3c | `20dp → 12dp` — see Scope addendum above |

## 5. Architectural gauntlet (this repo's hard rules)

### 5a. Always explicit (no shortcut)

- [x] **Expect/actual parity** — N/A. No `expect`/`actual` declaration is touched; this is
      Android-only `androidMain` widget code (Glance has no multiplatform counterpart in this
      app). `expect_actual_touched: no`.
      Approach: confirmed by investigation — no `expect` in `widget/` package.
- [x] **Dual localization** — N/A. No user-facing string is added or changed; this is a purely
      visual spacing token change. `localization_touched: no`.
      Approach: confirmed — no string resource touched.
- [x] **Secrets & logging** — confirmed. No API keys or credentials anywhere near this change.
      Approach: confirmed.
- [x] **No automated tests exist** — acknowledged. Verification is build-green
      (`./gradlew :composeApp:assembleDebug`) plus a manual placement of each of the 5 widget
      variants on an Android emulator/device home screen, per `mem:android_widget_spacing_tokens`
      ("visual correctness for widgets is verified by manual placement on a device").
      Acknowledged: yes.

### 5b. Confinement-conditional

- [x] **Confinement claim** — change is confined to `androidMain`'s `widget/components/` package
      (`WidgetTheme.kt` token definitions + `WidgetComponents.kt` consumers). No new domain
      interface, no new Koin binding, no cross-feature boundary crossed. Neither rule below is
      reachable.

## 6. Skills

**Skills**: direct edits

## 7. Risks

- Both clock-variant widgets (`SmallWeatherWithAnalogClockGlanceWidget`,
  `SmallWeatherWithDigitalClockGlanceWidget`) declare a `48dp` minimum height and are permanently
  classified `WidgetSizeClass.SMALL` (they never resize into Medium/Large token territory — see
  `mem:android_widget_content_dispatch_size_classification`). Their vertical content (icon+temp
  row, condition row, location row, each separated by `microGap`) already nearly fills that 48dp
  at the current `8.dp` vertical inset. Increasing Small's *vertical* padding risks text clipping
  on these two widgets specifically — mitigated by keeping Small's vertical token conservative
  while increasing horizontal generously (see Step 2), but must be visually confirmed on-device.
- `WeatherWidgetErrorContent` double-applies the content-padding token
  (`mem:android_widget_error_content_double_padding`, pre-existing, out of scope to fix here). Its
  effective inset will shift by roughly double whatever delta this spec applies to the vertical/
  horizontal tokens — the error/loading widget appearance must be visually re-checked too, even
  though the double-application itself is left in place.
- Renaming `WidgetSpacing.contentPadding` to two fields is a breaking signature change for any
  call site not yet enumerated — Step 1's `find_referencing_symbols` pass must be exhaustive
  before Step 2/3 touch the struct, or the build will fail with unresolved references.

## Out-of-band actions

- None.

## 8. Acceptance criteria

- [x] **AC-1** — `WidgetSpacing` (in `WidgetTheme.kt`) exposes independent
      `contentPaddingHorizontal` / `contentPaddingVertical` tokens (replacing the single
      `contentPadding` field) for `SmallSpacing`, `MediumSpacing`, and `LargeSpacing`, with
      increased values per plan.md Step 2's target numbers (or a visually-tuned adjustment made
      during Step 4's on-device check).
- [x] **AC-2** — `WeatherWidgetBackground` and `WeatherWidgetErrorContent` in
      `WidgetComponents.kt` consume the two new fields (no remaining reference to the old
      `contentPadding` field name anywhere in the codebase).
- [x] **AC-3** — `./gradlew :composeApp:assembleDebug` is green after the change.
- [x] **AC-4** — Manually placing each of the 5 widget variants (Small, Medium, Large,
      Small+AnalogClock, Small+DigitalClock) on an Android emulator/device home screen shows
      visibly increased breathing room between content (icon, temperature, condition text,
      location, clock/date) and the widget's rounded-corner background edge, compared to the
      current build, with **no text clipping or line overlap** on either clock-variant widget.
      Achieved via the padding tokens (AC-1) **plus** reducing `bg_widget_glass.xml`'s corner
      radius `20dp → 12dp` (Step 3c) — the padding alone wasn't visually sufficient because the
      prior 20dp radius consumed most of the available corner-adjacent margin on the 48dp-tall
      Small widget. User-confirmed on-device.
- [x] **AC-5** *(added — Step 5)* — The two clock-variant widgets
      (`WeatherWithAnalogClockContent`, `WeatherWithDigitalClockContent`) show up to 2 lines of
      weather-condition text when resized tall enough (`LocalSize.current.height >= 100.dp`),
      instead of always truncating to 1 line. User-confirmed on-device.
- [x] **AC-6** *(added — Step 6)* — The Medium and Large widgets can be resized smaller on the
      home screen and their content correctly transforms via `AdaptiveWeatherWidgetContent` (was
      previously blocked — see `decisions.md` Step 6). User-confirmed on-device.

## 9. Open questions

- **OQ-1** *(resolved)* — The exact target `contentPaddingHorizontal`/`contentPaddingVertical` dp
  values (Small: 8→12dp horizontal, vertical unchanged at 8dp; Medium: 10→14dp horizontal,
  10→12dp vertical; Large: 12→16dp horizontal, 12→14dp vertical) plus the corner-radius reduction
  (20dp → 12dp) were confirmed sufficient by the user's on-device check (Step 4, round 4). No
  further tuning requested.

---

## Serena memories consulted

- `mem:android_widget_spacing_tokens` — `WidgetTheme.kt`'s size-class resolver and the
  `WidgetTypography`/`WidgetSpacing` token pattern (5 tokens, proportional scaling convention,
  manual-verification convention).
- `mem:android_widget_error_content_double_padding` — the pre-existing double background/padding
  application in `WeatherWidgetErrorContent`, relevant because this spec's field rename touches
  that same call site without fixing the underlying double-apply.
- `mem:android_widget_content_dispatch_size_classification` — why the two clock-variant widgets
  are permanently classified `SMALL` regardless of their actual rendered width, which drives the
  Risks section's vertical-padding caution.
- `mem:core` — repo source map and cross-links to the other widget memories.
