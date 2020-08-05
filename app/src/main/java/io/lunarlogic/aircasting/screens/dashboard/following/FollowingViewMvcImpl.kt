package io.lunarlogic.aircasting.screens.dashboard.following

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.screens.dashboard.SessionsRecyclerAdapter
import io.lunarlogic.aircasting.screens.dashboard.SessionsViewMvcImpl
import io.lunarlogic.aircasting.sensor.Session

class FollowingViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    context: Context,
    supportFragmentManager: FragmentManager
): SessionsViewMvcImpl<FollowingSessionViewMvc.Listener>(inflater, parent, context, supportFragmentManager),
    FollowingSessionViewMvc.Listener {

    override fun buildAdapter(
        inflater: LayoutInflater,
        context: Context,
        supportFragmentManager: FragmentManager
    ): SessionsRecyclerAdapter<FollowingSessionViewMvc.Listener> {
        return FollowingRecyclerAdapter(
            inflater,
            this,
            context,
            supportFragmentManager
        )
    }
}
