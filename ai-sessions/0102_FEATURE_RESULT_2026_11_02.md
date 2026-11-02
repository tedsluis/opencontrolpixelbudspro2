# 0102_FEATURE_RESULT_2026_11_02.md — Gen 1 support made ready for a pull request: `CAP-081`, the code fixes it showed, three draft ADRs

**Number:** 0102
**Category:** FEATURE
**Date:** 2026-11-02
**Title:** The contributor's Gen 1 branch brought up to the project's rules: a registered, redacted capture of the official app's and this app's Gen 1
writes (`CAP-081`), the code changes that capture required, `PROTOCOL.md` proposals and three draft ADRs for the maintainer.
**Status:** awaiting maintainer sign-off — ADR drafts D-1 … D-3 below and the `PROTOCOL.md` §4.6 promotions; nothing is accepted.

---

## Summary

1. **Gap analysis** (the prompt's first question): the branch enabled Gen 1 writes with no official-app capture of any of them, against `AGENTS.md` §6's
   per-feature gate and `PROJECT.md`'s non-goal "other Pixel Buds models"; its fixtures came from an unregistered logcat holding the Buds' serial numbers in
   plain text; `GEN1_SUPPORT_INVESTIGATION.md` failed `scripts/lint_docs.py` (dead links, no footer). The Android CI command passed.
2. **Capture `CAP-081`** (Group BQ): part 1, the official app on a Pixel 4 XL changing every Gen 1 setting; part 2, this app's build on a Pixel 9 Pro doing
   the same; the official app then read back this app's mode and multipoint. Redacted with a new `scripts/redact_btsnoop.py` (frame numbers kept).
3. **What it showed, and the code changes** (all in this pull request):
   - every official-app Gen 1 write on Maestro is byte for byte this app's encoder output → new `Gen1OfficialAppWritesTest` (22 frames);
   - **press-and-hold**: the official app writes one bud per write; the branch wrote both buds in one → the Gen 1 now uses the Pro 2's per-bud encoder,
     the both-buds encoder is deleted;
   - **Ring both**: not offered by the official app on the Gen 1 → hidden on the Find tab and refused by the gate (`WriteTarget.RingBoth`) for the Gen 1;
   - **noise-control mode and multipoint**: the official app uses signed Message Stream commands; this app's `WriteSetting` 13/11 are accepted and read back
     → kept, marked `TODO(verify)`, proposed in D-3;
   - the Gen 1 fixtures of `Gen1SupportTest`/`BudsRepositoryImplTest` moved from the logcat to `CAP-081` frames;
   - `scripts/pwrpc_decode.py`: a truncated fixed32/fixed64 field crashed it on part 1's log; fixed.
4. **Documents**: `CAP-081` findings, event notes, hashes; `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group BQ, index row), `id_registry.csv` (`CAP-081`),
   `PROTOCOL.md` (§0.1 row, new §4.6, changelog row — all 🟡 proposals), `GEN1_SUPPORT_INVESTIGATION.md` (lint fixes, a status note naming what `CAP-081`
   superseded; its §11 on the Assistant channel taken out of this pull request), `CHANGELOG.md` [Unreleased], `TODO.md` §5.
5. Removed from this pull request: `scripts/btsnoop_rfcomm.py`, `scripts/gsnd_control_decode.py` (the Assistant research, §11) and
   `scripts/decode_logcat_frames.py` (the logcat is no longer evidence). They stay in the contributor's branch history.

## Draft ADRs for the maintainer (not in `DECISIONS.md`; numbered on acceptance — the next free ADR numbers after ADR-070)

### D-1 — Support the original Pixel Buds Pro (Gen 1) as a second model (DRAFT — proposed, not accepted)

- **Context**: `PROJECT.md` "Non-goals": no other Pixel Buds models "unless the protocol is demonstrably identical — this must be separately verified, never
  assumed". `CAP-081` verifies it for the Gen 1 setting by setting: the same `maestro_pw.Maestro` service, channels and `WriteSetting` shapes (22 official
  frames = this app's), with three differences (no Adaptive/27/28/29, a three-boolean hold list, no Ring both) and two settings the official app writes another
  way (mode, multipoint — D-3).
- **Decision (proposed)**: support exactly one more model, the Pixel Buds Pro (Gen 1), on firmware `release_5.11_signed`; one pair of Buds at a time stays
  (`ARCHITECTURE.md` §15). `PROJECT.md`'s non-goal gets a dated note (`PROJECT_RULES.md` rule 9). The app name stays (ADR-051); whether the README and the
  compatibility line name the Gen 1 is the maintainer's call.
- **Consequences**: a Gen 1 hardware run joins the release checklist; Gen 1 firmware updates go through Safe Mode like the Pro 2's.

### D-2 — Device profiles and Gen 1 identification; amends ADR-042 (DRAFT — proposed, not accepted)

- **Context**: ADR-042's gate allows writes only for `release_5.203` and the Model ID `da2db1`. A Gen 1 needs its own firmware and its own set of writes.
- **Decision (proposed)**: (1) a profile per model (`DeviceProfiles.kt`): its firmware, device type (`GetHardwareInfo` field 1), Model ID, read sweep and
  allowed write targets. (2) The gate allows a write only when every announced firmware belongs to one profile and that profile allows the target. (3) Gen 1
  = firmware `release_5.11_signed` **and** device type 4 read on the same connection (a Gen 1 write waits for it, bounded like the firmware wait); Find also
  needs Model ID `6edaf7`. (4) The Pro 2 keeps every current rule; additionally a Pro 2 firmware with a read device type other than 6 is refused. (5)
  Anything else stays Safe Mode.
- **Evidence**: `CAP-081` §1 (A 915/931/568, B 1458/1591); the Pro 2's device type 6, `REVERSE_ENGINEERING.md` 2026-10-06 Update.

### D-3 — Gen 1 writes (DRAFT — proposed, not accepted)

- **Decision (proposed)**: on a Gen 1 the app writes settings 2, 4, 7 (one bud per write), 11, 12 (three booleans), 15, 16, 17, 19, 22 and the mode (13: 1–3),
  and rings Left or Right; never Adaptive, 27, 28, 29, Ring both or any other setting.
- **Evidence**: `CAP-081` §2 (official frames = encoder output, `Gen1OfficialAppWritesTest`), §4 (43 writes by this app, 43 OK), §3 (Find).
- **Caveat to decide on explicitly**: for the mode and multipoint the official app sends signed Message Stream commands (`08 12`, `07 12`), not `WriteSetting`.
  This app's `WriteSetting 4:{13:n}` (ADR-067's mechanism) and `4:{11:b}` (ADR-053's) are accepted by the Gen 1, the mode is pushed back on the settings
  stream, and the official app on another phone then showed both values (⚪ by eye, not filmed). Options: (a) accept with the `TODO(verify)` and a filmed
  read-back in the first Gen 1 hardware run (proposed); (b) make both read-only on the Gen 1 until then.
- **`PROTOCOL.md`**: promote §4.6's rows to 🟢 except the two caveat rows (🟢 for "accepted by the Buds", 🟡 for "equivalent to the official app's command").

## Verification

Run on the final tree (commands and results):

- `cd android && ./gradlew assembleDebug testDebugUnitTest test lint` → `BUILD SUCCESSFUL` (see the pull request's CI for the same jobs).
- `python3 scripts/lint_docs.py` in a full checkout of `main` with this pull request's files copied in → exit 0, the same output as `main` alone
  (`diff` of the two outputs: empty). (In a sparse checkout without `captures/` every capture link reads as dead — not a finding.)
- Redaction: `python3 -c "…m in d or m[::-1] in d"` for the three addresses on both committed logs → `False` ×6; `strings -n 6 … | grep -i -E
  "cameron|sydney|australia|LZAH"` → no output; the `WriteSetting` lists of the redacted and unredacted logs → identical (`diff`, exit 0).

## Deferred (`TODO.md` §5)

- "Gen 1 (`CAP-081`, `ai-sessions/0102`; only if the maintainer accepts the Gen 1 ADRs of that pull request): (a) the noise-control mode and multipoint …
  (b) `README.md`, `PROJECT.md` …, `ARCHITECTURE.md` §5a … and `RELEASING.md` … are not updated yet … (c) the Gen 1 tests still use two third-party frames …"
- The Assistant channel (the branch's §11 and its scripts) — a separate pull request, if the contributor wants one.

## Commits

To be back-filled by the next session (`AI_SESSION_LOG_PROCEDURE.md` §4b item 2).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0102_FEATURE_RESULT_2026_11_02.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0102_FEATURE_RESULT_2026_11_02
