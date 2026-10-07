---
spec_id: add-use-case-layer
source: manual
source_ref: dictated in chat, 2026-09-09
fetched_at: 2026-09-09
fetched_by: /spec-new
---

# Story: Add a use-case layer between consumers and repositories

<!--
RAW INTAKE — DO NOT EDIT AFTER FETCH

This file preserves the source as it arrived, so future readers can
audit what /spec-plan worked from. /spec-plan reads this and produces
proposal.md; story.md is append-only thereafter.
-->

## Metadata

| Field | Value |
|-------|-------|
| Type | refactor |
| Priority | normal |
| Created | 2026-09-09 |

## Description

Cambio de arquitectura: añadir una capa de casos de uso (use cases) entre los ViewModels/consumidores y los repositorios. Hoy varios puntos de la app inyectan e invocan directamente las interfaces de `domain/repository/*`. El nuevo flujo debe ser: consumidor (ViewModel, Worker, Widget, background task) → `domain/usecase/<feature>/*UseCase` → `domain/repository/*Interface` → `data/repository/<feature>/*Impl` → datasources. Los repositorios NO deben usarse directamente en ningún punto de la app — sólo los casos de uso los usan, y los casos de uso son lo único que los consumidores (ViewModels, y cualquier otro punto de la app) pueden inyectar.

Decisiones de forma (patrón que el usuario pidió replicar desde otro proyecto propio con casos de uso ya adoptados, pero **sin dejar ninguna referencia a ese otro proyecto en ningún comentario ni documento generado** — todo debe redactarse como si la decisión surgiera de este mismo repo):

- Clase base compartida `core/usecase/UseCase.kt`: `abstract class UseCase<in P, out R> { abstract suspend fun run(params: P): R; suspend operator fun invoke(params: P): R = run(params) }`.
- Un par de clases por cada operación de cada repositorio: una clase abstracta `XxxUseCase : UseCase<XxxUseCase.Params, ReturnType>()` con un `data class Params(...)` anidado (o `UseCase<Unit, ReturnType>` si la operación no toma parámetros), y una clase concreta separada `XxxUseCaseImpl(private val repository: XxxRepository) : XxxUseCase() { override suspend fun run(params: Params): ReturnType = ... }` que delega en el repositorio (sin lógica nueva salvo que ya exista hoy en el ViewModel y tenga sentido moverla — a decidir en planning).
- Ubicación: `domain/usecase/<feature>/` — un subpaquete por feature, igual que ya existe implícitamente en `data/repository/<feature>/`.
- Los ViewModels (y cualquier otro consumidor) inyectan el caso de uso tipado como su clase **abstracta**, nunca la interfaz de repositorio ni la `Impl`.
- Registro en Koin: un módulo plano `useCaseModule` (en `di/Modules.kt`, junto a `viewModelModule`) con `singleOf(::XxxUseCaseImpl).bind<XxxUseCase>()` por cada caso de uso, insertado en `initKoin()` (`di/Koin.kt`) antes de `viewModelModule`.

Alcance — repositorios existentes y sus métodos (confirmado por lectura directa de `domain/repository/`, 2026-09-09):

- `LocationRepository`: `getCurrentLocation(): LocationModel?`, `isLocationEnabled(): Boolean`
- `MapLayerRepository`: `getLayerTiles(): Result<MapLayerTiles, Error>`
- `UserCustomLocationLocalRepository`: `getSelectedLocation(): UserCustomLocation?`, `getCurrentLocation(): UserCustomLocation?`, `saveLocation(userCustomLocation, isCurrent: Boolean = false): UserCustomLocation`, `listAll(): List<UserCustomLocation>`, `delete(userCustomLocation): Boolean`
- `WeatherAlertsRemoteRepository`: `getWeatherAlertsData(lat, lon, apiKey): Result<CurrentAlertsForecast, Error>` — sin consumidor real hoy en ningún ViewModel/Worker (confirmado por grep), pero igual necesita su caso de uso por consistencia arquitectónica.
- `WeatherRemoteRepository`: `getWeatherData(city, lang, apiKey): Result<CurrentForecast, Error>`, `getWeatherDataForecast(city, lang, apiKey, days = 1): Result<Forecast, Error>` (overload por ciudad), `getWeatherDataForecast(lat, lon, lang, apiKey, days = 1): Result<Forecast, Error>` (overload por coordenadas — mismo nombre de método en el repo pero necesitará casos de uso con nombres distintos, a confirmar exactamente en planning), `getLastWeatherForecast(prefKey): Result<Forecast, Error>`, `setLastWeatherForecast(prefKey, forecast): Result<Boolean, Error>`

Total: 5 repositorios, 14 operaciones distintas (contando los dos overloads de `getWeatherDataForecast` como operaciones separadas) → 14 pares de clases (abstracta + Impl) nuevas bajo `domain/usecase/`.

Consumidores actuales que inyectan repositorios directamente y deben migrar a inyectar casos de uso (confirmado por grep de `domain.repository.` en todos los source sets, 2026-09-09):

- `features/add_city/AddCityViewModel.kt` — inyecta `WeatherRemoteRepository`, `UserCustomLocationLocalRepository`, `MapLayerRepository`, `LocationRepository`.
- `features/home/current_weather/WeatherViewModel.kt` — inyecta `WeatherRemoteRepository`, `UserCustomLocationLocalRepository`, `MapLayerRepository`, `LocationRepository`.
- `features/home/user_location/UserCustomLocationViewModel.kt` — inyecta `WeatherRemoteRepository`, `UserCustomLocationLocalRepository`.
- `features/home/about/AboutViewModel.kt` y `features/home/HomeViewModel.kt` — NO inyectan ningún repositorio de dominio hoy (no requieren cambios).
- (androidMain) `job/WeatherAlertNotificationWorker.kt` — inyecta `WeatherAlertsRemoteRepository`, `UserCustomLocationLocalRepository`.
- (androidMain) `job/WeatherNotificationWorker.kt` — inyecta `WeatherRemoteRepository`, `UserCustomLocationLocalRepository` (+ `PreferenceRepository`, que es infraestructura de `core/preferences`, NO uno de los 5 repos de dominio — fuera de alcance).
- (androidMain) `job/WeatherSuggestionNotificationWorker.kt` — inyecta `WeatherRemoteRepository`, `UserCustomLocationLocalRepository` (+ `PreferenceRepository`, fuera de alcance por la misma razón).
- (androidMain) `widget/BaseWeatherGlanceWidget.kt` — inyecta `WeatherRemoteRepository`, `UserCustomLocationLocalRepository` (+ `PreferenceRepository`, fuera de alcance).
- (iosMain) `core/job/WeatherNotificationBackgroundTask.kt` — inyecta `WeatherRemoteRepository`, `UserCustomLocationLocalRepository` (+ `PreferenceRepository`, fuera de alcance).

Nota importante a resolver/confirmar en planning: el pedido del usuario es categórico ("no se deben utilizar los repositorios de manera directa en el app"), no limitado a ViewModels — por eso se incluyen aquí también los 3 Workers de Android, el widget Glance y la tarea de background de iOS como parte del alcance, no sólo los 3 ViewModels. `core/preferences/repository/PreferenceRepository` se considera infraestructura transversal (no un repositorio de `domain/repository/<feature>`) y por tanto queda fuera de alcance de esta migración — a confirmar explícitamente en planning si el usuario está de acuerdo con ese corte.

Impacto esperado (a confirmar/expandir en planning):

- Nuevo: `core/usecase/UseCase.kt`.
- Nuevo: 14 pares de clases bajo `domain/usecase/<feature>/` (nombres exactos de features/paquetes y de las clases a confirmar en planning — posibles agrupaciones: `location`, `weather`, `radar` o `map_layer`, `alerts`, `user_custom_location`, espejando los subpaquetes ya existentes en `data/repository/<feature>`).
- Modificado: los 3 ViewModels + los 3 Workers de Android + el widget Glance + la tarea de background de iOS, para inyectar casos de uso (tipados como su clase abstracta) en vez de repositorios.
- Nuevo/modificado: `di/Modules.kt` (nuevo `useCaseModule`), `di/Koin.kt` (`initKoin()` añade `useCaseModule` antes de `viewModelModule`).
- Modificado: `CLAUDE.md` — la sección Architecture debe documentar el nuevo flujo con la capa de casos de uso (reemplazando "ViewModels depend on domain/repository interfaces" por el flujo con casos de uso en medio), y la sección Conventions/SOLID si aplica.
- Modificado (si existe una referencia al flujo antiguo): cualquier skill de scaffolding (`/new-feature` u otro) que hoy genere un ViewModel inyectando el repositorio directamente, para que en adelante genere también su caso de uso.
- Los repositorios (`domain/repository/*Interface`, `data/repository/<feature>/*Impl`) NO cambian de forma ni de firma — sólo se les añade una capa de casos de uso por encima. Sin cambio de comportamiento visible para el usuario final (refactor puro de arquitectura interna).

Sin cambios de comportamiento esperados — es un refactor puro, no debe alterar ningún dato ni UI visible en ninguna plataforma (Android/iOS/Desktop).

## Acceptance criteria (as written in source)

- Cada método de cada uno de los 5 repositorios de dominio existentes tiene un caso de uso correspondiente en `domain/usecase/<feature>/`, con el par abstracta+Impl y `operator fun invoke()`.
- Ningún ViewModel, Worker, widget o background task del árbol de código (`commonMain`, `androidMain`, `iosMain`) inyecta directamente una interfaz o implementación de `domain/repository/*` — grep de `domain.repository.` sólo debe dar resultado en los propios archivos de `data/repository/*Impl` y en los nuevos `domain/usecase/*Impl`.
- Los casos de uso están registrados en Koin y la app compila/arranca igual que antes (sin cambio de comportamiento visible).
- `./gradlew build` (o al menos `:composeApp:assembleDebug`, más una corrida manual en desktop/Android) queda verde.
- Ningún comentario nuevo en el código hace referencia a ningún otro proyecto — los comentarios (si los hay) deben redactarse como si la decisión de diseño surgiera de este mismo repo.
- `CLAUDE.md` refleja el nuevo flujo de datos con la capa de casos de uso.

## Comments / discussion

(none — file/manual/url intake, no comment thread)

## Attachments

-

## Links

-
