package pl.llp.aircasting.util.helpers.sensor.common.connector

import android.content.Context
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.helpers.sensor.airbeamNonSyncable.connector.AirBeam2Connector
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.SyncableAirBeamConfiguratorFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.connector.SyncableAirBeamConnector

open class AirBeamConnectorFactory(
    private val applicationContext: Context,
    private val mErrorHandler: ErrorHandler,
    private val bluetoothManager: BluetoothManager,
    private val airBeam2Connector: AirBeam2Connector,
    private val syncableAirBeamConfiguratorFactory: SyncableAirBeamConfiguratorFactory,
) {
    open fun get(deviceItem: DeviceItem): AirBeamConnector {
        return when (deviceItem.type) {
            DeviceItem.Type.AIRBEAM3, DeviceItem.Type.AIRBEAMMINI -> SyncableAirBeamConnector(
                applicationContext,
                mErrorHandler,
                bluetoothManager,
                syncableAirBeamConfiguratorFactory.create(deviceItem.type)
            )

            else -> airBeam2Connector
        }
    }
}
