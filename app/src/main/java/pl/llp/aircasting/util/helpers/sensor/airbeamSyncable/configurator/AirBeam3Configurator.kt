package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator

import android.content.Context
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.sensor.common.HexMessagesBuilder
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.reader.SyncableAirBeamReader
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardReader
import java.util.UUID

open class AirBeam3Configurator(
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
        private val temperatureSensorCharacteristic =
            UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
        private val humiditySensorCharacteristic =
            UUID.fromString("0000ffe3-0000-1000-8000-00805f9b34fb")
        private val pm10SensorCharacteristic =
            UUID.fromString("0000ffe6-0000-1000-8000-00805f9b34fb")
    }

    override val measurementsCharacteristicUUIDs: List<UUID> = listOf(
        temperatureSensorCharacteristic,
        humiditySensorCharacteristic,
        pm1SensorCharacteristic,
        pm2_5SensorCharacteristic,
        pm10SensorCharacteristic
    )
}