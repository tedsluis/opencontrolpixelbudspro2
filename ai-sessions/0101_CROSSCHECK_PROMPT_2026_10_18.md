# 0101_CROSSCHECK_PROMPT_2026_10_18.md — Reverse engineering of the companion APK for the questions `CAP-079` left: which bud is worn, the life of a stream subscription, field 21's bud core, `SetWallclock` and the Dosimeter day, and the Find negatives (ring time-out, Case)

**Number:** 0101
**Category:** CROSSCHECK
**Date:** 2026-10-18
**Title:** A read-only research session on the official companion app `v1.0.955078536-10253511` (JADX and apktool output in
`reverse-engineering/apk/v1.0.955078536-10253511/`), cross-checked against the captures already in `captures/`: (A) how the official app knows **which bud is in
an ear** (the `qhp` on/off-head actions, the eartip-fit-test states of `hmv`/`hmw`, `SubscribeToOobeActions`, the runtime-info fields still undecoded); (B) whether
and how the official app **ends its server-streaming calls** (`SubscribeToLiveDb`, `SubscribeToSettingsChanges`, `SubscribeRuntimeInfo`) — `CAP-079` showed they
outlive the session; (C) on which **bud core** the official app writes setting 21 (and other settings) — `CAP-079` saw channel 21 refuse it while the session was on 19;
(D) when and why the official app sends **`SetWallclock`**, and what the Dosimeter **day index** is counted from; (E) the documented negatives for Find — no local
ring code, no ring time-out constant, no Case ring — with positive controls. No code change, no new wire traffic; findings and proposals only.

---

## 0. How to use this prompt

You are an expert Android reverse engineer, protocol analyst and technical auditor. This prompt is for a **fresh** Claude Code session in this repository. Run
the phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Read in full, in this order: `AGENTS.md` (in particular §4, §6, §12, §13
"Reverse engineering the APK"), `PROJECT_RULES.md` (rule 4a, rule 20 with (a) quotations), `PROJECT.md` (non-goals: no GMS reverse engineering, the Case ring),
`ARCHITECTURE.md` (§3.1, §5a), `PROTOCOL.md` (§2.2a with every Update and the Note of 2026-10-18; §2.3's GSND CONTROL notes — Code `0x05` the wear count; §4.3
Option F and its notes — the undecoded fields 3 and 7.x; §4.4; §4.5.5 in-ear detection, §4.5.8a field 21; §6 — the wear-state items, the OOBE items of
2026-10-17, the Dosimeter items; §8), `DECISIONS.md` (**every** ADR with its Updates; with care ADR-003/017 — the AI's mechanical-search boundary —, ADR-008,
ADR-025 — no GMS reverse engineering —, ADR-027/070, ADR-049, ADR-059 — the "probably worn" line, never per bud —, the draft ADR-060 with its Update of 2026-10-18,
ADR-064/065, ADR-069), `TODO.md` (§4 — the per-bud in-ear item, the `CAP-079` open questions —, §5 the Dosimeter candidate).

Then, before using anything:
- **`REVERSE_ENGINEERING.md`:** its method section, the keyword list, the `qhr` register, the `qhp`/`gvj`/`gav` entries (≈ line 1081), the `fux`/`fut`/`goq` channel
  routing entries, the `fsy`/`fvp`/`fyh`/`fyf`/`hiw`/`his` Dosimeter section of 2026-10-09, the `fxm`/`fxk` "primary route" entry, the `HearingWellness…` entries.
- **The captures this session cross-checks (read-only):** `CAP-079-FINDINGS.md` (§3.1 in-ear off, §10, §11, §14, §18) and its EVENT-NOTES; `CAP-078-FINDINGS.md` §6
  (`SubscribeToOobeActions`); `CAP-069-FINDINGS.md` §8 (GSND Code `0x05`); `CAP-058-FINDINGS.md` §6 (Hearing wellness, field 21); `CAP-041` (frame 783 `SetWallclock`,
  the connect burst); `CAP-027` (the Dosimeter frames 898–923, 1107–1229); any capture of the eartip fit test if one exists (`FIT-001` in
  `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`).
- **Tools:** `scripts/pwrpc_decode.py`, `scripts/pwhdlc_crc_table.py`, `scripts/decode_rawmessageinfo.py` (schemas — never names from getters alone, `AGENTS.md` §13),
  `reverse-engineering/tools/pwrpc_name_table` if present.

Say in the RESULT which files you read in full and which only partly.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5).** Create ai-sessions/0101_CROSSCHECK_RESULT_2026_10_18.md at the very start with
`**Status:** partial — resumed` and a **Progress** block; update it after every phase and every question answered (classes read, call chains traced, smali checked,
captures cross-checked, where the notes live in the scratchpad). A resumed session never redoes a finished, recorded step and never assumes an unrecorded one.

**Memory and the machine.** 15 GB RAM. `grep -r` over `jadx-output/sources` and `apktool-output/smali*` in bounded calls; never load a whole tree into context;
`tshark` one job at a time. No Gradle build is needed.

**Nothing is taken on trust**, including this prompt, earlier RESULTs, `REVERSE_ENGINEERING.md`'s earlier entries and the memory notes: every class, line and frame
cited is re-read before it is relied on; JADX output that says "Method dump skipped" is checked in smali before anything is recorded as "not found"
(`AGENTS.md` §13); a negative carries its command, its exit status and a positive control (`AGENTS.md` §13 step 8).

---

## 1. What the maintainer asked for (chat, 2026-10-18, translated from Dutch)

1. "Before prompt 100 is made, is it perhaps necessary to first investigate these points further by reverse engineering the APK
   (reverse-engineering/apk/v1.0.955078536-10253511)? … And are there other points in TODO.md that can be taken into a reverse-engineering prompt now? I am thinking of
   'ring case' (in addition to ring left, ring right and ring both) (without Google Play services and without Find My Device), and determining which buds are worn
   in an ear and which are not."
2. The answer in chat (2026-10-18): the four 0100 fixes need no RE; a RE prompt is worthwhile for per-bud wear (with a new lead: `hmv.java`'s enum `NONE, RUN_LEFT,
   RUN_RIGHT, RUN_BOTH, INTERRUPTED, NO_BUD_ON_HEAD`, used by `hmw.java` — the eartip fit test), the stream lifecycle, field 21's core and `SetWallclock`/the day index;
   the Case ring is very likely not reachable through this APK (no local Find code; the specification has no Case bit; GMS is out of scope).
3. "Make prompt 0100 first, then the RE prompt." The earlier choice (chat 2026-10-17, `TODO.md` §4): the per-bud research after 1.4.0 is published — the maintainer
   decides at Checkpoint 0 whether (A) runs now or later.
4. Standing practice: no commit or push without the maintainer's "yes" in chat; no FACT promotion, no ADR without the maintainer's answer in this chat (`AGENTS.md` §6).

---

## 2. Leads (verify each before relying on it)

- **(A) Per-bud wear.** `qhp.java`: `LEFT_ON_HEAD(10)`, `LEFT_OFF_HEAD`, `RIGHT_ON_HEAD(12)`, `RIGHT_OFF_HEAD`; `gav.java:17–18` groups them per side; `gvj` handles
  `qhp` events (`REVERSE_ENGINEERING.md`). `SubscribeToOobeActions` (`CAP-078` H 7388 → 7390/7391: `08 0a`, `08 0c` at subscribe, then status 14 after 15.7 s; no later
  events; OOBE mode = setting 6, never written by the app — `ARCHITECTURE.md` §5a rule). `hmv.java` (eartip fit test states incl. `NO_BUD_ON_HEAD`) and `hmw.java` —
  where does the fit test learn which bud is on the head (a request, a stream, a GSND message, the runtime info)? GSND CONTROL Code `0x05` counts worn buds (🟡,
  `CAP-069` §8 — DLCI 0x08, a channel the app does not open, ADR-043). Runtime info's undecoded fields (top-level 3; 7.x; `qiy` has fields 4–7 only — `PROTOCOL.md`
  §4.3 Option F Note of 2026-10-06). The screen of the official app that shows per-bud wear: which data feeds it?
- **(B) Stream lifecycle.** `CAP-079`: settings pushes in a session before its own subscription (H1 4877, H2 5110); live-dB packets in a new ACL in the other user with
  no request (H4 1038). The official app: `fvp.b()` (session end) cancels the hearing-wellness worker — does anything cancel the pw_rpc calls (a `CLIENT_ERROR`
  CANCELLED, as `CAP-056` 4360 on `WriteSetting`)? Search every capture for phone-side `CLIENT_ERROR` / stream-end packets per method (`pwrpc_decode.py`).
- **(C) Field 21's core.** `CAP-079` §11: `WriteSetting 4:{21:b}` OK on channel 19 (the session's), `FAILED_PRECONDITION` on 21; `FetchDailySummaries`/`SubscribeToLiveDb`
  answered on both. The official app: `hey.java:165–190` (the switch), `fyh.java:28–36` (per-bud Dosimeter calls on `LEFT_BT_CORE`/`RIGHT_BT_CORE`), `fux.java:90–103` and
  `fut.java:178` (channel → address). Which route does a `WriteSetting` take — the "primary route" of `fxk.java:30–35`? `CAP-058`'s field-21 writes (5747 … 5790): on
  which channel, and which channel had the session announced?
- **(D) `SetWallclock` and the day index.** `CAP-041` 783 (`1:<ms>` = the frame's capture time); `CAP-079`: field 2 of runtime info and the announcement appears only after
  it (`PROTOCOL.md` §4.3 Option F Note of 2026-10-18). Where does the official app send it (every connect? which channel/core?), and does it send it per bud? The
  `qhz`/`qia` day index: `CAP-058` 462 on 2026-10-05, `CAP-079` 475 on 2026-10-18 (🟡 day 0 = 2025-06-30) — what the code counts from (`gdg`/`gda`, `hwy.aj`).
- **(E) Find negatives.** `PROTOCOL.md` §4.4: the companion app has no local ring trigger (Find Hub/GMS). A grep of `jadx-output` for ring strings found only
  `FocusRingDrawable` (chat 2026-10-18 — re-derive). The Case: the specification defines `0x01`, `0x02`, `0x03` only; pbpctrl `README.md:52` "the Case has no receiver
  of its own" (external; re-check). Record the negatives with commands and positive controls; no undefined value is proposed for a send.

---

## 3. Rules for this session

- **ADR-017's boundary:** the session searches, reads and reports mechanically; it states what the code does with file and line; **the maintainer decides relevance**
  and every promotion. Short quotations only (`PROJECT_RULES.md` rule 20 (a)); nothing decompiled enters the app.
- **Schemas from `scripts/decode_rawmessageinfo.py`**, not from getter names (`AGENTS.md` §13); field numbers on the wire only from captures.
- **No GMS reverse engineering** (ADR-025); no Find Hub/network path; no guessed opcode or value (`AGENTS.md` §6).
- **Labels on every conclusion** (🟢/🟡/⚪/🔴); a code-side finding is "🟢 (code)" with file:line; a wire finding needs a frame and the command (rule 4a).
- **No new wire traffic, no phone actions, no code change.** Experiments are proposed (exact bytes, the expected answer, the risk), each for its own later approval.

---

## 4. Tasks

### Phase 0 — set-up and Checkpoint 0
1. Create the RESULT. Record `git log -1`, `git status --short`, the branch (this session commits on the branch the maintainer names — ask). Check the APK workspace
   (`ls`, the base APK's SHA-256 against `REVERSE_ENGINEERING.md`).
2. **Checkpoint 0 (`AskUserQuestion`, one question per decision):** which of (A) … (E) run now (A was planned "after 1.4.0 is published"); the branch; whether a
   scratch name table (pw_rpc ids → names) may be regenerated.

### Phase 1 — the code (one question at a time; record each in `REVERSE_ENGINEERING.md` before the next)
3. (A) The per-bud wear chain: the screen(s) that show it, the data source, every request/stream/message on the way (class, method, file:line), the role of OOBE mode,
   `hmv`/`hmw`; what the app would need from the Buds to know the worn bud without OOBE mode.
4. (B) Stream cancellation in the code (pw_rpc client `cancel`, `CLIENT_ERROR`, call close) per Maestro and Dosimeter stream; when the official app subscribes and
   unsubscribes.
5. (C) The routing of `WriteSetting` and of the Dosimeter calls per core; the "primary route"; what decides the core for field 21.
6. (D) `SetWallclock`'s call sites and timing; the day index's origin.
7. (E) The Find negatives (commands, exit status, positive controls; smali included).

### Phase 2 — the wire cross-check (existing captures only)
8. For each code-side answer: the frames in the captures that confirm or contradict it (e.g. phone-side cancels; `SetWallclock` per connect; the channel of the
   official field-21 writes against the announced channel; any per-bud on/off-head message on any channel), with commands and counts.

### Phase 3 — findings, proposals, checkpoint
9. The RESULT's findings per question (labels, file:line, frames) and the proposals: `PROTOCOL.md` notes, `REVERSE_ENGINEERING.md` entries (written in this session —
   documentation of code, no status claim on the protocol), draft ADR texts for a feature (e.g. a per-bud wear line superseding ADR-059's "never per bud"; a
   stream-cancel rule; a field-21 core rule) — **as proposals only** — and capture experiments (exact bytes, expected answers, the risk, the approval each needs).
10. **Checkpoint D (`AskUserQuestion`, one question per decision):** each proposed `PROTOCOL.md` note (draft in the preview), each draft ADR (stand as "(DRAFT —
    proposed, not accepted)" or not at all — `AGENTS.md` §6 Note of 2026-10-15), the experiments for the next FEATURE/CAPTURE round, `TODO.md` changes, commit.
11. With the answers: `REVERSE_ENGINEERING.md`, `PROTOCOL.md` and `DECISIONS.md` only as approved, `TODO.md`, `ai-sessions/INDEX.md` row (0101); `ensure_footers.py`;
    `lint_docs.py` exit 0; the RESULT ends with "Deferred documentation" and "Commits". **Ask whether to commit and push**; only after a yes.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)
- Read-only: no code under `android/` changed, no build, no phone, no Buds.
- No FACT promotion, no ADR, no draft ADR in `DECISIONS.md` without the maintainer's consent in this chat.
- Quotations from decompiled code: a few lines with file and line, where the analysis needs the exact text; never whole methods.
- Privacy: no serial, MAC or account data in any committed text.

## 6. Deliverables
- `REVERSE_ENGINEERING.md` entries for (A) … (E) as run; proposals and experiment designs in the RESULT; approved notes/drafts.
- ai-sessions/0101_CROSSCHECK_RESULT_2026_10_18.md and the `ai-sessions/INDEX.md` row.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0101_CROSSCHECK_PROMPT_2026_10_18.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0101_CROSSCHECK_PROMPT_2026_10_18
