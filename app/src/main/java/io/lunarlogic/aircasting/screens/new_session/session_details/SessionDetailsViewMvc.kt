package io.lunarlogic.aircasting.screens.new_session.session_details

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem


interface SessionDetailsViewMvc: ObservableViewMvc<SessionDetailsViewMvc.Listener> {
    interface Listener {
        fun onSessionDetailsContinueClicked(
            sessionUUID: String,
            deviceItem: DeviceItem,
            sessionType: Session.Type,
            sessionName: String,
            sessionTags: ArrayList<String>,
            indoor: Boolean? = null,
            streamingMethod: Session.StreamingMethod? = null,
            wifiName: String? = null,
            wifiPassword: String? = null
        )
        fun validationFailed(errorMessage: String)
    }
}
