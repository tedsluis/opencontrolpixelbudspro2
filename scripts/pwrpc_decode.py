#!/usr/bin/env python3
"""Decode the DLCI 0x02 pw_rpc conversation (Pigweed pw_hdlc + pw_rpc RpcPacket) in a btsnoop capture.

Usage: scripts/pwrpc_decode.py <capture-btsnoop_hci.log>   (needs tshark on PATH)

Why this exists (ai-sessions/0040, DESKRESEARCH_FINDINGS.md 2026-09-19): the 32-bit service/method
IDs inside DLCI 0x02's constant frame prefix are the pw_rpc/pw_tokenizer 65599 hash of the names
("maestro_pw.Maestro" = 0x7ede71ea, "WriteSetting" = 0x9e8c9a1d, ...), so the payload can be read as a
plain RpcPacket protobuf. Per AGENTS.md §13 the capture is pre-filtered to the RFCOMM DLCI 2 frames;
direction is shown as the HCI source address (which side is the phone is stated in the capture's
own EVENT-NOTES, not guessed here). Field names of RpcPacket: 1 type, 2 channel_id, 3 service_id,
4 method_id, 5 payload, 6 status, 7 call_id (pw_rpc packet.proto).
"""
import collections
import struct
import subprocess
import sys


def h65599(name: str) -> int:
    """pw_tokenizer/pw_rpc 65599 hash: h = len; coeff = 65599; h += coeff*c; coeff *= 65599 (mod 2^32)."""
    b = name.encode()
    h, co = len(b), 65599
    for c in b:
        h = (h + co * c) & 0xFFFFFFFF
        co = (co * 65599) & 0xFFFFFFFF
    return h


SERVICES = {h65599("maestro_pw.Maestro"): "maestro_pw.Maestro"}
METHODS = {h65599(n): n for n in ("WriteSetting", "ReadSetting", "SubscribeToSettingsChanges", "GetSoftwareInfo")}
TYPES = {0: "REQUEST", 1: "RESPONSE", 2: "CLIENT_ERROR", 3: "SERVER_ERROR", 5: "CANCEL", 7: "SERVER_STREAM"}


def varint(b, i):
    r = s = 0
    while True:
        x = b[i]
        i += 1
        r |= (x & 0x7F) << s
        s += 7
        if not x & 0x80:
            return r, i


def parse(b):
    """Generic protobuf parse -> [(field, wiretype, value)], or None if b is not a valid message."""
    out, i = [], 0
    try:
        while i < len(b):
            t, i = varint(b, i)
            f, w = t >> 3, t & 7
            if f == 0:
                return None
            if w == 0:
                v, i = varint(b, i)
            elif w == 1:
                v = b[i:i + 8]; i += 8
            elif w == 5:
                v = b[i:i + 4]; i += 4
            elif w == 2:
                n, i = varint(b, i); v = b[i:i + n]; i += n
                if len(v) != n:
                    return None
            else:
                return None
            out.append((f, w, v))
    except IndexError:
        return None
    return out


def fmt(b, depth=0):
    p = parse(b) if b else []
    if p is None or (not p and b):
        return "0x" + b.hex()
    parts = []
    for f, w, v in p:
        if w == 0:
            parts.append(f"{f}:{v}")
        elif w == 5:
            parts.append(f"{f}:f32({struct.unpack('<f', v)[0]:.2f})")
        elif w == 2 and depth < 4 and v and parse(v) is not None:
            parts.append(f"{f}:{{{fmt(v, depth + 1)}}}")
        elif w == 2:
            parts.append(f"{f}:0x{v.hex()}")
        else:
            parts.append(f"{f}:raw")
    return " ".join(parts)


def unescape(b):
    o, i = bytearray(), 0
    while i < len(b):
        if b[i] == 0x7D:
            o.append(b[i + 1] ^ 0x20); i += 2
        else:
            o.append(b[i]); i += 1
    return bytes(o)


def main(cap):
    rows = subprocess.run(
        ["tshark", "-r", cap, "-Y", "btrfcomm.dlci==2 && btrfcomm.frame_type==0xef", "-T", "fields",
         "-e", "frame.number", "-e", "bluetooth.src", "-e", "data.data"],
        capture_output=True, text=True, check=True).stdout.strip().split("\n")
    streams = collections.OrderedDict()
    for r in rows:
        p = r.split("\t")
        if len(p) == 3 and p[2]:
            streams.setdefault(p[1], bytearray()).extend(bytes.fromhex(p[2]))
    for src, buf in streams.items():
        print(f"== HCI source {src}: {len(buf)} bytes")
        begin = None
        for i, b in enumerate(buf):
            if b != 0x7E:
                continue
            if begin is not None and i > begin + 1:
                u = unescape(bytes(buf[begin + 1:i]))
                j = 0
                while j < len(u) and not u[j] & 1:  # pw_hdlc address: one-terminated varint
                    j += 1
                pl = u[j + 2:-4]  # skip last address byte + control byte; drop CRC-32
                pk = parse(pl)
                if pk:
                    d = {f: v for f, w, v in pk}
                    svc = int.from_bytes(d[3], "little") if 3 in d else None
                    met = int.from_bytes(d[4], "little") if 4 in d else None
                    print(f"  {TYPES.get(d.get(1, 0), d.get(1))} ch={d.get(2)} "
                          f"svc={SERVICES.get(svc, hex(svc) if svc else None)} "
                          f"method={METHODS.get(met, hex(met) if met else None)} status={d.get(6)} "
                          f"| {fmt(d[5]) if 5 in d else ''}")
            begin = i


if __name__ == "__main__":
    main(sys.argv[1])
