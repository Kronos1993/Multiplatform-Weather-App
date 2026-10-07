# Home tabs share one ViewModelStoreOwner

- `features/home/HomeScreen.kt` renders the Weather / Location / Settings / About tabs (`ScrollableTabView`) inside its own composition, so they share the Home nav back-stack entry as `LocalViewModelStoreOwner`.
- Consequence: `koinViewModel<X>()` called in both `HomeScreen` and a tab screen (e.g. `SettingsScreen`) returns the SAME instance.
- Any UI flag (e.g. "show dialog") on such a shared ViewModel is observed by both screens. If both render the dialog, it shows twice. Keep per-screen flags, e.g. `BatteryOptimizationViewModel.showHomePrompt` vs `showSettingsDialog`, and have each screen render only its own.
