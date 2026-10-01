# 0060_CAPTURE_RESULT_2026_10_01.md — Full analysis of CAP-064 (Group AZ) and the "no automatic connect when the case is opened" observation

**Number:** 0060
**Category:** CAPTURE
**Date:** 2026-10-01
**Title:** Full analysis of CAP-064 (Group AZ) and the "no automatic connect when the case is opened" observation
**Status:** complete

---

## Progress

All phases done (0, A, B, C, D up to task 18). Maintainer decisions taken in this chat (§D). Commit/push: §Commits. Intermediate results lived in the
session scratchpad (1 fps frames, contact sheets, the scratch decoders for MAESTRO and the Message Stream, decoded tables, audio level and spectrogram) —
re-creatable from the commands in `CAP-064-FINDINGS.md`; nothing in the repository depends on them.

## A. Plain-language answers

1. **"The app does not connect by itself when the case is opened" — qualified, with the cause.** When you open the lid with both buds in the case, *nothing*
   happens on Bluetooth: no connection from the Buds, none from Android (18 s of silence on the HCI log after the lid opened at 10:05:12; again 10:15:56–16:11).
   The Buds only call the phone the moment a bud leaves the case. OpenControl re-opens its session when Android reports the Buds connected (ADR-044) — and it
   did so **every time** that happened while the app was on screen (4 of 4: 10:05:31, 10:06:13, 10:13:32, 10:17:32), plus 8 times after the Buds closed the
   channel on a wear change. So with both buds in an open case there is no event for the app to react to, on any tab; you have to tap Connect (which then
   works within a second — the app's connect makes the phone call the docked Buds, 10:16:11 → 10:16:12.4) or take a bud out. Not a regression; `CAP-063` saw
   the same. The case lid is visible on no channel the app may use. You chose to change nothing now.
2. **What works on this build:** automatic re-opens (12), the I-2 background-loss wording, balance with "Centre" (26 writes, all OK, persisted), the I-5 note,
   the press-and-hold list (written, read back, and **the Buds follow it — six long presses never reached Adaptive**), the in-ear detection switch (written,
   read back; SASS bit 4 follows; with it off no pause when a bud comes out), Disconnect/Connect read-back, press-and-hold chips hiding/showing the list,
   battery values with their times. 384 MAESTRO packets, all valid, all OK. No crash, no ANR (the "tombstoned" line is the bug report), no Safe Mode.
3. **What did not go as planned:** the "disabled ANC tap re-checks first" path (I-1) never ran — the Buds said "allowed" (`e8`) even with both buds on the
   table, so the taps went straight through (and the Buds accepted ANC changes with no bud worn); the tile once sent a `Set` on an 18-s-old "allowed" and got
   a NAK (reason 0x02). The "last connection" words of I-4 now live in the (i) dialog, not on the card.
4. **The Settable byte:** one worn bud ⇒ `e8` (both orientations, ears on film). With in-ear detection **off** it stays `e8` with no bud worn (answers the
   `CAP-056` 🔴). With it **on** it read `e8` for ≈ 28 s with both buds visibly on the table. Every `00` came with no bud worn. Recorded as the ADR-049 Update:
   "`00` ⇒ no bud worn" stays 🟡; "no bud worn ⇒ `00`" is refuted as a rule.
5. **Next:** the next FEATURE session builds `Get`-before-`Set` for ANC with new wording, and a settings menu (gear icon: Settings with dark mode, Debug, Info
   with firmware and build number). `CAP-065` (already run) needs its analysis; `CAP-066` (Group BB) holds the leftovers.

## B. Files read

- **In full:** `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `AI_SESSION_LOG_PROCEDURE.md`, `ARCHITECTURE.md`, `DECISIONS.md` (ADR-001 … ADR-049 with every
  Update), the `CAP-064` skeleton, `CAP-063-FINDINGS.md`, the `CAP-064` debug export (1,131 lines), `SessionReopener.kt`, `OsConnectionObserver.kt`,
  `MainActivity.kt`, `SessionReopenerTest.kt`.
- **In part:** `PROTOCOL.md` (§0, §1, §2.3, §4.1, §4.3 head/Option 0/Option F, §4.4, §4.5 all subsections, §8 tail — §2.1–§2.2a, §4.2, §5, §6, §7 not read);
  `TODO.md` (head, Phase 4, Known technical debt); `CAP-063-EVENT-NOTES.md` (lines 1–290); `BudsRepositoryImpl.kt` (lines 140–898); `BudsRepositoryImplTest.kt`
  (test names only); `AncTileService.kt` (grep); `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group AZ/BA, Capture Index rows); `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the
  named Test-ID rows); `CHANGELOG.md` (structure, recent entries); app logcat (searched in full, the last 25 lines read; 401 lines); system log (targeted
  windows: app lifecycle, `dumpstate`, Bluetooth consumers, `am_anr`/`am_crash`; not every one of its 61,308 lines). `CAP-056-FINDINGS.md` §4,
  `APP_TESTPLAN.md`, and the `ai-sessions/0054`/`0056`/`0057`/`0059` RESULTs were **not** read beyond what other files quote — the skeleton carried their steps.
- **Film:** every second at 1 fps (95 contact sheets at 2 s spacing viewed; key transitions at 1–5 fps, full resolution). **Audio:** level and spectrogram
  over the full duration. **HCI:** every packet on handle `0x000b` decoded by protocol (RFCOMM control, MAESTRO, Message Stream, HFP AT, AVRCP, HCI events).

## C. Phase 0 — set-up, privacy, migration

- `git log -1` at start `c9b7cd5`; `origin/main` was one CI commit ahead (`d79aba4`, sitemap) — fast-forwarded. `git status`: the five `CAP-064` files
  untracked, `android/.kotlin/` untracked.
- File identities (every value of the prompt's §2 re-checked): HCI 11,390 packets, times as stated; film 1514.80 s, 1.18 GB, 1280×720, 90000/3013, AAC
  stereo — as stated; export 1,130 newlines / 1,131 lines; logcat 401 lines, header as stated, UTC confirmed (− 2 h 00 min 00.000 s); system log 61,308
  lines. Modes `-rwxr-----` → 644 (approved). No events file, no `.last`. All five capture files `filter: lfs`.
- **Privacy** (question "Privacy", answer verbatim: *"Keep unblurred (Recommended)"*): shade legible at 10:23:34–36 and 10:24:56–10:25:02 (WhatsApp group and
  contact names, a LinkedIn name, a bank debit notice, news); no address overlay; no speech.
- **Migration** (question "Migration", answer verbatim: *"Approve as proposed (Recommended)"*): folder renamed to
  `captures/CAP-064-2026-10-01_10-04-14_10-29-28-Group_AZ` (overlay of the first and last frame, full-resolution crop), checksums identical before and after
  (`sha256sum -c`, 6/6 OK), the debug export keeps its own name, references updated in `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `TODO.md`, `_sidebar.md`; the
  `ai-sessions/0054`/`0056`/`0060` prompts and the `0054` RESULT keep the old name (history, not rewritten). `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` and the `CAP-065`
  skeleton had no reference to it.

## D. Checkpoint answers (verbatim, chat 2026-10-01)

| Question | Answer |
|---|---|
| Auto-connect fix | *"Nothing now"* |
| ADR-049 | *"Update as drafted (Recommended)"* (with the draft text in the preview) |
| ADR-046/047 | *"Record both (Recommended)"* |
| ANC fixes | *"Get-before-Set + new wording (Recommended)"* |
| To CAP-065 | *"Disabled-tap re-check (I-1a/c), Settable timing experiment, ANC-002/004 + I-4a (i), " ik heb CAP-065 al uitgevoerd, maar die moet nog geanalyseerd worden. Maak indien nodig CAP-066 aan. Ik spreek niet op de video's. Je kunt de commit hash of build zelf wel achterhalen, toch? Play service nearby is ON, voor onder andere het casten naar een chromecast.  Verplaats restore balance naar CAP-066""* |
| Next FEATURE | *"In de volgende FEATURE session build wil ik: 1) ANC Get-before-Set + wording. 2) Een settings-menu waarin een tabblad 'Settings' (met toggle voor dark mode: On, Off, System) , tabblad 'Debug' (zoals huidige debug scherm met Debug mode switch en Export debug log en Unidentied frames), 'Info' (met Firmware versie Buds en Case, met App build nummer) zit. Het nieuwe settings-menu moet toegangelijk worden via een tandwiel icoon dat op de plek zit van het huidige debug icoon.  "* |

Applied: ADR-046/047/049 dated Updates and `PROTOCOL.md` §4.1/§4.5.3/§4.5.5 dated Updates (+ §8 row), each citing this chat; `CAP-066` (Group BB) skeleton,
Capture Index row, Group BB section and `id_registry.csv` row; the maintainer's statements (no narration; *Nearby devices* allowed) in `CAP-064-EVENT-NOTES.md`.

On the maintainer's question "you can work out the commit hash or build yourself, can't you?": **not exactly** — no log, frame or screen of this run carries the
commit hash or build number (`versionCode` is 1 for every build). What the evidence shows: at least the `0057` UI with the `0056` writes, installed (app data
cleared by the installer) at 08:53:21, 16 min after `b65085a`, the last `android/` commit; nothing in the run contradicts `b65085a` and nothing proves it
(`CAP-064-FINDINGS.md` §0). The Info tab with a build number (next FEATURE) closes this gap for future captures.

## E. Improvements (proposals; nothing under `android/` changed)

The full list with tests and hardware steps is `CAP-064-FINDINGS.md` §9. Drafted ADR for the auto-connect option (b) — **not adopted** (maintainer: "Nothing
now"), kept here for the record, no number assigned, not registered:

```
ADR-XXX — One automatic connect attempt on resume when Android's link is down
- Status: Proposed (not adopted 2026-10-01)
- Context: CAP-064 §1: the lid is not observable; docked Buds in an open case accept a page (7201→7204, 0.96 s), a closed case times out (673→699, 5.1 s).
- Decision: amends ADR-044 item 1 (c): on resume, if visible, enabled, bonded, no session and Android's link reads NOT_CONNECTED, open the session once
  (as a tap). A failure is shown as "not connected", not as an error; never retried; no timer; foreground only; no new permission or service.
- Consequences: SessionReopener gains a RESUME_PAGE trigger; test + hardware step in CAP-064-FINDINGS §9.
```

## F. Results tables

The tables of Phase B are in `CAP-064-FINDINGS.md`: §1a (every lid-open / link-up with tab, visibility, HCI, link, re-open, tap), §2 (re-test verdicts, the
"Refuted if" list applied literally — one literal hit, I-1c), §3 (every `Notify` against the film's wear state), §5 (settings reads/writes, byte comparison),
§6 (session ends), §8 (per protocol). The step mapping is in `CAP-064-EVENT-NOTES.md`.

## G. External sources (fetched 2026-10-01)

- AOSP `packages/modules/Bluetooth/framework/java/android/bluetooth/BluetoothDevice.java` (`refs/heads/main`), `ACTION_ACL_CONNECTED`: *"Broadcast Action:
  Indicates a low level (ACL) connection has been established with a remote device. … ACL connections are managed automatically by the Android Bluetooth
  stack."* (`@RequiresPermission(BLUETOOTH_CONNECT)`).
- AOSP `…/BluetoothSocket.java`, `connect()`: *"Attempt to connect to a remote device. This method will block until a connection is made or the connection
  fails."*; its only extra permission sentence concerns `BLUETOOTH_PRIVILEGED` "only when `mDataPath` is different from `DATA_PATH_NO_OFFLOAD`".
- (The developer.android.com reference pages for both classes were tried first; the fetch returned only the navigation index, so the AOSP sources — the text
  those pages are generated from — were used.)

## H. Draft outline for the next FEATURE prompt (`0061`)

1. Reading block per `AI_SESSION_LOG_PROCEDURE.md` §9; `CAP-064-FINDINGS.md` §2–§3, §9; ADR-044/049; `ARCHITECTURE.md` §2.4, §3.1, §12.
2. **ANC `Get` before every `Set`** (`BudsRepositoryImpl.setAncMode`/`ancSetOnClaim`, the tile path): one claim, `08 11` first, `Set` only if that claim's
   `Notify` is non-zero; otherwise `AncNotAllowed` (+ the tile's toast). New wording on the ANC tab and tile. Unit tests with `CAP-064` 3433/3440/3443 (stale
   `e8` → NAK today) and 4091/4103 (`Get` → `e8`).
3. **Settings menu behind a gear icon** replacing the top bar's bug icon: tabs **Settings** (dark mode On / Off / System, persisted in the existing DataStore —
   no new dependency), **Debug** (the current Debug screen unchanged), **Info** (firmware of the Buds and the Case from the `GetSoftwareInfo` announcement —
   first establish from `PROTOCOL.md` §2.2a and real bytes which entry is the Case; the app's build number/commit via a Gradle `BuildConfig` field, no network).
   `ARCHITECTURE.md` §2.4 update; Robolectric UI tests.
4. Optional: one loss-cause log line instead of three (`CAP-064-FINDINGS.md` §6).
5. Hardware re-test steps → `CAP-066` (Group BB) additions; guardrails: no new permission, no network, no polling.

## Deferred documentation

Each item is also in `TODO.md` ("Open after `ai-sessions/0060`"):

- Analyse `CAP-065` (run, not analysed; its capture files sit untracked in the placeholder folder together with a `zip/` the maintainer created — not touched).
- Run `CAP-066` (Group BB).
- 🔴 Why Android re-paged the docked Buds at 10:17:27 but not at 10:15:55.
- The next FEATURE session (ANC `Get`-before-`Set` + wording; settings menu).
- `scripts/lint_docs.py` exits 1 on one entry only: `ai-sessions/0060_CAPTURE_PROMPT_2026_10_01.md` names the pre-rename folder
  `captures/CAP-064-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AZ/` (the prompt describes the state when it was written and is not rewritten; it moves to the
  "historical, informational" bucket once a newer session pair exists). Every other check passes.

## Commits

Committed and pushed after the maintainer's confirmation in chat (2026-10-01, "Commit and push (Recommended)"):

- `5e02a7f` — capture: the five `CAP-064` capture files via Git LFS.
- the docs commit that contains this file (`git log -1 --format=%h -- ai-sessions/0060_CAPTURE_RESULT_2026_10_01.md`) — findings, event notes, ADR/PROTOCOL Updates, `CAP-066` skeleton, registry, index.

Not staged: `android/.kotlin/` and the maintainer's `CAP-065` files (left as found).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0060_CAPTURE_RESULT_2026_10_01.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0060_CAPTURE_RESULT_2026_10_01
