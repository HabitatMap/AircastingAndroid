package io.lunarlogic.aircasting.screens.new_session.session_details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem

class SessionDetailsViewFactory() {
    companion object {
        fun get(
            inflater: LayoutInflater,
            parent: ViewGroup?,
            fragmentManager: FragmentManager,
            deviceItem: DeviceItem,
            sessionUUID: String,
            sessionType: Session.Type
        ): SessionDetailsViewMvcImpl {
            return when(sessionType) {
                Session.Type.MOBILE -> MobileSessionDetailsViewMvcImpl(inflater, parent, sessionUUID, deviceItem)
                Session.Type.FIXED -> FixedSessionDetailsViewMvcImpl(inflater, parent, fragmentManager, sessionUUID, deviceItem)
            }
        }
    }
}
