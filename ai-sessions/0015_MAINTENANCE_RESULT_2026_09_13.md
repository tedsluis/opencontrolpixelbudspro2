# 0015_MAINTENANCE_RESULT_2026_09_13.md — Investigate and resolve ai-sessions/0010's remaining sign-off items

**Number:** 0015
**Category:** MAINTENANCE
**Date:** 2026-09-13
**Title:** Investigate and resolve ai-sessions/0010's remaining sign-off items
**Status:** complete — both items approved by the maintainer directly in this chat session ("beide
akkoord") and applied

---

## Investigation

`ai-sessions/0010_CAPTURE_RESULT_2026_09_12.md`'s own "Final summary for the maintainer" section
lists 5 items under "Every proposed 🟢 FACT promotion or `DECISIONS.md`-relevant change awaiting
maintainer sign-off." Independently re-checked this list against the "Proposed" section of all 8
underlying `CAP-NNN-FINDINGS.md` files (`CAP-018`, `CAP-026`, `CAP-028`, `CAP-029`, `CAP-045`,
`CAP-046`, `CAP-048`, `CAP-049`) — the list is exhaustive. Of the 8 files' own proposals, only
`CAP-046` and `CAP-048` contain anything beyond a doc-pointer update or a new open-question
recording (both kinds already applied, per `0010`'s own phase table); those two files' remaining
proposals are exactly items 1–3 in `0010`'s list. Item 4 (`HOLD-005`) and item 5 (new open items)
are not FACT/ADR-relevant and need no sign-off — both are already correctly recorded as open.

**Item 1 — already resolved.** `PROTOCOL.md` §4.5.7's Volume Balance range/polarity promotion was
proposed here, then independently re-surfaced and **approved by the maintainer** in the same chat
session that authored `ai-sessions/0013_FEATURE_PROMPT_2026_09_13.md`/`RESULT` (Phase 4),
recorded as `DECISIONS.md` ADR-026. `0010`'s own file has been updated to point to this resolution
rather than re-litigate it.

**Items 2 and 3 — genuinely still open.** Neither has been touched by any session since `0010`
itself. Both are surfaced below.

## Decisions needed

### Item 1 (of this file) — mark the `CAP-037`/`CAP-048` dock-timing anomaly as resolved

**Background:** `CAP-037-FINDINGS.md` §5 flagged an anomaly — one chandle, mid-session, showed a
second "Notify ANC state" frame 18 seconds after the first with no preceding `08 11` Get, and its
`Settable-toggles` value flipped `0xe8`→`0x00`. `CAP-048` (a purpose-built repeat with continuous
physical dock-state video) reproduced the exact same shape and resolved it directly: the video,
extracted at the exact wire timestamp, shows a hand actively placing a bud into the case at that
moment — a genuine real-time dock-state change while the ACL connection stayed open, consistent
with (not in tension with) the already-🟢-FACT `DECISIONS.md` ADR-016 (the second bud's own
docking, 5.4s later, is what then triggers the disconnect, exactly matching ADR-016).

`PROTOCOL.md` §6 already carries this explanation (added 2026-09-12), but the bullet is still
parenthetically marked "(PROPOSAL, awaiting maintainer sign-off)" — this doesn't introduce any new
FACT beyond what ADR-016 already establishes, it only explains a previously-unexplained anomaly
using it, but per this project's own convention the text was deliberately left hedged pending
explicit review rather than self-closed.

**Recommendation:** approve. Remove the "(PROPOSAL, awaiting maintainer sign-off)" hedge from
`PROTOCOL.md` §6's bullet — no `DECISIONS.md` ADR change is needed, since this doesn't alter
ADR-016's own text, only closes a previously-open citation of an anomaly it already explains.

### Item 2 (of this file) — `DECISIONS.md` ADR-024 caveat for two counter-example readings

**Background:** `DECISIONS.md` ADR-024 (🟢 FACT) established `Settable-toggles` as a dock-state
indicator (`0x00`=both docked, `0xe8`=otherwise), video-verified 7/7 at the time. `CAP-048` found
**two** counter-examples in one session: two fresh classic reconnects (`17:44:45`, `17:47:42`)
report `Settable-toggles=0x00` (docked) while the video, checked at essentially the same wire
timestamp, shows the case visibly **empty**. Four other readings in the same session (including two
same-chandle DLCI reopens) are correct — this is not a wholesale contradiction of ADR-024, but a
real, reported-plainly exception `PROTOCOL.md` §6 already carries as a 🟡 HYPOTHESIS (not
reconciled): a fresh reconnect's own Get/Notify might sometimes return a value queried before the
Buds' own firmware has settled on an already-changed physical state.

**What's actually being asked:** whether to leave this exactly as it already is (an open 🟡
HYPOTHESIS in `PROTOCOL.md` §6, no `DECISIONS.md` change) or to also add a dated "Update" to
`DECISIONS.md` ADR-024 itself (per that document's own non-destructive-update convention, the same
pattern ADR-024's own 2026-09-05 "Update" entry already uses) — recording the exception directly on
the ADR, not just in `PROTOCOL.md`'s open-questions list, so a future reader of ADR-024 alone sees
the caveat without having to separately check §6.

**Draft `DECISIONS.md` ADR-024 Update — DRAFT, NOT COMMITTED:**

> **Update (2026-09-13):** two counter-examples found in a single session, not yet reconciled.
> `CAP-048` (`CAP-048-FINDINGS.md` §5, a purpose-built repeat with continuous dock-state video) found
> two fresh classic reconnects (`17:44:45`, `17:47:42`) reporting `Settable-toggles=0x00` (docked)
> while the video, checked at essentially the same wire timestamp, shows the case visibly **empty**.
> Four other readings in the same session (including two same-chandle DLCI reopens, not fresh
> reconnects) are correct — this is not a reversal of this ADR's own dock-state-indicator finding,
> which the same session's other readings continue to confirm. 🟡 **HYPOTHESIS, not confirmed:** a
> fresh reconnect's own Get/Notify may occasionally return a value queried before the Buds' own
> firmware has settled on an already-changed physical state — offered as a testable direction only;
> does not by itself explain why the other four fresh reconnects in the same session read correctly.
> Implementations reading this field on a fresh reconnect specifically (as opposed to a
> same-chandle DLCI reopen) should treat it as usually, not unconditionally, reliable immediately
> after connection.

**Recommendation:** add the Update above to ADR-024 — it costs nothing (this ADR already uses the
same non-destructive-update pattern) and makes the caveat visible to anyone reading the ADR in
isolation, which is exactly the kind of gap `PROJECT_RULES.md` §1's evidence discipline exists to
avoid. The alternative (leave it in `PROTOCOL.md` §6 only) is also defensible if the maintainer
would rather keep ADR text itself limited to fully-reconciled findings — this is a judgment call
about documentation style, not a disagreement about the underlying evidence.

---

## Applied (maintainer approved both, "beide akkoord")

1. **`PROTOCOL.md` §6** — the `CAP-037`/`CAP-048` dock-timing anomaly bullet's "(PROPOSAL, awaiting
   maintainer sign-off)" hedge removed; the bullet now cites this file for the sign-off. No
   `DECISIONS.md` change — ADR-016's own text is unaffected, only the previously-open citation is
   closed.
2. **`DECISIONS.md` ADR-024** — the drafted Update above committed verbatim, dated 2026-09-13,
   citing this file.
3. **`PROTOCOL.md` §8** — a new changelog row added recording both.
4. **`ai-sessions/0010_CAPTURE_RESULT_2026_09_12.md`** — items 2 and 3 marked resolved, pointing
   here; header `Status` updated from "awaiting maintainer sign-off" to `complete`
   (`AI_SESSION_LOG_PROCEDURE.md` §4a).
5. **`ai-sessions/INDEX.md`** — both `0010`'s and this file's own rows updated to `complete`.

Nothing further is pending from `ai-sessions/0010`.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0015_MAINTENANCE_RESULT_2026_09_13.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0015_MAINTENANCE_RESULT_2026_09_13
