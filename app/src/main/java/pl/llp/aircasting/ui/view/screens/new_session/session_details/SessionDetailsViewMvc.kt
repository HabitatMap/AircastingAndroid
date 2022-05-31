package pl.llp.aircasting.ui.view.screens.new_session.session_details

import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.ui.view.common.ObservableViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem


interface SessionDetailsViewMvc: ObservableViewMvc<SessionDetailsViewMvc.Listener> {
    interface Listener {
        fun onSessionDetailsContinueClicked(
            sessionUUID: String,
            deviceItem: DeviceItem,
            localSessionType: LocalSession.Type,
            sessionName: String,
            sessionTags: ArrayList<String>,
            indoor: Boolean = false,
            streamingMethod: LocalSession.StreamingMethod? = null,
            wifiName: String? = null,
            wifiPassword: String? = null
        )
        fun validationFailed(errorMessage: String)
    }
}
