# ktlint excludes KSP/Room-generated sources

- `composeApp/build.gradle.kts` adds `build/generated/ksp/metadata` to
  `commonMain`'s `kotlin.srcDir(...)` (for Room/KSP). Without an
  explicit exclude, ktlint lints this generated code (Room DAO impls,
  `ApplicationDatabase_Impl`, etc.) as if it were project source —
  violations reappear on every regeneration.
- Mitigated via a `filter { exclude { ... } }` block inside the
  `ktlint {}` extension in `composeApp/build.gradle.kts`, excluding any
  path containing a `generated` path segment. Keep this filter if the
  KSP/Room setup changes — don't let it silently disappear.
