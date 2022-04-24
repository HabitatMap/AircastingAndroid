package pl.llp.aircasting.util.events

import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem

class SensorDisconnectedEvent(val sessionDeviceId: String, val device: DeviceItem?, val sessionUUID: String?)
