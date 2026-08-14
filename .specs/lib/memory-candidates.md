# memory-candidates

Canonical reusable-knowledge surfacing ritual. Used by:

- `/spec-finalize` (PR-open mode, first run — immediately after the
  impl PR opens)
- `/spec-ship` (impl PR open phase — before the archive flow runs)

No placeholders — the ritual is identical across both skills. Each
skill keeps its own intro paragraph **outside** the lib markers.

Adapted from a Smartmatic-internal `.specs/lib/memory-candidates.md`.
This repo has a single Serena project, so memories live at one fixed
path — no "which product" choice is needed.

## How to copy into a skill

Wrap the canonical content (everything below the horizontal rule)
with anchor markers:

    <!-- lib:start:memory-candidates -->
    [verbatim from below]
    <!-- lib:end:memory-candidates -->

Run `./check-sync.sh` from this directory to verify after editing.

---

If `specs/<spec_id>/decisions.md` exists, parse each
`## <date> — <Step N> — <title>` heading and offer it as a
reusable-knowledge candidate, persisted as a Serena memory under
`.serena/memories/` at the repo root (following the dense-bullet
format used for this repo's existing seeded memories — plain markdown,
terse bullets, no YAML frontmatter):

1. Propose a name based on title content (topic-prefixed, matching the
   existing index — see `.serena/memories/memory_maintenance.md`).
2. Ask user: save / edit / skip.
3. On save: write the memory file, with the body from decisions.md
   reformatted into dense-bullet style.
4. Set `memory_candidates_surfaced: true` in handoff.json regardless
   of how many were accepted.

If decisions.md doesn't exist, set the flag to `true` immediately
and skip the prompt.
