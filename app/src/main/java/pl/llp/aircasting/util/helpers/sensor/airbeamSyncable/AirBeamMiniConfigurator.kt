package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable

import android.content.Context
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.sensor.HexMessagesBuilder
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardReader
import java.util.UUID

class AirBeamMiniConfigurator(
    applicationContext: Context,
    mErrorHandler: ErrorHandler,
    mSettings: Settings,
    hexMessagesBuilder: HexMessagesBuilder,
    airBeam3Reader: AirBeam3Reader,
    sdCardReader: SDCardReader,
) : SyncableAirBeamConfigurator(
    applicationContext,
    mErrorHandler,
    mSettings,
    hexMessagesBuilder,
    airBeam3Reader,
    sdCardReader
) {
    override val measurementsCharacteristicUUIDs: List<UUID> = listOf(
        pm1SensorCharacteristic,
        pm2_5SensorCharacteristic
    )
}