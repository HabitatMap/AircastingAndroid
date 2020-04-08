package io.lunarlogic.aircasting.screens.selectdevice.items

import io.lunarlogic.aircasting.devices.Device

class AirBeam2DeviceItem(private val mDevice: Device) : DeviceItem {
    override val title: String get() = mDevice.name
    override val id: String? get() = mDevice.id
    override val viewType: Int get() = 1
}