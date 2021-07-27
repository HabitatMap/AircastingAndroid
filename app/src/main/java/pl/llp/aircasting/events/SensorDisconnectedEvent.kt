package pl.llp.aircasting.events

import pl.llp.aircasting.screens.new_session.select_device.DeviceItem

class SensorDisconnectedEvent(val sessionDeviceId: String, val device: DeviceItem?, val sessionUUID: String?)
