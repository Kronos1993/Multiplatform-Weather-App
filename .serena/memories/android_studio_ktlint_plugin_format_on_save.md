# Android Studio's ktlint IDE plugin can auto-format open files

- Separate from the Gradle `org.jlleitschuh.gradle.ktlint` plugin: the
  ktlint IntelliJ/Android Studio plugin (editor-only, per-developer,
  not part of the repo) can format-on-save any `.kt` file open in the
  IDE — including files unrelated to whatever Gradle task is currently
  running.
- Observed 2026-09-09: several `androidMain`/`commonMain` files picked
  up ktlint-style formatting changes (trailing commas, blank-line
  cleanup) with no corresponding Gradle `ktlintFormat` command run
  against them.
- Before staging/committing after any session involving ktlint-related
  Gradle tasks, review the full `git diff` — don't assume every changed
  file traces back to a command you ran. Verify unexpected diffs are
  style-only (no logic change) before including them.
