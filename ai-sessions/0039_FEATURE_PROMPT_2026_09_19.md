# 0039_FEATURE_PROMPT_2026_09_19.md — Connect flickers back to Disconnected, EQ tab stays empty, inconsistent pairing behavior

**Number:** 0039
**Category:** FEATURE
**Date:** 2026-09-19
**Title:** Investigate and fix flaky Connect (flickers to Disconnected within 1-2s), an always-empty EQ tab, and inconsistent app-vs-OS Bluetooth connection state after `ai-sessions/0038`'s peer-disconnect-detection fix
**Status:** prompt only — not yet run

---

## 0. How to use this prompt

This prompt is meant to be handed to a Claude Code session on its own — read it together with the
mandatory project reading order (`AGENTS.md` §0.1: `AGENTS.md` → `PROJECT_RULES.md` → `PROJECT.md` →
`ARCHITECTURE.md` → `PROTOCOL.md` → `DECISIONS.md` → `TODO.md`) before starting, and follow every
guardrail in `AGENTS.md` throughout (most relevantly here: §6's FACT/ADR sign-off gate, §8's error
model, §9's logging rules, and §11's testing expectations). Read `ai-sessions/INDEX.md` for the
project's session history — this task follows directly from `ai-sessions/0037` (real `Connect`
implementation, a bottom-nav fix) and `ai-sessions/0038` (a self-directed audit that, among other
things, added `BudsTransport.connectionLost` — a new peer-disconnect-detection path that is a primary
suspect for part of what's reported below, see Task 3).

## 1. Context — what the maintainer observed

The maintainer built and tested the app produced by `ai-sessions/0038` on their real Pixel 9a
(GrapheneOS) paired with a real Pixel Buds Pro 2, across two testing rounds (2026-09-18 evening and
2026-09-19 morning). Screenshots, the app's own and the Android system's log output, and two exported
debug-log snapshots (via `ai-sessions/0038`'s new "Export debug log" button) were supplied under
`android/logs/` — same standing instruction as every prior session: information may be taken from
these files into other project files, but **no committed file may ever reference the log/screenshot
files themselves by name**. `android/logs/` is already fully gitignored; do not remove it from
`.gitignore` and do not commit anything from that directory.

Reported behavior, in the maintainer's own words (translated/organized here, not yet root-caused):

1. **Initial state mismatch.** The app's Connection screen showed "Disconnected" even though Android's
   own Bluetooth settings already showed the phone connected to the Pixel Buds Pro 2.
2. **Tapping "Connect" gave inconsistent results across attempts**, including:
   - "Connecting…" appearing, then within about **one second** reverting to "Disconnected" with the
     Connect button available again — while Android's own Bluetooth settings continued to show the
     Buds as connected the whole time.
   - "Connection lost" with a "Retry" button. Tapping Retry sometimes immediately gave "Connection
     lost" again; a later retry sometimes reached "Connected" (battery status shown as unknown), but
     that then reverted to "Disconnected" within about **two seconds**.
3. **An asymmetric case**: starting from a state where Android's Bluetooth settings showed the Buds as
   *not* connected, an app-driven connect attempt resulted in the app showing "Disconnected" — but
   afterward, Android's own Bluetooth settings showed the Buds as connected anyway. A real classic
   Bluetooth connection appears to have been established at the OS level despite the app reporting
   failure.
4. **One successful session**, where the app reached "Connected" and stayed there:
   - The Connection screen stayed on "Connected" the whole time.
   - Switching ANC modes worked and was audible through the Buds.
   - Find My Buds worked — each bud could be made to ring in turn, and stopped on request.
   - **The EQ tab displayed nothing at all** (empty screen, not even the "EQ state unknown" message
     `EqScreen` shows for a `null` gains value — confirm which of these it actually was from the
     screenshot).
   - The Debug screen worked; two debug-log exports were made from it during this session.
5. **A Google Play Services popup appeared during a pairing attempt**: a system notification
   ("Google Play Services needs to show a screen. Tap to allow"), which on tap opened a system popup
   showing a Pixel Buds Pro 2 image with text to the effect of "Pixel Buds Pro 2 appears on devices
   signed in to [the maintainer's Google account]" with "Close"/"Connect" options. This is Android's
   own Fast Pair "available devices" system surface (triggered by Google Play Services being present
   on this particular phone, independent of any code in this app) — **not** something this app's own
   code invokes, requests, or depends on (`AGENTS.md` §1's zero-GMS rule remains fully intact; this
   app has no Play Services dependency and must not gain one). It is included here only as an
   environmental fact that coincided with a pairing attempt and may be relevant to the instability
   above — see Task 6.

## 2. Task

1. Read every screenshot and every log/debug-log file supplied under `android/logs/` for both testing
   rounds, in full. Extract findings into `CAP`-style evidence-based notes in this session's own
   `RESULT` file or wherever most appropriate — never by filename reference (per the standing rule
   above), only by what the evidence actually shows (timestamps, exact status text observed, exact
   log lines).
2. Build a precise timeline for each connect/pair attempt: correlate the app's own `BleLogger`
   connection-event log lines (always-on, per `AGENTS.md` §9), the Android system log, and the
   screenshots' own timestamps. For each attempt, determine the **exact** state sequence
   (`ConnectionState` transitions) and, for each transition into `Disconnected`/`Failed`, which code
   path caused it — `BudsRepositoryImpl.connect()`'s own `BudsResult.Failure` branch,
   `disconnect()`'s explicit call, or the new `transport.connectionLost` collector added in
   `ai-sessions/0038`. Do not guess — cite the actual log evidence for each claim
   (`PROJECT_RULES.md`'s zero-creativity rule, `AGENTS.md` §13's capture-analysis discipline applied
   here to app logs instead of Bluetooth captures).
3. **Primary suspect**: `ai-sessions/0038` added `BudsTransport.connectionLost`, emitted from
   `RfcommBudsTransport`'s per-socket reader coroutine when an `IOException` occurs while the
   coroutine is still `isActive`. Investigate whether a *benign* condition on one of the two DLCI
   sockets (e.g. the peer briefly not being ready to accept data on one channel, a slow SDP
   resolution on the second socket while the first is already open, or any other real but non-fatal
   condition) is being misidentified as "the peer dropped the link" and tearing down the *entire*
   connection state even though the underlying OS-level Bluetooth connection is fine — this would
   directly explain the "Connected"/"Connecting…" flicker back to "Disconnected" within 1-2 seconds
   while Android's own Bluetooth settings keep showing a live connection. If confirmed, fix it —
   possible directions to evaluate on the evidence (do not commit to one without log support):
   tightening what counts as a genuine connection-loss signal (e.g. requiring the *first* read to
   fail rather than any read, or distinguishing "socket never became readable" from "socket was
   readable and then failed"), per-channel failure tolerance (one DLCI failing does not necessarily
   mean the whole connection is dead), and/or clearer `BudsError` categorization so a benign one-shot
   glitch doesn't read identically to an actual peer disconnect.
4. Determine why the EQ tab showed nothing in the one successful session while ANC/Find My Buds
   worked. ANC and Find My Buds go over DLCI 0x04 (Fast Pair Message Stream); EQ goes over DLCI 0x02
   (`MAESTRO`, `Dlci.MAESTRO`) via a completely separate `BluetoothSocket`
   (`RfcommBudsTransport.connect()` opens one socket per channel). Check whether the `MAESTRO` socket
   ever actually opened and stayed open in that session's logs, independent of the Message Stream
   socket — if it silently failed to open (or opened and then closed) while the Message Stream socket
   kept working, that would fully explain "ANC/Find work, EQ doesn't" without needing any EQ-specific
   bug at all.
5. Determine why the app's connect flow sometimes could not detect (or reuse) an already-OS-connected
   device, and why an app-driven connect attempt could apparently trigger a real OS-level Bluetooth
   connection while the app itself reported failure. Check `BudsCompanionPairing.bondedDevice()` (is
   *bonded/paired* being conflated anywhere with *currently connected*?) and whether
   `RfcommBudsTransport.connect()` has any way to notice or reuse an already-live ACL/RFCOMM link
   versus always attempting a fresh `createRfcommSocketToServiceRecord()` + `BluetoothSocket.connect()`
   pair, which is a different thing from an already-connected classic ACL link at the OS level.
6. Document the Google Play Services Fast Pair popup as an environmental factor: check whether its
   timing in the logs coincides with any of the instability above. Do **not** write any code that
   integrates with, suppresses, or works around Google Play Services or Fast Pair's own system UI —
   that would violate `AGENTS.md` §1's absolute zero-GMS rule. If it turns out to be a genuine
   contributing factor (e.g. two independent pairing/bonding attempts racing on the same device), the
   correct fix lives entirely on this app's own side (e.g. more defensive handling of an
   already-in-progress OS-level bond/connect), never by touching anything Play-Services-related.
7. Audit which connect/reconnect flows this app actually needs, and check each is genuinely present
   and correctly wired: (a) first-ever connect after pairing, (b) retry after an explicit failure,
   (c) reconnect after a genuine peer-initiated disconnect (`ai-sessions/0038`'s `connectionLost`),
   (d) app launch/resume when a device is already bonded but not connected, (e) app launch/resume when
   a device is already bonded **and already connected at the OS level** (this is case 1/3 above — the
   app currently has no way to notice this without the user tapping Connect, and per the evidence
   above, even tapping Connect may not handle it correctly). Call out explicitly any of these that
   turn out to be missing or broken, rather than only fixing what's already visibly broken.
8. Fix every root cause actually found, with evidence. If a fix would require a protocol-level fact
   that is not yet 🟢 FACT in `PROTOCOL.md`, do not implement it — flag it as a proposal per
   `AGENTS.md` §6 instead (never independently promote a finding or write a `DECISIONS.md` ADR).
9. Improve on-screen status and usability — the maintainer explicitly asked for this. Judgment calls
   are yours, but concretely consider: showing the specific `BudsError` reason rather than a generic
   message wherever one is available; not silently flashing "Connecting…"/"Connected" back to a bare
   "Disconnected" + Connect button within 1-2 seconds with no visible explanation of what happened;
   making the EQ tab's "no data yet" state unambiguous and distinct from "something is actually
   broken"; and anything else this investigation surfaces.
10. Add regression tests for every root cause found, wherever testable without real hardware —
    matching this project's existing `FakeBudsTransport`/`ConnectionStateMachine` unit-test patterns
    (`AGENTS.md` §11).
11. Re-verify with the full suite: `cd android && ./gradlew assembleDebug testDebugUnitTest test lint`.
    Fix anything it catches.
12. Write clear, concrete re-test instructions for the maintainer covering both the specific scenarios
    reported here and the general connect/reconnect/ANC/EQ/Find-My-Buds flows, structured so each
    individual fix can be confirmed or refuted on its own rather than only re-testing "does Connect
    work now."
13. Update `TODO.md` (check off/add entries for whatever is fixed, add any newly-found and
    deliberately-deferred item to "Known technical debt" or the open-items list as appropriate), log
    this session's result as this same prompt's paired `RESULT` file per
    `AI_SESSION_LOG_PROCEDURE.md` §1 (same number `0039`, category `FEATURE`, today's date), update
    `ai-sessions/INDEX.md`, and record a `CHANGELOG.md` entry — matching the style of the
    `ai-sessions/0034`-`0038` entries already there.
14. Commit and push once verified. Confirm `git status` shows nothing from `android/logs/` or any
    build directory staged before committing.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0039_FEATURE_PROMPT_2026_09_19.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0039_FEATURE_PROMPT_2026_09_19
