# 0040_FEATURE_PROMPT_2026_09_19.md — On-demand claiming of DLCI 0x04, Battery Option B decoder ADR, HFP-battery research, EQ-read research

**Number:** 0040
**Category:** FEATURE
**Date:** 2026-09-19
**Title:** Claim the shared Message Stream channel (DLCI 0x04) on the user's own ANC/Find tap, unblock the Battery Option B decoder by ADR, and research why HFP battery shows nothing and whether the EQ can be read
**Status:** prompt only — not yet run

---

## 0. How to use this prompt

Read `AGENTS.md` §0.1's order first (`AGENTS.md` → `PROJECT_RULES.md` → `PROJECT.md` → `ARCHITECTURE.md` →
`PROTOCOL.md` → `DECISIONS.md`, all ADRs → `TODO.md`), plus `ai-sessions/INDEX.md` and
`ai-sessions/0039_FEATURE_RESULT_2026_09_19.md` (this task follows directly from its §8 proposals and from the
maintainer's two further test rounds — Google Play services' "nearby devices" permission allowed, then denied).
Log/screenshot files under `android/logs/` are gitignored evidence: cite them by what they show, never by filename
in any committed file. This prompt was given in a chat session, not as a file; it is reconstructed here from that
conversation so the session record is complete (`AI_SESSION_LOG_PROCEDURE.md` §6).

## 1. The maintainer's instruction (chat, 2026-09-19, translated from Dutch)

Context the maintainer gave: on the Pixel 9a (GrapheneOS, sandboxed Play services) two test rounds — with Play
services' "allow access to nearby devices" **on**, the app connects every time but loses ANC/Find within ~5 s; with
it **off**, the app stays connected for over a minute, but eventually disconnected once anyway. The Nearby-devices
permission is also needed for other apps (e.g. Chromecast), and the maintainer does **not** want Play services in a
second profile.

Decisions and requests:

1. **Reversal of an earlier instruction.** The maintainer had said the app must never connect automatically, not
   even when ANC or EQ are changed. That is retracted for the Message Stream channel: *"Ik bedoel 'Lezing 1: de app
   claimt 0x04 zodra ik op ANC of Find druk', waarmee ik inderdaad terugkom op mijn eerdere statement, omdat ik toen
   dacht dat het daadwerkelijk de verbinding met de Pixel Buds Pro 2 verbrak, maar het gaat slechts over het
   kwijtraken van het kanaal 0x04. Dus ik wil dat een ANC- of Find-tik het kanaal zelf claimt."* (Options B/B2 —
   per-channel tolerance with or without manual take-over — were considered and not chosen.)
2. **"een ADR vrijgeven voor de batterijdecoder op 0x04"** — explicit maintainer approval to write an ADR unblocking
   the Battery Option B (`Group 0x03 Code 0x03`) decoder (`AGENTS.md` §6).
3. **"een onderzoek waarom de HFP-batterij op jouw telefoon niets levert"**.
4. **"een apart onderzoek voor EQ-lezen"**.

## 2. Guardrails that stay in force

`AGENTS.md` §1 (no GMS, no network — the on-demand claim must never touch or suppress Play services), §5
(never fabricate a battery value), §6 (an agent may not promote a FACT or write an ADR beyond what the maintainer
explicitly asked for; anything further is a labelled proposal), §9 (no MAC at INFO+, no raw payloads outside Debug
mode), §11 (regression tests for every root cause), and the standing rule on `android/logs/`.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0040_FEATURE_PROMPT_2026_09_19.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0040_FEATURE_PROMPT_2026_09_19
