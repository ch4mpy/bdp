#!/usr/bin/env bash

# Leaves a lab branch and switches back to main.
# --keep  : commit local changes (tracked + untracked) before switching.
# --reset : discard local changes to restore the initial lab branch state.
set -euo pipefail

usage() {
  cat >&2 <<'EOF'
Usage: ./labs/scripts/exit-lab.sh [--keep|--reset]

  --keep   Keep current work by committing local changes before switching to main.
  --reset  Restore initial TP branch state by discarding local changes, then switch to main.

Without an argument, an interactive choice is proposed.
EOF
  exit "${1:-1}"
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
    --help|-h)
      usage 0
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
  echo "  k) keep current work (commit tracked + untracked files)"
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
  commit_created="false"
  if [ "$has_local_changes" = "true" ]; then
    git add -A
    if ! git commit --quiet -m "WIP TP"; then
      echo "Failed to create WIP commit. Please check Git identity/configuration and try again." >&2
      exit 1
    fi
    commit_created="true"
  fi

  git switch main

  echo
  if [ "$commit_created" = "true" ]; then
    echo "Work committed on '$current_branch' and switched to 'main'."
    echo "To resume later:"
    echo "  git switch $current_branch"
    echo "  git log --oneline -n 3"
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
