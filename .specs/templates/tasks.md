---
spec_id: <short-kebab-slug>
mirrors: plan.md
generated_by: /spec-plan
---

> **Blockers**: <comma-separated OQ-N from proposal.md `blockers:`, or "none">
> **Depends on**: <comma-separated spec IDs from `depends_on:`, or "none">
>
> /spec-implement refuses to start while any blocker remains OR any
> dependency is not yet archived. To clear:
> - blocker → resolve the OQ in proposal.md §9, remove its ID from `blockers:`
> - dependency → wait for the depended spec to land in `specs/_archive/`,
>   then remove its ID from `depends_on:`
>
> Re-run /spec-plan after either to regenerate this banner.

# Tasks: <title>

<!--
CHECKBOX MIRROR OF plan.md

This file is the operational view /spec-implement updates as it works.
Every step in plan.md gets one checkbox here, in the same order, with
the same numbering. Sub-bullets capture sub-checks that must pass before
the parent step is marked done.

Rules:
- Tick a box ONLY when its verification (from plan.md) passes.
- Do not edit step text after /spec-plan generates this file. If the
  plan changes, re-run /spec-plan — do not patch tasks.md by hand.
- /spec-handoff refuses to run if any box is unchecked.
-->

## Implementation

- [ ] **Step 1** — <title from plan.md>
  - [ ] Code change applied
  - [ ] Build green (`./gradlew :composeApp:assembleDebug`)
  - [ ] Verification met (per plan.md)
- [ ] **Step 2** — <title from plan.md>
  - [ ] Code change applied
  - [ ] Build green
  - [ ] Verification met
- [ ] **Step 3** — …

## Pre-handoff checks

<!-- Repo-wide gates run once after all implementation steps pass. -->

- [ ] Full build green (`./gradlew build` — covers Android + JVM/desktop targets; run `./gradlew :composeApp:assembleDebug` at minimum if only Android was touched)
- [ ] iOS build manually verified via Xcode if any `iosMain`/`iosApp/` file changed (no headless build path exists — see `.specs/EXTERNAL_SKILLS.md`)
- [ ] No new logs/prints touch the WeatherAPI key or any other credential
- [ ] Every touched commonMain `expect` has a matching `actual` in every affected source set (manual review — see `.specs/config.json` `architecture.expect_actual_parity_required`)
- [ ] Any touched user-facing string shown by both Compose UI and native iOS code is updated in both `composeResources` and `iosApp/*.strings` (`architecture.dual_localization_required`)
- [ ] No automated-test checkbox invented — this repo has zero test source sets (confirmed in `CLAUDE.md`); verification is build-green + manual run only
- [ ] Acceptance criteria from proposal.md §8 satisfied
- [ ] proposal.md frontmatter `blockers: []` (empty)
- [ ] proposal.md frontmatter `depends_on:` either `[]` OR every listed ID has a folder under `specs/_archive/`
- [ ] All §9 OQs resolved or marked out-of-band
- [ ] No plan.md step retains `_(skeleton)_` (each expanded with concrete sub-checks)

## Handoff

- [ ] Branch created (`<type>/<key-or-slug>`)
- [ ] `/commit` executed
- [ ] Branch pushed
- [ ] PR opened against `develop`
- [ ] Spec folder archived to `specs/_archive/<key>/`
- [ ] Reusable-knowledge candidates from `decisions.md` proposed
