package io.lunarlogic.aircasting.screens.new_session.select_device.items

class AddNewDeviceItem : DeviceItem {
    override val title: String get() = "Add new"
    override val id: String? get() = null
    override val viewType: Int get() = ADD_NEW_DEVICE_VIEW_TYPE
}