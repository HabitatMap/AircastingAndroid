package pl.llp.aircasting.ui.view.screens.dashboard.reordering_following

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ItemTouchHelper
import pl.llp.aircasting.util.FollowingSessionReorderingTouchHelperCallback
import pl.llp.aircasting.util.ItemTouchHelperAdapter
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsRecyclerAdapter
import pl.llp.aircasting.ui.view.screens.dashboard.fixed.FixedSessionViewMvc
import pl.llp.aircasting.ui.view.screens.dashboard.following.FollowingSessionViewMvc
import pl.llp.aircasting.ui.view.screens.dashboard.following.FollowingViewMvcImpl

class ReorderingFollowingViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager
): FollowingViewMvcImpl(inflater, parent, supportFragmentManager),
    FollowingSessionViewMvc.Listener {
    init {
        addTouchHelperToRecyclerView()
    }
    override fun buildAdapter(
        inflater: LayoutInflater,
        supportFragmentManager: FragmentManager
    ): SessionsRecyclerAdapter<FixedSessionViewMvc.Listener> {
        return ReorderingFollowingRecyclerAdapter(
            mRecyclerSessions,
            inflater,
            this,
            supportFragmentManager
        )
    }

    // Below method attaches touch helper to our Reordering Recycler View
    private fun addTouchHelperToRecyclerView() {
        if (mAdapter is ItemTouchHelperAdapter) {
            val itemTouchCallback = FollowingSessionReorderingTouchHelperCallback(mAdapter)
            val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
            mAdapter.mItemTouchHelper = itemTouchHelper
            itemTouchHelper.attachToRecyclerView(mRecyclerSessions)
        }
    }
}
