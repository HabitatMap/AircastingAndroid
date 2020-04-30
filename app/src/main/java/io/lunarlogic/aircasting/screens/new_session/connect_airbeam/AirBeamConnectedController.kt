package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.content.Context
import android.os.Messenger
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.screens.new_session.select_device.items.DeviceItem
import io.lunarlogic.aircasting.sensor.airbeam2.AirBeam2Connector

class AirBeamConnectedController(
    private val mContext: Context?,
    private val mViewMvc: AirBeamConnectedViewMvc
) {

    fun registerListener(listener: AirBeamConnectedViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    fun unregisterListener(listener: AirBeamConnectedViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }
}