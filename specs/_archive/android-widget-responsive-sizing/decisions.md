# Decisions log: android-widget-responsive-sizing

<!--
APPEND-ONLY LOG of non-obvious choices made during /spec-implement.
See .specs/templates/decisions.md for the format contract.
-->

## 2026-08-18 — Step 3 — Spacing token set design (5 tokens, not the 4 sketched in plan.md)

`WidgetTypography` had 8 tokens covering fonts/icons but zero for spacing, so `WidgetComponents.kt`
used ad-hoc `.dp` literals ranging 2–40dp across composables for visually distinct purposes (tight
icon-caption gaps inside forecast chips, wide gaps between side-by-side forecast items, outer
content padding). Plan.md's sketch (`contentPadding`, `itemSpacing`, `iconTextGap`, `sectionGap`)
collapsed two visually distinct magnitudes into one bucket, so a 5th token (`microGap`, ~2-4dp) was
added for the tightest gaps (stacked label/icon pairs, the two-line clock-widget layout) to avoid
either inflating those tiny gaps to `sectionGap` scale or cramming them into `iconTextGap`. Values
were chosen proportionally across Small/Medium/Large (roughly following the existing icon-size
spread) rather than preserving every original literal exactly — visual correctness is verified
manually in plan.md Step 7, not by literal value matching.

## 2026-08-18 — Step 5 — `WeatherWidgetErrorContent` double-applies background + padding (pre-existing, not fixed)

While extending `WeatherWidgetErrorContent` to use the new spacing/typography tokens (satisfying
AC-3), found it always renders nested inside `WeatherWidgetBackground` (confirmed via
`SmallWeatherGlanceWidget`/`MediumWeatherGlanceWidget`/`LargeWeatherGlanceWidget`'s `provideContent`
blocks — each wraps `WeatherWidgetErrorContent` in `WeatherWidgetBackground { ... }`), yet it also
calls `.background(ImageProvider(R.drawable.bg_widget_glass)).padding(...)` itself — the same
background image and padding are applied twice, stacking the padding (`contentPadding` from
`WeatherWidgetBackground` + `WeatherWidgetErrorContent`'s own). This is pre-existing (predates this
spec) and out of this spec's stated scope (extending size-awareness, not restructuring the
background/error composition), so it was left in place — only the hardcoded `16.dp` padding value
was tokenized to `s.contentPadding`, preserving the double-application. Worth a follow-up cleanup:
removing `WeatherWidgetErrorContent`'s own `.background()`/`.padding()` call, relying solely on the
`WeatherWidgetBackground` wrapper, would both simplify the composable and slightly reduce the error
state's effective inset (currently double `contentPadding`), which is favorable for AC-5 (no
clipping) on the Small widget's tight error layout.

## 2026-08-18 — Step 7 — Tablet-only clipping on the analog clock widget (out of scope, deferred to a follow-up spec)

Manual verification (all 5 variants, en/es, Loading + Error states) was clean on a Samsung S20FE
phone (5x6 launcher grid). On a Samsung Tab A9 tablet (8x6 grid, landscape), the analog clock
widget's date row (`Tue, 18 Aug`) renders visibly clipped at the bottom — confirmed via screenshots
comparing the two devices side by side. The other 4 variants looked intact on the tablet, just
tighter. Root cause is judged to be the tablet launcher's landscape grid giving this widget's host
container less height than its own declared minimum (`widget_provider_info` XML), not a
token-calibration defect in this spec's `WidgetSpacing`/`WidgetTypography` work — the exact same
composable and tokens render correctly on the phone. This sits adjacent to proposal.md's OQ-1
(`SizeMode.Single` vs. actual host-provided bounds) and is scoped out of this spec; a follow-up spec
should audit widget host-container sizing behavior across grid densities/orientations, starting from
the analog clock widget's declared `minWidth`/`minHeight`/`minResizeHeight` in its provider-info XML
and how Android's `AppWidgetHost` actually allocates space for it on higher-density/tablet grids.
