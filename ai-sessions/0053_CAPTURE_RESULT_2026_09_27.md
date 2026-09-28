# 0053_CAPTURE_RESULT_2026_09_27.md — Full analysis of CAP-063 (Group AY) and a list of app improvements, without changing the app

**Number:** 0053
**Category:** CAPTURE
**Date:** 2026-09-27
**Title:** Fully analyse `CAP-063` (video, HCI snoop log, app debug export, app logcat, system log), record the events and findings, verify the maintainer's observations and produce a prioritised list of app improvements — no change to the app itself
**Status:** complete

## Progress

- Started 2026-09-27. `git log -1`: `ceaf05a chore(captures): add the raw CAP-063 capture files (Group AY) via Git LFS`. `git status --short`: clean.
  `origin/main` == `HEAD` (the capture files, incl. the recording, are already committed **and pushed** via LFS in `ceaf05a` — the prompt's
  "untracked" is out of date).
- Scratchpad: `/tmp/claude-1000/-home-tedsluis-git-opencontrolpixelbudspro2/eef7638e-a9b5-4f70-b448-8fee425a5a4b/scratchpad`.
- Reading done (see §2 of this file when written): in full — `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md` lines
  1–2435 and §6 tail 3080–3201 (the rest of §6 by keyword search), `DECISIONS.md` ADR-024, 032, 036, 043, 044, 045 in full (the others by title and
  as quoted in `ARCHITECTURE.md`), `0052` RESULT, `0048` §8–§10, `0051` §19, `APP_TESTPLAN.md`, `CAP-062-FINDINGS.md`, `CAP-062-EVENT-NOTES.md`
  (header), `CAP-063-EVENT-NOTES.md` (the draft).
- Phase 0 step 1 done: capinfos (11,615 packets, raw, 0 `cap_len≠len`, 1 out-of-order pair; `.last` 961 packets 15:56:10.981–15:57:16.332),
  ffprobe (1650.15 s, 49,253 video packets, audio stream "none, 0 channels" — `ffmpeg -map 0:a` fails "no decoder found for: none"), full
  sha256 (the draft's hash prefixes each lack their first hex digit — corrected in the notes), export 65,536 bytes / 746 lines, no exec bits.
- Phase 0 step 2 done: every frame scanned (`scan.py`, 49,253 frames, screen crop 50×105 gray; non-app segments → 523 sampled frames on 18
  contact sheets `priv/sheet01…18.png`, all viewed). Personal data: see §0 (privacy).
- Phase 0 step 3/4 done: maintainer (AskUserQuestion, this chat): privacy "Onveranderd laten (Recommended)"; migration "Ja, zo uitvoeren
  (Recommended)". The film's first frame reads **15:57:33** (not :34 as the draft/question said) → folder renamed with `git mv` to
  `captures/CAP-063-2026-09-27_15-57-33_16-25-03-Group_AY/`; `sha256sum -c` of the fresh source listing against the destination: 7/7 OK.
- Phase A/B/C analysis done (scratchpad: pw063t.txt pw_rpc decode with times, d48.txt DLCI 0x04/0x08/0x0a payloads, ctl.txt RFCOMM
  SABM/DISC/UA, d2sent.txt + fixture comparison, sys.txt system log 13:57:30–14:27:10 UTC, adv.txt Buds LE adverts, `priv/`, `f/` film crops).
  Clock: phone = overlay + 1.4 s (±0.2, measured at 16:05→16:06 and 16:23→16:24, no drift); logcat/system log UTC = local − 2 h 00 min 00.0 s.
- Phase D done: `CAP-063-FINDINGS.md` written; `CAP-063-EVENT-NOTES.md` rewritten (phone time, evidence column, step mapping; Appendix A kept).
- Phase E done: improvement list §6 below.
- **Resumed 2026-09-28** (after a usage-limit stop at the end of Phase E; the Progress block's next steps were followed in order): the Android
  source fetched (§8), the checkpoint asked (§9), the approved documentation applied (§10), `ensure_footers.py` run, `lint_docs.py` exit 0.
  Waiting only for the maintainer's commit/push decision (prompt task 23).

## 1. Plain-language answers (the maintainer's observations and wishes)

Film has **no sound**, so "you can hear it" stays your observation; what the logs add is whether the command was sent and accepted.

1. **Find My Buds** — ✅ both rings (docked 16:18:48 and on the table 16:24:14) were sent and **accepted** by the Buds, and both Stops. New: a bud
   **in the case** accepts a ring too.
2. **EQ and presets** — ✅ 5 of 5 changes accepted and stored in the Buds (the next Connect reads them back). That it sounds different is your observation.
3. **Balance and mono** — ✅ 20 balance and 5 mono changes, all accepted. The balance never showed "Centre": the slider has 201 steps on a small
   track, your finger landed on 1–11 — a small UI improvement (I-3).
4. **Conversation detection** — ✅ the switch works (4/4). While you talked, the **Buds themselves** paused the music (an ordinary "pause" media command
   at 16:09:22.9) and resumed it 7.4 s later — probably conversation detection; a tap on a bud sends exactly the same command, so the wire alone cannot
   prove it (🟡).
5. **Connects by itself when a bud comes out of the case** — ✅ 16:20:25 (buds out → the Buds called the phone → the app opened 1.6 s later), and also
   after pairing, after Android reconnected, after the Buds closed the channel (3×) and on return to the app.
6. **Battery** — ✅ every percentage on screen equals what the Buds sent at that moment, each with its own time. One puzzle: 16:02–16:03 a bud lay
   on the slot but the Buds themselves said "not charging" (probably not seated deep enough) — the app told the truth.
7. **In/out of the case** — ✅ every change was shown within a second. **Lid closed** and **in the ear**: the Buds send nothing the app can read
   (the lid is only visible as the Buds going silent over BLE, which needs scanning — not allowed; see §6 W-7). **Not possible now.**
8. **Connecting / disconnecting** — ✅ 14 session ends, every one handled correctly; the automatic re-open rules held every time.
9. **ANC** — ✅ app: 3 of 3 changes accepted. Long press: the mode changed three times **without** the app sending anything (🟡 your long presses,
   off film). ⚠️ One defect: after you put the buds back in your ears (16:06:48) the ANC buttons stayed **disabled** for ~6 minutes, because the app
   only re-checks on Refresh or a reconnect (I-1).
10. **Use touch controls** — ✅ accepted both ways; while it was off the Buds sent **no** tap command at all, with it on your taps paused/played.
11. **Press-and-hold** — ✅ all 10 changes accepted. **Digital assistant:** on the wire *nothing* happened when you held a bud — no voice command to
    Android, no media command. The assistant route of these Buds is (🟡, strong) the Google app's "Assistant for headphones" service over a separate
    Bluetooth channel (GSND), and that service was **stopped by Android** during your whole assistant test (16:00–16:15). So on GrapheneOS the option
    does nothing except the Buds' own sound. Proposal: keep it, but explain it (I-5). **Wish — checkboxes for the ANC modes the hold cycles through:**
    needs the Pixel 7a capture first (Group AR, `CAP-056`) because the order of the bits is disputed; then a protocol promotion and an ADR (W-11).
12. **In-ear detection** — read correctly (on) at all 15 Connects. **Writable:** possible after a small promotion of its label and a new ADR (W-12b,
    draft below). **Show when a bud is in an ear:** the Buds send no per-bud "worn" signal; only an "any bud worn" hint (🟡) — needs the one-bud-in-
    the-ear capture first (W-12a).

What does **not** work well: ANC stays disabled after re-wearing (I-1); a loss that happens while the app is in the background keeps the vague text
"The Maestro channel (equalizer) was closed" (I-2); "Centre" balance is hard to hit (I-3); for ~2 s after Connect the previous per-bud lines stay
(I-4). What the next session should build: I-1, I-2, I-3, I-5 (and I-4), all without anything new on the wire except I-1's re-check (see §7).

## 2. Reading and method (prompt §0)

- **In full:** `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `AI_SESSION_LOG_PROCEDURE.md`, `APP_TESTPLAN.md`, `0052` RESULT,
  `CAP-062-FINDINGS.md`, the draft and the skeleton `CAP-063-EVENT-NOTES.md` (Appendix A), `PROTOCOL.md` lines 1–2435 and 3080–3201.
- **Partly (disclosed):** `PROTOCOL.md` §6 lines 2436–3079 by keyword search only; `DECISIONS.md` ADR-024, ADR-032, ADR-036, ADR-043, ADR-044,
  ADR-045 in full with every Update, the other ADRs by title and as quoted in `ARCHITECTURE.md`/`PROTOCOL.md`; `0048` RESULT §8–§10; `0051` RESULT
  §19 only (not §5–§15); `CAP-062-EVENT-NOTES.md` header and first rows; `TODO.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` and
  `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` only in the parts edited or cited; `id_registry.csv` rows used. This is less than the prompt's "in full" for
  `DECISIONS.md`, `TODO.md` and `0051`; no finding here depends on the unread parts, but a reviewer should know.
- **Kotlin (read only, nothing changed):** `EqScreen.kt` 200–262 (balance and EQ sliders), `SettingsUi.kt` 45–60, `ConnectionScreen.kt` 330–360
  (`channelLostMessage`), `BudsRepositoryImpl.kt` 280–300 and 640–672 (`updateAncAvailability`, `setAncMode`), `SettingsFixtures.kt` (all constants).
  Not read in full: any of these files as a whole, `SessionLoss.kt`, `SessionReopener.kt`, `AncScreen.kt`. The improvement list names where a change
  would go; the next FEATURE session must read those files in full.
- **No subagent was used.** Every number in the notes/findings was re-derived with the commands in `CAP-063-FINDINGS.md` (rule 4a).
- **External sources** (§8): the Android `BluetoothHeadset` page was fetched 2026-09-28; the other spec sentences are the ones already quoted and dated in `PROTOCOL.md`/`CAP-062-FINDINGS.md`.

## 3. Corrections to the first (video-only) draft of the timeline

| Draft said | Correct (evidence) |
|---|---|
| film starts 15:57:34 | first frame reads **15:57:33** (single-frame crop); the folder name uses :33 |
| clock offset ≈ +1 s (provisional) | **+1.4 s ±0.2**, measured at two minute flips, no drift |
| sha256 prefixes `1e86f191…`, `946bb87b…`, … | each lacked its first hex digit: `11e86f19…`, `d946bb87…`, `60afcebc…`, `c533fa33…`, `f8f9e1d6…`, `4e22c894…` |
| 16:05:25–28 "ANC Refresh → ACTIVE, changed without an app tap" (F5) | an **ACTIVE tap** (film t = 470 s) → app `Set 08` 4497 → ACK 4508; F5 is not identifiable |
| 16:14:48–51 one Refresh | **two** Refreshes 0.79 s apart (export 557/567) — this is the AY-13b test, done |
| 16:16:25–30 "Left charging in the case (16:16:30)" | the screen says Left **not** charging (16:16:30); the Left bud was still in the hand, seated ≈ 16:16:33 |
| 16:02:09 "E3 ✗? a bud in the case but not charging on screen" | the app is right: the Buds report that bud not charging (stream 3386, battery 3369) |
| 16:07:41–16:08:00 "more writes than drags?" | 12 releases, 12 writes — one per release (ADR-045), each acknowledged; not a defect |
| Debug mode "on from 15:58:21" | on at ≈ 15:58:23 phone; the first hex line is 15:58:31.308 (export 33) |

## 4. Re-test verdicts and per-step results

See `CAP-063-FINDINGS.md` §1 (every AY item with frames; the "Refuted if" list: **nothing refuted**) and `CAP-063-EVENT-NOTES.md` "Step mapping".
Summary: **confirmed** AY-0, AY-1 (ACK), AY-2, AY-2b, AY-4, AY-5, AY-6, AY-7, AY-8/8b, AY-9, AY-10 (accidentally), AY-11, AY-13a, AY-13b,
AY-14 (answered 8/8), all `0052` §9 reads/writes/read-back; **skipped** AY-3a–c, F7, K1–K5, L3, A5, B4, (E), S0, Z1, Z2; **not identifiable** F5.

`APP_TESTPLAN.md` summary for this run: A ✅ A3 A4, ⚠️ A1 A2, — A5 A6 · B ✅ B1–B3, — B4 · C ✅ C1–C4, C6–C10, ⚠️ C5 (one tap) · D ✅ D1 D2 ·
E ✅ E1–E9 · F ✅ F1(—, Transparency not tapped) F2 F3 F4 F6 F8, — F5 F7 · G ✅ G3 (refusal) , — G1 G2 G4 · H ✅ H0 H1 H6, ⚠️ H2 H3 H4, — H5 ·
M ✅ M1 M2 M4 M5 M6, ⚠️ M3 (no "Centre") · N ✅ N1–N5, — N6 · I ✅ I1 I2 I4, — I3 · J ✅ J1–J4 (J4 ≈ 1 min) · K — · L ✅ L1 L2, — L3.

## 5. Protocol correlation

`CAP-063-FINDINGS.md` §10.

## 6. Improvements (prioritised; nothing here is built)

Each: problem (evidence) → change → layer/files → wire → rules → risk → test → effort. "Decision class": **A** possible within the current
decisions; **B** needs a new maintainer decision (named, drafted); **C** needs evidence first; **D** not possible.

| # | Problem (evidence) | Change | Where | Wire | Rules / class | Risk | Test | Effort |
|---|---|---|---|---|---|---|---|---|
| **I-1** (P1) | ANC stays disabled after re-wearing until Refresh/reconnect (FINDINGS §3; `BudsRepositoryImpl.kt:651-657`; film 16:06:58–16:07:13) | A mode tap while `NOT_ALLOWED` does a normal claim with `Get` (`08 11`); if that claim's `Notify` reads `e8`, send the `Set` in the same claim; if `00`, show the message and send nothing | `BudsRepositoryImpl.setAncMode`, `AncScreen.kt` (buttons stay tappable, "check again" wording), `AncTileService` | the existing claim (`SABM` 0x04, `08 11`), then `08 12` only if `e8` — no new message type | ADR-032 (claim on a user tap) and ADR-009/024 cover the messages; **B**: it reverses the `0048` I-3 choice "claim nothing while NOT_ALLOWED" (ARCHITECTURE §3.1) → maintainer's OK + an ARCHITECTURE update, no ADR | one extra claim per refused tap (Play services contention, ADR-032) | repository test with `CAP-063` 4774 (`00`) then a claim answering `e8` (`CAP-063` 4184) → one `Set`; with `00` → no `Set`; hardware: re-wear, tap ADAPTIVE → `08 11`/`08 13 … e8`/`08 12` in one claim | S |
| **I-2** (P2) | A loss while the app is not visible stays "The Maestro channel (equalizer) was closed" although the link is down (FINDINGS §6 #8; export 586–593) | On return (or on the first link reading after a loss), if the cause is still undetermined, use the **current** reading: `NOT_CONNECTED` → "Android no longer shows the Buds connected …"; `CONNECTED` → "The Buds closed …" with "(while the app was in the background)" | `SessionLoss.kt` (`classifySessionLoss`), `BudsRepositoryImpl.onAndroidLink`, `ConnectionScreen.channelLostMessage` | none | **A** (`0048` I-7 refinement) | wrong wording if the link changed in between (say "now") | domain test with the export times 16:15:22.008 loss / 16:15:25.062 `NOT_CONNECTED` | S |
| **I-3** (P2) | "Centre" practically unreachable (12 releases near centre, none 0; FINDINGS §2) | Snap to 0 within ±3 on release, or a small "Centre" button; show "Centre" for 0 only (unchanged) | `EqScreen.kt` `BalanceSlider`, `SettingsUi.kt` | `WriteSetting 4:{17:0}` (a value inside ADR-045's range; no fixture yet — `CAP-022`/`046` centre samples read 0/1/2) | **A** (ADR-045 allows −100…+100) | a user who wants "Left 2" can still drag past ±3 | UI test not available (no Compose tests) → repository test encoding `17:0`; hardware: drag near centre → `4:{17:0}` → OK, "Centre" | XS |
| **I-4** (P3) | For ≈ 2 s after Connect the previous session's per-bud lines stay ("charging in the case (16:00:17)") | At Connect mark per-bud charging lines "from the last connection" until the first new report (as the Case's "last seen") | `BudsRepositoryImpl` (reset flags), `ConnectionScreen.budLine` | none | **A** (ARCHITECTURE §3.1 "provisional until reconciled") | none | repository test: Connect → lines stale until `CAP-063` 3046/3059 | XS |
| **I-5** (P2) | "Digital assistant" does nothing on this phone (FINDINGS §7: no `AT+BVRA`, no AVRCP, GSND closed) | Keep the option (it is the Buds' own setting, used when the Google app's assistant service is present); add a one-line note under "Press and hold": "Digital assistant works only with an assistant app that supports headphones (e.g. the Google app); otherwise the Buds just play a tone." No detection (reading the assistant role needs a privileged permission) | `ControlsScreen.kt` (text) | none | **A** (UI text; no GMS dependency, `AGENTS.md` §1) | wording must stay 🟡-honest ("may") | none needed; hardware: read the text | XS |
| **I-6** (P3) | Refresh battery claims DLCI 0x04 although the answered re-subscription (8/8) already returns per-bud entries whose field 1 is the percentage (`PROTOCOL.md` §4.3 Option F, 🟢 "field 1 = %") | Take Left/Right % also from stream 6.2.1/6.3.1 (with their own time), so Refresh could skip the DLCI 0x04 claim | `RuntimeInfo.kt`, `BudsRepositoryImpl` | fewer claims; nothing new | **B**: widen ADR-043 item 2 (an ADR-043 Update) — **C** first: every value in `CAP-062`/`CAP-063` is 100, so 6.2.1/6.3.1 vs `03 03` has never been compared on a non-100 value | a mismatch while charging (bit 7 in `03 03`) | cross-capture script over the 45 captures with a non-100 percentage; then fixtures | M |
| **W-7** lid / in-ear display | No RFCOMM/stream/Android signal for the lid (FINDINGS §8); LE adverts stop while the lid is closed with both buds inside | — | — | — | **D** (no readable signal; LE scanning would need `BLUETOOTH_SCAN`, and ADR-006's bounded exception covers the battery advert only — and "no advert" also means "out of range") | — | — | — |
| **W-11** ANC-mode checkboxes (field 12) | the bit order and one-list-or-two are disputed (`PROTOCOL.md` §4.5.3, 🔴) | after evidence: a Controls section with four checkboxes, read (`ReadSetting 4:12`) and written (`WriteSetting 4:{12:{1..4}}`) | `SettingFrame.kt`, `Maestro.kt` (`READABLE_FIELDS`), `ControlsScreen.kt` | a new read and write of field 12 | **C** then **B**: Group AR (`CAP-056`, Pixel 7a, official app, `CAP-063-EVENT-NOTES.md` A.8) → promotion of the bit order → a new ADR (read + write field 12) | wrong bit order would change the hold cycle (reversible) | fixtures from `CAP-056`; hardware: untick one mode, hold the bud | M |
| **W-12a** show "worn" | no per-bud signal; Settable `00`/`e8` = no bud / at least one bud worn is 🟡 (0 counter-examples in 22 here) | show "worn: yes/no" per claim, dated | `AncScreen`/`ConnectionScreen` | none | **C** (AY-3 on film: one bud visibly worn, one on the table, `08 11` each time) then **B** (promotion, ADR-024 Update) | the aggregate cannot say which bud | fixtures from that capture | S |
| **W-12b** in-ear detection writable | field 2 is 🟢 at category level ("CATEGORY_OHD"), the label 🟡; `CAP-024` 1850/1912 are the official writes in both directions | a switch on Controls, `WriteSetting 4:{2:0|1}` | `SettingFrame.kt`, `BudsRepositoryImpl`, `ControlsScreen.kt` | a new write of field 2 | **B**: (1) promote the label equivalence from `CAP-024`'s film (as D-1(a) did for field 22), (2) a new ADR (the draft "in-ear detection write" ADR below). 🔴 what the Buds do with OHD off: whether they still `DISC` DLCI 0x02 on wear changes (ADR-044's re-open) and still report Settable `00`/`e8` — test in the same capture | turning it off may stop the Buds' auto-pause and change ANC availability | fixtures from `CAP-024`; hardware: off → take a bud out → music keeps playing? `DISC`? Settable? | M |

**Draft ADR "in-ear detection write" (proposal; number not assigned or registered):** "`WriteSetting` unblocked for `qhr` field 2 (In-ear detection), 0/1, byte-identical to `CAP-024`
frames 1850/1912 for the same channel, on the announced channel (ADR-034), through the Safe-Mode gate (ADR-042), one write per tap, applied only on
the empty `RESPONSE` OK; the current value is read at Connect (ADR-036). Precondition: the label promotion of `PROTOCOL.md` §4.5.5. Consequence: the
app warns that with in-ear detection off the automatic re-open (ADR-044) and the ANC 'while worn' check (ADR-024 Update) may behave differently
(untested)."

## 7. Draft outline for the next FEATURE prompt

1. Reading as §0.1; this RESULT §6, `CAP-063-FINDINGS.md` §3/§6/§7.
2. Build I-1 (after the maintainer's OK to reverse the `0048` I-3 "claim nothing"), I-2, I-3, I-4, I-5 — real `CAP-063` bytes as fixtures (4774, 4184,
   4233/4241, 3046/3059; export times for I-2).
3. No new message type on the wire; nothing on DLCI 0x08; field 12 and field 2 stay gated unless W-11/W-12b's decisions exist.
4. Gate: `:data`/`:domain`/`:hardware` tests, lint, mutation checks for I-1 (Set without `e8`) and I-2.
5. Re-test plan (Group AZ): re-wear → tap ANC (I-1); background loss with the buds docked (I-2); balance centre (I-3); plus AY-3 (one bud worn on
   film) and, if chosen, W-12b's OHD-off steps.

## 8. External sources

Not re-fetched in this session (no network use was needed for the conclusions); the relevant sentences are already quoted and dated in
`PROTOCOL.md` §4.1 (Hearable Controls "Settable toggles"), `CAP-062-FINDINGS.md` §2 (acknowledgement page, NAK reason `0x02`) and `ARCHITECTURE.md`
§6.0b (RFCOMM spec). Fetched 2026-09-28: developer.android.com/reference/android/bluetooth/BluetoothHeadset, `startVoiceRecognition(BluetoothDevice)`: *"Start
Bluetooth voice recognition. This methods sends the voice recognition AT command to the headset and establishes the audio connection."* — Android's
public voice-recognition route is the HFP voice-recognition AT command (`AT+BVRA`); in `CAP-063` no `AT+BVRA` appears in either direction (FINDINGS §7).

## 9. Checkpoint answers (Phase F, `AskUserQuestion`, chat 2026-09-28 — recorded verbatim)

- **"FACT P-1"** (the re-subscription answered): **"Promoveren (Recommended)"**, preview text as recorded in `PROTOCOL.md` §4.3 Option F and ADR-043.
- **"Notities"** (multi-select): **"Hoorbaarheid als observatie, §6: AVRCP/assistent/ACL (🟡/🔴), Stream pusht ook docked (🟢), Settable = worn (🟡) steun"**.
- **"Volgende"**: **"I-1, I-2, I-3, I-5, I-4 (Recommended)"** — this includes the maintainer's OK to reverse the `0048` I-3 "claim nothing while
  NOT_ALLOWED" for a user tap (I-1); `ARCHITECTURE.md` §3.1 is updated when it is built.
- **"Wensen"** (multi-select): **"Group AR eerst (wens 11) (Recommended), [the in-ear ADR draft] voorbereiden (wens 12), AY-3-capture (wens 12a)"** (the option label named a number; it is omitted here because it is not registered) — the draft keeps no
  number until it is written (the number was not registered; `lint_docs.py`).
- Phase 0 (asked 2026-09-27): privacy **"Onveranderd laten (Recommended)"**; migration **"Ja, zo uitvoeren (Recommended)"**.

## 10. Documentation applied (only what was approved)

`PROTOCOL.md` (§4.1, §4.2, §4.3 Option F ×2, §4.4, §4.5.5a, §4.5.7 dated Updates; three §6 items; §8 row), `DECISIONS.md` (ADR-024 Update, ADR-043
Update), `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group AY section, Capture Index row), `id_registry.csv` (`CAP-063` → analyzed), `TODO.md` (done items, next
FEATURE/captures, open items), `CHANGELOG.md`, `ai-sessions/INDEX.md`, `README.md` (status), `_sidebar.md` and `TODO.md` links to the new folder name;
`CAP-063-EVENT-NOTES.md` Appendix A: the events-file line notes "not provided for this run" (the only change to the skeleton text). Not changed:
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (its evidence cells were not edited — a gap left for a later documentation pass), `APP_TESTPLAN.md` (no test found
wrongly written), anything under `android/`. Historical references to the old folder name in `ai-sessions/0052` RESULT and the `0053` PROMPT stay as
written (the linter lists them as informational).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0053_CAPTURE_RESULT_2026_09_27.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0053_CAPTURE_RESULT_2026_09_27
