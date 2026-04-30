package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.v2

import android.util.Log
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Request
import pl.llp.aircasting.data.api.util.TAG
import java.io.DataInputStream
import java.io.EOFException
import java.io.InputStream
import java.net.InetAddress
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Downloads `http://192.168.4.1/sync` from the V2 firmware's SoftAP and parses the streamed
 * binary file format into a list of [V2SyncMeasurement]s.
 *
 * On-disk record layout (one block, repeated until EOF):
 * ```
 * [0xAB, 0xBA, count_u8, count × 8B records, xor_u8]
 * record (8 bytes, little-endian):
 *   ts_u32_LE + pm1_u16_LE + pm25_u16_LE
 * ```
 * The XOR checksum covers the count byte plus all record bytes.
 *
 * The HTTP body is consumed as the actual file bytes (firmware streams the raw file
 * contents in 4 KB chunks). We decode incrementally so we never buffer the whole file.
 */
class V2SyncFileDownloader(
    private val syncHostIp: String = DEFAULT_SYNC_HOST_IP,
) {
    companion object {
        const val DEFAULT_SYNC_HOST_IP = "192.168.4.1"
        private const val MAGIC_AB = 0xAB.toByte()
        private const val MAGIC_BA = 0xBA.toByte()
        private const val MAX_RECORDS_PER_BLOCK = 64 // FW caps at 10; 64 is safety margin
        private const val READ_TIMEOUT_SECONDS: Long = 60
    }

    /**
     * Build an OkHttp client whose connections are pinned to [boundNetwork] (so traffic goes
     * through the AirBeam SoftAP, not the user's regular WiFi/cell). Pass null on legacy
     * paths where the entire device is already on the AP.
     */
    private fun client(boundNetwork: android.net.Network?): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .connectTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .callTimeout(READ_TIMEOUT_SECONDS * 4, TimeUnit.SECONDS)
        if (boundNetwork != null) {
            builder.socketFactory(boundNetwork.socketFactory)
            builder.dns(object : Dns {
                override fun lookup(hostname: String): List<InetAddress> =
                    boundNetwork.getAllByName(hostname).toList()
            })
        } else {
            builder.dns(object : Dns {
                override fun lookup(hostname: String): List<InetAddress> =
                    listOf(InetAddress.getByName(hostname))
            })
        }
        return builder.build()
    }

    suspend fun download(boundNetwork: android.net.Network?): List<V2SyncMeasurement> {
        val httpClient = client(boundNetwork)
        val url = "http://$syncHostIp/sync"
        val request = Request.Builder().url(url).get().build()

        Log.d(TAG, "V2SyncDownloader: GET $url")
        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            Log.e(TAG, "V2SyncDownloader: HTTP ${response.code}")
            response.close()
            return emptyList()
        }

        return response.body?.byteStream()?.use { stream ->
            parseStream(stream)
        } ?: emptyList()
    }

    /**
     * Parse a concatenation of `[0xAB,0xBA, count, count*8B, xor]` blocks. Tolerates partial
     * blocks at EOF (returns whatever was successfully parsed) and records with a bad checksum
     * (logged + skipped).
     */
    internal fun parseStream(input: InputStream): List<V2SyncMeasurement> {
        val out = mutableListOf<V2SyncMeasurement>()
        val data = DataInputStream(input)

        while (true) {
            val first = readByteOrNull(data) ?: break
            if (first != MAGIC_AB) {
                Log.w(TAG, "V2SyncDownloader: stream desync — expected 0xAB, got ${"%02x".format(first)}; aborting")
                break
            }
            val second = readByteOrNull(data)
            if (second != MAGIC_BA) {
                Log.w(TAG, "V2SyncDownloader: stream desync — expected 0xBA after 0xAB; aborting")
                break
            }

            val countByte = readByteOrNull(data) ?: break
            val count = countByte.toInt() and 0xFF
            if (count == 0 || count > MAX_RECORDS_PER_BLOCK) {
                Log.w(TAG, "V2SyncDownloader: implausible record count=$count, aborting")
                break
            }

            val recordsBytes = ByteArray(count * 8)
            try {
                data.readFully(recordsBytes)
            } catch (e: EOFException) {
                Log.w(TAG, "V2SyncDownloader: truncated block (count=$count)")
                break
            }

            val checksumByte = readByteOrNull(data) ?: break

            // XOR over count + all record bytes (matches FW storage_iterator layout)
            var expected = countByte.toInt() and 0xFF
            for (b in recordsBytes) expected = expected xor (b.toInt() and 0xFF)
            val checksum = checksumByte.toInt() and 0xFF
            if (expected != checksum) {
                Log.w(TAG, "V2SyncDownloader: checksum mismatch (expected=${"%02x".format(expected)}, got=${"%02x".format(checksum)}) — skipping block")
                continue
            }

            for (i in 0 until count) {
                val offset = i * 8
                val ts = readU32Le(recordsBytes, offset)
                val pm1 = readU16Le(recordsBytes, offset + 4)
                val pm25 = readU16Le(recordsBytes, offset + 6)
                out.add(V2SyncMeasurement(Date(ts * 1000L), pm1, pm25))
            }
        }
        Log.d(TAG, "V2SyncDownloader: parsed ${out.size} measurements")
        return out
    }

    private fun readByteOrNull(input: DataInputStream): Byte? {
        val b = input.read()
        return if (b == -1) null else b.toByte()
    }

    private fun readU16Le(buf: ByteArray, offset: Int): Int =
        (buf[offset].toInt() and 0xFF) or ((buf[offset + 1].toInt() and 0xFF) shl 8)

    private fun readU32Le(buf: ByteArray, offset: Int): Long =
        ((buf[offset].toInt() and 0xFF).toLong()) or
                ((buf[offset + 1].toInt() and 0xFF).toLong() shl 8) or
                ((buf[offset + 2].toInt() and 0xFF).toLong() shl 16) or
                ((buf[offset + 3].toInt() and 0xFF).toLong() shl 24)
}
