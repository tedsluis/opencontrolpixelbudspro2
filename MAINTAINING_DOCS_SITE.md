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

## Checking it worked

`https://tedsluis.github.io/opencontrolpixelbudspro2/` — GitHub Pages builds typically go live
within a minute of the push.
