---
name: spec-plan
description: Use after /spec-new has produced story.md and the user has reviewed it. Drafts proposal.md, plan.md, and tasks.md for the spec by gathering the project knowledge relevant to the task type, running the architectural gauntlet, applying the split heuristic, surfacing inherited assumptions, and writing all three files at once with a summary of key decisions for the user to review. Re-running /spec-plan refreshes the banners in plan.md and tasks.md from proposal.md frontmatter (e.g. after a blocker is resolved or a dependency archives); it does NOT rewrite proposal.md unless proposal.md is deleted first. Spec folder layout, template fields, and gauntlet rules are documented in `.specs/templates/`, `.specs/IMPROVEMENTS.md`, and this repo's root CLAUDE.md.
metadata:
  claude_md_requires: '[]'
---

## Usage

```
/spec-plan [<spec_id>]
```

- With `<spec_id>` (e.g. `add-rain-radar-toggle`): plans that spec.
- Without: selects the candidate per `.specs/SPEC_DISCOVERY.md` —
  predicate: most recent `story.md` whose folder has no `proposal.md`.

## Preconditions

- `specs/<spec_id>/story.md` exists.
- The user has reviewed `story.md` and accepts it as faithful intake.
- For first-run mode: `proposal.md` does NOT exist in the folder.
- For re-run mode: `proposal.md` exists; only banners are refreshed.

## What this skill does (first-run mode)

1. Read `.specs/config.json` for `paths`, `plan.split_thresholds`, and `architecture.*`.
2. Read `specs/<spec_id>/story.md`.
3. Detect the **task type** (bug / story / refactor / chore) from the
   story body — see Decision rules.
4. Identify which **capability areas** the spec likely touches and
   read the seeded Serena memories + root `CLAUDE.md` — see Knowledge
   selection.
5. **Brief code investigation** when the spec touches a known
   area/class — one or two symbol-lookup / Glob calls to populate
   the Affected areas table with concrete paths. Stay shallow;
   deeper investigation belongs in `[investigate]` plan steps.
6. **Detect inherited assumptions** — if `story.md` has an Assumptions
   section, list each one for the proposal's "Inherited assumptions"
   section with a default stance.
7. **Apply the split heuristic** — set `recommend_split: yes` in
   frontmatter and draft the Split-recommendation section if any
   trigger fires (see Decision rules). Otherwise leave `no` and skip
   the section.
8. **Run the architectural gauntlet** — auto-resolve §5a and §5b items
   per Decision rules; flag anything ambiguous as an open question.
9. **Identify open questions and blockers** — anything the source did
   not answer becomes an OQ-N. OQs that gate implementation get
   **(BLOCKER)** and mirror into frontmatter `blockers:`.
10. **Identify cross-spec dependencies** — populate frontmatter
    `depends_on:` with sibling spec IDs the spec needs landed first.
11. **Draft `proposal.md`** from `.specs/templates/proposal.md`. Fill
    the unnumbered "## Out-of-band actions" section with the
    reviewer-facing digest of anything human/external/post-handoff;
    "None." when there are none.
12. **Draft `plan.md`** from `.specs/templates/plan.md`. The banner at
    top renders `Blockers:` and `Depends on:` from frontmatter.
13. **Draft `tasks.md`** from `.specs/templates/tasks.md`. Same banner.
14. **Print summary** of key decisions for the user to review (see Output).

## What this skill does (re-run mode)

If `proposal.md` already exists, the skill switches to **banner refresh**
mode:

1. Read `proposal.md` frontmatter.
2. **Sanity-check `blockers:` against §9.** Cross-check the frontmatter
   `blockers:` list against the §9 OQ markers: every ID in `blockers:`
   must have a matching `**OQ-N** **(BLOCKER)**` in §9, and every §9
   `(BLOCKER)` marker must appear in `blockers:`. On any divergence,
   STOP and surface the specific mismatch instead of rendering a banner
   from stale frontmatter.
3. Regenerate the banner blockquote at the top of `plan.md` and
   `tasks.md` to match current `blockers:` and `depends_on:`.
4. Do NOT rewrite `proposal.md`, the body of `plan.md`, or the body of
   `tasks.md`. Human edits to those are preserved.
5. Print "Banners refreshed for `<spec_id>`. Blockers: <list>. Depends
   on: <list>."

To regenerate everything from scratch: delete `proposal.md` (and
optionally `plan.md`, `tasks.md`) and re-run.

## Decision rules

### Type detection

| Signal in story.md | Type |
|---|---|
| "doesn't work", "crashes", "wrong value", "expected X got Y" | **bug** |
| "add", "as a user I want", explicit acceptance criteria for new behavior | **story** |
| Title or body emphasizes "rename", "extract", "clean up", "migrate to" | **refactor** |
| "bump dependency", "update Gradle/Kotlin/Compose version", "remove unused" | **chore** |
| Multiple signals → take the strongest one and record reasoning | mixed |

If undetermined, ask the user once.

### Knowledge selection

This repo has a **single** Serena project — all memories live under
`.serena/memories/` at the repo root. There is no per-module routing
index; read the whole `.serena/memories/` folder listing (it's small)
and skim titles for anything relevant, plus root `CLAUDE.md`. The
keyword table below is a starting hint — always confirm against the
actual folder listing, since new memories get added over time.

| Keyword in story | Likely area(s) |
|---|---|
| "weather", "forecast", "current conditions" | `domain/model`, `data/repository/weather` |
| "location", "GPS", "current position" | `data/repository/location`, `device/screen_config`, `core/preferences` |
| "radar", "rain", "map layer" | `data/repository/radar`, `components/maps` |
| "alert", "warning" | `data/repository/alerts`, `domain/model/alerts` |
| "notification", "suggestion" | `core/notification`, iOS `core/job/`, `iosApp/*.swift` |
| "widget" | `androidMain` `widget/` (Glance) |
| "custom location", "saved city", "add city" | `data/repository/user_custom_location`, `features/add_city` |
| "setting", "preference" | `core/preferences`, `features/home/setting` |
| "database", "cache", "Room" | `data/local` |
| "API", "Ktor", "network" | `data/remote` |
| "DI", "Koin", "injection" | `di/`, `core/di` |
| "string", "translation", "Spanish", "localization" | Compose `composeResources` AND `iosApp/*.strings` — see `architecture.dual_localization_required` |
| "iOS", "Swift", "SwiftUI" | `iosMain`, `iosApp/iosApp/` |
| "Android", "widget", "notification channel" | `androidMain` |

If the story touches something with no obviously-relevant memory,
`list_memories()` directly and skim titles — don't assume the table
above is exhaustive.

### Code investigation depth

Planning-phase investigation is intentionally shallow.

| Spec shape | Depth |
|---|---|
| Bug, single feature/class known | One symbol lookup for the symbol, one Glob for sibling features if the concept is duplicated. Done. |
| Story, cross-cutting (presence check) | Two-three symbol lookups + a Grep for the new concept. Stop at populating §4 with TBDs. |
| Story, cross-cutting (analysis) | Open-ended cross-file reasoning about how a new capability interacts with `core`/`di`. If too big to settle here, write TBDs and defer to an `[investigate]` plan step. |
| Refactor | Glob for the pattern being removed/renamed. One reference search (`find_referencing_symbols`) if the pattern is symbol-named. |
| Chore | None — usually self-contained (a version-catalog bump). |

Never go deeper than what's needed to write a credible §4. Detailed
file-level investigation belongs in `[investigate]` plan steps.

### Split heuristic

Set `recommend_split: yes` if any one of (thresholds from
`plan.split_thresholds` in `.specs/config.json`; defaults shown):

- The story.md footer flags a "Source-text inconsistency" — unconditional.
- Affected areas in §4 cross `split_thresholds.modules` (default 3)+
  **distinct capability areas** (not files/sub-folders of one area).
- Risk count in §7 reaches `split_thresholds.risks` (default 4)+.
- AC scenarios in §8 group naturally into `split_thresholds.subsystems`
  (default 2)+ independently-shippable subsystems.

When triggered, fill the Split-recommendation section with a reason
and 2-3 proposed sibling spec slugs with one-line scopes each.

If `recommend_split: no`, delete the Split-recommendation section
entirely from the generated proposal.

### Inherited assumptions

For each assumption in `story.md`'s Assumptions section, copy verbatim
into the proposal's "Inherited assumptions" section with a default
stance of **accept** plus a brief justification — unless clearly
violated by project knowledge, in which case **reject** with the
conflicting fact. If plausible but unverified, mark
**verify-in-Step-N** and add a matching question in plan.md Step N's
"Question(s) to answer" (Step N must be `[investigate]`).

If `story.md` has no Assumptions block, delete the section entirely.

### Architectural gauntlet automation

**§5a items — always tick with explicit reasoning:**

| Item | Auto-resolve rule |
|---|---|
| Expect/actual parity | If the change touches an existing `expect` declaration or adds a new one, list every source set that needs a matching `actual` (androidMain always; iosMain if the feature is iOS-reachable; jvmMain only if the desktop target is in scope) and confirm the plan covers all of them. If no `expect`/`actual` is touched, set `expect_actual_touched: no` and mark N/A. |
| Dual localization | If the change adds/edits a user-facing string reachable from both the Compose UI and native iOS code (notifications, widgets), confirm the plan updates both `composeResources` and `iosApp/*.strings`. If the string is Compose-UI-only, set `localization_touched: no` and mark N/A. |
| Secrets & logging | At plan time there is no diff to grep, so this records **intent, not verification**: confirm the spec does not intend to log the WeatherAPI key or any credential, and note it is re-verified against the actual diff by `/spec-implement`'s pre-handoff secrets-grep. |
| No automated tests exist | State explicitly (this is always true for this repo — see `.specs/config.json` `verification.no_test_suite`): the verification plan relies on build-green + a manual run on the affected platform, never a test-suite run. |

**§5b confinement claim — auto-tick if all of:**

- §4 areas are confined to a single feature's `features/<feature>/`
  package (ViewModel + Screen + Composables) with no new domain
  interface and no new repository.
- No new Koin binding registered.
- No cross-feature boundary crossed (no ViewModel directly referencing
  another feature's ViewModel or a `data/*Impl` class).

If any of those is false, untick Confinement claim and answer the
two sub-rules explicitly (DIP boundary, Result-type error handling).

### Affected areas

Populate §4 from a combination of:

- The seeded Serena memories (see Knowledge selection).
- Initial investigation (Glob, symbol lookup).
- Skills the spec will chain (per §6) — see "Choosing `/new-feature`
  vs direct edits" below.

For areas whose exact home is decided by a later `[investigate]`
step, fill the row with `(Resolved by Step N — see plan.md)`.

### Choosing `/new-feature` vs direct edits

Set proposal.md §6 to `/new-feature` (instead of "direct edits") when
**all** of these hold — otherwise use direct edits:

- `type: story` (a new capability, not a bug/refactor/chore).
- The story describes a **brand-new screen or capability** — a
  concept with no existing package under `features/` — not an addition
  to an existing feature's existing ViewModel/Screen.
- It's substantial enough to need its own ViewModel + Screen (a single
  new button on an existing screen is NOT this — that's direct edits
  on the existing feature).

When it applies:

1. Determine `--data`: `local` if the feature needs its own
   persisted state (check whether an existing repository already
   covers it — reuse before scaffolding new persistence); `remote` if
   it needs a new API-backed data source; `none` if it's pure UI over
   **existing** repositories.
2. Determine `--parent`: does the new package nest under an existing
   feature (like `current_weather`/`user_location`/`setting` all nest
   under `home/`), or is it a new top-level `features/<name>/` package?
   Ask the user if the story doesn't make this obvious.
3. Determine `--nav-route`: does this need its own reachable screen
   (`yes`, the common case) or is it embedded/opened from an existing
   screen (`no`)?
4. Write plan.md's Step 1 as a single `[implement]` step naming
   `/new-feature` with these args in "Skill args / inputs" (see the
   example in `.specs/templates/plan.md`). Do NOT also hand-author the
   files `/new-feature` scaffolds in other plan steps — later steps
   should only fill in the `// TODO`s the skill leaves (business logic,
   actual UI, actual queries), never duplicate its scaffolding.
5. Populate §4 Affected areas from `.claude/skills/new-feature/SKILL.md`'s
   file list for the chosen `--data`/`--nav-route` combination (Domain /
   Data / DI / Presentation / Navigation / Localization), not a guess.
6. Set §5a "Expect/actual parity" and "Dual localization" per whether
   the new feature touches either (usually `localization_touched: yes`
   at minimum, since every new screen needs string keys — see
   `/new-feature`'s own localization-stub step).

### Risks

Generate 1–N risks based on:

- The spec's surface area (a shared `core/` change ripples across every
  feature — call this out explicitly when `ParentViewModel`, `Result`,
  or a Koin module is touched).
- Known gotchas from the relevant Serena memories.
- Cross-spec dependency (note in §7 if any `depends_on` is non-empty).
- Third-party surface exposure: the WeatherAPI key/quota, GPS
  permission prompts (Moko permissions), MapLibre native framework
  behavior on iOS.

If the count reaches `plan.split_thresholds.risks` (default 4), the
split heuristic fires.

### Acceptance criteria

Distill AC into testable bullets, each observable in build or runtime
(a manual run in the Android emulator or iOS simulator/device) —
never "covered by a unit test", since none exist in this repo. End the
list with a build-verification entry
(`./gradlew :composeApp:assembleDebug` or `./gradlew build` green).

### Open questions and blockers

Add an OQ-N for every unresolved decision the source did not answer.
Mark **(BLOCKER)** if any of:

- Resolves a scope ambiguity that would change which areas are touched.
- Resolves an assumption set to verify-in-Step-N.
- Source-text inconsistency exists (always BLOCKER → OQ-1).

Mirror BLOCKER OQ IDs into frontmatter `blockers:`.

### depends_on

Populate from cross-spec references discovered during planning. Each
`depends_on` entry must reference a spec ID that exists somewhere in
`specs/` or `specs/_archive/`. If it doesn't yet exist, ask the user to
confirm the ID.

### Intake checklist (generates OQs / blockers)

Before finalizing §9, walk this checklist. Each unanswered item becomes
an OQ-N (mark **(BLOCKER)** where noted).

**1. Is a linked design doc or reference (if any) actually read?** If
the story references an external doc not already summarized in
`story.md` → OQ **(BLOCKER)**: "Fetch/read `<link>` and add its
substance to story.md, or confirm it's not load-bearing."

**2. Does this touch the WeatherAPI integration surface (rate limits,
response shape, the API key)?** If so, confirm the plan doesn't assume
undocumented API behavior — OQ (non-blocking) to verify against
WeatherAPI's actual docs/response during an `[investigate]` step if the
assumption is non-trivial.

**3. Does this touch both a domain `expect` and its platform
`actual`s?** Confirm the plan names every affected source set
explicitly (see the gauntlet above) — a plan step listing only
`commonMain` when `iosMain` also needs a change is incomplete.
Non-blocking unless the story implies iOS-specific behavior that isn't
addressed.

**4. For any user-facing text change, is the exact copy specified (not
just intent)?** "Tell the user the location couldn't be found"
describes intent; the actual string (in both languages, and in both
locations if dual-localized) is the deliverable. If only intent is
given → OQ, non-blocking unless the AC depends on exact wording.

**5. If this touches `core/` (ParentViewModel, Result, core/di,
core/preferences), are ALL consuming features identified?** `core/`
holds the cross-feature contracts every feature builds on. An
incomplete consumer list → OQ **(BLOCKER)**.

Summary of blocking conditions:

| # | Question | BLOCKER? | Resolved by |
|---|----------|----------|-------------|
| 1 | Is a linked design doc read? | Yes if scope changes | `[investigate]` step (or re-run `/spec-new`) |
| 2 | Is the WeatherAPI assumption verified? | No (unless AC depends on it) | `[investigate]` step |
| 3 | Are all affected source sets identified for expect/actual? | No (unless ambiguous) | `[investigate]` step |
| 4 | Is exact user-facing copy specified? | No (unless AC depends on it) | `[investigate]` step or direct answer |
| 5 | Are all `core/` consumers identified? | Yes | `[investigate]` step (`find_referencing_symbols`) |

## Output (three files)

After generation, write summary to the user:

```
spec_id:           <key>
type:              bug | story | refactor | chore
priority:          <from frontmatter>
recommend_split:   <yes|no>   (reasons listed if yes)
blockers:          [OQ-1, OQ-3]   or none
depends_on:        [<spec-id>, ...]   or none
affected areas:    <count> areas; <count> TBD pending [investigate] steps
skills:            <one-line from §6>
gauntlet:          5a all explicit; 5b confined / 2 rules explicit
acceptance:        <count> AC items
open questions:    <count> total, <count> blocking
memories consulted: <memory names>
```

Then point at the next action:

- If `blockers` is empty AND every `depends_on:` entry has a folder
  under `specs/_archive/<ID>/` (empty list also counts):
  `Next step: review the proposal, then /spec-implement.`
- Else:
  `Next step: resolve blockers / wait for unarchived dependencies.
   Re-run /spec-plan afterwards to refresh banners.`

## What this skill does NOT do

- Does not write any code under `composeApp/`/`iosApp/`.
- Does not commit, branch, push, or open PRs (`/spec-handoff`'s job).
- Does not chain into `/spec-implement` automatically.
- Does not contact any external tracker — this repo has none.
- Does not edit `story.md` (story is append-only after `/spec-new`).
- In re-run mode: does not rewrite `proposal.md` body.

## Failure modes and recovery

| Symptom | Cause | Recovery |
|---|---|---|
| `story.md` not found | Spec not initialized | Run `/spec-new <input>` first |
| `proposal.md` already exists, user wanted full regenerate | Forgot to delete first | Delete `proposal.md`, re-run |
| Banner refresh stops on a blockers/§9 mismatch | Frontmatter `blockers:` diverges from the §9 `(BLOCKER)` markers | Reconcile frontmatter `blockers:` with the §9 markers in `proposal.md`, then re-run |
| Architectural gauntlet has too many "ask user" flags | Story body too sparse to auto-resolve | Surface them all in §9 as OQs, let the user resolve |
| Split heuristic fires but user disagrees | Heuristic is conservative; judgment calls allowed | Set `recommend_split: no` manually in frontmatter, re-run banners; document reason in §3 Scope |

## Reference

- Templates: `.specs/templates/`.
- Config: `.specs/config.json`.
- Handoff contracts: `.specs/HANDOFFS.md` — H1 (consumes `story.md`),
  H2 (produces proposal/plan/tasks for `/spec-implement`).
- Config schema: `.specs/config.schema.md` — key catalog + per-skill
  reader map.
- Candidate discovery: `.specs/SPEC_DISCOVERY.md`.
- Backlog: `.specs/IMPROVEMENTS.md`.
