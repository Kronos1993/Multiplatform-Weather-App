---
name: new-feature
description: Scaffold a complete new feature end-to-end in this Kotlin Multiplatform app — domain model + repository interface, data repository implementation with a local (Room) and/or remote (Ktor) data source, Koin DI wiring, a ViewModel (extends ParentViewModel with a sealed ScreenState), a Compose Screen (koinViewModel injection), and optionally navigation registration (Destinations + NavHost route). Use when the user wants a brand-new screen/capability added to the app, not a change to an existing feature. Produces a compiling skeleton with TODOs for the actual business logic and UI — it does not invent behavior. Chained from /spec-implement when a spec's proposal.md §6 names this skill.
metadata:
  claude_md_requires: '[]'
---

## Usage

```
/new-feature <FeatureName> --data <local|remote|none> [--parent <existing_feature>] [--nav-route <yes|no>]
```

| Arg | Meaning |
|---|---|
| `<FeatureName>` | PascalCase feature identifier, e.g. `RainAlerts`. Drives every generated name (see Naming below). |
| `--data` | `local` — new Room-backed data source; `remote` — new Ktor-backed data source; `none` — the feature only reads/writes through **existing** repositories (no new domain/data layer generated). |
| `--parent` | Optional. An existing feature package to nest under (e.g. `home` → `features/home/<feature_snake>/`), mirroring how `current_weather`, `user_location`, `setting` nest under `home/`. Omit for a new top-level `features/<feature_snake>/` package. |
| `--nav-route` | `yes` (default) — add a `Destinations` entry and wire the Screen into `App.kt`'s `NavHost`. `no` — the Screen is meant to be embedded/opened another way (dialog, nested content) and navigation wiring is skipped. |

When invoked from `/spec-implement`, all args come from the plan.md step's "Skill args / inputs" — this skill does not re-prompt for them. When invoked directly by the user, ask for any argument not supplied (don't guess `--data` or `--parent` silently — the wrong choice means real rework).

## Preconditions

- The target package path does not already exist. If `features/<parent?>/<feature_snake>/` (or a same-name domain/data file) already exists, stop and ask whether this is actually a change to an existing feature instead (wrong skill — just edit directly).
- Per root `CLAUDE.md` Working Rules: **confirm the intended location with the user before creating any file** — state the resolved package path and the full file list (see Output) and wait for explicit approval before writing anything.

## Naming

All names derive from `<FeatureName>` (PascalCase input):

| Placeholder | Derivation | Example (`RainAlerts`) |
|---|---|---|
| `{Feature}` | as given | `RainAlerts` |
| `{feature_snake}` | snake_case | `rain_alerts` |
| `{FEATURE_SQL}` | SCREAMING_SNAKE_CASE, for Room table/column names | `RAIN_ALERTS` |

## What this skill does

Generate files in this order — later steps depend on earlier ones existing.

### 1. Domain model (only if the feature needs a new one)

If the feature represents a new concept not already modeled under
`domain/model/` (check first — reuse an existing model when the story
is really "a new view of existing data"), create:

```
domain/model/{Feature}.kt
```

A plain `data class {Feature}(...)` with the fields the story implies.
Ask the user for the field list if it's not obvious from the spec —
don't invent fields.

### 2. Domain repository interface (skip if `--data none`)

```
domain/repository/{Feature}Repository.kt
```

```kotlin
package com.kronos.multiplatform.weatherapp.domain.repository

import com.kronos.multiplatform.weatherapp.domain.model.{Feature}

interface {Feature}Repository {
    suspend fun listAll(): List<{Feature}>
    // TODO: add the operations this feature actually needs
}
```

Keep it narrow (ISP) — only the operations this feature needs, not a
speculative full CRUD set.

### 3a. Local data source (`--data local`)

Follows the `UserCustomLocation*` pattern (`mem:architecture`):

```
data/local/datasources/entity/{Feature}Entity.kt
data/local/datasources/dao/{Feature}Dao.kt
data/local/datasources/mapper/{Feature}Mapper.kt          # toDomain()/toEntity() extension functions
data/local/datasources/{Feature}LocalDataSource.kt          # interface
data/local/datasources/{Feature}LocalDataSourceImpl.kt      # implementation, wraps the Dao
data/repository/{feature_snake}/{Feature}RepositoryImpl.kt  # implements domain/repository/{Feature}Repository, delegates to the DataSource
```

Entity example:

```kotlin
@Entity(tableName = "{FEATURE_SQL}")
data class {Feature}Entity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID") val id: Long,
    // TODO: add columns
)
```

**Room migration gotcha (non-obvious, must not be skipped):** this
repo's Room setup (`data/local/database/ApplicationDatabase.kt`) has
`exportSchema = false` and no destructive-migration fallback, so a new
entity needs THREE manual edits to that one file, in this order:

1. Add `{Feature}Entity::class` to the `@Database(entities = [...])` list.
2. **Bump `version` by 1.**
3. Add a `MIGRATION_<old>_<new>` object with a raw
   `CREATE TABLE {FEATURE_SQL} (...)` SQL statement whose columns match
   the entity exactly (column names/types/defaults), and append it to
   the `MIGRATIONS` array. Room will NOT generate this for you.
4. Add `abstract fun {feature_snake}Dao(): {Feature}Dao` to
   `ApplicationDatabase`.

Skipping the migration crashes the app on upgrade for existing
installs — this is not optional polish.

### 3b. Remote data source (`--data remote`)

Follows the `WeatherRemoteDataSource`/`WeatherAlertsRemoteDataSource`
pattern:

```
data/remote/dto/{feature_snake}/{Feature}Dto.kt              # wire format, @Serializable
data/mapper/{Feature}Mapper.kt                                 # DTO -> domain mapping (data/mapper, not data/local/.../mapper)
data/remote/datasources/{Feature}RemoteDataSource.kt          # interface
data/remote/datasources/{Feature}RemoteDataSourceImpl.kt      # implementation, uses a KtorClientFactory
data/repository/{feature_snake}/{Feature}RepositoryImpl.kt    # implements domain/repository/{Feature}Repository, delegates to the DataSource, returns core/result/Result
```

The DataSourceImpl constructor takes a `KtorClientFactory` (inject via
the `named(KtorClientFactoryType.PUBLIC)` or `PRIVATE` qualifier — see
`data/remote/di/Modules.kt` for which one existing data sources use)
plus whatever the existing `WeatherRemoteDataSourceImpl` constructor
takes for base URL/serialization — read that file first with Serena to
match the exact shape rather than guessing.

Repository methods return `core/result/Result<D, Error>` (`Success`/
`Error`), never throw — mirror `WeatherRemoteRepositoryImpl`.

### 4. DI wiring

- `--data local`: add to `data/local/di/Modules.kt`
  `commonDataLocalModules`:
  ```kotlin
  single<{Feature}LocalDataSource> { {Feature}LocalDataSourceImpl(get()) }
  single { {Feature}RepositoryImpl(get()) }.bind<{Feature}Repository>()
  ```
- `--data remote`: add to `data/remote/di/Modules.kt`
  `commonRemoteModules`, following the qualifier pattern already there
  for the Ktor client factory.
- `--data none`: no new repository binding — the ViewModel will inject
  existing repositories directly.
- Always: register the ViewModel in `di/Modules.kt` `viewModelModule`:
  ```kotlin
  viewModelOf(::{Feature}ViewModel)
  ```

Use Serena's `insert_after_symbol`/`replace_content` on these existing
files — do not rewrite them wholesale.

### 5. ViewModel

```
features/{parent?}/{feature_snake}/{Feature}ViewModel.kt
```

```kotlin
class {Feature}ViewModel(
    private val {feature}Repository: {Feature}Repository, // omit if --data none; inject whichever existing repositories the feature actually needs instead
    private val loggerManager: ILogManager
) : ParentViewModel() {

    private val TAG = this::class.simpleName

    private val _screenState = MutableStateFlow<{Feature}ScreenState>({Feature}ScreenState.Idle)
    val screenState = _screenState.asStateFlow()

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            _screenState.value = {Feature}ScreenState.Loading
            // TODO: call the repository, convert Result into a screen state
        }
    }

    private fun log(item: String, isError: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            loggerManager.log(if (isError) LogLevel.ERROR else LogLevel.INFO, TAG.orEmpty(), item)
        }
    }
}

sealed class {Feature}ScreenState {
    object Idle : {Feature}ScreenState()
    object Loading : {Feature}ScreenState()
    // TODO: add the states this screen actually needs (an Obtained/Error pair at minimum)
}
```

Ask the user what states the screen actually needs beyond
`Idle`/`Loading` — don't guess a state machine for them.

### 6. Screen composable

```
features/{parent?}/{feature_snake}/{Feature}Screen.kt
```

```kotlin
@Composable
fun {Feature}Screen(
    navHost: NavHostController,
    // TODO: add whatever params sibling screens take (deviceScreenConfiguration, currentLang, etc.) that this screen actually needs
) {
    val viewModel = koinViewModel<{Feature}ViewModel>()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    // TODO: render per screenState
}
```

For a larger screen, mirror `user_location/content/` — put sub-composables
in a `{feature_snake}/content/` sub-package rather than one large file.

### 7. Navigation (only if `--nav-route yes`)

1. Add an entry to `Destinations.kt`:
   ```kotlin
   enum class Destinations {
       HOME, ADD_CITY, {FEATURE_SQL}
   }
   ```
2. Add a route in `App.kt`'s `NavHost`:
   ```kotlin
   composable(route = Destinations.{FEATURE_SQL}.name) {
       {Feature}Screen(navController, /* TODO: params */)
   }
   ```
   Use Serena's `insert_after_symbol`/`replace_content` — don't rewrite
   `App.kt` wholesale.

If `--nav-route no`, skip this step and note in the output summary how
the caller is expected to reach the new Screen (embedded, dialog, etc.)
— that wiring is the caller's responsibility, not this skill's.

### 8. Localization stub

Add placeholder string keys for any screen copy to
`composeApp/src/commonMain/composeResources/values/strings.xml` **and**
`values-es/strings.xml` (empty or `TODO` placeholder text — the real
copy is a content decision, not this skill's job to invent). If the
feature's strings will also be shown from native iOS code (a
notification, a widget), flag that explicitly in the summary — per
`architecture.dual_localization_required`, `iosApp/en.strings` +
`iosApp/es.strings` need the same keys too, and this skill does not
add those automatically (it doesn't know yet whether iOS needs them).

## What this skill does NOT do

- Does not invent business logic, UI layout, or screen states beyond
  `Idle`/`Loading` — every generated body has a `// TODO` where a
  decision belongs to the spec's own implementation steps.
- Does not write to `iosApp/*.strings` automatically (see step 8).
- Does not run the app or the build — the caller (`/spec-implement`,
  or the user directly) verifies per the usual
  `./gradlew :composeApp:assembleDebug` / manual-run process.
- Does not modify an existing feature — refuses if the target package
  already exists (see Preconditions).
- Does not commit anything.

## Output

After generation, print the full file list grouped by layer (Domain /
Data / DI / Presentation / Navigation / Localization), each marked
`created` or `edited` (for the DI/nav/localization files that were
modified in place, not created), plus a `TODO` checklist pulled from
every `// TODO` comment inserted — so the next step (or the user) has a
single list of what's left to actually implement.

## Failure modes and recovery

| Symptom | Cause | Recovery |
|---|---|---|
| Target package already exists | Feature name collides with an existing one | Confirm whether this should be an edit to the existing feature instead; pick a different name if genuinely new |
| `--data local` but the Room migration step is unclear | Entity has a type Room needs help mapping (e.g. nested object) | Stop and ask — do not guess a lossy SQL column mapping |
| `--data remote` but the existing `KtorClientFactory` qualifier pattern doesn't fit | The feature needs a new client shape not covered by `PUBLIC`/`PRIVATE` | Read `data/remote/di/Modules.kt` and `data/remote/ktor/` with Serena first, surface the mismatch, ask the user how to proceed rather than inventing a third factory type silently |
| `--nav-route yes` but there's no obvious parent screen to link to | Feature is meant to be reached from a not-yet-built entry point | Wire the `Destinations` entry and `NavHost` route anyway (so it's reachable via `navController.navigate(...)`), note in the summary that no caller triggers it yet |

## Reference

- Real examples to mirror (read with Serena before generating, don't
  rely on this file's templates alone — they're illustrative, the live
  code is the source of truth): `features/home/user_location/` (local
  data, full CRUD), `features/add_city/` (remote data + map
  interaction), `features/home/current_weather/` (remote data, simplest
  screen-state shape).
- `mem:architecture`, `mem:conventions` (Serena memories) — the layering
  and naming rules this skill encodes.
- `.claude/skills/solid/SKILL.md` — the SOLID rationale behind the
  domain/data split this skill scaffolds.
- Used by: `/spec-implement`, when a spec's `proposal.md` §6 names
  `/new-feature` (see `/spec-plan`'s Decision rules for when that's the
  right call).
