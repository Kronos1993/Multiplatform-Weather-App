# ktlint: use --continue when auditing compliance

- `./gradlew :composeApp:ktlintCheck` without `--continue` can fail-fast:
  once one source-set check task fails, Gradle stops scheduling
  independent tasks, so later ones (e.g. `ktlintAndroidMainSourceSetCheck`,
  `ktlintCommonMainSourceSetCheck`) never run to completion.
- Their absence from the failure list does NOT mean they passed — it
  means they never finished. Misreading this understated a real
  violation count by ~10x in one audit (2026-09-09, `adopt-ktlint` spec).
- Always run `./gradlew :composeApp:ktlintCheck --continue` (optionally
  `--rerun-tasks` to bypass caching) when measuring real ktlint
  compliance across this KMP module's source sets.
