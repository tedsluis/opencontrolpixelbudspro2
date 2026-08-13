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
  down to a single explanatory sentence, removing the near-verbatim repeat of
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
- Ran a full cross-file consistency pass over today's changes and found four
  remaining gaps, now fixed: `CAPTURE_BLUETOOTH_HCI_SNOOP.md` referenced the
  wrong filenames throughout (`PROTOCOL-NOTES.md` with a hyphen, 25
  occurrences, and `TESTPLAN_EN.md`, 6 occurrences — both pre-existing, not
  introduced today, but caught while checking overall consistency; every
  other document already used the correct `PROTOCOL_NOTES.md` /
  `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`); `DECISIONS.md` ADR-001 still described
  "four Gradle modules," missing `:app`, the same gap already fixed in
  `TODO.md` but not mirrored here; and `ARCHITECTURE.md` §15's own open
  question still contained the ambiguous "(Android 14+)" phrasing that §1
  had already been corrected to remove.

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
- Restructured the roles of `CAPTURE_BLUETOOTH_HCI_SNOOP.md` and
  `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` to remove the overlap between them
  (Option 1 of the reviewed restructuring options): `CAPTURE`'s Groups A–Q/Z
  are now explicitly framed as **capture scenarios** (how to run a session
  efficiently), not the project's test record; `TESTPLAN` was rewritten into
  a stable **action/behavior catalog** — 70 permanent Test-IDs (e.g.
  `ANC-001`), each with its existence-confidence (kept as a distinct axis
  from protocol confidence), its capture scenario(s), and a thin evidence
  pointer to `PROTOCOL_NOTES.md`/`PROTOCOL.md` rather than a duplicate
  results table. All 67 numbered actions across `CAPTURE`'s groups were
  annotated with their Test-ID, and the Capture Index (§9) gained a Test(s)
  column, closing the chain: Test-ID → Group → `CAP-NNN` capture → frame →
  `PROTOCOL_NOTES.md` finding. Also identified two genuine catalog gaps
  while mapping (`INEAR-004` — bud removed from ear without returning to the
  case, and `GATT-001` — Bluetooth device-detail-triggered GATT discovery),
  neither yet covered by a capture scenario; tracked in `TESTPLAN`'s new §9
  rather than silently added to `CAPTURE`.
- Ran a full information-preservation audit comparing every heavily-edited
  file against its original uploaded version (`AGENTS.md`, `ARCHITECTURE.md`,
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`),
  row-by-row and sentence-by-sentence rather than by memory. Found and fixed
  three genuine content losses, all restored:
  - `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `ANC-002` row had lost its note
    ("Sends a configuration command to the buds").
  - `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `BATT-004` row had lost its specific
    cross-reference to the named `HardwareStatus` hypothesis in
    `PROTOCOL_NOTES.md` §3.1, in favor of a more generic `PROTOCOL.md`
    pointer only.
  - A whole battery-reporting mechanism —
    `BluetoothDevice.ACTION_BATTERY_LEVEL_CHANGED` (API 31+), Android's
    generic OS-level battery broadcast — had been fully dropped from the
    repository during an earlier battery-priority reordering. Restored as a
    new, clearly-labeled supplementary "Option 0" (cheapest to check, not
    yet confirmed to fire for this device) in `PROTOCOL.md` §4.3,
    `ARCHITECTURE.md` §4, and `AGENTS.md` §5, without disturbing the
    existing, well-justified Fast-Pair-first ordering of the confirmed
    options.
  Also confirmed via full-repository grep that no `PROTOCOL-NOTES.md` /
  `TESTPLAN_EN.md` filename errors or stale "four Gradle modules" wording
  remain outside of `CHANGELOG.md`'s own past-tense descriptions of earlier
  fixes. Added `DECISIONS.md` ADR-007, documenting the `CAPTURE`/`TESTPLAN`
  restructuring itself, which had no ADR despite being exactly the kind of
  significant, option-compared decision `PROJECT_RULES.md` rule 8 calls for.
- Ran a second, stricter re-validation of the `CAPTURE`/`TESTPLAN`
  restructuring (ADR-007) against the goals of the chosen option, using
  clause-level bidirectional matching rather than line fingerprints. Found
  and fixed: a dropped epistemic point in the Fast Pair battery callout box
  (the reasoning for why the RFCOMM Message Stream finding raises confidence,
  and the generic-mechanism-vs-Buds-specific caveat); two catalog rows
  (`BATT-004`, `PAIR-002`) that had a capture scenario listed in
  `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` but no corresponding Test-ID annotation
  anywhere in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (both are legitimate
  piggyback observations rather than discrete numbered actions, but lacked
  the annotation that would make that clear rather than looking like an
  oversight); and one formatting inconsistency (a `BATT-004` mention using
  plain code-formatting instead of the `` [`ID`] `` bracket convention used
  everywhere else). Verified after fixes: 74 catalog rows, 68 distinct
  Test-IDs cross-referenced with zero orphans in either direction, all 18
  Group letters matched bidirectionally, zero markdown table column-count
  errors, and no duplicate `CAP`/Test-ID/ADR numbers.
- Reviewed two externally-reported issues against the `CAPTURE`/`TESTPLAN`
  restructuring: confirmed the `BATT-004` "missing from Group A" claim was
  incorrect (the annotation was already present from a prior fix), but
  found and fixed a real, smaller issue underneath it — `TESTPLAN`'s
  `BATT-004` row listed "Group A / Z" as its capture scenario, but Group Z's
  own text never claims to cover it (deliberately, since Z is a throwaway
  pipeline check, not an evidence-gathering scenario) — narrowed the row to
  "Group A" only. The second reported issue (`PAIR-003`'s §5/§9
  cross-reference) was reviewed and found to be working as designed (§9 is
  intentionally a thin index, not a duplicate) — no change made.
- Ran a full formal audit (traceability matrix, technical-correctness check,
  semantic-strength check for must/always/required-style softening) against
  `AGENTS.md`, `ARCHITECTURE.md`, and `PROTOCOL.md` using the same
  exhaustive, non-sampled method as the `CAPTURE`/`TESTPLAN` audit. Found
  and fixed two further gaps: `AGENTS.md` §5's battery-mechanism list had
  restored `ACTION_BATTERY_LEVEL_CHANGED`'s technical detail in an earlier
  round but left the HFP fallback mechanism named only by category, not by
  its specific Android API (`BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT`,
  `AT+IPHONEACCEV`/`AT+XAPL`) as the original had — restored for
  consistency; and `PROTOCOL.md` was missing the original's structured
  "Firmware / Version Compatibility Matrix" table (only the bare firmware
  string had survived) — restored as new §0.1, extensible for future
  firmware versions. Zero semantic-strength softenings found in either
  file (0 of 19 CAPTURE + 10 ARCHITECTURE strength-sentences weakened).
  `DECISIONS.md`, `README.md`, and `TODO.md` have no prior "original" to
  diff against (built from short example templates, not full source
  documents); checked instead for internal consistency — every section
  reference in both files resolves to a real heading. Surfaced one
  repository-wide gap unrelated to the restructuring itself: `EXPERIMENTS.md`
  is referenced as if it exists in seven different files (`AGENTS.md`,
  `PROJECT.md`, `PROJECT_RULES.md`, `PROTOCOL.md`, `README.md`,
  `REVERSE_ENGINEERING.md`, `TODO.md`) but was never actually created,
  despite being listed in the project's original scope. Not created here —
  flagged for a deliberate decision rather than an unrequested new file.
- Fixed a real bug in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3's `btsnooz.py`
  instructions, found via the project's actual first capture (`CAP-001`,
  Group Z pipeline validation — exactly the failure mode Group Z exists to
  catch before it derails a more valuable session). The example command
  assumed the bugreport's internal text file would be named after whatever
  name was passed to `adb bugreport` (e.g. `buds_capture.txt`); in reality,
  that name only applies to the `.zip` — the `.txt` inside always keeps
  Android's own generated name (`bugreport-<device>-<build>-<timestamp>.txt`).
  Also reordered the guidance to check the raw `btsnoop_hci.log` path first
  (simpler, no extra tooling) with `btsnooz.py` as the fallback when that
  path isn't present, based on this real capture confirming the raw path is
  often already there — previously framed the other way around, leaning on
  AOSP's documented recommendation alone without field data to weigh it
  against.
- Formalized "Group R" (forced GATT re-discovery) as a real, documented capture
  scenario in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4.1, following the same
  special-purpose pattern as Group Z rather than the numbered A–Q run-through:
  remove the bond via system settings (not the app's own "Forget," per
  `CAP-001`'s `CAP-001-FINDINGS.md` §6 finding that it doesn't fully clear),
  reconnect via a generic BLE tool instead of the official app, isolate the
  connect-and-discover sequence, and filter on the ATT discovery opcodes
  (`0x08`/`0x09`/`0x10`/`0x11`) during analysis. This gives `GATT-001` in
  `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` a real, dedicated Pixel 7a scenario for
  the first time (previously only exercised on the Pixel 9a session, tracked
  as an open item) — updated its row and removed it from §9's open-items
  list accordingly, and noted `PAIR-001` as incidentally re-exercised by
  Group R's bond removal. Added Group R to the Capture Index's group-letter
  reference note alongside Z. This was written up from an actual capture the
  maintainer had already performed, not designed speculatively.
- Formalized "Group S" (Google Play Services disabled, no Pixel Buds app) as
  a real, documented capture scenario in `CAPTURE_BLUETOOTH_HCI_SNOOP.md`
  §4.1, alongside a new `GFPS-001` Test-ID in
  `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (and the missing `GATT`/`GFPS` prefix
  rows in §0.4, since `GATT-001` was already in use without one). Purpose:
  isolate whether the Fast Pair Message Stream traffic identified in
  `CAP-002` (`CAP-002-FINDINGS.md` §3) is Buds-initiated or driven by Google Play
  Services' phone-side Fast Pair/Nearby logic — directly relevant to this
  project's Zero-GMS goal. The maintainer had already manually confirmed
  that, with the app uninstalled and GMS disabled, pairing still succeeds
  via system settings but the Fast Pair "Connect" half-sheet does not
  appear; `GFPS-001` is recorded as 🔴 not yet captured/analyzed, since that
  UI-level observation doesn't by itself say anything about the RFCOMM
  traffic the Test-ID actually asks about.

### Removed

- Nothing yet.