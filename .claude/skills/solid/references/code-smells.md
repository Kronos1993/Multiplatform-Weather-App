# Code Smells & Anti-Patterns

## What Are Code Smells?

Indicators that something MAY be wrong. Not bugs, but design problems that make code hard to understand, change, or test.

## The Five Categories

### 1. Bloaters
Code that has grown too large.

| Smell | Symptom | Refactoring |
|-------|---------|-------------|
| **Long Method** | > 10 lines | Extract Method |
| **Large Class** | > 50 lines, multiple responsibilities | Extract Class |
| **Long Parameter List** | > 3 parameters | Introduce Parameter Object (`data class Params`) |
| **Data Clumps** | Same group of variables appear together | Extract Class |
| **Primitive Obsession** | Primitives instead of small objects | Wrap in value class |

### 2. Object-Orientation Abusers
Misuse of OO principles.

| Smell | Symptom | Refactoring |
|-------|---------|-------------|
| **Type-code `when` chains** | Type checking, large `when`/`if-else` on a type field | Replace with Polymorphism or `sealed class` + `when` |
| **Parallel Inheritance** | Adding subclass requires adding another | Merge Hierarchies |
| **Refused Bequest** | Subclass doesn't use parent methods | Replace Inheritance with Delegation |
| **Alternative Classes** | Different interfaces, same concept | Rename, Extract Superclass |

### 3. Change Preventers
Code that makes changes difficult.

| Smell | Symptom | Refactoring |
|-------|---------|-------------|
| **Divergent Change** | One class changed for many reasons | Extract Class (SRP) |
| **Shotgun Surgery** | One change touches many classes | Move Method/Field together |
| **Parallel Inheritance** | (see above) | Merge Hierarchies |

### 4. Dispensables
Code that can be removed.

| Smell | Symptom | Refactoring |
|-------|---------|-------------|
| **Comments** | Explaining bad code | Rename, Extract Method |
| **Duplicate Code** | Copy-paste | Extract Method, Pull Up Method |
| **Dead Code** | Unreachable code | Delete |
| **Speculative Generality** | "Just in case" code | Delete (YAGNI) |
| **Lazy Class** | Class that does almost nothing | Inline Class |

### 5. Couplers
Excessive coupling between classes.

| Smell | Symptom | Refactoring |
|-------|---------|-------------|
| **Feature Envy** | Method uses another class's data extensively | Move Method |
| **Inappropriate Intimacy** | Classes know too much about each other | Move Method, Extract Class |
| **Message Chains** | `a.getB().getC().getD()` | Hide Delegate |
| **Middle Man** | Class only delegates | Inline Class |

---

## The Seven Most Common Code Smells

### 1. Long Method

**Symptom:** Method > 10 lines, doing multiple things.

```kotlin
// SMELL
fun processOrder(order: Order) {
    // Validate
    if (order.items.isEmpty()) throw IllegalStateException("Empty")
    if (order.customer == null) throw IllegalStateException("No customer")

    // Calculate
    var total = 0.0
    for (item in order.items) {
        total += item.price * item.quantity
        if (item.discount != null) {
            total -= item.discount
        }
    }

    // Apply tax
    val taxRate = getTaxRate(order.customer.state)
    total *= (1 + taxRate)

    // Save
    db.orders.insert(order.copy(total = total))

    // Notify
    emailService.send(order.customer.email, "Order confirmed")
}

// REFACTORED
fun processOrder(order: Order) {
    validateOrder(order)
    val total = calculateTotal(order)
    saveOrder(order, total)
    notifyCustomer(order)
}
```

### 2. Large Class

**Symptom:** Class with many responsibilities, > 50 lines.

```kotlin
// SMELL: God class
class Citizen {
    // Citizen data
    var name: String = ""
    var email: String = ""

    // Authentication
    fun login() { }
    fun logout() { }
    fun resetPassword() { }

    // Preferences
    fun setTheme() { }
    fun setLanguage() { }

    // Notifications
    fun sendEmail() { }
    fun sendSms() { }

    // Billing
    fun charge() { }
    fun refund() { }
}

// REFACTORED: Separate classes
data class Citizen(val name: String, val email: String)
class AuthService { /* login, logout, resetPassword */ }
class CitizenPreferences { /* setTheme, setLanguage */ }
class NotificationService { /* sendEmail, sendSms */ }
class BillingService { /* charge, refund */ }
```

> **Kotlin/Android:** In this project the responsibilities usually split along
> Clean Architecture lines — persistence into a `DataSource`/`Repository`,
> orchestration into a `UseCase`, screen state into a `ViewModel`. A large class
> is a prompt to ask which layer each method belongs to.

### 3. Feature Envy

**Symptom:** Method uses another class's data more than its own.

```kotlin
// SMELL: Order envies Customer
class Order {
    fun calculateShipping(customer: Customer): Int {
        if (customer.country == "US") {
            if (customer.state == "CA") return 10
            return 15
        }
        return 25
    }
}

// REFACTORED: Move to Customer
class Customer(val country: String, val state: String) {
    fun shippingCost(): Int {
        if (country == "US") {
            if (state == "CA") return 10
            return 15
        }
        return 25
    }
}

class Order(private val customer: Customer) {
    fun calculateShipping(): Int = customer.shippingCost()
}
```

### 4. Primitive Obsession

**Symptom:** Using primitives for domain concepts.

```kotlin
// SMELL
fun createCitizen(email: String, age: Int, zipCode: String) {
    // No validation, easy to pass wrong values / swap arguments
    if (!email.contains("@")) throw IllegalArgumentException()
    if (age < 0) throw IllegalArgumentException()
}

// REFACTORED: Value classes
@JvmInline
value class Email(val value: String) {
    init { require(value.contains("@")) { "Invalid email" } }
}

@JvmInline
value class Age(val value: Int) {
    init { require(value in 0..150) { "Invalid age" } }
}

fun createCitizen(email: Email, age: Age, address: Address) {
    // Type system prevents invalid data and argument swaps
}
```

> **Kotlin/Android:** `@JvmInline value class` gives you the type safety with no
> boxing. Wrap primitives that carry an invariant or are easily confused
> (`CitizenId` vs `EventId` — both `Long`); leave incidental primitives alone.
> Confirm before treating raw ids as a smell to fix: if the codebase already
> passes `Long`/`String` ids by convention, wrapping is an opt-in improvement at
> new boundaries, not a defect to refactor across existing signatures.

### 5. Type-code `when` chains

**Symptom:** Switching on a type field, repeated across the codebase.

```kotlin
// SMELL
fun getArea(shape: Shape): Double = when (shape.type) {
    "circle" -> Math.PI * shape.radius * shape.radius
    "rectangle" -> shape.width * shape.height
    "triangle" -> 0.5 * shape.base * shape.height
    else -> error("Unknown shape")
}

fun getPerimeter(shape: Shape): Double = when (shape.type) { // Same when again!
    "circle" -> 2 * Math.PI * shape.radius
    // ...
    else -> error("Unknown shape")
}

// REFACTORED: sealed class + polymorphism
sealed class Shape {
    abstract fun area(): Double
    abstract fun perimeter(): Double

    data class Circle(val radius: Double) : Shape() {
        override fun area() = Math.PI * radius * radius
        override fun perimeter() = 2 * Math.PI * radius
    }

    data class Rectangle(val width: Double, val height: Double) : Shape() {
        override fun area() = width * height
        override fun perimeter() = 2 * (width + height)
    }
}
```

> **Kotlin/Android:** A `when` over a `sealed class` is idiomatic and *good* —
> it's exhaustive and the compiler flags a missing branch. The smell is a `when`
> on a *string/int type-code* that is duplicated in several places; replace the
> type-code with a sealed hierarchy (behavior on each subtype) or, if the
> branches are pure data, a single exhaustive `when`.

### 6. Inappropriate Intimacy

**Symptom:** Classes know too much about each other's internals.

```kotlin
// SMELL
class Order(private val items: List<OrderItem>) {
    fun process(inventory: Inventory) {
        // Reaching into inventory's internals
        for (item in items) {
            val stock = inventory.stockLevels[item.sku]!!
            if (stock.quantity < item.quantity) {
                throw IllegalStateException("Out of stock")
            }
            inventory.stockLevels[item.sku]!!.quantity -= item.quantity
        }
    }
}

// REFACTORED: Tell, don't ask
class Inventory {
    fun reserve(items: List<OrderItem>): ReserveResult {
        // Inventory manages its own state
        items.firstOrNull { !canReserve(it) }?.let {
            return ReserveResult.OutOfStock(it)
        }
        deductStock(items)
        return ReserveResult.Success
    }
}

class Order(private val items: List<OrderItem>) {
    fun process(inventory: Inventory) {
        when (val result = inventory.reserve(items)) {
            is ReserveResult.OutOfStock -> throw OutOfStockError(result.item)
            ReserveResult.Success -> Unit
        }
    }
}
```

### 7. Speculative Generality

**Symptom:** "Just in case" abstractions that aren't used.

```kotlin
// SMELL: Over-engineered for hypothetical needs, throwing overrides
interface PaymentProcessor {
    fun process()
    fun rollback()
    fun audit()
    fun generateReport()
    fun scheduleRecurring()
}

class StripeProcessor : PaymentProcessor {
    override fun process() { /* actual code */ }
    override fun rollback() = TODO("Not implemented")
    override fun audit() = TODO("Not implemented")
    override fun generateReport() = TODO("Not implemented")
    override fun scheduleRecurring() = TODO("Not implemented")
}

// REFACTORED: YAGNI
interface PaymentProcessor {
    fun process()
}

class StripeProcessor : PaymentProcessor {
    override fun process() { /* actual code */ }
}
// Add other methods to the interface when actually needed
```

> **Kotlin/Android:** `TODO()` (which throws `NotImplementedError`) littering an
> interface's implementations is the tell — a fat interface forcing empty or
> throwing overrides. Keep interfaces minimal (ISP); grow them when a second
> real caller appears.

---

## Prevention Strategies

1. **Follow Object Calisthenics** - Rules prevent most smells
2. **Practice TDD** - Tests reveal design problems early (JUnit 4, fakes over mocks)
3. **Review in pairs** - Fresh eyes catch smells
4. **Refactor continuously** - Don't let smells accumulate
5. **Apply SOLID** - Prevents structural smells
6. **Use static analysis** - ktlint / Android Lint catch common issues

---

## When You Find a Smell

1. **Confirm it's a problem** - Not all smells need fixing
2. **Ensure test coverage** - Before refactoring
3. **Refactor in small steps** - Keep tests passing
4. **Commit frequently** - Easy to revert if needed
