---
name: commit
description: This command helps you create well-formatted commits with conventional commit messages, following this project's Kotlin Multiplatform conventions.
---

## Usage

```
/commit               # stage, build the message, and run the commit
/commit --stage-only  # stage normally, build the message, but PRINT the commit command instead of executing it
```

`--stage-only` is meant for environments where executing `git commit` is problematic — most notably a headless terminal where a GPG passphrase prompt for a signed commit corrupts the CLI input. In that mode the skill stages files and builds the message exactly as normal, then prints a ready-to-paste commit command instead of running it. `git add` is still executed by the agent; only `git commit` is held back.

## What This Command Does

1. Runs `git status` to see which files are staged.
2. If **no files are staged**, ask the user which files to stage — **never use `git add -A` or `git add .`**. Stage files explicitly by name.
   - `build/`, `.gradle/`, `.kotlin/` are build output/cache and are already gitignored — if any of them show up in `git status`, that's a sign something's misconfigured; flag it rather than staging it.
   - `.serena/cache/` is also gitignored (local LSP index); `.serena/memories/` and `.serena/project.yml` ARE meant to be committed — see root `CLAUDE.md`.
3. Runs `git diff --cached` to review the staged diff before committing.
4. Analyzes the diff to determine if multiple distinct logical changes are present.
5. If multiple distinct changes are detected, suggest breaking into separate commits and help the user stage/commit each group individually.
6. For each commit, constructs a message in **conventional commit** format (see below).
7. **Never** appends a `Co-Authored-By` trailer (or any other trailer) to the commit message.
8. **Final step depends on the mode:**
   - **Default:** run the commit (e.g. `git commit -m "..."`).
   - **`--stage-only`:** do **not** run `git commit`. Instead, print the exact command — including the full message — in a fenced code block for the user to copy and run manually. This avoids triggering an interactive GPG passphrase prompt inside the agent's shell.

### Split commits in `--stage-only` mode

When the diff is split into multiple logical commits, handle each group in sequence and **wait for user confirmation before moving to the next group**:

1. Stage the files for group N.
2. Print the commit command for group N (see heredoc format below).
3. Ask the user to run the command and confirm when done.
4. Only after confirmation, proceed to stage the files for group N+1.

This ensures each commit is in place before the next group is staged.

### Printing a multi-line message (`--stage-only`)

Because the commit body has a blank line and bullets, use a heredoc so it pastes as one command in a Linux/macOS shell:

```
git commit -F - <<'EOF'
<type>: <imperative description>

- bullet: what changed
- bullet: why (when non-obvious)
EOF
```

## Note on style checks

This repo has **no automated formatter/linter configured** (no detekt, no ktlint, no `.editorconfig` — confirmed in root `CLAUDE.md` and `.serena/memories/conventions.md`). There is nothing for `/commit` to run or duplicate — style is convention-only; match the surrounding code.

## Commit Message Format

```
<type>: <imperative description ≤72 chars>

- bullet: what changed
- bullet: why (when non-obvious)
```

## Conventional Commit Types

| Type | When to use |
|------|-------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation changes |
| `style` | Formatting/style (no logic change) |
| `refactor` | Code restructuring without behavior change |
| `perf` | Performance improvements |
| `chore` | Tooling, configuration, build changes |
| `ci` | CI/CD improvements |
| `revert` | Reverting changes |
| `build` | Gradle/build-script changes (`build.gradle.kts`, `libs.versions.toml`, `settings.gradle.kts`) |
| `wip` | Work in progress |

Note: this repo has **no automated test source sets** (per `CLAUDE.md` and `.serena/memories/task_completion.md`), so `test` as a commit type won't apply in practice — don't invent test-related commits unless a test module is genuinely being added as a deliberate, separately-discussed change.

## Best Practices

- **Atomic commits**: Each commit should contain related changes serving a single purpose.
- **Split large changes**: If changes touch multiple concerns, split them into separate commits.
- **Present tense, imperative mood**: "add feature" not "added feature".
- **Concise first line**: ≤72 characters.
- **Stage explicitly**: Always `git add <file>` by name — never `git add -A` or `git add .`.
- **Verify diff**: Always review `git diff --cached` before committing.

## Guidelines for Splitting Commits

Split when changes involve:
1. **Different concerns** — e.g. a change to one feature (`features/home`) mixed with an unrelated change to another (`features/add_city`)
2. **Mixed change types** — features mixed with fixes or refactors
3. **Different file patterns** — Kotlin source vs. Compose resources (`.xml` strings) vs. Gradle/version-catalog files vs. iOS `.strings`/Swift
4. **Logical groupings** — changes clearer to review separately
5. **Large diffs** — very large changes that benefit from breakdown

## Forbidden Operations

Never use these without explicit user approval:
- `git push --force` / `git push -f`
- `git reset --hard`
- `git commit --amend` on already-pushed commits
- `git rebase -i` without user instruction
- `--no-verify` to skip hooks

Also never add a `Co-Authored-By` trailer to commit messages.

## Examples

Good commit messages for this project:
- feat: add rain radar layer toggle to the map screen
- fix: correct UV index rounding in the weather suggestion string
- fix: keep loading state on screen while forecast refreshes
- build: bump Compose Multiplatform to 1.10.3
- refactor: extract weather notification scheduling into its own class
- docs: document the WeatherAPI key setup in README
- chore: add Serena MCP config and spec-driven workflow skills
