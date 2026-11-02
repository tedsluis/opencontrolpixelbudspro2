#!/usr/bin/env python3
"""Redact a btsnoop_hci.log for publication, keeping every frame number (CONTRIBUTING.md, "the PII exception").

Usage:
  scripts/redact_btsnoop.py <in.log> <out.log> --buds AA:BB:CC:DD:EE:FF [--phone 11:22:33:44:55:66]
                            [--text OLD=NEW ...]

What it does, record by record (the record count and every timestamp stay the same, so a frame number in the
redacted log points at the same frame as in the original):
  - ACL data on a connection handle that is not the Buds' (found from the Connection Complete events for --buds)
    is replaced by an empty vendor event (`04 ff 00`): another device's traffic is never published.
  - LE advertising reports and inquiry results (other devices nearby, their names and addresses) are replaced the
    same way.
  - The Buds' address becomes 00:00:00:00:EE:02, the phone's 00:00:00:00:EE:01 (in both byte orders, and in the
    "aa:bb:…" text form); every other address seen in a Connection Complete becomes 00:00:00:00:EE:1n.
  - Link keys are zeroed: HCI Link Key Request Reply (0x040b) and Link Key Notification (event 0x18).
  - Each --text OLD=NEW pair (same length, e.g. a serial number or a device name) is replaced everywhere, also when
    the text is split across two ACL fragments of the same connection and direction. A replaced string inside a
    pw_hdlc frame leaves that frame's CRC-32 unmatched: decoders report it as a bad frame. That is expected.

Nothing else is changed. Check the output with `strings` / `tshark` before committing it (the script prints a
summary and fails if any address or OLD text is still present).
"""
import argparse
import struct
import sys

HDR = 16
STUB = bytes([0x04, 0xFF, 0x00])  # H4 event, vendor-specific, no parameters


def mac_bytes(text):
    b = bytes(int(x, 16) for x in text.split(":"))
    if len(b) != 6:
        raise SystemExit(f"bad address {text}")
    return b


def read_records(data):
    if data[:8] != b"btsnoop\0":
        raise SystemExit("not a btsnoop file")
    out, off = [], HDR
    while off + 24 <= len(data):
        olen, ilen, flags, drops, ts = struct.unpack_from(">IIIIq", data, off)
        out.append([olen, flags, drops, ts, bytearray(data[off + 24:off + 24 + ilen])])
        off += 24 + ilen
    return out


def write_records(header, records):
    parts = [header]
    for olen, flags, drops, ts, pkt in records:
        parts.append(struct.pack(">IIIIq", len(pkt), len(pkt), flags, drops, ts))
        parts.append(bytes(pkt))
    return b"".join(parts)


def replace_all(buf, old, new):
    n, i = 0, buf.find(old)
    while i >= 0:
        buf[i:i + len(old)] = new
        n += 1
        i = buf.find(old, i + len(new))
    return n


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("inp")
    ap.add_argument("out")
    ap.add_argument("--buds", required=True)
    ap.add_argument("--phone")
    ap.add_argument("--text", action="append", default=[], help="OLD=NEW, same length")
    a = ap.parse_args()

    data = open(a.inp, "rb").read()
    header, records = data[:HDR], read_records(data)
    buds = mac_bytes(a.buds)[::-1]  # HCI carries addresses little-endian
    texts = []
    for pair in a.text:
        old, new = pair.split("=", 1)
        if len(old) != len(new):
            raise SystemExit(f"--text {old!r}: replacement must have the same length")
        texts.append((old.encode(), new.encode()))

    # Pass 1: the Buds' handles, and every other address in a Connection Complete.
    keep_handles, other_addrs = set(), []
    for _, _, _, _, p in records:
        if len(p) >= 3 and p[0] == 0x04:
            code, ln = p[1], p[2]
            if code == 0x03 and ln >= 11 and p[3] == 0:  # Connection Complete, status 0
                handle = struct.unpack_from("<H", p, 4)[0] & 0x0FFF
                addr = bytes(p[6:12])
                if addr == buds:
                    keep_handles.add(handle)
                elif addr not in other_addrs:
                    other_addrs.append(addr)
            if code == 0x3E and ln >= 1 and p[3] in (0x01, 0x0A) and ln >= 12 and p[4] == 0:  # LE (enhanced) conn complete
                addr = bytes(p[9:15])
                if addr != buds and addr not in other_addrs:
                    other_addrs.append(addr)
    if not keep_handles:
        raise SystemExit("no Connection Complete for --buds: wrong address?")

    mapping = [(buds, bytes([0x02, 0xEE, 0, 0, 0, 0]))]
    if a.phone:
        mapping.append((mac_bytes(a.phone)[::-1], bytes([0x01, 0xEE, 0, 0, 0, 0])))
    for i, addr in enumerate(other_addrs):
        mapping.append((addr, bytes([0x10 + i, 0xEE, 0, 0, 0, 0])))
    text_mapping = []
    for old, new in list(mapping):
        mapping.append((old[::-1], new[::-1]))  # the same address in display byte order (e.g. inside a Fast Pair message)
    for old, new in mapping[: len(mapping) // 2]:
        text_mapping.append((":".join(f"{x:02x}" for x in old[::-1]).encode(), ":".join(f"{x:02x}" for x in new[::-1]).encode()))
        text_mapping.append((":".join(f"{x:02X}" for x in old[::-1]).encode(), ":".join(f"{x:02X}" for x in new[::-1]).encode()))

    # Pass 2: stub what is not the Buds', zero keys.
    stats = {"stubbed_acl": 0, "stubbed_scan": 0, "keys_zeroed": 0}
    for r in records:
        p = r[4]
        if not p:
            continue
        if p[0] == 0x02 and len(p) >= 5:
            handle = struct.unpack_from("<H", p, 1)[0] & 0x0FFF
            if handle not in keep_handles:
                r[4], r[1] = bytearray(STUB), (r[1] & 1) | 2
                stats["stubbed_acl"] += 1
            continue
        if p[0] == 0x04 and len(p) >= 3:
            code = p[1]
            scan = code in (0x02, 0x22, 0x2F) or (code == 0x3E and len(p) >= 4 and p[3] in (0x02, 0x0B, 0x0D, 0x0E))
            if scan:
                r[4], r[1] = bytearray(STUB), (r[1] & 1) | 2
                stats["stubbed_scan"] += 1
            elif code == 0x18 and len(p) >= 3 + 22:  # Link Key Notification: addr(6) key(16) type(1)
                p[9:25] = bytes(16)
                stats["keys_zeroed"] += 1
        if p[0] == 0x01 and len(p) >= 4 and struct.unpack_from("<H", p, 1)[0] == 0x040B and len(p) >= 4 + 22:
            p[10:26] = bytes(16)  # Link Key Request Reply: addr(6) key(16)
            stats["keys_zeroed"] += 1

    # Pass 3: addresses and texts, per record.
    counts = {}
    for r in records:
        for old, new in mapping + text_mapping + texts:
            n = replace_all(r[4], old, new)
            if n:
                counts[old] = counts.get(old, 0) + n

    # Pass 4: texts split across consecutive ACL fragments of one handle and direction.
    streams = {}
    for idx, r in enumerate(records):
        p = r[4]
        if p and p[0] == 0x02 and len(p) >= 5:
            handle = struct.unpack_from("<H", p, 1)[0] & 0x0FFF
            streams.setdefault((handle, r[1] & 1), []).append(idx)
    split_fixed = 0
    for key, idxs in streams.items():
        for j in range(len(idxs) - 1):
            first, second = records[idxs[j]][4], records[idxs[j + 1]][4]
            for old, new in texts:
                tail, head = first[5:], second[5:]
                for k in range(1, len(old)):
                    if tail.endswith(old[:k]) and head.startswith(old[k:]):
                        first[len(first) - k:] = new[:k]
                        second[5:5 + len(old) - k] = new[k:]
                        split_fixed += 1

    out = write_records(header, records)
    # Verify: nothing of the originals is left.
    leftovers = [old for old, _ in mapping + text_mapping + texts if old in out]
    open(a.out, "wb").write(out)
    print(f"records {len(records)} (unchanged count); Buds handles {sorted(hex(h) for h in keep_handles)}; "
          f"other addresses {len(other_addrs)}; {stats}; split texts fixed {split_fixed}")
    for old, n in counts.items():
        print(f"  replaced {n}x {old!r}")
    if leftovers:
        print("STILL PRESENT:", leftovers)
        sys.exit(1)


if __name__ == "__main__":
    main()
