---
name: spec-status
description: Use whenever you need to know what's in flight, what's blocking, or what to work on next. Read-only inspector for the spec-driven workflow. Without arguments, lists every spec under specs/ (excluding _archive/) with its type, priority, current state, blockers, depends_on, and implementation progress. With <spec_id>, drills into a single spec showing every relevant field plus next-action pointer (run /spec-plan, /spec-implement, /spec-handoff, or /spec-finalize). Once a PR is open it reports PR_OPEN and lists the up-to-three PR URLs (impl, memory, archive). Pulls data from proposal.md frontmatter, tasks.md checkbox counts, handoff.json, and folder presence; never modifies any file.
metadata:
  claude_md_requires: '[]'
---

## Usage

```
/spec-status [<spec_id>]
```

- Without args: lists every active spec under the specs root
  (`paths.specs_root`, default `specs/`), excluding the archive root
  (`paths.archive_root`, default `specs/_archive/`).
- With `<spec_id>`: drill-in mode for one spec.

## What this skill does

This skill is read-only. It produces output to stdout/chat; it never
edits any file.

### List mode (no args)

1. Glob `<specs_root>/*/proposal.md`.
2. For each match, read frontmatter (`spec_id`, `title`, `type`,
   `priority`, `status`, `recommend_split`, `blockers`, `depends_on`).
   If the frontmatter fails to parse or is missing `spec_id`/`type`, do
   NOT drop the spec silently — record its folder path in a "skipped
   (unreadable proposal)" list and continue.
3. Read tasks.md: count Implementation checkboxes total / checked.
4. Check for `handoff.json` in the same folder (presence only for
   list mode, beyond `archived_pre_merge`).
5. Detect implicit state:
   - `INTAKE_PARSED` if no proposal.md (only story.md present).
   - `PR_OPEN` if `handoff.json` is present — takes precedence over
     the checkbox-derived states and over `BLOCKED` / `WAITING_DEPENDENCY`.
   - `PLANNED` if proposal.md exists but tasks.md has 0 ticked.
   - `IMPLEMENTING` if some ticked, not all.
   - `READY_FOR_HANDOFF` if all Implementation and Pre-handoff ticked.
   - `BLOCKED` overrides the three above when `blockers:` is non-empty.
   - `WAITING_DEPENDENCY` overrides the same three when `depends_on:`
     has entries not yet under `_archive/`.
6. Print a table sorted by `priority` (critical → high → normal → low),
   then by spec_id ascending.

### Drill-in mode (`<spec_id>` arg)

1. Resolve the spec folder. Try `<specs_root>/<spec_id>/` first; if
   absent, try `<archive_root>/<spec_id>/`. If found under the archive
   root, mark the spec as archived for the output below.
2. Read `<resolved>/proposal.md` frontmatter.
3. Read tasks.md and plan.md.
4. If `<resolved>/handoff.json` exists, read it (field contract:
   `.specs/handoff-schema.md`). Capture the impl PR (`pr_number` /
   `pr_url`), the memory PR, and the archive PR (any of the latter two
   may be `null`). Surface `archived_pre_merge: true` as a warning. For
   an archived spec, also capture `archive_reason` (`merged` /
   `abandoned`).
5. Print:
   - Identity: spec_id, title, type, priority, source, source_ref,
     created.
   - Status block: state, blockers, depends_on, recommend_split,
     skeleton-step count.
   - Progress: implementation X/Y, pre-handoff X/Y.
   - Affected areas count + TBD count.
   - Open questions count + blocking count.
   - Handoff: the up-to-three PR URLs (if `handoff.json` present) — `—`
     for any that is `null`.
   - Next action: a single line saying what to do next.
   - Frontmatter raw, last (small block).

### Pre-merge archive warning

When drill-in finds a spec under `_archive/` with
`archived_pre_merge: true` in `handoff.json`, surface this banner:

```
⚠ Archived pre-merge via /spec-ship.
  Impl PR #<pr_number> may not have merged yet — verify before
  treating this spec as fully landed.
  Recovery if impl PR closed without merge: see /spec-ship SKILL.md
  "Failure modes" → "Impl PR is later closed without merging".
```

Next-action line for this state:

```
Next action
  Verify impl PR #<pr_number> merged at <pr_url>.
  If merged: nothing to do (archive is correct).
  If closed unmerged: unarchive via the recipe in /spec-ship SKILL.md.
```

## State detection rules (precise)

Order matters — first match wins:

1. **No `proposal.md`** → `INTAKE_PARSED` — next action: `/spec-plan <key>`
2. **`handoff.json` present** (active folder) → `PR_OPEN` — next action:
   - If `archived_pre_merge: true`: verify impl PR merged.
   - Otherwise: run `/spec-finalize <key>` to poll merge state and archive.
   - If `archive_pr_number` set, the archive PR is also open; if
     `memory_pr_number` set, a memory PR is open too.
3. **`blockers:` non-empty** → `BLOCKED` — next action: resolve OQs
   listed in `blockers:`, then `/spec-plan <key>`.
4. **`depends_on:` non-empty AND any ID not in `_archive/`** →
   `WAITING_DEPENDENCY` — next action: wait for the ID(s) to archive,
   then `/spec-plan <key>`.
5. **`recommend_split: yes` AND the split is not honored** → `SPLIT_PENDING`
   — next action: confirm the split, then either (a) accept it (create
   sibling specs via `/spec-new`, archive this one) or (b) reject it
   (add an explicit override sentence to §3 Scope, re-run
   `/spec-plan <key>`).
6. **`tasks.md` Implementation 0 ticked / total > 0** → `PLANNED`
   — next action: `/spec-implement <key>`
7. **`tasks.md` Implementation partial** → `IMPLEMENTING` — next
   action: continue `/spec-implement <key>`
8. **All Implementation ticked, Pre-handoff partial** → `VERIFYING`
   — next action: finish Pre-handoff checks, then `/spec-handoff <key>`
9. **All Implementation + Pre-handoff ticked** → `READY_FOR_HANDOFF`
   — next action: `/spec-handoff <key>`

## List mode output format

Tabular. Columns: `KEY`, `Type`, `Pri`, `State`, `Progress`,
`Blockers / Depends`. Truncate title if needed; full title is in
drill-in mode.

```
KEY                         Type    Pri    State                Progress   Blockers / Depends
add-rain-radar-toggle       story   norm   PR_OPEN              6/6        —
fix-uv-index-rounding       bug     norm   READY_FOR_HANDOFF    4/4        —
add-hourly-forecast         story   norm   WAITING_DEPENDENCY   0/9        depends_on: add-rain-radar-toggle
widget-battery-drain        bug     high   BLOCKED              0/6        blockers: OQ-1
```

If no specs found:
```
No active specs under specs/. Run /spec-new <input> to start one.
```

If one or more proposals were skipped as unreadable, append a warning
footer.

## Drill-in mode output format

```
add-rain-radar-toggle — Add a toggle to show/hide the rain radar layer on the map

Identity
  type:        story
  priority:    normal
  source:      manual
  source_ref:  (dictated in chat)
  created:     2026-08-14

Status
  state:           READY_FOR_HANDOFF
  blockers:        none
  depends_on:      none
  recommend_split: no
  skeleton steps:  0

Progress
  implementation:  4 / 4
  pre-handoff:     6 / 6
  affected areas:  2 (0 TBD)
  open questions:  1 (0 blocking)

Memories consulted
  architecture, conventions

Next action
  /spec-handoff add-rain-radar-toggle
```

### Drill-in, PR open (handoff.json present, not yet archived)

```
add-rain-radar-toggle — Add a toggle to show/hide the rain radar layer on the map

Status
  state:           PR_OPEN

Progress
  implementation:  4 / 4
  pre-handoff:     6 / 6

Handoff
  impl PR:     #12  https://github.com/Kronos1993/Multiplatform-Weather-App/pull/12
  memory PR:   —
  archive PR:  —     (opens after the impl PR merges)

Next action
  Impl PR #12 is open. Run /spec-finalize add-rain-radar-toggle to poll
  merge state and archive when merged.
```

If the spec doesn't exist:
```
No spec at specs/some-key/. Use /spec-status (no args) to see
active specs, or /spec-new <input> to start a new one.
```

## What this skill does NOT do

- Does not modify any file.
- Does not run builds.
- Does not contact any issue tracker — this repo has none.
- Does not query `gh` for live PR merge state — `archived_pre_merge`
  is surfaced from `handoff.json`; the user verifies at the URL.
- Does not check for stale data — the tasks.md ticked count is the
  source of truth.
- Does not list archived specs by default in **list mode**. Drill-in
  mode does look under `_archive/` when a key is not found in `specs/`.

## Failure modes

| Symptom | Cause | Recovery |
|---|---|---|
| Drill-in: spec not found | Wrong key or already archived | Use no-arg list mode; or check `specs/_archive/<key>/` |
| List: zero results | No specs in flight, or the cwd is wrong | Run `/spec-new <input>` to start one; verify cwd is the repo root |
| Frontmatter parse fails | `proposal.md` was hand-edited and YAML broke | Drill-in: stops and surfaces the parse error. List mode: the spec is skipped and named in the unreadable-proposal warning footer |
| State shows `BLOCKED` after blocker was resolved | Frontmatter `blockers:` not synced with §9 | Edit frontmatter to remove the resolved ID; re-run `/spec-plan` to refresh banner |

## Examples

### Listing everything

```
/spec-status
```
Shows the table of all active specs with one-line status each.

### Drilling into one

```
/spec-status add-rain-radar-toggle
```
Shows the full block for `add-rain-radar-toggle`.

## Reference

- Templates: `.specs/templates/`.
- Config: `.specs/config.json`.
- `handoff.json` field contract: `.specs/handoff-schema.md`.
- State machine source of truth: this skill's "State detection rules" section.
- Sibling skills: `/spec-new`, `/spec-plan`, `/spec-implement`,
  `/spec-handoff`, `/spec-finalize`, `/spec-ship`.
- Handoff contracts: `.specs/HANDOFFS.md`.
- Config schema: `.specs/config.schema.md`.
- Candidate discovery: `.specs/SPEC_DISCOVERY.md`.
- Backlog: `.specs/IMPROVEMENTS.md`.
