# 0066_FEATURE_PROMPT_2026_10_02.md — Two app changes before `CAP-067` (balance slider without the `‹`/`›` steps; no `LICENSE` link on Info) and answers to the maintainer's release-preparation questions

**Number:** 0066
**Category:** FEATURE
**Date:** 2026-10-02
**Title:** Remove the balance steps of `ai-sessions/0064` F-2 and the Info tab's "Licence on GitHub" link, keep everything aligned (tests, ADR-050, `CAP-067`
skeleton, documentation), and answer the maintainer's questions on testing without Google Play services, clean release builds, video in the README, the app
name and R8

---

## 0. How this prompt came about

This prompt records a request the maintainer gave **in chat** on 2026-10-02, in the same Claude Code session that ran `ai-sessions/0064` and wrote the
`ai-sessions/0065` prompt. That session had already read `AGENTS.md` §0.1's seven files in full (`AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`,
`ARCHITECTURE.md`, `PROTOCOL.md`, `DECISIONS.md` ADR-001 … ADR-050, `TODO.md` — `ai-sessions/0064` RESULT §O) and every file this request touches. A later
session re-running this prompt reads those seven files first (`AI_SESSION_LOG_PROCEDURE.md` §8).

## 1. The request (chat 2026-10-02, translated from Dutch)

"I do not want to publish an APK release yet, but I do want to start getting the project ready for publishing. And before I run `CAP-067`, I want two
more changes in the app:

- in Settings → Info it is enough to be able to read the licence locally; the link to `LICENSE` in my GitHub project can be removed;
- on the Sound tab I want to go back to the previous version of the volume balance slider, without the `<` and `>` on both sides.

I understand I cannot simply change the Definition of done: I want to know how to install and test the OpenControl app in a profile without Google Play
services, without removing Google Play services from my GrapheneOS phone (that would make other apps need reinstalling). How does building a release from a
clean working directory work, so that the Info tab shows no hash with "-dirty"? How can I show the 15.3 MB video in my README.md? Would converting it to a
GIF help? What options are there for playing video in a README.md? About the name and trademark — the words "Pixel Buds" in the app name may fall under the
trademark ban of `AGENTS.md` §12: what are my options? What is an R8 build, and why would I not want it yet?"

## 2. Rules

The project rules apply unchanged: `AGENTS.md` (no network, no new permission or dependency, Kotlin, AGPL headers), the §6 gate (no ADR or ADR Update without
the maintainer's approval in chat), real-byte fixtures (`AGENTS.md` §11), a green test/lint gate, honest answers with sources (`PROJECT_RULES.md` rule 4a).
Nothing is published; nothing is committed without the maintainer's yes.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0066_FEATURE_PROMPT_2026_10_02.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0066_FEATURE_PROMPT_2026_10_02
