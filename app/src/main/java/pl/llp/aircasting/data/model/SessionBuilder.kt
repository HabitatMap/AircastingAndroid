package pl.llp.aircasting.data.model

import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem

class SessionBuilder {
    fun build(
        sessionUUID: String,
        deviceItem: DeviceItem,
        type: LocalSession.Type,
        name: String,
        tags: ArrayList<String>,
        status: LocalSession.Status,
        indoor: Boolean,
        streamingMethod: LocalSession.StreamingMethod?,
        currentLocation: LocalSession.Location?,
        settings: Settings
    ): LocalSession {
        val location = calculateLocation(type, indoor, currentLocation)
        val contribute = when(type) {
            LocalSession.Type.FIXED -> true
            LocalSession.Type.MOBILE -> settings.isCrowdMapEnabled()
        }
        val locationless = settings.areMapsDisabled()

        return LocalSession(
            sessionUUID,
            deviceItem.id,
            deviceItem.type,
            type,
            name,
            tags,
            status,
            indoor,
            streamingMethod,
            location,
            contribute,
            locationless
        )
    }

    private fun calculateLocation(type: LocalSession.Type, indoor: Boolean?, currentLocation: LocalSession.Location?): LocalSession.Location? {
        if (type == LocalSession.Type.FIXED && indoor == true) {
            return LocalSession.Location.FAKE_LOCATION
        }

        // mobile or outdoor
        return currentLocation
    }
}
