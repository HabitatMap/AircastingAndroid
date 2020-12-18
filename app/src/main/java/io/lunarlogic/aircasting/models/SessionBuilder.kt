package io.lunarlogic.aircasting.models

import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem

class SessionBuilder {
    fun build(
        sessionUUID: String,
        deviceItem: DeviceItem,
        type: Session.Type,
        name: String,
        tags: ArrayList<String>,
        status: Session.Status,
        indoor: Boolean?,
        streamingMethod: Session.StreamingMethod?,
        currentLocation: Session.Location?
    ): Session {
        val location = calculateLocation(type, indoor, currentLocation)

        return Session(
            sessionUUID,
            deviceItem.id,
            deviceItem.type,
            type,
            name,
            tags,
            status,
            indoor,
            streamingMethod,
            location
        )
    }

    private fun calculateLocation(type: Session.Type, indoor: Boolean?, currentLocation: Session.Location?): Session.Location? {
        if (type == Session.Type.FIXED && indoor == true) {
            return Session.Location.INDOOR_FAKE_LOCATION
        }

        // mobile or outdoor
        return currentLocation
    }
}
