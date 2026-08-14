# The four-stage immutability contract

The spec-driven workflow protects its artifacts with a staged
immutability gradient: each artifact freezes at a defined point and is
read-only (or append-only) thereafter, so a later skill can trust what
an earlier one produced. This file states the model once so every
skill inherits it rather than re-deriving it.

Adapted from a Smartmatic-internal `.specs/` workflow lineage — the
model itself is stack-agnostic.

Sibling anchors:

- `.specs/HANDOFFS.md` — the per-transition producer/consumer contracts.
- `.specs/handoff-schema.md` — the `handoff.json` field contract.
- `.specs/EXTERNAL_SKILLS.md` — contracts with skills outside the chain.

> Append-only ≠ mutable. "Append-only" means existing content is frozen
> but new content may be added at the end (a footer, a dated subsection,
> a new log line). It never licenses editing or deleting what is already
> there.

---

## The gradient

```
/spec-new        /spec-plan         /spec-implement        /spec-finalize | /spec-ship
   │                 │                    │                       │
   ▼                 ▼                    ▼                       ▼
 story.md         proposal §1–§8       tasks.md checkboxes      folder moved
 FROZEN           + plan/tasks text    flip; decisions.md       to _archive/,
 (append-only)    FROZEN               appends                  spec docs SEALED
```

Four stages, each tightening what may change:

### Stage 1 — Intake frozen (after `/spec-new`)

- **`story.md` is raw intake — frozen, append-only.** Its template
  header says `RAW INTAKE — DO NOT EDIT AFTER FETCH`. It captures the
  source (file / manual description / URL) verbatim so the proposal can
  always be checked against the original.
- **Allowed append:** `/spec-new` itself may add a
  `## Source-text inconsistency` footer when it detects one, for
  `/spec-plan` to lift into Open Questions. Nothing edits the fetched
  body.
- **Enforced by:** `/spec-plan` and `/spec-implement` both declare
  "does not modify `story.md`".

### Stage 2 — Plan sealed (after `/spec-plan` first run)

- **`proposal.md §1–§8` is spec authority — frozen.** These sections
  define what the spec *is*; downstream skills read them, never rewrite
  them.
- **`plan.md` step text and `tasks.md` step text/numbering are frozen.**
  If the plan changes, re-run `/spec-plan` — do not patch by hand.
- **User-managed, not skill-edited:** the frontmatter gates `blockers:`,
  `depends_on:`, `recommend_split:`. `/spec-plan` re-run mode only
  re-renders the `plan.md` / `tasks.md` banner from frontmatter.
- **Carve-out:** `/spec-implement` may refine **`proposal §4`**
  (Affected areas table) and **`proposal §9`** (Open Question
  resolutions) when later work surfaces an inaccuracy in an earlier
  step's output. Refinements made outside an `[investigate]` step are
  recorded as a **dated subsection**. §1–§3, §5–§8 stay frozen.
- **Enforced by:** `/spec-implement` ("does not modify proposal.md
  §1–§8 — those are spec authority", "does not modify proposal.md
  frontmatter"); `/spec-plan` ("in re-run mode: does not rewrite
  `proposal.md` body").

### Stage 3 — Implementation mutates state, not text (during `/spec-implement`)

- **What changes:** `tasks.md` **checkbox state** (Implementation +
  Pre-handoff boxes flip as steps complete), the §4/§9 carve-out from
  Stage 2, and code files within the plan's "Files / symbols" scope.
- **`decisions.md` is append-only**, created on the first non-obvious
  choice and absent if none arise.
- **What does not change:** step text, numbering, §1–§8, frontmatter,
  `story.md`. `/spec-review` (optional) modifies **nothing** — it is a
  pure reporter writing only its gitignored `.review-findings.md`.
- **Handoff checkboxes** in `tasks.md` are flipped later by
  `/spec-handoff` / `/spec-ship`, not by `/spec-implement`.

### Stage 4 — Folder sealed and moved (by `/spec-finalize` or `/spec-ship`)

- **`proposal.md` / `plan.md` / `tasks.md` are sealed** — finalize/ship
  do not modify them; `/spec-ship` "only ticks" the remaining Handoff
  checkboxes.
- **The folder moves:** `git mv specs/<KEY>/ → specs/_archive/<KEY>/`
  (post-merge for `/spec-finalize`, pre-merge for `/spec-ship`).
- **Only new artifact:** `handoff.json` (created at finalize/ship,
  carrying PR + archive metadata). After this stage the active-folder
  discovery in other skills no longer matches the spec.

---

## Enforcement summary

| Artifact | Frozen after | May still change | Who may change it |
|---|---|---|---|
| `story.md` | `/spec-new` | append a `## Source-text inconsistency` footer | `/spec-new` only |
| `proposal.md` §1–§3, §5–§8 | `/spec-plan` first run | nothing | — |
| `proposal.md` §4, §9 | `/spec-plan` first run | refinements (dated subsection if outside `[investigate]`) | `/spec-implement` |
| `proposal.md` frontmatter gates | `/spec-plan` first run | `blockers` / `depends_on` / `recommend_split` (by hand); banner re-render | user; `/spec-plan` re-run (banner only) |
| `plan.md` step text | `/spec-plan` first run | re-run `/spec-plan` to regenerate; banner re-render | `/spec-plan` |
| `tasks.md` step text / numbering | `/spec-plan` first run | nothing (re-run `/spec-plan` instead) | — |
| `tasks.md` checkbox state | — | Implementation/Pre-handoff boxes | `/spec-implement`; Handoff boxes by `/spec-handoff` / `/spec-ship` |
| `decisions.md` | — (append-only) | append a new entry | `/spec-implement` |
| `proposal` / `plan` / `tasks` (as a set) | Stage 4 archive | sealed; folder moved to `_archive/` | `/spec-finalize` / `/spec-ship` |
| `handoff.json` | created in Stage 4 | per `.specs/handoff-schema.md` | `/spec-finalize` / `/spec-ship` |

## Why it is staged, not all-or-nothing

Each freeze point is the boundary where a downstream skill starts to
*depend* on the artifact:

- `story.md` freezes at intake so the proposal is always auditable
  against the unedited source.
- The proposal/plan freeze at planning so implement, review, handoff,
  and status all read one stable definition.
- tasks.md text freezes but its checkboxes must move — that is the
  workflow's progress signal, which `/spec-status` reads.
- The folder seals at archive because the work is done; `handoff.json`
  is the only thing that still has news to record.

## Maintenance

- When a skill's "What this skill does NOT do" list changes an
  immutability rule, update this file and the enforcement table in the
  same change.
