# Event Notes: OpenControl for Pixel Buds Pro 2 on Pixel 9a (GrapheneOS, the secondary user without Google Play and the Owner user with it) — Group BP, a short run: the 1.4.1 fixes, a Disconnect during a Connect and the Dosimeter without a sound-level meter (`CAP-080`)

**Status:** 🔲 **planned** — skeleton written 2026-10-18 by `ai-sessions/0100` (the build: `ai-sessions/0100_FEATURE_RESULT_2026_10_18.md`). Nothing below is a
result; every "expected" is a prediction from `CAP-079` and the 1.4.1 code, to be confirmed or refuted by this run.

**What this run checks.** 1.4.1 fixes what `CAP-079` showed (CAP-079-FINDINGS.md §3.3, §5.2, §11): a Ring now holds the Message Stream claim (DLCI 0x04) for up to
**65 s** — `DECISIONS.md` ADR-069 Update 2026-10-18; no new bytes — so the Buds' stop status reaches the app and "Stopped on the bud at …" can appear; the red "The
Buds don't allow changing noise control right now …" line goes away as soon as the Buds report a settable mode; three texts are reworded; and the experiment build's
F1 `SetWallclock` reports its answer instead of a time-out. The run also tries the two steps `CAP-079` could not do: a **Disconnect during a Connect** (A68-APP-13,
BO-16 "not hit") and `WELL-002` (draft ADR-060's 🟡 i) — **without a sound-level meter** (the maintainer has none, chat 2026-10-18): a test file with four steps of
exactly 10 dB tells whether the live value is a power-like level of what the buds play, and the Buds' own day dose is compared with the integral of the live value
(in `CAP-079` the dose grew by exactly the integral in value × minutes, see below). TalkBack (A93-APP-07) is **not** in this run — the maintainer's choice in chat
2026-10-18; it is in `TODO.md`.

Storage per `DECISIONS.md` ADR-066: films, full logcats and system logs stay local; commit the HCI logs, the app's debug exports, the extracts.

## Preparation

| # | Check | Done |
|---|---|---|
| P0 | `adb shell am get-current-user`; `adb shell pm list packages --user 10 \| grep -E "com.google.android.gms$\|com.android.vending$"; echo "exit=$?"` (nothing, exit 1) and the same for `--user 0` (both, exit 0) | ☐ |
| P1 | **1.4.1** (the release APK of `feature/0100-fixes`, versionCode 10401) in **both users**; the **experiment APK 1.4.1-exp.1** (package `….experiment`) in user 10; `sha256sum` of both against the RESULT; the Info tab on film ("1.4.1, build …", not "-dirty") | ☐ |
| P2 | HCI snoop log on; a fresh log per part (three parts) | ☐ |
| P3 | Screen recording **with sound** + the camera on the phone and the buds; the status bar across a minute change at the start and the end of each part | ☐ |
| P4 | Do Not Disturb on; TalkBack off | ☐ |
| P5 | Buds charged; in-ear detection **on** at the start; all four press-and-hold modes ticked (Controls) | ☐ |
| P6 | **One app at a time:** force-stop the other OpenControl package before opening one (`adb shell am force-stop --user N <package>`) | ☐ |
| P7 | **Part 3:** the test file levels.wav on the phone (see "WELL-002 without a sound-level meter" below); a local player that plays it without "volume normalisation"; the media volume fixed at one value | ☐ |

**Rhythm:** one action, then 5–10 s; say what you do and what you hear. **Never ring with a bud in an ear**, and keep your ears away from a bud playing at full volume.

### How to do a Disconnect during a Connect (BP-9)

The app's Connect is normally far too short to interrupt by hand: in `CAP-079` the step "Disconnected → Connecting" → "Connecting → Discovering" took **0.19 – 0.60 s**
in 26 of 27 Connects. It took **4.41 s** once (E1 07:26:52), when the Bluetooth link to the Buds had dropped and the phone first had to find the Buds again. So
the trick is to **make the phone find the Buds again** before you tap Connect. And while the card says "Connecting…" it has **no Disconnect button**: the only other
way to end the session is **gear → Settings → "Use different Buds" → "Forget these Buds"** (it disconnects first, `MainActivity.kt` `useDifferentBuds`).

1. **Practise the taps once without connecting:** gear (top right) → Settings tab → scroll down to "Use different Buds" → tap it → the dialog → **Cancel**. Remember
   where each tap lands; the confirm button is "Forget these Buds".
2. In the app: Disconnect (the Connection card). Keep the buds **out of the case**, out of your ears, next to the phone.
3. In **Android's** Bluetooth settings (Settings → Connected devices → the Buds → **Disconnect**): this drops the phone's link to the Buds, so the next Connect has to
   find them first. Wait 5 s. Go back to the app (the card says "Disconnected"; the Buds stay paired).
4. Tap **Connect**, then at once: gear → Settings → "Use different Buds" → **Forget these Buds** — all within ≈ 3 s. Say "now" when you confirm.
5. Look at the Connection tab: expected **Disconnected** and "Pair a device" (the app forgot its association; the Android pairing stays).
6. Export the debug log and look for **"Connect finished after a Disconnect tap: its channels are closed again"**. If instead it says "Session ended by the user's
   Disconnect tap (was Ready)", the Connect was quicker than the taps: **Pair a device** (pick the same Buds in Android's picker), then repeat from step 2 — at most
   three times.
7. Afterwards: **Pair a device** to get the Buds back into the app.

🟡 That Android's "Disconnect" makes the app's Connect slow is a prediction from one sample (`CAP-079` E1 07:26:52); if every Connect is still < 1 s, say so — a
"not hit" with the measured Connect times is a result too.

### WELL-002 without a sound-level meter (BP-12 … BP-14)

**What is already known, without a meter.**
- 🟢 (code) The official app calls the live value "the current volume of your earbuds media measured in decibels (dB)" (`strings.xml`
  `summary_hearing_wellness_current_level_bottom_sheet`) and shows it as `round(10·log10(v))`, 0 below 1, clamped 0–120 (`hiw.java:32–37`). It labels ≥ 85 "Loud"
  and ≥ 100 "Very loud" (`HearingWellnessLiveDbPreference.java:54–55`). Its reference table says 80 dB → 40 hours per week, 90 → 4 h, 100 → 24 min, 110 → 4 min
  (`han.java:157–163`, `plurals.xml`).
- 🟢 (wire, `CAP-079` H3/H4, `scripts/dosimeter_dose_vs_live.py`) The Buds' day dose (`FetchDailySummaries`, the current day's field 6, and field 5) grew between
  two summaries by **1/60 of the integral of the live values in value × seconds** — ratio 0.01666 – 0.01705 in all six intervals, on both channels (for example
  channel 21, 08:13:43.567 frame 2692 → 08:20:14.733 frame 4152: dose +2.288 × 10⁹, integral 1.373 × 10¹¹). So the dose is the live value integrated over time in
  minutes. The official app's 7-day limit `3.3903457E11` (`hwy.java:1979–1998`) is then 40 h at a live value of 1.41 × 10⁸, i.e. 81.5 dB by its own formula
  (🟡: why 81.5 and not the 80 of its reference table is not in the code).
- 🟡 (wire, `CAP-079` §11) the value follows the media the buds play, not the room.

**What a meter would add, and what this run tests instead.** A meter would test Google's calibration (that "80" is 80 dB at the ear). That is the Buds'
firmware, not something this app computes; draft ADR-060 needs only that the app shows what the official app shows. So this run tests the two remaining questions
without a meter:
1. **Is the value a power of what is played?** With the media volume fixed, four steps of exactly 10 dB in the file should give four steps of ≈ 10 in
   `10·log10(v)` (refuted if the steps are ≈ 5, i.e. an amplitude, or not equal).
2. **Is the dose the integral in value × minutes** with known, steady levels (refuted if the ratio is not ≈ 1/60)?

**The test file** (made on the computer, 6 min, mono pink noise on both channels; checked in `ai-sessions/0100` with `ffmpeg … -af volumedetect`: RMS −20.2,
−30.2, −40.2, −50.2 dBFS):

    ffmpeg -f lavfi -i "anoisesrc=color=pink:amplitude=0.5:r=48000:seed=1:d=60" -filter_complex "[0]asplit=4[a][b][c][d];[a]volume=0dB[a0];[b]volume=-10dB[b0];[c]volume=-20dB[c0];[d]volume=-30dB[d0];anullsrc=r=48000:cl=mono:d=30[s0];anullsrc=r=48000:cl=mono:d=20[s1];anullsrc=r=48000:cl=mono:d=20[s2];anullsrc=r=48000:cl=mono:d=20[s3];anullsrc=r=48000:cl=mono:d=30[s4];[s0][a0][s1][b0][s2][c0][s3][d0][s4]concat=n=9:v=0:a=1,pan=stereo|c0=c0|c1=c0[out]" -map "[out]" -c:a pcm_s16le levels.wav
    adb push levels.wav /sdcard/Music/levels.wav

The timeline of the file: 0:00 – 0:30 silence · 0:30 – 1:30 step 1 (−20 dBFS) · 1:30 – 1:50 silence · 1:50 – 2:50 step 2 (−30) · 2:50 – 3:10 silence · 3:10 – 4:10
step 3 (−40) · 4:10 – 4:30 silence · 4:30 – 5:30 step 4 (−50) · 5:30 – 6:00 silence.

**How:**
1. In-ear detection **off** (Controls), so the buds keep playing out of the ears. Both buds **out of the ears**, on the table (pink noise at full scale is loud;
   nothing has to be heard).
2. The media volume at **half** (the volume panel on film) and leave it there; no equaliser or "normalise volume" in the player.
3. Experiment card: **F3 Daily 19**, **F3 Daily 21** (the dose before), then **F2 Live 19**, **F2 Live 21**.
4. Play levels.wav from the start and touch nothing for 6 min. Say "start" when it begins.
5. **F3 Daily 19**, **F3 Daily 21** (the dose after).
6. 🟡 If the live values stay 0 while the file plays (the Buds may measure only while worn — unknown), repeat steps 3–5 with **one bud in an ear** and the media
   volume at a **quarter**.
7. Optional, the volume curve: play step 2 again (1:50 – 2:50) at a quarter, half and three quarters of the media volume, 20 s each (the panel on film).

Live packets may already arrive before F2 — the subscriptions of an earlier session outlive it (`CAP-079` §11); that is expected. The analysis: `scripts/pwhdlc_crc_table.py`
on the part-3 HCI log, then `scripts/dosimeter_dose_vs_live.py`, and the median of `10·log10(v)` per step.

## Reference frames (every one is real bytes from a capture, pinned by a test)

| Request | Bytes | Real bytes |
|---|---|---|
| Ring Left / Right / both (DLCI 0x04) | `04 01 00 01 02` / `04 01 00 01 01` / `04 01 00 01 03` | `CAP-079` H1 3848 (`Cap079Fixtures.kt`) / `CAP-025` 2040 / `CAP-078` H 8203 |
| The Buds' ACK of a Ring | `ff 01 00 03 04 01 00` | `CAP-079` H1 3856 |
| The Buds' ring status, the phone's ACK of it | Buds `04 01 00 01 xx` (`00` = stopped); phone `ff 01 00 02 04 01` | `CAP-079` H1 3859 / 3862; stop `CAP-078` H 7816 (`RingCodecTest`) |
| Stop (the app) | `04 01 00 01 00` | `CAP-078` H 8287 (the Buds' status `00` after it: 8292) |
| `WriteSetting 4:{13:2}` Noise cancellation, channel 19 | `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25 1d 9a 8c 9e 2a 04 22 02 68 02 42 d8 d5 f5 7e` | `CAP-079` H1 2495 |
| Its answer with no bud worn (status 9) | `7e 80 a3 03 08 01 10 13 1d ea 71 de 7d 5e 25 1d 9a 8c 9e 30 09 c8 b1 e5 cb 7e` | `CAP-079` H1 2501 (`Cap079Fixtures.kt`) |
| The push of a settable mode (`4:{13:4}`) | `7e 80 a3 03 2a 04 22 02 68 04 08 07 10 13 1d ea 71 de 7d 5e 25 f5 ad 21 28 9a c5 72 66 7e` | `CAP-079` H1 2742 (`Cap079Fixtures.kt`) |
| Experiment F1 `SetWallclock` (channel 19) | `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25 4e ed 3b 67 2a 07 08 <ms varint> <crc> 7e` | `CAP-079` H3 2559 (`ExperimentFramesTest`) |
| Its answer | `7e 80 a3 03 08 01 10 13 1d ea 71 de 7d 5e 25 4e ed 3b 67 12 a9 05 3e 7e` | `CAP-079` H3 2563 (`Cap079Fixtures.kt`, `CodecRouterTest`) |
| Experiment F2 `SubscribeToLiveDb` | ch 19 `7e 00 3b 03 10 13 1d 05 d8 d5 73 25 e2 b6 93 4d e4 61 0c 6a 7e`; ch 21 `7e 00 4b 03 10 15 1d 05 d8 d5 73 25 e2 b6 93 4d e2 18 d6 68 7e` | `CAP-027` 1212 / 1214 |

(H1 = CAP-079-btsnoop_hci1.log, H3 = CAP-079-btsnoop_hci4.log.last, as in CAP-079-EVENT-NOTES.md.)

**Log lines to look for** (the app's debug export): "Ring: claim held until the Buds report the stop (at most 65 s)", "Ring status from the Buds: ringing
left/right/both | stopped", "Ring status acknowledged (ff 01 00 02 04 01)", "Ring: hold ended — the Buds reported the stop | the claim was closed by another client |
no stop report within 65 s", "Noise-control mode not written: the Buds allow no change (FAILED_PRECONDITION — no bud worn?)", "Noise-control mode from the settings
stream (…)", "Connect finished after a Disconnect tap: its channels are closed again"; experiment build: "Experiment F1: the phone's clock N ms", "Experiment F1:
RESPONSE OK", "Experiment F2: sent …", "Experiment F: <type> ch=N <method> <status> <fields>", "Experiment F: N live-stream packets in the last S s".

## Steps

| Step | Action (say it aloud) | Expected on screen | Expected on the wire | Test-ID | "Refuted if" |
|---|---|---|---|---|---|
| **Part 1 — user 10 (no Google Play), OpenControl 1.4.1** | | | | | |
| BP-1 | Connect (both buds worn); the Info tab on film | Connected; "1.4.1, build …" | the MAESTRO session; no SABM on DLCI 0x04 | `PAIR-003` | — |
| BP-2 | The battery card's (i); Find's (i) | the new texts: "Left, Right and the Case: sent by the Buds on their own on the app's channel — …" and "Ring and Stop use the Buds' Message Stream channel. While a bud rings the app keeps that channel (up to about a minute) …" | — | — | refuted if a 1.3.x text ("… Message Stream (at Connect, Refresh and each ANC / Find action)", "Each action briefly claims …") is shown |
| BP-3 | Take both buds out, on the table: ANC tab → Noise cancellation | the red line ending "… A tap still sends the mode; the Buds answer whether they accept it."; the mode stays | the write (channel 19 form above), the answer status 9 | `ANC-007` | refuted if the app shows the new mode |
| BP-4 | Put **one** bud in an ear; wait 5 s (touch nothing in the app) | the **red line goes away by itself**; the mode the Buds report is shown | a push `4:{13:n}` with n = 1–4 (the `CAP-079` H1 2742 form) | `ANC-007` | refuted if the red line stays after a push with n = 1–4 |
| BP-5 | **Buds out of the ears**, near the camera: Find → Ring Left; count aloud; **touch the Left bud at ≈ 30 s** | "Ringing: Left earbud …", then "Stopped on the bud at HH:MM:SS" | Ring, ACK, status `…02` → ACK; status `…00` ≈ 30 s later → ACK; the claim released ≈ 1.5 s after it | `FIND-001`, `FIND-005` | refuted if the status `00` comes and "Stopped on the bud" is missing, or the log says "no stop report" before 65 s |
| BP-6 | Ring Right and **touch nothing**; wait until it is quiet (≈ 1 min) | after the Buds' own stop "Stopped on the bud at …" | the status `00` without a Stop from the phone, ≈ 63 s after the Ring (`CAP-079` §5.2); "hold ended — the Buds reported the stop" | `FIND-005` | refuted if the tone ends but the log says "no stop report within 65 s" (the hold too short again) |
| BP-7 | Ring both; at ≈ 10 s touch **one** bud (say which); if the other still rings, touch it too | "Ringing: Both earbuds …"; then "Stopped on the bud …" when the Buds report `00` | `04 01 00 01 03`, ACK, status `03`; what status follows the first touch (`01`/`02`, or `00`)? — 🟡 not known, an observation | `FIND-004`, `FIND-005` | refuted if only one bud rings at the start |
| BP-8 | Ring Left; tap **Stop** in the app after ≈ 5 s | the notice ends; **no** "Stopped on the bud" line | Stop `04 01 00 01 00`, the Buds' status `00` (ACKed) | `FIND-002` | refuted if "Stopped on the bud" appears after the app's own Stop |
| BP-9 | **A Disconnect during a Connect** — follow "How to do a Disconnect during a Connect" above (up to three tries) | Disconnected and "Pair a device"; never Connected after the confirm | the channels the Connect opened are closed again (`DISC`); the export's "Connect finished after a Disconnect tap …" | `PAIR-003` (A68-APP-13) | refuted if the app shows Connected (or Ready) after "Forget these Buds" |
| BP-10 | Pair a device (the same Buds); Connect; export the debug log; force-stop the app | "Debug log saved (N lines)." | — | — | — |
| **Part 2 — the Owner user 0 (with Google Play), OpenControl 1.4.1** | | | | | |
| BP-11 | Connect; buds out of the ears: Ring Left; touch nothing for 10 s; Stop; export; force-stop | "Ringing: Left earbud …"; no "Stopped on the bud" the app did not hear | Ring, the hold, Play's cut (4.75 – 4.81 s after the claim's `SABM`, `CAP-079` §5.3), "hold ended — the claim was closed by another client"; Stop on a new claim | `FIND-001`, `FIND-005` | refuted if the app re-claims by itself after the cut |
| **Part 3 — user 10, the experiment app 1.4.1-exp.1** (force-stop the release app first; gear → Debug) | | | | | |
| BP-12 | Connect; **F1 Clock** once; the phone's clock in frame | "Experiment F1: RESPONSE OK" | `SetWallclock` with the phone's clock, its `RESPONSE` (the H3 2563 form) | `WELL-003` | refuted if it says "failed: Timeout" while the `RESPONSE` is on the wire |
| BP-13 | In-ear detection off; buds out of the ears; media volume at half; **F3 Daily 19**, **F3 Daily 21**; **F2 Live 19**, **F2 Live 21** | "Experiment F3/F2: sent …" | the two summaries; `SubscribeToLiveDb` on both channels | `WELL-002`, `WELL-001` | — |
| BP-14 | Play levels.wav (6 min, touch nothing); then **F3 Daily 19**, **F3 Daily 21**; if the live values stay 0, once more with one bud worn at a quarter volume | — | live packets with four plateaus; the summaries after | `WELL-002` | refuted (a power level) if the medians of `10·log10(v)` per step do not differ by ≈ 10 (± 1.5); refuted (the dose rule) if Δ day dose / ∫ v dt (s) is not ≈ 1/60 on either channel |
| BP-15 | In-ear detection on again; export; force-stop; `adb logcat -b all`; the three HCI logs; stop the films | — | — | — | — |

## Run card (one page for the maintainer)

1. P0–P7 (two users, two apps, HCI on, film + camera with sound; levels.wav on the phone for part 3).
2. **User 10, 1.4.1:** Connect, Info tab → the two (i) texts → both buds out, tap Noise cancellation (red line) → one bud in, wait (red line gone) → buds out of
   ears: Ring Left, touch at 30 s → Ring Right, wait for silence → Ring both, touch one, then the other → Ring Left + Stop → **Disconnect during a Connect** (the
   recipe above; up to three tries) → Pair a device, Connect → export.
3. **User 0, 1.4.1:** Connect → Ring Left, wait 10 s, Stop → export.
4. **User 10, experiment app:** Connect → F1 → in-ear detection off, buds out, volume half → F3 19/21 → F2 19/21 → play levels.wav (6 min, touch nothing) →
   F3 19/21 → (values 0? once more with one bud worn, volume a quarter) → in-ear detection on → export.
5. Pull everything; keep the films and the full logcats local (ADR-066).

---

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-080-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BP/CAP-080-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-080-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BP/CAP-080-EVENT-NOTES
