# Choosing how to finalize a spec

Once a spec's implementation is done (all `tasks.md` Implementation +
Pre-handoff boxes ticked, working tree dirty), there are **four** ways
to take it to a pull request and archive. They differ on two axes:
*when the commit happens* and *when the spec folder is archived
(pre-merge vs post-merge)*. This page is the decision tree so a new
contributor doesn't have to learn all four by reading every skill.

These are the consumer ends of handoffs **H4–H7** in
`.specs/HANDOFFS.md`; the artifacts they read/write follow
`.specs/handoff-schema.md`; the immutability rules they obey are in
`.specs/IMMUTABILITY.md`.

Adapted from a Smartmatic-internal `.specs/` workflow lineage — the
decision tree itself is stack-agnostic. Terminology changed throughout:
GitLab **MR** → GitHub **PR**, `glab` → `gh`, integration branch →
`develop` (see `.specs/config.json` `pr.default_target` — unconfirmed,
see its `_doc`).

---

## Decision tree

```
Is the work already committed on a branch?
│
├─ NO — working tree is dirty (the normal case)
│   │
│   └─ Do you want the spec folder archived only AFTER the PR merges
│      (safe, reversible) or immediately, before merge (eager)?
│      │
│      ├─ AFTER merge  → PATH A:  /spec-handoff   then   /spec-finalize
│      │                 (merge-gated archive — the default, safest)
│      │
│      └─ BEFORE merge → archive PR now, or batch several later?
│            │
│            ├─ now    → PATH B:  /spec-ship
│            │            (one-shot: commit + impl PR + pre-merge archive + archive PR)
│            │
│            └─ batch  → PATH C:  /spec-ship --no-archive-pr
│                         (one-shot, but no archive PR — open one for several specs later)
│
└─ YES — work was committed out-of-band (a hotfix, external tooling, a
         branch picked up from elsewhere)
    │
    └─ PATH D:  /spec-finalize  directly
                (skip /spec-handoff; finalize detects the existing commit)
```

## At a glance

| | Commit step | Impl PR | Archive timing | Archive PR | Memory PR* |
|---|---|---|---|---|---|
| **A** handoff → finalize | `/spec-handoff` | `/spec-finalize` 1st run | **post-merge** | always (mandatory, reviewer-assigned if any) | finalize 1st run |
| **B** spec-ship | `/spec-ship` | `/spec-ship` | **pre-merge** (eager) | yes | `/spec-ship` |
| **C** spec-ship --no-archive-pr | `/spec-ship` | `/spec-ship` | **pre-merge** (eager) | no (batch later) | `/spec-ship` |
| **D** direct finalize | (already done, external) | `/spec-finalize` 1st run | **post-merge** | always | finalize 1st run |

\* The memory PR opens in every path **iff** ≥1 reusable-knowledge
candidate was saved as a Serena memory; it is never suppressed by
`--no-archive-pr`.

## When to use which

- **PATH A — `/spec-handoff` → `/spec-finalize` (default).** The safe,
  reversible path. Use this unless you have a specific reason not to —
  the archive can't get ahead of the merge.

- **PATH B — `/spec-ship`.** One-shot: commits, opens the impl PR, and
  archives the spec folder **pre-merge**, opening the archive PR
  immediately. Use when you're confident the impl PR will merge and
  want to be done in one invocation.

- **PATH C — `/spec-ship --no-archive-pr`.** Same as B but skips
  opening the archive PR; use when shipping several specs and batching
  their archive moves into one PR later.

- **PATH D — `/spec-finalize` directly.** When the commit already
  exists (a hotfix or a branch you inherited), skip `/spec-handoff`.
  `/spec-finalize` auto-detects mode by the presence of `handoff.json`.

## Notes

- **Mode auto-detection, not a flag.** `/spec-finalize` picks PR-open
  vs status-check purely from whether `specs/<KEY>/handoff.json`
  exists — you run the *same command* repeatedly (great for `/loop`).
- **Pre-merge (B/C) vs post-merge (A/D) is the real fork.** Pre-merge is
  faster and single-shot but optimistic — if the impl PR is later
  closed without merging, the spec must be unarchived (recipe in
  `/spec-ship` SKILL.md "Failure modes").
- **All paths open the same set of up-to-three PRs** (impl, memory,
  archive); they differ only in *when* and *whether batched*.
- This is a solo-maintained repo today — the "reviewer-assigned"
  language above is a no-op when `reviewers.default` is empty (see
  `.specs/config.json`); no PR waits on a reviewer that doesn't exist.

## Reference

- `.specs/HANDOFFS.md` — H4 (→ handoff), H5/H6 (→ finalize), H7 (→ ship).
- `.specs/handoff-schema.md` — the `handoff.json` each path writes.
- `.specs/IMMUTABILITY.md` — the seal-and-move rules at archive.
- Skills: `/spec-handoff`, `/spec-finalize`, `/spec-ship`, `/create-pr`.
