#!/usr/bin/env python3
"""Deterministic [Group:1][Code:1][Length:2BE][Value:Length] TLV decoder for DLCI 0x08's private
envelope (PROTOCOL.md §2.3), used by CAP-050-FINDINGS.md.

RFCOMM server-channel numbers (and therefore which DLCI carries the private envelope on a given
reconnect) are session-local, not fixed (CAP-001-FINDINGS.md §2, CAP-038-FINDINGS.md §3) -- this
script does not assume a fixed DLCI; callers must select the correct (chandle, dlci) pair per
reconnect first, e.g. by locating the constant "google-pixel-buds-pro-v1" capability string
(Group 0x0e Code 0x02) via:

    tshark -r <log> -Y 'data.data contains 67:6f:6f:67:6c:65:2d:70:69:78:65:6c:2d:62:75:64:73:2d:70:72:6f:2d:76:31' \
        -T fields -e frame.number -e frame.time -e bthci_acl.chandle -e btrfcomm.dlci

Input: a tshark TSV with columns frame.number|frame.time_epoch|bthci_acl.chandle|btrfcomm.dlci|
frame.p2p_dir|data.data, e.g.:

    tshark -r <log> -Y "(btrfcomm.dlci==8 or btrfcomm.dlci==9) and btrfcomm.len>0" \
        -T fields -E separator='|' -e frame.number -e frame.time_epoch -e bthci_acl.chandle \
        -e btrfcomm.dlci -e frame.p2p_dir -e data.data > out.tsv

Per AGENTS.md §13.6: this script performs mechanical TLV parsing only -- it never guesses field
semantics. Group/Code/Value are printed as raw hex for the caller to interpret against documented
evidence.
"""
import sys
import collections


def parse_stream(rows):
    """rows: [(frameno, time, hex), ...] for one (chandle, dlci, direction) stream, frame-ordered.
    RFCOMM UIH frames on one DLCI are a continuous byte stream (not message-delimited at the socket
    level, ARCHITECTURE.md §5) -- concatenate before parsing."""
    buf = bytearray()
    frame_bounds = []
    for frameno, t, h in rows:
        frame_bounds.append((len(buf), frameno, t))
        buf += bytes.fromhex(h)
    msgs = []
    i = 0
    while i < len(buf):
        if i + 4 > len(buf):
            msgs.append(("TRUNCATED_HEADER", i, buf[i:].hex()))
            break
        group, code = buf[i], buf[i + 1]
        length = (buf[i + 2] << 8) | buf[i + 3]
        val_start, val_end = i + 4, i + 4 + length
        if val_end > len(buf):
            msgs.append(("TRUNCATED_VALUE", i, group, code, length))
            break
        value = bytes(buf[val_start:val_end])
        start_frame = None
        for off, fn, t in frame_bounds:
            if off <= i:
                start_frame = (fn, t)
        msgs.append(("TLV", i, group, code, length, value, start_frame))
        i = val_end
    return msgs


def load(tsv_path):
    per_key = collections.defaultdict(list)  # (chandle, dlci, dir) -> [(frameno, time, hex), ...]
    with open(tsv_path) as f:
        for line in f:
            parts = line.rstrip("\n").split("|")
            if len(parts) < 6:
                continue
            frameno, t, chandle, dlci, d, h = parts
            if not h:
                continue
            per_key[(chandle, dlci, d)].append((int(frameno), float(t), h))
    return per_key


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(1)
    pk = load(sys.argv[1])
    want_chandle = sys.argv[2] if len(sys.argv) > 2 else None
    want_dlci = sys.argv[3] if len(sys.argv) > 3 else None
    for (chandle, dlci, d), rows in sorted(pk.items(), key=lambda x: (x[0][0], x[0][1], x[0][2])):
        if want_chandle and chandle != want_chandle:
            continue
        if want_dlci and dlci != want_dlci:
            continue
        rows.sort()
        msgs = parse_stream(rows)
        dirname = "Sent" if d == "0" else "Rcvd"
        print(f"=== chandle {chandle} dlci {dlci} dir={dirname} ({len(rows)} frames) ===")
        for m in msgs:
            if m[0] == "TLV":
                _, off, group, code, length, valhex, sf = m
                print(
                    f"  off={off:5d} frame={sf[0]:6d} t={sf[1]:.6f} "
                    f"Group=0x{group:02x} Code=0x{code:02x} Len={length:5d} Value={valhex.hex()}"
                )
            else:
                print(f"  {m}")


if __name__ == "__main__":
    main()
