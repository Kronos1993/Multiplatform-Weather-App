# External skills referenced by the spec workflow

The spec-* skills depend on two external skills: `/commit` and
`/create-pr` (plus the general-purpose `/loop`, not specific to this
repo). Each is invoked from one or more spec skills with assumptions
about inputs, outputs, side effects, and failure shape. This file
documents those contracts so that:

- Anyone changing `/commit` or `/create-pr` in this repo can see what
  the spec chain expects.
- Anyone reading a spec skill can find the contract for an external
  invocation it makes.

Adapted from a Smartmatic-internal `.specs/` workflow lineage. That
lineage's repos are GitLab-hosted and use `glab`; this repo is
GitHub-hosted, so `/create-mr` becomes `/create-pr` using `gh`
throughout.

This repo has **no `/build` skill** — build directly via the Gradle
commands in `.specs/config.json` `verification.build_commands` (there
is only one Gradle module, `:composeApp`, so there is no per-product
matrix to wrap).

---

## `/commit`

**Purpose:** Stage-and-commit step. Produces a conventional commit on
the current branch (see `.claude/skills/commit/SKILL.md`).

**Called by:**

- `/spec-handoff` — the local commit that finalizes an implemented spec.
- `/spec-ship` — the local commit on the impl branch before opening
  the impl PR.

**Inputs:**

- Files must be pre-staged by the caller (`git add <file>` per file —
  this repo's `/commit` also never uses `git add -A`).
- No CLI args required (default mode). `--stage-only` exists for
  headless/GPG-prompt-hostile environments but spec callers should use
  the default mode.

**Outputs:**

- One new commit on the current branch. Callers can read
  `git rev-parse HEAD` after invocation to capture the SHA.

**Side effects:**

- Runs `git commit`.

**Failure modes:**

- Never `--no-verify` to skip hooks (this repo's `/commit` forbids it).

**Config keys read:** None. The skill embeds its own conventional-
commit type mapping.

**Compatibility note:** Spec callers will break if `/commit`:

- Requires unstaged input (current contract: caller stages files).
- Stops producing a plain conventional-commit message.

---

## `/create-pr`

**Purpose:** Push current branch and open a pull request on GitHub via
`gh pr create` (see `.claude/skills/create-pr/SKILL.md`).

**Called by:**

- `/spec-finalize` first-run (PR-open mode) — opens the impl PR.
- `/spec-finalize` first-run — opens the memory PR on
  `chore/memories-<KEY>` when reusable-knowledge candidates are saved,
  with `--reviewer` if `reviewers.default` is non-empty.
- `/spec-finalize` status-check mode — opens the archive PR
  (mandatory) on `chore/archive-<KEY>` after merge, with `--reviewer`
  if configured.
- `/spec-ship` — opens the impl PR (one-shot flow).
- `/spec-ship` — opens the memory PR when reusable-knowledge candidates
  are saved (not suppressed by `--no-archive-pr`), with `--reviewer`
  if configured.
- `/spec-ship` — optionally opens the archive PR (skipped under
  `--no-archive-pr`), with `--reviewer` if configured.

**Inputs (one of three content modes):**

- `--title "<text>" --body "<text>"` — explicit content (Tier A).
- `--fill` — title/body taken from the last commit (Tier B).
- `--interactive` — prompts the user inline (Tier C).

**Optional modifiers:**

- `--target <branch>` — override the configured default target
  (`develop` per `.specs/config.json` `pr.default_target` —
  **unconfirmed**, see that key's `_doc`).
- `--reviewer <user>` — set the GitHub Reviewer field via `gh`'s
  `--reviewer` flag (a GitHub username). Optional; when
  `reviewers.default` is empty, callers omit this flag entirely.

**Outputs:**

- `pr_number` (integer) and `pr_url` (string) returned to the caller
  in a parseable form. Spec callers write both into `handoff.json`.

**Side effects:**

- `git push -u origin <branch>`.
- Calls `gh pr create` against `github.com/Kronos1993/Multiplatform-Weather-App`.

**Failure modes:**

- `gh` not authenticated → `gh auth status` fails.
- Push rejected (non-fast-forward, branch protection) → exits non-zero.
- Network/host unreachable → exits non-zero.

**Config keys read** (all under `.specs/config.json` `pr.*`):
`pr.cli`, `pr.host`, `pr.repo_slug`, `pr.default_target`,
`pr.first_push_strict_host_checking`.

**Compatibility note:** Spec callers will break if `/create-pr`:

- Changes the three content modes.
- Drops the `--reviewer <user>` passthrough.
- Stops returning `pr_number` and `pr_url` in a parseable form.

---

## `/loop`

**Purpose:** Re-run a skill on a recurring interval. Used to poll
external state (e.g. PR merge status). This is a general Claude Code
skill (not specific to this repo), invoked directly by the user.

**Called by:**

- User invocation, typically `/loop 30m /spec-finalize <key>`. Not
  invoked programmatically by any spec skill.

**Spec-chain relevance:**

- `/spec-finalize` Status-check mode is designed to be `/loop`-
  friendly: it exits cleanly when the PR is open, signals TERMINAL
  STATE clearly when the PR is closed without merge, and performs
  the archive automatically when the PR is merged.

**Inputs:** Interval (e.g. `30m`, `1h`) followed by the wrapped skill
invocation. Omitting the interval lets the model self-pace.

**Failure modes:**

- Wrapped skill prompts interactively → loop stalls indefinitely.
  Mitigated in `/spec-finalize` by a TERMINAL STATE message on closed
  PRs instead of an interactive y/n prompt.

**Config keys read:** None from the spec chain.

---

## Build-equivalent: Gradle (no wrapper skill)

Where a per-product-matrix `/build <Product>` skill might exist in a
larger repo, `/spec-implement` and `/spec-ship`/`/spec-finalize`
pre-handoff gates in this repo instead invoke, directly, the commands
in `.specs/config.json` `verification.build_commands`:

```
./gradlew :composeApp:assembleDebug   # Android
./gradlew build                        # full multiplatform build
```

There is no per-product matrix — this is a single Gradle module
(`:composeApp`) — so "build once, green or not" is the entire automated
verification surface for Android/JVM changes. **iOS has no headless
build path**: a step touching `iosMain` or `iosApp/` is verified by
building the Xcode project manually (see `verification.build_commands.ios`).
Combined with `verification.no_test_suite: true`, a green build is the
sole automated gate; everything else (including any UI-visible change)
is a manual run in the emulator/simulator.

---

## Maintenance

- Update this file together with the relevant spec skill when an
  invocation's contract changes.
- Out of scope: inter-skill contracts among the spec-* skills
  themselves — those are anchored in `.specs/HANDOFFS.md`.
