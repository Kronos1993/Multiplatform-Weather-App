# ktlint: scope ktlintFormat to the current diff, not the whole module

`./gradlew :composeApp:ktlintCheck` currently reports a large pre-existing violation backlog
(`composeApp/build.gradle.kts`'s `ktlint { ignoreFailures = true }` bootstrap accommodation —
see `mem:conventions`). Running the Gradle `ktlintFormat` task reformats the **entire module**,
dragging hundreds of unrelated pre-existing violations into an otherwise-focused diff.

Fix: use the standalone `ktlint` CLI (`brew install ktlint`, separate from the Gradle plugin —
check with `which ktlint`) scoped to exactly the files a change touched or created:

```
ktlint -F <file1> <file2> ...
```

This respects the repo's root `.editorconfig` (same rules the Gradle plugin uses) and
auto-fixes mechanical issues (trailing commas, indentation, whitespace) only in those files,
leaving the rest of the backlog untouched. After formatting, re-run plain `ktlint <files...>`
(no `-F`) to confirm — anything left is either genuinely un-autofixable in this file (naming,
max-line-length) or pre-existing content outside what was actually touched; verify by reading
the flagged line before assuming it's new debt.

Use this whenever a spec/task touches Kotlin files and needs "new/touched code passes
ktlintCheck clean" (CLAUDE.md's Definition of Done) without also taking on the unrelated
cleanup spec tracked for the backlog.
