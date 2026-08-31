#!/usr/bin/env python3
"""
Decode DLCI 0x02 ("libmaestro" Pigweed pw_hdlc channel) frames from a
CAP-NNN-btsnoop_hci.log against the wire-confirmed nesting

    HDLC frame -> unescape -> CRC-32 verify -> [Address][Control][RpcPacket bytes][CRC]
    RpcPacket.field5 (payload, BYTES) -> qjc/qja (deserialized) .field4 (BYTES) -> qhr.fieldN

This nesting, the HDLC unescape/CRC-32 method, and the `qhr` field register are
all 🟢 FACT / already-confirmed per `PROTOCOL.md` §2.2a and `DECISIONS.md`
ADR-019, for 4 sampled frames across CAP-020 and CAP-021
(`REVERSE_ENGINEERING.md`'s `qjc`/`qja` and `qhr` entries, 2026-08-30 updates).
This script mechanically applies that SAME algorithm to every DLCI 0x02 frame
in any capture, instead of the prior one-off, per-frame manual decode.

Design note vs. the original CAP-020-FINDINGS.md addendum script: that script
hardcoded a hex-editor-derived "skip 13 bytes past Address+Control before
field 5" offset, which was only checked against 2 (CAP-020) + 4 (CAP-021)
frames sharing one capture session (and even across those two sessions the
header bytes were NOT byte-identical -- only same-length, see this script's
own commit history / CHANGELOG for the verification). Rather than assume that
offset generalizes to every capture (`PROTOCOL.md` §2.2a §"Not a fixed/
exhaustive set" already documents that the HDLC Address field itself can be
1, 2, 3, or more bytes depending on session), this script performs a small,
deterministic search over Address-field lengths 1-4 and accepts an offset
only when the remaining bytes parse as a FULLY self-consistent, FULLY
consuming top-level protobuf message (every byte accounted for, no invalid
wire type, no out-of-bounds read) that contains a field 5 of wire type 2
(length-delimited) -- i.e. RpcPacket.payload. If zero or more than one
Address-length candidate parses cleanly, the frame is reported UNPARSEABLE /
AMBIGUOUS respectively rather than force-fit to a guessed offset, per
PROJECT_RULES.md §1's "operate with zero creativity" rule.

This is a purely mechanical byte decode. It establishes WHICH qhr field
number/value was written/read in which frame -- it does NOT itself establish
what that field means or correlate it to a user action; that correlation
against each capture's own CAP-NNN-EVENT-NOTES.md is a separate step (see
this script's own companion analysis, not performed by this script).

Usage:
    python3 scripts/decode_qhr_settings.py <CAP-NNN-btsnoop_hci.log> [...] [--csv out.csv]

Requires `tshark` on PATH. For every capture log, runs exactly:
    tshark -r <log> -Y "btrfcomm.dlci==0x02 and btrfcomm.len>0" \\
        -T fields -E separator='|' \\
        -e frame.number -e frame.time_epoch -e frame.p2p_dir -e data.data
(frame.p2p_dir 0 = Sent, 1 = Rcvd -- the convention already used throughout
this project's own CAP-NNN-FINDINGS.md tshark commands, e.g. CAP-005/CAP-009.)
"""
import binascii
import csv
import struct
import subprocess
import sys


def unescape_hdlc(data: bytes) -> bytes:
    out = bytearray()
    i = 0
    n = len(data)
    while i < n:
        b = data[i]
        if b == 0x7D:
            i += 1
            if i >= n:
                raise ValueError("truncated escape sequence")
            out.append(data[i] ^ 0x20)
        else:
            out.append(b)
        i += 1
    return bytes(out)


def read_varint(data: bytes, i: int):
    val = 0
    shift = 0
    n = len(data)
    while True:
        if i >= n or shift > 63:
            return None
        b = data[i]
        val |= (b & 0x7F) << shift
        i += 1
        if not (b & 0x80):
            return val, i
        shift += 7


def parse_message(data: bytes):
    """Parse `data` as a sequence of top-level protobuf fields (tag = (field<<3)|wiretype).
    Returns a list of (field_num, wiretype, value) ONLY if the whole buffer is
    consumed with no invalid wiretype / no out-of-bounds read; else None."""
    i = 0
    n = len(data)
    fields = []
    if n == 0:
        return None
    while i < n:
        r = read_varint(data, i)
        if r is None:
            return None
        tag, i2 = r
        fnum = tag >> 3
        wt = tag & 7
        if fnum == 0:
            return None
        i = i2
        if wt == 0:
            r = read_varint(data, i)
            if r is None:
                return None
            val, i = r
        elif wt == 1:
            if i + 8 > n:
                return None
            val = data[i:i + 8]
            i += 8
        elif wt == 2:
            r = read_varint(data, i)
            if r is None:
                return None
            ln, i = r
            if ln < 0 or i + ln > n:
                return None
            val = data[i:i + ln]
            i += ln
        elif wt == 5:
            if i + 4 > n:
                return None
            val = data[i:i + 4]
            i += 4
        else:
            return None  # wiretype 3/4 (deprecated groups) never used here
        fields.append((fnum, wt, val))
    return fields


def find_rpc_packet_candidates(body: bytes):
    """body = unescaped subframe minus its trailing 4-byte CRC. Try HDLC
    Address field lengths 1..4 (+ 1 Control byte) and return every
    (addr_len, fields) whose remainder parses as a full, self-consistent
    message containing a field-5 length-delimited entry (RpcPacket.payload)."""
    candidates = []
    for addr_len in (1, 2, 3, 4):
        start = addr_len + 1  # + 1 Control byte
        if start >= len(body):
            continue
        fields = parse_message(body[start:])
        if fields is None:
            continue
        if any(fnum == 5 and wt == 2 for fnum, wt, _ in fields):
            candidates.append((addr_len, fields))
    return candidates


def decode_qhr(field5_bytes: bytes):
    """field5_bytes = RpcPacket.payload, expected to be a serialized qjc/qja.
    Returns (qhr_field_num, qhr_wiretype, qhr_value) or None."""
    qjc_fields = parse_message(field5_bytes)
    if qjc_fields is None:
        return None
    f4_candidates = [v for fnum, wt, v in qjc_fields if fnum == 4 and wt == 2]
    if not f4_candidates:
        return None
    for qhr_bytes in f4_candidates:
        qhr_fields = parse_message(qhr_bytes)
        if qhr_fields is not None and len(qhr_fields) == 1:
            return qhr_fields[0]
    return None


def split_subframes(raw: bytes):
    """Split a raw RFCOMM payload on the 0x7E HDLC flag byte, per PROTOCOL.md
    §2.2a's own verification method ("split each RFCOMM payload on the 0x7E
    flag byte"). Returns a list of non-empty inter-flag byte spans."""
    parts = []
    cur = bytearray()
    for b in raw:
        if b == 0x7E:
            if cur:
                parts.append(bytes(cur))
                cur = bytearray()
        else:
            cur.append(b)
    if cur:
        parts.append(bytes(cur))
    return parts


DIR_NAME = {"0": "Sent", "1": "Rcvd"}

WT_NAME = {0: "VARINT", 1: "FIXED64", 2: "BYTES", 5: "FIXED32"}


def decode_capture(log_path: str):
    """Yields one dict per (frame, subframe) DLCI 0x02 record."""
    cmd = [
        "tshark", "-r", log_path,
        "-Y", "btrfcomm.dlci==0x02 and btrfcomm.len>0",
        "-T", "fields", "-E", "separator=|",
        "-e", "frame.number", "-e", "frame.time_epoch",
        "-e", "frame.p2p_dir", "-e", "data.data",
    ]
    proc = subprocess.run(cmd, capture_output=True, text=True, check=True)
    for line in proc.stdout.splitlines():
        line = line.strip()
        if not line:
            continue
        parts = line.split("|")
        if len(parts) != 4:
            continue
        frame_no, ts, p2p_dir, hexstr = parts
        if not hexstr:
            continue
        direction = DIR_NAME.get(p2p_dir, f"dir={p2p_dir}")
        raw = bytes.fromhex(hexstr)
        subframes = split_subframes(raw)
        for sub_idx, sub in enumerate(subframes):
            record = {
                "frame": frame_no, "timestamp": ts, "direction": direction,
                "subframe_index": sub_idx, "raw_hex": sub.hex(),
                "status": None, "qhr_field": "", "qhr_wiretype": "",
                "qhr_value": "", "detail": "",
            }
            if len(sub) < 5:
                record["status"] = "TOO_SHORT"
                yield record
                continue
            try:
                un = unescape_hdlc(sub)
            except ValueError as e:
                record["status"] = "ESCAPE_ERROR"
                record["detail"] = str(e)
                yield record
                continue
            if len(un) < 5:
                record["status"] = "TOO_SHORT_UNESCAPED"
                yield record
                continue
            body, trailer = un[:-4], un[-4:]
            calc = struct.pack("<I", binascii.crc32(body) & 0xFFFFFFFF)
            if calc != trailer:
                record["status"] = "CRC_MISMATCH"
                record["detail"] = f"calc={calc.hex()} trailer={trailer.hex()}"
                yield record
                continue
            candidates = find_rpc_packet_candidates(body)
            if not candidates:
                record["status"] = "NO_RPCPACKET_MATCH"
                yield record
                continue
            # More than one HDLC Address-field length can produce a
            # structurally valid top-level parse (a longer address length
            # can "accidentally" realign onto what is really the middle of
            # the true parse's own field-3/field-4 bytes, since a fixed32
            # field's raw bytes can themselves look like a valid tag+varint).
            # What matters is whether that ambiguity actually changes the
            # recovered qhr field -- decode every candidate down to qhr and
            # only flag AMBIGUOUS if they disagree; if they all agree
            # (empirically the common case: the shorter, "extra" leading
            # fields a longer addr_len misses, e.g. channel_id, sit strictly
            # before field 5 and never change field 5's own byte range),
            # report the agreed value.
            qhr_results = []
            for addr_len, top_fields in candidates:
                f5_list = [v for fnum, wt, v in top_fields if fnum == 5 and wt == 2]
                for f5 in f5_list:
                    qhr = decode_qhr(f5)
                    if qhr is not None:
                        qhr_results.append((addr_len, qhr))
            if not qhr_results:
                record["status"] = "NO_QHR_MATCH"
                record["detail"] = f"addr_lens_tried={[c[0] for c in candidates]}"
                yield record
                continue
            distinct = {qhr for _, qhr in qhr_results}
            if len(distinct) > 1:
                record["status"] = "AMBIGUOUS_QHR"
                record["detail"] = "; ".join(
                    f"addr_len={a}: field={q[0]} wt={q[1]} val={q[2] if q[1]==0 else q[2].hex()}"
                    for a, q in qhr_results
                )
                yield record
                continue
            fnum, wt, val = next(iter(distinct))
            record["status"] = "OK"
            record["qhr_field"] = fnum
            record["qhr_wiretype"] = WT_NAME.get(wt, str(wt))
            if wt == 0:
                record["qhr_value"] = str(val)
            else:
                record["qhr_value"] = val.hex()
            record["detail"] = f"addr_lens_agreeing={[a for a, _ in qhr_results]}"
            yield record


def main(argv):
    if not argv:
        print(__doc__)
        return 1
    csv_path = None
    logs = []
    i = 0
    while i < len(argv):
        if argv[i] == "--csv":
            i += 1
            csv_path = argv[i]
        else:
            logs.append(argv[i])
        i += 1

    fieldnames = ["capture", "frame", "timestamp", "direction", "subframe_index",
                  "status", "qhr_field", "qhr_wiretype", "qhr_value", "detail", "raw_hex"]
    rows = []
    for log_path in logs:
        cap_id = log_path.split("/")[-1].split("-btsnoop_hci")[0]
        for rec in decode_capture(log_path):
            row = {"capture": cap_id, **rec}
            rows.append(row)

    writer = csv.DictWriter(sys.stdout, fieldnames=fieldnames)
    writer.writeheader()
    for row in rows:
        writer.writerow(row)

    if csv_path:
        with open(csv_path, "w", newline="") as f:
            w = csv.DictWriter(f, fieldnames=fieldnames)
            w.writeheader()
            for row in rows:
                w.writerow(row)

    ok = sum(1 for r in rows if r["status"] == "OK")
    print(f"# {len(rows)} DLCI 0x02 subframes across {len(logs)} capture(s); "
          f"{ok} decoded to a qhr field", file=sys.stderr)
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
