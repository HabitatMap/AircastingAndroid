package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator

import android.content.Context
import android.content.Intent
import android.os.Build
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.reader.SyncableAirBeamReader
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardReader
import pl.llp.aircasting.util.helpers.sensor.common.HexMessagesBuilder
import pl.llp.aircasting.util.helpers.sensor.services.BatteryLevelService
import java.util.UUID

open class AirBeamMiniConfigurator(
    private val applicationContext: Context,
    mErrorHandler: ErrorHandler,
    private val mSettings: Settings,
    hexMessagesBuilder: HexMessagesBuilder,
    syncableAirBeamReader: SyncableAirBeamReader,
    sdCardReader: SDCardReader,
    await: RequestQueueCall,
) : SyncableAirBeamConfigurator(
    applicationContext,
    mErrorHandler,
    mSettings,
    hexMessagesBuilder,
    syncableAirBeamReader,
    sdCardReader,
    await
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

    override fun reconnectMobileSession() {
        super.reconnectMobileSession()

        if(mSettings.isBatteryLevelRestartRequired()) {
            val intent = Intent(applicationContext, BatteryLevelService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                applicationContext.startForegroundService(intent)
            } else {
                applicationContext.startService(intent)
            }
        }
    }
}