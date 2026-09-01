# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group W, GATT handle↔UUID mapping (`CAP-034`)

**Status:** ✅ Captured and analyzed 2026-09-01 — see `CAP-034-FINDINGS.md` for the full decode.
This session combined (1) a confirmed-unlimited HCI snoop snaplen and (2) a genuine GATT cache-miss
for the *entire* database (not just the GATT service), per `CAP-014-FINDINGS.md` §8's recommended
next step, and **resolves the `0x0c0X`/`0x0f2X` handle↔UUID mapping question open since `CAP-002`**
(PROPOSAL, pending maintainer approval — see `CAP-034-FINDINGS.md`).

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
|       Date       |                     2026-09-01                      |
| Firmware version |    ⚪ ASSUMPTION `release_5.203` carried over from the most recent capture — no official Pixel Buds app used this session, so the firmware string was never re-confirmed on-the-wire or on-screen (DLCI 0x08 never opens, this session is BLE/GATT-only — see `CAP-034-FINDINGS.md` §2) |
|   Test device    | Pixel 9a, GrapheneOS (exact OS version not captured on screen this session). **Client app: nRF Connect for Mobile (Nordic Semiconductor)** — official Pixel Buds Companion App **not installed** this session (see Preparation below) |
| Video file       | `CAP-034-recording.mp4` — 374.5s, 06:46:31–06:52:45 local time |
| Log file         | `CAP-034-btsnoop_hci.log` — 485.4s, 3,717 packets, 06:45:35.44–06:53:40.82 local time (`+0200`) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` (confirmed — same physical Buds/case used throughout this project, sole LE connection in this log, chandle `0x0040`) |

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
| pre-`06:46:31` | `pm clear` / reboot already done pre-session — first frame of this session's log | — | — | frame 1 (06:45:35.44, 56s of pre-scan idle before video starts) |
| `06:47:06` | Open case, buds in pairing mode (visible on scanner) | User (Hardware) | `PAIR-001` | not independently isolable — Buds appear in the ongoing LE Extended Advertising Report stream (frames from 3191 onward), no discrete "case opened" HCI event exists |
| `06:47:41.966` | Tap CONNECT in nRF Connect scanner (video-confirmed, `f_70.9.jpg`) | User (App) | `GATT-001` | frame 3241 (`Sent LE Extended Create Connection [v1]`, 06:47:42.050644 — ~0.08s app-processing delay after the tap) |
| `06:47:42.073` | LE connection established | App/OS (Auto) | `GATT-001` | frame 3247 (`Rcvd LE Meta — LE Enhanced Connection Complete [v1]`, connection handle `0x0040`) |
| `06:47:42.147` - `06:47:45.490` | MTU exchange + **full, genuine discovery burst** (Read By Group Type / Read By Type / Find Information, handles `0x0001`–`0xffff`) — this is the primary evidence source, see `CAP-034-FINDINGS.md` §3–§4 | App (Auto) | `GATT-001` | frames 3264–3469 |
| `06:47:45` - `06:48:13` | CLIENT tab populated — full service list visible on screen (scrolling down) | User (App) | `GATT-001` | — (no ATT traffic; passive video-only, list already populated from the discovery burst above) |
| `06:48:13.798` - `06:48:23.489` | **Correction — not "read individual characteristics" broadly:** only 5 standard GATT/GAP reads succeed on the wire (Client Supported Features, Database Hash, Server Supported Features, Device Name, Appearance). No Fast Pair/Device-Information/Battery/Unknown-Service value was read in this window. | User (App) | `GATT-001` | frames 3473, 3474, 3476, 3477, 3479, 3481, 3482, 3483, 3485, 3486 |
| `06:49:56.741` - `06:50:00.860` | **New finding, not in the original plan:** nRF Connect's debug log (`f_274.1.jpg`) shows attempted reads of Model ID (`fe2c1233…`) and a Passkey (`fe2c1235…`) notify-subscribe — every attempt throws `Exception occurred (Reading characteristic/descriptor failed)` **1ms** after the call, and **zero** matching frames exist on the wire in this window. Consistent with a pre-bond local/API-level rejection (see `CAP-034-FINDINGS.md` §6b), not a wire-level ATT error. | User (App) | `GATT-001` | none (0 frames, confirmed exhaustively — see `CAP-034-FINDINGS.md` §6b) |
| `06:51:05.069` - `06:51:09.000` | **Correction — this is LE Security Manager (SMP) pairing, not classic SSP**, despite the on-screen dialog text; Cross-Transport Key Derivation (CTKD) pattern, matching `CAP-004`/`CAP-014` (both nRF-Connect-initiated bonds) | App/OS (Auto) | `PAIR-001` | frames 3492 (Pairing Request) – 3518 (Pairing DHKey Check, Rcvd) |
| `06:51:08.000` (approx., unpaused by the 5.5s on-screen dialog gap) | Confirmed system pairing dialog (tapped "Koppelen") | User (App) | `PAIR-001` | inferred from the 3509→3517 gap (`Pairing Confirm` at 05.396 → `Pairing DHKey Check` at 08.900); no separate on-the-wire event marks the tap itself |
| `06:51:10.091` - `06:51:10.332` | Classic BR/EDR link forms via CTKD (no SSP exchange) and device bonds | App/OS (Auto) | `PAIR-001` | frame 3559 (`Rcvd Connect Complete`, chandle `0x000b`) through `Command Complete (Link Key Request Reply)` ~3610 |
| `06:51:13.439` | Classic ACL disconnect (LE link `0x0040` stays up throughout — not a full reconnect, see `CAP-034-FINDINGS.md` §5) | OS (Auto) | `PAIR-003` | frame 3684 (`Disconnection Complete`, chandle `0x000b`) |
| `06:51:22.158` | **Correction — not a full "reconnect + service discovery":** nRF Connect's UI shows `DISCONNECTED`/offers `CONNECT` again (`f_291.1.jpg`), but no new LE connection event occurs anywhere in the log (same chandle `0x0040` throughout) — this tap's only wire-level effect is a single Database Hash re-check (cache hit) | User (App) | `PAIR-003`/`GATT-001` | frames 3690 (Sent) – 3691 (Rcvd) |
| `06:51:37.000` - `06:51:40.646` | **Correction — passive scroll, not a triggered read:** "Unknown Service" (`109b862f…`) characteristics render inline (`f_306.jpg`/`f_309.jpg`), already resolved by the primary discovery burst; **zero** additional Read Request/Response traffic occurs in this window | User (App) | `GATT-001` | frame 3695 only (`Handle Value Confirmation`, handle `0x0003` — an unrelated Service-Changed ack, not Unknown-Service content) |
| `06:51:40` - `06:52:45` | Idle observation window | — | `BATT-003` | — (no traffic) |
| `06:52:45` | End video recording | — | — | — |
| `06:53:40.815` | LE link `0x0040` finally disconnects (post-video, session teardown) | OS (Auto) | — | frame 3717 (`Disconnection Complete`) — last frame in the log |

## Decode / Analysis

Full decode with command + raw hex per finding lives in `CAP-034-FINDINGS.md` (per
`PROJECT_RULES.md` §1 rule 4a's pointer exception). Summary: the Buds are the sole LE connection in
this log (chandle `0x0040`, address `04:00:6e:cf:6e:07` — no cross-device contamination, unlike
`CAP-014`'s log). The 06:47:42.147–45.490 primary discovery burst (before bonding) resolves the full
15-primary-service handle map, including the `0x0c0X`/`0x0f2X` clusters this project has chased
since `CAP-002` — see `CAP-034-FINDINGS.md` §4 for the complete table.

## Open Questions

- 🔴 `FE2C1238-8366-4814-8EB0-01DE32100BEA` (handle `0x0c12`/`0x0c13`, Notify+Write+Read) has no
  confirmed official name — checked against the Fast Pair base spec, the Message Stream extension,
  and the Personalized Name extension; none document this UUID. See `CAP-034-FINDINGS.md` §4/§8.
- 🔴 Why nRF Connect's pre-bond reads/subscribes on the Fast Pair characteristics fail locally
  (`Exception occurred`, 1ms after the call, zero wire traffic) — plausibly an Android
  GATT_BUSY-style local rejection from firing requests without awaiting callbacks, not confirmed.
  See `CAP-034-FINDINGS.md` §6b.
- 🔴 Carried forward, unaffected by this session: the `libmaestro`/ANC-EQ control channel identity
  (this session is BLE/GATT-only, DLCI 0x08 never opens — see `CAP-034-FINDINGS.md` §2).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-034-2026-09-01_06-46-31_06-52-45-Group_W/CAP-034-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-034-2026-09-01_06-46-31_06-52-45-Group_W/CAP-034-EVENT-NOTES
