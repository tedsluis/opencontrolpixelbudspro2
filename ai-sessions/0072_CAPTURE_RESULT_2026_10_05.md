# 0072_CAPTURE_RESULT_2026_10_05.md — Full analysis of CAP-053 (Group AO), CAP-054 (Group AP) and CAP-058 (Group AT)

**Number:** 0072
**Category:** CAPTURE
**Date:** 2026-10-05
**Title:** Full analysis of CAP-053 (Group AO: EQ field 16 vs 18), CAP-054 (Group AP: the `0xFE2C` advertisement on case-open, connection-free) and CAP-058 (Group AT: `SDP-001` 3rd attempt with a process-liveness check)
**Status:** complete (all four checkpoint questions approved in chat 2026-10-06; not yet committed — see Commits)

## Progress

- **Done:** all phases. Phase 0 (git state, branch `capture/0072-cap-053-054-058` from `origin/main` = `771f400`, files identified, `.log.last`
  relations, privacy check, the maintainer's answers, migration); Phase A (films: 2-s sheets read in full by three read-only sub-reviews, every
  transition narrowed by the main session at 4 fps — several sub-review errors corrected; clock offsets; audio); Phase B (all five logs); Phase C (three
  EVENT-NOTES, three FINDINGS); Phase D (checkpoint, approved edits, footers, lint exit 0).
- **Next:** the maintainer's answer to the commit question; then commit/push/PR on the session branch.
- **Touched, unverified:** none (every number in the documents re-derived by the main session; sub-review output used only as brackets to narrow).
- **Intermediate results:** session scratchpad (contact sheets, 4-fps strips, decodes, the helper scripts inventory.py, msgs.py, fe2c_tlv.py,
  assemble.py, strip.sh). Re-create with the commands quoted in the three FINDINGS if gone.

## Plain-language answers

- **CAP-053 (EQ field 16 vs 18):** the official app writes the "last saved" EQ (field 18) **only when Save is tapped** — once, 0.2–0.5 s after the
  filmed tap. Letting go of a slider, waiting, or leaving the screen with Home/Recents writes nothing to field 18. Leaving with **Back** was not done, so
  the code's "navigate away, save" path is still untested. `PROTOCOL.md` §4.2 updated (approved). For OpenControl nothing changes: it writes field 16
  only. The run differed from the plan: Home instead of Back, Mid dragged twice (the EQ screen redrew the Mid slider by itself 10 s after the drag).
- **CAP-054 (battery advertisement on case-open):** the Buds do **not** put the documented battery field in their advertisement — not with the lid
  closed, not after opening it (twice), not with a bud out (610 reports, scanner on). Yet Android showed "Left 100% Case 53% Right 100%" ≈ 1.5 s after
  each lid opening without any connection — so the levels travel in a form the phone decodes with the account key (🟡). For OpenControl: no Case level
  without a connection; nothing to build (`PROTOCOL.md` §4.3 Option A and ADR-006 updated, approved). Each bud taken out started a Buds-initiated
  connection; the second lid close was skipped.
- **CAP-058 (`SDP-001`, 3rd attempt):** the force stop killed the companion app, but it was **running again ≈ 10 s later** (new PID), before the Forget
  and before both filmed pairings — so "app never running" was not reached; what started it is not on record (no events log), most likely Settings'
  device pages. The "default" UUID `3a046f6d-…` appears in **none** of four more full SDP browses, and the phone never asks for it. Status unchanged;
  dated notes in `REVERSE_ENGINEERING.md` and ADR-018; a 4th attempt (app disabled) is in `TODO.md` (approved).
- **CAP-058's extra tests (X1–X14):** "Phone calls" = HFP; "Media audio" = A2DP/AVRCP **and** the GSND channels; "Input device" = HID; "Use audio switch"
  = the phone's SASS flags `80 00`/`00 00`; "Bud return" / "Other alerts" = fields 28 / 27 (the label now 🟢); "Volume level notifications" =
  **`qhr` field 21** (first time on the wire, 🟢); Hearing wellness reads the Dosimeter; About and the other pages send nothing. Fast Pair pairing and
  Settings pairing differ: the Buds answer DisplayYesNo (key 0x05) vs NoInputNoOutput (0x04). New lead: Settings showed **Find device** while the Buds
  were not connected and not after they connected (🟡).
- **The app 1.0.955078536 after the downgrade:** same connect sweep and services as before; no service `0xbf6c9399`; fresh app data changed nothing on
  the wire.

## Checkpoint answers (chat 2026-10-06, `AskUserQuestion`, verbatim)

- "Field 18" → *"Approve as drafted (Recommended)"* — applied: `PROTOCOL.md` §4.2, Update of 2026-10-06.
- "Option A" → *"Approve both (Recommended)"* — applied: `PROTOCOL.md` §4.3 Option A Update; `DECISIONS.md` ADR-006 Update.
- "SDP-001" → *"Notes + 4th-attempt TODO (Recommended)"* — applied: `REVERSE_ENGINEERING.md` `gbm` entry Update; ADR-018 Update; `TODO.md` §3.
- "Extras" → *"Field 21 = Volume notif. (🟢), Pairing IO caps + X1–X3 (🟢), SASS 07 11 flags + Find device (🟡), Other alerts = field 27 (label)"*, with the
  note: *"Heb je ook vast gelegd wat er gebeurde toen ik de switches 'phone calls', 'media audio','input devie', 'buds return', 'other alerts' en 'use
  audio switch' ééń voor ééń uitschakelde en weer inschakelde?"* — applied: `PROTOCOL.md` §4.5.8a (new), §4.5.8 Update, §5.1 Note, §6 Behavior item;
  `REVERSE_ENGINEERING.md` `qhr` register Update; `TODO.md` §4 (Find device). Answer to the note: yes — `CAP-058-EVENT-NOTES.md` ("The extra tests") and
  `CAP-058-FINDINGS.md` §6 record each switch off and on with time, frames and result.
- The approvals were given on 2026-10-06 (the user's clock), so the dated texts carry 2026-10-06.

## Files read

In full: `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `TODO.md`, `AI_SESSION_LOG_PROCEDURE.md`, the three skeletons (committed and
working tree), `CAP-049-EVENT-NOTES.md`, `CAP-049-FINDINGS.md`, the `dumpsys` file, the prompt. `PROTOCOL.md`: §0–§5 in full; §6 partly (searched
for the items these captures touch: SDP, `0xFE2C`, battery notification, field 18). `DECISIONS.md`: the ADR list in full; ADR-006, -010, -018, -020,
-034, -037 in full; the others by title. `CAPTURE_BLUETOOTH_HCI_SNOOP.md`: §3, §5, §8, §9 header and rows, Groups AO, AP, AT (both versions of AT,
`git diff`). `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`: the rows of the Test-IDs touched. `REVERSE_ENGINEERING.md`: the `qhr` register, the `gbm` entry, the UUID
register row. `CAP-015-FINDINGS.md` §6. Not read: the earlier session files named in the prompt's list (`0016` … `0071`) and `CAP-033`/`CAP-044`/
`CAP-043`/`CAP-068`/`CAP-069` FINDINGS — their facts were taken from `PROTOCOL.md`/`DECISIONS.md` and re-derived from the logs where used
(`CAP-043`'s `0xFE2C` reports, `CAP-033`'s UUIDs).

## Phase B tables (summary; full tables in the FINDINGS)

| Capture | Key table | Where |
|---|---|---|
| CAP-053 | every `WriteSetting` (6: five field 16, one field 18) with floats, trigger, gap, CRC | `CAP-053-FINDINGS.md` §1 |
| CAP-054 | every `0xFE2C` advertiser (6 addresses, 610 reports) with field layout per step; post-salt byte vs lid 225/225 | `CAP-054-FINDINGS.md` §1 |
| CAP-058 | dumpsys runs vs film vs SDP browses; UUID search in 5 logs + control; pairings with IO caps; X1–X14 vs wire | `CAP-058-FINDINGS.md` §1–§3, §6 |

## External sources (raw text, fetched 2026-10-05)

- `https://developers.google.com/nearby/fast-pair/specifications/extensions/batterynotification` — *"One common use case for this is to use 0b0011
  when the case has opened and 0b0100 when buds have been removed from the case or it has been closed again."*; *"To prevent tracking, the Provider
  should not include raw battery data in the advertisement all the time."*
- `…/specifications/service/provider` — Account Key Data: *"type = 0b0000 (show UI indication) or 0b0010 (hide UI indication), Account Key Filter"*;
  Salt *"0b00100001 … type = 0b0001"*; discoverable data *"type = 0b0111, Model ID"*, *"type = 0b1000, FP Capability Map"*.
- `…/specifications/extensions/sass` — Table 4.2: *"Version and flags 0x10 … Account Key Data … Battery Data … Random Resolvable Data"*, field
  *"0bLLLL0110"*; *"Bit 0 (octet 6, MSB): Audio switch state 1, if Audio switch state is on"*.
- `https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/core/java/android/content/pm/ApplicationInfo.java` — *"Applications should
  avoid launching activities, binding to or starting services, or otherwise causing a stopped application to run unless initiated by the user. … An app
  can also return to the stopped state by a "force stop"."*
- `…/services/core/java/com/android/server/am/ActiveServices.java` — `bindServiceLocked`: `wasStopped` is only logged; `bringUpServiceLocked` starts the
  process (`HostingRecord.HOSTING_TYPE_SERVICE`).
- `…/services/core/java/com/android/server/am/EventLogTags.logtags` — *"30014 am_proc_start (User|1|5),(PID|1|5),(UID|1|5),(Process
  Name|3),(Type|3),(Component|3)"*.

## Phase 0 — maintainer's answers (chat 2026-10-05, `AskUserQuestion`, verbatim)

- "CAP-054 film" → *"Commit as recorded"*.
- "CAP-058/053" → *"Commit both as recorded"*.
- ".log.last" → *"Keep both, unchanged (Recommended)"*.
- "Migration" → *"Yes, as proposed (Recommended)"*.

Executed: per folder `sha256sum` listing before and after a plain `mv` (identical; 4, 3, 5 files) → `captures/CAP-053-2026-10-05_17-46-34_17-49-39-Group_AO/`,
`captures/CAP-054-2026-10-05_18-02-33_18-08-29-Group_AP/`, `captures/CAP-058-2026-10-05_21-39-51_21-46-22-Group_AT/`; `chmod 644` on the three films;
old paths replaced in `_sidebar.md` and the three EVENT-NOTES footers (no other reference outside `ai-sessions/`; the prompt names files, not folders).
Registry and Capture Index rows are updated with the documentation in Phase D.

## Phase 0 — privacy check (sub-reviews of the 2-s sheets; every item below re-checked by the main session on full-resolution frames)

Overlay (camera clock) at the first/last frame: CAP-053 17:46:34 / 17:49:39; CAP-054 18:02:33 / 18:08:29; CAP-058 21:39:51 / 21:46:22. The overlay carries
date and time only — **no street-address overlay** in any of the three films.

- **CAP-053:** device name "Pixel Buds Pro 2 van Ted"; carrier "AH Mobiel"; the launcher home screen (folder names, weather) at film ≈ 156–162 s;
  hands. No notification text, e-mail, Wi-Fi name, address or serial.
- **CAP-054:** notification shade at film ≈ 5.0–6.0 s (checked at 2 fps): a bank notification ("Bedrag betaald … −15,97 EUR van rekening *642…"), a
  LinkedIn notification naming a third person, WhatsApp "Logged out…", "You tracked a wor… · ted.sluis…" (partial e-mail); Quick Settings ≈ 5–22 s: Wi-Fi
  "Bachstraat20" (a street name + number), Wallet "••5759"; Android build `17 (CP3A.260905.009)` readable at film ≈ 8 s; the end (≈ 333–356 s): home
  screen, Play Store search history, Play points, another device of the account. Device name "…van Ted".
- **CAP-058:** Google account e-mail in full in the Fast Pair half-sheets (film ≈ 84–86 s, 106 s, 162–164 s; truncated in a list row ≈ 84 s);
  "Bachstraat20" (media card and Wi-Fi tile, ≈ 2–22 s), Wallet "••5759" (≈ 22 s); the phone's own Bluetooth address (≈ 134–136 s); four neighbouring TVs'
  Bluetooth names (≈ 134 s); the Buds' Bluetooth address (≈ 170–178 s) and the three full serial numbers (≈ 374–390 s) — both already in committed
  documents (ADR-010); device name "…van Ted". No laptop screen, no face.

## Phase 0 — files identified (re-checked 2026-10-05)

| File | Measured |
|---|---|
| `CAP-053-recording.mp4` | 184.54 s, 142,302,330 B, H.264 1280×720 rotation −90, 5,502 frames, AAC 7,913 frames, `creation_time` 2026-10-05T15:49:39Z, sha256 `d2397236…` |
| `CAP-053-btsnoop_hci.log` | 2,924 packets, 17:46:25.846 – 17:53:48.054, sha256 `3916b304…` |
| `CAP-053-btsnoop_hci.log.last` | 3,081 packets, 17:39:52.118 – 17:46:23.518, sha256 `70e6608d…` |
| `CAP-054-recording.mp4` | 356.02 s, 274,942,258 B, 10,654 frames, AAC 15,314 frames, `creation_time` 2026-10-05T16:08:29Z, sha256 `784f2a0f…` |
| `CAP-054-btsnoop_hci.log` | 4,807 packets, 18:02:45.507 – 18:10:34.930, sha256 `69223205…` |
| `CAP-058-recording.mp4` | 391.01 s, 302,393,835 B, 11,695 frames, AAC 16,803 frames, `creation_time` 2026-10-05T19:46:22Z, sha256 `e28ea836…` |
| `CAP-058-btsnoop_hci.log` | 6,093 packets, 21:40:06.636 – 21:49:13.292, sha256 `ee860f35…` |
| `CAP-058-btsnoop_hci.log.last` | 13,250 packets, 18:02:45.507 – 21:40:04.504, sha256 `07a76354…` |
| `CAP-058-adb-shell-dumpsys-activity-processes.txt` | 17,501 B, six runs (21:40:22, :35, :51, 21:41:00, :21, 21:42:14 laptop clock), sha256 `1c0f91d4…` |

`git check-attr filter`: `lfs` for the three films, the five logs and the `.txt`; `unspecified` for the three notes. Films mode `-rwxr-----`.

## Phase 0 — `.log.last` relations (evidence for the keep/leave-out proposal)

- `CAP-054-btsnoop_hci.log` (268,989 B) is a **byte-prefix** of `CAP-058-btsnoop_hci.log.last` (`cmp -n 268989` → equal; md5 of
  `frame.time_epoch, frame.len, frame.cap_len, data.data` over the 4,807 packets equal in both, `d6a2997d…`). `.log.last` continues to 21:40:04.504 and holds:
  a Buds ACL at 18:10:10 (handle 0x0005, CAP-054's third) that ends 18:18:00.80 (reason 0x13) — after CAP-054's log was pulled; LE links to other devices
  19:53–19:59; **`Delete Stored Link Key` 21:34:40.246** (frame 9436); **two complete re-pairings before the CAP-058 film**: LE link 21:36:28.24 → `Create
  Connection` → SSP → Link Key Notification 21:36:29.91 → SDP browse 21:36:30.27–21:36:34.17 → RFCOMM 0x0c/0x0a/0x08/0x04 (no DLCI 0x02) → disconnect
  21:36:42.54 (0x16) + `Delete Stored Link Key`; then LE link 21:37:06.60 → pairing → SDP 21:37:08.29–21:37:11.74 → RFCOMM incl. **DLCI 0x02 (MAESTRO) SABM
  21:37:10.63** → disconnect 21:37:25.35 (0x13). The last Buds-unrelated LE link ends 21:40:04.40.
- `CAP-053-btsnoop_hci.log.last` holds a separate earlier session: phone `Create Connection` 17:39:52.70 → ACL 17:39:55.44 → MAESTRO open 17:40:48.81, its
  connect reads (`ReadSetting` 16 = [−1,0,4,2,0], 18 = [−4.9,4.3,−4.4,3.5,−4.2]) and **no `WriteSetting`** → local disconnects 17:46:22.54 (0x16) — the
  Bluetooth-off before the film. `CAP-053-btsnoop_hci.log` starts 17:46:25.846 with its own `Sent Reset`.

## Phase 0 — git state at start

- `git log -1`: `771f400 docs: auto-regenerate sidebar [skip ci]`; branch `main`; `git fetch` → `HEAD..origin/main` empty, `origin/main..HEAD` empty.
- `git status --short`: ` M CAPTURE_BLUETOOTH_HCI_SNOOP.md`, ` M TODO.md`, ` M …CAP-053-EVENT-NOTES.md`, ` M …CAP-054-EVENT-NOTES.md`; untracked: this prompt, the
  CAP-053 `.log`/`.log.last`/`.mp4`, the CAP-054 `.log`/`.mp4`, the CAP-058 `.txt`/`.log`/`.log.last`/`.mp4`.


## Deferred documentation

Each item is also in `TODO.md`:

- Group AO, step 5 only, with Back — `TODO.md` §3 ("Group AO, step 5 only: drag a slider, wait ≥ 3 s, press **Back** …").
- Group AT, 4th attempt with the app disabled — `TODO.md` §3 ("Group AT, 4th attempt: disable the companion app first …").
- Re-check `CAP-015`'s film at its field-18 frames — `TODO.md` §3 ("Documentation only: re-check `CAP-015`'s film …").
- Open questions of the three FINDINGS — `TODO.md` §4 ("`CAP-054`: no clear Battery Notification field …", "`CAP-058`: what restarted …", "`CAP-053`: why
  the EQ screen redrew …") and the Find device observation in the Ring-status line.
- `_sidebar.md` statuses (still "planned") are regenerated by the CI workflow; only the paths were changed here.

## Commits

Not committed — awaiting the maintainer's answer to the commit question (task 18).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0072_CAPTURE_RESULT_2026_10_05.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0072_CAPTURE_RESULT_2026_10_05
