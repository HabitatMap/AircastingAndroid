package pl.llp.aircasting.screens.dashboard.following

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.screens.dashboard.SessionCardListener
import pl.llp.aircasting.screens.dashboard.SessionsRecyclerAdapter
import pl.llp.aircasting.screens.dashboard.SessionsViewMvcImpl

open class FollowingViewMvcImpl(
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

    override fun showDidYouKnowBox(): Boolean {
        return false
    }

    override fun recordNewSessionButtonId(): Int {
        return R.id.dashboard_record_new_session_button
    }

}
