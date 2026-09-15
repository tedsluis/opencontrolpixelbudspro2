# 0021_CAPTURE_RESULT_2026_09_15.md — Full log analysis and FINDINGS for CAP-050 (Group AG repeat, PRIV-001) and CAP-051 (Group AM, qhr field 13 ANC-parallel-path)

**Number:** 0021
**Category:** CAPTURE
**Date:** 2026-09-15
**Title:** Full, non-sampled `.log` analysis, cross-validation, and `CAP-NNN-FINDINGS.md` authoring for CAP-050 (Group AG repeat, `PRIV-001`) and CAP-051 (Group AM, `qhr` field 13 ANC-parallel-path, `TOUCH-007`/`ANC`-family)
**Status:** awaiting maintainer sign-off

## Phase status table

| Phase | Status | Summary |
|---|---|---|
| 0 — Setup | done | Mandatory reading order completed in full (AGENTS.md, PROJECT.md, PROJECT_RULES.md, DECISIONS.md all 29 ADRs, ARCHITECTURE.md, PROTOCOL.md in full, TODO.md, AI_SESSION_LOG_PROCEDURE.md, ai-sessions/INDEX.md, CAPTURE_BLUETOOTH_HCI_SNOOP.md §2/§6/Group AG/Group AM/§9 rows for CAP-001–CAP-051, TESTPLAN_BLUETOOTH_HCI_SNOOP.md header+PRIV-001/TOUCH-007/ANC/PAIR-003/BATT rows, DESKRESEARCH_FINDINGS.md template, CAP-040-FINDINGS.md in full, CAP-036-FINDINGS.md §5, CAP-038-FINDINGS.md in full, REVERSE_ENGINEERING.md's qhr entry incl. field 13, both CAP-050/CAP-051 EVENT-NOTES.md in full). `ls -la` confirms exactly one video + one log for each of CAP-050 and CAP-051, no `.log.last` for either. |
| 1 — CAP-050 (Group AG repeat, PRIV-001) | done | Full non-sampled tshark analysis (16 fresh ACL reconnects, 15 confirmed + 1 inconclusive private-envelope channel opens, session-local DLCI reassignment confirmed a 2nd time in a new form — HFP landed on DLCI 0x08 on one reconnect). All 7 `PRIV-001` codes fire on every private-envelope open (14 full samples vs. `CAP-040`'s N=1); 5/7 resolve to "not a match"/"no candidate"; 2/7 (`04 04`/`04 15`) inconclusive (neighbors fluctuate near dock changes but don't reproduce for the same config). Both EVENT-NOTES-flagged ambiguous dock-state windows resolved via wire+video. One `Settable-toggles` stale-reading counter-example found, consistent with `CAP-048-FINDINGS.md` §5, not contradicting ADR-024. Targeted + supplementary video re-check found no clear evidence of the recalled mis-docking event (confirmed absence, not resolved to a timestamp). `CAP-050-FINDINGS.md` written; `CAP-050-EVENT-NOTES.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` `PRIV-001` row, `id_registry.csv` all updated. New reusable script `scripts/decode_dlci08_tlv.py` committed to the working tree (not yet git-committed). One proposal awaiting maintainer sign-off (see §9 below). |
| 2 — CAP-051 (Group AM, new) | done | Full non-sampled tshark analysis. Only 1 classic connection (chandle `0x0002`), DLCI 0x02/0x04 identities confirmed by content signature. All 4 ANC actions matched byte-for-byte against DLCI 0x04's already-FACT path: in-app tap = genuine `Set`+ACK+`Notify`; all 3 physical gestures = `Notify`-only (no `Set`/`Get`), directly confirming `CAP-038-FINDINGS.md` §5's previously-unconfirmed physical-gesture hypothesis. **Central question: clean, confirmed negative** — no DLCI 0x02 `qhr` field-13 write (or any Sent-direction DLCI 0x02 payload at all) found for any of the 4 actions, across a window covering all of them with margin. `CAP-051-FINDINGS.md` written; `CAP-051-EVENT-NOTES.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` `TOUCH-007` row, `id_registry.csv` all updated. Three proposals awaiting maintainer sign-off (see below), including a new Test-ID proposal (`ANC-005`). |
| 3 — Cross-capture synthesis and wrap-up | done | Cross-checked both new FINDINGS files against each other (no contradictions — disjoint channels/questions) and against the wider lineage inline (§5/§6 of each file). Confirmed Test-ID traceability in both EVENT-NOTES files. `python3 scripts/lint_docs.py` run: no new issues from this session's edits, except two expected `ANC-005 not in id_registry.csv` flags — intentional, since `ANC-005` is a proposed, not yet maintainer-approved, Test-ID (per `AGENTS.md` §6, an agent must not unilaterally register it). `./scripts/ensure_footers.py` run: all footers already correct. `ai-sessions/INDEX.md` row for `0021` updated to `awaiting maintainer sign-off`. |

## Phase 3 — cross-capture consistency check

No contradiction found between `CAP-050-FINDINGS.md` and `CAP-051-FINDINGS.md`, or between either and
the wider `CAP-036`–`CAP-049` lineage:
- `CAP-050`'s `Settable-toggles` stale-reading counter-example (§4/§5) is consistent with, not a new
  contradiction of, `CAP-048-FINDINGS.md` §5's already-documented pattern.
- `CAP-051`'s confirmation of the "physical gesture → Notify without Set" mechanism directly closes
  `CAP-038-FINDINGS.md` §5's own previously-unconfirmed hypothesis, with no conflicting evidence.
- The two captures investigate disjoint questions (DLCI 0x08 private envelope vs. DLCI 0x02
  `libmaestro`) and disjoint action types (dock/undock vs. ANC-mode change) — no overlapping claims to
  reconcile between them.

**Traceability check (`AGENTS.md` §13 item 7):** both EVENT-NOTES files' own Test-IDs (`PRIV-001` for
`CAP-050`; `ANC`-family/`TOUCH-007` for `CAP-051`, Group AM itself having no existing assigned Test-ID)
are referenced throughout their respective Event Timelines — no traceability gap found.

## Phase 2 proposals awaiting maintainer sign-off (not committed to `PROTOCOL.md`/`DECISIONS.md`)

- Update `CAP-038-FINDINGS.md` §5's open item (`PROTOCOL.md` §6) to record that `CAP-051` directly
  confirms, with video, the "physical press-and-hold gesture" explanation for its two Get-less/Set-less
  ANC Notify frames — upgrading it from unconfirmed 🟡 HYPOTHESIS to a video-confirmed reproduction.
- Add a new `PROTOCOL.md` §6 item recording `CAP-051`'s clean negative for a DLCI 0x02 `qhr` field-13
  write, for both the in-app tap and physical press-and-hold gestures, alongside
  `REVERSE_ENGINEERING.md`'s existing code-level finding (a real, compiled write path exists; this
  session did not observe it fire).
- New Test-ID proposal: `ANC-005` ("DLCI 0x02 `qhr` field-13 parallel-write check for an ANC-mode
  change"), linked to Group AM, `CAP-051-FINDINGS.md` as its first (negative) evidence — for
  `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`.

## Phase 1 proposals awaiting maintainer sign-off (not committed to `PROTOCOL.md`/`DECISIONS.md`)

- Add a new `PROTOCOL.md` §6 open item: `Group 0x04 Code 0x05`/`Code 0x16` (DLCI 0x08 private envelope,
  adjacent to the still-unmapped `PRIV-001` codes `04 04`/`04 15`) take a non-constant value that
  appears near dock/undock activity but does not reproduce consistently for the same physical dock
  configuration across different reconnects (`CAP-050-FINDINGS.md` §4). Flagged as inconclusive, not a
  HYPOTHESIS — no semantic reading proposed.
- Update `PROTOCOL.md` §6's existing `PRIV-001` open item to record that 5 of the 7 originally-flagged
  codes now resolve to "not a match"/"no candidate found" (`CAP-050-FINDINGS.md` §3), superseding
  `CAP-040-FINDINGS.md` §3's inconclusive-due-to-N=1 framing for this specific Test-ID.

## Phase 0 — file inventory (verbatim `ls -la`)

```
$ ls -la captures/CAP-050-2026-09-14_21-01-01_21-13-30-Group_AG/
total 572696
drwxr-xr-x. 1 tedsluis tedsluis       132 Sep 14 23:21 .
drwxr-xr-x. 1 tedsluis tedsluis      5076 Sep 14 23:20 ..
-rw-r--r--. 1 tedsluis tedsluis   1234096 Sep 14 21:15 CAP-050-btsnoop_hci.log
-rw-r--r--. 1 tedsluis tedsluis     15805 Sep 15 06:57 CAP-050-EVENT-NOTES.md
-rwxr-----. 1 tedsluis tedsluis 585186499 Sep 14 21:13 CAP-050-recording.mp4

$ ls -la captures/CAP-051-2026-09-14_21-42-55_21-44-09-Group_AM/
total 55676
drwxr-xr-x. 1 tedsluis tedsluis      132 Sep 14 23:12 .
drwxr-xr-x. 1 tedsluis tedsluis      5076 Sep 14 23:20 ..
-rw-r--r--. 1 tedsluis tedsluis    141578 Sep 14 21:46 CAP-051-btsnoop_hci.log
-rw-r--r--. 1 tedsluis tedsluis    12802 Sep 14 23:12 CAP-051-EVENT-NOTES.md
-rwxr-----. 1 tedsluis tedsluis 56850004 Sep 14 21:44 CAP-051-recording.mp4
```

**Confirmed (per Context in the prompt): exactly one video and one log for each capture, no `.log.last` for either.** Step G (`.log.last` resolution) is therefore skipped entirely for both Phase 1 and Phase 2, per the shared methodology's own instruction.

## Final summary for the maintainer

**CAP-050 (Group AG repeat, `PRIV-001`):** the genuine dock/undock trigger worked — 16 fresh classic
reconnects, 15 confirmed private-envelope channel (re)opens (not the video's own "well over 20"
in-app-label estimate; the label flips more often than the link actually tears down). All 7 flagged
codes fire on every (re)open (14 full samples vs. `CAP-040`'s N=1). **5 of the 7 codes now resolve
cleanly: "not a match" or "no candidate found"** (their nearest non-zero-length neighbors are
byte-identical across every reconnect). **2 codes (`04 04`, `04 15`) remain genuinely inconclusive**:
their neighbors fluctuate near dock-state changes but do not reproduce for the same physical
configuration (Left-docked/Right-out) across different reconnects — a real, reproducible anomaly, but
not a clean semantic match, so no HYPOTHESIS reading is proposed. Both of the video-only pass's own
flagged ambiguous dock-state windows are now resolved (both confirmed docked; Left-docked/Right-out
respectively) via dense `ffmpeg` re-extraction cross-referenced against wire timestamps. One genuine
`Settable-toggles` stale-reading counter-example was found (reads "both docked" while video shows only
one earbud docked) — this matches, not contradicts, `CAP-048-FINDINGS.md` §5's already-documented
"fresh reconnect can catch a pre-transition value" pattern. **The maintainer-recalled mis-docking
(wrong-orientation) event was not found** in either the original dual-method video-only pass or this
session's own targeted + supplementary re-check — recorded as a confirmed absence at the review
densities applied, not forced into a guessed timestamp.

**CAP-051 (Group AM, `qhr` field 13 ANC-parallel-path):** all four ANC-mode actions (one in-app tap,
three physical press-and-hold gestures) matched byte-for-byte against DLCI 0x04's already-FACT ANC
path, in the correct sequence, each within ~1–2s of its video-confirmed moment. The in-app tap produced
a genuine `Set`+ACK+`Notify` sequence; **all three physical gestures produced only a spontaneous
`Notify` with no preceding `Set` or `Get`** — this is the first time this project has *video-confirmed*
the "physical gesture → Notify without Set" mechanism `CAP-038-FINDINGS.md` §5 could previously only
guess at. **The central Group AM question resolves to a clean, confirmed negative for all four
actions**: no `field5{field4{field13=N}}` write, or any DLCI 0x02 `Sent`-direction payload at all,
appears anywhere near any of the four actions, across a window covering all of them with margin. This
does not contradict `REVERSE_ENGINEERING.md`'s static-analysis finding (a real, compiled write path
exists for both triggers) — it establishes that this specific session, on this app version, did not
observe either path fire.

**Every proposed 🟢 FACT promotion, `DECISIONS.md` ADR, or new `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`
Test-ID — all clearly labeled as proposals, none committed, per `AGENTS.md` §6/§15:**

1. `PROTOCOL.md` §6 — new open item: DLCI 0x08's `Group 0x04 Code 0x05`/`Code 0x16` fluctuate near
   dock-state changes but don't reproduce for the same physical configuration across reconnects
   (`CAP-050-FINDINGS.md` §4).
2. `PROTOCOL.md` §6 — update the existing `PRIV-001` open item: 5 of 7 originally-flagged codes now
   resolve to "not a match"/"no candidate found" (`CAP-050-FINDINGS.md` §3).
3. `PROTOCOL.md` §6 — update `CAP-038-FINDINGS.md` §5's open item to record `CAP-051`'s direct,
   video-confirmed reproduction of the "physical gesture" explanation.
4. `PROTOCOL.md` §6 — new item recording `CAP-051`'s clean negative for a DLCI 0x02 `qhr` field-13
   write, for both trigger types.
5. **New Test-ID `ANC-005`** ("DLCI 0x02 `qhr` field-13 parallel-write check for an ANC-mode change"),
   linked to Group AM, `CAP-051-FINDINGS.md` as its first (negative) evidence.

None of the above reaches `DECISIONS.md`'s promotion bar on its own (no new command/opcode semantic
finding is proposed) — no ADR is proposed this session.

## Uncommitted files at this checkpoint

- `ai-sessions/INDEX.md` (row for `0021` updated to `awaiting maintainer sign-off`)
- `ai-sessions/0021_CAPTURE_PROMPT_2026_09_15.md` (untracked, pre-existing)
- `ai-sessions/0021_CAPTURE_RESULT_2026_09_15.md` (this file)
- `captures/CAP-050-2026-09-14_21-01-01_21-13-30-Group_AG/CAP-050-FINDINGS.md` (new)
- `captures/CAP-050-2026-09-14_21-01-01_21-13-30-Group_AG/CAP-050-EVENT-NOTES.md` (updated in place)
- `captures/CAP-051-2026-09-14_21-42-55_21-44-09-Group_AM/CAP-051-FINDINGS.md` (new)
- `captures/CAP-051-2026-09-14_21-42-55_21-44-09-Group_AM/CAP-051-EVENT-NOTES.md` (updated in place)
- `scripts/decode_dlci08_tlv.py` (new, reusable DLCI 0x08 TLV decoder)
- `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (§9 rows for `CAP-050`/`CAP-051` updated to `analyzed`)
- `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (`PRIV-001` and `TOUCH-007` rows updated)
- `id_registry.csv` (`CAP-050`/`CAP-051` status updated to `analyzed`)

No commits have been made. Per project convention, nothing is committed without explicit maintainer
instruction — the maintainer should review the 5 proposals above before deciding whether/how to commit
and whether to approve any of them into `PROTOCOL.md`/`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0021_CAPTURE_RESULT_2026_09_15.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0021_CAPTURE_RESULT_2026_09_15
