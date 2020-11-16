package io.lunarlogic.aircasting.screens.new_session.session_details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.models.Session

class SessionDetailsViewFactory() {
    companion object {
        fun get(
            inflater: LayoutInflater,
            parent: ViewGroup?,
            fragmentManager: FragmentManager,
            deviceId: String,
            sessionUUID: String,
            sessionType: Session.Type
        ): SessionDetailsViewMvc {
            return when(sessionType) {
                Session.Type.MOBILE -> MobileSessionDetailsViewMvcImpl(inflater, parent, sessionUUID, deviceId)
                Session.Type.FIXED -> FixedSessionDetailsViewMvcImpl(inflater, parent, fragmentManager, sessionUUID, deviceId)
            }
        }
    }
}
