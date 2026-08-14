# Task Completion Criteria

- No automated test suite exists in this repo (no test source sets) — never claim "tests pass";
  there is nothing to run.
- No lint/formatter task is configured — do not invent a `ktlintCheck`/`detekt` step.
- Practical "done" bar for a change: it compiles.
  - Android: `./gradlew :composeApp:assembleDebug`
  - Full multiplatform: `./gradlew build`
  - iOS-only changes: the `iosApp` Xcode project must build (see `mem:suggested_commands`).
