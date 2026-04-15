# Airbeam Mini V2 Firmware: Android App Integration Guide

This document outlines how BLE communication operates in the new Airbeam Mini firmware and how the Android app should integrate it. It serves as a comprehensive reference for implementation.

---

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
| **Reconnection** | Reconfigure mobile session from scratch | `ContinueSession (0x10)` for mobile; Running state auto-streams |

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

| Name | UUID | Permissions | Description |
| ---- | ---- | ----------- | ----------- |
| **Status** | `a0e1f000-0002-4b3c-8e9a-1f2d3c4b5a60` | Notify | Device sends its state (Idle, Running, HasSavedSession) + battery level. |
| **Command** | `a0e1f000-0003-4b3c-8e9a-1f2d3c4b5a60` | Write | App writes binary `AppCommand`s (little-endian byte streams). |
| **Response** | `a0e1f000-0004-4b3c-8e9a-1f2d3c4b5a60` | Notify | Device sends replies: Ack, Nack, Ready, SensorInfo, SyncInfo. |
| **Measurement** | `a0e1f000-0005-4b3c-8e9a-1f2d3c4b5a60` | Indicate | Live measurement stream during active session. |
| **Sync** | `a0e1f000-0006-4b3c-8e9a-1f2d3c4b5a60` | Indicate | Device streams historical (stored) measurements automatically after reconnection. Not manually triggered. |

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

### Battery Level

Battery level is a `u8` (signed `i8` in firmware, treat as percentage). It arrives:
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
  - `0x02`: InvalidConfig (e.g., WiFi connection failed)
  - `0x03`: StorageHasMeasurements
  - `0x04`: ClearStorageFailed / SyncStorageFailed
- `0x22` **Ready**: Procedure complete (e.g., WiFi connected, sync finished, storage cleared).
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

### C. `StartSync` (OpCode `0x12`)

**Payload:** Single byte `0x12`.
**Context:** Push locally stored measurements through the **Sync** characteristic. Stops session if running.

- `Ack (0x20)`, then `SyncInfo (0x24)` with WiFi SSID + password.
- Historical records stream on Sync characteristic as chunked indications.
  - Success: `Ready (0x22)`.
  - Failure: `Nack (0x04)`.

### D. `NewSessionConfig` (OpCode `0x13`)

**Payload (Mobile):** `0x13` + `16B_UUID` + `2B_interval_seconds(u16)` + `0x01`
Mobile sessions do **not** include `session_token` — the field is absent from the payload entirely.

**Payload (Fixed):** `0x13` + `16B_UUID` + `16B_session_token` + `2B_interval_seconds(u16)` + `0x00` + `1B_pm1_index` + `1B_pm2_5_index` + `32B_WiFi_SSID` + `64B_WiFi_Password`
Strings are null-byte padded to their container lengths. The `session_token` (16 bytes) is only included for fixed sessions (provided by backend API response).

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
  - Success: `Ready (0x22)`.
  - Failure: `Nack (0x02 InvalidConfig)`.

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
4. `session_token` is included in the `NewSessionConfig` payload (16 bytes)
5. App sends `NewSessionConfig (0x13)` with all the above data + WiFi credentials

---

---

## 8. `SetTime` Periodic Scheduling

`SetTime (0x15)` must be sent:
1. Immediately after connection (once Status is received)
2. Every hour while connected — **only for mobile sessions**. Fixed sessions get time from the backend server, so hourly scheduling is not needed.

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
