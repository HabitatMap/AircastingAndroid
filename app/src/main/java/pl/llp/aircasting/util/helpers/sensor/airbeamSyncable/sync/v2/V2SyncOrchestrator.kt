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
 *   3. WiFi: join the SoftAP via [V2WifiApConnector].
 *   4. HTTP: GET `http://192.168.4.1/sync` and parse the streamed file.
 *   5. Process measurements:
 *      - MOBILE saved session → [V2MobileMeasurementsInserter] (V1 insert/skip-finished logic).
 *      - FIXED saved session → [V2FixedMeasurementsUploader] POST to backend.
 *      - Unknown UUID → log + drop (FW will clear storage anyway).
 *   6. BLE: wait for the firmware's final `Ready (0x22)` indicating sync done.
 *   7. BLE: send `DiscardSession (0x11)` to clear the saved-session reference.
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
        private const val FINAL_READY_TIMEOUT_MS = 60_000L
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

        // Step 1: kick off StartSync. The configurator's BLE state machine suspends until the
        // firmware's *final* Ready (0x22) arrives — which only happens after HTTP completes.
        // Run it as an async sibling so we can drive the WiFi+HTTP work concurrently.
        val startSyncDone: kotlinx.coroutines.Deferred<Boolean> = async {
            configurator.sendStartSyncManualAndAwaitDone()
        }

        // Step 2: wait for ReadyToSync(password) on the Status characteristic.
        val password = withTimeoutOrNull(PASSWORD_TIMEOUT_MS) {
            v2StateRepository.readyToSyncPassword.first()
        }
        if (password.isNullOrEmpty()) {
            Log.e(TAG, "V2SyncOrchestrator: timed out waiting for ReadyToSync password")
            startSyncDone.cancel()
            return@coroutineScope false
        }
        Log.d(TAG, "V2SyncOrchestrator: got SoftAP password (len=${password.length})")

        // Steps 3+4: join AP, GET /sync, parse measurements. Restore network on completion.
        val apConnector = V2WifiApConnector(applicationContext)
        val measurements: List<V2SyncMeasurement>? = apConnector.withApConnection(password) { network ->
            V2SyncFileDownloader().download(network)
        }

        if (measurements == null) {
            Log.e(TAG, "V2SyncOrchestrator: AP join failed")
            startSyncDone.cancel()
            return@coroutineScope false
        }
        Log.d(TAG, "V2SyncOrchestrator: downloaded ${measurements.size} measurements")

        // Step 5: route measurements to the right destination based on the saved session type.
        val savedUuid = v2StateRepository.savedSessionUuid
        val deviceId = configurator.deviceId
        if (savedUuid != null && deviceId != null && measurements.isNotEmpty()) {
            processMeasurements(savedUuid, deviceId, measurements)
        } else if (measurements.isEmpty()) {
            Log.d(TAG, "V2SyncOrchestrator: no measurements to process")
        } else {
            Log.w(TAG, "V2SyncOrchestrator: missing savedUuid=$savedUuid or deviceId=$deviceId — dropping ${measurements.size} measurements")
        }

        // Step 6: wait for the firmware's final Ready (0x22).
        val readyOk = withTimeoutOrNull(FINAL_READY_TIMEOUT_MS) { startSyncDone.await() } ?: false
        if (!readyOk) {
            Log.w(TAG, "V2SyncOrchestrator: timed out / Nack waiting for final Ready (0x22)")
            startSyncDone.cancel()
            return@coroutineScope false
        }

        // Step 7: discard the (now empty) session on FW so the device is clean for the next one.
        val discarded = configurator.sendDiscardSessionAndAwait()
        if (!discarded) {
            Log.w(TAG, "V2SyncOrchestrator: DiscardSession after sync did not Ready in time")
        }

        true
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
