package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.v2

import android.util.Log
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Request
import pl.llp.aircasting.data.api.util.TAG
import java.io.DataInputStream
import java.io.EOFException
import java.io.IOException
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
        const val DEFAULT_SYNC_HOST_IP = "192.168.71.1"
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

    /**
     * Result of a /sync GET. [httpComplete] = true iff the response stream was read to
     * a clean EOF without IOException. Callers that need to clear FW storage on success
     * should gate on this rather than [measurements] count, since an empty file is also
     * a successful sync that should clear the saved-session marker.
     */
    data class Result(val measurements: List<V2SyncMeasurement>, val httpComplete: Boolean)

    suspend fun download(
        boundNetwork: android.net.Network?,
        expectedSize: Long? = null,
        onProgress: ((Int) -> Unit)? = null,
    ): Result {
        val httpClient = client(boundNetwork)
        val url = "http://$syncHostIp/sync"
        val request = Request.Builder().url(url).get().build()

        Log.d(TAG, "V2SyncDownloader: GET $url (expectedSize=${expectedSize ?: "unknown"})")
        val response = try {
            httpClient.newCall(request).execute()
        } catch (e: IOException) {
            Log.e(TAG, "V2SyncDownloader: HTTP execute failed: ${e.message}")
            return Result(emptyList(), false)
        }
        if (!response.isSuccessful) {
            Log.e(TAG, "V2SyncDownloader: HTTP ${response.code}")
            response.close()
            return Result(emptyList(), false)
        }

        return response.body?.byteStream()?.use { stream ->
            parseStream(stream, expectedSize, onProgress)
        } ?: Result(emptyList(), false)
    }

    /**
     * Parse a concatenation of `[0xAB,0xBA, count, count*8B, xor]` blocks. Tolerates partial
     * blocks at EOF (returns whatever was successfully parsed) and records with a bad checksum
     * (logged + skipped). [Result.httpComplete] is false when the stream throws IOException
     * mid-read, true on natural EOF.
     *
     * @param expectedSize total body size from BLE `ReadyToSync.file_size`. Drives the
     * progress callback only — FW handles graceful TCP shutdown (SO_LINGER + 500ms grace
     * before `wifi.stop()`), so the phone always sees a clean EOF on a successful sync.
     * @param onProgress optional 0..100 progress callback invoked after each parsed block
     * (and once at 100 on completion). Only fires when [expectedSize] is non-null and > 0.
     */
    internal fun parseStream(
        input: InputStream,
        expectedSize: Long? = null,
        onProgress: ((Int) -> Unit)? = null,
    ): Result {
        val out = mutableListOf<V2SyncMeasurement>()
        val counting = CountingInputStream(input)
        val data = DataInputStream(counting)
        var httpComplete = true
        var lastReportedPercent = -1

        fun reportProgress() {
            val size = expectedSize ?: return
            if (size <= 0) return
            val pct = ((counting.bytesRead.coerceAtMost(size) * 100L) / size).toInt()
            if (pct != lastReportedPercent) {
                lastReportedPercent = pct
                onProgress?.invoke(pct)
            }
        }

        // If the SoftAP drops mid-transfer, socket reads throw IOException. Keep whatever
        // full blocks were already parsed but mark the sync incomplete so the orchestrator
        // skips Discard and the user can retry.
        try {
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

                // XOR over [0xAB, 0xBA, count, all record bytes] — firmware computes the
                // checksum over `slice[..len - 1]` in storage_iterator.rs, which includes
                // the two magic bytes. Earlier we XOR'd only count + records, so every
                // checksum was off by `0xAB ^ 0xBA = 0x11` and every block was discarded.
                var expected = (MAGIC_AB.toInt() and 0xFF) xor (MAGIC_BA.toInt() and 0xFF)
                expected = expected xor (countByte.toInt() and 0xFF)
                for (b in recordsBytes) expected = expected xor (b.toInt() and 0xFF)
                val checksum = checksumByte.toInt() and 0xFF
                if (expected != checksum) {
                    Log.w(TAG, "V2SyncDownloader: checksum mismatch (expected=${"%02x".format(expected)}, got=${"%02x".format(checksum)}) — skipping block")
                    reportProgress()
                    continue
                }

                for (i in 0 until count) {
                    val offset = i * 8
                    val ts = readU32Le(recordsBytes, offset)
                    val pm1 = readU16Le(recordsBytes, offset + 4)
                    val pm25 = readU16Le(recordsBytes, offset + 6)
                    out.add(V2SyncMeasurement(Date(ts * 1000L), pm1, pm25))
                }
                reportProgress()
            }
        } catch (e: IOException) {
            httpComplete = false
            Log.w(
                TAG,
                "V2SyncDownloader: stream aborted (${e.message}) — keeping ${out.size} measurements parsed, " +
                        "bytes=${counting.bytesRead}/${expectedSize ?: -1L}",
            )
        }
        if (httpComplete) onProgress?.invoke(100)
        Log.d(TAG, "V2SyncDownloader: parsed ${out.size} measurements, bytes=${counting.bytesRead}, httpComplete=$httpComplete")
        return Result(out, httpComplete)
    }

    private class CountingInputStream(private val src: InputStream) : InputStream() {
        var bytesRead: Long = 0
            private set

        override fun read(): Int {
            val b = src.read()
            if (b != -1) bytesRead++
            return b
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            val n = src.read(b, off, len)
            if (n > 0) bytesRead += n
            return n
        }

        override fun close() = src.close()
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
