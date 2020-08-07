package io.lunarlogic.aircasting.screens.dashboard.mobile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.screens.dashboard.SessionsRecyclerAdapter
import io.lunarlogic.aircasting.screens.dashboard.SessionsViewMvcImpl
import io.lunarlogic.aircasting.sensor.Session


class MobileDormantViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager
): SessionsViewMvcImpl<MobileDormantSessionViewMvc.Listener>(inflater, parent, supportFragmentManager),
    MobileDormantSessionViewMvc.Listener {

    override fun buildAdapter(
        inflater: LayoutInflater,
        supportFragmentManager: FragmentManager
    ): SessionsRecyclerAdapter<MobileDormantSessionViewMvc.Listener> {
        return MobileDormantRecyclerAdapter(
            inflater,
            this,
            supportFragmentManager
        )
    }

    override fun onSessionDeleteClicked(session: Session) {
        for (listener in listeners) {
            listener.onDeleteSessionClicked(session.uuid)
        }
    }
}
