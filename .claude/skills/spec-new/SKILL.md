---
name: spec-new
description: Create a new spec folder under `specs/<key>/` from a local file, a pasted/dictated description, or a URL. Auto-detects the input shape, reads/transcribes the body, and writes `story.md` from the template. Use whenever the user starts work on a new piece of work and hands you a doc, a description, or a link. Do NOT use for /spec-plan, /spec-implement, or /spec-handoff — those are separate skills run after `/spec-new`. This repo has no issue tracker (no Jira, no active GitHub Issues) — this is a from-scratch intake skill, not a ticket importer. Spec folder layout and template fields are documented in `.specs/templates/` and `.specs/IMPROVEMENTS.md`.
metadata:
  claude_md_requires: '[]'
---

## Usage

```
/spec-new <input>
```

Where `<input>` is one of:

| Shape | Example | Detection |
|---|---|---|
| Local file path | `./notes/radar-layers.md` or an absolute path | starts with `/`, `./`, `~/`, or a drive letter |
| URL | `https://github.com/some/other-repo/issues/12`, a design doc link | starts with `http://` or `https://` |
| Manual / dictated | anything else — a sentence, a paragraph, a bug description typed straight into chat | fallback when neither of the above matches |

There is no ticket-key concept in this repo (no `PROJ-1234` pattern to
detect) — `manual` is the everyday path, not a fallback of last resort
like it would be in a Jira-backed repo.

## What this skill does

1. Read `.specs/config.json` for `paths` and `intake.sources`.
2. Detect the source type from `<input>` (table above).
3. Resolve `key` — a short kebab-case slug:
   - If the user supplies one (e.g. `/spec-new radar-layers: <description>`),
     use it.
   - Otherwise derive it from the title/first line of the input,
     lowercased, spaces → hyphens, trimmed to ~5 words
     (`"Add rain radar layer toggle"` → `add-rain-radar-layer-toggle`).
   - If the derived slug is ambiguous or the input has no clear title,
     ask the user for a short slug — never guess silently.
4. Confirm the spec folder path (`{specs_root}/{key}/`) does not already
   exist (see Folder existence).
5. Fetch/transcribe the body (per source type below).
6. Create the folder and write `{specs_root}/{key}/story.md` from
   `{templates_root}/story.md`, populating frontmatter and known
   fields from the input.
7. Hand off summary to the user with next-step pointer to `/spec-plan`.

The skill writes **only** `story.md`. The proposal/plan/tasks files are
created by `/spec-plan` after the user reviews `story.md`.

## Detection rules (precise)

Order matters — the first matching rule wins:

1. If `<input>` starts with `http://` or `https://` → **URL**.
2. Else if `<input>` exists as a file (Read returns content) →
   **local file**.
3. Else → **manual** — the input itself (or what follows a `:` after a
   user-supplied slug) is the description body.

## Fetch strategy by source

### Local file

- Use Read on the path. If relative, resolve against the repo root.
- Treat the file's content as the verbatim body.
- Set `story.md` frontmatter:
  - `source: file`
  - `source_ref:` the path **relative to the repo root** when the file
    is inside the repo; otherwise the basename. Never store an
    absolute OS path.

### URL

- Try `WebFetch` on the URL. If it resolves to readable content
  (e.g. a public GitHub issue, a design doc), use that as the body.
- If unreachable or the tool has no web access in this session, fall
  back to manual: tell the user "Couldn't fetch `<url>` — paste the
  relevant content and I'll use the URL as a reference link."
- Set `story.md` frontmatter:
  - `source: url`
  - `source_ref:` the URL verbatim.

### Manual

- The user's own words ARE the input — no fetch involved. If the
  message is short (a one-liner), ask one clarifying round ("what's
  the acceptance bar for this — what should be true when it's done?")
  before writing `story.md`, so the intake isn't thinner than a
  five-minute conversation would produce. Don't over-interrogate — one
  round is enough; deeper questions belong in `/spec-plan`'s Open
  Questions.
- Set `story.md` frontmatter:
  - `source: manual`
  - `source_ref:` a one-line paraphrase of who/when if relevant, else
    leave blank.

## Writing story.md

Copy `{templates_root}/story.md` to `{specs_root}/{key}/story.md`, then
replace placeholders:

| Template field | Source |
|---|---|
| `spec_id` | `{key}` |
| `source` | per Fetch strategy above |
| `source_ref` | per Fetch strategy above |
| `fetched_at` | current date as `YYYY-MM-DD` |
| `fetched_by` | `/spec-new` |
| `# Story: <title>` | derived title (see Title normalization) |
| Metadata table | best-effort type/priority guess from the body; leave blank if not inferable |
| Description | verbatim/transcribed body, rendered per Description rendering |
| Acceptance criteria | verbatim AC block if the source states one; otherwise leave the placeholder for `/spec-plan` to draft |
| Comments / discussion | always the stub `(none — file/manual/url intake, no comment thread)` |
| Attachments | file/URL sources: any referenced attachments by name/path; manual: omit |
| Links | any related specs/PRs/docs mentioned in the input |

**No truncation.** `story.md` is the raw-intake audit record — preserve
the body in full regardless of size.

### Title normalization

Take the first sentence or heading of the input as the title. Keep the
wording as given — no rewording, just trim leading filler ("So I want
to…" → drop the filler, keep the substance) only when it's unambiguous.

### Description rendering

- **File sources**: wrap the body in a markdown blockquote (`> `).
- **URL sources**: fetched content rendered verbatim, prefixed with the
  source URL as a link.
- **Manual sources**: transcribe as given, no blockquote (it's already
  the user's direct words, not an external quotation).

### Source-text inconsistency

If the input contains both a title-level summary and a body that
appear to describe different problems, preserve both verbatim and add
a footer note in story.md:

```
## Source-text inconsistency (flagged for proposal Open Questions)
<paragraph describing the mismatch>
```

`/spec-plan` will turn that footer into a (BLOCKER) OQ.

## Folder existence and overwrite

Before writing, check whether `{specs_root}/{key}/` exists:

- **Does not exist** → create the folder and write `story.md`.
- **Exists, contains only `story.md`** → ask: "Spec folder for `{key}`
  already exists with a story.md. Overwrite (y) / abort (n)?"
- **Exists with proposal/plan/tasks** → abort with message: "Spec
  `{key}` is already in flight (has proposal.md). Re-running
  `/spec-new` would clobber it. Use `/spec-status {key}` to inspect."

## Output to user

After successful write, print:

```
spec_id:       {key}
folder:        {specs_root}/{key}/
source:        {source}
source_ref:    {source_ref}
title:         {title}
type-guess:    bug | story | refactor | chore   (best-effort from content)

Next step: review specs/{key}/story.md, then run /spec-plan to draft
proposal.md, plan.md, and tasks.md.
```

If a source-text inconsistency was detected, surface it explicitly.

## What this skill does NOT do

- Does not write `proposal.md`, `plan.md`, `tasks.md` — that is `/spec-plan`.
- Does not chain into `/spec-plan` automatically.
- Does not push, branch, or comment anywhere — that's `/spec-handoff`.
- Does not validate AC structure or run heuristics — `/spec-plan` does.
- Does not interact with any issue tracker — this repo has none
  configured, and this skill never assumes one exists.

## Failure modes and recovery

| Symptom | Cause | Recovery |
|---|---|---|
| Ambiguous slug, skill asks for one | Input had no clear title | Supply a short kebab-case slug yourself: `/spec-new my-slug: <description>` |
| URL fetch fails | Not publicly reachable, or no web tool access this session | Paste the relevant content directly — it becomes a manual intake with the URL kept as a reference link |
| Folder exists with active spec | A `/spec-new` was already run for this key | Use `/spec-status {key}`; delete or archive the folder first if you really want to start over |

## Examples

### Manual intake (the common case)

```
/spec-new Add a toggle on the map screen to show/hide the rain radar layer, remembering the user's last choice.
```
No slug given → derives `add-toggle-rain-radar-layer` (or similar);
asks the user to confirm/adjust if ambiguous. Writes
`specs/add-toggle-rain-radar-layer/story.md` with `source: manual`.

### File intake

```
/spec-new ./notes/radar-layers.md
```
Reads the file, writes `specs/<slug>/story.md` (slug from the file's
first heading, or prompted if unclear).

## Reference

- Templates: `.specs/templates/story.md`, `.specs/templates/proposal.md`,
  `.specs/templates/plan.md`, `.specs/templates/tasks.md`.
- Config: `.specs/config.json` (`intake.*`, `paths.*`).
- Handoff contracts: `.specs/HANDOFFS.md` — H1 (produces `story.md`
  for `/spec-plan`).
- Config schema: `.specs/config.schema.md` — key catalog + per-skill
  reader map.
- Backlog: `.specs/IMPROVEMENTS.md`.
