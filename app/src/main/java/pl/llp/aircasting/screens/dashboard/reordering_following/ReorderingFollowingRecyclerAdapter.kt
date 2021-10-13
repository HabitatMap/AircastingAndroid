package pl.llp.aircasting.screens.dashboard.reordering_following

import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_sessions_tab.view.*
import kotlinx.android.synthetic.main.session_card.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.database.DatabaseProvider
import pl.llp.aircasting.lib.ItemTouchHelperAdapter
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.screens.dashboard.SessionCardListener
import pl.llp.aircasting.screens.dashboard.SessionViewMvc
import pl.llp.aircasting.screens.dashboard.following.FollowingRecyclerAdapter
import pl.llp.aircasting.screens.dashboard.following.FollowingSessionViewMvcImpl
import java.util.*

class ReorderingFollowingRecyclerAdapter (
    private val mInflater: LayoutInflater,
    private val mListener: SessionCardListener,
    supportFragmentManager: FragmentManager
):  FollowingRecyclerAdapter(mInflater, mListener, supportFragmentManager),
    ItemTouchHelperAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val viewMvc =
            ReorderingFollowingSessionViewMvcImpl(
                mInflater,
                parent,
                supportFragmentManager
            )

        viewMvc.registerListener(mListener)
        val myReorderingViewHolder = MyViewHolder(viewMvc)
        myReorderingViewHolder.itemView.findViewById<ImageView>(R.id.reorder_session_button).setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_DOWN)
            mItemTouchHelper.startDrag(myReorderingViewHolder)
            true
        }

        return myReorderingViewHolder
    }

    override fun prepareSession(session: Session, expanded: Boolean): Session {
        // We only have to reload measurements for fixed tab for expanded sessions. Followed sessions have measurements fetched anyway
        return session
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(mSessionUUIDS, i, i + 1)
                DatabaseProvider.runQuery {
                    mSessionsViewModel.updateOrder(mSessionUUIDS[i+1], i+1)
                    mSessionsViewModel.updateOrder(mSessionUUIDS[i], i)
                }
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(mSessionUUIDS, i, i - 1)
                DatabaseProvider.runQuery {
                    mSessionsViewModel.updateOrder(mSessionUUIDS[i-1], i-1)
                    mSessionsViewModel.updateOrder(mSessionUUIDS[i], i)
                }
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onItemDismiss(position: Int) {
        mSessionUUIDS.removeAt(position)

        for (session in mSessionUUIDS) {
            DatabaseProvider.runQuery {
                mSessionsViewModel.updateOrder(session, mSessionUUIDS.indexOf(session)) //todo: not sure if thats correct
            }
        }
        removeObsoleteSessions()
        notifyItemRemoved(position)
    }

    override fun removeObsoleteSessions() {
        mSessionPresenters.keys
            .filter { uuid -> !mSessionUUIDS.contains(uuid) }
            .forEach { uuid ->
                val sessionPresenter = mSessionPresenters[uuid]
                sessionPresenter?.session?.unfollow()
                if (sessionPresenter?.session != null) {
                    DatabaseProvider.runQuery {
                        mSessionsViewModel.updateFollowedAt(sessionPresenter.session!!)
                    }
                }
                mSessionPresenters.remove(uuid)
            }
    }

}
