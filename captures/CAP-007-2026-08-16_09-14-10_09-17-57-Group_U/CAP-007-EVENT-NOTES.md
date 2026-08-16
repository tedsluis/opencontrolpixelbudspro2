# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group U Capture (`CAP-007`)

**Status:** Reviewed against `CAP-007-recording.mp4` frame-by-frame (using the video's own
burned-in wall-clock overlay, sampled at up to 1s resolution around every event of interest) and
cross-checked against `CAP-007-btsnoop_hci.log` via `tshark`. This rewrites the previous draft of
this file in full — the previous draft's header metadata (labelled "Group S", `GFPS-001`,
captured 2026-08-11, "no Pixel Buds app", incidental Fitbit Charge 6 traffic) did not match either
this folder's actual video or its actual log content, and its Event Timeline's own timestamps
(09:14–09:17, 2026-08-16) already matched *this* capture, not the metadata above them — i.e. the
header appears to have been copy-pasted from a different capture (`CAP-004`, the real Group
S/`GFPS-001` session) while the timeline itself was already this session's. See the "Corrections"
section at the end. See `CAP-007-FINDINGS.md` in this same folder for the standardized,
evidence-graded protocol findings extracted from this correlation — this file is the *event
timeline*, `CAP-007-FINDINGS.md` is *what it means for the protocol*.

## Log Metadata

|      Field       |                       Value                        |
|------------------|----------------------------------------------------|
|    Capture ID    |                      `CAP-007`                     |
|      Group(s)    | U — DLCI 0x08 Group `0x04` Code `0x12` liveness/event bracket (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4.1 Group U), designed to resolve `CAP-004-FINDINGS.md` §5a Task 5's open question |
|       Date       |                     2026-08-16                     |
| Firmware version | `release_5.203` — confirmed on-wire this capture (ASCII `"release_5.203"`, frames 791/801/1559, e.g. `tshark -r CAP-007-btsnoop_hci.log -Y 'data.data contains "release_5.203"'`), not merely assumed |
|   Test device    | Pixel 7a. Android's own Bluetooth **"Device details"** page for the bonded "Pixel Buds Pro 2 van Ted" (Fast-Pair-enhanced system Settings, reached via the Bluetooth quick-settings panel) — **not** the dedicated Pixel Buds companion app: no app icon/home screen is ever shown on camera, only system `Settings > Connected devices > Bluetooth` screens throughout. Android version not visible on screen in this capture; carried over unverified from the same physical phone used in `CAP-001`–`CAP-006` (Android 17) — ⚪ ASSUMPTION, not independently confirmed here. |
| Video file       | `CAP-007-recording.mp4` — 227.36s, starts 09:14:10, ends 09:17:56/57 (wall clock, +0200, per the video's own burned-in overlay, cross-checked against `ffprobe` duration) |
| Log file         | `CAP-007-btsnoop_hci.log` — 366.24s, 09:14:08.335–09:20:14.579 (wall clock, +0200), 2,476 packets. **The log continues recording for ~137s after the video ends** (unattended, no camera coverage for that tail) — see the last timeline rows below. |

**Devices:** phone `Google_7e:ca:81` (Pixel 7a), peer `Google_cf:6e:07` (Buds/case) — the same
physical device addresses seen throughout `CAP-001`–`CAP-006` (partially redacted per `AGENTS.md`
§7/§9, consistent with those files' own convention).

## Event Timeline

Timestamps are the video's own on-screen wall-clock overlay (±1s where video is the only source);
where a log frame gives sub-second precision within ~1s of a visible action, that frame's own
timestamp is cited directly and is authoritative for ordering. `CAP-007-btsnoop_hci.log` uses the
same wall clock (+0200, `log_start` = 09:14:08.335479), so log frame times are directly comparable
to video overlay times without offset correction (video's `t=0` = wall 09:14:10, i.e.
`video_t = log_relative_t − 1.665s`).

| Time | Action / Event | Initiator | Test-ID | Evidence in `CAP-007-btsnoop_hci.log` |
|---|---|---|---|---|
| 09:14:10 | Start video recording. Screen shows the system **Bluetooth** settings sheet, "Bluetooth is off", `Use Bluetooth` toggled off. Both earbuds visible sitting in the open case above the phone. | — | — | — |
| 09:14:11–14 | Tap **Use Bluetooth** toggle on (exact tap not resolvable on camera at 1fps sampling; toggle is already on, with the Bluetooth quick-settings panel showing "Pixel Buds Pro 2 van Ted — Connecting…", by 09:14:15) | User (Settings) | — | `Create Connection` sent 09:14:14.719 (frame 209) |
| 09:14:16.516 | (not directly visible — still on the quick-settings panel) | — | `PAIR-003`-shaped (reconnect to an already-bonded device, no pairing dialog) | `Connect Complete`, status `0x00` (frame 313) |
| 09:14:16.6–18.1 | (not directly visible — still on the quick-settings panel) | — | `PAIR-003` | Full RFCOMM bring-up on a single ACL link: channel 0 control (SABM frame 402), channel 6/Hands-Free (SABM frame 446), channel 2→DLCI 0x04 (SABM frame 568) — carries the first ANC-state notify `08 13 00 04 01 e8 e8 80` (Group `0x08` Code `0x13`, value `0x80` = Transparency) at 09:14:17.337 (frame 623) — channel 4→DLCI 0x08 (SABM frame 665) — carries the first Group `0x04` Code `0x12` notify, value `0x02`, at 09:14:17.878/18.298 (frames 721, 835) — channel 5→DLCI 0x0a (SABM frame 691), channel 1→DLCI 0x02 (SABM frame 776) |
| 09:14:19 | Notification shade shows "Pixel Buds Pro 2 van Ted — Left 100%, Case 45%, Right 100%" | App/OS (Auto) | `BATT-001`-adjacent | — |
| ~09:14:22–27 | "Device details" page fully rendered: "Active", Left 100% / Case 45% / Right 100%, "Active noise control" row enabled with **Transparency** selected | App/OS (Auto) | `FW-002`/`FW-004`(view)-adjacent | — |
| 09:14:42 | A hand briefly enters the frame near the top edge of the phone, then withdraws; no change to the case/bud layout or the app screen | User (Hardware?) | — | No RFCOMM/L2CAP traffic beyond routine housekeeping identified in this window — inconclusive, not attributed to any Test-ID |
| 09:15:17 | Same as above — hand briefly enters frame, same area, no visible effect | User (Hardware?) | — | Same as above — inconclusive |
| 09:15:38 | **Left earbud is visibly lifted out of the still-open case** (Right earbud and the case itself are undisturbed). In the same ~1s window, the app's "Active noise control" row greys out/disables. | User (Hardware) | `INEAR-004`/`CASE-004` — see `CAP-007-FINDINGS.md` §3.1 for why this maps ambiguously to `INEAR-004`: the bud was never observed inserted in an ear at any point in this capture, only resting in the open case, so this is more precisely a case-removal event | `DISC Channel=1`, buds-initiated, 09:15:38.438 (frame 1351) opens a full RFCOMM teardown/rebuild of channels 1/2/4/5 (DLCI 0x02/0x04/0x08/0x0a), complete by 09:15:44.352 (frame 1572); the ANC-state notify re-fires with the **same, unchanged** value `0x80` (Transparency) at 09:15:38.436 and again at 09:15:44.352 (frames 1350, 1572); a fresh Group `0x04` Code `0x12` notify (value `0x02`) fires on DLCI 0x08 within ~60ms of that channel reopening, at 09:15:39.271/.285 (frames 1436, 1444) |
| 09:15:52–53 | "Active noise control" row **re-enables** in the app UI. The Left earbud remains physically out of the case, unchanged. | App/OS (Auto) | — | No RFCOMM/L2CAP data frame of any kind exists anywhere in the log within many seconds of this moment (only HCI-level `Mode Change`/`Sniff Subrating` baseband events and unrelated LE advertising reports from other nearby devices) — not attributable to any wire-level event, see `CAP-007-FINDINGS.md` §3.3 |
| 09:16:44–48 | User closes the case lid (Right earbud still inside the whole time; Left earbud remains outside, set down next to the case) | User (Hardware) | `OBS-003` (Group U step 2 — "close the case lid while the connection is still active") | Zero RFCOMM data traffic on any DLCI in a ±10s window around this action (checked 09:16:33–09:16:53) — clean negative result, see `CAP-007-FINDINGS.md` §3.4 |
| 09:17:56/57 | End video recording. On-screen app state unchanged since 09:15:53 (Active, Transparency selected). Left bud lies separately on the table; the case appears fully closed, standing on its hinge edge. | — | — | — |
| 09:18:43–09:19:45 | *(beyond the video — log continues unattended, no camera coverage)* Following an uninterrupted **~184s (~3m 4s) silence** on DLCI 0x08 (09:15:39.285 → 09:18:43.428, frames 1444 → 2074) — long enough to match the intended "≥3 minute idle wait", `OBS-003`'s third bracket, even though it fell after the camera stopped — Group `0x04` Code `0x12` notify frames resume on DLCI 0x08, now cycling between value `0x02` and `0x04` (one transitional `0x03` on the very first frame back) at irregular ~1–12s intervals, through 09:19:45.019 (frames 2074–2463). No RFCOMM channel teardown/rebuild accompanies this resumption (contrast with 09:15:38 above). | — | `OBS-003` | Frames 2074, 2173, 2234, 2251, 2255, 2306, 2318, 2335, 2344, 2356, 2359, 2370, 2399, 2463 — see `CAP-007-FINDINGS.md` §3.2 for the full decoded table |
| 09:20:14.579 | Latest packet in the log; capture ends | — | — | — |

## Corrections vs. the original draft of this file

- **The entire header/metadata block was wrong for this folder and has been replaced.** The
  previous draft labelled this "Group S Capture", Group(s) "S (`GFPS-001` — GMS disabled, no
  Pixel Buds app) + bonus classic pairing", dated 2026-08-11, described the test device as "nRF
  Connect… then Android system Bluetooth settings (no Pixel Buds app at any point)", and claimed
  the log "Contains a mix of Buds traffic and unrelated background traffic from a Fitbit Charge
  6". None of this matches `CAP-007-recording.mp4` (which shows only the Android system Bluetooth
  "Device details" page, continuously, with no nRF Connect UI ever on screen) or the directory's
  own name/date (`CAP-007-2026-08-16_09-14-10_09-17-57-Group_U`). The previous draft's own Event
  Timeline rows, by contrast, already carried this capture's real 2026-08-16 09:14–09:17
  timestamps — i.e. only the metadata header was mismatched, not the (sparse, unverified) timeline
  under it. This capture's real scope is Group U (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4.1), not
  Group S — Group S/`GFPS-001` was already captured and analyzed separately, in `CAP-004`.
- The previous draft's timeline had four sparse, unverified rows (Bluetooth toggle, "connects",
  "removes pixel bud from left ear", "closes case"), each with no Test-ID and no log evidence
  column filled in. All four are confirmed directionally correct against the video (something is
  toggled on, buds connect, the Left bud comes out, the case closes), but the previous draft's own
  wording — "User removes pixel bud from **left ear**" at 09:14:36 — does not survive frame-by-frame
  review: **no earbud is ever seen inserted in an ear anywhere in this video.** Both earbuds sit in
  the open case for the entire first 88 seconds; the Left earbud is lifted directly out of the
  case (not out of an ear) at 09:15:38, not 09:14:36. The previous 09:14:36 timestamp does not
  correspond to any visible action in the video at all.
- The previous draft's "09:16:44 User closes case" timestamp is confirmed accurate to within the
  video's own ±1s sampling resolution (this rewrite pins it to 09:16:44–48, covering the full
  close motion).
- Added: the exact toggle/connect sequence (09:14:11–18), the full RFCOMM/DLCI bring-up with frame
  numbers, the two unexplained hand-near-frame moments (09:14:42, 09:15:17), the ANC-row
  grey-out/re-enable pair bracketing the Left-bud removal (09:15:38/09:15:52–53), and the
  post-video log tail (09:18:43–09:19:45) — none of which existed in the previous draft.
