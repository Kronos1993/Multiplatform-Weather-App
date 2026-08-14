# `.specs/lib/` — shared snippets for spec skills

Skill files (`spec-*/SKILL.md`) are AI-readable documents — the model
reads each one fully when it dispatches a skill. So duplicated text
must stay duplicated **at runtime**: splitting it out into lib files
the model has to chase would cost tokens and reliability.

The convention here keeps duplication on disk (each skill carries an
inline copy) but lets a **linter** verify that copies stay in sync
with the canonical source.

Adapted from a Smartmatic-internal `.specs/lib/` mechanism — unchanged,
since it's stack-agnostic.

## Convention

1. Each lib file (e.g. `pr-content-synthesis.md`) is the **canonical
   version** of one chunk of skill behavior.
2. The lib file's front matter describes the chunk (purpose,
   consumers, placeholders). A single horizontal rule (`---`) on its
   own line separates front matter from canonical content.
   Everything **below** that rule is the verbatim canon.
3. Skills that need the chunk wrap an inline copy with anchor
   markers matching the lib's basename (without `.md`):
   ```
   <!-- lib:start:pr-content-synthesis -->
   …verbatim canon, with placeholders substituted…
   <!-- lib:end:pr-content-synthesis -->
   ```
4. **Placeholders** are lines containing a `%%snake_case%%` sigil in
   the canon. The linter treats them as **wildcards**.
5. Run `./check-sync.sh` from this directory to verify all skills'
   inline copies match the canon. Exit codes: 0 = in sync; 1 =
   drift; 2 = config error (e.g. unmatched markers).

## Files

| Lib | Canonical chunk | Used by | Placeholders |
|---|---|---|---|
| `pr-content-synthesis.md` | Tier A/B/C PR title + description synthesis | `/spec-finalize` (PR-open mode), `/spec-ship` (impl PR) | `%%audit_trail%%` |
| `memory-candidates.md` | decisions.md → reusable-knowledge prompt ritual | `/spec-finalize` (PR-open mode), `/spec-ship` (impl PR phase) | (none) |

## Updating a lib

1. Edit `<name>.md` in this directory.
2. Edit every skill that wraps it (search for the anchor name to find
   them under `.claude/skills/`).
3. Run `./check-sync.sh` until it exits 0.
4. Commit lib + skills together.

## Other linters in this directory

`check-claude-md.sh` is a **separate** linter (not part of the anchor-
sync mechanism above). It verifies that the CLAUDE.md sections each
skill declares in its frontmatter still exist:

- Each spec-* skill (and `/create-pr`) lists the root `CLAUDE.md`
  sections it leans on under `metadata.claude_md_requires` in its
  frontmatter (e.g. `metadata:` then
  `  claude_md_requires: '["Code style and analysis"]'`
  — empty list = no dependency).
- `./check-claude-md.sh` checks every listed name appears as a `##` or
  `###` heading in the repo-root `CLAUDE.md`. Exit codes: 0 = all
  present; 1 = a declared section is missing (renamed/removed); 2 =
  setup error.
- Run it after renaming a `CLAUDE.md` heading or editing a skill's
  `claude_md_requires` list.

## Future libs (when relevant)

Nothing concrete planned. The mechanism is ready for future
extractions whenever new duplication pain surfaces.
