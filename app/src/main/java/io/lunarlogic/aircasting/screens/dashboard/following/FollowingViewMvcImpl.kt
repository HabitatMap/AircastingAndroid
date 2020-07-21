package io.lunarlogic.aircasting.screens.dashboard.following

import android.view.LayoutInflater
import android.view.ViewGroup
import io.lunarlogic.aircasting.screens.dashboard.SessionsRecyclerAdapter
import io.lunarlogic.aircasting.screens.dashboard.SessionsViewMvcImpl
import io.lunarlogic.aircasting.sensor.Session

class FollowingViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?
): SessionsViewMvcImpl<FollowingSessionViewMvc.Listener>(inflater, parent),
    FollowingSessionViewMvc.Listener {

    override fun buildAdapter(inflater: LayoutInflater): SessionsRecyclerAdapter<FollowingSessionViewMvc.Listener> {
        return FollowingRecyclerAdapter(
            inflater,
            this
        )
    }
}