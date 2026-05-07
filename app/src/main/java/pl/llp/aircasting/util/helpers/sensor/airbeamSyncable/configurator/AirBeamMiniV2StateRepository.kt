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
     * orchestrator hands this to [V2SyncFileDownloader] so it can (1) drive a 0-100%
     * progress UI and (2) treat a trailing TCP abort as soft-success when all bytes
     * were already received — works around FW's deauth-before-flush teardown race.
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
    ) {
        deviceState = state
        hasSavedMeasurements = hasMeasurements
        savedSessionUuid = sessionUuid
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
}
