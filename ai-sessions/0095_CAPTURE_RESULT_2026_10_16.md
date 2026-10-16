# 0095_CAPTURE_RESULT_2026_10_16.md — Full analysis of CAP-077 (Group BM: the release-signed 1.3.1 build `7aa524b`, the fixes of `ai-sessions/0094`, in the secondary user without Google Play)

**Number:** 0095
**Category:** CAPTURE
**Date:** 2026-10-16
**Title:** Full analysis of `CAP-077` (Group BM): one screen recording (no audio), two HCI snoop logs, one app debug export, one app logcat, a full logcat and an adb shell log — the update 1.3.0 → 1.3.1 on film, secondary user without Play
**Status:** complete (the maintainer's verdict and approvals in chat 2026-10-16; committed and pushed on `release/1.3.1`; 1.3.1 published 2026-10-16)

## Summary (plain language first)

**What happened on the phone.** In the secondary user without Google Play the app was still 1.3.0 (build `ff27ee1`) at the start of the 14-minute screen recording
(no sound). The maintainer connected, switched Bluetooth off and on, looked at Settings, Info and the tabs, ticked a mode on Controls, then updated to the
release-signed 1.3.1 (`7aa524b`) from the laptop on film. In 1.3.1: "Use different Buds" with the new dialog → Forget, *Pair a device* (Android's dialog) and Connect;
a bud out of the case closed the app's channel and the app re-opened it by itself; six taps on the Quick Settings tile, five on the noise-control tab, eight
press-and-holds; every switch on Controls, Sound and Settings off and on; Ring Left/Right; two pulls. The installed APK is the tested build (SHA-256 checked).

**Did every hold show?** Yes — **8 of 8**, each on screen 6–20 ms from the Buds' push (measured on the film's own frames), with "Changed by the Buds at 07:03:58" in
the (i). But there was **no music playing** during the run (AVRCP said Stopped, the A2DP stream was suspended during all eight holds), so "during playback" is not
what the wire shows.

**Which 1.3.1 fixes were verified.** "Use different Buds" (asked first; afterwards no connection by name until a new association — and "removed 0 association(s)"
is right: user 10 had none since 2026-10-04), the tile taps (6 of 6 answered, none silent), one name per mode everywhere ("Heavy bass" too), the new log word
"(unchanged)". **Not exercised:** the kept "changed since" line after a reconnect, a push during a read, TalkBack, Cancel in the dialog — the maintainer did not run
those steps; their unit tests stand.

**What was unexpected.** For five settings each write was echoed three times by the Buds (one time for the others) — harmless, recorded as an open question. The Buds
switched the mode to Adaptive by themselves when the buds left the case, with in-ear detection off. Nothing else: the same seventeen requests as 1.3.0, byte for
byte, 23 writes all acknowledged, no crash, no wrong write, no Safe Mode, no Play traffic.

**What the maintainer decided (chat 2026-10-16, verbatim in "Checkpoints"):** release 1.3.1 as built; an ADR-065 Update and a `PROTOCOL.md` §6 note written; no
FEATURE prompt (the two small proposals in `TODO.md` §5); commit and push. **Next for the maintainer:** `RELEASING.md` C4 tick and D1–D4 (merge, tag, publish) — never by
this session.

## Progress

- **Started** 2026-10-16. Branch `release/1.3.1` at `0159376` (the 0095 prompt commit, after the build commit `7aa524b`); `git fetch`: nothing new on
  `origin/main` or `origin/release/1.3.1`; PR #64 open ("Release 1.3.1"). `git status --short`: the seven untracked capture files only.
- **Scratch directory:** /tmp/claude-1000/cap077.FxHE (scripts, contact sheets, decoded tables, notes).
- **Reading done (Phase 0):** AGENTS.md, PROJECT_RULES.md, PROJECT.md, AI_SESSION_LOG_PROCEDURE.md and DECISIONS.md (ADR-001 … ADR-065 with every
  Update, the draft ADR-060) in full; ARCHITECTURE.md §3.1, §3.2, §5a, §6.0b, §12 in full and §9's "Use different Buds" paragraph; PROTOCOL.md §2.3's table,
  §4.1 (head, the Notes/Updates/Correction of 2026-10-07 … 2026-10-15); TODO.md §1, §2, §5, §6; the CAP-077 skeleton in full.
- **Phase 0 step 2 done:** the folder listed fresh (`ls -la`, `sha256sum`) — the eight files and their first eight SHA-256 hex digits equal the prompt's §2
  table, modes as stated (three files 740: the debug export, the app logcat, the recording). **Difference:** none besides the one the prompt already names —
  no CAP-077-System-log file exists (the maintainer, Checkpoint 0: not made).
- **Checkpoint 0 answered** (chat 2026-10-16, verbatim in "Checkpoints" below).
- **Phase 0 done.** Originals copied with `cp -a` to ~/opencontrol-capture-originals/CAP-077/ (sorted `sha256sum` lists of source and copy `diff` → identical);
  CAP-077-sha256sums.txt written into the folder (the hashes as received); `chmod 644` on the three 740 files. **Phone checks (07:42, read-only; output
  verbatim in CAP-077-adb-checks-0095.log, copied into the folder):** `am get-current-user` → 10; `pm path --user 10` → one `base.apk`; the pulled APK's
  SHA-256 **= `9be3a89c…0ee0a` = `~/opencontrol-1.3.1-tested`'s**; `versionCode=10301`, `versionName=1.3.1`, `lastUpdateTime=2026-10-16 06:58:05` (no install
  after the run); `pm list packages --user 10 | grep -E "com.google.android.gms$|com.android.vending$"` → nothing, exit 1; the same in user 0 (positive
  control) → both packages, exit 0. `id_registry.csv`'s CAP-077 row already reads `planned`.
- **Phase A, film reviewed at 1 fps (done):** 864 frames decoded in three ranges (f1/, scale 360 px); per-second difference below the status bar
  (bin/diff.py → diff1.txt): **294 seconds** with a pixel change |Δ| > 25 viewed on 59 labelled contact sheets (sheets/sh000…sh058, 5 frames each, with
  t = 0) — the other **569 seconds** are unchanged (max |Δ| ≤ 25). Notes: notes_film.md in the scratch directory. **Clock:** the recording is variable
  frame rate (10,747 frames, `r_frame_rate=1/1`); the first native frame showing the new minute is at t = 45.179, 105.187, 165.186, 225.188, 405.190,
  585.223, 825.175 s (bin/flip2.sh, `showinfo` pts) ⇒ **phone time = 06:56:14.82 + t** (spread 0.05 s, no drift; the clock redraw is itself a frame, so ±0.05 s);
  the film covers **06:56:14.8 – 07:10:38.4**; `creation_time` 07:10:38 is its end. `ffprobe` shows one video stream and **no audio stream**.
- **Phase A, native-frame checks:** the six tile taps at the film's native frame rate (bin/native.sh, tile/, tileA.png): touch → new subtitle 1.13–1.34 s
  (0.35 s for tap 2, on the still-open claim).
- **Phase B, HCI logs (done):** handles from the Connection Complete events (`bluetooth.addr` matches 0 frames in both logs; the handle filter matches 286 /
  1,815 frames); scripts/pwhdlc_crc_table.py → rpc_*.tsv (35 + 320 pw_hdlc frames, **0 CRC failures**, equal to `pwrpc_decode.py --handle 0x000b`; CRC
  positive control: the CAP-051 1177 reference passes, one flipped bit fails); scripts/message_stream_claims.py → claims_*.tsv (1 + 20 claims, all the
  app's); 0 Play markers (positive control CAP-076 hci1 → 18); 0 frames on DLCI 0x08/0x0a (positive control CAP-029 → 109); AVDTP/AVRCP listed; the LE
  handle 0x0040 of the .last log is a GATT peripheral with the Heart Rate service (not the Buds). Every non-write request byte-identical to CAP-076's.
- **Phase B, export and app logcat read (done):** export 706 lines = 182 event lines (all read) + 191 pw_rpc summaries + 333 received-bytes hex lines (178
  DLCI 0x02, 155 DLCI 0x04; matched to the wire by script). App logcat: PID 25520 (1.3.0) 21 lines, PID 28176 (1.3.1) 459; FATAL/ANR/StrictMode 0
  (positive control: the tag OpenControlBuds 24). One SIGQUIT (signal 3) at 07:10:55 — "Wrote stack traces to tombstoned" (source TBD in the full logcat).
- **Phase B, adb shell log read; phone re-check:** `dumpsys companiondevice` (read-only, 07:56): user 10's association mId=51 was created 07:00:24 by the
  run; user 10's previous one (mId=48) was removed 2026-10-04 15:22:08 (pkg-data-cleared) ⇒ "removed 0 association(s)" is correct; summarised in the
  checks log (no address).
- **Phase B, full logcat (done):** process story, the update (adb install 06:57:57), the user switch, the CDM lines, the Bluetooth process; CAP-077-logcat-extract.txt
  written (22,441 lines; address and serial hex redacted; command in its header). The SIGQUIT at 07:10:55 is after L's end (07:10:32) — sender unknown.
- **Phase C (done):** CAP-077-EVENT-NOTES.md (timeline added, skeleton kept as the appendix) and CAP-077-FINDINGS.md (§0–§15) written; the holds re-measured on
  native frames (8 of 8, 6–20 ms); external validation fetched by a web-fetch sub-agent (pw_rpc packet.proto verbatim; AOSP CompanionDeviceManager.java partly
  verbatim; the Bluetooth SIG error-code page not readable — the names stay Wireshark's, labelled ⚪).
- **Phase D (done):** Checkpoint D answered (below). Folder renamed to captures/CAP-077-2026-10-16_06-56-14_07-10-38-Group_BM, the recording to
  CAP-077-recording1.mp4; the full logcat removed from the folder after its hash was matched against the kept original (~/opencontrol-capture-originals/CAP-077/).
  Updated: `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (the `CAP-077` row, Group BM status), `id_registry.csv` (analyzed), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (BM on 16 more
  exercised Test-IDs), `APP_TESTPLAN.md` §Z (results, run note), `TODO.md` §1/§2/§4/§5, `ARCHITECTURE.md` §5a, `DECISIONS.md` ADR-065 Update, `PROTOCOL.md` §6 note +
  new ❓ item + §8 row, `CHANGELOG.md` [1.3.1] (the hardware line; Known issues unchanged), `PROJECT.md` status, `ai-sessions/INDEX.md` (0094 back-fill, 0095).

## Checkpoints

**Checkpoint 0 (chat 2026-10-16, `AskUserQuestion`, answers verbatim):**
- "Map-naam" → *"Hernoemen na Phase A (Recommended)"* (the folder to the film's first/last phone times, the recording to CAP-077-recording1.mp4).
- "chmod/sha" → *"Beide (Recommended)"*.
- "Privacy" → *"Film zo; logcat-extract (Recommended)"* (the film committed as is; CAP-077-logcat-extract.txt committed, the full logcat kept local).
- "System log" → *"Niet gemaakt — vastleggen als niet geleverd (Recommended)"*.
- "APK pull" → *"Ja, read-only (Recommended)"*.
- "Originelen" → *"Ja, kopiëren (Recommended)"*.

**Checkpoint D (chat 2026-10-16, `AskUserQuestion`, answers verbatim):**
- "Verdict C4" → *"Release as built (Recommended)"*.
- "Notities" → *"ADR-065 Update + §6-notitie (Recommended)"*.
- "FEATURE" → *"Nee, in TODO §5 (Recommended)"*.
- "Commit" → *"Ja, commit + push (Recommended)"*.

**After the checkpoint (chat 2026-10-16):** the first push failed — GitHub: "This repository exceeded its LFS budget." The maintainer: "Ik kan het budget niet
verhogen, pas de commit aan, houdt de grote bestanden lokaal." The commits were rebuilt without the film; the second push failed the same way
(LFS refuses every upload, also the ≈ 4.3 MB of logs). The maintainer (`AskUserQuestion` "LFS geblokt"): *"Alleen .md committen, data lokaal (Recommended)"* — only
the two notes and the documents are committed; every CAP-077 data file stays local (the folder and ~/opencontrol-capture-originals/CAP-077/), ignored by
`.gitignore`, to be committed when LFS accepts uploads again (`TODO.md` §6).

**Continuation (chat 2026-10-16, after the release):** the capture storage was settled by **ADR-066** (maintainer-approved, `AskUserQuestion` "ADR-066" → *"ADR-066
zoals getoond (Recommended)"*, "Regel 18" → *"Ja, gedateerde verwijzing (Recommended)"*): from `CAP-077` on, files of at most 5 MB are ordinary git files, films and
full logcats stay local. `CAP-077`'s data files (≈ 4.3 MB) were committed under it; the film stays local.

## Files read

**In full:** `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `AI_SESSION_LOG_PROCEDURE.md`, `DECISIONS.md` (ADR-001 … ADR-065 with every Update, the draft ADR-060), the
CAP-077 skeleton, the adb shell log, the export's 182 event lines (its 191 pw_rpc lines and 333 hex lines matched to the wire by script), the app logcat's
app/lifecycle/Bluetooth lines (the rest by tag census: `input_focus`, `viewroot_draw_event`, `ImeTracker`, `InsetsController` …), `APP_TESTPLAN.md` §Z, `CHANGELOG.md`
[1.3.1], `RELEASING.md` C1–C4 and the 1.3.0 Release-log row, PR #64's body. **In part:** `ARCHITECTURE.md` (§3.1, §3.2, §5a, §6.0b, §12 in full; §9's "Use different
Buds" paragraph); `PROTOCOL.md` (§2.3's table, §4.1 head and its Notes/Updates of 2026-10-07 … 2026-10-15, the §6 wear-state item, §8's last rows); `TODO.md` (§1, §2, §4
head, §5, §6); `ai-sessions/0094` RESULT (the ledger rows A93-APP-01 … 08 and the mutation table); `CAP-076` FINDINGS (header, In short, §0 head, the section list) and
EVENT-NOTES (header, metadata, pre-flight, review method, first timeline rows); `BudsRepositoryImpl.kt` (the cause rules, `applyAncState`, the log strings — lines 260–460
and the grep of every log string used); `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the rows touched). **Not read:** the `ai-sessions/0093` AUDIT RESULT (326 KB — its findings
taken from the 0094 ledger), `CAP-071`'s FINDINGS/EVENT-NOTES (the `CAP-076` files, which follow them, were used as the layout), `BudsCompanionPairing.kt`,
`AncTileService.kt`, the `:ui` sources (the run's evidence for them is the film; no finding rests on their code), the full logcat beyond the stated filters.

## Deferred documentation

Each also in `TODO.md`:
- "**1.3.1 fixes not exercised in `CAP-077`** … BM-3 TalkBack …; BM-4 two holds while disconnected …; BM-6 a hold within ≈ 1 s of Connect …; BM-7 … Cancel; the dialog
  across a rotation." (`TODO.md` §2)
- "❓ `CAP-077` …: writes of fields 4, 11, 15, 27, 28 echoed by three stream packets …" (`TODO.md` §4)
- "**After `CAP-077` …** P1 log the EQ write …; P2 the tile's one- or two-frame "unavailable" look …; ❓ the SIGQUIT …" (`TODO.md` §5)

## Commits

- `302b521` docs: CAP-077 (Group BM) analysed — notes, findings, registers; ADR-065 Update, PROTOCOL.md §6 note; ai-sessions/0095 (on `release/1.3.1`; two earlier
  local attempts with the capture data were not pushed — Git LFS refused them).
- PR #64 merged by this session at the maintainer's request (chat 2026-10-16, "merge de PR en maak de release"): merge commit `67efae9`; signed tag `v1.3.1` on
  `7aa524b`; release published 2026-10-16T06:50:17Z. D4 by the maintainer (Obtainium).
- `RELEASING.md` E1–E4: branch `release/1.3.1-after` (this commit), its own pull request, merged at the maintainer's request.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0095_CAPTURE_RESULT_2026_10_16.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0095_CAPTURE_RESULT_2026_10_16
