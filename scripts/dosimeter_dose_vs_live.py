#!/usr/bin/env python3
"""The Buds' Dosimeter day dose against the integral of their live value, per channel — no sound-level meter needed.

PROJECT_RULES.md rule 4a: the tool behind a count is committed. Written in ai-sessions/0100 (CAP-079, WELL-002 without a meter).

Input: one or more outputs of scripts/pwhdlc_crc_table.py (TSV), for example
    python3 scripts/pwhdlc_crc_table.py CAP-079-btsnoop_hci4.log.last 0x000b > h3.tsv
    python3 scripts/pwhdlc_crc_table.py CAP-079-btsnoop_hci4.log 0x000b > h4.tsv
    python3 scripts/dosimeter_dose_vs_live.py h3.tsv h4.tsv
The rows of all files are merged in time order (HH:MM:SS.mmm of one day). For each channel (19, 21), between two consecutive
`maestro_pw.Dosimeter/FetchDailySummaries` RESPONSEs with the same current day (field 1), it prints:
  - the change of that day's entry (field 2 with 1 = the current day, its field 6) and of field 5;
  - the integral of the `SubscribeToLiveDb` SERVER_STREAM values (field 2, f32) on that channel over the same interval, in value x seconds,
    each value held until the next packet; a gap longer than MAX_HOLD_S counts MAX_HOLD_S (in silence the Buds send few packets, value 0);
  - the ratio (dose change) / (integral). A ratio of 1/60 = 0.01667 means the dose is the integral in value x minutes.
Raw numbers only: no unit, no dose, no health reading (draft DECISIONS.md ADR-060).
"""
import re
import sys

MAX_HOLD_S = 2.0


def read_rows(paths):
    rows = []
    for path in paths:
        with open(path) as fh:
            for line in fh:
                if line.startswith('#'):
                    continue
                c = line.rstrip('\n').split('\t')
                if len(c) < 9:
                    continue
                h, m, s = c[1].split(':')
                t = int(h) * 3600 + int(m) * 60 + float(s)
                rows.append((t, c[0], c[5], int(c[6]) if c[6].isdigit() else None, c[7], c[8]))
    rows.sort()
    return rows


def hms(t):
    return f"{int(t // 3600):02d}:{int(t % 3600 // 60):02d}:{t % 60:06.3f}"


def main(paths):
    rows = read_rows(paths)
    for ch in (19, 21):
        snaps, live = [], []
        for t, frame, typ, channel, method, fields in rows:
            if channel != ch:
                continue
            if method.endswith('FetchDailySummaries') and typ == 'RESPONSE':
                cur = re.match(r'1:(\d+)', fields)
                if not cur:
                    continue
                day = int(cur.group(1))
                entry = re.search(r'2:\{1:%d 6:f32\(([\d.eE+-]+)\)\}' % day, fields)
                f5 = re.search(r' 5:f32\(([\d.eE+-]+)\)', fields)
                snaps.append((t, frame, day, float(entry.group(1)) if entry else None, float(f5.group(1)) if f5 else None))
            elif method.endswith('SubscribeToLiveDb') and typ == 'SERVER_STREAM':
                v = re.search(r'2:f32\(([\d.eE+-]+)\)', fields)
                live.append((t, float(v.group(1)) if v else 0.0))
        print(f"== channel {ch}: {len(snaps)} summaries, {len(live)} live packets")
        for a, b in zip(snaps, snaps[1:]):
            seg = [(t, v) for t, v in live if a[0] <= t <= b[0]]
            integral = sum(v1 * min(t2 - t1, MAX_HOLD_S) for (t1, v1), (t2, _) in zip(seg, seg[1:]))
            gaps = sum(1 for (t1, _), (t2, _) in zip(seg, seg[1:]) if t2 - t1 > MAX_HOLD_S)
            dd = b[3] - a[3] if a[2] == b[2] and a[3] is not None and b[3] is not None else None
            d5 = b[4] - a[4] if a[4] is not None and b[4] is not None else None
            ratio = f"{dd / integral:.4g}" if dd is not None and integral else "-"
            print(f"{hms(a[0])} ({a[1]}) -> {hms(b[0])} ({b[1]}): day {a[2]}->{b[2]}"
                  f"  d(day entry) {'-' if dd is None else f'{dd:.4g}'}  d(field 5) {'-' if d5 is None else f'{d5:.4g}'}"
                  f"  live packets {len(seg)} (gaps > {MAX_HOLD_S:g} s: {gaps})  integral {integral:.4g}  ratio {ratio}")


if __name__ == '__main__':
    if len(sys.argv) < 2:
        sys.exit(__doc__)
    main(sys.argv[1:])
