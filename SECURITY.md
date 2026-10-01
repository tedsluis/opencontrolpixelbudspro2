# Security Policy

## Scope

This project's attack surface is narrow by design (`AGENTS.md` §1/§2): no
network permission, no telemetry, no cloud account. The main thing worth
security review is **parsing of untrusted Bluetooth input** —
`CodecRouter`/`FrameDecoder` (`ARCHITECTURE.md` §5) processes bytes from a
nearby Bluetooth peer, which is not a trusted input source (a malicious or
malfunctioning peer, or a corrupted transmission, could send malformed
frames). See `AGENTS.md` §11's fuzz-testing requirement for how this is
addressed in code.

What the code does about it (added 2026-09-24, `ai-sessions/0045`): every decoder returns
`MalformedFrame`/`UnidentifiedFrame` instead of throwing; the per-channel frame splitters are reset
whenever a channel opens, closes or is lost, and cap a single frame (1024 data bytes on the Message
Stream channels, 4096 bytes for a pw_hdlc frame), so one corrupted length field cannot stall later
frames; and every write — ANC, Find My Buds, EQ and the settings (ADR-045/046/047) — passes the Safe-Mode gate
(`DECISIONS.md` ADR-042): all need the verified firmware; on the Message Stream (ANC, Find) the same claim must also have
announced the Pixel Buds Pro 2's Fast Pair Model ID; on DLCI 0x02 (EQ, settings) no other Model ID may have been seen
(`BudsRepositoryImpl.writeGate`; corrected 2026-09-30, `ai-sessions/0059` A58-HK-05). A report that one of these can be
bypassed is in scope.

Out of scope: the reverse-engineering research itself (`REVERSE_ENGINEERING.md`,
`captures/`) is not a security-sensitive artifact — it documents protocol
*behavior*, not a vulnerability in Google's software.

## Reporting a vulnerability

Please **do not** open a public GitHub issue for a suspected security
vulnerability (e.g. a crash or memory-safety issue triggerable by a malicious
Bluetooth frame). Instead, use GitHub's private
[security advisory](https://docs.github.com/en/code-security/security-advisories/guidance-on-reporting-and-writing/privately-reporting-a-security-vulnerability)
feature on this repository, which notifies the maintainer without publicly
disclosing details until a fix is available.

Please include:

- The frame/byte sequence that triggers the issue (or a description of how
  to reproduce it), redacted of any real device identifiers.
- The affected component (`CodecRouter`, `BudsTransport`, etc.).
- Impact (crash, hang, incorrect state, etc.).

## Non-goals

This app is a hobbyist reverse-engineering project (see `PROJECT.md`), not a
hardened production system with a dedicated security team or an SLA. Best
effort, no guaranteed response time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/SECURITY.md - https://tedsluis.github.io/opencontrolpixelbudspro2/SECURITY
