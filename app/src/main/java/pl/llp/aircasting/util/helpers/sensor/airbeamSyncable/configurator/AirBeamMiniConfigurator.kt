package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator

import android.content.Context
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.sensor.common.HexMessagesBuilder
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.reader.SyncableAirBeamReader
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardReader
import java.util.UUID

open class AirBeamMiniConfigurator(
    applicationContext: Context,
    mErrorHandler: ErrorHandler,
    mSettings: Settings,
    hexMessagesBuilder: HexMessagesBuilder,
    syncableAirBeamReader: SyncableAirBeamReader,
    sdCardReader: SDCardReader,
) : SyncableAirBeamConfigurator(
    applicationContext,
    mErrorHandler,
    mSettings,
    hexMessagesBuilder,
    syncableAirBeamReader,
    sdCardReader
) {
    companion object {
        private val batteryLevelCharacteristic: UUID =
            UUID.fromString("0000ffe7-0000-1000-8000-00805f9b34fb")
    }

    override val measurementsCharacteristicUUIDs: List<UUID> = listOf(
        pm1SensorCharacteristic,
        pm2_5SensorCharacteristic,
        batteryLevelCharacteristic,
    )
}