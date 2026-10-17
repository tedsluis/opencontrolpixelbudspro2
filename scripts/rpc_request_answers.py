#!/usr/bin/env python3
"""Every pw_rpc REQUEST of chosen methods on the MAESTRO channel, with its RESPONSE and the settings-stream pushes that followed it.

PROJECT_RULES.md rule 4a: the tool behind a count is committed. Written in ai-sessions/0097 (CAP-078-FINDINGS.md §3, §7, §8): the experiment build's
`WriteSetting 4:{13:n}` and `ReadSetting 4:8/9/10`, and the check whether a push arrived between a `ReadSetting 4:13` and its answer.

It reads the table of scripts/pwhdlc_crc_table.py (every pw_hdlc frame of DLCI 0x02/0x03, CRC-checked) and pairs each REQUEST with the next RESPONSE of
the same method on the same channel; "pushes" are the SubscribeToSettingsChanges SERVER_STREAM packets in the window (default 1 s) after the request.

Usage (run from the repository root):
  python3 scripts/pwhdlc_crc_table.py <btsnoop log> 0x000b > /tmp/t.tsv
  python3 scripts/rpc_request_answers.py /tmp/t.tsv WriteSetting [ReadSetting ...]
"""
import sys


def secs(t):
    h, m, s = t.split(":")
    return int(h) * 3600 + int(m) * 60 + float(s)


def main(table, methods, window=1.0):
    rows = [line.rstrip("\n").split("\t") for line in open(table) if not line.startswith("#")]
    # columns: frame, time, direction (0 sent / 1 received), dlci, crc, type, channel, method, payload, raw
    for i, r in enumerate(rows):
        if len(r) < 9 or r[5] != "REQUEST" or not any(r[7].endswith("/" + m) for m in methods):
            continue
        resp = next((x for x in rows[i + 1:] if x[5] in ("RESPONSE", "SERVER_ERROR") and x[7] == r[7] and x[6] == r[6]), None)
        pushes = [x for x in rows[i + 1:] if secs(x[1]) - secs(r[1]) <= window and x[5] == "SERVER_STREAM"
                  and x[7].endswith("/SubscribeToSettingsChanges")]
        pushes = [x for x in pushes if 0 <= secs(x[1]) - secs(r[1])]
        print("\t".join([r[0], r[1], "ch" + r[6], r[7], r[8],
                         (resp[0] + " " + resp[1] + " " + resp[5] + " " + resp[8]) if resp else "no answer",
                         " ".join(f"{x[0]}:ch{x[6]}:{x[8]}" for x in pushes)]))


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2:])
