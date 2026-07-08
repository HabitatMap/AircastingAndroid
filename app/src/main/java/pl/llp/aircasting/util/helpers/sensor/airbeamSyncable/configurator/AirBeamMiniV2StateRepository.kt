package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.llp.aircasting.data.local.AppDatabase
import pl.llp.aircasting.data.local.entity.TrackedLocationDBObject
import pl.llp.aircasting.di.modules.IoCoroutineScope
import pl.llp.aircasting.di.UserSessionScope
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.events.LocationChanged
import pl.llp.aircasting.util.extensions.safeRegister
import pl.llp.aircasting.util.helpers.location.LocationHelper
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
class AirBeamMiniV2StateRepository @Inject constructor(
    private val mDatabase: AppDatabase,
    @IoCoroutineScope private val coroutineScope: CoroutineScope
) {

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
    private var currentIntervalSeconds: Int? = null

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

    init {
        coroutineScope.launch {
            val persisted = mDatabase.trackedLocations().getAll()
            synchronized(_trackedLocations) {
                _trackedLocations.clear()
                _trackedLocations.addAll(persisted.map {
                    TrackedLocation(it.latitude, it.longitude, it.time)
                })
            }
            val activeCount = mDatabase.sessions().getActiveSessionsCount()
            if (activeCount > 0) {
                android.util.Log.d("V2StateRepository", "Resuming location tracking on init, active count = $activeCount")
                isLocationTrackingActive = true
                EventBus.getDefault().safeRegister(this@AirBeamMiniV2StateRepository)
                updateLocationInterval()
            }
        }
    }

    fun startLocationTracking() {
        android.util.Log.d("V2StateRepository", "startLocationTracking")
        isLocationTrackingActive = true
        EventBus.getDefault().safeRegister(this)

        coroutineScope.launch {
            val activeCount = mDatabase.sessions().getActiveSessionsCount()
            if (activeCount <= 1) {
                synchronized(_trackedLocations) {
                    _trackedLocations.clear()
                }
                mDatabase.trackedLocations().deleteAll()
            }
            updateLocationInterval()
        }
    }

    fun stopLocationTrackingAndClear() {
        android.util.Log.d("V2StateRepository", "stopLocationTrackingAndClear")
        isLocationTrackingActive = false
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }

        coroutineScope.launch {
            val activeCount = mDatabase.sessions().getActiveSessionsCount()
            if (activeCount == 0) {
                synchronized(_trackedLocations) {
                    _trackedLocations.clear()
                }
                mDatabase.trackedLocations().deleteAll()
            }
            updateLocationInterval()
        }
    }

    private suspend fun updateLocationInterval() {
        val nonV2Count = mDatabase.sessions().getActiveNonV2SessionsCount()
        if (nonV2Count > 0) {
            android.util.Log.d("V2StateRepository", "updateLocationInterval: non-V2 active sessions exist, keeping default interval (1s)")
            currentIntervalSeconds = null
            LocationHelper.updateInterval(1000L)
            return
        }

        val activeV2Session = mDatabase.sessions().loadSessionByStatusTypeAndDeviceType(
            Session.Status.RECORDING,
            Session.Type.MOBILE,
            DeviceItem.Type.AIRBEAMMINI
        ) ?: mDatabase.sessions().loadSessionByStatusTypeAndDeviceType(
            Session.Status.DISCONNECTED,
            Session.Type.MOBILE,
            DeviceItem.Type.AIRBEAMMINI
        )

        if (activeV2Session != null && activeV2Session.measurementInterval != null) {
            val intervalMs = activeV2Session.measurementInterval * 1000L
            android.util.Log.d("V2StateRepository", "updateLocationInterval: setting location request interval to ${activeV2Session.measurementInterval}s")
            currentIntervalSeconds = activeV2Session.measurementInterval
            LocationHelper.updateInterval(intervalMs)
        } else {
            android.util.Log.d("V2StateRepository", "updateLocationInterval: no active V2 sessions, restoring default interval (1s)")
            currentIntervalSeconds = null
            LocationHelper.updateInterval(1000L)
        }
    }

    @Subscribe
    fun onMessageEvent(event: LocationChanged) {
        if (!isLocationTrackingActive) return
        val lat = event.latitude ?: return
        val lng = event.longitude ?: return

        val intervalSeconds = currentIntervalSeconds ?: 1
        val lastLocationTime = synchronized(_trackedLocations) {
            _trackedLocations.lastOrNull()?.time
        }

        if (lastLocationTime != null) {
            val minDiffMs = (intervalSeconds * 1000L * 0.9).toLong()
            if (event.time - lastLocationTime < minDiffMs) {
                // Throttle: discard location updates that are too frequent for the configured interval
                return
            }
        }

        val tracked = TrackedLocation(lat, lng, event.time)
        synchronized(_trackedLocations) {
            _trackedLocations.add(tracked)
        }
        android.util.Log.v("V2StateRepository", "Location tracked: $lat, $lng, time=${event.time}")

        coroutineScope.launch {
            mDatabase.trackedLocations().insert(TrackedLocationDBObject(lat, lng, event.time))
        }
    }

    /**
     * Closest tracked location to [measurementTimeMs] by time. [_trackedLocations] is kept in
     * ascending `time` order (locations arrive chronologically; the DB reload is ordered), so
     * this binary-searches the insertion point and compares the two neighbours — O(log N)
     * instead of the old O(N) linear scan. This is called ~2× per synced measurement, so for a
     * long session (hundreds of thousands of records × a large location list) the linear scan
     * plus a per-call log throttled the sync-insert consumer badly; the binary search keeps it
     * fast. No per-call logging for the same reason.
     */
    fun getClosestLocation(measurementTimeMs: Long, fallbackLocation: Session.Location): Session.Location {
        synchronized(_trackedLocations) {
            val size = _trackedLocations.size
            if (size == 0) return fallbackLocation

            var lo = 0
            var hi = size - 1
            while (lo < hi) {
                val mid = (lo + hi) ushr 1
                if (_trackedLocations[mid].time < measurementTimeMs) lo = mid + 1 else hi = mid
            }
            // lo = first entry with time >= target (or the last entry). Compare with its
            // predecessor to pick the nearer of the two straddling neighbours.
            var closest = _trackedLocations[lo]
            var minDiff = abs(closest.time - measurementTimeMs)
            if (lo > 0) {
                val prev = _trackedLocations[lo - 1]
                val prevDiff = abs(prev.time - measurementTimeMs)
                if (prevDiff < minDiff) {
                    minDiff = prevDiff
                    closest = prev
                }
            }
            return Session.Location(closest.latitude, closest.longitude)
        }
    }
}
