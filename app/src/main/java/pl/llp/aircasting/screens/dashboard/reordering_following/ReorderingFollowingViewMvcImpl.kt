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

    override fun sessionCardMoveInProgress() {
        Log.i("CARD_MOVED", "Session card move is in progress")
//        mRecyclerSessions.adapter.     ????
    }

    override fun layoutId(): Int { // TODO: these 3 below methods are not needed i guess
        return R.id.empty_dashboard
    }

    override fun showDidYouKnowBox(): Boolean {
        return false
    }

    override fun recordNewSessionButtonId(): Int {
        return R.id.dashboard_record_new_session_button
    }

}
