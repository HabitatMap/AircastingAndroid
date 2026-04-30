package pl.llp.aircasting.data.api.services

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.v2.V2SyncMeasurement
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

/**
 * POSTs buffered V2 fixed-session measurements to `/api/v3/fixed_sessions/{uuid}/measurements`,
 * the same backend endpoint the firmware itself uses (see `wifi_manager.rs::send_measurements`
 * on the `maunal-sync` branch).
 *
 * Uses the [NonAuthenticated] [ApiService] because the endpoint expects a per-session bearer
 * (the 32-char hex `session_token` returned by `createFixedSessionV3`), not the user bearer
 * injected by [AuthenticationInterceptor].
 *
 * Body matches FW byte layout exactly:
 * ```
 * [0xAB, 0xBA] +
 * count_u16_BE (= measurement_count × 2) +
 * per measurement:
 *   ts_u32_BE + pm1_idx_u8 + pm1_f32_BE +
 *   ts_u32_BE + pm25_idx_u8 + pm25_f32_BE +
 * xor_u8_checksum
 * ```
 *
 * The mobile app is not RAM-bound the way the device is (FW caps at ~30 records/POST), so
 * we batch by [BATCH_SIZE_MEASUREMENTS] to keep request size reasonable while minimising
 * round trips.
 */
@UserSessionScope
class V2FixedMeasurementsUploader @Inject constructor(
    @NonAuthenticated private val apiService: ApiService,
) {
    suspend fun upload(
        uuid: String,
        sessionTokenHex: String,
        pm1Index: Int,
        pm25Index: Int,
        measurements: List<V2SyncMeasurement>,
    ): Boolean {
        if (measurements.isEmpty()) return true

        val auth = "Bearer $sessionTokenHex"
        val mediaType = "application/octet-stream".toMediaType()

        measurements.chunked(BATCH_SIZE_MEASUREMENTS).forEachIndexed { idx, batch ->
            val body = encodeBatch(batch, pm1Index, pm25Index).toRequestBody(mediaType)
            val response = apiService.uploadV2FixedMeasurements(uuid, auth, body)
            if (!response.isSuccessful) {
                Log.e(
                    TAG,
                    "V2FixedUploader: batch $idx (${batch.size} measurements) failed " +
                            "code=${response.code()} msg=${response.message()}",
                )
                return false
            }
            Log.d(TAG, "V2FixedUploader: batch $idx (${batch.size} measurements) uploaded")
        }
        return true
    }

    private fun encodeBatch(
        batch: List<V2SyncMeasurement>,
        pm1Index: Int,
        pm25Index: Int,
    ): ByteArray {
        // header (4) + count×2 records × (4+1+4) bytes + xor (1)
        val recordsBytes = batch.size * 2 * (4 + 1 + 4)
        val total = 2 + 2 + recordsBytes + 1
        val buffer = ByteBuffer.allocate(total).order(ByteOrder.BIG_ENDIAN)

        buffer.put(0xAB.toByte())
        buffer.put(0xBA.toByte())
        buffer.putShort((batch.size * 2).toShort()) // count = measurements × 2 (one record per sensor)

        batch.forEach { m ->
            val tsSec = (m.timestamp.time / 1000L).toInt()
            buffer.putInt(tsSec)
            buffer.put(pm1Index.toByte())
            buffer.putFloat(m.pm1.toFloat())

            buffer.putInt(tsSec)
            buffer.put(pm25Index.toByte())
            buffer.putFloat(m.pm25.toFloat())
        }

        // XOR checksum over every byte written so far
        val arr = buffer.array()
        var xor = 0
        for (i in 0 until total - 1) xor = xor xor (arr[i].toInt() and 0xFF)
        arr[total - 1] = xor.toByte()
        return arr
    }

    companion object {
        // FW caps at ~30 records due to RAM. Mobile can comfortably do orders of magnitude more.
        // 1000 measurements ≈ 18 KB body — well under any practical request limit.
        private const val BATCH_SIZE_MEASUREMENTS = 1000
    }
}
