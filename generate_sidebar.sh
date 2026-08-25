#!/usr/bin/env bash
# Regenerates _sidebar.md for the Docsify site.
# Run from the repo root: ./generate_sidebar.sh
# Re-run any time captures/ changes (new capture, folder renamed after processing, etc.)
#
# Every generated link is absolute, with the site's own GitHub Pages subdirectory prefix
# baked in (e.g. /opencontrolpixelbudspro2/PROJECT.md), not just a leading `/`. Verified
# live: under routerMode: 'history' (index.html), Docsify's router uses a leading-`/`-only
# href as absolute-from-*domain*-root — https://tedsluis.github.io/PROJECT.md — dropping the
# /opencontrolpixelbudspro2/ subdirectory entirely and 404ing. Regular page content doesn't
# have this problem (it resolves via window.location.pathname, which already contains the
# subdirectory), but Docsify's sidebar-link handling apparently doesn't go through that same
# resolution — so the prefix has to be hardcoded into every link here instead. A bare relative
# link (no leading `/`) isn't an option either: that resolves against the *current* page's own
# directory (via relativePath: true, needed for CAP-NNN-FINDINGS.md's own `./sibling.md`
# links), breaking navigation from any page inside captures/CAP-NNN-.../ back to a root-level
# doc — the original bug this whole leading-`/` convention was added to fix, back in hash mode.
set -euo pipefail

BASE_PATH="/opencontrolpixelbudspro2"
OUT="_sidebar.md"

cat > "$OUT" <<EOF
- [Home](${BASE_PATH}/README.md)

- **Project**
  - [PROJECT.md](${BASE_PATH}/PROJECT.md)
  - [ARCHITECTURE.md](${BASE_PATH}/ARCHITECTURE.md)
  - [PROTOCOL.md](${BASE_PATH}/PROTOCOL.md)
  - [DECISIONS.md](${BASE_PATH}/DECISIONS.md)
  - [REVERSE_ENGINEERING.md](${BASE_PATH}/REVERSE_ENGINEERING.md)
  - [DESKRESEARCH_FINDINGS.md](${BASE_PATH}/DESKRESEARCH_FINDINGS.md)

- **Process & rules**
  - [AGENTS.md](${BASE_PATH}/AGENTS.md)
  - [PROJECT_RULES.md](${BASE_PATH}/PROJECT_RULES.md)
  - [TODO.md](${BASE_PATH}/TODO.md)
  - [CHANGELOG.md](${BASE_PATH}/CHANGELOG.md)
  - [SECURITY.md](${BASE_PATH}/SECURITY.md)
  - [CONTRIBUTING.md](${BASE_PATH}/CONTRIBUTING.md)
  - [MAINTAINING_DOCS_SITE.md](${BASE_PATH}/MAINTAINING_DOCS_SITE.md)

- **Capture procedure**
  - [CAPTURE_BLUETOOTH_HCI_SNOOP.md](${BASE_PATH}/CAPTURE_BLUETOOTH_HCI_SNOOP.md)
  - [TESTPLAN_BLUETOOTH_HCI_SNOOP.md](${BASE_PATH}/TESTPLAN_BLUETOOTH_HCI_SNOOP.md)
  - [WORKSTATION_PREPARATIONS.md](${BASE_PATH}/WORKSTATION_PREPARATIONS.md)

- **Reference screenshots**
  - [SCREENSHOTS_PIXEL_BUDS_APP.md](${BASE_PATH}/SCREENSHOTS_PIXEL_BUDS_APP.md)
  - [SCREENSHOTS_PIXEL_BUDS_WEB_APP.md](${BASE_PATH}/SCREENSHOTS_PIXEL_BUDS_WEB_APP.md)

- **Captures**
EOF

if [ -d captures ]; then
  find captures -maxdepth 1 -mindepth 1 -type d -name 'CAP-*' \
    | sort -t- -k2 -V \
    | while read -r dir; do
        folder="$(basename "$dir")"
        cap_id="$(echo "$folder" | grep -oE '^CAP-[0-9]+')"
        group="$(echo "$folder" | grep -oE 'Group_[A-Za-z]+$' | sed 's/Group_/Group /')"
        planned=""
        [[ "$folder" == *"yyyy-MM-dd"* ]] && planned=" _(planned)_"

        echo "  - **${cap_id} (${group})**${planned}" >> "$OUT"

        for f in "$dir/${cap_id}-EVENT-NOTES.md" "$dir/${cap_id}-FINDINGS.md"; do
          if [ -f "$f" ]; then
            label="$(basename "$f")"
            echo "    - [${label}](${BASE_PATH}/${f})" >> "$OUT"
          fi
        done
      done
fi

echo "Wrote $OUT"
