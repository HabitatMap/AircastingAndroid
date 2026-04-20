package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator

import pl.llp.aircasting.di.UserSessionScope
import javax.inject.Inject

@UserSessionScope
class AirBeamMiniV2StateRepository @Inject constructor() {

    var deviceState: AirBeamMiniV2Configurator.DeviceState = AirBeamMiniV2Configurator.DeviceState.UNKNOWN
        private set
    var hasSavedMeasurements: Boolean = false
        private set
    var savedSessionUuid: String? = null
        private set

    private var syncCallback: (suspend () -> Boolean)? = null

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

    fun reset() {
        deviceState = AirBeamMiniV2Configurator.DeviceState.UNKNOWN
        hasSavedMeasurements = false
        savedSessionUuid = null
        syncCallback = null
    }

    suspend fun startSync(): Boolean = syncCallback?.invoke() ?: false
}
