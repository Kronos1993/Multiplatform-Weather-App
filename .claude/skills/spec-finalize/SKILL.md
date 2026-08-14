---
name: spec-finalize
description: "Use after /spec-handoff to push the branch and open the PR (first run, when specs/<key>/handoff.json does not exist), then re-invoke to poll PR state and archive once merged. Two-mode skill taking a spec from locally-committed to merged-and-archived. First run synthesizes a PR title/body from proposal.md and the latest commit, calls /create-pr to push and open the PR, and writes handoff.json (branch/pr_number/pr_url); if reusable-knowledge candidates were saved as Serena memories, it commits them on chore/memories-<key> off origin/develop and opens a separate memory PR assigned to reviewers.default (if any). Later runs query gh for merge state: if merged, archive the spec under specs/_archive/ on a new branch and open the archive PR; if still open, report status and exit; if closed without merge, exit cleanly (re-run with --archive-closed to archive an abandoned spec). Pairs with /spec-handoff like /create-pr pairs with /commit."
metadata:
  claude_md_requires: '[]'
---

## Usage

```
/spec-finalize [<spec_id>] [--archive-closed]
```

- With `<spec_id>` (e.g. `add-rain-radar-toggle`): finalize that spec.
- Without: selects the candidate per `.specs/SPEC_DISCOVERY.md` —
  predicate: most recent `tasks.md` whose **Implementation** +
  **Pre-handoff** sections are fully ticked AND that has at least one
  local commit ahead of `origin/develop`.
- `--archive-closed`: archive the spec even if its PR is `CLOSED`
  without merging. Default behavior on a closed PR is to surface a
  terminal-state message and exit cleanly without prompting, so
  `/loop`-piped invocations can stop polling without stalling.

## Preconditions

- `specs/<spec_id>/proposal.md`, `plan.md`, `tasks.md` all exist.
- `/spec-handoff` already produced a local commit on a branch matching
  `.specs/config.json` `branch.naming.<type>`.
- `gh` is on PATH and authenticated (delegated to `/create-pr`'s
  preconditions on first run; required for `gh pr view` on later runs).
- For the merge-and-archive path: `origin/develop` is reachable.

## Operating modes

The skill auto-detects mode from the spec folder location and the
presence of `handoff.json`:

| State | Folder / `handoff.json` | Mode |
|---|---|---|
| Already archived | folder under `specs/_archive/<spec_id>/` | **Already-finalized (no-op)** |
| Local commit done, no PR yet | active folder, `handoff.json` absent | **PR-open mode** |
| PR open or merged | active folder, `handoff.json` present | **Status-check mode** |

**Check the archive first.** If `specs/<spec_id>/` does not exist but
`specs/_archive/<spec_id>/` does, the spec is already finalized. Print
a pointer to `/spec-status <spec_id>` and exit cleanly — this prevents
a re-run from opening a **duplicate PR**.

### PR-open mode (first run)

1. Read `.specs/config.json` for `paths`, `pr.*`, `reviewers.default`,
   and `branch.naming`.
2. Read `specs/<spec_id>/proposal.md` frontmatter and §2 / §3 / §7 /
   §8 / §9. Read the last commit on the current branch
   (`git log -1 --format=%H%n%s%n%b`).
3. Confirm the current branch matches the expected naming pattern for
   the spec's `type:` from frontmatter. If not, ask the user to switch
   first.
4. Synthesize the PR title and body (see "Content synthesis" below).
5. Call `/create-pr --title "<title>" --body "<body>"` (add
   `--reviewer <user>` only if `reviewers.default` is non-empty).
6. Parse the returned `pr_number` and `pr_url`.
7. Write `specs/<spec_id>/handoff.json` (see schema below).
8. Surface `decisions.md` reusable-knowledge candidates inline (see
   "Reusable-knowledge candidates" below).
9. If at least one candidate was saved as a Serena memory, commit those
   memory files and open the memory PR, then update `handoff.json` with
   its details (see "Memory PR" below). If none were saved, leave the
   memory PR fields null.
10. Print summary; point at "re-run `/spec-finalize <key>` after the
    PR merges to land the archive," and include the memory PR URL if
    one was opened.

### Status-check mode (subsequent runs)

1. Read `specs/<spec_id>/handoff.json` for `pr_number`, `target_branch`.
2. Query gh: `gh pr view <pr_number> --json state,mergedAt,url`.
   Derive the merge state:
   - `state == "MERGED"` (or `mergedAt` non-null) → **merged**.
   - `state == "CLOSED"` (and `mergedAt` null) → **closed**.
   - `state == "OPEN"` → **opened**.
   - Any unexpected shape: do NOT guess — print the raw `gh pr view`
     output and exit cleanly.
3. Branch on the derived state:
   - **`merged`** → proceed to archive flow.
   - **`opened`** → print "PR #<n> is still open. Re-run later or
     pipe into `/loop 30m /spec-finalize <key>`." Exit cleanly.
   - **`closed`** → if `--archive-closed` was NOT passed, print:
     "TERMINAL STATE: PR #<n> is closed without merging. Spec NOT
     archived. **Stop polling — manual review needed.** To archive
     the spec as-is, re-run with `/spec-finalize <key> --archive-closed`.
     To resume, push a new commit on the spec branch and reopen the PR
     via GitHub UI (`gh pr reopen <n>`)." Exit cleanly with no prompt
     and no archive action. If `--archive-closed` was passed, fall
     through to the archive flow.
4. Archive flow (runs when state is `merged`, OR when state is
   `closed` AND `--archive-closed` was passed):
   1. `git fetch origin develop --quiet`
   2. `git switch -c chore/archive-<key> origin/develop`.
   3. `mkdir -p specs/_archive` (idempotent).
   4. `git mv specs/<spec_id> specs/_archive/<spec_id>`. If it fails
      because `specs/<spec_id>` doesn't exist on `origin/develop`
      yet, confirm via
      `git ls-tree origin/develop specs/<spec_id>`; if genuinely
      absent, copy the local spec files into
      `specs/_archive/<spec_id>/` and `git add` them instead.
   5. Write the archived `handoff.json` **before** committing, at
      `specs/_archive/<spec_id>/handoff.json`. Set `archived_at`,
      `archive_branch`, `archive_reason` (`"merged"` normally,
      `"abandoned"` when via `--archive-closed`) and carry over
      everything recorded at first run. Leave `archive_pr_number` /
      `archive_pr_url` `null` (the archive PR doesn't exist yet).
   6. Stage the moved folder **and** the handoff.json update explicitly
      (`git add specs/_archive/<spec_id>` — never `git add -A`), then
      commit on `chore/archive-<key>`:
      `chore: archive <spec_id> (merged via PR #<n>)`.
   7. **Open the archive PR (mandatory).** If `reviewers.default` is
      non-empty, use its first entry as `--reviewer`; otherwise omit
      the flag entirely (solo-maintainer repos have none configured —
      this is normal, not an error). Call `/create-pr --title "chore:
      archive <spec_id>" --body "<archive PR body>"` (plus
      `--reviewer <user>` if resolved). Capture `archive_pr_number` and
      `archive_pr_url`.
   8. **Record the PR back-reference.** Set `archive_pr_number` /
      `archive_pr_url` in `specs/_archive/<spec_id>/handoff.json`, then
      commit just that file (`docs: record archive PR #<archive_n> in
      handoff.json`) and `git push`. Never amend the archive commit.
   9. Print: "Spec <spec_id> archived on `chore/archive-<key>`; archive
      PR #<archive_n> opened<reviewer note if any>. It can only merge
      after the implementation PR #<n> merges."
5. The archive PR is **always** opened — never optional or batched.

## Archive PR description body

```markdown
## Summary

Archive bookkeeping for spec <spec_id>, merged via PR #<n>.
Moves `specs/<spec_id>/` → `specs/_archive/<spec_id>/`.

## Notes

Created by `/spec-finalize` after the implementation PR merged. No
code changes — audit-trail move only.

---

🤖 Generated with [Claude Code](https://claude.com/claude-code)
```

When via `--archive-closed`, replace the Summary line with: "Archive
bookkeeping for spec <spec_id>, whose PR #<n> was closed without
merging (abandoned work)."

## Content synthesis (PR title and body)

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
` + `` `specs/<spec_id>/` `` + ` (will move to ` + `` `specs/_archive/<spec_id>/`
`` + ` after merge).`

## handoff.json schema

Full field contract: `.specs/handoff-schema.md`. The snapshot below is
the `/spec-finalize` view.

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
  "archived_at": null,
  "archive_branch": null,
  "archive_reason": null,
  "archive_pr_number": null,
  "archive_pr_url": null
}
```

## Reusable-knowledge candidates

Inherits from `/spec-handoff`. Run on first `/spec-finalize`
invocation (immediately after PR opens), not at merge time. Ritual
canon lives in `.specs/lib/memory-candidates.md`; run
`.specs/lib/check-sync.sh` after edits.

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

## Memory PR

When the reusable-knowledge step above saves at least one memory,
those files are committed and shipped in their **own** PR — separate
from the impl PR and the archive PR. Run this at the first
`/spec-finalize` invocation, immediately after the candidates are saved.

Flow (run only if ≥1 memory was saved this run):

1. Note the memory files created/modified (paths under
   `.serena/memories/...`). The current branch is the impl branch;
   these files are uncommitted in its working tree.
2. `git fetch origin develop --quiet`.
3. `git switch -c chore/memories-<key> origin/develop`. The saved
   memory files carry over to the new branch.
4. `git add` each saved memory file explicitly — never `git add -A`.
5. Commit: `docs: add Serena memories for <spec_id>`.
6. Resolve the reviewer exactly as the archive flow does: first entry
   of `reviewers.default` if non-empty, else omit `--reviewer`. Call
   `/create-pr --title "docs: add Serena memories for <spec_id>"
   --body "<memory PR body>"` (plus `--reviewer <user>` if resolved).
   Capture `memory_pr_number` and `memory_pr_url`.
7. `git switch <impl_branch>`.
8. Update `handoff.json`: set `memory_branch`, `memory_pr_number`,
   `memory_pr_url`.

If no candidate was saved, skip this entire flow.

### Memory PR description body

```markdown
## Summary

Serena memories captured from spec <spec_id> (implementation PR #<n>).

## Memories

{one bullet per saved memory: `- \`.serena/memories/<name>.md\` — <one-line title>`}

## Notes

Created by `/spec-finalize` after the implementation PR opened.
Documentation only — no code changes. Independent of PR #<n> and
mergeable on its own.

---

🤖 Generated with [Claude Code](https://claude.com/claude-code)
```

## What this skill does NOT do

- Does not commit the spec implementation — that's `/spec-handoff`.
- Does not push the implementation branch — `/create-pr` does that
  on its own first run.
- Does not modify proposal.md, plan.md, tasks.md.
- Does not merge PRs. The memory PR (first run) and archive PR (after
  merge) are opened automatically (and assigned to a reviewer if one
  is configured), but landing each stays a human decision via GitHub
  UI/`gh pr merge`.
- Does not comment anywhere — this repo has no issue tracker or chat
  webhook configured.

## Failure modes and recovery

| Symptom | Cause | Recovery |
|---|---|---|
| First run: branch has no commits ahead of develop | `/spec-handoff` didn't run, or commit was lost | Run `/spec-handoff` first, or inspect `git log` |
| First run: `/create-pr` returns an auth/permission error | gh token missing repo write scope | See `/create-pr` failure modes. Re-run `/spec-finalize` after fixing — idempotent up to PR creation |
| First run: PR opens but handoff.json write fails | Disk / permission error | Inspect `specs/<spec_id>/`; write manually with the URL `/create-pr` printed |
| First run: memory PR `/create-pr` fails after the commit lands | gh auth issue or transient error | The memory commit on `chore/memories-<key>` is already in place. Re-run `/create-pr --fill` (with `--reviewer` if configured) from that branch, then update `handoff.json` manually. Return to the impl branch afterward |
| Status check: handoff.json missing `pr_number` | Manual edit broke the file | Restore from git history or re-derive: `gh pr list --head <branch>` |
| Status check: PR closed without merge | Reviewer rejected, work abandoned | Skill exits cleanly with a terminal-state message and no archive action. To archive as-is, re-run with `--archive-closed`. To resume, push a new commit and reopen the PR via `gh pr reopen <n>` |
| Archive flow: `specs/<spec_id>` not on origin/develop | Original PR didn't include the spec folder | Copy local spec files into `specs/_archive/<spec_id>/` and stage; audit trail preserved |
| Archive flow: `/create-pr` for the archive PR fails | gh auth issue or transient error | The archive commit is already in place. Re-run `/create-pr --fill` from that branch later, then update `handoff.json` manually |
| Archive flow: no reviewer set | `reviewers.default` empty (solo-maintainer default) | Normal — archive PR opens without `--reviewer`. Add `reviewers.default`, or set a reviewer manually in GitHub UI, if/when a second maintainer joins |
| User reports PR was closed and reopened with a new number | Unlikely on GitHub (reopen keeps the number) but handle defensively | Update `handoff.json` `pr_number` manually and re-run if it ever happens |

## Examples

### First run (open PR)

```
/spec-finalize add-rain-radar-toggle
```
- handoff.json absent → PR-open mode.
- Reads proposal.md, synthesizes title+body (Tier A).
- Calls `/create-pr --title "..." --body "..."`.
- `/create-pr` pushes branch, runs `gh pr create`, returns `pr_number: 12`.
- Writes `specs/add-rain-radar-toggle/handoff.json`.
- Surfaces 1 reusable-knowledge candidate from decisions.md; user saves it.
- Branches `chore/memories-add-rain-radar-toggle` off origin/develop,
  commits `docs: add Serena memories for add-rain-radar-toggle`, opens
  the memory PR via `/create-pr ...` → `pr_number: 13`; switches back
  to the impl branch and records `memory_pr_*` in handoff.json.
- Output: "Impl PR #12 opened; memory PR #13 opened. Re-run
  `/spec-finalize add-rain-radar-toggle` after PR #12 merges to
  archive."

### Status check, merged

```
/spec-finalize add-rain-radar-toggle
```
- state == "MERGED".
- `git fetch origin develop`, switch to
  `chore/archive-add-rain-radar-toggle` off origin/develop, `git mv`,
  write handoff.json, commit
  `chore: archive add-rain-radar-toggle (merged via PR #12)`.
- `/create-pr --title "chore: archive add-rain-radar-toggle" ...` →
  `pr_number: 14`.
- Sets `archive_pr_number: 14` in the archived handoff.json, commits
  `docs: record archive PR #14 in handoff.json`, pushes.
- Output: "Spec add-rain-radar-toggle archived on
  `chore/archive-add-rain-radar-toggle`; archive PR #14 opened. It can
  only merge after PR #12 merges."

### Pipe into loop for hands-off polling

```
/loop 30m /spec-finalize add-rain-radar-toggle
```
Runs the status-check every 30 min until the PR is merged, then
auto-archives on the next tick.

## Reference

- Templates: `.specs/templates/`.
- Config: `.specs/config.json` — `pr.*`, `branch.naming.<type>`,
  `reviewers.default`.
- Sibling skills: `/spec-new`, `/spec-plan`, `/spec-implement`,
  `/spec-handoff`, `/spec-status`.
- Sub-skill: `/create-pr`.
- `/loop` (when piped).
- Handoff contracts: `.specs/HANDOFFS.md` — H5 (PR-open), H6 (status-check).
- Config schema: `.specs/config.schema.md`.
- Choosing how to finalize: `.specs/CHOOSING_FINALIZE.md`.
- Candidate discovery: `.specs/SPEC_DISCOVERY.md`.
- Backlog: `.specs/IMPROVEMENTS.md`.
