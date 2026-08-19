# Android widget: clock graphic (AnalogClock/TextClock) scaling is separate from the Glance token system

- The clock graphic in `SmallWeatherWithAnalogClockGlanceWidget`/`SmallWeatherWithDigitalClockGlanceWidget`
  is rendered via `AndroidRemoteViews` embedding a native Android View layout
  (`res/layout/widget_rtc_analog_clock.xml`/`widget_rtc_digital_clock.xml`) — entirely outside
  the Glance composable tree, so it can't consume `WidgetTypography`/`WidgetSpacing` directly.
- Digital clock: scales via `RemoteViews.setTextViewTextSize(viewId, unit, size)` at runtime — a
  plain call available well below `minSdk` 24, driven by 2 `WidgetTypography` fields
  (`clockDigitalDateSize`, `clockDigitalTimeSize`).
- Analog clock — **gotcha**: `android.widget.AnalogClock` (deprecated framework widget) only
  *shrinks* its dial/hand drawables to fit available View bounds; it never grows them past the
  drawables' own declared intrinsic `android:width`/`android:height`. Giving the `AnalogClock`
  View a larger `layout_width`/`layout_height` alone does nothing visible — confirmed by direct
  on-device testing (the container grew, the drawn clock did not).
- Fix: added `_medium`/`_large` variants of all 4 drawables (`clock_dial`, `clock_hour_hand`,
  `clock_minute_hand`, `clock_second_hand`) with a larger *intrinsic* `android:width`/
  `android:height` (`viewportWidth`/`viewportHeight`/pathData unchanged — the vector scales
  losslessly), plus `_medium`/`_large` layout XML variants referencing them, selected by
  `rememberWidgetSizeClass()` at `RemoteViews(...)` construction time. Avoids needing the
  API-31+-only `RemoteViews.setViewLayoutWidth`/`Height` — works on every supported API level.
- General takeaway: a native View's `layout_width`/`layout_height` only bounds where a
  `Drawable` *can* draw, not what size it *actually* draws at — check the specific View's own
  draw logic (especially deprecated/legacy framework widgets like `AnalogClock`) before assuming
  a layout-size change alone is sufficient.
