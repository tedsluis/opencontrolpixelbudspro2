# 0061_CAPTURE_RESULT_2026_10_01.md — Full analysis of CAP-065 (Group BA, the Pixel 9a / GrapheneOS robustness, UI and `ai-sessions/0059`-fixes run)

**Number:** 0061
**Category:** CAPTURE
**Date:** 2026-10-01
**Title:** Full analysis of CAP-065 (Group BA, the Pixel 9a / GrapheneOS robustness, UI and `ai-sessions/0059`-fixes run)
**Status:** complete

---

## Progress

- **Phase 0:** started. Git state at start: `HEAD` = `c9c6592` (docs: prompt 0061); `git fetch` → `HEAD..origin/main` empty; untracked: `android/.kotlin/`
  and the five `CAP-065` capture files. Registry: `CAP-065` row `planned`; ADR-049 last ADR; `0061` row in `ai-sessions/INDEX.md` "prompt only".
- **Reading done (Phase 0 floor):** `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `AI_SESSION_LOG_PROCEDURE.md`, `ARCHITECTURE.md` in full; `PROTOCOL.md`
  §0–§2.3, §4.1, §4.3 head/B/F, §4.5; `DECISIONS.md` ADR-032/034/036/037/042–049 in full, the rest by title; the `CAP-065` skeleton (HEAD), `CAP-064`
  FINDINGS in full and EVENT-NOTES (timeline, step mapping), `0060` RESULT, the `CAP-066` skeleton.
- **File identification done:** HCI 12,771 packets, 11:07:36.017203–11:30:07.973343, `H4 with linux header`, no size limit, 0 `cap_len≠len`, 4
  out-of-order; film 1252.98 s, 37,393 frames, 1280×720 rot −90, AAC stereo 44.1 kHz 1252.61 s, creation_time 09:28:23Z; export 739 lines (738 newlines); logcat 606; system
  log 64,665. sha256 in the scratchpad notes. Modes: HCI already 644, the other four `-rwxr-----`. All five `filter: lfs`.
- **Film pass done at 2 s spacing** (79 contact sheets of 8 frames, 1 fps extraction; scene-change list, 71 changes, all inside windows viewed). Notes:
  scratchpad film_notes.md. Narrowing of single transitions still to do (after the logs, where a claim depends on it).
- **Audio done:** per-second RMS median −71.7 dBFS, p90 −59.1, max −37.9 (t = 705 s ≈ 11:19:15, lid opening); 134 s > 12 dB above the median, broadband
  transients; no speech pattern; a faint tonal (stable-pitch) pattern at t ≈ 1030–1120 s (11:24:40–11:26:10) unidentified.
- **Clock offsets done:** phone = overlay + 0.5 s (±0.2 s), status-bar flip ≈ 0.5 s into overlay :59 at 11:07→11:08 and 11:27→11:28 (5 fps), no drift.
  Logcat/system log = phone − 2 h 00 min 00.000 s (export 11:27:04.669 = logcat 09:27:04.669, same line). Film overlay 11:07:30 – 11:28:23.
- **Logs read:** debug export 739/739 lines; app logcat in full (606 lines); system log: app lifecycle, install/kill, Bluetooth process, BluetoothAutoOff.
  Build: PID 21818 (the `CAP-064` process) ran until `am_kill … Reason=remove task` 09:00:00.760 UTC, restart PID 8072 09:00:16; no install line in the log
  (events buffer from 07:44 UTC).
- **HCI decoded:** handles, RFCOMM inventory (304 control frames), MAESTRO (266 packets, all CRC-OK, no write), Message Stream (51 claims, 25 app / 26 Play
  services), HFP AT, AVRCP (42 frames; the first field-based command gave 0 lines silently — positive control `btavctp or btavrcp` → 42). Scratchpad:
  dec065.py, dec.tsv, ms.tsv, claims.txt, rfcomm_ctl.tsv, film_notes.md.
- **Phase 0 checkpoint (chat, 2026-10-01), verbatim:** Privacy — *"Keep unblurred (Recommended)"*; Migration — *"Approve as proposed (Recommended)"*.
- **Migration — interrupted and redone.** My first move command (copy → checksum → `rm` loop → `rmdir`, chained) was rejected by the maintainer as
  dangerous; it did not run. Asked again (question "Rename"), answer verbatim: *"Delete copies, then rename (Recommended)"*: the verified copy folder was
  removed on its own, then the placeholder folder was renamed with `mv` to `captures/CAP-065-2026-10-01_11-07-30_11-28-23-Group_BA/`, `sha256sum -c` 6/6 OK,
  modes 644, `filter: lfs` ×5. References updated in `_sidebar.md` and `CAPTURE_BLUETOOTH_HCI_SNOOP.md`; `ai-sessions/0059`/`0061` keep the old name.
- **Phase A narrowing done:** tile colour per 0.5 s (grey = Off, red = a mode — `AncTile.kt:44`); BA-1 every frame t = 88.4–90.2 s (the `EqScreen.kt:217` note
  on film for ≈ 5 frames); clock flips at 5 fps. Collision mechanism at 11:27:10.450 in the system log; BT-process lines only 09:07 and 09:23–09:28 UTC.
- **`CAP-065-EVENT-NOTES.md` written** (timeline, step mapping, traceability, checklist, skeleton as Appendix A; export line numbers re-checked). Two
  corrections found while checking: a bug report did run (`dumpstate` lock 11:28:21); the LE device is the Fitbit Charge 6 (CDM association 37).
- **`CAP-065-FINDINGS.md` written** and its counts re-verified (corrected: 51 claims = 25 app / 26 Play services; 7 `Set`s; 6 LINK_BACK + 1 AFTER_LOSS).
- **External check:** developer.android.com "Create custom Quick Settings tiles for your app" (fetched 2026-10-01): *"`Tile` objects set to `STATE_ACTIVE` are
  the darkest, with `STATE_INACTIVE` and `STATE_UNAVAILABLE` increasingly lighter. The exact hue is specific to the manufacturer and version."* — the page says
  nothing about when a subtitle is shown.
- **Checkpoint done (chat 2026-10-01), answers verbatim:** ADR-049 — *"Dated Update, no status change (Recommended)"*; L-1 — *"FACT for the correlation,
  🟡 for 'host' (Recommended)"*; Other notes — *"§4.3 Option B/F: lid closed (Recommended), §6.0b: collision in the BT log (Recommended), §1/§5: ACL starts
  only from the Buds"*; FEATURE — *"Cut-off claim handling (Recommended), Dark-mode contrast check (Recommended), CloseGuard in debug builds"*; CAP-066 —
  *"Apply as proposed (Recommended)"*; Leftovers — *"Append to CAP-066, destructive last (Recommended)"*. (Each question showed the draft text in its preview.)
- **Documentation applied** (approved items only): `DECISIONS.md` ADR-049 Update; `PROTOCOL.md` §2.2a, §4.1, §4.3 Option F, §5 dated Updates + §8 row;
  `ARCHITECTURE.md` §6.0b note; `CAP-066-EVENT-NOTES.md`; `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group BA run note, Group BB, Capture Index rows); `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`
  (18 evidence cells); `id_registry.csv` (`CAP-065` analyzed, `CAP-066` text); `TODO.md`; `CHANGELOG.md`; `ai-sessions/INDEX.md`; `README.md`.
  `scripts/ensure_footers.py` ✓; `scripts/lint_docs.py` exits 1 on one entry only (see Deferred documentation).
- **All phases done.** Commit/push: only after the maintainer's confirmation (§Commits).
- **Files touched:** see "Documentation applied"; plus `CAP-065-EVENT-NOTES.md` (rewritten), `CAP-065-FINDINGS.md` (new), `_sidebar.md`, the folder rename. Nothing under `android/`.
- **Intermediate results:** the session scratchpad (`/tmp/claude-1000/-home-tedsluis-git-opencontrolpixelbudspro2/<session>/scratchpad/`) — re-create from
  the commands recorded in `CAP-065-FINDINGS.md` if it is gone.


## A. Plain-language answers

1. **What works on this build (the same installation as `CAP-064`).** The app re-opened its session by itself every time the Buds' link came up while it
   was on screen (6 of 6) and after one Buds-side close (1 of 1); it stayed off after a Disconnect tap until Connect; all 266 Maestro packets are valid and OK;
   every pull to refresh sent exactly its tab's action (Controls: seven reads; Sound: the EQ, then seven; Connection/Find: the battery refresh; ANC: one ANC
   read; while disconnected: one Connect); opening Debug or an (i) sends nothing; dark mode on/off recreated the screen without a crash. While the EQ is being
   read the Sound tab now says "Reading the Buds' current EQ… The sliders are off until it arrives" (on film).
2. **What did not work or was not tested.** The tile's subtitle could not be read — your ANC tile is the small (icon-only) kind, which shows no subtitle;
   and the app was swiped away, not force-stopped, so the "fresh start" case of the tile fix wasn't tested. **New:** twice the app's Message Stream connection
   was closed under it (Play services took the channel) just as the Buds answered — once the Buds switched to Noise cancellation but the app kept showing Off
   (you tapped again), once the app said "The Buds didn't respond in time" although they had. Rotation, Bluetooth off/on, out of range, the second export and
   the destructive steps were not done.
3. **The Settable byte.** With your ears on film: 28 readings `00`, every one with no bud in an ear; every `e8` with a bud in an ear — including the two
   situations ADR-049 asked about (one docked/one loose, both loose: `00`). Recorded as an ADR-049 Update; it stays a strong hypothesis because `CAP-064` once
   read `e8` with no bud worn.
4. **L-1.** With only the Left bud out the Buds announce channel 19, with only the Right out 21 — 7 of 7, now 🟢. Once the channel switched 21 → 19 while you
   wore both and touched nothing — 🟡 it names the bud that currently holds the phone's link; `CAP-066` tests that.
5. **What the app should change next** (your choices): the next FEATURE session adds, to `Get`-before-`Set` and the settings menu, the handling of an answer
   cut off by a closed channel, a dark-mode contrast check and a debug-only leak check.

## B. Files read

- **In full:** `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `AI_SESSION_LOG_PROCEDURE.md`, `ARCHITECTURE.md`, the `CAP-065` skeleton (HEAD), the `CAP-066`
  skeleton, `CAP-064-FINDINGS.md`, `ai-sessions/0060` RESULT, the `0061` prompt, the `CAP-065` debug export (739 lines), the app logcat (606 lines),
  `AncTileService.kt`.
- **In part:** `PROTOCOL.md` (§0, §1, §2.2a, §2.3, §4.1, §4.3 head/B/F, §4.5 all subsections, §5 head, §8 tail — §2.1/§2.2, §4.2, §4.4, §5.1/§5.2, §6, §7 not
  read); `DECISIONS.md` (ADR-032, 034, 036, 037, 042–049 in full; the other ADRs by title only); `CAP-064-EVENT-NOTES.md` (to line 240); `TODO.md` (the
  0059/0060 blocks); `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group AZ/BA/BB, Capture Index rows); `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the rows of the named Test-IDs);
  `CHANGELOG.md`, `README.md` (the entries changed); `BudsRepositoryImpl.kt` (grep around lines 259–321, 489, 543, 628, 693, 863, 882); `AncTile.kt` (lines
  36–56); `EqScreen.kt`/`ConnectionBanner.kt` (the two strings); `scripts/pwrpc_decode.py` (lines 1–175); the system log (searched: app lifecycle, install,
  kill, Bluetooth process, BluetoothAutoOff, CDM, dumpstate, the collision windows — not every one of its 64,665 lines).
- **Not read, contrary to the prompt's list:** `APP_TESTPLAN.md` (the skeleton carried its steps), `TODO.md` in full, `ai-sessions/0057`/`0059` RESULTs, `CAP-063`
  EVENT-NOTES/FINDINGS, `SessionReopener.kt`/`OsConnectionObserver.kt`/`MainActivity.kt`/`AppUiSession.kt`/`ConnectionStateMachine.kt`/the pairing code and their
  tests in full (`CAP-064`'s reading of them was relied on; no pairing step ran) — say if you want these done in a follow-up.
- **Film:** every second at 1 fps, viewed at 2 s spacing (79 sheets) + a scene-change pass; transitions at 2/5/30 fps. **Audio:** level and spectrogram over the
  full duration. **HCI:** every packet on handle `0x000b` decoded by protocol.

## C. Tables of Phase B

In `CAP-065-FINDINGS.md`: §1 (lid openings / link-ups), §2 (verdicts, "Refuted if" applied literally — no literal hit), §3 (every `Notify` vs wear), §4 (L-1 per
session), §5 (settings, byte comparison), §6 (session ends), §8 (per protocol). Step mapping and traceability: `CAP-065-EVENT-NOTES.md`.

## D. External sources (fetched 2026-10-01)

- developer.android.com, "Create custom Quick Settings tiles for your app": *"The system may tint the tile icon and background to reflect the state of your
  `Tile` object. `Tile` objects set to `STATE_ACTIVE` are the darkest, with `STATE_INACTIVE` and `STATE_UNAVAILABLE` increasingly lighter. The exact hue is
  specific to the manufacturer and version."* (no statement about when the subtitle is shown — the BA-2 verdict rests on the film).
- The RFCOMM collision behaviour cited in §3/`ARCHITECTURE.md` §6.0b rests on the device's own system log; the Bluetooth-spec quotes for it are those of
  `ARCHITECTURE.md` §6.0b (2026-09-24), not re-fetched.

## E. Draft outline for the next FEATURE prompt (building on `0060` §H)

1. Reading block per `AI_SESSION_LOG_PROCEDURE.md` §9; `CAP-064-FINDINGS.md` §2–§3, §9; `CAP-065-FINDINGS.md` §2–§3, §9; ADR-032/044/049; `ARCHITECTURE.md`
   §2.4, §3.1, §6.0b, §12.
2. **ANC `Get` before every `Set`** (as `0060` §H item 2) — fixtures `CAP-064` 3433/3440/3443, 4091/4103.
3. **An answer cut off by a closed claim** (`CAP-065-FINDINGS.md` §9 item 1): a distinct result and wording; the mode marked unconfirmed until the next
   `Notify`; fixtures `CAP-065` 2640/2649/2651 and 10321/10344/10356; no retry beyond ADR-032's bounded attempts.
4. **Settings menu behind a gear icon** (as `0060` §H item 3): Settings (dark mode On/Off/System) with a dark-scheme contrast check of the Connection card
   (Robolectric screenshot), Debug, Info (Buds/Case firmware — which announcement entry is the Case is still open; build number via `BuildConfig`).
5. Debug builds only: `StrictMode` `detectLeakedClosableObjects` with `penaltyLog`.
6. Optional: one loss-cause log line instead of three.
7. Hardware re-test → `CAP-066` (Group BB, already extended); guardrails: no new permission, no network, no polling, no background work.

## Deferred documentation

Each item is also in `TODO.md` ("Open after `ai-sessions/0061`" / "0060"):

- Run `CAP-066` (Group BB, extended).
- The next FEATURE session with the `CAP-065` additions.
- 🔴 The hosting-bud switch (L-1 🟡); 🔴 `CAP-064`'s 28-s `e8` vs `CAP-065`'s `00`; 🔴 what closed the claims at 11:10:06/11:21:35 and why the Buds closed the
  session at 11:17:46; 🔴 GrapheneOS `delayMillis: 0`.
- `scripts/lint_docs.py` exits 1 on one entry only: `ai-sessions/0061_CAPTURE_PROMPT_2026_10_01.md` names the pre-rename `CAP-065` folder (a prompt is not
  rewritten; it moves to the "historical" bucket once a newer session pair exists — `0060`'s prompt did so now).
- Not read in this session (see §B): `APP_TESTPLAN.md`, several app sources and their tests in full.

## Commits

Committed and pushed after the maintainer's confirmation in chat (2026-10-01, "Commit and push (Recommended)"):

- `3b66a05` — capture: the five `CAP-065` capture files via Git LFS (renamed folder).
- the docs commit that contains this file (`git log -1 --format=%h -- ai-sessions/0061_CAPTURE_RESULT_2026_10_01.md`) — findings, event notes, ADR-049 and
  `PROTOCOL.md`/`ARCHITECTURE.md` Updates, `CAP-066`, plan, test plan, registry, TODO, CHANGELOG, INDEX, README.

Not staged: `android/.kotlin/`.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0061_CAPTURE_RESULT_2026_10_01.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0061_CAPTURE_RESULT_2026_10_01
