---
spec_id: android-widget-content-padding
generated_by: /spec-plan
generated_at: 2026-08-19T00:00:00Z
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

# Plan: Increase Android widget content padding so text doesn't sit flush against the widget's edges

## Strategy

Split `WidgetSpacing.contentPadding` (a single uniform dp value) into independent horizontal and
vertical fields, then raise horizontal generously and vertical conservatively — conservatively on
Small in particular, since the two clock-variant widgets are permanently pinned to `SMALL` at a
48dp declared minimum height with little vertical slack (see proposal.md Risks). Step 1
enumerates every consumer of the old field before it's renamed, so the rename in Step 2 can't
leave a dangling reference; Step 3 updates those consumers; Step 4 is the manual on-device check
this repo relies on in place of automated tests.

## Steps

### Step 1 — Enumerate all consumers of `WidgetSpacing.contentPadding` [investigate]

- **Files / symbols**:
  - `composeApp/src/androidMain/kotlin/com/kronos/multiplatform/weatherapp/widget/components/WidgetTheme.kt` — `WidgetSpacing` struct definition
  - `composeApp/src/androidMain/kotlin/com/kronos/multiplatform/weatherapp/widget/components/WidgetComponents.kt` — known consumers `WeatherWidgetBackground`, `WeatherWidgetErrorContent`
- **Question(s) to answer**: does any file outside `widget/components/WidgetComponents.kt`
  reference `.contentPadding` on a `WidgetSpacing`/`rememberWidgetSpacing()` result? (Use
  `find_referencing_symbols` on the `contentPadding` property.)
- **Outputs to record**: the exact list of call sites in this step's notes (or directly in the
  Step 2/3 file lists if `/spec-implement` inlines them) — this is what makes the Step 2 rename
  safe to execute without a stray unresolved-reference build failure.
- **Why**: proposal.md Risks flags the rename as a breaking signature change; must be exhaustive
  before touching the struct.

### Step 2 — Split and increase the content-padding tokens [implement]

- **Skill**: direct edits
- **Area(s)**: `widget/components` (androidMain)
- **Files / symbols**:
  - `composeApp/src/androidMain/kotlin/com/kronos/multiplatform/weatherapp/widget/components/WidgetTheme.kt` —
    `WidgetSpacing` struct: replace `contentPadding: Dp` with `contentPaddingHorizontal: Dp` and
    `contentPaddingVertical: Dp`.
  - Same file — `SmallSpacing`: `contentPaddingHorizontal = 12.dp` (was `contentPadding = 8.dp`),
    `contentPaddingVertical = 8.dp` (unchanged — protects the 48dp-min-height clock widgets from
    clipping, see proposal.md Risks).
  - Same file — `MediumSpacing`: `contentPaddingHorizontal = 14.dp`, `contentPaddingVertical = 12.dp`
    (was `contentPadding = 10.dp` for both).
  - Same file — `LargeSpacing`: `contentPaddingHorizontal = 16.dp`, `contentPaddingVertical = 14.dp`
    (was `contentPadding = 12.dp` for both).
- **Skill args / inputs**: none
- **Why**: proposal.md §2/§3 — widen the edge-to-content inset, horizontal more than vertical on
  Small specifically to avoid clipping the clock widgets' 3 stacked text lines.
- **Verification**: symbol `WidgetSpacing` no longer declares `contentPadding`; all three spacing
  instances compile with the two new named args.

### Step 3 — Update token consumers to the new field names [implement]

- **Skill**: direct edits
- **Area(s)**: `widget/components` (androidMain)
- **Files / symbols**:
  - `composeApp/src/androidMain/kotlin/com/kronos/multiplatform/weatherapp/widget/components/WidgetComponents.kt` —
    `WeatherWidgetBackground`: `.padding(horizontal = s.contentPadding, vertical = s.contentPadding)`
    → `.padding(horizontal = s.contentPaddingHorizontal, vertical = s.contentPaddingVertical)`.
  - Same file — `WeatherWidgetErrorContent`: its own duplicate `.padding(s.contentPadding)` call
    (see proposal.md §3 Out of scope re: the pre-existing double-apply — only the field reference
    changes here, the double-apply itself is preserved) → update to the two new fields the same
    way.
  - Any additional call site recorded by Step 1.
- **Skill args / inputs**: none
- **Why**: Step 2 renamed the field these call sites read.
- **Verification**: `./gradlew :composeApp:assembleDebug` green (no unresolved-reference errors).

### Step 3b — Fix vertical centering that cancels the padding token [implement] — REVERTED

> **Reverted** (see `decisions.md`): this alignment change made the widgets look worse on-device
> (most widgets render taller than their natural content height, so top-anchoring left a visibly
> empty, unbalanced strip at the bottom). All 5 composables were reverted to their original
> `CenterVertically`/`CenterHorizontally` (or `Start`, where that was already the design). See
> Step 3c for the fix that replaced this approach.

- **Skill**: direct edits
- **Area(s)**: `widget/components` (androidMain)
- **Files / symbols**:
  - `composeApp/src/androidMain/kotlin/com/kronos/multiplatform/weatherapp/widget/components/WidgetComponents.kt` —
    `SmallWeatherWidgetContent`, `MediumWeatherWidgetContent`, `LargeWeatherWidgetContent`,
    `WeatherWithAnalogClockContent`, `WeatherWithDigitalClockContent`: change the outer/text-stack
    `Column`'s `verticalAlignment` from `Alignment.CenterVertically` to `Alignment.Top`. Horizontal
    alignment is left unchanged (still `CenterHorizontally` for the plain Small/Medium/Large
    widgets, still `Start` for the two clock widgets — this step only touches the vertical axis).
    Add `Spacer(GlanceModifier.defaultWeight())` after each existing fixed-height section spacer
    and a trailing one after the last element, so any leftover vertical room (widget resized taller
    than its natural content height) is distributed across the internal gaps instead of collecting
    as unused space below the content block.
- **Skill args / inputs**: none
- **Why**: discovered after Step 2/3 shipped with no visible on-device improvement (user report).
  Root cause: when content is *centered* within a `fillMaxSize()` container, the visible edge
  margin is `containerSize/2 - contentSize/2` — algebraically independent of any padding applied
  to that same container, as long as the padding doesn't force clipping (padding term cancels
  exactly). `WidgetSpacing.contentPaddingVertical` was therefore invisible on all 5 widgets before
  this step, regardless of its value. Switching to `Alignment.Top` makes the outer padding the
  real, guaranteed, controllable top margin. See `decisions.md` for the full derivation.
- **Verification**: `./gradlew :composeApp:assembleDebug` green; grep confirms no remaining
  `Alignment.CenterVertically` on the 5 composables' outer/text-stack `Column`. **(Reverted — see
  banner above.)**

### Step 3c — Reduce the widget background's corner radius [implement]

- **Skill**: direct edits
- **Area(s)**: `widget/components` (androidMain resources)
- **Files / symbols**:
  - `composeApp/src/androidMain/res/drawable/bg_widget_glass.xml` — `<corners android:radius="20dp" />`
    → `12dp`.
- **Skill args / inputs**: none
- **Why**: `20dp` on a `48dp`-tall Small widget consumes a curve nearly half the widget's height,
  so corner-adjacent content (the weather icon, top-left) visually reads as clipped by the curve
  regardless of the straight-line padding value. Confirmed with the user (`AskUserQuestion`) before
  changing; chosen over a bigger padding bump because it doesn't reduce the clock widgets' already
  tight 48dp vertical content budget. See `decisions.md`.
- **Verification**: `./gradlew :composeApp:assembleDebug` green.

### Step 4 — Manually verify all 5 widget variants on-device [verify]

- **What to check**: place (or resize an existing placement of) each of the 5 widgets — Small,
  Medium, Large, Small+AnalogClock, Small+DigitalClock — on an Android emulator or device home
  screen. Compare content-to-edge spacing against the pre-change build (or the user's reference
  screenshot). Specifically check the two clock-variant widgets for text clipping/overlap given
  their fixed 48dp height.
- **Pass criteria**: all 5 variants show visibly more breathing room between content and the
  widget's rounded background edge than before; no clipped or overlapping text on either
  clock-variant widget. If Small's vertical spacing still reads as insufficient, or a variant
  clips, hand-tune the dp values from Step 2 and re-check — this is expected per
  `mem:android_widget_spacing_tokens` ("visual correctness for widgets is verified by manual
  placement on a device").

## Dependencies

- Step 2 and Step 3 depend on Step 1's consumer list being complete.
- Step 4 depends on Step 2 and Step 3 both being build-green.

## Out-of-band actions

- None.

## Rollback

If Step 2/3 leaves the build red or Step 4 surfaces clipping that isn't resolved by hand-tuning
the dp values, `git restore` `WidgetTheme.kt` and `WidgetComponents.kt` (the only two files
touched) to return to the pre-change token values.
