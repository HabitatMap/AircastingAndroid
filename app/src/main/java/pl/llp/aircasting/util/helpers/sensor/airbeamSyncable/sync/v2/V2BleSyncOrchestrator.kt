package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.v2

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicLong
import pl.llp.aircasting.data.api.services.DownloadMeasurementsService
import pl.llp.aircasting.data.api.services.SessionsSyncService
import pl.llp.aircasting.data.api.services.V2FixedMeasurementsUploader
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.local.repository.MeasurementsRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.di.modules.IoCoroutineScope
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.AirBeamMiniV2Configurator
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.AirBeamMiniV2StateRepository
import java.util.Date
import javax.inject.Inject

/**
 * BLE-only manual-sync flow for AirBeam Mini V2 firmware (commits
 * e1a03b9415a8a4e70ee24acb824c1361f526d53c, c4dd9e62d5df599601d4edf57e5b8f5386440564).
 *
 *   1. BLE: send `StartBleSync (0x16)` — firmware Acks.
 *   2. BLE: firmware notifies Status `ReadyToSync (0x03)` with the storage file size
 *      (password byte field is empty on this path). The size feeds the shared 0..100%
 *      progress UI.
 *   3. BLE: firmware streams stored records on the Sync characteristic (same indication
 *      format as reconnect-time auto-sync — `[count_u8, 2B padding, count × 8B records]`,
 *      each record `ts_u32_LE + pm1_u16_LE + pm25_u16_LE`).
 *   4. BLE: firmware sends `Ready (0x22)` on Response when done; it then auto-clears
 *      storage + session config in its main loop's Stop handler. **No Discard needed.**
 *   5. App routes collected records to [V2MobileMeasurementsInserter] (mobile) or
 *      [V2FixedMeasurementsUploader] (fixed). Mobile sessions transition to FINISHED.
 *
 * Failure modes: write failure or `Nack 0x06 SyncFailed` resolve [sendStartBleSyncAndAwaitDone]
 * to false. The orchestrator returns false and leaves records on the device (firmware
 * retains them when it Nacks).
 */
@UserSessionScope
class V2BleSyncOrchestrator @Inject constructor(
    private val v2StateRepository: AirBeamMiniV2StateRepository,
    private val sessionsRepository: SessionsRepository,
    private val measurementsRepository: MeasurementsRepository,
    private val mobileInserter: V2MobileMeasurementsInserter,
    private val fixedUploader: V2FixedMeasurementsUploader,
    private val downloadMeasurementsService: DownloadMeasurementsService,
    private val sessionsSyncService: SessionsSyncService,
    @IoCoroutineScope private val syncScope: CoroutineScope,
) {
    companion object {
        private const val READY_TO_SYNC_TIMEOUT_MS = 30_000L
        // Tail delay after Ready (0x22) so any in-flight Sync characteristic indications
        // still queued on the Android BLE dispatcher land before we unhook the collector.
        private const val POST_READY_DRAIN_MS = 250L

        /**
         * On-disk LittleFS bytes-per-second throughput for the BLE manual-sync stream,
         * used to estimate sync duration from `file_size` before the user starts a sync.
         *
         * Calibrated from real-device run: file_size=25368B / elapsed=11735ms →
         * 2161 B/s observed. Dropped ~20% for headroom against weak-RSSI / older
         * Android BLE stacks so the ETA over-estimates rather than under-estimates.
         * Tune further with more samples from `V2BleSyncRate:` logcat entries.
         */
        private const val ESTIMATED_BYTES_PER_SECOND = 1700L

        /**
         * Estimate manual-sync duration in seconds from the `file_size` the firmware
         * reports in `Status::ReadyToSync (0x03)`. UI may call this after a Status
         * read to show an ETA on the sync confirmation screen.
         *
         * Returns 0 for non-positive sizes (empty storage / metadata failure).
         */
        fun estimateSyncSeconds(fileSizeBytes: Long): Long {
            if (fileSizeBytes <= 0L) return 0L
            return (fileSizeBytes + ESTIMATED_BYTES_PER_SECOND - 1) / ESTIMATED_BYTES_PER_SECOND
        }

        private fun logSyncRate(
            startMs: Long,
            endMs: Long,
            fileSize: Long,
            recordCount: Int,
            receivedBytes: Long,
        ) {
            if (startMs <= 0L) {
                Log.d(TAG, "V2BleSyncRate: skipped — ReadyToSync timestamp missing (file_size never arrived?)")
                return
            }
            val elapsedMs = (endMs - startMs).coerceAtLeast(1L)
            val bytesPerSec = if (fileSize > 0) fileSize * 1000L / elapsedMs else -1L
            val recordsPerSec = recordCount.toLong() * 1000L / elapsedMs
            val streamBytesPerSec = receivedBytes * 1000L / elapsedMs
            Log.d(
                TAG,
                "V2BleSyncRate: elapsedMs=$elapsedMs fileSize=$fileSize records=$recordCount " +
                        "bytesPerSec=$bytesPerSec recordsPerSec=$recordsPerSec " +
                        "streamBytesPerSec=$streamBytesPerSec",
            )
        }
    }

    /**
     * @param keepConnectedAfter Ignored — BLE manual sync never disconnects, so the link is
     *   always available for the caller's next command. Param retained for callback signature
     *   compatibility with the dormant [V2SyncOrchestrator].
     * @param onBeforePicker Ignored — BLE manual sync never asks the user to join a SoftAP.
     */
    suspend fun run(
        configurator: AirBeamMiniV2Configurator,
        @Suppress("UNUSED_PARAMETER") keepConnectedAfter: Boolean = false,
        @Suppress("UNUSED_PARAMETER") onBeforePicker: (suspend () -> Unit)? = null,
    ): Boolean = coroutineScope {
        Log.d(TAG, "V2BleSyncOrchestrator: run start")
        v2StateRepository.resetReadyToSyncPassword()
        v2StateRepository.setSyncInProgress(true)

        val savedUuid = v2StateRepository.savedSessionUuid
        val deviceId = configurator.deviceId

        // Resolve the target session up-front so MOBILE records can be persisted
        // chunk-by-chunk as they arrive (mirrors the reconnect-time auto-sync and the
        // live-measurement path). Buffering the whole backlog in memory and inserting only
        // after Ready (0x22) was unsafe: the firmware wipes its flash the instant it emits
        // Ready, so any hang/kill/force-quit before the terminal insert lost everything.
        val session = savedUuid?.let { sessionsRepository.getSessionByUUID(it) }
        val streamMobileToDb = session != null &&
                session.type == Session.Type.MOBILE &&
                deviceId != null

        // FIXED sessions still buffer — their records are HTTP-uploaded in one batch, not
        // written to the local measurements table.
        val collected = ArrayList<V2SyncMeasurement>()

        // AtomicLong so the BLE callback thread sees writes from the file-size collector
        // coroutine without word-tearing on Long reads.
        val receivedBytes = AtomicLong(0L)
        // MOBILE progress is driven off bytes actually PERSISTED (savedBytes), not bytes
        // received, so the bar reflects real DB persistence. Receiving into the channel is
        // instant, but the insert is the slow step — measuring at receive-time made the bar
        // hit ~99% while a large channel backlog was still draining, so it "froze" there for
        // the whole drain. Same byte formula as receivedBytes for a consistent scale.
        val savedBytes = AtomicLong(0L)
        val expectedSize = AtomicLong(-1L)

        fun publishSavedProgress() {
            val size = expectedSize.get()
            if (size > 0) {
                val pct = ((savedBytes.get() * 100L) / size).toInt().coerceIn(0, 99)
                v2StateRepository.setSyncProgress(pct)
            }
        }

        // Serialized per-chunk DB writer for MOBILE: a single consumer keeps stream
        // creation race-free, preserves arrival order, and spreads the insert work across
        // the transfer instead of one terminal bulk write. Records committed before an
        // interruption survive (the firmware retains its copy on Nack; a retry dedupes via
        // the unique measurements index).
        val mobileChunks = if (streamMobileToDb) Channel<List<V2SyncMeasurement>>(Channel.UNLIMITED) else null
        val savedRecords = AtomicLong(0L)
        val mobileWriterJob = if (mobileChunks != null && session != null && deviceId != null) {
            val dev = deviceId
            val sess = session
            launch {
                for (chunk in mobileChunks) {
                    runCatching { mobileInserter.insert(dev, sess, chunk) }
                        .onSuccess {
                            savedRecords.addAndGet(chunk.size.toLong())
                            savedBytes.addAndGet(5L + 8L * chunk.size)
                            publishSavedProgress()
                        }
                        .onFailure { Log.e(TAG, "V2BleSyncOrchestrator: per-chunk mobile insert failed: ${it.message}") }
                }
            }
        } else null

        configurator.setManualSyncChunkHandler { chunk ->
            // Track received bytes for the end-of-run transfer-rate log regardless of path.
            val received = receivedBytes.addAndGet(5L + 8L * chunk.size)
            if (mobileChunks != null) {
                // Hand off for immediate, ordered DB persistence; progress is published by
                // the consumer as each chunk lands in the DB (publishSavedProgress).
                mobileChunks.trySend(chunk)
            } else {
                // FIXED / buffered fallback: no consumer, so drive progress off received.
                collected.addAll(chunk)
                val size = expectedSize.get()
                if (size > 0) {
                    val pct = ((received * 100L) / size).toInt().coerceIn(0, 99)
                    v2StateRepository.setSyncProgress(pct)
                }
            }
        }

        // Subscribe to file_size in parallel: firmware sends ReadyToSync (Status 0x03)
        // ~100ms before the first Sync indication, so this resolves long before
        // sendStartBleSyncAndAwaitDone() returns (it awaits the post-stream Ready 0x22).
        // Without this, expectedSize stayed -1 for the entire stream and progress
        // never moved off 0%.
        // Captured when ReadyToSync (Status 0x03) lands — i.e. the moment FW is about to
        // start streaming (it sleeps 100ms after Status then starts indications). Pairs
        // with the post-Ready end timestamp below to derive an end-to-end transfer rate.
        val streamStartMs = AtomicLong(-1L)

        val fileSizeJob = launch {
            val size = withTimeoutOrNull(READY_TO_SYNC_TIMEOUT_MS) {
                v2StateRepository.readyToSyncFileSize.first()
            } ?: v2StateRepository.readyToSyncFileSize.replayCache.firstOrNull() ?: -1L
            streamStartMs.set(System.currentTimeMillis())
            expectedSize.set(size)
            if (size > 0) {
                val pct = ((receivedBytes.get() * 100L) / size).toInt().coerceIn(0, 99)
                v2StateRepository.setSyncProgress(pct)
            }
            Log.d(TAG, "V2BleSyncOrchestrator: ReadyToSync file_size=$size")
        }

        try {
            val done = configurator.sendStartBleSyncAndAwaitDone()
            val streamEndMs = System.currentTimeMillis()
            fileSizeJob.join()

            // Flush the mobile writer: stop accepting chunks and wait for every queued one
            // to commit before reporting completion or marking the session finished.
            mobileChunks?.close()
            mobileWriterJob?.join()

            Log.d(TAG, "V2BleSyncOrchestrator: command done=$done, expectedSize=${expectedSize.get()}, buffered=${collected.size}, mobileSaved=${savedRecords.get()}")

            if (!done) {
                // MOBILE chunks streamed before the failure are already persisted; the
                // firmware retains its copy on Nack so a later retry dedupes on insert.
                Log.e(TAG, "V2BleSyncOrchestrator: BLE sync did not complete (Nack or timeout)")
                return@coroutineScope false
            }

            val recordCount = collected.size + savedRecords.get().toInt()
            logSyncRate(streamStartMs.get(), streamEndMs, expectedSize.get(), recordCount, receivedBytes.get())

            // Tail-drain so any pending Sync indications on the BLE dispatcher land.
            delay(POST_READY_DRAIN_MS)
            v2StateRepository.setSyncProgress(100)

            if (streamMobileToDb && session != null) {
                // Records were persisted chunk-by-chunk during the stream; just finalize.
                markMobileFinished(session.uuid)
                // Backend upload of a large synced session is heavy (loadCompleteSession
                // pulls every row) and must NOT block the sync dialog — otherwise the UI
                // sits at "100%" for the entire upload. Fire-and-forget on an IO scope; the
                // finish flow (StopRecordingEvent -> RecordingHandler.stopRecording) also
                // triggers a backend sync once the user confirms.
                launchBackendSync()
                return@coroutineScope true
            }

            if (collected.isEmpty()) {
                Log.d(TAG, "V2BleSyncOrchestrator: zero measurements (storage was empty)")
                return@coroutineScope true
            }

            if (savedUuid == null || deviceId == null) {
                Log.w(TAG, "V2BleSyncOrchestrator: missing savedUuid=$savedUuid / deviceId=$deviceId — dropping ${collected.size} measurements")
                return@coroutineScope false
            }

            processMeasurements(savedUuid, deviceId, collected)
        } finally {
            configurator.setManualSyncChunkHandler(null)
            mobileChunks?.close()
            v2StateRepository.setSyncInProgress(false)
        }
    }

    private suspend fun processMeasurements(
        sessionUuid: String,
        deviceId: String,
        measurements: List<V2SyncMeasurement>,
    ): Boolean {
        val session = sessionsRepository.getSessionByUUID(sessionUuid)
        if (session == null) {
            Log.w(TAG, "V2BleSyncOrchestrator: saved session $sessionUuid not in local DB — dropping ${measurements.size} measurements")
            return true
        }

        return when (session.type) {
            Session.Type.MOBILE -> {
                mobileInserter.insert(deviceId, session, measurements)
                markMobileFinished(session.uuid)
                launchBackendSync()
                true
            }
            Session.Type.FIXED -> uploadFixed(session.uuid, measurements)
            else -> {
                Log.w(TAG, "V2BleSyncOrchestrator: unsupported session type=${session.type}")
                false
            }
        }
    }

    /**
     * Fire-and-forget backend sync on an IO scope. Must never block the caller: for a large
     * synced session the upload's `loadCompleteSession` pulls every measurement row, so
     * awaiting it here would freeze the sync dialog at "100%" for the whole upload.
     */
    private fun launchBackendSync() {
        syncScope.launch {
            runCatching { sessionsSyncService.sync() }
                .onFailure { Log.w(TAG, "V2BleSyncOrchestrator: background backend sync failed: ${it.message}") }
        }
    }

    private suspend fun markMobileFinished(uuid: String) {
        // Lightweight load (session row only) — do NOT use loadSessionAndMeasurementsByUUID,
        // which pulls the entire measurements @Relation into memory (hundreds of thousands of
        // rows for a long synced session) just to flip status. update() writes only the
        // session row, so the measurements are never needed here.
        val dbObject = sessionsRepository.getSessionByUUID(uuid) ?: run {
            Log.w(TAG, "V2BleSyncOrchestrator: markMobileFinished — session $uuid missing")
            return
        }
        val session = Session(dbObject)
        if (!session.isRecording() && !session.isDisconnected()) {
            Log.d(TAG, "V2BleSyncOrchestrator: session $uuid already in terminal status=${session.status}, skip")
            return
        }
        val endTime = measurementsRepository.lastMeasurementTime(dbObject.id) ?: Date()
        session.stopRecording(endTime)
        sessionsRepository.update(session)
        Log.d(TAG, "V2BleSyncOrchestrator: marked mobile session $uuid FINISHED (endTime=$endTime)")
    }

    private suspend fun uploadFixed(uuid: String, measurements: List<V2SyncMeasurement>): Boolean {
        val session = sessionsRepository.getSessionByUUID(uuid) ?: return false
        val token = sessionsRepository.getSessionToken(uuid)
        val pm1Index = session.fixedPm1Index
        val pm25Index = session.fixedPm25Index
        if (token.isNullOrEmpty() || pm1Index == null || pm25Index == null) {
            Log.e(
                TAG,
                "V2BleSyncOrchestrator: cannot upload — fixed session $uuid missing token or sensor indices " +
                        "(token=${token != null}, pm1=$pm1Index, pm25=$pm25Index)",
            )
            return false
        }
        val ok = fixedUploader.upload(uuid, token, pm1Index, pm25Index, measurements)
        Log.d(TAG, "V2BleSyncOrchestrator: fixed upload ok=$ok for $uuid")
        if (ok) {
            runCatching { downloadMeasurementsService.downloadMeasurements(uuid) }
                .onFailure { Log.w(TAG, "V2BleSyncOrchestrator: post-upload refresh failed: ${it.message}") }
        }
        return ok
    }
}
