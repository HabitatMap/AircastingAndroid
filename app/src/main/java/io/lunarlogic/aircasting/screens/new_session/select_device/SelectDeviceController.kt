package io.lunarlogic.aircasting.screens.new_session.select_device

import android.content.Context
import io.lunarlogic.aircasting.devices.Device

class SelectDeviceController(
    private val mContext: Context?,
    private val mViewMvc: SelectDeviceViewMvc
) {

    fun bindDevices() {
        val devices = listOf(Device("AirBeam2", ":0018961070D6"))
        mViewMvc.bindDevices(devices)
    }

    fun registerListener(listener: SelectDeviceViewMvc.Listener) {
        mViewMvc.registerListener(listener)
    }

    fun unregisterListener(listener: SelectDeviceViewMvc.Listener) {
        mViewMvc.unregisterListener(listener)
    }
}