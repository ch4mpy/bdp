#!/usr/bin/env bash

# Leaves a lab branch and switches back to main.
# --keep  : stash local changes (tracked + untracked) before switching.
# --reset : discard local changes to restore the initial lab branch state.
set -euo pipefail

usage() {
  cat >&2 <<'EOF'
Usage: ./labs/scripts/exit-lab.sh [--keep|--reset]

  --keep   Keep current work by stashing local changes before switching to main.
  --reset  Restore initial TP branch state by discarding local changes, then switch to main.

Without an argument, an interactive choice is proposed.
EOF
  exit 1
}

if [ "$#" -gt 1 ]; then
  usage
fi

mode=""
if [ "$#" -eq 1 ]; then
  case "$1" in
    --keep)
      mode="keep"
      ;;
    --reset)
      mode="reset"
      ;;
    *)
      usage
      ;;
  esac
fi

repo_root="$(git rev-parse --show-toplevel 2>/dev/null || true)"
if [ -z "$repo_root" ]; then
  echo "Not inside a Git repository." >&2
  exit 1
fi

cd "$repo_root"
current_branch="$(git branch --show-current)"

if [ -z "$mode" ]; then
  if [ ! -t 0 ]; then
    echo "Non-interactive mode: please pass --keep or --reset." >&2
    exit 1
  fi

  echo "Leave TP branch '$current_branch' and switch to 'main':"
  echo "  k) keep current work (stash tracked + untracked files)"
  echo "  r) restore initial TP state (discard local changes)"

  answer=""
  while [ "$answer" != "k" ] && [ "$answer" != "r" ]; do
    read -r -p "Choice [k/r]: " answer
    answer="$(printf '%s' "$answer" | tr '[:upper:]' '[:lower:]')"
  done

  if [ "$answer" = "k" ]; then
    mode="keep"
  else
    mode="reset"
  fi
fi

has_local_changes="false"
if ! git diff --quiet || ! git diff --cached --quiet || [ -n "$(git ls-files --others --exclude-standard)" ]; then
  has_local_changes="true"
fi

if [ "$mode" = "keep" ]; then
  stash_created="false"
  if [ "$has_local_changes" = "true" ]; then
    stash_message="tp-wip:$current_branch:$(date '+%Y-%m-%d %H:%M:%S')"
    git stash push --include-untracked --message "$stash_message" >/dev/null
    stash_created="true"
  fi

  git switch main

  echo
  if [ "$stash_created" = "true" ]; then
    echo "Work saved in stash and switched to 'main'."
    echo "To resume later:"
    echo "  git switch $current_branch"
    echo "  git stash pop"
  else
    echo "No local changes found. Switched to 'main'."
  fi
  exit 0
fi

# mode == reset
if [ "$has_local_changes" = "true" ]; then
  git reset --hard >/dev/null
  git clean -fd >/dev/null
fi

git switch main

echo
if [ "$has_local_changes" = "true" ]; then
  echo "Local changes discarded. Switched to 'main'."
else
  echo "No local changes found. Switched to 'main'."
fi
