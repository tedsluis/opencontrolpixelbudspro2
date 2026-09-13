# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AA (repeat, 2nd attempt), SDP UUID branch isolation (`CAP-044`)

**Status:** ✅ **Captured and analyzed 2026-09-13** — see `CAP-044-FINDINGS.md` for the full
write-up. This file's Event Timeline and Log Metadata below have been independently re-verified
against a full, non-sampled review of the video (5s-interval overview sheets plus four denser
1–2fps passes around the pairing sequence, the account-link dialog transition, and the second app
relaunch) and the complete wire log, per `ai-sessions/0016_CAPTURE_PROMPT_2026_09_13.md`. Several
of this file's original hand-typed timestamps were corrected by up to ~70 seconds — see
`CAP-044-FINDINGS.md` §1 for the full comparison table.

**Purpose (repeat of `CAP-033`/`SDP-001`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AA):**
`CAP-033-FINDINGS.md` §8 found the "default internal rfcomm socket" UUID still absent from the SDP
browse (an interesting negative), but the session's own procedure deviated — "Forget" preceded
"Force-stop" by ~10s (the reverse of the intended order) and step 3 (an app-open baseline
comparison) was never executed at all — capping the result at 🟡 HYPOTHESIS rather than a clean
FACT either way. This repeat corrects both deviations in one session.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-044`                     |
|      Group(s)    |               AA (repeat, 2nd attempt)             |
|       Date       |                     2026-09-13                     |
| Firmware version | `release_5.203` (🟢 confirmed on-wire, frame 4416, DLCI 0x08 Group `0x03` Code `0x02`) |
|   Test device    | Pixel 7a, Android 17 (🟢 FACT, MP4 container tag); app version `1.0.955078536`, Google Play services active (⚪ ASSUMPTION, maintainer-supplied context) |
| Video file       |   `CAP-044-recording.mp4` — 316.42s, 13:05:51–13:11:07 local (burned-in overlay + `ffprobe`-cross-checked) |
| Log file         |        `CAP-044-btsnoop_hci.log` — 6,363 packets, 850.35s, 12:59:08.43–13:13:18.78 local |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` (same unit as `CAP-001`/`CAP-002`/`CAP-032`/`CAP-033`) |

**Isolation check (required — this is the whole point of the repeat) — 🟡 PARTIALLY ACHIEVED, for a
different reason than `CAP-033`.** Force-stop *does* precede Forget in wall-clock order this time
(the very first, off-camera Force-stop, then the in-app Forget at 13:05:59) — that specific ordering
issue from `CAP-033` §1.1 does not reproduce. **However, no Force-stop action of any kind is
observed anywhere in the video between the in-app Forget (13:05:59) and the actual "Pair" tap that
triggers bonding + the SDP browse (13:08:07, not 13:07:00 as originally logged below — see
`CAP-044-FINDINGS.md` §1)** — a 2m8s window during which the app may have remained resident in the
background; this cannot be confirmed either way from the video or wire log. **Step 3 (the app-open
SDP browse) was confirmed executed on-camera this time** (`CAP-033`'s own gap) — but produces no
second SDP transaction to compare against, because the underlying classic connection never drops
between the initial pairing and the later app reopen (see `CAP-044-FINDINGS.md` §4 for the
wire-confirmed mechanism).

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AA, `SDP-001`)

1. **Force-stop** the official Pixel Buds Companion App **first**, confirmed via Settings → Apps
   (screenshot/video timestamp), *before* touching the Buds' pairing at all — the reverse of
   `CAP-033`'s own accidental ordering.
2. With the app still force-stopped, perform the per-device "Forget" (not a broader Bluetooth
   reset) on the Buds, then re-pair via system Bluetooth settings only (no companion app
   involvement) and capture the SDP Service Search Attribute Response during that re-pair.
3. **Execute step 3 this time** — after the SDP browse above is captured, *open* the official
   companion app (still logging) and capture a second SDP browse triggered by the app itself, to
   compare against step 2's system-only browse.
4. Extract via the raw log path (not `btsnooz.py`), per the same technical-debt note as `CAP-043`.

## Event Timeline

**Re-verified 2026-09-13 against a full, non-sampled video pass and the complete wire log** — see
`CAP-044-FINDINGS.md` §1 for the full frame-by-frame comparison against this table's original
hand-typed version. Several timestamps below were corrected by tens of seconds to over a minute;
corrections are noted inline.

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| 13:05:51 | Video starts. Case is open with buds inside. Screen shows Pixel Buds "App info" (Force stop button present). The Force-stop tap itself is off-camera/unverifiable. | User (OS) | `SDP-001` | Video t=0 |
| 13:05:55 | User navigates back into the Pixel Buds app via the recent-apps switcher (a cached thumbnail, not proof the app was still running) and taps it, relaunching the app to "Device details." | User (App) | `SDP-001` | **Deviation:** App is woken up again. |
| 13:05:59 | "Forget device" tapped within the Pixel Buds app. | User (App) | `SDP-001` | Device is forgotten. |
| 13:06:00–13:06:33 | Screen stays on the **system** "Connected devices" Settings page continuously — **no Force-stop action is visible anywhere in this ~33s window** (checked at 1fps). | — | `SDP-001` | 🔴 New finding: the app's process state during this window (and through 13:08:07 below) is unconfirmed — see `CAP-044-FINDINGS.md` §2. |
| 13:06:34 | User presses the pairing button on the back of the open case. | User (Hardware) | `SDP-001` | Entering pairing mode. |
| 13:06:38–13:06:44 | A Fast-Pair-style "Pixel Buds Pro 2 / Tap to pair" entry appears directly inside the system "Connected devices" list — **not tapped**. | System (OS) | `SDP-001` | Corrects the original note's "Fast Pair prompt appears, user taps Connect" — the user instead used the manual path below. |
| 13:06:45–13:06:48 | User taps "Pair new device" → "Available devices" → selects **"Pixel Buds Pro 2 van Ted"** (the classic-discovery entry). | User (OS) | `SDP-001` | — |
| 13:06:51–13:08:06 | "Pair with Pixel Buds Pro 2 van Ted?" confirmation dialog appears and sits **untapped for over a minute**. | — | `SDP-001` | Not in the original timeline at all. |
| **13:08:07** | **"Pair" tapped.** | User (OS) | `SDP-001` | **Corrects the original "13:07:00 standard pairing prompt accepted" by +67s.** This is the actual trigger for bonding + the SDP browse. |
| 13:08:08.64 | Link Key Notification (bonding completes) — the only bonding event in this log. | System | `SDP-001` | Log frame 4044 |
| 13:08:08.90–13:08:18.04 | **SDP browse** — standard-profile queries plus 5 targeted per-UUID queries (GSND CONTROL/DLCI 0x08, GSND AUDIO/DLCI 0x0a, GFPS RFCOMM/DLCI 0x04 ×2, MAESTRO APP/DLCI 0x02). "Default" UUID absent (5th consecutive negative). | System/App (unconfirmed which, see `CAP-044-FINDINGS.md` §5) | `SDP-001` | Log frames 4071–4890 — see `CAP-044-FINDINGS.md` §3 |
| 13:08:12 | System "Connected devices" shows "Pixel Buds Pro 2 van Ted — Active, 100% battery." | System | `SDP-001` | — |
| 13:08:17–13:10:11 | A **system-level Fast Pair account-link prompt** ("Save device to ted.sluis@gmail.com...") sits on top of Settings, untapped for ~2 minutes — **this is not the companion app's own UI**. | System (OS/GMS) | `SDP-001` | **Corrects the original "13:08:10 ... Pixel Buds app Device details/Setup UI automatically surfaces" — that was this system dialog, not the app.** |
| ~13:10:11 | "Save" tapped → dialog changes to "Set up device" (Skip/Set up). | User (OS) | `SDP-001` | Not in the original timeline. |
| ~13:10:15 | "Skip" tapped. | User (OS) | `SDP-001` | Not in the original timeline. |
| ~13:10:16–13:10:21 | Settings-app navigation consistent with the second Force-stop (exact tap not isolated at 1–2fps sampling). | User (OS) | `SDP-001` | Approximately matches the original "13:10:18" note. |
| 13:10:21–13:10:24 | Home screen → app drawer/search → Pixel Buds app launched fresh, showing the nearby-devices permission onboarding flow (consistent with a genuine fresh process start). | User (App) | `SDP-001` | Approximately matches the original "13:10:23" note. |
| 13:10:24–13:10:29 | Permission flow: "Allow a connection..." → Continue → "Allow the app Pixel Buds to access [nearby devices]?" → **Allow**. | User (App) | `SDP-001` | Not in the original timeline. |
| 13:10:31–13:11:07 | **Step 3 (app-open baseline)**: full companion-app "Device details" screen open, `Active`, `L:100% C:83% R:100%`. No second SDP transaction occurs — see `CAP-044-FINDINGS.md` §4 for why. | User/App | `SDP-001` | — |
| 13:11:07 | Video ends. | — | `SDP-001` | Video's final frame, cross-checked against `ffprobe` |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AA)

- [x] Pre-filter by address, then filter to `btsdp` per §13's CLI-hygiene rule. **Only one SDP
      transaction exists in the entire log (frames 4071–4890, 13:08:08.90–13:08:18.04)** — see
      `CAP-044-FINDINGS.md` §3.
- [x] Does the "default internal rfcomm socket" UUID (`3a046f6d-...`, either byte order) appear in
      *either* SDP browse this time? **No — zero occurrences, either byte order, raw-byte-scanned
      across the entire log.** 5th consecutive negative across independent sessions.
- [x] Does the "MAESTRO APP"/"GSND CONTROL"/"GSND AUDIO" naming (`CAP-033-FINDINGS.md` §3)
      reproduce identically? **Yes — all 4 channel-name↔UUID↔RFCOMM-channel mappings from `CAP-033`
      reproduce byte-for-byte** (MAESTRO APP=ch1/DLCI 0x02, GSND CONTROL=ch4/DLCI 0x08, GSND
      AUDIO=ch5/DLCI 0x0a, GFPS RFCOMM=ch2/DLCI 0x04) — see `CAP-044-FINDINGS.md` §3. Only one
      SDP transaction occurred this session (step 3's app-open did not trigger a second one, see
      `CAP-044-FINDINGS.md` §4), so a system-only-vs.-app-triggered comparison could not be made.
- [x] Record the result plainly either way. **Second consecutive attempt that does not cleanly close
      `SDP-001` either way, but for a different, more specific isolation gap than `CAP-033`'s** (no
      confirmed Force-stop covering the 2m8s window before the actual "Pair" tap) — see
      `CAP-044-FINDINGS.md` §5 for the full conclusion and a proposed procedure refinement for a
      possible 3rd attempt.

## Next steps after filling this in

- [x] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — `SDP-001` referenced throughout the timeline above; `SDP-002` not
      attempted (no firmware update pending).
- [x] Write `CAP-044-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [x] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [x] Update `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `SDP-001` row's Evidence column with a pointer
      to `CAP-044-FINDINGS.md` (result stays 🟡 HYPOTHESIS — no `PROTOCOL.md` promotion asserted,
      per `AGENTS.md` §6).
- [x] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to
      `CAP-044-2026-09-13_13-05-51_13-11-07-Group_AA` (video start/end times).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-044-2026-09-13_13-05-51_13-11-07-Group_AA/CAP-044-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-044-2026-09-13_13-05-51_13-11-07-Group_AA/CAP-044-EVENT-NOTES
