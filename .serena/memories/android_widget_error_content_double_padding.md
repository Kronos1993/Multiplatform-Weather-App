# Android widget: WeatherWidgetErrorContent double-applies background + padding

- `widget/components/WidgetComponents.kt`'s `WeatherWidgetErrorContent` always renders nested
  inside `WeatherWidgetBackground` (every `*GlanceWidget.kt`'s `provideContent` block wraps it:
  `WeatherWidgetBackground { ... WeatherWidgetErrorContent(...) }`).
- `WeatherWidgetErrorContent` ALSO calls its own `.background(ImageProvider(R.drawable.bg_widget_glass))`
  + `.padding(s.contentPadding)` — the glass background image and content padding are applied
  twice (once by the wrapper, once by the composable itself), stacking the padding.
- Pre-existing since before the widget-responsive-sizing spec; left in place there as out of
  scope (only the padding's hardcoded `16.dp` was tokenized, preserving the double-application).
  A real cleanup candidate: dropping `WeatherWidgetErrorContent`'s own `.background()`/`.padding()`
  call and relying solely on the `WeatherWidgetBackground` wrapper would both simplify it and
  reduce the error state's effective inset, which matters on the Small widget's tight layout.
