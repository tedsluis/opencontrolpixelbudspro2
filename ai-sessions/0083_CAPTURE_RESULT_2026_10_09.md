# 0083_CAPTURE_RESULT_2026_10_09.md — Full analysis of CAP-072 (Group BH: the 1.2.0 release APK installed over 1.1.1 in the GrapheneOS user without Google Play services — the release run of 1.2.0; film 1 of two, BH-25/BH-26 pending)

**Number:** 0083
**Category:** CAPTURE
**Date:** 2026-10-09
**Title:** Fully analyse `CAP-072` (one camera film without sound, one screen recording with sound, four HCI snoop logs, four app debug exports, one app logcat, one full system log, the P0/P1 shell log), record the real events and the findings, investigate the case-sound and press-and-hold observations, give the release verdict for 1.2.0 (`RELEASING.md` C4) and propose shorter hardware runs; no app change, no new skeleton, no publishing
**Status:** complete (closed 2026-10-10 by `ai-sessions/0084`: every checkpoint was answered in chat 2026-10-10; committed and merged — "Commits" below)

## Summary (plain language)

**The verdict (the maintainer, chat 2026-10-10): fix first.** Nothing heavy went wrong — but the press-and-hold display, one of 1.2.0's five new things, cannot
work as announced. 1.2.0 is rebuilt with `DECISIONS.md` **ADR-061** (the app keeps its noise-control channel open while the noise-control tab is on screen),
then a short hardware run; nothing is published until then. Film 2 is not made on this build; its items go into that run.

**The five 1.2.0 items:**
1. *"Changed by the Buds"* — works only when the Buds report a change within ≈ 1.5 s of an app action (seen once, 17:51:10, when the buds left the case);
   **a press-and-hold between actions is never shown** (below).
2. *Serial numbers* — work: one request per connection, byte-identical to the official app's, 29 of 30 answered (one cut off by the Buds, not retried), shown
   Case / Right bud / Left bud, the same strings every time.
3. *"Probably worn"* — works as specified; the Buds themselves said "allowed" for 42 s with both buds on a table (the known limit, longer than before).
4. *Case sounds on gear → Settings* — work (card, (i), writes, read-back).
5. *Conversation detection on Controls* — works.

**Case sounds — your observation.** Every "Earbuds replaced" write was confirmed by the Buds 6–35 s before the next bud went into the case, and every later
read — also by the other bud — returned what was written; there is no phone ↔ case connection on the wire, and the session drop when the Left leaves your ear
does not touch the stored setting. On the phone's microphone a faint short two-tone appears when the **Right** bud is put back with the switch on, not when the
**Left** is, and not with it off (one faint candidate). So: the app sets the switch correctly; what the case/Buds play varies, possibly per bud (🟡). An
experiment with a microphone at the case would settle it (in `TODO.md`).

**Press-and-hold — why the app did not show it.** By ADR-032 the app opens its noise-control channel only for ≈ 1.5 s after a tap, a pull, the tile or a connect.
From 17:44:34 to 17:48:46 — when the noise-control tab was up and you pressed and held — no channel was open, and in this user (no Play services) nobody else
holds it, so the Buds' report of the change had nowhere to go. The feature works inside a channel window (17:51:10). Fix: ADR-061.

**The incoming call** (18:02:25–18:04:59) was answered and ran on the Buds over eSCO; the app's session stayed open and untouched; the next write worked.
Do Not Disturb was not on. The caller's number is blurred in the committed screen recording and in no document.

**Compared with 1.1.1:** nothing changed outside the five items — the same reads, the same write bytes, the same claim and re-open timing; the APK's manifest is
the same (permissions, components).

**Shorter runs (your request; your choice "Both tiers + helper + card"):** a **minimal release run** (≤ 15 min, screen recording only) and an **extended run**
only when wire or wear behaviour changes, a one-page run card, and a `scripts/` helper that pulls and checks the logs — written as a proposal into `TODO.md` §6;
the plan files change in a later session. Details below.

**What the app should change next:** ADR-061 (before release); then the `HardwareInfoFixtures` swap and a finer balance slider (`TODO.md` §5).

## Progress

- **Done:** all phases (0, A, B, C, D up to the commit question): the films, audio and logs analysed; `CAP-072-EVENT-NOTES.md` and `CAP-072-FINDINGS.md`
  written; the checkpoint answered; the approved documentation edits made (DECISIONS ADR-058/059 Updates and ADR-061, PROTOCOL §2.2a Update, the capture
  index and Group BH note, the registry, TODO, APP_TESTPLAN §V/Summary, TESTPLAN rows, PROJECT, README, INDEX).
- **Next:** finish the blurred camera film (scratchpad blur/), swap the blurred films in, the `sha256sum` file, footers and lint, then the commit question.
- **Touched, unverified:** none — each edited file was checked by `grep`/`git diff` after the edit.
- **Intermediate results:** the session scratchpad
  (`/tmp/claude-1000/-home-tedsluis-git-opencontrolpixelbudspro2/d9c043cb-2e79-45fd-9f2f-70ee03cffec1/scratchpad/`), folders film, audio, hci, logs, aosp,
  notes.

## Phase 0 — session start (2026-10-09 22:57 CEST)

- `git log -1`: `ec6d163 Merge pull request #21 from tedsluis/feature/1.2.0`; branch `release/1.2.0`.
- `git status --short`: `M ai-sessions/INDEX.md` (the 0083 row), `?? ai-sessions/0083_CAPTURE_PROMPT_2026_10_09.md`, `?? android/domain/bin/` (build output, never
  staged), and the thirteen untracked capture files in the placeholder folder (the prompt said fourteen; with the committed notes the folder holds fourteen files).
- `git fetch`; `origin/main` and `origin/release/1.2.0` are both at `ec6d163`; `git log HEAD..origin/main` and `HEAD..origin/release/1.2.0` are empty;
  `git diff --stat ec6d163..origin/release/1.2.0 -- android` is empty (exit 0).
- `gh pr view 21`: `MERGED`, merge commit `ec6d163af390cfcb3f2705bcf462a13fb2e055c3`, head `531c99dc82f6…`. `gh pr list --head release/1.2.0 --state all`: empty.
- Film 2: not present at the session start (no second camera film, screen recording, `uiautomator` dump or later export in the folder).

## Phase 0 — checkpoint answers (chat, 2026-10-09/10, `AskUserQuestion`, verbatim)

- "Films" → *"Blur number+SSID+history (Recommended)"* — blur the caller's number, the Wi-Fi SSID and the browser history; commit the blurred copies.
- "System log" → *"Extract, filtered (Recommended)"* — the CAP-071-style extract, minus every line with the account e-mail, the SSID or `OpenControlBuds: DLCI` (hex dumps).
- "Migration" → *"Approve all (Recommended)"* — rename to `CAP-072-2026-10-09_17-27-26_18-23-39-Group_BH`, `chmod 644`, a `sha256sum` file, reference updates outside `ai-sessions/`.
- "Earlier app" → *"dist/1.2.0 release APK"* — the build that sent `GetHardwareInfo` before 17:24 (uid 10353) was the `dist/1.2.0` release APK (the maintainer's statement).

## Read

- **In full:** `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `AI_SESSION_LOG_PROCEDURE.md`, the prompt, the committed skeleton, `CAP-071-FINDINGS.md`,
  `BudsRepositoryImpl.kt`; in `DECISIONS.md` ADR-001 … ADR-012, ADR-029, ADR-032 … ADR-034, ADR-036, ADR-037 and ADR-042 … ADR-060 with every Update.
- **In part:** `DECISIONS.md` (ADR-013 … ADR-031, ADR-035, ADR-038 … ADR-041 by heading only); `PROTOCOL.md` (§2.2a L-1 Updates and notes, the change table);
  `TODO.md` (§1, §2, the §4/§5 items touched, §6); `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group BG/BH sections, the Capture Index); `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`
  (the `FW-003`, `INEAR-005`, `INEAR-006` rows); `APP_TESTPLAN.md` (§V, the Summary); `README.md` (status); `CAP-071-EVENT-NOTES.md` (header and layout);
  `scripts/pwrpc_decode.py` (usage, `packets`, `describe`).
- **Not read:** `ARCHITECTURE.md`, `CHANGELOG.md`, `RELEASING.md`, `REVERSE_ENGINEERING.md`, `ai-sessions/0082` prompt and RESULT, the `CAP-049`/`CAP-070`
  findings, the reference captures' FINDINGS (their frames were checked on the wire instead), and the Kotlin files other than `BudsRepositoryImpl.kt`
  (`AncModeCause.kt`, `WornReading.kt`, the screens) — their behaviour was judged from the screen recording and the exports. This falls short of the prompt's
  reading list; nothing in the findings depends on a text of those files that was not checked against the wire or the screen.

## Phase D — checkpoint answers (chat, 2026-10-10, `AskUserQuestion`, verbatim)

- "Hold result" → *"Fix first: hold 0x04 on ANC tab"* (preview: the draft ADR text).
- "Verdict" → *"Fix first"*.
- "Case sound" → *"Buds' behaviour + experiment (Recommended)"*.
- "L-1" → *"Promote both to 🟢 (Recommended)"* (preview: the §2.2a Update text). Applied: L-1 🟡 → 🟢; "only the Right out ⇒ 21" was already 🟢 since `CAP-065`,
  so it was recorded as six more samples, not as a new FACT.
- "Hold ADR" → *"Accept as ADR-061 now (Recommended)"* (preview: the ADR-061 text).
- "ADR Updates" → *"Add both Updates (Recommended)"* (ADR-058, ADR-059).
- "Hex serials" → *"Accept per ADR-058 item 5 (Recommended)"*.
- "Shorter runs" → *"Both tiers + helper + card (Recommended)"*.
- "Film 2" → *"Fold into the rebuild's run (Recommended)"*.
- "Speech" → *"Not needed (Recommended)"* — speech is recorded as present, not transcribed.
- "Changelog" → *"No CHANGELOG change now"*.

## Phase A–C — method and main tables

The full method, timeline and tables are in `CAP-072-EVENT-NOTES.md` and `CAP-072-FINDINGS.md` (§1 claims, §2 the thirteen requests, §3 every write, §4 the
`INEAR-006` table and L-1, §5 the press-and-hold table and hypotheses, §6 the case-sound table, §7 session ends, §8 the call). Re-derived in this session by
the main session (no sub-agents were used).

- **Clock offsets:** screen recording phone = 17:27:26.25 + t (the Disconnect tap, 10 fps); camera phone = 17:27:25.9 + t = overlay − 0.1 s (the same tap and the
  recorder pill flip); exports +6 ms after the HCI log; the app logcat UTC = local − 2 h.
- **Transcript:** none — no offline speech-to-text tool (`whisper`, `vosk`, `faster_whisper` absent; checked); speech located in 140 segments.
- **Fixture-swap proposal** (`TODO.md` §5, for a FEATURE session): `CAP-072-btsnoop_hci2.log.last` request A 7593 → answer A 7596 (channel 21, 17:29:27.052 →
  .087) and A 8616 → A 8620 (channel 19); the answer's field 7 strings X-ed out as `HardwareInfoFixtures` does today (`5707XXXXXXXX51` style).

## Testing burden — the proposal (approved as a direction, chat 2026-10-10; the plan files change in a later session)

Numbers from this run: 56 min of film; ≈ 120 timeline rows; 20 bud-into-case moves; 60 writes (30 of them the balance slider); 31 sessions. What found something
on hardware here: the press-and-hold gap (a design question, visible only with a hold and the wire), the worn line at 42 s (a wear sequence), the case-chime
pattern (wear + audio). What only re-confirmed bytes the unit tests already pin: every switch write, the Connect read order, the Ring/Stop bytes (identical since
`CAP-070`/`CAP-071`).

1. **Minimal release run (≤ 15 min, every release):** the update over the last release (Dark/Debug kept); Info (build without "-dirty", firmware, serials); one
   Connect (the read sequence); one ANC tap; each **new or changed** write once; Bluetooth off/on; one export. Screen recording only (no camera); the computer runs
   a helper that pulls the HCI log and the exports through `bugreportz`, names the files, decodes the session (`pwrpc_decode.py`) and compares every request with
   its unit-test fixture, printing a pass/fail list.
2. **Extended run (only when the wire or wear behaviour changes):** camera with the head and the case, the wear sequences, sounds said aloud.
3. **A printed one-page run card** with the steps and the things to remember (lid open, no app before the film, Do Not Disturb, the export before Bluetooth off).
4. **`TODO.md` §2:** the screen-reader dumps (T11) and C12/S12 fold into the rebuild's run; the per-switch re-writes leave the hardware plan (unit tests pin them).

## Deferred documentation (each also in `TODO.md`)

- "**1.2.0 rebuild (before release; verdict "fix first", the maintainer 2026-10-10):** build `DECISIONS.md` ADR-061 …" (`TODO.md` §5).
- "Replace the redacted-answer fixtures of `GetHardwareInfo` … by OpenControl's own exchange from `CAP-072` …" (`TODO.md` §5).
- "Balance slider: ≈ 30 writes and labels up to "Right 53" …" (`TODO.md` §5).
- "The case sound (`CAP-072-FINDINGS.md` §6 …) **Proposed experiment** …" (`TODO.md` §2).
- "Volume EQ (what is heard) and its writes — not done in `CAP-072` …" and "The worn line with in-ear detection **off** …" (`TODO.md` §2).
- "🔴 `CAP-072` §4: why the Buds kept Settable `e8` ≥ 42 s …" (`TODO.md` §4).
- "**Shorter hardware runs** …" and "`PROTOCOL.md` §2.2a … and §6 … write the three component serials in full …" (`TODO.md` §6).
- Not done in this session and not in `TODO.md` because they are checks, not work items: the GrapheneOS release notes between `CP3A.260905.009` and
  `CP3A.261005.005` (not fetched), the AOSP source for "a user switch restarts Bluetooth" (not fetched), the pull of the installed `base.apk` (phone not attached).

## Commits

Back-filled 2026-10-10 by `ai-sessions/0084` (`AI_SESSION_LOG_PROCEDURE.md` §4b item 2), on `release/1.2.0`, merged into `main` by PR #24 (merge commit `0c61b90`):

- `bc24c56` docs(capture): CAP-072 — the 1.2.0 release run (Group BH), analysed
- `51e490f` docs: CAP-072 results — ADR-061, ADR-058/059 Updates, L-1 promoted, plans
- `2d5255a` docs(ai-sessions): 0083 CAPTURE prompt, result and INDEX row

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0083_CAPTURE_RESULT_2026_10_09.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0083_CAPTURE_RESULT_2026_10_09
