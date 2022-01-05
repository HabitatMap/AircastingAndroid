package pl.llp.aircasting.screens.dashboard.reordering_following

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.database.DatabaseProvider
import pl.llp.aircasting.lib.ItemTouchHelperAdapter
import pl.llp.aircasting.screens.dashboard.SessionCardListener
import pl.llp.aircasting.screens.dashboard.following.FollowingRecyclerAdapter
<<<<<<< HEAD

=======
>>>>>>> ed7f3f27 (rebase 6)
import java.util.*

class ReorderingFollowingRecyclerAdapter (
    private val mInflater: LayoutInflater,
    private val mListener: SessionCardListener,
    supportFragmentManager: FragmentManager
):  FollowingRecyclerAdapter(mInflater, mListener, supportFragmentManager),
    ItemTouchHelperAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val viewMvc = ReorderingFollowingSessionViewMvcImpl(mInflater, parent, supportFragmentManager)

        viewMvc.registerListener(mListener)
        val myReorderingViewHolder = MyViewHolder(viewMvc)
        myReorderingViewHolder.itemView.findViewById<ImageView>(R.id.reorder_session_button).setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_DOWN)
            mItemTouchHelper.startDrag(myReorderingViewHolder)
            true
        }

        return myReorderingViewHolder
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                updateSessionsOrder(i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                updateSessionsOrder(i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    private fun updateSessionsOrder(firstPosition: Int, secondPosition: Int) {
        Collections.swap(mSessionUUIDS, firstPosition, secondPosition)
        DatabaseProvider.runQuery {
            mSessionsViewModel.updateOrder(mSessionUUIDS[secondPosition], secondPosition)
            mSessionsViewModel.updateOrder(mSessionUUIDS[firstPosition], firstPosition)
        }
    }

    override fun onItemDismiss(position: Int) {
        mSessionUUIDS.removeAt(position)

        for (session in mSessionUUIDS) {
            DatabaseProvider.runQuery {
                mSessionsViewModel.updateOrder(session, mSessionUUIDS.indexOf(session))

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
