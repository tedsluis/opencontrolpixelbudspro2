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
   - **FACT** (🟢) — directly observed in a capture, the APK, or an experiment.
     Reference the evidence (file + line/frame/offset).
   - **HYPOTHESIS** (🟡) — an assumption that has not yet been verified. Explicitly
     mark it as `HYPOTHESIS` and describe how it can be tested.
   - **ASSUMPTION** (⚪) — something treated as true without verification, because
     verification is not (yet) practical. Explicitly mark it as `ASSUMPTION` and
     explain why.
   - **OPEN QUESTION** (🔴) — genuinely unresolved: neither a specific hypothesis
     nor a working assumption exists yet, only an identified gap in knowledge.
     Explicitly mark it as `OPEN QUESTION` rather than forcing it into one of the
     other three labels. **Formally added 2026-08-20** to match established,
     unanimous practice: every `CAP-NNN-FINDINGS.md` file already used this
     fourth tier, and `PROTOCOL.md`'s own body already used 🔴 sixteen times,
     before this rule caught up to that practice — see `PROTOCOL.md` §0 for the
     canonical legend and `CHANGELOG.md` for the record of this reconciliation.
2. Never present a HYPOTHESIS (or an OPEN QUESTION) as a FACT, not even
   implicitly through word choice or confident phrasing.
3. Every conclusion in `PROTOCOL.md` must be traceable to at least one of:
   - a capture file in `captures/` (with frame number or timestamp),
   - a code fragment in `REVERSE_ENGINEERING.md` / the reverse-engineering
     workspace (with file + line reference),
   - a hypothesis test recorded in the relevant `captures/CAP-NNN-.../CAP-NNN-FINDINGS.md`
     (with a task/section reference within that file).
4. Confidence levels (see the status legend at the top of `PROTOCOL.md`) must
   be kept up to date as new evidence arrives — a claim that starts as
   🔴 OPEN QUESTION or 🟡 HYPOTHESIS must be re-labeled once it is confirmed or
   contradicted, not left stale.
4a. **Hex & script rule:** every decoding of a burst/packet (in `PROTOCOL.md`,
    a `CAP-NNN-FINDINGS.md`, `DESKRESEARCH_FINDINGS.md`, or elsewhere) MUST
    include both (a) the specific terminal/`tshark`/Python command used to
    extract or decode it, and (b) the raw hex bytes it operated on — not just
    the resulting interpretation. This lets anyone re-run the same command
    against the same bytes and independently verify the conclusion, rather
    than having to trust a stated result on faith.
    **Clarification (2026-08-23, closing an ambiguity `AUDIT_REPORT_2026-08-22.md`
    flagged):** when `PROTOCOL.md` restates a finding that already originates
    from a compliant `CAP-NNN-FINDINGS.md` (command + hex present there), a
    frame-number-and-file pointer in `PROTOCOL.md`'s own text satisfies this
    rule for that restatement — `PROTOCOL.md` does not need to duplicate the
    hex/command inline as long as the source file it points to actually has
    them. This rule is not satisfied, in either location, by a conclusion with
    no command/hex anywhere in the chain.

## 2. Document before implementing

5. New protocol knowledge is recorded first in the relevant capture's
   `CAP-NNN-FINDINGS.md`, and only **afterwards** promoted directly into
   `PROTOCOL.md` and implemented in code. There is no intermediate working-notes
   buffer (`PROTOCOL_NOTES.md` was retired 2026-08-15) — agents work straight
   from `CAP-NNN-FINDINGS.md` to `PROTOCOL.md`.
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
9a. **Scope of the non-destructive-update convention:** this "keep the old
    entry, add a dated `Update`/superseding entry" convention applies
    **strictly to `DECISIONS.md` and `PROTOCOL.md`** — documents whose value
    includes showing how understanding evolved over time. It does **not**
    apply to `CAP-NNN-FINDINGS.md` documents: those must be aggressively
    refactored and rewritten to state only the current truth. A
    `CAP-NNN-FINDINGS.md` file is a reference for "what do we currently know
    from this capture," not a changelog — an accumulating trail of
    "Update/Follow-up" addendums defeats that purpose by forcing every reader
    to reconstruct the current state from a stack of corrections. When new
    analysis changes a finding in one of these files, rewrite the finding in
    place; the history of *how* it changed belongs in git history and
    `CHANGELOG.md`, not in the findings document's prose.

    **Grandfather clause, added 2026-08-20:** this rule took effect 2026-08-15
    (see `CHANGELOG.md`). Findings dated before that are not retroactively
    required to be cleaned up unless independently revised for another reason
    — treat them as known, accepted legacy debt rather than a live violation.
    A finding dated on or after 2026-08-15 that still accumulates corrections
    inline is a live violation and should be rewritten in place.

    **Worked example, added 2026-08-20:**

    - ❌ **Non-compliant** (the pattern this rule prohibits): a bullet's
      original text is left standing, and a later analysis is appended
      directly below it as a dated blockquote that reverses or supersedes the
      original claim — e.g. `> **Correction (2026-08-10):** the above is
      wrong, actually X` followed later by `> **Full resolution (2026-08-12):**
      the correction above is *also* incomplete, actually Y`. A reader has to
      read all three layers and manually work out which one is still true.
    - ✅ **Compliant, option A — rewrite in place:** the bullet is edited
      directly to state Y (the current, correct understanding) with its
      evidence, as if it had always said that. Nothing in the document itself
      shows that it used to say X or X-then-corrected-to-something-else; that
      history lives in `git log`/`git blame` and, for anything significant,
      a `CHANGELOG.md` entry.
    - ✅ **Compliant, option B — sequential, self-contained sub-investigations:**
      where a question genuinely needed several distinct capture-analysis
      passes over time (not just a correction to one existing claim), each
      pass gets its own numbered subsection (e.g. `§4`, `§4b`, `§4c`), and
      **each subsection states its own clean, current conclusion** rather than
      assuming the reader has read the previous one — the *reasoning*
      accumulates across subsections (that's fine, it's genuinely new
      analysis each time), but the *conclusion* of the latest subsection is
      never something the reader must reconstruct by comparing it against an
      earlier one.

## 4. Hypothesis tests

Hypothesis testing is logged directly in the relevant capture's
`CAP-NNN-FINDINGS.md`, next to the evidence it tests, rather than in a
document disconnected from the capture it belongs to.

10. Before a conclusion is drawn from a hypothesis test, it is recorded in
    that capture's `CAP-NNN-FINDINGS.md` using a fixed template (hypothesis,
    setup, expected outcome, actual outcome, conclusion).
11. Hypothesis tests must be as reproducible as possible: record the Buds'
    firmware version, Android version, official app version (if used), and
    capture method.
12. A failed or inconclusive hypothesis test is still recorded — it is
    evidence too, and prevents the same hypothesis from being re-tested from
    scratch later.

## 5. AI behavior within this project

13. An AI model working on this project:
    - reads `AGENTS.md`, `PROJECT_RULES.md`, `ARCHITECTURE.md`, and the relevant
      sections of `PROTOCOL.md` at the start of a session (or whenever context
      has been reset).
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
    - uses consistent terminology as defined in `PROTOCOL.md`.

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
    details) is anonymized or excluded before committing — consistent with the
    MAC-address handling rules in `AGENTS.md` §7 and §9 (which govern the
    *shipped app's own runtime logging*, not this repo's research data).
    **Exception, `DECISIONS.md` ADR-010:** this rule does not apply to the
    maintainer's own Bluetooth captures under `captures/CAP-NNN-*/`, which
    intentionally retain real identifiers by informed consent — see
    `CONTRIBUTING.md`'s PII-exception section for the rationale and the
    third-party-contributor rule (which *is* held to this rule in full).

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
22. **Hardcoded-strings exception:** literal string values extracted directly
    from the wire protocol (e.g. `"google-pixel-buds-pro-v1"`, a capability
    identifier confirmed on-the-wire across `CAP-001`/`CAP-002`/`CAP-004`) may
    be hardcoded in the app's source, exclusively where required for protocol
    interoperability. These are not "magic strings" in the usual code-quality
    sense — they are fixed values the peer device expects byte-for-byte, not
    configuration or content that should be externalized, localized, or made
    editable. Do not "clean up" such a literal into a config file, resource
    string, or injected constant unless there's a reason beyond general style
    preference; do not invent or guess a value in this category — every such
    literal must trace back to a specific capture/frame (`PROJECT_RULES.md`
    §1's evidence rule still applies in full). This exception covers wire
    protocol values only — it does not extend to Google-owned trademarks,
    assets, or branding, which stay banned regardless (`AGENTS.md` §12,
    `PROJECT.md` non-goals).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/PROJECT_RULES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/#/PROJECT_RULES
