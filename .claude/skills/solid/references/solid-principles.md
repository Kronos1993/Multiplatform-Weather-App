# SOLID Principles

## Overview

SOLID helps structure software to be flexible, maintainable, and testable. These principles reduce coupling and increase cohesion.

## S - Single Responsibility Principle (SRP)

> "A class should have one, and only one, reason to change."

### Problem It Solves
God objects that do everything - hard to test, hard to change, hard to understand.

### How to Apply
Each class handles ONE responsibility. If you find yourself saying "and" when describing what a class does, split it.

```kotlin
// BAD: Multiple responsibilities
class Ballot {
    fun validate(): Boolean { ... }
    suspend fun saveToDatabase() { ... }        // Persistence
    fun renderReceipt(): String { ... }         // Presentation
}

// GOOD: Single responsibility each
class Ballot(private val items: List<BallotItem>) {
    fun validate(): Boolean { ... }
}

interface BallotRepository {
    suspend fun save(ballot: Ballot): Result<Unit>
}

class ReceiptGenerator {
    fun generate(ballot: Ballot): Receipt { ... }
}
```

> **Kotlin/Android:** This project already enforces SRP structurally through Clean
> Architecture: a `UseCase` orchestrates one operation, a `Repository` owns
> persistence access, a `DataSource` owns the ORM-Lite mechanics, and a `ViewModel`
> owns UI state. If a `UseCase` starts touching the DB directly or a `ViewModel`
> starts formatting reports, a responsibility has leaked across a layer.

### Detection Questions
- Does this class have multiple reasons to change?
- Can I describe it without using "and"?
- Would different stakeholders request changes to different parts?

---

## O - Open/Closed Principle (OCP)

> "Software entities should be open for extension but closed for modification."

### Problem It Solves
Having to modify existing, tested code every time requirements change. Risk of breaking working features.

### How to Apply
Design abstractions that allow new behavior through new classes, not edits to existing ones.

```kotlin
// BAD: Must modify to add new authentication mode
class VoterAuthenticator {
    fun authenticate(mode: String, citizen: CitizensModel): Boolean {
        if (mode == "fingerprint") return matchFingerprint(citizen)
        if (mode == "face") return matchFace(citizen)
        // Must add more ifs for QR, MRZ, biographic...
        return false
    }
}

// GOOD: Open for extension
interface AuthenticationMethod {
    suspend fun authenticate(citizen: CitizensModel): Result<Boolean>
}

class FingerprintAuthentication : AuthenticationMethod {
    override suspend fun authenticate(citizen: CitizensModel): Result<Boolean> = ...
}

class FaceAuthentication : AuthenticationMethod {
    override suspend fun authenticate(citizen: CitizensModel): Result<Boolean> = ...
}

// Add a new mode by creating a new class, not modifying existing ones
class QrAuthentication : AuthenticationMethod {
    override suspend fun authenticate(citizen: CitizensModel): Result<Boolean> = ...
}
```

> **Kotlin/Android:** A `sealed class` or `sealed interface` is the idiomatic way to
> model a *closed* set of variants exhaustively (the compiler forces every `when`
> branch). Use an open `interface` (as above) when the set of variants is meant to
> grow, and let Hilt supply the concrete implementations.

### Architectural Insight
OCP at architecture level means: **design your codebase so new features are added by adding code, not changing existing code.**

---

## L - Liskov Substitution Principle (LSP)

> "Subtypes must be substitutable for their base types without altering program correctness."

### Problem It Solves
Subclasses that break expectations, requiring type-checking and special cases.

### How to Apply
Subclasses must honor the contract of the parent. If the parent returns a non-negative count, subtypes cannot return negatives.

```kotlin
// BAD: Violates the contract
open class VoteQuota {
    open fun remaining(): Int = 0 // Non-negative expected
}

class BrokenQuota : VoteQuota() {
    override fun remaining(): Int = -5 // Breaks callers that assume >= 0
}

// GOOD: Enforce the invariant at construction
@JvmInline
value class VoteQuota(val remaining: Int) {
    init { require(remaining >= 0) { "Quota must be non-negative" } }
}
```

### Key Insight
This is why you can swap a fake `CitizensRepository` for the real ORM-Lite-backed
`CitizensRepositoryImpl` in a test - both honor the `CitizensRepository` interface
contract (same method signatures, same `Result<T>` guarantees), so a `UseCase`
under test behaves identically against either.

> **Kotlin/Android:** Kotlin classes are `final` by default; you must opt into
> inheritance with `open`. Favor that default. Substitutability in this codebase
> almost always means implementing a *shared interface* (Repository, DataSource,
> UseCase) rather than extending a concrete class.

---

## I - Interface Segregation Principle (ISP)

> "Clients should not be forced to depend on methods they do not use."

### Problem It Solves
Fat interfaces that force partial implementations, empty methods, or throws.

### How to Apply
Split large interfaces into smaller, cohesive ones. Clients depend only on what they need.

```kotlin
// BAD: Fat interface
interface PollingDevice {
    suspend fun captureFingerprint(): Result<FingerprintData>
    suspend fun scanQr(): Result<String>
    fun printReceipt(receipt: Receipt)
}

class FingerprintReader : PollingDevice {
    override suspend fun captureFingerprint(): Result<FingerprintData> = ...
    override suspend fun scanQr(): Result<String> =
        throw UnsupportedOperationException("Not supported") // Forced!
    override fun printReceipt(receipt: Receipt) =
        throw UnsupportedOperationException("Not supported")
}

// GOOD: Segregated interfaces
interface FingerprintCapture {
    suspend fun captureFingerprint(): Result<FingerprintData>
}

interface QrScanner {
    suspend fun scanQr(): Result<String>
}

interface ReceiptPrinter {
    fun printReceipt(receipt: Receipt)
}

class FingerprintReader : FingerprintCapture {
    override suspend fun captureFingerprint(): Result<FingerprintData> = ...
}
```

### Detection
If you see `throw UnsupportedOperationException(...)`, `TODO()`, or empty method bodies just to satisfy an interface, the interface is too fat.

---

## D - Dependency Inversion Principle (DIP)

> "High-level modules should not depend on low-level modules. Both should depend on abstractions."

### Problem It Solves
Tight coupling to specific implementations (databases, ORM, hardware). Hard to test, hard to swap.

### How to Apply
Depend on interfaces; inject implementations with Hilt. Never `new` a concrete dependency inside business logic.

```kotlin
// BAD: Direct dependency on a concrete class
class GetCitizenUseCaseImpl : GetCitizenUseCase() {
    private val repository = CitizensRepositoryImpl(...) // Locked in!

    override suspend fun run(params: Params): Result<CitizensModel> =
        repository.getByCitizenId(params.citizenId)
}

// GOOD: Depend on the abstraction, let Hilt provide the impl
abstract class GetCitizenUseCase : UseCase<GetCitizenUseCase.Params, CitizensModel>() {
    data class Params(val citizenId: Long)
}

class GetCitizenUseCaseImpl @Inject constructor(
    private val repository: CitizensRepository, // interface, not impl
) : GetCitizenUseCase() {
    override suspend fun run(params: Params): Result<CitizensModel> =
        repository.getByCitizenId(params.citizenId)
}

// Hilt binds the concrete implementation to the abstraction
@Module
@InstallIn(SingletonComponent::class)
abstract class CitizenUseCasesFactory {
    @Binds
    abstract fun providesGetCitizenUseCase(
        impl: GetCitizenUseCaseImpl,
    ): GetCitizenUseCase
}
```

> **Kotlin/Android:** Hilt *is* the dependency-inversion mechanism here. Constructor
> injection (`@Inject constructor`) plus `@Binds`/`@Provides` modules mean production
> code never constructs its own dependencies. In tests you swap in a hand-written
> fake (`object : CitizensRepository { override suspend fun getByCitizenId(...) = ... }`)
> — no framework needed. (`:viuclient` is the one legacy module still on kapt; every
> other module uses ksp.)

### The Dependency Rule
Source code dependencies should point **inward** toward high-level policies (domain logic), never toward low-level details (infrastructure).

```
Infrastructure → Application → Domain
      ↑              ↑            ↑
    (outer)       (middle)     (inner)

Dependencies flow: outer → inner
Never: inner → outer
```

> **Kotlin/Android:** This maps directly onto the project's module layering. A
> `*-data` module depends on the `*-domain` module's Repository/DataSource
> **interfaces** and provides the implementations; the domain module never depends
> on data. Gradle enforces the arrow: if you find yourself wanting a domain module
> to `import` a data class from a data module, the dependency has inverted the wrong
> way and belongs behind an interface.

---

## Applying SOLID at Architecture Level

These principles scale beyond classes:

| Principle | Architecture Application |
|-----------|--------------------------|
| SRP | Each module/layer has one responsibility (UseCase, Repository, DataSource) |
| OCP | New features = new modules, not edits to existing |
| LSP | Any impl behind a shared interface is substitutable |
| ISP | Thin, focused interfaces between layers |
| DIP | Domain modules don't know about ORM-Lite, Hilt, or hardware |

---

## Quick Reference

| Principle | One-Liner | Red Flag |
|-----------|-----------|----------|
| SRP | One reason to change | "This class handles X and Y and Z" |
| OCP | Add, don't modify | `if/else` / `when` chains over a type string |
| LSP | Subtypes are substitutable | Type-checking in calling code |
| ISP | Small, focused interfaces | `throw`/`TODO()` to satisfy an interface |
| DIP | Depend on abstractions | `SomeConcreteImpl(...)` in business logic |
