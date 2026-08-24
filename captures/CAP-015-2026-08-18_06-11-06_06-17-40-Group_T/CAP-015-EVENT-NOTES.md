# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group T Capture (`CAP-015`)

**Status:** Reviewed against `CAP-015-recording.mp4` frame-by-frame (1s resolution, using the
video's burned-in wall-clock overlay, with sub-second re-sampling around ambiguous moments) and
cross-checked against `CAP-015-btsnoop_hci.log` via `tshark`. This is a **fresh, independent
capture** — do not confuse with the earlier, incomplete `CAP-005-2026-08-15_15-02-31_15-03-45-Group_T/`
folder, which is superseded for Group T purposes by this one (that folder's own files are
untouched and still valid for their own record, but were not used as input here). See
`CAP-015-FINDINGS.md` in this same folder for the standardized, evidence-graded protocol findings
extracted from this correlation — this file is the *event timeline*, `CAP-015-FINDINGS.md` is
*what it means for the protocol*.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-015`                     |
|      Group(s)    |               T (EQ command isolation)             |
|       Date       |                     2026-08-18                     |
| Firmware version |                   release_5.203                    |
|   Test device    |    Pixel 7a, Android 17 (Official Pixel Buds Companion App v1.0.955078536) |
| Video file       | `CAP-015-recording.mp4` — 394.3s, 720x1280 H.264, starts 06:11:06, ends 06:17:40 (wall clock, +0200), burned-in on-screen clock |
| Log file         | `CAP-015-btsnoop_hci.log` — 641.5s, 3,728 packets (wall clock, +0200) |
| Devices          | Buds `04:00:6e:cf:6e:07` (matches `CAP-001`–`CAP-004`'s `Google_cf:6e:07`), single classic ACL connection, handle `0x0004` |

**Scope note (per the maintainer's test design):** this session isolates the Equalizer (EQ)
command channel per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group T / `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`
`EQP-002` (preset tap) and `EQS-004` (slider drag). In practice the maintainer ran a substantially
richer procedure than the Group T minimum (one preset + one slider): **five distinct presets**
were tapped in sequence, followed by **all five EQ sliders**, each dragged to its negative extreme,
its positive extreme, and back to near-zero, in three repeating passes — see
`CAP-015-FINDINGS.md` §4/§5 for why this turns out to directly resolve the field-to-band mapping
question the earlier (2026-08-15) `CAP-005` capture left open.

## Event Timeline

Timestamps are the video's own on-screen wall-clock overlay (±1s, 1fps sampling), CEST/+0200;
`CAP-015-btsnoop_hci.log` uses the same wall clock, so times below are directly comparable to log
frame timestamps without offset correction.

| Time | Action / Event | Initiator | Test-ID | Evidence in `CAP-015-btsnoop_hci.log` |
|----------|---|---|---|---|
| 06:11:06 | Start video recording. Screen shows the system **Bluetooth** settings sheet, **"Bluetooth is off"**, `Use Bluetooth` toggled off | — | — | — |
| 06:11:11 | `Use Bluetooth` toggled on; Bluetooth sheet now shows **"Pixel Buds Pro 2 van Ted"** already listed (`L: 100%, C: 100%, R: 100%`) — a previously-bonded device, not a fresh pair | User (App) | — | — |
| 06:11:21 | Tap the gear icon next to the device row → **Device details** screen opens | User (App) | — | — |
| 06:11:36 | Device details fully shown: Left/Case/Right all 100%, `Forget`/`Connect` buttons, **Sound** row visible ("Active Noise Control and EQ") | App (Auto) | — | 1st `Create Connection` sent 06:11:45.417 (frame 1376) |
| 06:11:46 | Tap **Connect** | User (App) | — | — |
| — | (not directly visible on screen) | — | — | 1st `Create Connection` → **Page Timeout** (status `0x04`, frame 1395, 06:11:50.547); 2nd `Create Connection` (frame 1396, 06:11:50.569) → succeeds (status `0x00`, frame 1402, 06:11:51.769) |
| 06:11:54–58 | (not directly visible on screen — app UI mid-transition) | — | — | RFCOMM channels opened in order: DLCI `0x00` (mux, 06:11:54.627), `0x0c` (06:11:54.845), `0x04` (06:11:54.928), `0x0a` (06:11:55.372), `0x08` (06:11:56.274), `0x02` (06:11:58.186) |
| 06:11:56 | **Sound** settings screen open: Conversation detection, Eartip seal check, **Equalizer**, Balance, Mono audio | App (Auto) | — | — |
| 06:12:06 | (still on Sound screen) | — | — | — |
| 06:12:13 | Tap **Equalizer** → Equalizer screen opens; preset dropdown shows **`Last saved`** selected (baseline, all bands centered) | User (App) | — | Frame 2111 (06:12:13.279, DLCI 0x02): quintet `[0.0, 0.0, 0.0, 0.0, 0.0]` — see `CAP-015-FINDINGS.md` §3 |
| 06:12:26 | Tap preset **Heavy bass** [`EQP-002`] | User (App) | `EQP-002` | Frame 2165 (06:12:26.370): `[5.0, 3.0, 0.0, 0.0, 0.0]` |
| 06:12:41 | Tap preset **Light bass** | User (App) | `EQP` (other) | Frame 2227 (06:12:41.340): `[-5.0, -1.5, 0.0, 0.0, 0.0]` |
| 06:12:57 | Tap preset **Balanced** | User (App) | `EQP` (other) | Frame 2303 (06:12:57.324): `[-3.5, 0.5, 1.0, -1.0, 2.5]` |
| 06:13:11 | Tap preset **Vocal boost** | User (App) | `EQP` (other) | Frame 2351 (06:13:11.028): `[-1.0, 0.0, 4.0, 2.0, 0.0]` |
| 06:13:26 | Tap preset **Clarity** | User (App) | `EQP` (other) | Frame 2400 (06:13:26.064): `[-2.0, 0.0, 2.0, 3.0, 5.0]` |
| 06:13:34 | Clarity fully applied on screen; all 5 sliders visible top-to-bottom: **Upper treble, Treble, Mid, Bass, Low bass** — this on-screen order is the key needed to resolve the field↔band mapping (`CAP-015-FINDINGS.md` §5) | App (Auto) | — | — |
| 06:13:39 | Finger briefly touches the **Upper treble** slider row (dropdown still closed, `Clarity` header shown) | User (App) | — | — |
| 06:13:40 | Dropdown reopened, `Clarity` still shown checked in the list — sequencing vs. the row above is ambiguous at 1fps; see `CAP-015-FINDINGS.md` §6 | User (App) | — | — |
| 06:13:41 | (not clearly resolvable on screen at 1fps) — this is when the first wire-visible change appears | — | `EQS` (Upper treble) | Frame 2470 (06:13:40.998): `[4.5, -4.9, 4.5, 3.8, 4.1]` — bands 1–4 already differ from Clarity's clean values here, with no intervening `Sent` frame to explain the jump; see `CAP-015-FINDINGS.md` §6 open question |
| 06:13:52–53 | Upper treble dragged further down | User (App) | `EQS` (Upper treble) | Frames 2527→2573 (06:13:52.55–53.31): band 5 (Upper treble) steps `3.9 → 1.6 → -1.1 → -3.5 → -5.3 → -6.0` |
| 06:13:54 | Value tooltip over the slider reads **"-60"** (i.e. -6.0), `Save` button active (purple), drag settles at -6.0 | App (Auto) | `EQS` (Upper treble) | Frame 2586 (06:13:54.638, outer field 18/"save" shape): `[4.5, -4.9, 4.5, 3.8, -6.0]` |
| 06:14:06–07 | Upper treble dragged back up to its positive maximum | User (App) | `EQS` (Upper treble) | Frames 2616→2653 (06:14:05.92–07.67): band 5 steps `-5.8 → -2.4 → 1.2 → 4.3 → 6.0`, saved at 2653 |
| 06:14:22–23 | Finger moves to **Treble** slider, drags rapidly down to its negative maximum | User (App) | `EQS` (Treble) | Frames 2701→2715 (06:14:22.68–23.14): band 4 (Treble) steps `3.5 → -1.1 → -5.3 → -6.0` |
| 06:14:37–46 | Treble dragged back up | User (App) | `EQS` (Treble) | Frame 2785 (06:14:37.66): band 4 = 5.9; saved at 2819 (06:14:46.55) |
| 06:14:52–54 | Treble dragged back down again and saved | User (App) | `EQS` (Treble) | Frame 2848 (06:14:52.41): band 4 = -6.0; saved at 2863 (06:14:54.26) |
| 06:15:05 | Finger on **Mid** row (video: finger tip directly over the "Mid" label, slider already at/near its positive maximum) | User (App) | `EQS` (Mid) | Frames 2917→2920 (06:15:04.96–05.11): band 3 (Mid) steps `5.0 → 6.0`, saved at 2937 (06:15:06.75) |
| 06:15:17–18 | Mid dragged down to its negative maximum, saved | User (App) | `EQS` (Mid) | Frames 2969→2979 (06:15:16.88–17.34): band 3 steps `5.7 → 2.1 → -4.0 → -6.0`, saved at 3009 (06:15:18.51) |
| 06:15:31–32 | Finger on **Bass** row mid-drag; `Save` button purple/active (unsaved change pending); Bass bar near far left (minimum) | User (App) | `EQS` (Bass) | Frames 3064→3068 (06:15:31.07–31.23): band 2 (Bass) steps `-5.2 → -6.0`, saved at 3083 (06:15:33.01) |
| 06:15:44–46 | Bass dragged up to its positive maximum, saved | User (App) | `EQS` (Bass) | Frame 3130 (06:15:44.76): band 2 = 6.0, saved at 3142 (06:15:46.18) |
| 06:15:58–59 | Finger at the **Low bass** row (bottom of the slider list); Bass bar already full-right (max) from the previous step | User (App) | `EQS` (Low bass) | Frame 3178 (06:15:58.20): band 1 (Low bass) = -6.0, saved at 3187 (06:16:00.10) |
| 06:16:10–13 | Low bass dragged up, saved | User (App) | `EQS` (Low bass) | Frame 3215 (06:16:10.68): band 1 = 5.8, saved at 3228 (06:16:12.77) |
| 06:16:23–25 | **Second full pass begins** — Upper treble changed again, dragged to a small near-zero value and saved | User (App) | `EQS` (Upper treble) | Frame 3271 (06:16:23.31): band 5 = 0.2, saved at 3285 (06:16:24.83) |
| 06:16:33–35 | Treble → near-zero, saved | User (App) | `EQS` (Treble) | Frame 3318 (06:16:33.69): band 4 = 0.2, saved at 3328 (06:16:34.85) |
| 06:16:45–47 | Mid → near-zero, saved | User (App) | `EQS` (Mid) | Frame 3373 (06:16:45.35): band 3 = 0.3, saved at 3382 (06:16:46.56) |
| 06:16:56–58 | Bass → near-zero, saved | User (App) | `EQS` (Bass) | Frame 3403 (06:16:56.48): band 2 = 0.0, saved at 3428 (06:16:57.79) |
| 06:17:07–09 | Low bass → near-zero, saved. All 5 sliders now visually centered (near-zero); `Save` button still active | User (App) | `EQS` (Low bass) | Frames 3451→3458 (06:17:07.60–07.75): band 1 steps `0.2 → 0.1`, saved at 3468 (06:17:09.03) |
| 06:17:39 | Video ending; sliders unchanged since 06:17:09, `Save` button now greyed out again | App (Auto) | — | No further DLCI 0x02 `Sent` traffic after frame 3468 |
| 06:17:40 | End video recording | — | — | — |

## Notes on this session vs. the Group T test-plan minimum

`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group T only calls for one isolated preset tap (`EQP-002`) and
one isolated slider drag (`EQS-004`, Bass). This session went well beyond that: five presets
(Heavy bass, Light bass, Balanced, Vocal boost, Clarity) and all five sliders, each pushed to both
extremes and back to near-zero (three passes total, always in the same **Upper treble → Treble →
Mid → Bass → Low bass** order). This was not anticipated by the original test design but turns out
to be exactly the data needed to confirm the field-to-band mapping that the earlier
(2026-08-15) `CAP-005-FINDINGS.md` §5b/§6 left as an open question (inferred from a single band
changing, in a single direction, in that capture) — see `CAP-015-FINDINGS.md` §5 in this folder for
the full resolution.

## Corrections vs. the original draft of this file

- Replaced the placeholder "Scope note" and empty Event Timeline with the full, video-verified
  timeline above.
- The connection sequence needed **two** `Create Connection` attempts (first: Page Timeout, second:
  success), the same general pattern documented for `CAP-001`/other reconnect sessions — not
  visible from the placeholder draft, added here from the log.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-015-2026-08-18_06-11-06_06-17-40-Group_T/CAP-015-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/#/captures/CAP-015-2026-08-18_06-11-06_06-17-40-Group_T/CAP-015-EVENT-NOTES
