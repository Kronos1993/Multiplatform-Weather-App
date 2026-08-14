---
name: spec-review
description: "Optional read-only review of the uncommitted diff against the spec's intent and this repo's architectural rules. Sits between /spec-implement and /spec-handoff but is decoupled from both — invoke explicitly when wanted. Pre-flights tasks.md completeness (refuses if any Implementation or Pre-handoff checkbox is unchecked). Runs inline review passes comparing the diff against proposal.md acceptance criteria, plan.md \"Files / symbols\" scope, this repo's Clean Architecture (domain/repository DIP boundary, Result-type error handling, Koin wiring) + expect/actual-parity + dual-localization + secrets rules, and the relevant Serena memories. Tags each finding [code] / [scope] / [intent] and classifies severity by confidence (Critical >=80, Important 50-79; <50 filtered). Pure reporter — writes findings to specs/<key>/.review-findings.md (gitignored) and exits 0 always; never commits, stages, edits code, or blocks /spec-handoff. Default --quick (architecture + security); --full adds scope/intent and a bug shallow-scan; --security-only and --scope-only narrow further."
metadata:
  claude_md_requires: '[]'
---

## Usage

```
/spec-review [<spec_id>] [--quick | --full | --security-only | --scope-only]
```

- With `<spec_id>` (e.g. `add-rain-radar-toggle`): review that spec.
- Without: selects the candidate per `.specs/SPEC_DISCOVERY.md` —
  predicate: most recent `tasks.md` whose Implementation + Pre-handoff
  sections are fully ticked.
- Modes:
  - `--quick` (default): architecture + security passes.
  - `--full`: all four passes (scope/intent, architecture, security, bugs).
  - `--security-only`: just the security pass.
  - `--scope-only`: just the scope/intent pass.

## Preconditions

- `specs/<spec_id>/proposal.md`, `plan.md`, `tasks.md` all exist.
- All tasks.md Implementation checkboxes ticked.
- All tasks.md Pre-handoff checkboxes ticked.
- Working tree has uncommitted changes (staged or unstaged).
- Serena active — this repo has a single Serena project at the repo
  root.

## Pre-flight checks

Fail closed — do not run any review pass if a check fails.

1. **Spec files present** — proposal.md, plan.md, tasks.md all exist.
2. **Implementation complete** — count unchecked boxes in tasks.md
   Implementation section. If > 0, stop with: "Spec has unfinished
   work: <list>. Run `/spec-implement` to finish, or tick manually if
   the steps were resolved out-of-band."
3. **Pre-handoff complete** — count unchecked boxes in Pre-handoff
   section. If > 0, stop with the list.
4. **Working tree dirty** — `git status --porcelain` returns
   non-empty. If empty, stop with: "No diff to review. Either nothing
   was changed, or the changes were already committed (inspect with
   `git show` — this skill only reads the uncommitted working tree)."

If all four pass, proceed.

## What this skill does

1. Read `proposal.md` (intent, frontmatter `type:`, §8 acceptance
   criteria), `plan.md` ("Files / symbols" lists per step), and
   `tasks.md` (completion state — already verified by pre-flight).
2. Capture the diff to review:
   - `git diff develop...HEAD` (commits on the branch since divergence)
   - `git diff` (unstaged)
   - `git diff --cached` (staged)
   - Combined into a single diff blob, plus the list of touched files.
3. Identify the relevant Serena memories (see `/spec-plan` "Knowledge
   selection" table for the keyword→area mapping) and read them via
   Serena MCP before each pass.
4. Run the review passes for the selected mode (see "Review passes"
   below) sequentially, each producing a JSONL findings stream.
5. Filter / classify findings — keep ≥`review.confidence_important`;
   tag ≥`review.confidence_critical` as `Critical`, the rest as
   `Important`. Tag each finding by class: `[code]` / `[scope]` /
   `[intent]`.
6. Write `specs/<spec_id>/.review-findings.md` (overwritten each run).
7. Print summary to conversation; exit 0.

## Review passes

| Pass | Triggered by mode | Reads | Outputs |
|---|---|---|---|
| **Scope & intent** | `--full`, `--scope-only` | diff file list, proposal.md §2/§3/§8, plan.md "Files / symbols" lists | findings tagged `[scope]` (creep / incomplete) and `[intent]` (missing AC coverage) |
| **Architecture** | `--quick` (default), `--full` | diff hunks for `.kt`/`.swift` files, this repo's domain/repository DIP boundary, Koin wiring, Result-type error handling, and expect/actual + dual-localization rules (root CLAUDE.md, the `solid` skill, `.serena/memories/`) | findings tagged `[code]` for: a ViewModel importing a `data/repository/*Impl` class directly; business logic leaking into a Composable instead of the ViewModel/repository; a new repository not wired through Koin; a touched `expect` with a missing `actual` on an affected platform; a shared string updated on one side (Compose resources / iOS `.strings`) but not the other |
| **Security** | `--quick` (default), `--full`, `--security-only` | diff hunks for `.kt`/`.swift`/`.xml` files, `verification.secret_log_keywords` | findings tagged `[code]` for the WeatherAPI key (or any credential-shaped string) logged, printed, or hardcoded outside its existing config location |
| **Bug shallow-scan** | `--full` | diff hunks only | findings tagged `[code]` for actual bugs — null handling, a sealed `XxxScreenState` `when` with a silent catch-all swallowing a state that should be handled explicitly, an unguarded `.first()`/`.single()` over potentially-empty data, a coroutine scope leak |

For each pass:

1. Read the relevant Serena memories via `mcp__serena__read_memory`.
   Do not paste memory bodies into the skill — read them directly.
2. Examine the diff hunks (Architecture / Security / Bug passes) or
   the diff file list against plan.md / proposal.md (Scope & intent).
3. Emit findings as JSONL records, one per line:
   `{"id": "<short>", "tag": "code|scope|intent", "severity_score": 0-100, "rule": "<one-line rule citation>", "location": "<file:line or N/A>", "summary": "<≤2-line description>", "fix_hint": "<one-line>"}`.

This skill is **read-only**: it never edits the working tree, the
spec folder, or any source file.

## Confidence classification

| Score | Class | Treatment |
|---|---|---|
| 80–100 | `Critical` | User should address before `/spec-handoff`. |
| 50–79 | `Important` | Worth a look but may be context-dependent. |
| <50 | (filtered) | Not surfaced. |

Thresholds live in `.specs/config.json` `review.confidence_critical`
and `review.confidence_important`.

## Finding-class taxonomy

| Tag | Means | Fix location |
|---|---|---|
| `[code]` | A code-level violation: bug, DIP-boundary break, expect/actual asymmetry, dual-localization gap, security rule | The Kotlin/Swift source file flagged in `location` |
| `[scope]` | A file was modified that isn't in `plan.md` "Files / symbols", OR a file is in plan.md but no longer touched | Either add the file to `plan.md` and re-run `/spec-plan`, OR revert the edit |
| `[intent]` | An acceptance criterion from `proposal.md` §8 doesn't appear addressed by the diff | Either implement the missing piece, OR amend `proposal.md` §8 if the criterion was wrong |

## Findings file format

Path: `specs/<spec_id>/.review-findings.md` — gitignored via
`specs/*/.review-findings.md`. Overwritten on every run.

```markdown
# Review findings: <spec_id>

- Run at: <ISO-8601 timestamp>
- Mode: --quick | --full | --security-only | --scope-only
- Diff: <N files changed, +<plus> / -<minus>>
- Passes run: <list>

## Critical (N)

### F1 — [code] WeatherViewModel imports WeatherRemoteRepositoryImpl directly

- Rule: ViewModels depend on `domain/repository` interfaces, never
  concrete `data/*Impl` classes (root CLAUDE.md / `mem:architecture`)
- Location: features/home/current_weather/WeatherViewModel.kt:12
- Confidence: 88
- Fix hint: inject `WeatherRemoteRepository` (the interface) via Koin
  instead of importing the `Impl` class directly.

[…more Critical findings…]

## Important (N)

### F4 — [intent] AC #3 not addressed

- Rule: proposal.md §8 acceptance criterion #3 — no corresponding code
  change in the diff.
- Location: N/A (missing implementation)
- Confidence: 65
- Fix hint: Add the missing behavior; or amend §8 if the AC was wrong.

[…more Important findings…]

## Clean

(If no findings ≥`confidence_important`: "No findings ≥<confidence_important>
across <N> passes. Diff is clean against the rules covered by mode
`<mode>`. Run with `--full` for a deeper pass if desired.")
```

## What this skill does NOT do

- Does NOT commit, stage, edit, push, or branch.
- Does NOT modify spec files (proposal.md / plan.md / tasks.md /
  story.md / decisions.md / handoff.json).
- Does NOT block `/spec-handoff` — it's a reporter, exits 0 always.
- Does NOT auto-fix any finding.
- Does NOT run a build — verification is `/spec-implement`'s job.
- Does NOT contact any issue tracker, GitHub, or chat webhook.
- Does NOT advance proposal.md frontmatter `status:`.
- Does NOT enforce its own findings across runs — re-running
  overwrites `.review-findings.md` with a fresh result.
- Does NOT invent an automated-test finding — none exist in this repo.

## Failure modes and recovery

| Symptom | Cause | Recovery |
|---|---|---|
| Pre-flight: unchecked Implementation step | Work not finished | Run `/spec-implement`; or tick manually if resolved out-of-band |
| Pre-flight: unchecked Pre-handoff step | Verification missing | Run the missing check, tick when green |
| Pre-flight: working tree clean | Nothing to review | Inspect with `git show` directly if already committed |
| All passes return empty | Diff is trivially clean OR mode is too narrow | Try `--full` for a deeper pass |
| Many `[scope]` findings | Implementation drifted from plan.md | Update plan.md (re-run `/spec-plan`), OR revert the out-of-scope edits |
| Many `[intent]` findings | Acceptance criteria don't match the diff | Implement the missing pieces, or amend proposal.md §8 |

## Examples

### Confined bug, --quick (default), all clean

```
/spec-review fix-uv-index-rounding
```
- Pre-flight passes. Working tree dirty in
  `features/home/current_weather`.
- Architecture + Security passes run. No findings.
- Output: "0 findings. Diff is clean for mode `--quick`."

### Story, --full, with blocking findings

```
/spec-review add-rain-radar-toggle --full
```
- 4 passes run. 2 Critical (a ViewModel importing a `*Impl` class
  directly, a missing iOS `actual` for a touched `expect`), 1
  Important (missing AC).
- Output: counts + top findings inlined, pointer to the findings file.

## Reference

- Templates: `.specs/templates/`.
- Config: `.specs/config.json` `review.*`.
- Root `CLAUDE.md`, the `solid` skill — the architecture rules this
  skill's Architecture pass checks against.
- Sibling skills: `/spec-new`, `/spec-plan`, `/spec-implement` (the
  step before), `/spec-handoff` (the step after; unchanged by this
  skill's existence), `/spec-finalize`, `/spec-status`, `/spec-ship`.
- Config schema: `.specs/config.schema.md`.
- Candidate discovery: `.specs/SPEC_DISCOVERY.md`.
- Backlog: `.specs/IMPROVEMENTS.md`.
