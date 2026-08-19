# Android widget: content-dispatch size classification is separate from typography's

- `WidgetTheme.kt`'s `resolveWidgetSizeClass` (SMALL if `width < 120dp OR height < 100dp`) is
  tuned for typography/spacing scaling only. Medium's own declared default (256×60) resolves
  `SMALL` under it purely because its height (60dp) is below the 100dp threshold — harmless for
  typography (matches what Medium already ships with today), but a real bug if reused to pick
  WHICH content composable renders.
- `WidgetComponents.kt`'s `AdaptiveWeatherWidgetContent` (added for live drag-resize
  content-transform across Small/Medium/Large) therefore compares `LocalSize.current` directly
  against the real declared minimums (`width>=256 && height>=120` → Large,
  `width>=256 && height>=60` → Medium, else Small) instead of calling `rememberWidgetSizeClass()`.
- This is only safe because `SizeMode.Responsive`'s candidate set (see
  `mem:android_widget_sizemode_responsive_candidates`) never reports an arbitrary in-between
  size — don't reuse this exact dispatch pattern if `LocalSize` could ever be continuous/unconstrained.
