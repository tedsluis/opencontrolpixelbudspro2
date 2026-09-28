# 0055_CAPTURE_RESULT_2026_09_28.md — Full analysis of CAP-056 (Group AR: the ANC-mode checklist of press-and-hold, and in-ear detection off, with the official app on the Pixel 7a)

**Number:** 0055
**Category:** CAPTURE
**Date:** 2026-09-28
**Title:** Fully analyse `CAP-056` (video with audio, HCI snoop log) — record CAP-056-EVENT-NOTES.md and CAP-056-FINDINGS.md, verify the in-ear-detection observation, draft what field 12 and field 2 would need
**Status:** complete

---

## Progress (final)

- Phase 0 — done: git state at start `3cb1195`, `git fetch` → nothing new; files identified; privacy reviewed and asked; folder migrated
  (`git mv` → `captures/CAP-056-2026-09-28_17-30-53_17-35-58-Group_AR/`, sha256 of all three files equal after the move, mp4 mode 0750 → 644, LFS confirmed).
- Phase A — done: the whole video (1 fps + frame-exact passes) and the whole audio track; clock measured at start and end.
- Phase B — done: every Buds packet on handle `0x0005` (DLCI 0x00/0x02/0x04/0x08/0x09/0x0a, AVRCP/AVDTP, HCI events); LE handle `0x0003` identified as
  an unrelated device.
- Phase C — done: `CAP-056-EVENT-NOTES.md` rewritten (skeleton kept as Appendix A), `CAP-056-FINDINGS.md` new.
- Phase D — done: checkpoint answered in chat, approved documentation applied, `ensure_footers.py` and `lint_docs.py` exit 0. **Commit/push: awaiting the
  maintainer's answer to the final question.** Nothing under `android/` was modified.
- Intermediate results (not committed) were in the session scratchpad: contact sheets, the pass-1 video timeline, the decoded pw_rpc table, the audio
  WAV/spectrograms, the per-frame checkbox/switch measurements.

## 1. Plain-language answers

- **Your observation (in-ear detection) — confirmed.** With in-ear detection **on**, the phone paused the music at every removal and resumed it at every
  re-insertion (3 of 3 each, e.g. Paused 17:33:46.818, Playing 17:33:54.248). With it **off**, nothing paused at any of the 6 wear changes. One correction of
  mechanism: the Buds did **not** send an AVRCP pause command in this capture — the phone's own player changed state (it reported "Paused"/"Playing" itself), each
  time ≈ 0.1–0.2 s after the Buds told the Google-app channel (DLCI 0x08) that a bud had come out or gone back in. On the Pixel 9a (`CAP-063`, where that channel
  was closed) the Buds did send AVRCP pause commands. The switching itself worked: 5 filmed taps, 5 accepted writes.
- **One list or two?** On the wire there is **one**: the "Customize right" writes are byte-for-byte the same as the "Customize left" writes, and the app's code
  sends no side. Whether the Buds really keep one list is still 🟡 — the step "open Customize right while Adaptive is unticked on the left" was not done
  (Adaptive was re-ticked first).
- **The bit order — settled.** 1 = Noise cancellation, 2 = Off, 3 = **Transparency**, 4 = **Adaptive** (the code was right, the on-screen order was not).
- **In-ear detection on/off on the wire.** The switch writes `qhr` field 2; the Buds confirm the new state themselves in a standard Fast Pair message (SASS
  capability bit 4, "on-head detection is turned on"). With it off: no pause, the Buds still sometimes close the app's channel on wear changes (so OpenControl's
  automatic re-open still matters), and the Buds report no ANC change when a bud comes out. Unknown: whether the "worn" byte (Settable) still drops to `00` when
  in-ear detection is off.
- **What the app can do next:** you approved **ADR-046** (read + write the ANC-mode list, field 12) and **ADR-047** (write in-ear detection, field 2). The next
  FEATURE session builds both, with real `CAP-056` bytes as test fixtures.

## 2. Reading (prompt §0)

- **Read in full:** `AGENTS.md` (in context), `PROJECT_RULES.md`, `PROJECT.md`, `DECISIONS.md` (ADR-001…ADR-045 with every Update), `AI_SESSION_LOG_PROCEDURE.md`,
  the committed `CAP-056` skeleton, `CAP-050-EVENT-NOTES.md`, `CAP-050-FINDINGS.md`, `qht.java`, `hgj.java` lines 55–338, `hgi.java` 1–80.
- **Read in part (the sections the prompt names):** `PROTOCOL.md` (§0, §0.1, §4.1, §4.3 Option F, §4.4–§4.5.9, §6 Framing head and Behavior tail, §8 tail);
  `ai-sessions/INDEX.md` (the new rows); `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group AJ, Group AR, §3, §5, §8, §9 head and the `CAP-056`/`CAP-063` rows);
  `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (`HOLD-*`, `INEAR-*`, `TOUCH-007`, the "no Group yet" list); `REVERSE_ENGINEERING.md` (the `qhr` field table, the `qjg`/`qht`
  entry, `qhs` via `qhs.java`); `id_registry.csv` (ADR/CAP/HOLD/INEAR rows); `ai-sessions/0051` (§5, §15–§20), `0053` (§6–§9), `0054` (§6, §8, §12);
  `CAP-021-FINDINGS.md` (§4, §8), `CAP-024-FINDINGS.md` (§3), `CAP-063-FINDINGS.md` (§3, §6–§8), `CAP-063-EVENT-NOTES.md` (A.8); `ARCHITECTURE.md` (§5a row, UI lines);
  `TODO.md` (the AR/AY/AZ items); `CHANGELOG.md` (latest entries).
- **Not read in full:** `ARCHITECTURE.md`, `PROTOCOL.md`, `TODO.md`, `REVERSE_ENGINEERING.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `TESTPLAN` (sizes 76–392 kB) —
  a deviation from the prompt's "in full", taken to keep the session's context usable; the relevant sections were read, and every claim written was
  re-derived from the capture or the named source lines. `CAP-045-FINDINGS.md` was read only through its section headings and `TESTPLAN`/`CAPTURE` summaries.

## 3. Step mapping (skeleton → what happened)

| Step | Result |
|---|---|
| 1 safeguard | done — "Customize left" on film 17:31:26 before the first toggle |
| 2 Left, one item at a time ≥ 10 s | done differently — untick + re-tick per item (8 taps), 5–6 s apart |
| 3 Right | done differently — same 8-tap pattern, screen on film 17:32:19 |
| A1 untick only Adaptive | done (1815), 5.6 s wait |
| A2 open Customize right with Adaptive unticked | **skipped** |
| A3 re-tick, untick only Transparency, re-tick | done (1830/1843/1863), repeated on the right |
| A4 one write per tap | done, 16/16 |
| W1 OHD on, Right bud out | done differently — pre-state was **off**; switched on first (2173); two removals, one per side of the head; which ear is not identifiable |
| W2 OHD off | done (2849), repeated (4048) |
| W3 repeat W1 with OHD off | done ×2 |
| W4 both out on the table, noise-control screen | done differently — held in the hands, screen not opened; done twice (OHD on and off) |
| W5 OHD on | done (3627), repeated (4344/4361) |

## 4. Phase B tables (summary; full tables in `CAP-056-FINDINGS.md`)

- **Field-12 writes (§1 there):** 16 writes, each a filmed tap, each OK; Adaptive → boolean 4 (1815, 1959), Transparency → boolean 3 (1843, 1991); byte-identical
  to `CAP-021` 5237/5247/5255.
- **Field-2 writes (§3 there):** 2173 (on), 2849 (off), 3627 (on), 4048 (off), 4344+4361 (on) — each a filmed tap on "In-ear detection", each OK, each followed by
  SASS `07 11` flags `b8`/`b0`; 2173/4048 byte-identical to `CAP-024` 1912/1850.
- **Wear changes (§4 there):** 12 rows — with OHD on: phone Paused/Playing 6/6, `Notify` mode `0x40`/`0x08`, field-13 push, Buds `DISC` in 2 of 3 clusters; with
  OHD off: no pause 0/6, no `Notify`, no push, Buds `DISC` in 3 of 3 clusters.
- **Long press:** none identifiable.
- **Clock:** overlay = phone clock ±0.3 s (start and end anchors, same 0.2 s UI lag).

## 5. Checkpoint answers (Phase D, `AskUserQuestion`, chat 2026-09-28 — recorded verbatim)

- Phase 0 — **"Privacy"**: **"Leave unchanged"**. **"Migration"**: **"Yes, do it (Recommended)"**.
- **"Field 12"**: **"Promote both (Recommended)"** — preview: *"§4.5.3 Update (2026-09-28, ai-sessions/0055, maintainer-approved in chat): qht bit order 🟢 FACT —
  1 = Noise cancellation, 2 = Off, 3 = Transparency, 4 = Adaptive. CAP-056 1815/1959 (Adaptive → 4), 1843/1991 (Transparency → 3), 1689/1891 (NC → 1), 1786/1928
  (Off → 2), on film; = qht.java:31 / hgj.java:216-331. The on-screen-order reading is refuted. The checklist ("Press and hold to cycle between the selected active
  noise control modes") is the code's "ANC gesture loop" — 🟢 FACT."*
- **"Lists"**: **"FACT no side, list 🟡 (Recommended)"**.
- **"Field 2"**: **"Promote label + behaviour (Recommended)"** — preview: *"§4.5.5 Update (2026-09-28, ai-sessions/0055, maintainer-approved in chat): "In-ear
  detection" = qhr field 2 — 🟢 FACT. CAP-056 2173/2849/3627/4048/4344 on film (CAP-024 1850/1912 byte-identical); Buds' SASS 07 11 flags b8/b0 = bit 4 "on-head
  detection is turned on" 5/5. With it off (🟢 for CAP-056): no phone pause (0/6 vs 6/6), Buds DISC of DLCI 0x02 still occurs (2923/3260/4168), no Notify on wear
  changes; Settable with it off and not worn 🔴."*
- **"Notes"** (multi-select): **"§6: pause route + DISC trigger, §6: DLCI 0x08 04 05 / 04 16, §6: field-13 push mirrors Notify, §4.1: Settable 00 support"**.
- **"ADRs"** (multi-select): **"D-A field 12 read+write, D-B field 2 write"** → recorded as **ADR-046** and **ADR-047**, registered in `id_registry.csv`.
- **"Next"**: **"Field 12 list + field 2 switch (Recommended)"**.

## 6. Documentation applied (only what was approved)

- `PROTOCOL.md`: §4.5.3 Update (bit order, name, no side field 🟢; one list 🟡), §4.5.5 Update (label 🟢, off-behaviour 🟢 for `CAP-056`), §4.1 Update (Settable
  support, 🟡), §6 Behavior ×3 new items, §8 changelog row.
- `DECISIONS.md`: ADR-019 Update (fields 12 and 2 names), ADR-024 Update, pointer Updates on ADR-036 and ADR-045, new **ADR-046**, **ADR-047**.
- `id_registry.csv`: ADR-046, ADR-047 added; ADR-045 row notes the field-12 supersession; `CAP-056` → 2026-09-28, analyzed.
- `CAPTURE_BLUETOOTH_HCI_SNOOP.md`: Group AR "Captured" note; Capture Index row. `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`: `HOLD-005`, `INEAR-001`…`004` evidence and Group AR;
  the `INEAR-002`/`003` "no capture" item closed. `REVERSE_ENGINEERING.md`: `qht` entry Update. `ARCHITECTURE.md` §5a: ADR-046/047 named (not built). `TODO.md`:
  AR and in-ear-ADR items done, next FEATURE item, open items. `CHANGELOG.md`, `ai-sessions/INDEX.md` (0055 row), `_sidebar.md` (renamed path).
- **Edits to other session records, disclosed:** `ai-sessions/0055_CAPTURE_PROMPT_2026_09_28.md` line 28 — the skeleton path now names the placeholder folder and the
  renamed one in brackets (lint's dead-reference check fails on the newest session pair otherwise). `ai-sessions/0054` left as written (history; lint reports it
  as informational).
- `README.md`: not changed (no app change).

## 7. External sources (fetched 2026-09-28)

- developers.google.com/nearby/fast-pair/specifications/extensions/sass — Message Group `0x07`, code `0x11` "Notify capability of Audio switch", octets 4–5
  version ("Current version (with the security enhancement) code is 0x0102"), octets 6–7 flags; *"Bit 3: 1, if this device supports on-head detection (even if on-head
  detection is turned off now); 0, otherwise"*; *"Bit 4: 1, if on-head detection is turned on; 0, otherwise (does not support on-head detection or on-head detection is
  disabled)"*.
- support.google.com/googlepixelbuds/answer/9642984 ("Manage in-ear detection") — *"If your Pixel Buds detect that you've taken them out, your current audio
  selection will automatically pause. When you put them back in, the audio will automatically resume."*; Pro 2: "More settings" → "In-ear detection".

## 8. Draft outline for the next FEATURE prompt

1. Reading per `AGENTS.md` §0.1; this RESULT; `CAP-056-FINDINGS.md` §1–§4 and §8; ADR-046, ADR-047.
2. Build under ADR-046: `SettingsCodec` encode/decode of field 12 (four booleans, always all four), `Maestro.READABLE_FIELDS` + 12, read at Connect, a
   "Modes for press and hold" list on "Controls" shown once for both buds, never fewer than two selected; fixtures `CAP-056` 1689, 1725, 1786, 1815, 1843 (writes),
   1531 (read), 1697 (`RESPONSE` OK).
3. Build under ADR-047: the "In-ear detection" switch (write `4:{2:0|1}`), fixtures `CAP-056` 2173/4048 (ch 19), 2849/3627 (ch 21), read 1502; the note on what
   "off" changes (no pause; the "only while worn" ANC check may not apply).
4. No new wire request beyond these two writes and the field-12 read; nothing on DLCI 0x08.
5. Gate: `:data`/`:domain`/`:hardware` tests, lint, mutation checks (bit order swapped 3↔4 must fail; a write with fewer than two modes must be refused).
6. Add to the `CAP-064` (Group AZ) skeleton: untick Adaptive + three long presses on film (`Notify` 08/20/80 only); in-ear detection off via OpenControl → SASS `b0`,
   a bud out → no pause, an ANC Refresh with no bud worn (Settable?); on a Pixel 7a run, "open Customize right while a mode is unticked on the left".

## 9. Open items

- 🔴 One list or two on the Buds (A2); what triggers the Buds' `DISC` of DLCI 0x02; Settable with in-ear detection off and no bud worn; the pause route with GSND
  closed; DLCI 0x08 `04 05`/`04 16` meaning; which ear each removal was; the official app's version and the Pixel 9a's state (not on film).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0055_CAPTURE_RESULT_2026_09_28.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0055_CAPTURE_RESULT_2026_09_28
