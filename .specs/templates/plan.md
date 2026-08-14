---
spec_id: <short-kebab-slug>
generated_by: /spec-plan
generated_at: <ISO 8601>
---

> **Blockers**: <comma-separated OQ-N from proposal.md `blockers:`, or "none">
> **Depends on**: <comma-separated spec IDs from `depends_on:`, or "none">
>
> /spec-implement refuses to start while any blocker remains OR any
> dependency is not yet archived. To clear:
> - blocker → resolve the OQ in proposal.md §9, remove its ID from `blockers:`
> - dependency → wait for the depended spec to land in `specs/_archive/`,
>   then remove its ID from `depends_on:`
>
> Re-run /spec-plan after either to regenerate this banner.

# Plan: <title>

<!--
EXECUTABLE PLAN

This file is the ordered, file-level version of proposal.md section 6.
/spec-implement walks the steps top-to-bottom, calling the skill named
in each step's "Skill" field. tasks.md mirrors this as a checkbox list.

Rules:
- Every step must name concrete file paths or symbol/class names. No
  vague "update the ViewModel" entries — say which ViewModel class and
  which method.
- A step that calls an existing skill delegates entirely to that
  skill — do NOT duplicate its logic here. (This repo has no
  scaffolding skills yet; most steps will be "direct edits".)
- Each step has a one-line verification telling /spec-implement what
  "done" looks like for that step (build green, symbol exists, etc.).
  This repo has no test suite — never write "tests pass" as a
  verification (see `.specs/config.json` `verification.no_test_suite`).
-->

## Strategy

<!--
1–3 sentences. The high-level approach. Why this sequence.

This is also where per-skill rationale lives (proposal.md §6 is names
only).
-->

## Steps

<!--
Each step is tagged with one of three kinds. Use the matching template.

  [investigate]  Read-only research that produces answers/decisions which
                 feed later steps or resolve open questions in proposal.md.
                 No code changes. Use Serena's symbolic read tools
                 (get_symbols_overview, find_symbol, find_referencing_symbols).

  [implement]    Code change. Names a skill (or "direct edits"), the
                 area(s)/files/symbols touched, and a verification. For
                 a brand-new feature, this is usually a single step
                 naming `/new-feature` with its args (see
                 `.claude/skills/new-feature/SKILL.md`) — do not also
                 hand-author the files it scaffolds in other steps.

  [verify]       Standalone check (build the app, inspect an
                 expect/actual pair, manually run in the emulator/
                 simulator). Most verification is per-step inline, but
                 cross-cutting checks earn their own [verify] step.

An [implement] or [verify] step may also be tagged conditional —
`[implement, conditional]` — when it runs only if a gate holds. Such a
step carries a structured **Condition** field.

Rules common to all kinds:
- Name concrete file paths, class names, or Composable/ViewModel names.
  No "update the feature".
- Skill steps delegate entirely to the named skill — do NOT duplicate
  its logic here.
- One step per skill invocation: a chained skill is named in a SINGLE
  [implement] step and produces ALL its artifacts in that one run. Do
  NOT split one skill across multiple steps.
- Verification must be one observable line (build green, symbol X
  exists, expect/actual pair resolves, OQ N answered with citation).
  Never "unit test passes" — no test suite exists here.
- Skeleton steps: a step too underspecified to execute yet (blocked on
  an OQ or on an earlier [investigate] step's output) may be written as
  a one-liner tagged `_(skeleton)_`. Skeleton steps MUST be expanded
  with concrete files/symbols and a verification before /spec-implement
  runs them.
-->

### Step 1 — <short imperative title> [investigate]

- **Files / symbols**:
  - `composeApp/src/commonMain/kotlin/.../path/To/File.kt` — <what to read>
- **Question(s) to answer**: <bullet list; cite proposal.md OQ N if applicable>
- **Outputs to record**: <where the answer lands — usually proposal.md
  Open Questions, Affected Areas table, or Risks. If this step gates a
  conditional step, also record a `Step N result: <label> = <value>`
  marker the condition matches.>
- **Why**: <one line>

### Step 2 — <short imperative title> [implement]

- **Skill**: direct edits
- **Area(s)**: `<area>` (e.g. `features/home`, `data/repository/weather`)
- **Files / symbols**:
  - `composeApp/src/commonMain/kotlin/.../path/To/File.kt` — <what changes>
- **Skill args / inputs**: none
- **Why**: <one line; only if not obvious from title>
- **Verification**: <build green (`./gradlew :composeApp:assembleDebug`) / symbol X exists / expect/actual pair resolves>

<!-- Example of a scaffolding step instead of direct edits:
### Step 1 — Scaffold the RainAlerts feature [implement]
- **Skill**: `/new-feature`
- **Area(s)**: `features/rain_alerts`, `domain`, `data/repository/rain_alerts`, `di`
- **Files / symbols**: (produced by the skill — see its Output section)
- **Skill args / inputs**: `RainAlerts --data remote --parent none --nav-route yes`
- **Why**: brand-new top-level screen, not a change to an existing feature
- **Verification**: build green; every generated `// TODO` resolved by a later step
-->


### Step 3 — <short imperative title> [verify]

- **What to check**: <observable thing — build the app, inspect an
  expect/actual pair for parity, manually run in the emulator/simulator
  and confirm the UI behaves as expected>
- **Pass criteria**: <single line; concrete>

### Step 4 — <short imperative title> [implement, conditional]

- **Condition**: <the gate that decides run vs skip. Supported shapes:
  "Step N result: <label> = <value>" / "Frontmatter <field> equals
  <value>" / "File path Y exists">
- **Skill**: direct edits
- **Area(s)**: `<area>`
- **Files / symbols**:
  - `composeApp/src/commonMain/kotlin/.../path/To/File.kt` — <what changes>
- **Why**: <one line>
- **Verification**: <build green / symbol X exists>

<!-- /spec-implement evaluates **Condition** first. If it resolves to
     skip, the step is ticked with "skipped: <reason>" and no sub-actions
     run; otherwise it executes as a normal [implement] (or [verify])
     step. Drop this template when the spec has no conditional steps. -->

<!-- Add or remove steps as needed. Keep each small enough that one
     verification check tells you the step is done. If an [implement]
     step needs >3 files, split it. -->

## Dependencies

<!-- Step ordering constraints beyond top-to-bottom. e.g., "Step 4
     depends on Step 2's new domain interface existing before the
     repository implementation can reference it." Omit the section if
     there are none. -->

-

## Out-of-band actions

<!--
Anything that requires a human, runs outside Claude's reach, or happens
after /spec-handoff. Examples:
- "Verify on a real iOS device — the simulator can't exercise GPS
  location the same way"
- "Rotate/verify the WeatherAPI key quota before shipping"
Leave empty if none.
-->

-

## Rollback

<!--
What to revert if a step fails halfway. For most specs: "git restore the
files listed above." This repo has no migration tool and no test suite,
so a half-applied change must be reverted by hand if it reaches a real
device/build.
-->

<!-- One paragraph. -->
