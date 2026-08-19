---
name: release
description: Check whether `develop` has changes not yet released to `main`, open the promote PR (`develop` → `main`) that triggers `.github/workflows/publish-android.yml` on merge, and prepare `composeApp/build.gradle.kts` for the next development version (versionCode +1, versionName's last segment +1). Use when the user asks to publish/release the app, or to check if there's anything pending to publish.
---

## Usage

```
/release
```

No arguments — the skill is interactive where a decision is needed (the next version's values).

## Preconditions

- `gh` is on PATH and authenticated (`gh auth status`).
- Working tree is clean, or the user has confirmed it's fine to proceed with what's there.

## What this skill does

### 1. Check for pending changes

```
git fetch origin main develop
git rev-list --count origin/main..origin/develop
```

- If the count is `0`: report "nothing to publish — `main` already has everything from
  `develop`" and stop.
- If the count is `> 0`: continue. These commits, and whatever `versionCode`/`versionName` are
  currently set in `composeApp/build.gradle.kts` on `develop`, are what's about to publish.

### 2. Open the promote PR (this is what actually triggers the publish)

Read the current `versionCode`/`versionName` from `composeApp/build.gradle.kts` on
`origin/develop` and put them in the PR title/body so it's clear what's shipping.

```
gh pr create --base main --head develop \
  --title "release: vX.Y.Z.W (versionCode N)" \
  --body "Promotes develop -> main. Merging this triggers .github/workflows/publish-android.yml, which builds, signs, and uploads the Android App Bundle to the Play Store internal track."
```

Report the PR URL. **Do not merge it** — merging is a deliberate human action; this skill only
opens it. Merging is what actually fires the publish workflow.

### 3. Prepare `develop` for the next version

This is separate from step 2 and happens regardless of whether the user merges the promote PR
right away — it just gets `develop` ready so nobody has to remember to bump the version later.

1. Show the user the current version (`versionCode`, `versionName`, and its segment/"digit"
   count — e.g. `1.0.1.3` is 4 segments) and the proposed next version:
   - `versionCode`: always **current + 1**, never anything else.
   - `versionName`: same segment count as the current one by default, with the **last segment
     incremented by 1** (e.g. `1.0.1.3` → `1.0.1.4`).
2. Ask the user to confirm or override — in particular, let them change the segment count for
   the next version (e.g. dropping to `1.1.0` for a minor bump, or adding a segment). Rules when
   the count changes:
   - More segments than before: pad the new trailing segments with `0` before incrementing the
     last one (e.g. 3→4 segments: `1.0.1` → `1.0.1.0` → increment → `1.0.1.1`... but check with
     the user which segment they actually want incremented — don't guess silently).
   - Fewer segments than before: truncate to the leading N segments, then increment the last of
     those, and say explicitly that precision from the dropped segments is lost.
3. Create a new branch off `develop` (name per `.specs/config.json` `branch.naming` —
   `chore/{key}`, e.g. `chore/prepare-next-version`).
4. Edit `versionCode`/`versionName` in `composeApp/build.gradle.kts` to the confirmed next
   values (plain `Edit` — Gradle `.kts` files don't need Serena's symbolic tools per root
   `CLAUDE.md`).
5. Commit via `/commit` (conventional `build:` type), then open a PR into `develop` via
   `/create-pr` (default target is already `develop`).
6. Report both PR URLs (promote PR from step 2, prepare-next-version PR from step 3) to the user.

## What this skill does NOT do

- Does not merge either PR — both are opened for human review/approval.
- Does not separately resolve `versionCode` for the Play Store upload — the Play Publisher
  plugin's `resolutionStrategy = FAIL` in `composeApp/build.gradle.kts` means whatever
  `versionCode` is set in the repo at merge-to-`main` time is exactly what gets uploaded (`AUTO`
  was tried first but breaks on an app's very first release — see
  [Triple-T/gradle-play-publisher#899](https://github.com/Triple-T/gradle-play-publisher/issues/899)).
  This makes step 3 load-bearing, not cosmetic: every promote PR must carry a `versionCode` higher
  than whatever Play Console already has, or the upload is rejected.
- Does not build, sign, or upload anything itself — that's entirely
  `.github/workflows/publish-android.yml`, which only runs on a push to `main`, and always
  publishes to the **internal** track only. It never touches production.
- Does not promote a release to production (or beta/alpha) — that's a separate, deliberate manual
  step, never automatic: `gh workflow run promote-android.yml -f to_track=production` (or run it
  from the Actions tab). This only moves the already-uploaded internal release to another track;
  it doesn't rebuild or re-sign anything.
- iOS/TestFlight and Desktop publishing are out of scope (Android → Play Store only, see
  `.github/workflows/publish-android.yml`).

## Reference

- Sibling skills: `/commit` (step 3's commit), `/create-pr` (step 3's PR; also the pattern step 2
  is modeled on, base/head reversed).
- `.github/workflows/publish-android.yml`: the actual build/sign/publish pipeline, triggered by
  `push: branches: [main]`. Always targets the internal track.
- `.github/workflows/promote-android.yml`: manual (`workflow_dispatch`) promotion of the current
  internal release to production/beta/alpha, via the Play Publisher plugin's
  `promoteReleaseArtifact` task. Never runs on its own.
- `composeApp/build.gradle.kts`: `signingConfigs["release"]` (env-var driven, CI-only) and the
  `play { ... }` block (track, resolution strategy).
