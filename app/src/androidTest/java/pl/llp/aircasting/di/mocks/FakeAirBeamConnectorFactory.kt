package pl.llp.aircasting.di.mocks

import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.helpers.sensor.AirBeamConnector
import pl.llp.aircasting.util.helpers.sensor.AirBeamConnectorFactory

class FakeAirBeamConnectorFactory(
    private val app: AircastingApplication,
    private val mSettings: Settings,
    private val mErrorHandler: ErrorHandler,
    private val bluetoothManager: BluetoothManager,
    ): AirBeamConnectorFactory(app, mSettings, mErrorHandler, bluetoothManager) {
    override fun get(deviceItem: DeviceItem): AirBeamConnector {
        return FakeAirBeam2Connector(
            app,
            mSettings,
            mErrorHandler,
            bluetoothManager
        )
    }
}
