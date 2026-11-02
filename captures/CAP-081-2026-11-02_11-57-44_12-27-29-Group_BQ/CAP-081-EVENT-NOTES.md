# Event Notes: the original Pixel Buds Pro (Gen 1) — the official app on a Pixel 4 XL, then OpenControl on a Pixel 9 Pro — Group BQ (`CAP-081`)

**Status:** 🟢 **captured 2026-11-02 11:57:44 – 12:27:29 and analysed 2026-11-02 by `ai-sessions/0102`** (findings: CAP-081-FINDINGS.md). A third-party
contribution: the Buds, the phones and the films are the contributor's; the committed logs are redacted (`scripts/redact_btsnoop.py`, CAP-081-FINDINGS.md §0).
Storage per `DECISIONS.md` ADR-066: the three screen recordings, the three bugreports and the unredacted logs stay local (hashes in CAP-081-sha256sums.txt).

## Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-081` |
| Group(s) | BQ |
| Date | 2026-11-02, 11:57:44 – 12:27:29 (local, AEDT, +11:00) — part 1 11:57:44 – 12:11:39, part 2 12:21:28 – 12:27:29 |
| Phones | Part 1: Pixel 4 XL, Android 13, Google Play services. Part 2: Pixel 9 Pro, GrapheneOS `CP3A.261005.005` (Android 17), no official app |
| Apps | Part 1: the official Pixel Buds app `1.0.996096625`. Part 2: OpenControl debug build of branch `gen1-support` (versionName 1.4.0; APK SHA-256 `f75f20f9…590757b`, built 12:17:03; installed 12:21:45) |
| Buds | Pixel Buds Pro (Gen 1), `release_5.11_signed` on case, left, right (A 915, B 1458); `GetHardwareInfo` device type 4; Model ID `6edaf7` |
| Video files | Part 1: two screen recordings, 179.2 s (phone time = 11:59:19.1 + s) and 102.7 s (12:08:11.3 + s); part 2: 218.8 s (12:22:25.2 + s). Start = the file name's time (the end) minus the length; matched to the log by A 1765 (touch controls off = recording 1 at 0:54) — local only |
| Log files | **A** CAP-081-btsnoop_hci1.log (part 1, 6,520 records, redacted), **B** CAP-081-btsnoop_hci2.log (part 2, 2,903 records, redacted), **E** CAP-081-opencontrol-debug-20261102-122613.txt (part 2's export, Debug mode on from about 12:23:20) |

## Capture-integrity pre-flight

- 🟢 Part 1 was pulled twice (`adb bugreport` at 12:05 and 12:11): the first snoop log is a byte prefix of the second (`cmp -n 452230`, exit 0), so A covers
  both. The bugreport's `btsnoop_hci.log.last` (2026-11-01) is not part of this capture.
- 🟢 The Buds' ACL handles: A `0x0003` and `0x0005` (Connection Complete for the Buds' address, A 291/296 and 5787), B `0x000b` (B 1083). The redaction kept only
  these handles' ACL data, so every filter is scoped to the Buds.
- 🟢 `scripts/pwrpc_decode.py` decodes A and B with exit 0 (after this pull request's fix for a truncated fixed32/fixed64 field, which crashed it on A).
- 🟢 Redaction did not change the frames analysed: the `WriteSetting` lists of the redacted and the unredacted logs are identical (`diff`, exit 0).

## Event Timeline

Phone time · what happened · Test-ID · evidence (A/B = frame numbers; r1/r2/r3 = recording seconds).

**Part 1 — the official app, Pixel 4 XL.**

| Time | Action | Test-ID | Evidence |
|---|---|---|---|
| 11:58:00 | The official app opens the Message Stream; Model ID `6edaf7` | `GFPS-001` | A 568 |
| 11:59:19 – 11:59:25 | Noise control → Off, Noise cancellation, Transparency (Message Stream `08 12`, signed) | `ANC-001`, `ANC-002`, `ANC-004` | A 1243, 1252, 1262; r1 0:00–0:06 |
| 11:59:42 | MAESTRO connects (r1 0:12 "Connecting…"); hello, hardware info, read sweep incl. 27/28/29 → status 2 | `FW-002` | A 915, 931, 941 … 1051 |
| 11:59:53 – 12:00:09 | Find: ring left, stop, ring right, stop (no "Ring both" offered) | `FIND-001`, `FIND-002`, `FIND-004` (not offered) | A 1416, 1477, 1491, 1620, 1726; r1 0:27–0:48 |
| 12:00:13 – 12:00:15 | Touch controls off, on | `TOUCH-001` | A 1765, 1786 |
| 12:00:21 – 12:00:31 | Press-and-hold left → noise control → Assistant; right → Assistant → noise control | `HOLD-001` … `HOLD-004` | A 1848, 1885, 1924, 1939; r1 0:57–1:09 |
| 12:00:39 – 12:00:42 | Conversation detection on, off | `CONV-001` | A 2012, 2035; r1 1:18–1:24 |
| 12:00:52 – 12:01:52 | EQ sliders (109 live writes), Volume EQ off/on, Save, presets "Default"/"Last saved" | `EQS-001` … `EQS-005`, `AUDIO-002`, `EQP-007`, `EQP-008` | A 2129 …, 2681, 2692, 2825; r1 1:30–2:33 |
| 12:01:59 – 12:02:05 | Balance −20, +20, 0; mono on, off | `AUDIO-003`, `AUDIO-001` | A 3114, 3131, 3159, 3171, 3189; r1 2:36–2:45 |
| 12:08:15 | Multipoint on (Message Stream `07 12`, signed) | `MULTI-001` | A 4543; r2 0:03–0:09 |
| 12:08:26 – 12:08:33 | Audio switch toggled | `SWITCH-001` | A 4769, 4846; r2 0:15–0:21 |
| 12:08:41 – 12:08:43 | In-ear detection off, on | `INEAR-001` | A 5014, 5070; r2 0:27–0:30 |
| 12:08:58 – 12:09:31 | Press-and-hold left/right; hold list with Off ticked and unticked (2 and 3 modes) | `HOLD-001` … `HOLD-005` | A 5181 … 5348; r2 0:42–1:27 |
| 12:09:45 – 12:09:46 | Touch controls off, on | `TOUCH-001` | A 5387, 5401; r2 1:30–1:33 |

**Part 2 — OpenControl (this pull request's build), Pixel 9 Pro.**

| Time | Action | Test-ID | Evidence |
|---|---|---|---|
| 12:22:42 | Connect: hello on channel 21, read sweep without 27/28/29, hardware info (device type 4); no Safe Mode | `FW-002`, `PAIR-003` | B 1458, 1464 … 1591; E 12:22:42–45 |
| 12:23:17 – 12:23:21 | Debug mode on | — | r3 0:52–0:56 |
| 12:23:41 – 12:23:53 | Noise control: Transparency, NC, Transparency, Off, NC, Transparency | `ANC-001`, `ANC-002`, `ANC-004`, `ANC-007` | B 1812 … 1874; E |
| 12:24:02 – 12:24:04 | Touch controls off, on | `TOUCH-001` | B 1931, 1941 |
| 12:24:06 – 12:24:13 | Press-and-hold left → NC → Assistant; right → Assistant → NC | `HOLD-001` … `HOLD-004` | B 1952 … 1970 |
| 12:24:14 – 12:24:22 | Hold list, 6 changes, always ≥ 2 modes | `HOLD-005` | B 1976 … 2006 |
| 12:24:24 – 12:24:35 | In-ear off/on, conversation detection on/off, multipoint off/on | `INEAR-001`, `CONV-001`, `MULTI-001` | B 2012 … 2046 |
| 12:24:42 – 12:25:04 | EQ sliders (10 writes), Volume EQ off/on | `EQS-001` … `EQS-005`, `AUDIO-002` | B 2103 … 2250, 2176, 2195 |
| 12:25:07 – 12:25:16 | Balance left, right, centre; mono on, off | `AUDIO-003`, `AUDIO-001` | B 2256 … 2293 |
| 12:25:31 – 12:25:41 | Find: ring left (twice), ring right, stop; the Buds' status left/right/stopped (after the app's Stop) ACKed; no "Ring both" button | `FIND-001`, `FIND-002`, `FIND-004` (not offered) | B 2396, 2429, 2453, 2489; E; r3 3:04–3:20 |
| 12:25:51 | Noise control → Off (left there) | `ANC-001` | B 2575 |
| 12:27:05 | Multipoint → off (left there; after the recording and the export) | `MULTI-001` | B 2720 |
| after 12:27 | Buds moved to the Pixel 4 XL: the official app shows Multipoint off, noise control Off, the EQ as written by OpenControl | — | ⚪ the contributor's report, not filmed |

## Test-ID coverage (`AGENTS.md` §13 step 7)

Exercised: `ANC-001`, `ANC-002`, `ANC-004`, `ANC-007`, `TOUCH-001`, `HOLD-001` … `HOLD-005`, `CONV-001`, `INEAR-001`, `MULTI-001`, `AUDIO-001` … `AUDIO-003`,
`EQS-001` … `EQS-005`, `EQP-007`, `EQP-008`, `FIND-001`, `FIND-002`, `FW-002`, `PAIR-003`, `GFPS-001`, `SWITCH-001`.
**Expected but not observed:** `FIND-004` (Ring both) — the official app does not offer it on a Gen 1, and this build hides it (a negative, with the positive
control in CAP-081-FINDINGS.md §3). `ANC-003` (Adaptive), `HEAD-001`, `CASE-001`, `CASE-002` — the Gen 1 has no such settings (27/28/29 status 2, A 1014 … 1020);
not offered by either app.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-081-2026-11-02_11-57-44_12-27-29-Group_BQ/CAP-081-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-081-2026-11-02_11-57-44_12-27-29-Group_BQ/CAP-081-EVENT-NOTES
