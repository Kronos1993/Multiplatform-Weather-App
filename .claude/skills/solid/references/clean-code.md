# Clean Code Practices

## What is Clean Code?

Code that is:
- **Easy to understand** - reveals intent clearly
- **Easy to change** - modifications are localized
- **Easy to test** - dependencies are injectable
- **Simple** - no unnecessary complexity

## The Human-Centered Approach

Code has THREE consumers:
1. **Users** - get their needs met
2. **Customers** - make or save money
3. **Developers** - must maintain it

Design for all three, but remember: **developers read code 10x more than they write it.**

## Naming Principles

### 1. Consistency & Uniqueness (HIGHEST PRIORITY)
Same concept = same name everywhere. One name per concept.

```kotlin
// BAD: Inconsistent names for same concept
fun getCitizenById(id: Long)
fun fetchVoterById(id: Long)
fun retrieveElectorById(id: Long)

// GOOD: Consistent
fun getCitizen(id: Long)
fun getEvent(id: Long)
fun getPollingStation(id: Long)
```

### 2. Understandability
Use domain language, not technical jargon.

```kotlin
// BAD: Technical
val arr = citizens.filter { it.flag }

// GOOD: Domain language
val authenticatedVoters = citizens.filter { it.isAuthenticated }
```

### 3. Specificity
Avoid vague names: `data`, `info`, `manager`, `handler`, `processor`, `utils`

```kotlin
// BAD: Vague
class DataManager { }
fun processInfo(data: Any) { }

// GOOD: Specific
class CitizensRepositoryImpl { }
fun validateFingerprint(sample: FingerprintSample) { }
```

### 4. Brevity (but not at cost of clarity)
Short names are good only if meaning is preserved.

```kotlin
// BAD: Too cryptic
val ctzLst = getCtzs()

// BAD: Unnecessarily long
val listOfAllAuthenticatedVotersInTheEvent = getAuthenticatedVoters()

// GOOD: Brief but clear
val authenticatedVoters = getAuthenticatedVoters()
```

### 5. Searchability
Names should be unique enough to grep/search.

```kotlin
// BAD: Common word, hard to search
val data = fetch()

// GOOD: Unique, searchable
val citizenSummary = fetchCitizenSummary()
```

### 6. Pronounceability
You should be able to say it in conversation.

```kotlin
// BAD
val genymdhms = generateYearMonthDayHourMinuteSecond()

// GOOD
val timestamp = generateTimestamp()
```

### 7. Austerity
Avoid unnecessary filler words.

```kotlin
// BAD: Redundant
val citizenData = citizen  // 'Data' adds nothing
class CitizenClass         // 'Class' adds nothing

// GOOD
val citizen = citizen
class Citizen
```

---

## Object Calisthenics (9 Rules)

Exercises to improve OO design. Follow strictly during practice, relax slightly in production.

> **Kotlin/Android:** These are practice exercises, not laws. Kotlin's language
> features (data classes, `val` properties, `when` expressions, extension
> functions) already satisfy the *spirit* of several rules while contradicting
> their literal TypeScript-era wording. Where a rule below is softened, the
> annotation says why — keep the underlying principle, drop the absolutism.

### 1. One Level of Indentation per Method

```kotlin
// BAD: Multiple levels
fun process(orders: List<Order>) {
    for (order in orders) {
        if (order.isValid()) {
            for (item in order.items) {
                if (item.inStock) {
                    // process...
                }
            }
        }
    }
}

// GOOD: Extract functions, lean on the stdlib
fun process(orders: List<Order>) {
    orders.filter { it.isValid() }.forEach(::processOrder)
}

fun processOrder(order: Order) {
    order.items.filter { it.inStock }.forEach(::processItem)
}
```

### 2. Don't Use the ELSE Keyword

Use early returns, guard clauses, or polymorphism.

```kotlin
// BAD: else
fun getDiscount(citizen: Citizen): Int {
    if (citizen.isPremium) {
        return 20
    } else {
        return 0
    }
}

// GOOD: Early return
fun getDiscount(citizen: Citizen): Int {
    if (citizen.isPremium) return 20
    return 0
}
```

> **Kotlin/Android:** Prefer early return and guard clauses for imperative
> branches. But `else` in a `when` *expression* is normal and often required for
> exhaustiveness — do not contort code to avoid it. `when (status) { … else -> … }`
> is idiomatic and fine.

### 3. Wrap All Primitives and Strings

Primitives should be wrapped in domain objects when they carry meaning or invariants.

```kotlin
// BAD: Primitive obsession
fun createCitizen(email: String, age: Int) { }

// GOOD: Value classes with invariants
@JvmInline
value class Email(val value: String) {
    init { require(value.contains("@")) { "Invalid email" } }
}

@JvmInline
value class Age(val value: Int) {
    init { require(value in 0..150) { "Invalid age" } }
}

fun createCitizen(email: Email, age: Age) { }
```

> **Kotlin/Android:** Use `@JvmInline value class` for single-value wrappers
> (zero runtime allocation) and `data class` for compound values. Don't wrap
> *every* primitive — wrap the ones that carry an invariant or domain meaning
> (an `Email`, a `CitizenId`). A loop counter stays an `Int`. Treat this as
> aspirational: a codebase that already passes raw `Long`/`String` ids
> everywhere makes wholesale wrapping a large, friction-heavy refactor. Apply it
> at *new* boundaries where it prevents a realistic bug, not retroactively.

### 4. First-Class Collections

Any class with a collection should have no other instance variables.

```kotlin
// BAD: Collection mixed with other state
class Order {
    val items: MutableList<OrderItem> = mutableListOf()
    var customerId: String = ""
    var total: Double = 0.0
}

// GOOD: Collection is its own class
class OrderItems(private val items: List<OrderItem> = emptyList()) {
    fun add(item: OrderItem): OrderItems = OrderItems(items + item)
    fun total(): Money = items.fold(Money.ZERO) { acc, i -> acc + i.subtotal() }
    fun isEmpty(): Boolean = items.isEmpty()
}

class Order(
    private val items: OrderItems,
    private val customerId: CustomerId,
)
```

### 5. One Dot per Line (Law of Demeter)

Don't chain through object graphs.

```kotlin
// BAD: Train wreck
val city = order.customer.address.city

// GOOD: Tell, don't ask
val city = order.shippingCity()
```

### 6. Don't Abbreviate

If a name is too long to type, the class is doing too much.

```kotlin
// BAD
val ctzRepo = CtzRepo()
val ord = Ord()

// GOOD
val citizensRepository = CitizensRepositoryImpl()
val order = Order()
```

### 7. Keep All Entities Small

- Classes: < 50 lines
- Methods: < 10 lines
- Files: < 100 lines

If larger, it's probably doing too much. Split it.

> **Kotlin/Android:** Treat these as *guidelines*, not hard limits. A `ViewModel`
> orchestrating several LiveData streams, or a `DataSourceImpl` mapping a wide
> table, is legitimately larger than 50 lines. The real question is "one
> responsibility?" not "how many lines?". Split when responsibilities diverge,
> not when a line counter trips.

### 8. No Classes with More Than Two Instance Variables

Forces small, focused classes.

```kotlin
// BAD: Too many unrelated STATE fields
class Order {
    var id: String = ""
    var customerId: String = ""
    var items: List<Item> = emptyList()
    var total: Double = 0.0
    var status: String = ""
}

// GOOD: Composed of smaller objects
class Order(
    private val id: OrderId,
    private val details: OrderDetails,
)

class OrderDetails(
    private val customer: Customer,
    private val lineItems: LineItems,
)
```

> **Kotlin/Android:** This rule targets sprawling *state*. Constructor-injected
> *dependencies* (`@Inject constructor(private val repository: …, private val
> logger: …, …)`) do **not** count — a UseCase or ViewModel with five injected
> collaborators is normal and correct. Watch instead for many unrelated mutable
> state fields, which signal a class doing too much.

### 9. No Getters/Setters/Properties

Objects should have behavior, not just data. Tell objects what to do.

```kotlin
// BAD: Anemic data bag the caller interrogates and mutates
class Account {
    var balance: Money = Money.ZERO
}

// Caller does all the work
if (account.balance >= amount) {
    account.balance = account.balance - amount
}

// GOOD: Behavior-rich object
class Account(private var balance: Money) {
    fun withdraw(amount: Money): WithdrawResult {
        if (!canWithdraw(amount)) {
            return WithdrawResult.InsufficientFunds
        }
        balance = balance.subtract(amount)
        return WithdrawResult.Success
    }

    private fun canWithdraw(amount: Money): Boolean = balance >= amount
}

// Caller tells, object decides
val result = account.withdraw(amount)
```

> **Kotlin/Android:** Kotlin `val` properties are idiomatic and encouraged — the
> rule is *not* "never expose properties". The real smell is an **anemic data
> bag**: a logic-free object that callers interrogate *and* mutate to do work
> that belongs on the object itself (Tell, Don't Ask). `data class`es used as
> immutable DTOs / domain values (`CitizensModel`, `Params`) are perfectly fine —
> they carry no behavior *because they represent data*. Reserve this rule for
> entities that own an invariant, like `Account` above.

---

## Comments

### When to Write Comments

**Only write comments to explain WHY, not WHAT or HOW.**

Code explains what and how. Comments explain business reasons, non-obvious decisions, or warnings.

```kotlin
// BAD: Explains what (redundant)
// Add 1 to counter
counter++

// GOOD: Explains why
// Compensate for 0-based page index in the legacy scanner SDK
counter++
```

> **Kotlin/Android:** A comment should describe only the code it annotates —
> what it does, why it behaves that way, any non-obvious constraint. Keep
> issue-tracker references (ticket IDs, URLs) out of comments; that
> traceability belongs in commit messages and merge-request descriptions.

### Prefer Self-Documenting Code

Instead of commenting, rename to make intent clear.

```kotlin
// BAD: Comment needed
// Check if citizen can be authenticated by fingerprint
if (citizen.hasEnrolledFingerprints && !citizen.isBlocked) { }

// GOOD: Self-documenting
if (citizen.canAuthenticateByFingerprint()) { }
```

---

## Formatting

### Vertical Spacing
- Related code together
- Blank lines between concepts
- Most important/public at top

### Horizontal Spacing
- Consistent indentation (ktlint 0.44.0, official Kotlin style)
- Space around operators
- Max line length ~80-120 characters

### Storytelling
Code should read top-to-bottom like a story. High-level at top, details below.

```kotlin
class OrderProcessor {
    // Public API first
    fun process(order: Order): ProcessResult {
        validate(order)
        calculateTotals(order)
        return save(order)
    }

    // Supporting methods below, in order of appearance
    private fun validate(order: Order) { }
    private fun calculateTotals(order: Order) { }
    private fun save(order: Order): ProcessResult { }
}
```
