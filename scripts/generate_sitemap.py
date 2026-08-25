#!/usr/bin/env python3
"""Generates sitemap.xml for the Docsify site — the site root only.

Root-only by deliberate choice (2026-08-25), not an oversight: index.html
serves routerMode: 'history' via GitHub Pages' 404.html fallback trick (no
server-side rewrite support there), which means every URL except the bare
site root returns a genuine HTTP 404 status — confirmed live by curl against
all 69 pages. Per Google's own documentation, a 4xx status is a hard stop:
"any content Google receives from URLs that return a 4xx status code is
ignored" (developers.google.com/search/docs/crawling-indexing/http-network-errors).
A sitemap listing all 69 pages would just be 68 confirmed dead entries in
Search Console's coverage report — this lists only the one URL that's
actually indexable as things stand.

If a future change makes individual pages return real 200s (e.g. build-time
pre-rendering to static files per route), this script's scope should expand
back to all pages — see MAINTAINING_DOCS_SITE.md and git history around
2026-08-25 for that discussion and the options considered.

`<lastmod>` is README.md's own last commit date (`git log -1 --format=%cI`) —
a full, unshallowed checkout is required for this to be accurate (see
`.github/workflows/update-sitemap.yml`'s `fetch-depth: 0`).

Usage: ./scripts/generate_sitemap.py
"""

from __future__ import annotations

import subprocess
import sys
from pathlib import Path
from xml.sax.saxutils import escape

sys.path.insert(0, str(Path(__file__).resolve().parent))
from lint_docs import REPO_ROOT  # noqa: E402

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


def main() -> int:
    readme = REPO_ROOT / "README.md"
    lastmod = last_commit_date(readme)

    lines = [
        '<?xml version="1.0" encoding="UTF-8"?>',
        '<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">',
        "  <url>",
        f"    <loc>{escape(BASE_URL)}/</loc>",
    ]
    if lastmod:
        lines.append(f"    <lastmod>{lastmod}</lastmod>")
    lines.append("  </url>")
    lines.append("</urlset>")
    lines.append("")

    out_path = REPO_ROOT / "sitemap.xml"
    out_path.write_text("\n".join(lines), encoding="utf-8")
    print(f"Wrote {out_path.relative_to(REPO_ROOT)} with 1 URL (root only — see this script's docstring)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
