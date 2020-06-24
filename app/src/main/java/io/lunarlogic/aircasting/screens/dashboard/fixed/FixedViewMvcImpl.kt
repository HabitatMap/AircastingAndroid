package io.lunarlogic.aircasting.screens.dashboard.fixed

import android.view.LayoutInflater
import android.view.ViewGroup
import io.lunarlogic.aircasting.screens.dashboard.SessionsRecyclerAdapter
import io.lunarlogic.aircasting.screens.dashboard.SessionsViewMvcImpl
import io.lunarlogic.aircasting.sensor.Session


class FixedViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?
): SessionsViewMvcImpl<FixedSessionViewMvc.Listener>(inflater, parent),
    FixedSessionViewMvc.Listener {

    override fun buildAdapter(inflater: LayoutInflater): SessionsRecyclerAdapter<FixedSessionViewMvc.Listener> {
        return FixedRecyclerAdapter(
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