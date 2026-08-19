# CLAUDE.md

Guidance for Claude Code (and other agents) working in this repository.

## Project

WeatherApp: a Kotlin Multiplatform weather app targeting **Android, iOS, and Desktop (JVM)**, built
with Compose Multiplatform. Single Gradle module `:composeApp` holds the shared UI/logic; `iosApp/`
is the native SwiftUI host/entry point for iOS. Root package: `com.kronos.multiplatform.weatherapp`.

Data comes from the [WeatherAPI](https://www.weatherapi.com/) REST API.

## Code intelligence: use Serena

This repo has the **Serena MCP server** configured (`.mcp.json`, project-scoped — works for anyone
who clones the repo and has `uv`/`uvx` installed). It has already been onboarded: `.serena/memories/`
contains dense, curated notes on the source layout, tech stack, architecture, conventions, build
commands, and completion criteria.

- Read `mem:core` first — it's the entry point and links to the rest.
- Prefer Serena's symbolic tools (`get_symbols_overview`, `find_symbol`, `find_referencing_symbols`,
  `replace_symbol_body`, etc.) over raw `Read`/`Edit`/grep for exploring or modifying Kotlin/Swift
  code — they're structure-aware and cheaper. Use plain file tools for config/resource files
  (Gradle scripts, XML, `.strings`, plist) and for pure text-content discovery (grep/glob).
- Keep memories current: when you learn something durable and non-obvious about the codebase, write
  it to a memory (dense bullets, not prose) instead of letting it live only in conversation.

## Architecture — Clean Architecture + MVVM, SOLID

- `domain/repository/*`: interfaces only — the abstraction ViewModels depend on (Dependency
  Inversion). Never let a feature depend on a `data/*Impl` class directly.
- `data/repository/<feature>/*Impl`: implementations of those interfaces, one subpackage per
  feature (`weather`, `location`, `radar/rain`, `user_custom_location`, `alerts`).
- `data/remote/`: Ktor API clients, DTOs, datasources, DI. `data/local/`: Room entities/DAOs,
  mappers, DB setup, DI. `data/mapper/`: DTO/Entity → domain model mapping.
- `domain/model/`: platform-agnostic models.
- `core/result/Result.kt`: sealed `Result` (`Success`/`Error`) with `map`/`onSuccess`/`onError`
  helpers — the standard return type for repository/data-layer calls. Prefer this over throwing.
- `core/viewmodel/ParentViewModel`: base class for feature ViewModels.
- `features/<feature>/`: one folder per screen (`home`, `home/current_weather`, `home/about`,
  `home/setting`, `home/user_location`, `add_city`), each with `XxxViewModel` + `XxxScreen` and a
  sealed `XxxScreenState` the Screen composable renders.
- `di/`: Koin composition; `initKoin()` in `di/Koin.kt` wires everything together. New
  repositories/data sources get registered in the matching Koin module.
- Platform-specific (`expect`/`actual`) code lives under `core/*` and `data/local/*` with per-target
  file suffixes: `.ios.kt` (iOS), `.native.kt` (shared native/iOS-only), unsuffixed under
  `androidMain`/`jvmMain`.

Full details, including the tech stack (Koin, Ktor, Room, DataStore, MapLibre, Coil3, Moko
permissions, Glance, WorkManager, etc.) and exact package paths: see `mem:architecture` and
`mem:tech_stack` via Serena.

### SOLID in this codebase

- **SRP**: one feature/screen per package under `features/`; repository implementations scoped to a
  single feature under `data/repository/<feature>`.
- **OCP/DIP**: ViewModels and other consumers depend on `domain/repository` interfaces, not on
  concrete `data/*Impl` classes — swap implementations without touching callers.
- **ISP**: repository interfaces are narrow and feature-specific (`WeatherRemoteRepository`,
  `LocationRepository`, `MapLayerRepository`, …) rather than one god interface.
- **LSP**: `ParentViewModel` subclasses must remain substitutable — don't override base behavior in
  a way that breaks callers relying on the base contract.

When adding code, follow the existing pattern (interface in `domain`, implementation in `data`,
wiring in `di`) rather than introducing a new structure.

## Conventions

- Localized strings live in **two places kept in sync manually**:
  1. `composeApp/src/commonMain/composeResources/values[-es]/*.xml` (Compose UI), split by topic
     (`strings.xml`, `strings_about.xml`, `strings_suggestions.xml`, `strings_loading_dialog.xml`,
     `preference_strings.xml`, `months.xml`).
  2. `iosApp/en.strings` + `iosApp/es.strings` (native iOS notifications/widgets, resolved via
     `SuggestionStringResolver.swift`).
  A new user-facing string touched from both sides needs edits in both places.
- No detekt/ktlint/`.editorconfig` — no automated style enforcement; match surrounding style.
- **No test source sets exist** — don't assume a test command exists or claim tests pass.

## Build & run

- Android debug build: `./gradlew :composeApp:assembleDebug`
- Desktop (JVM) run: `./gradlew :composeApp:run`
- iOS: no Gradle run target — build/run via Xcode (`iosApp/iosApp.xcodeproj`) or the IDE run
  configuration; it links the `ComposeApp` framework produced by the `:composeApp` build.
- Full build, all targets: `./gradlew build`

## Definition of done for a change

- It compiles: `./gradlew :composeApp:assembleDebug` (and `./gradlew build` for multiplatform
  changes; the iOS Xcode project must build for iOS-side changes).
- There is no lint or test command to run — do not invent one.

## Working Rules

- Work one feature/area at a time.
- Use Serena's symbolic tools for code exploration and edits — never read whole `.kt`/`.swift` files
  to explore, and never plain text-replace on them (symbolic edits preserve imports and whitespace).
  Plain `Edit` is fine for non-code files (Gradle `.kts`, XML, `.strings`, markdown, JSON).
- If a task requires changes outside its stated scope, stop and report first.
- Never silently delete code — state what is removed and why.
- Before creating any new file or package, confirm the intended location with the user first.

## Git Workflow

- Integration branch: **`develop`** — confirmed with the user as the target for all PRs opened
  against this repo (`.specs/config.json` `pr.default_target` is already set to `develop`). `main`
  also exists on `origin`, but only as the target of separate, periodic "promote develop → main"
  PRs opened outside this workflow — never a PR base for feature/bugfix/chore work.
- Never commit directly to `develop`/`main`/`master` when using the spec workflow — branch first
  (see `branch.naming` in `.specs/config.json`).
- Host: GitHub (`github.com/Kronos1993/Multiplatform-Weather-App`), PRs via `gh` (see `/create-pr`).

### Forbidden Operations

These require explicit written approval before proceeding:

- `git push --force` / `git push -f`
- `git reset --hard`
- `git commit --amend` on already-pushed commits
- `git rebase -i` without user instruction
- `--no-verify` (skipping hooks)
- Deleting branches without confirmation

## Skills

| Skill | Purpose |
|-------|---------|
| `/commit` | Stage, format message, commit (conventional commits; no automated style check — see Conventions) |
| `/create-pr` | Push current branch + `gh pr create` against `develop` |
| `/solid` | Senior-engineer code review/design lens tuned to this repo's Clean Architecture + MVVM + Koin shape and its lack of a test suite |
| `/new-feature` | Scaffold a complete new feature end-to-end (domain model/repository, data source, Koin wiring, ViewModel, Screen, optional nav route). Chained automatically from `/spec-implement` when a spec's proposal.md §6 names it — see `/spec-plan`'s "Choosing `/new-feature` vs direct edits" |
| `/spec-new`, `/spec-plan`, `/spec-implement`, `/spec-handoff`, `/spec-finalize`, `/spec-status` | Spec-driven workflow — intake is file/manual/URL-based (this repo has no issue tracker). See `.specs/CHOOSING_FINALIZE.md` for the lifecycle overview, `.specs/templates/` for templates, and each skill's own `SKILL.md` for details |
| `/spec-review` *(optional)* | Read-only review of the uncommitted diff against `proposal.md` intent, `plan.md` scope, this repo's architecture rules, and relevant Serena memories. Sits between `/spec-implement` and `/spec-handoff`. Pure reporter — exits 0 always; never blocks handoff |
| `/spec-ship` | One-shot alternative to `/spec-handoff` + `/spec-finalize`: commit + impl PR + pre-merge archive in one run |
| `/release` | Check for changes on `develop` pending publish to `main`, open the `develop`→`main` promote PR that triggers `.github/workflows/publish-android.yml` on merge, and prepare `develop`'s next dev version |

> If a skill file is unavailable, stop and ask — do not infer the pattern manually.

> Before creating a new skill or editing any `SKILL.md`, follow the Agent Skills format
> (`SKILL.md` frontmatter + body, optional `scripts/`/`references/`/`assets/`) — see any existing
> `.claude/skills/*/SKILL.md` for the shape used in this repo.

## Serena Memory Index

At the start of any session, run `list_memories()` to see available context, then read the `core`
memory. **`core` is the entry point** — it links to the rest. Read the relevant memories before
starting any task.

| Task | Read these memories first |
|------|--------------------------|
| Any task, first thing | `core` |
| Understanding the stack / adding a dependency | `tech_stack` |
| Adding/changing a feature, repository, or ViewModel | `architecture` |
| Naming, localization, error handling, style questions | `conventions` |
| Building or running the app | `suggested_commands` |
| Deciding whether a change is "done" | `task_completion` |

## Serena Memory Hygiene

When you discover something non-obvious during any task (a pattern, a gotcha, a package-boundary
rule, a build quirk, a debugging insight), propose saving it as a Serena memory before closing the
session.

Rules:
- Show the proposed memory name and content, wait for approval before calling `write_memory`.
- Prefer updating an existing memory over creating a duplicate — run `list_memories()` first to check.
- Never write a memory that contradicts this file — if you think a rule here needs updating, flag it
  to the user instead.

What to put in a memory (durability):
- What cannot be derived by reading the code today: non-obvious "why"s, traps, lookup pointers.
- Cut implementation specifics that rot with refactors (exact parameter lists, line counts) — point
  at the file/symbol instead.

When to propose a new memory:
- You had to explore more than 2 files to understand something.
- A task revealed a non-obvious platform-specific (`expect`/`actual`) gotcha.
- A build failed for a non-obvious reason and you found the fix.
- You used a pattern not yet documented in any existing memory.
