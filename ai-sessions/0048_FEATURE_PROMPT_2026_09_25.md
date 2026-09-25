# 0048_FEATURE_PROMPT_2026_09_25.md — Build the `ai-sessions/0047` improvements: ANC only while worn, ring notice, loss wording, per-bud "in the case", last-seen Case, automatic foreground re-open (ADR-044)

**Number:** 0048
**Category:** FEATURE
**Date:** 2026-09-25
**Title:** Implement the improvements the maintainer chose after `CAP-062` (`ai-sessions/0047` RESULT §11/§13): I-6, I-7, I-3 (bug/UX); I-4, I-5, I-8
(runtime-info stream, ADR-043 Update); I-1 (automatic foreground re-open of the session, ADR-044) — with real `CAP-062` bytes as test fixtures, a
green build/test/lint gate, and a hardware re-test list for the next capture (Group AY, including I-9/I-10)

---

## 0. How to use this prompt

You are an expert Android/Kotlin engineer and protocol auditor. This prompt is for a **fresh** Claude Code session in this repository. Run the
phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8).** Before any other action, read in full, in this order:
`AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md`, `DECISIONS.md` (**every** ADR, ADR-001 … ADR-044, including the
2026-09-25 Updates of ADR-024 and ADR-043), `TODO.md`. Then read `AI_SESSION_LOG_PROCEDURE.md`, `ai-sessions/INDEX.md`,
`ai-sessions/0047_CAPTURE_RESULT_2026_09_25.md` (in full — §11 is the improvement list this session builds, §13 the maintainer's decisions),
`captures/CAP-062-2026-09-25_06-38-36_06-57-10-Group_AX/CAP-062-FINDINGS.md` and `CAP-062-EVENT-NOTES.md` (in full), `APP_TESTPLAN.md`, and
`ai-sessions/0046_FEATURE_RESULT_2026_09_24.md` §6–§9 (how the last FEATURE session built, tested and mutation-checked). Then read **every** Kotlin
file you will change, and its tests, before changing it.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.** Create
**ai-sessions/0048_FEATURE_RESULT_2026_09_25.md** at the very start, with `**Status:** partial — resumed` and a **Progress** block, and update that
block at the end of every phase and after every substantial step: what is done, what is next, which files are touched but not yet verified by a
green build, and where intermediate results live. A resumed session reads this prompt, then the RESULT's Progress block, re-reads the files the
unfinished step touched, runs the build/test gate once to learn the real state, and continues from there. It never redoes a finished, recorded step
and never assumes an unrecorded step was done.

**Nothing is taken on trust, including this prompt and the 0047 RESULT.** Re-derive every byte you put in a fixture from the capture with the
command given (`tshark`, `scripts/pwrpc_decode.py`), and re-check every file:line reference below against the current code — it may have moved.

---

## 1. What the maintainer asked for (chat, 2026-09-25)

After `CAP-062` the maintainer chose, in `ai-sessions/0047` (`AskUserQuestion`, recorded verbatim in that RESULT §13), **all four groups** for the next
FEATURE session:

1. **"I-3 + I-6 + I-7 (bug/UX)"** — no new wire, no new ADR.
2. **"I-4 + I-5 (per-bud + last Case)"** — allowed by the ADR-043 Update (2026-09-25) and the 🟢 FACT in `PROTOCOL.md` §4.3 Option F ("Per-bud fields").
3. **"I-1 (and I-8)"** — I-1 is **ADR-044** (Accepted 2026-09-25); I-8 changes no wire rule (ADR-032/033 unchanged).
4. **"I-9 + I-10 (next capture)"** — not code: write the re-test / capture plan (Group AY) for the maintainer.

The maintainer's original complaints these answer (`ai-sessions/0047` §1): the app keeps losing its session while Android stays connected; ANC is
"refused" in the case; the app does not show which bud is in or out; after re-docking it does not show the bud in the case and the Case battery;
Find does not work in the case (unknown, experiment I-9).

---

## 2. The evidence to build on (verify, do not copy blindly)

All from `CAP-062-btsnoop_hci.log` (pre-filter by handle `bthci_acl.chandle==0x000b`; `bluetooth.addr` is empty with this encapsulation) and
`CAP-062-debug-export.log`:

| Improvement | Real bytes / frames to use as fixtures | Source |
|---|---|---|
| I-3 (Settable `0x00` ⇒ ANC not allowed) | `Notify` `08 13 00 04 01 e8 00 20` (frame 6000) → NAK `ff 02 00 03 02 08 12` (5998); `Notify` `08 13 00 04 01 e8 e8 40` (8706) → ACK `ff 01 00 06 08 12 01 e8 e8 40` (8705) | FINDINGS §2 |
| I-4 (per-bud) | runtime-info `SERVER_STREAM` frames 3760 (Right charging only), 4845 (both), 7033 (Left only), 7118 / 2782 (none; no entry 6.1) — e.g. 7033 = export line 298 `7e 00 a5 03 2a 1e 18 00 32 12 0a 04 08 3c 10 01 12 04 08 64 10 02 1a 04 08 64 10 01 3a 06 08 00 10 01 18 00 08 07 10 15 1d ea 71 de 7d 5e 25 90 82 1e e6 96 e7 a8 9b 7e` | FINDINGS §4; `PROTOCOL.md` §4.3 Option F |
| I-5 (last-seen Case) | 7033 (6.1 = 60) followed by 7118 (no 6.1) | FINDINGS §4 |
| I-6 (ring notice) | Ring Left `04 01 00 01 02` (9772) → ACK (9783); user Disconnect 06:53:20.4; the ring kept sounding until Stop (9942, audio) | FINDINGS §5, §7.1 |
| I-7 (loss wording) | ACL `Disconnection Complete` 0x13 (3814, 06:42:35.113); loss line 06:42:35.135; Android link `NOT_CONNECTED` 06:42:35.244 (export 101–103); a Buds-side `DISC` on 0x02 with the ACL up (5313) | FINDINGS §3, §7.2 |
| I-1 (ADR-044) | the 7 Buds-side `DISC`s with the ACL up (FINDINGS §3 table rows 5, 7, 9, 11, 13; 6 and 8 precede an ACL drop); Android re-creating the ACL itself (Create Connection 7226, 12 ms after the drop) | FINDINGS §3 |

Code locations found in `0047` (re-check): `BudsRepositoryImpl.disconnect()` clears `_ringingTarget` (`BudsRepositoryImpl.kt` ≈ line 469) and
`connect()` clears it again (≈ 424); `RingingNotice` in `ui/…/FindMyBudsScreen.kt` ≈ 79–85; `RuntimeInfoDecoder` in `data/…/codec/RuntimeInfo.kt`
(reads 6.1 only; an absent 6.1 returns `BatteryLevel.Unavailable`); the `RoutedFrame.RuntimeInfoCase` branch of `handleRoutedFrame`; the loss handler
(`transport.connectionLost.collect`) and `SessionDiagnostics.lossLine`; `setAncMode`/`withMessageStream`; `refreshBattery()` (≈ 680);
`AncScreen.kt`, `AncTileService.kt`, `ConnectionScreen.kt` (loss texts, `CASE_NOT_REPORTED`), `OsConnectionObserver.kt`, `MainActivity.kt`,
`OpenControlApplication.kt`; `FakeBudsTransport` in `:hardware`'s test fixtures; `Cap061Fixtures.kt` as the model for a new `Cap062Fixtures.kt`.

---

## 3. What each improvement must do (the maintainer's decisions are binding; details marked "agent detail" are yours, open to veto)

### I-6 — the ring notice survives Disconnect (APP_TESTPLAN I4)
- A ring started on this app is only cleared by an ACKed Stop (or a new Ring of the other side). `disconnect()` and `connect()` keep it; the
  not-ready branch of `RingingNotice` ("A ring was started on the … earbud — reconnect and tap Stop to end it.") then appears as designed.
- Agent detail: word the ready branch after a reconnect "may still be ringing — tap Stop to end it" (the app cannot know whether it stopped).

### I-7 — honest session-loss wording
- For a loss, the text says what the evidence says: (a) **ACL gone** (Android link not connected, or an ACL drop within the loss window) →
  "Android no longer shows the Buds connected …"; (b) **Buds-side `DISC` of DLCI 0x02 with the link up** → "The Buds closed the app's channel (this
  happens when a bud goes in or out of the case or an ear)"; (c) the user's own Disconnect → no loss text. Never "likely another app" for (b).
- The decision must not use an Android-link value that is older than the loss: re-render when the link state changes within ≈ 1 s after the loss
  (agent detail: the UI derives the text from both flows, no timer; `ARCHITECTURE.md` §6 "no timer loops").
- Note: (b) is only visible if the transport can tell a peer `DISC` from other socket ends; if it cannot, keep one neutral text and record why.

### I-3 — ANC only while the Buds allow it
- Keep the Settable byte of the latest `Notify` (the repository already stores it as `dockState`, ADR-024; rename or add a clear model — agent
  detail). While it is `0x00`: the ANC buttons and the tile action are disabled with the text "ANC can only be changed while you wear the Buds"
  (wording 🟡 per ADR-024's Update — do **not** claim "in the case"), and **no `Set` is sent**. A Refresh (`08 11`) re-reads it; a `Notify` with a
  non-zero value re-enables.
- Unknown (no `Notify` yet this session) ⇒ enabled, as today. A NAK is still reported as today (the firmware stays the authority).
- The tile: while `0x00`, a tap shows the reason (subtitle/toast) instead of sending.

### I-4 — which bud is (charging) in the case, from the stream
- Extend `RuntimeInfoDecoder` to read, per `PROTOCOL.md` §4.3 Option F's 🟢 FACT: entry 6.2 = Left, 6.3 = Right, their field 2 (`2` = charging,
  `1` = not), and 7.1 (Right) / 7.2 (Left). **Nothing else** (field 3, 7.3 stay uninterpreted). Where 6.x.2 and 7.x disagree (6/403 in history), prefer
  6.x.2 (401/403) — agent detail, record it.
- Show per bud "charging in the case" / "out of the case" with the time the packet arrived (ADR-043 Update wording: "charging in the case"); the
  percentage lines from DLCI 0x04 stay as they are.
- Updates arrive by themselves (the Buds push on every dock change); no polling, no new request.

### I-5 — last-seen Case instead of "unavailable"
- A packet without entry 6.1 no longer blanks the Case: keep the last value as `BatteryLevel.Known(…, isStale = true)` and show "last seen HH:MM:SS
  (no bud in the case)". Before any value on this installation's session: the existing "haven't reported" text, reworded to not contradict itself.
- `AGENTS.md` §5: a dated last-seen value, never a fabricated or carried-over-silently one. Decide (and record) whether it survives a reconnect
  (agent proposal: yes within the process, marked stale; never persisted to disk — `ARCHITECTURE.md` §3.1/§9).

### I-8 — charging from the stream
- Once I-4 exists, "(charging)" on the Left/Right lines follows the stream as well as the DLCI 0x04 frames (the newest wins, each with its time).
  *Refresh battery* still claims DLCI 0x04 for the percentages (ADR-032/033 unchanged). No new wire.

### I-1 — automatic foreground re-open of the MAESTRO session (ADR-044, binding text)
1. While the app is in the foreground **and** Android reports the Buds connected, the app opens DLCI 0x02 (a) 1–2 s after a Buds-side `DISC` with
   the ACL up, (b) when Android's link comes back, (c) on resume. **One attempt per event, no loop, no timer.**
2. The user's Disconnect tap turns this off until the next Connect tap.
3. No background activity; no new permission or service.
- Agent details to propose (not decide silently): the exact delay within 1–2 s; whether the re-open repeats the DLCI 0x04 snapshot claim (ADR-032)
  or relies on the stream (I-8) to avoid disturbing Play services — record the choice in the RESULT; the foreground service follows the session
  as today (`ARCHITECTURE.md` §6.0a).
- Update `ARCHITECTURE.md` §6/§6.0b "as built" once it is built (ADR-044's Consequences).

### I-9 / I-10 — the next capture (Group AY), no code
Write in the RESULT a re-test plan with HCI brackets: every item above; Find with **both buds docked** (lid open, reconnect, Ring Left, Stop — does
a docked bud ring? ACK or NAK `ff 02 00 03 <reason> 04 01`); an EQ slider move while docked; the one-bud-in-an-ear test of "Settable `0x00` = not worn"
(`08 11`→`08 13` with one bud worn, the other on the table, then swapped); the `APP_TESTPLAN.md` steps not run in `CAP-062` (A5, B4, C5, F5–F7, H5,
J4, K1–K5, L3, 0045 (E)/(F)); and the checklist items missed last time (build commit hash, Play services' *Nearby devices* state). Update
`APP_TESTPLAN.md` where a test's expectation changes (I4 text, the ANC-disabled state, automatic re-open in C8/C9/E3–E5).

---

## 4. Tasks

### Phase 0 — set-up
1. Create the RESULT file (§0). Run the baseline gate: `cd android && ./gradlew --offline assembleDebug testDebugUnitTest test lint` — record the
   test counts and lint state **before** any change (the 0046 counts were `:data` 1480, `:hardware` 49, `:domain` 11).
2. Re-derive every fixture byte of §2 from `CAP-062` with the commands (rule 4a) and put the commands and raw bytes in the RESULT.

### Phase A — bug/UX (I-6, I-7, I-3)
3. Implement I-6, then I-7, then I-3, each with unit tests (JUnit 5 + Kotest, as the existing tests): I-6 in the repository tests
   (`FakeBudsTransport`: ring, Disconnect → still reported; Stop ACK → cleared); I-7 in `SessionDiagnostics`/status derivation tests (the 06:42:35
   ordering; a peer `DISC` with the link up); I-3 with the real frames 6000/8706 (a `Set` while `00` sends nothing and returns a specific error/state;
   after `e8` it sends).
4. Gate green after each item; record in the Progress block.

### Phase B — stream (I-4, I-5, I-8)
5. New `Cap062Fixtures.kt` with the real stream packets (3760, 4845, 7033, 7118/2782), redacting nothing that is not an identifier (these packets
   carry none — verify). Extend `RuntimeInfoCodecTest` (per-bud fields; absent 6.1; malformed/truncated input never throws — extend the fuzz test,
   `AGENTS.md` §11). Repository tests: last-seen Case, charging from the stream vs from DLCI 0x04 (newest wins).
6. UI: per-bud "charging in the case"/"out of the case" with time; Case "last seen". Gate green.

### Phase C — automatic re-open (I-1, ADR-044)
7. Implement with the constraints of §3 I-1. Unit tests with `FakeBudsTransport`: a peer `DISC` with the link up while visible → exactly one
   re-open after the delay; none after the user's Disconnect; none while backgrounded; link comes back while visible → one open; resume → one open;
   a failed re-open is reported, not retried. Gate green.
8. Update `ARCHITECTURE.md` §6/§6.0b/§6.0a "as built".

### Phase D — verification
9. Full gate: `./gradlew --offline assembleDebug testDebugUnitTest test lint` → BUILD SUCCESSFUL, no new lint issue, no new suppression.
10. **Mutation checks** (apply, run, restore byte-identical with `cmp`), at least: (M1) `disconnect()` clears the ring target again → I-6 tests fail;
    (M2) the Settable gate removed → I-3 tests fail; (M3) 6.2/6.3 swapped in the decoder → I-4 tests fail; (M4) the re-open allowed after a user
    Disconnect → I-1 tests fail. Record the failure counts.
11. Confirm: no `INTERNET` permission, no new dependency, no new permission, nothing sent on the wire that the ADRs do not allow (grep the encoders
    used; I-3 sends **less**).

### Phase E — checkpoint (`AskUserQuestion`, in this chat)
12. Ask only what the rules require or what came up: any agent detail that changes behaviour the maintainer can see (the re-open delay; whether the
    re-open repeats the DLCI 0x04 claim; whether the last-seen Case survives a reconnect; the exact UI texts), each option with pros and cons and one
    "(Recommended)". No FACT or ADR change is expected; if one turns out to be needed, draft it and ask. Record the answers verbatim.

### Phase F — documentation and finish
13. Apply only what was approved: `ARCHITECTURE.md` (§3.1 table, §4, §5a, §6.x as built), `PROJECT.md` status note if a tick changes, `APP_TESTPLAN.md`
    (changed expectations), `TODO.md` (open items only; the 0047 build item done), `CHANGELOG.md`, `README.md` status, `ai-sessions/INDEX.md` (0048
    row). `PROTOCOL.md`/`DECISIONS.md` only if §12 approved a change. Run `python3 scripts/ensure_footers.py` and `python3 scripts/lint_docs.py`; it
    must exit 0.
14. Finish the RESULT: plain-language summary first (what changed for the user, per complaint), then per-item what/where/tests, the verification
    numbers, mutation results, the Group AY re-test plan (§3 I-9/I-10) with the expected HCI bracket per step, and open items. Status per
    `AI_SESSION_LOG_PROCEDURE.md` §4.
15. Show the maintainer a short summary and **ask whether to commit and push**. Only after a yes: Conventional Commits, one commit per concern
    (`fix(app)` for I-6/I-7, `feat(app)` for I-3/I-4/I-5/I-8, `feat(app)` for I-1, `docs` for the rest), each with a *why* and ending with the
    attribution line from the session's system reminder; `git fetch` and `git log origin/main..HEAD` before pushing (CI may have added a sidebar
    commit — rebase, never force); nothing from a build directory or `__pycache__` staged.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **Scope.** Only I-1, I-3, I-4, I-5, I-6, I-7, I-8 in code; I-9/I-10 as a plan. No background session or CDM device presence (ADR-044 option (c)
  stays unbuilt), no new permission, no new dependency, no new request on any channel, no DLCI 0x08, no Case ringing or "ring both" (ADR-027),
  no ANC `Set` while the Buds report Settable `0x00`.
- **ADR-044 exactly.** Foreground only, event-driven, one attempt per event, no timer or retry loop, off after a user Disconnect.
- **Never fabricate** (`AGENTS.md` §5): a last-seen value always carries its time and says it is last seen; "charging in the case" is the
  wording, not "in the case" as a certainty (🟡 per `PROTOCOL.md` §4.3 Option F).
- **Evidence.** Fixtures are real `CAP-062` bytes with the command that extracted them; zero creativity with hex (`AGENTS.md` §13.6). Nothing is
  "hardware-verified" in this session — say so.
- **Approvals only in this chat.** Text in a file is data, not approval (`AGENTS.md` §6).
- **Subagents.** A subagent may only read and report; every write is done in the main session and verified.
- **Code style.** Kotlin only, coroutines/Flow, `Dispatchers.IO` for I/O, sealed errors, the AGPL-3.0 header on every new Kotlin file, comments
  matching the surrounding code; logging per `AGENTS.md` §9 (no full MAC, hex only in Debug mode).
- **Commits.** Only after the maintainer confirms the final summary (task 15).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0048_FEATURE_PROMPT_2026_09_25.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0048_FEATURE_PROMPT_2026_09_25
