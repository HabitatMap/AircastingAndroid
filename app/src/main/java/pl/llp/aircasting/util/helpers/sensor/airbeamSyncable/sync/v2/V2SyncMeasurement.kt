package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.v2

import java.util.Date

/**
 * One PM record streamed by the AirBeam Mini V2 firmware over the manual-sync
 * HTTP endpoint. Values are integer μg/m³ as transmitted on the wire (firmware
 * stores `pm1_avg`/`pm2_5_avg` as `u16`).
 */
data class V2SyncMeasurement(
    val timestamp: Date,
    val pm1: Int,
    val pm25: Int,
)
