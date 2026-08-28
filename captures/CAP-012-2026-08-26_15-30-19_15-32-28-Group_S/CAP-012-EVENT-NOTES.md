# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group S repeat, clean GMS-disabled/no-app procedure (`CAP-012`)

**Status:** Reviewed against `CAP-012-recording.mp4` frame-by-frame (1s resolution, using the
video's burned-in wall-clock overlay) and cross-checked against `CAP-012-btsnooz_hci.log` via
`tshark`. Corrects and extends the original draft. See `CAP-012-FINDINGS.md` in this same folder
for the standardized, evidence-graded protocol findings extracted from this correlation — this
file is the *event timeline*, `CAP-012-FINDINGS.md` is *what it means for the protocol*.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group S, repeat):** repeat of Group S following its
**original system-settings-only procedure exactly (no nRF Connect at any point)**, to isolate
whether `CAP-004`'s Cross-Transport Key Derivation bonding result was an artifact of nRF
Connect's early BLE connection, or a genuine effect of GMS being disabled
(`CAP-004-FINDINGS.md` §8 item 4). The core `GFPS-001` result itself is **not** expected to
change — only the §2-equivalent CTKD-vs-classic-SSP bonding-mechanism finding.
**Result: confirmed classic SSP, not CTKD — see `CAP-012-FINDINGS.md` §2/§10.**

**Setup, confirmed for this session:** Pixel Buds app uninstalled and Google Play Services
disabled; paired via system Bluetooth settings only — **no BLE tool (nRF Connect or otherwise) is
ever visible on screen, and the wire log independently confirms zero BLE connection to the Buds at
any point** (`CAP-012-FINDINGS.md` §2) — this session genuinely avoided the `CAP-004` confound,
unlike that earlier attempt.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-012`                     |
|      Group(s)    |         S (repeat) + bonus `PAIR-003` (§6 of `CAP-012-FINDINGS.md`) |
|       Date       |                     2026-08-26                     |
| Firmware version | ⚪ assumed `release_5.203` — not re-confirmed on the wire this session (truncated log, see `CAP-012-FINDINGS.md` §1/§5) |
|   Test device    | Pixel 7a, Android ⚪ 17 (assumed, not shown on screen) — GMS disabled, Pixel Buds app uninstalled — no BLE tool used at any point (confirmed, see above) |
| Video file       | `CAP-012-recording.mp4` — 128.8s, starts 15:30:19, ends 15:32:28 (wall clock, +0200) |
| Log file         | `CAP-012-btsnooz_hci.log` — 542.2s, 15:25:52.11–15:34:54.33 (wall clock, +0200), 1,436 packets. **Severely ACL-truncated (`btsnooz`-fallback extraction, ~15-byte captured length per data packet) — see `CAP-012-FINDINGS.md` §1.** |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6E:CF:6E:07` — independently re-verified this session (`CAP-012-FINDINGS.md` header), not carried over from `CAP-004` |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group S)

1. Confirm the Pixel Buds app is uninstalled and Google Play Services is disabled, per the setup
   note above.
2. Work through §2 (enable HCI snoop, restart Bluetooth/reboot) as usual.
3. **Pair via system Bluetooth settings** [`GFPS-001`] — no app, no BLE tool. Note whether the
   device was already unpaired or whether this capture also includes a fresh bonding handshake.
4. **Isolate the whole pair-and-settle sequence as one action window**: note the exact connect-tap
   time and when the connection visibly settles.

## Event Timeline

Timestamps are the video's own on-screen wall-clock overlay; the log uses the same wall clock
(+0200), directly comparable. All timestamps below are video-confirmed to within 1s.

| Time | Action / Event | Initiator | Test-ID | Wire evidence / Notes |
|----------|---|---|---|---|
| 15:30:19 | Start video recording. | — | — | — |
| 15:30:23 | User turns Bluetooth on (system Bluetooth quick-settings panel) | User (App) | — | — |
| 15:30:32 | User opens the Buds charging case lid | User (Hardware) | `CASE-003` | — |
| 15:30:50 | User presses the pair button on the Buds case | User (Hardware) | — | Case LED visible lit in subsequent frames |
| 15:30:56 | User taps **"Pair new device"** in Bluetooth settings | User (App) | — | — |
| 15:30:57 | "Pair new device" screen: phone's Bluetooth address `E8:D5:2B:7E:CA:81` shown | App (Auto) | — | Matches this session's `Google_7e:ca:81` phone identity |
| 15:30:58 | **"Pixel Buds Pro 2 van Ted"** appears in "Available devices" list | App (Auto) | — | — |
| 15:31:08 | User taps "Pixel Buds Pro 2 van Ted" in the list | User (App) | `PAIR-001` | `Delete Stored Link Key` (frame 457, 15:31:05.955) → `Create Connection` (frame 459, 15:31:05.958) → `Connect Complete` (frame 463, 15:31:06.410) → classic `IO Capability Request/Response` (frames 485–495, 15:31:06.448–.466) — see `CAP-012-FINDINGS.md` §2 |
| 15:31:09 | System **"Pair with Pixel Buds Pro 2 van Ted?"** dialog appears | App (Auto) | — | Coincides with the IO Capability exchange having just completed (frame 495, 15:31:06.466) |
| 15:31:20 | User taps **Pair** | User (App) | — | `Simple Pairing Complete` (frame 517, 15:31:18.872) — ~1s before this on-screen tap timestamp, within this project's usual ±1–2s video/wire tolerance; `Link Key Notification`/`Authentication Complete`/`Encryption Change` follow through 15:31:19.008 (frames 518–526) |
| 15:31:21 | App returns to the phone's home screen (Bluetooth status-bar icon now shown) | App (Auto) | — | RFCOMM channels 0/4/5/6 open and settle 15:31:19.39–20.39; channels 4/5 (DLCI 0x08/0x0a) are torn down again (`DISC`, frames 897–902) once the connection-setup handshake completes, leaving only channels 0/6 (mux/HFP) open |
| 15:31:35 | User opens system Bluetooth settings again: **"Pixel Buds Pro 2 van Ted — Active. 100% battery."** shown | User (App) | — | Generic Android per-device Bluetooth settings page, no ANC/EQ/Sound section — same as `CAP-004-FINDINGS.md` §7's identical observation for this GMS-disabled/no-app condition |
| 15:32:04 | User taps the **"Pixel Buds Pro 2 van Ted"** device row itself (not the gear icon) — this disconnects the device; chip changes to **"Saved"** by 15:32:05 | User (App) | `PAIR-003` | `Sent DISC Channel=6` (frame 962, 15:32:03.203) → `Sent DISC Channel=0` (frame 971, 15:32:03.589) → L2CAP `Disconnection Request`/`Response` (15:32:03.652–.666) — phone-initiated, no hardware event nearby |
| 15:32:08 | User taps the same device row again — reconnects; **"Connecting…"** shown by 15:32:10 | User (App) | `PAIR-003` | `Create Connection` (frame 1001, 15:32:07.802) → `Connect Complete` (frame 1003, 15:32:08.630) → `Link Key Request Reply` (stored key reused, no SSP — frame 1020, 15:32:08.645) → `Encryption Change` (frame 1044, 15:32:08.698); full HFP AT-command handshake re-occurs on DLCI 0x0c starting frame 1185 — see `CAP-012-FINDINGS.md` §6 |
| 15:32:28 | End video recording | — | — | — |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group S)

- [x] Check whether a channel/DLCI carrying the same `[Group][Code][Length][Value]` framing as
      `CAP-002` §3 appears at all. **DLCI 0x04 (the specific channel `CAP-002` §3 used) never
      opens this session — same as `CAP-004`.** See `CAP-012-FINDINGS.md` §3/§4.
- [x] If present, check whether the same fields (e.g. Code `0x09`'s value) match `CAP-002`'s. **Not
      applicable — DLCI 0x04 never opens.** DLCI 0x08's private envelope (a structurally distinct
      channel) does open, with the same Group/Code header shape as `CAP-004-FINDINGS.md` §5a, but
      this log's severe ACL truncation makes value-level comparison impossible — see
      `CAP-012-FINDINGS.md` §1/§5.
- [x] Classic bonding mechanism: does this session use CTKD (as `CAP-004` did) or classic SSP (as
      `CAP-002`/`CAP-003` did)? **Classic SSP, confirmed — see `CAP-012-FINDINGS.md` §2.** This
      resolves the specific confound this repeat was meant to isolate.

## Next steps after filling this in

- [x] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — see `CAP-012-FINDINGS.md` §8 (`GFPS-001`/`PAIR-001` referenced above;
      bonus `CASE-003`/`PAIR-003` also exercised, no gap found).
- [x] Write `CAP-012-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [x] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
      Done — the row is filled in (Android build still ⚪ assumed, not screen-confirmed this
      session, per that row's own text).
- [x] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time (already done — this folder is
      `CAP-012-2026-08-26_15-30-19_15-32-28-Group_S`).

## Corrections vs. the original draft of this file

- The original draft's eleven timestamps were all accurate to within 1s of the video-verified
  values above — no timestamp needed correcting.
- **Added:** the 15:32:03–15:32:10 manual disconnect/reconnect (`PAIR-003`) — not present in the
  original draft at all. This is a real, on-screen, wire-confirmed event window (user taps the
  device row twice), not a capture artifact, and it incidentally supplies this project's first
  Pixel-7a `PAIR-003` data point — see `CAP-012-FINDINGS.md` §6.
- **Added:** the 15:31:21 "app returns to home screen" / RFCOMM-channel-settling row, and precise
  wire-evidence citations (frame numbers) for every row, which the original draft's skeleton left
  as `-`/`TBD`.
- **Added:** the capture-integrity caveat (severe ACL truncation, `CAP-012-FINDINGS.md` §1) to the
  Log Metadata table — the original draft's `TBD` log-file row gave no indication this log would
  turn out to be materially different in quality from `CAP-001`–`CAP-011`'s logs.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-012-2026-08-26_15-30-19_15-32-28-Group_S/CAP-012-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-012-2026-08-26_15-30-19_15-32-28-Group_S/CAP-012-EVENT-NOTES
