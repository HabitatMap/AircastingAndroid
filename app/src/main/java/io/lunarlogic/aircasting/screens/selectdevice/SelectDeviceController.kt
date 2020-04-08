package io.lunarlogic.aircasting.screens.selectdevice

import android.content.Context
import io.lunarlogic.aircasting.devices.Device

class SelectDeviceController(
    private val mContext: Context?,
    private val mViewMvc: SelectDeviceViewMvc
) : SelectDeviceViewMvc.Listener {

    fun onStart() {
        mViewMvc.registerListener(this)

        val devices = listOf(Device("Add new device", "add-new-device"), Device("AirBeam2", ":0018961070D6"))
        mViewMvc.bindDevices(devices)
    }

    fun onStop() {
        mViewMvc.unregisterListener(this)
    }

    override fun onDeviceSelected(device: Device) {
        System.out.println("onDeviceSelected! " + device.name)
    }
}