# Use-case layer: consumers inject only the use cases they actually call

Pattern (`domain/usecase/<feature>/`, `core/usecase/UseCase.kt`): every repository operation gets
its own `XxxUseCase` (abstract) + `XxxUseCaseImpl` pair, registered in `di/Modules.kt`'s
`useCaseModule`, and consumers (ViewModels, Workers, widgets, background tasks) inject the abstract
`XxxUseCase` instead of the repository interface/`Impl` directly (see `mem:architecture`).

A consumer does NOT need to inject every use case a repository's full operation set produces —
only the ones its own methods actually call. Example: `PreferenceRepository` has 8 operations
(4 typed `get`/`set` overloads), so there are 8 preference use cases, but `PreferenceViewModel`
only injects 6 (`GetString`/`GetInt`/all 4 `Set*`) since nothing in that ViewModel reads a
Boolean or Double preference. The other 2 (`GetBooleanPreferenceUseCase`,
`GetDoublePreferenceUseCase`) still exist and stay Koin-registered for interface parity — same
precedent as `GetWeatherAlertsUseCase`, created even though no consumer calls it yet.

When adding a new consumer of an existing multi-operation repository, only add constructor params
for the use cases actually referenced in that class's body — don't inject the full set
speculatively.
