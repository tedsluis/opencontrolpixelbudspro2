#!/usr/bin/env python3
"""Decode the DLCI 0x02 pw_rpc conversation (Pigweed pw_hdlc + pw_rpc RpcPacket) in a btsnoop capture.

Usage (needs tshark on PATH):
  scripts/pwrpc_decode.py <capture-btsnoop_hci.log>            list every pw_rpc packet with its frame number
  scripts/pwrpc_decode.py --eq <capture> [<capture> ...]       EQ (qhr field 16/18) ReadSetting responses and
                                                               WriteSetting requests, in capture order
  scripts/pwrpc_decode.py --channels <capture> [<capture> ...] per capture: channel/HDLC address of the Buds' first
                                                               unsolicited Maestro push vs. the phone's requests

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
# pw_rpc packet.proto PacketType. 0/1/7 are observed in the captures (REQUEST from the phone, RESPONSE and
# SERVER_STREAM from the Buds); 2/4/5/8 are from the public proto (ai-sessions/0041 corrected an earlier table
# that had 2/3/5 wrong).
TYPES = {0: "REQUEST", 1: "RESPONSE", 2: "CLIENT_STREAM", 4: "CLIENT_ERROR", 5: "SERVER_ERROR",
         7: "SERVER_STREAM", 8: "CLIENT_REQUEST_COMPLETION"}
# pw_rpc Status codes seen in the captures (google.rpc.Code numbering).
STATUS = {0: "OK", 1: "CANCELLED", 2: "UNKNOWN", 5: "NOT_FOUND", 9: "FAILED_PRECONDITION"}


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


def packets(cap):
    """Yield (frame_number, hci_source, hdlc_address_hex, hdlc_control, rpc_fields) for every pw_hdlc frame on
    RFCOMM DLCI 2 whose payload parses as an RpcPacket. The frame number is the btsnoop frame that carried the
    frame's closing 0x7E flag."""
    rows = subprocess.run(
        ["tshark", "-r", cap, "-Y", "btrfcomm.dlci==2 && btrfcomm.frame_type==0xef", "-T", "fields",
         "-e", "frame.number", "-e", "bluetooth.src", "-e", "data.data"],
        capture_output=True, text=True, check=True).stdout.strip().split("\n")
    streams = collections.OrderedDict()
    for r in rows:
        p = r.split("\t")
        if len(p) == 3 and p[2]:
            buf, marks = streams.setdefault(p[1], (bytearray(), []))
            marks.append((len(buf), int(p[0])))
            buf.extend(bytes.fromhex(p[2]))
    for src, (buf, marks) in streams.items():
        begin = None
        for i, b in enumerate(buf):
            if b != 0x7E:
                continue
            if begin is not None and i > begin + 1:
                u = unescape(bytes(buf[begin + 1:i]))
                j = 0
                while j < len(u) and not u[j] & 1:  # pw_hdlc address: one-terminated varint
                    j += 1
                if len(u) >= j + 6:
                    pk = parse(u[j + 2:-4])  # skip last address byte + control byte; drop CRC-32
                    if pk:
                        frame = max((fn for off, fn in marks if off <= i), default=None)
                        yield frame, src, u[:j + 1].hex(), u[j + 1], {f: v for f, w, v in pk}
            begin = i


def describe(d):
    svc = int.from_bytes(d[3], "little") if 3 in d else None
    met = int.from_bytes(d[4], "little") if 4 in d else None
    return (f"{TYPES.get(d.get(1, 0), d.get(1))} ch={d.get(2)} "
            f"svc={SERVICES.get(svc, hex(svc) if svc else None)} "
            f"method={METHODS.get(met, hex(met) if met else None)} "
            f"status={STATUS.get(d.get(6, 0), d.get(6))} call_id={d.get(7)} "
            f"| {fmt(d[5]) if 5 in d else ''}")


def eq_quintet(payload):
    """`4:{N:{1..5 float32}}` -> (N, [5 floats]) for N in (16, 18), else None."""
    outer = parse(payload)
    if not outer:
        return None
    for f, w, v in outer:
        if f == 4 and w == 2:
            inner = parse(v)
            if inner and len(inner) == 1 and inner[0][0] in (16, 18) and inner[0][1] == 2:
                vals = parse(inner[0][2])
                if vals and len(vals) == 5 and all(w2 == 5 for _, w2, _ in vals):
                    return inner[0][0], [round(struct.unpack("<f", v2)[0], 2) for _, _, v2 in vals]
    return None


def main_list(cap):
    for frame, src, addr, control, d in packets(cap):
        print(f"{frame:>6} addr={addr} ctl={control:02x} {describe(d)}")


def main_eq(caps):
    for cap in caps:
        print(f"== {cap}")
        for frame, src, addr, control, d in packets(cap):
            met = int.from_bytes(d[4], "little") if 4 in d else None
            q = eq_quintet(d[5]) if 5 in d else None
            name = METHODS.get(met)
            if q and name in ("ReadSetting", "WriteSetting"):
                kind = {(0, "ReadSetting"): "read-resp?", (1, "ReadSetting"): "READ  <-", (0, "WriteSetting"): "WRITE ->"}.get(
                    (d.get(1, 0), name), describe(d)[:20])
                print(f"{frame:>6} ch={d.get(2)} addr={addr} {kind} field {q[0]}: {q[1]}")


def main_channels(caps):
    for cap in caps:
        first, req, resp = None, collections.Counter(), collections.Counter()
        for frame, src, addr, control, d in packets(cap):
            svc = int.from_bytes(d[3], "little") if 3 in d else None
            met = int.from_bytes(d[4], "little") if 4 in d else None
            if svc not in SERVICES:
                continue
            kind = d.get(1, 0)
            if first is None and METHODS.get(met) == "GetSoftwareInfo" and kind == 1 and d.get(7) == 0xFFFFFFFF:
                first = (d.get(2), addr, frame)
            if METHODS.get(met) in ("ReadSetting", "WriteSetting", "SubscribeToSettingsChanges"):
                (req if kind == 0 else resp)[(d.get(2), addr)] += 1
        if first or req:
            top = req.most_common(1)[0][0][0] if req else None
            print(f"{cap.split('/')[-1]}: first push (ch, addr, frame)={first} | requests (ch, addr)={dict(req)} "
                  f"| most-used request channel == first-push channel: {top == (first[0] if first else None)}")


if __name__ == "__main__":
    if sys.argv[1] == "--eq":
        main_eq(sys.argv[2:])
    elif sys.argv[1] == "--channels":
        main_channels(sys.argv[2:])
    else:
        main_list(sys.argv[1])
