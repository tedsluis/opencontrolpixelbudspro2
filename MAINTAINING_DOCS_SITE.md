# Keeping the docs site up to date

This site (Docsify, `index.html` + `_sidebar.md`) has no build step — Docsify fetches raw
markdown from GitHub Pages live, on every page load. What you need to do after an edit depends on
*what kind* of edit it was.

## You edited the content of an existing file

Nothing extra to do. Commit and push to `main` as normal — the live site reflects it within
seconds (no rebuild, no regeneration).

## You added a new capture, or renamed a capture folder (e.g. the `yyyy-MM-dd` placeholder
## became a real date after processing)

```
./generate_sidebar.sh
git add _sidebar.md
git commit -m "docs: regenerate sidebar after CAP-NNN update"
git push
```
(Skip this if you set up the GitHub Action below — it does this step for you.)

## You added a brand-new root-level `.md` file (not a capture)

Add one line to `generate_sidebar.sh`'s heredoc, in whichever section it belongs
(Project / Process & rules / Capture procedure / Reference screenshots), then:
```
./generate_sidebar.sh
git add generate_sidebar.sh _sidebar.md
git commit -m "docs: add <FILE>.md to sidebar"
git push
```

## Fully automatic (optional)

`.github/workflows/update-docs-sidebar.yml` (added alongside this file) re-runs
`generate_sidebar.sh` and commits the result automatically on every push to `main` that touches
`captures/**` or any `*.md` file — so the capture-rename/add case above needs no manual step
either. New root-level docs still need the one-line addition to the script by hand, since the
script can't guess which sidebar section a new doc belongs in.

## Every page needs the standard footer

Every doc page (except `_sidebar.md` and `CLAUDE.md`) ends with a footer linking to its own GitHub
blob view and its own Docsify site view — see the bottom of this file for an example. It's added
and kept correct mechanically, not by hand:

- `scripts/ensure_footers.py` adds/repairs the footer on every tracked `.md` file (safe to
  re-run — it's a no-op once footers are already correct, and self-heals after a file/folder
  rename since the URLs are derived from the current path each time).
- `scripts/lint_docs.py` (wired into `.github/workflows/lint-docs.yml`, runs on every PR/push
  touching a `.md` file) fails CI if a page's footer is missing or stale — this is what keeps the
  footer in place even if a future edit (human or AI) accidentally strips it.

If you add a brand-new page, run `./scripts/ensure_footers.py` once (same as the sidebar-generation
step above) rather than typing the footer by hand.

## Checking it worked

`https://tedsluis.github.io/opencontrolpixelbudspro2/` — GitHub Pages builds typically go live
within a minute of the push.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/MAINTAINING_DOCS_SITE.md - https://tedsluis.github.io/opencontrolpixelbudspro2/MAINTAINING_DOCS_SITE
