# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AJ (new), `HOLD-005` Left/Right ANC-rotation-checklist split (`CAP-045`)

**Status:** ⚠️ **Captured and analyzed 2026-09-12, but the session did NOT run Group AJ's own
procedure.** Full video (100.75s) and full log (2,620 packets, untruncated) reviewed — see
`CAP-045-FINDINGS.md`. **Major finding:** the video shows the maintainer **physically long-pressing
each earbud** to cycle its ANC mode (matching the draft's own "user long-pressed left/right bud"
notes) — never opening Device details → Controls and gestures → the ANC-mode rotation checklist that
Group AJ's procedure calls for. The wire confirms this: every ANC-state change in this log is a
`Notify`-without-`Set` frame (the already-documented `TOUCH-007` press-and-hold mechanism,
`CAP-027-FINDINGS.md` §4), and zero DLCI 0x02 writes matching the rotation-checklist's `qhr` field-12
shape occur anywhere in the session. **`HOLD-005`'s own question (Left/Right split within the
rotation-*checklist setting*) is not answered by this capture — a re-run with the actual checklist
screen open is still needed.**

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AJ, added 2026-09-09):** `PROTOCOL.md` §4.5.3's
ANC-mode rotation checklist (`qhr` field 12, confirmed field-number identity as `qht`) has 16
wire-observed boolean flags (`HOLD-005`, `CAP-021`) but no Left/Right-distinguishing field for this
specific write — unlike `HOLD-001`–`HOLD-004`, it's unknown which frames belong to which earbud's
own rotation list.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-045`                     |
|      Group(s)    |                     AJ (new)                       |
|       Date       |                     2026-09-12                     |
| Firmware version | ⚪ ASSUMPTION `release_5.203` (carried over) |
|   Test device    | Pixel 7a, Android 17, Pixel Buds Companion App `1.0.955078536`, Google Play services active |
| Video file       | `CAP-045-recording.mp4` — 100.75s, overlay `08:22:51`–`08:24:32`. Shows physical long-press gestures on each earbud, **not** the rotation-checklist settings screen. |
| Log file         | `CAP-045-btsnoop_hci.log` — 2,620 packets, 237.53s, `08:22:52.686`–`08:26:50.215`, untruncated |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |         `04:00:6e:...:07`         |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AJ)

1. Open Device details → Controls and gestures → the ANC-mode rotation checklist for the **Left**
   earbud specifically (the UI is confirmed to expose this per-earbud, per `PROTOCOL.md` §4.5.3's
   own UI description).
2. Toggle each of the 4 checklist items (Noise cancellation / Off / Adaptive / Transparency) for
   the Left earbud **one at a time**, with a clear pause (≥10s) and a distinct video-visible action
   between each toggle, so each write can be isolated to one specific checklist item.
3. Repeat step 2 for the **Right** earbud's own rotation checklist, again one item at a time.

## Event Timeline (corrected against full video review + full log analysis, 2026-09-12)

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| 08:22:51 | Video starts. Left bud is in the case, right bud is out (loose). Bluetooth is off. | System | — | — |
| ~08:22:53 | Bluetooth toggled on | User | — | — |
| 08:22:56.997 | Classic `Connect Complete` (stored key) | System | — | Log frame 294. |
| 08:22:57.421–.471 | DLCI 0x04 `Get`/`Notify` (`Settable=0xe8`=undocked, `Current=0x40`=Adaptive) | System (auto) | — | Log frames 609/612. |
| ~08:23:01–04 | Pixel Buds app opened, "Device details" screen — **the main ANC-toggle row, not the "Controls and gestures" rotation-checklist screen** | User (App) | — | Video `t=30` confirms: the top-level "Noise cancellation/Off/Adaptive/Transparency" row is visible, "Transparency" selected black. |
| **08:23:21.949** | **Physical long-press, Left earbud: Adaptive→Transparency** | User (Hardware) | `TOUCH-007` (not `HOLD-005`) | Log frame 1583, `Notify`-only (`0813000401e8e880`), **no preceding `08 12` Set frame** — matches `CAP-027-FINDINGS.md` §4's press-and-hold mechanism exactly. |
| **08:23:32.507** | Physical long-press, Left: Transparency→Noise cancellation | User (Hardware) | `TOUCH-007` | Log frame 1755, `Notify`-only (`...e808`). |
| **08:23:46.899** | Physical long-press, Left: Noise cancellation→Adaptive | User (Hardware) | `TOUCH-007` | Log frame 1818, `Notify`-only (`...e840`). |
| **08:23:54.406** | Left earbud removed from ear (ANC reads "Off") | User (Hardware) | — | Log frame 1849, `Notify`-only (`...0020`) — bud-removal produces the same wire shape as a gesture Notify; only timing/video distinguishes them. |
| 08:23:59.785–.810 | A fresh `Get`/`Notify` pair fires (`Current=Off`) | System (auto) | — | Log frames 2077/2079 — plausibly tied to the right bud's in-ear insertion (below), not re-checked further. |
| **~08:24:00** | Right earbud inserted into ear | User (Hardware) | — | — |
| **08:24:02.089** | ANC reads Adaptive (no video-visible discrete gesture at this exact moment) | — | — | Log frame 2122, `Notify`-only. |
| **08:24:06.395** | Physical long-press, Right: Adaptive→Transparency | User (Hardware) | `TOUCH-007` | Log frame 2168, `Notify`-only (`...e880`). |
| **08:24:16.232** | Physical long-press, Right: Transparency→Noise cancellation | User (Hardware) | `TOUCH-007` | Log frame 2218, `Notify`-only (`...e808`). |
| **08:24:20.835** | Physical long-press, Right: Noise cancellation→Adaptive | User (Hardware) | `TOUCH-007` | Log frame 2237, `Notify`-only (`...e840`). |
| **08:24:27.033** | Physical long-press, Right: Adaptive→Transparency | User (Hardware) | `TOUCH-007` | Log frame 2254, `Notify`-only (`...e880`) — **corrects the draft's duplicate "08:24:20" timestamp for this row; the wire places it 6.2s later.** |
| **08:24:30.086** | Right earbud removed from ear (ANC reads "Off") | User (Hardware) | — | Log frame 2273, `Notify`-only (`...0020`). |
| 08:24:32 | Video ends (100.75s) | System | — | — |

**No DLCI 0x02 write matching the ANC-rotation-checklist's `field5{field4{field12{...}}}` shape
(`qhr` field 12/`qht`, `PROTOCOL.md` §4.5.3) occurs anywhere in this session** — every DLCI 0x02
`Sent` frame in the full-session query matches the already-documented connect-time settling-burst
shape (`CAP-036-FINDINGS.md` §4), not a settings write. **This session never opened the rotation
checklist and does not answer `HOLD-005`'s own question.**

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AJ)

- [x] Isolate each of the 8 toggles (4 Left + 4 Right) to its own DLCI 0x02 `Sent` frame. → **N/A —
      no such frames exist in this session.** The actual actions performed (physical press-and-hold
      ANC cycling) ride DLCI 0x04's `Notify`-without-`Set` mechanism instead (`TOUCH-007`), not
      DLCI 0x02's settings-write path `HOLD-005` targets.
- [x] Do the Left-earbud toggles and Right-earbud toggles produce a distinguishable wire pattern? →
      For the mechanism actually exercised (`TOUCH-007`), **no** — the `08 13 00 04 01 e8 <state>`
      Notify shape structurally has no Left/Right field at all (confirmed against
      `PROTOCOL.md` §4.1's documented layout); only timing/video correlation distinguishes which
      earbud triggered a given Notify, extending `CAP-027-FINDINGS.md` §4's own single-earbud
      finding to a 10-sample, two-earbud replication.
- [x] Record the result plainly either way. → **`HOLD-005`'s own question is unanswered by this
      capture — the wrong procedure was run.** A genuine re-run, opening the rotation-checklist
      screen and toggling individual items, is still needed.

## Next steps after filling this in

- [x] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — `HOLD-005` was **not** actually exercised; `TOUCH-007` was, incidentally.
- [x] Write `CAP-045-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [x] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [x] Update `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `HOLD-005` row — **not** promotable from this
      session; noted as still needing a genuine attempt.
- [x] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-045-2026-09-12_08-22-51_08-24-32-Group_AJ/CAP-045-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-045-2026-09-12_08-22-51_08-24-32-Group_AJ/CAP-045-EVENT-NOTES
