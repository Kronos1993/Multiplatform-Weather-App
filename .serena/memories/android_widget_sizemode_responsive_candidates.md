# Android widget: SizeMode.Responsive candidate-set design principle

- `BaseWeatherGlanceWidget.sizeMode = SizeMode.Responsive(WIDGET_RESPONSIVE_SIZES)` (shared
  across all 5 subclasses via inheritance — no subclass overrides `sizeMode` itself) —
  candidate set is exactly the 4 *distinct* declared minimums across all
  `widget_provider_info*.xml` files: `DpSize(128,48)` Small, `DpSize(196,48)` both clock
  variants, `DpSize(256,60)` Medium, `DpSize(256,120)` Large.
- Deliberately NOT synthetic in-between values (e.g. a `180x110` midpoint) —
  `SizeMode.Responsive` pins `LocalSize.current` to exactly one candidate from the set, so
  using only real, already-designed breakpoints keeps every consumer (typography tokens,
  content dispatch) reasoning about 4 known, individually-verified combinations instead of an
  unbounded continuous range.
- A synthetic midpoint was tried on paper and rejected: it can resolve to a *different* size
  class under typography's `resolveWidgetSizeClass` than under a width-primary content-dispatch
  scheme, i.e. bigger fonts rendered inside a smaller/tighter layout — a real clipping risk.
- See `mem:android_widget_content_dispatch_size_classification` for the related dispatch-side
  finding, and `mem:android_widget_spacing_tokens` for the pre-existing token system this feeds.
