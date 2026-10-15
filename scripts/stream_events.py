#!/usr/bin/env python3
"""The settings stream's noise-control pushes (Maestro setting 13) as change events: the pair of packets of one change merged into one event.

PROJECT_RULES.md rule 4a: the tool behind a count is committed. Written in ai-sessions/0092 (CAP-076-FINDINGS.md §4.3): with both buds out of the
case the Buds push every change of setting 13 twice, once with channel_id 21 and once with 19, 1–200 ms apart (🟡 one per bud core).

Input: one or more tables of scripts/pwhdlc_crc_table.py. Every SERVER_STREAM packet of SubscribeToSettingsChanges whose payload is `4:{13:n}` is
an event; a packet with the same value as the previous event within MERGE_S seconds is added to that event. Output (one event per line): the first
packet's time and frame, the value (1 Off, 2 Noise cancellation, 3 Transparency, 4 Adaptive, 0 no mode settable — PROTOCOL.md §4.1) and every
packet of the event as ch<channel>@<frame>.

Usage:
  python3 scripts/pwhdlc_crc_table.py <log> 0x000b > table.tsv
  python3 scripts/stream_events.py table.tsv [table2.tsv …]
"""
import sys

MERGE_S = 0.4
MODE = {"1": "OFF", "2": "ACTIVE", "3": "TRANSPARENT", "4": "ADAPTIVE", "0": "none"}


def secs(t):
    h, m, s = t.split(":")
    return int(h) * 3600 + int(m) * 60 + float(s)


def main(paths):
    events = []
    for path in paths:
        for line in open(path):
            p = line.rstrip("\n").split("\t")
            if len(p) < 9 or p[5] != "SERVER_STREAM" or "SubscribeToSettingsChanges" not in p[7] or not p[8].startswith("4:{13:"):
                continue
            value = p[8][len("4:{13:"):].rstrip("}")
            if events and events[-1][2] == value and secs(p[1]) - secs(events[-1][0]) < MERGE_S:
                events[-1][3].append(f"ch{p[6]}@{p[0]}")
                continue
            events.append([p[1], p[0], value, [f"ch{p[6]}@{p[0]}"]])
    for t, fn, value, packets in events:
        print(t, fn, MODE.get(value, value), " ".join(packets))
    print(f"# events={len(events)} packets={sum(len(e[3]) for e in events)}", file=sys.stderr)


if __name__ == "__main__":
    if len(sys.argv) < 2:
        sys.exit(__doc__)
    main(sys.argv[1:])
