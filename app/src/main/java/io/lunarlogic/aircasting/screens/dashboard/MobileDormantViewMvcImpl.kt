package io.lunarlogic.aircasting.screens.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.sensor.Session


class MobileDormantViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?
): SessionsViewMvcImpl<DormantSessionViewMvc.Listener>(inflater, parent), DormantSessionViewMvc.Listener {

    override fun buildAdapter(inflater: LayoutInflater): SessionsRecyclerAdapter<DormantSessionViewMvc.Listener> {
        return MobileDormantRecyclerAdapter(
            inflater,
            this
        )
    }

    override fun onSessionDeleteClicked(session: Session) {
        for (listener in listeners) {
            listener.onDeleteSessionClicked(session.uuid)
        }
    }
}