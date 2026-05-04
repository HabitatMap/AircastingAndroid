package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.v2

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import pl.llp.aircasting.data.api.services.V2FixedMeasurementsUploader
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.AirBeamMiniV2Configurator
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.AirBeamMiniV2StateRepository
import javax.inject.Inject

/**
 * End-to-end V2 manual-sync flow:
 *
 *   1. BLE: send `StartSync (0x12)` — firmware Acks, opens SoftAP "AirBeam Mini Sync".
 *   2. BLE: wait for `Status::ReadyToSync(password)` notification.
 *   3. WiFi: join the SoftAP via [V2WifiApConnector] (BLE link kept alive — firmware parks
 *      the HTTP server on `WifiManager` so it survives any BLE jitter, and the bumped 30s
 *      `recv_wait_timeout`/`send_wait_timeout` absorb coex-induced packet loss).
 *   4. HTTP: GET `http://192.168.71.1/sync` and parse the streamed file.
 *   5. Process measurements:
 *      - MOBILE saved session → [V2MobileMeasurementsInserter] (V1 insert/skip-finished logic).
 *      - FIXED saved session → [V2FixedMeasurementsUploader] POST to backend.
 *      - Unknown UUID → log + drop (FW will clear storage anyway).
 *   6. BLE: send `DiscardSession (0x11)` over the still-live link so firmware clears the
 *      saved session + storage. Skipped when the HTTP stream aborted mid-read so unsent
 *      data on the device survives for the next attempt.
 *   7. Restore network binding (handled by [V2WifiApConnector]).
 *
 * Entry points: the new-session "Sync measurements?" dialog and the user-triggered SD-sync
 * flow. Both call [run] with the live [AirBeamMiniV2Configurator] held by the connector.
 */
@UserSessionScope
class V2SyncOrchestrator @Inject constructor(
    private val applicationContext: Context,
    private val v2StateRepository: AirBeamMiniV2StateRepository,
    private val sessionsRepository: SessionsRepository,
    private val mobileInserter: V2MobileMeasurementsInserter,
    private val fixedUploader: V2FixedMeasurementsUploader,
) {
    companion object {
        private const val PASSWORD_TIMEOUT_MS = 30_000L
    }

    /**
     * Returns true if the sync ran end-to-end without errors that should leave the FW state
     * dirty. False on AP-join failure, HTTP failure, or BLE timeouts; the caller can decide
     * whether to retry or surface a dialog.
     */
    suspend fun run(configurator: AirBeamMiniV2Configurator): Boolean = coroutineScope {
        v2StateRepository.resetReadyToSyncPassword()
        v2StateRepository.setSyncInProgress(true)
        try {
            runInner(configurator)
        } finally {
            v2StateRepository.setSyncInProgress(false)
        }
    }

    private suspend fun runInner(configurator: AirBeamMiniV2Configurator): Boolean = coroutineScope {

        // Step 1: kick off StartSync. Fire-and-forget — firmware never sends a final Ready
        // (0x22) after this command on the manual-sync branch, so awaiting it would just
        // burn time. We capture savedSessionUuid + deviceId from the *current* live BLE
        // state below before tearing down the link.
        val startSyncJob: Job = async { configurator.sendStartSyncManualAndAwaitDone() }

        // Step 2: wait for ReadyToSync(password) on the Status characteristic.
        val password = withTimeoutOrNull(PASSWORD_TIMEOUT_MS) {
            v2StateRepository.readyToSyncPassword.first()
        }
        if (password.isNullOrEmpty()) {
            Log.e(TAG, "V2SyncOrchestrator: timed out waiting for ReadyToSync password")
            startSyncJob.cancel()
            return@coroutineScope false
        }
        Log.d(TAG, "V2SyncOrchestrator: got SoftAP password (len=${password.length})")

        val savedUuid = v2StateRepository.savedSessionUuid
        val deviceId = configurator.deviceId

        // Steps 3+4: join AP, GET /sync over the SoftAP, parse measurements. BLE link stays
        // open — new firmware parks the HTTP server on WifiManager so any BLE jitter no
        // longer tears the server down, and httpd_config recv/send wait timeouts of 30s
        // absorb the residual coex stalls. Keeping BLE alive here means we can send the
        // post-sync Discard on the same link instead of doing a fragile reconnect.
        val apConnector = V2WifiApConnector(applicationContext)
        val downloadResult = apConnector.withApConnection(password) { network ->
            V2SyncFileDownloader().download(network)
        }

        if (downloadResult == null) {
            Log.e(TAG, "V2SyncOrchestrator: AP join failed")
            startSyncJob.cancel()
            return@coroutineScope false
        }
        val measurements = downloadResult.measurements
        val httpComplete = downloadResult.httpComplete
        Log.d(TAG, "V2SyncOrchestrator: downloaded ${measurements.size} measurements, httpComplete=$httpComplete")

        // Step 5: route measurements to the right destination based on the saved session type.
        if (savedUuid != null && deviceId != null && measurements.isNotEmpty()) {
            processMeasurements(savedUuid, deviceId, measurements)
        } else if (measurements.isEmpty()) {
            Log.d(TAG, "V2SyncOrchestrator: no measurements to process")
        } else {
            Log.w(TAG, "V2SyncOrchestrator: missing savedUuid=$savedUuid or deviceId=$deviceId — dropping ${measurements.size} measurements")
        }

        // The StartSync deferred never resolves — firmware doesn't emit a post-StartSync
        // Ready (0x22). Cancel before issuing Discard so its own deferred isn't shadowed.
        startSyncJob.cancel()

        // Step 6: send Discard on the still-open BLE link so firmware clears storage + the
        // saved-session marker. Gate on httpComplete (not record count) — an empty file
        // is still a successful sync, and we want to clear the marker so the device
        // doesn't keep reporting HasSavedSession on subsequent connects. Skip when the
        // HTTP stream aborted mid-read so any unsent data on the device survives.
        if (httpComplete) {
            val discarded = configurator.sendDiscardSessionAndAwait()
            Log.d(TAG, "V2SyncOrchestrator: post-sync Discard discarded=$discarded")
        } else {
            Log.w(TAG, "V2SyncOrchestrator: HTTP did not complete — skipping Discard, leaving data on device")
        }

        httpComplete
    }

    private suspend fun processMeasurements(
        sessionUuid: String,
        deviceId: String,
        measurements: List<V2SyncMeasurement>,
    ) {
        val session = sessionsRepository.getSessionByUUID(sessionUuid)
        if (session == null) {
            Log.w(TAG, "V2SyncOrchestrator: saved session $sessionUuid not in local DB — dropping ${measurements.size} measurements")
            return
        }

        when (session.type) {
            Session.Type.MOBILE -> mobileInserter.insert(deviceId, session, measurements)
            Session.Type.FIXED -> uploadFixed(session.uuid, measurements)
            else -> Log.w(TAG, "V2SyncOrchestrator: unsupported session type=${session.type}")
        }
    }

    private suspend fun uploadFixed(uuid: String, measurements: List<V2SyncMeasurement>) {
        val session = sessionsRepository.getSessionByUUID(uuid) ?: return
        val token = sessionsRepository.getSessionToken(uuid)
        val pm1Index = session.fixedPm1Index
        val pm25Index = session.fixedPm25Index
        if (token.isNullOrEmpty() || pm1Index == null || pm25Index == null) {
            Log.e(
                TAG,
                "V2SyncOrchestrator: cannot upload — fixed session $uuid missing token or sensor indices " +
                        "(token=${token != null}, pm1=$pm1Index, pm25=$pm25Index)",
            )
            return
        }
        val ok = fixedUploader.upload(uuid, token, pm1Index, pm25Index, measurements)
        Log.d(TAG, "V2SyncOrchestrator: fixed upload ok=$ok for $uuid")
    }
}
