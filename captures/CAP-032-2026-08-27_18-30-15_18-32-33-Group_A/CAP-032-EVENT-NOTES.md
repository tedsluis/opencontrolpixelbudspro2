# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group A repeat, logging started before any prior association (`CAP-032`)



## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-032`                     |
|      Group(s)    |                    A (repeat, 4th attempt)         |
|       Date       |                     2026-08-27                     |
| Firmware version | release_5.203 (screen-confirmed, see timeline; also wire-confirmed via DLCI 0x08 `Group 0x03 Code 0x02`, `CAP-032-FINDINGS.md` §4) |
|   Test device    | Pixel 7a, Android 17 (⚪ assumed — build number not screen-confirmed this session), system Bluetooth settings + Pixel Buds companion app for setup steps only |
| Video file       | `CAP-032-recording.mp4` — 137.8s, 18:30:15–18:32:33 |
| Log file         | `CAP-032-btsnoop_hci.log` — genuine raw untruncated BTSnoop (not `-btsnooz_hci.log`; see Corrections below and `CAP-032-FINDINGS.md` §0) |

## Event Timeline

Timestamps are the video's own on-screen wall-clock overlay (bottom-right, seconds-resolution); the log
uses the same wall clock (+0200), independently confirmed (`date`/`TZ` on the analysis machine and the
log's own `+0200` frame timestamps both agree, matching the video overlay convention). Screenshots
validated via `ffmpeg -y -ss <offset> -i CAP-032-recording.mp4 -frames:v 1 <out>.png` at 1s precision
around every timestamp below (video start = 18:30:15, so offset = wall-clock time − 18:30:15).

| Time     | Action | Initiator | Test-ID | Wire evidence / Notes |
|----------|---|---|---|---|
| 18:30:15 | start of video `CAP-032-recording.mp4`. Screenshot-confirmed: quick-settings "Bluetooth" panel already open, showing **"Bluetooth is off"** and "Use Bluetooth" toggle OFF. | - | - | Log's own first frame is 18:29:45.722826 — **30s *before* video start**, already mid-way through an HCI `Reset`/stack-bring-up sequence that is LE-only at this point (see next row) |
| 18:30:21 | user's finger visible on the "Use Bluetooth" toggle. | User | `PAIR-001`/`PAIR-004` | Matches the log's classic-radio bring-up burst (`Write Voice Setting`/`Write Scan Enable`/`Write Page Scan Activity`/`Write Extended Inquiry Response`, frames 177–319) starting 18:30:22.381 — ~1s after the tap, consistent with UI-to-HCI lag. **Before this tap**, the log already shows LE-only activity from 18:29:45.722 (Reset, LE scanning, one LE GATT connection to an unrelated random-address device) — read as Android's BLE "opportunistic scan" mode being active while the user-facing Bluetooth toggle was off; see `CAP-032-FINDINGS.md` §0 for the full account, not asserted beyond 🟡 HYPOTHESIS |
| 18:30:30 | user opens "Device details" for "Pixel Buds Pro 2 van Ted" (Bluetooth quick-settings panel). | User | `PAIR-001`/`PAIR-004` | - |
| 18:30:31 | "Device details" screen shown (Left 100%/Case 57%/Right 100%), with per-device trash-can **"Forget"** button (not a phone-wide reset). Screenshot-confirmed. | User | `PAIR-004` | Confirms this session used the narrow, per-device Forget path (matching `CAP-031`, unlike `CAP-013`'s broader reset) |
| 18:30:37 | user taps **"Forget"** — "Forget device? / Your phone will no longer be paired with Pixel Buds Pro 2 van Ted" confirmation dialog appears. Screenshot-confirmed. | User | `PAIR-004` | - |
| 18:30:42 | finger visible touching the **"Forget device"** confirmation button (screenshot at offset 27s). | User | `PAIR-004` | **This is the primary-question clearing action.** Matches `Sent Delete Stored Link Key` at 18:30:43.927732 (frame 768) to within ~1s — see `CAP-032-FINDINGS.md` §0/§1 |
| 18:30:44 | "Connected devices" screen shown (post-Forget). Screenshot-confirmed at 18:30:43 the transition is already complete. | - | - | - |
| 18:30:51 | user selects "See all" under "Saved devices" — section present but empty of the Buds entry (Forget succeeded). | User | `PAIR-004` | - |
| 18:31:08 | user opens the Buds case, case visible open, phone still on "Connected devices". | User | `PAIR-001` | - |
| 18:31:11 | user presses the pair button on the case. | User | `PAIR-001` | LE Extended Advertising Reports from the Buds' public address (`04:00:6e:cf:6e:07`) begin at 18:31:14.998 (frame 991), ~3.6s later |
| 18:31:14 | Pop-up appears: "Pair with Pixel Buds Pro 2". | - | - | - |
| 18:31:24 | user taps **"Connect"** on the pairing pop-up. | User | `PAIR-001`/`PAIR-004` | Matches `Sent Delete Stored Link Key` at 18:31:26.776 (frame 1090) — 0 keys deleted again (already deleted at the Forget tap) — full fresh-SSP sequence follows, frames 1090–1153, see `CAP-032-FINDINGS.md` §2 |
| 18:31:36 | user taps "Start using the device" — Buds now paired. | User | `PAIR-001` | - |
| 18:31:37 | "Set up device" pop-up appears. | - | - | - |
| 18:31:46 | user taps "Set up" — "Allow a connection to your Pixel Buds" permission screen appears. | User | `APP-001`/`APP-002` | - |
| 18:31:59 | user taps "Continue" — "Allow your Pixel to find..." pop-up appears. | User | `APP-001`/`APP-002` | DLCI 0x02 already opened well before this (18:31:30.895, frame 1645, ~2.1s after DLCI 0x00) — within the initial multiplexer burst, not gated on this permission step, consistent with `CAP-031-FINDINGS.md` §5's negative result, not `CAP-013`'s outlier |
| 18:32:04 | user taps "Allow" — "Allow the Pixel Buds app to access the Pixel Buds Pro 2 Bud van Ted" pop-up appears. | User | `APP-001`/`APP-002` | - |
| 18:32:15 | Firmware screen re-shown: `release_5.203`, status bar now shows Bluetooth/headset icons (paired). | - | - | - |
| 18:32:33 | end of video `CAP-032-recording.mp4` (video duration 137.8s from 18:30:15 start, matches folder name and `ffprobe`). | - | - | - |

## Corrections vs. the original draft of this file

- The metadata table's **Date** field read 2026-08-26 — corrected to **2026-08-27**, matching the
  capture folder name (`CAP-032-2026-08-27_18-30-15_18-32-33-Group_A`) and `capinfos`' own packet
  timestamps.
- The metadata table's **Log file** field read `CAP-032-btsnooz_hci.log` (with a "z", the `btsnooz`
  fallback-extraction naming convention) — corrected to the file's actual name,
  `CAP-032-btsnoop_hci.log` (no "z"). This is not cosmetic: `file(1)` and `capinfos` both confirm
  this is a genuine, untruncated raw BTSnoop capture (`Packet size limit: file hdr: (not set)`,
  `frame.cap_len == frame.len` for all 2,455 frames) — structurally different from `CAP-012`'s,
  `CAP-013`'s, and `CAP-031`'s `btsnooz`-fallback logs, which were all severely truncated (~15-byte
  cap) and *were* correctly named with the "z". See `CAP-032-FINDINGS.md` §0 for the full
  extraction-path comparison and its bearing on this project's earlier hypothesis that the naming
  itself tracked the extraction path used.
- The Event Timeline's "Wire evidence / Notes" column was essentially empty in the original draft —
  filled in above from `ffmpeg` screenshot verification and `tshark`/`capinfos` log correlation, per
  this session's Step 0/Step 2/Step 3. In particular, the original draft's 18:30:21 row ("user
  enabled Bluetooth") did not note that the log already contains ~36s of LE-only activity
  *before* that tap (from the log's own first frame at 18:29:45.722) — this is now flagged
  explicitly, since it is directly relevant to how much of the pre-Forget window this capture
  actually covers.
- The original draft's 18:30:43 row said "user taps 'Forget device'... finger visible on button" —
  frame-by-frame `ffmpeg` extraction (1s and sub-second offsets) shows the confirmation dialog is
  still on screen with no finger present at 18:30:39–18:30:41, and the finger is clearly on the
  "Forget device" button at 18:30:42, with the post-Forget "Connected devices" screen already shown
  by 18:30:43 — corrected the row's timestamp to 18:30:42 to match the video, not the original
  draft's placeholder second.
- Added `Test-ID` attributions and wire cross-references throughout, matching the style of
  `CAP-031-EVENT-NOTES.md`; the original draft's `Wire evidence / Notes` column was `-` throughout.

## Traceability check (`AGENTS.md` §13 point 7, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s Group-A Test-IDs)

Group A's Test-IDs per `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §5 and `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s
Group-A-repeat note: `PAIR-001`, `PAIR-004`, incidental `BATT-004`.

- **`PAIR-001`** (pairing/bonding handshake, forget-and-re-pair baseline) — ✅ exercised and
  captured in full: the Forget tap (18:30:42), case-open/pair-button sequence (18:31:08–18:31:11),
  and the successful classic pairing handshake (18:31:26.776–28.019, logged in full,
  `CAP-032-FINDINGS.md` §2).
- **`PAIR-004`** (does Forget clear a pre-existing BLE association/link-key completely, even one
  not established via the Pixel Buds app?) — ✅ **fully exercised for the first time in four
  attempts**: unlike `CAP-013`/`CAP-031`, this capture's log genuinely covers the window before the
  Forget tap (log starts 18:29:45.722, ~58s before the 18:30:42 tap, and ~30s before the video
  itself even starts) — see `CAP-032-FINDINGS.md` §0. Both the primary claim (state *before* the
  Forget tap) and the secondary claim (fresh SSP on re-pairing) are answered — see
  `CAP-032-FINDINGS.md` §0/§1/§2/§7.
- **`BATT-004`** (battery data via RFCOMM after connecting) — ✅ **usefully exercised, for the
  first time in this Group-A-repeat series**: this log is untruncated, so the `Group 0x0e Code
  0x01` battery push's full `[value, flag, index]` triplets decode cleanly (Left 100%/Right
  100%/Case 57%, frame 1417) and match the on-screen reading exactly (`CAP-032-FINDINGS.md` §4) —
  resolving the truncation gap `CAP-013`/`CAP-031` both left open for this Test-ID.

No Test-ID expected for this Group was left entirely unreferenced in the timeline above.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-032-2026-08-27_18-30-15_18-32-33-Group_A/CAP-032-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-032-2026-08-27_18-30-15_18-32-33-Group_A/CAP-032-EVENT-NOTES
