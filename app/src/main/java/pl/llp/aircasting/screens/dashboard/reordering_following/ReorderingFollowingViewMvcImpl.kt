package pl.llp.aircasting.screens.dashboard.reordering_following

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ItemTouchHelper
import pl.llp.aircasting.lib.FollowingSessionReorderingTouchHelperCallback
import pl.llp.aircasting.lib.ItemTouchHelperAdapter
import pl.llp.aircasting.screens.dashboard.SessionCardListener
import pl.llp.aircasting.screens.dashboard.SessionsRecyclerAdapter
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

    override fun addTouchHelperToRecyclerView() {
        if (mAdapter is ItemTouchHelperAdapter) {
            val itemTouchCallback = FollowingSessionReorderingTouchHelperCallback(mAdapter)
            val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
            mAdapter.mItemTouchHelper = itemTouchHelper
            itemTouchHelper.attachToRecyclerView(mRecyclerSessions)
        }
    }


}
