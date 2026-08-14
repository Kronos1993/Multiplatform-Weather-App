# Spec workflow — improvements backlog

This is the running log of deferred/unfinished improvements to this
`.specs/` workflow itself (not to the app code). Adapted from a
Smartmatic-internal `.specs/` workflow lineage — that lineage's own log
entries are specific to its tuning history and are not reproduced here;
this file starts fresh for this repo's own workflow.

Add an entry whenever a spec-* skill's design surfaces a known gap,
deferred feature, or rough edge that isn't worth fixing immediately.

## Format

```
### <YYYY-MM-DD> — <short title>

**Where:** <skill or doc>
**What:** <the gap or deferred item>
**Why deferred:** <one line>
```

## Deferred items carried over from the workflow's design (re-evaluate if/when relevant here)

- **Notifications** (`notifications.*` in `.specs/config.json`) — all
  channels disabled, none configured. No skill fans out any external
  notification today; `/spec-finalize` opens PRs only.
- **`pr.default_target` is unconfirmed** — this repo has `develop`,
  `main`, and `master` on origin with no PR history to infer a
  convention from. Defaulted to `develop` (see `config.json`
  `pr._default_target_doc`); confirm with the user at the first real
  `/spec-handoff` → `/spec-finalize` run and update the config once
  settled.
- **`/spec-resume`** — a hypothetical future skill to append new
  information to an already-planned spec's `story.md` (e.g. after a
  scope conversation continues outside the original intake). Not built.
- **`--checkpoint-per-step` commit mode** for `/spec-implement` — an
  opt-in mode that would commit after each `[implement]` step for
  granular bisectability. The default (zero commits during implement,
  one commit at `/spec-handoff`) is the current contract.

## This repo's own entries

(none yet)
