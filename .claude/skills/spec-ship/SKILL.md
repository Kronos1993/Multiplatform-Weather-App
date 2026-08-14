---
name: spec-ship
description: "One-shot finalize. Commits the implemented spec, opens the implementation PR, surfaces decisions.md memory candidates, and archives the spec folder on a separate chore/archive-<key> branch — all in one pass, without waiting for the impl PR to merge. Equivalent to /spec-handoff then /spec-finalize (first-run) back-to-back, plus an eager archive sourced from the impl branch (since specs/<key>/ does not yet exist on origin/develop). Produces up to three PRs: the implementation PR (mergeable immediately); a memory PR on chore/memories-<key> when reusable-knowledge candidates are saved (independent, mergeable immediately); and the archive PR (mergeable only after the impl PR merges; skip with --no-archive-pr for batching). Memory and archive PRs are assigned to reviewers.default if configured. Writes handoff.json with archived_pre_merge true so /spec-status can flag specs whose impl PR was later closed-without-merge. Use the careful /spec-handoff then /spec-finalize path when archive bookkeeping should wait for a real merge."
metadata:
  claude_md_requires: '[]'
---

## Usage

```
/spec-ship [<spec_id>] [--no-archive-pr]
```

- With `<spec_id>` (e.g. `add-rain-radar-toggle`): ship that spec.
- Without: selects the candidate per `.specs/SPEC_DISCOVERY.md` —
  predicate: most recent `tasks.md` whose **Implementation** +
  **Pre-handoff** sections are fully ticked AND whose working tree is
  dirty.
- `--no-archive-pr`: create the local archive branch and commit, but
  skip `/create-pr` for it (the archive branch stays **local,
  unpushed**). Use when batching several archives into one PR later.

## When to use this skill

- You want `/spec-handoff` + `/spec-finalize` collapsed into one step,
  and you accept that the spec folder will move out of `specs/<key>/`
  before the impl PR actually merges.
- You will not be coming back to run `/spec-finalize` after merge.

## When NOT to use this skill

- You want explicit review of the archive move after merge —
  use `/spec-handoff` then `/spec-finalize` instead.
- The impl PR is likely to be closed without merging (experimental
  work) — pre-merge archive would strand the archive branch.
- A commit is already in place on the branch (working tree is clean) —
  run `/spec-finalize` directly; this skill expects a dirty tree.

## Preconditions

- `specs/<spec_id>/proposal.md`, `plan.md`, `tasks.md` all exist.
- All `tasks.md` Implementation checkboxes ticked.
- All `tasks.md` Pre-handoff checkboxes ticked.
- Working tree has uncommitted changes (staged or unstaged).
- Current branch is either `develop` (skill will branch off) or a
  branch matching the naming convention from `.specs/config.json`
  `branch.naming` for the spec's type.
- `gh` is on PATH and authenticated.
- `origin/develop` is reachable (`git fetch origin develop` must
  succeed).

## Pre-flight checks

Fail closed. These mirror `/spec-handoff` exactly.

1. **Spec files present** — proposal.md, plan.md, tasks.md all exist.
2. **Implementation complete** — count unchecked boxes in tasks.md
   Implementation section. If > 0, stop with the list.
3. **Pre-handoff complete** — count unchecked boxes in Pre-handoff
   section. If > 0, stop with the list.
4. **Working tree dirty** — if empty, stop with: "No changes to
   commit. The commit may already exist — run `/spec-finalize <key>`
   instead, which opens the PR and waits for merge before archiving."
5. **Branch name compatible** — see Branch handling below.
6. **`origin/develop` reachable** —
   `git fetch origin develop --quiet`. If this fails, stop.
7. **gh auth healthy** — `gh auth status` returns 0. If not, stop and
   surface `/create-pr`'s recovery hint.

## Branch handling

Identical to `/spec-handoff`:

```
current = git rev-parse --abbrev-ref HEAD

if current in ("develop", "main", "master"):
    if frontmatter `branch_suggested` is set and non-empty:
        branch = branch_suggested
    else:
        branch = render branch.naming[spec.type] with {key} = spec_id
    confirm <branch> with the user; accept an override name if given
    git checkout -b <branch>
    proceed
elif current matches the naming convention for spec.type:
    proceed
elif current matches the naming convention for *some* type:
    warn and ask to proceed
else:
    stop
```

The `branch.naming` table: `bug: bugfix/{key}`, `story: feature/{key}`,
`refactor: refactor/{key}`, `chore: chore/{key}`, `hotfix: hotfix/{key}`.

## What this skill does (in order)

1. **Pre-flight** — see above. Stop on any failure.
2. **Resolve / create the impl branch** per Branch handling.
3. **Determine the file set to stage** — read tasks.md and plan.md
   "Files / symbols" entries; cross-check with `git status --porcelain`.
   Out-of-scope changes stop the run. **Carve-out**: changes under
   `specs/<spec_id>/**` are always in-scope.
4. **Stage explicitly by file name** — never `git add -A`.
5. **Invoke `/commit`** via the Skill tool.
6. **Synthesize PR title + body** for the impl PR — see Content
   synthesis (Tier A → B → C, lifted from `/spec-finalize`).
7. **Open the impl PR** via `/create-pr --title ... --body ...`.
   Capture `pr_number`, `pr_url`.
8. **Surface decisions.md reusable-knowledge candidates** — see
   Reusable-knowledge candidates section.
9. **Open the memory PR (if any memory was saved)** — see Memory flow.
   Runs before the archive flow so the impl working tree is clean for
   the archive branch switch.
10. **Write & commit `handoff.json` on the impl branch** at
    `specs/<spec_id>/handoff.json`, with every field known so far —
    impl `pr_*`, `opened_*`, `memory_*`, `archive_branch`
    (= `chore/archive-<key>`), `archived_at`, `archive_reason: "merged"`
    (optimistic), `archived_pre_merge: true`, `shipped_via`; leave
    `archive_pr_*` null. `git add specs/<spec_id>/handoff.json`, commit
    (`docs: add handoff.json for <key>`), then `git push`.
11. **Archive on a separate branch** — see Archive flow.
12. **Tick all six Handoff sub-checkboxes** in tasks.md. "Branch
    pushed" refers to the **impl** branch. The **archive** branch is
    pushed by `/create-pr` in the archive flow only when the archive PR
    is opened; when skipped (`--no-archive-pr` or
    `ship.open_archive_pr: false`) the archive branch stays local and
    unpushed. The "Spec folder archived" row gets a parenthetical
    "(pre-merge)".
13. **Return to the impl branch**.
14. **Print summary** with all PR URLs and the standard reminder:
    "Archive PR can only be merged after impl PR #<n> merges."

## Memory flow

When step 8 saves at least one memory (a create or update under
`.serena/memories/`), ship those files in their **own** PR — separate
from the impl PR and the archive PR. Run this **before** the archive
flow.

```
impl_branch    = current branch
memory_branch  = "chore/memories-<key>"

git fetch origin develop --quiet
git switch -c <memory_branch> origin/develop

git add .serena/memories/<...>   # explicit, never git add -A

git commit -m "docs: add Serena memories for <key>"

/create-pr --title "docs: add Serena memories for <key>" \
           --body <memory PR body — see below> \
           [--reviewer <user>]   # only if reviewers.default is non-empty
# capture memory_pr_number, memory_pr_url

git switch <impl_branch>
```

`--no-archive-pr` does **not** suppress the memory PR.

### Memory PR description body

```markdown
## Summary

Serena memories captured from spec <key> (implementation PR #<n>).

## Memories

{one bullet per saved memory: `- \`.serena/memories/<name>.md\` — <one-line title>`}

## Notes

Created by `/spec-ship`. Documentation only — no code changes.
Independent of PR #<n> and mergeable on its own.

---

🤖 Generated with [Claude Code](https://claude.com/claude-code)
```

## Archive-PR decision

Two controls, **inverse polarity**:

- `ship.open_archive_pr` (config, default `true`) — "open the archive PR?"
- `--no-archive-pr` (CLI flag) — a per-run override, forces skip.

Effective rule: **open the archive PR iff `ship.open_archive_pr` is
`true` AND `--no-archive-pr` was NOT passed.** When skipped, the
archive branch + commit are still created locally but not pushed.

## Archive flow (pre-merge variant)

Because `origin/develop` does not yet contain `specs/<key>/`,
source from the impl branch tip:

```
impl_branch    = current branch
archive_branch = "chore/archive-<key>"

git fetch origin develop --quiet
git switch -c <archive_branch> origin/develop

mkdir -p specs/_archive

git checkout <impl_branch> -- specs/<key>/

git mv specs/<key> specs/_archive/<key>

git add specs/_archive/<key>

git commit -m "chore: archive <key> (impl PR #<pr_number>, pre-merge)"

if archive PR is enabled (see "Archive-PR decision"):
    /create-pr --title "chore: archive <key>" \
               --body <archive PR body — see below> \
               [--reviewer <user>]   # only if reviewers.default is non-empty
    # capture archive_pr_number, archive_pr_url

    # edit specs/_archive/<key>/handoff.json: set archive_pr_number/url
    git add specs/_archive/<key>/handoff.json
    git commit -m "docs: record archive PR #<archive_pr_number> in handoff.json"
    git push
# (archive PR skipped: the chore/archive-<key> branch + commit stay LOCAL
#  and UNPUSHED, archive_pr_* stay null)

git switch <impl_branch>
```

### Archive PR description body

```markdown
## Summary

Archive bookkeeping for the spec finalized in PR #<impl_pr_number>.
Moves `specs/<key>/` → `specs/_archive/<key>/`.

## Merge order

This PR can only be merged after PR #<impl_pr_number> merges.
GitHub will not auto-detect the order — please merge #<impl_pr_number>
first.

## Notes

Created by `/spec-ship` in pre-merge mode (`archived_pre_merge: true`
in `handoff.json`).

---

🤖 Generated with [Claude Code](https://claude.com/claude-code)
```

## Content synthesis (impl PR title and body)

Three-tier fallback. Canonical text lives in
`.specs/lib/pr-content-synthesis.md`; the wrapped block below is
the verbatim copy with `%%audit_trail%%` substituted. Run
`.specs/lib/check-sync.sh` after editing to verify the copy still
matches the canon.

<!-- lib:start:pr-content-synthesis -->

### Tier A — synthesize from proposal.md and last commit (default)

- **Title**: take the last commit's subject line (this repo's
  conventional-commit style per `/commit`). Cap at 255 chars.
- **Description body**:

```markdown
## Summary

{verbatim from proposal.md §2 "Problem / Why" body, first paragraph}

## Scope

**In scope**
{verbatim from proposal.md §3 "In scope" bullets}

**Out of scope**
{verbatim from proposal.md §3 "Out of scope" bullets, if any}

## Test plan

{transform each AC bullet from proposal.md §8 into `- [ ] <text>` —
 GitHub PR Markdown supports task list checkboxes. This repo has no
 automated test suite (`verification.no_test_suite: true`), so every
 item here is a manual/build-time check, never "covered by unit test X"}

## Expect/actual & localization

{include only when proposal frontmatter `expect_actual_touched: yes`
 and/or `localization_touched: yes` — one line per touched expect/actual
 pair or shared string confirming parity/sync (see decisions.md for the
 review note). Omit the heading entirely when both are `no`}

## Notes for reviewers

{include this section only if §7 has risks worth surfacing, or §9
 has unresolved-but-out-of-band OQs, or decisions.md has a
 reusable-knowledge candidate marked "reviewer note"}

---

%%audit_trail%%

🤖 Generated with [Claude Code](https://claude.com/claude-code)
```

If any required section in proposal.md is missing or empty, skip
that section in the body (don't render an empty heading). If §2 is
missing entirely, fall through to Tier B — the proposal isn't rich
enough to justify Tier A.

### Tier B — `gh pr create --fill` (fallback)

Call `/create-pr --fill`. `gh` uses the last commit's subject as
title and body as description. Suitable when proposal.md is too
thin to derive structured sections but the commit message is
descriptive.

### Tier C — interactive (last resort)

Call `/create-pr --interactive`. The user types the title and
body inline. Only reachable if Tier B also fails.
<!-- lib:end:pr-content-synthesis -->

`%%audit_trail%%` substitution for this skill: `Spec audit trail:
` + `` `specs/<spec_id>/` `` + ` (already moved to ` + `` `specs/_archive/<spec_id>/`
`` + ` on ` + `` `chore/archive-<key>` `` + ` — see archive PR).`

## Reusable-knowledge candidates

Identical behavior to `/spec-finalize`. Inline immediately after the
impl PR opens (before the archive flow runs) — context is freshest
right after implementation. Ritual canon lives in
`.specs/lib/memory-candidates.md`; run `.specs/lib/check-sync.sh`
after edits.

<!-- lib:start:memory-candidates -->

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
<!-- lib:end:memory-candidates -->

## handoff.json schema (extended)

Full field contract: `.specs/handoff-schema.md`.

```json
{
  "spec_id": "add-rain-radar-toggle",
  "branch": "feature/add-rain-radar-toggle",
  "target_branch": "develop",
  "pr_number": 12,
  "pr_url": "https://github.com/Kronos1993/Multiplatform-Weather-App/pull/12",
  "opened_at": "2026-08-14T18:23:00Z",
  "opened_via": "tier-A",
  "memory_candidates_surfaced": true,
  "memory_branch": "chore/memories-add-rain-radar-toggle",
  "memory_pr_number": 13,
  "memory_pr_url": "https://github.com/Kronos1993/Multiplatform-Weather-App/pull/13",
  "archived_at": "2026-08-14T18:24:00Z",
  "archive_branch": "chore/archive-add-rain-radar-toggle",
  "archive_reason": "merged",
  "archive_pr_number": 14,
  "archive_pr_url": "https://github.com/Kronos1993/Multiplatform-Weather-App/pull/14",
  "archived_pre_merge": true,
  "shipped_via": "spec-ship"
}
```

## What this skill does NOT do

- Does not poll the PR state.
- Does not merge PRs.
- Does not auto-batch archive PRs across specs.
- Does not modify proposal.md, plan.md, tasks.md content (only ticks
  Handoff section checkboxes).
- Does not run any verification beyond the pre-flight.
- Does not comment anywhere — this repo has no issue tracker or chat
  webhook configured.

## Failure modes and recovery

| Symptom | Cause | Recovery |
|---|---|---|
| Pre-flight: unchecked Implementation step | Work not finished | Run `/spec-implement`; or tick manually |
| Pre-flight: unchecked Pre-handoff step | Verification missing | Run the missing check; tick when green |
| Pre-flight: working tree clean | Commit already exists | Use `/spec-finalize <key>` instead |
| Pre-flight: out-of-scope file changes | Implementation went beyond plan.md | Include the files in plan.md (re-run `/spec-plan`) or stash/revert |
| Pre-flight: `git fetch origin develop` fails | Offline, or no remote access | Get network, re-run |
| `/commit` fails on hooks | Project hook tripped | Fix the issue, re-run. Never `--no-verify` |
| `/commit` fails on build (compile error) | The change doesn't compile | Inspect, fix with Serena's symbolic edit tools, re-run |
| Memory flow: `/create-pr` for the memory PR fails after the commit lands | gh auth issue or transient error | The memory commit is already in place. Re-run `/create-pr --fill` from that branch, update `handoff.json` manually, return to the impl branch before re-running the archive portion |
| Impl PR opens, archive flow fails (dirty leftover, conflict) | Working tree has leftover files from `/spec-implement` | `handoff.json` is already committed on the impl branch, so nothing is lost. Clean the working tree, re-run only the archive portion (recipe: switch to `chore/archive-<key>` off `origin/develop`, checkout the impl branch's `specs/<key>/`, `git mv`, commit) |
| `/create-pr` for archive PR fails | gh auth issue or transient error | Impl PR is fine. Re-run `/create-pr --fill` from `chore/archive-<key>` later; set `archive_pr_*` in handoff.json, commit, push |
| Impl PR is later closed without merging | Reviewer rejection, scope rethink | The archive branch is stranded; the spec is in `specs/_archive/` locally. Unarchive: switch to a new branch off `origin/develop`, checkout the impl branch's `specs/<key>/`, remove `specs/_archive/<key>/`, commit a revert, open a PR. `/spec-status <key>` surfaces this via `archived_pre_merge: true` |
| User runs `/spec-ship` twice on same spec | handoff.json already exists | Stop with "handoff.json already exists for <key>" — do NOT re-ship; instead (a) add a fix to the already-open impl PR, or (b) retry the archive only, or (c) restore the branch manually first |

## Examples

### Standard fast path

```
/spec-ship fix-uv-index-rounding
```
- Pre-flight green. Currently on `develop`. Skill creates
  `bugfix/fix-uv-index-rounding`, switches. Stages files explicitly.
  `/commit` produces
  `fix: correct UV index rounding in the weather suggestion string`.
- Synthesizes Tier-A PR body. `/create-pr` returns `pr_number: 12`.
- Surfaces 1 reusable-knowledge candidate; user saves it.
- Memory flow: branches `chore/memories-fix-uv-index-rounding` off
  origin/develop, commits, `/create-pr ...` returns `pr_number: 13`,
  switches back.
- Writes `handoff.json` (impl + memory fields, `archived_pre_merge:
  true`, `archive_pr_*` null), commits, pushes.
- Archives on `chore/archive-fix-uv-index-rounding`, `/create-pr`
  returns `pr_number: 14`; records the back-reference, pushes.
- Output: "Spec fix-uv-index-rounding shipped. Impl PR #12, memory PR
  #13, archive PR #14. Merge #12 first, then #14."

### Batched archive (skip archive PR)

```
/spec-ship fix-uv-index-rounding --no-archive-pr
```
Same through the handoff.json commit — the memory PR still opens.
Archive branch created and committed but not pushed.

## Reference

- Templates: `.specs/templates/`.
- Config: `.specs/config.json` — `branch.naming.<type>`, `pr.*`,
  `ship.open_archive_pr`.
- `/commit` skill — invoked at step 5.
- `/create-pr` skill — invoked at steps 7 and 9.
- Sibling skills: `/spec-new`, `/spec-plan`, `/spec-implement`,
  `/spec-handoff`, `/spec-finalize`, `/spec-status`.
- Handoff contracts: `.specs/HANDOFFS.md` — H7.
- Config schema: `.specs/config.schema.md`.
- Choosing how to finalize: `.specs/CHOOSING_FINALIZE.md`.
- Candidate discovery: `.specs/SPEC_DISCOVERY.md`.
- Backlog: `.specs/IMPROVEMENTS.md`.
