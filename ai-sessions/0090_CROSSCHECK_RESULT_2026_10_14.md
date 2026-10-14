# 0090_CROSSCHECK_RESULT_2026_10_14.md — Point-by-point check of the pbpctrl-derived findings list (E-01 … E-23), led by the noise-control question on DLCI 0x02

**Number:** 0090
**Category:** CROSSCHECK
**Date:** 2026-10-14
**Title:** Point-by-point check of the pbpctrl-derived findings list (E-01 … E-23) against the external sources at pinned commits and this repository's own
captures, APK notes and documents
**Status:** awaiting maintainer sign-off — every proposal answered in chat 2026-10-14 and applied; not committed (the commit question is open)

## Summary

**The leading question — answered: yes.** In this project's own captures of the official app, DLCI 0x02 carries the current noise-control mode as Maestro
setting 13 (1 Off, 2 NC, 3 Transparency, 4 Adaptive, 0 when no mode is settable). A client that subscribes with `SubscribeToSettingsChanges` gets a
`4:{13:n}` push for every mode change the Buds report: 73 of 73 Buds-side changes (incl. the three filmed holds of `CAP-051` and three `TOUCH-007` holds of
`CAP-027`) and 39 of 39 app-driven ones, 1–150 ms after the Message Stream's Notify — and also when **no** client holds the Message Stream (`CAP-038` 2170,
2205). The only misses are changes the Buds make in the same instant as their own close of MAESTRO (12 of 77); the re-open's `ReadSetting 4:13` shows them.
OpenControl never subscribed and never read 13 (0 of 33 logs) — that is why nothing moved on DLCI 0x02 at `CAP-075`'s 31 holds. On the code side the official
app's ANC state has a single writer fed only by setting 13 (`fxb.smali:1237`) — it does not depend on the Message Stream. pbpctrl, MagicPodsCore and
omarchy-pixelbuds all control ANC through setting 13 and subscribe to the stream; their names are most likely Google's (E-22). Promoted to 🟢 in
`PROTOCOL.md` §4.1 and accepted as **ADR-064** (subscribe + read 13, read only) — both approved in chat. **What settles it for OpenControl:** the build of
ADR-064 and its hardware run in both users — holds with the noise-control tab closed must appear without any DLCI 0x04 claim; a wear-change re-home and the
re-open's read must catch the rest. Switching ANC with `WriteSetting 4:{13:n}` stays an untested experiment (no capture holds such a write).

**Applied** (all approved in chat): the §4.1 FACT Update, §6 pointers and a 🟡 one-bud/two-bud item, the §2.2 attribution correction, the §4.5.7 balance note,
the §0.1 Class-of-Device note; ADR-064 (+ pointer Updates on ADR-025, ADR-034, ADR-036) and ADR-060's draft item (vii); the `qhr` register Update (the APK chain
and pbpctrl's names, labelled as naming); a `DESKRESEARCH_FINDINGS.md` entry (tables, recounts, provenance) and a wall-clock note; `CAP-051-FINDINGS.md` §3
rewritten in place; `AGENTS.md` §5 note; `ARCHITECTURE.md` §5a "never write 3 or 6"; `TODO.md` lines; the committed script
`scripts/anc_setting13_crosscheck.py`. **Next for the maintainer:** whether to commit and push; then the FEATURE session for ADR-064.

## Progress

- Phase 0 (set-up): done.
- Phase A (E-02, E-01, E-03): done.
- Phase B (E-04 … E-23): done (evidence and the verdict table below).
- Phase C (checkpoint): done (two rounds, answers below).
- Phase D (apply): done; `ensure_footers` and `lint_docs` below.
- Phase E (finish): the RESULT, the INDEX row; commit/push awaiting the maintainer.

## Phase 0 — set-up

- Prompt: `ai-sessions/0090_CROSSCHECK_PROMPT_2026_10_14.md`; number check: `ai-sessions/INDEX.md` ends at the planned 0090 row, no other 0090 files
  (`ls ai-sessions | grep -E '^00(89|9[0-9])'` → only the 0089 pair and this prompt).
- `git log -1 --oneline` → `c386a1f Merge pull request #54 from tedsluis/bot/sitemap`; `git status --short` → `?? android/domain/bin/` (build output, never
  staged); `git fetch` → nothing new; `git log --oneline HEAD..origin/main` → empty.
- Branch: `research/pbpctrl-crosscheck` (from an up-to-date `main`).

- `python3 scripts/lint_docs.py` before any change: exit 1, with one error only — the new RESULT file had no footer; after
  `PYTHONDONTWRITEBYTECODE=1 python3 scripts/ensure_footers.py` (1 file updated, the RESULT) exit 0. The "dead filename reference" lines it prints for
  historical session logs are informational.
- External sources, each cloned into its own empty directory under this session's scratchpad (never into the repository), checked out at the pin, never
  built or run:

  | Source | `rev-parse HEAD` | Later commits on the branch |
  |---|---|---|
  | `qzed/pbpctrl` (`main`, plus `refs/pull/*/head`) | `2620367a41f92135059f637d92a2e4f427abb44e` | none (`git log --oneline 2620367..origin/main` empty); PR branches `pr/15`, `pr/18`, `pr/21`, `pr/22` |
  | `T3CHNOLOG1C/MagicPodsCore` `pbp2` | `e0288c311450bcc82188de10dfe6438985e28de4` (= `origin/pbp2`) | none; upstream `steam3d/MagicPodsCore#24` is **merged**, head `e0288c3` (`gh pr view 24 -R steam3d/MagicPodsCore`, 2026-10-14) |
  | `rdoupe/omarchy-pixelbuds` | `2614ac1543829cf35489215fc92ef1c0162d549e` | none |

  `ba253a7`, `003a3fc`, `7e75204` and `4ca2adf` are ancestors of the pbpctrl pin (`git merge-base --is-ancestor <c> 2620367`).

## Phase A — the leading items

### The tool behind the counts

`scripts/anc_setting13_crosscheck.py` (new, this session; reuses `scripts/pwrpc_decode.py`'s pw_hdlc/pw_rpc reader and `scripts/message_stream_tally.py`'s
message reassembly). Per log it lists every Buds `Notify ANC state` (`08 13`) on DLCI 0x04/0x05 — classed `set` (an `08 12` from the phone ≤ 3 s before),
`get` (an `08 11 00 00` ≤ 1 s before) or `unprovoked` — every `SubscribeToSettingsChanges` REQUEST, every `SERVER_STREAM` `4:{13:n}`, every
`ReadSetting` RESPONSE `4:{13:n}` and every `WriteSetting` `4:{13:n}`, with each frame's capture time; `--census` counts each event once across logs (a
`.log.last` that repeats a neighbour adds nothing) and tracks RFCOMM `UA`/`DISC` per channel. The predicted mapping (pbpctrl's `AncState`, the APK's
`qhs`): 1 Off ↔ `0x20`, 2 Active ↔ `0x08`, 3 Aware ↔ `0x80`, 4 Adaptive ↔ `0x40`, and 0 Unknown when the Notify's Settable byte is `00`.

**Scoping (`AGENTS.md` §13 step 1).** In every capture used below all DLCI 2/3/4/5 payload sits on the handle(s) whose HCI Connection Complete names the
Buds' address (redacted here to `04:00:…:07`): `tshark -r <log> -Y 'bthci_evt.code==0x03 && bthci_evt.status==0' -T fields -e frame.number -e
bthci_evt.connection_handle -e bthci_evt.bd_addr` against `tshark -r <log> -Y 'btrfcomm.len>0 && (btrfcomm.dlci>=2 && btrfcomm.dlci<=5)' -T fields -e
bthci_acl.chandle | sort | uniq -c` — e.g. `CAP-051` frame 786 → handle `0x0002`, 170 payload frames all on `0x0002`; `CAP-069` frames 291/6584/7763 →
`0x0002`/`0x0001`/`0x0002`, payload 690 + 188 frames on those two; `CAP-038` 841/2814 → `0x0001`/`0x0004`; `CAP-056` 835 → `0x0005`.

### E-02 — Buds-side mode changes on the settings stream (M)

**1. Which captures subscribe.** `python3 scripts/anc_setting13_crosscheck.py --summary <every log>` (96 logs, all present as `btsnoop` files; the
`btsnooz` logs of `CAP-012`/`013`/`031` are not among them): a `SubscribeToSettingsChanges` REQUEST appears in the official-app captures only — once per
MAESTRO (re-)open, e.g. `CAP-051` frame 1177 `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25 f5 ad 21 28 01 97 54 b0 7e` (REQUEST, channel 21, `maestro_pw.Maestro`
`0x7ede71ea`, method `0x2821adf5` = `h65599("SubscribeToSettingsChanges")`, empty payload). 0 in `CAP-004`, `017`, `033`, `034`, `035`, `043` (no official
app on the wire) and 0 in all 33 OpenControl logs (item 4).

**2–3. Every Buds-initiated Notify against the stream** — census over the 46 official-app captures that subscribe (`--census` on every `btsnoop` log of
`CAP-001`, `002`, `003`, `005`–`011`, `014`–`016`, `018`–`029`, `032`, `036`–`042`, `044`–`051`, `053`, `054`, `056`, `058`, `069`; `CAP-049`'s combined
log left out, it repeats its parts):

| Class (each event once) | Count | What it means |
|---|---|---|
| Notify with no Set/Get before it (a Buds-side change), a `4:{13:n}` stream packet within 1 s, `n` = the predicted value | **73** | the stream mirrors the change |
| — the same, two Notifies 6–168 ms apart, matched in order | 2 | `CAP-056` 3983/4001 ↔ 4025 `13:4`/4028 `13:2`; `CAP-040` `.log.last` 10841/10851 ↔ 10842 `13:3`/10858 `13:0` (the nearest-in-time rule paired them crosswise) |
| — no stream packet: the Buds `DISC` MAESTRO (and the Message Stream) 2–30 ms after the Notify | 12 | `CAP-006` 2875, `007` 1350, `016` 2768, `037` 7165, `038` 3847, `040` 6386, `045` 1849, `050` 5972, `051` 1993, `056` 2458 and 3670, `069` 6156 — e.g. `CAP-051`: Notify 1993 21:45:45.603, Buds `DISC` DLCI 0x02 1996 21:45:45.608 and DLCI 0x04 1998 21:45:45.612 (`tshark … -Y 'btrfcomm.frame_type!=0xef && (btrfcomm.dlci==2||btrfcomm.dlci==4)'`); the session re-homing of lead L-1 |
| — no stream packet: MAESTRO being re-opened at that moment | 1 | `CAP-047` `-2.log` 3996 (04:35:02.195); `SABM` DLCI 0x03 4030 (02.825), subscribe 4051; the re-open's `ReadSetting 4:13` (4066) reads 3 = the Notify's `0x80` |
| — no stream packet: a Notify at a fresh Message Stream open, mode unchanged | 1 | `CAP-005` 2348 (`SABM`/`UA` 2330/2332, Settable `00`, Off — the same as the session's first report) |
| Notify after an `08 12` Set (the phone's own change) with a stream packet, same value | **39** | the stream mirrors a Message-Stream Set too |
| `4:{13:n}` stream packet with a Notify within 1 s | **100** | |
| `4:{13:n}` stream packet with **no** Notify and **no Message Stream open on any handle** | **3** | `CAP-038` 2170 `13:3` (06:50:50.588), 2205 `13:2` (06:51:02.395), 2479 `13:0` (06:51:53.435) — the phone closed DLCI 0x05 at 1756 (06:50:37.225 `DISC`, `UA` 1773) and nothing re-opened it on handle `0x0001` before the ACL dropped (2484, 06:51:57.185); 0 `WriteSetting 4:{13}` in the log |

The filmed holds, frame by frame (Δt = stream − Notify):

| Capture | Hold (film) | Notify (DLCI 0x04) | Stream (DLCI 0x02) | Δt |
|---|---|---|---|---|
| `CAP-051` | gesture #1, contact 21:43:38–42 | 1707 21:43:43.335 `08 13 00 04 01 e8 e8 08` | 1717 21:43:43.479 `7e 00 a5 03 2a 04 22 02 68 02 08 07 10 15 1d ea 71 de 7d 5e 25 f5 ad 21 28 cb b3 58 d5 7e` = `SERVER_STREAM` ch 21 `4:{13:2}` | +0.144 s |
| `CAP-051` | gesture #2, ~21:43:51–54 | 1754 21:43:56.002 `… e8 e8 40` | 1755 21:43:56.003 `7e 00 a5 03 2a 04 22 02 68 04 08 07 10 15 … 39 67 a2 f7 7e` = `4:{13:4}` | +0.002 s |
| `CAP-051` | gesture #3, ~21:44:02–03 | 1801 21:44:05.101 `… e8 e8 80` | 1802 21:44:05.149 `… 68 03 …` = `4:{13:3}` | +0.048 s |
| `CAP-027` | `TOUCH-007` Right | 2930 15:48:24.898 `… e8 e8 08` | 2932 `4:{13:2}` | +0.012 s |
| `CAP-027` | `TOUCH-007` Left | 3056 15:48:46.657 `… e8 e8 40` | 3057 `4:{13:4}` | +0.009 s |
| `CAP-027` | `TOUCH-007` Left (second step) | 3091 15:48:52.342 `… e8 e8 80` | 3093 `4:{13:3}` | +0.017 s |

The in-app tap of `CAP-051` (Set 1527) is mirrored too: Notify 1530/1533 → stream 1534 `4:{13:3}` (`7e 00 a5 03 2a 04 22 02 68 03 08 07 …`). `CAP-021`
holds 13 unprovoked Notifies cycling Adaptive → Transparency → NC → Off (06:00:39–06:04:53 UTC), each mirrored within 1–56 ms with the predicted value
(1743/1744 `13:4` … 5166/5167 `13:4`); `CAP-021-EVENT-NOTES.md` names no action at those times — 🟡 press-and-holds (the order is the hold loop's), not on
film. `CAP-019` 6/6, `CAP-045` 8/8 (+ 1 at a `DISC`), `CAP-069` 5/5 (+ 1 at a `DISC`, + 8 Set-provoked) likewise.

`CAP-051-FINDINGS.md` §3 reported "no `Sent`-direction payload of any kind on DLCI 0x02 at or near any of the four actions" — true, and it is the phone's
side only; the Buds' `SERVER_STREAM` packets 1534/1717/1755/1802 in the same window were read there as "the already-documented periodic push"
(the 10-s runtime-info stream is 1565/1711/1796). The stream packets are a second, independent report of each hold.

**4. OpenControl's own captures.** `--summary` on the 33 logs of `CAP-059`…`068`, `070`…`075`: `subscribe=0 stream13=0 read13=0 write13=0` in every
one. Positive controls: the same command finds `subscribe=4 … read13=13` in `CAP-069` and `subscribe=6 … read13=7` in `CAP-056`, and on OpenControl's
own logs the decoder sees OpenControl's MAESTRO requests — `python3 scripts/pwrpc_decode.py <log> | grep -c 'dir=Sent REQUEST.*method=ReadSetting'` →
117 (`CAP-073` `.log.last`), 26 (`CAP-075` `hci2.log.last`), 112 (`CAP-064`), fields `4:2 4:4 4:7 4:11 4:12 4:15 4:16 4:17 4:19 4:22 4:27 4:28 4:29`, never
`4:13`; 0 `SubscribeToSettingsChanges` packets in either direction. So `CAP-075` §12 item 4 ("nothing on 0x02 moved at any of 31 holds") is what an
unsubscribed session shows: 🟢 FACT that OpenControl never subscribed or read 13; 🟡 that the Buds send the settings stream only to a subscriber (pw_rpc
server streams answer a call; no capture holds a stream packet on a MAESTRO session that had not subscribed — 0 of 33).

### E-01 — setting 13 = the current mode; `ReadSetting 4:13` (M)

- **External (pinned).** pbpctrl `cli/src/main.rs:158–175`: `SetSetting::Anc` → `SettingValue::CurrentAncrState(AncState::Off|Aware|Active|Adaptive)`
  written with `WriteSetting`; `libmaestro/proto/maestro_pw.proto:96` `ALLEGRO_CURRENT_ANCR_STATE = 13;`, `:175–181` `enum AncState { … OFF = 1; ACTIVE = 2;
  AWARE = 3; ADAPTIVE = 4; }`; `cli/src/main.rs:444–476` `cmd_anc_cycle` reads 12 and 13 and writes 13. MagicPodsCore `src/sdk/pbp/PixelBudsHelper.h:51`
  `CurrentAncrStateValueField = 13; // SettingValue.current_ancr_state`, `src/sdk/pbp/setters/PixelBudsSetAnc.cpp:13–21` (`WriteSetting` with field 13),
  `src/sdk/pbp/watchers/PixelBudsAncWatcher.cpp:47–56` (the mode from a `ReadSetting` RESPONSE **and** from `SubscribeToSettingsChanges` SERVER_STREAM).
  omarchy-pixelbuds `bridge/maestro.py:488` `S_ANC = 13`, `:549`, `bridge/pixelbuds_bridge.py:783` `self.client.subscribe(maestro.M_SUB_SETTINGS)`.
  pbpctrl's own `libmaestro/examples/maestro_listen.rs:156–162` subscribes to settings changes; the unmerged PR #22 TUI subscribes and maps
  `CurrentAncrState` to "anc" (`git diff 2620367...pr/22`, `tui/src/maestro_client.rs`). The PR #24 text: *"Pixel Buds Pro 2 were extensively tested and
  verified working but Pixel Buds Pro is untested on hardware"*. None of the three sends anything on the Message Stream for ANC.
- **APK (mechanical, ADR-017).** `qhs.java:5–10` `ANC_STATE_UNKNOWN(0) … OFF(1) ACTIVE(2) AWARE(3) ADAPTIVE(4)` — the same numbers; field 13 = `ENUM qhs`
  (the `qhr` register); written only by `fye.a(qhs)` (`fye.java:17–39`, `qhrVar.b = 13`); read by `fxb.java:278–311` (`case 13`, "Log ANC settings to
  Clearcut").
- **Wire (this project).** `--census`: 382 `ReadSetting 4:13` answers (each once): **359** equal the latest mode the Buds reported before them (a Notify's
  predicted value or a stream `n`), 6 have no earlier report in their log, 17 differ — each after a stretch with no report reachable (a MAESTRO re-open after a
  `DISC`, or minutes of silence), e.g. `CAP-047` `-2.log` 2537 `13:3` 65.6 s after a Settable-`00` Notify (buds out of the case again), `CAP-051` 2247 `13:0`
  4.7 s after the re-homing `DISC`; one (`CAP-042` `.log.last` 4096 `13:2`, 15:20:08.166) answered 0.4 s **before** the Notify/stream of the same change
  (4099/4100). All five values occur: `CAP-021` 1841/1842 Off ↔ `13:1`, 1806/1807 NC ↔ `13:2`, 1779/1780 Transparency ↔ `13:3`, 1743/1744 Adaptive ↔
  `13:4`; Settable `00` ↔ `13:0` (`CAP-045` 2273/2277, `CAP-069` 7520/7530, `CAP-038` 4158/4169). 🟢 for the mapping on the wire (this project's
  captures, 112 Notify/stream pairs, 0 counter-examples once Settable `00` ↔ 0 is applied).
- **Writes.** `write13=0` in all 96 logs (the prompt's "0 in 80 logs", re-derived over today's set; duplicated `.log.last` content included, which can only
  add, not remove, a write). Neither the official app nor OpenControl has ever written 13 on the wire — what the Buds do with a `WriteSetting 4:{13:n}` is
  **untested here** (external projects only: 🟡).

### E-03 — settings 8 / 9 / 10 (M)

- **External.** `maestro_pw.proto:91–93` `ALLEGRO_ANC_ACCESSIBILITY_MODE = 8; ALLEGRO_ANCR_STATE_ONE_BUD = 9; ALLEGRO_ANCR_STATE_TWO_BUDS = 10;` and `:125–127`
  "reading anc_accessibility_mode / ancr_state_one_bud / ancr_state_two_buds returns non-zero status (code: 2)" — commit `4ca2adf` (2022-12-23,
  "libmaestro: Add proto spec for known settings"), i.e. on the gen-1 Pixel Buds Pro of that time. They are absent from pbpctrl's `SettingValue` oneof.
- **APK.** `grep -rl` over `jadx-output/sources` and the smali: `ANCR_STATE_ONE_BUD` 0/0, `ANCR_STATE_TWO_BUDS` 0/0, `ANC_ACCESSIBILITY` 0/0, `ANCR_STATE`
  0/0, `CURRENT_ANCR` 0/0 (positive control: `ANC_STATE_ADAPTIVE` 3/2). The register's types agree with the names: 8 `BOOL`, 9 and 10 `ENUM qhs`; no write
  site and no reader (`fxb` default branch, "Receive unhandled value case"), and the official sweep skips 6, 8, 9, 10, 14, 20, 33 (`fyo.java:25–26`).
  `AllegroSetting` appears once, in the log string "handleAllegroSetting for %s" (`fxb.java:43`) — the source of pbpctrl's type name (E-22).
- **`CAP-067` 08:07:34 → 08:12:44** (OpenControl 1.0.0, the user without Play): no settings stream (not subscribed), no GSND Code `0x05` (DLCI 0x08 not
  opened). The signals that exist are the six Notifies with the film's wear state (`CAP-067-FINDINGS.md` §2–§3; `tshark -r CAP-067-btsnoop_hci.log{.last,} -Y
  'btrfcomm.len>0 && (btrfcomm.dlci==4||btrfcomm.dlci==5) && frame contains 08:13:00:04'`): A1027 08:06:09 `e8 e8 80` and A1178 08:07:34 `e8 e8 80` — one bud
  worn, the other on the table; B474 08:12:44 `e8 e8 08` and B1025 08:15:54 `e8 e8 08` — both worn. Transparent with one bud, Active with two: consistent with
  separate one-bud and two-bud modes. Which wear change happened between 08:07:34 and 08:12:44 is not on the wire (Bluetooth was off part of that time, B432
  re-open after Bluetooth-on).
- **`CAP-056`** (`CAP-056-FINDINGS.md` §4 table, rows 1–4, 9, 10): one bud out → `40` Adaptive (2257, 2458, 3983), back to two worn → `08` NC (2322, 2764,
  4001), each mirrored on the stream where MAESTRO was open (`13:4` 2259, `13:2` 2324/2765, 4025/4028). `CAP-066-FINDINGS.md` §3 ("`80` after a bud went into
  or out of an ear with the other bud not worn"; `CAP-064`/`CAP-065` saw `40` in the same places).
- 🟡 **HYPOTHESIS (E-03):** the Buds keep one mode for one bud worn and one for two (pbpctrl's names 9/10 fit), and set 13 from it on a wear change — the
  one-bud mode was `40` in `CAP-056`/`064`/`065` and `80` in `CAP-066`/`067`, so it is stored, not fixed. Not tested: reading 9/10 on the Pro 2 (pbpctrl
  reports status 2 on the gen 1; the official app never asks). Test: `ReadSetting 4:8`, `4:9`, `4:10` once (read-only), then one bud out and in, with the
  stream on.

## Phase B — the other items

### E-04 — Ring status and its ACK (M)
- External: pbpctrl `libgfps/examples/ring.rs:57–64` (sends `0x03`; "Pixel Buds Pro ignore messages with a timeout"), `:135–160` (status updates after the
  ACK, each answered with an ACK; "follow-up updates once the user has touched the buds and ringing stops"). The live Device Action page (fetched 2026-10-14):
  *"Providers may want to notify a Seeker when it changes the ringing status … Seeker's will listen for this message and provide an acknowledgement"*.
- Wire, every Buds `04 01 00 01 xx` in all 96 logs (`tshark -r <log> -Y 'btrfcomm.len>0 && (btrfcomm.dlci==4||btrfcomm.dlci==5) && (frame contains
  04:01:00:01 || frame contains ff:01:00:02:04:01)' -T fields -e frame.time_epoch -e frame.p2p_dir -e data.data`, classed by an awk pass quoted in the session
  log): 44 messages in 10 logs (40 distinct — `CAP-071`/`072` logs repeat), **every one < 1 s after the phone's own Ring/Stop with the same value** (e.g.
  `CAP-025` 2040 Sent `04 01 00 01 01` → 2044 ACK `ff 01 00 03 04 01 00` → 2045 Rcvd `04 01 00 01 01` → 2048 Sent `ff 01 00 02 04 01`); **0 unprompted** (no
  capture stopped a ring on the bud while a client held the channel). The official app ACKs (4 of 4, `CAP-025`); OpenControl never (0 in `CAP-059`…`072`).
- Verdict: claim partly valid (the echo and the ACK exist; a Buds-side "stopped" report is unobserved here). Usefulness high (the known issue since 1.0.1).

### E-05 — "Ring both" = `0x03`; the case has no receiver (M)
- Spec page (fetched 2026-10-14): *"0x03 (0b00000011): Ring both left and right"*; *"The second byte in additional data, if present, represents the timeout in
  seconds."* pbpctrl `README.md:52` "the case does not have a Bluetooth receiver itself". Already in ADR-027's 2026-09-24 Update and `TODO.md` §4.
  One ACK per command (`CAP-025`: one `ff 01 00 03 04 01 00` per Ring).

### E-06 — §2.2 "Hypothesis B (pbpctrl-derived)" (M)
- pbpctrl's framing history: `git log --reverse --date=short -- libmaestro/src/hdlc` → 6be65c1 2022-12-17 "Prepare HDLC support", b2c3dbf 2022-12-17 "Add
  CRC32 implementation"; `decoder.rs:147` `crc::crc32(...)`. `0x5a` occurs only inside the CRC-32 table (`crc.rs:95`); no magic byte, no length field, no
  CRC-16 anywhere (`grep -rn -i 'magic\|crc.\?16'` → 0). The third-party summary (KashaMalaga/PixelBudsMacOS `Reference/README.md:21`, read with `gh api`,
  2026-10-14): "HDLC U-frames (control byte `0x03`, CRC-16)" — wrong against pbpctrl and against this project's 640/640 CRC-32 check (§2.2a).
  Verdict: valid — §2.2's attribution "pbpctrl-derived" is not supported by pbpctrl.

### E-07 — Balance sign convention (M)
- `python3 -I -c` over raw 0…200: pbpctrl `VolumeAsymmetry::from_raw` (`settings.rs:478–489`) equals `−((n>>1) ^ −(n&1))` for **all 201 values** (199 → +100,
  200 → −100, 123 → +62). So pbpctrl's −100 "(left)" (`cli.rs:243`) = this project's +100 = Left (ADR-026): the same physical mapping. Its `Display`
  (`settings.rs:510–515`) shows `left = min(100, 100 − v)`, `right = min(100, 100 + v)` — linear attenuation of the other side (🟡 external, for §4.5.7's 🔴).

### E-08 — Firmware entry order (M)
- pbpctrl `maestro_pw.proto:18–21` "Note: order might not be correct" case 1, right 2, left 3; this project's code trace (§2.2a 2026-10-01) 1 Case, 2 Left,
  3 Right; the wire cannot tell (573 identical entries). `HardwareInfo` serial order `proto:38–40` case/right/left = ADR-058. NOTE.

### E-09 — Setting 18 (M)
- pbpctrl `proto:135` "reading last_saved_user_eq returns non-zero status (code: 2)" (gen 1, 2022); on the Pro 2 `ReadSetting 4:18` answers (ADR-034, e.g.
  `CAP-006` 974). 🟡 a generation or firmware difference.

### E-10 — Wall clock (M)
- pbpctrl `service/impls/maestro.rs:115–116` `// TODO: - SetWallClock`; `proto:44` `timestamp_ms = 2; // maybe unix time in ms (consistent ~60s difference to
  actual time)`. Fits the 🟡 of `DESKRESEARCH_FINDINGS.md` 2026-10-03 item 3 (a clock set by the official app, 52–548 ms off right after `SetWallclock`, then
  left to drift if nobody sets it) — it does not prove it. ADR-060 is a draft: adding "set the clock first; drift" to its 🟡 list is a maintainer edit.

### E-11 — Dosimeter (M)
- pbpctrl `proto:294–296` `float intensity = 2; // convert to dB via log10(x) * 10`, `examples/maestro_listen.rs:166–168`; `service/impls/dosimeter.rs:14,24`
  `ServerStreamRpc` (its proto `:300` says unary). `CAP-027` `SubscribeToLiveDb` streams (`python3 scripts/pwrpc_decode.py … | grep SubscribeToLiveDb`):
  197 values per channel; `10·log10`: ch 19 min −12.2 / median 69.9 / max 83.2, ch 21 −20.0 / 69.9 / 83.1 — a plausible dB range (🟡). `FetchDailySummaries`
  (`CAP-036` 1423 ch 19: `1:36 2:{1:33 6:f32(0.00)} 2:{1:34 …}`) matches pbpctrl's `DosimeterSummary` {1, repeated 2 {1, 6 float}, 4, 5}. A note for
  `WELL-002`; it does not replace the meter.

### E-12 — OOBE (M)
- pbpctrl `cli.rs:128–138` (setting 3 "You normally do not want to change this flag"), `:157–164` (setting 6 "used to intercept and block touch gestures during
  the setup wizard"). APK: `qje` field 1 validates against `qhp` (`qgx.java:52–53`), whose 17 constants (`qhp.java:8–24`, `ACTION_TYPE_UNKNOWN(0)` …
  `LEFT_ON_HEAD(10)`, `LEFT_OFF_HEAD(11)`, `RIGHT_ON_HEAD(12)`, `RIGHT_OFF_HEAD(13)` … `HOLD_CANCEL(16)`) equal pbpctrl's `OobeAction` (`proto:196–213`) name for
  name. The APK names 3 and 6 itself ("Set device(%s) oobe state …", "Enable/Disable OOBE mode", register 2026-10-06). A per-bud on/off-head stream would be
  a new subscription (never on any wire): EXPERIMENT.

### E-13 — Settings 1 and 5 (M)
- Census (`python3 scripts/pwrpc_decode.py` over the 96 logs, each `ReadSetting` RESPONSE paired with the request before it; awk quoted in the session log):
  setting 1 **136 of 136 `UNKNOWN`**, setting 5 **136 of 136 `4:{5:0}`**; without `CAP-042`'s `.log.last` (leftover `CAP-041`) 133 / 133. `ai-sessions/0073`'s
  "138" counted a different log set; the duplicate does not change either conclusion. pbpctrl `cli.rs:109–117` (1 = automatic OTA "when the device is not in
  use"), `:147–155` (5 makes the app send diagnostics to Google). `UNKNOWN` means the read is unsupported, nothing more.

### E-14 — Hold actions 1–4 (M)
- pbpctrl `proto:158–166` `RegularActionTarget` 1 CHECK_NOTIFICATIONS, 2 PREVIOUS_TRACK_REPEAT, 3 NEXT_TRACK, 4 PLAY_PAUSE_TRACK, 5 ANC_CONTROL, 6
  ASSISTANT_QUERY. The APK's own `fph` enum (`fph.java:6–14`) shares 1–3 but has 4 TRANSLATE … 8 ANC — an app-side enum, not the wire's; the wire uses 5 and 6
  only (ADR-045). Names 🟡 (pbpctrl); never written.

### E-15 — Peers and channels (M)
- pbpctrl `protocol/addr.rs:8–20` = APK `goq` constant for constant (`goq.java`: `HOST(1)` … `DEBUG_APP(9)`, `MAESTRO_A(10)`, `LEFT_TAHITI(11)`,
  `RIGHT_TAHITI(12)`, `MAESTRO_B(13)`); channels 18–27 `addr.rs:87–96` = `fux.java:90–103`. `protocol/utils.rs:20–26` probes 18, 19, 21, 23, 24, 26.
  Announcement census (`pwrpc_decode.py`, `call_id 4294967295`): 459 in 87 logs (duplicates included) on channels 19 (218), 21 (238), 24 (1), 26 (2) — **never
  18 or 23**. OpenControl handles an unknown channel already (`Maestro.kt:116–127`, `forChannel` → null, nothing sent; ADR-034 item 3). DebugApp 9 ↔ SDP "DEBUG
  APP" stays a speculative note.

### E-16 — Re-home timings (M)
- pbpctrl `examples/maestro_listen.rs:88–94` ("On a switch, the connection is reset. Wait a bit and then try to reconnect", 500 ms); MagicPodsCore
  `Device.cpp:127–135` (a restart after `sleep_for(seconds(1))`), `Device.h:31` `MAX_CLIENT_RECONNECT_ATTEMPTS = 12`, commit 97e36ab ("a link the buds drop on a
  primary role handover is restarted … with a bounded number of retries"). The "first connect refused, retry 1 s later succeeded" sentence is in neither the
  code nor the PR text at the pin — not checkable here. External support for L-1 (🟢 in this project already); no basis to call any delay optimal.

### E-17 — Explicit `GetSoftwareInfo` (M)
- 0 `GetSoftwareInfo` REQUESTs in the 96 logs; MAESTRO opens (`UA` after `SABM`, DLCI 2/3) vs an announcement within 5 s: 523 opens, 459 announced, 64 not —
  63 lived ≤ 1.0 s (the refused re-opens around docking, `CAP-025`, `CAP-040`, `CAP-011`), **one** stayed open without an announcement: `CAP-050` 17702
  (21:12:13.213, handle `0x000c`), where the official app's Dosimeter requests (17712) got no answer either. At most a low-priority idea.

### E-18 — Fast Pair details (M)
- Already built: `MessageStreamReply.kt:40–45` names reasons 0x00 "not supported", 0x01 "device busy", 0x02, 0x03, 0x04; `BudsError.CommandRejected(reason,
  name)` (`BudsRepositoryImpl.kt:1116–1118`). No `TODO.md` line needed. Remaining battery time `0x04`: never on the wire (`TODO.md` §4). SASS stays out (ADR-008).

### E-19 — `HardwareInfo` fields (M)
- `CAP-036` 1423: `1:6 2:17 5:9 6:7 7:{…} 8:{…}`. The register already names them from the APK (`gck.java:288`: 1 device type, 2 SKU, 5 bud hardware
  version, 6 case hardware version); pbpctrl calls them `unknown`. NOTE.

### E-20 — Class of Device (M)
- `tshark -r CAP-016-btsnoop_hci.log -Y 'frame.number==1213' -V | grep 'Class of Device'` → `0x244404 (Audio/Video:Wearable Headset Device - services:
  Rendering Audio)` in the Buds' Connection Request (BD_ADDR `…:6e:07`); the same in `CAP-064` 903. = pbpctrl `bt.rs` (ba253a7, `PIXEL_BUDS2_CLASS = 0x244404`)
  and MagicPodsCore `PixelBudsHelper.h:33–34`. Gen-1 `0x240404` is external only.

### E-21 — Confirmations (M)
- Cycle: pbpctrl's `cmd_anc_cycle` (`main.rs:454–459`) is Active → Off → Aware → Adaptive; the Buds' own hold order on the wire is Off → Adaptive →
  Transparency → NC (`CAP-021` 1743…5166: 40, 80, 08, 20, 40 …; `CAP-064` with Adaptive unticked: 80, 08, 20) — pbpctrl's is its own app-side order.
  Two-mode minimum: `main.rs:141–150` = ADR-046. EQ ±6.0: `cli.rs:220–236`, `settings.rs:368` = ADR-016. Case battery only with a bud in the case
  (`README.md:52`): an external reading of why entry 6.1 is sometimes absent (`PROTOCOL.md` §4.3 Option F) — 🟡.

### E-22 — Provenance (M)
- Names that only Google's app holds reappear in pbpctrl letter for letter: `goq`'s 13 peers incl. `DEBUG_APP`; `qhp`'s 17 gesture types; `qhs`'s
  `ANC_STATE_*`; "handleAllegroSetting" (`fxb.java:43`) and `fsf.java`'s `ALLEGRO` device types 4/5 ↔ `AllegroSettingType`; pw_rpc ids are 65599 hashes, so
  the method names must come from a string source. pbpctrl's battery enum (`proto:61–65`, `BatteryState` UNKNOWN/NOT_CHARGING/CHARGING) differs from the APK's `qhw`
  (`NOT_CHARGING(1) WIRED(2) GENERIC_QI(3)`) — its own guess there. Its own reverse-engineering hints: `pwrpc/utils.rs:11–14` (`EncodedMessage` "for
  reverse-engineering … protoscope"), "returns non-zero status" comments. ⇒ pbpctrl's names weigh as "APK name", not independent evidence.
- `AGENTS.md` §5's sentence that pbpctrl "depends on BlueZ, D-Bus, AVRCP, and UPower for battery state": pbpctrl's `README.md:39–52` uses UPower only for
  BlueZ's experimental AVRCP battery (one combined meter) and reads the detailed battery through Maestro. A wording proposal (project law).

### E-23 — The second model's overclaims (M)
- Confirmed as corrections: E-03 "solves" (a 🟡); E-10 "proves" (fits, proves nothing); E-13 "proves" (`UNKNOWN` = unsupported read); E-16 "optimal" (no basis);
  E-09 as fact (🟡); E-02 `CAP-045` — usable after all: its procedure differed from Group AJ's plan, but its 10 unprovoked Notifies are 8 mirrored + 1 at a
  `DISC` + 1 duplicate stream (2178); E-05 "both buds ACK" (one ACK); E-17 `CAP-061` (a parser bug); "no contention on DLCI 0x02" — true for other clients,
  but the Buds close DLCI 0x02 themselves (12 of the 77 Buds-side changes above fell exactly on such a close), so a 13-based path still needs ADR-044's re-open
  and a `ReadSetting 4:13` after it.

## Verdict table

| ID | Claim (short) | Validity | Usefulness | Verdict | Evidence | M/S |
|---|---|---|---|---|---|---|
| E-02 | the settings stream reports Buds-side mode changes | valid (official app's captures) | high — the noise-control question | PROPOSE (§4.1/§6 FACT text, draft ADR) + EXPERIMENT | Phase A E-02 | M |
| E-01 | ANC via `WriteSetting`/`ReadSetting` 13 | read: valid on the wire; write: not checkable here (never on any wire) | high | PROPOSE (read: part of the E-02 ADR) + EXPERIMENT (write) | Phase A E-01 | M |
| E-03 | 8/9/10 = accessibility / one-bud / two-bud | names: valid as pbpctrl's; behaviour: 🟡 | medium (explains `CAP-067` §2) | APPLY (register names, 🟡 note) + EXPERIMENT (read 8/9/10) | Phase A E-03 | M |
| E-04 | ring status + ACK | partly valid | high (known issue) | PROPOSE (an ACK is a new send) + EXPERIMENT | E-04 | M |
| E-05 | ring both `0x03`; case has no receiver | valid (spec re-fetched) | medium | EXPERIMENT (filmed, buds out of the ears) | E-05 | M |
| E-06 | §2.2's pbpctrl attribution is wrong | valid | medium (accuracy) | PROPOSE (dated correction note) | E-06 | M |
| E-07 | same bytes, opposite sign | valid (201/201) | low–medium | APPLY (§4.5.7 note) | E-07 | M |
| E-08 | firmware entry order | valid | low | NOTE | E-08 | M |
| E-09 | 18 readable here, status 2 on gen 1 | valid as 🟡 | low | NOTE | E-09 | M |
| E-10 | wall clock, never set by pbpctrl | partly valid (fits the 🟡) | low | APPLY (desk note) + PROPOSE (ADR-060 draft text) | E-10 | M |
| E-11 | dosimeter in dB | valid as 🟡 | medium (for `WELL-002`) | APPLY (note) | E-11 | M |
| E-12 | OOBE 3/6, on/off-head events | valid | medium | APPLY (names) + PROPOSE ("never write 3 or 6") + EXPERIMENT | E-12 | M |
| E-13 | setting 1 = auto-OTA, 5 = diagnostics | valid (names 🟡; reads unsupported/constant) | low | APPLY (register names) | E-13 | M |
| E-14 | hold actions 1–4 | valid as names | low | APPLY (register names, 🟡) | E-14 | M |
| E-15 | peer/channel table | valid (= `goq`, `fux`) | low | NOTE | E-15 | M |
| E-16 | re-home timings | partly valid (the 1-s-refusal sentence not found) | low | NOTE | E-16 | M |
| E-17 | explicit `GetSoftwareInfo` | valid (1 long open without announcement) | low | NOTE (+ optional `TODO.md` idea) | E-17 | M |
| E-18 | NAK reasons, battery bytes | valid; already built | none | NOTE | E-18 | M |
| E-19 | `HardwareInfo` 1/2/5/6 | valid; already named | low | NOTE | E-19 | M |
| E-20 | Class of Device `0x244404` | valid (wire) | low | PROPOSE (`PROTOCOL.md` §0.1 line) + `TODO.md` §5 idea | E-20 | M |
| E-21 | confirmations | valid (cycle order differs — pbpctrl's own) | low | NOTE | E-21 | M |
| E-22 | provenance | valid | medium (weighting) | APPLY (note; place = maintainer) + PROPOSE (`AGENTS.md` §5 wording) | E-22 | M |
| E-23 | overclaims | valid as corrections (E-02 `CAP-045` usable) | — | NOTE | E-23 | M |

No sub-reviews were used; every row is the main session's (M).

## Checkpoint (Phase C) — the maintainer's answers, verbatim (chat 2026-10-14)

Round 1:
- "FACT §4.1" → *"Promote as shown (Recommended)"* (the text in the preview, applied as `PROTOCOL.md` §4.1 Update of 2026-10-14).
- "ADR-064" → *"Accept now, build next (Recommended)"* (the text in the preview, applied as `DECISIONS.md` ADR-064).
- "PROTOCOL" → *"E-06 §2.2 correction (Recommended), E-03 §6 🟡 one-bud/two-bud, E-07 §4.5.7 note, E-20 §0.1 Class of Device"*.
- "APPLY" → *"Desk entry + script (Recommended), CAP-051-FINDINGS §3 rewrite, RE register names, Notes E-10, E-11, TODO lines"*, with the added instruction:
  *"Probeer de bevindingen te valideren (middels reverse engineering) aan de hand van de google pixel buds APK. Probeer nog meer bevindingen te halen uit het
  github project qzed/pbpctrl."* — done before applying: the APK chain (`fxg` → `SubscribeToSettingsChanges` → `fxb` case 13 → `gea.I`, one writer; `fwt` →
  `ReadSetting 4:13` on runtime-info field 7), and from pbpctrl its `RuntimeInfo` layout (6.1 case, 6.2 left, 6.3 right; 7.1 right in case, 7.2 left in case —
  consistent with `PROTOCOL.md` §4.3 Option F and the code trace) and `docs/Notes.md` ("allows for subscribing to events, such as settings changes"); both
  recorded in the documents above (the RuntimeInfo layout only here — 🟡 external support for the 🔴 "7.x = in the case").

Round 2:
- "APK check" → *"RE Update + ADR-025 pointer (Recommended)"* (preview text applied).
- "Skeletons" → *"None now; with the FEATURE (Recommended)"*.
- "Provenance" → *"DESKRESEARCH_FINDINGS (Recommended)"*.
- "Law/drafts" → *"AGENTS.md §5 pbpctrl wording, ADR-060 draft: clock items, 'Never write 3 or 6' rule"* (not chosen: the `TODO.md` §5 gen-1 idea — not added).

## Applied (Phase D)

| File | Change | Approval |
|---|---|---|
| `PROTOCOL.md` | §4.1 Update (🟢 setting 13 = mode; the stream); §6 two pointers + 🟡 item (E-03); §2.2 correction (E-06); §4.5.7 note (E-07); §0.1 note (E-20); §8 row | "FACT §4.1", "PROTOCOL" |
| `DECISIONS.md`, `id_registry.csv` | ADR-064 (accepted) + its registry row; pointer Updates on ADR-025, ADR-034, ADR-036; ADR-060 draft item (vii) | "ADR-064", "APK check", "Law/drafts" |
| `REVERSE_ENGINEERING.md` | `qhr` register Update of 2026-10-14 (the APK chain; pbpctrl names labelled as naming; hold actions; `qje`/`qhp`; `goq`) | "APK check", "APPLY" |
| `DESKRESEARCH_FINDINGS.md` | entry 2026-10-14 (§1–§8); note at 2026-10-03 item 3 (E-10) | "APPLY", "Provenance" |
| `captures/CAP-051-*/CAP-051-FINDINGS.md` | §3 rewritten in place (the settings stream at each hold; "Notify alone" corrected) | "APPLY" |
| `AGENTS.md` | §5 dated note on pbpctrl's battery sources | "Law/drafts" |
| `ARCHITECTURE.md` | §5a rule: settings 3 and 6 are never written | "Law/drafts" |
| `TODO.md` | §1, §4 (lead (4) answered; E-01, E-03, E-04, E-05, E-12, E-17 lines), §5 (next: ADR-064 FEATURE), §6 (reading owed) | "APPLY" |
| `scripts/anc_setting13_crosscheck.py` | new (rule 4a) | "APPLY" |

`PYTHONDONTWRITEBYTECODE=1 python3 scripts/ensure_footers.py` → all footers up to date; `python3 scripts/lint_docs.py` → exit 0 (after one fix: the new §8 row
lacked its third cell). Nothing under `android/` changed; nothing from the external repositories was copied in (quotations of a few words with file and line only).

## Files read

- **In full:** `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `DECISIONS.md` (ADR-001 … ADR-063 with every Update), `TODO.md`, `AI_SESSION_LOG_PROCEDURE.md`, the
  prompt; `scripts/pwrpc_decode.py`, `scripts/message_stream_tally.py`.
- **In part:** `PROTOCOL.md` (§0–§2.2a in full; §4.1 in full; §4.3 Option F, §4.5.7, the §6 items on setting 13, the burst, `CAP-038`/`CAP-051`, `CAP-056`,
  `CAP-069`; §8's last rows); `ARCHITECTURE.md` (§5a's head and table only); `REVERSE_ENGINEERING.md` (the `qhr` entry with its register and Updates, the
  `qjn`/`qjt` head, the `fux` service table rows, the `qiv` line); `DESKRESEARCH_FINDINGS.md` (the entries of 2026-10-03 and 2026-10-06 at the cited items);
  `ai-sessions/INDEX.md` (last rows); FINDINGS/EVENT-NOTES of `CAP-021`, `CAP-027`, `CAP-038`, `CAP-051`, `CAP-056`, `CAP-066`, `CAP-067` at the cited sections;
  `id_registry.csv` (the CAP-059…076 and ADR-060…063 rows).
- External (pinned clones): the pbpctrl, MagicPodsCore and omarchy files cited above, at the cited lines.

## Deferred documentation

- `TODO.md` §6: "Reading owed by `ai-sessions/0090` (its RESULT "Files read"): `PROTOCOL.md`, `ARCHITECTURE.md` and `REVERSE_ENGINEERING.md` were read only in
  the sections named by its prompt (and the sections it changed), not in full; the FINDINGS of `CAP-038`, `CAP-045`, `CAP-064`, `CAP-069`, `CAP-075` only at the
  cited frames — carry into the next reading pass."
- `TODO.md` §4/§5 carry every EXPERIMENT of the verdict table (ADR-064's run with the FEATURE; `WriteSetting 4:{13:n}`; read 8/9/10; ring ACK; ring "both";
  OOBE actions; the `GetSoftwareInfo` idea) with the wording shown there.

## Commits

Not committed — the maintainer has not yet been asked (Phase E: "ask whether to commit and push").

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0090_CROSSCHECK_RESULT_2026_10_14.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0090_CROSSCHECK_RESULT_2026_10_14
