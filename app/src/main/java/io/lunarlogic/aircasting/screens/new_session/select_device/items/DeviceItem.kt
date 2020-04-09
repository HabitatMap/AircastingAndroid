package io.lunarlogic.aircasting.screens.new_session.select_device.items

interface DeviceItem {
    val title: String
    val id: String?
    val viewType: Int
}

val ADD_NEW_DEVICE_VIEW_TYPE = 0
val SELECT_AIRBEAM2_VIEW_TYPE = 1