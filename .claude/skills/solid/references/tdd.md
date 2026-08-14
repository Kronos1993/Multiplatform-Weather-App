# Test-Driven Development

> **Kotlin/Android:** TDD is a strongly-encouraged discipline here, not an
> absolute law. It pays off most in the pure `*-domain` layer — UseCases,
> value objects, and mappers — where logic is deterministic and fakes are
> cheap. Android UI, framework, and ORM-Lite code is often verified by
> instrumentation or manual testing rather than unit-test-first. Apply the
> loop below where it earns its keep; don't force it onto glue code.

## The Core Loop

```
RED → GREEN → REFACTOR → RED → ...
```

### RED Phase
Write a failing test that describes the behavior you want. The test should:
- Use domain language, not technical jargon
- Describe WHAT, not HOW
- Be a concrete example, not an abstract statement

```kotlin
// BAD: Abstract
@Test
fun `can add numbers`() { ... }

// GOOD: Concrete example
@Test
fun `when adding 2 + 3, returns 5`() { ... }
```

### GREEN Phase
Write the **simplest possible code** to make the test pass. Two strategies:

1. **Fake It** - Return a hardcoded value
   ```kotlin
   fun add(a: Int, b: Int): Int {
       return 5 // Simplest thing!
   }
   ```

2. **Obvious Implementation** - If you know the solution
   ```kotlin
   fun add(a: Int, b: Int): Int {
       return a + b
   }
   ```

**Prefer Fake It** when learning or unsure. Let more tests drive the real implementation.

### REFACTOR Phase
This is where **design happens**. Look for:
- Duplication (but wait for Rule of Three)
- Long methods to extract
- Poor names to improve
- Complex conditions to simplify

## The Three Laws of TDD

Treat these as aspirational discipline for domain logic, not a gate on every commit:

1. **No production code** without a failing test
2. **No more test code** than sufficient to fail (compilation failures count)
3. **No more production code** than sufficient to pass the one failing test

## The Rule of Three

**Only extract duplication when you see it THREE times.**

Why? Wrong abstractions are worse than duplication. Wait for the pattern to emerge.

```kotlin
// Duplication #1 - Leave it
// Duplication #2 - Note it, leave it
// Duplication #3 - NOW extract it
```

## Triangulation

Each new test "sculpts" the solution toward a general, robust implementation.

Think of **degrees of freedom** - like a car that needs forward/back, left/right, and rotation. Each test carves out one degree of freedom until the implementation handles all cases.

## Transformation Priority Premise

When going from RED to GREEN, prefer simpler transformations:

| Priority | Transformation |
|----------|----------------|
| 1 | {} → null |
| 2 | null → constant |
| 3 | constant → variable |
| 4 | unconditional → conditional |
| 5 | scalar → collection |
| 6 | statement → recursion |
| 7 | value → mutated value |

Higher priority = simpler. Avoid jumping to complex transformations too early.

## Arrange-Act-Assert

Structure every test:

```kotlin
@Test
fun `calculates total with discount`() {
    // ARRANGE - Set up the world
    val order = Order()
    order.addItem(Item(price = 100))
    val discount = PercentDiscount(10)

    // ACT - Execute the behavior
    val total = order.calculateTotal(discount)

    // ASSERT - Verify the outcome (expected value FIRST)
    assertEquals(90, total)
}
```

> **Kotlin/Android:** UseCases return `Result<T>`. Assert success and the
> unwrapped value, and call suspend functions inside `runTest { ... }`:
>
> ```kotlin
> @Test
> fun `returns citizen for known id`() = runTest {
>     // ARRANGE
>     val citizen = CitizensModel(citizenId = 42L, name = "Ada")
>     val useCase = GetCitizenUseCaseImpl(fakeCitizensRepository(citizen))
>
>     // ACT
>     val result = useCase.execute(citizenId = 42L)
>
>     // ASSERT
>     assertTrue(result.isSuccess)
>     assertEquals(citizen, result.getOrNull())
> }
> ```

## Writing Tests Backwards

Sometimes it helps to write AAA in reverse:
1. Write the ASSERT first - what do you want to verify?
2. Write the ACT - what action produces that result?
3. Write the ARRANGE - what setup is needed?

## Test Naming Principles

- Use **behavior-driven names** with domain language
- Provide **concrete examples**, not abstract statements
- **One example per test** for easy debugging
- Avoid leaking implementation details

```kotlin
// BAD: Technical, implementation-focused
@Test
fun `should set the data property to 1`() { ... }

// GOOD: Behavior-focused, domain language (backtick names read cleanly)
@Test
fun `recognizes "mom" as a palindrome`() { ... }
```

> **Kotlin/Android:** Backtick method names are the project standard for
> tests. Do not use `describe`/`it` nesting — that is a Jest idiom. One
> `@Test` function per concrete example.

## Classic vs Mockist TDD

**Classic (Detroit/Chicago) TDD:**
- Test with real dependencies (or lightweight fakes)
- Higher confidence, slower tests
- Best for: Pure functions, value objects, integration tests

**Mockist (London) TDD:**
- Substitute external dependencies with test doubles
- Faster tests, more isolated
- Best for: UseCases with repository/data-source dependencies

Start with Classic TDD to learn the technique. Introduce fakes when testing code that reaches a repository, data source, or database.

> **Kotlin/Android:** This project has **no mocking DSL** (no MockK). The
> "mockist" style here means hand-written **fakes** — an object expression
> implementing the collaborator's interface. See `testing.md` (Test Doubles)
> for the full pattern.

## Common Mistakes

1. **Writing code before tests** - Violates the fundamental principle
2. **Writing too much test** - Just enough to fail
3. **Writing too much code** - Just enough to pass
4. **Skipping refactor** - This is where design lives
5. **Testing implementation** - Test behavior, not how it's done
6. **Abstract test names** - Use concrete examples
7. **Extracting too early** - Wait for Rule of Three
