# Design Patterns

## What Are Design Patterns?

Reusable solutions to common design problems. A shared vocabulary for discussing design.

## WARNING: Don't Force Patterns

> "Let patterns emerge from refactoring, don't force them upfront."

Patterns should solve problems you HAVE, not problems you MIGHT have.

> **Kotlin/Android:** Many classic GoF patterns are already provided by the
> language (delegation `by`, `object`, sealed classes) or the framework
> (Hilt DI, LiveData/Flow). Reach for a hand-rolled pattern only when these
> don't already cover the need.

## When to Use Patterns

1. **You recognize the problem** - You've seen it before
2. **The pattern fits** - Not forcing it
3. **It simplifies** - Doesn't add unnecessary complexity
4. **Team understands it** - Shared knowledge

---

## Creational Patterns

### Singleton

**Purpose:** Ensure only one instance exists.

**When to use:** Global configuration, connection pools, logging.

**Warning:** Often overused. Consider dependency injection instead.

> **Kotlin/Android:** A hand-rolled singleton is almost never needed here.
> Use a Kotlin `object` for a stateless global, or — preferably — let Hilt own
> the lifetime with `@Singleton` scoping so the instance is injectable and
> testable. Prefer the DI-scoped form over a global you can't swap in tests.

```kotlin
// Preferred: Hilt-scoped, injectable, replaceable in tests
@Singleton
class AuditLogger @Inject constructor() {
    fun log(message: String) { /* ... */ }
}

// Acceptable for a truly stateless global with no dependencies
object VersionInfo {
    const val SCHEMA_VERSION = 12
}
```

### Factory

**Purpose:** Create objects without specifying exact class.

**When to use:** Object creation logic is complex, or varies by type.

> **Kotlin/Android:** Hilt often replaces manual factories entirely — bind the
> implementation with `@Binds`/`@Provides` and inject it. Keep an explicit
> factory only when the concrete type is chosen at runtime from data (as below).

```kotlin
interface Notification {
    fun send(message: String)
}

class EmailNotification @Inject constructor() : Notification { /* ... */ }
class SmsNotification @Inject constructor() : Notification { /* ... */ }
class PushNotification @Inject constructor() : Notification { /* ... */ }

enum class NotificationType { EMAIL, SMS, PUSH }

class NotificationFactory @Inject constructor(
    private val email: Provider<EmailNotification>,
    private val sms: Provider<SmsNotification>,
    private val push: Provider<PushNotification>,
) {
    fun create(type: NotificationType): Notification = when (type) {
        NotificationType.EMAIL -> email.get()
        NotificationType.SMS -> sms.get()
        NotificationType.PUSH -> push.get()
    }
}
```

### Builder

**Purpose:** Construct complex objects step by step.

**When to use:** Objects with many optional parameters, test data creation.

> **Kotlin/Android:** Named + default arguments and `data class.copy()` replace
> the Builder pattern for most cases — construct directly and override only the
> fields you need. Reserve a real builder for genuinely staged construction
> (e.g. a DSL, or where validation must run between steps).

```kotlin
// Kotlin idiom: named/default args + copy() replace most builders
data class Citizen(
    val name: String,
    val documentId: String,
    val age: Int? = null,
)

val citizen = Citizen(name = "Alice", documentId = "A-123")
val older = citizen.copy(age = 40)

// A builder still earns its keep for complex, staged construction:
class ReportBuilder {
    private val sections = mutableListOf<Section>()
    fun header(title: String) = apply { sections += Section.Header(title) }
    fun row(vararg cells: String) = apply { sections += Section.Row(cells.toList()) }
    fun build(): Report = Report(sections.toList())
}
```

### Prototype

**Purpose:** Create new objects by cloning existing ones.

**When to use:** Object creation is expensive, or you need copies with slight variations.

> **Kotlin/Android:** `data class.copy()` is the idiomatic prototype — it clones
> and lets you override selected fields in one expression. A manual `clone()` is
> only needed for classes that aren't data classes or that require deep copies.

```kotlin
data class Document(
    val title: String,
    val content: String,
    val metadata: Metadata,
) {
    // copy() gives shallow-clone-with-overrides for free:
    // val v2 = doc.copy(title = "New title")

    // Manual deep clone only when a nested field must not be shared:
    fun deepClone(): Document = copy(metadata = metadata.copy())
}
```

---

## Structural Patterns

### Adapter

**Purpose:** Make incompatible interfaces work together.

**When to use:** Integrating third-party libraries, legacy code.

> **Kotlin/Android:** A common use here is wrapping a legacy `:viuclient` (Java)
> API behind a clean domain interface so the rest of the app depends only on the
> abstraction.

```kotlin
// Legacy library with a different interface
class OldPaymentApi {
    fun makePayment(cents: Int): Boolean { /* ... */ }
}

// Our domain interface
interface PaymentGateway {
    fun charge(amount: Money): ChargeResult
}

// Adapter
class OldPaymentAdapter(
    private val oldApi: OldPaymentApi,
) : PaymentGateway {
    override fun charge(amount: Money): ChargeResult {
        val success = oldApi.makePayment(amount.toCents())
        return if (success) ChargeResult.Success else ChargeResult.Failed
    }
}
```

### Decorator

**Purpose:** Add behavior to objects dynamically.

**When to use:** Adding features without modifying existing code.

> **Kotlin/Android:** Kotlin's interface delegation (`by`) implements the
> decorator's forwarding boilerplate automatically — you only override the
> methods you want to augment.

```kotlin
interface Notifier {
    fun send(message: String)
}

class EmailNotifier : Notifier {
    override fun send(message: String) { println("Email: $message") }
}

// `by wrapped` forwards everything not overridden — no manual delegation
class SmsDecorator(private val wrapped: Notifier) : Notifier by wrapped {
    override fun send(message: String) {
        wrapped.send(message)
        println("SMS: $message")
    }
}

class SlackDecorator(private val wrapped: Notifier) : Notifier by wrapped {
    override fun send(message: String) {
        wrapped.send(message)
        println("Slack: $message")
    }
}

// Usage - compose behaviors
val notifier = SlackDecorator(SmsDecorator(EmailNotifier()))
notifier.send("Alert!") // Sends to all three
```

### Proxy

**Purpose:** Control access to an object.

**When to use:** Lazy loading, access control, logging, caching.

> **Kotlin/Android:** For pure lazy initialization, `by lazy { }` is the
> idiomatic proxy and needs no separate class. A dedicated proxy class earns its
> place when you also add access control, caching, or logging around each call.

```kotlin
interface Image {
    fun display()
}

class RealImage(private val filename: String) : Image {
    init { loadFromDisk() } // Expensive
    private fun loadFromDisk() { /* ... */ }
    override fun display() { /* ... */ }
}

// Lazy-loading proxy via `by lazy`
class ImageProxy(private val filename: String) : Image {
    private val realImage: RealImage by lazy { RealImage(filename) }
    override fun display() = realImage.display()
}
```

### Composite

**Purpose:** Treat individual objects and compositions uniformly.

**When to use:** Tree structures, hierarchies (files/folders, UI components).

> **Kotlin/Android:** A `sealed interface` models the leaf/branch split cleanly
> and gives exhaustive `when` handling.

```kotlin
sealed interface Component {
    fun price(): Int
}

class Product(private val price: Int) : Component {
    override fun price() = price
}

class Box : Component {
    private val children = mutableListOf<Component>()
    fun add(component: Component) { children += component }
    override fun price() = children.sumOf { it.price() }
}

// Usage
val smallBox = Box().apply {
    add(Product(10))
    add(Product(20))
}
val bigBox = Box().apply {
    add(smallBox)
    add(Product(50))
}
println(bigBox.price()) // 80
```

---

## Behavioral Patterns

### Strategy

**Purpose:** Define a family of algorithms, make them interchangeable.

**When to use:** Multiple ways to do something, switchable at runtime.

> **Kotlin/Android:** This is exactly the Dependency Inversion Principle as
> practiced in this project — depend on an interface, inject the concrete
> strategy with Hilt. Constructor injection *is* the strategy hand-off.

```kotlin
interface PricingStrategy {
    fun calculate(basePrice: Int): Int
}

class RegularPricing @Inject constructor() : PricingStrategy {
    override fun calculate(basePrice: Int) = basePrice
}

class PremiumDiscount @Inject constructor() : PricingStrategy {
    override fun calculate(basePrice: Int) = (basePrice * 0.8).toInt() // 20% off
}

class BlackFriday @Inject constructor() : PricingStrategy {
    override fun calculate(basePrice: Int) = basePrice / 2 // 50% off
}

// Injected strategy — chosen at the Hilt binding, swappable in tests
class ShoppingCart @Inject constructor(
    private val pricing: PricingStrategy,
) {
    fun calculateTotal(items: List<Item>): Int =
        pricing.calculate(items.sumOf { it.price })
}
```

### Observer

**Purpose:** Notify multiple objects about state changes.

**When to use:** Event systems, pub/sub, reactive updates.

> **Kotlin/Android:** `LiveData` and `Flow` are the idiomatic observers on
> Android — a `ViewModel` exposes `LiveData` and the Fragment/Activity observes
> it, respecting lifecycle automatically. A hand-rolled emitter (below) is only
> for cases outside the Android lifecycle.

```kotlin
// Idiomatic: ViewModel exposes observable state, the View observes it
@HiltViewModel
class OrderViewModel @Inject constructor(
    private val placeOrder: PlaceOrderUseCase,
) : ViewModel() {
    private val _events = MutableLiveData<OrderEvent>()
    val events: LiveData<OrderEvent> = _events.asLiveData()

    fun submit(order: Order) = viewModelScope.launch {
        placeOrder(order)
        _events.value = OrderEvent.Placed(order)
    }
}

// Fragment side:
// viewModel.events.observe(viewLifecycleOwner) { event -> /* react */ }

// A manual emitter only for non-lifecycle contexts:
class EventEmitter {
    private val observers = mutableListOf<(Event) -> Unit>()
    fun subscribe(observer: (Event) -> Unit) { observers += observer }
    fun notify(event: Event) = observers.forEach { it(event) }
}
```

### Template Method

**Purpose:** Define algorithm skeleton, let subclasses override steps.

**When to use:** Common algorithm with varying steps.

> **Kotlin/Android:** The project's base `UseCase` class is a real Template
> Method — the base defines the public entry point (e.g. `invoke`) and the
> common validation/error handling, and each concrete use case implements the
> varying step (`run`). Use an `abstract class` with `abstract`/`open` steps.

```kotlin
abstract class DataExporter {
    // Template method - defines the algorithm skeleton
    fun export(data: List<Data>) {
        validate(data)
        val formatted = format(data)
        write(formatted)
        notifyDone()
    }

    // Common steps
    private fun validate(data: List<Data>) { /* ... */ }
    private fun notifyDone() { /* ... */ }

    // Steps to override
    protected abstract fun format(data: List<Data>): String
    protected abstract fun write(content: String)
}

class CsvExporter : DataExporter() {
    override fun format(data: List<Data>) = data.joinToString("\n") { it.toCsv() }
    override fun write(content: String) = File("export.csv").writeText(content)
}

class JsonExporter : DataExporter() {
    override fun format(data: List<Data>) = Json.encodeToString(data)
    override fun write(content: String) = File("export.json").writeText(content)
}
```

### Command

**Purpose:** Encapsulate a request as an object.

**When to use:** Undo/redo, queuing, logging actions.

> **Kotlin/Android:** A UseCase is itself an encapsulated action — an invocable
> object. For undoable actions, an `interface` (or `sealed interface` of
> commands) with `execute`/`undo` keeps each action self-contained.

```kotlin
interface Command {
    fun execute()
    fun undo()
}

class AddItemCommand(
    private val cart: Cart,
    private val item: Item,
) : Command {
    override fun execute() = cart.add(item)
    override fun undo() = cart.remove(item)
}

class CommandHistory {
    private val history = ArrayDeque<Command>()

    fun execute(command: Command) {
        command.execute()
        history.addLast(command)
    }

    fun undo() {
        history.removeLastOrNull()?.undo()
    }
}
```

---

## Pattern Awareness

### The Four-Dimensional Lens

When analyzing new code/libraries, ask:

1. **What problem does it solve?** (Creational, Structural, Behavioral)
2. **What scope?** (Object-level, Class-level, System-level)
3. **When is it applied?** (Compile-time, Runtime)
4. **How coupled?** (Tight, Loose)

This helps recognize patterns even in unfamiliar code.

---

## Anti-Patterns to Avoid

| Anti-Pattern | Problem | Solution |
|--------------|---------|----------|
| **God Object** | Class does everything | Split by responsibility |
| **Spaghetti Code** | Tangled, no structure | Refactor to layers |
| **Golden Hammer** | Using one pattern for everything | Match pattern to problem |
| **Premature Optimization** | Optimizing before needed | YAGNI, profile first |
| **Copy-Paste Programming** | Duplication | Extract, Rule of Three |
