package io.lunarlogic.aircasting.di

import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import io.lunarlogic.aircasting.sensor.AirBeamConnector
import io.lunarlogic.aircasting.sensor.AirBeamConnectorFactory

class FakeAirBeamConnectorFactory(
    private val app: AircastingApplication,
    private val mSettings: Settings,
    private val mErrorHandler: ErrorHandler
): AirBeamConnectorFactory(app, mSettings, mErrorHandler) {
    override fun get(deviceItem: DeviceItem): AirBeamConnector? {
        return FakeAirBeam2Connector(app, mSettings, mErrorHandler)
    }
}
