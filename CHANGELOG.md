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

- Resolved a documentation inconsistency between the absolute no-BLE-scanning
  rule in `AGENTS.md` §7 and the Fast Pair Battery Notification mechanism in
  `PROTOCOL.md` §4.3 Option A, which (read literally) could have caused an AI
  agent to refuse implementing battery reporting entirely. Defined a narrow,
  bounded scanning exception — filtered to the bonded device,
  foreground-triggered, time-boxed, stopped on backgrounding — recorded in
  `DECISIONS.md` ADR-006 and reflected in `AGENTS.md` §7 and
  `ARCHITECTURE.md` §9.1.
- Removed the duplicated, checkbox-synced "open questions" list from
  `TODO.md` in favor of single-source pointers to `PROTOCOL.md` §6
  (protocol-level questions) and `ARCHITECTURE.md` §15 (architecture-level
  questions), after that duplication was identified as the reason the
  scanning-policy question above needed updates in four separate files to
  resolve.
- Tightened the `ProtocolCodec` implementation gate: reaching 🟢 FACT
  confidence on the RFCOMM framing question (`PROTOCOL.md` §2.3) now also
  requires a `DECISIONS.md` ADR recording that determination before
  `FrameEncoder`/`FrameDecoder` implementation may begin, coupled to the same
  trigger already in `AGENTS.md` §6 rather than added as a separate,
  independently-drifting rule (`ARCHITECTURE.md` §2.1).
- Trimmed `ARCHITECTURE.md` §9.1's summary of the bounded scanning exception
  down to a single duiding sentence, removing the near-verbatim repeat of
  `AGENTS.md` §7's exact bounds so there is nothing left in the summary that
  could drift out of sync with the authoritative rule.
- Added an explicit ADR-numbering rule to `DECISIONS.md`'s intro (sequential,
  never reused, checked against existing entries rather than pre-guessed
  elsewhere) to prevent stale hardcoded ADR-number references from
  accumulating in other documents like `TODO.md`.
- Fixed three issues in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` found during review:
  clarified that the pairing/bonding baseline (Group A) is a lightweight
  forget-and-re-pair action, distinct from the destructive, deliberately-last
  factory reset (Group P #16) — `TODO.md` previously conflated the two by
  describing the *first* capture as a factory-reset pairing; corrected the
  conceptually wrong "one continuous `adb bugreport` session" phrasing, since
  `adb bugreport` is a one-time extraction, not a live logging mechanism; and
  added the missing §9 Capture Index (`CAP-NNN` table), which `TODO.md` and
  `PROJECT_RULES.md` rule 14 both already assumed existed.
- Changed `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §2 step 5 to recommend a full
  reboot by default (toggle Bluetooth off/on remains a documented, faster
  alternative once verified reliable on a given phone). Checked this against
  official AOSP documentation and GrapheneOS/Pixel community reports first —
  found no confirmed evidence that the toggle is broadly unreliable on recent
  Android versions, so the change is framed as cost-based insurance (an empty
  capture costs a full re-session; a reboot costs about a minute) rather than
  as a corrected reliability claim.
- Clarified that the passive BLE scan used to capture the Fast Pair Battery
  Notification (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4.1 Group Q #18, `TODO.md`
  Phase 1) is a one-off reverse-engineering technique and does not authorize
  broader scanning in the production app, which stays governed by the
  narrower bounded exception in `AGENTS.md` §7 / `DECISIONS.md` ADR-006.
- Fixed `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5's frame-analysis instructions,
  which generically told every captured frame to be checked against the
  RFCOMM envelope hypothesis (§2). A captured BLE advertisement (the Battery
  Notification) is not RFCOMM traffic and has an unrelated structure — the
  step now branches by frame type, with a `btle` Wireshark filter added
  alongside the existing `btrfcomm`/`btatt` ones, so a battery capture isn't
  force-fit against the wrong structure or wrongly logged as an open
  question.
- Applied a further round of technical review fixes to
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, verified individually before adopting:
  made `btsnooz.py` (Android's own documented bugreport-extraction script,
  confirmed against the current AOSP source docs) the primary log-extraction
  method in §3, keeping the internal-path search as a fallback; softened the
  unsubstantiated "single most common reason" claim about empty captures to
  what's actually documented (a restart is required, full stop — this
  project has no capture statistics yet to back a frequency claim); replaced
  "non-rootable" with the technically accurate "non-rooted production
  build," since Pixel hardware is in fact rootable by other means and only
  the current, as-configured state of these two phones is relevant here;
  downgraded "confirmed by GrapheneOS's own community support" to an
  accurately-labeled community forum report rather than an implied official
  guarantee; clarified that Wireshark recognizing the BTSnoop file format
  does not mean it decodes the proprietary `libmaestro` payload; and
  rewrote the FAQ's encryption entry to distinguish HCI-boundary visibility
  from actual link-layer encryption, since unfamiliar-looking protobuf bytes
  are far more likely than genuine encryption at this capture layer. One
  suggested addition — a note that Android's documentation places the log at
  `/sdcard/btsnoop_hci.log` — was checked against the current official AOSP
  page and not found there (only in lower-quality/outdated third-party
  sources), so it was deliberately **not** added.
- Fixed four experimental-design gaps in `CAPTURE_BLUETOOTH_HCI_SNOOP.md`
  §4, found during review: clarified that "one action per capture window"
  means one user-triggered event, not one frame, since pairing legitimately
  produces a multi-frame burst as its own automatic exchange (Group A #1,
  Pixel 9a §4.2 #1); marked the ~5–10s post-action wait as a heuristic with
  a known failure mode (a delayed response can get misattributed to the
  next action) rather than a guarantee, with a matching mitigation added to
  the Wireshark analysis step; added explicit observation-start/-end
  boundary logging (plus connection and app foreground state) to the
  passive windows in Group L and the Pixel 9a idle observation, so settling
  traffic from a preceding action isn't mistaken for spontaneous traffic;
  and replaced "note whether anything appears on the wire" for Loud Noise
  Protection/Adaptive Audio (Group Q #19–20) with a proper three-way
  outcome taxonomy (traffic observed / confirmed no traffic — itself a
  positive finding for on-device-only behavior / inconclusive), fitted to
  the project's existing 🔴/🟡/🟢 confidence system instead of treating
  absence of captured traffic as automatic proof of on-device
  implementation.
- Fixed a self-contradictory "Android 14+ (API 34)" phrasing in
  `ARCHITECTURE.md` §1 and `README.md` — the "+" implied a minimum-supported
  floor while `ARCHITECTURE.md` §15 already listed minimum supported API as
  an open question. Both now separate the decided compile/target SDK (API
  34) from the still-undecided minimum supported API, fixed in both files
  together so correcting one didn't leave the other newly inconsistent.
- Fixed `TODO.md` undercounting the project's Gradle modules as four
  (`:ui`, `:domain`, `:data`, `:hardware`) and omitting `:app` entirely;
  `ARCHITECTURE.md` §2 defines five modules including `:app` as the
  composition/DI-wiring module.
- Adopted three methodology improvements from a reviewed prioritized capture
  plan: added a Group Z pipeline-validation capture to
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4.1 (verify the whole HCI-snoop →
  bugreport → `btsnooz.py` → Wireshark chain on one trivial action before
  spending the pairing baseline or Find My Buds on discovering a broken
  pipeline); added an explicit cross-command framing check after Find My
  Buds (Group K) so the framing hypothesis isn't promoted toward FACT off a
  single frame that merely resembles the spec's worked example; and
  reordered `TODO.md` Phase 1 to match (pipeline validation → pairing
  baseline → Find My Buds → cross-check → independent battery experiment).
  Declined the plan's suggestion to hardcode specific `CAP-NNN` numbers into
  `TODO.md` — that would reintroduce the same numbering-drift risk already
  fixed for `DECISIONS.md` ADRs; capture IDs are assigned in the Capture
  Index (§9) as work actually happens, not pre-assigned in a task list.

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