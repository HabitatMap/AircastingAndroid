package io.lunarlogic.aircasting.screens.dashboard.following

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.screens.dashboard.SessionsRecyclerAdapter
import io.lunarlogic.aircasting.screens.dashboard.SessionsViewMvcImpl
import io.lunarlogic.aircasting.sensor.Session

class FollowingViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager
): SessionsViewMvcImpl<FollowingSessionViewMvc.Listener>(inflater, parent, supportFragmentManager),
    FollowingSessionViewMvc.Listener {

    override fun buildAdapter(
        inflater: LayoutInflater,
        supportFragmentManager: FragmentManager
    ): SessionsRecyclerAdapter<FollowingSessionViewMvc.Listener> {
        return FollowingRecyclerAdapter(
            inflater,
            this,
            supportFragmentManager
        )
    }

    override fun onMapButtonClicked(session: Session) {
        for (listener in listeners) {
            listener.onMapButtonClicked(session.uuid)
        }
    }
}
