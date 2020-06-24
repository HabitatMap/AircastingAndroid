package io.lunarlogic.aircasting.screens.dashboard.mobile

import android.view.LayoutInflater
import android.view.ViewGroup
import io.lunarlogic.aircasting.screens.dashboard.SessionsRecyclerAdapter
import io.lunarlogic.aircasting.screens.dashboard.SessionsViewMvcImpl
import io.lunarlogic.aircasting.sensor.Session

class MobileActiveViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?
): SessionsViewMvcImpl<MobileActiveSessionViewMvc.Listener>(inflater, parent),
    MobileActiveSessionViewMvc.Listener {

    override fun buildAdapter(inflater: LayoutInflater): SessionsRecyclerAdapter<MobileActiveSessionViewMvc.Listener> {
        return MobileActiveRecyclerAdapter(
            inflater,
            this
        )
    }

    override fun onSessionStopClicked(session: Session) {
        for (listener in listeners) {
            listener.onStopSessionClicked(session.uuid)
        }
    }
}