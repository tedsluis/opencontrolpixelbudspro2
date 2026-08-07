# CHANGELOG.md

All notable changes to this project are documented in this file. Format loosely
based on [Keep a Changelog](https://keepachangelog.com/).

This project is currently in the reverse-engineering phase — there is no
working app yet, so entries so far are documentation, tooling, and process
rather than app releases. See `TODO.md` for current status and `PROJECT.md`
for the "definition of done" that will mark v1.

## [Unreleased]

### Added

- Project structure and documentation guard rails set up (`AGENTS.md`,
  `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md`,
  `PROTOCOL_NOTES.md`, `README.md`).
- Fedora 44 workstation setup script/notes for the development environment
  (`WORKSTATION_PREPARATIONS.md`): Claude Code, Google Antigravity, Java 21,
  Kotlin via SDKMAN, Wireshark, Android SDK/adb, JADX, apktool.
- Bluetooth HCI snoop capture procedure documented
  (`CAPTURE_BLUETOOTH_HCI_SNOOP.md`).
- Test plan for mapping user/app/hardware actions to expected Bluetooth
  traffic, sections 1–4 validated against official app/web screenshots and
  official Google support documentation
  (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`).
- Reference screenshots collected for the official Pixel Buds Android app and
  the official web companion app (`SCREENSHOTS_PIXEL_BUDS_APP.md`,
  `SCREENSHOTS_PIXEL_BUDS_WEB_APP.md`).

### Changed

- Nothing yet.

### Reverse engineering findings

- Identified the official Google Fast Pair Service (GFPS) "Battery
  Notification" and "Message Stream: Device Information" extensions as
  officially documented, standard mechanisms that likely cover battery
  reporting — reducing the amount of protocol that needs to be reverse
  engineered from scratch versus initially assumed (`PROTOCOL_NOTES.md`,
  `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`).
- Corrected an earlier assumption of periodic/fixed-interval battery polling:
  per the official Fast Pair spec, battery updates are event-driven (on RFCOMM
  connect or on value change), not polled at a fixed step size.
- Open question raised: whether `libmaestro`'s control channel (ANC/EQ
  commands) shares the same RFCOMM channel as the Fast Pair Message Stream
  under a custom Message Group ID, or is a fully separate channel — still to
  be confirmed via capture.

### Removed

- Nothing yet.