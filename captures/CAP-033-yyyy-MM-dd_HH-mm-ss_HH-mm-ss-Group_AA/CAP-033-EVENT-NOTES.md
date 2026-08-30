# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AA, SDP UUID branch isolation for `gbm.a()`'s "default internal rfcomm socket" path (`CAP-033`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-033-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AA` to the actual session
date/start-time/end-time, e.g. `CAP-033-2026-09-01_08-30-00_08-35-00-Group_AA`.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AA, added 2026-08-30):** `REVERSE_ENGINEERING.md`'s
`gbm`/`fzd` entries and `DECISIONS.md` ADR-018 found the companion app's own decompiled code
(`gbm.java:35-43`) picks between two internal RFCOMM sockets depending on which of two SDP UUIDs is
present: "pigweed" (`25e97ff7-24ce-4c4c-8951-f764a708f7b5`, confirmed = DLCI 0x02 in every capture
so far) or "default" (`3a046f6d-24d2-7655-6534-0d7ecb759709`, never observed on the wire anywhere —
a raw-byte scan of all 26 capture files this project has, in every format, found zero occurrences
in either byte order). This session tests `SDP-001`: whether the discovered-UUID set the app's
channel selector sees depends on *who* triggers the SDP browse — the OS's own default pairing flow
(app force-stopped, never opened) vs. the companion app's own `fetchUuidsWithSdp()` re-fetch
(`fxm.java:110`). `SDP-002` (firmware-update before/after comparison) is opportunistic and may not
be exercisable in this same session — see the Event Timeline's optional second block.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-033`                     |
|      Group(s)    |                         AA                         |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    |                    Pixel 7a                        |
| App version      |    TBD — n/a during step 1–2 (app force-stopped)   |
| Video file       |          TBD — optional (system-settings screens only, nothing app-side to confirm until step 3) |
| Log file         |             TBD — `CAP-033-btsnoop_hci.log`        |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

**Isolation check (required — this is the whole point of the session):** confirm and record
explicitly that the Pixel Buds companion app was force-stopped (`Settings → Apps → Pixel Buds →
Force stop`) **before** the "Forget" action in step 1, and that it was not opened, auto-launched,
or otherwise foregrounded at any point until step 3 begins.

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AA)

1. **[`SDP-001`]** Force-stop the Pixel Buds companion app. Start Bluetooth HCI snoop logging (§2).
   "Forget" the Buds via system Bluetooth settings only (same action as `PAIR-001`, **not**
   `CASE-007`'s factory reset). Re-pair via system Bluetooth settings' "Pair new device" flow only.
   Note the exact time pairing completes.
2. Keep observing for at least 60s after bonding completes, without opening the companion app or
   touching the buds/case.
3. **Still `SDP-001`, second half of the same session:** open the companion app normally and let it
   connect as usual (baseline for in-session comparison).
4. Pull the bugreport (§3) once, at the end.
5. **[`SDP-002`], opportunistic — only if a firmware update happens to be available at capture
   time:** repeat the SDP-observation half of steps 1–2 immediately before installing the update,
   and again immediately after it completes and the Buds reconnect. If no update is available,
   leave this row `not attempted` rather than forcing it.

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Companion app force-stopped | User (Hardware) | `SDP-001` | TBD |
| TBD | "Forget" via system Bluetooth settings | User (Hardware) | `SDP-001` | TBD |
| TBD | Re-pair via system Bluetooth settings ("Pair new device") | User (Hardware) | `SDP-001` | TBD |
| TBD | Bonding completes | Buds/Case (Auto) | `SDP-001` | TBD |
| TBD | Observation window end (≥60s after bonding, app still not opened) | — | `SDP-001` | TBD |
| TBD | Companion app opened | User (App) | `SDP-001` | TBD |
| TBD | App connects / stabilizes | App (Auto) | `SDP-001` | TBD |
| TBD (opportunistic) | Firmware update installed | App (Auto) | `SDP-002` | TBD — mark `not attempted` if no update was available |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AA)

- [ ] Pre-filter by address (`AGENTS.md` §13 CLI hygiene): `bluetooth.addr == 04:00:6e:cf:6e:07`.
- [ ] Then filter for `btsdp`, extracting `btsdp.data_element.value.uuid_128` and
      `btsdp.protocol.channel` (exact `tshark` command in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AA
      and `REVERSE_ENGINEERING.md`'s `gbm` entry).
- [ ] Check the pre-app-open window (step 2) specifically: does `3a046f6d-24d2-7655-6534-0d7ecb759709`
      (or its byte-reversed form `099775cb-7e0d-3465-5576-d2246d6f043a`) appear anywhere, instead of
      or alongside the "pigweed" UUID?
- [ ] Compare against the post-app-open window (step 3) — does the UUID set change once the app has
      had a chance to run its own `fetchUuidsWithSdp()`?
- [ ] If `SDP-002` was attempted: compare the before/after-update SDP UUID sets.
- [ ] Record whichever three-way outcome applies (positive / negative / `SDP-002` positive) per
      `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AA's Analysis section — don't leave it ambiguous.

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `SDP-001` (and `SDP-002` if attempted) are clearly referenced
      above.
- [ ] Write `CAP-033-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Update `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `SDP-001`/`SDP-002` rows' Evidence column with a
      pointer once promoted into `PROTOCOL.md`.
- [ ] If positive: any promotion to 🟢 FACT in `PROTOCOL.md` or new/superseding `DECISIONS.md` ADR
      still requires explicit maintainer sign-off (`AGENTS.md` §6) — write it up as a proposal, do
      not commit it as settled.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-033-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AA/CAP-033-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-033-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AA/CAP-033-EVENT-NOTES
