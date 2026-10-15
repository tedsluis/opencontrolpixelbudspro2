#!/usr/bin/env python3
"""Every pw_hdlc frame of the MAESTRO channel (RFCOMM DLCI 0x02/0x03) of one ACL handle, CRC-32 checked, as a table.

PROJECT_RULES.md rule 4a: the tool behind a count is committed. Written in ai-sessions/0092 (CAP-076).

The RFCOMM payloads are reassembled per (DLCI, direction), split on the 0x7E flags, unescaped (0x7D xx -> xx ^ 0x20); each frame's CRC-32
(zlib polynomial, little-endian trailer) is checked over address + control + data (PROTOCOL.md §2.2a); the payload after the one-terminated
address and the control byte is parsed as a pw_rpc RpcPacket with scripts/pwrpc_decode.py's own functions. pwrpc_decode.py does not check
the CRC; this table does, and its parsed-frame count equals `pwrpc_decode.py --handle <h> <log> | wc -l`.

Output (TSV, one frame per line, capture order): frame, time, p2p_dir (0 = phone -> Buds, 1 = Buds -> phone), dlci, crc (ok/BAD), type,
channel_id, service/method ("M/" = maestro_pw.Maestro), the decoded payload (+ status/call_id), the raw frame hex. A summary line
"# frames=N crc_bad=M" goes to stderr.

Usage (needs tshark on PATH; run from the repository root):
  python3 scripts/pwhdlc_crc_table.py <btsnoop log> 0x000b > table.tsv
"""
import collections
import datetime
import os
import subprocess
import sys
import zlib

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import pwrpc_decode as P  # noqa: E402


def main(cap, handle):
    flt = f"(btrfcomm.dlci==2 || btrfcomm.dlci==3) && btrfcomm.frame_type==0xef && bthci_acl.chandle=={handle}"
    rows = subprocess.run(["tshark", "-r", cap, "-Y", flt, "-T", "fields", "-e", "frame.number", "-e", "frame.time_epoch",
                           "-e", "data.data", "-e", "btrfcomm.dlci", "-e", "frame.p2p_dir"],
                          capture_output=True, text=True, check=True).stdout.splitlines()
    streams = collections.OrderedDict()
    for r in rows:
        p = r.split("\t")
        if len(p) < 5 or not p[2]:
            continue
        buf, marks = streams.setdefault((p[3], p[4]), (bytearray(), []))
        marks.append((len(buf), int(p[0]), float(p[1])))
        buf.extend(bytes.fromhex(p[2]))
    out, bad, total = [], 0, 0
    for (dlci, direction), (buf, marks) in streams.items():
        begin = None
        for i, b in enumerate(buf):
            if b != 0x7E:
                continue
            if begin is not None and i > begin + 1:
                raw = bytes(buf[begin:i + 1])
                u = P.unescape(bytes(buf[begin + 1:i]))
                fn, t = max(((f, tt) for off, f, tt in marks if off <= i), default=(None, None))
                if u is None or len(u) < 6:
                    out.append((fn, t, direction, dlci, "SHORT", "", "", "", "", raw.hex()))
                    begin = i
                    continue
                total += 1
                crc_ok = zlib.crc32(u[:-4]) == int.from_bytes(u[-4:], "little")
                bad += 0 if crc_ok else 1
                j = 0
                while j < len(u) and not u[j] & 1:
                    j += 1
                pk = P.parse(u[j + 2:-4])
                if pk:
                    d = {f: v for f, w, v in pk}
                    svc = int.from_bytes(d[3], "little") if 3 in d else None
                    met = int.from_bytes(d[4], "little") if 4 in d else None
                    name = (P.SERVICES.get(svc, hex(svc) if svc else "").replace("maestro_pw.Maestro", "M") + "/" +
                            P.METHODS.get(met, hex(met) if met else ""))
                    payload = (P.fmt(d[5]) if 5 in d else "") + (f" status={P.STATUS.get(d[6], d[6])}" if 6 in d else "") + \
                        (f" call_id={d[7]}" if 7 in d else "")
                    out.append((fn, t, direction, dlci, "ok" if crc_ok else "BAD", P.TYPES.get(d.get(1, 0), d.get(1)), d.get(2), name,
                                payload, raw.hex()))
                else:
                    out.append((fn, t, direction, dlci, "ok" if crc_ok else "BAD", "NOPARSE", "", "", "", raw.hex()))
            begin = i
    for o in sorted(out, key=lambda x: x[0] or 0):
        t = datetime.datetime.fromtimestamp(o[1]).strftime("%H:%M:%S.%f")[:-3] if o[1] else ""
        print("\t".join(str(x) for x in (o[0], t) + o[2:]))
    print(f"# frames={total} crc_bad={bad}", file=sys.stderr)


if __name__ == "__main__":
    if len(sys.argv) != 3:
        sys.exit(__doc__)
    main(sys.argv[1], sys.argv[2])
