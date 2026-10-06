# CAP-053: EQ outer field 16 vs 18 — release only, Save, leaving the screen (Group AO, `EQS-004`, `EQP-008`)

Evidence: `CAP-053-recording.mp4` (184.54 s), `CAP-053-btsnoop_hci.log` (the run), `CAP-053-btsnoop_hci.log.last` (an earlier connection the same
evening, before the film). Timeline with every frame: `CAP-053-EVENT-NOTES.md`. Analysed in `ai-sessions/0072` (2026-10-05). Labels per
`PROJECT_RULES.md` §1:

- 🟢 **FACT** — observed in this capture (frame number, film time or `file:line`).
- 🟡 **HYPOTHESIS** — a reading not yet confirmed, with the test that would settle it.
- ⚪ **ASSUMPTION** — taken as true without a check here.
- 🔴 **OPEN QUESTION** — not answered by this capture.

## 0. Capture metadata

| Field | Value |
|---|---|
| Purpose | Group AO: which action writes EQ outer field 18 — slider release, the Save button, or navigating away (`PROTOCOL.md` §4.2) |
| Date | 2026-10-05, film 17:46:32.5–17:49:37.0 phone time |
| Phone / app | Pixel 7a, Android 17; official Pixel Buds app 1.0.955078536 (maintainer's `dumpsys package` block in the EVENT-NOTES) |
| Firmware | `release_5.203` (announcement frame 870, entries Case/Left/Right) |
| Log path | raw `btsnoop_hci.log` and its rotated `.log.last` (not `btsnooz.py`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3) |
| Buds link | classic ACL handle 0x0002 (frame 396), MAESTRO on DLCI 0x02, announced channel 19 (address `00 3b` / `80 a3`) |
| Clock | phone = film + 17:46:32.5 ± 0.13 s (status-bar minute flips at both ends) |

## 1. The Group's question, answered per condition

Every `WriteSetting` on DLCI 0x02 in the whole log, with the filmed trigger (command: `python3 scripts/pwrpc_decode.py CAP-053-btsnoop_hci.log |
grep WriteSetting`; exit 0; raw frames and CRC below):

| Frame | Phone time | Outer field | Five floats (Low bass, Bass, Mid, Treble, Upper treble) | Filmed trigger (film s) | Gap | Mirror / answer |
|---|---|---|---|---|---|---|
| 1577 | 17:47:35.998 | **16** | −1.0, 0.0, 4.0, 2.0, **5.0** | Upper treble drag 62.6–63.62 | during the drag | 1583 / OK 1584 |
| 1639 | 17:47:50.268 | **16** | −1.0, 0.0, 4.0, **−4.8**, 5.0 | Treble drag 76.6–77.85 | during the drag | 1643 / 1644 |
| 1727 | 17:48:10.180 | **16** | −1.0, 0.0, **0.3**, −4.8, 5.0 | Mid drag 96.6–97.59 | 0.2 s | 1733 / 1734 |
| 1799 | 17:48:29.824 | **16** | −1.0, 0.0, **4.2**, −4.8, 5.0 | second Mid drag 116.36–117.36 | during the drag | 1804 / 1805 |
| 1838 | 17:48:40.823 | **18** | −1.0, 0.0, 4.2, −4.8, 5.0 | **Save tap** 127.86–128.10 (toast "EQ saved" 128.36) | **0.2–0.5 s** | 1844 / 1845 |
| 1914 | 17:48:52.515 | **16** | −1.0, **4.3**, 4.2, −4.8, 5.0 | Bass drag 138.87–139.87 | during the drag | 1916 / 1917 |

| Condition (Group AO) | Window | Field-18 writes | Result |
|---|---|---|---|
| Release only, slider 1 (Upper treble) | 17:47:36.3 – 17:47:49.1 (12.8 s; no hand, Save untouched, screen unchanged on film) | **0** | 🟢 release does not write 18 |
| Release only, slider 2 (Treble) | 17:47:50.6 – 17:48:09.1 (18.5 s) | **0** | 🟢 |
| Release, then wait before Save (slider 3, Mid, dragged twice) | 17:48:30.1 – 17:48:40.3 (10.2 s) | **0** | 🟢 |
| **Save tap** | 17:48:40.3 – 17:48:40.6 | **1** (1838) | 🟢 the Save tap writes 18 with the screen's current values |
| Release, wait (slider 4, Bass) | 17:48:52.6 – 17:49:08.1 (15.5 s) | **0** | 🟢 |
| Leave the screen with **Home**, Recents, back into the EQ (film ends there) | 17:49:08.1 → log end 17:53:48.1 | **0** | 🟢 Home/Recents do not write 18; **Back was not done** — 🔴 |

**Negative with its positive control (`AGENTS.md` §13 step 8):** the same command finds the one field-18 write (1838, `4:{18:…}`) and five
field-16 writes; nothing else. Cross-capture confirmation of what was stored: the next connection's reads in `CAP-054` (the official app's sweep,
three ACLs) return **16 = `[−1.0, 4.3, 4.2, −4.8, 5.0]`** (the last write, including the unsaved Bass change) and **18 = `[−1.0, 0.0, 4.2, −4.8, 5.0]`**
(the Save) — `CAP-054` frames 2378/2386, 3264/3278, 4671/4677 — so no field-18 write happened unseen between this log's end and `CAP-054`.

**Raw frames and CRC (rule 4a).** `tshark -r CAP-053-btsnoop_hci.log -Y "frame.number==1838" -T fields -e data.data` →
`7e003b0310131dea71de7d5e251d9a8c9e2a1e221c9201190d000080bf15000000001d66668640259a9999c02d0000a0408506f3577e`: address `00 3b`, control `03`,
`10 13` channel 19, `1d ea71de7e` Maestro, `25 1d9a8c9e` `WriteSetting`, payload `2a 1e 22 1c` → field 4 → **`92 01`** (field 18, wire type 2) `19 0d
000080bf` (−1.0) `15 00000000` (0.0) `1d 66668640` (4.2) `25 9a9999c0` (−4.8) `2d 0000a040` (5.0); the field-16 writes carry **`82 01`** there (e.g. 1799
`…2a1e221c8201190d000080bf15000000001d66668640259a9999c02d0000a04055d7ac4c7e`). CRC-32 over the unescaped body equals the trailing four bytes for all
six frames (scratch check with `zlib.crc32`, little-endian: 1577 `e4f30c72`, 1639 `a9b39879`, 1727 `2e039299`, 1799 `55d7ac4c`, 1838 `8506f357`,
1914 `c0346535`).

## 2. The official app 1.0.955078536's connect sequence (after its downgrade and data wipe)

🟢 Order on handle 0x0002: phone `Create Connection` 271 (17:47:02.249, 0.3 s after the Bluetooth-on tap) → ACL 396 → RFCOMM `SABM` 0x00 482, HFP 0x0c
516, Message Stream 0x04 690, GSND AUDIO 0x0a 747, GSND CONTROL 0x08 777, MAESTRO 0x02 **859** (last, as `PROTOCOL.md` §5.2). The Buds announce channel
19 at 870 (`GetSoftwareInfo`, `call_id 0xFFFFFFFF`, 3 × `(1779298694, release_5.203)`), 13 ms after the `UA` (861). The app then calls
`Dosimeter.FetchDailySummaries` on 19 and 21, `ReadSetting 4:13`, `SubscribeToSettingsChanges`, `SubscribeRuntimeInfo`, `BundledUpdate.GetStatus`,
`GetHardwareInfo`, `UpdateHelperService.GetRunningVersion`/`GetStagedVersion` (NOT_FOUND), `SetWallclock`, `DynamicServerConfigService.SetConfig 1:1 2:0
3:0`, the `ReadSetting` sweep of fields 1–5, 7, 11–13, 15–19, 21–32, 34–38 (UNKNOWN for 1, 25, 34–38) and `Multipoint.SubscribeToQuietModeStatus`,
`Dosimeter.SubscribeToLiveDb` (frames 873–1082). The same services and the same field set as the earlier 1.0.955078536 runs and as `TODO.md` §4's
inventory; **no** service `0xbf6c9399` (seen with 1.0.990706425 in `CAP-069`). Fresh app data (the uninstall) changed nothing visible on the wire.
Command: `python3 scripts/pwrpc_decode.py CAP-053-btsnoop_hci.log` (119 lines; statuses: 110 OK, 7 UNKNOWN, 2 NOT_FOUND; 0 `CLIENT_ERROR`/`SERVER_ERROR`).

Opening Device details, Sound and the EQ screen sent **nothing** on any channel (no RFCOMM data frame on handle 0x0002 between 17:47:08.512 — frame 1083, the end of the connect
sweep — and 1577 at 17:47:35.998; the screens were opened at 17:47:09.4, 17:47:16.7, 17:47:18.5) — 🟢, as `CAP-036`'s screen-open negative.

## 3. Wire against the code (`v1.0.955078536-10253511`, the run's version)

- 🟢 **Save path:** `hju.java:207–214` (case 19) logs "On click save EQ button" and calls `userEqFragment.aK()` if `homVar.k()` — the wire write
  1838 0.2–0.5 s after the filmed tap agrees.
- 🟢 **No release path:** the 4 releases wrote no field 18 — agrees with the code trace (`REVERSE_ENGINEERING.md`, `qjw` entry: only two call sites).
- 🟡 **"Navigate away" path:** `hod.java:32–39` runs `userEqFragment.aK()` ("Navigate away, save EQ") when a destination other than
  `R.id.userEq_fragment` is reported to it and the unsaved flag is set; `hod` implements `ebe` (`ebe.java`: `void a(ebo)`), is registered in
  `UserEqFragment.java:82/556` (`bvl.n(this).g(this.am)`) and called from `edd.java:703–706` for each back-stack change — the shape of a navigation
  library's destination-changed listener (obfuscated names; not proven). Pressing **Home** leaves the app's back stack unchanged, so this path would
  not run — consistent with the 0 writes, but the path itself is **untested** because Back was not pressed. Test: drag, wait, press Back (no Save).

## 4. The Mid slider moved by itself 10 s after the drag (🟢 film, 🔴 cause)

The finger drags Mid to the centre (thumb at the centre at 97.59 s; write 1727 with Mid 0.3 at 17:48:10.180). At 98.09 s the UI shows the Mid thumb
back near its old position (≈ 0.82 of the track) and keeps it there to 107.59 s; between **107.59 and 107.86 s** (17:48:20.0–20.3) the thumb jumps to
the centre with no hand on screen, and the log has no frame on handle 0x0002 between 17:48:11.224 (1755) and 17:48:29.824 (1799). The maintainer then
dragged Mid back (1799, 4.2). 🔴 Why the UI redrew 10 s late (a UI-only effect: the Buds held 0.3 from 1733 on). Not relevant to the wire; it explains
the repeated drag in step 4.

## 5. Other protocols in this run

- **Message Stream (DLCI 0x04)**, message-level parse (`scratchpad msgs.py`: reassembles per handle/DLCI/direction, splits
  `[Group][Code][Len BE][Value]`; 0 leftover bytes): Play-services claim `03 08 00 02 01 25` (702), session nonce `03 0a` (704), Model ID `03 01 …
  da 2d b1`, BLE address `03 02 … 56 30 04 c9 fd b5`, `03 09` "Revision 6", `03 0b` (25 bytes, FHN, out of scope), battery `03 03 00 03 64 64 ff` ×3 per
  push, `08 11` → `08 13 00 04 01 e8 e8 40` (Adaptive, Settable `e8`), SASS `07 10`/`07 11`/`07 21`/`07 40`/`07 41 "in-use"`/`07 42`/`07 34`, ACKs. 🟢
  No `08 12` (no ANC change). The Buds repeat battery every push (17:47:51, 17:48:11, 17:48:41, 17:48:51, 17:49:21, 17:49:51, 17:50:01) together with
  GSND `0e 02`/`0e 01`/`04 03` and `AT+BIEV=2,100` within 10 ms — not tied to any filmed action.
- **GSND CONTROL (DLCI 0x08):** the phone's opening burst (`05 0c`, `04 02`, `04 04`, `04 11`, `04 13`, `04 15`, `0e 04`, `09 03`, `03 01` with the time
  zone "Europe/Amsterdam", `01 07`, `02 05`, `02 0b`, `01 08`, `09 02`) and the Buds' answers incl. `03 02 … release_5.203` (822); `0e 01` entry 3 = 0x37 =
  **55** (short form) = the Case shown on Device details.
- **GSND AUDIO (DLCI 0x0a):** opened (747/756), **0** data frames.
- **HFP (DLCI 0x0c):** SLC at 538–641 (`AT+BRSF=921` … `AT+BIEV=2,100`), then `AT+BIEV=2,100` with each push (1660, 1752, 1858, 1901, …). Command:
  `tshark -r CAP-053-btsnoop_hci.log -Y 'frame contains "AT+"'` (positive control for the HFP dissector gap, `AGENTS.md` §13 step 8).
- **AVRCP/A2DP:** 6 AVCTP and 14 AVDTP frames (`btavctp`, `btavdtp`), no AVDTP Start (`btavdtp.signal_id==0x07` → 0): no media.
- **Links:** ACL 0x0002 never drops; LE link to `72:0a:e4:99:8d:56` (not the Buds); LE link to the Buds' LE address 17:49:53.924–17:50:01.276 (local
  disconnect `0x16`) — after the film, cause not on film.

## 6. `CAP-053-btsnoop_hci.log.last`

🟢 A separate connection 17:39:52.70–17:46:22.54 (phone `Create Connection` 143 → ACL 147 → MAESTRO `SABM` 512 at 17:40:48.806, 53 s after the ACL), the
official app's connect reads (100 decoded packets: `ReadSetting` ×64, statuses 86 OK, 7 UNKNOWN, 2 NOT_FOUND, 5 `CANCELLED` client errors) and
**no `WriteSetting`**; ends with the Bluetooth-off disconnects 3077–3079. It holds the EQ state **before** the run: 16 = Vocal boost, 18 =
`[−4.9, 4.3, −4.4, 3.5, −4.2]` (last 647/653) — the custom curve `CAP-069` left. Kept (maintainer, 2026-10-05).

## 7. Differences from earlier captures

- 🟢 Field 18 after Save: a second sample after `CAP-069` 6050 (1.0.990706425, 1 s after the tap); together 2 of 2 Save taps → one field-18 write each.
- 🔴 **Contradiction with `CAP-015-FINDINGS.md` §6** (15 field-18 writes, each after a drag, "no video-visible Save tap"; read as "release writes
  18"): here 4 releases wrote none, with the same app version. The gaps of `CAP-015`'s field-18 writes are 1.14–8.89 s after the preceding field-16
  write (`TODO.md` §6, `A68-CAP-16`), long enough for a Save tap. Not reconciled here; test: re-review `CAP-015`'s film at its 15 field-18 frames for a
  Save tap or a screen change (a documentation session, no capture).

## 8. Proposals (awaiting the maintainer — nothing is promoted by this file)

1. **`PROTOCOL.md` §4.2, "Outer field 16 vs. 18"** — a dated Update: the trigger is 🟢 **the Save button** (`CAP-053` 1838, `CAP-069` 6050) and 🟢 **not
   the slider release** (`CAP-053`: 4 releases, 0 writes; windows 12.8/18.5/10.2/15.5 s); leaving with Home/Recents writes nothing (🟢 `CAP-053`); the
   in-app navigate-away path (`hod.java:32–39`) stays 🟡 untested; `CAP-015`'s reading is contradicted (🔴 until its film is re-checked).
2. **`REVERSE_ENGINEERING.md` `qjw` entry** — pointer: Save path and no-release-path agree with the wire (`CAP-053`); `hod` is the listener of
   back-stack changes (🟡, `UserEqFragment.java:82/556`, `edd.java:703–706`).
3. **Next attempt (no skeleton in this session):** a Group AO re-run of step 5 only — drag, wait ≥ 3 s, press **Back** (system back gesture and the
   app bar arrow, once each), no Save; expected per the code: one field-18 write at the Back. `TODO.md` §3.
4. **OpenControl:** no change. It writes only field 16 (ADR-020) and reads 16/18 (ADR-034); a "Save as custom" button that writes field 18 would need
   its own ADR (drafted only if wanted: *"DLCI 0x02: `WriteSetting 4:{18:…}` is sent only on the user's Save tap, with the values of field 16, never on a
   slider release"* — evidence `CAP-053` 1838, fixture its bytes above; hardware re-test: a ReadSetting 18 at the next connection).

## 9. Open questions

- 🔴 Whether Back (in-app navigation) writes field 18 (`hod.java`).
- 🔴 Why `CAP-015` shows field-18 writes after drags.
- 🔴 Why the EQ screen redrew the Mid thumb 10 s after the drag (§4).

## 10. Test-ID traceability

`EQS-004` (Bass) — exercised (1914). `EQP-008` (Save) — exercised (1838). Not named by the Group but exercised: `EQS-001` (1577), `EQS-002` (1639),
`EQS-003` (1727, 1799), `PAIR-003` (reconnect at the start).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-053-2026-10-05_17-46-34_17-49-39-Group_AO/CAP-053-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-053-2026-10-05_17-46-34_17-49-39-Group_AO/CAP-053-FINDINGS
