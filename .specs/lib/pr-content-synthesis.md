# pr-content-synthesis

Canonical Tier A → B → C PR title + description synthesis. Used by:

- `/spec-finalize` (PR-open mode, first run)
- `/spec-ship` (impl PR opened in the one-shot flow)

Adapted from a Smartmatic-internal `.specs/lib/mr-content-synthesis.md`
(GitLab MR synthesis) — renamed for this repo's GitHub `/create-pr`.
This repo has no `ui_changes`/`settings_contract_changes` frontmatter
concept, so those sections are dropped entirely rather than made
conditional.

## Placeholder

| Sigil | Meaning | Skill values |
|---|---|---|
| `%%audit_trail%%` | One-line reviewer-facing note stating where the spec folder lives at PR-open time | `/spec-finalize`: `Spec audit trail: ` + `` `specs/<spec_id>/` `` + ` (will move to ` + `` `specs/_archive/<spec_id>/` `` + ` after merge).`<br>`/spec-ship`: `Spec audit trail: ` + `` `specs/<spec_id>/` `` + ` (already moved to ` + `` `specs/_archive/<spec_id>/` `` + ` on ` + `` `chore/archive-<KEY>` `` + ` — see archive PR).` |

Each skill substitutes the placeholder line with its own value when
copying the canon into its body. The linter ignores the substituted
line position-wise.

## How to copy into a skill

Wrap the canonical content (everything below the horizontal rule
that follows this paragraph) with anchor markers:

    <!-- lib:start:pr-content-synthesis -->
    [verbatim from below, with %%audit_trail%% substituted]
    <!-- lib:end:pr-content-synthesis -->

Run `./check-sync.sh` from this directory to verify after editing.

---

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
