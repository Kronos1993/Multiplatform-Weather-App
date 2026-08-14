# handoff.json schema

`handoff.json` is the per-spec finalize record. It lives at
`specs/<KEY>/handoff.json` (active) and travels with the spec folder to
`specs/_archive/<KEY>/handoff.json` when the spec is archived. It is the
single artifact that links a spec to its pull request(s) and records
the archive outcome.

Adapted from a Smartmatic-internal `.specs/` workflow lineage; the
schema shape is stack-agnostic. Field names changed from `mr_*` to
`pr_*` throughout — this repo is GitHub-hosted, not GitLab.

Three skills touch it:

- **`/spec-finalize`** writes it (PR-open mode creates it; status-check
  mode fills the archive fields). Post-merge archive.
- **`/spec-ship`** writes it (one-shot: impl + memory + pre-merge
  archive). Adds the `archived_pre_merge` / `shipped_via` fields.
- **`/spec-status`** reads it (drill-in mode) to show PR URLs and the
  pre-merge-archive warning.

If a writer adds or renames a field, update this file and the reader in
the same change.

---

## Field reference

`Writer` = which skill first populates the field with a non-null value.
`Both` = `/spec-finalize` and `/spec-ship` both write it. Types: ISO
8601 = `YYYY-MM-DDThh:mm:ssZ`.

| Field | Type | Required | Writer | Meaning |
|---|---|---|---|---|
| `spec_id` | string | yes | Both | The spec key, e.g. `weather-radar-layers`. |
| `branch` | string | yes | Both | The implementation branch the impl PR was opened from. |
| `target_branch` | string | yes | Both | PR target, normally `develop` (see `.specs/config.json` `pr.default_target`). |
| `pr_number` | integer | yes | Both | The **impl** PR number. |
| `pr_url` | string | yes | Both | The impl PR URL. |
| `opened_at` | ISO 8601 | yes | Both | When the impl PR was opened. |
| `opened_via` | string | yes | Both | Synthesis tier used for the impl PR content: `tier-A` (synthesized from proposal), `tier-B` (`--fill`), or `tier-C` (interactive). |
| `memory_candidates_surfaced` | boolean | yes | Both | Whether the memory-candidate ritual ran (set `true` even if zero memories were saved). |
| `memory_branch` | string \| null | yes | Both | `chore/memories-<KEY>` when ≥1 reusable-knowledge candidate was saved as a Serena memory and shipped in its own PR; `null` otherwise. |
| `memory_pr_number` | integer \| null | yes | Both | The memory PR number; `null` when no memory PR was opened. |
| `memory_pr_url` | string \| null | yes | Both | The memory PR URL; `null` likewise. |
| `archived_at` | ISO 8601 \| null | yes | Both | When the spec folder was archived. `null` until archive happens. |
| `archive_branch` | string \| null | yes | Both | `chore/archive-<KEY>`; `null` until the archive flow runs. |
| `archive_reason` | string \| null | yes | Both | `"merged"` (normal case) or `"abandoned"` (`/spec-finalize --archive-closed`). `null` until the archive flow runs. |
| `archive_pr_number` | integer \| null | yes | Both | The archive PR number; `null` until the archive PR opens (and stays `null` under `/spec-ship --no-archive-pr`). |
| `archive_pr_url` | string \| null | yes | Both | The archive PR URL; `null` likewise. |
| `archived_pre_merge` | boolean | `/spec-ship` only | `/spec-ship` | Present and `true` only in `/spec-ship` output — tells the reader the spec was archived **before** the impl PR merged. Absent in `/spec-finalize` output. |
| `shipped_via` | string | `/spec-ship` only | `/spec-ship` | Informational origin marker: `spec-ship`. |

There can be **up to three PRs per spec**: the impl PR (`pr_*`), the
memory PR (`memory_pr_*`), and the archive PR (`archive_pr_*`). Any of
the latter two may be `null`.

## How the fields get populated

### `/spec-finalize` (post-merge, two runs)

1. **PR-open run** (no `handoff.json` yet): opens the impl PR, writes
   `spec_id` … `memory_*`, leaves all `archived_*` / `archive_*` fields
   `null`. Opens the memory PR and fills `memory_*` if memories were
   saved.
2. **Status-check run(s)** (`handoff.json` exists): polls the impl PR.
   When merged (or closed + `--archive-closed`), runs the archive flow —
   writes `archived_at` / `archive_branch` / `archive_reason` into the
   **archive commit**, then records `archive_pr_number` / `archive_pr_url`
   in a **second commit** the archive PR carries.

### `/spec-ship` (pre-merge, one run)

All impl + memory fields, `archive_branch`, `archived_at`,
`archive_reason: "merged"` (optimistic — paired with
`archived_pre_merge: true`), and `shipped_via` are known up front. The
file is **written and committed on the impl branch** (with
`archive_pr_*` null) so it is tracked and reachable by `/spec-status`
before the impl PR merges.

## Sequencing invariant (shared by both writers)

`archive_pr_*` cannot be known until the archive PR opens, and the PR
cannot open without a commit to build against. Both writers write
`handoff.json` with `archive_pr_*` null into the archive commit, then
record the PR number/URL in a follow-up commit on the same archive
branch that the PR carries. Never amend the archive commit once pushed
— amend would force-push.

## Reader: `/spec-status`

Drill-in mode (`/spec-status <KEY>`) reads `handoff.json` when present:

- Shows impl, memory, and archive PR URLs in the Handoff block.
- `archived_pre_merge: true` triggers the pre-merge-archive warning and
  the `ARCHIVED_PRE_MERGE` state.
- `archive_reason` (`merged` / `abandoned`) is shown in the archived
  drill-in.
- Does **not** query `gh` for live merge state — fields are surfaced
  as-recorded; the user verifies at the URL.

## Example — fully populated (post-merge, `/spec-finalize`)

```json
{
  "spec_id": "weather-radar-layers",
  "branch": "feature/weather-radar-layers",
  "target_branch": "develop",
  "pr_number": 12,
  "pr_url": "https://github.com/Kronos1993/Multiplatform-Weather-App/pull/12",
  "opened_at": "2026-08-14T18:23:00Z",
  "opened_via": "tier-A",
  "memory_candidates_surfaced": true,
  "memory_branch": "chore/memories-weather-radar-layers",
  "memory_pr_number": 13,
  "memory_pr_url": "https://github.com/Kronos1993/Multiplatform-Weather-App/pull/13",
  "archived_at": "2026-08-15T09:10:00Z",
  "archive_branch": "chore/archive-weather-radar-layers",
  "archive_reason": "merged",
  "archive_pr_number": 14,
  "archive_pr_url": "https://github.com/Kronos1993/Multiplatform-Weather-App/pull/14"
}
```

## Example — pre-merge archive (`/spec-ship`)

Adds `archived_pre_merge` + `shipped_via`; otherwise the same shape.

```json
{
  "spec_id": "weather-radar-layers",
  "branch": "feature/weather-radar-layers",
  "target_branch": "develop",
  "pr_number": 12,
  "pr_url": "https://github.com/Kronos1993/Multiplatform-Weather-App/pull/12",
  "opened_at": "2026-08-14T18:23:00Z",
  "opened_via": "tier-A",
  "memory_candidates_surfaced": true,
  "memory_branch": "chore/memories-weather-radar-layers",
  "memory_pr_number": 13,
  "memory_pr_url": "https://github.com/Kronos1993/Multiplatform-Weather-App/pull/13",
  "archived_at": "2026-08-14T18:24:00Z",
  "archive_branch": "chore/archive-weather-radar-layers",
  "archive_reason": "merged",
  "archive_pr_number": 14,
  "archive_pr_url": "https://github.com/Kronos1993/Multiplatform-Weather-App/pull/14",
  "archived_pre_merge": true,
  "shipped_via": "spec-ship"
}
```

## Maintenance

- Update this file together with the writer skill when a field is added,
  renamed, or changes nullability.
- The three skills carry a one-line pointer back to this file
  (`Schema: .specs/handoff-schema.md`).
