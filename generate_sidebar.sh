#!/usr/bin/env bash
# Regenerates _sidebar.md for the Docsify site.
# Run from the repo root: ./generate_sidebar.sh
# Re-run any time captures/ changes (new capture, folder renamed after processing, etc.)
#
# All generated links start with a leading `/`. index.html sets relativePath: true
# (required so same-folder links inside CAP-NNN-FINDINGS.md, e.g. `./CAP-006-EVENT-NOTES.md`,
# resolve against that file's own directory) — but Docsify applies that same relative
# resolution to sidebar links too. Without the leading `/`, once the user is on a page inside
# captures/CAP-NNN-.../, every other sidebar link resolves against that folder instead of the
# site root, breaking navigation. A leading `/` forces root-relative resolution regardless of
# the current page.
set -euo pipefail

OUT="_sidebar.md"

cat > "$OUT" <<'EOF'
- [Home](/README.md)

- **Project**
  - [PROJECT.md](/PROJECT.md)
  - [ARCHITECTURE.md](/ARCHITECTURE.md)
  - [PROTOCOL.md](/PROTOCOL.md)
  - [DECISIONS.md](/DECISIONS.md)
  - [REVERSE_ENGINEERING.md](/REVERSE_ENGINEERING.md)
  - [DESKRESEARCH_FINDINGS.md](/DESKRESEARCH_FINDINGS.md)

- **Process & rules**
  - [AGENTS.md](/AGENTS.md)
  - [PROJECT_RULES.md](/PROJECT_RULES.md)
  - [TODO.md](/TODO.md)
  - [CHANGELOG.md](/CHANGELOG.md)
  - [SECURITY.md](/SECURITY.md)
  - [CONTRIBUTING.md](/CONTRIBUTING.md)
  - [MAINTAINING_DOCS_SITE.md](/MAINTAINING_DOCS_SITE.md)

- **Capture procedure**
  - [CAPTURE_BLUETOOTH_HCI_SNOOP.md](/CAPTURE_BLUETOOTH_HCI_SNOOP.md)
  - [TESTPLAN_BLUETOOTH_HCI_SNOOP.md](/TESTPLAN_BLUETOOTH_HCI_SNOOP.md)
  - [WORKSTATION_PREPARATIONS.md](/WORKSTATION_PREPARATIONS.md)

- **Reference screenshots**
  - [SCREENSHOTS_PIXEL_BUDS_APP.md](/SCREENSHOTS_PIXEL_BUDS_APP.md)
  - [SCREENSHOTS_PIXEL_BUDS_WEB_APP.md](/SCREENSHOTS_PIXEL_BUDS_WEB_APP.md)

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
            echo "    - [${label}](/${f})" >> "$OUT"
          fi
        done
      done
fi

echo "Wrote $OUT"
