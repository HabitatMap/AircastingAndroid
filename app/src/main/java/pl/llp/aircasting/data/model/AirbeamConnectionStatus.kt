package pl.llp.aircasting.data.model

import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem

data class AirbeamConnectionStatus(
    val deviceItem: DeviceItem? = null,
    val sessionUUID: String? = null,
    val isConnected: Boolean,
)
