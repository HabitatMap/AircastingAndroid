package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.content.Context
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import io.lunarlogic.aircasting.sensor.airbeam2.AirBeam2Connector

class ConnectingAirBeamController(
    mContext: Context,
    private val mListener: Listener
) {
    interface Listener {
        fun onConnectionSuccessful(deviceId: String)
        fun onConnectionCancel()
    }

    fun onBackPressed() {
        mListener.onConnectionCancel()
    }
}
