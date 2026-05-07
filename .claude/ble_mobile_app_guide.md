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
- `0x01` **HasSavedSession**: Payload = `[0x01, battery_level_u8, session_uuid_16B_LE, has_measurements_u8_bool]`. Active session stored on device (device was turned off and on).
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

### C. `StartSync` (OpCode `0x12`)

**Payload:** Single byte `0x12`.
**Context:** Initiate the **manual file-sync** flow on the `maunal-sync` firmware branch.
Stops the session if running. **This is distinct from the auto-streaming on the Sync
characteristic that fires during reconnect of an active mobile session (§9)** — those
do not require `StartSync`.

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

### D. `NewSessionConfig` (OpCode `0x13`)

**Payload (Mobile):** `0x13` + `16B_UUID` + `2B_interval_seconds(u16)` + `0x01`
Mobile sessions do **not** include `session_token` — the field is absent from the payload entirely.

**Payload (Fixed):** `0x13` + `16B_UUID` + `2B_interval_seconds(u16)` + `0x00` + `1B_pm1_index` + `1B_pm2_5_index` + `16B_session_token` + `32B_WiFi_SSID` + `64B_WiFi_Password`
Total: 134 bytes. Strings are null-byte padded to their container lengths. **Byte 19 is the mode byte (0x00=FIXED, 0x01=MOBILE)** — the firmware reads this to distinguish session types, so the order matters. The `session_token` (16 bytes) comes AFTER the indices, not immediately after the UUID.

**Interval per session type:**
- Mobile: `interval_seconds = 1` (1 measurement per second)
- Fixed: `interval_seconds = 60` (1 measurement per minute)

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
