# 0101_CROSSCHECK_RESULT_2026_10_18.md — Reverse engineering of the companion APK for the questions `CAP-079` left: which bud is worn, the life of a stream subscription, field 21's bud core, `SetWallclock` and the Dosimeter day, and the Find negatives (ring time-out, Case)

**Number:** 0101
**Category:** CROSSCHECK
**Date:** 2026-10-18
**Title:** A read-only research session on the official companion app `v1.0.955078536-10253511`, cross-checked against the captures already in `captures/`:
(A) which bud is in an ear, (B) the life of a server-streaming call, (C) the bud core of setting 21, (D) `SetWallclock` and the Dosimeter day index, (E) the Find
negatives — proposals only; no code change, no new wire traffic.
**Status:** complete (closed 2026-10-18 in chat — Checkpoint D answered; every proposal decided, nothing promoted)

## Progress

| Phase | State | Notes |
|---|---|---|
| Reading | done | "Files read" below |
| 0 — set-up, Checkpoint 0 | done | §0, §Checkpoint 0 |
| 1 (A) per-bud wear | done | §1A; `REVERSE_ENGINEERING.md` entry "the on-head state per bud" |
| 1 (B) stream lifecycle | done | §1B; entry "the life of a server-streaming call" |
| 1 (C) field 21's core | done | §1C; entry "the route of a write, and the wall clock" |
| 1 (D) `SetWallclock`, day index | done | §1D; same entry |
| 1 (E) Find negatives (+ the maintainer's Case addition) | done | §1E; entry "Find negatives" |
| 2 — wire cross-check | done | §2; four censuses over all 117 capture logs (scratchpad) |
| 3 — findings, proposals, Checkpoint D | done | §3 proposals; §4 the answers; the approved notes, entries and lines written |

Scratchpad: /tmp/claude-1000/-home-tedsluis-git-opencontrolpixelbudspro2/88a322fc-4ab2-4f58-a916-e8beb16f172f/scratchpad — client_error_census.txt (census 1),
census2.txt (OOBE/SetWallclock/day index/device type per log), census3.txt (request channel vs announced channel per log), census4.txt (the OOBE method id per
log), cancel_batch.txt (the cancel-effect measurement on nine logs), the scripts census_client_error.sh, census2.sh, census3.py, census4.sh, cancel_effect.py
(scratch files, not committed; the commands they run are quoted in the entries).

## 0. Set-up

- `git log -1`: `5b5d5b8594b8f47736fc8f7c3b645a29b03b1dc9 docs: ai-sessions/0100 RESULT — the […] APKs (build commit b0a9283)` (the subject names the
  planned 1.4.1 capture, whose id is registered on `feature/0100-fixes` only); `git status --short`: clean (0 lines);
  branch at start: `feature/0100-fixes` (`main` at `98ad4bf`, PR #69 merged). Session clock: 2026-10-18 16:48 CEST.
- APK workspace: `reverse-engineering/apk/v1.0.955078536-10253511/` holds `base.apk`, the two splits, `jadx-output/sources/` (11,365 files under `defpackage/`),
  `apktool-output/{smali,smali_classes2,smali_classes3}`, `apktool-output-arm64_v8a/`, `pbtk-output/`. `sha256sum base.apk` =
  `b12d75d07f7743b83ed0d507a9d6b7b2abf61d8f1e7c3e63aadc442c097d6c86` — equal to `reverse-engineering/APK_VERSIONS.md` line 31.
- Tools: `tshark` 4.6.9, `python3`; `reverse-engineering/tools/pwrpc_name_table` present (not needed: `scripts/pwrpc_decode.py` names every id but `0x57f3becb`,
  which this session matched by its raw bytes). Machine: 15 GB RAM, 2 GB free at the start — every grep bounded, one `tshark` at a time (the censuses ran
  sequentially in the background).

## Files read

In full: `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md` (all 3,928 lines, in 13 parts), `DECISIONS.md` (all 3,086 lines,
every ADR with its Updates), `TODO.md`, `AI_SESSION_LOG_PROCEDURE.md`, `CAP-079-FINDINGS.md`, `CAP-079-EVENT-NOTES.md`, the prompt.
In part: `REVERSE_ENGINEERING.md` (the header and method, 1–131; `fut`/`fux`, 481–562; the `qhr` register with its Updates, 1017–1487; `goq` and the
decoded-shapes register, 2464–2543; `frb`/`fuh`/`glk`/`gjv` and the Hearing-wellness section, 3269–3531; the `qjb` entry, 2050–2112; the tail, 4036–4560);
`CAP-078-FINDINGS.md` §0 (the log letters), §4–§15; `CAP-069-FINDINGS.md` §8; `CAP-058-FINDINGS.md` §6; `CAP-041-FINDINGS.md` §3, §8; `CAP-027-FINDINGS.md` §3.3;
`reverse-engineering/APK_VERSIONS.md` (the table); `ai-sessions/INDEX.md` (the tail). Decompiled code read in full: `qhp`, `qje`, `qhy`, `gao`, `gav`, `gam`,
`gvj`, `gvi` (via the register), `hmv`, `hmw`, `gux`, `guy`, `hkf`, `hjd`, `fvl`, `fwv`, `fwx`, `fxk`, `gcc`, `gca`, `nps`, `nqf`, `nqo`, `fsf`, `fzh`, `gan`;
in part: `gaa`, `fxm`, `fyv`, `fyf`, `fyo`, `fxz`, `fua`, `fst`, `fsy`, `fsz`, `fsg`, `fqg`, `fqe`, `fzg`, `frb`, `fmp` (smali), `frk` (smali), `fws`, `esa`,
`esk`, `fvp`, `fuj`, `fvk`, `glk`, `glm`, `gtc`, `ftw`, `fzz`, `grj`, `guq`, `hmk`, `hms`, `gih`, `gfp`, `gon`, `nqb`, `mne`, `oql`, `oys`, `oyr`, `hwy`, `gda`,
`a`. Not read: the `ai-sessions/0099`/`0100` RESULTs (the prompt: nothing on trust; their claims were re-derived where used).

## Checkpoint 0 (chat 2026-10-18, `AskUserQuestion` "Vragen" / "Branch" / "Naamtabel")

- **Questions:** all five run now — (A), (B), (C), (D)+(E) selected. **Addition by the maintainer to (E):** within the ADR-017 mechanical-search
  boundary, search the decompiled APK (JADX and smali) for any local, non-GMS/non-FMD mechanism to ring the charging case — specifically all
  callers/references of `goq.CASE` (channels 18/23, addresses `00 2b` / `80 2d`), the unmapped action-trigger `qhr` fields (23, 30, 31, 34–39), any local
  ring/earcon/find/beacon command builders, and any GATT/RFCOMM references to `FE2C1238` or `109b862f` — every match with `file:line`, a positive control
  per negative search.
- **Branch:** a new branch from `main` (`98ad4bf`) — `crosscheck/0101-apk-wear-streams`; the prompt and the INDEX row "planned" are on `main` (checked:
  `git ls-tree main ai-sessions/ | grep -c 0101` → 1). Note: `main` lacks the `ai-sessions/0100` docs commits (the ADR-069 Update, `TODO.md`'s 1.4.1 lines);
  this session edits `main`'s versions — merge order is the maintainer's (`TODO.md` §1: the 1.4.1 hardware run and release first).
- **Scratch name table:** yes, in the scratchpad only (in the end not needed, see §0).

## 1. The code (every claim `file:line` under `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/` or `apktool-output/`; 🟢 (code) = this code exists and does this; the full entries are in `REVERSE_ENGINEERING.md`, section of 2026-10-18)

### 1A. Which bud is in an ear

1. 🟢 (code) **The official app's per-bud on/off-head state comes from `maestro_pw.Maestro/SubscribeToOobeActions`** (`gav.java:46–52`, "Init oobe actions rpc
   call"), parsed as `qje` field 1 → `qhp` (`LEFT_ON_HEAD(10)`, `LEFT_OFF_HEAD(11)`, `RIGHT_ON_HEAD(12)`, `RIGHT_OFF_HEAD(13)`, `qhp.java:18–21`), split into a
   Left and a Right stream mapped ON_HEAD → 2, OFF_HEAD/UNKNOWN → 1 (`gav.java:17–31`). The call is opened per primary route ("Route change to %d") and only
   while a screen observes the LiveData (`gav.java:82–88`); it retries three times on an error (`.J(3L)`, `oyr.java:30–45`) and is cancelled when the last
   observer leaves (`frb.java:86–93`).
2. 🟢 (code) **Which implementation the Pro 2 gets:** the `gao` provider is chosen by device type — 1, 2 → `gam` (legacy TLV-fed), 4, 5, 6, 9 → `gav`
   (`fqg.java:369–376`); 🟢 (wire) `GetHardwareInfo` field 1 = 6 in all 82 logs that carry the answer → `gav`. (The app's type names: 1 PRESTO, 2 STRETTO,
   4/5 ALLEGRO, 6 LARGO, 9 AG12AG22 — `fsf.java:9–15`; ⚪ which product each name is, the SKU colours `ijf.java:41–55` suggest LARGO = the Pro 2 — not needed.)
3. 🟢 (code) **Who reads it — the complete list:** the eartip seal test (`hmw.java:67` → `hmv` `NONE`/`RUN_LEFT`/`RUN_RIGHT`/`RUN_BOTH`/`INTERRUPTED`/
   `NO_BUD_ON_HEAD`, `frk.smali:1300–1385` "Got either buds detected, start OTTS test." / "OTTS interrupted." / "No bud detected for OTTS."), the OOBE music
   demo controller `gvj` ("Control audio with left/right OHD changed: %d", `gvj.java:124/133`, "Buds off head, do nothing", `:53`), the OOBE-mode lifecycle
   `guy` ("Enable/Disable OOBE mode" = `qhr` 6, `guy.java:25/39`) and the OTA screens (`hkf.java:12`: both on head within 5 s). **No settings, status or battery
   screen observes it**, and no string or resource names a per-bud in-ear display (only the in-ear detection *setting* and the fit test's "Put one or both
   earbuds in before starting"). So the screen the maintainer saw switching per bud is one of these flows, or the GMS-drawn device page — to settle in chat.
4. 🟢 (code) **The modes around it:** the OOBE wizard writes setting 6 on/off (`guy`); the fit test writes setting 14 ("Init OTTS test.", `hmw.java:76`). The
   app never writes 3 or 6 — `ARCHITECTURE.md` §5a's rule stands.
5. 🟢 (code) **Runtime info is not a per-bud wear source:** field 7 (`qhy`, 3 × BOOL) — 7.2/7.1 gate the Dosimeter live subscription per bud (`fsy.java:122–169`),
   **7.3 = "Low latency mode"** (`fwx.java:22`, `fst.java:73–79`, `frb.java:70`); top-level fields 2/3 are not in the schema. Nothing in the APK reads GSND
   CONTROL Code `0x05` (ADR-025).
6. 🟢 (wire, re-derived) `CAP-078` H 7388 → 7390 `1:10`, 7391 `1:12` → 7429 RESPONSE status 14 at 16:53:31.516 — **65 ms before** the settings push 13 = 3 of the
   bud-out (7432) and **16 s before** the Buds' `DISC` (7441): the call ended at the wear change, not with the session. The method id is in `CAP-078` only
   (0 of 115 other logs, `tshark -Y "frame contains 25:cb:be:f3:57"`; positive control `CAP-078` → 4).
7. 🟡 **Reading:** the firmware serves `SubscribeToOobeActions` with the state at subscribe and keeps it open only while OOBE mode (6) or OTTS mode (14) is
   on; with neither on it ends the call (`UNAVAILABLE`) at the first wear change. One trial; the official app re-subscribes up to three times — a client that
   re-subscribes after each `UNAVAILABLE` would get the new state at once (🟡, untested). **What OpenControl would need without OOBE mode:** either that
   re-subscribe loop (one request per wear change, read-only — experiment E1 below), or a mode write (6 is barred; 14 = the fit-test mode, unknown side
   effects — not proposed).

### 1B. The life of a stream subscription

1. 🟢 (code) **The official app cancels its streams:** `nps.l()` (`nps.java:65–113`) sends an `RpcPacket` type 4 (`CLIENT_ERROR`, `a.java:842–843`) with status
   `CANCELLED`; every stream is opened with `Observable.using` whose disposer cancels the call if it is still open: runtime info (`frb.java:73–81`), quiet mode
   (`:56–65`), the settings stream (`fmp.smali:556/601/610` "Try to cancel setting rpc"), the Dosimeter live stream per bud (`frb.java:46–55`), OOBE actions
   (`:86–93`), head gestures (`gah.java:35–37`). Triggers: the last observer leaves; **a route change** (each stream is re-opened on the new route, the old
   call cancelled); **the session end** — `fut.e()` (`fut.smali:1209`, the socket close) stops every per-session subscriber (`fuh.b()`), whose disposers send
   the cancels ("Failed to send cancellation packet" if the socket is already gone, `nqf.java:23–30`).
2. 🟢 (code) **A packet for a call that is not pending** gets a `CLIENT_ERROR` `FAILED_PRECONDITION` reply (`gon.java:238/248`, `nqb.java:35–62`).
3. 🟢 (code) **Writes:** a pending `WriteSetting` of the same field is cancelled by the next one (`fyv.java:56–83`, `aie` discriminator 7).
4. 🟢 (wire) **374 `CLIENT_ERROR` packets in the 117 logs, all official-app, 0 OpenControl:** `CANCELLED` `SubscribeToLiveDb` 100, `SubscribeToQuietModeStatus` 94,
   `SubscribeRuntimeInfo` 57, `SubscribeToSettingsChanges` 56, `WriteSetting` 13 (all `CAP-050`, 0.00–0.01 s before the next write — item 3), `KpiStream` 2;
   `FAILED_PRECONDITION` `SubscribeToLiveDb` 24, `WriteSetting` 13, `SubscribeToSettingsChanges` 8, `ReadSetting` 3, `GetStatus` 2, `FetchDailySummaries` 2 (item 2).
   In `CAP-037` each cancel set precedes the phone's own `DISC` of MAESTRO by 0–100 ms (1743/1744/1745 → 1746 sent, +7 ms): **cancel, then close**.
5. 🔴 **Not shown by the logs:** whether the Buds stop a running stream on a `CANCELLED` with the channel left open — every cancel of an active stream in
   `CAP-005`/`009`/`015`/`039`/`040`/`048`/`050`/`069`/`002` precedes a `DISC` or fell in a silent stream (scratch cancel_batch.txt). `CAP-079`'s streams outlived
   the session because OpenControl neither cancels nor answers (the Buds kept them per channel id, `CAP-079-FINDINGS.md` §14). Experiment E2.

### 1C. Field 21's core

1. 🟢 (code) **A `WriteSetting` goes to the primary route** — the core whose `GetSoftwareInfo` answer the app took as primary ("Update primary to %d",
   `fxk.java:30–35`; `fyv.java:44–49` → `fsz.java:62–74` → `esk.java:129–148`). `ReadSetting` uses the same client map (`fyv.java:51–54`). **The Dosimeter
   calls address both bud cores by name** (`fyh.java:28–36`, `fyf.smali:294`; `fvp.java:55–58`).
2. 🟢 (wire, all 117 logs) the official app's `WriteSetting` 1,044 of 1,044 on the announced channel (the six "other" are OpenControl's F4 on 21 in
   `CAP-079`, refused); `ReadSetting` 8,852 of 8,852; `SubscribeRuntimeInfo`, `SubscribeToSettingsChanges`, `SetWallclock`, `GetHardwareInfo` likewise;
   `FetchDailySummaries` 220 same / 222 other, `SubscribeToLiveDb` 213 / 209 — the per-core pairs. `CAP-058`'s field-21 writes 5747 … 5790: channel 21 =
   the announced channel (4 of 4). **Nobody ever sent a `ReadSetting` or a `WriteSetting` to the non-primary core except OpenControl's F4** — so
   "reads work on both" holds only for the Dosimeter methods; the firmware's rule for the other core is 🟡 "refuses `WriteSetting`" (4 of 4) and 🔴 for reads.
3. Consequence for ADR-034 item 3 (send only on the announced channel): already what the official app does for every `maestro_pw.Maestro` call.

### 1D. `SetWallclock` and the day index

1. 🟢 (code) **Once per connection, first thing:** `fyo.a()` (`fyo.java:18–19`) calls `ftm.i()` before the `ReadSetting` sweep → `fua.i()` (`fua.java:183–187`) →
   "Set wall clock time" (`fst.java:53–59`) → `gcc.a()` (`gcc.java:21–48`, "syncWallclock to %s"): `Instant.now().toEpochMilli()`, the primary route's channel,
   retry 3. One request per bud-core session, never per bud.
2. 🟢 (wire) 144 requests in 57 official-app logs, all on the announced channel, one per connection (41 logs exactly one; `CAP-037` 30 = its 30 connections);
   `CAP-041` 783's value = the frame's capture time (`CAP-041-FINDINGS.md` §8); 0 in OpenControl's logs except the experiment (`CAP-079`, 2).
3. 🟢 (code) **The app never converts the day index to a date** (no `toEpochDay`/`ofEpochDay` in the Maestro code — the 16 hits are `j$.time` and two
   Bundle/Notification helpers, `ccs`/`cdb`); `hwy.ai` (`hwy.java:1971–1989`) sums the entries `c − 6 … c` with `c` = the Buds' current index (`gdg.c`, the
   larger of the two buds).
4. 🟢 (wire, field 1 of every `FetchDailySummaries` answer, 57 logs; the Buds' clock = real time in all three regimes, runtime-info field 2 within 0.3 s of the
   capture time in `CAP-050`/`056`/`069`/`058`): **three regimes** — 2026-08-09 … 09-14: 11 … 47, one per 24 h with a boundary ≈ 05:30–05:50 UTC (day 0 ≈
   2026-07-29/30); 2026-09-28: 227; 2026-10-04 … 10-18: 461, 462, 475 (day 0 = 2025-06-30, the 🟡 of `CAP-079`). The jumps (+180 in 14 days, +234 in 6 days) fall
   in the periods of OpenControl's runs without `SetWallclock`. 🔴 what the index counts; the 🟡 "day 0 = 2025-06-30" holds for 14 days only. For draft
   ADR-060 (iii) the epoch does not matter if the app does what the official app does: the Buds' current index is "today".

### 1E. The Find negatives, and the maintainer's Case addition

1. 🟢 (code) The complete pw_rpc method catalogue of `fux.java` (36 names, listed in the entry) has no ring, find, locate or beacon method (positive control
   `WriteSetting`). 🟢 No ring/find/beacon/earcon builder in the app's packages: the grep finds the OOBE Find My Device terms page (`gtc.java:63–91`), the GMS
   "FindMyProxyLiveData" (`gsz`) and the `case_earcon` preference (setting 27) only. 🟢 No ring time-out constant: the app has no ring; the Device Action
   page's timeout byte is a Message Stream matter (`PROTOCOL.md` §4.4 Note of 2026-10-18) and the Message Stream is not in the APK (ADR-025).
2. 🟢 (code) **`goq.CASE` is used by the OTA machinery only** — the channel map (`fux.java:91/99`), the per-route channel outputs (`frg.java:51–228`),
   `pw.transfer.Transfer` Read/Write clients for the CASE route (`fsg.java:112`, `fvk.java:19–34`), the `charging_case` OTA bundle (`glk.java:374`, `glm.java:79`),
   the DI map (`fqg.java:265`). 🟢 (wire) no request on channels 18/23 (or 20/22/25/27) in any of the 117 logs. (The smali names the enum by its obfuscated field
   letter — `Lgoq;->CASE` → 0 is not evidence; positive control `LEFT_BT_CORE` → 0 as well.)
3. 🟢 (code) **No write site for `qhr` 23, 30, 31, 34–39** (23 `iput` sites; the 8-line window grep → 0; positive control field 29 at `cmi.smali:3495–3499`).
4. 🟢 (code) **`FE2C1238` / `109b862f` are not in the APK** (JADX, three smali trees, `res` → exit 1; no `fe2c` at all; positive control `25e97ff7` → `fzd`,
   `gbm`, `fqg`).
5. 🟢 (external, re-read 2026-10-18, pin `2620367`) pbpctrl `README.md:52`: "the case does not have a Bluetooth receiver itself"; no README sentence mentions
   ringing or finding the case.
6. **Conclusion for the Case ring (ADR-027 stands):** nothing in this APK rings the case locally; the only local channel to the case core is pw_rpc on
   channels 18/23, used for OTA transfer, never seen on any wire. No value is proposed for a send.

## 2. The wire cross-check (existing captures only; commands in the entries and the scratchpad scripts)

| Code-side answer | Wire check | Result |
|---|---|---|
| (A) `SubscribeToOobeActions` is the per-bud source, opened only on OOBE/fit-test/OTA screens | `tshark -Y "frame contains 25:cb:be:f3:57"` per log (117) | only `CAP-078` (4 frames) — the official app never opened it in 115 logs ✓ |
| (A) the call ends at a wear change without OOBE/OTTS mode (🟡) | `CAP-078` H 7388–7441 re-read | status 14 at 31.516, push 13 = 3 at 31.580, `DISC` at 47.86 ✓ (one trial) |
| (B) streams are cancelled with `CLIENT_ERROR CANCELLED`; stray packets answered `FAILED_PRECONDITION` | census 1 (374 packets) | ✓ counts per method; `CAP-037` cancel → `DISC` +7 ms |
| (B) the Buds stop a stream on a cancel with the channel open | `cancel_effect.py` on 9 logs | not decidable from the logs (🔴) |
| (C) writes/reads go to the primary = announced core; Dosimeter per core | census 3 | ✓ 1,044/1,044; 8,852/8,852; Dosimeter ≈ half/half; no CASE channel |
| (D) `SetWallclock` once per connection on the primary route | census 2 | ✓ 144, all on the announced channel, 1 per connection |
| (D) the day index | census 2 + the clock in four logs | three regimes; the Buds' clock real in all — 🔴 |
| (E) Case ring | greps + census 3 (channels 18/23) | no mechanism, no traffic ✓ |

## 3. Proposals (nothing promoted — the maintainer decides, `AGENTS.md` §6)

### 3.1 `PROTOCOL.md` notes (drafts; code-side findings are "🟢 (code)", wire findings carry their frames and commands)

- **N1 — §4.5.5 In-ear detection / §6 (the OOBE items of 2026-10-17):** "Note (2026-10-18, `ai-sessions/0101`; no status change) — 🟢 (code) the official
  app's per-bud on/off-head state is `SubscribeToOobeActions` (`gav.java:46–66`), read only by the eartip seal test, the OOBE music demo, the OOBE-mode
  lifecycle (which writes setting 6) and the OTA screens (`REVERSE_ENGINEERING.md`, entry of 2026-10-18); no settings, status or battery screen reads a
  per-bud state, and nothing in the APK reads GSND CONTROL `0x05`. 🟢 (wire) the method id occurs in `CAP-078` only (0 of 115 other logs); there the call
  ended with status 14 at 16:53:31.516, 65 ms before the settings push of the bud-out (7432) and 16 s before the Buds' `DISC` (7441). 🟡 the firmware keeps
  the stream open only while setting 6 (OOBE mode) or 14 (OTTS mode) is on; a client that re-subscribes after each `UNAVAILABLE` may get the new state —
  experiment E1."
- **N2 — §4.3 Option F:** "Note (2026-10-18, `ai-sessions/0101`; no status change) — 🟢 (code) entry 7's three booleans: 7.1 (Right) and 7.2 (Left) gate the
  official app's Dosimeter live subscription per bud (`fsy.java:122–169`: `true` → unsubscribe, 0.0), **7.3 = 'Low latency mode'** (`fwx.java:22`,
  `frb.java:70`); the stream's top-level fields 2 and 3 stay outside the schema. The 🔴 '7.3' of this Option is answered on the code side."
- **N3 — §2.2a (after the Note of 2026-10-18) / §6:** "Note (2026-10-18, `ai-sessions/0101`; no status change) — 🟢 (code) the official app ends a stream call
  with a `CLIENT_ERROR` packet, status `CANCELLED` (`nps.java:65–113`), on a route change, when its last observer leaves and at the socket close
  (`fut.smali:1209`), and answers a packet of a call it no longer holds with `CLIENT_ERROR` `FAILED_PRECONDITION` (`gon.java:238/248`). 🟢 (wire) 374 such
  packets in 117 logs, none from OpenControl: CANCELLED — SubscribeToLiveDb 100, SubscribeToQuietModeStatus 94, SubscribeRuntimeInfo 57,
  SubscribeToSettingsChanges 56, WriteSetting 13 (`CAP-050`: a pending write cancelled by the next one), KpiStream 2; FAILED_PRECONDITION 52; in `CAP-037`
  each cancel set precedes the phone's `DISC` of MAESTRO by ≤ 100 ms (1743–1745 → 1746). 🔴 whether the Buds stop a running stream on a CANCELLED with the
  channel open — experiment E2. Commands: `scripts/pwrpc_decode.py` per log, `grep CLIENT_ERROR` (`ai-sessions/0101` RESULT §2)."
- **N4 — §4.5.8a (after the Note of 2026-10-18):** "Note (2026-10-18, `ai-sessions/0101`; no status change) — 🟢 (code) the official app sends every
  `maestro_pw.Maestro` request to its primary route — the core of the `GetSoftwareInfo` answer (`fxk.java:30–35`, `fyv.java:44–49`) — and only the Dosimeter
  calls to both bud cores by name (`fyh.java:28–36`, `fvp.java:55–58`). 🟢 (wire, 117 logs) `WriteSetting` 1,044 of 1,044 and `ReadSetting` 8,852 of 8,852
  on the announced channel; `CAP-058`'s four field-21 writes on channel 21 = the announced channel. Nobody but OpenControl's F4 ever wrote or read a
  setting on the other core: the 🟡 'refused on the non-hosting core' has 4 samples and no counter-sample; reads there are 🔴."
- **N5 — §6 (the Dosimeter item of 2026-10-18 / draft ADR-060 (iii)):** "Note (2026-10-18, `ai-sessions/0101`; no status change) — 🟢 (code) `SetWallclock`
  is sent once per connection before the settings sweep (`fyo.java:18–19`, `gcc.java:21–48`), on the primary route, with the phone's clock; 🟢 (wire) 144
  requests in 57 logs, all on the announced channel, one per connection. 🟢 (code) the app never converts the day index to a date; it sums the seven
  entries up to the Buds' current index (`hwy.java:1971–1998`). 🟢 (wire) the index is not one calendar count: 11 … 47 on 2026-08-09 … 09-14 (one per 24 h,
  boundary ≈ 05:30–05:50 UTC), 227 on 09-28, 461 … 475 on 10-04 … 10-18, while the Buds' clock (runtime-info field 2) was real time in every official-app
  session checked; the jumps fall in OpenControl's periods without `SetWallclock`. 🔴 what the index counts; the 🟡 'day 0 = 2025-06-30' holds since 10-04."

### 3.2 Draft ADR texts — the session's advice

- **Per-bud wear (ADR-059's "never per bud"):** **no draft now.** The code-side source is a stream the firmware ended at the first wear change in the one
  trial without OOBE mode; a feature would rest on experiment E1 (read-only) first. If E1 shows a usable loop, the later ADR would say: one
  `SubscribeToOobeActions` per MAESTRO session, re-sent after each `UNAVAILABLE`, the two events at subscribe as the per-bud state, shown as "Left/Right:
  in ear / out" with the subscribe time; setting 6 and 14 never written.
- **Stream cancel (a design rule for ADR-060 and the settings stream):** a draft for the maintainer to accept or not, under the next free ADR number:
  "**(DRAFT — proposed, not accepted) — Cancel what you subscribed to.** Before the app closes the MAESTRO channel itself (Disconnect, 'Use different Buds', app exit) it sends one
  `CLIENT_ERROR` packet with status `CANCELLED` per open stream call (`SubscribeRuntimeInfo`, `SubscribeToSettingsChanges`; the Dosimeter streams if ADR-060
  is ever accepted), byte-identical in shape to the official app's (`CAP-037` 1743–1745; type 4, the call's channel/service/method ids, status 1); a
  `SERVER_STREAM` packet for a call the app does not hold is answered with `CLIENT_ERROR` `FAILED_PRECONDITION` (the official app's `gon.java:248`). Why:
  `CAP-079` §14 — the Buds keep a stream per channel id across RFCOMM closes, ACL losses and a user switch, so an uncancelled subscription keeps the Buds
  streaming to nobody (≈ 1 live packet/s per channel with audio). Not covered: a Buds-side `DISC` (nothing can be sent). Verification: the next app run —
  the cancels on the wire before the app's `DISC`; and E2 for what the Buds do with them." The session recommends this draft only if E2 is planned; else a
  `TODO.md` line.
- **Field 21's core:** **no ADR** — ADR-034 item 3 (send only on the announced channel) already is the official app's rule; the note N4 records the wire.

### 3.3 Experiments for the next CAPTURE/FEATURE round (each needs its own approval; exact bytes from the reference table of `CAP-079-EVENT-NOTES.md`)

- **E1 (read-only, re-send of an approved request): the OOBE stream across wear changes.** Experiment card, user 10: `SubscribeToOobeActions`
  (`7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25 cb be f3 57 …` on the announced channel) with both buds worn → expect `1:10`, `1:12`; take the Left out → expect
  either `1:11` or RESPONSE status 14; **re-send at once after a status 14** → expect the state (`1:11`? `1:12`) — repeat for Right out, both in; ears on
  film. Risk: none known (two trials so far, no side effect). Settles the 🟡 of 1A item 7 and whether a per-bud feature is possible without a mode write.
- **E2 (a new send — needs an ADR before it): cancel a live stream with the channel open.** Experiment card: `SubscribeToLiveDb` on channel 19 with music
  playing (≈ 60 packets/min), then one `CLIENT_ERROR` `CANCELLED` built as `CAP-037` 1749 for that call (type 4, channel 19, service `0x73d5d805`, method
  `0x4d93b6e2`, status 1; its CRC) → expect the packets to stop within a second; then the same for `SubscribeToSettingsChanges` (a hold afterwards must not
  be pushed). Risk: a wrong packet is ignored or answered with an error (pw_rpc); no setting is written.
- **E3 (read-only re-test): field 21 after a re-home.** With the session on 19, F4 on 19 → OK (known); take the Left out so the Buds re-home to 21; F4 on 21
  → expect OK, on 19 → expect `FAILED_PRECONDITION`; and one `ReadSetting 4:21` on the non-hosting core → the 🔴 of 1C item 2. Risk: none beyond `CAP-079`'s.
- **E4 (read-only): the day index.** `FetchDailySummaries` on both channels daily for a week (one tap each), once with the Buds rebooted (both in the case,
  lid closed, overnight) and once after an official-app connection (which sends `SetWallclock`) — does the index keep one count, and what moves it?
- **Not proposed:** writing setting 14 (OTTS mode) to keep the OOBE stream alive — unknown side effects on the fit-test firmware path.

### 3.4 `TODO.md` changes (drafts)

- §4, the per-bud item of 2026-10-17: replace its task list by "answered by `ai-sessions/0101` (`REVERSE_ENGINEERING.md`, entry of 2026-10-18): the source is
  `SubscribeToOobeActions`, read only by the OOBE/fit-test/OTA screens; the stream ended at the first wear change without OOBE mode (`CAP-078`); next: E1".
- §4, the `CAP-079` open questions: "how long a stream subscription survives" → the cancel finding (N3) and E2; "why field 21 is refused on the
  non-hosting core" → N4 and E3; "why one bud's day index moved at F1" → N5 and E4.
- §4, the Find item: "`FIND-004` … the Case has no receiver of its own" — add "re-checked 2026-10-18 (`ai-sessions/0101`): no local Case mechanism in the APK
  (`goq.CASE` = OTA transfer only)".
- §5, the ADR-060 candidate: add "(iii) the day index has three regimes in the captures — the app should use the Buds' current index as 'today' (N5)".

## 4. Checkpoint D (chat 2026-10-18, `AskUserQuestion` "PROTOCOL" / "Draft ADR" / "Experimenten" / "TODO.md")

- **`PROTOCOL.md` notes:** all five approved and written — N1 (§4.5.5 Note + the §6 OOBE item's Note), N2 (§4.3 Option F Note), N3 (§2.2a Note), N4
  (§4.5.8a Note), N5 (a new §6 item); one §8 row. No status change anywhere.
- **Draft ADR "Cancel what you subscribed to":** *"Alleen een TODO.md-regel"* — no draft in `DECISIONS.md`; the rule is a `TODO.md` §4 line for the next FEATURE
  session that touches the Connect/Disconnect sequence (the `CAP-079` item). The per-bud ADR: none now (as advised). Field 21: no ADR (as advised).
- **Experiments:** E1, E2, E3 and E4 all go to the next CAPTURE/FEATURE round (each with its own approval; E2 needs an ADR before it is sent) — recorded in
  `TODO.md` §4.
- **`TODO.md`:** all four drafts applied (§4 the per-bud item, §4 the `CAP-079` questions, §4 the Find line, §5 the ADR-060 candidate).
- Also written: four `REVERSE_ENGINEERING.md` entries (dated 2026-10-18, before the template), the `ai-sessions/INDEX.md` row (`complete`), this file.
- Checks: `python3 scripts/ensure_footers.py` (all footers up to date); `python3 scripts/lint_docs.py` — the result is in §5 below. The "dead filename
  reference" lines the lint prints for `ai-sessions/0054` … `0091` are present on a docs-only export of `main` as well (`git archive main` without
  `captures/`, the lint there: "clean", exit 0) and are informational.

## 5. Open after this session (not deferred documentation — questions)

- The screen on which the maintainer saw the official app switch per bud (chat 2026-10-17) — **answered in chat 2026-10-18 (`AskUserQuestion` "Per-bud
  scherm"): the eartip fit test (Sound → eartip seal)**, i.e. `hmw`/`hmv` (§1A item 3) — the one screen the code says reads the per-bud stream outside OOBE and
  OTA; consistent with the trace. (The GMS-drawn device page is out of this APK, ADR-025.)
- The planned 1.4.1 capture's id is registered on `feature/0100-fixes` only; this branch, from `main`, does not cite it (the lint would flag it until the
  branches merge).

## Deferred documentation

Each item has its `TODO.md` line (quoted so `grep` finds it):
- `TODO.md` §4 (the per-bud item): "**Next: experiment E1** (the stream re-subscribed after each `UNAVAILABLE`, read-only, ears on film …)".
- `TODO.md` §4 (the `CAP-079` item): "experiment **E2** (a cancel on an open channel — a new send, its own ADR first)"; "the rule "cancel what you subscribed to
  before the app's own DISC; answer a stream packet of a call the app does not hold with `FAILED_PRECONDITION`" is a TODO, not a draft ADR"; "**E3** (F4 after a
  re-home, plus one `ReadSetting 4:21` on the non-hosting core)"; "**E4** (daily for a week, after a Buds reboot and after an official-app connection)".
- `TODO.md` §5 (the ADR-060 candidate): "**(iii), `ai-sessions/0101` (2026-10-18):** the day index runs in three regimes …".
- Nothing else: the per-bud-screen question of §5 is answered (the fit test).

## Commits

The maintainer's "yes" in chat 2026-10-18 (`AskUserQuestion` "Commit", *"Commit en push (Recommended)"*). On `crosscheck/0101-apk-wear-streams` (from `main`
`98ad4bf`), pushed to `origin`:
- COMMIT1_PLACEHOLDER
- COMMIT2_PLACEHOLDER
No pull request was opened by the agent (the maintainer's step).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0101_CROSSCHECK_RESULT_2026_10_18.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0101_CROSSCHECK_RESULT_2026_10_18
