# Tech Stack

- Kotlin 2.3.21, Compose Multiplatform 1.10.3. Targets: `androidTarget`, `iosArm64`/`iosX64`/
  `iosSimulatorArm64` (static framework `ComposeApp`), `jvm` (desktop).
- DI: Koin 4.2.1 (+ koin-annotations). Modules split by layer/platform: `di/Modules.kt` (commonMain
  composition), `core/di/`, `data/local/di/`, `data/remote/di/`; platform actuals add bindings via
  `Module.ios.kt` / `Modules.ios.kt` etc. Entry point: `initKoin()` in `di/Koin.kt`.
- Networking: Ktor 3.5.0 client (`okhttp` engine on Android, `darwin` engine on iOS) against the
  WeatherAPI (weatherapi.com) REST API.
- Persistence: Room 2.8.4 + KSP codegen, `androidx-sqliteBundled` driver (KMP-compatible Room);
  DataStore (`datastore`/`datastore-preferences`) for key-value prefs.
- Other notable libs: Kermit (logging), Moko permissions (location/notifications/storage), Coil3
  (images), MapLibre Compose + native MapLibre iOS framework via `spmForKmp` Swift Package Manager
  plugin (cinterop name `maplibre`), compose-charts, Glance (Android widgets), WorkManager (Android
  background jobs), kotlinx-datetime, kotlinx-serialization.
- Build: Gradle version catalog at `gradle/libs.versions.toml`; AGP 9.0.1; compileSdk/targetSdk 36,
  minSdk 24.
- iOS native shell: SwiftUI app in `iosApp/`, consumes the `ComposeApp` framework; localization via
  `iosApp/en.strings` + `iosApp/es.strings`, independent from Compose Multiplatform's composeResources
  (see `mem:conventions` for the duplication this implies).
