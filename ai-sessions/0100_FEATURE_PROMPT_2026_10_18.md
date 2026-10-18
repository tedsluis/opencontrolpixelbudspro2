# 0100_FEATURE_PROMPT_2026_10_18.md — The 1.4.0 fixes after `CAP-079`: the ring hold, the stale "don't allow" line, three 1.3.x texts, the experiment's SetWallclock answer — and the skeleton of a short hardware run

**Number:** 0100
**Category:** FEATURE
**Date:** 2026-10-18
**Title:** Fix what the hardware run `CAP-079` (`ai-sessions/0099`) found wrong in 1.4.0 — (1) "Stopped on the bud" never appeared because the Ring's hold
(`RING_HOLD_MAX_MS` = 20 s) ends long before the Buds stop ringing (≈ 63 s untouched); (2) the red "The Buds don't allow changing noise control right now …"
line stays after the Buds allow a change again; (3) three screen texts still describe 1.3.x; (4) the experiment build's F1 `SetWallclock` is answered on the
wire but reported as "Timeout" — each change first decided by the maintainer in this chat (an ADR-069 Update for (1)); the tests with `CAP-079`'s bytes, the
mutation checks, the clean gate; the skeleton of a short hardware run that repeats what `CAP-079` could not show (the ring stops, TalkBack, a Disconnect during
a Connect, `WELL-002` with a sound-level meter); the documents. No tag, no publication, no merge.

---

## 0. How to use this prompt

You are an expert Android/Kotlin engineer, protocol analyst and technical auditor. This prompt is for a **fresh** Claude Code session in this repository. Run
the phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Read in full, in this order: `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`,
`ARCHITECTURE.md` (in particular §3.1, §3.2 the timing constants incl. `RING_HOLD_MAX_MS`, §5a with the `CAP-079` run note, §6.0b the 1.4.0 rule "the Message
Stream is claimed only for Find" and its log lines, §7, §12, §13), `PROTOCOL.md` (§4.1 with the Note of 2026-10-18, §4.3 Option F with its Note of 2026-10-18,
**§4.4 with the Update of 2026-10-17 and the Note of 2026-10-18** — the ring time-out, the specification's timeout byte —, §2.2a's Note of 2026-10-18, §4.5.8a,
§6, §8), `DECISIONS.md` (**every** ADR with every dated Update; read with care **ADR-069** (the ring status, the hold, item 3's 20 s), ADR-070, ADR-067 and its
Note, ADR-068, ADR-032, ADR-042, ADR-057, the draft ADR-060 with its Update of 2026-10-18 — not a decision), `TODO.md` (§1, §3, §4 — the 1.4.0 item of
2026-10-18 —, §5).

Then, before using or changing anything:
- **The run and its analysis, in full:** `ai-sessions/0099_CAPTURE_PROMPT_2026_10_18.md` and its RESULT (Checkpoint 0 and D answers verbatim);
  `captures/CAP-079-2026-10-18_07-07-31_08-42-33-Group_BO/CAP-079-FINDINGS.md` (every section — §3.3 the defects, §5 the ring, §9, §11 F1, §14, §17 the proposals,
  §18) and its EVENT-NOTES (the timeline; the skeleton in the appendix as the model for this session's skeleton).
- **The build being fixed:** `ai-sessions/0098_FEATURE_PROMPT_2026_10_17.md` and its RESULT (the log strings, the mutation table, the gate, `RELEASING.md`'s A-steps).
- **The code, in full before it is changed:** `BudsRepositoryImpl.kt` (`ringBud`, `stopRinging`, `startRingHold`, `endRingHold`, `onRingStatus`, `withMessageStream`,
  `scheduleRelease`, `writeAncMode`, `refreshAncMode`, `applyAncState`, `updateAncAvailability`, `ancFailed`, the companion constants); `codec/CodecRouter.kt`
  (`routeMaestro`), `codec/RingFrame*.kt`, `codec/Maestro.kt`, `codec/PwRpc.kt`; `:ui` `AncScreen.kt` (`ANC_NOT_ALLOWED_TEXT` and line 216), `ConnectionScreen.kt`
  (`BATTERY_EXPLANATION`), `ConnectionBanner.kt` (`MESSAGE_STREAM_HINT_TEXT`), `FindMyBudsScreen.kt`; the experiment source set (`ExperimentRunner.kt`,
  `ExperimentHook.kt`); the tests these touch (`BudsRepositoryImplTest`, `RingCodecTest`, `CodecRouter*Test`, `Cap078Fixtures.kt`, `ExperimentFramesTest`,
  `AncScreenTest`, `FindMyBudsScreenTest`, `ConnectionScreenTest`, `UserMessageTest`).
- **Procedure:** `AI_SESSION_LOG_PROCEDURE.md` (§4b, §5, §9), `RELEASING.md` (A1–A6 and the release log; B–D are the maintainer's), `CHANGELOG.md` [1.4.0],
  `APP_TESTPLAN.md` §AB, `id_registry.csv`, `ai-sessions/INDEX.md`.

Say in the RESULT which files you read in full and which only partly.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.** Create
ai-sessions/0100_FEATURE_RESULT_2026_10_18.md at the very start with `**Status:** partial — resumed` and a **Progress** block; update it after every phase and every
substantial step (each fix built, each test added, each mutation run, the gate). A resumed session reads this prompt, the Progress block, re-reads the files the
unfinished step touched, and continues; it never redoes a finished, recorded step and never assumes an unrecorded one was done.

**Memory and the machine.** 15 GB RAM, `/tmp` a 7.7 GB tmpfs. Gradle: `--max-workers=2` for a single module, `--max-workers=1` for the full clean gate; one
mutation at a time, the source restored and checked (md5) after each; check for leftover mutations after any crash. Never `git worktree add` without a sparse
checkout that excludes `captures/` (gigabytes).

**Nothing is taken on trust**, including this prompt, the 0099 RESULT and the memory notes: every frame number, byte sequence, timing and line of code cited
below is re-derived from the capture files, the code or the cited page before it is relied on.

---

## 1. What the maintainer asked for (chat, 2026-10-18, translated from Dutch)

1. After `CAP-079` (`ai-sessions/0099`, Checkpoint D): "release 1.4.0 with Known issues" (*"Release met Known issues"*), and *"Ja, 0100 fixes + korte run
   (Recommended)"* — a FEATURE session for the fixes and the skeleton of a short hardware run.
2. Asked whether reverse engineering of the APK must come first: the analysis of 2026-10-18 (chat) found that none of the four fixes needs it — the companion app
   has no local Find code (the ring time-out is a spec/wire question), and (2)–(4) are bugs in this app's own code. A separate CROSSCHECK prompt (`ai-sessions/0101`)
   takes the APK questions (per-bud wear, the stream lifecycle, field 21's core, `SetWallclock`'s use); it is written after this prompt and runs on its own.
3. "Make prompt 0100 first, then the RE prompt." Standing practice: no commit or push without the maintainer's "yes" in chat; no release step; `main` takes no
   direct push; no FACT promotion and no ADR or ADR Update without the maintainer's answer in this chat (`AGENTS.md` §6).

---

## 2. What `CAP-079` established (re-derive each before relying on it)

- **The ring outlasts the hold.** User 10 (`CAP-079-btsnoop_hci1.log`, "H1"): Ring Left H1 4535 07:23:52.116 with nothing touched; the app's hold ended
  "no stop report within 20 s" (E1 07:24:12.142) and released DLCI 0x04 at 07:24:13.649 (H1 4576); the 3.14-kHz tone ran until 07:24:55.0 (camera audio) — 62.9 s;
  the screen kept "Ringing: Left earbud" to the Disconnect. Ring Left H1 3848 07:21:19.360: the bud touched from 07:21:38 (camera), tone to 07:21:43.3, hold ended
  07:21:39.403 — no stop status reached the app. Ring both: the hold ended 07:23:16.622, 0.35 s before the app's Stop 07:23:16.971. `CAP-078`'s 14.8–17.1-s stops
  (which ADR-069 item 3 cites) were most likely touches (`CAP-079-FINDINGS.md` §5.2; `PROTOCOL.md` §4.4 Note of 2026-10-18, 🟡 one sample).
- **The specification** (Device Action page, fetched 2026-10-18, "Last updated 2025-08-12 UTC"): "The second byte in additional data, if present, represents the
  timeout in seconds. This value should be used by the Provider to determine how long it should ring before silencing itself"; "Ringing should continue until an
  additional action is received directing a stop, or a timeout value has passed"; no default. The app sends no timeout byte (`04 01 00 01 xx`).
- **The user with Play:** Play cuts the app's claim 4.75–4.81 s after its `SABM` and receives the stop status itself (`CAP-079` H2 4785 → 4788) — a longer hold
  changes nothing there; the screen keeps "Ringing …" until Stop (a Known issue that stays).
- **The stale line:** after a status 9 (H1 2501) or a Refresh with 13 = 0, `_ancError` stays `AncNotAllowed` while the Buds push a settable mode (H1 2742 13 = 4,
  07:14:14.174; screen s072815 382 until a Refresh 07:14:22; user 0 s074935 173). `_ancError` is cleared only by an OK write or a successful Refresh.
- **The texts:** ANC card "… Tapping a mode checks again first." (1.4.0 writes without a check); battery (i) "Left/Right %: read from the Buds when the app last
  reached their Message Stream (at Connect, Refresh and each ANC / Find action)." (since ADR-068: runtime info); Find (i) "Each action briefly claims the Buds'
  Message Stream channel. …" (a ring holds it up to the hold's limit).
- **F1:** `SetWallclock` H3 2559 → RESPONSE OK H3 2563 in 78 ms (`CAP-079-btsnoop_hci4.log.last`); E3 "Experiment F1 failed: Timeout" — `routeMaestro` returns an
  `RpcResult` only for ReadSetting/WriteSetting/GetHardwareInfo, so the runner's wait never matched; the answer was listed as an unidentified frame.

---

## 3. Scope and constraints

- **In scope:** the four fixes; their tests (real `CAP-079` bytes as fixtures, `AGENTS.md` §11 — frame number and command in a comment); the mutation checks; the
  clean gate; the skeleton of the short run; the documents (`CHANGELOG.md`, `ARCHITECTURE.md` §3.2/§5a/§6.0b, `APP_TESTPLAN.md`, `TODO.md`, registers).
- **Decided at Checkpoint 1, not before:** the version (rebuild 1.4.0 before it is published, or 1.4.1 on top of a published 1.4.0 — the maintainer may publish 1.4.0
  first under `RELEASING.md`); the branch; the ring design (options in §4); the exact texts; whether `SetWallclock`'s answer is classified in the release codec or
  only in the experiment source set.
- **Out of scope:** any new request in the release app other than an approved ring form; the Dosimeter in the release (draft ADR-060 stays a draft); the per-bud
  wear, the stream lifecycle and field 21's routing (`ai-sessions/0101`); the Case ring (no local mechanism — ADR-027 stands for the Case); any change to settings
  3 or 6 (`ARCHITECTURE.md` §5a rule).
- **A new send** (e.g. a Ring with a timeout byte `04 01 00 02 0x tt`) has never been on the wire: it needs the maintainer's explicit approval of the exact bytes, an
  ADR (Update), a `// TODO(verify)` until a capture shows it, and its own step in the skeleton.

---

## 4. Tasks

### Phase 0 — set-up, re-derivation and Checkpoint 1
1. Create the RESULT (§0). `git fetch`; record `git log -1`, `git status --short`, `HEAD..origin/main`, draft PR #69's state. Re-derive §2 from the files (the frames,
   the export lines, the code lines) and record any difference.
2. **Checkpoint 1 (`AskUserQuestion`, one question per decision, options with pros and cons, "(Recommended)" on one, code or text in the preview):**
   - **Version and branch:** (a) rebuild 1.4.0 before publication on `feature/0098-round-2` (PR #69 gets the fixes); (b) 1.4.1 after 1.4.0 is published, on a new
     branch from the published tag; (c) another arrangement.
   - **The ring hold (an ADR-069 Update, draft text in the preview):** (a) `RING_HOLD_MAX_MS` ≈ 65 s, no new bytes — the claim held up to ≈ 66.5 s in the user without
     Play; (b) send the specification's timeout byte (e.g. `04 01 00 02 02 3C` = 60 s) and hold until it plus a margin — a new send, unverified until a capture;
     (c) hold until the stop status, the app's Stop, a new Ring or a foreign close, with a generous cap; plus what the screen says when the hold ends without a
     stop ("may still be ringing" vs. nothing) — with the evidence of §2 for each.
   - **The stale line:** clear `ancError` when setting 13 becomes settable (a push or a read with 1–4), or show it only while availability is "not allowed".
   - **The three texts:** the proposed wording of each (the maintainer's words; English UI, `UserMessageTest`/screen tests pin them).
   - **F1's answer:** classify `SetWallclock`'s RESPONSE as an `RpcResult` in the release codec (no behaviour change for the release, which never sends it) or in
     the experiment source set only; and whether the Dosimeter packets should stop filling "Unidentified frames" in the experiment build.
   - **The short run's scope:** which steps (ring touched after > 20 s, ring left alone, Ring both + touch, TalkBack, a Disconnect during a Connect, the meter for
     `WELL-002`, the user with Play) and whether the experiment build rides along.
   Record the answers verbatim in the RESULT. ADR texts are written only after the maintainer accepts them here.

### Phase 1 — build (one fix at a time, each with its tests before the next)
3. The ring hold as decided (with a test using `CAP-079`'s bytes: H1 3848 `04 01 00 01 02`, the ACK and the status `04 01 00 01 02`; a stop status after the old 20 s
   must still be heard and shown as "Stopped on the bud"); `ARCHITECTURE.md` §3.2's row and §6.0b's text follow.
4. The stale line (a test: a status 9, then a push `4:{13:4}` — `CAP-079` H1 2501, 2742 — clears the error).
5. The three texts (screen tests with the literal texts).
6. F1's classification (the runner's test with H3 2559/2563's bytes).
7. Mutation checks (one per rule changed: the hold's end, the error's clearing, the classification) — killed, or a new test until killed.

### Phase 2 — the gate and the proofs
8. The clean gate (`--max-workers=1`, the task list of `.github/workflows/android.yml` and `RELEASING.md` A4, both APKs); the release APK without experiment code
   (`dexdump`, as 0098); permissions (`aapt2 dump badging`, no `INTERNET`); dependencies unchanged; `lint_docs.py` exit 0.

### Phase 3 — the skeleton and the documents
9. The skeleton of the short run as the next free `CAP-NNN` (register it in `id_registry.csv`, "planned"; a new Group letter — check `CAPTURE_BLUETOOTH_HCI_SNOOP.md`):
   preparation (both users, one app at a time, a sound-level meter in frame if chosen, TalkBack off at the start), the reference frames, the steps with expected
   screen and wire and "Refuted if", the Test-IDs (`FIND-001`, `FIND-002`, `FIND-004`, `FIND-005`, `PAIR-003`, `WELL-002`, and the A93-APP-07 TalkBack step), a run card.
10. `CHANGELOG.md` (the fixed known issues, as decided for the version), `ARCHITECTURE.md`, `APP_TESTPLAN.md` (a new section for the run), `TODO.md`, `README.md` if the
    version changes, `DECISIONS.md` only as accepted; `ensure_footers.py`; `lint_docs.py` exit 0.
11. The RESULT: plain-language summary first, the phases, the files read, the checkpoint answers, the mutation table, the gate, "Deferred documentation" (each also in
    `TODO.md`), "Commits"; the `ai-sessions/INDEX.md` row (0100). Show a short summary and **ask whether to commit and push**; only after a yes: commits per concern
    (Conventional Commits, each with its *why* and the attribution line), push without force, never merge.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)
- No ADR, ADR Update or `PROTOCOL.md` status change without the maintainer's answer in this chat; ADR-060 stays a draft.
- No new wire traffic in this session (no phone actions); a new send form only as approved, flagged `// TODO(verify)` until a capture.
- Kotlin only; coroutines/StateFlow; no fixed-interval timer (a hold is one bounded wait); no new permission, no `INTERNET`, no new dependency without justification.
- Tests with real capture bytes; a hand-built frame only as a labelled structural test beside them.
- No release step (`RELEASING.md` B–D), no tag, no `dist/` change, no merge, no rebase, no force push; `android/domain/bin/` is never staged.
- Privacy: no serial unredacted, no MAC, nothing from the films' private content in any committed text.

## 6. Deliverables
- The four fixes as decided, with tests, mutation checks and the green gate.
- The ADR-069 Update (as accepted), the documents, the skeleton of the short run.
- ai-sessions/0100_FEATURE_RESULT_2026_10_18.md and the `ai-sessions/INDEX.md` row.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0100_FEATURE_PROMPT_2026_10_18.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0100_FEATURE_PROMPT_2026_10_18
