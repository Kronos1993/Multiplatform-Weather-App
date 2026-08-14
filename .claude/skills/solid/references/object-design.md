# Object-Oriented Design

## Responsibility-Driven Design (RDD)

The key insight: **Objects are defined by their responsibilities, not their data.**

### Finding Objects

Start with:
1. **Nouns** in requirements → candidate objects
2. **Verbs** → candidate methods/behaviors
3. **Domain concepts** → value objects

### Finding Responsibilities

Each object should answer:
- What does this object **know**?
- What does this object **do**?
- What does this object **decide**?

### Object Stereotypes

Every class fits one (or maybe two) stereotypes:

| Stereotype | Purpose | Example |
|------------|---------|---------|
| **Information Holder** | Knows things, holds data | `CitizensModel`, `PollingStation` |
| **Structurer** | Maintains relationships | `EventVoters`, `RoleWorkflow` |
| **Service Provider** | Performs work | `FingerprintMatcher`, `ReceiptPrinter` |
| **Coordinator** | Orchestrates workflow | `AuthenticateVoterUseCase` |
| **Controller** | Makes decisions, delegates | `SearchViewModel` |
| **Interfacer** | Transforms between systems | `CitizensDataSourceImpl`, `MrzParser` |

### The Two Questions

For every class, ask:
1. **"What pattern is this?"** - Which stereotype? Which design pattern?
2. **"Is it doing too much?"** - Check object calisthenics rules

If you can't answer clearly, the class needs refactoring.

---

## Tell, Don't Ask

**Command objects to do work. Don't interrogate them and do the work yourself.**

```kotlin
// BAD: Asking, then doing (anemic data bag)
if (quota.remaining >= 1) {
    quota.remaining = quota.remaining - 1
    // more logic here...
}

// GOOD: Telling
val result = quota.consumeOne()
result.fold(
    onSuccess = { /* ... */ },
    onFailure = { /* ... */ },
)
```

The object that has the data should have the behavior.

> **Kotlin/Android:** Kotlin `val` properties are idiomatic and completely fine —
> exposing state is not the anti-pattern. The smell is an **anemic data bag**: an
> object whose callers read its fields and mutate them to perform logic the object
> should own. A `data class` used purely as a DTO across layers is fine; a `data class`
> that everyone reaches into to enforce rules is not. Keep the behavior next to the
> data that governs it.

---

## Design by Contract (DbC)

Every method has:
- **Preconditions** - What must be true BEFORE calling
- **Postconditions** - What will be true AFTER calling
- **Invariants** - What is ALWAYS true about the object

```kotlin
class VoteQuota(private var remaining: Int) {
    init { require(remaining >= 0) }

    // INVARIANT: remaining is never negative

    // PRECONDITION: none (count is validated at construction)
    // POSTCONDITION: remaining decreased by one OR a failure is returned
    fun consumeOne(): Result<Int> {
        if (remaining <= 0) {
            return Result.failure(NoQuotaRemainingException())
        }
        remaining -= 1
        return Result.success(remaining)
    }
}
```

> **Kotlin/Android:** `require(...)` (throws `IllegalArgumentException`) and
> `check(...)` (throws `IllegalStateException`) express pre/postconditions concisely.
> For recoverable outcomes across layer boundaries, return `Result<T>` rather than
> throwing — the project's repositories and use cases use `suspend fun ...: Result<T>`
> and callers `.fold(...)`.

---

## Composition Over Inheritance

**Prefer composing objects over extending classes.**

### Why Inheritance is Problematic:
- Tight coupling between parent and child
- Fragile base class problem
- Difficult to change parent without breaking children
- Forces "is-a" relationship that may not fit

### When to Use Inheritance:
- True "is-a" relationship (rare)
- Framework requirements
- Template Method pattern (intentional)

### Prefer Composition:
```kotlin
// BAD: Inheritance for behavior variation
open class Voter { open fun priority(): Int = 0 }
class PriorityVoter : Voter() { override fun priority(): Int = 10 }

// GOOD: Constructor-injected strategy
interface PriorityPolicy {
    fun priorityFor(citizen: CitizensModel): Int
}

class Voter(private val priorityPolicy: PriorityPolicy) {
    fun priority(citizen: CitizensModel): Int = priorityPolicy.priorityFor(citizen)
}

// Behavior is now pluggable
Voter(ElderlyPriorityPolicy())
Voter(StandardPriorityPolicy())
Voter(NoPriorityPolicy())
```

> **Kotlin/Android:** Kotlin's class delegation (`by`) makes composition first-class —
> `class LoggingRepository(private val delegate: CitizensRepository) : CitizensRepository by delegate`
> forwards every method to the delegate and lets you override just the ones you care
> about. Prefer `by` (or constructor-injected strategies as above) over `open`/`override`
> hierarchies. Note the domain `UseCase` layer does use an intentional Template Method:
> `abstract class ...UseCase : UseCase<Params, T>()` with a `suspend fun run(params)`
> hook — that inheritance is a framework contract, not behavior sharing.

---

## The Law of Demeter (Principle of Least Knowledge)

**Only talk to your immediate friends.**

A method should only call:
1. Methods on `this`
2. Methods on parameters
3. Methods on objects it creates
4. Methods on its direct components

```kotlin
// BAD: Reaching through objects
citizen.getPollingStation().getMunicipality().getName()

// GOOD: Ask the immediate friend
citizen.pollingStationName()
```

This reduces coupling - changes to `Municipality` don't ripple through all callers.

---

## Encapsulation

**Hide internal details, expose behavior.**

### Levels of Encapsulation:
1. **Data** - private mutable state, no direct external mutation
2. **Implementation** - how things work internally
3. **Type** - concrete class hidden behind interface
4. **Design** - architectural decisions hidden from clients

```kotlin
// BAD: Exposed mutable internals
class BallotBox {
    val ballots: MutableList<Ballot> = mutableListOf()
    var count: Int = 0
}

// Client can corrupt state
box.ballots.add(ballot)
box.count = -999 // Oops!

// GOOD: Encapsulated
class BallotBox {
    private val ballots = mutableListOf<Ballot>()

    val count: Int get() = ballots.size // read-only view

    fun cast(ballot: Ballot) {
        ballots.add(ballot)
        // invariants stay internal
    }
}
```

> **Kotlin/Android:** The idiomatic pattern is a private `MutableList`/`MutableLiveData`
> exposed through a read-only type — exactly how ViewModels publish state:
> `private val _state = MutableLiveData<...>()` exposed as `val state = _state.asLiveData()`.
> Callers can observe but never mutate. Expose `List`, not `MutableList`; `LiveData`,
> not `MutableLiveData`.

---

## Polymorphism

**Replace conditionals with types.**

```kotlin
// BAD: Type checking
fun authenticate(mode: String, citizen: CitizensModel): Boolean {
    if (mode == "fingerprint") return matchFingerprint(citizen)
    if (mode == "face") return matchFace(citizen)
    if (mode == "qr") return matchQr(citizen)
    throw IllegalArgumentException("Unknown mode")
}

// GOOD: Polymorphism
interface AuthenticationMethod {
    suspend fun authenticate(citizen: CitizensModel): Result<Boolean>
}

class FingerprintAuthentication : AuthenticationMethod {
    override suspend fun authenticate(citizen: CitizensModel): Result<Boolean> = ...
}

class FaceAuthentication : AuthenticationMethod {
    override suspend fun authenticate(citizen: CitizensModel): Result<Boolean> = ...
}

// Usage - no conditionals
suspend fun authenticate(method: AuthenticationMethod, citizen: CitizensModel) =
    method.authenticate(citizen)
```

> **Kotlin/Android:** When the variant set is genuinely closed, a `sealed class`/
> `sealed interface` with an exhaustive `when` (no `else`) is equally polymorphic and
> compiler-checked — the right tool for UI events/states. Use an open `interface`
> when the set should stay extensible (OCP).

---

## Value Objects vs Entities

### Value Objects
- Defined by their attributes (no identity)
- Immutable
- Compared by value
- Examples: `CitizenId`, `DocumentNumber`, `DateRange`

```kotlin
// Single-value wrapper: value class with validation
@JvmInline
value class CitizenId(val value: Long) {
    init { require(value > 0) { "CitizenId must be positive" } }
}

// Multi-attribute value object: data class (equals/hashCode by value, free)
data class DateRange(val start: Long, val end: Long) {
    init { require(start <= end) { "start must not exceed end" } }

    fun contains(timestamp: Long): Boolean = timestamp in start..end
}
```

> **Kotlin/Android:** `@JvmInline value class` is the idiomatic wrapper for a single
> primitive that deserves a type and validation (no allocation at runtime). `data class`
> gives value equality for multi-field value objects for free — no hand-written `equals`.

### Entities
- Have identity (survives attribute changes)
- Usually mutable (via methods)
- Compared by identity, not by all attributes
- Examples: `CitizensModel`, `EventModel`

```kotlin
// Entity: identity is the citizenId, not the full attribute set.
// A plain data class compares by ALL fields, so override equality
// to compare by identity when the object is an entity.
class CitizensModel(
    val citizenId: CitizenId,
    var documentNumber: String,
    var fullName: String,
) {
    override fun equals(other: Any?): Boolean =
        other is CitizensModel && other.citizenId == citizenId

    override fun hashCode(): Int = citizenId.hashCode()

    fun updateDocument(newNumber: String) {
        documentNumber = newNumber // still the same citizen
    }
}
```

---

## Aggregates

A cluster of objects treated as a single unit for data changes.

- One object is the **aggregate root** (entry point)
- External code only references the root
- Root enforces invariants for the entire cluster

```kotlin
// Event is the aggregate root; authenticated voters live inside it
class Event(private val maxVoters: Int) {
    private val voters = mutableListOf<CitizensModel>()

    val voterCount: Int get() = voters.size

    // All access goes through the root
    fun admit(citizen: CitizensModel): Result<Unit> {
        if (voters.size >= maxVoters) {
            return Result.failure(EventCapacityExceededException())
        }
        voters.add(citizen)
        return Result.success(Unit)
    }

    fun remove(citizenId: CitizenId) {
        voters.removeAll { it.citizenId == citizenId }
    }
}

// BAD: Reaching into the cluster directly
// event.voters.add(citizen) // Won't compile — voters is private (bypasses the invariant)

// GOOD: Through the root
event.admit(citizen) // capacity check happens
```

> **Kotlin/Android:** Keep the collection `private` and expose only intent-revealing
> methods plus read-only views — this is the same encapsulation rule applied at the
> cluster level. The root is the only place the aggregate's invariants live.
