package pl.llp.aircasting.di.mocks

import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.helpers.sensor.AirBeamConnector
import pl.llp.aircasting.util.helpers.sensor.AirBeamConnectorFactory
import pl.llp.aircasting.util.helpers.sensor.airbeam2.AirBeam2Configurator
import pl.llp.aircasting.util.helpers.sensor.airbeam2.AirBeam2Reader
import pl.llp.aircasting.util.helpers.sensor.airbeam2.NonSyncableAirBeamConnector
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.SyncableAirBeamConfiguratorFactory

class FakeAirBeamConnectorFactory(
    private val app: AircastingApplication,
    private val mErrorHandler: ErrorHandler,
    private val bluetoothManager: BluetoothManager,
    private val mAirBeamConfigurator: AirBeam2Configurator,
    private val mAirBeam2Reader: AirBeam2Reader,
    syncableAirBeamConfiguratorFactory: SyncableAirBeamConfiguratorFactory,
    nonSyncableAirBeamConnector: NonSyncableAirBeamConnector,
) : AirBeamConnectorFactory(
    app.applicationContext,
    mErrorHandler,
    bluetoothManager,
    nonSyncableAirBeamConnector,
    syncableAirBeamConfiguratorFactory
) {
    override fun get(deviceItem: DeviceItem): AirBeamConnector {
        return FakeNonSyncableAirBeamConnector(
            app,
            mErrorHandler,
            bluetoothManager,
            mAirBeamConfigurator,
            mAirBeam2Reader,
        )
    }
}
