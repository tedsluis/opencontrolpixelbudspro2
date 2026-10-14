#!/usr/bin/env python3
"""Cross-check the noise-control mode on the Message Stream (DLCI 0x04/0x05) against Maestro setting 13 (DLCI 0x02/0x03).

PROJECT_RULES.md rule 4a: the tool behind a count is committed. Written in ai-sessions/0090 (the pbpctrl cross-check, E-01/E-02).

For one btsnoop log it lists, in capture order and with each frame's capture time:
  - every `Notify ANC state` (08 13) from the Buds, classed by what the phone sent just before it on the Message Stream:
    `set` (an 08 12 within SET_WINDOW s), `get` (an 08 11 00 00 within GET_WINDOW s), else `unprovoked`;
  - every Maestro `SubscribeToSettingsChanges` REQUEST and every SERVER_STREAM packet whose payload is `4:{13:n}`;
  - every `ReadSetting` RESPONSE `4:{13:n}`, with the Message Stream mode last reported before it;
  - every `WriteSetting` REQUEST `4:{13:n}`.
Then, per Notify, the nearest `4:{13:n}` stream packet within MATCH_WINDOW s (either side), and per stream packet whether a Notify
lies within MATCH_WINDOW s.

Mode byte of the Message Stream (PROTOCOL.md §4.1): 0x20 Off, 0x08 Noise cancellation, 0x80 Transparency, 0x40 Adaptive. The mapping
tested against setting 13 is pbpctrl's / the APK's `qhs` enum: 1 Off, 2 Active, 3 Aware, 4 Adaptive, 0 Unknown.

Usage (needs tshark on PATH; run from the repository root):
  python3 scripts/anc_setting13_crosscheck.py <log> [--handle 0x000b]      the event list and the match tables
  python3 scripts/anc_setting13_crosscheck.py --summary <log> [<log> ...]   one line per log with the counts
  python3 scripts/anc_setting13_crosscheck.py --census <log> [<log> ...]    every event once across the logs, classed (see census())
"""
import collections
import glob
import os
import subprocess
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import pwrpc_decode as pw  # noqa: E402  (the repository's own decoder, same directory)

SET_WINDOW = 3.0
GET_WINDOW = 1.0
MATCH_WINDOW = 1.0
MS_MODE = {0x20: 1, 0x08: 2, 0x80: 3, 0x40: 4}
MODE_NAME = {0: "Unknown", 1: "Off", 2: "NC", 3: "Transp", 4: "Adaptive"}


def frame_times(log):
    out = subprocess.run(["tshark", "-r", log, "-Y", "btrfcomm", "-T", "fields", "-e", "frame.number", "-e", "frame.time_epoch"],
                         capture_output=True, text=True, check=True).stdout
    return {int(a): float(b) for a, b in (l.split("\t") for l in out.splitlines() if "\t" in l)}


def ms_messages(log, handle=None):
    flt = "btrfcomm.len>0 && (btrfcomm.dlci==4 || btrfcomm.dlci==5)"
    if handle:
        flt += f" && bthci_acl.chandle=={handle}"
    out = subprocess.run(["tshark", "-r", log, "-Y", flt, "-T", "fields", "-e", "frame.number", "-e", "frame.p2p_dir",
                          "-e", "bthci_acl.chandle", "-e", "btrfcomm.dlci", "-e", "data.data"],
                         capture_output=True, text=True, check=True).stdout
    buf = collections.defaultdict(bytearray)
    for line in out.splitlines():
        p = line.split("\t")
        if len(p) < 5 or not p[4]:
            continue
        b = buf[(p[1], p[2], p[3])]
        b += bytes.fromhex(p[4].replace(":", ""))
        while len(b) >= 4:
            n = (b[2] << 8) | b[3]
            if n > 1024:
                b.clear()
                break
            if len(b) < 4 + n:
                break
            yield int(p[0]), "Sent" if p[1] == "0" else "Rcvd", p[2], bytes(b[:4 + n])
            del b[:4 + n]


def channel_events(log, handle=None):
    """(time, frame, handle, 'ms'|'maestro', 'open'|'close') from RFCOMM UA (after a SABM) and DISC on DLCI 2/3 and 4/5."""
    flt = "(btrfcomm.dlci==2 || btrfcomm.dlci==3 || btrfcomm.dlci==4 || btrfcomm.dlci==5) && btrfcomm.frame_type!=0xef && btrfcomm.frame_type!=0x03"
    if handle:
        flt += f" && bthci_acl.chandle=={handle}"
    out = subprocess.run(["tshark", "-r", log, "-Y", flt, "-T", "fields", "-e", "frame.number", "-e", "frame.time_epoch",
                          "-e", "bthci_acl.chandle", "-e", "btrfcomm.dlci", "-e", "btrfcomm.frame_type"],
                         capture_output=True, text=True, check=True).stdout
    ev, pending = [], set()
    for line in out.splitlines():
        p = line.split("\t")
        if len(p) < 5:
            continue
        ft = int(p[4], 16) & 0xEF
        kind = "ms" if int(p[3], 16) in (4, 5) else "maestro"
        if ft == 0x2F:
            pending.add((p[2], kind))
        elif ft == 0x63 and (p[2], kind) in pending:
            pending.discard((p[2], kind))
            ev.append((float(p[1]), int(p[0]), p[2], kind, "open"))
        elif ft == 0x43:
            ev.append((float(p[1]), int(p[0]), p[2], kind, "close"))
    return ev


def is_open(chev, kind, t):
    """Whether any `kind` channel is open at time t (last open/close event before t, on any handle)."""
    state = {}
    for e in chev:
        if e[0] > t:
            break
        if e[3] == kind:
            state[e[2]] = e[4] == "open"
    return any(state.values())


def setting13(d):
    """`4:{13:n}` -> n, else None."""
    if 5 not in d:
        return None
    outer = pw.parse(d[5]) or []
    for f, w, v in outer:
        if f == 4 and w == 2:
            inner = pw.parse(v) or []
            if len(inner) == 1 and inner[0][0] == 13 and inner[0][1] == 0:
                return inner[0][2]
    return None


def events(log, handle=None):
    t = frame_times(log)
    ev = []
    for fn, direction, ch, m in ms_messages(log, handle):
        if m[:2] == b"\x08\x13" and direction == "Rcvd" and len(m) >= 8:
            ev.append((t[fn], fn, "notify", {"settable": m[6], "mode": m[7], "hex": m.hex()}))
        elif m[:2] == b"\x08\x12" and direction == "Sent" and len(m) >= 8:
            ev.append((t[fn], fn, "set", {"mode": m[7]}))
        elif m[:4] == b"\x08\x11\x00\x00" and direction == "Sent":
            ev.append((t[fn], fn, "get", {}))
    for frame, src, addr, ctl, d, dlci, direction in pw.packets(log, handle):
        met = pw.METHODS.get(int.from_bytes(d[4], "little")) if 4 in d else None
        kind = pw.TYPES.get(d.get(1, 0))
        n = setting13(d)
        if met == "SubscribeToSettingsChanges" and kind == "REQUEST":
            ev.append((t[frame], frame, "subscribe", {"ch": d.get(2)}))
        elif met == "SubscribeToSettingsChanges" and kind == "SERVER_STREAM" and n is not None:
            ev.append((t[frame], frame, "stream13", {"n": n, "ch": d.get(2)}))
        elif met == "ReadSetting" and kind == "RESPONSE" and n is not None:
            ev.append((t[frame], frame, "read13", {"n": n, "ch": d.get(2)}))
        elif met == "ReadSetting" and kind == "REQUEST" and pw.fmt(d.get(5, b"")) == "4:13":
            ev.append((t[frame], frame, "read13req", {"ch": d.get(2)}))
        elif met == "WriteSetting" and kind == "REQUEST" and n is not None:
            ev.append((t[frame], frame, "write13", {"n": n, "ch": d.get(2)}))
    ev.sort(key=lambda e: (e[0], e[1]))
    return ev


def classify(ev):
    """Notify -> set / get / unprovoked, by the phone's last Message Stream request before it."""
    last = {"set": None, "get": None}
    out = []
    for e in ev:
        if e[2] in ("set", "get"):
            last[e[2]] = e[0]
        if e[2] == "notify":
            c = "unprovoked"
            if last["set"] is not None and e[0] - last["set"] <= SET_WINDOW:
                c = "set"
            elif last["get"] is not None and e[0] - last["get"] <= GET_WINDOW:
                c = "get"
            out.append((e, c))
    return out


def nearest(e, pool):
    best = None
    for p in pool:
        dt = p[0] - e[0]
        if abs(dt) <= MATCH_WINDOW and (best is None or abs(dt) < abs(best[1])):
            best = (p, dt)
    return best


def report(log, handle=None):
    ev = events(log, handle)
    notes = classify(ev)
    streams = [e for e in ev if e[2] == "stream13"]
    notifies = [e for e, _ in notes]
    ts = lambda x: __import__("time").strftime("%H:%M:%S", __import__("time").gmtime(x)) + f".{int((x % 1) * 1000):03d}Z"
    print(f"== {log}" + (f" (handle {handle})" if handle else ""))
    for e in ev:
        if e[2] in ("subscribe", "read13req", "write13"):
            print(f"  {e[1]:>6} {ts(e[0])} {e[2]} {e[3]}")
    print("  -- Notify ANC state (08 13) from the Buds: class, mode, nearest 4:{13:n} stream packet within 1 s")
    for e, c in notes:
        m = nearest(e, streams)
        exp = MS_MODE.get(e[3]["mode"])
        s = f"stream {m[0][1]} n={m[0][3]['n']} dt={m[1]:+.3f}s {'MATCH' if m[0][3]['n'] == exp else 'DIFF'}" if m else "no stream within 1 s"
        print(f"  {e[1]:>6} {ts(e[0])} {c:<10} settable={e[3]['settable']:02x} mode={e[3]['mode']:02x}->{exp} | {s}")
    print("  -- 4:{13:n} stream packets: nearest Notify within 1 s")
    for p in streams:
        m = nearest(p, notifies)
        s = f"notify {m[0][1]} mode={m[0][3]['mode']:02x} dt={-m[1]:+.3f}s" if m else "no Notify within 1 s"
        print(f"  {p[1]:>6} {ts(p[0])} n={p[3]['n']} ({MODE_NAME.get(p[3]['n'])}) | {s}")
    print("  -- ReadSetting 4:13 answers: the Message Stream mode last reported before it (any distance)")
    for e in ev:
        if e[2] == "read13":
            prev = [n for n in notifies if n[0] <= e[0]]
            s = f"last notify {prev[-1][1]} mode={prev[-1][3]['mode']:02x} settable={prev[-1][3]['settable']:02x} {e[0]-prev[-1][0]:.1f}s before" if prev else "no Notify before"
            print(f"  {e[1]:>6} {ts(e[0])} n={e[3]['n']} ({MODE_NAME.get(e[3]['n'])}) | {s}")


def summary(logs):
    for log in logs:
        try:
            ev = events(log)
        except subprocess.CalledProcessError as ex:
            print(f"{os.path.basename(log)}: TSHARK ERROR {ex.returncode}")
            continue
        notes = classify(ev)
        streams = [e for e in ev if e[2] == "stream13"]
        c = collections.Counter(k for _, k in notes)
        unp = [e for e, k in notes if k == "unprovoked"]
        unp_match = sum(1 for e in unp if (m := nearest(e, streams)) and m[0][3]["n"] == MS_MODE.get(e[3]["mode"]))
        unp_any = sum(1 for e in unp if nearest(e, streams))
        k = collections.Counter(e[2] for e in ev)
        print(f"{os.path.basename(log)}: subscribe={k['subscribe']} stream13={k['stream13']} read13={k['read13']} write13={k['write13']} "
              f"notify={len(notes)} (set {c['set']}, get {c['get']}, unprovoked {c['unprovoked']}; unprovoked with a 4:{{13}} "
              f"stream packet within 1 s: {unp_any}, same mode: {unp_match})")


def expected(notify):
    """The setting-13 value a Notify predicts: 0 when Settable is 00 (the Buds allow no mode), else the mode's enum value."""
    return 0 if notify[3]["settable"] == 0 else MS_MODE.get(notify[3]["mode"])


def census(logs):
    """Across logs, every event counted once by (capture time to the ms, kind, value) — a .log.last that repeats a
    neighbour's content adds nothing. Classes:
      Notify unprovoked: stream-same (a 4:{13:n} within 1 s, n = expected), stream-other, none+maestro-closing (the Buds'
      DISC of DLCI 2/3 within 0.1 s after it), none+not-subscribed (no subscription on the open MAESTRO session), none-other;
      Notify after a Set: stream-same / none;
      stream packet: with-notify, no-notify+ms-closed (no Message Stream open on any handle), no-notify+ms-open."""
    seen, rows = set(), collections.Counter()
    detail = collections.defaultdict(list)
    for log in logs:
        ev, chev = events(log), channel_events(log)
        name = os.path.basename(log)
        notes = classify(ev)
        streams = [e for e in ev if e[2] == "stream13"]
        notifies = [e for e, _ in notes]
        subs = [e for e in ev if e[2] == "subscribe"]
        for e, c in notes:
            key = (round(e[0], 3), "notify", e[3]["hex"])
            if key in seen or c == "get":
                continue
            seen.add(key)
            m = nearest(e, streams)
            if m:
                k = f"notify-{c}: stream-{'same' if m[0][3]['n'] == expected(e) else 'other'}"
            else:
                closing = any(x[3] == "maestro" and x[4] == "close" and 0 <= x[0] - e[0] <= 0.1 for x in chev)
                opened = [x[0] for x in chev if x[3] == "maestro" and x[4] == "open" and x[0] <= e[0]]
                subscribed = bool(opened) and any(opened[-1] <= s_[0] <= e[0] for s_ in subs)
                k = (f"notify-{c}: none, Buds close MAESTRO within 0.1 s" if closing else
                     f"notify-{c}: none, MAESTRO not subscribed" if not subscribed else f"notify-{c}: none, other")
            rows[k] += 1
            detail[k].append(f"{name} {e[1]} mode={e[3]['mode']:02x} settable={e[3]['settable']:02x}")
        for p_ in streams:
            key = (round(p_[0], 3), "stream13", p_[3]["n"])
            if key in seen:
                continue
            seen.add(key)
            m = nearest(p_, notifies)
            k = ("stream: with a Notify within 1 s" if m else
                 "stream: no Notify, Message Stream open" if is_open(chev, "ms", p_[0]) else
                 "stream: no Notify, no Message Stream open")
            rows[k] += 1
            detail[k].append(f"{name} {p_[1]} n={p_[3]['n']}")
        # ReadSetting 4:13 against the latest mode the Buds reported before it (a Notify's expected value or a stream n)
        states = sorted([(e[0], expected(e), f"notify {e[1]}") for e in notifies] +
                        [(p_[0], p_[3]["n"], f"stream {p_[1]}") for p_ in streams])
        for e in ev:
            if e[2] != "read13":
                continue
            key = (round(e[0], 3), "read13", e[3]["n"])
            if key in seen:
                continue
            seen.add(key)
            prev = [s_ for s_ in states if s_[0] <= e[0]]
            if not prev:
                k = "read 4:13: no earlier report on this log"
            else:
                k = f"read 4:13: {'same as' if prev[-1][1] == e[3]['n'] else 'differs from'} the latest report"
            rows[k] += 1
            detail[k].append(f"{name} {e[1]} n={e[3]['n']}" + (f" latest {prev[-1][2]} -> {prev[-1][1]}, {e[0]-prev[-1][0]:.1f}s before" if prev else ""))
    for k in sorted(rows):
        print(f"{rows[k]:>4}  {k}")
        if "other" in k or "no Notify" in k or "none" in k or "differs" in k:
            for d in detail[k]:
                print(f"        {d}")


if __name__ == "__main__":
    a = sys.argv[1:]
    if a and a[0] == "--summary":
        summary(a[1:])
    elif a and a[0] == "--census":
        census(a[1:])
    elif len(a) == 3 and a[1] == "--handle":
        report(a[0], a[2])
    else:
        report(a[0])
