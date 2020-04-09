package io.lunarlogic.aircasting.screens.select_device

import android.content.Context
import io.lunarlogic.aircasting.devices.Device
import io.lunarlogic.aircasting.screens.select_device.items.DeviceItem

class SelectDeviceController(
    private val mContext: Context?,
    private val mViewMvc: SelectDeviceViewMvc
) : SelectDeviceViewMvc.Listener {

    fun onStart() {
        mViewMvc.registerListener(this)

        val devices = listOf(Device("AirBeam2", ":0018961070D6"))
        mViewMvc.bindDevices(devices)
    }

    fun onStop() {
        mViewMvc.unregisterListener(this)
    }

    override fun onDeviceItemSelected(deviceItem: DeviceItem) {
        System.out.println("onDeviceSelected! " + deviceItem.title)
    }
}