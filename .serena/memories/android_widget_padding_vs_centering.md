# Android widget: centering cancels padding; check corner radius before tuning padding tokens

- Centering (`verticalAlignment`/`horizontalAlignment = Alignment.CenterVertically`/
  `CenterHorizontally`) applied to a child inside a `fillMaxSize()` container algebraically
  **cancels any padding** applied by an ancestor Box (e.g. `WeatherWidgetBackground`'s
  `.padding(...)`): the visible margin reduces to `containerSize/2 - contentSize/2`, independent
  of the padding value, as long as it doesn't force clipping. Increasing
  `WidgetSpacing.contentPadding` had **zero visible effect** on any of the 5 home-screen widget
  composables until this was diagnosed — see `mem:android_widget_spacing_tokens` for the token
  system this affects.
- Switching the affected composables' alignment to `Alignment.Top` (to make padding "real" by
  removing the centering that cancels it) was tried and reverted: widgets that render taller than
  their natural content height then showed an ugly empty strip at the bottom (top-anchored content
  vs. the previous, if padding-blind, centered look). Don't reach for this fix reflexively — it
  trades one visual problem for another on any widget whose actual rendered size commonly exceeds
  its natural content size.
- The actual fix for a "content sits flush against the edge" complaint was unrelated to alignment:
  `widget/components/bg_widget_glass.xml`'s corner radius (was `20dp`) consumed most of the
  corner-adjacent margin on the Small widget's `48dp` declared height — a radius that large
  relative to a compact widget's own size visually eats the padding before it can register. Fixed
  by reducing the radius to `12dp`. **Lesson**: on a compact widget, check the background
  drawable's corner radius against the widget's actual size before assuming a padding/spacing
  token is the lever to pull for a "looks cramped near the edge" complaint.
