# 0070_CAPTURE_RESULT_2026_10_04.md — Full analysis of CAP-068 (Group BD: the 1.0.1 release APK in a GrapheneOS profile without Google Play services — the hardware test of the 0069 hotfix)

**Number:** 0070
**Category:** CAPTURE
**Date:** 2026-10-04
**Title:** Fully analyse `CAP-068` (two films, four HCI snoop logs, five app debug exports, one app logcat, no system log), recorded with the release-signed 1.0.1 APK in a GrapheneOS secondary user without Google Play; record the real events and the findings; give the release verdict for 1.0.1 and the capture evidence for Definition-of-done criterion 2
**Status:** complete — release of `e1fc886` as 1.0.1 approved in chat; Definition of done 2 now with frames; not committed yet (awaiting the maintainer's word)

---

## Summary — plain answers

- **Release verdict (`RELEASING.md` C4): release the tested build `e1fc886` as 1.0.1** — the maintainer's choice in chat 2026-10-04 (`AskUserQuestion` "Verdict",
  *"Release e1fc886 as 1.0.1 (Recommended)"*). No heavy defect: no crash or ANR in the app's buffers, every write acknowledged (12 ANC `Set`, 6 EQ
  `WriteSetting`), Safe Mode let the writes pass on `release_5.203`, all 15 session ends recovered, no frame on DLCI 0x08/0x0a, no "Decoder fault" or
  "Unexpected error". Known issue (now in `CHANGELOG.md` `[1.0.1]` Known limits): after a ring is stopped by touching the bud, the app keeps saying it is ringing
  until Stop is tapped.
- **Definition of done:** criterion 2's ANC change is now **captured**: 12 `Set` → 12 ACK, 0 NAK, from the ANC tab and the Quick Settings tile, on film, by the
  release build in the user without Play services; 0 Play-services claims in all four logs (positive control `CAP-066` 26). `PROJECT.md` row 2 and the
  `RELEASING.md` 1.0.0 exception updated (maintainer-approved, "DoD text"). Criteria 1 and 3 hold again (connects; 15 session ends recovered).
- **What works on 1.0.1 (the `A68-APP-*` fixes on hardware):** values from the last connection dimmed with the dot and the tile "Open the app" (A68-APP-02, S1/S2);
  the Case "last seen 07:25:14 (last connection)" (S4); FLAT (S5); "Use different Buds" + the picker (A68-APP-04/05, S7/S8); the connect-failure text without
  naming another app (S10); the tile toast for every failure (S11); the Bluetooth-off cause final, no "provisional" (F-3); the version line (S13). A68-APP-01
  (decoder guard) and A68-APP-10 (socket exceptions): no fault occurred, not exercised. Not shown on hardware: the S3 transition, S6 "—", S9, S12.
- **`ANC-006`:** OpenControl sends a `Set` for the mode already reported; the Buds ACK it and send a `Notify` (A2034 → A2036 → A2037). No NAK. Kept; decide
  after `CAP-069` (maintainer).
- **`FIND-005`:** not observable in this design — the app releases the Message Stream 1.5 s after its Ring, so no client held the channel when the bud was
  touched; the later Stop got only an ACK. Still open → `CAP-069` VII.
- **The secondary user:** nothing behaves differently from the Owner runs except what is missing — no Play services, no Google-app GSND channels, no system log.
  The logs suggest the app was installed fresh rather than updated over 1.0.0 (permissions asked again, 0 associations removed) — 🟡.
- **What the app should change next (proposals only):** the ring-notice wording ("… or touch the bud"); possibly skip a `Set` for the reported mode after
  `CAP-069`. Procedure: install times at P1, a screen recording for S6, film the S12 rotation.

## Files read

- **In full:** `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `TODO.md`, `AI_SESSION_LOG_PROCEDURE.md`, the prompt, the `CAP-068` skeleton
  (HEAD and working tree), `DECISIONS.md` ADR-001…019, 032, 034, 037, 042…051; `APP_TESTPLAN.md` section S, the C5/H5/F5/I4/J4/K1/L3 rows and the Summary; all
  five exports and the logcat line by line; `CHANGELOG.md` `[1.0.1]`; `dist/1.0.1/release-notes.md`.
- **In part:** `PROTOCOL.md` (§0–§2.3, §4.1–§4.4 and §4.5's intro in full; §5's intro and §5.2's opening; §6 by search); `DECISIONS.md` ADR-020…031, 033, 035,
  036, 038…041 (headings; their points as restated in `PROTOCOL.md` §4.3); `RELEASING.md` (the rules around the 1.0.0 exception, the release log row);
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Groups BB–BE, §9 rows); `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the named Test-ID rows); `CAP-066`/`CAP-067` EVENT-NOTES and
  FINDINGS (layout and cited values); `README.md` (current state); app sources by search: `BudsRepositoryImpl.kt` (`setAncModeAfterGet`, `ancSetOnClaim`, the
  Ring branch), `FindMyBudsScreen.kt` (ring texts), `MainActivity.kt` (export), the UI text tests, `android/app/build.gradle.kts` (version, git hash).
- **Not read:** the `0069` PROMPT/RESULT beyond their summaries, `CAP-025-FINDINGS.md` (its frames cited via `PROTOCOL.md` §4.4), `DESKRESEARCH_FINDINGS.md`
  (L68-5 got no samples here). The prompt asked for these in full; the analysis did not depend on them — recorded here rather than claimed.

## Phase log

- **Phase 0:** HEAD `e1fc886` = the tip of `origin/maintenance/0069` (`git log HEAD..origin/maintenance/0069` and `HEAD..origin/main` empty); PR #1 OPEN, head
  `e1fc886`. capinfos/ffprobe/sha256 of all 13 files (values in the EVENT-NOTES). `dist/1.0.1`: APK SHA-256 and certificate re-computed, both match (read only).
  Privacy asked in chat ("Privacy": *"Commit as is"*) — the Wi-Fi name, two device names, the carrier, the maintainer's head, a neighbour's TV in the HCI log; no
  street-address overlay. Migration ("Migration": *"Approve as shown (Recommended)"*): `captures/CAP-068-2026-10-04_07-22-57_07-54-10-Group_BD/` by a plain `mv`
  after a checksum comparison (identical before and after), `chmod 644` on the 8 files with `-rwxr-----`, all 12 capture files `lfs`, the one reference in
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md` updated.
- **Phase A:** both films at 1 fps (contact sheets of 2-s steps), every transition at 1 s, the decisive ones at 4 fps; audio level per second and per 0.25 s;
  clock offsets at both ends of both films (phone = overlay − 0…0.5 s).
- **Phase B:** HCI: the file overlap (byte prefix), link events, the RFCOMM inventory, the Message Stream per claim, MAESTRO with `scripts/pwrpc_decode.py
  --handle 0x000b` plus a CRC-32 check (341/341), HFP/AVRCP/LE; negatives with exit status and positive control. Exports: nested-prefix check, read line by
  line; logcat read.
- **Phase C:** `CAP-068-EVENT-NOTES.md` rewritten (skeleton as Appendix A), `CAP-068-FINDINGS.md` written.
- **Phase D:** checkpoint (answers below); documentation applied: `PROJECT.md`, `RELEASING.md`, `CHANGELOG.md`, `PROTOCOL.md` (§4.1, §4.4 Updates),
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (run note, index row), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (11 evidence pointers), `APP_TESTPLAN.md` (run summary),
  `id_registry.csv`, `TODO.md`, `README.md` (current state), `ai-sessions/INDEX.md`. No file under `android/`, `dist/` or `scripts/` changed.

## Checkpoint answers (chat 2026-10-04, option labels verbatim)

| Question | Answer |
|---|---|
| Privacy | "Commit as is" |
| Migration | "Approve as shown (Recommended)" |
| Verdict | "Release e1fc886 as 1.0.1 (Recommended)" (the CHANGELOG Known-limits text in its preview) |
| DoD text | "Approve as shown (Recommended)" (`PROJECT.md` row 2 and the `RELEASING.md` closing note, as previewed) |
| ANC-006 | "Keep the Set; decide after CAP-069 (Recommended)" (`PROTOCOL.md` §4.1 Update as previewed) |
| Ring | "Dated note only, test in CAP-069 (Recommended)" (`PROTOCOL.md` §4.4 Update as previewed) |
| ADRs (`TODO.md` §1) | "Not now, keep in TODO (Recommended)" |

## Key tables (in `CAP-068-FINDINGS.md`)

Definition of done: §2 (the 12 `Set`/ACK rows) and §1 (0 Play-services claims, command, exit 0, positive control). Session ends: §5 (15 rows). Find frames by
direction: §6. Section S: §7. Step mapping: `CAP-068-EVENT-NOTES.md` "Step mapping".

## External sources

- grapheneos.org/usage, "Sandboxed Google Play" (fetched 2026-10-04 with `curl`, raw text checked): *"GrapheneOS has a compatibility layer providing the option
  to install and use the official releases of Google Play in the standard app sandbox."* and *"Since the Google Play apps are simply regular apps on GrapheneOS,
  you install them within a specific user or work profile and they're only available within that profile."* — for the P0 output (the page names no package; that
  `app.grapheneos.gmscompat*` is this layer is 🟡).

## The maintainer's next steps (`RELEASING.md` Release checklist D1–E4 — instructions, not executed)

1. Commit this session's work (asked in chat) and push `maintenance/0069`; check `git diff e1fc886..HEAD -- android` is empty.
2. Merge PR #1 into `main` with a **merge commit**; tag **`e1fc886`** as `v1.0.1` (the build commit read from the Info tab); create the GitHub release from the
   kept `dist/1.0.1/` files (APK, `.sha256`, `THIRD_PARTY_NOTICES.txt`, the release notes with the known issue added).
3. After publishing: the `CHANGELOG.md` date, `README.md` "Latest release" / "Known issues", the `RELEASING.md` §13 Release log row, the next
   `scripts/release_notes.template` line.

## README media (an extra request in this session's chat, 2026-10-04)

The maintainer asked to replace the README media with new screenshots and a new screen recording of the 1.0.1 build. All 8 screenshots and the whole
recording were viewed first (every 2 s): app screens only, no notification, name or address. `scripts/readme_media.sh` re-encoded the 90.8-s recording
(23.5 MB, 1080×2424) to `images/opencontrol-for-buds-demo.mp4` (902,160 bytes, 570×1280, metadata stripped — under GitHub's 10 MB limit) and
`images/opencontrol-for-buds-demo-preview.gif` (208,460 bytes, 320 px, the first 12 s); the poster it also writes was removed (the README does not use it). The
PNG screenshots were converted to `images/opencontrol-for-buds-20261004-HHMMSS.jpg` (quality 88, no metadata). `README.md`'s Screenshots section points to the
new files (new alt texts; "made on 2026-10-04 with the 1.0.1 release build"); the 8 old `…IMG_20261001_*.jpg` are removed with `git rm`; the `TODO.md` item
"README media … re-take on a release build" is done and removed. The original untracked PNGs and the 23.5-MB recording were deleted after conversion.

## Release files (the maintainer's request in chat 2026-10-04: "Werk nu ook de CHANGELOG.md, README.md en overige files bij die bij deze release horen. Dan alles committen en pushen")

Done in advance of the tag (RELEASING.md E1/E2, which normally follow publication): `CHANGELOG.md` `[1.0.1] - 2026-10-04` (build commit, `CAP-068`, a
Documentation entry for the README media); `README.md` "Latest release: 1.0.1 (2026-10-04)", the Status paragraph and "Known issue in 1.0.1"; `PROJECT.md`
"Status after 1.0.x"; `RELEASING.md` §13 Release log row for 1.0.1; `TODO.md` §1. `dist/1.0.1/release-notes.md` (not tracked) got the known-issue line; the APK is
untouched (`sha256sum -c` OK). Left for after publishing: `scripts/release_notes.template` (E4, a `TODO.md` item). The tag `v1.0.1` and the GitHub release
(D1–D3) are the maintainer's acts — until then the README's release link points to a tag that does not exist yet.

## Lint

`python3 scripts/ensure_footers.py` → footers up to date. `PYTHONDONTWRITEBYTECODE=1 python3 scripts/lint_docs.py` → exit 0 after one path in this session's
prompt was updated to the renamed folder (marked there; CI runs the same lint). The lint also found a cell-count error in the new Capture Index row, fixed.

## Deferred documentation

- `APP_TESTPLAN.md` H5 with the *Read EQ again* button; S6, S12, S9 not shown — `TODO.md` §2.
- P1 install-time check for release runs — `TODO.md` §2.
- Ring status after a stop on the bud (`CAP-069` VII) and the 1.0.1 known limit — `TODO.md` §4.
- The `ANC-006` decision after `CAP-069` IV — `TODO.md` §4.
- The CDM picker's two empty searches — `TODO.md` §4.
- The two ADR questions of `TODO.md` §1 — kept there with the `CAP-068` pointer.

## Commits

Not committed — the prompt asks for the maintainer's word first (task 20).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0070_CAPTURE_RESULT_2026_10_04.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0070_CAPTURE_RESULT_2026_10_04
