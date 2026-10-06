# CAP-054: the `0xFE2C` advertisement on case-open, connection-free (Group AP, `BATT-007`, `BATT-002`, `BATT-003`)

Evidence: `CAP-054-recording.mp4` (356.02 s) and `CAP-054-btsnoop_hci.log` (= the first 4,807 packets of `CAP-058-btsnoop_hci.log.last`, which also
holds the end of this run's third connection). Timeline: `CAP-054-EVENT-NOTES.md`. Analysed in `ai-sessions/0072` (2026-10-05). Labels per
`PROJECT_RULES.md` §1: 🟢 FACT, 🟡 HYPOTHESIS, ⚪ ASSUMPTION, 🔴 OPEN QUESTION.

## 0. Capture metadata

| Field | Value |
|---|---|
| Purpose | Group AP (lead L68-6): does the Buds' `0xFE2C` advertisement carry the Fast Pair Battery Notification right after the lid opens, both buds inside, phone not connected (`PROTOCOL.md` §4.3 Option A, ADR-006) |
| Date | 2026-10-05, film 18:02:31.4–18:08:27.4 phone time |
| Phone / app | Pixel 7a, Android 17 `CP3A.260905.009`; official app 1.0.955078536, force-stopped on film (18:02:59) |
| Firmware | `release_5.203` (announcements 2295, 3029, 4550) |
| Log path | raw `btsnoop_hci.log` (pulled while Bluetooth stayed on; it is a byte-prefix of `CAP-058-btsnoop_hci.log.last`) |
| Clock | phone = film + 18:02:31.4 ± 0.13 s |

## 1. The Group's question: no Battery Notification field (🟢 FACT for this run)

**The specification** (raw text of `developers.google.com/nearby/fast-pair/specifications/extensions/batterynotification`, fetched 2026-10-05 with
`curl`): after the Account Key Data, *"s + 1 | uint8 | Battery level length and type 0bLLLLTTTT … length = 0b0011 = 3 battery values · type = 0b0011
(show UI indication) or 0b0100 (hide UI indication)"*, then *"s + 2, s + 3, s + 4 … The battery values should be ordered as left bud (s + 2), right bud
(s + 3) and case (s + 4)"*, each `0bSVVVVVVV`; *"One common use case for this is to use 0b0011 when the case has opened and 0b0100 when buds have been
removed from the case or it has been closed again."* and *"To prevent tracking, the Provider should not include raw battery data in the advertisement
all the time."* The SASS page (`…/extensions/sass`, fetched the same way) gives the full order: *"Version and flags 0x10 · Account Key Data · Battery
Data (Optional) · Random Resolvable Data"*, the last field typed *"0bLLLL0110"*; the provider page (`…/service/provider`): Account Key Filter
*"type = 0b0000 (show UI indication) or 0b0010 (hide UI indication)"*, Salt *"0b00100001 … type = 0b0001"*.

**The wire.** Every advertising report with service data `0xFE2C` in the log (`tshark -r CAP-054-btsnoop_hci.log -Y 'btcommon.eir_ad.entry.uuid_16 ==
0xfe2c' -T fields -E separator='|' -e frame.number -e frame.time_epoch -e bthci_evt.bd_addr -e bthci_evt.rssi -e btcommon.eir_ad.entry.service_data -e
btcommon.eir_ad.entry.uuid_16`; exit 0; **610** reports), walked field by field (version byte, `0bLLLLTTTT` account key filter, salt):

| Advertiser | When (phone) | Reports | Fields after the version byte | Battery field? |
|---|---|---|---|---|
| `36:91:c4:7d:54:e4` (closed-case form, as `CAP-043`) | whole log | 256 | filter **type 2 (hide UI)**, 5 bytes · salt (2) · then **`0x2a` with the lid closed (51 of 51), `0x29` with the lid open (174 of 174)** followed by 8 bytes that are not a field of the three pages | no |
| `59:c2:44:e0:c7:4e` | 18:04:21.512 – 18:05:03.919 (lid open #1) | 78 | filter **type 0 (show UI)**, 5 · salt · **`0x46`** = Random Resolvable Data, 4 bytes | no |
| `6e:e9:9a:a6:b7:26` | 18:05:37.949 – 18:06:15.226 (lid open #2) | 77 | type 0 · salt · RRD (4) | no |
| `43:ec:60:de:d5:d8` | 18:06:15.871 – 18:07:01.903 (Left bud out, then in) | 39 | type 0 · salt · RRD (4) · sometimes `0x1a` + 1 byte + 3 bytes | no |
| `75:77:c6:2d:85:83` | 18:07:03.226 – 18:10:11 (Right bud out, …) | 150 | as `43:ec…` | no |
| `72:b4:f7:b4:89:c4` | 18:10:17 – (after the film) | 10 | as `43:ec…` | no |

**Byte after the salt equal to `0x33`/`0x34` (a 3-value battery field, show/hide): 0 of 610.** Raw examples (`data` as above):
lid closed, 129 (18:02:47.764): `10 52 39a5051540 21 87eb 2a e1d423e253d23b73`; lid open, 923 (18:04:19.985): `10 52 00eacc2602 21 3586 29 d8483fe45336e595`;
show-UI advertiser, 970 (18:04:21.770): `10 50 001b72a102 21 fdf9 46 b213843d`; bud out, 1769 (18:06:15.871): `10 50 081c841a13 21 f342 46 4347e3dd 1a76630722`.

**Positive controls.** (a) The scanner ran the whole time (`LE Set Extended Scan Enable` at 18:02:45 … 18:07:39) and delivered the Buds' reports in
every step (table). (b) The same filter on `CAP-043-btsnoop_hci.log` (the closed-case baseline) returns 320 reports, 60 of them the same
closed-case form (`52:39:01:d3:49:1e`: `10 52 13d3802844 21 54a6 2a b029be65f6a05016`).

**Attribution.** 🟢 `43:ec…`, `75:77…`, `72:b4…` are the Buds: each is the LE address the Buds reported in their Message Stream "BLE address updated"
(`03 02`, frames 2046, 3124, 4475). 🟡 `59:c2…` and `6e:e9…`: same structure, they appear only while the lid is open and stop at the close / the bud
removal. 🟡 `36:91…`: the closed-case form of `CAP-043`, RSSI −13 to −28 dBm next to the phone, its post-salt byte follows the filmed lid state in
225 of 225 reports, its payload changed at 18:04:16.511 — 0.2–0.5 s before Settings changed "C: 54%" to "C: 53%".

**Answer.** 🟢 With this firmware the Buds send **no** Battery Notification field in clear — not with the lid closed, not in the ≈ 44 s and ≈ 50 s after
each lid opening, not with one bud out — and on lid open they switch to a **show-UI** account-key advertisement (filter type 0) with Random Resolvable
Data. The spec's "use case" sentence is not followed in the documented form.

## 2. Yet Android showed the levels without a connection (🟢 film, 🟡 source)

Settings changed "C: 54%" → "C: 53%" at 18:04:16.7–17.0 with the lid closed, and a heads-up "Pixel Buds Pro 2 van Ted · **Left 100% Case 53% Right
100%**" appeared at 18:04:21.0–21.2 (≈ 1.5 s after lid open #1) and at 18:05:38.2–38.5 (lid open #2). No classic ACL and no LE link to the Buds
existed before 18:06:18 (`tshark -Y "bthci_evt.code==0x03 || (bthci_evt.code==0x3e && bthci_evt.le_meta_subevent==0x0a)"` → only the LE link to
`63:2c:d8:77:cb:fa` at 18:02:48). The values match the Buds' own later report (runtime info Case 53, frame 2308; battery 100/100, 2054). 🟡
HYPOTHESIS: the phone (Play services' Fast Pair) took L/C/R from the advertisement — not from the documented clear field, so from bytes it can decode
with the account key: the 8 undocumented bytes after `0x2a`/`0x29` of the closed-case form (they changed 0.2–0.5 s before the "C 53" display; the first
show-UI report came 0.3 s *after* the first heads-up), or the Random Resolvable Data. Test: a long lid-closed session watching the Case % in Settings
against payload changes of the closed-case advertiser; the phone's own log (`adb logcat`, Play services' tags) at a lid opening — observation only,
Play services is not reverse-engineered (ADR-025).

## 3. The run was not connection-free at the bud steps (🟢)

Each bud taken out started a **Buds-initiated** classic connection (as `PROTOCOL.md` §5's 2026-10-01 Update): Left out 18:06:16.0–16.7 → `Connect
Request` 1773 at 18:06:18.004 → ACL 0x0001; Left back 18:06:37.2 → `Disconnect Complete` 0x13 18:06:37.242; Right out 18:07:03.0–03.7 → 2639 at
18:07:04.081 → ACL 0x0004; Right back 18:07:38.5 → 0x13 18:07:38.850. The lid openings alone started no connection (2 of 2). The first connection
showed "Problem connecting. Turn device off & back on" for ≈ 2 s before "Active". On ACL 0x0004 the Buds opened the multiplexer (`SABM` DLCI 0 Rcvd 2864,
phone's own 2850 collided) — MAESTRO on DLCI 0x03, Message Stream on 0x05. Which bud was out is confirmed on the wire: Message Stream battery `64 e4`
(Right charging) on 0x0001 and `e4 64` (Left charging) on 0x0004 (2054, 3133); the announced channel was 19 with the Left out and 21 with the Right out
(2295, 3029) — two more samples for `PROTOCOL.md` §2.2a's channel/bud FACT. A third Buds-initiated ACL (0x0005) began at 18:10:10, after the film (both
buds out per its runtime info, 4573) and ended 18:18:00.803 (`CAP-058-btsnoop_hci.log.last` 5829).

## 4. The force-stopped app ran again (🟢 wire, 🔴 trigger)

Force stop at 18:02:59 (film). On each ACL the phone searched SDP for the "pigweed" UUID `25e97ff7-…` (2268, 2994, 4453) and opened MAESTRO (2282,
3003, 4515) with the official app's full sweep (`ReadSetting` ×196 over the three connections, `pwrpc_decode.py`) — the app's own channel (ADR-018), so
its process ran again by 18:06:22 at the latest. What started it is not on record (no `logcat -b events`). It reads the EQ the run of `CAP-053` left:
16 = `[−1.0, 4.3, 4.2, −4.8, 5.0]`, 18 = `[−1.0, 0.0, 4.2, −4.8, 5.0]` (2378/2386) — `CAP-053-FINDINGS.md` §1.

## 5. Other protocols

Message Stream per connection (`msgs.py`): Play-services claim, nonce, Model ID, BLE address, battery ×3, `08 11`/`08 13` (Off, Settable `00` on 0x0001
2092 and 0x0004 3146; Transparent → Adaptive → NC on 0x0005 4503/4650/4759 — after the film), SASS. GSND CONTROL on DLCI 0x08/0x09 (both directions
open), HFP opened by the Buds on DLCI 0x09 (the phone's channel 4) on 0x0001/0x0005. pw_rpc: 294 lines, 1 `CLIENT_ERROR` (phone), no unknown service.

## 6. Differences from earlier captures

- 🟢 First capture of the Buds' advertising across a lid opening without a connection: the closed-case form (`CAP-043`) gets a lid-state byte
  (`0x2a` closed / `0x29` open — new), and show-UI advertisers with Random Resolvable Data appear only while the lid is open.
- 🟡 `CAP-036`'s 407 reports from the Buds' LE address with first byte `0x10` (`PROTOCOL.md` §4.3 Option A) are this same version byte (`0x10` = the SASS
  table's "Version and flags"), not a broken "Flags" byte — a reading to add, not a contradiction.

## 7. Proposals (awaiting the maintainer)

1. **`PROTOCOL.md` §4.3 Option A** — dated Update: 🟢 (`CAP-054`, firmware `release_5.203`) no Battery Notification field in clear in 610 reports over a
   closed baseline, two lid openings and both bud removals; on lid open the Buds advertise show-UI account data with Random Resolvable Data; the
   advertisement format is the SASS table's (`0x10`, account key data, RRD); a lid-state byte after the salt (🟡 meaning); Android still showed
   L/C/R without a connection (🟡 decoded from encrypted/undocumented bytes). Status of Option A: from "never matched" to "not sent in clear by this
   firmware in the spec's own use case" (🟢 one run).
2. **ADR-006** — no change to the bounds; a dated note that a bounded scan would find no clear battery field on this firmware, so nothing is built
   under it (the Case level stays DLCI 0x02 Option F, ADR-043).
3. **`PROTOCOL.md` §6** — the `CAP-011` open item ("why do the payloads not match the Battery Notification layout") gets this answer; new 🔴: the
   meaning of the post-salt bytes and of the `0x1a` field.

## 8. Consequences for OpenControl

None to build. A Case level without a connection is not available from a clear advertisement field on this firmware; decoding encrypted data would need
the Fast Pair account key (Play services owns it; ADR-008/025). The app's Case source stays DLCI 0x02 (ADR-043).

## 9. Open questions

- 🔴 What the 8–9 bytes after the salt of the closed-case advertiser hold, and the `0x1a` field of the bud-out advertisers.
- 🔴 What Android decoded "Left 100% Case 53% Right 100%" from (§2).
- 🔴 What restarted the force-stopped app before 18:06:22 (§4).

## 10. Test-ID traceability

`BATT-007` — exercised (2 lid openings; negative). `BATT-002` — exercised (no battery field in any step). `BATT-003` — **not exercised** (no bud worn).
Also: `CASE-003`, `CASE-004` (Left out), `CASE-005` (Right out), `PAIR-003`, `BATT-001`, `BATT-004` (Message Stream battery on each connection).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-054-2026-10-05_18-02-33_18-08-29-Group_AP/CAP-054-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-054-2026-10-05_18-02-33_18-08-29-Group_AP/CAP-054-FINDINGS
