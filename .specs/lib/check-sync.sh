#!/usr/bin/env bash
# .specs/lib/check-sync.sh — verify that skill lib copies match the canon.
#
# For each .md file under .specs/lib/ (except README.md), find every
# skill that wraps a copy via anchor markers:
#   <!-- lib:start:<name> -->
#   <content>
#   <!-- lib:end:<name> -->
# and verify that <content> matches the lib's canonical section
# (everything after the first standalone --- line in the lib file).
#
# Placeholders: lines in the canon containing %%snake_case%% are
# wildcards. The corresponding skill line is replaced with the same
# WILDCARD sentinel before comparison, so any substitution passes.
#
# Exit codes:
#   0 = all in sync
#   1 = drift found
#   2 = config error (e.g. unmatched markers, no lib files)

# Note: no `set -e` — we want to keep iterating across all (lib, skill)
# pairs and report every drift before exiting non-zero.

LIB_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$LIB_DIR/../.." && pwd)"

EXIT_CODE=0

check_pair() {
  local libfile="$1" skillfile="$2"
  local libname
  libname="$(basename "${libfile%.md}")"
  local mstart="<!-- lib:start:$libname -->"
  local mend="<!-- lib:end:$libname -->"

  if ! grep -qF "$mstart" "$skillfile"; then
    return 0
  fi
  if ! grep -qF "$mend" "$skillfile"; then
    echo "CONFIG ERROR: $skillfile has $mstart but no $mend" >&2
    return 2
  fi

  local tmpd
  tmpd="$(mktemp -d 2>/dev/null || mktemp -d -t libsync)"
  local canon="$tmpd/canon"
  local copy="$tmpd/copy"
  local canon_norm="$tmpd/canon_norm"
  local copy_norm="$tmpd/copy_norm"

  # Canon: everything after the first standalone --- line in the lib.
  awk 'started {print} /^---$/ && !started {started=1; next}' "$libfile" > "$canon"

  # Skill copy: text between the markers (markers themselves excluded).
  awk -v ms="$mstart" -v me="$mend" '
    index($0, ms) {inside=1; next}
    index($0, me) {inside=0}
    inside
  ' "$skillfile" > "$copy"

  # Normalize: lib lines containing %%snake_case%% become WILDCARD.
  # Skill lines at the same positions also become WILDCARD.
  awk '/%%[a-z_]+%%/ {print "WILDCARD"; next} {print}' "$canon" > "$canon_norm"
  awk 'NR==FNR {mask[FNR]=($0 ~ /%%[a-z_]+%%/); next}
       {if (mask[FNR]) print "WILDCARD"; else print}' "$canon" "$copy" > "$copy_norm"

  if diff -u "$canon_norm" "$copy_norm" > "$tmpd/diff" 2>&1; then
    rm -rf "$tmpd"
    return 0
  fi

  echo "" >&2
  echo "=== DRIFT: $skillfile :: $libname ===" >&2
  cat "$tmpd/diff" >&2
  rm -rf "$tmpd"
  return 1
}

shopt -s nullglob

SKILL_FILES=()
for f in "$REPO_ROOT"/.claude/skills/spec-*/SKILL.md; do
  [[ -f "$f" ]] && SKILL_FILES+=("$f")
done

found_any_lib=0
for libfile in "$LIB_DIR"/*.md; do
  [[ "$(basename "$libfile")" == "README.md" ]] && continue
  found_any_lib=1
  for skillfile in "${SKILL_FILES[@]}"; do
    ec=0
    check_pair "$libfile" "$skillfile" || ec=$?
    [[ $ec -gt $EXIT_CODE ]] && EXIT_CODE=$ec
  done
done

if [[ $found_any_lib -eq 0 ]]; then
  echo "No lib files (other than README.md) found in $LIB_DIR" >&2
  exit 2
fi

if [[ $EXIT_CODE -eq 0 ]]; then
  echo "All lib copies in sync."
fi
exit $EXIT_CODE
