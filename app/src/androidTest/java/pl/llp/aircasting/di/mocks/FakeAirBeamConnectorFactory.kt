package pl.llp.aircasting.di.mocks

import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.helpers.sensor.common.connector.AirBeamConnector
import pl.llp.aircasting.util.helpers.sensor.common.connector.AirBeamConnectorFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamNonSyncable.configurator.AirBeam2Configurator
import pl.llp.aircasting.util.helpers.sensor.airbeamNonSyncable.reader.AirBeam2Reader
import pl.llp.aircasting.util.helpers.sensor.airbeamNonSyncable.connector.AirBeam2Connector
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.SyncableAirBeamConfiguratorFactory

class FakeAirBeamConnectorFactory(
    private val app: AircastingApplication,
    private val mErrorHandler: ErrorHandler,
    private val bluetoothManager: BluetoothManager,
    private val mAirBeamConfigurator: AirBeam2Configurator,
    private val mAirBeam2Reader: AirBeam2Reader,
    syncableAirBeamConfiguratorFactory: SyncableAirBeamConfiguratorFactory,
    airBeam2Connector: AirBeam2Connector,
) : AirBeamConnectorFactory(
    app.applicationContext,
    mErrorHandler,
    bluetoothManager,
    airBeam2Connector,
    syncableAirBeamConfiguratorFactory
) {
    override fun get(deviceItem: DeviceItem): AirBeamConnector {
        return FakeAirBeam2Connector(
            app,
            mErrorHandler,
            bluetoothManager,
            mAirBeamConfigurator,
            mAirBeam2Reader,
        )
    }
}
