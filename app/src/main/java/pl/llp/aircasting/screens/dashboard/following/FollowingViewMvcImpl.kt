package pl.llp.aircasting.screens.dashboard.following

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ItemTouchHelper
import pl.llp.aircasting.R
import pl.llp.aircasting.lib.FollowingSessionReorderingTouchHelperCallback
import pl.llp.aircasting.lib.ItemTouchHelperAdapter
import pl.llp.aircasting.screens.dashboard.SessionCardListener
import pl.llp.aircasting.screens.dashboard.SessionsRecyclerAdapter
import pl.llp.aircasting.screens.dashboard.SessionsViewMvcImpl
import pl.llp.aircasting.screens.dashboard.reordering_dashboard.ReorderingFollowingRecyclerAdapter

open class FollowingViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager,
    val isReordering: Boolean
): SessionsViewMvcImpl<SessionCardListener>(inflater, parent, supportFragmentManager),
    SessionCardListener {

    override fun buildAdapter(
        inflater: LayoutInflater,
        supportFragmentManager: FragmentManager
    ): SessionsRecyclerAdapter<SessionCardListener> {
        if (!isReordering) { // TODO: I dont get why this is always "false" even if i put "true" to the constructor
            return ReorderingFollowingRecyclerAdapter(
                inflater,
                this,
                supportFragmentManager
            )
        } else {
            return FollowingRecyclerAdapter(
                inflater,
                this,
                supportFragmentManager
            )
        }
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

    override fun addTouchHelperToRecyclerView() {
        // Do nothing here
    }
<<<<<<< HEAD

=======
>>>>>>> e73675a7 (rebase 7)
}
