package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.llp.aircasting.di.UserSessionScope
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.events.LocationChanged
import pl.llp.aircasting.util.extensions.safeRegister
import java.util.Collections
import kotlin.math.abs

sealed class FixedSessionConfigureOutcome {
    object Success : FixedSessionConfigureOutcome()
    data class Failure(val reason: Reason, val errorCode: Int = -1) : FixedSessionConfigureOutcome()

    enum class Reason {
        INVALID_WIFI_CREDENTIALS,   // Nack 0x05 during NewSessionConfig
        FIRST_MEASUREMENT_FAILED,   // Nack 0x02 (InvalidConfig / first-measurement POST failure)
        OTHER_NACK,                 // Any other Nack
        WRITE_FAILED,               // BLE write failed outright
    }
}

@UserSessionScope
class AirBeamMiniV2StateRepository @Inject constructor() {

    var deviceState: AirBeamMiniV2Configurator.DeviceState = AirBeamMiniV2Configurator.DeviceState.UNKNOWN
        private set
    var hasSavedMeasurements: Boolean = false
        private set
    var savedSessionUuid: String? = null
        private set

    data class TrackedLocation(val latitude: Double, val longitude: Double, val time: Long)

    private val _trackedLocations = Collections.synchronizedList(mutableListOf<TrackedLocation>())
    val trackedLocations: List<TrackedLocation> get() = _trackedLocations

    private var isLocationTrackingActive = false

    private val _hasSavedMeasurementsFlow = MutableStateFlow(false)
    /**
     * Mirrors [hasSavedMeasurements] as a StateFlow so UI surfaces can react when the
     * firmware re-notifies `STATE_RUNNING` with the has-measurements byte cleared —
     * i.e. the BLE Active Sync stream on `0006` has finished flushing stored
     * measurements. Used by [SyncAndFinishV2SessionDialog] to auto-finalize.
     */
    val hasSavedMeasurementsFlow: StateFlow<Boolean> = _hasSavedMeasurementsFlow.asStateFlow()

    private val _activeSyncDrainingFlow = MutableStateFlow(false)
    /**
     * True while the BLE Active Sync stream on `0006` is actively delivering stored
     * measurements. Firmware does not include a `has_measurements` byte in the
     * `STATE_RUNNING` Status payload, so [hasSavedMeasurements] cannot be used to
     * detect mid-drain during an active mobile session. This flag is driven by the
     * arrival of sync indications: set true on each chunk, cleared by an idle
     * timeout in [AirBeamMiniV2Configurator] (no chunk for a few seconds = drain
     * complete). [SyncAndFinishV2SessionDialog] observes it to keep itself open
     * while the device is still pushing stored measurements.
     */
    val activeSyncDrainingFlow: StateFlow<Boolean> = _activeSyncDrainingFlow.asStateFlow()
    val isActiveSyncDraining: Boolean get() = _activeSyncDrainingFlow.value

    fun setActiveSyncDraining(value: Boolean) {
        _activeSyncDrainingFlow.value = value
    }

    // Bytes-on-disk reported by the firmware's HasSavedSession status (FW commit
    // `3990cf22`). Fed into [V2BleSyncOrchestrator.estimateSyncSeconds] so the
    // "sync before new session" dialog can render an ETA before the user starts.
    var savedSessionFileSize: Long = 0L
        private set

    private var syncCallback: (suspend (Boolean, (suspend () -> Unit)?) -> Boolean)? = null

    /**
     * True while [V2SyncOrchestrator.run] is executing. Used by lifecycle owners
     * (`AirBeamSyncService`, `AirBeamMiniFallbackConnector`) to skip teardown actions that
     * would otherwise cancel the in-flight HTTP download when BLE drops mid-sync (common
     * BLE+Wi-Fi coex symptom on Android 12 + ESP32).
     */
    private val _syncInProgress = AtomicBoolean(false)
    val syncInProgress: Boolean get() = _syncInProgress.get()

    fun setSyncInProgress(inProgress: Boolean) {
        _syncInProgress.set(inProgress)
    }

    private val _configureOutcome = MutableSharedFlow<FixedSessionConfigureOutcome>(
        replay = 0,
        extraBufferCapacity = 4,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val configureOutcome: SharedFlow<FixedSessionConfigureOutcome> = _configureOutcome.asSharedFlow()

    /**
     * Emits the SoftAP password each time the firmware sends `Status::ReadyToSync (0x03)`.
     * Replay = 1 so a late subscriber (orchestrator) still sees the most recent password.
     */
    private val _readyToSyncPassword = MutableSharedFlow<String>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val readyToSyncPassword: SharedFlow<String> = _readyToSyncPassword.asSharedFlow()

    /**
     * Sync file size (bytes) included in the firmware's `Status::ReadyToSync (0x03)`
     * payload as `u64_LE` directly after the opcode byte (FW commit `ed751b180`). The
     * orchestrator hands this to [V2SyncFileDownloader] so it can drive a 0-100%
     * progress UI off the per-block byte counter.
     */
    private val _readyToSyncFileSize = MutableSharedFlow<Long>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val readyToSyncFileSize: SharedFlow<Long> = _readyToSyncFileSize.asSharedFlow()

    /**
     * Manual-sync HTTP body progress in 0..100. Reset to 0 at the start of each
     * orchestrator run; reaches 100 when the body stream is fully consumed (or treated
     * as soft-complete via [readyToSyncFileSize]). Consumed by every UI surface that
     * drives manual sync — the SD-sync wizard syncing screen, the Sync-and-Finish
     * dialog, and the Sync-before-new-session dialog — so all entry points show the
     * same percentage from a single source of truth.
     */
    private val _syncProgress = MutableStateFlow(0)
    val syncProgress: StateFlow<Int> = _syncProgress.asStateFlow()

    fun update(
        state: AirBeamMiniV2Configurator.DeviceState,
        hasMeasurements: Boolean,
        sessionUuid: String?,
        savedFileSize: Long = 0L,
    ) {
        deviceState = state
        hasSavedMeasurements = hasMeasurements
        _hasSavedMeasurementsFlow.value = hasMeasurements
        savedSessionUuid = sessionUuid
        savedSessionFileSize = savedFileSize
    }

    fun setSyncCallback(callback: (suspend (Boolean, (suspend () -> Unit)?) -> Boolean)?) {
        syncCallback = callback
    }

    fun emitConfigureOutcome(outcome: FixedSessionConfigureOutcome) {
        _configureOutcome.tryEmit(outcome)
    }

    fun emitReadyToSyncPassword(password: String) {
        _readyToSyncPassword.tryEmit(password)
    }

    fun emitReadyToSyncFileSize(size: Long) {
        _readyToSyncFileSize.tryEmit(size)
    }

    fun setSyncProgress(percent: Int) {
        _syncProgress.value = percent.coerceIn(0, 100)
    }

    @Suppress("OPT_IN_USAGE")
    fun resetReadyToSyncPassword() {
        _readyToSyncPassword.resetReplayCache()
        _readyToSyncFileSize.resetReplayCache()
        _syncProgress.value = 0
    }

    @Suppress("OPT_IN_USAGE")
    fun reset() {
        // Keep hasSavedMeasurements and savedSessionUuid so DisconnectedView can still read
        // the last known state after BLE disconnect. parseStatus() will overwrite them on
        // the next connection.
        deviceState = AirBeamMiniV2Configurator.DeviceState.UNKNOWN
        syncCallback = null
        _syncInProgress.set(false)
        _activeSyncDrainingFlow.value = false
        _readyToSyncPassword.resetReplayCache()
        _readyToSyncFileSize.resetReplayCache()
        _syncProgress.value = 0
    }

    /**
     * @param keepConnectedAfter Skip the final voluntary BLE disconnect that the orchestrator
     * normally performs after the post-sync `DiscardSession`. Required when the caller plans
     * to write another command (e.g. `NewSessionConfig`) on the same GATT link right after.
     * @param onBeforePicker Optional suspend hook invoked just before the system Wi-Fi picker
     * is launched. Lets the calling UI surface an educational dialog and resume only after
     * the user confirms.
     */
    suspend fun startSync(
        keepConnectedAfter: Boolean = false,
        onBeforePicker: (suspend () -> Unit)? = null,
    ): Boolean = syncCallback?.invoke(keepConnectedAfter, onBeforePicker) ?: false

    fun startLocationTracking() {
        android.util.Log.d("V2StateRepository", "startLocationTracking")
        _trackedLocations.clear()
        isLocationTrackingActive = true
        EventBus.getDefault().safeRegister(this)
    }

    fun stopLocationTrackingAndClear() {
        android.util.Log.d("V2StateRepository", "stopLocationTrackingAndClear")
        isLocationTrackingActive = false
        _trackedLocations.clear()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    @Subscribe
    fun onMessageEvent(event: LocationChanged) {
        if (!isLocationTrackingActive) return
        val lat = event.latitude ?: return
        val lng = event.longitude ?: return
        _trackedLocations.add(TrackedLocation(lat, lng, event.time))
        android.util.Log.v("V2StateRepository", "Location tracked: $lat, $lng, time=${event.time}")
    }

    fun getClosestLocation(measurementTimeMs: Long, fallbackLocation: Session.Location): Session.Location {
        synchronized(_trackedLocations) {
            if (_trackedLocations.isEmpty()) {
                return fallbackLocation
            }
            var closest = _trackedLocations[0]
            var minDiff = abs(closest.time - measurementTimeMs)
            for (i in 1 until _trackedLocations.size) {
                val loc = _trackedLocations[i]
                val diff = abs(loc.time - measurementTimeMs)
                if (diff < minDiff) {
                    minDiff = diff
                    closest = loc
                }
            }
            android.util.Log.d("V2StateRepository", "Closest location found: ${closest.latitude}, ${closest.longitude} diff=${minDiff}ms")
            return Session.Location(closest.latitude, closest.longitude)
        }
    }
}
