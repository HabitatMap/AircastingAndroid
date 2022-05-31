package pl.llp.aircasting.ui.view.screens.new_session.session_details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem

class SessionDetailsViewFactory {
    companion object {
        fun get(
            inflater: LayoutInflater,
            parent: ViewGroup?,
            fragmentManager: FragmentManager,
            deviceItem: DeviceItem,
            sessionUUID: String,
            localSessionType: LocalSession.Type
        ): SessionDetailsViewMvc {
            return when(localSessionType) {
                LocalSession.Type.MOBILE -> MobileSessionDetailsViewMvcImpl(inflater, parent, sessionUUID, deviceItem)
                LocalSession.Type.FIXED -> FixedSessionDetailsViewMvcImpl(inflater, parent, fragmentManager, sessionUUID, deviceItem)
            }
        }
    }
}
