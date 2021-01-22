package io.lunarlogic.aircasting.screens.dashboard.dormant

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.dashboard.SessionsRecyclerAdapter
import io.lunarlogic.aircasting.screens.dashboard.SessionsViewMvcImpl
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.dashboard.EditSessionBottomSheet
import io.lunarlogic.aircasting.screens.lets_start.MoreInfoBottomSheet


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

    override fun onSessionEditClicked(session: Session) {
        for (listener in listeners) {
            listener.onEditSessionClicked(session)
        }
    }

    override fun onSessionShareClicked(session: Session) {
        for (listener in listeners) {
            listener.onShareSessionClicked(session)
        }
    }

    override fun onSessionDeleteClicked(session: Session) {
        for (listener in listeners) {
            listener.onDeleteSessionClicked(session.uuid)
        }
    }

    override fun layoutId(): Int {
        return R.id.empty_dashboard //todo: maybe empty_mobile_dashboard ???
    }

    override fun recordNewSessionButtonId(): Int {
        return R.id.dashboard_record_new_session_button
    }
}
