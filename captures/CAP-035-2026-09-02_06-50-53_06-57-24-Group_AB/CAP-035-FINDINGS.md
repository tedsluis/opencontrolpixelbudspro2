# Findings: `CAP-035` (Group AB — DLCI 0x08/0x0a/0x06/0x12 GMS-independence check)

**✅ Maintainer sign-off obtained 2026-09-02, per `AGENTS.md` §6/§15.** All three promotions proposed
in §10 (`CAP-004-FINDINGS.md` §4b cross-reference, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `GSND-001`
row, `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s Capture Index + Group AB section) have been applied. No
`PROTOCOL.md`/`DECISIONS.md` promotion was proposed (§10 item 4) — this session strengthens but does
not fully close the OS-stack-vs-GMS question.

Standardized, evidence-based extraction from `CAP-035-btsnoop_hci.log` +
`CAP-035-recording1.mp4`/`CAP-035-recording2.mp4`, staged here for later promotion into
`PROTOCOL.md` per `PROJECT_RULES.md` §2. Modeled on `CAP-001-FINDINGS.md`/`CAP-034-FINDINGS.md`.
Every claim below carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-035` · **Date:** 2026-09-02 · **Firmware:** ⚪ ASSUMPTION `release_5.203`
(not re-confirmed — no official app used). **Phone:** Pixel 9a, GrapheneOS. **Method:** system
Bluetooth settings only — no Pixel Buds Companion App, no nRF Connect, no third-party BLE/GATT tool.
**Log file:** `CAP-035-btsnoop_hci.log` (488.7s, 1,945 packets, 2026-09-02
06:50:58.76–06:59:07.47 local/+0200). **Video:** two files, `CAP-035-recording1.mp4` (67.69s,
06:50:53–~06:52:01) and `CAP-035-recording2.mp4` (317.57s, 06:52:04–~06:57:22) — see §1 for their
resolved relationship. **Devices:** phone (Pixel 9a), peer `04:00:6E:CF:6E:07` ("Pixel Buds Pro 2
van Ted") — the same physical Buds/case used throughout this project.

---

## 0. Capture integrity: unlimited snaplen, zero truncation (🟢 FACT)

```
$ capinfos CAP-035-btsnoop_hci.log
...
Number of packets:   1,945
Packet size limit:   file hdr: (not set)
Interface #0 info: Capture length = 262144
...

$ tshark -r CAP-035-btsnoop_hci.log -T fields -e frame.number -e frame.cap_len -e frame.len \
  | awk '$2!=$3 {c++} END{print "mismatches:", c+0}'
mismatches: 0
```
No snaplen limit; 0/1,945 mismatches. Not truncated.

## 1. Step 0, point 1 — video relationship: sequential, ~7s gap, NOT ~5 minutes (🟢 FACT)

`CAP-035-EVENT-NOTES.md`'s original header claimed "a ~5 min gap between videos," but its own event
table anchored both video 1's "Confirm system pairing dialog" row and video 2's "Start video 2
recording — pairing dialog visible" row at the identical timestamp `06:52:04`. This needed
resolving from the videos themselves, not the header note.

**Method:** extracted video 1's last frame and video 2's first frame directly
(`ffmpeg -ss <t> -i <file> -frames:v 1 ...`), plus a full 5-second-interval contact sheet of all of
video 1 (16 frames covering its entire 67.69s) and 10 sample points across video 2's 317.57s.

**Finding:**
- Video 1's **last** captured frame (t=65s → on-screen clock 06:51:57) shows the system dialog
  **"Koppelen met Pixel Buds Pro 2 van Ted?"** already open, with "Annuleren"/"Koppelen" buttons
  visible, untapped.
- Video 2's **first** frame (t=0s → on-screen clock 06:52:04) shows the **same dialog**, same
  wording, same layout, still untapped — and the case is now visibly open (buds inside visible)
  in both this frame and video 1's last frame, confirming continuity of physical state.
- Video 1's true duration (67.69s from a 06:50:53 start) puts its actual end at **~06:52:00.7**,
  only **~3–4 seconds before** video 2's start timestamp (06:52:04) — not overlapping (video 1's
  clock never reaches 06:52:04) and not a 5-minute gap.
- The dialog remains untapped through video 2's own first several seconds too (confirmed at t=3s,
  06:52:07 — still showing "Koppelen"/"Annuleren", untapped) — the real tap only happens at
  **06:52:36–37** (video-confirmed, finger mid-tap at t=33s/06:52:37), some 32–33 seconds into
  video 2's own recording.

**Conclusion — interpretation, stated plainly:** this is **case (a) refined, not case (c)**: video 1
and video 2 are two **sequential** recordings of the same real-time sequence, separated by only a
short (~3–7s) recording-stop/restart gap — the user apparently stopped recording while the dialog
sat open and unconfirmed, then started a second recording ~7s later from the same still-pending
state. It is *not* a "redo from the pairing dialog" (case (c)) in the sense of discarding and
re-attempting the pairing flow — nothing was cancelled, re-tapped, or reset between the two videos;
the same single pairing attempt simply continues across the recording boundary. The practical
consequence matches part of what case (c) predicted, though: **video 1's later, blank-timestamped
rows (idle window, disconnect, reconnect) never actually happened on video 1** — video 1 ends before
even the pairing tap occurs, so those rows are removed from the corrected timeline in
`CAP-035-EVENT-NOTES.md`, not filled in from video 1's own footage.

**Bonus correction, discovered while resolving this:** video 2's own original (pre-verification)
time estimates for the disconnect/reconnect pair (guessed as `06:54:17`/`06:54:23`) were **~91–97
seconds off** from the wire log's actual timestamps (`06:55:48.26`/`06:55:59.93`, confirmed by frame
extraction at the exact wire-log moments — screen visibly waking at 06:55:48 and a finger tapping
"Verbinden" at 06:56:00, §5). The corrected timeline in `CAP-035-EVENT-NOTES.md` uses the
wire-log-verified times throughout, not the original estimates.

## 2. Step 0, point 2 — GMS precondition: present, but genuinely DISABLED (verified 2026-09-02 addendum) — still not absent (🟢 FACT)

**Original finding (2026-09-02, initial analysis pass):** the preparation checklist's own verbatim
`pm list packages | grep -i "google\|gms\|play"` output includes `package:com.google.android.gms`
— Google Play Services **is present** on this phone, not absent. At that point the only note
addressing its disabled state was a handwritten "Play services was disabled!" with no corresponding
`dumpsys` evidence, and neither video showed a Settings→Apps→Google Play Services screen anywhere
(checked exhaustively — video 1 via a full 5s-interval contact sheet of all 67.69s, video 2 at 10
sample points across its 317.57s). At that point this session's "disabled" claim was unverified.

**Addendum — the maintainer has since added the missing `dumpsys` output to
`CAP-035-EVENT-NOTES.md`'s preparation checklist, closing this gap:**
```
$ for PACKAGE in $(adb shell pm list packages | grep -i -P "(gms|play)" | sed 's/package://'); do
    echo $PACKAGE; adb shell dumpsys package $PACKAGE | grep -i enabled
  done
...
com.google.android.gms
    [... intent-filter noise from `grep -i enabled` matching "*_ENABLED_*" action strings ...]
    User 0: ceDataInode=17180 deDataInode=17183 pccCeDataInode=0 pccDeDataInode=0 installed=true
      hidden=false suspended=false appLockEnabled=false distractionFlags=0 stopped=false
      notLaunched=false enabled=3 instant=false virtual=false quarantined=false
      instant=false virtual=false quarantined=false
```
**`enabled=3` is Android's `PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER`** — the exact
value produced by tapping "Uitschakelen"/"Disable" for an app in Settings, or by
`pm disable-user`. (Enabled-state values: `0`=DEFAULT, `1`=ENABLED, `2`=DISABLED (system-level),
`3`=DISABLED_USER, `4`=DISABLED_UNTIL_USED.) This **independently confirms** the checklist's
handwritten "Play services was disabled!" note — not merely as an assumption now, but as a verified
platform-level state: with this setting, Android's package manager will not start any of
`com.google.android.gms`'s components (services, receivers, providers) in response to system
events, including Bluetooth/Nearby-related broadcasts.

**Revised conclusion, stated at the correct strength — updated from the original pass:** this
session still does **not** achieve the *original*, stronger design goal of this Group (`com.google.
android.gms` genuinely **absent**, package not installed at all) — the package is present on disk.
But its disabled state, which was previously an unverified claim, is now **hard-confirmed** via the
platform's own enabled-state code, not a handwritten assumption. This meaningfully strengthens
(without fully closing) the DLCI 0x08 result in §5: a `COMPONENT_ENABLED_STATE_DISABLED_USER`
package cannot have its components started by the OS, so **for practical runtime-behavior purposes
this is a well-verified negative precondition, even though it is not literally "absent."**
**Framed precisely: this capture is now a *properly verified* second data point for
`CAP-004-FINDINGS.md` §4a's "GMS present but disabled" condition — on a different phone/OS (Pixel
9a/GrapheneOS vs. Pixel 7a) — with materially better evidentiary rigor than `CAP-004`'s own original
write-up (which does not document an independent `dumpsys` re-check of its own `pm disable-user`
precondition either). It is still not the genuinely-GMS-absent confirmation this Group was
originally designed to produce; that remains a distinct, not-yet-closed open question (§11).**

## 3. Connection identification / CLI hygiene (🟢 FACT)

```
$ tshark -r CAP-035-btsnoop_hci.log -Y "bthci_evt.bd_addr==04:00:6e:cf:6e:07" -T fields -e frame.number -e _ws.col.Info | grep -iE "connect|disconn"
552   Rcvd LE Meta (LE Enhanced Connection Complete [v1])   [chandle 0x0040]
688   Rcvd LE Meta (LE Enhanced Connection Complete [v1])   [chandle 0x0041]
792   Rcvd Connect Complete                                 [chandle 0x000b]
1455  Rcvd Connect Complete                                 [chandle 0x000b, reconnect]
```
Two LE connections (`0x0040`, `0x0041` — background pairing-related activity while the dialog sat
unconfirmed and during SMP/CTKD, see §1) and one classic connection reused across a disconnect/
reconnect (`0x000b`). All RFCOMM traffic in this session sits on chandle `0x000b`
(`tshark -Y btrfcomm -T fields -e bthci_acl.chandle | sort -u` → `0x000b` only) — no cross-device
contamination check is needed beyond this; the log contains many unrelated LE advertising-report
frames from nearby devices (normal background scan noise) but forms no other connection.

## 4. Full-session DLCI census (🟢 FACT)

```
$ tshark -r CAP-035-btsnoop_hci.log -Y "btrfcomm" -T fields -e btrfcomm.dlci | sort | uniq -c
     42 0x00
     88 0x08
      8 0x0a
     92 0x0c
```
Only four DLCIs appear in this entire 488.7s session: `0x00` (RFCOMM multiplexer control),
`0x08` ("GSND CONTROL"), `0x0a` ("GSND AUDIO"), and `0x0c` (standard "Handsfree"/HFP, per
`CAP-033-FINDINGS.md` §3 — ordinary HFP SLC setup, not one of this Group's target channels, not
analyzed further here). **`0x06` ("DEBUG APP") and `0x12` ("BTIS") never open at all, anywhere in
this session** — a clean, unambiguous negative, checked across the full file, not just the
video-covered window.

## 5. DLCI 0x08 ("GSND CONTROL"): opens and exchanges its full handshake, both on connect and reconnect (🟢 FACT for structure and content match)

```
$ tshark -r CAP-035-btsnoop_hci.log -Y "bthci_acl.chandle==0x000b and btrfcomm.dlci==8" -T fields -e frame.number -e frame.time -e btrfcomm.len -e _ws.col.Info
```
**First connect:** opens 06:52:37.491 (frame 1085, `SABM Channel=4`) → `UA` 06:52:37.496 (1093) →
a dense burst of `UIH` payload frames 06:52:37.549–37.884 (frames 1143–1249) → two trailing frames
at 06:53:35.920–35.943 (1306–1314) → closes (phone-initiated) `DISC`/`UA` at 06:54:01.767–01.840
(1407/1408 Sent, 1411/1412 Rcvd).

**Reconnect:** opens 06:56:00.357 (frame 1692) → `UA` 06:56:00.365 (1704) → the same dense `UIH`
burst 06:56:00.431–00.754 (frames 1749–1820) → closes 06:57:20.609–20.767 (1909/1910 Sent,
1913/1914 Rcvd).

**Content — byte-level verified against the known strings, both windows:**
```
$ tshark -r CAP-035-btsnoop_hci.log -Y 'frame contains "google-pixel-buds-pro-v1"' -T fields -e frame.number -e frame.time
1182  06:52:37.587014
1793  06:56:00.463847

$ tshark -r CAP-035-btsnoop_hci.log -Y 'frame contains "Europe/Amsterdam"' -T fields -e frame.number -e frame.time
1180  06:52:37.584245
1775  06:56:00.451994

$ tshark -r CAP-035-btsnoop_hci.log -Y 'frame contains "release_5.203"' -T fields -e frame.number -e frame.time
1194, 1210, 1225, 1241, 1246   (first connect)
1801, 1803, 1806, 1814, 1817   (reconnect)
```

Raw hex, frame 1180 (`Europe/Amsterdam`):
```
0000  02 0b 00 23 00 1f 00 0f a0 23 ef 37 08 a0 03 10   ...#.....#.7....
0010  b5 80 d9 83 06 1a 10 45 75 72 6f 70 65 2f 41 6d   .......Europe/Am
0020  73 74 65 72 64 61 6d 5a                           sterdamZ
```
Payload framing: `08 <varint> 10 <varint> 1a 10` + `"Europe/Amsterdam"` (exactly 16 bytes) —
**structurally identical** to `CAP-004-FINDINGS.md` §4b's documented pattern
(`08 9f 03 10 c3 b8 c2 f8 0e 1a 10` + the same 16-byte string): tag `08`/`10` varint fields carry
different (session-specific, "nonce-like") values, exactly as `CAP-004`'s own note already
predicted, while the `1a 10` tag+length and the string itself are byte-identical.

Raw hex, frame 1182 (`google-pixel-buds-pro-v1`):
```
0000  02 0b 20 27 00 23 00 52 00 21 ff 3d 01 0e 02 00   .. '.#.R.!.=....
0010  1a 0a 18 67 6f 6f 67 6c 65 2d 70 69 78 65 6c 2d   ...google-pixel-
0020  62 75 64 73 2d 70 72 6f 2d 76 31 9c               buds-pro-v1.
```
`1a 0a 18` + 24-byte ASCII string `"google-pixel-buds-pro-v1"` (length byte `0x18`=24 matches the
string's exact character count) — byte-identical string content to `CAP-001`/`CAP-002`/`CAP-003`/
`CAP-004`/`CAP-010`.

Raw hex, frame 1210 (`release_5.203`):
```
0000  02 0b 20 24 00 20 00 52 00 21 ff 37 01 02 04 00   .. $. .R.!.7....
0010  17 08 01 18 00 20 00 2a 0d 72 65 6c 65 61 73 65   ..... .*.release
0020  5f 35 2e 32 30 33 38 03 9c                        _5.2038..
```
`2a 0d` (tag 5, length 13) + `"release_5.203"` (13 bytes, matches length byte exactly) —
byte-identical to the pattern documented in `CAP-010-FINDINGS.md` §5.

**Conclusion for this channel:** DLCI 0x08's content is byte-identical in structure and string
content to every prior capture, reproducing **twice** in this single session (fresh connect and
reconnect), with no Pixel Buds app, no nRF Connect, and — per §2's addendum — with GMS present but
**verified** `COMPONENT_ENABLED_STATE_DISABLED_USER` (`enabled=3`, `dumpsys`-confirmed). **This is
now a properly verified second data point for `CAP-004-FINDINGS.md` §4a's finding, on a different
phone/OS (Pixel 9a/GrapheneOS vs. Pixel 7a), with better evidentiary rigor than `CAP-004`'s own
write-up.** It still does not prove GMS-independence in the strongest sense (package genuinely
absent, §2/§11), but a package Android's own package manager will not start components for is a
materially strong negative precondition for "does GMS's Nearby module drive this traffic" — this
result leans meaningfully toward the OS/vendor-Bluetooth-stack explanation, short of a fully
conclusive proof.

## 6. DLCI 0x0a ("GSND AUDIO"): opens both times, carries zero payload both times (🟢 FACT)

```
$ tshark -r CAP-035-btsnoop_hci.log -Y "bthci_acl.chandle==0x000b and btrfcomm.dlci==10" -T fields -e frame.number -e frame.time -e btrfcomm.len -e _ws.col.Info
1126  06:52:37.532807  0  Sent SABM Channel=5
1133  06:52:37.539022  0  Rcvd UA Channel=5
1408  06:54:01.771623  0  Sent DISC Channel=5
1412  06:54:01.840777  0  Rcvd UA Channel=5
1739  06:56:00.412701  0  Sent SABM Channel=5
1743  06:56:00.417967  0  Rcvd UA Channel=5
1910  06:57:20.615489  0  Sent DISC Channel=5
1914  06:57:20.769122  0  Rcvd UA Channel=5
```
DLCI 0x0a opens in lockstep with DLCI 0x08 (within ~5–40ms of it, both times) and closes in lockstep
with it too — but **every one of its 8 frames is a zero-length control frame** (`SABM`/`UA`/`DISC`/
`UA`); no `UIH` (payload) frame ever appears on this DLCI, in either the first connect or the
reconnect window, across the full ~3-minute and ~1.3-minute idle windows that follow each open.

**Conclusion:** this matches the established baseline (`CAP-001`, `CAP-002`, `CAP-005`, `CAP-006`,
`CAP-007`, `CAP-011`, `CAP-016`, `CAP-019`, `CAP-020`, `CAP-022`–`CAP-025` — silent every time except
`CAP-021`'s single anomalous burst, `CAP-021-FINDINGS.md` §4a, which involved DLCI 0x06's absence
too — correction to this project's own prior citation: that section documents a DLCI **0x0a** burst
specifically, not a DLCI 0x06 occurrence; DLCI 0x06 has no established payload precedent at all,
consulted directly in this session's own preparation). **This is the first time DLCI 0x0a's
silence has been specifically checked under a GMS-present-but-disabled, app-absent, tool-absent
condition** — the answer is a clean, precisely-recorded negative: opens reliably (paired with
0x08's open/close lifecycle), never carries content, in either of two independent connect events
in this session.

## 7. DLCI 0x06 ("DEBUG APP") and 0x12 ("BTIS"): never open (🟢 FACT)

Per §4's full-session census, neither DLCI appears anywhere in this log — not as a control frame,
not as payload. This is the first capture to specifically check for these two channels' presence
under this Group's conditions; the result is a clean, unambiguous negative, not a truncation or
CLI-hygiene artifact (§0, §3 rule out both).

## 8. ADR-008 compliance: no Accessory Non-Owner Service interaction (🟢 FACT)

```
$ tshark -r CAP-035-btsnoop_hci.log -Y "btatt" -T fields -e frame.number -e btatt.handle -e _ws.col.Info
```
38 ATT frames total, all incidental to the OS's own LE/CTKD bonding housekeeping (Model Number
String, Device Name, Server/Client Supported Features, Database Hash — all standard GAP/GATT
service handles `0x0001`–`0x0009` — plus `0x0f32`/`0x0f33`, the Battery Level characteristic and its
CCCD per `CAP-034-FINDINGS.md` §4.5). **No handle in the `0x0c00`–`0x0c18` range (the Fast Pair
Service or the Accessory Non-Owner Service, per `CAP-034-FINDINGS.md` §4) is ever touched** — no
discovery walk, no read, no write. This session used system Bluetooth settings only, with no Fast
Pair half-sheet and no app, so this is confirmed rather than merely expected, per this task's own
guardrail.

## 9. Test-ID traceability (`AGENTS.md` §13 requirement)

- **`GSND-001`** (this session's primary goal): exercised in full. Result: DLCI 0x08 content
  reproduces (§5, bounded by §2's precondition gap); DLCI 0x0a opens but stays silent both times
  (§6); DLCI 0x06/0x12 never open (§7).
- **`PAIR-001`** (fresh pairing, incidental): exercised — `Delete Stored Link Key` (frame 176, the
  Forget action) through classic bonding complete (frame 831), including LE SMP/CTKD (§1, §3).
- **`PAIR-003`** (disconnect/reconnect to an already-bonded device, incidental): exercised cleanly
  — frame 1452 (disconnect) → frame 1455 (reconnect, reused link key, no fresh SMP).
- **`BATT-003`** (idle spontaneous-traffic window, incidental): two idle windows observed
  (~3m7s and ~1m19s) — no spontaneous traffic beyond the DLCI 0x08/0x0a channels' own
  self-initiated close, no battery-specific push observed (expected — no HFP `AT+BIEV`/Fast-Pair
  Message-Stream mechanism was engaged this session, per `PROTOCOL.md` §4.3's documented triggers).

## 10. Conclusions & downstream updates (✅ maintainer sign-off obtained 2026-09-02, applied)

**Confirmed, at the strength the evidence actually supports — not overstated:**
- DLCI 0x08's content reproduces byte-for-byte (structure and string content) with no Pixel Buds
  app and no nRF Connect installed, across two independent connection events in one session
  (🟢 FACT, §5), with Google Play Services **present but verifiably disabled**
  (`enabled=3` = `COMPONENT_ENABLED_STATE_DISABLED_USER`, `dumpsys`-confirmed, §2). **This is now a
  properly verified second data point for `CAP-004-FINDINGS.md` §4a's "GMS present but disabled"
  finding — on a different phone/OS — with better evidentiary rigor than `CAP-004`'s own original
  write-up.** It leans meaningfully toward an OS/vendor-stack explanation over a GMS-driven one,
  short of the fully conclusive "genuinely absent" proof this Group was originally designed to
  produce.
- DLCI 0x0a opens in lockstep with DLCI 0x08 both times but never carries payload (🟢 FACT, §6) —
  consistent with the established multi-capture baseline, now confirmed once more under this
  Group's specific (verified-disabled-GMS) conditions.
- DLCI 0x06 and 0x12 never open at all (🟢 FACT, §7) — first check of their existence under any
  condition; clean negative.
- ADR-008 compliant (🟢 FACT, §8).

**Maintainer sign-off obtained 2026-09-02 — applied:**
1. **`CAP-004-FINDINGS.md` §4b** — a forward cross-reference added, noting `CAP-035` reproduces
   the DLCI 0x08 finding a second time, under a different phone/OS (Pixel 9a/GrapheneOS vs. Pixel
   7a), with GMS's disabled state independently `dumpsys`-verified (`enabled=3`) rather than merely
   assumed — strengthening the existing finding's generality and rigor without changing its
   evidentiary category (still "disabled," not "absent").
2. **`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`**'s `GSND-001` row — updated from 🔴 to 🟡, with the
   precise outcome: DLCI 0x08 reproduces under a *verified*-disabled-GMS condition (a meaningfully
   strong, though not fully conclusive, second data point); DLCI 0x0a/0x06/0x12 show no payload/
   never open — and notes that the genuinely-GMS-*absent* condition this row was designed to test
   ideally is still untested, for anyone wanting full closure.
3. **`CAPTURE_BLUETOOTH_HCI_SNOOP.md`**'s Capture Index and Group AB section — updated from
   *planned* to analyzed, with this session's precise outcome and precondition detail, plus a
   status banner on Group AB itself.
4. **No `PROTOCOL.md`/`DECISIONS.md` promotion was proposed or applied** — this session
   meaningfully strengthens confidence toward the OS-stack explanation for DLCI 0x08 but does not,
   on its own, fully resolve the OS-stack-vs-GMS question `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AB
   was designed to answer to the "genuinely absent" standard.

## 11. Open questions after this session

- 🔴 **Primary, narrowed but not fully closed by this session:** whether DLCI 0x08/0x0a/0x06/0x12
  depend on Google Play Services — this session provides a `dumpsys`-verified "GMS present but
  disabled" data point (§2), which is meaningfully strong evidence but not the fully conclusive
  "genuinely absent" test Group AB was originally designed around. **Optional next step, for full
  closure:** repeat with `com.google.android.gms` genuinely uninstalled (not merely disabled) —
  e.g. a phone/profile where it was never installed — if the maintainer judges this session's
  verified-disabled result insufficient on its own.
- 🔴 DLCI 0x06's ("DEBUG APP") and 0x12's ("BTIS") purpose remains completely unaddressed — they
  have never been observed to open under any condition in this project's captures to date.
- 🔴 "GSND"'s expansion and DLCI 0x08/0x0a's precise Group/Code semantics remain unresolved — this
  session adds a reproduction data point, not new structural decode.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-035-2026-09-02_06-50-53_06-57-24-Group_AB/CAP-035-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-035-2026-09-02_06-50-53_06-57-24-Group_AB/CAP-035-FINDINGS
