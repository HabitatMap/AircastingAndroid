package io.lunarlogic.aircasting.screens.new_session.select_device.items

interface DeviceItem {
    val title: String
    val id: String?
    val viewType: Int
}