package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.content.Context
import io.lunarlogic.aircasting.screens.new_session.select_device.items.DeviceItem
import io.lunarlogic.aircasting.sensor.Session
import io.lunarlogic.aircasting.sensor.airbeam2.AirBeam2Connector

class ConnectingAirBeamController(
    mContext: Context,
    private val deviceItem: DeviceItem,
    private val airbeam2Connector: AirBeam2Connector,
    mListener: Listener
) {
    interface Listener {
        fun onConnectionSuccessful(deviceId: String)
    }

    init {
        airbeam2Connector.listener = mListener
    }

    fun onStart() {
        airbeam2Connector.connect(deviceItem)
    }

    fun onBackPressed() {
        airbeam2Connector.cancel()
    }
}