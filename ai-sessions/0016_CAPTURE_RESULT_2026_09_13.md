# 0016_CAPTURE_RESULT_2026_09_13.md — Full video+log re-analysis and FINDINGS for CAP-043, CAP-044

**Number:** 0016
**Category:** CAPTURE
**Date:** 2026-09-13
**Title:** Full video+log re-analysis and FINDINGS for CAP-043 (Group Q repeat, BATT-002/BATT-003) and CAP-044 (Group AA repeat, SDP-001/SDP-002)
**Status:** complete

> **Status updated 2026-09-24** (`ai-sessions/0045`, 2026-09-24, per `AI_SESSION_LOG_PROCEDURE.md` §4a; 0044 finding S-3): this file's one open
> proposal (the `CAP-044-FINDINGS.md` §5 procedure fix for `SDP-001`) was approved by the maintainer on 2026-09-18 as capture Group AT
> (`CAP-058`, `ai-sessions/0031`; `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AT). `INDEX.md` already said `complete`.

## Phase status

| Phase | Status | Summary |
|---|---|---|
| 0 — Setup | done | Mandatory reading order completed (`AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md` §2/§4.3, `DECISIONS.md` ADR-018/ADR-025 + scan of all ADRs, `TODO.md`, `AI_SESSION_LOG_PROCEDURE.md`, `ai-sessions/INDEX.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group Q/AA + §9 rows, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` BATT-002/003/SDP-001/002 rows, `DESKRESEARCH_FINDINGS.md` template, `CAP-011-FINDINGS.md`, `CAP-033-FINDINGS.md` full, `REVERSE_ENGINEERING.md` `gbm`/`fzd` entries, both `CAP-043`/`CAP-044-EVENT-NOTES.md` skeletons). Confirmed via `ls`: neither `CAP-043` nor `CAP-044` folder has a `.log.last` file — Step G skipped for both phases. |
| 1 — CAP-043 | done | Genuinely clean isolation confirmed (zero classic/RFCOMM/SDP traffic, dissector + raw-byte scan). Own-Buds `0xFE2C` advertisement (RSSI -20..-23dBm) still does not structurally match Option A's Battery Notification layout — second confirmed non-match, closes `CAP-011-FINDINGS.md` §4 by ruling out the active-connection confound. `BATT-003` not exercised (no value change this session). Folder renamed to `CAP-043-2026-09-13_09-50-31_09-53-41-Group_Q`. No FACT promotions proposed. |
| 2 — CAP-044 | done | Full video re-review corrected the draft timeline substantially (actual "Pair" tap at 13:08:07, not 13:07:00; the "app surfaces" the draft saw at 13:08:10 was actually a system Fast Pair account-link dialog, not the companion app). Isolation gap identified and precisely characterized: no confirmed force-stop covers the 2m8s window before the "Pair" tap that triggers the session's one SDP browse. Step 3 (app-open) executed on-camera but produces no second SDP transaction (classic connection never drops — wire-confirmed mechanism). "Default" UUID absent a 5th consecutive time (🟢 FACT-level negative); MAESTRO APP/GSND CONTROL/GSND AUDIO/GFPS RFCOMM channel mappings reproduced byte-for-byte via a new, targeted per-UUID SDP query style (different from CAP-033's generic wildcard browse) — proposed as a HYPOTHESIS-level lead, not promoted. SDP-001 remains 🟡 HYPOTHESIS; a procedure-refinement proposal (on-device process-liveness check) is offered for a possible 3rd attempt, awaiting maintainer decision. Folder renamed to `CAP-044-2026-09-13_13-05-51_13-11-07-Group_AA`. No FACT promotions or DECISIONS.md ADRs proposed. |
| 3 — Synthesis | done | Cross-checked both `CAP-NNN-FINDINGS.md` files for consistency (no conflicts found, both build cleanly on `ADR-018`/`CAP-011`/`CAP-033`). `scripts/lint_docs.py` run — only pre-existing, unrelated noise from other captures/historical `ai-sessions/` files remains (none introduced by this session's folder renames). `scripts/ensure_footers.py` run — fixed one missing footer on this file itself, confirmed no other file needs it. `ai-sessions/INDEX.md` row for `0016` updated to `complete`. |

## Notes

- This is the first run of this prompt (no prior `0016_CAPTURE_RESULT_*.md` existed) — completed in
  one continuous session, no rate-limit/context interruption occurred.
- Working directory had all four relevant capture folders present with real (non-placeholder)
  `.log`/`.mp4`/`-EVENT-NOTES.md` content for `CAP-043`/`CAP-044`, per this prompt's own note that
  both sessions were already run by the maintainer today (2026-09-13) but not yet analyzed/renamed.
- `_sidebar.md` still shows the old placeholder paths for `CAP-043`/`CAP-044` — this file is
  CI-auto-regenerated (`git log` shows repeated "docs: auto-regenerate sidebar [skip ci]" commits by
  a bot, no local generator script for it exists) and is expected to self-correct on the next CI run;
  not hand-edited here to avoid fighting that process, consistent with several other
  not-yet-renamed captures' skeletons (`CAP-030`, `CAP-047`, `CAP-050`–`CAP-052`) showing the same
  lag today.

## Final summary for the maintainer

### `CAP-043` (Group Q repeat, `BATT-002`/`BATT-003`) — clean result, closes `CAP-011-FINDINGS.md` §4

- **Isolation genuinely achieved this time** (unlike `CAP-011`): zero classic connection, zero
  RFCOMM, zero SDP traffic to the Buds' address anywhere in the 306.9s log, confirmed both via
  `tshark`'s dissector and a raw-byte scan.
- **The own-Buds `0xFE2C` Fast Pair advertisement still does not structurally match `PROTOCOL.md`
  §4.3 Option A's documented Battery Notification byte layout** (first byte `0x10`, not `0x00`; no
  `0x33`/`0x34` marker at any offset) — a **second confirmed non-match**, now under genuinely clean
  conditions. This closes `CAP-011-FINDINGS.md` §4's open item: the earlier active-connection
  procedure deviation is ruled out as the explanation, strengthening the alternative reading that the
  Buds Pro 2 simply don't broadcast a Battery-Notification-shaped frame under idle/case-closed
  conditions.
- `BATT-003` was not exercised (no battery value changed during the session, by the procedure's own
  design).
- One minor, unresolved timing note (a ~3s gap between the log's single Bluetooth-adapter reset and
  the video's own displayed toggle state) is flagged but doesn't affect any conclusion.
- **No FACT promotion or `DECISIONS.md` entry proposed** — this is a negative result, strengthening
  an existing HYPOTHESIS, not something to promote.

### `CAP-044` (Group AA repeat, `SDP-001`/`SDP-002`) — still not cleanly closed, but for a new, more precise reason, plus a solid corroborating lead

- **The draft `CAP-044-EVENT-NOTES.md` timeline needed substantial correction**: the actual "Pair"
  tap was at 13:08:07 (not 13:07:00 — a 67s difference), and what the draft described as "the Pixel
  Buds app's Device details/Setup UI automatically surfacing" at 13:08:10 was actually a **system**
  Fast Pair account-link dialog, not the companion app — the app itself doesn't reappear until the
  deliberate step-3 reopen around 13:10:24.
- **A new, more precise isolation gap was identified**: Force-stop does correctly precede Forget this
  time (fixing `CAP-033`'s ordering mistake), but **no Force-stop is confirmed anywhere in the video
  during the 2-minute-8-second window between the in-app Forget and the actual "Pair" tap** that
  triggers this session's one and only SDP browse. Whether the companion app's process was still
  alive in the background during that window is unconfirmed either way.
- Step 3 (opening the app for a baseline comparison) **was** executed on-camera this time (fixing
  `CAP-033`'s other gap) — but it produces **no second SDP transaction**, because the classic
  connection and all RFCOMM channels, once opened at 13:08:09–18, never close again for the rest of
  the log; the app simply attaches to the already-connected session.
- **Positive, corroborating result, independent of the isolation question**: the "default internal
  rfcomm socket" UUID is absent a **5th consecutive time** across independent sessions (a solid,
  repeatedly-confirmed negative). The "MAESTRO APP"/"GSND CONTROL"/"GSND AUDIO"/"GFPS RFCOMM"
  channel-name↔UUID↔RFCOMM-channel mappings `CAP-033` first surfaced all reproduce byte-for-byte in
  this independent session, 14 days later — via a genuinely different SDP query *style* (five
  targeted per-UUID queries here, vs. `CAP-033`'s one generic wildcard browse), which is itself a
  new, interesting data point about what an app/GMS-driven vs. OS-driven SDP interaction might look
  like structurally.
- **Proposal awaiting maintainer sign-off (not committed, per `AGENTS.md` §6):** `CAP-044-FINDINGS.md`
  §5 proposes that a possible 3rd `SDP-001` attempt add an explicit on-device process-liveness check
  (e.g. `adb shell dumpsys activity processes`) immediately before the "Pair" tap, to close the
  specific evidentiary gap this session leaves open — and alternatively proposes that, if a 3rd
  attempt isn't pursued, `SDP-001`'s question be reframed from "does the UUID set differ by
  triggering path" (which this hardware/OS's UI flow may make impossible to isolate cleanly) to "has
  the default UUID ever appeared under any triggering path" (already answered, cleanly, 5-for-5
  negative). **This is the one item in this session genuinely awaiting a maintainer decision** —
  everything else is either a closed negative result or an explicitly-labeled HYPOTHESIS-level
  proposal that doesn't block on sign-off to stand as recorded.
- **No FACT promotion or `DECISIONS.md` ADR proposed.**

### Uncommitted work

All changes from this session are on disk but **not committed** (per this project's convention of
never committing without being explicitly asked): two capture folders renamed (`git mv`), two new
`CAP-NNN-FINDINGS.md` files, both `CAP-NNN-EVENT-NOTES.md` files rewritten, `CAPTURE_BLUETOOTH_HCI_SNOOP.md`
§9 and `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` updated, `id_registry.csv` updated, `ai-sessions/INDEX.md`
updated, and this `RESULT` file created. The maintainer should review before committing, particularly
the one open procedure-feasibility proposal in `CAP-044-FINDINGS.md` §5.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0016_CAPTURE_RESULT_2026_09_13.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0016_CAPTURE_RESULT_2026_09_13
