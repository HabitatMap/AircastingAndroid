package pl.llp.aircasting.ui.view.screens.dashboard.reordering_following

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ItemTouchHelper
import pl.llp.aircasting.ui.view.screens.dashboard.SessionCardListener
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsRecyclerAdapter
import pl.llp.aircasting.ui.view.screens.dashboard.following.FollowingViewMvcImpl
import pl.llp.aircasting.util.FollowingSessionReorderingTouchHelperCallback
import pl.llp.aircasting.util.ItemTouchHelperAdapter

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
            mRecyclerSessions,
            inflater,
            this,
            supportFragmentManager
        )
    }

    // Below method attaches touch helper to our Reordering Recycler View
    override fun addTouchHelperToRecyclerView() {
        if (mAdapter is ItemTouchHelperAdapter) {
            val itemTouchCallback = FollowingSessionReorderingTouchHelperCallback(mAdapter)
            val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
            mAdapter.mItemTouchHelper = itemTouchHelper
            itemTouchHelper.attachToRecyclerView(mRecyclerSessions)
        }
    }
}
