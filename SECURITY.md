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

What the code does about it (added 2026-09-24, `ai-sessions/0045`; corrected 2026-10-03, `ai-sessions/0069`): every decoder is written to return
`MalformedFrame`/`UnidentifiedFrame` instead of throwing — **in 1.0.0 two length checks did not hold that promise** (a declared length near the top of
its integer range overflowed the check and the reader threw or looped; found by the `ai-sessions/0068` audit, fixed in 1.0.1). Since 1.0.1 the router
also decodes each frame inside a guard, so a reader that fails costs that one frame and never the stream, and the fuzz tests mutate real captured frames
*inside* their payload and re-seal them with a valid CRC (random bytes rarely get past the CRC, so the older fuzz tests did not reach those readers); the per-channel frame splitters are reset
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

## Supported versions

Only the latest release gets fixes (added 2026-10-03, `ai-sessions/0069`). There are no maintained older branches: a fix is a new release, installed
over the old one.

## Verifying a release

Every release APK is signed with the same key. `apksigner verify --print-certs <file>.apk` must show this certificate SHA-256:

`a7530f5ceadcdfc9cddcbb0e9889e7699961e8a4ef18898c4ec7738cc79d8dcb`

An APK that shows another value was not built by this project's maintainer — do not install it, and please report where you found it.

## Reporting a vulnerability

Please **do not** open a public GitHub issue for a suspected security
vulnerability (e.g. a crash or memory-safety issue triggerable by a malicious
Bluetooth frame). Instead, use GitHub's private vulnerability reporting — **Security → Report a vulnerability** on this repository (enabled) — the
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
