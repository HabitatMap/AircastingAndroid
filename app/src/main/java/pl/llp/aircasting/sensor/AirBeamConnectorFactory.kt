package pl.llp.aircasting.sensor

import android.content.Context
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.sensor.airbeam2.AirBeam2Connector
import pl.llp.aircasting.sensor.airbeam3.AirBeam3Connector

open class AirBeamConnectorFactory(
    private val mContext: Context,
    private val mSettings: Settings,
    private val mErrorHandler: ErrorHandler
) {
    open fun get(deviceItem: DeviceItem): AirBeamConnector? {
        when(deviceItem.type) {
            DeviceItem.Type.AIRBEAM2 -> return AirBeam2Connector(mSettings, mErrorHandler)
            DeviceItem.Type.AIRBEAM3 -> return AirBeam3Connector(mContext, mSettings, mErrorHandler)
            else -> return AirBeam2Connector(mSettings, mErrorHandler)
        }
    }
}
