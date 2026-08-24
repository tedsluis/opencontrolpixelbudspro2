#!/usr/bin/env python3
"""Adds or repairs the standard cross-link footer at the bottom of every doc page.

Every tracked `.md` file (except `_sidebar.md` and `CLAUDE.md` — not content
pages, see `lint_docs.FOOTER_EXCLUDED_FILES`) should end with:

    ---
    https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/<path> - https://tedsluis.github.io/opencontrolpixelbudspro2/#/<path-without-.md>

so a reader can always jump between the GitHub blob view and the Docsify site
view of the same page. `lint_docs.py`'s footer check (wired into
`.github/workflows/lint-docs.yml`) fails CI if a page's footer is missing or
stale — this script is how you fix that, and how you get the footer on a
brand-new page in the first place. The footer format itself lives in
`lint_docs.py` (`expected_footer()`), imported here, so the two can't drift
apart.

Idempotent: re-running with nothing changed is a no-op; re-running after a
file/folder rename replaces the stale footer with the correct one (recognized
by the trailing `---` + URL-pair line, not by exact prior content).

Usage: ./scripts/ensure_footers.py
"""

from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from lint_docs import REPO_ROOT, expected_footer, footer_files  # noqa: E402


def ensure_footer(path: Path) -> bool:
    """Append/repair `path`'s footer. Returns True if the file was changed."""
    rel = path.relative_to(REPO_ROOT).as_posix()
    text = path.read_text(encoding="utf-8", errors="replace")
    expected = expected_footer(rel).rstrip("\n")

    body = text.rstrip("\n")
    if body.endswith(expected):
        return False

    lines = body.split("\n")
    if len(lines) >= 2 and lines[-2] == "---" and lines[-1].startswith("http") and " - http" in lines[-1]:
        # Strip a stale footer (e.g. after a rename changed the expected URLs)
        # so re-running doesn't pile up duplicate footer blocks.
        body = "\n".join(lines[:-2]).rstrip("\n")

    path.write_text(body + "\n\n" + expected + "\n", encoding="utf-8")
    return True


def main() -> int:
    changed = sorted(
        path.relative_to(REPO_ROOT).as_posix()
        for path in footer_files()
        if ensure_footer(path)
    )
    if changed:
        print(f"Updated {len(changed)} file(s):")
        print("\n".join(changed))
    else:
        print("ensure_footers: all footers already up to date.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
