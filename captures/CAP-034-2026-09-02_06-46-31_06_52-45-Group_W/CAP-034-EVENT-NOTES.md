# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group W, GATT handle↔UUID mapping (`CAP-034`)

**Status:** ⬜ Not yet captured. Skeleton prepared for `CAP-014-FINDINGS.md` §8's recommended next
step: a session combining (1) a confirmed-unlimited HCI snoop snaplen and (2) a genuine GATT
cache-miss for the *entire* database (not just the GATT service, `CAP-014-FINDINGS.md` §4).

**Method for this session — a hybrid not yet tried, per the maintainer's own device availability:**
Group W's Option (b) (a phone that has never connected to this Buds unit) was originally intended
for the Pixel 9a, but the maintainer confirmed this specific Pixel 9a *has* connected to this Buds
unit before — so Option (b)'s own precondition doesn't hold as-is. This session instead combines
Option (a)'s cache-clearing action (`pm clear com.android.bluetooth`) **applied to the Pixel 9a**
(GrapheneOS) rather than the Pixel 7a used throughout the rest of this project, using nRF Connect
as the GATT client (the same third-party-client approach that got `CAP-017` a genuine, if
truncated, discovery walk). Fill in each `[ ]`/`___` below as the session happens — do not
pre-fill any log-derived value (frame numbers, exact timestamps) before the capture exists.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-034`                     |
|      Group(s)    | W (`GATT-001` — GATT handle↔UUID mapping, cache-busted via `pm clear com.android.bluetooth` on a phone not otherwise used in this project's captures) |
|       Date       |                     `___` (fill in)                 |
| Firmware version |    `___` (confirm on-screen if possible — no official Pixel Buds app is used this session, see below, so this may have to stay ⚪ ASSUMPTION `release_5.203` carried over from the most recent capture) |
|   Test device    | Pixel 9a, GrapheneOS `___` (Android/GrapheneOS version — check Settings → About phone). **Client app: nRF Connect for Mobile (Nordic Semiconductor)** — official Pixel Buds Companion App **not installed** this session (see Preparation below) |
| Video file       | `CAP-034-recording.mp4` — `___`s, `___`–`___` local time |
| Log file         | `CAP-034-btsnoop_hci.log` — `___`s, `___` packets, `___`–`___` local time |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` (expected — same physical Buds/case used throughout this project; confirm on capture) |

**Capture-integrity pre-flight (do this immediately after extraction, before any analysis — per
`CAP-014-FINDINGS.md` §0's method and `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §6's snaplen gotcha):**

```
$ capinfos CAP-034-btsnoop_hci.log
# check: "Packet size limit" should read "(not set)" / "inferred: 262144" or similar — NOT a small value

$ tshark -r CAP-034-btsnoop_hci.log -T fields -e frame.number -e frame.cap_len -e frame.len \
  | awk '$2!=$3 {c++} END{print "mismatches:", c+0}'
# expect: mismatches: 0
```

If this fails (truncated), stop before spending analysis time — re-extract via §3 step 3's raw
`FS/data/log/bt/btsnoop_hci.log` / `FS/data/misc/bluetooth/logs/btsnoop_hci.log` path instead of
the `btsnooz.py` fallback (the fallback is what produced `CAP-017`'s truncation).

## Preparation checklist (before recording)

- [x] Pixel Buds Companion App: **not installed** on the Pixel 9a for this session — not needed
      (GATT-001 only needs a generic GATT client; matches this project's own established Pixel 9a
      baseline, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §1's device table: "no Pixel Buds app").
- [x] Confirmed: this Pixel 9a **has** connected to this Buds unit before (per this session's own
      preparation) — a real cache risk, not a hypothetical one. Run, in this order:
      1. `adb shell pm clear com.android.bluetooth` (clears **all** Bluetooth pairings on the
         9a, not just the Buds' — confirm this is acceptable; re-pairing any other device on
         this phone afterward is expected).
      2. Clear nRF Connect's own app cache/data too — `adb shell pm clear <nrf connect package>`
         (check the actual installed package name) or Settings → Apps → nRF Connect → Storage →
         Clear storage/cache. `CAP-004-FINDINGS.md` §6 already found nRF Connect's on-screen
         service list can be served from the app's *own* cache even when `com.android.bluetooth`
         itself has none — clearing only one of the two risks a false "it worked" read from the
         UI that the HCI log won't back up.
      - *(A full Settings → System → Reset options → "Reset Wi-Fi, mobile & Bluetooth" would also
        work, but is more than this needs — it additionally wipes Wi-Fi networks and every other
        paired Bluetooth device on the phone. `pm clear com.android.bluetooth` alone is the
        targeted equivalent; use the full reset only if `pm clear` turns out not to be available/
        sufficient.)*
- [x] Bluetooth HCI snoop logging enabled (Developer options), then **reboot** the Pixel 9a
      (recommended default per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §2 step 5 — more reliable than a
      plain toggle).
- [x] USB debugging enabled, `adb devices` shows the 9a as `device` (not `unauthorized`/`offline`).
- [x] A way to note wall-clock timestamps of your own actions during the session (§1.3).
- [x] Confirm the Buds are currently **not** bonded to the 9a on screen (Settings → Connected
      devices) before starting — if they still show up bonded, the `pm clear` above didn't take;
      re-check before proceeding.

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group W + §4.2's Pixel-9a session shape)

1. Start video recording (wall-clock overlay visible) and HCI snoop logging.
2. Open the Buds case, press the pairing button. **[`PAIR-001`]**
3. In nRF Connect's **Scanner** tab, find and tap **CONNECT** on the Buds. Note the exact time.
   **[`GATT-001`]**
4. Let discovery run — do not interact for a few seconds after connecting, so the initial
   MTU-exchange + discovery burst is cleanly isolated (mirrors `CAP-017`'s 18:31:39.58–41.94
   window). **[`GATT-001`]**
5. Once nRF Connect's **CLIENT** tab populates, scroll through the **full** service list
   on screen (top to bottom) so every service/characteristic name is video-visible, in case the
   wire log needs a video cross-check the way `CAP-017`'s truncated log did.
6. **Drill down into "Unknown Service" (`109b862f-…`)** specifically — tap each of its
   characteristics, use "Read" where available, to trigger characteristic-level (not just
   service-level) declaration traffic. This is the specific action `CAP-014`/`CAP-017` never
   completed. **[`GATT-001`]**
7. Do **not** open or read "Accessory Non-Owner Service" — out of scope per `DECISIONS.md`
   ADR-008 (`PROJECT.md` non-goals). Skip it entirely if it appears in the list.
8. System SSP pairing dialog should appear at some point (classic BR/EDR bonding) — confirm it.
   Note the exact time. **[`PAIR-001`]**
9. Idle ~30–60s once fully connected/paired, without touching anything, to capture any
   spontaneous status traffic. **[`BATT-003`]**
10. Optional, if time allows: disconnect and reconnect once, as an isolated pair of actions.
    **[`PAIR-003`]**
11. Stop video recording and HCI snoop logging. Keep the session short — don't let it run long
    enough to risk on-device log rotation (§2's note).
12. Extract via `adb bugreport` (§3) — check for the raw `btsnoop_hci.log` path first, before
    falling back to `btsnooz.py`.

## Event Timeline

*(Fill in after reviewing the video frame-by-frame against its wall-clock overlay, cross-checked
against `CAP-034-btsnoop_hci.log` via `tshark` — per `AGENTS.md` §13. Add a row per distinct
action/event; don't compress the discovery burst into one row without at least noting its start/end
frame numbers, the way `CAP-017-EVENT-NOTES.md` did.)*

| Time (local) | Action / Event | Initiator | Test-ID | Evidence in `CAP-034-btsnoop_hci.log` |
|---|---|---|---|---|
| `06:46:31` | Start video recording | — | — | — |
| pre-`06:46:31` | `pm clear` / reboot already done pre-session — first frame of this session's log | — | — | frame `___` |
| `06:47:06` | Open case, buds in pairing mode (visible on scanner) | User (Hardware) | `PAIR-001` | — |
| `06:47:41.966` | Tap CONNECT in nRF Connect scanner | User (App) | `GATT-001` | frame `___` |
| `06:47:42.147` - `06:47:45.490` | MTU exchange + discovery burst (Read By Type / Read By Group Type / Find Information) | App (Auto) | `GATT-001` | frames `___`–`___` |
| `06:47:45` - `06:48:13` | CLIENT tab populated — full service list visible on screen (scrolling down) | User (App) | `GATT-001` | — |
| `06:48:13.798` - `06:50:00` | Read individual characteristics (starts with Client Supported Features `2b29`, Fast pair Model ID `fe2c1233...`, etc.) | User (App) | `GATT-001` | frames `___` |
| `06:51:05.069` | System SSP pairing dialog appears ("Koppelen met Pixel Buds Pro 2 van Ted?") | App/OS (Auto) | `PAIR-001` | frame `___` |
| `06:51:08.000` | Confirmed SSP pairing dialog (tapped "Koppelen") | User (App) | `PAIR-001` | frame `___` |
| `06:51:10.332` | Device successfully bonded | App/OS (Auto) | `PAIR-001` | frame `___` |
| `06:51:13.568` | Disconnect (automatic ACL_DISCONNECT following bond completion) | OS (Auto) | `PAIR-003` | frame `___` |
| `06:51:22.142` | Tap CONNECT again (Reconnect from nRF Connect UI) | User (App) | `PAIR-003` | frame `___` |
| `06:51:23.765` | Service discovery on reconnect (should hit cache) | App (Auto) | `GATT-001` | frame `___` |
| `06:51:37.000` - `06:51:40.000` | Drill into "Unknown Service" (`109b862f...`), tap Read on characteristics | User (App) | `GATT-001` | frames `___` |
| `06:51:40` - `06:52:45` | Idle observation window | — | `BATT-003` | — |
| `06:52:45` | End video recording | — | — | — |

## Decode / Analysis

*(Fill in after capture — isolate the Buds' own `chandle` first per `AGENTS.md` §13's CLI-hygiene
rule and `CAP-014-FINDINGS.md` §4a's own worked example, in case another device's LE connection is
in the same log. Then walk the `0x0c0X`/`0x0f2X` handle cluster the same way `CAP-014-FINDINGS.md`
§4c did — command + raw hex per finding, per `PROJECT_RULES.md` §1 rule 4a.)*

## Open Questions

- 🔴 *(carry forward any that remain unresolved after this session)*

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-034-Group_W/CAP-034-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-034-Group_W/CAP-034-EVENT-NOTES
