---
name: create-pr
description: Open a pull request (PR) for the current branch using gh (GitHub CLI) — pushes the branch to origin, runs `gh pr create` against `develop`, and returns the PR number and URL. Use when opening or creating a PR. Not for committing (that's `/commit`) and not for merging — this only opens.
---

## Usage

```
/create-pr --title "<title>" --body "<body>" [--target <branch>] [--reviewer <user>]
/create-pr --fill [--target <branch>] [--reviewer <user>]
/create-pr --interactive [--target <branch>] [--reviewer <user>]
```

Modes:

- `--title` + `--body`: explicit content. Recommended when the
  caller (another skill, or you after reading the diff) can synthesize
  a title/body from the change itself.
- `--fill`: pass through to `gh pr create --fill` — gh uses the
  last commit subject as the title and the commit body as the
  description.
- `--interactive`: prompt the user inline for title + body.
  Last resort when `--fill` would also be insufficient.

`--target <branch>` overrides the default target, which is
**`develop`** per `.specs/config.json` `pr.default_target` — this
repo has `develop`, `main`, and `master` on origin with no PR history
yet to confirm the convention from; treat this as provisional until
confirmed with the user.

`--reviewer <user>` adds the GitHub **Reviewer** field via gh's
`--reviewer` flag — a GitHub username. Optional; omit it and no
reviewer is set (this repo's `reviewers.default` is empty today — solo
maintainer).

## Preconditions

- `gh` is on PATH and authenticated (`gh auth status` shows logged
  in). If not installed, install with `brew install gh` (macOS) then
  `gh auth login`. If not authenticated, stop with: "Run `gh auth
  login` and retry."
- Current working tree is **not** `develop`/`main`/`master` — never
  push from any of them directly.
- The branch has at least one commit ahead of the target branch.

## What this skill does

1. Resolve settings from `.specs/config.json` `pr.*`:
   - `cli`: `gh`
   - `host`: `github.com`
   - `repo_slug`: `Kronos1993/Multiplatform-Weather-App`
   - `default_target`: `develop`
   - `first_push_strict_host_checking`: `accept-new`
   If `.specs/config.json` is absent (e.g. this skill used outside the
   spec workflow), fall back to these same values as fixed defaults —
   derive `repo_slug` from `git remote get-url origin` if it differs.
2. Confirm `gh auth status` passes.
3. Push the current branch:
   ```
   git push -u origin HEAD
   ```
4. Build the `gh pr create` invocation:
   - `--base <resolved-target>` (default `develop`)
   - `--head <current-branch>`
   - Mode-specific: `--title ... --body ...` OR `--fill`
   - `--reviewer <user>` when the caller passed `--reviewer`
5. Execute. Parse the PR URL from stdout (`gh pr create` prints it on
   success).
6. Extract the PR number from the URL (the trailing path segment after
   `/pull/`).
7. Return:
   ```
   pr_number: <N>
   pr_url:    <URL>
   target:    <resolved-target>
   ```

## Fallback chain

When explicit `--title`/`--body` and gh rejects it for content
reasons (e.g. unescaped characters), retry once with `--fill`. If that
also fails, ask the user inline (`--interactive`). Non-content failures
(auth, network, repo not found) stop the skill.

## What this skill does NOT do

- Does not commit. Caller commits first.
- Does not assign reviewers/labels beyond the optional `--reviewer` flag.
- Does not poll or wait for merge.
- Does not squash-merge or delete the branch on merge — on GitHub those
  are choices made at merge time (`gh pr merge --squash --delete-branch`)
  by whoever merges, not by the creator. This skill only opens the PR.

## Failure modes and recovery

| Symptom | Cause | Recovery |
|---|---|---|
| `gh auth status` fails | Not logged in | `gh auth login`, then retry. |
| `gh: command not found` | gh CLI not installed | `brew install gh` (macOS), then `gh auth login`. |
| `git push` fails with `Host key verification failed` | First push to host over SSH | Use HTTPS remote (this repo's origin is already HTTPS), or accept the host key once manually. |
| `gh pr create` returns a permissions/403 error | Token/auth missing repo write scope | `gh auth refresh -s repo` or re-run `gh auth login` with the right scopes. |
| `gh pr create` says "no commits between source and target" | Branch even with target | Commit first. |
| `gh pr create` returns "a pull request for branch ... already exists" | A PR for this branch already exists | Fetch via `gh pr view <branch>`. |
| `gh pr create --fill` hangs or exits without creating a PR | Unlikely with gh (no confirmation prompt by default) — if it happens, check `gh --version` is current | Update gh, retry. |

## Reference

- Sibling skill: `/commit` (local commit; see
  `.claude/skills/commit/SKILL.md`).
- This repo's `.specs/` spec-driven workflow (`/spec-new` … `/spec-ship`)
  calls `/create-pr` from `/spec-finalize` and `/spec-ship`; see
  `.specs/EXTERNAL_SKILLS.md` for that contract. `/create-pr` can also
  be called directly once a branch is ready to ship, outside that
  workflow.
