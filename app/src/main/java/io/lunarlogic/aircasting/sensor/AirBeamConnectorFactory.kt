package io.lunarlogic.aircasting.sensor

import android.content.Context
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import io.lunarlogic.aircasting.sensor.airbeam2.AirBeam2Connector
import io.lunarlogic.aircasting.sensor.airbeam3.AirBeam3Connector

open class AirBeamConnectorFactory(
    private val mContext: Context,
    private val mSettings: Settings,
    private val mErrorHandler: ErrorHandler
) {
    open fun get(deviceItem: DeviceItem): AirBeamConnector? {
        when(deviceItem.type) {
            DeviceItem.Type.AIRBEAM2 -> return AirBeam2Connector(mContext, mSettings, mErrorHandler)
            DeviceItem.Type.AIRBEAM3 -> return AirBeam3Connector(mContext, mSettings, mErrorHandler)
            else -> return AirBeam2Connector(mContext, mSettings, mErrorHandler)
        }
    }
}
