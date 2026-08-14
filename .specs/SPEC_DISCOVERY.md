# Spec candidate discovery

Single source of truth for how a spec-* skill picks **which spec to act
on when invoked without an explicit `<spec_id>`**. Six skills share this
"find the right candidate" behavior; this doc owns the skeleton + edge
rules so the skills only have to name their **predicate**.

This is an anchor doc (like `HANDOFFS.md`, `IMMUTABILITY.md`,
`config.schema.md`): referenced by the skills, not byte-synced into them.

Adapted from a Smartmatic-internal `.specs/` workflow lineage; the
algorithm itself is stack-agnostic, unchanged here.

## The algorithm

When a skill runs with no `<spec_id>` argument, resolve the candidate
this way:

1. **Enumerate.** List every spec folder directly under the specs root
   — `paths.specs_root` from `.specs/config.json` (default `specs`).
   **Exclude the archive root** — `paths.archive_root` (default
   `specs/_archive`); archived specs are never candidates.
2. **Filter by predicate.** Keep only folders whose state matches the
   calling skill's predicate (see the table below). The predicate is the
   *only* part that varies between skills.
3. **Rank by recency.** Sort the survivors by the mtime of the predicate's
   anchor file (`story.md` or `tasks.md`), newest first. On an exact mtime
   tie, fall back to `spec_id` descending so the choice is deterministic.
4. **Resolve.**
   - **Exactly one match** → use it.
   - **More than one** → list the candidates (key + title + mtime) and
     prompt the user to pick. Never silently guess among several.
   - **Zero matches** → stop with a message naming the predicate that
     found nothing and the skill to run instead (e.g. "no spec with an
     unchecked Implementation step — run `/spec-plan` first").

With an explicit `<spec_id>`, skip all of the above and act on
`<specs_root>/<spec_id>/` directly (the skill's own Preconditions still
apply).

## Per-skill predicates

The anchor file is the file whose mtime step 3 ranks on.

| Skill | Anchor file | Predicate (keep the folder if…) |
|---|---|---|
| `/spec-plan` | `story.md` | folder has **no** `proposal.md` (intake not yet planned) |
| `/spec-implement` | `tasks.md` | Implementation section has **≥1 unchecked** step |
| `/spec-review` | `tasks.md` | Implementation **+** Pre-handoff sections **fully ticked** |
| `/spec-handoff` | `tasks.md` | Implementation **+** Pre-handoff sections **fully ticked** |
| `/spec-finalize` | `tasks.md` | fully ticked **AND** ≥1 local commit ahead of `origin/develop` |
| `/spec-ship` | `tasks.md` | fully ticked **AND** the working tree is dirty (uncommitted spec work) |

`/spec-status` is **not** in this table: it enumerates *all* specs (it
doesn't pick a single candidate) and derives a richer state per spec. It
shares only step 1's root-resolution rule.

## Why an anchor doc, not a lib chunk

The shared skeleton is ~4 lines and each predicate is ~1 line. A
`.specs/lib/` byte-sync chunk would be mostly placeholder (the predicate).
Referencing one doc keeps the skeleton in a single place while each
skill's predicate stays legible at its own call site.

## See also

- `.specs/config.schema.md` — `paths.*` key catalog (specs root /
  archive root).
- `.specs/HANDOFFS.md` — what each skill consumes/produces (the predicates
  here mirror those entry preconditions).
