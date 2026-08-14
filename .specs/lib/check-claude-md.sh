#!/usr/bin/env bash
# .specs/lib/check-claude-md.sh — verify each skill's declared CLAUDE.md
# dependencies still exist as headings in the root CLAUDE.md.
#
# Each spec-* skill (and /create-pr) declares the CLAUDE.md sections it
# leans on in its frontmatter, nested under `metadata:` (kept off the
# top level so the frontmatter stays within the Agent Skills spec):
#   metadata:
#     claude_md_requires: '["Code style and analysis", ...]'
# This script checks every listed name appears as a `##` or `###`
# heading in the root CLAUDE.md, so a section rename/removal is caught
# before it silently breaks a skill. Run it after editing CLAUDE.md
# headings or a skill's claude_md_requires list.
#
# Exit codes:
#   0 = every declared section is present
#   1 = at least one declared section is missing from CLAUDE.md
#   2 = setup error (CLAUDE.md or skills dir not found)
#
# Note: no `set -e` — we want to report every missing reference across
# all skills before exiting non-zero.

LIB_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$LIB_DIR/../.." && pwd)"
CLAUDE_MD="$REPO_ROOT/CLAUDE.md"
SKILLS_DIR="$REPO_ROOT/.claude/skills"

if [[ ! -f "$CLAUDE_MD" ]]; then
  echo "CONFIG ERROR: CLAUDE.md not found at $CLAUDE_MD" >&2
  exit 2
fi
if [[ ! -d "$SKILLS_DIR" ]]; then
  echo "CONFIG ERROR: skills dir not found at $SKILLS_DIR" >&2
  exit 2
fi

# Collect CLAUDE.md heading texts (## and ###), hashes and surrounding
# whitespace stripped, into a temp file for exact-line matching.
HEADINGS="$(mktemp 2>/dev/null || mktemp -t claudemdhdr)"
trap 'rm -f "$HEADINGS"' EXIT
grep -E '^#{2,3} +' "$CLAUDE_MD" | sed -E 's/^#{2,3} +//; s/[[:space:]]+$//' > "$HEADINGS"

EXIT_CODE=0
checked=0

shopt -s nullglob
for skillfile in "$SKILLS_DIR"/*/SKILL.md; do
  # Pull the claude_md_requires line out of the frontmatter (it lives
  # near the top; bound the scan so a body mention can't be mistaken
  # for the declaration). The key is nested under `metadata:` so it is
  # indented — allow leading whitespace.
  reqline="$(head -n 30 "$skillfile" | grep -E '^[[:space:]]*claude_md_requires:' || true)"
  [[ -z "$reqline" ]] && continue
  checked=$((checked + 1))

  skillname="$(basename "$(dirname "$skillfile")")"

  # Extract the quoted section names (empty list → nothing to check).
  names="$(printf '%s' "$reqline" | grep -oE '"[^"]+"' | sed 's/"//g')"
  [[ -z "$names" ]] && continue

  while IFS= read -r name; do
    [[ -z "$name" ]] && continue
    if ! grep -Fxq "$name" "$HEADINGS"; then
      echo "MISSING: $skillname declares CLAUDE.md section \"$name\" — not found as a ## or ### heading" >&2
      EXIT_CODE=1
    fi
  done <<EOF
$names
EOF
done

if [[ $EXIT_CODE -eq 0 ]]; then
  echo "All claude_md_requires sections present in CLAUDE.md (checked $checked skill(s))."
fi
exit $EXIT_CODE
