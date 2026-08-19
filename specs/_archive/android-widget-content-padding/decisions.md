# Decisions log: android-widget-content-padding

<!--
APPEND-ONLY LOG of non-obvious choices made during /spec-implement.

This file does NOT exist at /spec-plan time. /spec-implement creates it
from this template on the first non-obvious decision, and appends one
dated subsection per decision thereafter. If implementation surfaces
nothing non-obvious, the file stays absent — that is fine.

Each entry should read as a reusable-knowledge candidate: /spec-finalize
surfaces these entries as Serena memory candidates at archive time
(written under .serena/memories/, following the dense-bullet style
already used for this repo's seeded memories — plain markdown, no YAML
frontmatter). Capture what cannot be derived by reading the code today —
the "why", the trap, the alternative rejected — not a restatement of the
diff.

Rules:
- Append only. Never edit or delete an earlier entry.
- One subsection per decision; newest at the bottom.
- 2–5 sentences each. If it needs more, it is probably two decisions.
-->

## 2026-08-19 — Step 3b — Centering silently cancels padding; had to switch to Top alignment

Steps 2/3 shipped a working, bumped `contentPaddingHorizontal`/`contentPaddingVertical` token, but
the user reported zero visible on-device improvement. Root cause: all 5 widget content composables
center their text stack (`verticalAlignment = Alignment.CenterVertically`, plus
`horizontalAlignment = Alignment.CenterHorizontally` on the plain Small/Medium/Large ones) inside a
`fillMaxSize()` container that itself already has the padding applied one level up
(`WeatherWidgetBackground`'s `.padding(...)`). For a centered child, the visible edge margin
algebraically reduces to `containerSize/2 - contentSize/2` — the padding term cancels out exactly,
regardless of its value, as long as it doesn't force clipping. So no padding value, however large,
would have been visible until it grew large enough to start clipping content. The fix was changing
`verticalAlignment` to `Alignment.Top` (leaving horizontal alignment as originally designed) so the
outer padding becomes a real, uncancelled, guaranteed margin; flexible (`defaultWeight()`) spacers
were added between sections so any leftover room on a resized-taller widget still gets used instead
of collecting as dead space below the content. Alternative considered and rejected: fully centering
+ pumping padding until near the clipping threshold — rejected because the margin still wouldn't
scale with the token value, defeating the point of a tunable spacing system.

## 2026-08-19 — Step 3b (reverted) / Step 3c — Alignment change looked worse; the real culprit was the corner radius

The `Alignment.Top` change from the previous entry made the widgets look *worse* on-device (user
report) — likely because most widgets render taller than their natural content height, so
top-anchoring left a visibly empty, unbalanced strip at the bottom instead of the previous
(centered, if padding-blind) look. Reverted `verticalAlignment`/`horizontalAlignment` on all 5
composables back to their original `CenterVertically`/`CenterHorizontally` (or `Start` where that
was already the design, e.g. the clock widgets) — see git diff for the exact revert.

Investigated `widget/components/bg_widget_glass.xml` next (user's request) and found the real
secondary contributor: its corner radius was `20dp` on a Small widget that is only `48dp` tall —
the rounded corner alone consumes a curve nearly half the widget's height, so content anchored
near the top-left corner (e.g. the weather icon) visually reads as "cut by the curve" regardless of
the straight-line padding value, since padding (8-16dp depending on size class) is smaller than the
radius. Reduced the radius to `12dp` — still visually rounded, but no longer competing with the
padding tokens for the same corner-adjacent visual space. This was the missing piece: padding
alone can't create a "margin" it perceptually if a large corner radius already eats into the same
area from the start.

## 2026-08-19 — Step 5 — Clock widgets: 2-line condition text when resized taller

User request (out of the original padding scope, but small and related — implemented in the same
spec at their request rather than opening a new one). `WeatherWithAnalogClockContent` and
`WeatherWithDigitalClockContent` hardcoded `maxLines = 1` for the weather condition text
regardless of the widget's actual rendered size. Both widgets share `BaseWeatherGlanceWidget`'s
`SizeMode.Responsive(WIDGET_RESPONSIVE_SIZES)` candidate set (`128x48`, `196x48`, `256x60`,
`256x120`) — if the user drags a clock widget large enough, the system can hand it the `256x120`
candidate, so `LocalSize.current` genuinely does change on resize even though it's a fixed content
composable (no `Adaptive*` dispatch). Added `val size = LocalSize.current` and
`maxLines = if (size.height >= 100.dp) 2 else 1` — a raw literal comparison (matching
`AdaptiveWeatherWidgetContent`'s existing pattern), not `resolveWidgetSizeClass`/`SMALL_WIDGET_HEIGHT`,
because that constant is `private` to `WidgetTheme.kt` (file-private, not package-private in
Kotlin) and because `mem:android_widget_content_dispatch_size_classification` already documents
why content-dispatch decisions should use raw `LocalSize` comparisons rather than the
typography-tuned size-class resolver.

## 2026-08-19 — Step 6 — Medium/Large widgets couldn't shrink back down (missing minResizeWidth/Height)

Separate bug surfaced by the user while testing: placing the Large widget and then trying to
shrink it did not transform its content to Medium/Small, even though `LargeWeatherGlanceWidget`
(and `MediumWeatherGlanceWidget`) already call `AdaptiveWeatherWidgetContent` and should adapt.
Root cause: `widget_provider_info_large.xml` and `widget_provider_info_medium.xml` declared only
`android:minWidth`/`android:minHeight` (256x120 and 256x60 respectively) with no
`android:minResizeWidth`/`android:minResizeHeight`. Per the `AppWidgetProviderInfo` platform
contract, an unset `minResizeWidth`/`minResizeHeight` defaults to `minWidth`/`minHeight` — so the
launcher physically would not let the user drag either widget smaller than its own full declared
size, and `LocalSize.current` never received a smaller candidate to dispatch on. Fixed by adding
`android:minResizeWidth="128dp"` / `android:minResizeHeight="48dp"` (the smallest candidate in
`WIDGET_RESPONSIVE_SIZES`) to both provider XMLs. The Small widget's own provider XML already
declares `minWidth="128dp" minHeight="48dp"` — already the smallest candidate — so it needed no
change (its adaptive behavior on shrink was never blocked).
