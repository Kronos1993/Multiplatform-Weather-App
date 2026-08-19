# Android widget: missing minResizeWidth/Height silently blocks adaptive content transform on shrink

- `widget_provider_info_large.xml` and `widget_provider_info_medium.xml` declared only
  `android:minWidth`/`android:minHeight` (`256x120` and `256x60` respectively) with no
  `android:minResizeWidth`/`android:minResizeHeight`. Per the `AppWidgetProviderInfo` platform
  contract, an **unset `minResizeWidth`/`minResizeHeight` defaults to `minWidth`/`minHeight`** — so
  the launcher would not let the user drag either widget smaller than its own full declared size.
- Effect: `LargeWeatherGlanceWidget`/`MediumWeatherGlanceWidget` both already call
  `AdaptiveWeatherWidgetContent(weatherData)` (correctly wired to dispatch by `LocalSize.current` —
  see `mem:android_widget_content_dispatch_size_classification`), but it silently never dispatched
  away from the Large/Medium composable on shrink, because `LocalSize.current` never received a
  smaller candidate from `WIDGET_RESPONSIVE_SIZES` in the first place. This looked like a Compose
  dispatch bug but was actually a manifest/provider-XML omission.
- Fixed by adding `android:minResizeWidth="128dp"` / `android:minResizeHeight="48dp"` (the smallest
  candidate in `WIDGET_RESPONSIVE_SIZES` — see `mem:android_widget_sizemode_responsive_candidates`)
  to both provider XMLs. The Small widget's own provider XML already declares
  `minWidth="128dp" minHeight="48dp"` (already the smallest candidate), so it needed no change.
- **General rule**: any widget provider XML meant to participate in live drag-resize content
  transformation via an `Adaptive*` composable must declare `minResizeWidth`/`minResizeHeight` down
  to the smallest candidate it should be able to reach — `resizeMode="horizontal|vertical"` alone
  is not sufficient; it only permits resizing, the min-resize attributes set the actual floor.
