package pl.llp.aircasting.screens.dashboard.reordering_following

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.screens.dashboard.SessionCardListener
import pl.llp.aircasting.screens.dashboard.SessionsRecyclerAdapter
import pl.llp.aircasting.screens.dashboard.SessionsViewMvcImpl
import pl.llp.aircasting.screens.dashboard.following.FollowingRecyclerAdapter
import pl.llp.aircasting.screens.dashboard.following.FollowingViewMvcImpl

class ReorderingFollowingViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager
): FollowingViewMvcImpl(inflater, parent, supportFragmentManager),
    SessionCardListener {

    override fun buildAdapter(
        inflater: LayoutInflater,
        supportFragmentManager: FragmentManager
    ): SessionsRecyclerAdapter<SessionCardListener> {
        return ReorderingFollowingRecyclerAdapter(
            inflater,
            this,
            supportFragmentManager
        )
    }

}
