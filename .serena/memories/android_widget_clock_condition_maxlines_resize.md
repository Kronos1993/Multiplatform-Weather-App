# Android widget: clock-variant widgets DO see a changed LocalSize on resize, despite no adaptive dispatch

- `WeatherWithAnalogClockContent`/`WeatherWithDigitalClockContent` (in `WidgetComponents.kt`) have
  no `Adaptive*`-style size dispatch (unlike `AdaptiveWeatherWidgetContent` for Small/Medium/Large)
  — they always render the same fixed composable. It's tempting to assume `LocalSize.current`
  therefore never changes for them, but that's wrong: both clock widgets share
  `BaseWeatherGlanceWidget`'s `SizeMode.Responsive(WIDGET_RESPONSIVE_SIZES)` candidate set (`128x48`,
  `196x48`, `256x60`, `256x120` — see `mem:android_widget_sizemode_responsive_candidates`), and if
  the user drags a clock widget large enough, the system can hand it the `256x120` candidate even
  though it has no adaptive layout-switching logic of its own.
- Used this to let the weather-condition `Text` grow from 1 to 2 `maxLines` when resized tall
  enough: added `val size = LocalSize.current` and
  `maxLines = if (size.height >= 100.dp) 2 else 1` in both composables.
- Used a **raw literal `100.dp` comparison**, not `resolveWidgetSizeClass`/`SMALL_WIDGET_HEIGHT`,
  for two reasons: (1) `SMALL_WIDGET_HEIGHT` is `private` at file scope in `WidgetTheme.kt` — in
  Kotlin, top-level `private` is **file-private, not package-private**, so it isn't visible from
  `WidgetComponents.kt` even though both files share a package; (2)
  `mem:android_widget_content_dispatch_size_classification` already documents why content-dispatch
  decisions in this codebase use raw `LocalSize` comparisons rather than the typography-tuned
  size-class resolver.
