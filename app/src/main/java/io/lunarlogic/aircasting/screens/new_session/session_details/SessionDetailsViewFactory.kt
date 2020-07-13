package io.lunarlogic.aircasting.screens.new_session.session_details

import android.view.LayoutInflater
import android.view.ViewGroup
import io.lunarlogic.aircasting.sensor.Session

class SessionDetailsViewFactory() {
    companion object {
        fun get(
            inflater: LayoutInflater,
            parent: ViewGroup?,
            deviceId: String,
            sessionType: Session.Type
        ): SessionDetailsViewMvc {
            return when(sessionType) {
                Session.Type.MOBILE -> MobileSessionDetailsViewMvcImpl(inflater, parent, deviceId)
                Session.Type.FIXED -> FixedSessionDetailsViewMvcImpl(inflater, parent, deviceId)
            }
        }
    }
}