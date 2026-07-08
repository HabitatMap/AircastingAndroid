Compile Kotlin sources only. Do not mind local.properties or google-services.json missing

# Airbeam Mini V2 Firmware: Android App Integration Guide

This document outlines how BLE communication operates in the new Airbeam Mini firmware and how the Android app should integrate it. It serves as a comprehensive reference for implementation.

---

## If something is unclear from the guide, fetch and reference the firmware code to find relevant information:
https://github.com/HabitatMap/AirbeamMiniFirmware/tree/wifi-session

The manual file-sync flow (`StartSync (0x12)`) lives on a separate branch:
https://github.com/HabitatMap/AirbeamMiniFirmware/tree/maunal-sync

## 0. Key Differences from Old (V1) Firmware

| Aspect | Old Firmware (V1) | New Firmware (V2) |
| ------ | ----------------- | ----------------- |
| **BLE Name** | `airbeammini` | `airbeammini` (same name) |
| **Service UUID** | `0000ffdd-0000-1000-8000-00805f9b34fb` | `a0e1f000-0001-4b3c-8e9a-1f2d3c4b5a60` |
| **Protocol** | `0xFE/0xFF`-wrapped ASCII hex messages via single config characteristic | Binary little-endian opcodes via dedicated Command characteristic |
| **Characteristics** | Separate per-sensor (PM1, PM2.5, battery), config, SD card download | 5 purpose-based: Status, Command, Response, Measurement, Sync |
| **Auth** | UUID + auth token sent after connection | No auth. Only UUID sent as part of session config |
| **Measurement format** | Semicolon-delimited ASCII string (parsed by `ResponseParser`) | Binary: `[count_u8, timestamp_u32_LE, pm1_u16_LE, pm2_5_u16_LE]` |
| **Battery level** | Separate BLE characteristic (`0000ffe7`) | Embedded in Status notification byte |
| **Sync** | SD card CSV file download via dedicated characteristics | Binary records streamed via Sync characteristic (indicate) |
| **Session config** | Multiple sequential messages (location, time, mode) | Single binary command `NewSessionConfig (0x13)` |
| **Time sync** | Date string in `dd/MM/yy-HH:mm:ss` format | Unix epoch i64, sent every hour via `SetTime (0x15)` |
| **Reconnection** | Reconfigure mobile session from scratch | `ContinueSession (0x10)` for mobile; Running state auto-streams. Fixed sessions auto-resume on the firmware side (BLE setup timeout + saved fixed session → WiFi reconnect, no app needed). |
| **Fixed-session offline buffering** | N/A (no fixed-session support) | Measurements persisted to littlefs when WiFi is down; replayed via WiFi POST when connectivity returns (firmware-only, no app involvement on current firmware `d15eff2`+). |

### Backward Compatibility

The old V1 firmware implementation (`AirBeamMiniConfigurator`, `SyncableAirBeamConfigurator`, `HexMessagesBuilder`, etc.) must remain fully intact and operational. V2 is a parallel code path.

---

## 1. Device Detection (Old vs New Firmware)

Both old and new firmware devices advertise as `"airbeammini"`. The app must distinguish them by the **advertised service UUID** in the BLE scan result.

- **Old firmware** advertises service UUID: `0000ffdd-0000-1000-8000-00805f9b34fb`
- **New firmware** advertises service UUID: `a0e1f000-0001-4b3c-8e9a-1f2d3c4b5a60`

### Android Implementation

During BLE scan, check `ScanResult.scanRecord.serviceUuids`:
- If it contains `a0e1f000-0001-...` → V2 firmware → route to `AirBeamMiniV2Configurator`
- If it contains `0000ffdd-...` (or no match) → V1 firmware → route to existing `AirBeamMiniConfigurator`

Store a firmware version indicator on `DeviceItem` so the factory layer (`SyncableAirBeamConfiguratorFactory`, `AirBeamConnectorFactory`) can route correctly.

### Existing files to modify
- **`DeviceItem.kt`** — Add a firmware version field (e.g., `enum FirmwareVersion { V1, V2 }`)
- **`AirBeamDiscoveryService.kt`** (or scan callback) — Extract advertised service UUID from `ScanResult` and pass to `DeviceItem`
- **`SyncableAirBeamConfiguratorFactory.kt`** — Add V2 branch in `create()`
- **`AirBeamConnectorFactory.kt`** — Route V2 devices appropriately

---

## 2. BLE GATT Infrastructure (V2)

The device acts as a peripheral BLE GATT Server.

**Service UUID:** `a0e1f000-0001-4b3c-8e9a-1f2d3c4b5a60`

**Characteristics:**

| Name            | UUID | Permissions | Description |
|-----------------| ---- | ----------- | ----------- |
| **Status**      | `a0e1f000-0002-4b3c-8e9a-1f2d3c4b5a60` | Notify | Device sends its state (Idle, Running, HasSavedSession) + battery level. |
| **Command**     | `a0e1f000-0003-4b3c-8e9a-1f2d3c4b5a60` | Write | App writes binary `AppCommand`s (little-endian byte streams). |
| **Response**    | `a0e1f000-0004-4b3c-8e9a-1f2d3c4b5a60` | Notify | Device sends replies: Ack, Nack, Ready, SensorInfo, SyncInfo. |
| **Measurement** | `a0e1f000-0005-4b3c-8e9a-1f2d3c4b5a60` | Indicate | Live measurement stream during active session. |
| **Active Sync** | `a0e1f000-0006-4b3c-8e9a-1f2d3c4b5a60` | Indicate | Device streams historical (stored) measurements automatically after reconnection. Not manually triggered. |

### Connection Flow (No Auth)

1. Connect to device via BLE
2. Discover service `a0e1f000-0001-...` in `isRequiredServiceSupported()`
3. Subscribe to Status (notify), Response (notify), Measurement (indicate), Sync (indicate)
4. Wait ~300ms for device to settle
5. Device automatically sends Status notification with current state + battery level
6. App reads Status and decides next action (no auth handshake needed)

---

## 3. Status Notifications (`Status` Characteristic)

On connection (after ~300ms delay), the device sends a state notification. The app uses this to understand the device context.

- `0x00` **Idle**: Payload = `[0x00, battery_level_u8]`. No ongoing session.
- `0x01` **HasSavedSession**: Payload = `[0x01, battery_level_u8, session_uuid_16B_LE, has_measurements_u8_bool, file_size_u64_LE (8B)]` (27 bytes; FW commit `3990cf22`). `file_size` is the byte length of the unsynced measurements file on the device, fed to `V2BleSyncOrchestrator.estimateSyncSeconds()` so the "sync before new session" dialog can show an ETA before the user starts. Falls back to 0 on metadata failure or when `has_measurements` is false. Older firmware emits the original 19-byte payload — the Android parser short-reads `file_size` as 0 in that case and the ETA hint is omitted.
- `0x02` **Running**: Payload = `[0x02, battery_level_u8, session_uuid_16B_LE]`. Session actively running.
- `0x03` **ReadyToSync**: Payload = `[0x03, file_size_u64_LE (8B), utf8_password_bytes...]` (FW commit `ed751b180`). Emitted while `StartSync (0x12)` is in progress and the firmware has opened the SoftAP "AirBeam Mini Sync". `file_size` is the byte length of the upcoming `/sync` HTTP body — the app uses it to drive a 0..100% progress UI shared across all manual-sync entry points. Password is variable-length UTF-8, no terminator. **Important:** this status has no battery byte at offset 1 — parsers must short-circuit before generic battery decoding.

### Battery Level

Battery level byte is a **signed `i8`** in firmware, transmitted as `u8` via two's complement:
- **Positive value** → charging (e.g. `54` = 54% and charging)
- **Negative value** → discharging (e.g. `-54` → transmitted as `202u8`, means 54% discharging)

Mobile app must read the byte as signed (`bytes[1].toInt()` in Kotlin, which sign-extends), then use `abs()` for the percentage level and check the sign for charging direction.

It arrives:
- In every Status notification (all states)
- Updated with each live measurement sent (Status is re-notified alongside Measurement indications)

This replaces the old separate battery characteristic (`0000ffe7`).

### App Behavior per Status

| Status | Mobile Session | Fixed Session |
| ------ | -------------- | ------------- |
| **Idle** | Start new session via `NewSessionConfig` | Start new session via `NewSessionConfig` |
| **HasSavedSession** | Send `ContinueSession (0x10)` — device transitions to Running and streams both sync + live data | N/A (fixed sessions don't reconnect this way) |
| **Running** | Sync + live data flow automatically (interleaved). No command needed. | Just subscribe — measurements flow automatically |

**Note:** Sync data (stored measurements) streams automatically on the Sync characteristic — there is no manual trigger. For `HasSavedSession`, sending `ContinueSession` activates the device and starts both sync and live data. For `Running` (e.g., phone went out of range), both flows start automatically on reconnection.

---

## 4. Responses (`Response` Characteristic)

All replies to app commands arrive as notification bytes on the Response characteristic.

- `0x20` **Ack**: Command understood. Wait for further replies (like `Ready`) if applicable.
- `0x21` **Nack**: Command rejected. Next byte = Error Code:
  - `0x01`: NoSession
  - `0x02`: InvalidConfig (generic config failure; for fixed sessions also fires if the **first** measurement POST fails after the session is freshly configured — firmware signals this and then stops, expecting the app to reconfigure)
  - `0x03`: StorageHasMeasurements
  - `0x04`: ClearStorageFailed / SyncStorageFailed
  - `0x05`: **InvalidWifiCredentials** — sent when `NewSessionConfig` WiFi connect fails because the credentials themselves are wrong (distinct from `0x02`). App should prompt user to re-enter SSID/password.
  - `0x06`: **SyncFailed** — emitted by the BLE manual sync flow (`StartBleSync 0x16`) when an indication on the Sync characteristic fails to ACK. Firmware stops the loop; app surfaces a sync-failure dialog and leaves records on the device for a retry.
- `0x22` **Ready**: Procedure complete (e.g., WiFi connected, sync finished, storage cleared). For a running fixed session, firmware also emits `Ready` after **every successful measurement POST** while BLE is connected — i.e. it doubles as a per-measurement heartbeat. The app must treat repeated `Ready` as idempotent: the first one completes the configure flow / kicks off setup work; subsequent ones are heartbeat-only and must not re-trigger setup.
- `0x23` **SensorInfo**: Response to `GetSensors`. Bytes after `0x23` = ASCII string `"PM1,μg/m3;PM2.5,μg/m3"`.
- `0x24` **SyncInfo**: Response to `StartSync`. Bytes after `0x24` = `32B_WiFi_SSID_string` + `64B_WiFi_Password_string` (null-padded).

---

## 5. `AppCommand` Scenarios (`Command` Characteristic)

All numerical values encoded as **Little Endian**.

### A. `ContinueSession` (OpCode `0x10`)

**Payload:** Single byte `0x10`.
**Context:** Resume a saved session after device was turned off and on. **Only needed for mobile sessions.**

- **Has Unsynced Measurements:** `Nack (0x03 StorageHasMeasurements)`. Must sync first.
- **Has Clean Session:** `Ack (0x20)`, resumes running state.
- **No Saved Session:** `Nack (0x01 NoSession)`.

### B. `DiscardSession` (OpCode `0x11`)

**Payload:** Single byte `0x11`.
**Context:** Terminate session, wipe locally stored measurements. Also stops a running session.

- `Ack (0x20)`, then attempts wipe.
  - Success: `Ready (0x22)`.
  - Failure: `Nack (0x04 ClearStorageFailed)`.

**Android wiring:** `AirBeamConnector.onMessageEvent(StopRecordingEvent)` calls `discardSession()` then `disconnect()`. `AirBeamMiniV2Configurator.discardSession()` overrides the interface default and writes `0x11` synchronously (blocking up to 2s on the BLE write callback) so the command lands before `close()` tears down GATT. Required for both mobile and fixed V2 sessions — without it the device stays in `Running` (mobile) or auto-resumes via WiFi on reconnect (fixed, see §6b).

### C. `StartWiFiSync` (OpCode `0x12`) — LEGACY (WiFi-SoftAP path, currently dormant)

**Payload:** Single byte `0x12`.
**Context:** Legacy WiFi-SoftAP manual sync. Renamed from `StartSync` to `StartWiFiSync` in
firmware once the BLE path (§C-BLE below, OpCode `0x16`) shipped. The Android app no longer
sends `0x12` — `V2SyncOrchestrator` + `V2WifiApConnector` + `V2SyncFileDownloader` remain in
the codebase but are not invoked. Documentation kept for reference / future revival.
Stops the session if running. **This is distinct from the auto-streaming on the Sync
characteristic that fires during reconnect of an active mobile session (§9)** — those
do not require `StartWiFiSync`.

Sequence:
1. App writes `0x12` to Command. Device replies `Ack (0x20)` on Response.
2. Firmware opens SoftAP `"AirBeam Mini Sync"` (random WPA2 password) and an HTTP server.
3. Device notifies Status with `ReadyToSync (0x03) + file_size_u64_LE + utf8_password_bytes` (§3).
4. App joins the SoftAP using the password and `GET http://192.168.4.1/sync`.
5. Body is `application/octet-stream` containing the raw measurement file:
   concatenated blocks of `[0xAB, 0xBA, count_u8, count × 8B records, xor_u8]`.
   Each 8-byte record is `ts_u32_LE + pm1_u16_LE + pm25_u16_LE`. Total body length
   matches the BLE-side `file_size`, which the app uses to drive a 0..100% progress UI.
6. When the HTTP transfer completes, firmware emits `Ready (0x22)` on Response and
   automatically clears stored measurements via `storage.clear_measurements()`.

**FW shutdown sequence after `/sync`:** firmware's `wifi_manager::cancel_manual_sync`
sets `SO_LINGER` on the response socket inside `sync_get_handler` (so kernel-side
`close()` blocks on TCP ACK of all queued bytes) and adds a 500 ms grace sleep
between `httpd_stop` and `wifi.stop()`. Together these guarantee TCP FIN/ACK
exchange completes before the SoftAP station is deauthed — phone always sees a
clean EOF on a successful sync. App-side parser therefore treats any IOException
during body read as a real failure (no soft-success path); orchestrator skips
Discard and surfaces an error.

- Failure: `Nack (0x04)` (`SyncStorageFailed`/`ClearStorageFailed`).

**Android client responsibility for fixed-session measurements:** After draining `/sync`,
upload the parsed records to the same backend endpoint the firmware uses —
`POST /api/v3/fixed_sessions/{uuid}/measurements`, `Content-Type: application/octet-stream`,
`Authorization: Bearer <session_token_hex>`. Body matches FW byte layout (BE u32 timestamps,
BE f32 values, magic + count_u16_BE + records + xor). Mobile sessions follow the V1 SD-sync
logic locally instead (skip finished, filter `> lastMeasurementTime`, insert with last-known
location).

**Note:** `SyncInfo (0x24)` is NOT emitted by the manual-sync flow on this branch — the
SoftAP password is delivered via the new `Status::ReadyToSync (0x03)` notification.

### C-BLE. `StartBleSync` (OpCode `0x16`) — current manual-sync path

**Payload:** Single byte `0x16`.
**Context:** BLE-only manual sync. Replaces the WiFi-SoftAP flow on the Android client.
Firmware commits: `e1a03b9415a8a4e70ee24acb824c1361f526d53c` and `c4dd9e62d5df599601d4edf57e5b8f5386440564`.

Sequence:
1. App writes `0x16` to Command. Device replies `Ack (0x20)` on Response.
2. Device notifies Status `ReadyToSync (0x03)` with `file_size_u64_LE` + empty password.
   (Password byte field is present but zero-length on the BLE path — only `file_size` is
   meaningful, and the app uses it to drive the same 0..100% progress UI shared with the
   legacy WiFi path.)
3. Firmware sleeps ~100 ms to let the app finalize subscription state.
4. Firmware streams stored records on the **Sync characteristic** (indicate). Indication
   payload matches the reconnect-time auto-sync format: `[count_u8, 2B padding,
   count × 8B records]` where each record is `ts_u32_LE + pm1_u16_LE + pm25_u16_LE`.
   Up to 30 records per indication (`Vec::with_capacity(30)` on firmware side).
5. When all records have been sent, firmware sends `Ready (0x22)` on Response, then
   auto-clears storage **and** the saved session-config in its main loop's Stop handler.
   **No `DiscardSession` is needed from the app afterwards.**
6. Failure: `Nack (0x06 SyncFailed)` if an indication does not ACK, or
   `Nack (0x04 ClearStorageFailed)` if the post-sync storage wipe fails. Firmware retains
   records on Nack so the app can retry.

**Android wiring:**
- New opcode `OPCODE_START_BLE_SYNC = 0x16` on `AirBeamMiniV2Configurator`.
- New error code `NACK_SYNC_FAILED = 0x06`.
- `AirBeamMiniV2Configurator.parseSyncChunk` routes indications to a registered
  `manualSyncChunkHandler` while a manual BLE sync is in flight — bypasses the default
  reconnect-time DB save path so the orchestrator can route mobile vs fixed records.
- `V2BleSyncOrchestrator` is the new orchestrator wired into `v2StateRepository.startSync`
  and `triggerSDCardDownload()`. No BLE disconnect/reconnect, no SoftAP, no
  `DiscardSession` — the firmware handles cleanup itself.
- `keepConnectedAfter` and `onBeforePicker` params on the orchestrator are no-ops, kept
  only for signature compatibility with the dormant `V2SyncOrchestrator`.

**Progress UI:** firmware emits the ReadyToSync (Status 0x03) notification ~100 ms
*before* the first Sync indication and uses the on-disk LittleFS file size
(`std::fs::metadata(FILE_PATH).len()`, `.unwrap_or(1)` on metadata failure) as
`file_size`. Each on-disk storage block is framed as
`[0xAB, 0xBA, count_u8, count × 8B records, xor_u8]` = `5 + 8 × count` bytes, so the
orchestrator increments `receivedBytes += 5 + 8 × chunk.size` per indication and
computes `pct = receivedBytes * 100 / file_size` (clamped 0..99 mid-stream, set to
100 after Ready 0x22). **Critical**: the file-size collector must run in a parallel
`launch` so `expectedSize` is updated as soon as ReadyToSync lands — `awaiting` it
after `sendStartBleSyncAndAwaitDone()` is too late (that call only resolves on the
post-stream `Ready 0x22`, by which time every chunk has already arrived with
`expectedSize == -1` and progress stays at 0%).

### D. `NewSessionConfig` (OpCode `0x13`)

**Payload (Mobile):** `0x13` + `16B_UUID` + `2B_interval_seconds(u16)` + `0x01`
Mobile sessions do **not** include `session_token` — the field is absent from the payload entirely.

**Payload (Fixed):** `0x13` + `16B_UUID` + `2B_interval_seconds(u16)` + `0x00` + `1B_pm1_index` + `1B_pm2_5_index` + `16B_session_token` + `32B_WiFi_SSID` + `64B_WiFi_Password`
Total: 134 bytes. Strings are null-byte padded to their container lengths. **Byte 19 is the mode byte (0x00=FIXED, 0x01=MOBILE)** — the firmware reads this to distinguish session types, so the order matters. The `session_token` (16 bytes) comes AFTER the indices, not immediately after the UUID.

**Interval per session type:**
- Mobile: `interval_seconds = 1` (1 measurement per second) — default. User-configurable via the "Interval (seconds)" input on the New Session Details screen; integer ≥ 1.
- Fixed: `interval_seconds = 60` (1 measurement per minute) — default. User-configurable via the same input field on the Fixed New Session Details screen; integer ≥ 1.

The plumbing carries `intervalSeconds: Int?` from `SessionDetailsViewMvc.Listener.onSessionDetailsContinueClicked` →
`NewSessionController` → `StartRecordingEvent` → `RecordingHandlerImpl.startRecording` →
`ConfigureSession` event → `AirBeamConnector.configureSession` → `AirBeamBleConfigurator.configure` →
`AirBeamMiniV2Configurator.sendNewSessionConfig` → `buildMobileSessionPayload` / `buildFixedSessionPayload`.
Defaults `DEFAULT_MOBILE_INTERVAL_SECONDS = 1` / `DEFAULT_FIXED_INTERVAL_SECONDS = 60` live on
`AirBeamMiniV2Configurator`'s companion object and apply only when the param is null
(legacy/non-V2 paths).

**Sparse-interval averaging gotcha (`AveragingService`).** Averaging assumes the native
sample rate is finer-grained than the averaging window. When that is violated (a
5-second-native session in the 5s FIRST window, or any session with native ≥ window),
each window holds ≤ 1 sample → `perform`'s `size <= 1` branch leaves `averaging_frequency`
at the native value → `deleteLeftoverMeasurements` re-queries
`getMeasurementsToAverage(streamId, window)` and wipes every remaining row. The card
stays (session row + streams) but `measurements` is empty: graph/map/share/upload all
fail silently.

Fix: persist the native interval per session on a nullable `sessions.measurement_interval`
column (DB v37, `MIGRATION_36_37` adds `INTEGER`). `RecordingHandlerImpl.startRecording`
writes `session.measurementInterval = intervalSeconds` for V2 sessions before insert;
V1/legacy/external rows stay `null` (interpreted as 1s native rate). The gate then
operates per averaging window rather than as an absolute interval threshold:

- `AveragingService.stopAndPerformFinalAveraging` computes the chosen window first,
  then skips `perform` + sweep when `nativeInterval >= currentWindow.value`. So a 1s
  session runs at both FIRST(5s) and SECOND(60s); a 5s session skips FIRST(5s) but still
  runs SECOND(60s); a 10-min session skips both.
- `AveragingService.startPeriodicAveraging` applies the same per-tick gate so a live 5s
  session does not write a misleading `averagingFrequency=5` to its session row before
  it crosses 9 hours.
- `RecordingHandlerImpl.startRecording` schedules periodic averaging unless
  `nativeInterval >= AveragingWindow.SECOND.value` (=60s); for native ≥ 60s no window
  can ever produce real averaging, so scheduling is skipped entirely.

### UUID Byte Encoding (Little-Endian)

All UUIDs in V2 binary payloads use **mixed-endian (LE)** encoding, matching the firmware's `Uuid::from_slice_le()`:
- The first three groups are byte-reversed: time_low (4B), time_mid (2B), time_hi_and_version (2B)
- The last 8 bytes (clock_seq + node) remain in standard order

Example: UUID `"a4a3a2a1-b2b1-c2c1-d1d2-d3d4d5d6d7d8"` encodes as bytes `[a1,a2,a3,a4, b1,b2, c1,c2, d1,d2,d3,d4,d5,d6,d7,d8]`.

**Context:** Start recording a new session.

- **Mobile:** `Ack (0x20)` → `Ready (0x22)` → starts tracking.
- **Fixed:**
  - `Ack (0x20)`.
  - Firmware attempts WiFi connection with provided credentials.
  - Success: `Ready (0x22)` — and then another `Ready` after each subsequent measurement POST while BLE is connected (per-measurement heartbeat).
  - Failure: `Nack (0x02 InvalidConfig)` (generic / first-measurement POST failure) or `Nack (0x05 InvalidWifiCredentials)`.

### E. `GetSensors` (OpCode `0x14`)

**Payload:** Single byte `0x14`.
**Context:** Query which sensor metrics the hardware supports.

- Response: `0x23` + ASCII bytes `"PM1,μg/m3;PM2.5,μg/m3"`.

### F. `SetTime` (OpCode `0x15`)

**Payload:** `0x15` + `8B_unix_epoch_seconds(i64_LE)`.
**Context:** Synchronize firmware's internal RTC.

- Updates internal system time. **No Ack emitted.**
- **Must be sent on connection and repeated every hour.**

---

## 6. Measurement Data Format (Binary)

### Live Measurements (`Measurement` Characteristic — Indicate)

Single measurement, 9 bytes:
```
[count_u8=1, timestamp_u32_LE, pm1_u16_LE, pm2_5_u16_LE]
```

- `count`: Always `1` for live measurements.
- `timestamp`: Unix epoch seconds, `u32` little-endian.
- `pm1`: PM1.0 value in μg/m³, `u16` little-endian.
- `pm2_5`: PM2.5 value in μg/m³, `u16` little-endian.

After each live measurement indication, the device also re-notifies the Status characteristic with `Running` state (updating battery level).

### Historical/Sync Measurements (`Sync` Characteristic — Indicate)

Batched records, up to 244 bytes:
```
[count_u8, padding_2B, record_0(8B), record_1(8B), ...]
```

Each record is 8 bytes:
```
[timestamp_u32_LE, pm1_u16_LE, pm2_5_u16_LE]
```

- `count`: Number of records in this chunk.
- Records start at byte offset 3.

**This replaces the old SD card CSV file download entirely.** The old `SDCardReader`, `SDCardSyncService`, `SDCardCSVFileChecker`, and related classes are **not used** for V2.

### SD-Sync Wizard "Unplug Your AirBeam" Screen

V2 has no SD card → no unplug step needed. The wizard's `UnplugAirBeamFragment` is therefore:
- **Skipped entirely on V2** — `SyncController.onAirbeamSyncedContinueClicked` checks `isV2Sync` and goes straight to the post-sync continuation (TurnOffLocationServices or finish).
- **Moved to AFTER the "successfully synced" screen on V1** — previously shown before device selection; now shown after the user taps Continue on the synced screen, then proceeds to the post-sync continuation.

The V2 flag is plumbed from `AirBeamSyncService` → `SDCardSyncFinished(isV2 = true)` event → `AirbeamSyncingViewMvc.Listener.syncFinished(isV2)` → `SyncController.syncFinished(isV2)`. The V1 post site in `SDCardSyncService` keeps the default `isV2 = false`.

### Mapping to `NewMeasurementEvent`

The V2 binary format does NOT include sensor metadata (package name, thresholds, etc.) like the old ASCII format. The app must construct `NewMeasurementEvent` using:
- Sensor info from `GetSensors` response: `"PM1,μg/m3;PM2.5,μg/m3"`
- Hardcoded thresholds matching the AirBeam Mini sensor profile
- Device ID from the connected `DeviceItem`

The old `ResponseParser` is **not reusable** for V2 — a new binary parser is needed.

---

## 6a. Fixed Session — WiFi-Drop Storage & Replay

When a fixed session is running and WiFi drops (or fails to connect), the
firmware does NOT lose measurements. Storage replay is a **firmware-side
concern handled over WiFi** — the app does not participate.

1. `send_measurement` (fixed path) POSTs to
   `/api/v3/fixed_sessions/{uuid}/measurements` via
   `wifi_manager.send_measurements(...)`.
2. If WiFi is not connected, the record is persisted to littlefs storage
   (`src/main.rs:171-175`).
3. Every main-loop tick (100 ms), if `storage.has_measurements() &&
   wifi_manager.is_connected()`, firmware replays stored records through
   the WiFi POST endpoint via `sync_from_storage` (`src/main.rs:198-204`,
   closure routes FIXED → WiFi, MOBILE → BLE).
4. The WiFi POST response includes an `X-Server-Time` header parsed into
   `LoopEvent::TimeUpdate`, keeping the device clock server-authoritative.
   `SetTime` BLE hourly scheduling remains unused for fixed sessions.
5. `connected()` predicate in the main loop is WiFi for FIXED, BLE for
   MOBILE — so sync loop only fires on the correct transport.

**First-measurement failure signalling (commit `25f94a1`):** On a freshly
started fixed session, if the first measurement send fails AND BLE is
still connected, firmware emits `Nack(0x02 InvalidConfig)` and stops the
loop — a hint to the app that WiFi creds/connectivity are bad.
Suppressed on resumed sessions (see §6b).

---

## 6b. Fixed Session — Firmware-Side Resume (No App Required)

Commit `25f94a1` added autonomous resume for fixed sessions:

- On BLE setup timeout, if a saved FIXED session exists on the device,
  firmware auto-reconnects WiFi and returns `SetupResult::Continue`. The
  session keeps running **without the app**.
- First-measurement WiFi failure signalling is suppressed in this
  resumed path (avoid re-prompting creds on every power-cycle).

### Android Implication

On BLE reconnect to a fixed-session device:
- Status notification may be `Running (0x02)` with a session UUID the
  app already knows — treat as normal continuation.
- Do NOT assume "no session running" just because the app wasn't
  involved in the latest setup. Always trust Status.
- No `ContinueSession (0x10)` is needed for fixed sessions (still mobile-only).

---

## 7. Fixed Session Flow (Backend Integration)

### Step-by-step

1. App sends `GetSensors (0x14)` → receives `"PM1,μg/m3;PM2.5,μg/m3"`
2. App calls backend `POST /api/v3/fixed_sessions`:

**Request:**
```json
{
  "uuid": "<session-uuid>",
  "title": "...",
  "latitude": 40.7128,
  "longitude": -74.006,
  "contribute": true,
  "is_indoor": false,
  "airbeam": {
    "mac_address": "AA:BB:CC:DD:EE:FF",
    "model": "AirBeamMini",
    "name": "..."
  },
  "streams": [
    { "sensor_name": "AirBeamMini-PM1", "unit_symbol": "µg/m³" },
    { "sensor_name": "AirBeamMini-PM2.5", "unit_symbol": "µg/m³" }
  ]
}
```

**Response:**
```json
{
  "location": "http://aircasting.org/s/ab12c",
  "session_token": "a3f2c1d4e5b6a7f8c9d0e1f2a3b4c5d6",
  "streams": [
    { "sensor_name": "AirBeam-PM2.5", "sensor_type_id": 2 }
  ]
}
```

3. `streams[].sensor_type_id` values become `pm1_index` and `pm2_5_index` in the `NewSessionConfig` payload
4. `session_token` is a 16-byte integer stored by the backend and returned as a 32-char hex string. Decode it to 16 raw bytes, then **reverse the byte order to little-endian** before including in the BLE payload — firmware reads it via `u128::from_le_bytes(...)`. The hex string is big-endian (MSB first), so `tokenBytes.reversedArray()` is required.
5. App sends `NewSessionConfig (0x13)` with all the above data + WiFi credentials

---

---

## 8. `SetTime` Periodic Scheduling

`SetTime (0x15)` must be sent:
1. Immediately after connection (once Status is received)
2. Every hour while connected — **only for mobile sessions**. Fixed sessions get time from the backend server: the WiFi POST to `/api/v3/fixed_sessions/{uuid}/measurements` returns an `X-Server-Time` header that firmware parses into an internal `TimeUpdate` event (guarded by a ≥60s delta to avoid clock thrash). App must NOT schedule hourly `SetTime` for fixed sessions.

The app should schedule a repeating timer/coroutine for this. The command does not produce an Ack response.

---

## 8a. Backend Timestamp Convention (Round-Trip)

The AirCasting BE persists session/measurement timestamps using a "local wall-clock numerals treated as UTC" convention, not real UTC. Failing to match it on either parse or upload produces an offset on the dashboard card and graph.

Mechanism on BE (`HabitatMap/AirCasting`):

- `Session#start_time_local` / `end_time_local` use `skip_time_zone_conversion_for_attributes`. `TimeToLocalInUTC.convert` strips the offset on assignment, so the column stores the local wall-clock numerals as-is.
- `Measurement#time` on the V3 binary ingester path is written via `Utils.to_local_as_utc(epoch, session.time_zone)` — same convention.
- `FixedPolling::Serializer` / `Session#as_json` serialize via `iso8601(3)`, appending a literal `"Z"` suffix. The `Z` is misleading: the numerals are the session's local wall clock, not real UTC.

The wall clock is the session's `time_zone` column on BE. For **mapped (outdoor) fixed sessions** BE looks up the TZ from `latitude` / `longitude` — usually ≈ the phone-default TZ in normal use, so phone-default parsing/formatting works end-to-end. For **indoor / locationless fixed sessions** BE has no coordinates, so `session.time_zone` defaults to `UTC`, and the wall-clock numerals BE writes (notably for `Measurement#time` and `Session#end_time` updated from measurement ingest) are UTC. The app must parse/format those timestamps as UTC, not phone-default, or the graph and end-time drift by the phone-TZ offset.

Required app handling (every BE-facing fixed-session timestamp): use phone-default TZ when `is_indoor == false`, UTC when `is_indoor == true`. `DownloadMeasurementsService.beTimeZone(isIndoor)` is the canonical helper.

Affected files:

- `SessionDownloadService` — `start_time` / `end_time` parsing keyed on `sessionResponse.is_indoor`.
- `DownloadMeasurementsService.updateSessionEndTime` — response `end_time` parsing keyed on `dbSession.is_indoor`.
- `DownloadMeasurementsService.saveStreamData` — passes the indoor-aware TZ into `MeasurementsFactory.get`.
- `MeasurementsFactory.get` — accepts a `TimeZone` param (default phone-local) for measurement `time` parsing (V3 fixed-polling response).
- `LastMeasurementTimeStringFactory.get` — accepts a `TimeZone` param so the `since` cursor matches BE's stored numerals.
- `SessionParams` — `start_time` / `end_time` upload formatting uses phone-default TZ (the V1 path; BE strips the offset on assignment regardless of session TZ, so this stays unchanged).
- `GzippedParams` — `Date` type adapter for gzipped JSON payloads (V1 measurement uploads, sync_with_versioning, fixed-session params). Pinning this adapter to UTC silently re-broke the round-trip — leave it at the JVM default.

V2 binary measurement uploads (`V2FixedMeasurementsUploader`, `POST /api/v3/fixed_sessions/{uuid}/measurements`) send `u32` epoch seconds directly; BE applies `to_local_as_utc` itself, so no app-side TZ handling is needed there — but the resulting `Measurement#time` and `end_time_local` BE writes use the session-TZ wall-clock convention (UTC for indoor, geo TZ for mapped), which is why the download/parse side must match.

Mobile sessions are not affected: they never hit the V3 fixed-polling endpoint and are not flagged `is_indoor`.

`SimpleDateFormat` instances for these patterns use `Locale.US`. That is correct and unrelated to user-facing i18n: the locale only affects how pattern symbols (digits, AM/PM) are rendered, and BE expects ASCII digits. Changing this to `Locale.getDefault()` would break parsing on devices with non-Latin numeral locales.

---

## 9. Mobile Session Reconnection (Phase 3)

When the app reconnects to the device during an active mobile session, two scenarios apply:

### Scenario A: Device was Running (phone went out of range)

The device continued recording while disconnected. On BLE reconnect:
1. Status notification = `Running (0x02)`
2. **No command needed** — device automatically streams:
   - Stored measurements on **Sync characteristic** (batched indications)
   - Live measurements on **Measurement characteristic**
   - Both can be interleaved
3. App parses Sync indications and saves each chunk to DB with original timestamps
4. When all stored data is streamed, Status re-notifies as `Running` with `has_measurements=false`

### Scenario B: Device was power-cycled (HasSavedSession)

The device was turned off and back on. On BLE reconnect:
1. Status notification = `HasSavedSession (0x01)` with `has_measurements` flag
2. App sends `ContinueSession (0x10)` — device transitions to Running immediately
3. From here, same as Scenario A: sync + live data flow interleaved
4. App parses and saves sync chunks to DB

### Sync Data Format (Sync Characteristic — Indicate)

Batched records, up to 244 bytes:
```
[count_u8, padding_2B, record_0(8B), record_1(8B), ...]
```
Each 8-byte record: `[timestamp_u32_LE, pm1_u16_LE, pm2_5_u16_LE]`

Each chunk is saved to the DB immediately (not accumulated) since there can be many stored measurements.

### Key Implementation Detail

`StartSync (0x12)` is **NOT** used for mobile reconnection sync. The sync is automatic. `StartSync` may be used for other purposes (e.g., fixed session sync) but is not part of the mobile reconnection flow.

### Session UUID Validation for Sync Data

When sync measurements arrive on the Sync characteristic, **always verify the device's session UUID** (from the last Status notification, stored as `savedSessionUuid` in `AirBeamMiniV2Configurator`) matches the current app session UUID before saving. If they don't match, the sync data is from an older session that the device still had in storage — discard it.

The device UUID arrives in LE-encoded form via `savedSessionUuid: ByteArray?` and must be decoded with `leBytesToUuid()` before comparing to the DB session UUID.

### Live Measurements — Direct DB Save + UI Notification

V2 saves live measurements **directly** to the DB via `saveMeasurementsToSession()` (same path as sync chunks), using the device timestamp from the binary packet and the full device ID from `AirBeamMiniV2Configurator.deviceId`.

`NewMeasurementEvent.deviceId` originally split `sensorPackageName` on `:` and took the last segment, which broke V2 (`"AirBeamMini:AA:BB:CC:DD:EE:FF"` → only `"FF"`). It now uses `substringAfterLast(':')` so the full MAC is extracted (V1/microphone unchanged: `"AirBeamMini:246f28c47698"` → `"246f28c47698"`, `"Builtin"` → `"Builtin"`).

After each direct DB save, V2 also posts a `NewMeasurementEvent` for PM1 and PM2.5 so UI subscribers (`SessionDetailsViewController` graph, `MobileActiveController` loader) refresh on the go. To avoid the standard observer double-saving the same measurement, `RecordingHandlerImpl.startRecording` skips `startObservingNewMeasurements` for V2 mobile sessions — V2 owns its own DB writes; the EventBus is used only for UI notification.

### Finishing a Mobile Session While Stored Measurements Are Still Draining

The dashboard "Finish recording" button (`MobileActiveSessionActionsBottomSheet`) and the `DisconnectedView` secondary button always show the plain `FinishSessionConfirmationDialog` first so the user must confirm intent. When `AirBeamMiniV2StateRepository.hasSavedMeasurements || AirBeamMiniV2StateRepository.isActiveSyncDraining` is true at tap-time, an `onConfirmed` callback is passed to the confirmation dialog; after the user taps "Finish recording" there, the callback dismisses the confirmation and shows `SyncAndFinishV2SessionDialog` (chained, not replacing). Otherwise the dialog completes with the standard `StopRecordingEvent` path.

**Why two flags:** the firmware's `STATE_RUNNING` Status payload is only 18 bytes (`[opcode, battery, uuid_16B]`) — it does **not** include a `has_measurements` byte. So `hasSavedMeasurements` cannot detect mid-drain during an active mobile session; it is only meaningful when the device is in `STATE_HAS_SAVED_SESSION` (pre-`ContinueSession`). To compensate, `AirBeamMiniV2Configurator.parseSyncChunk()` calls `markActiveSyncDraining()` on every Sync (`0006`) indication: it sets `AirBeamMiniV2StateRepository.activeSyncDrainingFlow = true` and (re)schedules an idle-timeout job that flips the flag back to `false` after `SYNC_DRAIN_IDLE_TIMEOUT_MS` (3 s) with no further chunks. This is what reliably represents "drain in progress" during recording.

`SyncAndFinishV2SessionDialog` drives the **BLE manual-sync** flow — the same `V2BleSyncOrchestrator` path used by `SyncBeforeNewV2SessionDialog`. The dialog auto-starts the sync on open so `Status::ReadyToSync (0x03)` (FW commit `ed751b180`) lands with `file_size_u64_LE` ~100 ms later, giving the app a reliable ETA even when the device was in `STATE_RUNNING` (where `savedSessionFileSize` is unreachable — `STATE_RUNNING` payload omits the suffix added by FW commit `3990cf22`).

`AirbeamSyncingFragment` (used during the SD card sync wizard) also displays the estimated sync time after the connection screen is cleared (only for V2/new firmware) when the file size information becomes available (either initially from `savedSessionFileSize` or on status update from `readyToSyncFileSize` Flow). It queries the local DB to check if the session stored on the device was a mobile session with `<1m` interval (in which case it assumes `8.4` bytes per measurement, otherwise `12.0` bytes per measurement). Since each BLE indicate call contains up to 30 measurements, the estimated time is calculated as `indicateCalls * 0.12` seconds, where `indicateCalls = ceil(measurements / 30.0)`.

Flow:

1. Open → render "Syncing measurements" header, "Preparing sync…" button (disabled), "Discard & Finish" cancel button. `isCancelable = false`. Immediately call `v2StateRepository.startSync(keepConnectedAfter = true)` in `ioScope`.
2. `V2BleSyncOrchestrator` writes `StartBleSync (0x16)`. Firmware stops the running session in its Stop handler — no new mobile measurements stream after this point.
3. `readyToSyncFileSize` flow emits → dialog computes `V2BleSyncOrchestrator.estimateSyncSeconds(fileSize)` and rewrites the description with the ETA hint.
4. `syncProgress` flow emits per-chunk percent → action button shows "Syncing… X%" (label-only; disabled).
5. **Discard mid-stream** — user taps "Discard & Finish". Dialog cancels its `syncJob`, which unwinds the orchestrator's `coroutineScope { ... }` and runs its `finally` (clears `manualSyncChunkHandler`, closes the mobile channel, `setSyncInProgress(false)`). For a **mobile** session, chunks that already streamed have been persisted (see §Mobile chunk persistence below), so a discard keeps whatever synced so far — it does not roll everything back. Dialog then posts `StopRecordingEvent`, and `AirBeamConnector.onMessageEvent(StopRecordingEvent)` writes `0x11 DiscardSession` followed by `disconnect()` — firmware honors `0x11` even while a `StartBleSync` stream is in flight and wipes on-device storage.
6. **Sync success** — for a mobile session records were already persisted chunk-by-chunk; the orchestrator just calls `markMobileFinished()` to flip status to FINISHED in the local DB. Dialog renders "Sync complete"; the Done button posts `StopRecordingEvent` (trailing `0x11` is a no-op because FW already auto-cleared storage on its `Ready 0x22` Stop-handler path).
7. **Sync failure** (Nack 0x06 SyncFailed / write failure / timeout) — orchestrator returns `false`, dialog renders "Sync failed". Continue button posts `StopRecordingEvent`; the on-device data is wiped by the `0x11` write. Chunks persisted before the failure remain in the local DB; the firmware retains its copy on Nack, so a retry re-streams and dedupes via the unique measurements index.

### Mobile chunk persistence (data-safety, FW commit `Ready 0x22` auto-wipe)

The firmware auto-clears its flash **the instant it emits `Ready (0x22)`**. Therefore `V2BleSyncOrchestrator` must NOT buffer the whole backlog in memory and insert only after `Ready`: for a long offline session (e.g. 67h @ 1s ≈ 241k records) the terminal insert stalls for minutes behind the `O(measurements × trackedLocations)` `getClosestLocation` scans while the UI already reads "100%", and a hang / OS-kill / force-quit in that window loses everything (device already wiped; the single Room transaction rolls back to zero rows).

Instead, **mobile** manual-sync records stream to the DB continuously, mirroring the reconnect-time auto-sync / live-measurement paths:
- `run()` resolves the target session from `savedSessionUuid` up-front. If it is `MOBILE` (and `deviceId` present), the chunk handler hands each chunk to a `Channel(UNLIMITED)`.
- A single serialized consumer coroutine drains the channel and inserts each chunk via `V2MobileMeasurementsInserter.insert` — one consumer keeps stream creation race-free and preserves arrival order, and spreads the insert work across the transfer instead of one terminal bulk write.
- After `Ready 0x22`, the orchestrator closes the channel, `join()`s the consumer (so every queued chunk commits), then calls `markMobileFinished()` — no terminal bulk insert.
- **Fixed** sessions still buffer into `collected` and upload as one HTTP batch (`V2FixedMeasurementsUploader`); they are not written to the local measurements table, so per-chunk DB save does not apply.

All paths converge on `onFinishMobileSessionConfirmed(session)`, so `SessionManager` cleans up services, navigates to the dormant tab, and the backend sync runs from `RecordingHandler.stopRecording`. V2 manual sync is BLE-only — no Wi-Fi/SoftAP fallback on this flow.

### V2 → V1 Fallback: Nordic Disconnect-Path + Double-Fire Hazard

On an AirBeam Mini with V1 firmware, `AirBeamMiniV2Configurator.isRequiredServiceSupported()` returns false after V2 GATT connect + service discovery. Nordic 2.x surfaces this through **`ConnectionObserver.onDeviceDisconnected(reason=4 = REASON_NOT_SUPPORTED)`** — NOT through `onDeviceFailedToConnect`. The `ConnectRequest.fail { ... }` callback fires later (with `reason=-2`) once the request is finalized.

If `onDeviceDisconnected` runs the standard teardown (`onDisconnected()` → posts `SensorDisconnectedUnexpectedlyEvent`, `mListener?.onDisconnect()` → `AirBeamService.onDisconnect()` → `stopSelf()`, then `disconnect()` → `unregisterFromEventBus`), the foreground `AirBeamRecordSessionService` is killed and `AirBeamConnector` loses its EventBus subscription **before** the trailing `.fail` callback runs. The fallback then starts a V1 connect that succeeds — UI even shows "AirBeam Connected" because `connectionStatusFlow` is still emitting — but `ConfigureSession` events posted on Start Recording have no subscriber, so the V1 device never receives the location/time/mobile-mode commands and the session never starts streaming.

Required guards in `AirBeamMiniFallbackConnector`:

1. `onDeviceDisconnected`: if `isV2Attempt && !connectionEstablished.get()` (V2 attempt in flight, no `onConnectionSuccessful` yet), route through `onFailedCallback(device, reason, fromV2Leg = true)` and `return`. Do NOT call `onDisconnected()` or `disconnect()` — keep the service alive and the EventBus subscription intact.
2. `onDeviceFailedToConnect`: route through `onFailedCallback` too, mapping `fromV2Leg = isV2Attempt`.
3. `connectWith`: capture leg at closure-creation time (`val isV2Leg = isV2Attempt`) and pass it into the `.fail` lambda as `fromV2Leg`. Nordic's V2 ConnectRequest fires `.fail` ~15ms after the observer's `onDeviceDisconnected`, by which point `isV2Attempt` has already flipped to false; reading the shared field at call time would misroute the late V2 `.fail` into the V1-failure branch and post `AirBeamConnectionFailedEvent` mid-V1-connect, triggering `NewSessionController.onBackPressed()` → `ConnectingAirBeamController.onBackPressed` → `DisconnectExternalSensorsEvent` → V1 torn down.
4. `onFailedCallback`: idempotent per leg. `v2FailureHandled` / `v1FailureHandled` flags (reset in `start()`) short-circuit duplicate callbacks for that leg.
5. V2 transition cleanup: `v2Configurator.closeConnection()` + `v2Configurator.reset()` before `connectWith(v1Configurator, …)` so V2's BleManager state (characteristics, jobs, `AirBeamMiniV2StateRepository`) is wiped — previously `reset()` only ran on the now-bypassed `onDisconnected` teardown path.

Together these mean: V2 service-not-supported → silent transition to V1 → V1 success → ConfigureSession reaches V1 → V1 mobile session starts as it did before V2 was added.
