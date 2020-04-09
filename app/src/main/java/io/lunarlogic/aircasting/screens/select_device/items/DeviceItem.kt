package io.lunarlogic.aircasting.screens.select_device.items

interface DeviceItem {
    val title: String
    val id: String?
    val viewType: Int
}