---
spec_id: <short-kebab-slug>
title: <one-line summary>
type: bug | story | refactor | chore
priority: <low | normal | high | critical>
source: file | manual | url
source_ref: <path, note, or URL>
created: <YYYY-MM-DD>
status: INTAKE_PARSED
recommend_split: no      # yes if this spec should split into siblings before /spec-implement
blockers: []             # list of OQ-N ids from §9 that must resolve before /spec-implement
depends_on: []            # list of sibling spec ids that must be archived before /spec-implement
expect_actual_touched: no # yes if §4 touches any commonMain `expect` declaration → §5a expect/actual parity rule is load-bearing, not boilerplate
localization_touched: no  # yes if §4 touches any user-facing string shown by both Compose UI and native iOS code → §5a dual-localization rule is load-bearing
branch_suggested:             # optional; author's suggested branch name. /spec-handoff confirms or overrides — it owns the final branch decision (falls back to branch.naming[type] if empty)
---

# Proposal: <title>

<!--
HOW TO FILL THIS TEMPLATE
- Replace every <placeholder> and "TBD". A field that is genuinely not
  applicable must be set to "N/A" with a one-line reason.
- §5 is the architectural gauntlet. /spec-implement refuses to start unless
  every item in §5a has an explicit answer AND §5b is satisfied (either by
  the Confinement claim with justification, or by explicit answers to all
  three rules).
- §9 (Open questions) — each OQ gets an ID (OQ-1, OQ-2, ...). OQs
  marked **(BLOCKER)** must also be listed in frontmatter `blockers:`
  and prevent /spec-implement from starting until resolved.
- Cross-spec dependencies (this spec needs another spec to land first)
  go in frontmatter `depends_on:`, NOT as an OQ.
- Inherited assumptions — if the source states assumptions, list
  each in the "Inherited assumptions" section below with an explicit
  stance (accept / verify-in-Step-N / reject).
- Keep prose tight. If a section needs more than one paragraph, the spec
  is probably too big — split it.

SPLIT CHECK (set frontmatter `recommend_split: yes` if any one is true):
- Title and source body describe different problems.
- Affected-areas table (§4) crosses `plan.split_thresholds.modules`
  (default 3) of this repo's capability areas (domain, data, features,
  core, di, components, device, validator — see `.specs/config.json`
  `architecture.modules`) with distinct concerns — not 2+ files within
  ONE area.
- Risks list has 4+ entries.
- Source has 5+ AC scenarios that group into distinct subsystems.

If `recommend_split: yes`, fill the Split recommendation section below.
Otherwise, delete that section entirely.
-->

## Split recommendation

<!--
Only fill this section when `recommend_split: yes` in frontmatter.
Otherwise delete the heading and body — do not leave it empty.
-->

**Reason for split**: <one paragraph>

**Proposed splits**:
- `<slug-a>` — <one-line scope>
- `<slug-b>` — <one-line scope>

## Inherited assumptions

<!--
List every assumption stated in the source. For each, declare a stance:

  accept           — we believe this assumption holds; no extra work.
  verify-in-Step-N — we'll confirm during plan.md Step N (must be an
                    [investigate] step).
  reject           — we believe this assumption is wrong; explain why.

If the source has no assumptions, delete this entire section.
-->

- _<assumption verbatim from source>_ — **accept** / **verify-in-Step-N** / **reject** — <one-line justification if not "accept">

## 1. Source

<!-- One paragraph. Link the file/URL, or summarize the manual intake. Pulled from story.md. -->

## 2. Problem / Why

<!-- What user-facing or technical behavior is wrong (bug) or missing (story). Why now. -->

## 3. Scope

<!--
If `recommend_split: yes`, scope is contingent on the split being
accepted: describe the canonical child's intended scope here with
"(under proposed split)" qualifiers. If `recommend_split: no`, fill
normally.

An explicit override of a `recommend_split: yes` recommendation also
lives here — a sentence naming the recommendation and the rationale for
proceeding without splitting (this is what /spec-implement pre-flight
looks for).
-->

**In scope**
-

**Out of scope**
-

## 4. Affected areas

<!--
Cross-reference the seeded Serena memories (`.serena/memories/` at the
repo root, especially `mem:architecture`) and root `CLAUDE.md`. List
every capability area touched (domain, data, features/<feature>, core,
di, components, device, validator — see `.specs/config.json`
`architecture.modules`) plus which platform source sets are involved
(commonMain / androidMain / iosMain / jvmMain / iosApp Swift).

"Resolved by" names the plan.md step that will settle a TBD entry: an
area whose home is decided by an [investigate] step writes that step
ID (e.g. "Step 1") in the column and "TBD" in Area. Leave "Resolved
by" blank for areas already known.
-->

| Area | Source set(s) | Class(es) / file(s) touched | Change type | Resolved by | Notes |
|--------|--------------|-------------------------------|-------------|-------------|-------|
|        |              |                                |             |             |       |

## 5. Architectural gauntlet (this repo's hard rules)

<!--
The gauntlet has two parts. /spec-implement refuses to start unless
BOTH §5a (every box ticked, explicit answer) AND §5b are satisfied.

§5b allows a "Confinement claim" shortcut: if the change is confined to
a single feature's UI/ViewModel with no new domain interface, no new
expect/actual, and no touched shared string, then §5b items can
collapse to a single "Confined — see §4" answer. This shortcut is NEVER
allowed for §5a.
-->

### 5a. Always explicit (no shortcut)

- [ ] **Expect/actual parity** — every commonMain `expect` declaration
      added or changed has a matching `actual` in every source set that
      needs it (`androidMain`, `iosMain`, and `jvmMain` if the desktop
      target is affected). If `expect_actual_touched: no` in
      frontmatter, this is N/A — confirm no new/changed `expect` slipped
      in during investigation.
      Approach: <confirmed / explain>
- [ ] **Dual localization** — any user-facing string touched that is
      shown by both the Compose UI and native iOS code (notifications,
      widgets) is updated in BOTH
      `composeApp/src/commonMain/composeResources/values[-es]/*.xml`
      AND `iosApp/en.strings` + `iosApp/es.strings`. If
      `localization_touched: no`, this is N/A.
      Approach: <confirmed / explain>
- [ ] **Secrets & logging** — no API keys (the WeatherAPI key) or other
      credentials logged, printed, included in error messages, or
      hardcoded outside the existing config location.
      Approach: <confirmed / explain>
- [ ] **No automated tests exist** — this repo has ZERO test source
      sets (see `CLAUDE.md` and `.specs/config.json`
      `verification.no_test_suite`). The verification plan (§8,
      plan.md) must never assume or invent a test-suite run —
      build-green (+ a manual run in the emulator/simulator for
      UI-visible changes) is the entire safety net.
      Acknowledged: <yes>

### 5b. Confinement-conditional

<!--
If the change is confined to a single feature's UI/ViewModel layer with
no new domain interface, no touched expect/actual, and no shared
string (per the comment at the top of §5), tick "Confinement claim"
below and write a one-line justification — the two rules below
auto-resolve to "N/A — confined". Otherwise, leave Confinement claim
unticked and answer each rule explicitly.
-->

- [ ] **Confinement claim** — change is confined to <list area/path
      categories from §4>. Neither rule below is reachable.

If Confinement claim is unticked, answer each:

- [ ] **Domain/data boundary (DIP)** — new or changed data access goes
      through a `domain/repository/*` interface implemented under
      `data/repository/<feature>/*Impl`; a ViewModel never references a
      `data/*Impl` class directly (see `mem:architecture`).
      Approach: <describe or N/A>
- [ ] **Result-type error handling** — repository/data-layer functions
      that can fail return the sealed `core/result/Result` type rather
      than throwing; ViewModels convert failures into sealed screen
      states (see `mem:architecture`, `mem:conventions`).
      Approach: <describe or N/A>

## 6. Skills

<!--
ONE LINE. Names only — no per-skill rationale here. Pick from:
  direct edits | /new-feature

Use `/new-feature` when this spec introduces a brand-new screen/capability
(a new package under `features/`, not a change to an existing one) —
see `/spec-plan`'s Decision rules for the exact trigger. Everything else
is "direct edits".

For multiple skills, separate by:
  ", "  parallel or independent
  " → " strict ordering
-->

**Skills**: <choice — usually "direct edits">

## 7. Risks

<!--
Consider: a shared component or ViewModel used by multiple features
changing behavior for all of them; a Koin binding change breaking
injection at a call site not touched directly; an expect/actual drift
shipping a build-green-on-one-platform-but-broken-on-another change; UI
regressions with no automated test to catch them. One bullet per risk;
no hand-waving.

Risk count is a split smell: 4+ risks is one of the SPLIT CHECK triggers
at the top of this file.
-->

-

## Out-of-band actions

<!--
Proposal-level summary of anything that needs a human, runs outside
Claude's reach, or happens after /spec-handoff: a manual check on a
physical iOS/Android device, an App Store / Play Store metadata update,
a WeatherAPI plan/quota change, etc. Write "None." if there are none.
-->

-

## 8. Acceptance criteria

<!--
Concrete and verifiable. Tied back to the source where possible. Since
this repo has no automated tests, phrase ACs so they're checkable by a
build (`./gradlew :composeApp:assembleDebug` / `./gradlew build`) or a
manual run in the Android emulator / iOS simulator — never "covered by
unit test X".

AC↔scenario mapping: when the source has numbered Gherkin Scenarios,
prefix each criterion with `**AC-N (Scenario N)** —`. Otherwise, plain
`**AC-N** —` is enough.
-->

- [ ] **AC-1** —
- [ ] **AC-2** —
- [ ] **AC-3** —

## 9. Open questions

<!--
Each OQ gets an ID (OQ-1, OQ-2, ...) so it can be referenced from
frontmatter `blockers:` and from plan.md step text.

An OQ marked **(BLOCKER)** must also be listed in frontmatter
`blockers:`.
-->

- **OQ-1** **(BLOCKER)** — <question>
- **OQ-2** — <question>

---

## Serena memories consulted

<!--
Recorded by /spec-plan after reading the relevant memories under
`.serena/memories/` at the repo root. Append-only; do not edit during
/spec-implement.
-->

-
-

<!-- Branch is no longer a footer here. The author's optional suggestion
     lives in frontmatter `branch_suggested:`; /spec-handoff confirms or
     overrides it (and falls back to branch.naming[type] when empty). -->
