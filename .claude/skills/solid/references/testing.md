# Testing Strategy

## The Testing Pyramid

```
       /\
      /  \        E2E Tests (Few)
     /----\       - Full system
    /      \      - Slow, brittle
   /--------\
  /          \    Integration Tests (Some)
 /------------\   - Multiple components
/              \  - Medium speed
----------------
      Unit Tests (Many)
      - Single unit
      - Fast, isolated
```

## Test Types

### Unit Tests

Test ONE class or function in isolation.

**Characteristics:**
- Fast (milliseconds)
- No external dependencies (faked)
- Most of your tests should be unit tests

```kotlin
class VoterTurnoutTest {
    @Test
    fun `sums authenticated voters across stations`() {
        val turnout = VoterTurnout()
        turnout.addStation(count = 100)
        turnout.addStation(count = 50)

        assertEquals(150, turnout.total())
    }
}
```

> **Kotlin/Android:** Best home for a value object or a pure UseCase.
> `import org.junit.Test`, `org.junit.Assert.*`; expected value comes first
> in `assertEquals(expected, actual)`.

### Integration Tests

Test multiple components together.

**Characteristics:**
- Slower (may use a real DB)
- Test boundaries between components
- Fewer than unit tests

```kotlin
class CitizensRepositoryIntegrationTest {
    private lateinit var repository: CitizensRepositoryImpl

    @Before
    fun setUp() {
        val dataSource = CitizensDataSourceImpl(testDao)
        repository = CitizensRepositoryImpl(dataSource)
    }

    @Test
    fun `saves and retrieves a citizen`() = runBlocking {
        val citizen = CitizensModel(citizenId = 123L, name = "Ada")
        repository.save(citizen)

        val result = repository.getByCitizenId(123L)

        assertTrue(result.isSuccess)
        assertEquals(citizen, result.getOrNull())
    }
}
```

> **Kotlin/Android:** A `RepositoryImpl` backed by a real `DataSourceImpl`
> (ORM-Lite) is the canonical integration test. These usually live as
> instrumented tests when they touch a real database.

### E2E / Acceptance Tests

Test the entire system from the user's perspective.

**Characteristics:**
- Slowest
- Most brittle (many moving parts)
- Test critical paths only

> **Kotlin/Android:** The app leans on `LiveData` + manual and instrumented
> testing for full-flow verification. Keep E2E coverage to critical polling
> flows (open event → authenticate voter → close), driven by
> Espresso-style instrumented tests. Prefer to push logic down into UseCases
> so most behavior is covered by fast unit tests instead.

---

## Arrange-Act-Assert (AAA)

Structure EVERY test this way:

```kotlin
@Test
fun `applies discount to premium users`() {
    // ARRANGE - Set up the test world
    val user = User(isPremium = true)
    val cart = Cart(user)
    cart.addItem(Item(price = 100))

    // ACT - Execute the behavior under test
    val total = cart.calculateTotal()

    // ASSERT - Verify the expected outcome (expected FIRST)
    assertEquals(80, total) // 20% discount
}
```

### Writing AAA Backwards

Sometimes easier to write in reverse:

1. **Assert first** - What do you want to verify?
2. **Act** - What action produces that result?
3. **Arrange** - What setup is needed?

---

## Test Naming

### Bad: Abstract, Technical

```kotlin
fun `should work correctly`()
fun `handles the edge case`()
fun `sets the data property`()
```

### Good: Concrete Examples, Domain Language

```kotlin
fun `calculates 20% discount for premium users`()
fun `returns error when cart is empty`()
fun `recognizes "racecar" as a palindrome`()
```

### Format

Backtick names carry the whole scenario — no nesting blocks:

```kotlin
// Option 1: should + behavior
fun `should apply tax based on shipping state`()

// Option 2: when + then
fun `when adding 2 + 3, then returns 5`()

// Option 3: given/when/then folded into one name (complex scenarios)
fun `given a premium user, when they checkout, then they receive 20% discount`()
```

> **Kotlin/Android:** No `describe`/`it` blocks. Group related cases by
> putting them in the same test class; each case is one backticked `@Test`.

---

## Test Doubles

> **Kotlin/Android:** No mocking library (MockK/Mockito) is currently used
> anywhere in this repo's tests, and real unit-test coverage is sparse — so
> there is no deeply entrenched convention, just a sensible default: build
> doubles as hand-written **object expressions implementing the interface**
> (Fakes) to keep tests dependency-light, and follow the nearest existing test
> in the module. A "spy" is a fake that records calls into a plain list you
> assert on — no verification DSL needed.

### Dummy

Object passed but never used.

```kotlin
val dummyLogger = object : Logger {
    override fun log(message: String) { /* never called */ }
}
CitizenService(realRepo, dummyLogger)
```

### Stub

Returns canned `Result` values.

```kotlin
private fun stubCitizensRepository(citizen: CitizensModel) =
    object : CitizensRepository {
        override suspend fun getByCitizenId(citizenId: Long) =
            Result.success(citizen)

        override suspend fun save(citizen: CitizensModel) =
            Result.success(Unit)
    }
```

### Spy

A fake that records how it was called.

```kotlin
class NotificationSpy : Notifier {
    val notified = mutableListOf<String>()

    override fun notify(recipient: String, message: String) {
        notified.add(recipient)
    }
}

// Later
val spy = NotificationSpy()
// ...
assertTrue(spy.notified.contains("voter@example.com"))
```

### Mock

Verifies expected interactions — expressed as a spy plus an assertion (no DSL).

```kotlin
class SaveSpy : CitizensRepository {
    var saved: CitizensModel? = null

    override suspend fun save(citizen: CitizensModel): Result<Unit> {
        saved = citizen
        return Result.success(Unit)
    }

    override suspend fun getByCitizenId(citizenId: Long) =
        Result.success(CitizensModel(citizenId, "Test"))
}

// After the act step
assertEquals(expectedCitizen, saveSpy.saved)
```

### Fake

Working in-memory implementation (simplified). **This is the project default.**

```kotlin
class InMemoryCitizensRepository : CitizensRepository {
    private val citizens = mutableMapOf<Long, CitizensModel>()

    override suspend fun save(citizen: CitizensModel): Result<Unit> {
        citizens[citizen.citizenId] = citizen
        return Result.success(Unit)
    }

    override suspend fun getByCitizenId(citizenId: Long): Result<CitizensModel> {
        val found = citizens[citizenId]
        return if (found != null) Result.success(found)
        else Result.failure(NoSuchElementException())
    }
}
```

---

## Testing Strategies by Layer

### Domain Layer (Most Tests)

- Unit tests with **no fakes** — pure logic
- Test business rules, value objects, mappers
- Fast, comprehensive

```kotlin
class MoneyTest {
    @Test
    fun `adds amounts with same currency`() {
        val a = Money.dollars(10)
        val b = Money.dollars(20)
        assertEquals(Money.dollars(30), a.add(b))
    }

    @Test
    fun `throws when adding different currencies`() {
        val usd = Money.dollars(10)
        val eur = Money.euros(10)
        assertThrows(CurrencyMismatch::class.java) {
            usd.add(eur)
        }
    }
}
```

> **Kotlin/Android:** For exception assertions use `assertThrows`
> (JUnit 4.13+) or `@Test(expected = CurrencyMismatch::class)`.

### Application / UseCase Layer

- Tests with **faked repositories** (object expressions)
- Test use case orchestration; assert on the returned `Result`

```kotlin
class RegisterVoterUseCaseTest {
    @Test
    fun `saves voter and returns success`() = runTest {
        val repo = InMemoryCitizensRepository()
        val useCase = RegisterVoterUseCaseImpl(repo)

        val result = useCase.execute(CitizensModel(123L, "Ada"))

        assertTrue(result.isSuccess)
        assertEquals(CitizensModel(123L, "Ada"), repo.getByCitizenId(123L).getOrNull())
    }
}
```

### Infrastructure / DataSource Layer

- Integration tests with real dependencies
- Test the ORM-Lite data source and its queries

```kotlin
class CitizensDataSourceImplTest {
    private lateinit var dataSource: CitizensDataSourceImpl

    @Before
    fun setUp() {
        dataSource = CitizensDataSourceImpl(testDao)
    }

    @Test
    fun `persists and retrieves citizen`() = runBlocking {
        val citizen = CitizensModel(123L, "Ada")
        dataSource.save(citizen)

        val found = dataSource.getByCitizenId(123L)

        assertEquals(citizen, found)
    }
}
```

---

## High-Value Integration Tests

Focus integration tests on:

1. **Boundaries** - Where systems meet (Repository ↔ DataSource)
2. **Critical paths** - Authentication, security, core polling features
3. **Complex queries** - Database operations

### Contract Tests

Verify every implementation of an interface behaves the same. Write one
shared function and run it against the fake **and** the real impl.

```kotlin
// Shared contract test
suspend fun assertCitizensRepositoryContract(repo: CitizensRepository) {
    // saves and retrieves a citizen
    val citizen = CitizensModel(123L, "Test")
    repo.save(citizen)
    val found = repo.getByCitizenId(123L)
    assertTrue(found.isSuccess)
    assertEquals(citizen, found.getOrNull())

    // returns failure for a missing citizen
    val missing = repo.getByCitizenId(999L)
    assertTrue(missing.isFailure)
}

// Apply to all implementations
class CitizensRepositoryContractTest {
    @Test
    fun `in-memory fake honors the contract`() = runBlocking {
        assertCitizensRepositoryContract(InMemoryCitizensRepository())
    }

    @Test
    fun `ORM-Lite impl honors the contract`() = runBlocking {
        assertCitizensRepositoryContract(CitizensRepositoryImpl(CitizensDataSourceImpl(testDao)))
    }
}
```

---

## Test Builders

Create test objects easily.

> **Kotlin/Android:** In Kotlin a `data class` plus `.copy()` and default
> constructor arguments usually replace the builder pattern — start there:
>
> ```kotlin
> val base = CitizensModel(citizenId = 1L, name = "Base", status = PENDING)
> val paid = base.copy(status = AUTHENTICATED)
> ```
>
> Keep a small builder only when construction is genuinely complex:

```kotlin
class CitizenBuilder {
    private var citizenId: Long = 1L
    private var name: String = "Test"
    private var status: Status = Status.PENDING

    fun withId(id: Long) = apply { citizenId = id }
    fun authenticated() = apply { status = Status.AUTHENTICATED }

    fun build() = CitizensModel(citizenId, name, status)
}

// Usage
val citizen = CitizenBuilder()
    .withId(42L)
    .authenticated()
    .build()
```

---

## Common Testing Mistakes

| Mistake | Problem | Solution |
|---------|---------|----------|
| Testing implementation | Brittle tests | Test behavior only |
| Too many fakes | Tests prove nothing | Use real objects when possible |
| Shared state | Flaky tests | Isolate each test (`@Before` resets) |
| No assertions | False confidence | Always assert something meaningful |
| Testing trivial code | Wasted effort | Focus on logic and edge cases |
| Slow tests | Reduced feedback | Optimize, use unit tests |
