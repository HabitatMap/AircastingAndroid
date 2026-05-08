package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.v2

import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import pl.llp.aircasting.util.extensions.isConnected
import pl.llp.aircasting.data.api.services.DownloadMeasurementsService
import pl.llp.aircasting.data.api.services.SessionsSyncService
import pl.llp.aircasting.data.api.services.V2FixedMeasurementsUploader
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.local.repository.MeasurementsRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.AirBeamMiniV2Configurator
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.AirBeamMiniV2StateRepository
import java.util.Date
import javax.inject.Inject

/**
 * End-to-end V2 manual-sync flow:
 *
 *   1. BLE: send `StartSync (0x12)` — firmware Acks, opens SoftAP "AirBeam Mini Sync".
 *   2. BLE: wait for `Status::ReadyToSync(password)` notification.
 *   3. BLE: voluntary GATT disconnect (Nordic `disconnect()`, NOT `close()`). ESP32 + this
 *      phone share one 2.4 GHz radio; leaving BLE up during the SoftAP HTTP transfer trips
 *      the GATT supervision timeout in ~2s, fires Nordic's auto-reconnect, and the resulting
 *      reconnect storm starves WiFi long enough for the phone-side TCP read to abort.
 *      Voluntary disconnect skips auto-reconnect and lets the [BleManager] be reused for the
 *      post-sync Discard.
 *   4. WiFi: join the SoftAP via [V2WifiApConnector].
 *   5. HTTP: GET `http://192.168.71.1/sync` and parse the streamed file.
 *   6. Process measurements:
 *      - MOBILE saved session → [V2MobileMeasurementsInserter] (V1 insert/skip-finished logic).
 *      - FIXED saved session → [V2FixedMeasurementsUploader] POST to backend.
 *      - Unknown UUID → log + drop (FW will clear storage anyway).
 *   7. BLE: re-`connect()` the same [BleManager] (so `initialize()` repopulates characteristic
 *      refs), send `DiscardSession (0x11)`, await `Ready (0x22)`, voluntarily disconnect again.
 *      Skipped when HTTP did not complete cleanly so unsent data on the device survives.
 *   8. Restore network binding (handled by [V2WifiApConnector]).
 *
 * Entry points: the new-session "Sync measurements?" dialog and the user-triggered SD-sync
 * flow. Both call [run] with the live [AirBeamMiniV2Configurator] held by the connector.
 */
@UserSessionScope
class V2SyncOrchestrator @Inject constructor(
    private val applicationContext: Context,
    private val v2StateRepository: AirBeamMiniV2StateRepository,
    private val sessionsRepository: SessionsRepository,
    private val measurementsRepository: MeasurementsRepository,
    private val mobileInserter: V2MobileMeasurementsInserter,
    private val fixedUploader: V2FixedMeasurementsUploader,
    private val downloadMeasurementsService: DownloadMeasurementsService,
    private val sessionsSyncService: SessionsSyncService,
) {
    companion object {
        private const val PASSWORD_TIMEOUT_MS = 30_000L
        // After releasing the SoftAP, give Android a moment to flip the system default
        // back to a network with INTERNET capability before we hit the backend uploader.
        // Without this, NetworkConnectionInterceptor sees activeNetwork == SoftAP (no
        // INTERNET) and synthesizes a 503 for our upload, losing the just-downloaded
        // batch.
        private const val DEFAULT_NETWORK_RESTORE_TIMEOUT_MS = 8_000L
        private const val DEFAULT_NETWORK_POLL_MS = 200L
    }

    /**
     * Returns true if the sync ran end-to-end without errors that should leave the FW state
     * dirty. False on AP-join failure, HTTP failure, or BLE timeouts; the caller can decide
     * whether to retry or surface a dialog.
     *
     * @param keepConnectedAfter Skip the post-Discard voluntary BLE disconnect so the caller
     * can write another command (e.g. `NewSessionConfig`) on the same GATT link right after.
     * Defaults to false for the dashboard SD-sync entry point which expects the device to be
     * fully disconnected when the orchestrator returns.
     */
    suspend fun run(
        configurator: AirBeamMiniV2Configurator,
        keepConnectedAfter: Boolean = false,
        onBeforePicker: (suspend () -> Unit)? = null,
    ): Boolean = coroutineScope {
        Log.d(
            TAG,
            "V2SyncOrchestrator: run start — device=${Build.MANUFACTURER} ${Build.MODEL}, " +
                    "androidSdk=${Build.VERSION.SDK_INT} (release=${Build.VERSION.RELEASE}), " +
                    "keepConnectedAfter=$keepConnectedAfter",
        )
        v2StateRepository.resetReadyToSyncPassword()
        v2StateRepository.setSyncInProgress(true)
        try {
            runInner(configurator, keepConnectedAfter, onBeforePicker)
        } finally {
            v2StateRepository.setSyncInProgress(false)
        }
    }

    private suspend fun runInner(
        configurator: AirBeamMiniV2Configurator,
        keepConnectedAfter: Boolean,
        onBeforePicker: (suspend () -> Unit)?,
    ): Boolean = coroutineScope {

        // Step 0: educational dialog FIRST, before we ask the firmware to open SoftAP.
        // Firmware closes the SoftAP a few seconds after opening it if no client joins;
        // showing the dialog *after* StartSync burned that window on Android 10 where the
        // system Wi-Fi picker takes a few seconds to render and a few more for the user to
        // tap. With the dialog up-front, the SoftAP-open → picker-shown gap shrinks to the
        // BLE round-trip only.
        runCatching { onBeforePicker?.invoke() }
            .onFailure { Log.w(TAG, "V2SyncOrchestrator: onBeforePicker hook failed: ${it.message}") }

        // Step 1: kick off StartSync. Fire-and-forget — firmware never sends a final Ready
        // (0x22) after this command on the manual-sync branch, so awaiting it would just
        // burn time. We capture savedSessionUuid + deviceId from the *current* live BLE
        // state below before tearing down the link.
        val startSyncJob: Job = async { configurator.sendStartSyncManualAndAwaitDone() }

        // Step 2: wait for ReadyToSync(file_size, password) on the Status characteristic.
        // file_size flow is emitted alongside the password by parseStatus, so it has the
        // value cached (replay=1) by the time we read it below. -1 if FW pre-`ed751b180`
        // or payload was malformed — downloader gracefully degrades to no progress when
        // expectedSize is null/<=0.
        val password = withTimeoutOrNull(PASSWORD_TIMEOUT_MS) {
            v2StateRepository.readyToSyncPassword.first()
        }
        if (password.isNullOrEmpty()) {
            Log.e(TAG, "V2SyncOrchestrator: timed out waiting for ReadyToSync password")
            startSyncJob.cancel()
            return@coroutineScope false
        }
        val fileSize = v2StateRepository.readyToSyncFileSize.replayCache.firstOrNull() ?: -1L
        Log.d(TAG, "V2SyncOrchestrator: got SoftAP password (len=${password.length}), fileSize=$fileSize")

        val savedUuid = v2StateRepository.savedSessionUuid
        val deviceId = configurator.deviceId

        // Step 3: voluntary BLE disconnect to free the phone radio for the SoftAP HTTP
        // transfer. Without this the GATT supervision timeout fires during HTTP and Nordic's
        // auto-reconnect storm starves WiFi until phone-side TCP aborts.
        Log.d(TAG, "V2SyncOrchestrator: voluntary BLE disconnect before HTTP to avoid coex starvation")
        startSyncJob.cancel()
        runCatching { configurator.disconnectGattForSync() }
            .onFailure { Log.w(TAG, "V2SyncOrchestrator: BLE disconnect failed: ${it.message}") }

        // Steps 4+5: join AP, GET /sync, parse measurements. Progress callback flows
        // from the downloader's per-block byte counter into the shared sync-progress
        // StateFlow so all UI surfaces (SD-sync wizard, Sync-and-Finish dialog,
        // Sync-before-new-session dialog) reflect the same %.
        val apConnector = V2WifiApConnector(applicationContext)
        val downloadResult = apConnector.withApConnection(password) { network ->
            V2SyncFileDownloader().download(
                boundNetwork = network,
                expectedSize = fileSize.takeIf { it > 0 },
                onProgress = { percent -> v2StateRepository.setSyncProgress(percent) },
            )
        }

        if (downloadResult == null) {
            Log.e(TAG, "V2SyncOrchestrator: AP join failed")
            return@coroutineScope false
        }
        val measurements = downloadResult.measurements
        val httpComplete = downloadResult.httpComplete
        Log.d(TAG, "V2SyncOrchestrator: downloaded ${measurements.size} measurements, httpComplete=$httpComplete")

        // Step 6: route measurements to the right destination based on the saved session
        // type. Wait for Android to restore an INTERNET-capable default network first —
        // otherwise the backend uploader gets 503'd by NetworkConnectionInterceptor while
        // activeNetwork is still the just-released no-internet SoftAP.
        val processed = if (savedUuid != null && deviceId != null && measurements.isNotEmpty()) {
            waitForDefaultNetwork()
            processMeasurements(savedUuid, deviceId, measurements)
        } else if (measurements.isEmpty()) {
            Log.d(TAG, "V2SyncOrchestrator: no measurements to process")
            true
        } else {
            Log.w(TAG, "V2SyncOrchestrator: missing savedUuid=$savedUuid or deviceId=$deviceId — dropping ${measurements.size} measurements")
            false
        }

        // Step 7: reconnect BLE and send Discard so firmware clears storage + the
        // saved-session marker. Gate on httpComplete *and* successful processing — losing
        // unsent records to a 503 race would be worse than letting the firmware keep the
        // saved file for a retry. An empty-but-complete sync still triggers Discard so the
        // saved-session marker doesn't linger.
        if (httpComplete && processed) {
            val discarded = configurator.reconnectAndSendDiscard(keepConnectedAfter)
            Log.d(TAG, "V2SyncOrchestrator: post-sync Discard discarded=$discarded keepConnectedAfter=$keepConnectedAfter")
        } else {
            Log.w(
                TAG,
                "V2SyncOrchestrator: skipping Discard (httpComplete=$httpComplete, processed=$processed) — leaving data on device",
            )
        }

        httpComplete && processed
    }

    /**
     * Poll [Context.isConnected] until Android has flipped the system default back to a
     * network with `NET_CAPABILITY_INTERNET`. The freshly-released SoftAP has no INTERNET
     * capability and lingers as activeNetwork for a beat after `unregisterNetworkCallback`,
     * which makes [NetworkConnectionInterceptor] short-circuit any upload to a synthetic
     * 503.
     */
    private suspend fun waitForDefaultNetwork() {
        if (applicationContext.isConnected) return
        Log.d(TAG, "V2SyncOrchestrator: waiting for INTERNET-capable default network")
        val ok = withTimeoutOrNull(DEFAULT_NETWORK_RESTORE_TIMEOUT_MS) {
            while (!applicationContext.isConnected) delay(DEFAULT_NETWORK_POLL_MS)
            true
        } ?: false
        Log.d(TAG, "V2SyncOrchestrator: default network ready=$ok")
    }

    /**
     * @return true if the measurements were persisted/uploaded successfully (or are an
     * acceptable no-op like "session not in local DB"). False on backend upload failure
     * so the orchestrator can keep the data on the device for a retry.
     */
    private suspend fun processMeasurements(
        sessionUuid: String,
        deviceId: String,
        measurements: List<V2SyncMeasurement>,
    ): Boolean {
        val session = sessionsRepository.getSessionByUUID(sessionUuid)
        if (session == null) {
            Log.w(TAG, "V2SyncOrchestrator: saved session $sessionUuid not in local DB — dropping ${measurements.size} measurements")
            return true
        }

        return when (session.type) {
            Session.Type.MOBILE -> {
                mobileInserter.insert(deviceId, session, measurements)
                // Per V1 SD-sync semantics: once the session's measurements are persisted
                // locally, the session itself is finished — there's nothing left to record
                // and the device is being told to Discard right after this. Without this
                // transition the dashboard keeps the session in the active mobile tab even
                // though no recording is happening, and the next reconnect would re-attach
                // to a dead session UUID.
                markMobileFinished(session.uuid)
                // Backend upload of the now-finished session. Mirrors the trailing
                // sessionsSyncService.sync() that V1 SDCardSyncService runs after persisting
                // mobile measurements.
                runCatching { sessionsSyncService.sync() }
                    .onFailure { Log.w(TAG, "V2SyncOrchestrator: backend sync after mobile insert failed: ${it.message}") }
                true
            }
            Session.Type.FIXED -> uploadFixed(session.uuid, measurements)
            else -> {
                Log.w(TAG, "V2SyncOrchestrator: unsupported session type=${session.type}")
                false
            }
        }
    }

    private suspend fun markMobileFinished(uuid: String) {
        val session = sessionsRepository.loadSessionAndMeasurementsByUUID(uuid) ?: run {
            Log.w(TAG, "V2SyncOrchestrator: markMobileFinished — session $uuid missing")
            return
        }
        if (!session.isRecording() && !session.isDisconnected()) {
            Log.d(TAG, "V2SyncOrchestrator: session $uuid already in terminal status=${session.status}, skip")
            return
        }
        val sessionId = sessionsRepository.getSessionIdByUUID(uuid)
        val endTime = sessionId?.let { measurementsRepository.lastMeasurementTime(it) } ?: Date()
        session.stopRecording(endTime)
        sessionsRepository.update(session)
        Log.d(TAG, "V2SyncOrchestrator: marked mobile session $uuid FINISHED (endTime=$endTime)")
    }

    private suspend fun uploadFixed(uuid: String, measurements: List<V2SyncMeasurement>): Boolean {
        val session = sessionsRepository.getSessionByUUID(uuid) ?: return false
        val token = sessionsRepository.getSessionToken(uuid)
        val pm1Index = session.fixedPm1Index
        val pm25Index = session.fixedPm25Index
        if (token.isNullOrEmpty() || pm1Index == null || pm25Index == null) {
            Log.e(
                TAG,
                "V2SyncOrchestrator: cannot upload — fixed session $uuid missing token or sensor indices " +
                        "(token=${token != null}, pm1=$pm1Index, pm25=$pm25Index)",
            )
            return false
        }
        val ok = fixedUploader.upload(uuid, token, pm1Index, pm25Index, measurements)
        Log.d(TAG, "V2SyncOrchestrator: fixed upload ok=$ok for $uuid")

        // Backend POST is the authoritative store for fixed-session measurements; the app's
        // local DB only sees them once DownloadMeasurementsService GETs the session back.
        // Without this refresh the dashboard stays empty until the user expands the session
        // card (which is the only other code path that triggers a fetch).
        if (ok) {
            runCatching { downloadMeasurementsService.downloadMeasurements(uuid) }
                .onFailure { Log.w(TAG, "V2SyncOrchestrator: post-upload refresh failed: ${it.message}") }
        }
        return ok
    }
}
