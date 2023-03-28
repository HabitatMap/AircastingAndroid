package pl.llp.aircasting.ui.view.screens.dashboard.reordering_following

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ItemTouchHelper
import pl.llp.aircasting.data.local.entity.SessionWithStreamsAndMeasurementsDBObject
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsRecyclerAdapter
import pl.llp.aircasting.ui.view.screens.dashboard.fixed.FixedSessionViewMvc
import pl.llp.aircasting.ui.view.screens.dashboard.following.FollowingSessionViewMvc
import pl.llp.aircasting.ui.view.screens.dashboard.following.FollowingViewMvcImpl
import pl.llp.aircasting.util.FollowingSessionReorderingTouchHelperCallback
import pl.llp.aircasting.util.ItemTouchHelperAdapter

class ReorderingFollowingViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager,
    reloadSessionCallback: suspend (uuid: String) -> SessionWithStreamsAndMeasurementsDBObject?,
    private val sessionDismissCallback: (session: Session) -> Unit,
    private val sessionUpdateFollowedAtCallback: (session: Session) -> Unit,
) : FollowingViewMvcImpl(inflater, parent, supportFragmentManager, reloadSessionCallback),
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
            supportFragmentManager,
            reloadSessionCallback,
            sessionDismissCallback,
            sessionUpdateFollowedAtCallback
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
