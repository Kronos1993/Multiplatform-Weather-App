# Architecture — Clean Architecture + MVVM (SOLID)

Layering inside `commonMain` (`com.kronos.multiplatform.weatherapp`):

- `domain/repository/*`: interfaces only (e.g. `WeatherRemoteRepository`, `LocationRepository`,
  `MapLayerRepository`, `UserCustomLocationLocalRepository`, `WeatherAlertsRemoteRepository`) — the
  abstraction ViewModels depend on (Dependency Inversion).
- `data/repository/<feature>/*Impl`: concrete implementations of the domain interfaces, one
  subpackage per feature (`weather`, `location`, `radar/rain`, `user_custom_location`, `alerts`).
- `data/remote/`: Ktor API clients (`api/`), DTOs (`dto/current`, `dto/forecast`), datasources, DI.
- `data/local/`: Room entities/DAOs (`datasources/entity`, `datasources/dao`), mappers, database
  setup, DI.
- `data/mapper/`: DTO/Entity -> domain model mapping.
- `domain/model/`: platform-agnostic models (`current`, `forecast`, `alerts` subpackages).
- `core/result/Result.kt`: sealed `Result` (`Success`/`Error`) + `map`/`onSuccess`/`onError`/
  `asEmptyDataResult` helpers — the standard return type for repository/data-layer calls instead of
  throwing exceptions.
- `core/viewmodel/ParentViewModel`: base class all feature ViewModels extend (limit/offset,
  `refreshing` flag, `log()` helper).
- `features/<feature>/`: one folder per screen/feature (`home`, `home/current_weather`, `home/about`,
  `home/setting`, `home/user_location`, `add_city`), each with `XxxViewModel` + `XxxScreen` (larger
  screens add a nested `content/`). ViewModels expose a sealed `XxxScreenState` (e.g.
  `WeatherScreenState`: `Idle`/`Loading`/`NoWeather`/`WeatherObtained`) consumed by the Screen composable.
- `di/`: Koin composition; `initKoin()` wires `viewModelModule` + core/data/domain module lists.

## Conventions to follow when extending
- New feature/data source: define the interface under `domain/repository`, implement under
  `data/repository/<feature>`, register both in the matching Koin module. Features must depend on
  the `domain` interface, never directly on a `data/*Impl` class.
- Platform-specific code (`expect`/`actual`) lives under `core/*` and `data/local/*` with per-target
  file suffixes: `.ios.kt` (iOS), `.native.kt` (shared native/iOS-only helper, e.g.
  `ApplicationDatabaseFactory`), unsuffixed files under `androidMain`/`jvmMain` source dirs.
- Maps: MapLibre Compose (common) + native MapLibre iOS framework via Swift Package Manager
  (`spmForKmp` plugin, cinterop `maplibre` configured in `composeApp/build.gradle.kts`).
