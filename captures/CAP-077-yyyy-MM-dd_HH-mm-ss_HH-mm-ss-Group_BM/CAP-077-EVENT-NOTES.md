# Event Notes: OpenControl for Pixel Buds Pro 2 on Pixel 9a (GrapheneOS, the secondary user without Google Play services) — Group BM, 1.3.1: the four `CAP-076` / `ai-sessions/0093` fixes, the screen reader and the single mode label (`CAP-077`)

**Status:** 🔲 **Not yet captured — skeleton only** (written by `ai-sessions/0094`, 2026-10-15). The hardware run of **1.3.1** (versionCode 10301), the fix release
the maintainer chose in chat 2026-10-15 ("1.3.1 met APP-01…06"): "Use different Buds" now acts on the pairing the repository connects with (A93-APP-01) and
asks first (A93-APP-08); a re-open's "Changed since the app last saw it" is kept when the same session's `ReadSetting 4:13` answers the same mode (A93-APP-02,
`CAP-076` §6.2); a tap's `Set` no longer takes the `Get`'s own `Notify` as its answer (A93-APP-03, `CAP-076` §5.3); a settings-stream push no longer ends a
pending read (A93-APP-04); the switch rows and EQ sliders are read with their names by TalkBack (A93-APP-07); one name for each mode on the buttons, the (i), the
tile and the notification (A93-APP-08). **Nothing new on the wire:** the app's requests are the 1.3.0 ones (the `transport.send` call sites are unchanged,
`ai-sessions/0094` RESULT). **One part, ≤ 15 minutes on film with sound**, in the **secondary user (user 10) without Google Play** (`ai-sessions/0093` §7 D4 — no
collision with Play on DLCI 0x04). The maintainer films; a later CAPTURE session analyses it and gives the release verdict (`RELEASING.md` C3/C4). After the run:
rename this folder to the screen recording's first/last phone times and the recording to CAP-077-recording1.mp4.

**Not in this run** (say so if asked): the user with Google Play (the per-tap collision is unchanged — `CAP-076` §5); a second pair of Buds (A93-APP-01 is
tested with the same pair chosen again in Android's picker); the case-sound experiment; the wear sequences.

## Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-077` |
| Group(s) | BM (new) |
| Date | TBD |
| Phone | Pixel 9a, GrapheneOS (Android 17) — **the secondary user (user 10) without Google Play** (`CAP-076` part 2's user) |
| App under test | OpenControl for Pixel Buds Pro 2 **1.3.1** (versionCode 10301), the release APK of `scripts/release.sh 1.3.1` (B1), kept as `~/opencontrol-1.3.1-tested` (B3) — **Info tab on film**: "App: 1.3.1, build <B1 hash> (<date>)", no "-dirty" |
| Previous version | 1.3.0 build `ff27ee1` (installed 2026-10-15, `CAP-076`) |
| Official Pixel Buds app | not used |
| Buds | Pixel Buds Pro 2, firmware `release_5.203` expected (any other ⇒ Safe Mode: say so, stop) — both buds **worn** for the hold and tap steps |
| Video files | TBD — the screen recording **with sound** (say each action aloud); no camera |
| Log files | TBD — the HCI snoop log, the app's debug export (Debug mode on), the app logcat, the P0/P1 outputs |

## Preparation

| # | Check | Done |
|---|---|---|
| P0 | `adb shell am get-current-user` (10); `adb shell pm list packages --user 10 \| grep -i -E "gms\|vending"; echo "exit=$?"` (nothing, exit 1); positive control `grep opencontrol` (exit 0) | ☐ |
| P1 | In the installed 1.3.0: Debug mode **on** (on film). `adb shell dumpsys package io.github.tedsluis.opencontrolpixelbuds \| grep -E "firstInstallTime\|lastUpdateTime\|versionCode"` → save. `adb install -r` the tested 1.3.1 APK; the same `dumpsys` again (versionCode 10301) | ☐ |
| P2 | HCI snoop log on | ☐ |
| P3 | Android's **screen recorder with sound** on; the status bar across a minute change at the start and the end | ☐ |
| P4 | **Do Not Disturb on**; TalkBack **off** at the start (BM-3 turns it on and off again) | ☐ |
| P5 | Buds charged, both worn; Android shows them connected before the app is opened; **no app use before the film** | ☐ |

**Rhythm:** one action, then 5–10 s; say what you do. Export the debug log at the end. Serials: never unredacted in any note (first 4 + last 2 characters).

## Steps

DLCIs are session-local (MAESTRO 0x02 or 0x03, Message Stream 0x04 or 0x05). Every MAESTRO request starts `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25` on
**channel 19** or `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25` on **channel 21** (Info → "Control channel"). **Reference frames** — "H1" =
`CAP-076-btsnoop_hci1.log` (1.3.0 `ff27ee1`, handle `0x000b`), re-derived by `ai-sessions/0094` with `tshark -r CAP-076-btsnoop_hci1.log -Y "frame.number==N"
-T fields -e frame.number -e frame.time -e bthci_acl.chandle -e btrfcomm.dlci -e data.data` (exit 0):

| Request / event | Reference |
|---|---|
| The Connect's seventeen MAESTRO requests, with `SubscribeToSettingsChanges` and `ReadSetting 4:13` after `SubscribeRuntimeInfo` | `CAP-076` §2 (H1 4129 → 4130 on channel 21, byte-identical to `CAP-051` 1177/1178); unchanged in 1.3.1 |
| **The snapshot `Get`'s answer, then the equal `4:13` answer** (A93-APP-02) | H1 **4094** 06:41:33.418 `0813000401e8e808` (DLCI 0x04, Noise cancellation) → **4135** 06:41:34.165 `7e00a5032a0422026802080110151dea71de7d5e2551aed0aef4aa8ebc7e` (`4:{13:2}`, +747 ms) — in 1.3.0 the export said "Noise-control mode from the mode read (read)"; in 1.3.1 the (i) keeps "Changed since the app last saw it" and the export says "(unchanged)" |
| **A tile tap right after the `Get`'s answer** (A93-APP-03) | `CAP-076` §5.3 (06:46:08.96: no `Set` on the wire, nothing said) — in 1.3.1 every tap ends in a `Set` (`08 12 …`) with its ACK, or in a failure message |

Anything else from the app on the wire (a request not in this table or in `CAP-076` §2, a claim of DLCI 0x04 that is not a tap, the tile, Refresh, a pull or a
Connect's snapshot, a `WriteSetting 4:{13:n}`) is a finding.

| Step | Action (say it aloud) | Expected on screen | Expected on the wire | Test-ID | Result |
|---|---|---|---|---|---|
| BM-1 | P1's update; open the app; Info; gear → Settings | "App: 1.3.1, build …"; Settings = Dark mode, Case sounds, Use different Buds | the seventeen requests | `PAIR-003`, `FW-003` | |
| BM-2 | The labels: the ANC tab and its (i); pull the notification shade; the tile; the Sound tab's presets | the (i) "ANC mode: Noise cancellation …" (not "ACTIVE"), "Connection: connected"; the notification "Connected — noise control: …"; the tile's subtitle the same word; the presets "Heavy bass", not "HEAVY BASS" | — | | |
| BM-3 | TalkBack on: Settings → Case sounds (touch each switch row); Controls → a switch row; Sound → the Bass slider; the bottom tabs; TalkBack off | each row read with its name and state ("Earbuds replaced, switch, on"); the slider "Bass, … dB"; each tab named once | — | | |
| BM-4 | Disconnect; two press-and-holds while disconnected (the mode changes); Connect; the ANC (i) | after the Connect: **"Changed since the app last saw it (read at HH:MM:SS) …"** stays (A93-APP-02) | the snapshot `Get`'s `Notify` (new mode), then the `4:13` answer with the same mode, as H1 4094 → 4135; the export "(unchanged)" for the second | `PAIR-003`, `TOUCH-007`, `ANC-001`…`ANC-004` | |
| BM-5 | Right after a Connect (within ≈ 2 s of "Ready"): ten taps on the tile, ≈ 3 s apart | every tap changes the mode or says why not — **none silent** (A93-APP-03) | per tap: `08 11`, its `Notify`, then `08 12 …` and the ACK (or the failure) | `ANC-001`…`ANC-004` | |
| BM-6 | A press-and-hold during a Connect (tap Connect, hold within ≈ 1 s) | the held mode shown | a `4:{13:n}` stream packet before the `4:13` answer is possible; the export's "Noise-control mode read (channel N)" follows the RESPONSE, not the push (A93-APP-04) | `TOUCH-007`, `PAIR-003` | |
| BM-7 | Settings → **Use different Buds** → **Cancel** | the dialog closes; nothing changes | — | | |
| BM-8 | **Use different Buds** → **Forget these Buds**; rotate the phone; Connect | not connected to the Buds by their name (A93-APP-01): the app asks to pair (*Pair a device*) | **no `SABM`** on DLCI 0x02 after the Forget until BM-9 | `PAIR-003` | |
| BM-9 | *Pair a device* → choose the same Buds in Android's picker | connected; the seventeen requests | the Connect's requests | `PAIR-003` | |
| BM-end | Export the debug log; the app logcat; the APK pull | "Debug log saved (N lines)." | — | | |

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-077-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BM/CAP-077-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-077-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BM/CAP-077-EVENT-NOTES
