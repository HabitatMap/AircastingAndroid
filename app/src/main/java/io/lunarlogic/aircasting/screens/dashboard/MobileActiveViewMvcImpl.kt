package io.lunarlogic.aircasting.screens.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.sensor.Session

class MobileActiveViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?
): SessionsViewMvcImpl<ActiveSessionViewMvc.Listener>(inflater, parent), ActiveSessionViewMvc.Listener {

    override fun buildAdapter(inflater: LayoutInflater): SessionsRecyclerAdapter<ActiveSessionViewMvc.Listener> {
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