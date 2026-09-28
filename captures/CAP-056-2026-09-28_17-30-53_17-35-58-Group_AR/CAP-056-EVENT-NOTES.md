# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AR, `HOLD-005` ANC-mode checklist per bud, the `qht` bit order, and in-ear detection off (`CAP-056`)

**Status:** 🟢 **Video (incl. audio) pass + full HCI log analysis complete** (`ai-sessions/0055_CAPTURE_PROMPT_2026_09_28.md`). See
`CAP-056-FINDINGS.md` for the wire-level analysis, hex + commands and conclusions. The planned procedure (the skeleton committed before the
capture) is kept unchanged as **Appendix A**; the timeline below records what was **actually** done, taken from the film and the log — not from
the skeleton. The maintainer changed the order and the shape of several steps (the ears had to be filmed); each change is marked in the timeline
and in the step-mapping table.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AR + the skeleton's Additions (A) and (B)):** (1) `HOLD-005` — which boolean of `qhr` field 12
(`qht`) each checkbox of the press-and-hold ANC-mode list writes, and whether "Customize left" and "Customize right" have separate lists; (2) the
`qht` bit order of `ai-sessions/0051` F-6 (code: 3 = Transparency, 4 = Adaptive; on-screen reading: the reverse); (3) W-12b — the "In-ear
detection" switch on film against `qhr` field 2, and what the Buds do with it off (pause, `DISC` of DLCI 0x02, Settable).

## Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-056` |
| Group(s) | AR (+ Additions (A) `0051` F-6 and (B) W-12b W1–W5) |
| Date | 2026-09-28 |
| Firmware version | `release_5.203` — on the wire in every `GetSoftwareInfo` announcement (e.g. frame 1447, `…72656c656173655f352e323033` = `release_5.203`, ×3 components); not shown on screen |
| Test device | Pixel 7a (status bar "AH Mobiel", clock 17:30–17:35; the phone model is not shown on screen — ⚪ carried over from the skeleton and the Capture Index), official Pixel Buds app (screens "Device details", "Controls and gestures", "Customize left/right", "Sound", "More settings"). **App version not on film** ("About" was not opened) — not recorded. Android version not on film. |
| Pixel 9a | Not on film; its Bluetooth state cannot be confirmed. The phone's log shows only one classic link to the Buds (handle `0x0005`); a second host would be on the Buds' side of the link and is not visible in the 7a's log (the Buds' SASS status frames `07 34` are encrypted). 🔴 not established |
| Other devices | An LE link (handle `0x0003`, random address `53:6e:dc:c4:60:59`, 17:30:58–17:31:16) to a device exposing a **Heart Rate** service — not the Buds (same unrelated-device signature as `CAP-018`/`CAP-028`/`CAP-032`); excluded |
| Video file | `CAP-056-recording.mp4` — 306.04 s (`ffprobe`), H.264 1280×720 (stored portrait 720×1280), variable ≈ 29.8 fps, AAC stereo 44.1 kHz 305.1 s, creation_time 2026-09-28T15:35:59Z. Burned-in overlay clock: first frame **17:30:53**, last frame **17:35:58** |
| Log file | `CAP-056-btsnoop_hci.log` — 5,376 packets, 17:30:42.812–17:39:02.441 (499.6 s), raw path, 0/5,376 `cap_len≠len` (untruncated), encapsulation Bluetooth H4 with linux header |
| Film ↔ log coverage | The film covers log time 17:30:53–17:35:58. Before it: 17:30:42–17:30:57 Bluetooth off (no ACL frame). After it (17:35:58–17:39:02): only the Buds' unnamed-service stream `0x73d5d805` pushes and phone HFP `+CIEV: 4,…` — no user action, no wear event, no setting change |
| Clock offset | Measured twice (FINDINGS §0): start — the "Noise cancellation" checkbox on "Customize left" starts to change 0.21–0.24 s after the phone's write 1689 (17:31:30.889); end — the "In-ear detection" switch starts to move 0.22 s after write 4048 (17:35:33.419). The overlay's seconds tick at video t = 37.706 s (17:31:31) and 280.612 s (17:35:34) — the same phase within 2 frames. ⇒ **overlay clock = phone clock within ≈ 0.3 s, no drift**; the UI renders ≈ 0.2 s after the write |
| Buds MAC (partial, `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` (ADR-010) |
| Buds state at start | Case open and **empty** on film for the whole recording (crop of t = 1 s and 175 s); both buds not in the case. On-screen battery at 17:31:14 and 17:33:12: Left 100 %, Case 45 %, Right 100 % |

## Capture-integrity pre-flight

```
capinfos CAP-056-btsnoop_hci.log    → 5,376 packets, 499.628954 s, 17:30:42.812406 – 17:39:02.441360, "Packet size limit: file hdr: (not set)"
tshark -r CAP-056-btsnoop_hci.log -T fields -e frame.cap_len -e frame.len | awk '$1!=$2{c++} END{print c+0}'   → 0
sha256sum: log cfbdc45bbc0986bab89a25bd38156f62773390492bf1f8d6ea94034277647d1e
           mp4 135bc165cd1f72ace20f1321e260b6fd14c6e2ed69839963fd27a3e123b73cfa
git check-attr filter → lfs (both); file modes 644 (the mp4 was 0750 on arrival, set to 644 in this session with the maintainer's approval)
```

## Video and audio review method (incl. privacy)

- **Video, 100 %:** every second (1 fps, 306 frames, contact sheets with the overlay time), plus frame-exact passes (all ≈ 30 frames/s) around the
  two clock anchors, full-resolution frames of every screen (labels read from 720×1280 frames: "Customize left/right", the four rows
  "Noise cancellation / Off / Adaptive / Transparency", the footnote "Press and hold to cycle between the selected active noise control modes",
  "In-ear detection — Earbuds automatically play audio when in and pause audio when out"), and crops of the case and the ear frames.
- **Audio, 100 %:** `ffmpeg -i CAP-056-recording.mp4 -vn -ac 1 -ar 16000 audio.wav`; per-second RMS/peak over all 305 s and a full-length
  spectrogram (plus a 0–4 kHz zoom of 180–255 s). The room level is −77…−81 dBFS; the only events are **broadband transients** (energy over the whole
  band, no harmonic stack) at t ≈ 135–136 s, 159–166 s, 184–192 s (peak −10 dBFS at 188–189 s), 203–213 s, 225–233 s, 244–249 s and 294–300 s — every
  one while a hand, the head or hair is next to the camera (handling / rubbing noise). **No speech, no music from the phone's speaker, no Buds tone is
  identifiable.** The audio therefore times nothing beyond the video and says nothing about what played inside the buds.
- **Privacy (maintainer decision in chat 2026-09-28: "Leave unchanged").** No face on film (hair, one ear at a time, glasses). The phone screen shows
  personal data in two windows: t ≈ 137.5–144.5 s (17:33:11–17:33:17, notification shade — Wi-Fi SSID, the maintainer's Google e-mail address, a
  Wallet card's last four digits, a city weather forecast, a bank notification, a Photos title, the podcast playing) and t ≈ 0–19 s (quick settings
  behind the Bluetooth dialog — SSID, Wallet digits). Reported and asked; the file is committed unchanged (ADR-010/ADR-037).

## Event Timeline

Times: phone clock = overlay clock (±0.3 s, see Log Metadata). "Step" = skeleton step (Appendix A): **1–3** Group AR procedure; **A1–A4** Addition (A);
**W1–W5** Addition (B). **done** / **var.** (done differently) / **rep.** (repeated) / **extra** (not in the plan). OHD = the "In-ear detection" setting.
Frame numbers: `CAP-056-btsnoop_hci.log`. Checklist state written as `{NC Off Adaptive Transparency}` on screen, and as `{1 2 3 4}` on the wire.

| Time (phone) | Action / what the film shows | Actor | Step | Test-ID | Wire evidence |
|---|---|---|---|---|---|
| 17:30:53 | Film starts. Quick settings → Bluetooth dialog, "Use Bluetooth" **off**; case open and empty | — | — | — | log since 17:30:42, no ACL yet |
| 17:30:56–57 | Taps "Use Bluetooth" on; "Pixel Buds Pro 2 van Ted — Connecting…" (17:30:59–17:31:09) | User | extra | `PAIR-003` | `Create Connection` 247 (17:30:57.686) → Page Timeout 793; 2nd `Create Connection` 794 → `Connection Complete` **handle 0x0005** 835 (17:31:05.320). A2DP (AAC) set up 924–933; DLCI 0x00/0x04/0x0a/0x08 opened by the phone 979–1085; HFP DLCI 0x09 opened by the Buds 1259 |
| 17:31:09–10 | (DLCI 0x04 content) | Buds/phone | — | `ANC-*` (read) | `08 11 00 00` Get 1029 → `08 13 00 04 01 e8 e8 08` 1047 (Settable `e8`, ANC); SASS capability `07 11 … 01 02 b0 00` 1053 (on-head detection **off**) |
| 17:31:11–16 | Home screen, notification "Pixel Buds Pro 2 van Ted connected — Left 100% Case 45% Right 100%"; recents flash **More settings with "In-ear detection" OFF** (17:31:13, t = 20.3 s) | User | extra | — | — |
| 17:31:15–19 | Official app opens DLCI 0x02 and reads every setting | App | — | — | `SABM` 1432 (17:31:15.294); `GetSoftwareInfo` 1447 (ch 19); `ReadSetting` sweep: **`4:{2:0}` 1502** (OHD off), **`4:{12:{1:1 2:1 3:1 4:1}}` 1531**, `4:{7:{1:{4:{1:5}} 2:{4:{1:5}}}}` 1524, `4:{13:2}` 1534 … (FINDINGS §6) |
| 17:31:17–22 | Device details (L 100 %, C 45 %, R 100 %; ANC "Noise cancellation") → Controls and gestures (17:31:23) | User | — | — | — |
| **17:31:26** | **"Customize left"**, tab "Active noise control": the four-row checklist **visible, all four ticked** — anti-repeat safeguard met before the first toggle | User | **1 done** | `HOLD-005` | matches read 1531 |
| 17:31:30 | Left: untick **Noise cancellation** → `{□ ☑ ☑ ☑}` | User | 2 var. | `HOLD-005` | `WriteSetting 4:{12:{1:0 2:1 3:1 4:1}}` 1689 (17:31:30.889) → push 1696 → `RESPONSE` OK 1697 |
| 17:31:36 | Left: re-tick Noise cancellation | User | 2 var. | `HOLD-005` | `4:{12:{1:1 2:1 3:1 4:1}}` 1725 → 1727 → OK 1729 |
| 17:31:42 | Left: untick **Off** | User | 2 var. | `HOLD-005` | `4:{12:{1:1 2:0 3:1 4:1}}` 1786 → 1788 → OK 1789 |
| 17:31:47–48 | Left: re-tick Off | User | 2 var. | `HOLD-005` | `4:{12:{… all 1}}` 1802 → 1804 → OK 1805 |
| **17:31:53–54** | Left: untick **only Adaptive** → `{☑ ☑ □ ☑}` | User | **A1 done** (wait 5.6 s, plan ≥ 10 s) | `HOLD-005` | **`4:{12:{1:1 2:1 3:1 4:0}}` 1815** (17:31:54.306) → push 1817 → OK 1818 — **boolean 4 clears** |
| 17:31:59–32:00 | Left: re-tick Adaptive | User | A3 done | `HOLD-005` | `4:{12:{… all 1}}` 1830 → 1832 → OK 1833 |
| — | *Open "Customize right" while Adaptive is unticked on the left* | — | **A2 skipped** (Adaptive was re-ticked first) | `HOLD-005` | — |
| **17:32:05–06** | Left: untick **only Transparency** → `{☑ ☑ ☑ □}` | User | **A3 done** | `HOLD-005` | **`4:{12:{1:1 2:1 3:0 4:1}}` 1843** (17:32:05.982) → push 1845 → OK 1846 — **boolean 3 clears** |
| 17:32:11–12 | Left: re-tick Transparency | User | A3 done | `HOLD-005` | `4:{12:{… all 1}}` 1863 → 1865 → OK 1866 |
| **17:32:17–20** | Back → Controls and gestures → **"Customize right"**: checklist visible, all four ticked | User | **3 done** (safeguard met) | `HOLD-005` | no frame on screen open |
| 17:32:21–22 | Right: untick Noise cancellation | User | 3 var. | `HOLD-005` | `4:{12:{1:0 2:1 3:1 4:1}}` 1891 → 1895 → OK 1896 (byte-identical to the Left write 1689) |
| 17:32:28–29 | Right: re-tick NC | User | 3 var. | `HOLD-005` | `… all 1` 1908 → 1910 → OK 1913 |
| 17:32:34–35 | Right: untick Off | User | 3 var. | `HOLD-005` | `4:{12:{1:1 2:0 3:1 4:1}}` 1928 → 1930 → OK 1931 |
| 17:32:40–41 | Right: re-tick Off | User | 3 var. | `HOLD-005` | `… all 1` 1944 → 1946 → OK 1947 |
| 17:32:45–46 | Right: untick **Adaptive** | User | 3 var. (A repeated on the right) | `HOLD-005` | **`4:{12:{1:1 2:1 3:1 4:0}}` 1959** → 1963 → OK 1964 — boolean 4 |
| 17:32:51–52 | Right: re-tick Adaptive | User | 3 var. | `HOLD-005` | `… all 1` 1975 → 1977 → OK 1978 |
| 17:32:57–58 | Right: untick **Transparency** | User | 3 var. | `HOLD-005` | **`4:{12:{1:1 2:1 3:0 4:1}}` 1991** → 1994 → OK 1995 — boolean 3 |
| 17:33:03–04 | Right: re-tick Transparency | User | 3 var. | `HOLD-005` | `… all 1` 2006 → 2008 → OK 2009 |
| 17:33:11–17 | Notification shade; taps play on the podcast player (≈ 17:33:13) | User | W-prep ("music playing") | — | phone PlaybackStatusChanged **Playing** 2029 (17:33:13.881), AVDTP Start 2031 |
| 17:33:22–31 | Device details → Sound (17:33:25–28) → Device details → More settings | User | extra | — | none |
| **17:33:32** | More settings: **"In-ear detection" is OFF** (pre-state, as read in 1502) | — | W1 pre-state **not as planned** (plan: on) | `INEAR-001` | — |
| **17:33:34–35** | Taps **In-ear detection → ON**, switch and finger on film | User | **extra** (a W5-like step before W1) | `INEAR-001` | **`WriteSetting 4:{2:1}` 2173** (17:33:35.629) → push 2175 → OK 2179; Buds SASS `07 11 … 01 02 b8 00` 2176 (+18 ms, on-head detection **on**); DLCI 0x08 `04 05 … 08 06` 2177 |
| 17:33:41–54 | Head in from frame-left; hand at the ear 17:33:44–45 (**a bud out**) and again 17:33:50–53 (**back in**) — which ear is not decidable from the overhead camera | User | **W1 var.** (1st removal, OHD on) | `INEAR-004`, `INEAR-002/003` | out: DLCI 0x08 `04 05 08 04` 2253 (17:33:46.689) → `Notify 08 13 … e8 e8 40` 2257 → push `4:{13:4}` 2259 → **phone PlaybackStatusChanged Paused 2268** (17:33:46.818) → AVDTP Suspend 2294. **No** `DISC`. In: `04 05 08 06` 2318 (17:33:54.135) → Notify `… e8 08` 2322 → `4:{13:2}` 2324 → **Playing 2326** (17:33:54.248) |
| 17:34:05–17 | Head in from frame-right; finger at the ear 17:34:07–08 (**a bud out**), the bud visible in the fingers 17:34:14–16, back in ≈ 17:34:16–17 | User | **W1 rep.** (2nd removal, OHD on, the other side of the head) | `INEAR-004`, `INEAR-002/003` | out: `04 05 08 05` 2453 (17:34:09.187) → Notify `… e8 40` 2458 → **Buds `DISC` DLCI 0x02 2459 and 0x04 2462** (17:34:09.212/.223) → phone `DISC` 0x08/0x0a 2473/2474 → **Paused 2482** (17:34:09.353). Official app re-opens DLCI 0x02 at 2629 (+3.4 s). In: `04 05 08 06` 2760 (17:34:17.660) → Notify `… e8 08` 2764 → `4:{13:2}` 2765 → **Playing 2772** (17:34:17.800) |
| **17:34:24–26** | Taps **In-ear detection → OFF**, switch and finger on film | User | **W2 done** | `INEAR-001` | **`WriteSetting 4:{2:0}` 2849** (17:34:25.565, ch 21) → push 2851 → OK 2855; SASS `07 11 … b0 00` 2852 (on-head detection off); `04 05 08 01` 2854 |
| 17:34:28–39 | Head from frame-left; **bud in the fingers 17:34:30–31**, back in 17:34:37–39 | User | **W3 done** (OHD off) | `INEAR-004`, `INEAR-002/003` | out: **Buds `DISC` DLCI 0x02/0x04 2923/2924** (17:34:31.965), `04 05 08 01` 2920 (unchanged); **no** Notify, **no** field-13 push, **no** PlaybackStatusChanged, **no** Suspend. In: `04 05 08 01` 3187 (17:34:39.947), nothing else |
| 17:34:47–59 | Head from frame-right (glasses); hand at the ear ≈ 17:34:49 (out) and 17:34:56–57 (in) | User | **W3 rep.** (OHD off, other side) | `INEAR-004`, `INEAR-002/003` | out: **Buds `DISC` 3260/3263** (17:34:50.902), `04 05 08 01` 3259; in: `04 05 08 01` 3518 (17:34:58.410); **no** Notify, no field-13 push, no pause |
| **17:35:17–19** | Taps **In-ear detection → ON** | User | **W5 done** (earlier than planned: before W4) | `INEAR-001` | **`4:{2:1}` 3627** (17:35:18.806) → push/OK 3632; SASS `… b8 00` 3630; `04 05 08 06` 3631 |
| 17:35:21–28 | **Both buds out**, held in the hands in front of the phone (on film 17:35:22–28) — not on the table; the noise-control screen was **not** opened | User | **W4 var.** (with OHD **on**) | `INEAR-004` | first bud: `04 05 08 04` 3667 (17:35:21.985) → Notify `… e8 40` 3670 → **Buds `DISC` 0x02/0x04 3672/3673**; second: `04 05 08 03` 3687 (17:35:22.074), `04 16 08 02` 3689 → **Paused 3705** (17:35:22.166). After the phone's re-open: `ReadSetting 4:{13:0}` 3870, Get → **`Notify 01 e8 00 20` 3940** (17:35:28.123, **Settable `00`**, mode Off) |
| 17:35:29–31 | Both back in (off film) | User | W4 var. | `INEAR-002/003` | `04 05 08 04` 3974 (17:35:31.289) → Notify `… e8 40` 3983 → `04 16 08 01` 3985 → **Playing 3991** (17:35:31.465) → `04 05 08 06` 3995 → Notify `… e8 08` 4001 → pushes `13:4`/`13:2` 4025/4028 |
| **17:35:32–34** | Taps **In-ear detection → OFF** (end clock anchor) | User | **W2 rep.** | `INEAR-001` | **`4:{2:0}` 4048** (17:35:33.419, byte-identical to `CAP-024` 1850) → 4051 → OK 4056; SASS `… b0 00` 4052; `04 05 08 01` 4054 |
| 17:35:37–44 | **Both buds out**, held in the hands (on film 17:35:38–44) | User | **W4 var. rep.** (with OHD **off**) | `INEAR-004` | `04 05 08 01` 4099/4102, **`04 16 08 02` 4103** (17:35:37.550); **no** `DISC`, **no** Notify, **no** PlaybackStatusChanged |
| ≈ 17:35:45–48 | Both back in (off film) | User | W4 var. rep. | `INEAR-002/003` | `04 05 08 01` 4166, **`04 16 08 01` 4167** (17:35:48.465), **Buds `DISC` 0x02/0x04 4168/4170** (17:35:48.467) |
| **17:35:49–52** | Taps **In-ear detection → ON**; the switch shows on at ≈ 17:35:52 | User | **W5 rep.** | `INEAR-001` | DLCI 0x02 was closed (4168); official app re-opens it 4304 (17:35:51.800), then **`4:{2:1}` 4344** (17:35:52.190) → phone `CLIENT_ERROR CANCELLED` 4360 → `4:{2:1}` again 4361 → push + OK 4362, push + OK 4367/4369 → phone `CLIENT_ERROR FAILED_PRECONDITION` 4370; SASS `… b8 00` 4425; `04 05 08 06` 4357 |
| 17:35:58 | Film ends on More settings, In-ear detection ON | — | — | — | log continues to 17:39:02 (nothing user-driven) |

## Step mapping (skeleton → what happened)

| Step | Plan | Result |
|---|---|---|
| 1 | Checklist screen on film before any toggle | **done** — "Customize left" 17:31:26, first toggle 17:31:30 |
| 2 | Left: toggle each of the four items, one at a time, ≥ 10 s apart | **done differently** — each item unticked **and re-ticked** (8 taps, 17:31:30–32:12), 5–6 s apart |
| 3 | Right: same, screen on film again | **done differently** — "Customize right" 17:32:19, same 8-tap pattern 17:32:21–33:04 |
| A1 | Left: untick only Adaptive, wait ≥ 10 s | **done** (5.6 s wait) — frame 1815 |
| A2 | Open "Customize right" while Adaptive is unticked on the left | **skipped** — Adaptive re-ticked (1830) before the right screen was opened |
| A3 | Re-tick Adaptive; untick only Transparency; re-tick | **done** — 1830, 1843, 1863 (and repeated on the right: 1959/1975/1991/2006) |
| A4 | One `WriteSetting 4:{12:{…}}` per tap | **done** — 16 taps, 16 writes, 16 OK (FINDINGS §1) |
| W1 | OHD on (pre-state), Right bud out on film 10 s, back | **done differently** — the pre-state was **off** (read 1502, film 17:33:32); the maintainer switched it on first (2173); then **two** removals (17:33:44–54, 17:34:07–17), one on each side of the head; which ear is Right is not identifiable on film |
| W2 | OHD off on film | **done** 17:34:24 (2849); **repeated** 17:35:32 (4048) |
| W3 | Repeat W1 with OHD off | **done ×2** (17:34:30–39, 17:34:49–57) |
| W4 | Both buds out onto the table 10 s, open the noise-control screen, both back | **done differently** — both buds held in the hands (not on the table), ≈ 6 s, noise-control screen not opened; **done twice**, once with OHD on (17:35:21–31) and once with OHD off (17:35:37–48) |
| W5 | OHD on again on film | **done** 17:35:17 (3627); **repeated** 17:35:49 (4344/4361) |

## Analysis checklist

- [x] Each toggle produces a `field5{field4{field12{1..4}}}` write on DLCI 0x02 — 16/16 (FINDINGS §1).
- [x] Left vs Right distinguishable on the wire? **No** — the Right writes are byte-identical to the Left writes for the same checkbox (1689 = 1891,
      1815 = 1959, 1843 = 1991); only the film tells them apart (FINDINGS §2).
- [x] (A) which bit clears: Adaptive → boolean **4**, Transparency → boolean **3** (FINDINGS §1).
- [ ] (A) whether "Customize right" shows a change made on "Customize left" — **not tested** (A2 skipped) (FINDINGS §2).
- [x] (B) W2/W5: every field-2 write against the filmed switch labelled "In-ear detection" — 5 writes, 5 filmed taps (FINDINGS §3).
- [x] (B) W1 vs W3: pause, Buds `DISC`, Settable, other pushes with OHD on vs off (FINDINGS §4).

## Open questions — carried into `CAP-056-FINDINGS.md`

- One list or two: the film test (A2) was skipped; the wire and the code say one message without a side (FINDINGS §2).
- Which physical ear each removal was (overhead camera) — FINDINGS §4.
- The Pixel 9a's Bluetooth state and the official app's version — not on film.
- Settable with OHD **off** and no bud worn — no `Notify` was taken in that state (FINDINGS §4).

## Next steps after filling this in

- [x] Traceability check (`AGENTS.md` §13.7): every skeleton step is in the timeline or marked skipped (A2); `HOLD-005`, `INEAR-001`, `INEAR-002`,
      `INEAR-003`, `INEAR-004` and `PAIR-003` are referenced.
- [x] `CAP-056-FINDINGS.md` written (rule 4a: commands + raw hex).
- [x] Folder renamed to `CAP-056-2026-09-28_17-30-53_17-35-58-Group_AR` (overlay clock, maintainer-approved in chat 2026-09-28).
- [ ] Capture Index / `id_registry.csv` / `TESTPLAN` rows — after the checkpoint (`ai-sessions/0055` Phase D).

---

## Appendix A — the procedure as planned (the skeleton as committed before the capture, commit `a77a15a`, file `CAP-056-EVENT-NOTES.md` in the placeholder folder `captures/CAP-056-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AR/`; text unchanged, heading levels lowered by two)

### Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AR (new), `HOLD-005` ANC-rotation-checklist Left/Right split, genuine re-run (`CAP-056`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-056-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AR` to the actual session
date/start-time/end-time, e.g. `CAP-056-2026-09-15_08-30-00_08-40-00-Group_AR`.

**⚠️ Read this before starting, per `CAP-045`'s own gap:** `CAP-045` (Group AJ) was intended to run
this exact procedure but never actually opened the rotation-checklist screen — the maintainer
performed physical press-and-hold ANC cycling instead, which produces *no* rotation-checklist write
at all (`CAP-045-FINDINGS.md` §2/§4). **This session only counts as valid if the checklist screen
itself is clearly visible on camera before any toggle is made** — see step 1 below.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AR, added 2026-09-13,
`ai-sessions/0017_MAINTENANCE_RESULT_2026_09_13.md` Phase 4):** `PROTOCOL.md` §4.5.3's ANC-mode
rotation checklist (`qhr` field 12, confirmed field-number identity as `qht`) has 16 wire-observed
boolean flags (`HOLD-005`, `CAP-021`) but no Left/Right-distinguishing field for this specific
write — unlike `HOLD-001`–`HOLD-004`. `CAP-045` did not answer this; this Group is a fresh attempt,
**not** a `CAP-045` v2 (Group AJ's own procedure remains valid and unchanged).

#### Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-056`                     |
|      Group(s)    |                     AR (new)                       |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    |       TBD (Pixel 7a, official Pixel Buds Companion App) |
| Video file       |    TBD — **must show the ANC-mode rotation checklist screen itself** (a 4-item checkbox list: Noise cancellation / Off / Adaptive / Transparency) on camera for at least one full toggle, per the anti-repeat safeguard above |
| Log file         |             TBD — `CAP-056-btsnoop_hci.log` |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

#### Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AR)

1. **Anti-repeat safeguard (mandatory):** before toggling anything, video-confirm the actual
   on-screen destination is "Device details → Controls and gestures → [the ANC-mode rotation
   checklist screen]" — the checkbox list must be clearly visible on-camera for **at least one full
   toggle** before this session counts as having run the procedure at all. If the checklist screen is
   not visible on camera at this point, **stop and restart** — do not proceed and hope the wire data
   disambiguates it after the fact, as happened in `CAP-045`.
2. With the checklist screen confirmed open for the **Left** earbud specifically, toggle each of the
   4 checklist items **one at a time**, with a clear pause (≥10s) and a distinct video-visible action
   between each toggle.
3. Navigate to the **Right** earbud's own rotation checklist (video-confirm the screen again, per
   step 1's safeguard) and repeat step 2.

#### Additions (planned after `CAP-063`)

**(A) The `qht` bit order and one-list-or-two (`ai-sessions/0051` F-6):** the four steps of `CAP-063-EVENT-NOTES.md` Appendix A.8 — untick **only
Adaptive** on "Customize left", open "Customize right" on film (still ticked there?), re-tick, then untick **only Transparency** and re-tick; one
`WriteSetting 4:{12:{…}}` per tap is expected. They settle `PROTOCOL.md` §4.5.3's 2026-09-26 🔴 (code: 3 = Transparency, 4 = Adaptive; on-screen reading: the
reverse).

**(B) In-ear detection OFF (W-12b; added by `ai-sessions/0054` on the maintainer's request in chat 2026-09-28: "make W-12b in-ear detection off part of
`CAP-056`").** Why: `ai-sessions/0053` §6 drafts an "in-ear detection write" ADR (field 2, not numbered, **not accepted**); before it two things need evidence —
(1) the label "In-ear detection" = `qhr` field 2 ("CATEGORY_OHD", 🟢 category only, `PROTOCOL.md` §4.5.5) **on film**, as D-1(a) did for field 22; (2) 🔴 what
the Buds do with it OFF: do they still close DLCI 0x02 on wear changes (the trigger of ADR-044's re-open), still report Settable `00`/`e8` (the ANC rule of
`ai-sessions/0054` I-1), still pause the media when a bud comes out? The official app on the **Pixel 7a** writes it (the app cannot); the Pixel 9a's Bluetooth
**off**. Keep your **ears visible on film** for W1/W3/W4, and have music playing.

| Step | Pre-state | Action | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|
| W1 | in-ear detection **on** (as read in every capture so far), both buds worn, music playing | Take the **Right** bud out **on film**, wait 10 s, put it back | baseline: AVRCP PAUSE/PLAY from the Buds; a Buds `DISC` of DLCI 0x02 (the official app's session) or not; a `Notify` Settable byte if the official app claims DLCI 0x04 | | |
| W2 | same | Device details → More settings → **In-ear detection: OFF** — the switch and the finger visible on film | `WriteSetting 4:{2:0}` → empty `RESPONSE` status OK (as `CAP-024` 1850/1912) | | |
| W3 | OFF, both worn | Repeat W1 (Right out on film, 10 s, back) | does the pause still happen? a `DISC` of DLCI 0x02? | | |
| W4 | OFF | Both buds out onto the table **on film**, wait 10 s; open the official app's noise-control screen; then both back in the ears | Settable `00` or `e8` in any `Notify` while on the table / worn | | |
| W5 | OFF | **In-ear detection: ON** again, on film | `WriteSetting 4:{2:1}` → `RESPONSE` OK | | |

#### Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Session start, raw-path HCI snoop logging confirmed | User | — | Conn. state: TBD |
| TBD | ANC-rotation-checklist screen opened for **Left** earbud, video-confirmed (anti-repeat safeguard) | User (App) | `HOLD-005` | TBD |
| TBD | Left: toggle item 1 (Noise cancellation), pause ≥10s | User (App) | `HOLD-005` | TBD |
| TBD | Left: toggle item 2 (Off), pause ≥10s | User (App) | `HOLD-005` | TBD |
| TBD | Left: toggle item 3 (Adaptive), pause ≥10s | User (App) | `HOLD-005` | TBD |
| TBD | Left: toggle item 4 (Transparency), pause ≥10s | User (App) | `HOLD-005` | TBD |
| TBD | ANC-rotation-checklist screen opened for **Right** earbud, video-confirmed | User (App) | `HOLD-005` | TBD |
| TBD | Right: toggle item 1 (Noise cancellation), pause ≥10s | User (App) | `HOLD-005` | TBD |
| TBD | Right: toggle item 2 (Off), pause ≥10s | User (App) | `HOLD-005` | TBD |
| TBD | Right: toggle item 3 (Adaptive), pause ≥10s | User (App) | `HOLD-005` | TBD |
| TBD | Right: toggle item 4 (Transparency), pause ≥10s | User (App) | `HOLD-005` | TBD |
| TBD | Session end | — | — | TBD |

#### Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AR)

- [ ] Confirm each toggle produces a `field5(len12){field4(len10){field12(len8){field1..4=0|1}}}`
      write on DLCI 0x02 (`qhr` field 12 / `qht`), per `PROTOCOL.md` §4.5.3.
- [ ] Do the Left-earbud toggles and Right-earbud toggles produce distinguishable wire patterns (a
      different inner field position, a different correlation-ID pattern), or is Left/Right only
      inferable from timing/video correlation (as `CAP-021`/`CAP-045` already found for the
      structurally related press-and-hold-cycle Notify)?
- [ ] Record the result plainly either way — this directly closes `PROTOCOL.md` §6's open item on
      this question.
- [ ] (A) Which bit clears for Adaptive vs Transparency; whether "Customize right" shows the change made on "Customize left".
- [ ] (B) W2/W5: the field-2 writes against the filmed switch (the label promotion needs the maintainer's approval in chat); W1 vs W3: pause, Buds `DISC`
      of DLCI 0x02 and Settable with in-ear detection on vs off — the evidence the draft in-ear-detection-write ADR (`ai-sessions/0053` §6) waits for.

#### Next steps after filling this in

- [ ] Cross-reference this session's own findings against `PROTOCOL.md` §4.5.3/§6 and
      `CAP-021-FINDINGS.md`/`CAP-045-FINDINGS.md` (`AGENTS.md` §13's traceability check).
- [ ] Write `CAP-056-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Update `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `HOLD-005` row with the result.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.


---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-056-2026-09-28_17-30-53_17-35-58-Group_AR/CAP-056-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-056-2026-09-28_17-30-53_17-35-58-Group_AR/CAP-056-EVENT-NOTES
