package io.lunarlogic.aircasting.screens.dashboard.following

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.dashboard.SessionCardListener
import io.lunarlogic.aircasting.screens.dashboard.SessionsRecyclerAdapter
import io.lunarlogic.aircasting.screens.dashboard.SessionsViewMvcImpl

class FollowingViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager
): SessionsViewMvcImpl<SessionCardListener>(inflater, parent, supportFragmentManager),
    SessionCardListener {

    override fun buildAdapter(
        inflater: LayoutInflater,
        supportFragmentManager: FragmentManager
    ): SessionsRecyclerAdapter<SessionCardListener> {
        return FollowingRecyclerAdapter(
            inflater,
            this,
            supportFragmentManager
        )
    }

    override fun layoutId(): Int {
        return R.id.empty_dashboard
    }
}
