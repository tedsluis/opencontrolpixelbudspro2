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
      log/video by bare filename), OR is a bare filename listed literally in
      `.gitignore` (e.g. a too-large-for-git/LFS recording the maintainer
      keeps locally but never commits — see `load_gitignored_filenames()`).
  (b) every `CAP-NNN` / `ADR-NNN` / Test-ID token referenced anywhere resolves
      to a known entry in `id_registry.csv` (append-only source of truth —
      see that file's own header).
  (c) the pre-2026-08-14/15-rename project name ("Pixel Buds Pro 2 Control")
      does not reappear outside `CHANGELOG.md` and this audit report (both of
      which reference it deliberately, as history).
  (d) every doc page ends with the standard cross-link footer (GitHub blob URL
      + Docsify site URL, see `expected_footer()` below) — run
      `scripts/ensure_footers.py` to add/repair it rather than hand-editing.

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

# Generic, un-prefixed mentions of the two btsnoop-log naming conventions
# themselves (raw `btsnoop_hci.log` vs. the `btsnooz.py`-fallback
# `btsnooz_hci.log`, each with or without a leading dash for the
# `CAP-NNN-` prefix they'd normally carry) — prose *about* the naming
# pattern (e.g. "extracted via the raw `btsnoop_hci.log` path"), not a
# reference to any specific file. A real per-capture reference always
# carries its own `CAP-NNN-` prefix and is checked normally; this only
# exempts the bare/dash-only generic form. Found repeatedly false-flagging
# across `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `DESKRESEARCH_FINDINGS.md`,
# `TODO.md`, and several `CAP-NNN-FINDINGS.md`/`EVENT-NOTES.md` files
# discussing the `CAP-012`/`013`/`017`/`031`/`032` extraction-path finding.
GENERIC_LOG_FILENAME_RE = re.compile(r"^-?btsnoo[pz]_hci\.log$")

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
    "AUDIT_REPORT_2026-08-22.md",  # transient audit-report artifact, never committed (2026-08-23);
                                    # its findings live on in the docs/CHANGELOG.md entries that
                                    # cite it by name, same lifecycle as AUDIT_REPORT_2026-08-20.md
    "CAP-032-btsnooz_hci.log",  # CAP-032-EVENT-NOTES.md's own "Corrections" section quotes this as
                                # the pre-fix (wrong) value of its Log Metadata table's Log file
                                # field, kept as a "sic"/before-value — same pattern as the
                                # CAP-005-recoding.mp4 entry above, not a live reference.
    "REVIEW_REPORT.md",  # a deleted, unofficial third-party report REVERSE_ENGINEERING.md's
                          # fua/gax/gbo/gba/hjy entry references by name to explain why its
                          # "channel conflation" claim is not repeated there — same
                          # deliberate-historical-reference pattern as the entries above.
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

# Standard per-page footer: a GitHub blob link (so the source is reachable from
# whatever renders the raw file, e.g. GitHub's own viewer) plus the matching
# Docsify site link (`index.html` uses routerMode: 'history', so this is a
# real path-based URL, no `#`). Kept here as the single source of truth;
# `ensure_footers.py` and `generate_sitemap.py` both import from this module
# so the lint check, the footer repair tool, and the sitemap can never drift
# apart.
GITHUB_BLOB_BASE = "https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main"
DOCSIFY_BASE = "https://tedsluis.github.io/opencontrolpixelbudspro2"

# Not real content pages — _sidebar.md is Docsify's own nav config (not linked
# from itself) and CLAUDE.md is a one-line `@AGENTS.md` include for Claude
# Code, not a site page.
FOOTER_EXCLUDED_FILES = {"_sidebar.md", "CLAUDE.md"}


def docsify_route(rel_path: str) -> str:
    """Docsify hash-route for a repo-relative `.md` path (extension stripped)."""
    return rel_path[:-3] if rel_path.endswith(".md") else rel_path


def expected_footer(rel_path: str) -> str:
    """The exact footer block (including leading '---' rule) for `rel_path`."""
    github_url = f"{GITHUB_BLOB_BASE}/{rel_path}"
    docsify_url = f"{DOCSIFY_BASE}/{docsify_route(rel_path)}"
    return f"---\n{github_url} - {docsify_url}\n"


def footer_files() -> list[Path]:
    return [p for p in all_markdown_files() if p.name not in FOOTER_EXCLUDED_FILES]


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


def load_gitignored_filenames() -> set[str]:
    """Bare filenames listed literally in .gitignore (not glob/directory
    patterns) — e.g. CAP-009-recording1.mp4, excluded as "# to big" for git/LFS.
    A doc reference to one of these isn't a dead link: the file genuinely
    exists on the maintainer's machine and is intentionally never committed,
    the same tradeoff .gitignore itself already documents. A fresh clone
    won't have it either, by design — that's accepted, not a lint failure.
    """
    gitignore_path = REPO_ROOT / ".gitignore"
    if not gitignore_path.exists():
        return set()
    names = set()
    for line in gitignore_path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        if any(ch in line for ch in "*?[]!/"):
            continue  # a glob pattern or directory rule, not a bare filename
        names.add(line)
    return names


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
    gitignored = load_gitignored_filenames()
    for path in files:
        text = path.read_text(encoding="utf-8", errors="replace")
        for match in FILENAME_RE.finditer(text):
            name = match.group(1)
            if (
                PLACEHOLDER_FILENAME_RE.search(name)
                or GENERIC_LOG_FILENAME_RE.match(name)
                or name in KNOWN_HISTORICAL_REFERENCES
                or Path(name).name in gitignored
            ):
                continue
            if not resolve_filename(name, path):
                errors.append(f"{path.relative_to(REPO_ROOT)}: dead filename reference `{name}`")
        for match in IMAGE_RE.finditer(text):
            name = match.group(1)
            if name.startswith(("http://", "https://")):
                continue  # remote image, not a repo-relative path
            if (
                PLACEHOLDER_FILENAME_RE.search(name)
                or GENERIC_LOG_FILENAME_RE.match(name)
                or name in KNOWN_HISTORICAL_REFERENCES
                or Path(name).name in gitignored
            ):
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


def check_footers(files: list[Path]) -> list[str]:
    errors = []
    for path in files:
        rel = path.relative_to(REPO_ROOT).as_posix()
        text = path.read_text(encoding="utf-8", errors="replace")
        expected = expected_footer(rel).rstrip("\n")
        if not text.rstrip("\n").endswith(expected):
            errors.append(
                f"{rel}: missing or incorrect footer (run scripts/ensure_footers.py)"
            )
    return errors


def main() -> int:
    known_ids = load_registry()
    files = all_markdown_files()

    filename_errors = check_filenames(files)
    id_warnings = check_ids(files, known_ids)
    name_errors = check_old_project_name(files)
    footer_errors = check_footers(footer_files())

    if filename_errors:
        print("=== Dead filename references ===")
        print("\n".join(sorted(filename_errors)))
    if id_warnings:
        print("\n=== Unregistered ID references (register in id_registry.csv, or fix the typo) ===")
        print("\n".join(sorted(set(id_warnings))))
    if name_errors:
        print("\n=== Pre-rename project name regressions ===")
        print("\n".join(sorted(name_errors)))
    if footer_errors:
        print("\n=== Missing/incorrect page footers ===")
        print("\n".join(sorted(footer_errors)))

    total_errors = len(filename_errors) + len(name_errors) + len(footer_errors)
    if not (filename_errors or id_warnings or name_errors or footer_errors):
        print("lint_docs: clean — no dead filenames, no unregistered IDs, no stale project name, "
              "no missing footers.")

    # ID warnings are informational (a brand-new, not-yet-registered ID is
    # normal mid-session) — only dead filenames, the project-name check, and
    # the footer check fail the build.
    return 1 if total_errors else 0


if __name__ == "__main__":
    raise SystemExit(main())
