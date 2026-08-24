# Contributing

This project is currently a solo reverse-engineering effort (see `TODO.md` for
status), but is open source and PRs are welcome. Before contributing, read
`AGENTS.md`, `PROJECT_RULES.md`, and `PROJECT.md` — the same rules that bind
AI agents working on this project bind human contributors too.

## Code contributions

- Follow `ARCHITECTURE.md` for the app's structure; propose architecture
  changes via `DECISIONS.md` before implementing them broadly
  (`PROJECT_RULES.md` §3).
- Every generated/written Kotlin source file includes the standard AGPL-3.0
  file header (`AGENTS.md` §12).
- Commit messages follow [Conventional Commits](https://www.conventionalcommits.org/)
  (`PROJECT_RULES.md` §7).
- No `INTERNET` permission, telemetry, analytics, or GMS dependency in any PR
  (`AGENTS.md` §1) — this is a hard constraint, not a style preference.
- Do not reproduce code from the official Google Pixel Buds app. Only
  protocol *behavior* may be reconstructed, never Google's implementation
  (`PROJECT_RULES.md` §8, `AGENTS.md` §12).

## Protocol/capture contributions (the PII exception)

**This is the one place where third-party contributions are held to a
stricter bar than the maintainer's own work in this repository, and the
distinction is deliberate, not an oversight:**

The maintainer's own existing and future Bluetooth captures (`captures/CAP-NNN-.../`)
intentionally retain real data — MAC addresses, timestamps, device identifiers
— because that data is itself the evidence this project's reverse engineering
depends on, captured from the maintainer's own hardware with the maintainer's
own informed consent to publish it. That is a decision only the maintainer
can make about their own data, and it is not revisited by this document.

A third-party contributor submitting a new capture is in a different
position: it is *their* device, *their* account, potentially *their*
location data, being published into someone else's public repository. Before
opening a PR that adds or modifies anything under `captures/`, or any new
`CAP-NNN-*` files:

1. **Redact MAC addresses** — replace with a consistent placeholder per
   device (e.g. `AA:BB:CC:DD:EE:01` for "phone", `...:EE:02` for "left bud"),
   consistent within the PR so correlation across frames still works.
2. **Strip location data** — GPS coordinates, Wi-Fi SSIDs, cell info, or
   anything else that could reveal where the capture was taken.
3. **Strip account identifiers** — Google account IDs/emails, Fast Pair
   Account Key material, device serial numbers tied to a real purchase.
4. **Do not submit raw, unreviewed bugreports.** Only the extracted
   `CAP-NNN-btsnoop_hci.log` (or `.pcapng`) and your own findings/notes —
   never the full `adb bugreport` zip, which contains far more than
   Bluetooth data (installed app list, other device identifiers, etc.).

A PR that adds capture data without evidence of this sanitization will be
asked to redo it before review continues, not accepted with a promise to fix
it later — once merged, that history is difficult to fully scrub.

## Evidence bar for protocol PRs

Same rules as everywhere else in this project (`PROJECT_RULES.md` §1):

- Every claim is labeled 🟢 FACT / 🟡 HYPOTHESIS / ⚪ ASSUMPTION — never stated
  as more confident than the evidence supports.
- Include capture metadata: firmware version, Android version, official app
  version (if used), capture method (`PROJECT_RULES.md` §4).
- Findings go in a `CAP-NNN-FINDINGS.md` first, referencing the Test-ID(s)
  from `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` involved, per the hex & script rule
  (`PROJECT_RULES.md` §1, rule 4a) — include the exact command used and the
  raw hex bytes, not just the conclusion.
- **A PR does not get to unilaterally promote a finding to 🟢 FACT in
  `PROTOCOL.md`, or add/supersede a `DECISIONS.md` ADR.** Propose it in the
  PR description; the maintainer makes the promotion/ADR decision explicitly,
  same as the restriction that applies to AI agents (`AGENTS.md` §6).

## Reporting a security issue

See `SECURITY.md` — do not open a public issue for a suspected vulnerability.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/CONTRIBUTING.md - https://tedsluis.github.io/opencontrolpixelbudspro2/#/CONTRIBUTING
