# Android widget: size-class resolution & spacing tokens

- `widget/components/WidgetTheme.kt` is the single source of truth for widget size-class
  resolution: `resolveWidgetSizeClass(size: DpSize): WidgetSizeClass` (private) is called by
  both `rememberWidgetTypography()` and `rememberWidgetSizeClass()` — never re-derive the
  `SMALL_WIDGET_WIDTH`/`MEDIUM_WIDGET_WIDTH`/`SMALL_WIDGET_HEIGHT` breakpoint logic elsewhere;
  add a new size-dependent value by branching on `resolveWidgetSizeClass(LocalSize.current)`.
- `WidgetSpacing` (Small/Medium/Large instances, exposed via `rememberWidgetSpacing()`) mirrors
  `WidgetTypography`'s pattern and holds 5 tokens: `contentPadding`, `iconTextGap`, `microGap`,
  `sectionGap`, `itemSpacing`. 5, not 4 — `microGap` (~2-4dp) exists specifically for the
  tightest gaps (stacked label/icon pairs, the two-line clock-widget layout) that don't fit
  either `iconTextGap` or `sectionGap`'s scale without visually inflating or cramming them.
- Every composable in `widget/components/WidgetComponents.kt` (including `LoadingWidget` and
  `WeatherWidgetErrorContent`) calls `rememberWidgetTypography()`/`rememberWidgetSpacing()`
  directly rather than threading `WidgetTypography`/`WidgetSpacing` as parameters — Glance
  composables can freely call other `@Composable` functions for local values regardless of what
  the caller passes, so no signature changes were needed to wire a new leaf composable in.
- Token dp values are chosen proportionally across Small/Medium/Large (not preserving every
  pre-existing literal), so a new token addition should also scale proportionally rather than
  match old hardcoded values exactly — visual correctness for widgets is verified by manual
  placement on a device (see `mem:task_completion`), not literal-value matching.
- `GlanceAppWidget.sizeMode` is not overridden (defaults to `SizeMode.Single`): `LocalSize.current`
  always reflects the *declared* min size from the widget's `res/xml/widget_provider_info*.xml`,
  never a live drag-resize of an already-placed instance.
