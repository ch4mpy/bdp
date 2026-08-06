#!/usr/bin/env bash
# (Re)crée la branche lab/<id-tp> à partir de main, avec l'énoncé du TP <id-tp>.
#
# Usage : ./labs/scripts/build-lab.sh <id-tp>   (ex: ./labs/scripts/build-lab.sh 1.4.3)
#
# Peut être relancé à tout moment. Si la branche lab/<id-tp> existe déjà en
# local, une question est posée : la garder telle quelle (aucun changement) ou
# la réinitialiser depuis origin/main (l'énoncé reflète alors l'état actuel de
# main, mais toute modification locale déjà faite sur lab/<id-tp> est perdue).
set -euo pipefail

usage() {
  echo "Usage: $0 <id-tp>" >&2
  echo "  ex: $0 1.4.3" >&2
  exit 1
}

[ $# -eq 1 ] || usage
LAB_ID="$1"

REPO_ROOT="$(git rev-parse --show-toplevel)"
LAB_DIR="$REPO_ROOT/labs/$LAB_ID"
BRANCH="lab/$LAB_ID"

cd "$REPO_ROOT"

if ! git diff --quiet || ! git diff --cached --quiet; then
  echo "Des modifications non commitées sont en cours dans le repo : commit ou stash-les d'abord." >&2
  exit 1
fi

echo "Récupération de main..."
git fetch origin main

# Vérifié sur origin/main (pas sur le répertoire de travail courant : si on est
# déjà sur lab/<id-tp>, labs/<id-tp>/ y a été supprimé par une génération précédente).
if ! git cat-file -e "origin/main:labs/$LAB_ID/lab.md" 2>/dev/null; then
  echo "Pas de TP '$LAB_ID' : labs/$LAB_ID/lab.md est introuvable sur origin/main." >&2
  exit 1
fi

if git show-ref --verify --quiet "refs/heads/$BRANCH"; then
  echo "La branche '$BRANCH' existe déjà en local."
  if [ ! -t 0 ]; then
    echo "Entrée standard non interactive : impossible de demander quoi faire." >&2
    echo "Supprimez-la manuellement (git branch -D $BRANCH) ou relancez depuis un terminal." >&2
    exit 1
  fi
  ANSWER=""
  while [ "$ANSWER" != "b" ] && [ "$ANSWER" != "r" ]; do
    read -r -p "(b) basculer dessus telle quelle, sans rien changer / (r) la réinitialiser depuis origin/main [b/r] : " ANSWER
    ANSWER="$(printf '%s' "$ANSWER" | tr '[:upper:]' '[:lower:]')"
  done
  if [ "$ANSWER" = "b" ]; then
    git checkout "$BRANCH"
    echo
    echo "Branche '$BRANCH' basculée dans son état existant (aucun changement appliqué)."
    exit 0
  fi
fi

echo "Création de $BRANCH à partir de origin/main..."
git checkout -B "$BRANCH" origin/main

echo "Retrait des éléments à reconstruire pour le TP $LAB_ID..."
python3 "$REPO_ROOT/labs/scripts/strip_markers.py" "$LAB_ID" "$REPO_ROOT"

cp "$LAB_DIR/lab.md" "$REPO_ROOT/lab.md"
rm -rf "$LAB_DIR"

git add -A
git commit --quiet -m "Énoncé du TP $LAB_ID"

echo
echo "Branche '$BRANCH' prête, avec l'énoncé dans lab.md. Bon TP !"