# 0071_CAPTURE_RESULT_2026_10_04.md — Full analysis of CAP-069 (Group BE: the official Pixel Buds app 1.0.990706425 on the Pixel 7a)

**Number:** 0071
**Category:** CAPTURE
**Date:** 2026-10-04
**Title:** Full analysis of CAP-069 (Group BE: the official Pixel Buds app 1.0.990706425 on the Pixel 7a — head gestures, Multipoint, assistant hold, a tap on the current ANC mode, EQ Default, wear states, Find)
**Status:** awaiting maintainer sign-off — analysis complete, approved documentation edits applied, committed and pushed on `capture/0071-cap-069` (PR open)

## Progress

- **Done:** reading; Phase 0 (branch `capture/0071-cap-069` from `origin/main` = `0378076`; files identified; privacy check; folder renamed); Phase A (film, every
  2 s + 4-fps narrowing; clock); Phase B (every Buds packet class); Phase C (`CAP-069-EVENT-NOTES.md`, `CAP-069-FINDINGS.md`); Phase D (two checkpoint rounds; the
  approved edits; footers; lint).
- **Next:** none in this session; the next session back-fills the documentation commit's hash.
- **Touched, unverified:** none (each count in the documents was re-derived in this session; the counts behind two checkpoint previews were corrected after the
  question — see "Checkpoint answers").
- **Intermediate results:** session scratchpad only (contact sheets, 4-fps sheets, the film notes, the pw_rpc listing, the Message-Stream/GSND message lists); re-create
  with the commands in `CAP-069-FINDINGS.md` if gone.

## Plain-language answers

- **What works and what does not in the official app (1.0.990706425):** every setting change was accepted by the Buds (26 writes, 26 OK; 4 ANC changes, 4 ACK).
  **Find device is gone from the app** — the maintainer's statement is confirmed on film: no screen offers it. Find Hub's *Play sound* for Left and Right said
  "Can't play sound" and put nothing on the local Bluetooth link. The app's ANC button moves by itself when the Buds change mode on a wear change (Buds behaviour).
- **Head gestures:** the switch writes `qhr` field 29 — **1 = off, 2 = on** — 6 filmed taps, 6 writes, 6 OK. Promoted to 🟢 FACT; **ADR-052** unblocks read + write.
  The GSND CONTROL Code `0x16` value is the inverse (01 = active) and also changes with wear (🟡).
- **Multipoint:** the switch writes field 11 (0/1), 4 filmed taps; the Buds' SASS capability byte flips `b8`/`98` = SASS "Bit 2: multipoint current state". SASS
  bit promoted to 🟢 FACT; **ADR-053** unblocks the write. Build order: Multipoint first (maintainer).
- **Assistant hold:** three assistant sessions; each starts with the Buds' GSND message `01 09 00 03 0a 01 03` and the audio channel 2 ms later; a 43.5-s quiet
  window had no wave. The hold itself is **not on film** (bud out of view) — the trigger link stays 🟡.
- **`ANC-006`:** the official app sends **no** `Set` when the selected mode is tapped (0 of 3; the 4 real changes are the positive control). The maintainer chose
  to **keep** OpenControl's `Set` (ACKed in `CAP-068`).
- **EQ Default:** `0.0 × 5`, byte-identical to OpenControl's "Flat"; the maintainer keeps the name "Flat". One sample of field 18 written right after **Save** (🟡).
- **Wear states:** GSND Code `0x05`: 3 = no bud worn, 4 = one, 6 = both (🟡, 15 samples); 5 unclear. Settable `00` only with no bud worn (5/5). The charging bit
  can lag a removal by ≈ 12 s. Ears were never on film.
- **Find:** no ring was possible → `FIND-005` stays untested; OpenControl's own Find is unaffected (it sends its own command, `CAP-068` §6).
- **New in this app version (wire):** an unnamed MAESTRO service `0xbf6c9399` (a stream while the device screens are open) and `maestro_pw.JitterBuffer` — never
  seen before; the Buds send a Find Hub Network identifier message (`03 0b`). A follow-up to pull and decompile the APK is approved.
- **Improvements for OpenControl, in order:** (1) Multipoint switch (ADR-053); (2) head-gestures switch (ADR-052); (3) optional "changed by the Buds" note when the
  mode changes without a tap; (4) careful wording of "charging in the case" (lag); (5) test `FIND-005` with OpenControl's own Ring; (6) field 18 / Save only after
  `CAP-053`. Not changed: `ANC-006` (keep the `Set`), "Flat" (keep the name). Details: `CAP-069-FINDINGS.md` §12.

## Phase 0 — set-up

- `git log -1`: `0378076 docs: auto-regenerate sidebar [skip ci]`; branch was `main`; `git fetch` → `HEAD..origin/main` empty. Branch `capture/0071-cap-069`
  created from `origin/main` with the untracked files carried over.
- `git status --short` at start: ` M ai-sessions/INDEX.md` (the 0071 row), `?? ai-sessions/0071_CAPTURE_PROMPT_2026_10_04.md`, `?? …/CAP-069-btsnoop_hci.log`,
  `?? …/CAP-069-recording.mp4`. The skeleton was unchanged in the working tree. `id_registry.csv` line 121 `CAP-069,capture,planned`. `git check-attr filter`:
  film and log `lfs`, notes `unspecified`. Only `CAP-069-recording.mp4` exists (no "recoding" file).

| File | Measured |
|---|---|
| `CAP-069-recording.mp4` | 987,545,073 bytes, sha256 `c4f46782…6004`; H.264 1280×720, rotation −90, 38,863 frames, 1,297.98 s, ≈ 29.94 fps; `creation_time` 2026-10-04T14:27:07Z (= the film's end, overlay 16:27:07); the audio track has no sample table — **no samples** |
| `CAP-069-btsnoop_hci.log` | 763,068 bytes, sha256 `45d0aa9f…fda1`; `H4 with linux header`; 10,582 packets; 16:05:56.024618 – 16:30:06.677298; frame 1 = `Sent Reset` (Bluetooth on) |
| Not present | no `.log.last` (the pre-toggle buffer), no system log, no app logcat, no bugreport, no events file — nothing in the analysis needed them except the pre-toggle part (no Buds activity of interest happened before the toggle on film) |

**Clock:** phone = overlay + 0.43 s (± 0.03) at both ends (status-bar minute flips at 30 fps against the overlay second; `CAP-069-FINDINGS.md` §0).
**Privacy:** SSID, Wallet card digits, "Utrecht", first name in device names, launcher and Play Store search history, the Find Hub map with a past location —
maintainer: **"Commit as is"**. **Migration:** `captures/CAP-069-2026-10-04_16-05-29_16-27-07-Group_BE/` (overlay first/last), `mv` with a checksum check (3 × OK),
film `chmod 644` — maintainer: **"Approve as shown (Recommended)"**.

## Step mapping (summary; full table in `CAP-069-EVENT-NOTES.md`)

P1 not identifiable · P2, P4, P5, P7 done · P3, P6 done differently · BE-1 done · BE-2…5 done (order 3, 2, 5, 4) + one extra pair · BE-6…8 done · BE-9 done ·
BE-10/11 done differently (not on film) · BE-12 done + an extra hold · BE-13…16 done (+ one repeat) · BE-17 done early + repeated · BE-18 done differently (Last
saved, not Balanced) · BE-19 done differently (link drops when both docked) · BE-20…27 done · BE-28 could not be run as written (Find Hub instead) · BE-29
skipped. Not in plan: the field-12 Off tick (not undone), EQ drags + Save, Find Hub, Play Store.

## Phase B tables (summary; full tables in `CAP-069-FINDINGS.md`)

- **Settings writes:** 26 (`4:{12}` ×1, `4:{16}` ×12, `4:{18}` ×1, `4:{29}` ×6, `4:{11}` ×4, `4:{7}` ×2 = 26, counted 26 REQUEST / 26 RESPONSE OK by
  `scripts/pwrpc_decode.py`); 0 `SERVER_ERROR`; 5 phone `CLIENT_ERROR CANCELLED` (stream cancellations).
- **Message Stream:** 4 claims, each opened by `03 08 00 02 01 25` (Play services) — 577, 6416, 6836, 8231; ANC `{'get': 4, 'notify_00': 5, 'notify_e8': 14,
  'set': 4, 'ack': 4}` (`scripts/message_stream_tally.py -v`); SASS `07 11` flags `b8`/`98`; no Group `0x04`.
- **Session ends:** Buds `DISC` MAESTRO + MS at a bud out of an ear (6157/6158, 16:19:36.36); ACL drops `0x13` at both docked (6468, 7658); Buds-initiated
  reconnects (6580, 7759).
- **GSND AUDIO:** 4 bursts (258, 221, 7, 256 frames), all inside assistant sessions.

## Checkpoint answers (verbatim option labels, chat 2026-10-04)

| Question | Answer |
|---|---|
| Privacy | "Commit as is" |
| Migration | "Approve as shown (Recommended)" |
| Field 29 | "FACT + ADR-052 (Recommended)" — preview text applied (`PROTOCOL.md` §4.5.4 Update, ADR-052) |
| Field 11 | "FACT + ADR-053 (Recommended)" — preview text applied (`PROTOCOL.md` §4.5.2 Update, ADR-053) |
| ANC-006 | "Keep the Set" |
| EQ Default | "Record only, keep 'Flat'" |
| Find | "Dated note, status 🟡 (Recommended)" |
| Wear table | "Desk entry + §6 🟡 (Recommended)" |
| New APK | "Yes, TODO + APK_VERSIONS line (Recommended)" |
| Order | "Multipoint first" |

Corrections after the questions (re-derived before writing): the "Wear table" question said Code `0x05` = 3 in **7** samples; the list is **6** (694, 6326,
6875, 7517, 7615, 8352) — the documents say 6 (15 samples in all). The `ANC-006` and "EQ Default" answers chose app behaviour; the corresponding `PROTOCOL.md`
§4.1/§4.2 dated Updates record the observation (🟢 for `CAP-069`) together with that choice — no status of an existing entry was changed.

## Documents changed (only after the answers above)

`captures/CAP-069-…/CAP-069-EVENT-NOTES.md` (rewritten, skeleton as Appendix A), `CAP-069-FINDINGS.md` (new); `PROTOCOL.md` (§4.1, §4.2, §4.4, §4.5.2, §4.5.3,
§4.5.4 dated Updates; §6 new item); `DECISIONS.md` (ADR-052, ADR-053; ADR-049 supporting Update); `id_registry.csv` (`CAP-069` analyzed; ADR-052/053);
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group BE run note, Capture Index row, folder path); `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (evidence pointers for 13 Test-IDs);
`DESKRESEARCH_FINDINGS.md` (entry 2026-10-04); `reverse-engineering/APK_VERSIONS.md` (1.0.990706425 used, not pulled); `TODO.md`; `README.md` (62 analyzed / 5
planned); `ai-sessions/INDEX.md`; `_sidebar.md` (folder path); `ai-sessions/0071_CAPTURE_PROMPT_2026_10_04.md` (folder path, with a note). Nothing under
`android/`, `dist/` or `scripts/` changed (`scripts/__pycache__/lint_docs.cpython-314.pyc` unchanged — every script run with `PYTHONDONTWRITEBYTECODE=1`).

**Checks:** `python3 scripts/ensure_footers.py` → "all footers already up to date". `PYTHONDONTWRITEBYTECODE=1 python3 scripts/lint_docs.py` → **exit 1** with
63 "dead filename reference" lines, all in older `ai-sessions/` files (0010…0067; e.g. placeholder capture paths of earlier skeletons) — the same 63 lines as on the
base tree (run with the changes stashed; that run also listed this RESULT's own line); this session's own lines (a scratch filename in this RESULT) was removed. The lint is therefore not
clean on `origin/main` either — reported, not fixed (history files).

## External sources (raw text fetched 2026-10-04 with `curl`)

- https://developers.google.com/nearby/fast-pair/specifications/extensions/sass — *"Bit 2: multipoint current state 1, if multipoint is on 0, otherwise"*.
- https://developers.google.com/nearby/fast-pair/specifications/extensions/deviceinformation — *"0x0B: Current FHN ephemeral identifier message 0x0018:
  Additional data, length 24 or 36 bytes"*.

## Files read

In full: `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `DECISIONS.md` (ADR-001…051 with every Update), `TODO.md`, `AI_SESSION_LOG_PROCEDURE.md`, the prompt, the
committed skeleton. In part (the sections the prompt names): `PROTOCOL.md` §0–§0.1, §2.2a–§2.3, §4.1, §4.2 (preset and Flat parts), §4.3 Option F, §4.4,
§4.5.1–§4.5.4 (§4.5.5 and §6 only by search); `ARCHITECTURE.md` §3.1, §5a; `CAP-056-FINDINGS.md` (header and section list), `CAP-056-EVENT-NOTES.md` (section
list); `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group BD/BE notes, index rows); `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, `id_registry.csv`, `DESKRESEARCH_FINDINGS.md`,
`reverse-engineering/APK_VERSIONS.md`, `README.md` (the rows/lines changed). **Not read:** `ARCHITECTURE.md` §5/§6.0b/§8.1 in full, `CAP-068` EVENT-NOTES/FINDINGS
and `ai-sessions/0069`/`0070` RESULTs beyond what `PROTOCOL.md`/`TODO.md` quote, `CAP-019`/`020`/`021` FINDINGS (their frames were re-derived from the logs instead).

## Deferred documentation

Each item is in `TODO.md` with this wording's substance:

- `ARCHITECTURE.md` §5a and `PROJECT.md` ("Status after 1.0.x", the feature list) still describe head gestures and Multipoint as waiting for `CAP-069`; update
  with the app session that builds the switches (`TODO.md` §4).
- Pull and decompile the official app 1.0.990706425 — name `0xbf6c9399` and `JitterBuffer` `0x8d99df93` (`TODO.md` §4).
- `CAP-069` leftovers: the lagging charging bit, the GSND AUDIO re-open on ACL 3, `03 0b` length 25, field 18 after Save (`TODO.md` §4).
- **M**: the Buds were left with a custom EQ and Off ticked in the press-and-hold mode list (`TODO.md` §4).
- `lint_docs.py`'s 63 pre-existing dead references in older `ai-sessions/` files — not added to `TODO.md` (history files are left as written; the maintainer may
  decide on a lint exception).

## Commits

The maintainer said "yes" to commit, push and a pull request (chat 2026-10-04). On branch `capture/0071-cap-069`:
- `e16aeef` chore(capture): add CAP-069 film and HCI log (Group BE, official app 1.0.990706425) — LFS objects `c4f4678…` (film), `45d0aa9…` (log).
- The documentation commit that carries this file (its hash is completed by the next session, `AI_SESSION_LOG_PROCEDURE.md` §4b item 2).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0071_CAPTURE_RESULT_2026_10_04.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0071_CAPTURE_RESULT_2026_10_04
