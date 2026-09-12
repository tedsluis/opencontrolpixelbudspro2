# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AF (repeat), unexplained disconnect/reconnect cycling (`CAP-049`)

**Status:** ✅ **Captured and analyzed 2026-09-12.** `CAP-049-recordings.mp4` renamed to
`CAP-049-recording.mp4` (`git mv`, matching every other capture's singular naming). Full video
(495.45s) and full log reviewed — see `CAP-049-FINDINGS.md`. **`.log.last` determination: genuine
same-session rotation** (unlike `CAP-048`'s leftover-content finding) — `.log.last` covers this
session's own first ~62s (its own fresh `Sent Reset` at video start, ending with the
deliberate Bluetooth-off step the draft's own procedure describes), and the main log continues
seamlessly from the matching Bluetooth-on step 2.2s later. A safe, header-preserving combined file
(`CAP-049-btsnoop_hci-combined.log`, `capinfos`-verified: 4,392 packets = 1,866+2,526, 0 truncated)
was produced per this task's Step G method and used as this session's evidence. **Central finding:
the disconnect/reconnect cycling `CAP-039-FINDINGS.md` §6 flagged does NOT reproduce** — after the
one deliberate reconnect the draft's own procedure calls for, the classic connection stays open and
stable for the rest of the ~9-minute session (confirmed via RFCOMM traffic still flowing at
`18:24:00.99`, near the log's own end) — a clean, useful negative result, not a failed session.

**Purpose (repeat of `CAP-039`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AF):** `CAP-039-FINDINGS.md`
§6 found the classic ACL connection disconnected and reconnected 5 times across a single ~6-minute
session with no clearly camera-visible trigger for most of them (4 of 5 locally terminated, reason
`0x16`) — genuinely open what caused the repeated cycling.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-049`                     |
|      Group(s)    |                   AF (repeat)                      |
|       Date       |                     2026-09-12                     |
| Firmware version | ⚪ ASSUMPTION `release_5.203` (carried over) |
|   Test device    | Pixel 7a, Android 17, Pixel Buds Companion App `1.0.955078536`, Google Play services active |
| Video file       | `CAP-049-recording.mp4` (renamed from `-recordings.mp4`) — 495.45s, overlay `18:13:54`–`18:21:49`, **continuous** phone-screen recording throughout |
| Log file         | `CAP-049-btsnoop_hci-combined.log` (main+`.log.last`, genuine same-session rotation — see Status note above) — 4,392 packets, 620.53s, `18:13:55.218`–`18:24:15.745`, 0 truncated frames |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |         `04:00:6e:...:07`         |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AF)

1. Reproduce `CAP-039`'s own general setup (Settable-toggles Set-vs-Get comparison at a fixed dock
   state, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AF) but this time with the phone screen recorded
   continuously throughout (not just at action moments) so any background system event (a
   Bluetooth-settings toggle, a notification, a screen-off/on cycle) is video-visible.
2. If the cycling reproduces, note the phone's own state (screen on/off, any visible notification,
   any app in foreground) at each disconnect/reconnect moment.
3. If it does *not* reproduce, that is itself a useful negative result — record it as such rather
   than treating the session as failed.

## Event Timeline (corrected against full video review + full combined-log analysis, 2026-09-12)

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| 18:13:54 | Video starts. Case visible (buds not in it — worn/off-frame). Bluetooth off. | User | `OBS-006` | Video `t=0`. |
| ~18:13:56 | Bluetooth toggled on | User (OS) | `OBS-006` | — |
| 18:13:55.218 | `.log.last`'s own fresh `Sent Reset` | System | — | Matches the toggle-on step to within ~2s. |
| 18:14:05.361 / 18:14:09.395 | Classic `Connect Complete` (two chandles, `0x0002` then `0x0005` — a brief reconnect settling, not individually investigated further) | System | `OBS-006` | — |
| 18:14:10.186–.247 | DLCI 0x04 `Get`/`Notify` — `Settable=0xe8`(undocked), `Current=0x08`(Noise cancellation) | System (auto) | — | Matches on-screen "Noise cancellation" selected. |
| **18:14:24.543–.987** | **ANC `Set`→`Notify` — user taps 'Adaptive'** | User (App) | `OBS-006` | Video `t=30` (`f_030.png`) shows "Device details," Adaptive selected — matches exactly. `Settable=0xe8` (Set-triggered sample). |
| ~18:14:48 | Bluetooth toggled off (quick panel) | User (OS) | `OBS-006` | — |
| 18:14:56.403 / 18:14:56.666 | Disconnection Complete ×2 (reason `0x16`, locally-terminated) — `.log.last`'s own final events | System | — | — |
| 18:14:58.996 | Main log's own fresh `Sent Reset` — Bluetooth toggled back on | User (OS) | `OBS-006` | 2.2s after `.log.last` ends — the genuine same-session rotation boundary. |
| **18:15:01.967** | Classic `Connect Complete` (forced reconnect) | System | `OBS-006` | Video `t=68` (`f_068.png`), "Active" shown at `18:15:02`. |
| **18:15:03.032–.052** | **DLCI 0x04 `Get`/`Notify` — `Settable=0xe8`(undocked, same as the Set sample), `Current=0x40`(Adaptive, unchanged)** | System (auto) | `OBS-006` | Get-triggered sample — directly comparable to the Set-triggered sample above. |
| ~18:15:15 onward | Screen shows the system Bluetooth quick-settings sheet (not the Pixel Buds app's own "Device details," contrary to the draft's claim for this stretch) | User | — | Video `t=300` (`f_300.png`, `18:18:54`) — "Active. L:100%, R:100% batt…" shown in the device row; consistent, unchanged state, spot-checked across the remainder of the video. |
| 18:17:43.674 | Disconnect on an unrelated chandle (`0x0003`, reason `0x08` Connection Timeout) — **not** the Buds' own classic connection, which stays open throughout (see Findings §2) | — | — | A separate/background BLE link, not investigated further. |
| 18:13:54–18:21:49 | **No further Buds-attributable disconnect/reconnect anywhere in the video's own coverage** | — | `OBS-006` | Video spot-checked at `t=100,150,200,250,300,350,400,450,494` — unchanged "Active" state throughout. |
| 18:21:49 | Video ends (495.45s) | System | `OBS-006` | — |
| (combined log continues to 18:24:15) | Buds' own classic connection (chandle `0x0002`) still carrying RFCOMM traffic at `18:24:00.99`, near the log's own end — **never disconnects** | — | — | Directly answers this Group's own question: the cycling does **not** reproduce here. |

**Correction to the draft:** the cycling behavior this session was designed to catch (`CAP-039`'s 5
unexplained disconnect/reconnect cycles) did **not** occur. The draft's own final rows already
correctly describe this as "no spontaneous disconnects observed" — confirmed here with full
log/video evidence, not just an impression from partial review. The draft's claim that the screen
was on "Device details" for the bulk of the session is corrected: it was the system Bluetooth
settings sheet.

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AF)

- [x] Correlate each `Disconnection Complete`/reconnect pair against the continuous phone-screen
      video to identify (or rule out) a phone-side trigger. → Only the one deliberate,
      draft-documented Bluetooth toggle-off/on cycle occurs; no unexplained cycling to correlate.
- [x] For each disconnect, record the phone's own state at that exact moment. → The one deliberate
      disconnect (`18:14:48`ish) is user-initiated via the quick panel, video-confirmed.
- [x] If the cycling does not reproduce at all, record that explicitly as a useful negative result,
      not a failed session. → **Recorded: it did not reproduce** — see `CAP-049-FINDINGS.md` §2.
- [x] Decode both the Set-triggered and Get-triggered Notify frames' `Settable-toggles` byte and
      compare directly. → Done — both read `0xe8` (undocked), consistent/trigger-independent, a
      further (if sparser) same-session confirmation of `DECISIONS.md` ADR-024, matching `CAP-039`'s
      own 10-sample result in kind.

## Next steps after filling this in

- [x] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `OBS-006` is clearly referenced above.
- [x] Write `CAP-049-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [x] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [x] Update `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `OBS-006` row's Evidence column with a pointer
      once promoted into `PROTOCOL.md`.
- [x] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-049-2026-09-12_18-13-54_18-21-49-Group_AF/CAP-049-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-049-2026-09-12_18-13-54_18-21-49-Group_AF/CAP-049-EVENT-NOTES
