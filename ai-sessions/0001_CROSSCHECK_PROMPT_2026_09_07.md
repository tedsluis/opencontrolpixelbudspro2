# 0001_CROSSCHECK_PROMPT_2026_09_07.md — Deep V1-without-GMS iterative cross-check (2nd pass)

**Number:** 0001
**Category:** CROSSCHECK
**Date:** 2026-09-07
**Title:** Deep V1-without-GMS iterative cross-check (2nd pass)

> **Reconstruction note:** `ai-sessions/` and this logging convention (`AI_SESSION_LOG_PROCEDURE.md`)
> did not exist yet when this session ran — its prompt was given directly in a chat conversation and
> was never saved verbatim to a file. This `PROMPT` file is a **reconstruction**, assembled from the
> task instructions the paired `0001_CROSSCHECK_RESULT_2026_09_07.md` file itself quotes, restates,
> and works from throughout (its Phase 0 reading list, its "per this task's instructions"/"per this
> task's own instruction" references, and its Work-plan checklist). It is not a byte-for-byte replay
> of the original prompt text. Entry `0001` is bootstrapped this way, by explicit maintainer
> instruction, so this new system starts from real content instead of an empty registry — later
> entries are logged from the actual prompt text going forward.

---

## Reconstructed task instructions

Perform a deep, iterative re-investigation of the V1-without-GMS scope, cross-checking every
relevant Bluetooth capture against the decompiled companion-app APK source. This is a second, more
thorough pass at what `AUDIT_REPORT_2026-09-07.md` Phase 1 attempted in a rushed, single-pass form
(two of three planned research passes had failed mid-run on a session-wide rate limit; the surviving
pass was a single-pass manual investigation, not the broader iterative search a dedicated pass would
run).

**Operational constraints:**
- Work sequentially, in one continuous session — no parallel sub-agent/Task-tool passes.
- Maintain a running progress file (`DEEP_CROSSCHECK_PROGRESS_2026-09-07.md`), appended after every
  phase and after any individually time-consuming step, so a resumed session can read it first and
  continue from wherever it stopped, never restarting a phase already checkpointed there.
- **AI-assistance boundary throughout, per `DECISIONS.md` ADR-017:** search, list candidates, and
  explain already-surfaced code — never decide relevance, never record a finding directly in
  `REVERSE_ENGINEERING.md`, never self-promote anything to 🟢 FACT, never write or amend a
  `DECISIONS.md` ADR. Everything produced is a proposal for maintainer review unless explicitly
  marked otherwise.

**Required reading order at session start**, per `AGENTS.md` §0.1: `AGENTS.md` (full),
`PROJECT_RULES.md` (full), `PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md` (full, including its
changelog and open items), `DECISIONS.md` (every ADR, ADR-001 through the most recent — with extra
care on the ADRs most relevant to the GMS-boundary/DLCI-0x02 questions this pass investigates),
`TODO.md`. Plus, for this specific task: `REVERSE_ENGINEERING.md` (full), the reverse-engineering
procedure and version-tracking docs, the capture index in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9, the
Test-ID catalog, `DESKRESEARCH_FINDINGS.md`, and `id_registry.csv`.

**Work plan — the questions this task needs to answer:**

- **Phase 1 (GMS-boundary Q1–Q3 re-investigation):** go beyond the prior audit pass's keyword-only
  grep. Q1: search for any AIDL-generated interface / `Binder`/`ServiceConnection`/callback
  interface with an ANC-state/settings-notification-shaped method signature, by structural pattern,
  not just keyword. Q2: for any class that only *receives* an already-decoded domain event, trace
  its registration/subscription call site to determine which system component it actually registers
  with. Q3: check whether `apktool-output/AndroidManifest.xml` declares any binding to a GMS-side
  service relevant to this boundary. Produce a per-question verdict.
- **Phase 2 (deepen DLCI 0x02 / `libmaestro` tracing):** trace which `maestro_pw.*` services fire
  specifically inside the connect-time burst; trace `frb.java`'s `"primary route change"` callback
  further; fully resolve `fsz.java`'s shared-dispatcher structure (trace `WriteSetting`'s actual
  caller through the R8-merged-lambda dispatcher rather than leaving it as an acknowledged gap);
  trace `qhr` field 13's ANC-write call site trigger; apply ADR-019's static-analysis method to `qhr`
  field 11 (Multipoint) and field 15 (Volume EQ), the two fields `TODO.md` explicitly flags as
  unchecked.
- **Phase 3 (per-V1-feature confirmation pass):** for every V1-scope feature in `PROJECT.md`'s
  functional checklist (battery, ANC, EQ, touch controls, head gestures, firmware/serial, Find My
  Buds Left/Right vs. Case/"both", in-ear detection, multipoint, case sounds), state which evidence
  is DLCI-0x02/companion-app-code-backed versus DLCI-0x04/0x08/GMS-boundary, incorporating whatever
  Phase 1/Phase 2 turn up.
- **Phase 4 (write-up):** draft (as proposals only, per the AI-assistance boundary above) proposed
  `PROTOCOL.md` §6 updates, proposed `REVERSE_ENGINEERING.md` updates, proposed `TODO.md`
  closures/reprioritizations, and a draft `DECISIONS.md` ADR-025 Update note — none of it committed
  directly.

**Deliverable:** the progress file itself, ending with a summary for the maintainer covering what's
now more strongly confirmed, what's newly found, what changed from the previous pass's conclusions,
and what's proposed and awaiting maintainer sign-off.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0001_CROSSCHECK_PROMPT_2026_09_07.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0001_CROSSCHECK_PROMPT_2026_09_07
