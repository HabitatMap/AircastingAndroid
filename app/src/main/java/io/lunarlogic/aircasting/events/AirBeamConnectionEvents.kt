package io.lunarlogic.aircasting.events

import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem

class AirBeamConnectionSuccessfulEvent(val deviceItem: DeviceItem, val sessionUUID: String?)
class AirBeamConnectionFailedEvent
