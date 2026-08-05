#!/usr/bin/env python3
"""Retire les blocs marqués `LAB:<id>:...` du code source pour un TP donné.

Voir labs/README.md pour la syntaxe des marqueurs.
"""

import re
import sys
from pathlib import Path

EXCLUDED_DIR_NAMES = {".git", "labs", "node_modules", "target", "dist", "build", ".idea"}

LINE_COMMENT_EXTENSIONS = {
    ".java": "//",
    ".yml": "#",
    ".yaml": "#",
    ".properties": "#",
}
BLOCK_COMMENT_EXTENSIONS = {
    ".xml": ("<!--", "-->"),
}


def build_line_pattern(lab_id, prefix):
    p = re.escape(prefix)
    lid = re.escape(lab_id)
    return re.compile(
        rf"^(?P<indent>[ \t]*){p}[ \t]*LAB:{lid}:(?P<verb>REMOVE|TODO):START"
        rf"(?:[ \t]+(?P<hint>.*?))?[ \t]*\r?\n"
        rf"(?P<body>.*?)"
        rf"^[ \t]*{p}[ \t]*LAB:{lid}:(?P=verb):END[ \t]*\r?\n?",
        re.DOTALL | re.MULTILINE,
    )


def build_block_pattern(lab_id, open_tok, close_tok):
    o, c = re.escape(open_tok), re.escape(close_tok)
    lid = re.escape(lab_id)
    return re.compile(
        rf"^(?P<indent>[ \t]*){o}[ \t]*LAB:{lid}:(?P<verb>REMOVE|TODO):START"
        rf"(?:[ \t]+(?P<hint>.*?))?[ \t]*{c}[ \t]*\r?\n"
        rf"(?P<body>.*?)"
        rf"^[ \t]*{o}[ \t]*LAB:{lid}:(?P=verb):END[ \t]*{c}[ \t]*\r?\n?",
        re.DOTALL | re.MULTILINE,
    )


def replacement_for(match, wrap):
    if match.group("verb") == "REMOVE":
        return ""
    indent = match.group("indent")
    hint = (match.group("hint") or "").strip()
    note = f"TODO: {hint}" if hint else "TODO"
    return f"{indent}{wrap(note)}\n"


def process_file(path, lab_id):
    suffix = path.suffix.lower()
    if suffix in LINE_COMMENT_EXTENSIONS:
        prefix = LINE_COMMENT_EXTENSIONS[suffix]
        pattern = build_line_pattern(lab_id, prefix)
        wrap = lambda text, p=prefix: f"{p} {text}"
    elif suffix in BLOCK_COMMENT_EXTENSIONS:
        open_tok, close_tok = BLOCK_COMMENT_EXTENSIONS[suffix]
        pattern = build_block_pattern(lab_id, open_tok, close_tok)
        wrap = lambda text, o=open_tok, c=close_tok: f"{o} {text} {c}"
    else:
        return False

    text = path.read_text(encoding="utf-8")
    if f"LAB:{lab_id}:" not in text:
        return False

    new_text, count = pattern.subn(lambda m: replacement_for(m, wrap), text)
    if count == 0:
        return False

    path.write_text(new_text, encoding="utf-8")
    return True


def iter_candidate_files(root):
    all_exts = set(LINE_COMMENT_EXTENSIONS) | set(BLOCK_COMMENT_EXTENSIONS)
    for path in root.rglob("*"):
        if path.is_dir():
            continue
        if any(part in EXCLUDED_DIR_NAMES for part in path.relative_to(root).parts):
            continue
        if path.suffix.lower() in all_exts:
            yield path


def main():
    if len(sys.argv) != 3:
        print(f"Usage: {sys.argv[0]} <id-tp> <racine-du-repo>", file=sys.stderr)
        sys.exit(1)

    lab_id, repo_root = sys.argv[1], Path(sys.argv[2]).resolve()

    modified = []
    for path in iter_candidate_files(repo_root):
        if process_file(path, lab_id):
            modified.append(path.relative_to(repo_root))

    if modified:
        print(f"{len(modified)} fichier(s) modifié(s) pour le TP {lab_id} :")
        for p in sorted(modified):
            print(f"  - {p}")
    else:
        print(
            f"Aucun marqueur LAB:{lab_id}:... trouvé "
            "(TP purement théorique, ou pas encore équipé de marqueurs)."
        )


if __name__ == "__main__":
    main()