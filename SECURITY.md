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
