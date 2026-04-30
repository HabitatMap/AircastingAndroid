package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import pl.llp.aircasting.di.UserSessionScope
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

    private var syncCallback: (suspend () -> Boolean)? = null

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

    fun update(
        state: AirBeamMiniV2Configurator.DeviceState,
        hasMeasurements: Boolean,
        sessionUuid: String?,
    ) {
        deviceState = state
        hasSavedMeasurements = hasMeasurements
        savedSessionUuid = sessionUuid
    }

    fun setSyncCallback(callback: (suspend () -> Boolean)?) {
        syncCallback = callback
    }

    fun emitConfigureOutcome(outcome: FixedSessionConfigureOutcome) {
        _configureOutcome.tryEmit(outcome)
    }

    fun emitReadyToSyncPassword(password: String) {
        _readyToSyncPassword.tryEmit(password)
    }

    @Suppress("OPT_IN_USAGE")
    fun resetReadyToSyncPassword() {
        _readyToSyncPassword.resetReplayCache()
    }

    @Suppress("OPT_IN_USAGE")
    fun reset() {
        // Keep hasSavedMeasurements and savedSessionUuid so DisconnectedView can still read
        // the last known state after BLE disconnect. parseStatus() will overwrite them on
        // the next connection.
        deviceState = AirBeamMiniV2Configurator.DeviceState.UNKNOWN
        syncCallback = null
        _readyToSyncPassword.resetReplayCache()
    }

    suspend fun startSync(): Boolean = syncCallback?.invoke() ?: false
}
