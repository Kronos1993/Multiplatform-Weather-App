---
name: solid
description: Use this skill when writing code, implementing features, refactoring, planning architecture, designing systems, reviewing code, or debugging anywhere in this Kotlin Multiplatform (Android/iOS/Desktop, Compose Multiplatform) weather app. It transforms junior-level code into senior-engineer quality software through SOLID principles, clean code, and professional software design, tuned to this codebase's Clean Architecture + MVVM + Koin shape, and its complete absence of an automated test suite.
---

# Solid Skills: Professional Software Engineering (Kotlin Multiplatform)

You are now operating as a senior software engineer. Every line of Kotlin
you write, every design decision you make, and every refactoring you
perform must embody professional craftsmanship.

> **Precedence:** this skill supplies general engineering judgment. When it
> conflicts with this codebase's own documented conventions (`CLAUDE.md`,
> `.serena/memories/`) or established patterns, the project's rules win.
> The `references/` files below are shared across this skill's origin
> (a different Kotlin Android app) — their illustrative code and any
> "Kotlin/Android:" callout describing a concrete module/package layout
> is just that, illustrative. For THIS repo's actual shape, always defer
> to `mem:architecture`, `mem:conventions`, and root `CLAUDE.md`.

## When This Skill Applies

**Use this skill when:**
- Writing code (features, fixes, utilities)
- Refactoring existing code
- Planning or designing architecture
- Reviewing code quality
- Debugging issues
- Making design decisions

## Core Philosophy

> "Code is to create products for users & customers. Testable, flexible, and maintainable code that serves the needs of the users is GOOD because it can be cost-effectively maintained by developers."

In this repo specifically: it's a personal, single-maintainer Kotlin
Multiplatform app shipping to real users on Android and iOS. There's no
team to catch a bad design decision in review, and no test suite to
catch a regression — the code itself, and how carefully it follows the
established Clean Architecture shape, is the only safety net.

## The Core Process

### 1. There Is No Automated Test Suite — Design for Manual Verifiability Instead

Per `CLAUDE.md` / `.serena/memories/task_completion.md`: **this repo
has zero test source sets — do not look for or assume a test runner/
framework.** This is a hard fact about this repo, not a gap to silently
work around by inventing a test module unprompted. Since TDD's usual
safety net doesn't exist here:

- **Favor small, obviously-correct functions** over clever ones —
  there's no test to catch a subtle mistake, so the code itself must be
  the evidence of correctness.
- **Make ViewModel state transitions easy to eyeball** — a sealed
  `XxxScreenState` (e.g. `WeatherScreenState`: `Idle`/`Loading`/
  `NoWeather`/`WeatherObtained`) should make every reachable UI state
  explicit; a `when` over it should be exhaustive, not defaulted.
- **If you do add a test module**, that's a deliberate, separate
  decision to raise with the user first — don't quietly bolt tests onto
  an otherwise test-free repo without discussing it, since it changes
  the build/CI story for the whole project.

See: [references/tdd.md](references/tdd.md) — read it as "why tests help,"
not as a mandate you can act on unilaterally here.

### 2. Apply SOLID Principles Rigorously

| Principle | Question to Ask |
|-----------|-----------------|
| **S**RP | "Does this have ONE reason to change?" |
| **O**CP | "Can I extend without modifying?" |
| **L**SP | "Can subtypes replace base types safely?" |
| **I**SP | "Are clients forced to depend on unused methods?" |
| **D**IP | "Do high-level modules depend on abstractions?" |

This codebase's DI mechanism is **Koin** (a service locator wired at
startup via `initKoin()` in `di/Koin.kt`), not a compile-time DI
container:

- **DIP** — ViewModels and other consumers depend on `domain/repository/*`
  interfaces (`WeatherRemoteRepository`, `LocationRepository`,
  `MapLayerRepository`, …), never on the concrete `data/repository/
  <feature>/*Impl` classes. Koin binds interface → implementation in the
  `di/` modules; a ViewModel never `get()`s an `Impl` type directly.
- **OCP** — a new weather data source or feature is a new
  `domain/repository` interface + `data/repository/<feature>/*Impl` pair
  registered in Koin — never a modification of an unrelated feature's
  internals.
- **ISP** — each repository interface is scoped to one concern
  (`LocationRepository` doesn't also expose weather-alert methods) — not
  a grab-bag shared across features.

See: [references/solid-principles.md](references/solid-principles.md)

### 3. Write Clean, Human-Readable Code

**Naming (in order of priority):**
1. **Consistency** — `Xxx­ViewModel`, `XxxScreen`, `XxxScreenState`,
   `XxxRepository` / `XxxRepositoryImpl` are naming *patterns* baked
   into every existing feature; a new feature must follow them exactly,
   not invent parallel names.
2. **Understandability** — domain language from the weather domain
   (Forecast, Alert, Location, Suggestion) rather than ad hoc synonyms.
3. **Specificity**, **Brevity**, **Searchability**.

**Structure:**
- `core/result/Result.kt`'s sealed `Result` (`Success`/`Error`) is the
  standard return type for repository/data-layer operations that can
  fail — prefer it over throwing. Convert failures into a sealed
  `XxxScreenState` in the ViewModel, don't let exceptions reach the UI.
- `core/viewmodel/ParentViewModel` is the base class every feature
  ViewModel extends — don't duplicate its `refreshing`/limit-offset/
  `log()` plumbing in a new ViewModel.
- `expect`/`actual` platform code uses the file-suffix convention:
  `.ios.kt` (iOS), `.native.kt` (shared native/iOS-only), unsuffixed
  under `androidMain`/`jvmMain` source dirs. A new `expect` in
  commonMain needs a matching `actual` in every source set that needs
  it — a missing one is a compile error for that target only, easy to
  miss if you're not building all targets.
- This repo has no lint/formatter (no detekt/ktlint/`.editorconfig`) —
  match the surrounding file's style exactly; don't introduce a
  personal style variant.

See: [references/clean-code.md](references/clean-code.md)

### 4. Design with Responsibility in Mind

**Object Stereotypes, mapped to this codebase's real class families:**
- **Information Holder** — `domain/model/*` (platform-agnostic data
  classes), `data/remote/dto/*` (API wire format)
- **Service Provider** — `data/repository/<feature>/*Impl` (actual
  network/DB work via Ktor/Room)
- **Coordinator** — `features/<feature>/XxxViewModel` (holds UI state,
  delegates to repositories, converts `Result` into `XxxScreenState`)
- **Interfacer** — `domain/repository/*` interfaces (the DIP boundary
  between a ViewModel and the concrete data layer)

See: [references/object-design.md](references/object-design.md)

### 5. Manage Complexity Ruthlessly

**Essential complexity** — reconciling three platforms (Android, iOS,
Desktop) behind one shared UI/logic layer, plus real-world data (GPS
location, a third-party weather API, offline caching) is inherently
fiddly.
**Accidental complexity** — anything introduced by our own solution.

**Detect complexity through:**
- Change amplification — adding one new piece of weather data should
  touch a DTO, a mapper, a domain model field, and a ViewModel/UI
  consumer; needing to touch far more than that for "just one field" is
  a signal something's off.

**Fight complexity with:** YAGNI, KISS, DRY (Rule of Three) — and note
that `core/` is this repo's equivalent of a strict shared API
(`ParentViewModel`, `Result`, `core/di`, `core/preferences`) consumed
across every feature. Changing one of these ripples across the app —
check with `find_referencing_symbols` before touching `core/`.

See: [references/complexity.md](references/complexity.md)

### 6. Architect for Change

Respect the established flow:

```
features/<feature>/XxxScreen (Composable)
  → features/<feature>/XxxViewModel (extends ParentViewModel)
  → domain/repository/*Interface  (DIP boundary)
  → data/repository/<feature>/*Impl
  → data/remote (Ktor client + DTOs) | data/local (Room DAOs/entities)
```

- **Features only depend on `domain` interfaces, `core`, and their own
  packages** — never directly on another feature's ViewModel or on a
  `data/*Impl` class. If you find yourself wanting one feature to reach
  into another's internals, that capability likely belongs in `core` or
  `domain`, or should be exposed as its own repository method instead.
- **Everything cross-layer is wired through Koin**, resolved by
  constructor injection at the `di/` boundary — never `new` up a
  concrete repository implementation inside a ViewModel as a shortcut;
  it breaks the DIP boundary the whole app depends on.
- **KMP expect/actual is the platform boundary** — new
  platform-specific behavior (notifications, background jobs, GPS)
  gets an `expect` declaration in `commonMain` and an `actual` in every
  source set that needs it, never a hardcoded `if (Platform.isAndroid)`
  branch inside shared code.

See: [references/architecture.md](references/architecture.md)

## The Four Elements of Simple Design (XP)

1. **Passes verification** — since there's no automated test, "passes"
   means the app builds (`./gradlew :composeApp:assembleDebug` /
   `./gradlew build`) and behaves correctly in a real run on the
   affected platform(s).
2. **Expresses intent**
3. **No duplication** (Rule of Three)
4. **Minimal**

## Code Smell Detection

| Smell | Solution |
|-------|----------|
| ViewModel doing repository work directly (inline Ktor/Room calls) | Extract to a `data/repository/*Impl` behind a `domain/repository` interface |
| Large Composable doing too much | Extract sub-composables, single responsibility |
| `when` over a sealed `XxxScreenState` with no `else` needed but one added anyway | Keep it exhaustive — an added branch later should be a compile error, not silently swallowed |
| Missing `actual` for a new `expect` on one platform | Compile error on that target only — always build/check all affected targets, not just the one you're actively running |
| Primitive Obsession | Prefer domain model types over raw `String`/`Double` once a value crosses into `domain/model` — DTOs at the network boundary are the one place raw types are expected |
| Speculative Generality | YAGNI — don't add a repository method or config flag "in case a future screen needs it" |

See: [references/code-smells.md](references/code-smells.md)

## Design Patterns Awareness

**Already pervasive here — recognize and extend, don't reinvent:**
- **Repository** (`domain/repository` interface + `data/repository/*Impl`)
  — this codebase's core abstraction over data access
- **MVVM** (`XxxViewModel` + `XxxScreenState` + `XxxScreen`) — every
  feature
- **Service Locator via Koin** — `initKoin()` resolving dependencies at
  startup, not a compile-time DI graph
- **Result/Either-style error handling** (`core/result/Result.kt`) —
  makes failure a first-class return value instead of a thrown exception

**Warning:** don't introduce a second DI mechanism alongside Koin,
don't bypass the `Result` type with raw `try`/`catch` scattered through
ViewModels — that's a structural change to the app's error-handling
contract, not a local code-quality improvement.

See: [references/design-patterns.md](references/design-patterns.md)

## Pre-Code Checklist

1. [ ] Do I understand the requirement?
2. [ ] Is this a new feature (new package under `features/`) or an
       addition to an existing one? Get this right before writing
       anything — they're different scopes.
3. [ ] What is the simplest solution?
4. [ ] Have I checked `mem:architecture` / an existing sibling feature
       for the pattern to follow?
5. [ ] Am I solving a real problem or a hypothetical one?

## During-Code Checklist

1. [ ] Is this the simplest thing that could work?
2. [ ] Does the ViewModel depend on a `domain/repository` interface, not a concrete `*Impl`?
3. [ ] Is every reachable UI state represented in the sealed `XxxScreenState`?
4. [ ] Did I add an `actual` for every `expect` I touched, on every affected platform?
5. [ ] Did I update BOTH string locations if this touches a user-facing string shown on iOS too (`mem:conventions`)?

## Post-Code Checklist

1. [ ] Have I run the app on the affected platform(s) and manually exercised the change (no automated test will catch a regression)?
2. [ ] Is there any dead code to remove?
3. [ ] Are names still accurate after changes?
4. [ ] Would a teammate (future you) understand this without running it?

## Red Flags — Stop and Rethink

- A ViewModel importing a `data/repository/*Impl` class directly
- A sealed `XxxScreenState` `when` with a silent `else -> {}` catching
  states that should be handled explicitly
- A feature reaching into another feature's ViewModel or screen state
  instead of going through `domain`/`core`
- A new `expect` added without its `actual` on every affected platform
- Silently adding a test module without raising it with the user first
- Business logic leaking into a Composable instead of living in the
  ViewModel or repository

## Remember

> "A little bit of duplication is 10x better than the wrong abstraction."

> "Focus on WHAT needs to happen, not HOW it needs to happen."

The journey: Code-first → Best-practice-first → Pattern-first →
Responsibility-first → **Systems Thinking**.
