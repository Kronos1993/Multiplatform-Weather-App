# Inter-skill handoff contracts (spec-* chain)

The spec-driven workflow is a chain of skills, each consuming what the
previous one produced. Every transition is an implicit contract: the
**producer** guarantees some artifact state on success, and the
**consumer** gates its entry on that state. This file anchors both
halves so a change to one skill's output is checked against what the
next skill requires.

This is the **internal** twin of `.specs/EXTERNAL_SKILLS.md` (which
pins the `/commit`, `/create-pr`, `/loop` contracts). It is symmetric
with two other anchors:

- `.specs/EXTERNAL_SKILLS.md` — contracts with skills *outside* the
  spec chain.
- `.specs/handoff-schema.md` — the `handoff.json` field contract (the
  artifact several of these handoffs carry).

Each spec-* skill carries a one-line pointer back to the relevant
handoff row here (`Handoff contract: .specs/HANDOFFS.md#<anchor>`), at
its Preconditions (consumer side) and at its outputs / "What this skill
does NOT do" (producer side).

Adapted from a Smartmatic-internal `.specs/` workflow lineage; the
chain shape is stack-agnostic. That lineage's C#-installer-engine gate
(Execute()/Undo() symmetry + reflection assembly/type consistency) does
not apply here — this repo is a Kotlin Multiplatform Clean Architecture
app. It is replaced below with this repo's own architectural gate:
**expect/actual parity** plus **dual-localization consistency** (see
`.specs/config.json` `architecture.*`).

---

## The chain

```
/spec-new → /spec-plan → /spec-implement → [/spec-review] → /spec-handoff → /spec-finalize ⟲
                                         └→ [/spec-review] → /spec-ship  (one-shot alternative)
```

`/spec-review` is optional and read-only (pure reporter; never advances
state). `/spec-ship` is a one-shot alternative that fuses
`/spec-handoff` + `/spec-finalize` (commit + impl PR + memory PR +
pre-merge archive). `/spec-status` is read-only and sits outside the
chain — it consumes proposal frontmatter, tasks.md checkbox counts, and
`handoff.json`, and produces nothing.

The artifact contract is layered: **story.md** (frozen after intake) →
**proposal.md §1–§8** (frozen after planning) → **tasks.md checkboxes**
(mutated during implement) → **folder moved** (by finalize/ship). Full
immutability model: `.specs/IMMUTABILITY.md`.

## Quick reference

| # | Producer → Consumer | Producer guarantees | Consumer requires |
|---|---|---|---|
| H1 | `/spec-new` → `/spec-plan` | `specs/<KEY>/story.md` with `source` / `source_ref` set | story.md exists + user-reviewed; no `proposal.md` yet |
| H2 | `/spec-plan` → `/spec-implement` | `proposal.md` + `plan.md` + `tasks.md`; frontmatter `blockers` / `depends_on` / `recommend_split` set; checkboxes unchecked | blockers empty; depends_on archived; no `_(skeleton)_` steps; split resolved; a `verification.*` mode chosen |
| H3 | `/spec-implement` → `/spec-review` *(opt)* | Impl + Pre-handoff checkboxes ticked; working tree dirty | same + Serena active |
| H4 | `/spec-implement` (or `/spec-review`) → `/spec-handoff` | Impl + Pre-handoff ticked; dirty tree; in-scope edits only | dirty tree; on `develop` or a `branch.naming.<type>` branch; every touched KMP `expect` has a matching `actual` in every affected source set (architectural gate — see `.specs/config.json` `architecture.expect_actual_parity_required`); any touched user-facing string is updated in both Compose resources and iOS `.strings` (`architecture.dual_localization_required`) |
| H5 | `/spec-handoff` → `/spec-finalize` | One conventional commit on the impl branch; **no** `handoff.json` | commit(s) on impl branch; `gh` authed |
| H6 | `/spec-finalize` PR-open → `/spec-finalize` status-check | `handoff.json` written with `pr_number` + impl branch pushed | `handoff.json` present with a readable `pr_number`; `origin/develop` reachable |
| H7 | `/spec-implement` (or `/spec-review`) → `/spec-ship` | (consumes H4 state) | H4 requirements + `gh` authed + `origin/develop` reachable |

---

## H1 — `/spec-new` → `/spec-plan`

**Producer (`/spec-new`) guarantees:**

- `specs/<KEY>/story.md` exists, populated from the template, with
  frontmatter `source`, `source_ref`, `fetched_at`, `fetched_by` set.
- No `proposal.md` / `plan.md` / `tasks.md` — `/spec-new` never plans.
- If a source-text inconsistency was detected, a
  `## Source-text inconsistency` footer is appended for `/spec-plan`
  to lift into Open Questions.

**Consumer (`/spec-plan`) requires:**

- `specs/<spec_id>/story.md` exists and has been reviewed by the user.
- First-run mode keys on `proposal.md` **absent**.

**Breaks if:** `/spec-new` stops writing `story.md` frontmatter
`source`/`source_ref`, or starts emitting `proposal.md`.

## H2 — `/spec-plan` → `/spec-implement`

**Producer (`/spec-plan`) guarantees:**

- `proposal.md` (§1–§9), `plan.md` (kind-tagged steps with "Files /
  symbols"), `tasks.md` (Implementation + Pre-handoff checkboxes, all
  **unchecked**) all exist.
- Frontmatter set: `type`, `priority`, `recommend_split` (yes/no),
  `blockers` (OQ-ID list), `depends_on` (spec-ID list).
- `plan.md` / `tasks.md` carry the `Blockers:` / `Depends on:` banner
  rendered from frontmatter.

**Consumer (`/spec-implement`) requires** (refuses to start otherwise):

- All three files present.
- `blockers:` is an empty list.
- `depends_on:` empty, or every ID has a `specs/_archive/<ID>/` folder.
- No `plan.md` step still tagged `_(skeleton)_`.
- `recommend_split: no`, or `yes` with an explicit §3 Scope override.
- A verification mode is selected (one of `verification.*` true, or
  `verification.skip_all_local`). Since `verification.no_test_suite` is
  always `true` in this repo, no step may be tagged as a test-suite run.

**Breaks if:** `/spec-plan` stops rendering the banner from frontmatter,
drops the `_(skeleton)_` marker convention, or renames gating frontmatter fields.

## H3 — `/spec-implement` → `/spec-review` *(optional)*

**Producer (`/spec-implement`) guarantees:**

- Code edits confined to `plan.md` "Files / symbols" scope.
- `tasks.md` Implementation **and** Pre-handoff checkboxes ticked.
- proposal §4 / §9 refined with investigation findings; `decisions.md`
  appended if any non-obvious choice arose.
- **Zero commits** — the working tree is left dirty (commit model:
  `/spec-handoff` makes the single commit).

**Consumer (`/spec-review`) requires:**

- All three spec files present; Implementation + Pre-handoff ticked.
- Working tree dirty (`git status --porcelain` non-empty).
- Serena project active.

**Producer (`/spec-review`) guarantees** (it is also a producer into
H4): writes `specs/<spec_id>/.review-findings.md` (gitignored), exits 0
always, edits no code, does **not** advance `status:`.

**Breaks if:** the commit model changes so `/spec-implement` commits,
or Pre-handoff ticking moves out of `/spec-implement`.

## H4 — `/spec-implement` (or `/spec-review`) → `/spec-handoff`

**Producer guarantees:** as H3 producer — dirty tree, all Implementation
+ Pre-handoff checkboxes ticked, edits in-scope, no commits.

**Consumer (`/spec-handoff`) requires:**

- All three files present; Implementation + Pre-handoff ticked.
- Working tree dirty.
- Current branch is `develop` (it will branch off per
  `branch.naming.<type>`) or already a matching branch.
- **Expect/actual parity gate**: for every KMP `expect` declaration
  touched by the change, a matching `actual` exists in every source set
  that needs it (`androidMain`, `iosMain`, and `jvmMain` if the desktop
  target is affected).
- **Dual-localization gate**: any user-facing string touched that is
  shown by both the Compose UI and native iOS code is updated in both
  `composeApp/src/commonMain/composeResources/values[-es]/*.xml` and
  `iosApp/en.strings` + `iosApp/es.strings`.

**Producer (`/spec-handoff`) guarantees** (into H5):

- Creates/switches to the `branch.naming.<type>` branch.
- Stages only `plan.md` "Files / symbols" scope, plus the always-in
  scope `specs/<spec_id>/**`.
- One conventional commit via `/commit` on that branch.
- Ticks tasks.md Handoff rows `Branch created` + `/commit executed`;
  leaves `Branch pushed` / `PR opened` / `Spec folder archived` /
  `Reusable-knowledge candidates surfaced` unchecked, annotated
  `*(see /spec-finalize)*`.
- Does **not** push, open a PR, write `handoff.json`, or archive.

**Breaks if:** the branch-naming convention key (`branch.naming.<type>`)
is renamed, or `/spec-handoff` starts writing `handoff.json`.

## H5 — `/spec-handoff` → `/spec-finalize` (PR-open mode)

**Producer (`/spec-handoff`) guarantees:** one (or more) local commit on
the impl branch; **no** `handoff.json` present.

**Consumer (`/spec-finalize`, PR-open mode) requires:**

- Commit(s) on the impl branch (a branch ahead of `develop`).
- `handoff.json` **absent** — this is the mode discriminator.
- `gh` on PATH and authenticated.

**Producer (`/spec-finalize` PR-open) guarantees** (into H6):

- Pushes the impl branch and opens the impl PR on `develop`.
- Writes `specs/<spec_id>/handoff.json` per
  `.specs/handoff-schema.md` — `pr_number` / `pr_url` / `opened_*`
  populated; `archive_*` null; `memory_*` populated iff ≥1
  reusable-knowledge candidate was saved.

**Breaks if:** the `handoff.json`-presence mode discriminator changes,
or `handoff.json` stops being written with a parseable `pr_number`.

## H6 — `/spec-finalize` (PR-open) → `/spec-finalize` (status-check)

A self re-entry, often driven by `/loop`.

**Producer guarantees:** `handoff.json` present with `pr_number` +
`pr_url`; impl branch pushed.

**Consumer (status-check mode) requires:**

- `handoff.json` present with a readable `pr_number`.
- `origin/develop` reachable for the archive flow.

**Consumer guarantees** (terminal output): on `merged` it runs the
archive flow — `git mv` to `specs/_archive/<KEY>/`, writes the moved
`handoff.json` with `archived_at` / `archive_branch` into the archive
commit, then records `archive_pr_*` in a follow-up commit the archive
PR carries.

**Breaks if:** the `handoff.json` schema drops `pr_number`/`pr_url`, or
the archive-PR sequencing invariant changes.

## H7 — `/spec-implement` (or `/spec-review`) → `/spec-ship`

`/spec-ship` is a one-shot alternative to H4 + H5 + H6: it does the
commit, impl PR, optional memory PR, and **pre-merge** archive in a
single run.

**Producer guarantees:** identical to the H4 producer state (dirty tree,
all checkboxes ticked, in-scope edits, no commits).

**Consumer (`/spec-ship`) requires:** the full H4 consumer set **plus**
`gh` authed and `origin/develop` reachable.

**Consumer guarantees:**

- One commit + impl PR on `develop`.
- `handoff.json` written and committed **on the impl branch** with
  `archived_pre_merge: true`, `shipped_via: "spec-ship"`, `archive_*`
  null.
- Memory PR opened iff memories saved.
- Pre-merge archive on `chore/archive-<KEY>`; archive PR opened unless
  `--no-archive-pr`. Returns to the impl branch.

**Breaks if:** the H4 producer contract changes, or the
`archived_pre_merge` marker is dropped.

---

## Reader: `/spec-status` (no outbound handoff)

`/spec-status` is read-only and produces no artifact. As a *consumer*
it depends on:

- proposal.md frontmatter (`type`, `priority`, `status`,
  `recommend_split`, `blockers`, `depends_on`),
- tasks.md Implementation / Pre-handoff checkbox counts,
- `handoff.json` presence + fields,
- folder location (`specs/<KEY>/` vs `specs/_archive/<KEY>/`).

## Maintenance

- Update this file together with the relevant spec skill when an
  entry-precondition or completion-output changes.
- Out of scope: the contracts with skills *outside* the spec chain
  (`/commit`, `/create-pr`, `/loop`) — those live in
  `.specs/EXTERNAL_SKILLS.md`.
- This workflow has no sibling spec/proposal system in this repo — it
  is the only one installed here.
