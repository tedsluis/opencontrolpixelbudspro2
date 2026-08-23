#!/usr/bin/env python3
"""Grep-based documentation lint for this repo — no LLM involved.

Catches the class of regression a documentation audit found by hand on
2026-08-20 (see `CHANGELOG.md`'s matching entry): stale filename references,
ID reuse/typos, and the pre-rename project name creeping back in. Run it
locally or wire it into CI on any PR touching a `.md` file.

Checks:
  (a) every backtick-quoted filename referenced in a `.md` file resolves to a
      real path (relative to the repo root, or to the referencing file's own
      directory — most `CAP-NNN-FINDINGS.md` files reference their sibling
      log/video by bare filename).
  (b) every `CAP-NNN` / `ADR-NNN` / Test-ID token referenced anywhere resolves
      to a known entry in `id_registry.csv` (append-only source of truth —
      see that file's own header).
  (c) the pre-2026-08-14/15-rename project name ("Pixel Buds Pro 2 Control")
      does not reappear outside `CHANGELOG.md` and this audit report (both of
      which reference it deliberately, as history).

Exit code is non-zero if any check fails, so this is CI/pre-commit-friendly.
"""

from __future__ import annotations

import csv
import re
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent

# Known template/placeholder tokens that are intentionally generic, not real
# files or IDs — e.g. `CAP-NNN-FINDINGS.md` used as a doc-writing convention.
PLACEHOLDER_FILENAME_RE = re.compile(r"CAP-(NNN|nnn|00n|CAP-\d+)", re.IGNORECASE)

# Deliberate historical references this project keeps on purpose (retired
# docs, a corrected typo kept as a "sic"/before-value in a Corrections note,
# an illustrative example filename, an external repo's path quoted for
# protocol *knowledge* only per AGENTS.md §12) — not dead cross-references,
# don't flag them. Extend this set if a future deliberate historical mention
# starts tripping the check.
KNOWN_HISTORICAL_REFERENCES = {
    "PROTOCOL_NOTES.md",
    "PROTOCOL-NOTES.md",
    "TESTPLAN_EN.md",
    "EXPERIMENTS.md",
    "CAP-004-recording_3.mp4",
    "CAP-006-recoding.mp4",
    "CAP-005-recoding.mp4",  # renamed 2026-08-20; CHANGELOG.md documents the rename by name
    "docs/Notes.md",  # qzed/pbpctrl's own doc, referenced by name only
    "captures/2026-08-02_pixel7a_anc-toggle.log",  # illustrative example name
}

# Only lint cross-references to the project's own capture/doc artifacts —
# source-code-shaped filenames (.kt, .toml, .xml, .proto, .java) describe the
# future Android app, not yet part of this repo, and would false-positive.
FILENAME_RE = re.compile(
    r"`([A-Za-z0-9_.\-/]+\.(?:md|log|mp4|png|txt|csv|yaml|yml|json))`"
)

# Markdown image syntax, e.g. `![alt text](images/foo.png)` — used by
# SCREENSHOTS_PIXEL_BUDS_APP.md/SCREENSHOTS_PIXEL_BUDS_WEB_APP.md, which
# FILENAME_RE's backtick-only pattern above does not see at all
# (AUDIT_REPORT_2026-08-22.md finding: this was a real blind spot in the
# dead-filename check, even though no reference was actually broken at the
# time it was found).
IMAGE_RE = re.compile(r"!\[[^\]]*\]\(([^)\s]+)\)")

# Test-ID area prefixes, per TESTPLAN_BLUETOOTH_HCI_SNOOP.md §0.4 — update
# this list if that section's prefix table changes.
TEST_ID_PREFIXES = (
    "PAIR|ANC|CONV|MULTI|EQP|EQS|TOUCH|HEAD|HOLD|AUDIO|FW|FWUPD|INEAR|CASE|"
    "FIND|OBS|BATT|LOUD|ADAPT|GATT|GFPS|CALL|APP"
)
ID_RE = re.compile(
    rf"\b((?:CAP|ADR|{TEST_ID_PREFIXES})-\d{{3}})\b"
)

OLD_PROJECT_NAME = "Pixel Buds Pro 2 Control"
OLD_NAME_ALLOWED_IN = {"CHANGELOG.md"}  # audit reports are excluded by glob below


def load_registry() -> set[str]:
    registry_path = REPO_ROOT / "id_registry.csv"
    if not registry_path.exists():
        print(f"FATAL: {registry_path} not found", file=sys.stderr)
        sys.exit(2)
    with registry_path.open(newline="", encoding="utf-8") as f:
        return {row["id"] for row in csv.DictReader(f)}


def all_markdown_files() -> list[Path]:
    return [
        p
        for p in REPO_ROOT.rglob("*.md")
        if ".git" not in p.parts and "AUDIT_REPORT_" not in p.name
        # audit reports intentionally quote historical/mistaken references —
        # excluded from all three checks, not just the project-name one.
    ]


PLANNED_CAPTURE_PLACEHOLDER = "yyyy-MM-dd_HH-mm-ss_HH-mm-ss"


def resolve_filename(name: str, referencing_file: Path) -> bool:
    """True if `name` resolves to a real file somewhere sensible in the repo."""
    if name.startswith("/") or name.startswith("FS/"):
        # Device-side or bugreport-archive path (e.g. from CAPTURE_BLUETOOTH_HCI_SNOOP.md's
        # extraction instructions) — not a path in this repo at all.
        return True
    if (REPO_ROOT / name).exists():
        return True
    if (referencing_file.parent / name).exists():
        return True
    if PLANNED_CAPTURE_PLACEHOLDER in str(referencing_file.parent):
        # A not-yet-captured session's EVENT-NOTES.md skeleton — its own
        # sibling log/video/FINDINGS.md are expected not to exist yet.
        cap_id = referencing_file.parent.name.split("-", 2)[0:2]
        if len(cap_id) == 2 and name.startswith("-".join(cap_id) + "-"):
            return True
    # A bare `CAP-NNN-*` filename referenced from a *different* doc (or from
    # a different capture's own folder) still resolves if that capture's own
    # folder has it — captures/CAP-NNN-<date>-Group_X/<name>.
    cap_match = re.match(r"CAP-(\d{3})-", name)
    if cap_match:
        pattern = f"captures/CAP-{cap_match.group(1)}-*/{name}"
        if list(REPO_ROOT.glob(pattern)):
            return True
    return False


def check_filenames(files: list[Path]) -> list[str]:
    errors = []
    for path in files:
        text = path.read_text(encoding="utf-8", errors="replace")
        for match in FILENAME_RE.finditer(text):
            name = match.group(1)
            if PLACEHOLDER_FILENAME_RE.search(name) or name in KNOWN_HISTORICAL_REFERENCES:
                continue
            if not resolve_filename(name, path):
                errors.append(f"{path.relative_to(REPO_ROOT)}: dead filename reference `{name}`")
        for match in IMAGE_RE.finditer(text):
            name = match.group(1)
            if name.startswith(("http://", "https://")):
                continue  # remote image, not a repo-relative path
            if PLACEHOLDER_FILENAME_RE.search(name) or name in KNOWN_HISTORICAL_REFERENCES:
                continue
            if not resolve_filename(name, path):
                errors.append(f"{path.relative_to(REPO_ROOT)}: dead image reference `{name}`")
    return errors


def check_ids(files: list[Path], known_ids: set[str]) -> list[str]:
    warnings = []
    for path in files:
        text = path.read_text(encoding="utf-8", errors="replace")
        for match in ID_RE.finditer(text):
            token = match.group(1)
            if token not in known_ids:
                warnings.append(
                    f"{path.relative_to(REPO_ROOT)}: `{token}` not in id_registry.csv "
                    f"(new ID not yet registered, or a typo/reused number)"
                )
    return warnings


def check_old_project_name(files: list[Path]) -> list[str]:
    errors = []
    for path in files:
        if path.name in OLD_NAME_ALLOWED_IN:
            continue
        text = path.read_text(encoding="utf-8", errors="replace")
        if OLD_PROJECT_NAME in text:
            errors.append(
                f"{path.relative_to(REPO_ROOT)}: pre-rename project name "
                f'"{OLD_PROJECT_NAME}" found — should read "OpenControl for '
                f'Pixel Buds Pro 2"'
            )
    return errors


def main() -> int:
    known_ids = load_registry()
    files = all_markdown_files()

    filename_errors = check_filenames(files)
    id_warnings = check_ids(files, known_ids)
    name_errors = check_old_project_name(files)

    if filename_errors:
        print("=== Dead filename references ===")
        print("\n".join(sorted(filename_errors)))
    if id_warnings:
        print("\n=== Unregistered ID references (register in id_registry.csv, or fix the typo) ===")
        print("\n".join(sorted(set(id_warnings))))
    if name_errors:
        print("\n=== Pre-rename project name regressions ===")
        print("\n".join(sorted(name_errors)))

    total_errors = len(filename_errors) + len(name_errors)
    if not (filename_errors or id_warnings or name_errors):
        print("lint_docs: clean — no dead filenames, no unregistered IDs, no stale project name.")

    # ID warnings are informational (a brand-new, not-yet-registered ID is
    # normal mid-session) — only dead filenames and the project-name check
    # fail the build.
    return 1 if total_errors else 0


if __name__ == "__main__":
    raise SystemExit(main())
