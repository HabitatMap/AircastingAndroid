package pl.llp.aircasting.events

import pl.llp.aircasting.screens.new_session.select_device.DeviceItem

class AirBeamConnectionSuccessfulEvent(val deviceItem: DeviceItem, val sessionUUID: String?)
class AirBeamConnectionFailedEvent(val deviceItem: DeviceItem)
