# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AS (new), Live `GetSoftwareInfo`/`GetHardwareInfo` correlation against the connect-time burst (`CAP-057`)

**Status:** ⛔ **Withdrawn 2026-09-24 (`ai-sessions/0045`, maintainer-approved in chat) — never to be captured.** The question
this capture was designed for is answered from existing bytes: decoding `CAP-036`'s connect-time burst with `scripts/pwrpc_decode.py`
and the 65599 hashes of the APK's `maestro_pw.Maestro` method names shows frame 1423 is the **`GetHardwareInfo`** RESPONSE (method id
`0x28eca5e3`) carrying the three component serials (`PROTOCOL.md` §6, 🟢 FACT). Which serial belongs to which component stays 🟡; a
screen-transcription check could still settle that, but no capture is planned for it. The skeleton below is kept as history.

**Original status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-057-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AS` to the actual session
date/start-time/end-time, e.g. `CAP-057-2026-09-15_08-30-00_08-40-00-Group_AS`.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AS, added 2026-09-13,
`ai-sessions/0017_MAINTENANCE_RESULT_2026_09_13.md` Phase 5):** `PROTOCOL.md` §6's serial-number
candidate (`CAP-036` frame 1423, three length-14 strings inside DLCI 0x02's connect-time burst) was
re-traced in session 0017: the decoded sub-message's own top-level fields (1/2/3, each a direct
`STRING`) structurally match `qjm`/`qjr` (`GetHardwareInfo`'s two oneof alternatives, `qiv`) exactly,
and do **not** match `qie` (`GetSoftwareInfo`'s own oneof alternative, whose 3 fields are typed
`MESSAGE`, requiring an extra nesting level not present in the observed bytes) — a correction to,
not a confirmation of, the existing HYPOTHESIS. Static analysis alone cannot go further; this needs
a live correlation.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-057`                     |
|      Group(s)    |                     AS (new)                       |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    |       TBD (Pixel 7a, official Pixel Buds Companion App; full DLCI 0x02 traffic retained, raw-path extraction) |
| Video file       |    TBD — must clearly show the transcribed serial numbers per component (Left/Right/Case) on the firmware/serial-number screen |
| Log file         |             TBD — `CAP-057-btsnoop_hci.log` (raw path, not `btsnooz.py`) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AS)

1. With full DLCI 0x02 traffic retained (raw-path extraction, not `btsnooz.py`), open Device details →
   More settings → "View firmware version per component (L/R/Case)" **and** "View serial numbers per
   component" (`FW-002`/`FW-003`) — video-confirm and transcribe the exact displayed serial numbers
   per component (Left/Right/Case) on camera.
2. Separately, disconnect and reconnect the Buds (a fresh classic connection) to capture a fresh
   connect-time DLCI 0x02 burst.

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Session start, raw-path HCI snoop logging confirmed | User | — | Conn. state: TBD |
| TBD | Firmware version per component viewed, video-confirmed/transcribed | User (App, view) | `FW-002` | TBD |
| TBD | Serial numbers per component viewed, video-confirmed/transcribed: Left=___, Right=___, Case=___ | User (App, view) | `FW-003` | TBD |
| TBD | Disconnect (system Bluetooth toggle or in-app) | User | — | TBD |
| TBD | Reconnect — fresh connect-time DLCI 0x02 burst captured | User | — | TBD |
| TBD | Session end | — | — | TBD |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AS)

- [ ] Locate the connect-time DLCI 0x02 burst's own 3-string sub-message (same shape as `CAP-036`
      frame 1423 — 3 fields, each a direct length-delimited `STRING`).
- [ ] Compare the 3 decoded strings against the video-transcribed serial numbers from step 1 — do
      they match? If so, does the "EC"/"DR"/"DL"-substring ordering confirm Case/Right/Left (still
      🟡 HYPOTHESIS per `AGENTS.md` §13.6 even if confirmed)?
- [ ] Decode the burst's surrounding RPC envelope (service/method identifiers, per
      `REVERSE_ENGINEERING.md`'s `nqx`/`npy`/`nqo` pw_rpc entries) — does it resolve to a
      `GetHardwareInfo` call/response (per session 0017's structural finding) or `GetSoftwareInfo`,
      settling this session 0017 left open?
- [ ] Record the result plainly either way, with the exact tshark/script commands and raw hex used,
      per `PROJECT_RULES.md` rule 4a.

## Next steps after filling this in

- [ ] Cross-reference this session's own findings against `PROTOCOL.md` §6 and
      `REVERSE_ENGINEERING.md`'s `qjb`/`qie`/`qjm`/`qjr` entries (`AGENTS.md` §13's traceability
      check).
- [ ] Write `CAP-057-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-057-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AS/CAP-057-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-057-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AS/CAP-057-EVENT-NOTES
