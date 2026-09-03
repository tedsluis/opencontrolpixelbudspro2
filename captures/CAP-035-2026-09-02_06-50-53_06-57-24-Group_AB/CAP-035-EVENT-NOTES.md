# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AB, DLCI 0x08/0x0a/0x06/0x12 GMS-independence check (`CAP-035`)

**Status:** ✅ Captured and analyzed 2026-09-02 — see `CAP-035-FINDINGS.md` for the full decode.
**Correction, discovered during initial analysis, then narrowed by a later addendum:** this session
does **not** achieve the intended "GMS genuinely absent" condition — `com.google.android.gms` is
present on this phone. Its disabled state, first only a handwritten note, is now **independently
confirmed** via the `dumpsys` output added below: `enabled=3` =
`PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER`, the exact platform-level state a manual
"Uitschakelen"/Disable tap produces. This session is therefore a **properly verified second data
point for `CAP-004-FINDINGS.md` §4a's "GMS present but disabled" condition** (with better rigor than
`CAP-004`'s own original write-up) — meaningfully strong evidence, but still not the fully
conclusive "genuinely absent" confirmation this Group was originally designed to produce. See
`CAP-035-FINDINGS.md` §2 for the full reasoning.

**Method for this session:** Pixel 9a (GrapheneOS), system Bluetooth settings only — no Pixel Buds
Companion App, no nRF Connect, no other BLE/GATT tool. Fill in each `[ ]`/`___` below as the session
happens — do not pre-fill any log-derived value (frame numbers, exact timestamps) before the capture
exists.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-035`                     |
|      Group(s)    | AB (`GSND-001` — DLCI 0x08/0x0a/0x06/0x12 GMS-independence, incidental `PAIR-001`/`PAIR-003`/`BATT-003`) |
|       Date       |                     2026-09-02                 |
| Firmware version |    ⚪ ASSUMPTION `release_5.203` |
|   Test device    | Pixel 9a, GrapheneOS. **No app used — system Bluetooth settings only.** |
| Video file       | `CAP-035-recording1.mp4` — 67.69s, 06:50:53–~06:52:01 local · `CAP-035-recording2.mp4` — 317.57s, 06:52:04–~06:57:22 local (sequential, ~7s recording-stop/restart gap — see the corrected note above the Event Timeline) |
| Log file         | `CAP-035-btsnoop_hci.log` — 488.7s, 1,945 packets, 06:50:58.76–06:59:07.47 local (`+0200`) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` |

**Capture-integrity pre-flight (do this immediately after extraction, before any analysis — per
`CAP-014-FINDINGS.md` §0's method and `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §6's snaplen gotcha):**

```
$ capinfos CAP-035-btsnoop_hci.log
# check: "Packet size limit" should read "(not set)" / "inferred: 262144" or similar — NOT a small value

$ tshark -r CAP-035-btsnoop_hci.log -T fields -e frame.number -e frame.cap_len -e frame.len \
  | awk '$2!=$3 {c++} END{print "mismatches:", c+0}'
# expect: mismatches: 0
```

If this fails (truncated), stop before spending analysis time — re-extract via §3 step 3's raw
`FS/data/log/bt/btsnoop_hci.log` / `FS/data/misc/bluetooth/logs/btsnoop_hci.log` path instead of
the `btsnooz.py` fallback.

## Preparation checklist (before recording)

- [x] **Confirm no Google Play Services package is present at all** (not merely disabled) —
      GrapheneOS's optional sandboxed Play compatibility layer is a separate, explicitly-installed
      app, so check that isn't present either:
      ```
      adb shell pm list packages | grep -i "google\|gms\|play"

      package:com.google.android.overlay.permissioncontroller
      package:com.google.android.overlay.glanceablehubconfig
      package:app.grapheneos.gmscompat.config
      package:com.google.android.apps.fitness
      package:com.google.android.calendar
      package:com.google.android.euiccoverlay
      package:com.google.android.overlay.glanceablehubsettings
      package:com.google.android.overlay.googleconfig
      package:com.google.euiccpixel.overlay.zumapro
      package:com.google.android.networkstack.tethering.overlay2021
      package:com.google.euiccpixel
      package:com.google.android.rilextension
      package:com.google.android.keep
      package:app.grapheneos.gmscompat.lib
      package:com.google.android.gm
      package:com.google.android.overlay.pixelconfigcommon
      package:com.android.internal.display.cutout.emulation.double
      package:com.android.pixeldisplayservice.auto_generated_rro_product__
      package:com.google.ar.lens
      package:com.google.android.overlay.pixelconfig2021
      package:com.android.internal.display.cutout.emulation.avoidAppsInCutout
      package:com.google.android.overlay.pixelconfig2018
      package:com.google.android.pixelnfc
      package:com.google.earth
      package:com.android.internal.display.cutout.emulation.noCutout
      package:com.android.settings.SettingsGoogleSyntheticOverlay
      package:app.grapheneos.gmscompat
      package:com.google.android.overlay.permissioncontroller.safetycenter
      package:com.android.internal.display.cutout.emulation.hole
      package:com.google.android.systemui.overlay.glanceablehubconfig
      package:com.android.internal.display.cutout.emulation.tall
      package:com.google.android.projection.gearhead
      package:com.android.settings.SettingsIntelligenceGoogleSyntheticOverlay
      package:com.android.systemui.SystemUIGoogleSyntheticOverlay
      package:com.google.android.overlay.trafficlightfaceoverlay
      package:com.google.android.apps.maps
      package:com.google.android.apps.camera.services
      package:com.google.android.wifi.resources.pixel
      package:com.google.android.inputmethod.latin
      package:com.google.android.overlay.udfpsoverlay
      package:com.google.android.systemui.gxoverlay
      package:com.google.android.GoogleCamera
      package:com.google.android.apps.docs
      package:com.google.android.systemui.overlay.pixelbatteryhealthconfig
      package:com.google.android.connectivity.resources.overlay
      package:com.android.internal.display.cutout.emulation.waterfall
      package:com.google.android.nfc.overlay
      package:com.google.android.apps.docs.editors.docs
      package:com.google.android.googlequicksearchbox
      package:com.google.android.documentsui.theme.pixel
      package:com.google.euiccpixel.permissions
      package:com.google.android.nfc.overlay.common
      package:com.google.android.markup
      package:com.android.internal.display.cutout.emulation.narrow
      package:com.android.internal.display.cutout.emulation.corner
      package:com.google.android.gms
      package:com.google.android.haptics.overlay.tegu
      package:com.android.pixeldisplayservice
      package:com.android.internal.display.cutout.emulation.wide
      package:com.google.pixel.camera.services
      package:com.google.android.euicc
      package:com.google.android.systemui.overlay.pixelbatterylotxconfig
      package:com.google.android.iwlan
      package:com.google.android.apps.adm
      package:com.google.android.overlay.glanceablehubsettings2022
      ```
      Record the exact output here, verbatim, even if empty: `___`
      If any GMS-related package **is** present, also record its enabled/disabled state
      (`adb shell dumpsys package <package> | grep -i enabled`) — "absent" and "present but
      disabled" are different conditions and change how strong this session's result is. Do not
      assume "disabled in Settings" means "absent" — check.    
      Play services was disabled!
      
      ```bash
      $ adb shell pm list packages | grep -i -P "(gms|play)"
      package:app.grapheneos.gmscompat.config
      package:app.grapheneos.gmscompat.lib
      package:com.android.internal.display.cutout.emulation.double
      package:com.android.pixeldisplayservice.auto_generated_rro_product__
      package:com.android.internal.display.cutout.emulation.avoidAppsInCutout
      package:com.android.internal.display.cutout.emulation.noCutout
      package:app.grapheneos.gmscompat
      package:com.android.internal.display.cutout.emulation.hole
      package:com.android.internal.display.cutout.emulation.tall
      package:com.android.internal.display.cutout.emulation.waterfall
      package:com.android.internal.display.cutout.emulation.narrow
      package:com.android.internal.display.cutout.emulation.corner
      package:com.google.android.gms
      package:com.android.pixeldisplayservice
      package:com.android.internal.display.cutout.emulation.wide

      $ for PACKAGE in $(adb shell pm list packages | grep -i -P "(gms|play)" | sed 's/package://'); do echo $PACKAGE; adb shell dumpsys package $PACKAGE | grep -i enabled; done
      app.grapheneos.gmscompat.config
          User 0: ceDataInode=2228 deDataInode=726 pccCeDataInode=0 pccDeDataInode=0 installed=true hidden=false suspended=false appLockEnabled=false distractionFlags=0 stopped=false notLaunched=false enabled=0 instant=false virtual=false quarantined=false
          User 0: ceDataInode=0 deDataInode=0 pccCeDataInode=0 pccDeDataInode=0 installed=true hidden=false suspended=false appLockEnabled=false distractionFlags=0 stopped=false notLaunched=false enabled=0 instant=false virtual=false quarantined=false
      app.grapheneos.gmscompat.lib
          User 0: ceDataInode=2817 deDataInode=1182 pccCeDataInode=0 pccDeDataInode=0 installed=true hidden=false suspended=false appLockEnabled=false distractionFlags=0 stopped=false notLaunched=false enabled=0 instant=false virtual=false quarantined=false
      com.android.internal.display.cutout.emulation.double
          User 0: ceDataInode=2212 deDataInode=706 pccCeDataInode=0 pccDeDataInode=0 installed=true hidden=false suspended=false appLockEnabled=false distractionFlags=0 stopped=false notLaunched=false enabled=0 instant=false virtual=false quarantined=false
      com.android.pixeldisplayservice.auto_generated_rro_product__
          User 0: ceDataInode=2288 deDataInode=822 pccCeDataInode=0 pccDeDataInode=0 installed=true hidden=false suspended=false appLockEnabled=false distractionFlags=0 stopped=false notLaunched=false enabled=0 instant=false virtual=false quarantined=false
      com.android.internal.display.cutout.emulation.avoidAppsInCutout
          User 0: ceDataInode=2980 deDataInode=1406 pccCeDataInode=0 pccDeDataInode=0 installed=true hidden=false suspended=false appLockEnabled=false distractionFlags=0 stopped=false notLaunched=false enabled=0 instant=false virtual=false quarantined=false
      com.android.internal.display.cutout.emulation.noCutout
          User 0: ceDataInode=2190 deDataInode=676 pccCeDataInode=0 pccDeDataInode=0 installed=true hidden=false suspended=false appLockEnabled=false distractionFlags=0 stopped=false notLaunched=false enabled=0 instant=false virtual=false quarantined=false
      app.grapheneos.gmscompat
          User 0: ceDataInode=3016 deDataInode=1461 pccCeDataInode=0 pccDeDataInode=0 installed=true hidden=false suspended=false appLockEnabled=false distractionFlags=0 stopped=false notLaunched=false enabled=0 instant=false virtual=false quarantined=false
      com.android.internal.display.cutout.emulation.hole
          User 0: ceDataInode=2396 deDataInode=940 pccCeDataInode=0 pccDeDataInode=0 installed=true hidden=false suspended=false appLockEnabled=false distractionFlags=0 stopped=false notLaunched=false enabled=0 instant=false virtual=false quarantined=false
      com.android.internal.display.cutout.emulation.tall
          User 0: ceDataInode=2399 deDataInode=945 pccCeDataInode=0 pccDeDataInode=0 installed=true hidden=false suspended=false appLockEnabled=false distractionFlags=0 stopped=false notLaunched=false enabled=0 instant=false virtual=false quarantined=false
      com.android.internal.display.cutout.emulation.waterfall
          User 0: ceDataInode=2904 deDataInode=1289 pccCeDataInode=0 pccDeDataInode=0 installed=true hidden=false suspended=false appLockEnabled=false distractionFlags=0 stopped=false notLaunched=false enabled=0 instant=false virtual=false quarantined=false
      com.android.internal.display.cutout.emulation.narrow
          User 0: ceDataInode=2255 deDataInode=771 pccCeDataInode=0 pccDeDataInode=0 installed=true hidden=false suspended=false appLockEnabled=false distractionFlags=0 stopped=false notLaunched=false enabled=0 instant=false virtual=false quarantined=false
      com.android.internal.display.cutout.emulation.corner
          User 0: ceDataInode=2202 deDataInode=691 pccCeDataInode=0 pccDeDataInode=0 installed=true hidden=false suspended=false appLockEnabled=false distractionFlags=0 stopped=false notLaunched=false enabled=0 instant=false virtual=false quarantined=false
      com.google.android.gms
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
            com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE:
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "android.app.action.DEVICE_ADMIN_ENABLED"
            android.app.action.DEVICE_ADMIN_ENABLED:
                Action: "android.app.action.DEVICE_ADMIN_ENABLED"
                Action: "android.app.action.DEVICE_ADMIN_ENABLED"
                Action: "android.app.action.DEVICE_ADMIN_ENABLED"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
            com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE:
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
                Action: "com.google.android.gms.auth.proximity.ACTION_FEATURE_ENABLED_CHANGE"
                Action: "com.google.android.gms.backup.ACTION_BACKUP_ENABLED_ON_WEARABLE"
            android.permission.CHANGE_COMPONENT_ENABLED_STATE
            android.permission.PROVIDE_DEFAULT_ENABLED_CREDENTIAL_SERVICE
          User 0: ceDataInode=17180 deDataInode=17183 pccCeDataInode=0 pccDeDataInode=0 installed=true hidden=false suspended=false appLockEnabled=false distractionFlags=0 stopped=false notLaunched=false enabled=3 instant=false virtual=false quarantined=false
            enabledComponents:
      com.android.pixeldisplayservice
          User 0: ceDataInode=2989 deDataInode=1421 pccCeDataInode=0 pccDeDataInode=0 installed=true hidden=false suspended=false appLockEnabled=false distractionFlags=0 stopped=false notLaunched=false enabled=0 instant=false virtual=false quarantined=false
      com.android.internal.display.cutout.emulation.wide
          User 0: ceDataInode=2402 deDataInode=950 pccCeDataInode=0 pccDeDataInode=0 installed=true hidden=false suspended=false appLockEnabled=false distractionFlags=0 stopped=false notLaunched=false enabled=0 instant=false virtual=false quarantined=false
      ```

- [x] **Confirm the Pixel Buds Companion App is not installed:**
      ```
      $ adb shell pm list packages | grep -i pixelbuds
      $ # no output
      ```
      Expected: no output.
- [x] **Confirm nRF Connect (or any other BLE/GATT tool) is not installed/will not be used** this
      session — system Bluetooth settings only, same constraint as Group S.
- [x] **If the Buds are currently bonded to this phone** (e.g. left over from `CAP-034`), "Forget"
      them via Settings → Connected devices → the Buds → Forget — a narrow, per-device action.
      **Do not use Settings → System → Reset options → "Reset Wi-Fi, mobile & Bluetooth"** — that
      wipes every other paired device and all Wi-Fi networks on this phone for no benefit this
      session needs (this Group isn't chasing a GATT-cache question the way Group W was, so Group
      W's heavier cache-busting doesn't apply here). A plain per-device Forget gets a clean
      fresh-pairing handshake as a bonus `PAIR-001` data point; it is not strictly required for the
      DLCI-content question itself (`CAP-010-FINDINGS.md` §5 already showed DLCI 0x08's handshake
      reproduces on a reconnect to an already-bonded device too) — if you'd rather skip this step
      and capture a reconnect-only session instead, that's a valid variant, just record which one
      you did.
- [x] Bluetooth HCI snoop logging enabled (Developer options), then **reboot** the Pixel 9a
      (recommended default per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §2 step 5 — more reliable than a
      plain toggle).
- [x] USB debugging enabled, `adb devices` shows the 9a as `device` (not `unauthorized`/`offline`).
- [x] A way to note wall-clock timestamps of your own actions during the session (§1.3).
- [x] Confirm the Buds are currently **not** bonded to the 9a on screen (Settings → Connected
      devices) before starting, if step 4 above was done — if they still show up bonded, the
      Forget didn't take; re-check before proceeding.

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AB)

1. Start video recording (wall-clock overlay visible) and HCI snoop logging.
2. Open the Buds case, press the pairing button. **[`PAIR-001`]**
3. On the phone: Settings → Connected devices → Pair new device → select the Buds from the list.
   Confirm the system pairing dialog. Note the exact tap time. **[`PAIR-001`, `GSND-001`]**
4. Once connected, **do not open any app or tool** — leave the connection idle for at least
   **90–120 seconds**. This is deliberately longer than Group S's window: DLCI 0x06/0x0a's only
   prior payload-bearing occurrence (`CAP-021-FINDINGS.md` §4a) did not appear immediately on
   connect, so a short window risks a false negative for those two channels specifically.
   **[`GSND-001`, `BATT-003`]**
5. Disconnect and reconnect once, as an isolated pair of actions — the one condition under which
   DLCI 0x0a has ever carried content before. **[`PAIR-003`, `GSND-001`]**
6. Idle again for ~30–60s after the reconnect settles.
7. Stop video recording and HCI snoop logging. Keep the session short — don't let it run long
   enough to risk on-device log rotation (§2's note).
8. Extract via `adb bugreport` (§3) — check for the raw `btsnoop_hci.log` path first, before
   falling back to `btsnooz.py`.

## Event Timeline

*(Fill in after reviewing the video frame-by-frame against its wall-clock overlay, cross-checked
against `CAP-035-btsnoop_hci.log` via `tshark` — per `AGENTS.md` §13. Add a row per distinct
action/event; don't compress a burst into one row without at least noting its start/end frame
numbers.)*

**Corrected 2026-09-02, per this session's own Step-0 verification (see `CAP-035-FINDINGS.md` §1):
video 1 and video 2 are sequential, with only a ~7s recording-stop/restart gap — NOT a ~5-minute
gap as originally guessed, and NOT a "redo" of the sequence.** Video 1 (67.69s, 06:50:53–~06:52:01)
stopped recording while the "Koppelen met Pixel Buds Pro 2 van Ted?" dialog was already showing,
untapped. Video 2 (317.57s, 06:52:04–~06:57:22) resumed ~3s later showing the *same*
still-untapped dialog, then captured the real tap and everything after — confirmed by comparing
video 1's last frame and video 2's first two frames directly (byte-for-byte identical on-screen
dialog state) and by cross-referencing every claimed action against the wire log's own timestamps.
**Video 1's original "idle window / disconnect / reconnect" rows never happened on video 1** — they
are removed below; every real event in this timeline comes from video 2 and the wire log, not from
video 1's guessed rows or video 2's own original (unverified, and as it turned out, inaccurate by
~90–100s in places) time estimates.

| Time (local) | Action / Event | Initiator | Test-ID | Evidence in `CAP-035-btsnoop_hci.log` |
|---|---|---|---|---|
| `06:50:53` | Start video 1 recording | — | — | — |
| `06:51:08` | Verify Developer Options: Bluetooth HCI snoop logging is ON | User (OS) | — | — |
| `06:51:25.30` | Tap "Vergeten" (Forget) — video shows the Buds' "Apparaatgegevens" screen at 06:51:25 | User (OS) | Pre-condition | frame 176 (`Sent Delete Stored Link Key`) |
| `06:51:40` | Buds no longer listed as "Opgeslagen" in the Bluetooth panel — Forget confirmed complete | User (OS) | Pre-condition | — |
| — | **Not shown on camera in either video** — no Settings→Apps→Google Play Services (or any GMS app-info) screen appears anywhere in video 1 (checked exhaustively, 5s-interval contact sheet, full 67.69s) or at any of 10 sampled points across video 2's 317.57s. **Since independently confirmed off-camera via `dumpsys`** (added to the Preparation checklist above): `com.google.android.gms` shows `enabled=3` (`COMPONENT_ENABLED_STATE_DISABLED_USER`) — a verified disabled state, not merely a handwritten claim. See `CAP-035-FINDINGS.md` §2. | User (OS) | Pre-condition | — |
| ~`06:51:50`–`06:51:53` | Buds appear in scan list; tap to connect | User (OS) | `PAIR-001`, `GSND-001` | frame 552 (`LE Enhanced Connection Complete`, 06:51:53.610, handle `0x0040`) |
| `06:52:00` | "Koppelen met Pixel Buds Pro 2 van Ted?" dialog appears on screen — **left unconfirmed**; video 1 stops recording ~1s later while it is still showing | User (OS) | `PAIR-001` | — |
| `06:52:00.69` (approx.) | **End video 1 recording** (67.69s duration) | — | — | — |
| — | *(~7s gap — video 1 stopped, video 2 started; dialog remains on screen, untouched, throughout)* | — | — | — |
| **`06:52:04`** | **Start video 2 recording.** Same still-unconfirmed pairing dialog visible (confirmed identical to video 1's last frame). | — | — | — |
| `06:52:26.42` | Background: 2nd LE connection attempt (`0x0041`) while the dialog still sits unconfirmed; SMP pairing begins | OS (Auto) | — | frame 688 (`LE Enhanced Connection Complete`) |
| `06:52:36.8`–`06:52:37.5` | **Real tap on "Koppelen"** (video-confirmed, `f_33s.jpg`, finger visible mid-tap at 06:52:37) → DHKey Check → classic `Create Connection` → **Connect Complete** → Link Key Request/Reply | User (OS) → OS (Auto) | `PAIR-001`, `GSND-001` | frames 739/740 (DHKey Check), 750 (Create Connection), 792 (Connect Complete, 06:52:36.814), 823/831 (Link Key Request/Reply) |
| `06:52:37.49`–`06:52:37.88` | DLCI 0x08 ("GSND CONTROL") **and** DLCI 0x0a ("GSND AUDIO") both open (SABM/UA); DLCI 0x08 exchanges its full handshake burst (`google-pixel-buds-pro-v1`, `Europe/Amsterdam`, `release_5.203`); DLCI 0x0a carries **zero** payload | App/OS (Auto) | `GSND-001` | frames 1085–1249 (see `CAP-035-FINDINGS.md` §3 for the full byte-level breakdown) |
| `06:52:41.68` | 2nd LE link (`0x0041`) disconnects — connection fully settled | OS (Auto) | — | frame 1303 |
| `06:52:41` – `06:55:48` | **Idle observation window #1** (≈3m7s, no app/tool touched) — DLCI 0x08/0x0a close on their own (phone-initiated) partway through, at 06:54:01.77 | — | `GSND-001`, `BATT-003` | frames 1303–1452; DLCI close at 1407/1408 (Sent DISC)/1411/1412 (Rcvd UA) |
| `06:55:48.26` | **Disconnect** (manual, video-confirmed screen-wake at `f_224s.jpg`) | User (OS) | `PAIR-003` | frame 1452 (`Disconnection Complete`, chandle `0x000b`) |
| `06:55:58.56`–`06:55:59.93` | **Reconnect** (manual tap on "Verbinden", video-confirmed at `f_236s.jpg`) → Connect Complete → Link Key Request/Reply (reused key, no fresh SMP) | User (OS) → OS (Auto) | `PAIR-003`, `GSND-001` | frames 1453 (Create Connection), 1455 (Connect Complete), 1498/1502 (Link Key Request/Reply) |
| `06:56:00.36`–`06:56:00.75` | DLCI 0x08 **and** DLCI 0x0a both reopen; DLCI 0x08 exchanges the same handshake content again; DLCI 0x0a again carries **zero** payload | App/OS (Auto) | `GSND-001` | frames 1692–1820 |
| `06:56:01` – `06:57:20` | **Idle observation window #2** (≈1m19s) — DLCI 0x08/0x0a close again (phone-initiated) at 06:57:20.61 | — | `GSND-001`, `BATT-003` | frames 1820–1909; DLCI close at 1909/1910 (Sent DISC)/1913/1914 (Rcvd UA) |
| ~`06:57:21`–`06:57:24` | **End video 2 recording** (317.57s duration, video-confirmed near-identical still-connected screen at `f_313s.jpg`, 06:57:17) | — | — | — |
| `06:57:41.83` | Final classic disconnect — last frame in the log (post-recording session teardown) | OS (Auto) | — | frame 1925 (`Disconnection Complete`) |

**DLCI 0x06 ("DEBUG APP") and DLCI 0x12 ("BTIS") never open at all, anywhere in this session** —
confirmed via a full-file DLCI census (`tshark -Y btrfcomm -T fields -e btrfcomm.dlci | sort | uniq -c`
→ only `0x00`, `0x08`, `0x0a`, `0x0c` appear; `0x0c` is ordinary HFP/Handsfree per
`CAP-033-FINDINGS.md` §3, ordinary and unrelated to this Group's target channels).

## Decode / Analysis

*(Fill in after capture — isolate the Buds' own `chandle`/RFCOMM traffic first per `AGENTS.md` §13's
CLI-hygiene rule. Then check each of the four named channels — command + raw hex per finding, per
`PROJECT_RULES.md` §1 rule 4a.)*

```
tshark -r CAP-035-btsnoop_hci.log -Y "bluetooth.addr == 04:00:6e:cf:6e:07 and btrfcomm.dlci in {6,8,10,18}" \
  -T fields -e frame.number -e frame.time_relative -e btrfcomm.dlci -e _ws.col.Info
```
(DLCI values: 0x06=6, 0x08=8, 0x0a=10, 0x12=18 — decimal, since `tshark` field comparisons are
numeric.)

For DLCI 0x08 specifically, if it opens, reproduce `CAP-001`/`CAP-004`'s exact content check:
```
tshark -r CAP-035-btsnoop_hci.log -Y 'frame contains "google-pixel-buds-pro-v1"' -T fields -e frame.number -e frame.time
```

**Three-way outcome, stated in advance so the result isn't read selectively (per
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AB):**
- DLCI 0x08's content appears, byte-identical to prior captures, with no GMS present at all →
  strengthens `CAP-004`'s single data point into a 2nd, stronger confirmation of OS/vendor-stack-level
  (not GMS/Nearby-dependent) behavior for this channel.
- DLCI 0x08's content is absent or differs → contradicts `CAP-004`, a new open question in its own
  right, not a "failed" session.
- DLCI 0x0a/0x06/0x12 show any payload at all (previously unobserved outside `CAP-021`'s single
  DLCI 0x0a burst) → first data point on whether these depend on GMS presence.

Any promotion into `PROTOCOL.md`/`DECISIONS.md` still requires explicit maintainer sign-off
(`AGENTS.md` §6) — write findings as a proposal in `CAP-035-FINDINGS.md`, do not edit those files
directly.

## Open Questions

- 🔴 *(carry forward any that remain unresolved after this session)*

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-035-2026-09-02_06-50-53_06-57-24-Group_AB/CAP-035-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-035-2026-09-02_06-50-53_06-57-24-Group_AB/CAP-035-EVENT-NOTES
