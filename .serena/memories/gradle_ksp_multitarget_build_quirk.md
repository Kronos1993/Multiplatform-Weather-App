# Gradle/KSP Multi-Target Build Quirk

`./gradlew build` (the full aggregate task — Android debug+release, JVM,
iOS arm64+simulatorArm64, scheduled together with parallel execution
where possible) can fail with "Task failed with an exception" errors like:

```
Gradle detected a problem with the following location:
'.../composeApp/build/generated/ksp/metadata'
Reason: Task ':composeApp:compileKotlinIosSimulatorArm64' uses this
output of task ':composeApp:kspCommonMainKotlinMetadata' without
declaring an explicit or implicit dependency.
```

This is a **real, pre-existing defect** in this repo's Gradle
configuration (confirmed via a `git stash`-to-clean-`develop` baseline
check) — several KSP/compile tasks across different targets
(`:composeApp:compileKotlinIosSimulatorArm64`,
`:composeApp:compileDebugKotlinAndroid`, `:composeApp:kspDebugKotlinAndroid`,
`:composeApp:kspKotlinIosSimulatorArm64`, etc.) consume
`:composeApp:kspCommonMainKotlinMetadata`'s generated output without a
declared task dependency, and Gradle's parallel-execution validation
catches the race. It is **not** caused by any particular source change.

**What actually matters for local verification is unaffected**: single-
target builds are green —
`./gradlew :composeApp:assembleDebug` (Android) and a full `xcodebuild
-scheme iosApp -destination 'platform=iOS Simulator,...' build` (iOS,
which invokes Gradle for just the `iosMain` klib as a build phase) both
succeed cleanly. This matches root `CLAUDE.md`'s actual Definition of
Done (`:composeApp:assembleDebug` + the iOS Xcode build), which never
asked for the aggregate `./gradlew build` to be the local gate.

**Separately**: after removing/restructuring a Kotlin source file, a
*stale* KSP-generated-code symptom can also show up (Room
"Redeclaration"/"has no corresponding expected declaration" errors
around `ApplicationDatabaseConstructor`/`*_Impl` classes) even on a
single-target build — this is incremental-build cache staleness, not the
same defect above, and is fixed by `./gradlew clean` before rebuilding
(don't assume a clean is unnecessary just because an earlier build in the
same session succeeded).

Not fixed as part of any single spec — it's a standalone Gradle/KSP task-
graph wiring issue, out of scope for feature work. Worth a dedicated fix
if a fully green aggregate `./gradlew build` is ever needed (e.g. for CI).
