package pl.llp.aircasting.util.events

import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem

class StartRecordingEvent(
    val session: Session,
    val wifiSSID: String?,
    val wifiPassword: String?,
    val firmwareVersion: DeviceItem.FirmwareVersion = DeviceItem.FirmwareVersion.V1,
    val intervalSeconds: Int? = null,
)
