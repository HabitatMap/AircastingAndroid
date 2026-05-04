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
 *   3. BLE: voluntarily close the GATT connection. ESP32 BLE+Wi-Fi share one 2.4 GHz radio,
 *      and on Android 12 + this hardware leaving BLE active during the SoftAP HTTP transfer
 *      reliably starves Wi-Fi packets long enough that the firmware's HTTP server times out
 *      mid-stream and aborts the connection. Closing BLE here lets Wi-Fi run uncontended.
 *   4. WiFi: join the SoftAP via [V2WifiApConnector].
 *   5. HTTP: GET `http://192.168.71.1/sync` and parse the streamed file.
 *   6. Process measurements:
 *      - MOBILE saved session → [V2MobileMeasurementsInserter] (V1 insert/skip-finished logic).
 *      - FIXED saved session → [V2FixedMeasurementsUploader] POST to backend.
 *      - Unknown UUID → log + drop (FW will clear storage anyway).
 *   7. BLE: reconnect briefly and send `DiscardSession (0x11)` so the firmware clears the
 *      saved session + storage. Skipped on partial/empty download to keep the data on
 *      device for retry. Reconnect (rather than keeping BLE open through HTTP) preserves
 *      the coex isolation that made step 5 reliable.
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

        // Step 3: drop BLE so Wi-Fi has the radio to itself for the SoftAP HTTP transfer.
        // Without this, BLE+Wi-Fi coex on Android 12 + ESP32 starves the TCP stream and the
        // firmware's HTTP server aborts mid-body. We don't need BLE during HTTP — and the
        // firmware doesn't send a post-sync Ready we'd be waiting on anyway.
        Log.d(TAG, "V2SyncOrchestrator: closing BLE before HTTP to avoid Wi-Fi/BLE coex starvation")
        startSyncJob.cancel()
        runCatching { configurator.closeConnection() }
            .onFailure { Log.w(TAG, "V2SyncOrchestrator: BLE close failed: ${it.message}") }

        // Steps 4+5: join AP, GET /sync, parse measurements. Restore network on completion.
        val apConnector = V2WifiApConnector(applicationContext)
        val measurements: List<V2SyncMeasurement>? = apConnector.withApConnection(password) { network ->
            V2SyncFileDownloader().download(network)
        }

        if (measurements == null) {
            Log.e(TAG, "V2SyncOrchestrator: AP join failed")
            return@coroutineScope false
        }
        Log.d(TAG, "V2SyncOrchestrator: downloaded ${measurements.size} measurements")

        // Step 6: route measurements to the right destination based on the saved session type.
        // savedUuid + deviceId captured pre-close above so the BLE teardown didn't lose them.
        if (savedUuid != null && deviceId != null && measurements.isNotEmpty()) {
            processMeasurements(savedUuid, deviceId, measurements)
        } else if (measurements.isEmpty()) {
            Log.d(TAG, "V2SyncOrchestrator: no measurements to process")
        } else {
            Log.w(TAG, "V2SyncOrchestrator: missing savedUuid=$savedUuid or deviceId=$deviceId — dropping ${measurements.size} measurements")
        }

        // Step 7: re-open BLE and send Discard so firmware clears storage + the saved-session
        // marker. Skip on empty/failed download — leave data on device for the next attempt.
        if (measurements.isNotEmpty()) {
            val discarded = configurator.reconnectAndSendDiscard()
            Log.d(TAG, "V2SyncOrchestrator: post-sync Discard discarded=$discarded")
        }

        measurements.isNotEmpty()
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
