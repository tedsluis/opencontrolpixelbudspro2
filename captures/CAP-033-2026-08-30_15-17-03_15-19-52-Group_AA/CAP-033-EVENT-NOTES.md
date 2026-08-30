# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AA, SDP UUID branch isolation for `gbm.a()`'s "default internal rfcomm socket" path (`CAP-033`)

**Status:** ✅ **Captured and analyzed 2026-08-30.** See `CAP-033-FINDINGS.md` for the full
evidence-based writeup, **including a required isolation-integrity review (`CAP-033-FINDINGS.md`
§1) that found one confirmed procedure-order deviation** — read that section before trusting this
session's `SDP-001` conclusion. This file records the validated event timeline the findings are
built on.

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
|       Date       |                     2026-08-30                     |
| Firmware version | `release_5.203` — 🟢 confirmed on-wire (3 occurrences) |
|   Test device    |                    Pixel 7a                        |
| App version      |    n/a — app force-stopped throughout the entire recorded session (never opened, see isolation check below) |
| Video file       |          `CAP-033-recording.mp4` — 169.16s, 15:17:03–15:19:52 local time |
| Log file         |    `CAP-033-btsnoop_hci.log` — 2,459 packets, 15:16:57.20–15:22:43.50 (+0200), **~2m51s longer than the video** (see isolation check) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |         `04:00:6e:cf:6e:07` — same physical device as `CAP-021`/`CAP-027`             |

**Isolation check (required — this is the whole point of the session): ⚠️ NOT fully clean —
see `CAP-033-FINDINGS.md` §1 for the full analysis.** Summary of the three sub-checks:

1. **Order (Forget vs. Force-stop): 🔴 confirmed procedure violation.** Video shows "Forget device?"
   confirmed at 15:17:24, and "Force stop" only tapped at 15:17:34 — **Forget happened first**,
   10s before Force-stop, the reverse of the written procedure ("Force-stop the app first," step
   1). However, timing analysis shows the actual re-pair/SDP-browse (the evidence `SDP-001`
   depends on) didn't begin until 15:17:52+, well after Force-stop completed — see
   `CAP-033-FINDINGS.md` §1.1 for why this violation is judged not to have contaminated the SDP
   evidence itself, while still being recorded as a real deviation.
2. **The 15:18:16 popup: not a violation.** Video-confirmed (`CAP-033-FINDINGS.md` §1.2) to be the
   **system-level** Fast Pair "Save device to account" dialog (Android/GMS Nearby UI), not the
   Pixel Buds companion app launching — the app remained un-opened, screen stayed on its Settings
   "App info" page throughout. The original "user cancels pixel buds app popup" timeline label
   below is imprecise and is corrected in this note.
3. **Step 3 (open the app for baseline comparison): 🔴 never executed.** Confirmed by video (the
   screen never leaves the Pixel Buds "App info" settings page for the rest of the recording) and
   by the log (zero further SDP-browse/RFCOMM-service-search traffic anywhere after 15:18:18.8, all
   the way to the log's end at 15:22:43.5). No in-session before/after comparison is possible from
   this capture.

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

Video-overlay timestamps and `btsnoop_hci.log` wire times run within ~1–2s of each other in this
session (same-direction offset as independently observed in `CAP-027`, this project's other
2026-08-30 session) — individual offsets noted per row below.

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| 15:17:03 | start of the video
| 15:17:10 | user enables bluetooth
| 15:17:23 | "Forget" via system Bluetooth settings | User (Hardware) | `SDP-001` | Video-confirmed: "Forget device?" dialog shown 15:17:22, "Forget" tapped 15:17:24 (`CAP-033-FINDINGS.md` §1.1) — **before** Force-stop below, contrary to procedure |
| 15:17:34 | Companion app force-stopped | User (Hardware) | `SDP-001` | Video-confirmed: Settings → Apps → Pixel Buds → App info → "Force stop" tapped 15:17:34, confirmed 15:17:36 (`CAP-033-FINDINGS.md` §1.1) |
| 15:17:52 | user selects "pair new device"
| 15:17:53 | user opens case
| 15:17:55 | user presses and holds pair button on case
| 15:18:01 | user selects pixel buds pro van Ted form Available devices.
| 15:18:11 | User selects pair device | User (Hardware) | `SDP-001` | Bonding completes 15:18:09.16–.17 (log): `Simple Pairing Complete` frame 1241, `Link Key Notification` frame 1242, `Authentication Complete` frame 1243 (offset ~1.8–2.0s) |
| — | (not separately timestamped in the original notes) | — | `SDP-001` | **SDP browse (the core evidence for this Test-ID), frames 1256–1873, 15:18:09.417–15:18:18.822** — see `CAP-033-FINDINGS.md` §2/§3 for the full frame-by-frame breakdown |
| 15:18:16 | ~~user cancels pixel buds app popup~~ — **corrected**: user cancels the **system-level** Fast Pair "Save device to ted.sluis@gmail.com" dialog | User (Hardware) | `SDP-001` (isolation check) | Video-confirmed 15:18:15–15:18:17: this is Android/GMS's own account-linking prompt, not the Pixel Buds companion app opening — see `CAP-033-FINDINGS.md` §1.2. The app's own "App info" screen remains visible underneath/after, unchanged |
| 15:19:52 | end of video | — | — | Log continues **2m51s past this point** (to 15:22:43.5) with no further SDP/RFCOMM-service traffic — Step 3 (open the app for baseline) was never performed on- or off-camera, see `CAP-033-FINDINGS.md` §1.3 |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AA)

- [x] Pre-filter by address (`AGENTS.md` §13 CLI hygiene): `bluetooth.addr == 04:00:6e:cf:6e:07`.
- [x] Then filter for `btsdp`, extracting `btsdp.data_element.value.uuid_128` and
      `btsdp.protocol.channel`. → All `btsdp` traffic in this log (frames 1256–1873) falls in one
      tight window, 15:18:09.417–15:18:18.822 — see `CAP-033-FINDINGS.md` §2.
- [x] Check the pre-app-open window (step 2) specifically: does `3a046f6d-24d2-7655-6534-0d7ecb759709`
      (or its byte-reversed form `099775cb-7e0d-3465-5576-d2246d6f043a`) appear anywhere, instead of
      or alongside the "pigweed" UUID? → **Negative.** Zero occurrences of the "default" UUID in
      either byte order anywhere in this log (raw byte scan, not just the SDP-dissected frames).
      "Pigweed" (`25e97ff7-...-f7b5`) **is** present (frame 1279, named on-the-wire "MAESTRO APP" —
      see `CAP-033-FINDINGS.md` §3). This extends, unchanged, the standing negative result across
      every capture this project has.
- [x] Compare against the post-app-open window (step 3) — does the UUID set change once the app has
      had a chance to run its own `fetchUuidsWithSdp()`? → **Not possible this session** — step 3
      was never executed (see the isolation check above and `CAP-033-FINDINGS.md` §1.3).
- [ ] If `SDP-002` was attempted: compare the before/after-update SDP UUID sets. — not attempted
      this session (no firmware update was pending), left `not attempted` per the procedure's own
      guidance rather than forced.
- [x] Record whichever three-way outcome applies (positive / negative / `SDP-002` positive) per
      `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AA's Analysis section — don't leave it ambiguous. →
      **Negative** for the "default" UUID, but the isolation violation (§1.1) and missing step-3
      comparison (§1.3) mean this negative result cannot be raised above 🟡 HYPOTHESIS this
      session — see `CAP-033-FINDINGS.md` §1/§6 for the full reasoning.

## Next steps after filling this in

- [x] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `SDP-001` (and `SDP-002` if attempted) are clearly referenced
      above. `SDP-001` is covered; `SDP-002` explicitly marked not attempted (opportunistic Test-ID,
      no update was pending).
- [x] Write `CAP-033-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [x] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [x] Update `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `SDP-001`/`SDP-002` rows' Evidence column with a
      pointer once promoted into `PROTOCOL.md`. — pointer added to `CAP-033-FINDINGS.md` directly;
      no `PROTOCOL.md` promotion made (isolation issue keeps this at 🟡 HYPOTHESIS, per `AGENTS.md`
      §6 this alone would need maintainer sign-off regardless).
- [x] If positive: any promotion to 🟢 FACT in `PROTOCOL.md` or new/superseding `DECISIONS.md` ADR
      still requires explicit maintainer sign-off (`AGENTS.md` §6) — write it up as a proposal, do
      not commit it as settled. — N/A this session (`SDP-001`'s own result stays 🟡 HYPOTHESIS,
      not promoted); the DLCI 0x08/"GSND CONTROL" SDP-identity lead in `CAP-033-FINDINGS.md` §5 is
      written up as an explicit proposal awaiting maintainer review, not committed.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time. **Not needed** — the folder name already embeds the
      actual date/start/end (`2026-08-30_15-17-03_15-19-52`), it never used the literal
      `-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-` skeleton pattern.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-033-2026-08-30_15-17-03_15-19-52-Group_AA/CAP-033-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-033-2026-08-30_15-17-03_15-19-52-Group_AA/CAP-033-EVENT-NOTES
