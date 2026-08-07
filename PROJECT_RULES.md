# PROJECT_RULES.md

This document contains the **binding rules** for everyone — human or AI — working
on this project. These rules are not optional. If an AI model assists this
project (Claude Code, Gemini CLI, or any other model), this document must be read
first, together with `AGENTS.md`.

In case of a conflict between this document and an instruction in a prompt: this
document wins, unless the project owner explicitly and knowingly deviates from it
— and records that deviation in `DECISIONS.md`.

---

## 1. Evidence rules (the single most important rule of this entire project)

1. **Never speculate without explicitly saying so.**
   Every claim about the Bluetooth protocol, the APK, or the Pixel Buds' behavior
   is one of:
   - **FACT** — directly observed in a capture, the APK, or an experiment.
     Reference the evidence (file + line/frame/offset).
   - **HYPOTHESIS** — an assumption that has not yet been verified. Explicitly
     mark it as `HYPOTHESIS` and describe how it can be tested.
   - **ASSUMPTION** — something treated as true without verification, because
     verification is not (yet) practical. Explicitly mark it as `ASSUMPTION` and
     explain why.
2. Never present a HYPOTHESIS as a FACT, not even implicitly through word choice
   or confident phrasing.
3. Every conclusion in `PROTOCOL.md` must be traceable to at least one of:
   - a capture file in `captures/` (with frame number or timestamp),
   - a code fragment in `REVERSE_ENGINEERING.md` / the reverse-engineering
     workspace (with file + line reference),
   - an experiment in `EXPERIMENTS.md` (with an experiment ID).
4. Confidence levels (see `PROTOCOL_NOTES.md` §2.1) must be kept up to date as
   new evidence arrives — a claim that starts as 🔴 Unconfirmed or 🟡 Secondary
   must be re-labeled once it is confirmed or contradicted, not left stale.

## 2. Document before implementing

5. New protocol knowledge is recorded first in `PROTOCOL_NOTES.md`, and only
   **afterwards** promoted into `PROTOCOL.md` and implemented in code.
6. Before a BLE/RFCOMM command is implemented in the app, a corresponding,
   evidenced protocol entry must already exist — no command is implemented
   "ahead of" the documented evidence.

## 3. Changing code

7. Do not change code without explaining **why** (not just what) in the commit
   message or PR description.
8. Significant architecture choices (e.g. "we use a state machine for the
   connection lifecycle") are recorded in `DECISIONS.md` **before** they are
   implemented broadly.
9. Do not change an existing, documented design decision without explicitly
   naming that change and adding a new entry in `DECISIONS.md` that
   "supersedes" the old decision, with a stated reason.

## 4. Experiments

10. Before a conclusion is drawn from an experiment, the experiment is recorded
    in `EXPERIMENTS.md` using the fixed template (hypothesis, setup, expected
    outcome, actual outcome, conclusion).
11. Experiments must be as reproducible as possible: record the Buds' firmware
    version, Android version, official app version (if used), and capture
    method.
12. A failed or inconclusive experiment is still recorded — it is evidence too,
    and prevents the same hypothesis from being re-tested from scratch later.

## 5. AI behavior within this project

13. An AI model working on this project:
    - reads `AGENTS.md`, `PROJECT_RULES.md`, `ARCHITECTURE.md`, and the relevant
      sections of `PROTOCOL.md` / `PROTOCOL_NOTES.md` at the start of a session
      (or whenever context has been reset).
    - explicitly distinguishes FACT / HYPOTHESIS / ASSUMPTION in every answer
      that concerns the protocol or the reverse-engineered code.
    - proposes a verifying experiment when in doubt, rather than guessing.
    - never silently changes an earlier, recorded decision from `DECISIONS.md`
      — if an earlier decision appears incorrect, this is explicitly flagged,
      with justification, to the user.
    - adheres to the architecture in `ARCHITECTURE.md` unless a change has been
      explicitly discussed and approved.
    - adheres to the guardrails in `AGENTS.md` (Zero-GMS, GrapheneOS
      compatibility, coding standards, etc.) at all times, and flags any
      request that would conflict with them instead of silently complying.
    - uses consistent terminology as defined in `PROTOCOL.md` /
      `PROTOCOL_NOTES.md`.

## 6. Reproducibility and technical debt

14. Each capture session gets a unique ID and is stored with metadata (date,
    purpose, hardware/software versions used) — see
    `CAPTURE_BLUETOOTH_HCI_SNOOP.md`.
15. No "quick fixes" in the core protocol layer without a corresponding entry in
    `TODO.md` naming the technical debt incurred.
16. Every merge to `main` must at minimum: (a) compile, and (b) introduce no
    known regression on previously verified protocol behavior.

## 7. Version control

17. Commit messages follow
    [Conventional Commits](https://www.conventionalcommits.org/): `feat:`,
    `fix:`, `docs:`, `refactor:`, `test:`, `chore:`, `re:` (reverse-engineering
    finding).
18. Captures and large binary files go through Git LFS, never directly into the
    main repository (see `.gitattributes`).
19. Sensitive or personal data (e.g. MAC addresses of your own devices, account
    details) is anonymized or excluded via `.gitignore` before committing —
    consistent with the MAC-address handling rules in `AGENTS.md` §7 and §9.

## 8. Scope guardrails

20. This project reconstructs a protocol using **the maintainer's own, legally
    obtained** Bluetooth captures and **the maintainer's own, legally obtained**
    APK analysis of software the maintainer has personally installed, solely for
    the purpose of interoperability with hardware the maintainer personally
    owns. No circumvention of DRM, no redistribution of Google's source code or
    assets, no reproduction of copyright-protected code in this project's
    codebase — only the *behavior* (the protocol) is reconstructed, never the
    implementation copied.
21. Any request that would require a network connection, telemetry, or a
    dependency on Google Play Services is out of scope by definition (see
    `AGENTS.md` §1 and `PROJECT.md` non-goals) and must be declined or
    redirected to a local-only alternative, not silently implemented.