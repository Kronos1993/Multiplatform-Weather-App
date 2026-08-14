# Managing Complexity

## The Two Types of Complexity

### Essential Complexity
Inherent to the problem domain. Cannot be removed, only managed.
- Business rules
- Domain logic
- User requirements

### Accidental Complexity
Introduced by our solutions. CAN and SHOULD be minimized.
- Poor abstractions
- Unnecessary indirection
- Framework ceremony
- Technical debt

**Goal: Minimize accidental complexity while clearly expressing essential complexity.**

---

## Detecting Complexity

### 1. Change Amplification
Small changes require touching many files.

**Symptom:** "To add this field, I need to update 15 files."

**Cause:** Scattered responsibilities, poor abstraction boundaries.

### 2. Cognitive Load
Code is hard to understand, requires holding too much in memory.

**Symptom:** "I need to understand 10 other classes to understand this one."

**Cause:** Tight coupling, hidden dependencies, unclear naming.

### 3. Unknown Unknowns
Behavior is surprising, side effects are hidden.

**Symptom:** "I changed this, and something completely unrelated broke."

**Cause:** Global state, hidden dependencies, implicit contracts.

---

## The XP Values for Fighting Complexity

From Extreme Programming:

### 1. Communication
Code should communicate clearly. Names, structure, tests all contribute.

### 2. Simplicity
Do the simplest thing that could possibly work.

### 3. Feedback
Fast feedback loops catch complexity early. TDD, CI, code review.

### 4. Courage
Refactor aggressively. Don't let complexity accumulate.

### 5. Respect
Respect future readers (including yourself). Write for humans first.

---

## KISS - Keep It Simple, Silly

> "The simplest solution that works is usually the best."

### How to Apply:
1. Start with the obvious solution
2. Only add complexity when REQUIRED
3. Prefer boring, well-understood approaches
4. Question every abstraction

```kotlin
// Over-engineered
class CitizenServiceFactoryProvider private constructor() {
    companion object {
        private var instance: CitizenServiceFactoryProvider? = null
        fun getInstance(): CitizenServiceFactoryProvider { /* ... */ }
    }
    fun createFactory(): CitizenServiceFactory { /* ... */ }
}

// KISS
class GetCitizenUseCaseImpl @Inject constructor(
    private val repository: CitizensRepository,
) : GetCitizenUseCase() {
    override suspend fun run(params: Params): Result<CitizensModel> =
        repository.getByCitizenId(params.citizenId)
}
```

> **Kotlin/Android:** Don't hand-roll factories/singletons — Hilt supplies
> instances via `@Inject constructor` and `@Binds`/`@Provides`. Reaching for a
> manual factory-provider is usually accidental complexity here.

---

## YAGNI - You Aren't Gonna Need It

> "Don't build features until they're actually needed."

### Warning Signs:
- "We might need this later"
- "It would be nice to have"
- "Just in case"
- "For future extensibility"

### The Cost of YAGNI Violations:
1. **Development time** - Building unused features
2. **Maintenance burden** - Code that must be maintained
3. **Cognitive load** - More to understand
4. **Wrong abstraction** - Guessing future needs incorrectly

```kotlin
// YAGNI violation: Building for hypothetical needs
data class Citizen(
    val name: String,
    val email: String,
    // "We might need these someday"
    val middleName: String? = null,
    val secondaryEmail: String? = null,
    val faxNumber: String? = null,
    val linkedinProfile: String? = null,
    val twitterHandle: String? = null,
)

// YAGNI: Only what's needed NOW
data class Citizen(
    val name: String,
    val email: Email,
)
```

---

## DRY - Don't Repeat Yourself (with The Rule of Three)

> "Every piece of knowledge should have a single, unambiguous representation."

### BUT: The Rule of Three

**Don't extract duplication until you see it THREE times.**

Why? The wrong abstraction is worse than duplication.

```
Duplication #1 → Leave it
Duplication #2 → Note it, leave it
Duplication #3 → NOW extract it
```

### Example:
```kotlin
// First time - leave it
fun processCitizenOrder(order: Order) {
    validate(order)
    calculateTax(order)
    save(order)
}

// Second time - note the similarity, but leave it
fun processGuestOrder(order: Order) {
    validate(order)
    calculateTax(order)
    save(order)
    sendGuestEmail(order)
}

// Third time - NOW extract
fun processCorporateOrder(order: Order) {
    validate(order)
    calculateTax(order)
    save(order)
    applyCorporateDiscount(order)
}

// After three, extract the common parts (higher-order function)
fun processOrder(order: Order, postProcessing: (Order) -> Unit) {
    validate(order)
    calculateTax(order)
    save(order)
    postProcessing(order)
}
```

---

## Separation of Concerns

> "Each module should address a single concern."

### Concerns to Separate:
- **Business logic** vs **Infrastructure**
- **What** (policy) vs **How** (mechanism)
- **Input** vs **Processing** vs **Output**
- **Data** vs **Behavior**

### Example:
```kotlin
// BAD: Mixed concerns — validation, business logic, persistence, notification
class OrderProcessor {
    fun process(order: Order) {
        // Validation
        if (order.items.isEmpty()) throw IllegalStateException("Empty")

        // Business logic
        var total = 0.0
        for (item in order.items) {
            total += item.price * item.quantity
        }

        // Persistence
        val db = Database()
        db.query("INSERT INTO orders...")

        // Notification
        val email = EmailClient()
        email.send(order.customer.email, "Order confirmed")
    }
}

// GOOD: A UseCase orchestrating injected collaborators
abstract class ProcessOrderUseCase : UseCase<ProcessOrderUseCase.Params, Order>() {
    data class Params(val order: Order)
}

class ProcessOrderUseCaseImpl @Inject constructor(
    private val validator: OrderValidator,
    private val calculator: OrderCalculator,
    private val repository: OrderRepository,
    private val notifier: OrderNotifier,
) : ProcessOrderUseCase() {

    override suspend fun run(params: Params): Result<Order> {
        val order = params.order
        validator.validate(order).getOrElse { return Result.failure(it) }
        val total = calculator.calculateTotal(order)
        return repository.save(order.copy(total = total))
            .onSuccess { notifier.notifyConfirmation(it) }
    }
}
```

> **Kotlin/Android:** Each concern is a separately testable, Hilt-injected
> collaborator behind an interface (`OrderValidator`, `OrderRepository`, …). The
> `UseCase` only *orchestrates* — it holds no persistence or transport logic
> itself, honoring Clean Architecture's UseCase → Repository → DataSource flow.
> Return `Result<T>` and let the ViewModel `fold` it.

---

## Managing Technical Debt

### Types of Technical Debt:
1. **Deliberate** - Conscious trade-off for speed
2. **Accidental** - Mistakes, lack of knowledge
3. **Bit rot** - Code degrades over time

### The Boy Scout Rule:
> "Leave the code better than you found it."

Every time you touch code:
- Improve one small thing
- Fix one naming issue
- Extract one method
- Add one missing test

### When to Pay Down Debt:
- When it's in your path (you're already there)
- When it's blocking new features
- When it's causing bugs
- During dedicated refactoring time

### When NOT to Refactor:
- Code that works and won't change
- Code being replaced soon
- When you don't have tests

---

## The Four Elements of Simple Design

In priority order (from XP):

1. **Runs all the tests**
   - If it doesn't work, nothing else matters

2. **Expresses intent**
   - Clear names, obvious structure
   - Code tells the story

3. **No duplication**
   - DRY (but Rule of Three)
   - Single source of truth

4. **Minimal**
   - Fewest classes and methods possible
   - Remove anything unnecessary

If these four are true, the design is simple enough.
