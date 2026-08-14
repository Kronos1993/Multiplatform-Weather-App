---
name: spec-implement
description: "Use after /spec-plan has produced proposal.md / plan.md / tasks.md and the user has reviewed proposal.md. Executes the implementation steps in specs/<key>/plan.md, updating tasks.md checkboxes as work completes. Pre-flights spec readiness (blockers empty, every depends_on archived, no skeleton steps, recommend-split honored, at least one local verification path enabled), then walks each unchecked step, routing by kind: [investigate] reads code and records outputs to proposal.md; [implement] makes changes via the named skill or Serena's symbolic edit tools; [verify] runs builds/inspections. Stops on the first failed verification — does NOT auto-fix or push past failures. Does NOT commit, branch, push, or open PRs (that's /spec-handoff), and never edits beyond the file list named by plan.md. Conventions for step kinds, gauntlet, blockers, and depends_on live in .specs/templates/, .specs/IMPROVEMENTS.md, and this repo's root CLAUDE.md."
metadata:
  claude_md_requires: '[]'
---

## Usage

```
/spec-implement [<spec_id>]
```

- With `<spec_id>` (e.g. `add-rain-radar-toggle`): implement that spec.
- Without: selects the candidate per `.specs/SPEC_DISCOVERY.md` —
  predicate: most recent `tasks.md` whose Implementation section has at
  least one unchecked step.

## Preconditions

- `specs/<spec_id>/proposal.md`, `plan.md`, `tasks.md` all exist.
- proposal.md frontmatter `blockers: []` (empty).
- proposal.md frontmatter `depends_on:` is either empty OR every
  listed ID has a folder under `specs/_archive/<ID>/`. The list does
  NOT need to be edited to empty after dependencies land — archive
  presence is the gate.
- No plan.md step still tagged `_(skeleton)_`.
- Serena is active — this repo has a single Serena project at the
  repo root (`.serena/`).

## Pre-flight checks

Run all five before touching any file. Fail closed.

1. **Blockers empty** — read `proposal.md` frontmatter. If
   `blockers:` contains any ID, stop with: "Spec is blocked on <list>.
   Resolve in §9, remove from frontmatter, re-run /spec-plan to
   refresh banners, then re-run /spec-implement."
2. **Dependencies archived** — read `depends_on:`. For each ID,
   confirm `specs/_archive/<ID>/` exists. If any does not, stop.
3. **No skeleton steps** — Grep `plan.md` for `_(skeleton)_`. If any
   match, stop with the list.
4. **Recommend-split honored** — read `recommend_split` from
   frontmatter. Honored when *either* `recommend_split: no`, *or*
   `recommend_split: yes` AND §3 Scope carries an explicit override
   sentence naming the split recommendation and the rationale for
   proceeding without splitting. A bare `recommend_split: yes` with no
   such sentence is NOT honored — stop.
5. **At least one local verification enabled** — read
   `verification.build_per_implement_step` and
   `verification.build_matrix_at_pre_handoff` from
   `.specs/config.json`. If BOTH are `false`, also read
   `verification.skip_all_local`. If not `true`, stop with: "All local
   verification paths are disabled and `verification.skip_all_local`
   is not set. Set at least one build flag to `true`, or set
   `skip_all_local: true` to explicitly accept no local builds." If
   `skip_all_local: true`, proceed but surface
   `[!] skip_all_local opt-in active — no local builds will run` in the
   final summary.

If all five pass, proceed.

## What this skill does

1. Read `proposal.md`, `plan.md`, `tasks.md`.
2. Confirm Serena is active.
3. Walk unchecked steps in `tasks.md` Implementation section
   sequentially. For each step:
   - Locate the matching step in `plan.md` (same number).
   - Read the step's kind tag (`[investigate]` / `[implement]` /
     `[verify]`) and its `**Condition**:` field if present.
   - Evaluate the condition (if any). If skip, tick with sub-bullet
     "skipped: <reason>", move on.
   - Otherwise, route to the matching execution path below.
   - On success, tick all sub-checkboxes whose verifications were
     met, then tick the parent step.
   - On failure, stop with a structured report (see Failure modes).
4. After all Implementation steps are green, run the Pre-handoff
   checks one by one.
5. If anything non-obvious was decided during implementation, append
   to `decisions.md` in the spec folder (create if absent).
6. Print summary; point the user at `/spec-handoff`.

## Step execution by kind

### `[investigate]` step

Use Serena's symbolic read tools (`find_symbol` with
`include_body: true`, `get_symbols_overview`, `find_referencing_symbols`)
for whole-symbol reads without loading raw files. Use Glob/Grep for
pattern-presence checks (e.g. confirming a similar ViewModel/Screen
pattern in a sibling feature).

For each `[investigate]` step:

1. Read the step from plan.md (Files / symbols, Question(s) to
   answer, Outputs to record).
2. For each symbol or file in "Files / symbols", read it with the
   appropriate symbolic tool.
3. Answer each question. Write the answer to the location named in
   "Outputs to record" — typically `proposal.md` §9 (OQ resolutions)
   with file:line citations, or §4 (Affected areas refinements).
   **When this step gates a conditional step**, also record a
   `Step N result: <label> = <value>` marker.
4. Tick the matching sub-checkbox(es) in tasks.md.

Step verification: "all questions answered with citations."

### Build verification policy

Read `.specs/config.json` `verification.*` at skill start. This repo
has no `/build` wrapper skill — invoke the Gradle commands from
`verification.build_commands` directly:

- `android`: `./gradlew :composeApp:assembleDebug`
- `full` (Android + JVM/desktop): `./gradlew build`
- `ios`: no headless command exists — build via Xcode
  (`iosApp/iosApp.xcodeproj`) or note it as a manual out-of-band check
  when a step touches `iosMain`/`iosApp/`.

- `verification.build_per_implement_step` (default `false`) — when
  `true`, run the appropriate command above after each `[implement]`
  step (use `android` unless the step touched `jvmMain`, in which case
  use `full`).
- `verification.build_matrix_at_pre_handoff` (default `true`) — when
  `true`, run `full` once during pre-handoff.
- `verification.skip_all_local` (default `false`) — explicit opt-in
  when both flags above are `false`.
- `verification.distinguish_pre_existing_baseline` (default `true`) —
  see below.

This repo has no lint/formatter configured (no detekt/ktlint/
`.editorconfig`) — a green build is the only automated signal; there is
no separate style-check step to run.

Explicit `[verify]` steps authored in plan.md always run regardless of
these flags.

### Distinguishing pre-existing baseline failures

A red build is not always the spec's fault — the app may already fail
to build on `develop` before our edits (a stale dependency, an
unrelated pre-existing compile error). Blaming the spec for a failure
it did not introduce wastes a debugging cycle.

When `verification.distinguish_pre_existing_baseline: true` (default)
and a `[verify]` build (or the pre-handoff build) returns **red** for a
real compile reason:

1. Prompt the user: "The build is red. Re-run after `git stash` to
   confirm it's pre-existing on baseline? (y/n)".
2. On `y`: `git stash`, re-run the exact same build command on the
   clean tree, then `git stash pop` regardless of outcome.
   - **Baseline also red** → pre-existing. Surface `red (pre-existing
     baseline)`, record it in `decisions.md` (error signature), tick
     with that sub-bullet, and **continue**.
   - **Baseline green** → the spec's edits introduced the red. Hard
     stop per Failure modes.
3. On `n` (or `distinguish_pre_existing_baseline: false`): treat as a
   hard stop without the baseline check.

Never `git stash` without restoring: always `git stash pop` afterward.

### `[implement]` step with skill

This repo has one scaffolding skill: `/new-feature` (see
`.claude/skills/new-feature/SKILL.md`), used for brand-new
screens/capabilities per `/spec-plan`'s "Choosing `/new-feature` vs
direct edits" rule. proposal.md §6 names it when it applies; plan.md's
step carries the exact `--data`/`--parent`/`--nav-route` args in "Skill
args / inputs".

For each `[implement]` step that names a skill:

1. Read the step from plan.md: skill name, args/inputs, area(s),
   files/symbols, why, verification.
2. Invoke the named skill via the `Skill` tool, passing the args
   verbatim from "Skill args / inputs" — do not re-prompt the user for
   anything the plan already answered. `/new-feature` will still ask
   for its own confirmation before writing files (per its
   Preconditions) — that confirmation is expected, not a re-prompt.
3. If `verification.build_per_implement_step: true`, run the
   appropriate build command (see Build verification policy) after the
   skill completes.
4. Tick the sub-checkboxes once the listed verifications pass. For
   `/new-feature` specifically: "verification met" means the scaffold
   compiles — the `// TODO`s it leaves are NOT failures, they're the
   scope of the plan's subsequent `[implement]` steps.
5. On failure (the skill aborts, or the scaffold doesn't compile),
   stop. Do not attempt to undo partial changes — the user decides.
   Working-tree changes survive across reruns.

### `[implement]` step with direct edits

- Use Serena's symbolic edit tools (`replace_symbol_body`,
  `insert_after_symbol`, `insert_before_symbol`) for `.kt`/`.swift` file
  edits — they handle whitespace and import context correctly. Do NOT
  use raw `Edit` on `.kt`/`.swift` files when a symbolic edit is
  possible.
- For non-code edits (Compose resource `.xml`, iOS `.strings`, Gradle
  `.kts`, `libs.versions.toml`, markdown), `Edit` is fine.
- After edits:
  - If `verification.build_per_implement_step: true`, run the
    appropriate build command (see Build verification policy).
    Otherwise skip per the flag.
  - Tick sub-checkboxes per the verification.
- Edits must stay within the file list named by plan.md "Files /
  symbols". If another file must also change, **stop and surface it**
  — do not silently expand scope. The user updates plan.md and re-runs
  `/spec-plan`.

### `[verify]` step

Run the checks inline via Bash / Grep / Glob.

Common check types:

- Android/JVM build: `./gradlew :composeApp:assembleDebug` or
  `./gradlew build`.
- Expect/actual parity: confirm a touched `expect` in `commonMain` has
  a matching `actual` in every affected source set (Grep for the
  declaration name across `androidMain`/`iosMain`/`jvmMain`).
- Dual localization: confirm a touched user-facing string appears in
  both the Compose resource XML (`values` and `values-es`) and, if
  iOS-reachable, both `iosApp/en.strings` and `iosApp/es.strings`.
- Manual run in the Android emulator or iOS simulator/device (when
  feasible in the current environment) to confirm the change behaves
  as expected.
- Grep sweep for the absence of a pattern.

**No automated tests, ever.** This repo has zero test source sets
(`verification.no_test_suite: true`, confirmed in root `CLAUDE.md`) —
never write or invent a "run tests" `[verify]` step.

**Grep-sweep guard (avoid false-green).** Before ticking an
"absence of pattern" check, confirm the search scope resolved to **at
least one file**. A glob/path matching zero files is a mis-scoped
check, not a pass: stop and surface `verify scope empty: <scope>
matched 0 files`.

For build commands whose output may be large, prefer
`run_in_background: true` plus a tail/grep over the log file so the
full build log never enters main context.

### Conditional step (`Condition:` field present in plan.md)

Same evaluation rules as before — match "Step N result: <label> =
<value>" / "Frontmatter <field> equals <value>" / "File path Y exists"
against a structured source, never fuzzy prose. If the marker is
absent, STOP rather than infer.

## Pre-handoff checks (after all Implementation steps green)

Run each item in tasks.md "Pre-handoff checks" section:

1. Verify each precondition again (frontmatter empty, OQs resolved, no
   skeleton steps).
2. If `verification.build_matrix_at_pre_handoff: true` (default), run
   `./gradlew build` once. **Inherit, don't re-run**: if an explicit
   `[verify]` step already built the app this session, reuse its
   verdict instead of rebuilding. Send the build as Bash
   `run_in_background: true` writing to a log file, then wait and read
   the tail/grep for success/failure. If the flag is `false`, skip and
   surface `[!] build deferred to a later manual run`.
3. If any `iosMain`/`iosApp/` file changed, surface
   `[!] iOS build must be verified manually via Xcode — no headless
   path exists` rather than silently skipping it.
4. Grep changed files for new logger/print calls touching the
   WeatherAPI key or any other credential-shaped string
   (`verification.secret_log_keywords` in `.specs/config.json`,
   case-insensitive). Any hit stops the run.
5. Confirm every touched commonMain `expect` has a matching `actual`
   in every affected source set (manual review, per
   `architecture.expect_actual_parity_required`).
6. Confirm any touched user-facing string shown by both Compose UI and
   native iOS code is updated in both locations
   (`architecture.dual_localization_required`).
7. **Do not run or invent an automated-test step** — none exist in
   this repo. Note this explicitly in the summary rather than silently
   omitting it, so the absence reads as "confirmed N/A", not "skipped
   by accident".
8. Tick each Pre-handoff check as it passes.

After all Pre-handoff checks pass, the spec is implementation-complete
and ready for `/spec-handoff`.

## Re-running

`/spec-implement` is safe to re-run; it is **idempotent up to ticked
state**. A re-run resumes at the first unchecked step, skips
already-ticked steps (their recorded outputs are not duplicated), and
never unticks or reverts anything.

To force a step to re-execute, untick its box (and sub-boxes) in
tasks.md first. To re-plan instead, delete proposal.md and re-run
`/spec-plan`.

## Commit model

This skill produces **zero commits**. The single commit per spec is
made by `/spec-handoff`, which runs after the user reviews the
working-tree diff.

Implications:

- The working tree carries spec progress between steps.
- `/spec-handoff` requires a dirty working tree. If work was committed
  externally, follow the recovery row in `/spec-handoff`'s
  failure-modes table — do NOT amend.
- Branch creation lives in `/spec-handoff`, not here.

## What this skill does NOT do

- Does not commit, branch, push, or open PRs (`/spec-handoff`).
- Does not contact any external tracker — this repo has none.
- Does not modify `story.md` (append-only after `/spec-new`).
- Does not modify proposal.md frontmatter (blockers / depends_on /
  recommend_split are user-managed).
- Does not modify proposal.md §1–§8 — those are spec authority. May
  update §4 and §9 when later work surfaces an inaccuracy in an
  earlier step's output (dated subsection if outside `[investigate]`).
  Full model: `.specs/IMMUTABILITY.md`.
- Does not chain into `/spec-handoff` automatically.
- Does not auto-fix verification failures — stop and surface.
- Does not silently expand scope.
- Does not run or invent an automated test suite — none exists.

## decisions.md

Append-only log of non-obvious choices made during implementation.
Created on first append (seed from `.specs/templates/decisions.md`);
absent if nothing non-obvious arose. `/spec-finalize` (and
`/spec-ship`) surface these entries as reusable-knowledge candidates
at archive time — saved as Serena memories under `.serena/memories/`
at the repo root.

## Failure modes and recovery

| Symptom | Cause | Recovery |
|---|---|---|
| Pre-flight fails on `blockers:` non-empty | OQ not resolved | Resolve in §9, remove from frontmatter, re-run `/spec-plan` then `/spec-implement` |
| Pre-flight fails on unarchived dependency | An ID in `depends_on:` lacks a folder under `specs/_archive/` | Wait for the sibling to land via `/spec-finalize`; re-run when its archive folder exists |
| Pre-flight fails on skeleton steps | Plan was incomplete | Expand the skeleton steps in plan.md (and tasks.md), re-run |
| Pre-flight fails on `recommend_split: yes` | Split decision pending | Confirm split OR override in §3 Scope, re-run |
| Pre-flight fails on all-local-verification-disabled | Both build flags false, `skip_all_local` not true | Set at least one build flag to `true`, or set `skip_all_local: true` |
| Build fails (compile error) | Compile error from the edits | Inspect, fix with Serena's symbolic edit tools, re-run. Check pre-existing-baseline first if enabled |
| `[verify]`/build returns `red (pre-existing baseline)` | App already red on clean `develop` before the spec's edits | Not a spec failure — recorded in decisions.md, ticked, spec proceeds |
| Out-of-scope file change needed | Plan was incomplete | Stop. Surface the file. User updates plan.md, re-run |
| `[verify]` reports `scope empty: <scope> matched 0 files` | Mis-scoped absence check | Fix the scope in plan.md, re-run. Do NOT tick green |
| Conditional step: gating Step N has no `result:` marker | The `[investigate]` step didn't record it | Add the result marker, re-run |
| Serena not active | Not activated, or wrong cwd | Activate the repo's Serena project, retry |

## Reference

- Templates: `.specs/templates/`.
- Config: `.specs/config.json`.
- Handoff contracts: `.specs/HANDOFFS.md` — H2 (consumes the plan),
  H3/H4/H7 (produces ticked tasks + dirty tree for
  review/handoff/ship).
- Config schema: `.specs/config.schema.md`.
- Candidate discovery: `.specs/SPEC_DISCOVERY.md`.
- Backlog: `.specs/IMPROVEMENTS.md`.
- Sibling skills: `/spec-new`, `/spec-plan`, `/spec-handoff`.
