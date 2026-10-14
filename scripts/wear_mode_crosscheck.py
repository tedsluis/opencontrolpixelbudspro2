#!/usr/bin/env python3
"""The Buds' noise-control mode against their wear signal: does each wear state keep its own mode?

PROJECT_RULES.md rule 4a: the tool behind a count is committed. Written in ai-sessions/0090 (continuation in chat, 2026-10-14).

Wear signal: the Buds' GSND CONTROL message `04 05 00 02 08 XX` (DLCI 0x08/0x09, from the Buds; XX 03 = no bud worn, 04/05 = one bud,
06 = both — a HYPOTHESIS, CAP-069-FINDINGS.md §8). Mode: every `Notify ANC state` (Settable 00 -> 0) and every setting-13 value on DLCI
0x02 (stream or ReadSetting answer), read with scripts/anc_setting13_crosscheck.py. Value 0 (no mode settable) is not a stored mode.

Usage (needs tshark on PATH; run from the repository root):
  python3 scripts/wear_mode_crosscheck.py <log> [<log> ...]            every mode change, classed app / at a wear change / no wear change
  python3 scripts/wear_mode_crosscheck.py --restore <log> [<log> ...]  the restore test: on entering one/two, does the mode equal the last
                                                                        mode seen in that same state earlier in the log?
"""
import collections
import os
import subprocess
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import anc_setting13_crosscheck as x  # noqa: E402

WEAR = {3: "none", 4: "one", 5: "one", 6: "two"}
SET_WINDOW = 3.0       # a mode change within 3 s after the phone's 08 12 is the app's
WEAR_BEFORE = 3.0      # a wear change up to 3 s before (or 1 s after) a mode change counts as its cause
WEAR_AFTER = 1.0
REPORT_WINDOW = 2.5    # the restore test reads the Buds' first reports up to 2.5 s after a wear change


def wear_events(log):
    """(time, frame, XX) for every `04 05 00 02 08 XX` from the Buds outside the MAESTRO and Message Stream DLCIs."""
    out = subprocess.run(["tshark", "-r", log, "-Y", "btrfcomm.len>0 && frame.p2p_dir==1 && frame contains 04:05:00:02:08",
                          "-T", "fields", "-e", "frame.number", "-e", "frame.time_epoch", "-e", "btrfcomm.dlci", "-e", "data.data"],
                         capture_output=True, text=True, check=True).stdout
    ev = []
    for line in out.splitlines():
        p = line.split("\t")
        if len(p) < 4 or int(p[2], 16) in (2, 3, 4, 5):
            continue
        d = p[3].replace(":", "")
        i = d.find("0405000208")
        if i >= 0 and len(d) >= i + 12:
            ev.append((float(p[1]), int(p[0]), int(d[i + 10:i + 12], 16)))
    return ev


def mode_reports(ev):
    """(time, frame, mode) from Notifies (Settable 00 -> 0) and setting-13 streams/reads, in time order."""
    return sorted([(e[0], e[1], x.expected(e)) for e in ev if e[2] == "notify"] +
                  [(e[0], e[1], e[3]["n"]) for e in ev if e[2] in ("stream13", "read13")])


def classify(logs):
    seen, rows = set(), []
    for log in logs:
        we = wear_events(log)
        if not we:
            continue
        ev = x.events(log)
        sets = [e[0] for e in ev if e[2] == "set"]
        mode = None
        for t, fn, n in mode_reports(ev):
            if n is None:
                continue
            if mode is not None and n != mode and (round(t, 1), n) not in seen:
                seen.add((round(t, 1), n))
                before = [w for w in we if w[0] <= t - WEAR_BEFORE]
                near = [w for w in we if t - WEAR_BEFORE < w[0] <= t + WEAR_AFTER]
                wb = WEAR.get(before[-1][2]) if before else "?"
                wa = WEAR.get((near or before)[-1][2]) if (near or before) else "?"
                kind = ("app" if any(0 <= t - s <= SET_WINDOW for s in sets)
                        else "wear" if near and wa != wb else "no-wear")
                rows.append((os.path.basename(log), fn, mode, n, wb, wa, kind, [w[2] for w in near]))
            mode = n
    print("mode changes by class:", dict(collections.Counter(r[6] for r in rows)))
    for r in rows:
        print(f"  {r[0]:<32} {r[1]:>6} {r[2]}->{r[3]}  wear {r[4]}->{r[5]} {r[7]}  [{r[6]}]")


def restore(logs):
    res, det = collections.Counter(), []
    for log in logs:
        we = wear_events(log)
        if not we:
            continue
        rep = [r for r in mode_reports(x.events(log)) if r[2]]
        last, cur = {}, None
        for wt, wfn, wv in we:
            st = WEAR.get(wv)
            if st == cur:
                continue
            nxt = min([w[0] for w in we if w[0] > wt and WEAR.get(w[2]) != st] or [1e18])
            after = [r for r in rep if wt - 0.3 <= r[0] <= min(wt + REPORT_WINDOW, nxt - 0.01)]
            if st in ("one", "two") and after and st in last:
                ok = after[-1][2] == last[st]
                res["match" if ok else "differ"] += 1
                det.append(f"{os.path.basename(log)} wear {wfn} {cur}->{st}: mode {after[-1][2]} (frame {after[-1][1]}), "
                           f"last {st}-mode {last[st]} {'OK' if ok else 'DIFF'}")
            elif st in ("one", "two"):
                res["first visit" if after else "no report within 2.5 s"] += 1
            cur = st
            inside = [r for r in rep if wt - 0.3 <= r[0] < nxt - 0.3]
            if st in ("one", "two") and inside:
                last[st] = inside[-1][2]
    print(dict(res))
    for d in det:
        print("  " + d)


if __name__ == "__main__":
    a = sys.argv[1:]
    if a and a[0] == "--restore":
        restore(a[1:])
    else:
        classify(a)
