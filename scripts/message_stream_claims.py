#!/usr/bin/env python3
"""The claim inventory of the Fast Pair Message Stream (RFCOMM DLCI 0x04/0x05) of one ACL handle: who claimed it, how long, what came on it.

PROJECT_RULES.md rule 4a: the tool behind a count is committed. Written in ai-sessions/0092 (CAP-076), after the method of CAP-075-FINDINGS.md §1.

A claim = a phone SABM on DLCI 0x04/0x05 up to the first DISC or DM on it (either side) — or the end of the log/ACL ("open"). Its owner is the
first phone message after the UA: `08 11 …` = OpenControl's Get (DECISIONS.md ADR-032), `03 08 00 02 01 25` = Google Play services' Fast Pair
(Platform type, CAP-075-FINDINGS.md §1); anything else is printed as "other:<4 bytes>" (e.g. `04 01 00 01` = OpenControl's Ring/Stop). Messages are
split as [Group][Code][Len:2 BE][Value] per direction (one RFCOMM frame may hold several).

Output (TSV, one claim per line): #n, the SABM's frame and time, the DLCI, the owner, the end (frame, time, sender, DISC/DM), the duration, and
the frames of the Buds' Notify (`08 13`, with settable/enabled/mode bytes), the phone's Set (`08 12`), the Buds' ACK (`ff 01 00 06 08 …`) and NAK.

Usage (needs tshark on PATH; run from the repository root):
  python3 scripts/message_stream_claims.py <btsnoop log> 0x000b
"""
import datetime
import subprocess
import sys

KIND = {0x2f: "SABM", 0x3f: "SABM", 0x63: "UA", 0x73: "UA", 0x43: "DISC", 0x53: "DISC", 0x0f: "DM", 0x1f: "DM"}
PLAY = bytes.fromhex("030800020125")


def ts(t):
    return datetime.datetime.fromtimestamp(float(t)).strftime("%H:%M:%S.%f")[:-3]


def main(cap, handle):
    flt = f"bthci_acl.chandle=={handle} && (btrfcomm.dlci==4 || btrfcomm.dlci==5)"
    rows = subprocess.run(["tshark", "-r", cap, "-Y", flt, "-T", "fields", "-e", "frame.number", "-e", "frame.time_epoch", "-e", "frame.p2p_dir",
                           "-e", "btrfcomm.dlci", "-e", "btrfcomm.frame_type", "-e", "data.data"],
                          capture_output=True, text=True, check=True).stdout.splitlines()
    claims, cur, bufs = [], None, {}
    for r in rows:
        fn, t, d, dl, ft, data = (r.split("\t") + [""] * 6)[:6]
        kind = KIND.get(int(ft, 16) if ft else 0, "UIH")
        who = "phone" if d == "0" else "buds"
        if kind == "SABM" and who == "phone":
            cur = {"sabm": (fn, t), "dlci": dl, "msgs": [], "owner": None, "end": None}
            claims.append(cur)
            bufs = {}
            continue
        if cur is None or cur["end"] is not None:
            continue
        if kind in ("DISC", "DM"):
            cur["end"] = (fn, t, who, kind)
            continue
        if kind == "UIH" and data:
            b = bufs.setdefault(who, bytearray())
            b.extend(bytes.fromhex(data))
            while len(b) >= 4:
                n = int.from_bytes(b[2:4], "big")
                if len(b) < 4 + n:
                    break
                m = bytes(b[:4 + n])
                del b[:4 + n]
                cur["msgs"].append((fn, t, who, m.hex()))
                if who == "phone" and cur["owner"] is None:
                    cur["owner"] = "app" if m[:2] == b"\x08\x11" else ("play" if m[:6] == PLAY else "other:" + m[:4].hex())
    for i, c in enumerate(claims, 1):
        e = c["end"]
        dur = (float(e[1]) - float(c["sabm"][1])) if e else None
        sel = lambda who, pre: [m for m in c["msgs"] if m[2] == who and m[3].startswith(pre)]  # noqa: E731
        print(f"#{i}\t{c['sabm'][0]}\t{ts(c['sabm'][1])}\tdlci{c['dlci']}\t{c['owner']}\t"
              f"end={e[0] if e else '-'} {ts(e[1]) if e else ''} {e[2] + ' ' + e[3] if e else 'open'}\tdur={'%.3f' % dur if dur else '-'}\t"
              f"notify={','.join(m[0] + ':' + m[3][10:16] for m in sel('buds', '0813'))}\t"
              f"set={','.join(m[0] + ':' + m[3][8:16] for m in sel('phone', '0812'))}\t"
              f"ack={','.join(m[0] for m in sel('buds', 'ff01000608'))}\tnak={','.join(m[0] for m in sel('buds', 'ff02'))}")


if __name__ == "__main__":
    if len(sys.argv) != 3:
        sys.exit(__doc__)
    main(sys.argv[1], sys.argv[2])
