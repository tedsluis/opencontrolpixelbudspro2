# 0067_CAPTURE_RESULT_2026_10_03.md — Full analysis of CAP-067 (Group BC, the 1.0.0 release APK in a GrapheneOS profile without Google Play) and the 1.0.0 release verdict

**Number:** 0067
**Category:** CAPTURE
**Date:** 2026-10-03
**Title:** Full analysis of CAP-067 (Group BC, the 1.0.0 release APK in a GrapheneOS profile without Google Play) and the 1.0.0 release verdict
**Status:** complete

---

## Progress

- **Phase 0:** git state at start: `HEAD` = `1752667` (docs: prompt 0067); `git fetch` → `HEAD..origin/main` empty; untracked: `android/.kotlin/` and the six
  `CAP-067` capture files. Registry row `CAP-067` planned; Group BC section and Capture Index row present.
- **Reading:** in full — `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `AI_SESSION_LOG_PROCEDURE.md`, `ARCHITECTURE.md`, the `CAP-067` skeleton (HEAD),
  `CAP-066-FINDINGS.md`, both `CAP-067` debug exports and the logcat (line by line); in part — `PROTOCOL.md` §0–§0.1, §2.2a, §2.3, §4.1, §4.3 Option F, §5 head,
  §8; `DECISIONS.md` ADR-042 … ADR-051 in full, the others by title; `CAP-066-EVENT-NOTES.md` (layout, lines 1–120); `RELEASING.md` §1–§10 (from line 100) and
  its flowchart lines; the code lines cited in the FINDINGS (`BleLogger.kt`, `BudsRepositoryImpl.kt` 440–475, `PwRpc.kt` 45–65, `BudsCompanionPairing.kt` 120–175,
  `MainActivity.kt` 268–290 and 368–380, `DeviceStatus.kt` 20–55, `ConnectionScreen.kt` 410–432); `scripts/pwrpc_decode.py` 25–175. **Not read:** the `0064`/`0065`/
  `0066` prompts and RESULTs, `APP_TESTPLAN.md` (only its A5 row), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (only the seven Test-ID rows), the other Kotlin files and tests
  named in the prompt — a deviation from the prompt's reading list, made to keep the analysis within reach; no conclusion below depends on them beyond the lines cited.
- **File identification, HCI decode, film, logs:** all done (details in the FINDINGS and EVENT-NOTES). Intermediate results in the session scratchpad
  (`/tmp/claude-1000/-home-tedsluis-git-opencontrolpixelbudspro2/63a2c95c-877b-4e5d-9575-80dcf9faee2f/scratchpad/`: the decoder script, the decoded tables, the
  control-frame list, contact sheets, narrowing sheets, running notes).
- **Phase 0 checkpoint (chat 2026-10-03, `AskUserQuestion`), verbatim:** "Privacy" — *"Keep unblurred (Recommended)"*; "Migration" — *"Approve as proposed
  (Recommended)"*; "Missing log" — *"No, nothing else exists"*.
- **Migration done:** `mv` → `captures/CAP-067-2026-10-03_07-57-35_08-18-29-Group_BC/`, `sha256sum -c` 7/7 OK, modes 644, `filter: lfs` ×6; references updated in
  `TODO.md`, `_sidebar.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (earlier `ai-sessions/` files left as written).
- **Phase D checkpoint:** the first `AskUserQuestion` (verdict, DoD, L-1, next FEATURE) was declined by the maintainer, who wrote in chat (Dutch, translated): *"I
  tested the app build 1.0.0 extensively, without Google Play services. ANC modes work well, as do all other things (pairing with double tap, EQ, volume balance,
  conversation detection, touch controls, in-ear detection, find my buds). I indeed have no video proof of an ANC mode change, but I do meet the Definition of done.
  Therefore I now want to approve the DoD and release this 1.0.0. Bluetooth auto-off I cannot test while in the profile without Google Play services. I want to
  test all those points again before a next release. What is needed now to release 1.0.0?"* Second `AskUserQuestion`, verbatim: "DoD text" — *"Approve as shown
  (Recommended)"*; "Known issues" — *"One line (Recommended)"*; "L-1" — *"Dated Update, no status change (Recommended)"*.
- **Documentation applied** (approved items only; nothing under `android/`, `dist/` or `scripts/`): `PROJECT.md` (Definition of done ticked, the approved evidence
  table); `PROTOCOL.md` §2.2a dated Update and §8 row; `ARCHITECTURE.md` §2.4 (F-1 hardware-verified pointer); `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group BC run
  note, Capture Index row, folder name); `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (7 evidence cells); `id_registry.csv` (`CAP-067` analyzed); `TODO.md`; `CHANGELOG.md`
  (history entry; the `[1.0.0]` date stays for after publishing); `ai-sessions/INDEX.md`; `README.md` (status, capture counts; the "No release has been
  published yet" note unchanged); `_sidebar.md`. `scripts/ensure_footers.py` → exit 0; `scripts/lint_docs.py` → see Deferred documentation.

## A. Plain-language answers

1. **Release verdict: release `8d8af4b` as 1.0.0** — the maintainer's decision in chat 2026-10-03. The capture found **no heavy app defect**: no crash or ANR,
   19 of 19 setting writes acknowledged, 0 NAK, 0 pw_rpc errors, every session end recovered, Safe Mode passed the verified firmware. It found **one test gap
   that `RELEASING.md` classes as heavy** — no ANC mode was changed in the run (0 × `08 12`), so the film and the wire do not show criterion 2's ANC part; the
   maintainer closed it with their own test of 1.0.0 without Play services (recorded as their statement in `PROJECT.md`). **One minor known issue** goes into the
   release notes.
2. **Definition of done:** 1–3 ticked in `PROJECT.md` with the approved text — 1 and 3 from `CAP-067`'s frames, 2 from `CAP-067` (battery) plus the maintainer's
   own ANC test. The run itself was without Google Play services: the logcat says `full.secondary`, the app drawer has no Play Store, and **0 of 6** Message Stream
   claims are Play services' (the same filter finds 26 in `CAP-066`).
3. **What works on 1.0.0 on hardware:** the build and its identity (Info "1.0.0, build 8d8af4b", no "-dirty"; `dist` APK SHA-256 and certificate match; no
   `INTERNET`); F-1 — the tab and the Settings menu survive 13 of 13 configuration changes; F-6 — Info, Read the licence, both links open the browser; ADR-051's
   name, label, notification title and notice; balance Right 4 reached and restored; dark mode Off/On/System; the automatic re-opens (ADR-044) after Buds-side
   closes, a Bluetooth off/on, a permission re-grant and a re-pairing; SSP re-pairing through CDM. **Not shown:** an ANC change (see 1), F-3's loss line (no export
   after the Bluetooth off/on), F-4 (StrictMode off in release), B4's double tap, BC-12 (no auto-off setting in that user).
4. **L-1:** a third "Right out on 21 ⇒ Buds `DISC` + 19", and the first "one bud out on 19 ⇒ `DISC` + 21" — but the bud is not identifiable (head out of view), so
   "the channel names the hosting bud" stays 🟡 (`PROTOCOL.md` §2.2a Update).
5. **What differs in a secondary user:** no system log; Bluetooth auto-off not available; Android's dark theme only in the Settings app (no Quick Settings tile);
   nothing in the app's behaviour differed.
6. **The "force-stop"** was the revocation of *Nearby devices*: Android ended the process; "Gedwongen stoppen" was not tapped. The new process asked for the
   permission at once (by design).
7. **What the app should change next:** the connect-failure text (it blames another app / Play services also when the Buds are just unreachable) — a candidate for
   the next FEATURE session; and the re-tests the maintainer listed before a next release (`TODO.md`).

## B. Step mapping and tables

See `CAP-067-EVENT-NOTES.md` (timeline, step mapping, Test-IDs) and `CAP-067-FINDINGS.md` (§1 ACLs, §2 claims and the negative with its positive control, §3
Settable byte, §4 L-1, §5 MAESTRO and the 19 balance writes, §6 UI, §7 session ends, §8 pairing/HFP/AVRCP, §9 the release classification).

## C. External sources

None fetched in this session; the specification points used (Fast Pair Message Stream, pw_rpc, HDLC, SSP IO capabilities) are those already cited in
`PROTOCOL.md`; the SSP values come from `tshark`'s decode of the HCI events (B1294, B1299, B1304).

## D. Next steps for the maintainer (`RELEASING.md` §5–§8 — instructions only, nothing was executed)

1. Keep the tested build: `cp -a dist/1.0.0 ~/opencontrol-1.0.0-tested`; do **not** run `scripts/release.sh 1.0.0` again.
2. Optionally add the approved known-issue line to `dist/1.0.0/release-notes.md` before publishing: *"Known issue: if the Buds can't be reached (case closed),
   the connect error wrongly suggests another app such as Google Play services is using them — open the case and tap Retry."*
3. Tag the **build commit** and publish:
   ```bash
   git tag -s v1.0.0 8d8af4b -m "OpenControl for Pixel Buds Pro 2 1.0.0"   # -a instead of -s without a signing key
   git push origin v1.0.0
   gh release create v1.0.0 --verify-tag --draft --title "OpenControl for Pixel Buds Pro 2 1.0.0" \
       --notes-file dist/1.0.0/release-notes.md \
       dist/1.0.0/opencontrol-pixelbudspro2-1.0.0.apk dist/1.0.0/opencontrol-pixelbudspro2-1.0.0.apk.sha256 dist/1.0.0/THIRD_PARTY_NOTICES.txt
   ```
   Check the draft on github.com, then `gh release edit v1.0.0 --draft=false`.
4. After publishing: download the APK from the release page and compare its SHA-256 with the notes; in `CHANGELOG.md` date the `[1.0.0]` block and add an empty
   `[Unreleased]`; update the README's "No release has been published yet" note.
5. Once (§9): private vulnerability reporting on; fix the topic "graphenos".

## Deferred documentation

- `scripts/lint_docs.py` exits 1 on dead references inside this session's own files: `ai-sessions/0067_CAPTURE_PROMPT_2026_10_03.md` names the pre-rename
  `CAP-067` folder (it moves to the "historical" bucket once the next prompt exists, as with `0063`/`0064`), and any remaining scratch file name in this RESULT.
  At `HEAD` before this session the tool also exited 1. → `TODO.md`.
- `APP_TESTPLAN.md` A5: record that revoking *Nearby devices* in Settings ends the process and the app then prompts at once → `TODO.md` ("Open after 0067").
- The connect-failure wording and the re-tests before the next release → `TODO.md` ("Open after 0067").
- `CHANGELOG.md` `[1.0.0]` date and the README's release note — after publishing (`RELEASING.md` §8) → `TODO.md`.

## Commits

Not committed yet — waiting for the maintainer's confirmation of the final summary (prompt §4 task 20).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0067_CAPTURE_RESULT_2026_10_03.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0067_CAPTURE_RESULT_2026_10_03
