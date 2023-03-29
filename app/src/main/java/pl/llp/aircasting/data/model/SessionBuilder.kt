package pl.llp.aircasting.data.model

import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.Settings
import javax.inject.Inject

class SessionBuilder @Inject constructor() {
    fun build(
        sessionUUID: String,
        deviceItem: DeviceItem,
        type: Session.Type,
        name: String,
        tags: ArrayList<String>,
        status: Session.Status,
        indoor: Boolean,
        streamingMethod: Session.StreamingMethod?,
        currentLocation: Session.Location?,
        settings: Settings
    ): Session {
        val location = calculateLocation(type, indoor, currentLocation)
        val contribute = when(type) {
            Session.Type.FIXED -> true
            Session.Type.MOBILE -> settings.isCrowdMapEnabled()
        }
        val locationless = settings.areMapsDisabled()

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
            location,
            contribute,
            locationless
        )
    }

    private fun calculateLocation(type: Session.Type, indoor: Boolean?, currentLocation: Session.Location?): Session.Location? {
        if (type == Session.Type.FIXED && indoor == true) {
            return Session.Location.FAKE_LOCATION
        }

        // mobile or outdoor
        return currentLocation
    }
}
