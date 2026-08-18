---
spec_id: android-widget-responsive-sizing
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

# Plan: Complete size-responsive typography, icon, spacing, and style system for Android home-screen widgets

## Strategy

Work bottom-up through the existing size-adaptive system in
`widget/components/`: first consolidate the duplicated size-class
resolver so later steps have one source of truth, then extend the
existing `WidgetTypography` token set with spacing/density tokens, then
apply those tokens across the content composables and the two orphaned
states (`LoadingWidget`, `WeatherWidgetErrorContent`), then verify style
parity, then verify visually across all 5 widget variants. Investigation
(Step 1) runs first because Step 6 (color alignment) is conditional on
its answer.

## Steps

### Step 1 — Check widget color/style tokens against the app's design system [investigate]

- **Files / symbols**:
  - `composeApp/src/androidMain/kotlin/com/kronos/multiplatform/weatherapp/widget/components/WidgetTheme.kt` — read in full, note every color reference/definition
  - Search `composeApp/src/commonMain/kotlin/.../` for the app's shared theme/color source (e.g. a `Theme.kt`/`Color.kt` under a `components`/`core` package) to compare against
- **Question(s) to answer**:
  - Does `WidgetTheme.kt` define its own hardcoded color values, or already reference `GlanceTheme.colors` / a shared token set derived from the app's Material theme? (proposal.md OQ-2)
  - If hardcoded, do the values visually match the app's current palette, or have they drifted?
- **Outputs to record**: proposal.md OQ-2 answer; a `Step 1 result: theme_divergence = yes|no` marker gating Step 6.
- **Why**: AC-4 (visual parity with the app) cannot be answered without knowing the current state of the widget's color tokens.

### Step 2 — Consolidate the duplicated size-class resolver [implement]

- **Skill**: direct edits
- **Area(s)**: `components` (Android widget)
- **Files / symbols**:
  - `composeApp/src/androidMain/kotlin/.../widget/components/WidgetTheme.kt` — extract a single `resolveWidgetSizeClass(size: DpSize): WidgetSizeClass` (or equivalent) that both `rememberWidgetTypography()` and `rememberWidgetSizeClass()` delegate to, replacing their independent `LocalSize`-branching logic
- **Skill args / inputs**: none
- **Why**: eliminates the drift risk called out in proposal.md §7 before any new token is layered on top
- **Verification**: `./gradlew :composeApp:assembleDebug` green; `resolveWidgetSizeClass` (or equivalent) exists and both `rememberWidgetTypography()`/`rememberWidgetSizeClass()` call it (no independent breakpoint literals remain in either function)

### Step 3 — Add size-class-aware spacing/density tokens [implement]

- **Skill**: direct edits
- **Area(s)**: `components` (Android widget)
- **Files / symbols**:
  - `composeApp/src/androidMain/kotlin/.../widget/components/WidgetTheme.kt` — add spacing fields (e.g. `contentPadding`, `itemSpacing`, `iconTextGap`, `sectionGap`) to `WidgetTypography` (or a parallel token set with its own `rememberWidgetSpacing()`), with Small/Medium/Large values proportional to the existing font/icon token spread
- **Skill args / inputs**: none
- **Why**: this is the actual gap identified in proposal.md §2 — spacing is currently the only token category NOT size-aware
- **Verification**: `./gradlew :composeApp:assembleDebug` green; the new spacing fields/tokens exist on all three size instances

### Step 4 — Replace hardcoded spacing literals in the content composables [implement]

- **Skill**: direct edits
- **Area(s)**: `components` (Android widget)
- **Files / symbols**:
  - `composeApp/src/androidMain/kotlin/.../widget/components/WidgetComponents.kt` — `WeatherWidgetBackground`, `SmallWeatherWidgetContent`, `MediumWeatherWidgetContent`, `LargeWeatherWidgetContent`, `WeatherWithAnalogClockContent`, `WeatherWithDigitalClockContent`, `LocationRow`, `WeatherDetailRow`, `ForecastDayCompact`, `ForecastDayFull` — replace every hardcoded `Spacer`/`padding` `.dp` literal with the Step 3 spacing tokens
- **Skill args / inputs**: none
- **Why**: satisfies AC-2 directly
- **Verification**: `./gradlew :composeApp:assembleDebug` green; grep for bare `.dp` literals inside `Spacer(...)`/`.padding(...)` calls in these composables returns none (all route through the spacing tokens)

### Step 5 — Extend size-aware tokens to Loading and Error states [implement]

- **Skill**: direct edits
- **Area(s)**: `components` (Android widget)
- **Files / symbols**:
  - `composeApp/src/androidMain/kotlin/.../widget/components/WidgetComponents.kt` — `LoadingWidget` (currently fixed 44.dp icon / 13.sp text), `WeatherWidgetErrorContent` (currently fixed 36.dp icon / 16.dp padding / 13.sp text) — both call `rememberWidgetTypography()`/`rememberWidgetSpacing()` per proposal.md OQ-3's default (reuse the same tokens as the surrounding content)
- **Skill args / inputs**: none
- **Why**: satisfies AC-3; these two states are the only composables entirely outside the size-adaptive system today
- **Verification**: `./gradlew :composeApp:assembleDebug` green; `LoadingWidget`/`WeatherWidgetErrorContent` no longer contain the fixed `44.dp`/`13.sp`/`36.dp`/`16.dp` literals

### Step 6 — Align widget color/style tokens with the app theme [implement, conditional]

- **Condition**: Step 1 result: theme_divergence = yes
- **Skill**: direct edits
- **Area(s)**: `components` (Android widget)
- **Files / symbols**:
  - `composeApp/src/androidMain/kotlin/.../widget/components/WidgetTheme.kt` — update color definitions per Step 1's findings
- **Why**: satisfies AC-4 only if Step 1 found an actual divergence to fix
- **Verification**: `./gradlew :composeApp:assembleDebug` green

### Step 7 — Manual visual verification across all widget variants [verify]

- **What to check**: place all 5 widgets (Small, Medium, Large,
  SmallWithDigitalClock, SmallWithAnalogClock) on an Android
  emulator/device home screen, once in English and once in Spanish
  system locale; confirm icons/text render proportionally, spacing no
  longer looks "stretched" at Large vs "cramped" at Small, and both the
  Loading and Error states (trigger by disabling network briefly) render
  consistently with the normal content state.
- **Pass criteria**: no clipped or overlapping text in any of the 5
  variants in either locale; Loading/Error states visually match the
  content state's density (AC-1, AC-2, AC-3, AC-5).

### Step 8 — Build verification [verify]

- **What to check**: `./gradlew :composeApp:assembleDebug`
- **Pass criteria**: build succeeds with no errors (AC-6).

## Dependencies

- Step 2 (resolver consolidation) must land before Step 3 (new tokens
  built on the same resolver) to avoid layering new tokens on top of
  soon-to-be-refactored branching logic.
- Step 6 only runs if Step 1 finds a divergence — see its Condition.
- Step 7 depends on Steps 2–6 (or 2–5 if Step 6 is skipped) all being
  build-green first.

## Out-of-band actions

- Step 7 requires a human running the Android emulator or a physical
  device — it cannot be automated headlessly.

## Rollback

If any step fails partway, `git restore` the two touched files
(`WidgetTheme.kt`, `WidgetComponents.kt`) to their pre-spec state. Since
all changes are confined to these two files with no DB migration, no
Koin wiring, and no expect/actual pair, a full revert is a single `git
restore` — no partial-migration cleanup is possible or needed.
