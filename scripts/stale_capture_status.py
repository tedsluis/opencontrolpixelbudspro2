#!/usr/bin/env python3
"""List 🟢/🟡/🔴/"proposal" lines in captures/*/CAP-*.md whose subject is now settled (🟢) in PROTOCOL.md / DECISIONS.md.

ai-sessions/0058 A58-CAP-04 / ai-sessions/0059. Each SUBJECT maps a regex (matched case-insensitively on one line) to the
superseding source. The output is a review list, not an edit list: a hit can be a historical sentence that is already fine.
Usage: python3 scripts/stale_capture_status.py [--counts]
"""
import glob, re, sys

SUBJECTS = [
    (r"settable.*(dock|docked)|dock.state indicator|both docked", "ADR-049 (Settable: 0x00 ⇒ NAK; 🟡 0x00 ⇔ no bud worn)"),
    (r"03 03 00 03|group 0x03 code 0x03|option b candidate", "ADR-031/033 (Battery updated 🟢, charging flag 🟢)"),
    (r"0x0f32|0x0f28|0x0c0x.*(open|unresolved)|handle.?.uuid", "PROTOCOL.md §6 (CAP-034 GATT map 🟢)"),
    (r"leb128", "PROTOCOL.md §2.2a (one-terminated varint, 2026-09-30)"),
    (r"0xd180|address 0x0000|control 0x3b", "ADR-034 (pw_hdlc address/control, 🟢)"),
    (r"opaque.*sent|not (yet )?decoded.*dlci 0x02|dlci 0x02.*not (yet )?decoded|connect.time burst.*(undecoded|not decoded)", "ADR-034, PROTOCOL.md §6 2026-09-24 burst item (🟢)"),
    (r"2-field sub-message|two-field", "PROTOCOL.md §4.3 Option F (entry 6.1 = Case, ADR-043 🟢)"),
    (r"0e 04.*unattributed|unattributed.*0e 04", "PROTOCOL.md §4.3 Option E 2026-09-24 correction (🟢)"),
    (r"bit order.*🔴|on-screen order", "ADR-046 / PROTOCOL.md §4.5.3 2026-09-28 (🟢)"),
    (r"field ?(2|15|22|28)\b.*🟡|🟡.*field ?(2|15|22|28)\b", "PROTOCOL.md §4.5 / ADR-019 Updates (🟢)"),
    (r"one-time (capability|handshake)|capability blob", "ADR-014 / Option E (0e 01 recurs, 🟢)"),
    (r"nothing promoted|awaiting (maintainer )?sign-off|pending maintainer", "see the file's own proposal-status line / the ADR named there"),
    (r"does not re-?open|no automatic (re-?open|session)", "ADR-044 (built ai-sessions/0048)"),
    (r"serial.*1779298694|1779298694.*serial", "PROTOCOL.md §2.2a 2026-09-30 (a version number, 🟢)"),
]
LABEL = re.compile(r"🟢|FACT|🟡|🔴|HYPOTHESIS|OPEN QUESTION|proposal|awaiting|pending", re.I)

def main():
    counts = "--counts" in sys.argv
    total = 0
    per = {}
    for path in sorted(glob.glob("captures/*/CAP-*.md")):
        for n, line in enumerate(open(path, encoding="utf-8"), 1):
            if not LABEL.search(line):
                continue
            for rx, src in SUBJECTS:
                if re.search(rx, line, re.I):
                    total += 1
                    per[path] = per.get(path, 0) + 1
                    if not counts:
                        print(f"{path}:{n}: [{src}] {line.strip()[:160]}")
                    break
    if counts:
        for p, c in sorted(per.items()):
            print(c, p)
    print(f"{total} lines", file=sys.stderr)

if __name__ == "__main__":
    main()
