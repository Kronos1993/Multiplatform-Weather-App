# Software Architecture

## The Goal of Architecture

Enable the development team to:
1. **Add** features with minimal friction
2. **Change** existing features safely
3. **Remove** features cleanly
4. **Test** features in isolation
5. **Deploy** independently when possible

## Architectural Principles

### 1. Vertical Boundaries (Features/Slices)

Organize by **feature**, not by technical layer.

```
BAD: Layer-first
features/
  activities/
    CitizenActivity.kt
    EventActivity.kt
  viewmodels/
    CitizenViewModel.kt
    EventViewModel.kt
  repositories/
    CitizenRepository.kt
    EventRepository.kt

GOOD: Feature-first
features/
  citizen/
    CitizenActivity.kt
    CitizenViewModel.kt
    CitizenRepository.kt
  event/
    EventActivity.kt
    EventViewModel.kt
    EventRepository.kt
```

**Why:** Changes to the "citizen" feature stay in `features/citizen/`. High cohesion within features.

> **Kotlin/Android:** This project uses Gradle feature modules under
> `features/<feature>/`. A module boundary is a compile-time boundary — Gradle
> enforces the vertical slice, not just convention.

### 2. Horizontal Boundaries (Layers)

Separate concerns into layers with clear dependencies.

```
┌──────────────────────────────────────┐
│           Presentation               │  Activity/Fragment, ViewModel, View/Compose
├──────────────────────────────────────┤
│           Application                │  UseCases, Orchestration
├──────────────────────────────────────┤
│             Domain                   │  Business Logic, Models (data/enum/sealed)
├──────────────────────────────────────┤
│          Infrastructure              │  Repository/DataSource impls, ORM-Lite, APIs
└──────────────────────────────────────┘
```

### 3. The Dependency Rule

**Dependencies point INWARD.**

```
Infrastructure → Application → Domain
      ↓               ↓            ↓
   (outer)        (middle)      (inner)
```

- Inner layers know NOTHING about outer layers
- Domain has zero dependencies on infrastructure
- Use interfaces to invert dependencies

> **Kotlin/Android:** The dependency rule is a **module dependency rule** here.
> Data modules depend on `*-domain`, never the reverse. A `*-domain` module has
> no Android, no ORM-Lite, and no Hilt-implementation dependencies — it holds
> interfaces, UseCases, and models only. `:viuclient` is wiring only.

```kotlin
// Domain module (*-domain) defines the interface (inner)
interface CitizensRepository {
    suspend fun save(citizen: Citizen): Result<Unit>
    suspend fun findById(id: CitizenId): Result<Citizen?>
}

// Data module implements it (outer), delegating to a DataSource
class CitizensRepositoryImpl @Inject constructor(
    private val dataSource: CitizensDataSource,
) : CitizensRepository {
    override suspend fun save(citizen: Citizen): Result<Unit> =
        dataSource.save(citizen)

    override suspend fun findById(id: CitizenId): Result<Citizen?> =
        dataSource.findById(id)
}

// UseCase (Application) depends on the abstraction
class GetCitizenUseCase @Inject constructor(
    private val repository: CitizensRepository, // depends on abstraction
)
```

### 4. Contracts

Interfaces define boundaries between components.

> **Kotlin/Android:** The contract lives in `*-domain`; each implementation
> lives in a data module, and a fake implementation lives in the test source set.
> Hilt binds the real one for the app; tests inject the fake.

```kotlin
// The contract (in *-domain)
interface PaymentGateway {
    suspend fun charge(amount: Money, card: CardDetails): Result<ChargeResult>
    suspend fun refund(chargeId: String): Result<RefundResult>
}

// Multiple implementations possible
class StripeGateway @Inject constructor(/* ... */) : PaymentGateway { /* ... */ }
class PayPalGateway @Inject constructor(/* ... */) : PaymentGateway { /* ... */ }
class FakeGateway : PaymentGateway { /* ... */ }  // For tests
```

### 5. Cross-Cutting Concerns

Concerns that span multiple features: logging, auth, validation, error handling.

**Options:**
- Interceptors (network) / Hilt-provided wrappers
- Delegation (`by`) to add behavior without subclassing
- Base classes (use sparingly — the base `UseCase` is one)

> **Kotlin/Android:** Bind cross-cutting collaborators (loggers, interceptors)
> once in a Hilt module and inject them where needed rather than reaching for
> globals. Keep this layer thin.

```kotlin
// OkHttp interceptor for a network cross-cutting concern
class LoggingInterceptor @Inject constructor(
    private val logger: AuditLogger,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        logger.log("Request: ${request.url}")
        val response = chain.proceed(request)
        logger.log("Response: ${response.code}")
        return response
    }
}
```

### 6. Conway's Law

> "Organizations design systems that mirror their communication structure."

**Implication:** Team structure affects architecture. Align both intentionally.

---

## Common Architectural Styles

### Layered Architecture

Traditional layers: Presentation → Application → Domain → Infrastructure

**Pros:** Simple, well-understood
**Cons:** Can become a "big ball of mud" without discipline

### Hexagonal Architecture (Ports & Adapters)

Domain at center, adapters around the edges.

```
        ┌─────────────────────┐
        │   Activity/ViewModel │
        └─────────┬───────────┘
                  │
┌─────────────────▼─────────────────┐
│              DOMAIN                │
│   ┌─────────────────────────┐     │
│   │      Business Logic      │     │
│   │      Use Cases           │     │
│   └─────────────────────────┘     │
└─────────────────┬─────────────────┘
                  │
        ┌─────────▼───────────┐
        │  DataSource / ORM    │
        └─────────────────────┘
```

**Ports:** Interfaces defined by the domain (Repository, DataSource)
**Adapters:** Implementations that connect to the outside world (ORM-Lite, network)

> **Kotlin/Android:** This matches the project directly — Repository and
> DataSource interfaces are ports in `*-domain`; `RepositoryImpl` and
> `DataSourceImpl` are adapters in the data modules.

### Clean Architecture

Similar to Hexagonal, with explicit layers:

1. **Entities** - Enterprise business rules (domain models)
2. **Use Cases** - Application business rules
3. **Interface Adapters** - ViewModels, Repository/DataSource implementations
4. **Frameworks & Drivers** - Android, ORM-Lite, network, external interfaces

> **Kotlin/Android:** The project's `UseCase → Repository → DataSource` layering
> is Clean Architecture as practiced here.

---

## Feature-Driven Structure (Android feature module)

```
features/
  citizen/
    CitizenActivity.kt        # or Fragment
    CitizenViewModel.kt       # @HiltViewModel, exposes LiveData
    CitizenView.kt            # View binding or Compose UI
    di/
      CitizenModule.kt        # Hilt @Module / @Binds wiring
    build.gradle              # depends on citizen-domain
  event/
    EventActivity.kt
    EventViewModel.kt
    EventView.kt
    di/
```

---

## Feature-Driven Structure (module split)

```
citizen-domain/               # interfaces, UseCases, models — no Android/ORM/Hilt-impl deps
  CitizensRepository.kt       # interface (port)
  CitizensDataSource.kt       # interface (port)
  GetCitizenUseCase.kt        # UseCase
  Citizen.kt                  # data class model

citizen-data-repository-ormlite/
  CitizensRepositoryImpl.kt   # implements CitizensRepository, delegates to DataSource

datasources/citizen-ormlite/
  CitizensDataSourceImpl.kt   # implements CitizensDataSource, ORM-Lite

viuclient/                    # wiring only — no business logic, UI, or data access
```

> **Kotlin/Android:** `*-domain` never depends on data/impl modules. The data
> modules and `:viuclient` depend on `*-domain`. This is the dependency rule
> enforced at the Gradle level.

---

## The Walking Skeleton

Start with a minimal end-to-end slice:

1. **Thinnest possible feature** that touches all layers
2. **Runnable** on-device from day one
3. **Proves the architecture** works

Example walking skeleton for the poll book:
- Operator can select ONE polling station (hardcoded)
- Operator can open an event
- Operator can "authenticate" a voter (just logs)

From there, flesh out each feature fully.

---

## Testing Architecture

```
┌────────────────────────────────────────────┐
│         Instrumented / E2E Tests           │  Few, slow, high confidence
├────────────────────────────────────────────┤
│            Integration Tests               │  Some, medium speed
├────────────────────────────────────────────┤
│              Unit Tests (JUnit)            │  Many, fast, isolated
└────────────────────────────────────────────┘
```

**Test by layer:**
- **Domain / UseCases:** Unit tests with fake repositories (most tests here)
- **ViewModel:** Unit tests with fake UseCases, LiveData assertions
- **Repository / DataSource:** Integration tests against ORM-Lite
- **Instrumented / E2E:** Critical flows only

> **Kotlin/Android:** Because collaborators are injected interfaces, UseCase and
> ViewModel tests inject fakes without Hilt — construct the class directly with
> test doubles.

---

## Architecture Decision Records (ADRs)

Document significant decisions:

```markdown
# ADR 001: Use a local relational store for persistence

## Status
Accepted

## Context
We need on-device persistence. Options: relational store, key-value, flat files.

## Decision
A local relational store for:
- Structured queries over voter/event data
- Team familiarity
- Transactional integrity

## Consequences
- Schema migrations required
- Strong query capabilities
- Encryption of the data store must be handled explicitly
```

---

## Red Flags in Architecture

- **Circular dependencies** between modules
- **Domain depending on infrastructure** (e.g. a `*-domain` module referencing ORM-Lite, Android, or a Hilt impl)
- **Framework code in business logic** (Android/ORM types inside UseCases)
- **No clear boundaries** between feature modules
- **Shared mutable state** across modules
- **"Util" or "Common" modules** that grow forever
- **Database schema driving the domain model**
- **Business logic, UI, or data access in `:viuclient`** (it is wiring only)
