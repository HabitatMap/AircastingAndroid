package pl.llp.aircasting.di.mocks

import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.sensor.AirBeamConnector
import pl.llp.aircasting.sensor.AirBeamConnectorFactory

class FakeAirBeamConnectorFactory(
    private val app: AircastingApplication,
    private val mSettings: Settings,
    private val mErrorHandler: ErrorHandler
): AirBeamConnectorFactory(app, mSettings, mErrorHandler) {
    override fun get(deviceItem: DeviceItem): AirBeamConnector? {
        return FakeAirBeam2Connector(
            app,
            mSettings,
            mErrorHandler
        )
    }
}
