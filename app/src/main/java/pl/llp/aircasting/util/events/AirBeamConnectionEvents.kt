package pl.llp.aircasting.util.events

import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem

class AirBeamConnectionSuccessfulEvent(val deviceItem: DeviceItem, val sessionUUID: String?)
class AirBeamConnectionFailedEvent(val deviceItem: DeviceItem)
class AirBeamDiscoveryFailedEvent()
