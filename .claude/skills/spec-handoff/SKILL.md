---
name: spec-handoff
description: Use after /spec-implement reports the spec is implementation-complete and Pre-handoff checks are green. Finalizes the spec with a local commit by pre-flighting tasks.md completeness, ensuring the working tree is on a feature/bugfix/hotfix branch (creating one from develop if necessary, never committing directly to develop), and invoking /commit to stage and produce the conventional commit. Does NOT push, open PRs, archive the spec folder, or surface memory candidates — push and PR-open belong to /spec-finalize (which uses /create-pr); archiving and memory-candidate surfacing also live in /spec-finalize so they reflect actual merge outcomes. Run /spec-finalize next to push + open the PR. Branch naming map lives in `.specs/config.json` `branch.naming`.
metadata:
  claude_md_requires: '[]'
---

## Usage

```
/spec-handoff [<spec_id>]
```

- With `<spec_id>`: handoff that spec.
- Without: selects the candidate per `.specs/SPEC_DISCOVERY.md` —
  predicate: most recent `tasks.md` whose Implementation + Pre-handoff
  sections are fully ticked.

## Preconditions

- `specs/<spec_id>/proposal.md`, `plan.md`, `tasks.md` all exist.
- All tasks.md Implementation checkboxes ticked.
- All tasks.md Pre-handoff checkboxes ticked.
- Working tree has uncommitted changes (staged or unstaged).
- Current branch is either `develop` (skill will branch off) or a
  branch matching the naming convention from `.specs/config.json`
  `branch.naming` for the spec's type.

## Pre-flight checks

Fail closed — do not stage or commit if any check fails.

1. **Spec files present** — proposal.md, plan.md, tasks.md all exist.
2. **Implementation complete** — count unchecked boxes in tasks.md
   Implementation section. If > 0, stop with: "Spec has unfinished
   work: <list>. Run /spec-implement to finish, or tick manually if
   the steps were resolved out-of-band."
3. **Pre-handoff complete** — count unchecked boxes in Pre-handoff
   section. If > 0, stop with the list.
4. **Working tree dirty** — `git status --porcelain` returns
   non-empty. If empty, stop with: "No changes to commit. Did
   /spec-implement actually run, or were the changes committed
   externally? Inspect with git status."
5. **Branch name compatible** — see Branch handling below.

## Branch handling

Hard rule (this repo's `/create-pr` and `/commit` skills both assume
it): **never commit directly to `develop`, `main`, or `master`.**

Logic:

```
current = git rev-parse --abbrev-ref HEAD

if current in ("develop", "main", "master"):
    if frontmatter `branch_suggested` is set and non-empty:
        branch = branch_suggested
    else:
        branch = render branch.naming[spec.type] with {key} = spec_id
    confirm <branch> with the user; accept an override name if given
    git checkout -b <branch>
    proceed

elif current matches the naming convention for spec.type:
    proceed

elif current matches the naming convention for *some* type:
    warn: "On branch <current>, but spec type is <type> which expects
           pattern <naming[type]>. Proceed anyway? (y/n)"
    on yes: proceed
    on no: stop

else:
    stop: "Current branch <current> doesn't match any spec naming
           convention. Move to a feature/bugfix/hotfix branch (or
           develop) and re-run."
```

The `branch.naming` table from config:

| Spec type | Branch pattern |
|---|---|
| bug | `bugfix/{key}` |
| story | `feature/{key}` |
| refactor | `refactor/{key}` |
| chore | `chore/{key}` |
| hotfix | `hotfix/{key}` |

`{key}` substitutes the `spec_id` from proposal.md frontmatter. This
repo's git history so far uses `feat:`/`fix:` commit-type prefixes
loosely with no enforced branch naming (solo-maintained, no prior PR
flow) — this table is this workflow's own going-forward convention,
not a retroactive judgment.

## What this skill does

1. Run pre-flight checks. Stop on any failure.
2. Resolve / create the branch per Branch handling.
3. **Resolve the canonical file set** from plan.md "Files / symbols"
   (the staging + scope authority), normalizing each entry:
   - **File path** (e.g. `composeApp/src/commonMain/kotlin/.../
     WeatherViewModel.kt — what changes`): strip the trailing
     `— <description>`; the leading path token is the file.
   - **Glob** (e.g. `composeApp/src/**/*ViewModel.kt`): expand via Glob.
   - **Symbol name** (e.g. `WeatherViewModel.getWeather`, no path
     separator): resolve to the declaring file(s) via `find_symbol`,
     and remember the named symbol(s) for the symbol-level check below.

   The union of resolved files is the canonical set. Cross-check it
   against `git status --porcelain`:
   - **File-level scope.** Any changed file **outside** the canonical
     set stops the run: "Out-of-scope changes detected: <list>. Not in
     plan.md. Stash, revert, or add to plan.md before re-running."
   - **Symbol-level scope.** For an entry scoped to specific *symbols*
     (not the whole file), inspect the diff hunks in that file: hunks
     touching symbols **other than** the named ones are symbol-level
     scope creep. Surface them and ask the user to confirm they belong
     or update plan.md. Do not silently fold them into the commit.

   **Carve-out**: changes under `specs/<spec_id>/**` (proposal/tasks/
   decisions edits written by `/spec-implement`) are always in-scope
   and skip both checks.
4. Stage **explicitly by file name** — never `git add -A` or
   `git add .`. Use `git add <file>` per file.
5. Invoke `/commit` via the Skill tool. `/commit` handles the
   conventional-commit message.
6. After `/commit` succeeds:
   - Tick the **Branch created**, **`/commit` executed** sub-checkboxes
     in tasks.md Handoff section.
   - Leave the **Branch pushed**, **PR opened**, **Spec folder
     archived**, and **Reusable-knowledge candidates surfaced** rows
     unchecked — those are `/spec-finalize`'s job. Annotate them
     with `*(see /spec-finalize)*`.
7. Print summary, point at the next action: run `/spec-finalize
   <spec_id>` to push the branch and open the PR.

## What this skill does NOT do

- Does not push the branch — `/spec-finalize` does this via `/create-pr`.
- Does not open a PR.
- Does not archive the spec folder — `/spec-finalize` does the
  `git mv` on a new branch off `origin/develop` **after** the PR
  merges, so archive timing matches reality.
- Does not surface `decisions.md` reusable-knowledge candidates —
  moved to `/spec-finalize` (first run, immediately after PR opens).
- Does not comment anywhere — this repo has no issue tracker or chat
  webhook configured.
- Does not run any verification beyond the pre-flight.
- Does not advance proposal.md frontmatter `status:`.

## Failure modes and recovery

| Symptom | Cause | Recovery |
|---|---|---|
| Pre-flight: unchecked Implementation step | Work not finished | Run /spec-implement; or tick manually if resolved out-of-band |
| Pre-flight: unchecked Pre-handoff step | Verification missing | Run the missing check; tick when green |
| Pre-flight: working tree clean | Nothing to commit | Inspect `git log` — was the work already committed? If yes, manually tick the Handoff sub-rows (`Branch created`, `/commit executed`) in tasks.md and run `/spec-finalize <spec_id>`. **Do not amend** |
| Out-of-scope changes (file **or symbol** level) | Implementation touched something plan.md didn't name | File-level: add it to plan.md (re-run /spec-plan) or stash/revert. Symbol-level: confirm the extra symbols belong or revert them |
| Branch is `main`/`master` or some unrelated branch | Wrong cwd or leftover branch from earlier work | Move to `develop` (or directly to the right feature branch); re-run |
| `/commit` fails on hooks | Project hook tripped | Read the hook output, fix the issue, re-run. Never `--no-verify` |
| `/commit` fails on build (compile error) | The change doesn't compile | Inspect, fix with Serena's symbolic edit tools, re-run |

## Examples

### Confined bug, on develop, all green

```
/spec-handoff fix-uv-index-rounding
```
- Pre-flight: all tasks ticked, working tree dirty in
  `composeApp/src/commonMain/kotlin/.../features/home/current_weather`
  (plus `specs/fix-uv-index-rounding/*` — carved out as in-scope). Pass.
- Branch: currently on `develop`. Skill creates
  `bugfix/fix-uv-index-rounding` and switches.
- Stages the changed code files and the spec metadata explicitly.
- Invokes `/commit`. Commit created.
- Output: "Spec fix-uv-index-rounding handed off. Branch
  `bugfix/fix-uv-index-rounding`, 1 commit ahead of develop. Run
  `/spec-finalize fix-uv-index-rounding` to push and open the PR."

### Recovery: work committed externally

```
/spec-handoff add-rain-radar-toggle
```
- Pre-flight #4 stops the run with "No changes to commit." — the
  working tree is clean because the work is already in `git log`.
- Recovery: inspect `git log` to confirm the spec's commits are
  present, manually tick the Handoff sub-rows in tasks.md, then run
  `/spec-finalize add-rain-radar-toggle` to push and open the PR. Do
  **not** amend any of the existing commits.

## Reference

- Templates: `.specs/templates/`.
- Config: `.specs/config.json` (`branch.naming`; `pr.*` is read by
  `/create-pr` via `/spec-finalize`).
- `/commit` skill — invoked by step 5; do not duplicate its logic here.
- Next-step skill: `/spec-finalize` — push, PR-open, memory-candidate
  surfacing, and merge-poll/archive.
- Handoff contracts: `.specs/HANDOFFS.md` — H4 (consumes ticked tasks
  + dirty tree), H5 (produces the impl-branch commit for
  `/spec-finalize`).
- Config schema: `.specs/config.schema.md`.
- Choosing how to finalize: `.specs/CHOOSING_FINALIZE.md`.
- Candidate discovery: `.specs/SPEC_DISCOVERY.md`.
- Backlog: `.specs/IMPROVEMENTS.md`.
- Sibling skills: `/spec-new`, `/spec-plan`, `/spec-implement`,
  `/spec-finalize`, `/spec-status`.
