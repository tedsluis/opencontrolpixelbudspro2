#!/usr/bin/env python3
"""Generates sitemap.xml for the Docsify site (history-mode, real path-based URLs).

Reuses the exact file set `ensure_footers.py`/`lint_docs.py` already treat as
"doc pages" (`footer_files()` — every tracked `.md` file except `_sidebar.md`,
`CLAUDE.md`, and `AUDIT_REPORT_*.md`, the last excluded by
`all_markdown_files()`) so this can't silently drift from what the footer
system considers a real page.

Each URL uses `docsify_route()` (same helper the footer URLs use) so the
sitemap always matches whatever path scheme `index.html`'s routerMode
actually serves — history mode, no `#`. `README.md` maps to the site root
(`BASE_URL/`), matching `index.html`'s `homepage: 'README.md'`.

`<lastmod>` is each file's last commit date (`git log -1 --format=%cI`) — a
full, unshallowed checkout is required for this to be accurate (see
`.github/workflows/update-sitemap.yml`'s `fetch-depth: 0`); a file with no
resolvable git history (e.g. never committed yet) is emitted without one
rather than guessing.

Usage: ./scripts/generate_sitemap.py
"""

from __future__ import annotations

import subprocess
import sys
from pathlib import Path
from xml.sax.saxutils import escape

sys.path.insert(0, str(Path(__file__).resolve().parent))
from lint_docs import REPO_ROOT, docsify_route, footer_files  # noqa: E402

BASE_URL = "https://tedsluis.github.io/opencontrolpixelbudspro2"


def last_commit_date(path: Path) -> str | None:
    result = subprocess.run(
        ["git", "log", "-1", "--format=%cI", "--", str(path.relative_to(REPO_ROOT))],
        cwd=REPO_ROOT,
        capture_output=True,
        text=True,
        check=False,
    )
    date = result.stdout.strip()
    return date or None


def page_url(path: Path) -> str:
    rel = path.relative_to(REPO_ROOT).as_posix()
    if rel == "README.md":
        return f"{BASE_URL}/"
    return f"{BASE_URL}/{docsify_route(rel)}"


def main() -> int:
    files = sorted(footer_files(), key=lambda p: p.relative_to(REPO_ROOT).as_posix())

    lines = [
        '<?xml version="1.0" encoding="UTF-8"?>',
        '<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">',
    ]
    for path in files:
        url = page_url(path)
        lastmod = last_commit_date(path)
        lines.append("  <url>")
        lines.append(f"    <loc>{escape(url)}</loc>")
        if lastmod:
            lines.append(f"    <lastmod>{lastmod}</lastmod>")
        lines.append("  </url>")
    lines.append("</urlset>")
    lines.append("")

    out_path = REPO_ROOT / "sitemap.xml"
    out_path.write_text("\n".join(lines), encoding="utf-8")
    print(f"Wrote {out_path.relative_to(REPO_ROOT)} with {len(files)} URLs")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
